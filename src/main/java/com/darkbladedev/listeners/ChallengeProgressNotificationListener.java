package com.darkbladedev.listeners;

import com.darkbladedev.events.ChallengeProgressUpdateEvent;
import com.darkbladedev.utils.MM;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listener que maneja las notificaciones de progreso de desafíos en tiempo real.
 * Registra internamente el progreso pero oculta mensajes repetitivos del chat.
 * Solo muestra mensajes de completado para evitar saturar al jugador.
 */
public class ChallengeProgressNotificationListener implements Listener {
    
    /**
     * Maneja los eventos de actualización de progreso de desafíos.
     * Oculta mensajes de progreso repetitivos pero mantiene notificaciones de completado.
     * 
     * @param event El evento de actualización de progreso
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChallengeProgressUpdate(ChallengeProgressUpdateEvent event) {
        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }
        
        String challengeId = event.getChallengeId();
        int currentProgress = event.getCurrentProgressAsInt();
        int maxProgress = event.getMaxProgressAsInt();
        boolean isCompleted = event.isCompleted();
        
        // Determinar el nombre del desafío para mostrar
        String challengeName = getChallengeDisplayName(challengeId);
        
        // Solo mostrar mensajes cuando el desafío se completa
        // Los mensajes de progreso se ocultan para evitar spam
        if (isCompleted) {
            // Mensaje de completado (se mantiene visible)
            Component completedMessage = MM.toComponent(
                "<green>✓ Desafío completado: <yellow>" + challengeName + "</yellow> " +
                "<gray>(" + currentProgress + "/" + maxProgress + ")</gray></green>"
            );
            player.sendMessage(completedMessage);
            
            // Sonido de completado
            player.playSound(player.getLocation(), "entity.player.levelup", 0.7f, 1.2f);
        }
        
        // NOTA: Los mensajes de progreso intermedio se han ocultado intencionalmente
        // para evitar saturar el chat del jugador con notificaciones repetitivas.
        // El progreso sigue siendo registrado internamente por el sistema de eventos.
        
        // El registro interno del progreso se mantiene automáticamente a través del
        // sistema de eventos de AbstractWeeklyEvent.updateChallengeProgress()
    }
    
    /**
     * Obtiene el nombre de visualización del desafío.
     * 
     * @param challengeId ID del desafío
     * @return Nombre formateado del desafío
     */
    private String getChallengeDisplayName(String challengeId) {
        switch (challengeId) {
            case "mob_head_collector":
                return "Coleccionista de Cabezas";
            case "explosion_kill_master":
                return "Maestro de Explosiones";
            case "acid_rain_survivor":
                return "Superviviente de Lluvia Ácida";
            default:
                // Convertir snake_case a formato legible
                String[] words = challengeId.replace("_", " ").toLowerCase().split(" ");
                StringBuilder result = new StringBuilder();
                for (String word : words) {
                    if (word.length() > 0) {
                        result.append(Character.toUpperCase(word.charAt(0)))
                              .append(word.substring(1))
                              .append(" ");
                    }
                }
                return result.toString().trim();
        }
    }
    
    /**
     * Obtiene el color del progreso basado en el porcentaje completado.
     * 
     * @param current Progreso actual
     * @param max Progreso máximo
     * @return Color MiniMessage para el progreso
     */
    private String getProgressColor(int current, int max) {
        if (max <= 0) return "<gray>";
        
        double percentage = (double) current / max;
        
        if (percentage >= 1.0) {
            return "<green>";
        } else if (percentage >= 0.75) {
            return "<yellow>";
        } else if (percentage >= 0.5) {
            return "<gold>";
        } else if (percentage > 0) {
            return "<red>";
        } else {
            return "<dark_gray>";
        }
    }
}