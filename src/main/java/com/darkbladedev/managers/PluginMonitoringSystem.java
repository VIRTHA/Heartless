package com.darkbladedev.managers;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.MM;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sistema de monitoreo que detecta las causas de desactivación del plugin
 * y determina automáticamente las acciones apropiadas para los eventos activos.
 */
public class PluginMonitoringSystem implements Listener {
    
    private final HeartlessMain plugin;
    private final Logger logger;
    private final WeeklyEventManager eventManager;
    
    // Estado del sistema de monitoreo
    private final AtomicBoolean isMonitoring = new AtomicBoolean(false);
    private final AtomicBoolean shutdownDetected = new AtomicBoolean(false);
    
    // Tareas de monitoreo
    private BukkitTask healthCheckTask;
    private BukkitTask memoryMonitorTask;
    
    // Registro de incidentes
    private final Map<String, IncidentRecord> incidents = new ConcurrentHashMap<>();
    private final AtomicInteger incidentCounter = new AtomicInteger(0);
    
    // Configuración de monitoreo
    private static final int HEALTH_CHECK_INTERVAL = 20; // 1 segundo
    private static final int MEMORY_CHECK_INTERVAL = 100; // 5 segundos
    private static final double CRITICAL_MEMORY_THRESHOLD = 0.9; // 90%
    private static final long RESPONSE_TIMEOUT_MS = 5000; // 5 segundos
    
    public PluginMonitoringSystem(HeartlessMain plugin, WeeklyEventManager eventManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.eventManager = eventManager;
    }
    
    /**
     * Inicia el sistema de monitoreo.
     */
    public void startMonitoring() {
        if (isMonitoring.compareAndSet(false, true)) {
            // Registrar eventos
            Bukkit.getPluginManager().registerEvents(this, plugin);
            
            // Iniciar tareas de monitoreo
            startHealthCheckTask();
            startMemoryMonitorTask();
            
            // Registrar shutdown hook
            registerShutdownHook();
            
            logger.info("Sistema de monitoreo iniciado correctamente");
            recordIncident("MONITORING_STARTED", "Sistema de monitoreo iniciado", DisconnectionCause.SYSTEM_START);
        }
    }
    
    /**
     * Detiene el sistema de monitoreo.
     */
    public void stopMonitoring() {
        if (isMonitoring.compareAndSet(true, false)) {
            // Cancelar tareas
            if (healthCheckTask != null) {
                healthCheckTask.cancel();
            }
            if (memoryMonitorTask != null) {
                memoryMonitorTask.cancel();
            }
            
            recordIncident("MONITORING_STOPPED", "Sistema de monitoreo detenido", DisconnectionCause.MANUAL_STOP);
            logger.info("Sistema de monitoreo detenido");
        }
    }
    
    /**
     * Inicia la tarea de verificación de salud del plugin.
     */
    private void startHealthCheckTask() {
        healthCheckTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, 
            this::performHealthCheck, 0L, HEALTH_CHECK_INTERVAL);
    }
    
    /**
     * Inicia la tarea de monitoreo de memoria.
     */
    private void startMemoryMonitorTask() {
        memoryMonitorTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, 
            this::checkMemoryUsage, 0L, MEMORY_CHECK_INTERVAL);
    }
    
    /**
     * Realiza una verificación de salud del plugin.
     */
    private void performHealthCheck() {
        try {
            // Verificar si el plugin sigue habilitado
            if (!plugin.isEnabled()) {
                handlePluginDisconnection(DisconnectionCause.PLUGIN_DISABLED, "Plugin deshabilitado");
                return;
            }
            
            // Verificar si el servidor está respondiendo
            long startTime = System.currentTimeMillis();
            
            // Ejecutar una tarea síncrona simple para verificar respuesta
            checkServerResponseTime(startTime);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error en verificación de salud", e);
            handlePluginDisconnection(DisconnectionCause.PLUGIN_ERROR, "Error en health check: " + e.getMessage());
        }
    }
    
    /**
     * Verifica el tiempo de respuesta del servidor.
     */
    private void checkServerResponseTime(long startTime) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            long responseTime = System.currentTimeMillis() - startTime;
            if (responseTime > RESPONSE_TIMEOUT_MS) {
                handlePluginDisconnection(DisconnectionCause.SERVER_LAG, 
                        "Tiempo de respuesta excesivo: " + responseTime + "ms");
            }
        });
    }
    
    /**
     * Verifica el uso de memoria del servidor.
     */
    private void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsage = (double) usedMemory / maxMemory;
        
        if (memoryUsage > CRITICAL_MEMORY_THRESHOLD) {
            String message = String.format("Uso crítico de memoria: %.1f%% (%d MB / %d MB)", 
                    memoryUsage * 100, usedMemory / 1024 / 1024, maxMemory / 1024 / 1024);
            
            handlePluginDisconnection(DisconnectionCause.MEMORY_CRITICAL, message);
        }
    }
    
    /**
     * Registra un shutdown hook para detectar cierre del servidor.
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (isMonitoring.get()) {
                shutdownDetected.set(true);
                handlePluginDisconnection(DisconnectionCause.SERVER_SHUTDOWN, "Cierre del servidor detectado");
            }
        }));
    }
    
    /**
     * Maneja la desconexión del plugin y determina la acción apropiada.
     */
    private void handlePluginDisconnection(DisconnectionCause cause, String details) {
        logger.warning("Desconexión detectada - Causa: " + cause + ", Detalles: " + details);
        
        // Registrar el incidente
        recordIncident("DISCONNECTION_" + cause.name(), details, cause);
        
        // Determinar acción basada en la causa
        EventAction action = determineEventAction(cause);
        
        // Ejecutar acción en el hilo principal usando un método separado
        executeEventActionAsync(action, cause, details);
    }
    
    /**
     * Ejecuta la acción de evento de forma asíncrona en el hilo principal.
     */
    private void executeEventActionAsync(EventAction action, DisconnectionCause cause, String details) {
        Bukkit.getScheduler().runTask(plugin, () -> executeEventAction(action, cause, details));
    }
    
    /**
     * Determina la acción apropiada basada en la causa de desconexión.
     */
    private EventAction determineEventAction(DisconnectionCause cause) {
        return switch (cause) {
            case SERVER_SHUTDOWN, SERVER_RESTART -> {
                // Reinicio programado - pausar para reanudar después
                logger.info("[MONITOREO] Causa detectada: " + cause + " - Pausando evento para reanudación");
                yield EventAction.PAUSE_EVENT;
            }
            case PLUGIN_DISABLED, PLUGIN_RELOAD -> {
                // Deshabilitación o recarga - pausar para reanudar después
                logger.info("[MONITOREO] Causa detectada: " + cause + " - Pausando evento para reanudación");
                yield EventAction.PAUSE_EVENT;
            }
            case PLUGIN_ERROR -> {
                // Error del plugin - detener por seguridad
                logger.warning("[MONITOREO] Error del plugin detectado - Deteniendo evento por seguridad");
                yield EventAction.STOP_EVENT;
            }
            case MEMORY_CRITICAL, SERVER_LAG -> {
                // Problemas de rendimiento - pausar temporalmente
                logger.warning("[MONITOREO] Problema de rendimiento detectado: " + cause + " - Pausando evento temporalmente");
                yield EventAction.PAUSE_EVENT;
            }
            case MANUAL_STOP, SYSTEM_START -> {
                // Acciones manuales o inicio - no requieren acción automática
                logger.info("[MONITOREO] Acción manual/inicio detectada: " + cause + " - Sin acción requerida");
                yield EventAction.NO_ACTION;
            }
            case CRITICAL_ERROR, UNKNOWN -> {
                // Errores críticos o desconocidos - detener por precaución
                logger.warning("[MONITOREO] Causa crítica/desconocida detectada: " + cause + " - Deteniendo evento por precaución");
                yield EventAction.STOP_EVENT;
            }
        };
    }
    
    /**
     * Ejecuta la acción determinada en los eventos activos.
     */
    private void executeEventAction(EventAction action, DisconnectionCause cause, String details) {
        try {
            if (!eventManager.isEventActive()) {
                logger.info("[MONITOREO] No hay evento activo, no se requiere acción");
                return;
            }
            
            String eventName = eventManager.getCurrentEventType() != null ? 
                eventManager.getCurrentEventType().getEventName() : "Desconocido";
            
            switch (action) {
                case PAUSE_EVENT -> {
                    if (eventManager.pauseCurrentEvent()) {
                        logger.info("[MONITOREO] Evento '" + eventName + "' pausado automáticamente debido a: " + cause);
                        
                        // Anuncio a los jugadores
                        Bukkit.broadcast(MM.toComponent(
                            "<yellow>⚠ El evento ha sido pausado automáticamente debido a: " + 
                            getCauseDisplayName(cause) + "</yellow>"
                        ), "heartless.admin");
                        
                        Bukkit.broadcast(MM.toComponent(
                            "<gray>El evento se reanudará automáticamente cuando sea posible.</gray>"
                        ), "heartless.admin");
                        
                        recordIncident("EVENTS_PAUSED", "Evento '" + eventName + "' pausado por " + cause, cause);
                    } else {
                        logger.warning("[MONITOREO] No se pudo pausar el evento '" + eventName + "'");
                        // Si no se puede pausar, intentar detener por seguridad
                        eventManager.forceStopCurrentEvent();
                        logger.info("[MONITOREO] Evento '" + eventName + "' detenido como medida de seguridad");
                        recordIncident("EVENTS_FORCE_STOPPED", "Evento '" + eventName + "' detenido por seguridad tras fallo de pausa", cause);
                    }
                }
                case STOP_EVENT -> {
                    eventManager.forceStopCurrentEvent();
                    logger.warning("[MONITOREO] Evento '" + eventName + "' detenido automáticamente debido a: " + cause);
                    
                    // Anuncio a los jugadores
                    Bukkit.broadcast(MM.toComponent(
                        "<red>⚠ El evento ha sido detenido automáticamente debido a: " + 
                        getCauseDisplayName(cause) + "</red>"
                    ), "heartless.admin");
                    
                    Bukkit.broadcast(MM.toComponent(
                        "<gray>Se iniciará un nuevo evento según la programación normal.</gray>"
                    ), "heartless.admin");
                    
                    recordIncident("EVENTS_STOPPED", "Evento '" + eventName + "' detenido por " + cause, cause);
                }
                case NO_ACTION -> {
                    logger.info("[MONITOREO] No se requiere acción para los eventos: " + cause);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error ejecutando acción de evento", e);
            recordIncident("ACTION_ERROR", "Error ejecutando acción: " + e.getMessage(), DisconnectionCause.CRITICAL_ERROR);
        }
    }
    
    /**
     * Obtiene el nombre de visualización para una causa de desconexión.
     */
    private String getCauseDisplayName(DisconnectionCause cause) {
        return switch (cause) {
            case SERVER_SHUTDOWN -> "cierre del servidor";
            case SERVER_RESTART -> "reinicio del servidor";
            case PLUGIN_DISABLED -> "deshabilitación del plugin";
            case PLUGIN_ERROR -> "error del plugin";
            case PLUGIN_RELOAD -> "recarga del plugin";
            case MEMORY_CRITICAL -> "memoria crítica";
            case SERVER_LAG -> "lag del servidor";
            case MANUAL_STOP -> "parada manual";
            case SYSTEM_START -> "inicio del sistema";
            case CRITICAL_ERROR -> "error crítico";
            case UNKNOWN -> "causa desconocida";
        };
    }
    
    /**
     * Registra un incidente en el sistema.
     */
    private void recordIncident(String type, String description, DisconnectionCause cause) {
        String incidentId = "INC_" + incidentCounter.incrementAndGet() + "_" + System.currentTimeMillis();
        IncidentRecord incident = new IncidentRecord(incidentId, type, description, cause);
        
        incidents.put(incidentId, incident);
        
        // Escribir a archivo de log
        writeIncidentToFile(incident);
        
        // Limpiar incidentes antiguos (mantener solo los últimos 100)
        if (incidents.size() > 100) {
            cleanupOldIncidents();
        }
    }
    
    /**
     * Escribe un incidente al archivo de log.
     */
    private void writeIncidentToFile(IncidentRecord incident) {
        try {
            File logDir = new File(plugin.getDataFolder(), "monitoring");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            File logFile = new File(logDir, "incidents.log");
            
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(incident.toLogString() + "\n");
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error escribiendo incidente a archivo", e);
        }
    }
    
    /**
     * Limpia incidentes antiguos para evitar uso excesivo de memoria.
     */
    private void cleanupOldIncidents() {
        List<String> sortedKeys = new ArrayList<>(incidents.keySet());
        sortedKeys.sort(Comparator.comparing(key -> incidents.get(key).getTimestamp()));
        
        // Remover los más antiguos
        int toRemove = incidents.size() - 50;
        for (int i = 0; i < toRemove; i++) {
            incidents.remove(sortedKeys.get(i));
        }
    }
    
    /**
     * Obtiene el historial de incidentes.
     */
    public Map<String, IncidentRecord> getIncidentHistory() {
        return new HashMap<>(incidents);
    }
    
    // Event Handlers
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(plugin)) {
            handlePluginDisconnection(DisconnectionCause.PLUGIN_DISABLED, "Plugin deshabilitado por el servidor");
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand().toLowerCase();
        
        if (command.startsWith("stop") || command.startsWith("shutdown")) {
            handlePluginDisconnection(DisconnectionCause.SERVER_SHUTDOWN, "Comando de cierre ejecutado: " + command);
        } else if (command.startsWith("restart")) {
            handlePluginDisconnection(DisconnectionCause.SERVER_RESTART, "Comando de reinicio ejecutado: " + command);
        } else if (command.startsWith("reload") || command.contains("plugman")) {
            handlePluginDisconnection(DisconnectionCause.PLUGIN_RELOAD, "Comando de recarga ejecutado: " + command);
        }
    }
    
    // Enums y clases internas
    
    public enum DisconnectionCause {
        SERVER_SHUTDOWN,
        SERVER_RESTART,
        PLUGIN_DISABLED,
        PLUGIN_ERROR,
        PLUGIN_RELOAD,
        MEMORY_CRITICAL,
        SERVER_LAG,
        MANUAL_STOP,
        SYSTEM_START,
        CRITICAL_ERROR,
        UNKNOWN
    }
    
    public enum EventAction {
        PAUSE_EVENT,
        STOP_EVENT,
        NO_ACTION
    }
    
    /**
     * Registro de un incidente del sistema.
     */
    public static class IncidentRecord {
        private final String id;
        private final String type;
        private final String description;
        private final DisconnectionCause cause;
        private final LocalDateTime timestamp;
        
        public IncidentRecord(String id, String type, String description, DisconnectionCause cause) {
            this.id = id;
            this.type = type;
            this.description = description;
            this.cause = cause;
            this.timestamp = LocalDateTime.now();
        }
        
        public String toLogString() {
            return String.format("[%s] %s | %s | %s | %s", 
                    timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    id, type, cause, description);
        }
        
        // Getters
        public String getId() { return id; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public DisconnectionCause getCause() { return cause; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}