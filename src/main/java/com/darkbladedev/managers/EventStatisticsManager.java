package com.darkbladedev.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.darkbladedev.HeartlessMain;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import com.darkbladedev.models.EventStatistics;

/**
 * Gestor de estadísticas de eventos que recopila, formatea y publica
 * estadísticas relevantes al finalizar cada evento semanal.
 */
public class EventStatisticsManager {
    
    @SuppressWarnings("unused")
    private final HeartlessMain plugin;
    private final Logger logger;
    
    // Estadísticas por evento
    private final Map<String, EventStatistics> eventStats = new ConcurrentHashMap<>();
    
    public EventStatisticsManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Inicia el seguimiento de estadísticas para un evento.
     */
    public void startTracking(String eventName, String eventType) {
        EventStatistics stats = new EventStatistics(eventName, eventType);
        eventStats.put(eventName, stats);
        logger.info("Iniciado seguimiento de estadísticas para evento: " + eventName);
    }
    
    /**
     * Detiene el seguimiento y publica las estadísticas finales.
     */
    public void stopTrackingAndPublish(String eventName) {
        EventStatistics stats = eventStats.remove(eventName);
        if (stats != null) {
            stats.endEvent();
            publishStatistics(stats);
            logger.info("Publicadas estadísticas finales para evento: " + eventName);
        }
    }
    
    /**
     * Registra la participación de un jugador en el evento.
     */
    public void recordPlayerParticipation(String eventName, UUID playerId, String playerName) {
        EventStatistics stats = eventStats.get(eventName);
        if (stats != null) {
            stats.addParticipant(playerId, playerName);
        }
    }
    
    /**
     * Registra una acción específica del evento.
     */
    public void recordEventAction(String eventName, String actionType, Object... details) {
        EventStatistics stats = eventStats.get(eventName);
        if (stats != null) {
            stats.recordAction(actionType, details);
        }
    }
    
    /**
     * Registra una muerte relacionada con el evento.
     */
    public void recordEventDeath(String eventName, UUID playerId, String cause) {
        EventStatistics stats = eventStats.get(eventName);
        if (stats != null) {
            stats.recordDeath(playerId, cause);
        }
    }
    
    /**
     * Registra daño causado por el evento.
     */
    public void recordEventDamage(String eventName, UUID playerId, double damage) {
        EventStatistics stats = eventStats.get(eventName);
        if (stats != null) {
            stats.recordDamage(playerId, damage);
        }
    }
    
    /**
     * Publica las estadísticas del evento automáticamente
     * @param event Evento que finalizó
     * @param eventType Tipo de evento
     * @param duration Duración del evento en milisegundos
     * @param forced Si el evento fue forzado a detenerse
     */
    public void publishEventStatistics(Object event, Object eventType, long duration, boolean forced) {
        String eventName = eventType != null ? eventType.toString() : "Evento Desconocido";
        EventStatistics stats = eventStats.get(eventName);
        
        if (stats != null) {
            stats.endEvent();
            publishStatistics(stats);
            
            // Log adicional si fue forzado
            if (forced) {
                logger.warning("[ESTADÍSTICAS] Evento " + eventName + " fue detenido forzosamente");
            }
        } else {
            logger.warning("[ESTADÍSTICAS] No se encontraron estadísticas para el evento: " + eventName);
        }
    }
    
    /**
     * Publica las estadísticas a todos los participantes del evento.
     */
    private void publishStatistics(EventStatistics stats) {
        List<Component> messages = formatStatistics(stats);
        
        // Enviar a todos los jugadores online
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("htl.access")) {
                for (Component message : messages) {
                    player.sendMessage(message);
                }
            }
        }
        
        // Log en consola
        logger.info("[ESTADÍSTICAS] Publicadas estadísticas del evento: " + stats.eventName);
    }
    
    /**
     * Formatea las estadísticas en componentes de texto legibles.
     */
    private List<Component> formatStatistics(EventStatistics stats) {
        List<Component> messages = new ArrayList<>();
        
        // Encabezado
        messages.add(Component.text("═══════════════════════════════════════")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));
        
        messages.add(Component.text("📊 ESTADÍSTICAS DEL EVENTO")
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD));
        
        messages.add(Component.text("Evento: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(stats.getEventName())
                        .color(NamedTextColor.WHITE)
                        .decorate(TextDecoration.BOLD)));
        
        messages.add(Component.text("Tipo: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(stats.getEventType())
                        .color(NamedTextColor.AQUA)));
        
        // Duración
        Duration duration = stats.getDuration();
        String durationStr = String.format("%02d:%02d:%02d", 
                duration.toHours(), 
                duration.toMinutesPart(), 
                duration.toSecondsPart());
        
        messages.add(Component.text("Duración: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(durationStr)
                        .color(NamedTextColor.GREEN)));
        
        // Participantes
        messages.add(Component.text("Participantes: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(stats.getParticipants().size()))
                        .color(NamedTextColor.YELLOW)));
        
        // Estadísticas específicas del evento
        messages.add(Component.text("═══ Estadísticas Detalladas ═══")
                .color(NamedTextColor.GOLD));
        
        // Muertes totales
        messages.add(Component.text("💀 Muertes totales: ")
                .color(NamedTextColor.RED)
                .append(Component.text(String.valueOf(stats.getTotalDeaths()))
                        .color(NamedTextColor.WHITE)));
        
        // Daño total
        messages.add(Component.text("⚔ Daño total causado: ")
                .color(NamedTextColor.DARK_RED)
                .append(Component.text(String.format("%.1f", stats.getTotalDamage()))
                        .color(NamedTextColor.WHITE)));
        
        // Acciones del evento
        messages.add(Component.text("🎯 Acciones del evento: ")
                .color(NamedTextColor.BLUE)
                .append(Component.text(String.valueOf(stats.getTotalActions()))
                        .color(NamedTextColor.WHITE)));
        
        // Top participantes por daño recibido
        List<Map.Entry<UUID, Double>> topDamage = stats.getTopDamageReceivers(5);
        if (!topDamage.isEmpty()) {
            messages.add(Component.text("🏆 Top Supervivientes (menos daño):")
                    .color(NamedTextColor.GOLD));
            
            for (int i = 0; i < topDamage.size(); i++) {
                Map.Entry<UUID, Double> entry = topDamage.get(i);
                String playerName = stats.getParticipants().get(entry.getKey());
                
                messages.add(Component.text(String.format("  %d. %s - %.1f daño", 
                        i + 1, playerName, entry.getValue()))
                        .color(NamedTextColor.WHITE));
            }
        }
        
        // Pie de página
        messages.add(Component.text("═══════════════════════════════════════")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));
        
        messages.add(Component.text("Gracias por participar en el evento!")
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.ITALIC));
        
        return messages;
    }
    
}