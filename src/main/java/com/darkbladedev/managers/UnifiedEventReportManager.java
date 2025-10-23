package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.AbstractWeeklyEvent;
import com.darkbladedev.utils.MM;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.darkbladedev.models.UnifiedEventReport;

/**
 * Manager unificado para generar y mostrar reportes de finalización de eventos semanales.
 * Elimina la duplicación de mensajes y proporciona una plantilla consistente.
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class UnifiedEventReportManager {
    
    private final Logger logger;
    
    // Cache para evitar regeneración de reportes
    private final Map<String, Map<UUID, UnifiedEventReport>> cachedReports = new ConcurrentHashMap<>();
    
    public UnifiedEventReportManager(HeartlessMain plugin) {
        this.logger = plugin.getLogger();
    }
    
    /**
     * Genera y envía el reporte unificado de finalización del evento a todos los jugadores participantes.
     * Este método reemplaza todos los sistemas de mensajes duplicados.
     * 
     * @param event El evento que ha finalizado
     */
    public void generateAndSendUnifiedEventReport(AbstractWeeklyEvent event) {
        if (event == null) {
            logger.warning("[UnifiedEventReportManager] Evento nulo proporcionado para generar reporte");
            return;
        }
        
        String eventId = event.getId();
        logger.info("[UnifiedEventReportManager] Generando reporte unificado para evento: " + eventId);
        
        try {
            // Anunciar finalización del evento globalmente
            announceEventCompletion(event);
            
            // Generar y enviar reportes individuales
            Set<UUID> participants = event.getAllPlayersWithStatistics();
            if (participants.isEmpty()) {
                logger.info("[UnifiedEventReportManager] No hay participantes para el evento: " + eventId);
                return;
            }
            
            Map<UUID, UnifiedEventReport> reports = new ConcurrentHashMap<>();
            
            // Generar reportes para cada participante
            for (UUID playerId : participants) {
                try {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        UnifiedEventReport report = generateUnifiedReport(playerId, event);
                        if (report != null) {
                            reports.put(playerId, report);
                            sendUnifiedReport(player, report, event);
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "[UnifiedEventReportManager] Error generando reporte para jugador " + playerId, e);
                }
            }
            
            // Cachear reportes generados
            cachedReports.put(eventId, reports);
            
            logger.info("[UnifiedEventReportManager] Reportes enviados a " + reports.size() + " jugadores para evento: " + eventId);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[UnifiedEventReportManager] Error crítico generando reportes para evento: " + eventId, e);
        }
    }
    
    /**
     * Anuncia la finalización del evento de manera global
     */
    private void announceEventCompletion(AbstractWeeklyEvent event) {
        String eventName = getEventDisplayName(event.getId());
        Component announcement = MM.toComponent(
            event.getPrefix() + " <green>¡" + eventName + " ha terminado!</green>"
        );
        Bukkit.broadcast(announcement);
    }
    
    /**
     * Genera un reporte unificado para un jugador específico
     */
    private UnifiedEventReport generateUnifiedReport(UUID playerId, AbstractWeeklyEvent event) {
        try {
            // Obtener estadísticas del jugador
            Map<String, Object> playerStats = event.getPlayerStatistics(playerId);
            if (playerStats == null) {
                playerStats = new HashMap<>();
            }
            
            // Obtener desafíos completados
            Set<String> completedChallenges = event.getCompletedChallenges(playerId);
            if (completedChallenges == null) {
                completedChallenges = new HashSet<>();
            }
            
            // Obtener progreso de desafíos
            Map<String, Object> challengeProgress = event.getChallengeProgress(playerId);
            if (challengeProgress == null) {
                challengeProgress = new HashMap<>();
            }
            
            // Obtener desafíos disponibles
            Map<String, AbstractWeeklyEvent.ChallengeDefinition> availableChallenges = event.getAvailableChallenges();
            
            return new UnifiedEventReport(
                playerId,
                event.getId(),
                playerStats,
                completedChallenges,
                challengeProgress,
                availableChallenges,
                System.currentTimeMillis()
            );
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "[UnifiedEventReportManager] Error generando reporte para jugador " + playerId, e);
            return null;
        }
    }
    
    /**
     * Envía el reporte unificado a un jugador específico
     */
    private void sendUnifiedReport(Player player, UnifiedEventReport report, AbstractWeeklyEvent event) {
        try {
            // === ENCABEZADO DEL REPORTE ===
            sendReportHeader(player, event);
            
            // === SECCIÓN DE DESAFÍOS COMPLETADOS ===
            sendChallengeSection(player, report, event);
            
            // === FOOTER CON ESTADÍSTICAS INDIVIDUALES ===
            sendStatisticsFooter(player, report, event);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "[UnifiedEventReportManager] Error enviando reporte a " + player.getName(), e);
        }
    }
    
    /**
     * Envía el encabezado del reporte con título claro y descriptivo
     */
    private void sendReportHeader(Player player, AbstractWeeklyEvent event) {
        String eventName = getEventDisplayName(event.getId());
        
        player.sendMessage(MM.toComponent("<gray><b>════════════════════════════════</b></gray>"));
        player.sendMessage(MM.toComponent("<green><b>REPORTE FINAL - " + eventName.toUpperCase() + "</b></green>"));
        player.sendMessage(MM.toComponent("<gray><b>════════════════════════════════</b></gray>"));
        player.sendMessage(MM.toComponent(""));
    }
    
    /**
     * Envía la sección de desafíos con información detallada
     */
    private void sendChallengeSection(Player player, UnifiedEventReport report, AbstractWeeklyEvent event) {
        player.sendMessage(MM.toComponent("<yellow><b>📋 DESAFÍOS COMPLETADOS:</b></yellow>"));
        player.sendMessage(MM.toComponent(""));
        
        Map<String, AbstractWeeklyEvent.ChallengeDefinition> availableChallenges = report.getAvailableChallenges();
        Set<String> completedChallenges = report.getCompletedChallenges();
        Map<String, Object> challengeProgress = report.getChallengeProgress();
        
        int totalChallenges = availableChallenges.size();
        int completedCount = completedChallenges.size();
        
        // Mostrar cada desafío con su estado
        for (Map.Entry<String, AbstractWeeklyEvent.ChallengeDefinition> entry : availableChallenges.entrySet()) {
            String challengeId = entry.getKey();
            AbstractWeeklyEvent.ChallengeDefinition challenge = entry.getValue();
            
            boolean isCompleted = completedChallenges.contains(challengeId);
            String status = isCompleted ? "<green>✓</green>" : "<red>✗</red>";
            String challengeName = challenge.getDisplayName();
            
            // Obtener progreso real del jugador
            Object currentProgress = challengeProgress.getOrDefault(challengeId + "_current", 0);
            Object maxProgress = challengeProgress.getOrDefault(challengeId + "_max", challenge.getRequiredProgress());
            
            // Crear hover text con descripción y progreso
            String hoverText = "<yellow>Descripción:</yellow>\n<gray>" + challenge.getDescription() + "</gray>\n\n" +
                              "<yellow>Progreso:</yellow> <white>" + currentProgress + "/" + maxProgress + "</white>";
            
            // Crear mensaje con hover para mostrar descripción y progreso
            Component challengeComponent = MM.toComponent(
                "  " + status + " <white>" + challengeName + "</white>"
            ).hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
            
            player.sendMessage(challengeComponent);
        }
        
        player.sendMessage(MM.toComponent(""));
        player.sendMessage(MM.toComponent("<gold>Total completados: <white>" + completedCount + "/" + totalChallenges + "</white></gold>"));
        player.sendMessage(MM.toComponent(""));
    }
    
    /**
     * Envía el footer con estadísticas individuales del jugador
     */
    private void sendStatisticsFooter(Player player, UnifiedEventReport report, AbstractWeeklyEvent event) {
        player.sendMessage(MM.toComponent("<blue><b>📊 TUS ESTADÍSTICAS PERSONALES:</b></blue>"));
        player.sendMessage(MM.toComponent(""));
        
        Map<String, Object> playerStats = report.getPlayerStatistics();
        
        // Mostrar estadísticas específicas del evento
        displayEventSpecificStats(player, playerStats, event.getId());
        
        player.sendMessage(MM.toComponent(""));
        player.sendMessage(MM.toComponent("<gold>═══════════════════════════════════</gold>"));
    }
    
    /**
     * Muestra estadísticas específicas según el tipo de evento
     */
    private void displayEventSpecificStats(Player player, Map<String, Object> playerStats, String eventId) {
        switch (eventId.toLowerCase()) {
            case "acid_week":
                displayAcidWeekStats(player, playerStats);
                break;
            case "blood_and_iron_week":
                displayBloodAndIronStats(player, playerStats);
                break;
            case "explosive_week":
                displayExplosiveWeekStats(player, playerStats);
                break;
            case "undead_week":
                displayUndeadWeekStats(player, playerStats);
                break;
            default:
                displayGenericStats(player, playerStats);
                break;
        }
    }
    
    /**
     * Muestra estadísticas específicas de la Semana Ácida
     */
    private void displayAcidWeekStats(Player player, Map<String, Object> playerStats) {
        Object acidDamage = playerStats.get("acid_damage_received");
        Object fishCollected = playerStats.get("fish_types_collected");
        Object rainSurvivalTime = playerStats.get("rain_survival_time");
        
        if (acidDamage != null) {
            player.sendMessage(MM.toComponent("<yellow>🧪 Daño ácido recibido: <white>" + acidDamage + "</white></yellow>"));
        }
        
        if (fishCollected != null) {
            player.sendMessage(MM.toComponent("<blue>🐟 Tipos de pescado recolectados: <white>" + fishCollected + "/4</white></blue>"));
        }
        
        if (rainSurvivalTime != null) {
            player.sendMessage(MM.toComponent("<green>🌧️ Tiempo bajo lluvia ácida: <white>" + formatTime(((Number) rainSurvivalTime).longValue()) + "</white></green>"));
        }
    }
    
    /**
     * Muestra estadísticas específicas de la Semana de Sangre y Hierro
     */
    private void displayBloodAndIronStats(Player player, Map<String, Object> playerStats) {
        Object mobsKilled = playerStats.get("mobs_killed");
        Object damageDealt = playerStats.get("damage_dealt");
        Object ironCollected = playerStats.get("iron_collected");
        
        if (mobsKilled != null) {
            player.sendMessage(MM.toComponent("<red>⚔️ Mobs eliminados: <white>" + mobsKilled + "</white></red>"));
        }
        
        if (damageDealt != null) {
            player.sendMessage(MM.toComponent("<dark_red>💥 Daño total infligido: <white>" + damageDealt + "</white></dark_red>"));
        }
        
        if (ironCollected != null) {
            player.sendMessage(MM.toComponent("<gray>⛏️ Hierro recolectado: <white>" + ironCollected + "</white></gray>"));
        }
    }
    
    /**
     * Muestra estadísticas específicas de la Semana Explosiva
     */
    private void displayExplosiveWeekStats(Player player, Map<String, Object> playerStats) {
        Object explosionsTriggered = playerStats.get("explosions_triggered");
        Object blocksDestroyed = playerStats.get("blocks_destroyed");
        Object tntUsed = playerStats.get("tnt_used");
        
        if (explosionsTriggered != null) {
            player.sendMessage(MM.toComponent("<gold>💣 Explosiones activadas: <white>" + explosionsTriggered + "</white></gold>"));
        }
        
        if (blocksDestroyed != null) {
            player.sendMessage(MM.toComponent("<yellow>🧱 Bloques destruidos: <white>" + blocksDestroyed + "</white></yellow>"));
        }
        
        if (tntUsed != null) {
            player.sendMessage(MM.toComponent("<red>🧨 TNT utilizada: <white>" + tntUsed + "</white></red>"));
        }
    }
    
    /**
     * Muestra estadísticas específicas de la Semana de No-Muertos
     */
    private void displayUndeadWeekStats(Player player, Map<String, Object> playerStats) {
        Object undeadKilled = playerStats.get("undead_killed");
        Object nightsSurvived = playerStats.get("nights_survived");
        Object bonesCollected = playerStats.get("bones_collected");
        
        if (undeadKilled != null) {
            player.sendMessage(MM.toComponent("<dark_purple>🧟 No-muertos eliminados: <white>" + undeadKilled + "</white></dark_purple>"));
        }
        
        if (nightsSurvived != null) {
            player.sendMessage(MM.toComponent("<dark_blue>🌙 Noches sobrevividas: <white>" + nightsSurvived + "</white></dark_blue>"));
        }
        
        if (bonesCollected != null) {
            player.sendMessage(MM.toComponent("<white>🦴 Huesos recolectados: <white>" + bonesCollected + "</white></white>"));
        }
    }
    
    /**
     * Muestra estadísticas genéricas para eventos no específicos
     */
    private void displayGenericStats(Player player, Map<String, Object> playerStats) {
        for (Map.Entry<String, Object> entry : playerStats.entrySet()) {
            String statName = getStatisticDisplayName(entry.getKey());
            Object value = entry.getValue();
            
            player.sendMessage(MM.toComponent("<gray>📈 " + statName + ": <white>" + value + "</white></gray>"));
        }
    }
    
    /**
     * Obtiene el nombre de visualización del evento
     */
    private String getEventDisplayName(String eventId) {
        switch (eventId.toLowerCase()) {
            case "acid_week":
                return "Semana Ácida";
            case "blood_and_iron_week":
                return "Semana de Sangre y Hierro";
            case "explosive_week":
                return "Semana Explosiva";
            case "undead_week":
                return "Semana de No-Muertos";
            default:
                return "Evento Semanal";
        }
    }
    
    /**
     * Obtiene el nombre de visualización de una estadística
     */
    private String getStatisticDisplayName(String statKey) {
        switch (statKey.toLowerCase()) {
            case "acid_damage_received":
                return "Daño ácido recibido";
            case "fish_types_collected":
                return "Tipos de pescado recolectados";
            case "rain_survival_time":
                return "Tiempo bajo lluvia ácida";
            case "mobs_killed":
                return "Mobs eliminados";
            case "damage_dealt":
                return "Daño total infligido";
            case "iron_collected":
                return "Hierro recolectado";
            case "explosions_triggered":
                return "Explosiones activadas";
            case "blocks_destroyed":
                return "Bloques destruidos";
            case "tnt_used":
                return "TNT utilizada";
            case "undead_killed":
                return "No-muertos eliminados";
            case "nights_survived":
                return "Noches sobrevividas";
            case "bones_collected":
                return "Huesos recolectados";
            default:
                return statKey.replace("_", " ");
        }
    }
    
    /**
     * Formatea tiempo en milisegundos a formato legible
     */
    private String formatTime(long timeMs) {
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Limpia reportes antiguos del cache
     */
    public void cleanupOldReports(long maxAge) {
        long currentTime = System.currentTimeMillis();
        cachedReports.entrySet().removeIf(entry -> {
            Map<UUID, UnifiedEventReport> reports = entry.getValue();
            return reports.values().stream()
                .anyMatch(report -> (currentTime - report.getGeneratedAt()) > maxAge);
        });
    }
    
}