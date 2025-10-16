package com.darkbladedev.utils;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.AcidWeek;
import com.darkbladedev.mechanics.BloodAndIronWeek;
import com.darkbladedev.mechanics.ExplosiveWeek;
import com.darkbladedev.mechanics.ToxicFog;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.models.TimeExpression;

/**
 * Enum representing the different types of events that can be created.
 * Each event type corresponds to a mechanic in the mechanics package.
 */
public enum EventType {
    TOXIC_FOG_WEEK("toxic_fog", "Creates a toxic fog that damages players"),
    ACID_WEEK("acid_week", "The atmosphere and water are contaminated with acid"),
    UNDEAD_WEEK("undead_week", "Undead hordes dominate the world with Red Moon nights"),
    EXPLOSIVE_WEEK("explosive_week", "Everything becomes more explosive and dangerous"),
    BLOOD_AND_IRON_WEEK("blood_and_iron_week", "The coliseum of chaos is open. Eliminate or be eliminated"),
    EMPTY("empty", "empty");

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
     * Finds an event type by its event name.
     * @param name The event name to search for
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
        // Crear una lista mutable en lugar de una inmutable
        List<String> eventNames = new java.util.ArrayList<>();
        for (EventType type : values()) {
            eventNames.add(type.getEventName());
        }
        eventNames.removeIf(Predicate.isEqual("empty"));
        return eventNames;
    }

    /**
     * Convierte este tipo de evento en una instancia de WeeklyEvent.
     * @param plugin El plugin principal
     * @param duration Duración del evento en ticks
     * @return Una nueva instancia de WeeklyEvent correspondiente a este tipo, o null si no está implementado
     */
    public WeeklyEvent toEvent(HeartlessMain plugin, long duration) {
        // Create TimeExpression from duration in seconds
        TimeExpression durationExpression = TimeExpression.fromSeconds(duration);
        
        switch (this.getEventName()) {
            case "acid_week":
                return new AcidWeek(plugin, durationExpression);
                
            case "toxic_fog":
                return new ToxicFog(plugin, durationExpression);
                
            case "undead_week":
                return new UndeadWeek(plugin, durationExpression);
                
            case "explosive_week":
                return new ExplosiveWeek(plugin, durationExpression);
                
            case "blood_and_iron_week":
                return new BloodAndIronWeek(plugin, durationExpression);
            
            case "empty":
                return new EmptyEvent(plugin, duration);
                
            default:
                Bukkit.getConsoleSender().sendMessage(
                    MM.toComponent("<red>Evento no implementado para ejecución: " + this.getEventName())
                );
                return null;
        }
    }
}