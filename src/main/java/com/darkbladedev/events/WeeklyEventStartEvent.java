package com.darkbladedev.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.darkbladedev.utils.EventType;

/**
 * Evento que se dispara cuando un evento semanal inicia
 */
public class WeeklyEventStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final EventType eventType;
    private final long duration;
    private final long startTime;
    private boolean cancelled = false;
    
    /**
     * Constructor del evento de inicio de evento semanal
     * @param eventType Tipo de evento que está iniciando
     * @param duration Duración del evento en milisegundos
     * @param startTime Tiempo de inicio del evento
     */
    public WeeklyEventStartEvent(EventType eventType, long duration, long startTime) {
        this.eventType = eventType;
        this.duration = duration;
        this.startTime = startTime;
    }
    
    /**
     * Obtiene el tipo de evento que está iniciando
     * @return El tipo de evento
     */
    public EventType getEventType() {
        return eventType;
    }
    
    /**
     * Obtiene la duración del evento en milisegundos
     * @return La duración del evento
     */
    public long getDuration() {
        return duration;
    }
    
    /**
     * Obtiene el tiempo de inicio del evento
     * @return El tiempo de inicio en milisegundos
     */
    public long getStartTime() {
        return startTime;
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