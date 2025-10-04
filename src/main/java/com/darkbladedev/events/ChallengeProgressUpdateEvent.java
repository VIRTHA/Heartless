package com.darkbladedev.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Evento que se dispara cuando se actualiza el progreso de un desafío.
 * Permite a otros sistemas reaccionar a cambios de progreso en tiempo real.
 */
public class ChallengeProgressUpdateEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String challengeId;
    private final Object currentProgress;
    private final Object maxProgress;
    private final String eventId;
    private final boolean isCompleted;
    
    /**
     * Constructor del evento de actualización de progreso.
     * 
     * @param player El jugador cuyo progreso se actualizó
     * @param challengeId ID del desafío
     * @param currentProgress Progreso actual
     * @param maxProgress Progreso máximo requerido
     * @param eventId ID del evento semanal
     * @param isCompleted Si el desafío se completó con esta actualización
     */
    public ChallengeProgressUpdateEvent(Player player, String challengeId, 
                                      Object currentProgress, Object maxProgress, 
                                      String eventId, boolean isCompleted) {
        this.player = player;
        this.challengeId = challengeId;
        this.currentProgress = currentProgress;
        this.maxProgress = maxProgress;
        this.eventId = eventId;
        this.isCompleted = isCompleted;
    }
    
    /**
     * Obtiene el jugador cuyo progreso se actualizó.
     * 
     * @return El jugador
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Obtiene el ID del desafío.
     * 
     * @return ID del desafío
     */
    public String getChallengeId() {
        return challengeId;
    }
    
    /**
     * Obtiene el progreso actual.
     * 
     * @return Progreso actual
     */
    public Object getCurrentProgress() {
        return currentProgress;
    }
    
    /**
     * Obtiene el progreso máximo requerido.
     * 
     * @return Progreso máximo
     */
    public Object getMaxProgress() {
        return maxProgress;
    }
    
    /**
     * Obtiene el ID del evento semanal.
     * 
     * @return ID del evento
     */
    public String getEventId() {
        return eventId;
    }
    
    /**
     * Verifica si el desafío se completó con esta actualización.
     * 
     * @return true si se completó, false en caso contrario
     */
    public boolean isCompleted() {
        return isCompleted;
    }
    
    /**
     * Obtiene el progreso como entero.
     * 
     * @return Progreso actual como entero
     */
    public int getCurrentProgressAsInt() {
        if (currentProgress instanceof Number) {
            return ((Number) currentProgress).intValue();
        }
        return 0;
    }
    
    /**
     * Obtiene el progreso máximo como entero.
     * 
     * @return Progreso máximo como entero
     */
    public int getMaxProgressAsInt() {
        if (maxProgress instanceof Number) {
            return ((Number) maxProgress).intValue();
        }
        return 1;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}