package com.darkbladedev.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.darkbladedev.utils.EventType;

/**
 * Evento que se dispara cuando un evento semanal se pausa
 */
public class WeeklyEventPauseEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final EventType eventType;
    private final long pauseTime;
    private final long timeRemaining;
    private boolean cancelled = false;
    
    /**
     * Constructor del evento de pausa de evento semanal
     * @param eventType Tipo de evento que se está pausando
     * @param pauseTime Tiempo en que se pausó el evento
     * @param timeRemaining Tiempo restante del evento al momento de pausar
     */
    public WeeklyEventPauseEvent(EventType eventType, long pauseTime, long timeRemaining) {
        this.eventType = eventType;
        this.pauseTime = pauseTime;
        this.timeRemaining = timeRemaining;
    }
    
    /**
     * Obtiene el tipo de evento que se está pausando
     * @return El tipo de evento
     */
    public EventType getEventType() {
        return eventType;
    }
    
    /**
     * Obtiene el tiempo en que se pausó el evento
     * @return El tiempo de pausa en milisegundos
     */
    public long getPauseTime() {
        return pauseTime;
    }
    
    /**
     * Obtiene el tiempo restante del evento al momento de pausar
     * @return El tiempo restante en milisegundos
     */
    public long getTimeRemaining() {
        return timeRemaining;
    }
    
    /**
     * Verifica si el evento ha sido cancelado
     * @return true si el evento está cancelado
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Establece si el evento debe ser cancelado
     * @param cancelled true para cancelar el evento
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}