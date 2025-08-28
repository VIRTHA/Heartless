package com.darkbladedev.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.AcidWeek;
import com.darkbladedev.mechanics.HealthRewards;
import com.darkbladedev.mechanics.HealthSteal;
import com.darkbladedev.utils.EventType;
import com.darkbladedev.utils.MM;

public class EventManager {

    @SuppressWarnings("unused")
    private final HeartlessMain plugin;
    private WeeklyEventManager weeklyEventManager;
    private HealthSteal healthStealSystem;
    @SuppressWarnings("unused")
    private HealthRewards healthRewardsSystem;

    private AcidWeek acidWeek;

    public EventManager(HeartlessMain plugin) {
        this.plugin = plugin;
        // Usar la instancia de WeeklyEventManager del plugin principal en lugar de crear una nueva
        this.weeklyEventManager = plugin.getWeeklyEventManager();
        this.healthStealSystem = new HealthSteal(plugin);
        this.healthRewardsSystem = new HealthRewards(plugin);

        Bukkit.getPluginManager().registerEvents(healthStealSystem, plugin);
    }

    // STARTERS
    public void startEvent(EventType event, long duration) {
        weeklyEventManager.startEventFromCommand(event, duration);
    }

    // ACTIONS
    public boolean stopEvent(EventType event, CommandSender executor) {
        try {
            weeklyEventManager.stopSpecificEvent(event);
            return true;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MM.toComponent("<red><b>Ha ocurrido un problema al detener un evento.</b></red>"));
            Bukkit.getConsoleSender().sendMessage(MM.toComponent("  <yellow><b>Detalles:</b></yellow>"));
            Bukkit.getConsoleSender().sendMessage(MM.toComponent("    <gray><b>Evento: </b></gray>").append(MM.toComponent(event.getEventName())));
            Bukkit.getConsoleSender().sendMessage(MM.toComponent("    <gray><b>Ejecutor: </b></gray>").append(MM.toComponent(executor.getName())));
            Bukkit.getConsoleSender().sendMessage(MM.toComponent("    <gray><b>Registro de error: </b></gray>").appendNewline().append(MM.toComponent(e.getMessage())));
            return false;
        }
    }

    public boolean stopEvent(CommandSender executor) {
        try {
            weeklyEventManager.stopCurrentEvent();
            return true;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MM.toComponent("<red><b>Ha ocurrido un problema al detener un evento.</b></red>"));
            Bukkit.getConsoleSender().sendMessage(MM.toComponent("  <yellow><b>Detalles:</b></yellow>"));
            Bukkit.getConsoleSender().sendMessage(MM.toComponent("    <gray><b>Evento: </b></gray>").append(MM.toComponent(weeklyEventManager.getCurrentEvent().getName())));
            Bukkit.getConsoleSender().sendMessage(MM.toComponent("    <gray><b>Ejecutor: </b></gray>").append(MM.toComponent(executor.getName())));
            Bukkit.getConsoleSender().sendMessage(MM.toComponent("    <gray><b>Registro de error: </b></gray>").appendNewline().append(MM.toComponent(e.getMessage())));
            return false;
        }
    }

    // GETTERS
    public AcidWeek getAcidWeek() {
        return  acidWeek;
    }
    
    /**
     * Recarga los datos de eventos
     * Este método delega la recarga al WeeklyEventManager
     */
    public void reload() {
        weeklyEventManager.reload();
    }
}
