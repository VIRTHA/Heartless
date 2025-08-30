package com.darkbladedev.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.darkbladedev.utils.EventType;

/**
 * Evento que se dispara cuando un evento semanal se reanuda
 */
public class WeeklyEventResumeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final EventType eventType;
    private final long resumeTime;
    private final long pausedDuration;
    private final long timeRemaining;
    private boolean cancelled = false;
    
    /**
     * Constructor del evento de reanudación de evento semanal
     * @param eventType Tipo de evento que se está reanudando
     * @param resumeTime Tiempo en que se reanudó el evento
     * @param pausedDuration Duración total que el evento estuvo pausado
     * @param timeRemaining Tiempo restante del evento después de reanudar
     */
    public WeeklyEventResumeEvent(EventType eventType, long resumeTime, long pausedDuration, long timeRemaining) {
        this.eventType = eventType;
        this.resumeTime = resumeTime;
        this.pausedDuration = pausedDuration;
        this.timeRemaining = timeRemaining;
    }
    
    /**
     * Obtiene el tipo de evento que se está reanudando
     * @return El tipo de evento
     */
    public EventType getEventType() {
        return eventType;
    }
    
    /**
     * Obtiene el tiempo en que se reanudó el evento
     * @return El tiempo de reanudación en milisegundos
     */
    public long getResumeTime() {
        return resumeTime;
    }
    
    /**
     * Obtiene la duración total que el evento estuvo pausado
     * @return La duración pausada en milisegundos
     */
    public long getPausedDuration() {
        return pausedDuration;
    }
    
    /**
     * Obtiene el tiempo restante del evento después de reanudar
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