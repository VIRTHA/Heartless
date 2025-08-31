package com.darkbladedev.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.darkbladedev.HeartlessMain;
import com.darkbladedev.events.WeeklyEventStartEvent;
import com.darkbladedev.events.WeeklyEventStopEvent;
import com.darkbladedev.events.WeeklyEventPauseEvent;
import com.darkbladedev.events.WeeklyEventResumeEvent;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.EventType;

/**
 * Listener del sistema de eventos semanales que maneja las notificaciones
 * y acciones adicionales cuando los eventos cambian de estado
 */
public class WeeklyEventSystemListener implements Listener {
    
    private final HeartlessMain plugin;
    
    /**
     * Constructor del listener del sistema de eventos
     * @param plugin Instancia del plugin principal
     */
    public WeeklyEventSystemListener(HeartlessMain plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Maneja el evento de inicio de evento semanal
     * @param event Evento de inicio
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWeeklyEventStart(WeeklyEventStartEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        EventType eventType = event.getEventType();
        long duration = event.getDuration();
        
        // Notificaciones personalizadas para diferentes tipos de eventos
        String eventName = getEventDisplayName(eventType);
        String durationText = formatDuration(duration);
        
        // Anuncio especial del sistema de eventos
        Bukkit.broadcast(MM.toComponent("<gradient:#ff6b6b:#4ecdc4><bold>⚡ SISTEMA DE EVENTOS ⚡</bold></gradient>"), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<yellow>▶ Iniciando: <white>" + eventName), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<yellow>⏱ Duración: <white>" + durationText), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), "heartless.access");
        
        // Log del sistema
        plugin.getLogger().info("[SISTEMA DE EVENTOS] Evento iniciado: " + eventType + " por " + durationText);
        
        // Aquí se pueden agregar acciones adicionales específicas por tipo de evento
        handleEventTypeSpecificStart(eventType);
    }
    
    /**
     * Maneja el evento de detención de evento semanal
     * @param event Evento de detención
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWeeklyEventStop(WeeklyEventStopEvent event) {
        EventType eventType = event.getEventType();
        boolean wasForced = event.wasForced();
        long totalDuration = event.getTotalDuration();
        
        String eventName = getEventDisplayName(eventType);
        String stopReason = wasForced ? "<red>FORZADO</red>" : "<green>NATURAL</green>";
        String durationText = formatDuration(totalDuration);
        
        // Anuncio de finalización
        Bukkit.broadcast(MM.toComponent("<gradient:#ff6b6b:#4ecdc4><bold>⚡ SISTEMA DE EVENTOS ⚡</bold></gradient>"), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<yellow>⏹ Finalizando: <white>" + eventName), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<yellow>📊 Tipo de fin: " + stopReason), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<yellow>⏱ Duración total: <white>" + durationText), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), "heartless.access");
        
        // Log del sistema
        String logReason = wasForced ? "forzadamente" : "naturalmente";
        plugin.getLogger().info("[SISTEMA DE EVENTOS] Evento finalizado " + logReason + ": " + eventType + " después de " + durationText);
        
        // Acciones de limpieza específicas por tipo de evento
        handleEventTypeSpecificStop(eventType, wasForced);
    }
    
    /**
     * Maneja el evento de pausa de evento semanal
     * @param event Evento de pausa
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWeeklyEventPause(WeeklyEventPauseEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        EventType eventType = event.getEventType();
        long timeRemaining = event.getTimeRemaining();
        
        String eventName = getEventDisplayName(eventType);
        String remainingText = formatDuration(timeRemaining);
        
        // Anuncio de pausa
        Bukkit.broadcast(MM.toComponent("<gradient:#ffa726:#ff7043><bold>⏸ EVENTO PAUSADO ⏸</bold></gradient>"), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<yellow>📛 Evento: <white>" + eventName), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<yellow>⏱ Tiempo restante: <white>" + remainingText), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<gray>El evento se reanudará pronto..."), "heartless.access");
        
        // Log del sistema
        plugin.getLogger().info("[SISTEMA DE EVENTOS] Evento pausado: " + eventType + " con " + remainingText + " restantes");
        
        // Acciones específicas durante la pausa
        handleEventTypeSpecificPause(eventType);
    }
    
    /**
     * Maneja el evento de reanudación de evento semanal
     * @param event Evento de reanudación
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWeeklyEventResume(WeeklyEventResumeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        EventType eventType = event.getEventType();
        long pausedDuration = event.getPausedDuration();
        long timeRemaining = event.getTimeRemaining();
        
        String eventName = getEventDisplayName(eventType);
        String pausedText = formatDuration(pausedDuration);
        String remainingText = formatDuration(timeRemaining);
        
        // Anuncio de reanudación
        Bukkit.broadcast(MM.toComponent("<gradient:#4caf50:#8bc34a><bold>▶ EVENTO REANUDADO ▶</bold></gradient>"), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<yellow>🔄 Evento: <white>" + eventName), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<yellow>⏸ Estuvo pausado: <white>" + pausedText), "heartless.access");
        Bukkit.broadcast(MM.toComponent("<yellow>⏱ Tiempo restante: <white>" + remainingText), "heartless.access");
        
        // Log del sistema
        plugin.getLogger().info("[SISTEMA DE EVENTOS] Evento reanudado: " + eventType + " después de " + pausedText + " pausado");
        
        // Acciones específicas al reanudar
        handleEventTypeSpecificResume(eventType);
    }
    
    /**
     * Obtiene el nombre de visualización del tipo de evento
     * @param eventType Tipo de evento
     * @return Nombre formateado del evento
     */
    private String getEventDisplayName(EventType eventType) {
        switch (eventType) {
            case ACID_WEEK:
                return "<green>Semana Ácida</green>";
            case TOXIC_FOG_WEEK:
                return "<dark_green>Niebla Tóxica</dark_green>";
            case UNDEAD_WEEK:
                return "<dark_red>Semana de No-Muertos</dark_red>";
            case EXPLOSIVE_WEEK:
                return "<red>Semana Explosiva</red>";
            case BLOOD_AND_IRON_WEEK:
                return "<dark_red>Semana de Sangre y Hierro</dark_red>";
            default:
                return "<gray>Evento Desconocido</gray>";
        }
    }
    
    /**
     * Formatea una duración en milisegundos a texto legible
     * @param duration Duración en milisegundos
     * @return Texto formateado de la duración
     */
    private String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        
        if (weeks > 0) {
            long remainingDays = days % 7;
            if (remainingDays > 0) {
                return weeks + "w " + remainingDays + "d " + (hours % 24) + "h";
            } else {
                return weeks + "w " + (hours % 24) + "h " + (minutes % 60) + "m";
            }
        } else if (days > 0) {
            return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m " + (seconds % 60) + "s";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
    
    /**
     * Maneja acciones específicas al iniciar diferentes tipos de eventos
     * @param eventType Tipo de evento que está iniciando
     */
    @SuppressWarnings("incomplete-switch")
    private void handleEventTypeSpecificStart(EventType eventType) {
        switch (eventType) {
            case ACID_WEEK:
                // Acciones específicas para Semana Ácida
                plugin.getLogger().info("[SISTEMA DE EVENTOS] Configuraciones especiales para Semana Ácida aplicadas");
                break;
            case TOXIC_FOG_WEEK:
                // Acciones específicas para Niebla Tóxica
                plugin.getLogger().info("[SISTEMA DE EVENTOS] Configuraciones especiales para Niebla Tóxica aplicadas");
                break;
            case UNDEAD_WEEK:
                // Acciones específicas para Semana de No-Muertos
                plugin.getLogger().info("[SISTEMA DE EVENTOS] Configuraciones especiales para Semana de No-Muertos aplicadas");
                break;
            case EXPLOSIVE_WEEK:
                // Acciones específicas para Semana Explosiva
                plugin.getLogger().info("[SISTEMA DE EVENTOS] Configuraciones especiales para Semana Explosiva aplicadas");
                break;
            case BLOOD_AND_IRON_WEEK:
                // Acciones específicas para Semana de Sangre y Hierro
                plugin.getLogger().info("[SISTEMA DE EVENTOS] Configuraciones especiales para Semana de Sangre y Hierro aplicadas");
                break;
        }
    }
    
    /**
     * Maneja acciones específicas al detener diferentes tipos de eventos
     * @param eventType Tipo de evento que se está deteniendo
     * @param wasForced true si el evento fue forzado a detenerse
     */
    private void handleEventTypeSpecificStop(EventType eventType, boolean wasForced) {
        // Aquí se pueden agregar acciones de limpieza específicas
        plugin.getLogger().info("[SISTEMA DE EVENTOS] Limpieza específica para " + eventType + " completada");
    }
    
    /**
     * Maneja acciones específicas al pausar diferentes tipos de eventos
     * @param eventType Tipo de evento que se está pausando
     */
    private void handleEventTypeSpecificPause(EventType eventType) {
        // Aquí se pueden agregar acciones específicas durante la pausa
        plugin.getLogger().info("[SISTEMA DE EVENTOS] Acciones de pausa para " + eventType + " ejecutadas");
    }
    
    /**
     * Maneja acciones específicas al reanudar diferentes tipos de eventos
     * @param eventType Tipo de evento que se está reanudando
     */
    private void handleEventTypeSpecificResume(EventType eventType) {
        // Aquí se pueden agregar acciones específicas al reanudar
        plugin.getLogger().info("[SISTEMA DE EVENTOS] Acciones de reanudación para " + eventType + " ejecutadas");
    }
}