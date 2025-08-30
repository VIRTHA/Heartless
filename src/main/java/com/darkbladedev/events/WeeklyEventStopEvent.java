package com.darkbladedev.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.darkbladedev.utils.EventType;

/**
 * Evento que se dispara cuando un evento semanal se detiene
 */
public class WeeklyEventStopEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final EventType eventType;
    private final long endTime;
    private final boolean wasForced;
    private final long totalDuration;
    
    /**
     * Constructor del evento de detención de evento semanal
     * @param eventType Tipo de evento que se está deteniendo
     * @param endTime Tiempo de finalización del evento
     * @param wasForced true si el evento fue forzado a detenerse
     * @param totalDuration Duración total que el evento estuvo activo
     */
    public WeeklyEventStopEvent(EventType eventType, long endTime, boolean wasForced, long totalDuration) {
        this.eventType = eventType;
        this.endTime = endTime;
        this.wasForced = wasForced;
        this.totalDuration = totalDuration;
    }
    
    /**
     * Obtiene el tipo de evento que se está deteniendo
     * @return El tipo de evento
     */
    public EventType getEventType() {
        return eventType;
    }
    
    /**
     * Obtiene el tiempo de finalización del evento
     * @return El tiempo de finalización en milisegundos
     */
    public long getEndTime() {
        return endTime;
    }
    
    /**
     * Verifica si el evento fue forzado a detenerse
     * @return true si el evento fue forzado a detenerse
     */
    public boolean wasForced() {
        return wasForced;
    }
    
    /**
     * Obtiene la duración total que el evento estuvo activo
     * @return La duración total en milisegundos
     */
    public long getTotalDuration() {
        return totalDuration;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}