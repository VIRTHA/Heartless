package com.darkbladedev.listeners;

import com.darkbladedev.events.ChallengeProgressUpdateEvent;
import com.darkbladedev.utils.MM;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listener que maneja las notificaciones de progreso de desafíos en tiempo real.
 * Envía mensajes actualizados al jugador cuando su progreso cambia.
 */
public class ChallengeProgressNotificationListener implements Listener {
    
    /**
     * Maneja los eventos de actualización de progreso de desafíos.
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
        
        // Crear el mensaje de progreso
        if (isCompleted) {
            // Mensaje de completado
            Component completedMessage = MM.toComponent(
                "<green>✓ Desafío completado: <yellow>" + challengeName + "</yellow> " +
                "<gray>(" + currentProgress + "/" + maxProgress + ")</gray></green>"
            );
            player.sendMessage(completedMessage);
        } else {
            // Mensaje de progreso actualizado
            String progressColor = getProgressColor(currentProgress, maxProgress);
            
            // Crear hover text con detalles
            String hoverText = "<yellow>Desafío: " + challengeName + "</yellow>\n" +
                             "<gray>Progreso: " + progressColor + currentProgress + "/" + maxProgress + "</gray>\n" +
                             "<gray>Restante: <white>" + (maxProgress - currentProgress) + "</white></gray>";
            
            Component hoverComponent = MM.toComponent(hoverText);
            
            // Mensaje principal con hover
            Component progressMessage = MM.toComponent(
                "<yellow>⚡ Progreso actualizado: " + progressColor + currentProgress + "/" + maxProgress + "</yellow>"
            ).hoverEvent(HoverEvent.showText(hoverComponent));
            
            player.sendMessage(progressMessage);
        }
        
        // Enviar sonido de notificación
        if (isCompleted) {
            player.playSound(player.getLocation(), "entity.player.levelup", 0.7f, 1.2f);
        } else {
            player.playSound(player.getLocation(), "block.note_block.pling", 0.5f, 1.5f);
        }
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