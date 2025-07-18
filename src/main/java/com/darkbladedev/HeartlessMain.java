package com.darkbladedev;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import com.darkbladedev.commands.CommandHandler;
import com.darkbladedev.managers.BanManager;
import com.darkbladedev.managers.ContentManager;
import com.darkbladedev.managers.EventManager;
import com.darkbladedev.managers.PlaceholderApiManager;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.MM;

public class HeartlessMain extends JavaPlugin {

    private static final String prefix = "<gray>[ <gradient:#ffc329:#ffb029:#ff9c29:#ff8929:#ff7629:#ff6329:#ff5029:#ff3c29:#ff2929>Heartless</gradient> ]</gray>";
    private static HeartlessMain instance;

    private static EventManager eventManager;
    private static ContentManager contentManager;
    private static WeeklyEventManager weeklyEventManager;
    private static PlaceholderApiManager papiManager;
    private static BanManager banManager;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        eventManager = new EventManager(instance);
        contentManager = new ContentManager(instance);
        weeklyEventManager = new WeeklyEventManager(instance);
        papiManager = new PlaceholderApiManager(instance);
        banManager = new BanManager(instance);

        initializeSystems();

        Bukkit.getConsoleSender().sendMessage(MM.toComponent(prefix + " <green>Plugin activado correctamente."));
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getConsoleSender().sendMessage(MM.toComponent(prefix + " <green>Plugin desactivado correctamente."));
    }

    private void initializeSystems() {
        weeklyEventManager.initialize();

        registerCommands();
    }

    private void registerCommands() {
        CommandHandler handler = new CommandHandler();

        this.getServer().getCommandMap().register("heartless", new org.bukkit.command.Command("heartless") {
            {
                this.setDescription("Comando principal del plugin Heartless");
                this.setUsage("/heartless <subcomando>");
                this.setAliases(List.of("hs"));
            }

            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
                return handler.onCommand(sender, this, label, args);
            }
            
            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                return handler.onTabComplete(sender, this, alias, args);
            }
        });
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

    public static WeeklyEventManager getWeeklyEventManager_() {
        return weeklyEventManager;
    }
    
    public WeeklyEventManager getWeeklyEventManager() {
        return weeklyEventManager;
    }

    public PlaceholderApiManager getPapiManager() {
        return papiManager;
    }

    public BanManager getBanManager() {
        return banManager;
    }
}
