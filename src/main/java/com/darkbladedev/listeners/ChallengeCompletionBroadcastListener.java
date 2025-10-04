package com.darkbladedev.listeners;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.events.ChallengeCompletedEvent;
import com.darkbladedev.utils.MM;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listener que maneja la difusión global de mensajes cuando un jugador completa un desafío.
 * 
 * Este listener escucha el evento ChallengeCompletedEvent y envía un mensaje de difusión
 * a todos los jugadores en línea informando sobre la completación del desafío.
 * 
 * Características:
 * - Mensajes configurables desde config.yml
 * - Formato MiniMessage con gradientes y colores
 * - Validaciones de seguridad
 * - Manejo de errores robusto
 * - Logs para administradores
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class ChallengeCompletionBroadcastListener implements Listener {
    
    private final HeartlessMain plugin;
    
    /**
     * Constructor del listener.
     */
    public ChallengeCompletionBroadcastListener() {
        this.plugin = HeartlessMain.getInstance();
    }
    
    /**
     * Maneja el evento de completación de desafíos y envía un mensaje de difusión.
     * 
     * @param event El evento de completación de desafío
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChallengeCompleted(ChallengeCompletedEvent event) {
        try {
            // Verificar si la difusión está habilitada
            if (!isBroadcastEnabled()) {
                return;
            }
            
            // Validaciones de seguridad
            if (event.getPlayer() == null) {
                plugin.getLogger().warning("ChallengeCompletedEvent recibido con jugador null");
                return;
            }
            
            if (event.getChallengeId() == null || event.getChallengeId().trim().isEmpty()) {
                plugin.getLogger().warning("ChallengeCompletedEvent recibido con challengeId null o vacío");
                return;
            }
            
            if (!event.isPlayerOnline()) {
                plugin.getLogger().info("Jugador " + event.getPlayer().getName() + " no está en línea, omitiendo difusión");
                return;
            }
            
            // Obtener información del evento
            Player player = event.getPlayer();
            String playerName = player.getName();
            String challengeName = event.getChallengeName();
            String eventName = formatEventName(event.getEventName());
            
            // Crear el mensaje de difusión
            String broadcastMessage = createBroadcastMessage(playerName, challengeName, eventName);
            
            // Enviar mensaje a todos los jugadores en línea
            broadcastToAllPlayers(broadcastMessage);
            
            // Log para administradores si está habilitado
            if (isConsoleLogEnabled()) {
                plugin.getLogger().info("Difusión enviada: " + playerName + " completó el desafío '" + challengeName + "' en " + eventName);
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error al procesar ChallengeCompletedEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica si la difusión de completación de desafíos está habilitada.
     * 
     * @return true si está habilitada, false en caso contrario
     */
    private boolean isBroadcastEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("messages.challenge-completion.enabled", true);
    }
    
    /**
     * Verifica si el log en consola está habilitado.
     * 
     * @return true si está habilitado, false en caso contrario
     */
    private boolean isConsoleLogEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("messages.challenge-completion.console-log", true);
    }
    
    /**
     * Crea el mensaje de difusión formateado usando la configuración.
     * 
     * @param playerName Nombre del jugador
     * @param challengeName Nombre del desafío
     * @param eventName Nombre del evento
     * @return El mensaje formateado
     */
    private String createBroadcastMessage(String playerName, String challengeName, String eventName) {
        // Obtener formato desde configuración
        String format = plugin.getConfigManager().getConfig().getString(
            "messages.challenge-completion.format",
            "<gradient:#FFD700:#FFA500><bold>🏆 DESAFÍO COMPLETADO 🏆</bold></gradient>\\n<white>El jugador <yellow><bold>{player}</bold></yellow> ha completado el desafío</white>\\n<gold><bold>\\\"{challenge}\\\"</bold></gold> <white>en</white> <aqua><bold>{event}</bold></aqua><white>!</white>"
        );
        
        // Reemplazar placeholders
        return format
            .replace("{player}", playerName)
            .replace("{challenge}", challengeName)
            .replace("{event}", eventName)
            .replace("\\n", "\n")  // Convertir \\n a saltos de línea reales
            .replace("\\\"", "\""); // Convertir \\\" a comillas reales
    }
    
    /**
     * Formatea el nombre del evento para mostrar de manera más legible.
     * 
     * @param eventName El nombre del evento
     * @return El nombre formateado
     */
    private String formatEventName(String eventName) {
        if (eventName == null || eventName.trim().isEmpty()) {
            return "Evento Desconocido";
        }
        
        // Convertir de UPPER_CASE a Title Case
        String[] words = eventName.toLowerCase().replace("_", " ").split(" ");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                if (formatted.length() > 0) {
                    formatted.append(" ");
                }
                formatted.append(Character.toUpperCase(word.charAt(0)))
                         .append(word.substring(1));
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * Envía el mensaje de difusión a todos los jugadores en línea.
     * 
     * @param message El mensaje a enviar
     */
    private void broadcastToAllPlayers(String message) {
        try {
            // Obtener todos los jugadores en línea
            var onlinePlayers = Bukkit.getOnlinePlayers();
            
            if (onlinePlayers.isEmpty()) {
                if (isConsoleLogEnabled()) {
                    plugin.getLogger().info("No hay jugadores en línea para recibir la difusión");
                }
                return;
            }
            
            // Convertir el mensaje usando MiniMessage
            var component = MM.toComponent(message);
            
            // Enviar a todos los jugadores
            int sentCount = 0;
            for (Player player : onlinePlayers) {
                try {
                    if (player != null && player.isOnline()) {
                        player.sendMessage(component);
                        sentCount++;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error enviando mensaje a " + player.getName() + ": " + e.getMessage());
                }
            }
            
            if (isConsoleLogEnabled()) {
                plugin.getLogger().info("Mensaje de difusión enviado a " + sentCount + " jugadores");
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error crítico al enviar difusión: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: enviar mensaje simple sin formato
            try {
                String fallbackMessage = "🏆 Un jugador ha completado un desafío!";
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player != null && player.isOnline()) {
                        player.sendMessage(fallbackMessage);
                    }
                }
                plugin.getLogger().info("Mensaje de fallback enviado debido a error en formato MiniMessage");
            } catch (Exception fallbackError) {
                plugin.getLogger().severe("Error en fallback de difusión: " + fallbackError.getMessage());
            }
        }
    }
}