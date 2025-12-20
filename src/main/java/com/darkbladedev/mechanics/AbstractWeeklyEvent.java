package com.darkbladedev.mechanics;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.challenges.Reward;
import com.darkbladedev.content.custom.CustomEnchantments;
import com.darkbladedev.events.ChallengeProgressUpdateEvent;
import com.darkbladedev.managers.PlayerStatisticsReportManager;
import com.darkbladedev.managers.UnifiedEventReportManager;
import com.darkbladedev.managers.ConfigManager;
import com.darkbladedev.persistence.EventDataPersistenceManager;
import com.darkbladedev.utils.MM;
import com.darkbladedev.models.TimeExpression;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

/**
 * Clase abstracta base que proporciona funcionalidades comunes para todos los eventos semanales.
 * 
 * Esta clase implementa el patrón Template Method para estandarizar el comportamiento
 * de los eventos semanales mientras permite personalización específica.
 * 
 * Funcionalidades proporcionadas:
 * - Sistema de desafíos y logros unificado
 * - Persistencia automática de datos específicos del evento
 * - Validaciones de estado robustas
 * - Gestión de estadísticas de jugadores
 * - Manejo de errores y recuperación automática
 * - Thread-safety completo
 * 
 * @author DarkBladeDev
 * @version 2.0
 * @since 1.0
 */
public abstract class AbstractWeeklyEvent extends WeeklyEvent {
    
    // === CONSTANTES DEL SISTEMA ===
    protected static final long CHALLENGE_CHECK_INTERVAL = 30 * 20L; // 30 segundos
    protected static final long STATISTICS_UPDATE_INTERVAL = 60 * 20L; // 1 minuto
    protected static final long DATA_PERSISTENCE_INTERVAL = 5 * 60 * 20L; // 5 minutos
    protected static final int MAX_RETRIES = 3;
    
    // === GESTIÓN DE DESAFÍOS ===
    protected final Map<String, ChallengeDefinition> availableChallenges = new ConcurrentHashMap<>();
    protected final Map<UUID, Set<String>> completedChallenges = new ConcurrentHashMap<>();
    protected final Map<UUID, Map<String, Object>> challengeProgress = new ConcurrentHashMap<>();
    protected final Map<UUID, Long> lastChallengeCheck = new ConcurrentHashMap<>();
    
    // === GESTIÓN POR MUNDO ===
    protected final Map<String, Set<UUID>> worldActivePlayers = new ConcurrentHashMap<>();
    protected final Map<String, AtomicBoolean> worldEventStatus = new ConcurrentHashMap<>();
    protected final Map<String, Long> worldStartTimes = new ConcurrentHashMap<>();
    protected final Set<String> activeWorlds = ConcurrentHashMap.newKeySet();
    
    // === SINCRONIZACIÓN DE DATOS ===
    protected final ReadWriteLock dataLock = new ReentrantReadWriteLock();
    
    // === ESTADÍSTICAS DEL EVENTO ===
    protected final Map<UUID, Map<String, Object>> playerStatistics = new ConcurrentHashMap<>();
    protected final Map<String, AtomicLong> globalStatistics = new ConcurrentHashMap<>();
    protected final AtomicLong totalParticipants = new AtomicLong(0);
    protected final AtomicLong totalChallengesCompleted = new AtomicLong(0);
    
    // === PERSISTENCIA DE DATOS ===
    protected final Map<String, Object> eventSpecificData = new ConcurrentHashMap<>();
    protected final AtomicBoolean dataDirty = new AtomicBoolean(false);
    protected final AtomicLong lastDataSave = new AtomicLong(0);
    
    // === GESTIÓN DE TAREAS ===
    private BukkitTask challengeTask;
    private BukkitTask statisticsTask;
    private BukkitTask persistenceTask;
    
    // === SISTEMA DE REPORTES ===
    protected final PlayerStatisticsReportManager reportManager;
    protected final UnifiedEventReportManager unifiedReportManager;
    
    // === CONFIGURACIÓN DEL SISTEMA ===
    protected final AtomicBoolean challengeSystemEnabled = new AtomicBoolean(true);
    protected final AtomicBoolean statisticsEnabled = new AtomicBoolean(true);
    protected final AtomicBoolean autoSaveEnabled = new AtomicBoolean(true);
    
    /**
     * Constructor base para eventos semanales abstractos.
     * 
     * @param plugin El plugin principal
     * @param duration Duración del evento
     */
    public AbstractWeeklyEvent(HeartlessMain plugin, TimeExpression duration) {
        super(plugin, duration);
        this.reportManager = new PlayerStatisticsReportManager(plugin);
        this.unifiedReportManager = new UnifiedEventReportManager(plugin);
        initializeEventSystems();
    }
    
    /**
     * Inicia el evento solo en mundos que no están en la blacklist.
     * Este método es similar a start() pero filtra los mundos excluidos.
     */
    public final void startInNonExcludedWorlds() {
        try {
            globalStatistics.get("event_starts").incrementAndGet();
            
            // Inicializar eventos solo en mundos no excluidos
            initializeWorldEvents();
            
            // Inicializar datos específicos del evento
            initializeEventSpecificData();
            
            // Iniciar tareas del sistema abstracto
            startAbstractEventTasks();
            
            // Llamar al método específico del evento
            onEventStart();
            
            // Iniciar tareas específicas del evento
            super.start();
            
            logger.info("[" + getId() + "] Evento iniciado correctamente en " + 
                       activeWorlds.size() + " mundos no excluidos con " + getActivePlayerCount() + " jugadores");
            
        } catch (Exception e) {
            globalStatistics.get("total_errors").incrementAndGet();
            logger.log(Level.SEVERE, "[" + getId() + "] Error al iniciar el evento en mundos no excluidos", e);
            handleEventError("startInNonExcludedWorlds", e);
        }
    }
    
    /**
     * Inicializa los sistemas internos del evento abstracto.
     */
    private void initializeEventSystems() {
        // Inicializar estadísticas globales básicas
        globalStatistics.put("event_starts", new AtomicLong(0));
        globalStatistics.put("event_stops", new AtomicLong(0));
        globalStatistics.put("total_errors", new AtomicLong(0));
        globalStatistics.put("data_saves", new AtomicLong(0));
        globalStatistics.put("successful_saves", new AtomicLong(0));
        globalStatistics.put("failed_saves", new AtomicLong(0));
        
        // Configurar desafíos básicos si están habilitados
        if (challengeSystemEnabled.get()) {
            setupBasicChallenges();
        }
        
        logger.info("[" + getId() + "] Sistemas del evento abstracto inicializados correctamente");
    }
    
    /**
     * Configura los desafíos básicos comunes a todos los eventos.
     * Los eventos específicos deben sobrescribir este método para añadir sus desafíos personalizados.
     * Este método base no registra desafíos genéricos.
     */
    protected void setupBasicChallenges() {
        // Los eventos específicos deben implementar sus propios desafíos
        // No se registran desafíos genéricos aquí
    }
    
    // === TEMPLATE METHODS ===
    
    @Override
    public final void start() {
        try {
            globalStatistics.get("event_starts").incrementAndGet();
            
            // Inicializar eventos por mundo individual
            initializeWorldEvents();
            
            // Inicializar datos específicos del evento
            initializeEventSpecificData();
            
            // Iniciar tareas del sistema abstracto
            startAbstractEventTasks();
            
            // Llamar al método específico del evento
            onEventStart();
            
            // Iniciar tareas específicas del evento
            super.start();
            
            logger.info("[" + getId() + "] Evento iniciado correctamente en " + 
                       activeWorlds.size() + " mundos con " + getActivePlayerCount() + " jugadores");
            
        } catch (Exception e) {
            globalStatistics.get("total_errors").incrementAndGet();
            logger.log(Level.SEVERE, "[" + getId() + "] Error al iniciar el evento", e);
            handleEventError("start", e);
        }
    }
    
    /**
     * Inicia el evento solo en un mundo específico.
     * Este método ejecuta todas las inicializaciones necesarias pero solo para el mundo especificado.
     * 
     * @param world El mundo donde inicializar el evento
     */
    public final void startInSpecificWorld(World world) {
        try {
            globalStatistics.get("event_starts").incrementAndGet();
            
            // Marcar el evento como activo para permitir las tareas del sistema
            isActive.set(true);
            isPaused.set(false);
            
            // Asegurar que los eventos estén registrados
            ensureEventHandlersRegistered();
            
            // Inicializar el evento solo en el mundo específico
            initializeWorldEvent(world);
            
            // Inicializar datos específicos del evento
            initializeEventSpecificData();
            
            // Inicializar tareas del sistema (auto-guardado y limpieza)
            initializeSystemTasks();
            
            // Iniciar tareas del sistema abstracto
            startAbstractEventTasks();
            
            // Llamar al método específico del evento
            onEventStart();
            
            // Iniciar tareas específicas del evento
            startEventTasks();
            
            // Anunciar el inicio del evento
            announceEventStart();
            
            // Establecer tiempo de inicio
            startTime.set(System.currentTimeMillis());
            
            logger.info("[" + getId() + "] Evento iniciado correctamente en el mundo específico: " + 
                       world.getName() + " con " + world.getPlayers().size() + " jugadores");
            
        } catch (Exception e) {
            globalStatistics.get("total_errors").incrementAndGet();
            logger.log(Level.SEVERE, "[" + getId() + "] Error al iniciar el evento en mundo específico: " + world.getName(), e);
            handleEventError("startInSpecificWorld", e);
        }
    }
    
    @Override
    public final void stop() {
        try {
            globalStatistics.get("event_stops").incrementAndGet();
            
            // Detener tareas del sistema abstracto
            stopAbstractEventTasks();
            
            // Guardar datos finales
            if (autoSaveEnabled.get()) {
                saveEventSpecificData();
            }
            
            // Procesar estadísticas finales
            processFinalStatistics();
            
            // Anunciar el fin del evento
            announceEventEnd();
            
            // Llamar al método específico del evento
            onEventStop();
            
            // Detener el evento base
            super.stop();
            
            logger.info("[" + getId() + "] Evento detenido correctamente");
            
        } catch (Exception e) {
            globalStatistics.get("total_errors").incrementAndGet();
            logger.log(Level.SEVERE, "[" + getId() + "] Error al detener el evento", e);
            handleEventError("stop", e);
        }
    }
    
    // === MÉTODOS ABSTRACTOS PARA IMPLEMENTACIÓN ESPECÍFICA ===
    
    /**
     * Llamado cuando el evento se inicia. Los eventos específicos deben implementar
     * su lógica de inicialización aquí.
     */
    protected abstract void onEventStart();
    
    /**
     * Llamado cuando el evento se detiene. Los eventos específicos deben implementar
     * su lógica de limpieza aquí.
     */
    protected abstract void onEventStop();
    
    /**
     * Inicializa los datos específicos del evento.
     * Los eventos deben sobrescribir este método para configurar sus datos iniciales.
     */
    protected abstract void initializeEventSpecificData();
    
    /**
     * Guarda los datos específicos del evento.
     * Los eventos deben sobrescribir este método para persistir sus datos.
     */
    protected abstract void saveEventSpecificData();
    
    /**
     * Procesa las estadísticas específicas del evento.
     * Llamado periódicamente durante el evento.
     */
    protected abstract void processEventStatistics();
    
    // === IMPLEMENTACIÓN DE PERSISTENCIA ===
    
    /**
     * Implementación del método saveEventData para AbstractWeeklyEvent.
     * Este método coordina el guardado de todos los datos del evento.
     */
    @Override
    protected void saveEventData() {
        if (!isActive.get()) {
            return; // No guardar si el evento no está activo
        }
        
        try {
            // Marcar datos como "sucios" para forzar el guardado
            dataDirty.set(true);
            
            // Guardar datos específicos del evento
            saveEventSpecificData();
            
            // Usar EventDataPersistenceManager si está disponible
            EventDataPersistenceManager persistenceManager = HeartlessMain.getEventDataPersistenceManager();
            if (persistenceManager != null) {
                persistenceManager.saveEventDataAsync(this).thenAccept(success -> {
                    if (success) {
                        logger.fine("[" + getId() + "] Datos de evento guardados exitosamente via EventDataPersistenceManager");
                        globalStatistics.get("successful_saves").incrementAndGet();
                    } else {
                        logger.warning("[" + getId() + "] Error guardando datos via EventDataPersistenceManager");
                        globalStatistics.get("failed_saves").incrementAndGet();
                    }
                }).exceptionally(throwable -> {
                    logger.log(Level.WARNING, "[" + getId() + "] Excepción en guardado asíncrono", throwable);
                    globalStatistics.get("failed_saves").incrementAndGet();
                    return null;
                });
            }
            
            // Usar StorageManager como respaldo
            try {
                plugin.getStorageManager().saveEvent(this);
                logger.fine("[" + getId() + "] Datos guardados via StorageManager");
            } catch (Exception e) {
                logger.log(Level.WARNING, "[" + getId() + "] Error guardando via StorageManager", e);
            }
            
            // Actualizar tiempo de último guardado
            lastDataSave.set(System.currentTimeMillis());
            dataDirty.set(false);
            
        } catch (Exception e) {
            globalStatistics.get("total_errors").incrementAndGet();
            globalStatistics.get("failed_saves").incrementAndGet();
            logger.log(Level.SEVERE, "[" + getId() + "] Error crítico en saveEventData", e);
        }
    }
    
    // === GESTIÓN POR MUNDO ===
    
    /**
     * Inicializa el evento en todos los mundos disponibles, aplicando exclusiones.
     */
    protected final void initializeWorldEvents() {
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            
            // Verificar si el mundo está excluido
            if (isWorldExcluded(world)) {
                logger.info("[" + getId() + "] Mundo excluido del evento: " + worldName);
                continue;
            }
            
            // Inicializar el evento en este mundo
            initializeWorldEvent(world);
        }
        
        logger.info("[" + getId() + "] Evento inicializado en " + activeWorlds.size() + " mundos");
    }
    
    /**
     * Inicializa el evento en un mundo específico.
     * 
     * @param world El mundo donde inicializar el evento
     */
    public final void initializeWorldEvent(World world) {
        String worldName = world.getName();
        
        try {
            // Marcar el mundo como activo
            activeWorlds.add(worldName);
            worldEventStatus.put(worldName, new AtomicBoolean(true));
            worldStartTimes.put(worldName, System.currentTimeMillis());
            
            // Inicializar jugadores en este mundo
            Set<UUID> worldPlayers = new HashSet<>();
            for (Player player : world.getPlayers()) {
                UUID playerId = player.getUniqueId();
                worldPlayers.add(playerId);
                activePlayers.add(playerId);
                playerJoinTimes.put(playerId, System.currentTimeMillis());
                onPlayerJoinEvent(player);
            }
            
            worldActivePlayers.put(worldName, worldPlayers);
            
            // Llamar al hook específico del evento para este mundo
            onWorldEventStart(world);
            
            logger.info("[" + getId() + "] Evento iniciado en mundo: " + worldName + 
                       " con " + worldPlayers.size() + " jugadores");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[" + getId() + "] Error al inicializar evento en mundo: " + worldName, e);
            // Remover el mundo de la lista activa si falló la inicialización
            activeWorlds.remove(worldName);
            worldEventStatus.remove(worldName);
            worldStartTimes.remove(worldName);
            worldActivePlayers.remove(worldName);
        }
    }
    
    /**
     * Verifica si un mundo está excluido del evento.
     * 
     * @param world El mundo a verificar
     * @return true si el mundo está excluido
     */
    protected final boolean isWorldExcluded(World world) {
        ConfigManager configManager = plugin.getConfigManager();
        if (configManager == null) {
            return false;
        }
        
        List<String> excludedWorlds = configManager.getExcludedWorlds();
        return excludedWorlds != null && excludedWorlds.contains(world.getName());
    }
    
    /**
     * Verifica si un jugador está en un mundo excluido.
     * 
     * @param player El jugador a verificar
     * @return true si el jugador está en un mundo excluido
     */
    protected final boolean isPlayerInExcludedWorld(Player player) {
        return isWorldExcluded(player.getWorld());
    }
    
    /**
     * Obtiene el estado del evento en un mundo específico.
     * 
     * @param worldName Nombre del mundo
     * @return true si el evento está activo en ese mundo
     */
    protected final boolean isEventActiveInWorld(String worldName) {
        AtomicBoolean status = worldEventStatus.get(worldName);
        return status != null && status.get();
    }

    /**
     * Obtiene el conjunto de mundos donde el evento está activo
     * @return Set de nombres de mundos activos
     */
    public final Set<String> getActiveWorlds() {
        return new HashSet<>(activeWorlds);
    }
    
    /**
     * Detiene el evento en un mundo específico.
     * 
     * @param world El mundo donde detener el evento
     */
    public final void stopWorldEvent(World world) {
        String worldName = world.getName();
        
        if (!activeWorlds.contains(worldName)) {
            return;
        }
        
        try {
            // Marcar el mundo como inactivo
            AtomicBoolean status = worldEventStatus.get(worldName);
            if (status != null) {
                status.set(false);
            }
            
            // Limpiar jugadores de este mundo
            Set<UUID> worldPlayers = worldActivePlayers.get(worldName);
            if (worldPlayers != null) {
                for (UUID playerId : worldPlayers) {
                    activePlayers.remove(playerId);
                    playerJoinTimes.remove(playerId);
                }
            }
            
            // Llamar al hook específico del evento para este mundo
            onWorldEventStop(world);
            
            // Remover de las estructuras de datos
            activeWorlds.remove(worldName);
            worldEventStatus.remove(worldName);
            worldStartTimes.remove(worldName);
            worldActivePlayers.remove(worldName);
            
            logger.info("[" + getId() + "] Evento detenido en mundo: " + worldName);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[" + getId() + "] Error al detener evento en mundo: " + worldName, e);
        }
    }
    
    /**
     * Hook llamado cuando el evento se inicia en un mundo específico.
     * Los eventos pueden sobrescribir este método para lógica específica por mundo.
     * 
     * @param world El mundo donde se inicia el evento
     */
    protected void onWorldEventStart(World world) {
        // Implementación por defecto vacía
    }
    
    /**
     * Hook llamado cuando el evento se detiene en un mundo específico.
     * Los eventos pueden sobrescribir este método para lógica específica por mundo.
     * 
     * @param world El mundo donde se detiene el evento
     */
    protected void onWorldEventStop(World world) {
        // Implementación por defecto vacía
    }
    
    // === SISTEMA DE DESAFÍOS ===
    
    /**
     * Registra un nuevo desafío en el sistema.
     * 
     * @param challengeId ID único del desafío
     * @param definition Definición del desafío
     */
    protected final void registerChallenge(String challengeId, ChallengeDefinition definition) {
        if (challengeId == null || definition == null) {
            logger.warning("[" + getId() + "] Intento de registrar desafío nulo");
            return;
        }
        
        availableChallenges.put(challengeId, definition);
        logger.info("[" + getId() + "] Desafío registrado: " + challengeId);
    }
    
    /**
     * Verifica si un jugador ha completado un desafío específico.
     * 
     * @param playerId ID del jugador
     * @param challengeId ID del desafío
     * @return true si el desafío está completado
     */
    public final boolean hasChallengeCompleted(UUID playerId, String challengeId) {
        if (playerId == null || challengeId == null) return false;
        
        Set<String> playerChallenges = completedChallenges.get(playerId);
        return playerChallenges != null && playerChallenges.contains(challengeId);
    }
    
    /**
     * Marca un desafío como completado para un jugador.
     * 
     * @param playerId ID del jugador
     * @param challengeId ID del desafío
     */
    public final void completeChallengeForPlayer(UUID playerId, String challengeId) {
        if (playerId == null || challengeId == null) return;
        
        // Verificar si el desafío ya está completado para evitar duplicados
        Set<String> playerChallenges = completedChallenges.get(playerId);
        boolean wasAlreadyCompleted = playerChallenges != null && playerChallenges.contains(challengeId);
        
        if (wasAlreadyCompleted) {
            logger.fine("[" + getId() + "] Desafío " + challengeId + " ya estaba completado para " + playerId);
            return; // Ya completado, no hacer nada
        }
        
        // Agregar el desafío a la lista de completados
        completedChallenges.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                          .add(challengeId);
        
        totalChallengesCompleted.incrementAndGet();
        dataDirty.set(true);
        
        // Solo notificar y otorgar recompensas si no estaba completado previamente
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            ChallengeDefinition challenge = availableChallenges.get(challengeId);
            if (challenge != null) {
                notifyPlayerChallengeCompleted(player, challenge);
                // Las recompensas se otorgan automáticamente por el sistema de desafíos específico del evento
                // Eliminada la llamada duplicada: giveRewards(player, challenge.getRewards());
            }
            
            // Disparar evento de completación para difusión global
            try {
                com.darkbladedev.challenges.ChallengeDefinition challengeDefForEvent = 
                    new com.darkbladedev.challenges.ChallengeDefinition(
                        challengeId, 
                        challenge.getDisplayName(), 
                        challenge.getDescription(), 
                        challenge.getRequiredProgress(), 
                        challenge.getRewards()
                    );
                com.darkbladedev.events.ChallengeCompletedEvent completedEvent = 
                      new com.darkbladedev.events.ChallengeCompletedEvent(player, challengeId, challengeDefForEvent, this);
                Bukkit.getPluginManager().callEvent(completedEvent);
            } catch (Exception e) {
                logger.warning("Error al disparar ChallengeCompletedEvent: " + e.getMessage());
            }
        }
        
        logger.info("[" + getId() + "] Desafío completado: " + challengeId + " por " + playerId);
    }
    
    /**
     * Actualiza el progreso de un desafío para un jugador.
     * 
     * @param playerId ID del jugador
     * @param challengeId ID del desafío
     * @param progress Progreso actual
     * @param maxProgress Progreso máximo requerido
     */
    public final void updateChallengeProgress(UUID playerId, String challengeId, 
                                               Object progress, Object maxProgress) {
        if (playerId == null || challengeId == null) {
            return;
        }
        
        Map<String, Object> playerProgress = challengeProgress.computeIfAbsent(
            playerId, k -> new ConcurrentHashMap<>());
        
        // Obtener progreso anterior para comparar
        Object previousProgress = playerProgress.get(challengeId + "_current");
        
        playerProgress.put(challengeId + "_current", progress);
        playerProgress.put(challengeId + "_max", maxProgress);
        
        dataDirty.set(true);
        
        // Disparar evento de actualización de progreso si hay cambios
        if (!Objects.equals(previousProgress, progress)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // Verificar si el desafío se completó
                boolean isCompleted = false;
                if (progress instanceof Number && maxProgress instanceof Number) {
                    isCompleted = ((Number) progress).intValue() >= ((Number) maxProgress).intValue();
                }
                
                // Crear y disparar el evento personalizado
                ChallengeProgressUpdateEvent event = new ChallengeProgressUpdateEvent(
                    player, challengeId, progress, maxProgress, getId(), isCompleted
                );
                
                Bukkit.getPluginManager().callEvent(event);
            }
        }
    }
    
    /**
     * Obtiene todos los desafíos disponibles en este evento.
     * 
     * @return Mapa inmutable con los IDs de desafíos como claves y sus definiciones como valores
     */
    public final Map<String, ChallengeDefinition> getChallenges() {
        return Collections.unmodifiableMap(availableChallenges);
    }
    
    // === SISTEMA DE ESTADÍSTICAS ===
    
    /**
     * Actualiza una estadística específica para un jugador.
     * 
     * @param playerId ID del jugador
     * @param statistic Nombre de la estadística
     * @param value Valor a establecer
     */
    public final void updatePlayerStatistic(UUID playerId, String statistic, Object value) {
        if (playerId == null || statistic == null || value == null) return;
        
        playerStatistics.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                       .put(statistic, value);
        dataDirty.set(true);
    }
    
    /**
     * Incrementa una estadística numérica para un jugador.
     * 
     * @param playerId ID del jugador
     * @param statistic Nombre de la estadística
     * @param increment Cantidad a incrementar
     */
    protected final void incrementPlayerStatistic(UUID playerId, String statistic, long increment) {
        if (playerId == null || statistic == null) return;
        
        Map<String, Object> stats = playerStatistics.computeIfAbsent(
            playerId, k -> new ConcurrentHashMap<>());
        
        Object current = stats.get(statistic);
        long newValue = (current instanceof Number ? ((Number) current).longValue() : 0) + increment;
        stats.put(statistic, newValue);
        
        dataDirty.set(true);
    }
    
    /**
     * Obtiene una estadística específica de un jugador.
     * 
     * @param playerId ID del jugador
     * @param statistic Nombre de la estadística
     * @return Valor de la estadística o null si no existe
     */
    protected final Object getPlayerStatistic(UUID playerId, String statistic) {
        if (playerId == null || statistic == null) return null;
        
        Map<String, Object> stats = playerStatistics.get(playerId);
        return stats != null ? stats.get(statistic) : null;
    }
    
    /**
     * Actualiza una estadística global del evento.
     * 
     * @param statistic Nombre de la estadística
     * @param value Valor a establecer
     */
    public final void updateGlobalStatistic(String statistic, long value) {
        if (statistic == null) return;
        
        globalStatistics.computeIfAbsent(statistic, k -> new AtomicLong(0)).set(value);
        dataDirty.set(true);
    }
    
    /**
     * Incrementa una estadística global del evento.
     * 
     * @param statistic Nombre de la estadística
     * @param increment Cantidad a incrementar
     */
    protected final void incrementGlobalStatistic(String statistic, long increment) {
        if (statistic == null) return;
        
        globalStatistics.computeIfAbsent(statistic, k -> new AtomicLong(0))
                       .addAndGet(increment);
        dataDirty.set(true);
    }
    
    // === TAREAS DEL SISTEMA ABSTRACTO ===
    
    /**
     * Inicia las tareas del sistema abstracto.
     */
    private void startAbstractEventTasks() {
        if (challengeSystemEnabled.get()) {
            startChallengeCheckTask();
        }
        
        if (statisticsEnabled.get()) {
            startStatisticsTask();
        }
        
        if (autoSaveEnabled.get()) {
            startPersistenceTask();
        }
    }
    
    /**
     * Detiene las tareas del sistema abstracto.
     */
    private void stopAbstractEventTasks() {
        if (challengeTask != null && !challengeTask.isCancelled()) {
            challengeTask.cancel();
        }
        
        if (statisticsTask != null && !statisticsTask.isCancelled()) {
            statisticsTask.cancel();
        }
        
        if (persistenceTask != null && !persistenceTask.isCancelled()) {
            persistenceTask.cancel();
        }
    }
    
    /**
     * Inicia la tarea de verificación de desafíos.
     */
    private void startChallengeCheckTask() {
        challengeTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    processChallengeChecks();
                } catch (Exception e) {
                    globalStatistics.get("total_errors").incrementAndGet();
                    logger.log(Level.WARNING, "[" + getId() + "] Error en verificación de desafíos", e);
                }
            }
        }.runTaskTimerAsynchronously(plugin, CHALLENGE_CHECK_INTERVAL, CHALLENGE_CHECK_INTERVAL);
    }
    
    /**
     * Inicia la tarea de actualización de estadísticas.
     */
    private void startStatisticsTask() {
        statisticsTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    processEventStatistics();
                } catch (Exception e) {
                    globalStatistics.get("total_errors").incrementAndGet();
                    logger.log(Level.WARNING, "[" + getId() + "] Error en procesamiento de estadísticas", e);
                }
            }
        }.runTaskTimerAsynchronously(plugin, STATISTICS_UPDATE_INTERVAL, STATISTICS_UPDATE_INTERVAL);
    }
    
    /**
     * Inicia la tarea de persistencia automática.
     */
    private void startPersistenceTask() {
        persistenceTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (dataDirty.get() && 
                        System.currentTimeMillis() - lastDataSave.get() > DATA_PERSISTENCE_INTERVAL * 50) {
                        saveEventSpecificData();
                        globalStatistics.get("data_saves").incrementAndGet();
                        dataDirty.set(false);
                        lastDataSave.set(System.currentTimeMillis());
                    }
                } catch (Exception e) {
                    globalStatistics.get("total_errors").incrementAndGet();
                    logger.log(Level.WARNING, "[" + getId() + "] Error en persistencia automática", e);
                }
            }
        }.runTaskTimerAsynchronously(plugin, DATA_PERSISTENCE_INTERVAL, DATA_PERSISTENCE_INTERVAL);
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    /**
     * Procesa las verificaciones de desafíos para todos los jugadores activos.
     */
    private void processChallengeChecks() {
        long currentTime = System.currentTimeMillis();
        
        for (UUID playerId : getActivePlayers()) {
            Long lastCheck = lastChallengeCheck.get(playerId);
            if (lastCheck == null || currentTime - lastCheck > CHALLENGE_CHECK_INTERVAL * 50) {
                checkPlayerChallenges(playerId);
                lastChallengeCheck.put(playerId, currentTime);
            }
        }
    }
    
    /**
     * Verifica los desafíos específicos para un jugador.
     * Los eventos específicos pueden sobrescribir este método.
     * 
     * @param playerId ID del jugador
     */
    protected void checkPlayerChallenges(UUID playerId) {
        // Los eventos específicos deben implementar sus propias verificaciones de desafíos
        // No se verifican desafíos genéricos aquí
    }
    
    /**
     * Procesa las estadísticas finales del evento.
     */
    /**
     * Procesa las estadísticas finales del evento y genera reportes individuales.
     * Se ejecuta al finalizar el evento para mostrar estadísticas a cada jugador.
     */
    private void processFinalStatistics() {
        totalParticipants.set(getActivePlayers().size());
        
        // Los eventos específicos deben manejar sus propios desafíos de finalización
        // No se verifican desafíos genéricos aquí
        
        // Generar y enviar reportes individuales de estadísticas
        generateAndSendPlayerReports();
    }
    
    /**
     * Genera y envía reportes individuales de estadísticas a todos los jugadores participantes.
     * Utiliza el UnifiedEventReportManager para crear reportes unificados sin duplicación.
     */
    private void generateAndSendPlayerReports() {
        if (unifiedReportManager == null) {
            plugin.getLogger().warning("UnifiedReportManager no está inicializado para el evento " + getId());
            return;
        }

        // Usar el nuevo sistema unificado que elimina duplicación de mensajes
        unifiedReportManager.generateAndSendUnifiedEventReport(this);
    }
    
    /**
     * Notifica a un jugador que ha completado un desafío con formato MiniMessage y hover text.
     * 
     * @param player El jugador
     * @param challenge El desafío completado
     */
    private void notifyPlayerChallengeCompleted(Player player, ChallengeDefinition challenge) {
        UUID playerId = player.getUniqueId();
        
        // Obtener progreso actual y objetivo usando las claves correctas
        Map<String, Object> progress = challengeProgress.getOrDefault(playerId, new HashMap<>());
        Object currentProgress = progress.getOrDefault(challenge.getId() + "_current", challenge.getRequiredProgress());
        Object maxProgress = progress.getOrDefault(challenge.getId() + "_max", challenge.getRequiredProgress());
        
        // Crear el texto del hover con el progreso
        String hoverText = "<gray>Progreso: <white>" + currentProgress + "/" + maxProgress + "</white></gray>";
        Component hoverComponent = MM.toComponent(hoverText);
        
        // Crear el mensaje principal con hover en el nombre del desafío
        Component challengeNameWithHover = MM.toComponent("<yellow>" + challenge.getDisplayName() + "</yellow>")
                .hoverEvent(HoverEvent.showText(hoverComponent));
        
        // Mensaje completo
        Component fullMessage = MM.toComponent(prefix + " <green>Has completado el desafío </green>")
                .append(challengeNameWithHover)
                .append(MM.toComponent("<green>!</green>"));
        
        player.sendMessage(fullMessage);
        
        // Otorgar recompensas usando el nuevo sistema
        challenge.grantRewardsTo(player, getId());
    }
    
    /**
     * Otorga recompensas a un jugador usando el sistema legacy (compatibilidad hacia atrás).
     * 
     * @param player El jugador
     * @param rewards Lista de recompensas como strings
     * @deprecated Usar ChallengeDefinition.grantRewardsTo() en su lugar
     */
    @SuppressWarnings("unused")
    @Deprecated
    private void giveRewards(Player player, List<String> rewards) {
        if (rewards == null || rewards.isEmpty()) return;
        
        for (String reward : rewards) {
            giveReward(player, reward);
        }
    }
    
    /**
     * Otorga una recompensa específica a un jugador usando el sistema legacy.
     * 
     * @param player El jugador
     * @param reward La recompensa en formato "tipo:valor" o "tipo:item:cantidad"
     * @deprecated Usar objetos Reward en su lugar
     */
    @Deprecated
    protected void giveReward(Player player, String reward) {
        if (player == null || reward == null || reward.trim().isEmpty()) {
            return;
        }
        
        try {
            String[] parts = reward.split(":");
            logger.info("[" + getId() + "] Procesando recompensa: " + reward + " (partes: " + parts.length + ")");
            
            if (parts.length < 2) {
                logger.warning("[" + getId() + "] Formato de recompensa inválido: " + reward);
                return;
            }
            
            String type = parts[0].toLowerCase();
            
            switch (type) {
                case "experience":
                case "exp":
                    try {
                        int amount = Integer.parseInt(parts[1]);
                        player.giveExp(amount);
                        logger.info("[" + getId() + "] Experiencia otorgada: " + amount + " a " + player.getName());
                    } catch (NumberFormatException e) {
                        logger.warning("[" + getId() + "] Cantidad de experiencia inválida: " + parts[1]);
                    }
                    break;
                    
                case "item":
                    if (parts.length >= 3) {
                        try {
                            String itemName = parts[1].toUpperCase();
                            int quantity = Integer.parseInt(parts[2]);
                            
                            org.bukkit.Material material = org.bukkit.Material.valueOf(itemName);
                            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material, quantity);
                            
                            // Intentar agregar al inventario
                            java.util.HashMap<Integer, org.bukkit.inventory.ItemStack> leftover = player.getInventory().addItem(item);
                            
                            // Si hay items sobrantes, tirarlos al suelo
                            if (!leftover.isEmpty()) {
                                for (org.bukkit.inventory.ItemStack leftoverItem : leftover.values()) {
                                    player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
                                }
                            }
                            
                            logger.info("[" + getId() + "] Item otorgado: " + quantity + "x " + itemName + " a " + player.getName());
                        } catch (IllegalArgumentException e) {
                            if (e instanceof NumberFormatException) {
                                logger.warning("[" + getId() + "] Cantidad de item inválida: " + parts[2]);
                            } else {
                                logger.warning("[" + getId() + "] Material inválido: " + parts[1]);
                            }
                        }
                    } else {
                        logger.warning("[" + getId() + "] Formato de item inválido: " + reward);
                    }
                    break;
                    
                case "tag":
                    if (parts.length >= 2) {
                        String tagName = parts[1];
                        // Aquí se integraría con el sistema de tags del plugin
                        logger.info("[" + getId() + "] Tag otorgado: " + tagName + " a " + player.getName());
                    }
                    break;
                    
                case "enchant":
                    if (parts.length >= 3) {
                        try {
                            String enchantName = parts[1].toUpperCase();
                            int level = Integer.parseInt(parts[2]);
                            
                            org.bukkit.enchantments.Enchantment enchantment = HeartlessMain.getContentManager().getEnchantment(enchantName);
                            if (enchantment != null) {
                                org.bukkit.inventory.ItemStack mainHand = player.getInventory().getItemInMainHand();
                                if (mainHand != null && mainHand.getType() != org.bukkit.Material.AIR) {
                                    // Validar si el encantamiento puede ser aplicado al item
                                    if (CustomEnchantments.canApplyEnchantment(enchantment, mainHand)) {
                                        mainHand.addUnsafeEnchantment(enchantment, level);
                                        logger.info("[" + getId() + "] Encantamiento otorgado: " + enchantName + " " + level + " a " + player.getName());
                                    } else {
                                        logger.warning("[" + getId() + "] El encantamiento " + enchantName + " no puede ser aplicado al item en la mano de " + player.getName());
                                        player.sendMessage(MM.toComponent("<red>El encantamiento " + enchantName + " no puede ser aplicado a este item.</red>"));
                                    }
                                } else {
                                    logger.warning("[" + getId() + "] No hay item en la mano para encantar para " + player.getName());
                                }
                            } else {
                                logger.warning("[" + getId() + "] Encantamiento inválido: " + enchantName);
                            }
                        } catch (NumberFormatException e) {
                            logger.warning("[" + getId() + "] Nivel de encantamiento inválido: " + parts[2]);
                        }
                    } else {
                        logger.warning("[" + getId() + "] Formato de encantamiento inválido: " + reward);
                    }
                    break;
                    
                case "money":
                case "coins":
                    try {
                        double amount = Double.parseDouble(parts[1]);
                        
                        // Integración con CoinsEngine
                        if (org.bukkit.Bukkit.getPluginManager().getPlugin("CoinsEngine") != null) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "thalos give " + player.getName() + " " + amount);
                            player.sendMessage(MM.toComponent("<gold>¡Has recibido <yellow>" + amount + "</yellow> monedas!</gold>"));
                            logger.info("[" + getId() + "] Monedas otorgadas: " + amount + " a " + player.getName());
                        } else {
                            logger.warning("[" + getId() + "] CoinsEngine no está disponible para otorgar " + amount + " monedas a " + player.getName());
                            // Fallback: dar experiencia equivalente
                            int expEquivalent = (int)(amount / 10); // 10 monedas = 1 exp
                            player.giveExp(expEquivalent);
                            player.sendMessage(MM.toComponent("<yellow>¡Has recibido <green>" + expEquivalent + "</green> puntos de experiencia! (CoinsEngine no disponible)</yellow>"));
                        }
                    } catch (NumberFormatException e) {
                        logger.warning("[" + getId() + "] Cantidad de monedas inválida: " + parts[1]);
                    }
                    break;
                    
                default:
                    logger.warning("[" + getId() + "] Tipo de recompensa desconocido: " + type);
                    break;
            }
        } catch (Exception e) {
            logger.warning("[" + getId() + "] Error procesando recompensa '" + reward + "' para " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Maneja errores del evento de manera robusta.
     * 
     * @param operation Operación que causó el error
     * @param error El error ocurrido
     */
    private void handleEventError(String operation, Exception error) {
        logger.log(Level.SEVERE, "[" + getId() + "] Error crítico en operación: " + operation, error);
        
        // Intentar recuperación automática
        try {
            if ("start".equals(operation) && !isActive()) {
                logger.info("[" + getId() + "] Intentando recuperación automática del inicio...");
                // Lógica de recuperación específica
            }
        } catch (Exception recoveryError) {
            logger.log(Level.SEVERE, "[" + getId() + "] Fallo en recuperación automática", recoveryError);
        }
    }
    
    // === GETTERS PARA ACCESO A DATOS ===
    
    /**
     * Obtiene todas las estadísticas de un jugador.
     * 
     * @param playerId ID del jugador
     * @return Mapa con las estadísticas del jugador
     */
    public final Map<String, Object> getPlayerStatistics(UUID playerId) {
        Map<String, Object> stats = playerStatistics.get(playerId);
        return stats != null ? new HashMap<>(stats) : new HashMap<>();
    }
    
    /**
     * Obtiene todas las estadísticas globales del evento.
     * 
     * @return Mapa con las estadísticas globales
     */
    public final Map<String, Long> getGlobalStatistics() {
        Map<String, Long> stats = new HashMap<>();
        globalStatistics.forEach((key, value) -> stats.put(key, value.get()));
        return stats;
    }
    
    /**
     * Obtiene los desafíos completados por un jugador.
     * 
     * @param playerId ID del jugador
     * @return Set con los IDs de desafíos completados
     */
    public final Set<String> getCompletedChallenges(UUID playerId) {
        Set<String> challenges = completedChallenges.get(playerId);
        return challenges != null ? new HashSet<>(challenges) : new HashSet<>();
    }
    
    /**
     * Obtiene el progreso de desafíos de un jugador.
     * 
     * @param playerId ID del jugador
     * @return Mapa con el progreso de desafíos
     */
    public final Map<String, Object> getChallengeProgress(UUID playerId) {
        Map<String, Object> progress = challengeProgress.get(playerId);
        return progress != null ? new HashMap<>(progress) : new HashMap<>();
    }
    
    /**
     * Obtiene todos los desafíos disponibles en el evento.
     * 
     * @return Mapa con los desafíos disponibles (ID -> ChallengeDefinition)
     */
    public final Map<String, ChallengeDefinition> getAvailableChallenges() {
        return new HashMap<>(availableChallenges);
    }
    
    /**
     * Obtiene una lista de IDs de desafíos disponibles.
     * 
     * @return Lista con los IDs de desafíos disponibles
     */
    public final List<String> getAvailableChallengeIds() {
        return new ArrayList<>(availableChallenges.keySet());
    }
    
    /**
     * Obtiene un desafío específico por su ID.
     * 
     * @param challengeId ID del desafío
     * @return ChallengeDefinition del desafío o null si no existe
     */
    public final ChallengeDefinition getChallenge(String challengeId) {
        return availableChallenges.get(challengeId);
    }
    
    /**
     * Obtiene el total de participantes del evento.
     * 
     * @return Número total de participantes
     */
    public final long getTotalParticipants() {
        return totalParticipants.get();
    }
    
    /**
     * Obtiene el total de desafíos completados.
     * 
     * @return Número total de desafíos completados
     */
    public final long getTotalChallengesCompleted() {
        return totalChallengesCompleted.get();
    }
    
    /**
     * Obtiene todos los jugadores que tienen estadísticas registradas.
     * 
     * @return Set con los UUIDs de todos los jugadores con estadísticas
     */
    public final Set<UUID> getAllPlayersWithStatistics() {
        return new HashSet<>(playerStatistics.keySet());
    }
    
    // === CONFIGURACIÓN DEL SISTEMA ===
    
    /**
     * Habilita o deshabilita el sistema de desafíos.
     * 
     * @param enabled true para habilitar, false para deshabilitar
     */
    protected final void setChallengeSystemEnabled(boolean enabled) {
        challengeSystemEnabled.set(enabled);
    }
    
    /**
     * Habilita o deshabilita el sistema de estadísticas.
     * 
     * @param enabled true para habilitar, false para deshabilitar
     */
    protected final void setStatisticsEnabled(boolean enabled) {
        statisticsEnabled.set(enabled);
    }
    
    /**
     * Habilita o deshabilita el guardado automático.
     * 
     * @param enabled true para habilitar, false para deshabilitar
     */
    protected final void setAutoSaveEnabled(boolean enabled) {
        autoSaveEnabled.set(enabled);
    }
    
    /**
     * Definición de un desafío del evento.
     */
    public static class ChallengeDefinition {
        private final String id;
        private final String displayName;
        private final String description;
        private final int requiredProgress;
        private final List<String> rewards; // Mantenido para compatibilidad hacia atrás
        private final List<Reward> rewardObjects; // Nueva lista de objetos Reward
        
        /**
         * Constructor privado para uso interno
         */
        private ChallengeDefinition(String id, String displayName, String description, 
                                  int requiredProgress, List<String> rewards, 
                                  List<Reward> rewardObjects) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.requiredProgress = requiredProgress;
            this.rewards = rewards != null ? new ArrayList<>(rewards) : new ArrayList<>();
            this.rewardObjects = rewardObjects != null ? new ArrayList<>(rewardObjects) : new ArrayList<>();
        }
        
        /**
         * Factory method que acepta recompensas como strings (compatibilidad hacia atrás)
         */
        public static ChallengeDefinition fromStringRewards(String id, String displayName, String description, 
                                                           int requiredProgress, List<String> rewards) {
            List<String> rewardStrings = rewards != null ? new ArrayList<>(rewards) : new ArrayList<>();
            List<Reward> rewardObjects = new ArrayList<>();
            
            // Convertir strings a objetos Reward
            if (rewards != null) {
                for (String rewardString : rewards) {
                    try {
                        rewardObjects.add(new Reward(rewardString));
                    } catch (IllegalArgumentException e) {
                        // Log error pero continúa con las otras recompensas
                        java.util.logging.Logger.getLogger(ChallengeDefinition.class.getName())
                            .warning("Error parseando recompensa '" + rewardString + "': " + e.getMessage());
                    }
                }
            }
            
            return new ChallengeDefinition(id, displayName, description, requiredProgress, rewardStrings, rewardObjects);
        }
        
        /**
         * Factory method que acepta objetos Reward directamente
         */
        public static ChallengeDefinition fromRewardObjects(String id, String displayName, String description, 
                                                           int requiredProgress, List<Reward> rewardObjects) {
            List<String> rewardStrings = new ArrayList<>();
            
            // Convertir objetos Reward a strings para compatibilidad
            if (rewardObjects != null) {
                for (Reward reward : rewardObjects) {
                    rewardStrings.add(reward.toString());
                }
            }
            
            return new ChallengeDefinition(id, displayName, description, requiredProgress, rewardStrings, 
                                         rewardObjects != null ? new ArrayList<>(rewardObjects) : new ArrayList<>());
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getRequiredProgress() { return requiredProgress; }
        
        /**
         * Obtiene las recompensas como strings (compatibilidad hacia atrás)
         */
        public List<String> getRewards() { return new ArrayList<>(rewards); }
        
        /**
         * Obtiene las recompensas como objetos Reward
         */
        public List<Reward> getRewardObjects() { 
            return new ArrayList<>(rewardObjects); 
        }
        
        /**
         * Otorga todas las recompensas al jugador especificado
         */
        public void grantRewardsTo(org.bukkit.entity.Player player, String eventId) {
            for (Reward reward : rewardObjects) {
                reward.grantTo(player, eventId);
            }
        }
    }


    /**
     * Obtiene los datos específicos del evento.
     * 
     * @return Mapa con los datos específicos del evento
     */
    public final Map<String, Object> getEventSpecificData() {
        return new HashMap<>(eventSpecificData);
    }

    /**
     * Obtiene todos los desafíos completados de todos los jugadores.
     * 
     * @return Mapa con todos los desafíos completados (UUID -> Set<String>)
     */
    public final Map<UUID, Set<String>> getAllCompletedChallenges() {
        Map<UUID, Set<String>> allCompleted = new HashMap<>();
        for (Map.Entry<UUID, Set<String>> entry : completedChallenges.entrySet()) {
            allCompleted.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return allCompleted;
    }
    
    /**
     * Obtiene todo el progreso de desafíos de todos los jugadores.
     * 
     * @return Mapa con todo el progreso de desafíos (UUID -> Map<String, Object>)
     */
    public final Map<UUID, Map<String, Object>> getAllChallengeProgress() {
        Map<UUID, Map<String, Object>> allProgress = new HashMap<>();
        for (Map.Entry<UUID, Map<String, Object>> entry : challengeProgress.entrySet()) {
            allProgress.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        return allProgress;
    }
    
    /**
     * Carga el progreso de desafíos desde los datos persistentes usando merge inteligente.
     * Este método es llamado por StorageManager durante la carga del evento.
     * 
     * CAMBIO CRÍTICO: Ahora usa merge en lugar de clear() para preservar datos en tiempo real.
     * 
     * @param data Mapa con el progreso de desafíos (UUID -> Map<String, Object>)
     */
    public final void loadChallengeProgress(Map<UUID, Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            plugin.getLogger().info("[" + getId() + "] No hay datos de progreso de desafíos para cargar");
            return;
        }
        
        dataLock.writeLock().lock();
        try {
            plugin.getLogger().info("[" + getId() + "] Iniciando carga inteligente de progreso de desafíos...");
            
            int mergedEntries = 0;
            int newEntries = 0;
            int preservedEntries = 0;
            
            for (Map.Entry<UUID, Map<String, Object>> entry : data.entrySet()) {
                UUID playerId = entry.getKey();
                Map<String, Object> playerProgress = entry.getValue();
                
                if (playerProgress == null || playerProgress.isEmpty()) {
                    continue;
                }
                
                Map<String, Object> existingProgress = challengeProgress.get(playerId);
                
                if (existingProgress == null) {
                    // Nuevo jugador - agregar todos los datos
                    challengeProgress.put(playerId, new ConcurrentHashMap<>(playerProgress));
                    newEntries++;
                } else {
                    // Jugador existente - merge inteligente
                    for (Map.Entry<String, Object> progressEntry : playerProgress.entrySet()) {
                        String challengeId = progressEntry.getKey();
                        Object newProgress = progressEntry.getValue();
                        Object existingValue = existingProgress.get(challengeId);
                        
                        if (existingValue == null) {
                            // Nuevo desafío para este jugador
                            existingProgress.put(challengeId, newProgress);
                            mergedEntries++;
                        } else if (newProgress instanceof Number && existingValue instanceof Number) {
                            // Comparar valores numéricos y mantener el mayor
                            double newVal = ((Number) newProgress).doubleValue();
                            double existingVal = ((Number) existingValue).doubleValue();
                            
                            if (newVal > existingVal) {
                                existingProgress.put(challengeId, newProgress);
                                mergedEntries++;
                            } else {
                                preservedEntries++;
                            }
                        } else {
                            // Para otros tipos, mantener el existente (más reciente)
                            preservedEntries++;
                        }
                    }
                }
            }
            
            plugin.getLogger().info(String.format(
                "[%s] Carga de progreso completada - Nuevos: %d, Merged: %d, Preservados: %d",
                getId(), newEntries, mergedEntries, preservedEntries
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, 
                "[" + getId() + "] Error durante la carga de progreso de desafíos", e);
        } finally {
            dataLock.writeLock().unlock();
        }
    }
    
    /**
     * Carga los desafíos completados desde los datos persistentes usando merge inteligente.
     * Este método es llamado por StorageManager durante la carga del evento.
     * 
     * CAMBIO CRÍTICO: Ahora usa merge en lugar de clear() para preservar datos en tiempo real.
     * 
     * @param data Mapa con los desafíos completados (UUID -> Set<String>)
     */
    public final void loadCompletedChallenges(Map<UUID, Set<String>> data) {
        if (data == null || data.isEmpty()) {
            plugin.getLogger().info("[" + getId() + "] No hay datos de desafíos completados para cargar");
            return;
        }
        
        dataLock.writeLock().lock();
        try {
            plugin.getLogger().info("[" + getId() + "] Iniciando carga inteligente de desafíos completados...");
            
            int mergedPlayers = 0;
            int newPlayers = 0;
            int preservedChallenges = 0;
            int addedChallenges = 0;
            
            for (Map.Entry<UUID, Set<String>> entry : data.entrySet()) {
                UUID playerId = entry.getKey();
                Set<String> playerCompletedChallenges = entry.getValue();
                
                if (playerCompletedChallenges == null || playerCompletedChallenges.isEmpty()) {
                    continue;
                }
                
                Set<String> existingCompleted = completedChallenges.get(playerId);
                
                if (existingCompleted == null) {
                    // Nuevo jugador - agregar todos los desafíos completados
                    completedChallenges.put(playerId, ConcurrentHashMap.newKeySet());
                    completedChallenges.get(playerId).addAll(playerCompletedChallenges);
                    newPlayers++;
                    addedChallenges += playerCompletedChallenges.size();
                } else {
                    // Jugador existente - merge inteligente (unión de conjuntos)
                    int sizeBefore = existingCompleted.size();
                    
                    for (String challengeId : playerCompletedChallenges) {
                        if (!existingCompleted.contains(challengeId)) {
                            existingCompleted.add(challengeId);
                            addedChallenges++;
                        } else {
                            preservedChallenges++;
                        }
                    }
                    
                    if (existingCompleted.size() > sizeBefore) {
                        mergedPlayers++;
                    }
                }
            }
            
            plugin.getLogger().info(String.format(
                "[%s] Carga de desafíos completados - Nuevos jugadores: %d, Jugadores merged: %d, " +
                "Desafíos añadidos: %d, Preservados: %d",
                getId(), newPlayers, mergedPlayers, addedChallenges, preservedChallenges
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, 
                "[" + getId() + "] Error durante la carga de desafíos completados", e);
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    /**
     * Método llamado cuando un jugador se conecta al servidor.
     * Carga el progreso de desafíos del jugador desde la base de datos.
     * 
     * @param player El jugador que se conectó
     */
    @Override
    protected void onPlayerJoinEvent(Player player) {
        if (!isActive()) {
            return;
        }
        UUID playerId = player.getUniqueId();
        try {
            
            // Cargar progreso de desafíos del jugador desde la base de datos
            plugin.getStorageManager().loadPlayerChallengeProgress(this, playerId);
            
            plugin.getLogger().info(String.format(
                "[%s] Progreso de desafíos cargado para jugador: %s", 
                getId(), playerId.toString()
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, 
                String.format("[%s] Error al cargar progreso de desafíos para jugador %s", 
                    getId(), playerId.toString()), e);
        }
    }
}