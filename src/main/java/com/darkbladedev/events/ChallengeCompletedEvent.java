package com.darkbladedev.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.challenges.ChallengeDefinition;

/**
 * Evento que se dispara cuando un jugador completa un desafío durante un evento semanal.
 * 
 * Este evento permite a otros sistemas reaccionar a la completación de desafíos,
 * como enviar mensajes de difusión, otorgar recompensas adicionales, etc.
 * 
 * @author DarkBladeDev
 * @since 1.0.0
 */
public class ChallengeCompletedEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Player player;
    private final String challengeId;
    private final ChallengeDefinition challenge;
    private final WeeklyEvent weeklyEvent;
    private final long completionTime;
    
    /**
     * Constructor del evento de completación de desafío.
     * 
     * @param player El jugador que completó el desafío
     * @param challengeId El ID del desafío completado
     * @param challenge La definición del desafío completado (puede ser null)
     * @param weeklyEvent El evento semanal en el que se completó el desafío
     */
    public ChallengeCompletedEvent(Player player, String challengeId, ChallengeDefinition challenge, WeeklyEvent weeklyEvent) {
        this.player = player;
        this.challengeId = challengeId;
        this.challenge = challenge;
        this.weeklyEvent = weeklyEvent;
        this.completionTime = System.currentTimeMillis();
    }
    
    /**
     * Obtiene el jugador que completó el desafío.
     * 
     * @return El jugador que completó el desafío
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Obtiene el ID del desafío completado.
     * 
     * @return El ID del desafío
     */
    public String getChallengeId() {
        return challengeId;
    }
    
    /**
     * Obtiene la definición del desafío completado.
     * 
     * @return La definición del desafío, o null si no está disponible
     */
    public ChallengeDefinition getChallenge() {
        return challenge;
    }
    
    /**
     * Obtiene el evento semanal en el que se completó el desafío.
     * 
     * @return El evento semanal activo
     */
    public WeeklyEvent getWeeklyEvent() {
        return weeklyEvent;
    }
    
    /**
     * Obtiene el timestamp de cuando se completó el desafío.
     * 
     * @return El tiempo de completación en milisegundos
     */
    public long getCompletionTime() {
        return completionTime;
    }
    
    /**
     * Obtiene el nombre del evento semanal.
     * 
     * @return El nombre del evento semanal, o "Desconocido" si no está disponible
     */
    public String getEventName() {
        return weeklyEvent != null ? weeklyEvent.getId() : "Desconocido";
    }
    
    /**
     * Obtiene el nombre del desafío para mostrar.
     * 
     * @return Nombre del desafío o "Desafío Desconocido" si no está disponible
     */
    public String getChallengeName() {
        if (challenge != null && challenge.getTitle() != null) {
            return challenge.getTitle();
        }
        return "Desafío Desconocido";
    }
    
    /**
     * Verifica si el jugador está en línea.
     * 
     * @return true si el jugador está en línea, false en caso contrario
     */
    public boolean isPlayerOnline() {
        return player != null && player.isOnline();
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    
    @Override
    public String toString() {
        return "ChallengeCompletedEvent{" +
                "player=" + (player != null ? player.getName() : "null") +
                ", challengeId='" + challengeId + '\'' +
                ", eventName='" + getEventName() + '\'' +
                ", completionTime=" + completionTime +
                '}';
    }
}