package com.darkbladedev.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Evento que se dispara cuando un jugador se cura de la infección zombie
 * durante la Semana de los No Muertos.
 */
public class PlayerCuredEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemStack cureItem;
    private final int totalCuresCount;
    
    /**
     * Constructor del evento de curación
     * @param player El jugador que se curó
     * @param cureItem El item usado para curarse (manzana dorada o zanahoria dorada)
     * @param totalCuresCount El número total de veces que este jugador se ha curado
     */
    public PlayerCuredEvent(Player player, ItemStack cureItem, int totalCuresCount) {
        this.player = player;
        this.cureItem = cureItem;
        this.totalCuresCount = totalCuresCount;
    }
    
    /**
     * Obtiene el jugador que se curó
     * @return El jugador curado
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Obtiene el item usado para curarse
     * @return El item de curación
     */
    public ItemStack getCureItem() {
        return cureItem;
    }
    
    /**
     * Obtiene el número total de veces que este jugador se ha curado
     * @return El contador total de curaciones
     */
    public int getTotalCuresCount() {
        return totalCuresCount;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}