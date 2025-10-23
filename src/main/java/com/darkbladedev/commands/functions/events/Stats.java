package com.darkbladedev.commands.functions.events;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.mechanics.AbstractWeeklyEvent;
import com.darkbladedev.utils.MM;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Comando para que los jugadores puedan ver sus estadísticas personales durante un evento activo.
 * Permite consultar estadísticas en tiempo real sin esperar al final del evento.
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class Stats implements SubcommandExecutor {
    
    
    private boolean enabled = true;

    public Stats() {
        // Los managers se obtendrán dinámicamente para evitar problemas de inicialización
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MM.toComponent("<red>Este comando solo puede ser ejecutado por jugadores.</red>"));
            return;
        }

        Player player = (Player) sender;
        HeartlessMain plugin = HeartlessMain.getInstance();
        WeeklyEventManager eventManager = plugin.getWeeklyEventManager();

        // Verificar si hay un evento activo
        if (!eventManager.isEventActive()) {
            player.sendMessage(MM.toComponent("<yellow>📊 No hay ningún evento activo en este momento.</yellow>"));
            return;
        }

        // Obtener el evento actual
        AbstractWeeklyEvent currentEvent = eventManager.getCurrentEvent();
        if (currentEvent == null) {
            player.sendMessage(MM.toComponent("<red>❌ Error al obtener información del evento actual.</red>"));
            return;
        }

        // Mostrar estadísticas del jugador
        showPlayerStatistics(player, currentEvent);
    }

    /**
     * Muestra las estadísticas personales del jugador para el evento actual
     */
    private void showPlayerStatistics(Player player, AbstractWeeklyEvent event) {
        UUID playerId = player.getUniqueId();
        
        // Obtener estadísticas del jugador
        Map<String, Object> playerStats = event.getPlayerStatistics(playerId);
        if (playerStats == null || playerStats.isEmpty()) {
            player.sendMessage(MM.toComponent("<yellow>📊 Aún no tienes estadísticas registradas en este evento.</yellow>"));
            return;
        }

        // Obtener información de desafíos
        Set<String> completedChallenges = event.getCompletedChallenges(playerId);
        Map<String, AbstractWeeklyEvent.ChallengeDefinition> availableChallenges = event.getAvailableChallenges();
        
        // Calcular tiempo de sesión usando reflexión para acceder a playerJoinTimes
        long sessionTime = calculatePlayerSessionTime(event, playerId);
        String sessionTimeFormatted = formatTime(sessionTime);

        // === ENCABEZADO ===
        String eventName = getEventDisplayName(event.getId());
        player.sendMessage(MM.toComponent("<gray>════════════════════════════════</gray>"));
        player.sendMessage(MM.toComponent("<green><b>📊 ESTADÍSTICAS PERSONALES</b></green>"));
        player.sendMessage(MM.toComponent("<yellow>Evento: <white>" + eventName + "</white></yellow>"));
        player.sendMessage(MM.toComponent("<yellow>Tiempo de participación: <white>" + sessionTimeFormatted + "</white></yellow>"));
        player.sendMessage(MM.toComponent(""));

        // === ESTADÍSTICAS GENERALES ===
        player.sendMessage(MM.toComponent("<aqua><b>📈 TUS ESTADÍSTICAS:</b></aqua>"));
        
        // Mostrar estadísticas ordenadas
        List<Map.Entry<String, Object>> sortedStats = new ArrayList<>(playerStats.entrySet());
        sortedStats.sort(Map.Entry.comparingByKey());
        
        for (Map.Entry<String, Object> entry : sortedStats) {
            String statName = entry.getKey();
            Object statValue = entry.getValue();
            
            // Formatear nombre de estadística
            String formattedName = formatStatisticName(statName);
            String formattedValue = formatStatisticValue(statValue);
            
            player.sendMessage(MM.toComponent("<gray>• <white>" + formattedName + ": <yellow>" + formattedValue + "</yellow></white></gray>"));
        }
        
        player.sendMessage(MM.toComponent(""));

        // === PROGRESO DE DESAFÍOS ===
        player.sendMessage(MM.toComponent("<gold><b>🏆 PROGRESO DE DESAFÍOS:</b></gold>"));
        
        // Obtener el progreso actual de todos los desafíos
        Map<String, Object> challengeProgress = event.getChallengeProgress(playerId);
        
        if (availableChallenges == null || availableChallenges.isEmpty()) {
            player.sendMessage(MM.toComponent("<gray>No hay desafíos disponibles para este evento.</gray>"));
        } else {
            for (Map.Entry<String, AbstractWeeklyEvent.ChallengeDefinition> entry : availableChallenges.entrySet()) {
                String challengeId = entry.getKey();
                AbstractWeeklyEvent.ChallengeDefinition challenge = entry.getValue();
                
                boolean isCompleted = completedChallenges != null && completedChallenges.contains(challengeId);
                
                // Obtener progreso actual manejando el tipo Object
                int currentProgress = 0;
                if (challengeProgress != null) {
                    Object progressObj = challengeProgress.get(challengeId + "_current");
                    if (progressObj instanceof Number) {
                        currentProgress = ((Number) progressObj).intValue();
                    }
                }
                
                int targetProgress = challenge.getRequiredProgress();
                
                String status = isCompleted ? "<green>✓</green>" : "<red>✗</red>";
                String challengeName = challenge.getDisplayName();
                String progressText = "<yellow>" + currentProgress + "/" + targetProgress + "</yellow>";
                
                player.sendMessage(MM.toComponent("<gray>• " + status + " <white>" + challengeName + "</white> " + progressText + "</gray>"));
            }
            
            // Mostrar resumen
            int totalChallenges = availableChallenges.size();
            int completedCount = completedChallenges != null ? completedChallenges.size() : 0;
            player.sendMessage(MM.toComponent(""));
            player.sendMessage(MM.toComponent("<gold>Total completados: <white>" + completedCount + "/" + totalChallenges + "</white></gold>"));
        }
        
        // === RESUMEN ===
        int totalChallenges = availableChallenges != null ? availableChallenges.size() : 0;
        int completedCount = completedChallenges != null ? completedChallenges.size() : 0;
        
        player.sendMessage(MM.toComponent(""));
        player.sendMessage(MM.toComponent("<blue><b>📋 RESUMEN:</b></blue>"));
        player.sendMessage(MM.toComponent("<gray>Total completados: <yellow>" + completedCount + "/" + totalChallenges + "</yellow></gray>"));
        
        if (totalChallenges > 0) {
            double percentage = (double) completedCount / totalChallenges * 100;
            player.sendMessage(MM.toComponent("<gray>Progreso: <yellow>" + String.format("%.1f", percentage) + "%</yellow></gray>"));
        }
        
        player.sendMessage(MM.toComponent("<gray>════════════════════════════════</gray>"));
    }
    
    /**
     * Calcula el tiempo de sesión del jugador usando reflexión para acceder a playerJoinTimes
     */
    private long calculatePlayerSessionTime(AbstractWeeklyEvent event, UUID playerId) {
        try {
            // Usar reflexión para acceder al campo protegido playerJoinTimes de WeeklyEvent
            java.lang.reflect.Field field = event.getClass().getSuperclass().getDeclaredField("playerJoinTimes");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Long> playerJoinTimes = (Map<UUID, Long>) field.get(event);
            
            Long joinTime = playerJoinTimes.get(playerId);
            if (joinTime != null) {
                return System.currentTimeMillis() - joinTime;
            }
        } catch (Exception e) {
             // Si falla la reflexión, intentar obtener de las estadísticas del jugador
             Map<String, Object> playerStats = event.getPlayerStatistics(playerId);
             if (playerStats != null && playerStats.containsKey("join_time")) {
                 Object joinTime = playerStats.get("join_time");
                 if (joinTime instanceof Long) {
                     return System.currentTimeMillis() - (Long) joinTime;
                 }
             }
         }
        
        return 0L; // Tiempo desconocido
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
     * Muestra estadísticas de la Semana Ácida
     */
    private void displayAcidWeekStats(Player player, Map<String, Object> playerStats) {
        Object acidDamage = playerStats.get("acid_damage_taken");
        Object blocksAffected = playerStats.get("blocks_affected_by_acid");
        Object acidResistanceTime = playerStats.get("acid_resistance_time");

        if (acidDamage != null) {
            player.sendMessage(MM.toComponent("<green>🧪 Daño por ácido recibido: <white>" + acidDamage + "</white></green>"));
        }
        if (blocksAffected != null) {
            player.sendMessage(MM.toComponent("<yellow>🧱 Bloques afectados por ácido: <white>" + blocksAffected + "</white></yellow>"));
        }
        if (acidResistanceTime != null) {
            player.sendMessage(MM.toComponent("<blue>⏱️ Tiempo con resistencia al ácido: <white>" + formatTime(((Number) acidResistanceTime).longValue()) + "</white></blue>"));
        }
    }

    /**
     * Muestra estadísticas de la Semana de Sangre y Hierro
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
     * Muestra estadísticas de la Semana Explosiva
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
     * Muestra estadísticas de la Semana de No-Muertos
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
            String key = entry.getKey();
            Object value = entry.getValue();
            String displayKey = formatStatKey(key);
            player.sendMessage(MM.toComponent("<white>• " + displayKey + ": <yellow>" + value + "</yellow></white>"));
        }
    }

    /**
     * Muestra el progreso de desafíos del jugador
     */
    private void displayChallengeProgress(Player player, Set<String> completedChallenges, 
                                        Map<String, AbstractWeeklyEvent.ChallengeDefinition> availableChallenges) {
        // Este método ya no se usa, el progreso se muestra directamente en showPlayerStatistics
        // Se mantiene por compatibilidad pero no se llama
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
     * Formatea el nombre de una estadística para mostrar
     */
    private String formatStatisticName(String key) {
        String formatted = key.replace("_", " ");
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }

    /**
     * Formatea el valor de una estadística para mostrar
     */
    private String formatStatisticValue(Object value) {
        if (value instanceof Number) {
            return String.format("%,.0f", ((Number) value).doubleValue());
        }
        return String.valueOf(value);
    }

    /**
     * Formatea una clave de estadística para mostrar
     */
    private String formatStatKey(String key) {
        return key.replace("_", " ")
                 .substring(0, 1).toUpperCase() + key.replace("_", " ").substring(1);
    }

    /**
     * Formatea tiempo en milisegundos a formato legible
     */
    private String formatTime(long timeInMillis) {
        long seconds = timeInMillis / 1000;
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
}