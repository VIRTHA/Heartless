package com.darkbladedev;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import com.darkbladedev.commands.CommandHandler;
import com.darkbladedev.content.custom.listeners.EnchantmentListeners;
import com.darkbladedev.listeners.UndeadWeekStatsListener;
import com.darkbladedev.managers.BanManager;
import com.darkbladedev.managers.ConfigManager;
import com.darkbladedev.managers.ContentManager;
import com.darkbladedev.managers.CustomEffectsManager;
import com.darkbladedev.managers.EventManager;
import com.darkbladedev.managers.PlaceholderApiManager;
import com.darkbladedev.managers.StorageManager;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.managers.WeeklyEventTaskOptimizer;
import com.darkbladedev.managers.WeeklyEventMigrationManager;
import com.darkbladedev.utils.MM;

public class HeartlessMain extends JavaPlugin {

    private static final String prefix = "<gray>[ <gradient:#ffc329:#ffb029:#ff9c29:#ff8929:#ff7629:#ff6329:#ff5029:#ff3c29:#ff2929>Heartless</gradient> ]</gray>";
    private static HeartlessMain instance;

    private static ConfigManager configManager;
    private static EventManager eventManager;
    private static ContentManager contentManager;
    private static WeeklyEventManager weeklyEventManager;
    private static PlaceholderApiManager papiManager;
    private static BanManager banManager;
    private static StorageManager storageManager;
    private static CustomEffectsManager customEffectsManager;
    private static EnchantmentListeners enchantmentListeners;
    private static WeeklyEventTaskOptimizer taskOptimizer;
    private static WeeklyEventMigrationManager migrationManager;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        
        // Mostrar banner de inicio
        displayStartupBanner();
        
        // Inicializar ConfigManager PRIMERO para que esté disponible para todos los demás managers
        configManager = new ConfigManager(instance);
        
        // Inicializar WeeklyEventManager primero ya que EventManager lo necesita
        weeklyEventManager = new WeeklyEventManager(instance);
        eventManager = new EventManager(instance);
        contentManager = new ContentManager(instance);
        papiManager = new PlaceholderApiManager(instance);
        banManager = new BanManager(instance);
        storageManager = new StorageManager(instance);
        customEffectsManager = new CustomEffectsManager(instance);
        enchantmentListeners = new EnchantmentListeners(instance);
        taskOptimizer = new WeeklyEventTaskOptimizer(instance);
        migrationManager = new WeeklyEventMigrationManager(instance);

        initializeSystems();

        // Mostrar resumen final
        displayStartupSummary();
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        
        // Guardar datos del evento semanal activo
        if (weeklyEventManager != null) {
            weeklyEventManager.shutdown();
        }
        
        // Guardar datos del ciclo de día
        if (storageManager != null) {
            storageManager.saveDayCycleData();
        }
        
        // Limpiar recursos de efectos personalizados
        if (customEffectsManager != null) {
            customEffectsManager.cleanup();
        }
        
        // Limpiar recursos de los listeners de encantamientos
        if (enchantmentListeners != null) {
            enchantmentListeners.cleanup();
        }
        
        // Detener optimizador de tareas
        if (taskOptimizer != null) {
            taskOptimizer.stop();
        }
        
        Bukkit.getConsoleSender().sendMessage(MM.toComponent(prefix + " <green>Plugin desactivado correctamente."));
    }

    private void initializeSystems() {
        weeklyEventManager.initialize();
        customEffectsManager.initialize();
        
        // Inicializar optimizador de tareas
        taskOptimizer.start();
        
        // Verificar y ejecutar migraciones si es necesario
        if (migrationManager.needsMigration()) {
            getLogger().info("Ejecutando migraciones de eventos semanales...");
            migrationManager.performMigration();
        }

        // Registrar listeners personalizados
        getServer().getPluginManager().registerEvents(new UndeadWeekStatsListener(this), this);
        getServer().getPluginManager().registerEvents(new com.darkbladedev.listeners.WeeklyEventSystemListener(this), this);

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
    
    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public String getPrefix() {
        return prefix;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public static ContentManager getContentManager() {
        return contentManager;
    }

    public static WeeklyEventManager getWeeklyEventManager_() {
        return weeklyEventManager;
    }
    
    public static CustomEffectsManager getCustomEffectsManager() {
        return customEffectsManager;
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

    public StorageManager getStorageManager() {
        return storageManager;
    }
    
    public static EnchantmentListeners getEnchantmentListeners() {
        return enchantmentListeners;
    }
    
    public static WeeklyEventTaskOptimizer getTaskOptimizer() {
        return taskOptimizer;
    }
    
    public static WeeklyEventMigrationManager getMigrationManager() {
        return migrationManager;
    }
    
    /**
     * Muestra el banner de inicio del plugin
     */
    @SuppressWarnings("deprecation")
    private void displayStartupBanner() {
        Bukkit.getConsoleSender().sendMessage(MM.toComponent(""));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>┌─────────────────────────────────────────────────────────────┐"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│                                                             │"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│         <gradient:#ffc329:#ffb029:#ff9c29:#ff8929:#ff7629:#ff6329:#ff5029:#ff3c29:#ff2929>Heartless</gradient> Plugin Enabled <gray>🖤                          │"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│                                                             │"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│         <white>Version:</white> <yellow>" + getDescription().getVersion() + "</yellow>                                      │"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│         <white>Author:</white> <yellow>" + "DarkBladeDev" + "</yellow>                                │"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│                                                             │"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>└─────────────────────────────────────────────────────────────┘"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent(""));
    }
    
    /**
     * Muestra el resumen final de inicialización
     */
    private void displayStartupSummary() {
        Bukkit.getConsoleSender().sendMessage(MM.toComponent(""));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>┌─────────────────────────────────────────────────────────────┐"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│                   <green>✓ Initialization Complete</green>                 │"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│                                                             "));
        
        // Mostrar estado de los sistemas
        String eventStatus = weeklyEventManager.isEventActive() ? "<green>Active</green>" : "<yellow>Standby</yellow>";
        String eventType = weeklyEventManager.getCurrentEventType() != null ? 
            weeklyEventManager.getCurrentEventType().getEventName() : "None";
        
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│    <white>Weekly Events:</white> " + eventStatus + "                              "));
        if (weeklyEventManager.isEventActive()) {
            Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│    <white>Current Event:</white> <yellow>" + eventType + "</yellow>                        "));
        }
        
        int activeEffects = customEffectsManager.getActiveEffects().size();
        int totalEffects = customEffectsManager.getAllEffects().size();
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│    <white>Custom Effects:</white> <yellow>" + activeEffects + "/" + totalEffects + " Active</yellow>                      "));
        
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│    <white>PlaceholderAPI:</white> <green>Connected</green>                         "));
        
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│                                                             "));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>│                     <green>Enjoy your game! :)</green>                     │"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent("<gray>└─────────────────────────────────────────────────────────────┘"));
        Bukkit.getConsoleSender().sendMessage(MM.toComponent(""));
    }
}
