package com.darkbladedev;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.darkbladedev.managers.ContentManager;
import com.darkbladedev.managers.EventManager;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.MM;

public class HeartlessMain extends JavaPlugin {

    private static final String prefix = "<gray>[ <gradient:#ffc329:#ffb029:#ff9c29:#ff8929:#ff7629:#ff6329:#ff5029:#ff3c29:#ff2929>Heartless</gradient> ]</gray>";
    private static HeartlessMain instance;

    private static EventManager eventManager;
    private static ContentManager contentManager;
    private static WeeklyEventManager weeklyEventManager;

    
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        eventManager = new EventManager(instance);
        contentManager = new ContentManager(instance);
        weeklyEventManager = new WeeklyEventManager(instance);

        Bukkit.getConsoleSender().sendMessage(MM.toComponent(prefix + " <green>Plugin activado correctamente."));
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getConsoleSender().sendMessage(MM.toComponent(prefix + " <green>Plugin desactivado correctamente."));
    }
    
    public static HeartlessMain getInstance() {
        return instance;
    }

    public String getPrefix() {
        return prefix;
    }

    public static EventManager getEventManager() {
        return eventManager;
    }

    public static ContentManager getContentManager() {
        return contentManager;
    }

    public WeeklyEventManager getWeeklyEventManager() {
        return weeklyEventManager;
    }
}
