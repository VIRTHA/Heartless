package com.darkbladedev.events;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NightPassedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final World world;
    private final long nightsPassed; // Cuántas noches pasaron desde el último check
    private final long totalNights;  // Total acumulado

    public NightPassedEvent(World world, long nightsPassed, long totalNights) {
        this.world = world;
        this.nightsPassed = nightsPassed;
        this.totalNights = totalNights;
    }

    public World getWorld() {
        return world;
    }

    public long getNightsPassed() {
        return nightsPassed;
    }

    public long getTotalNights() {
        return totalNights;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
