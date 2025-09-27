package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.AbstractWeeklyEvent;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.PlayerStatisticsValidator;
import com.darkbladedev.utils.PerformanceOptimizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager especializado para generar y mostrar reportes de estadísticas individuales
 * de jugadores al finalizar eventos semanales.
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class PlayerStatisticsReportManager {
    
    private final HeartlessMain plugin;
    private final Logger logger;
    private final PlayerStatisticsValidator validator;
    private final PerformanceOptimizer performanceOptimizer;
    
    // Cache de reportes generados para evitar regeneración innecesaria
    private final Map<String, Map<UUID, PlayerEventReport>> cachedReports = new ConcurrentHashMap<>();
    
    // Configuración de validación de datos
    private static final int MAX_STAT_VALUE = 1000000; // Valor máximo razonable para estadísticas
    private static final int MIN_SESSION_TIME = 1000; // Tiempo mínimo de sesión en ms
    
    public PlayerStatisticsReportManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.validator = new PlayerStatisticsValidator();
        this.performanceOptimizer = new PerformanceOptimizer(plugin);
    }
    
    /**
     * Genera y envía reportes de estadísticas individuales a todos los jugadores
     * que participaron en el evento utilizando optimizaciones de rendimiento.
     * 
     * @param event El evento que ha finalizado
     */
    public void generateAndSendFinalReports(AbstractWeeklyEvent event) {
        if (event == null) {
            logger.warning("[PlayerStatisticsReportManager] Evento nulo proporcionado para generar reportes");
            return;
        }
        
        String eventId = event.getId();
        logger.info("[PlayerStatisticsReportManager] Generando reportes finales para evento: " + eventId);
        
        // Usar PerformanceOptimizer para procesamiento optimizado
        performanceOptimizer.processPlayersAsync(
            event.getActivePlayers(),
            (batch) -> {
                // Validar que el batch no sea null o vacío
                if (batch == null || batch.isEmpty()) {
                    logger.warning("[PlayerStatisticsReportManager] Batch de jugadores nulo o vacío para evento: " + eventId);
                    return new HashMap<UUID, PlayerEventReport>();
                }
                
                Map<UUID, PlayerEventReport> batchReports = new HashMap<>();
                for (UUID playerId : batch) {
                    try {
                        // Validar que el playerId no sea null
                        if (playerId == null) {
                            logger.warning("[PlayerStatisticsReportManager] PlayerId nulo encontrado en batch para evento: " + eventId);
                            continue;
                        }
                        
                        PlayerEventReport report = generatePlayerReport(playerId, event);
                        if (report != null) {
                            batchReports.put(playerId, report);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "[PlayerStatisticsReportManager] Error generando reporte para jugador " + playerId, e);
                    }
                }
                return batchReports;
            },
            (allReports) -> {
                // Validar que allReports no sea null
                if (allReports == null) {
                    logger.warning("[PlayerStatisticsReportManager] No se generaron reportes para el evento: " + eventId);
                    // Cachear un mapa vacío en lugar de null
                    cachedReports.put(eventId, new HashMap<>());
                    return;
                }
                
                // Cachear todos los reportes generados
                cachedReports.put(eventId, allReports);
                
                // Enviar reportes en el hilo principal solo si hay reportes
                if (!allReports.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        sendReportsToPlayers(allReports, event);
                    });
                }
                
                logger.info("[PlayerStatisticsReportManager] Reportes generados y enviados para " + allReports.size() + " jugadores");
            }
        );
    }
    
    /**
     * Genera un reporte individual para un jugador específico.
     * Incluye validación completa de datos antes de crear el reporte.
     * 
     * @param playerId ID del jugador
     * @param event El evento
     * @return Reporte del jugador o null si no hay datos suficientes o válidos
     */
    private PlayerEventReport generatePlayerReport(UUID playerId, AbstractWeeklyEvent event) {
        Map<String, Object> playerStats = event.getPlayerStatistics(playerId);
        Set<String> completedChallenges = event.getCompletedChallenges(playerId);
        Map<String, Object> challengeProgress = event.getChallengeProgress(playerId);
        
        // Verificar si el jugador tiene datos suficientes
        if (playerStats.isEmpty() && completedChallenges.isEmpty()) {
            return null;
        }
        
        // Validar estadísticas del jugador
        PlayerStatisticsValidator.ValidationResult statsValidation = 
            validator.validatePlayerStatistics(playerId, playerStats);
        if (!statsValidation.isValid()) {
            logger.warning("Estadísticas inválidas para jugador " + playerId + ": " + statsValidation.getErrorMessage());
            @SuppressWarnings("unchecked")
            Map<String, Object> cleanedStats = statsValidation.getCleanedDataAs(Map.class);
            playerStats = (cleanedStats != null) ? cleanedStats : new HashMap<>();
        } else if (statsValidation.getCleanedData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cleanedStats = statsValidation.getCleanedDataAs(Map.class);
            if (cleanedStats != null) {
                playerStats = cleanedStats;
            }
        }
        
        // Validar desafíos completados
        PlayerStatisticsValidator.ValidationResult challengesValidation = 
            validator.validateCompletedChallenges(playerId, completedChallenges);
        if (!challengesValidation.isValid()) {
            logger.warning("Desafíos completados inválidos para jugador " + playerId + ": " + challengesValidation.getErrorMessage());
            @SuppressWarnings("unchecked")
            Set<String> cleanedChallenges = challengesValidation.getCleanedDataAs(Set.class);
            completedChallenges = (cleanedChallenges != null) ? cleanedChallenges : new HashSet<>();
        } else if (challengesValidation.getCleanedData() != null) {
            @SuppressWarnings("unchecked")
            Set<String> cleanedChallenges = challengesValidation.getCleanedDataAs(Set.class);
            if (cleanedChallenges != null) {
                completedChallenges = cleanedChallenges;
            }
        }
        
        // Validar progreso de desafíos
        PlayerStatisticsValidator.ValidationResult progressValidation = 
            validator.validateChallengeProgress(playerId, challengeProgress);
        if (!progressValidation.isValid()) {
            logger.warning("Progreso de desafíos inválido para jugador " + playerId + ": " + progressValidation.getErrorMessage());
            @SuppressWarnings("unchecked")
            Map<String, Object> cleanedProgress = progressValidation.getCleanedDataAs(Map.class);
            challengeProgress = (cleanedProgress != null) ? cleanedProgress : new HashMap<>();
        } else if (progressValidation.getCleanedData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cleanedProgress = progressValidation.getCleanedDataAs(Map.class);
            if (cleanedProgress != null) {
                challengeProgress = cleanedProgress;
            }
        }
        
        // Detectar anomalías
        Player player = plugin.getServer().getPlayer(playerId);
        List<String> anomalies = validator.detectAnomalies(playerId, playerStats, player);
        if (!anomalies.isEmpty()) {
            logger.info("Anomalías detectadas para jugador " + playerId + ": " + String.join(", ", anomalies));
        }
        
        return new PlayerEventReport(
            playerId,
            event.getId(),
            playerStats,
            completedChallenges,
            challengeProgress,
            System.currentTimeMillis()
        );
    }
    
    /**
     * Valida que un reporte contenga datos coherentes y válidos.
     * 
     * @param report El reporte a validar
     * @return true si el reporte es válido
     */
    @SuppressWarnings("unused")
    private boolean validateReport(PlayerEventReport report) {
        if (report == null || report.getPlayerId() == null || report.getEventId() == null) {
            return false;
        }
        
        // Validar estadísticas numéricas
        for (Map.Entry<String, Object> entry : report.getPlayerStatistics().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Number) {
                long numValue = ((Number) value).longValue();
                if (numValue < 0 || numValue > MAX_STAT_VALUE) {
                    logger.warning("[PlayerStatisticsReportManager] Valor estadístico inválido para " + 
                                 entry.getKey() + ": " + numValue);
                    return false;
                }
            }
        }
        
        // Validar tiempo de sesión si existe
        Object sessionTime = report.getPlayerStatistics().get("session_time");
        if (sessionTime instanceof Number) {
            long time = ((Number) sessionTime).longValue();
            if (time < MIN_SESSION_TIME) {
                logger.fine("[PlayerStatisticsReportManager] Tiempo de sesión muy corto: " + time + "ms");
            }
        }
        
        return true;
    }
    
    /**
     * Envía los reportes a los jugadores correspondientes utilizando procesamiento optimizado.
     * 
     * @param reports Mapa de reportes por jugador
     * @param event El evento finalizado
     */
    private void sendReportsToPlayers(Map<UUID, PlayerEventReport> reports, AbstractWeeklyEvent event) {
        if (reports.isEmpty()) {
            logger.info("[PlayerStatisticsReportManager] No hay reportes para enviar");
            return;
        }
        
        // Filtrar solo jugadores online
        List<UUID> onlinePlayerIds = new ArrayList<>();
        for (UUID playerId : reports.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                onlinePlayerIds.add(playerId);
            }
        }
        
        if (onlinePlayerIds.isEmpty()) {
            logger.info("[PlayerStatisticsReportManager] No hay jugadores online para recibir reportes");
            return;
        }
        
        // Usar PerformanceOptimizer para envío por lotes
        performanceOptimizer.processPlayersSync(onlinePlayerIds, (batch) -> {
            for (UUID playerId : batch) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    PlayerEventReport report = reports.get(playerId);
                    if (report != null) {
                        sendIndividualReport(player, report, event);
                    }
                }
            }
        });
        
        logger.info("[PlayerStatisticsReportManager] Enviados reportes a " + onlinePlayerIds.size() + " jugadores online");
    }
    
    /**
     * Envía un reporte individual a un jugador específico.
     * 
     * @param player El jugador
     * @param report El reporte del jugador
     * @param event El evento
     */
    private void sendIndividualReport(Player player, PlayerEventReport report, AbstractWeeklyEvent event) {
        try {
            // Separador visual
            player.sendMessage(MM.toComponent("<gray><b>========================================</b></gray>"));
            player.sendMessage(MM.toComponent("<gold><b>REPORTE FINAL - " + event.getId().toUpperCase() + "</b></gold>"));
            player.sendMessage(MM.toComponent("<gray><b>========================================</b></gray>"));
            
            // Mostrar desafíos completados con hover text
            sendChallengeCompletionMessages(player, report, event);
            
            // Mostrar estadísticas principales
            sendMainStatistics(player, report);
            
            // Separador final
            player.sendMessage(MM.toComponent("<gray><b>========================================</b></gray>"));
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "[PlayerStatisticsReportManager] Error enviando reporte a " + player.getName(), e);
        }
    }
    
    /**
     * Envía mensajes de desafíos completados con hover text mostrando progreso.
     * 
     * @param player El jugador
     * @param report El reporte del jugador
     * @param event El evento
     */
    private void sendChallengeCompletionMessages(Player player, PlayerEventReport report, AbstractWeeklyEvent event) {
        Set<String> completedChallenges = report.getCompletedChallenges();
        Map<String, Object> challengeProgress = report.getChallengeProgress();
        
        if (completedChallenges.isEmpty()) {
            player.sendMessage(MM.toComponent("<yellow>No completaste ningún desafío en este evento.</yellow>"));
            return;
        }
        
        player.sendMessage(MM.toComponent("<green><b>DESAFÍOS COMPLETADOS:</b></green>"));
        
        for (String challengeId : completedChallenges) {
            String challengeName = getChallengeDisplayName(challengeId);
            
            // Obtener progreso actual y máximo
            Object currentProgress = challengeProgress.get(challengeId + "_current");
            Object maxProgress = challengeProgress.get(challengeId + "_max");
            
            String progressText = formatProgress(currentProgress, maxProgress);
            
            // Crear mensaje con hover text
            Component hoverText = MM.toComponent(
                "<yellow>Progreso alcanzado:</yellow>\n" +
                "<white>" + progressText + "</white>\n" +
                "<gray>¡Desafío completado exitosamente!</gray>"
            );
            
            Component message = MM.toComponent("<green>✓</green> <yellow>Has completado el desafío " + challengeName + "!</yellow>")
                .hoverEvent(HoverEvent.showText(hoverText));
            
            player.sendMessage(message);
        }
    }
    
    /**
     * Envía las estadísticas principales del jugador.
     * 
     * @param player El jugador
     * @param report El reporte del jugador
     */
    private void sendMainStatistics(Player player, PlayerEventReport report) {
        Map<String, Object> stats = report.getPlayerStatistics();
        
        player.sendMessage(MM.toComponent("<blue><b>ESTADÍSTICAS PRINCIPALES:</b></blue>"));
        
        // Tiempo de sesión
        Object sessionTime = stats.get("session_time");
        if (sessionTime instanceof Number) {
            long timeMs = ((Number) sessionTime).longValue();
            String formattedTime = formatTime(timeMs);
            player.sendMessage(MM.toComponent("<white>⏱ Tiempo en el evento: <aqua>" + formattedTime + "</aqua></white>"));
        }
        
        // Otras estadísticas relevantes
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (!key.equals("session_time") && value instanceof Number) {
                String displayName = getStatisticDisplayName(key);
                player.sendMessage(MM.toComponent("<white>📊 " + displayName + ": <aqua>" + value + "</aqua></white>"));
            }
        }
    }
    
    /**
     * Obtiene el nombre de visualización de un desafío.
     * 
     * @param challengeId ID del desafío
     * @return Nombre para mostrar
     */
    private String getChallengeDisplayName(String challengeId) {
        // Mapeo de IDs a nombres legibles
        Map<String, String> challengeNames = Map.of(
            "long_stay", "Permanencia Prolongada",
            "ghast_killer", "Cazador de Ghasts",
            "mob_head_collector", "Coleccionista de Cabezas",
            "explosion_killer", "Maestro de Explosiones",
            "warden_creeper_killer", "Cazador Élite"
        );
        
        return challengeNames.getOrDefault(challengeId, challengeId);
    }
    
    /**
     * Obtiene el nombre de visualización de una estadística.
     * 
     * @param statKey Clave de la estadística
     * @return Nombre para mostrar
     */
    private String getStatisticDisplayName(String statKey) {
        Map<String, String> statNames = Map.of(
            "zombie_kills", "Zombies eliminados",
            "player_interactions", "Interacciones con jugadores",
            "stat_updates", "Actualizaciones de estadísticas",
            "cured_infections", "Infecciones curadas",
            "red_moon_kills", "Eliminaciones en luna roja"
        );
        
        return statNames.getOrDefault(statKey, statKey.replace("_", " "));
    }
    
    /**
     * Formatea el progreso de un desafío.
     * 
     * @param current Progreso actual
     * @param max Progreso máximo
     * @return Texto formateado del progreso
     */
    private String formatProgress(Object current, Object max) {
        if (current == null || max == null) {
            return "Completado";
        }
        
        return current + "/" + max;
    }
    
    /**
     * Formatea un tiempo en milisegundos a un formato legible.
     * 
     * @param timeMs Tiempo en milisegundos
     * @return Tiempo formateado
     */
    private String formatTime(long timeMs) {
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Obtiene un reporte cacheado para un jugador y evento específico.
     * 
     * @param eventId ID del evento
     * @param playerId ID del jugador
     * @return Reporte cacheado o null si no existe
     */
    public PlayerEventReport getCachedReport(String eventId, UUID playerId) {
        Map<UUID, PlayerEventReport> eventReports = cachedReports.get(eventId);
        return eventReports != null ? eventReports.get(playerId) : null;
    }
    
    /**
     * Limpia reportes cacheados antiguos para liberar memoria.
     * 
     * @param maxAge Edad máxima en milisegundos
     */
    public void cleanupOldReports(long maxAge) {
        long currentTime = System.currentTimeMillis();
        
        cachedReports.entrySet().removeIf(eventEntry -> {
            Map<UUID, PlayerEventReport> reports = eventEntry.getValue();
            return reports.values().stream()
                .anyMatch(report -> (currentTime - report.getGeneratedAt()) > maxAge);
        });
    }
    
    /**
     * Clase interna que representa un reporte de estadísticas de un jugador.
     */
    public static class PlayerEventReport {
        private final UUID playerId;
        private final String eventId;
        private final Map<String, Object> playerStatistics;
        private final Set<String> completedChallenges;
        private final Map<String, Object> challengeProgress;
        private final long generatedAt;
        
        public PlayerEventReport(UUID playerId, String eventId, Map<String, Object> playerStatistics,
                               Set<String> completedChallenges, Map<String, Object> challengeProgress,
                               long generatedAt) {
            this.playerId = playerId;
            this.eventId = eventId;
            this.playerStatistics = new HashMap<>(playerStatistics);
            this.completedChallenges = new HashSet<>(completedChallenges);
            this.challengeProgress = new HashMap<>(challengeProgress);
            this.generatedAt = generatedAt;
        }
        
        // Getters
        public UUID getPlayerId() { return playerId; }
        public String getEventId() { return eventId; }
        public Map<String, Object> getPlayerStatistics() { return new HashMap<>(playerStatistics); }
        public Set<String> getCompletedChallenges() { return new HashSet<>(completedChallenges); }
        public Map<String, Object> getChallengeProgress() { return new HashMap<>(challengeProgress); }
        public long getGeneratedAt() { return generatedAt; }
    }
}