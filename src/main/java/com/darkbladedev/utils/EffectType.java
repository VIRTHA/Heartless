package com.darkbladedev.utils;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.command.CommandSender;

/**
 * Enum representing the different types of events that can be created.
 * Each event type corresponds to a mechanic in the mechanics package.
 */
public enum EffectType {
    ZOMBIE_INFECTION("Infeccion Zombie", "Simplemente una infección zombie", "zombie_infection");

    private final String effectName;
    private final String description;
    private final String id;

    EffectType(String effectName, String description, String id) {
        this.effectName = effectName;
        this.description = description;
        this.id = id;
    }

    /**
     * Gets the command name used to reference this event type.
     * @return The command name
     */
    public String getEffectName() {
        return effectName;
    }

    /**
     * Gets the description of this event type.
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    public String getID() {
        return id;
    }

    /**
     * Finds an event type by its event name.
     * @param name The event name to search for
     * @return The matching EventType or null if not found
     */
    public static EffectType getByName(String name) {
        for (EffectType type : values()) {
            if (type.getEffectName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Sends a list of all available event types to a command sender.
     * @param sender The command sender to send the list to
     */
    public static void sendEffectList(CommandSender sender) {
        sender.sendMessage("Available effect types:");
        for (EffectType type : values()) {
            sender.sendMessage("- " + type.getEffectName() + ": " + type.getDescription());
        }
    }

    /**
     * Finds an event type by its command name.
     * @return All EventType names as List
     */
    public static List<String> getEffectNames() {
        // Crear una lista mutable en lugar de una inmutable
        List<String> eventNames = new java.util.ArrayList<>();
        for (EffectType type : values()) {
            eventNames.add(type.getEffectName());
        }
        eventNames.removeIf(Predicate.isEqual("empty"));
        return eventNames;
    }
}