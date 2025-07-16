package com.darkbladedev.utils;

import java.util.List;

import org.bukkit.command.CommandSender;

/**
 * Enum representing the different types of events that can be created.
 * Each event type corresponds to a mechanic in the mechanics package.
 */
public enum EventType {
    SIZE_RANDOMIZER("size_randomizer", "Randomizes the size of players"),
    TOXIC_FOG("toxic_fog", "Creates a toxic fog that damages players"),
    PARANOIA_EFFECT("paranoia_effect", "Players experience a paranoia effect"),
    ACID_WEEK("acid_week", "The atmosphere and water are contaminated with acid"),
    MOB_RAIN("mob_rain", "Mobs rain from the sky periodically"),
    UNDEAD_WEEK("undead_week", "Undead hordes dominate the world with Red Moon nights"),
    EXPLOSIVE_WEEK("explosive_week", "Everything becomes more explosive and dangerous"),
    BLOOD_AND_IRON_WEEK("blood_and_iron_week", "The coliseum of chaos is open. Eliminate or be eliminated");

    private final String eventName;
    private final String description;

    EventType(String eventName, String description) {
        this.eventName = eventName;
        this.description = description;
    }

    /**
     * Gets the command name used to reference this event type.
     * @return The command name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Gets the description of this event type.
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Finds an event type by its command name.
     * @param name The command name to search for
     * @return The matching EventType or null if not found
     */
    public static EventType getByName(String name) {
        for (EventType type : values()) {
            if (type.getEventName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Sends a list of all available event types to a command sender.
     * @param sender The command sender to send the list to
     */
    public static void sendEventList(CommandSender sender) {
        sender.sendMessage("Available event types:");
        for (EventType type : values()) {
            sender.sendMessage("- " + type.getEventName() + ": " + type.getDescription());
        }
    }

    /**
     * Finds an event type by its command name.
     * @return All EventType names as List
     */
    public static List<String> getEventNames() {
        List<String> eventNames = List.of();
        for (EventType type : values()) {
            eventNames.add(type.getEventName());
        }
        return eventNames;
    }
}