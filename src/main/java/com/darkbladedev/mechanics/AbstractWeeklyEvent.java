package com.darkbladedev.mechanics;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.managers.PlayerStatisticsReportManager;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.TimeExpression;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
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
        initializeEventSystems();
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
        
        // Configurar desafíos básicos si están habilitados
        if (challengeSystemEnabled.get()) {
            setupBasicChallenges();
        }
        
        logger.info("[" + getId() + "] Sistemas del evento abstracto inicializados correctamente");
    }
    
    /**
     * Configura los desafíos básicos comunes a todos los eventos.
     * Los eventos específicos pueden sobrescribir este método para añadir desafíos personalizados.
     */
    protected void setupBasicChallenges() {
        // Desafío de participación básica
        registerChallenge("participation", new ChallengeDefinition(
            "participation",
            "Participar en el evento",
            "Únete al evento semanal",
            1,
            Collections.singletonList("heartless:participation_reward")
        ));
        
        // Desafío de supervivencia
        registerChallenge("survivor", new ChallengeDefinition(
            "survivor",
            "Superviviente",
            "Sobrevive durante todo el evento",
            1,
            Collections.singletonList("heartless:survivor_reward")
        ));
    }
    
    // === TEMPLATE METHODS ===
    
    @Override
    public final void start() {
        try {
            globalStatistics.get("event_starts").incrementAndGet();
            
            // Inicializar jugadores online
            initializeOnlinePlayers();
            
            // Inicializar datos específicos del evento
            initializeEventSpecificData();
            
            // Iniciar tareas del sistema abstracto
            startAbstractEventTasks();
            
            // Llamar al método específico del evento
            onEventStart();
            
            // Iniciar tareas específicas del evento
            super.start();
            
            logger.info("[" + getId() + "] Evento iniciado correctamente con " + 
                       getActivePlayerCount() + " jugadores");
            
        } catch (Exception e) {
            globalStatistics.get("total_errors").incrementAndGet();
            logger.log(Level.SEVERE, "[" + getId() + "] Error al iniciar el evento", e);
            handleEventError("start", e);
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
        
        completedChallenges.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                          .add(challengeId);
        
        totalChallengesCompleted.incrementAndGet();
        dataDirty.set(true);
        
        // Notificar al jugador
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            ChallengeDefinition challenge = availableChallenges.get(challengeId);
            if (challenge != null) {
                notifyPlayerChallengeCompleted(player, challenge);
                giveRewards(player, challenge.getRewards());
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
    protected final void updateChallengeProgress(UUID playerId, String challengeId, 
                                               Object progress, Object maxProgress) {
        if (playerId == null || challengeId == null) return;
        
        Map<String, Object> playerProgress = challengeProgress.computeIfAbsent(
            playerId, k -> new ConcurrentHashMap<>());
        
        playerProgress.put(challengeId + "_current", progress);
        playerProgress.put(challengeId + "_max", maxProgress);
        
        dataDirty.set(true);
    }
    
    // === SISTEMA DE ESTADÍSTICAS ===
    
    /**
     * Actualiza una estadística específica para un jugador.
     * 
     * @param playerId ID del jugador
     * @param statistic Nombre de la estadística
     * @param value Valor a establecer
     */
    protected final void updatePlayerStatistic(UUID playerId, String statistic, Object value) {
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
    protected final void updateGlobalStatistic(String statistic, long value) {
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
        // Verificar desafío de participación
        if (!hasChallengeCompleted(playerId, "participation")) {
            completeChallengeForPlayer(playerId, "participation");
        }
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
        
        // Verificar desafío de supervivencia para jugadores activos
        for (UUID playerId : getActivePlayers()) {
            if (!hasChallengeCompleted(playerId, "survivor")) {
                completeChallengeForPlayer(playerId, "survivor");
            }
        }
        
        // Generar y enviar reportes individuales de estadísticas
        generateAndSendPlayerReports();
    }
    
    /**
     * Genera y envía reportes individuales de estadísticas a todos los jugadores participantes.
     * Utiliza el PlayerStatisticsReportManager para crear reportes personalizados.
     */
    private void generateAndSendPlayerReports() {
        if (reportManager == null) {
            plugin.getLogger().warning("ReportManager no está inicializado para el evento " + getId());
            return;
        }

        // Usar el método público generateAndSendFinalReports que maneja todo el proceso
        reportManager.generateAndSendFinalReports(this);
    }
    
    /**
     * Notifica a un jugador que ha completado un desafío con formato MiniMessage y hover text.
     * 
     * @param player El jugador
     * @param challenge El desafío completado
     */
    private void notifyPlayerChallengeCompleted(Player player, ChallengeDefinition challenge) {
        UUID playerId = player.getUniqueId();
        
        // Obtener progreso actual y objetivo
        Map<String, Object> progress = challengeProgress.getOrDefault(playerId, new HashMap<>());
        Object currentProgress = progress.getOrDefault(challenge.getId(), 0);
        int targetProgress = challenge.getRequiredProgress();
        
        // Crear el texto del hover con el progreso
        String hoverText = "<gray>Progreso: <white>" + currentProgress + "/" + targetProgress + "</white></gray>";
        Component hoverComponent = MM.toComponent(hoverText);
        
        // Crear el mensaje principal con hover en el nombre del desafío
        Component challengeNameWithHover = MM.toComponent("<yellow>" + challenge.getDisplayName() + "</yellow>")
                .hoverEvent(HoverEvent.showText(hoverComponent));
        
        // Mensaje completo
        Component fullMessage = MM.toComponent(prefix + " <green>Has completado el desafío </green>")
                .append(challengeNameWithHover)
                .append(MM.toComponent("<green>!</green>"));
        
        player.sendMessage(fullMessage);
    }
    
    /**
     * Otorga recompensas a un jugador.
     * 
     * @param player El jugador
     * @param rewards Lista de recompensas
     */
    private void giveRewards(Player player, List<String> rewards) {
        if (rewards == null || rewards.isEmpty()) return;
        
        for (String reward : rewards) {
            // Aquí se integraría con el sistema de recompensas del plugin
            // Por ahora, solo registramos la recompensa
            logger.info("[" + getId() + "] Recompensa otorgada a " + player.getName() + ": " + reward);
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
    protected static class ChallengeDefinition {
        private final String id;
        private final String displayName;
        private final String description;
        private final int requiredProgress;
        private final List<String> rewards;
        
        public ChallengeDefinition(String id, String displayName, String description, 
                                 int requiredProgress, List<String> rewards) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.requiredProgress = requiredProgress;
            this.rewards = rewards != null ? new ArrayList<>(rewards) : new ArrayList<>();
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getRequiredProgress() { return requiredProgress; }
        public List<String> getRewards() { return new ArrayList<>(rewards); }
    }
}