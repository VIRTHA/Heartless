package com.darkbladedev.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Evento que se dispara cuando un jugador es infectado por el virus zombie
 * durante la Semana de los No Muertos.
 */
public class PlayerInfectedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String infectionSource;
    
    /**
     * Constructor del evento de infección
     * @param player El jugador que fue infectado
     * @param infectionSource La fuente de la infección (ej: "zombie_attack", "skeleton_arrow")
     */
    public PlayerInfectedEvent(Player player, String infectionSource) {
        this.player = player;
        this.infectionSource = infectionSource;
    }
    
    /**
     * Obtiene el jugador que fue infectado
     * @return El jugador infectado
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Obtiene la fuente de la infección
     * @return La fuente de la infección
     */
    public String getInfectionSource() {
        return infectionSource;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}