package com.darkbladedev.mechanics;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.models.TimeExpression;
import com.darkbladedev.utils.MM;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

/**
 * Ejemplo de implementación de AbstractWeeklyEvent.
 * 
 * Esta clase demuestra cómo extender AbstractWeeklyEvent para crear
 * un evento semanal personalizado con funcionalidades específicas.
 * 
 * Características del ejemplo:
 * - Sistema de desafíos personalizado
 * - Estadísticas específicas del evento
 * - Persistencia de datos automática
 * - Manejo de eventos de jugadores
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public class ExampleAbstractEvent extends AbstractWeeklyEvent {
    
    // === CONFIGURACIÓN DEL EVENTO ===
    private static final String EVENT_ID = "example_event";
    private static final String EVENT_PREFIX = "<gradient:#ff6b6b:#4ecdc4>[Evento Ejemplo]</gradient>";
    
    // === DATOS ESPECÍFICOS DEL EVENTO ===
    private long eventStartTime;
    private int maxPlayersReached;
    
    /**
     * Constructor del evento ejemplo.
     * 
     * @param plugin El plugin principal
     * @param duration Duración del evento
     */
    public ExampleAbstractEvent(HeartlessMain plugin, TimeExpression duration) {
        super(plugin, duration);
        
        // Configurar el prefix del evento
        this.prefix = EVENT_PREFIX;
        
        // Registrar el listener para eventos de jugadores
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    // === IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS ===
    
    @Override
    public String getId() {
        return EVENT_ID;
    }
    
    @Override
    protected void onEventStart() {
        // Inicializar datos específicos del evento
        eventStartTime = System.currentTimeMillis();
        maxPlayersReached = getActivePlayerCount();
        
        // Configurar desafíos específicos del evento
        setupEventSpecificChallenges();
        
        // Inicializar estadísticas específicas
        initializeEventStatistics();
        
        logger.info("[" + getId() + "] Evento ejemplo iniciado con configuración personalizada");
    }
    
    @Override
    protected void onEventStop() {
        // Procesar estadísticas finales
        processFinalEventStatistics();
        
        // Limpiar recursos específicos del evento
        cleanupEventResources();
        
        logger.info("[" + getId() + "] Evento ejemplo detenido correctamente");
    }
    
    // === IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS DE WeeklyEvent ===
    
    @Override
    protected void startEventTasks() {
        // Iniciar tareas específicas del evento ejemplo
        logger.info("[" + getId() + "] Iniciando tareas específicas del evento ejemplo");
        
        // Ejemplo: Tarea que se ejecuta cada 30 segundos
        addCustomTask("example_periodic_task", new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) return;
                
                // Actualizar estadísticas de jugadores activos
                maxPlayersReached = Math.max(maxPlayersReached, getActivePlayerCount());
                
                // Procesar eventos específicos del ejemplo
                processPeriodicEventLogic();
            }
        }.runTaskTimer(plugin, 0L, 30 * 20L)); // Cada 30 segundos
        
        // Ejemplo: Tarea de notificaciones
        addCustomTask("notification_task", new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) return;
                
                // Enviar notificaciones periódicas a jugadores activos
                sendPeriodicNotifications();
            }
        }.runTaskTimer(plugin, 60 * 20L, 5 * 60 * 20L)); // Cada 5 minutos, empezando después de 1 minuto
    }
    
    @Override
    protected void stopEventTasks() {
        // Cancelar todas las tareas personalizadas del evento
        cancelCustomTask("example_periodic_task");
        cancelCustomTask("notification_task");
        
        logger.info("[" + getId() + "] Tareas específicas del evento ejemplo detenidas");
    }
    
    @Override
    protected void initializeEventSpecificData() {
        // Inicializar datos específicos que se guardarán automáticamente
        eventSpecificData.put("start_time", System.currentTimeMillis());
        eventSpecificData.put("initial_players", getActivePlayerCount());
        eventSpecificData.put("event_version", "1.0");
        
        // Marcar datos como modificados para activar el guardado automático
        dataDirty.set(true);
    }
    
    @Override
    protected void saveEventSpecificData() {
        // Actualizar datos antes del guardado
        eventSpecificData.put("last_save", System.currentTimeMillis());
        eventSpecificData.put("current_players", getActivePlayerCount());
        eventSpecificData.put("max_players_reached", maxPlayersReached);
        
        // Aquí se implementaría la lógica de guardado en base de datos
        // Por ejemplo, usando el DatabaseManager del plugin
        logger.info("[" + getId() + "] Datos del evento guardados: " + eventSpecificData.size() + " entradas");
    }
    
    @Override
    protected void processEventStatistics() {
        // Actualizar estadísticas globales
        updateGlobalStatistic("current_players", getActivePlayerCount());
        updateGlobalStatistic("uptime_minutes", (System.currentTimeMillis() - eventStartTime) / 60000);
        
        // Procesar estadísticas de jugadores activos
        for (UUID playerId : getActivePlayers()) {
            processPlayerStatistics(playerId);
        }
        
        // Actualizar máximo de jugadores si es necesario
        int currentPlayers = getActivePlayerCount();
        if (currentPlayers > maxPlayersReached) {
            maxPlayersReached = currentPlayers;
            updateGlobalStatistic("max_players_reached", maxPlayersReached);
        }
    }
    
    // === CONFIGURACIÓN DE DESAFÍOS ESPECÍFICOS ===
    
    /**
     * Configura los desafíos específicos de este evento.
     */
    private void setupEventSpecificChallenges() {
        // Desafío de conexión temprana
        registerChallenge("early_bird", AbstractWeeklyEvent.ChallengeDefinition.fromStringRewards(
            "early_bird",
            "Madrugador",
            "Únete al evento en los primeros 10 minutos",
            1,
            Arrays.asList("heartless:early_bird_reward", "heartless:bonus_xp")
        ));
        
        // Desafío de permanencia
        registerChallenge("long_stay", AbstractWeeklyEvent.ChallengeDefinition.fromStringRewards(
            "long_stay",
            "Resistencia",
            "Permanece conectado durante 2 horas",
            7200, // 2 horas en segundos
            Collections.singletonList("heartless:endurance_reward")
        ));
        
        // Desafío social
        registerChallenge("social_butterfly", AbstractWeeklyEvent.ChallengeDefinition.fromStringRewards(
            "social_butterfly",
            "Mariposa Social",
            "Interactúa con 5 jugadores diferentes",
            5,
            Arrays.asList("heartless:social_reward", "heartless:friendship_token")
        ));
    }
    
    /**
     * Inicializa las estadísticas específicas del evento.
     */
    private void initializeEventStatistics() {
        // Estadísticas globales específicas
        updateGlobalStatistic("messages_sent", 0);
        updateGlobalStatistic("interactions_count", 0);
        updateGlobalStatistic("early_birds", 0);
    }
    
    // === PROCESAMIENTO DE ESTADÍSTICAS ===
    
    /**
     * Procesa las estadísticas específicas de un jugador.
     * 
     * @param playerId ID del jugador
     */
    private void processPlayerStatistics(UUID playerId) {
        // Actualizar tiempo de conexión
        Object joinTime = getPlayerStatistic(playerId, "join_time");
        if (joinTime instanceof Long) {
            long sessionTime = System.currentTimeMillis() - (Long) joinTime;
            updatePlayerStatistic(playerId, "session_time", sessionTime);
            
            // Verificar desafío de permanencia
            if (sessionTime >= 7200000 && !hasChallengeCompleted(playerId, "long_stay")) { // 2 horas
                completeChallengeForPlayer(playerId, "long_stay");
            }
        }
        
        // Incrementar estadística de actualizaciones
        incrementPlayerStatistic(playerId, "stat_updates", 1);
    }
    
    /**
     * Procesa las estadísticas finales del evento.
     */
    private void processFinalEventStatistics() {
        long eventDuration = System.currentTimeMillis() - eventStartTime;
        updateGlobalStatistic("total_duration_minutes", eventDuration / 60000);
        
        // Calcular estadísticas de participación
        int totalParticipants = getActivePlayers().size();
        updateGlobalStatistic("final_participants", totalParticipants);
        
        logger.info("[" + getId() + "] Estadísticas finales procesadas - Duración: " + 
                   (eventDuration / 60000) + " minutos, Participantes: " + totalParticipants);
    }
    
    // === VERIFICACIÓN DE DESAFÍOS PERSONALIZADA ===
    
    @Override
    protected void checkPlayerChallenges(UUID playerId) {
        // Llamar a la verificación base
        super.checkPlayerChallenges(playerId);
        
        // Verificar desafío de madrugador
        if (!hasChallengeCompleted(playerId, "early_bird")) {
            long playerJoinTime = (Long) getPlayerStatistic(playerId, "join_time");
            if (playerJoinTime != 0 && (playerJoinTime - eventStartTime) <= 600000) { // 10 minutos
                completeChallengeForPlayer(playerId, "early_bird");
                incrementGlobalStatistic("early_birds", 1);
            }
        }
        
        // Verificar desafío social
        Object interactions = getPlayerStatistic(playerId, "player_interactions");
        if (interactions instanceof Integer && (Integer) interactions >= 5) {
            if (!hasChallengeCompleted(playerId, "social_butterfly")) {
                completeChallengeForPlayer(playerId, "social_butterfly");
            }
        }
    }
    
    // === MANEJO DE EVENTOS DE JUGADORES ===
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isActive()) return;
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Usar métodos de la clase padre para gestionar jugadores activos
        activePlayers.add(playerId);
        playerJoinTimes.put(playerId, System.currentTimeMillis());
        
        // Inicializar estadísticas del jugador
        initializePlayerStatistics(playerId);
        
        // Enviar mensaje de bienvenida
        player.sendMessage(MM.toComponent(prefix + " <white>¡Bienvenido al evento ejemplo! Completa desafíos para obtener recompensas."));
        
        // Mostrar desafíos disponibles
        showAvailableChallenges(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!isActive()) return;
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Procesar estadísticas finales del jugador antes de que se desconecte
        processPlayerFinalStats(playerId);
        
        // Usar métodos de la clase padre para gestionar jugadores activos
        activePlayers.remove(playerId);
        
        // Mantener datos de tiempo de conexión para estadísticas
        // No remover playerJoinTimes aquí para mantener el historial
    }
    
    /**
     * Procesa las estadísticas finales de un jugador al salir.
     * 
     * @param playerId ID del jugador
     */
    private void processPlayerFinalStats(UUID playerId) {
        Object joinTime = getPlayerStatistic(playerId, "join_time");
        if (joinTime instanceof Long) {
            long sessionTime = System.currentTimeMillis() - (Long) joinTime;
            updatePlayerStatistic(playerId, "final_session_time", sessionTime);
        }
        
        // Marcar datos como modificados para guardado automático
        dataDirty.set(true);
    }
    
    // === MÉTODOS DE UTILIDAD ESPECÍFICOS ===
    
    /**
     * Registra una interacción entre jugadores para el desafío social.
     * 
     * @param playerId ID del jugador que interactúa
     */
    public void registerPlayerInteraction(UUID playerId) {
        incrementPlayerStatistic(playerId, "player_interactions", 1);
        incrementGlobalStatistic("interactions_count", 1);
    }
    
    /**
     * Registra un mensaje enviado por un jugador.
     * 
     * @param playerId ID del jugador
     */
    public void registerMessageSent(UUID playerId) {
        incrementPlayerStatistic(playerId, "messages_sent", 1);
        incrementGlobalStatistic("messages_sent", 1);
    }
    
    /**
     * Limpia los recursos específicos del evento.
     */
    private void cleanupEventResources() {
        // Limpiar datos temporales
        eventStartTime = 0;
        maxPlayersReached = 0;
        
        // Aquí se limpiarían otros recursos específicos del evento
        logger.info("[" + getId() + "] Recursos del evento limpiados");
    }
    
    // === GETTERS ESPECÍFICOS DEL EVENTO ===
    
    /**
     * Obtiene el tiempo de inicio del evento.
     * 
     * @return Timestamp del inicio del evento
     */
    public long getEventStartTime() {
        return eventStartTime;
    }
    
    /**
     * Obtiene el máximo número de jugadores alcanzado.
     * 
     * @return Máximo de jugadores simultáneos
     */
    public int getMaxPlayersReached() {
        return maxPlayersReached;
    }
    
    /**
     * Obtiene la duración actual del evento en minutos.
     * 
     * @return Duración en minutos
     */
    public long getCurrentDurationMinutes() {
        if (eventStartTime == 0) return 0;
        return (System.currentTimeMillis() - eventStartTime) / 60000;
    }

    /**
     * Procesa la lógica periódica del evento
     */
    private void processPeriodicEventLogic() {
        // Verificar condiciones específicas del evento
        checkEventSpecificConditions();
        
        // Actualizar estadísticas globales
        updateGlobalStatistics();
        
        // Procesar desafíos activos
        processChallenges();
    }

    /**
     * Envía notificaciones periódicas a los jugadores
     */
    private void sendPeriodicNotifications() {
        for (UUID playerId : getActivePlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // Enviar recordatorio sobre el evento
                player.sendMessage(MM.toComponent("<yellow>[Evento] ¡El evento ejemplo está activo! Completa desafíos para obtener recompensas."));
                
                // Mostrar progreso si tiene desafíos activos
                if (hasActiveChallenges(playerId)) {
                    showChallengeProgress(player);
                }
            }
        }
    }

    /**
     * Verifica condiciones específicas del evento
     */
    private void checkEventSpecificConditions() {
        // Verificar si hay suficientes jugadores activos
        if (getActivePlayers().size() < 3) {
            // Lógica para eventos con pocos jugadores
            adjustEventDifficulty(0.8f);
        } else if (getActivePlayers().size() > 10) {
            // Lógica para eventos con muchos jugadores
            adjustEventDifficulty(1.2f);
        }
    }

    /**
     * Ajusta la dificultad del evento
     */
    private void adjustEventDifficulty(float multiplier) {
        // Implementar lógica de ajuste de dificultad
        logger.info("Ajustando dificultad del evento con multiplicador: " + multiplier);
    }

    /**
     * Inicializa las estadísticas de un jugador
     */
    private void initializePlayerStatistics(UUID playerId) {
        // Inicializar estadísticas básicas
        updatePlayerStatistic(playerId, "challenges_completed", 0);
        updatePlayerStatistic(playerId, "points_earned", 0);
        updatePlayerStatistic(playerId, "time_participated", 0L);
        updatePlayerStatistic(playerId, "last_activity", System.currentTimeMillis());
    }

    /**
     * Muestra los desafíos disponibles al jugador
     */
    private void showAvailableChallenges(Player player) {
        player.sendMessage(MM.toComponent("<green>=== Desafíos Disponibles ==="));
        
        // Mostrar desafíos registrados
        player.sendMessage(MM.toComponent("<yellow>- Madrugador: Únete en los primeros 10 minutos"));
        player.sendMessage(MM.toComponent("<yellow>- Resistencia: Permanece conectado 2 horas"));
        player.sendMessage(MM.toComponent("<yellow>- Mariposa Social: Interactúa con 5 jugadores"));
    }

    /**
     * Muestra el progreso de desafíos al jugador
     */
    private void showChallengeProgress(Player player) {
        UUID playerId = player.getUniqueId();
        
        player.sendMessage(MM.toComponent("<blue>=== Progreso de Desafíos ==="));
        
        // Mostrar progreso de desafíos específicos
        if (!hasChallengeCompleted(playerId, "early_bird")) {
            player.sendMessage(MM.toComponent("<aqua>Madrugador: En progreso"));
        }
        
        if (!hasChallengeCompleted(playerId, "long_stay")) {
            Object joinTime = getPlayerStatistic(playerId, "join_time");
            if (joinTime instanceof Long) {
                long sessionTime = System.currentTimeMillis() - (Long) joinTime;
                long minutes = sessionTime / 60000;
                player.sendMessage(MM.toComponent("<aqua>Resistencia: " + minutes + "/120 minutos"));
            }
        }
        
        Object interactions = getPlayerStatistic(playerId, "player_interactions");
        if (interactions instanceof Integer) {
            player.sendMessage(MM.toComponent("<aqua>Mariposa Social: " + interactions + "/5 interacciones"));
        }
    }

    // Métodos auxiliares para compatibilidad
    private boolean hasActiveChallenges(UUID playerId) {
        return !hasChallengeCompleted(playerId, "early_bird") || 
               !hasChallengeCompleted(playerId, "long_stay") || 
               !hasChallengeCompleted(playerId, "social_butterfly");
    }

    private void updateGlobalStatistics() {
        // Actualizar estadísticas globales del evento
        updateGlobalStatistic("current_players", getActivePlayerCount());
        updateGlobalStatistic("uptime_minutes", getCurrentDurationMinutes());
    }

    private void processChallenges() {
        // Procesar desafíos para todos los jugadores activos
        for (UUID playerId : getActivePlayers()) {
            checkPlayerChallenges(playerId);
        }
    }
}