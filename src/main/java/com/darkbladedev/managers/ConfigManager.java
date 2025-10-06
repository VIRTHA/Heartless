package com.darkbladedev.managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.darkbladedev.HeartlessMain;

/**
 * Gestor de configuración que maneja la carga y recarga automática
 * de todos los valores configurables del plugin.
 */
public class ConfigManager {
    
    private final HeartlessMain plugin;
    private FileConfiguration config;
    private File configFile;
    
    // Cache de valores de configuración para acceso rápido
    private boolean databaseEnabled;
    private String databaseType;
    private String databaseHost;
    private int databasePort;
    private String databaseName;
    private String databaseUsername;
    private String databasePassword;
    private String sqliteFile;
    private int databasePoolSize;
    private int databaseConnectionTimeout;
    
    private boolean eventsEnabled;
    private boolean eventsAutoStart;
    private int eventsSaveInterval;
    private int eventsMaxConcurrent;
    private int eventsNotificationRadius;
    private List<String> eventsExcludedWorlds;
    
    private double healthDefaultMax;
    private double healthMinimum;
    private boolean healthStealEnabled;
    private boolean healthRewardsEnabled;
    
    private boolean banSystemEnabled;
    private int banDefaultDuration;
    private boolean banBroadcast;
    
    private boolean customEffectsEnabled;
    private boolean zombieInfectionEnabled;
    private int zombieInfectionDuration;
    private double zombieInfectionSpreadChance;
    private int zombieInfectionCureVillagers;
    
    private String messagesPrefix;
    private String messagesReloadSuccess;
    private String messagesReloadError;
    
    private boolean optimizationTaskOptimizerEnabled;
    private int optimizationMaxThreads;
    private int optimizationQueueSize;
    
    private boolean debugEnabled;
    private String debugLogLevel;
    private boolean debugVerboseEvents;
    
    public ConfigManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }
    
    /**
     * Carga o recarga la configuración desde el archivo
     */
    public void loadConfig() {
        // Crear el archivo de configuración si no existe
        if (!configFile.exists()) {
            createDefaultConfig();
        }
        
        // Cargar la configuración
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Cargar todos los valores en cache
        loadConfigValues();
        
        plugin.getLogger().info("Configuración cargada exitosamente.");
    }
    
    /**
     * Recarga la configuración y notifica a todos los managers
     */
    public void reloadConfig() {
        try {
            loadConfig();
            notifyManagersOfConfigChange();
            plugin.getLogger().info("Configuración recargada exitosamente.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error al recargar la configuración", e);
        }
    }
    
    /**
     * Crea el archivo de configuración por defecto
     */
    private void createDefaultConfig() {
        try {
            // Crear directorio si no existe
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            // Copiar el archivo de configuración desde los recursos
            try (InputStream inputStream = plugin.getResource("config.yml")) {
                if (inputStream != null) {
                    Files.copy(inputStream, configFile.toPath());
                    plugin.getLogger().info("Archivo config.yml creado con valores por defecto.");
                } else {
                    plugin.getLogger().warning("No se pudo encontrar config.yml en los recursos del plugin.");
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al crear el archivo de configuración", e);
        }
    }
    
    /**
     * Carga todos los valores de configuración en variables cache
     */
    private void loadConfigValues() {
        // Database configuration
        databaseEnabled = config.getBoolean("database.enabled", false);
        databaseType = config.getString("database.type", "sqlite").toLowerCase();
        
        // MySQL configuration
        databaseHost = config.getString("database.mysql.host", "localhost");
        databasePort = config.getInt("database.mysql.port", 3306);
        databaseName = config.getString("database.mysql.database", "heartless");
        databaseUsername = config.getString("database.mysql.username", "root");
        databasePassword = config.getString("database.mysql.password", "");
        
        // SQLite configuration
        sqliteFile = config.getString("database.sqlite.file", "htl.db");
        
        // Pool configuration
        databasePoolSize = config.getInt("database.pool.size", 10);
        databaseConnectionTimeout = config.getInt("database.pool.connection-timeout", 30000);
        
        // Events configuration
        eventsEnabled = config.getBoolean("events.enabled", true);
        eventsAutoStart = config.getBoolean("events.auto-start", true);
        eventsSaveInterval = config.getInt("events.save-interval", 300);
        eventsMaxConcurrent = config.getInt("events.max-concurrent-events", 1);
        eventsNotificationRadius = config.getInt("events.notification-radius", 100);
        eventsExcludedWorlds = config.getStringList("events.excluded-worlds");
        
        // Health configuration
        healthDefaultMax = config.getDouble("health.default-max-health", 20.0);
        healthMinimum = config.getDouble("health.minimum-health", 2.0);
        healthStealEnabled = config.getBoolean("health.health-steal-enabled", true);
        healthRewardsEnabled = config.getBoolean("health.health-rewards-enabled", true);
        
        // Ban system configuration
        banSystemEnabled = config.getBoolean("ban-system.enabled", true);
        banDefaultDuration = config.getInt("ban-system.default-duration", 3600);
        banBroadcast = config.getBoolean("ban-system.broadcast-bans", true);
        
        // Custom effects configuration
        customEffectsEnabled = config.getBoolean("custom-effects.enabled", true);
        zombieInfectionEnabled = config.getBoolean("custom-effects.zombie-infection.enabled", true);
        zombieInfectionDuration = config.getInt("custom-effects.zombie-infection.duration", 600);
        zombieInfectionSpreadChance = config.getDouble("custom-effects.zombie-infection.spread-chance", 0.15);
        zombieInfectionCureVillagers = config.getInt("custom-effects.zombie-infection.cure-villagers-required", 1);
        
        // Messages configuration
        messagesPrefix = config.getString("messages.prefix", "<gray>[ <gradient:#ffc329:#ffb029:#ff9c29:#ff8929:#ff7629:#ff6329:#ff5029:#ff3c29:#ff2929>Heartless</gradient> ]</gray>");
        messagesReloadSuccess = config.getString("messages.reload-success", "<green>Plugin recargado exitosamente!</green>");
        messagesReloadError = config.getString("messages.reload-error", "<red>Error al recargar el plugin.</red>");
        
        // Optimization configuration
        optimizationTaskOptimizerEnabled = config.getBoolean("optimization.task-optimizer-enabled", true);
        optimizationMaxThreads = config.getInt("optimization.max-threads", 4);
        optimizationQueueSize = config.getInt("optimization.queue-size", 1000);
        
        // Debug configuration
        debugEnabled = config.getBoolean("debug.enabled", false);
        debugLogLevel = config.getString("debug.log-level", "INFO");
        debugVerboseEvents = config.getBoolean("debug.verbose-events", false);
    }
    
    /**
     * Notifica a todos los managers sobre cambios en la configuración
     */
    private void notifyManagersOfConfigChange() {
        // Notificar al DatabaseManager
        if (HeartlessMain.getDatabaseManager() != null) {
            HeartlessMain.getDatabaseManager().onConfigReload();
        }
        
        // Notificar al WeeklyEventManager
        if (plugin.getWeeklyEventManager() != null) {
            plugin.getWeeklyEventManager().onConfigReload();
        }
        
        // Notificar al CustomEffectsManager
        if (HeartlessMain.getCustomEffectsManager() != null) {
            HeartlessMain.getCustomEffectsManager().onConfigReload();
        }
        
        // Notificar al BanManager
        if (plugin.getBanManager() != null) {
            plugin.getBanManager().onConfigReload();
        }
        
        // Notificar al TaskOptimizer
        if (HeartlessMain.getTaskOptimizer() != null) {
            HeartlessMain.getTaskOptimizer().onConfigReload();
        }
        
        // Notificar al EventManager
        if (plugin.getEventManager() != null) {
            plugin.getEventManager().onConfigReload();
        }

        // Notificar al PermissionBonusManager para recargar bonificaciones
        if (plugin.getPermissionBonusManager() != null) {
            plugin.getPermissionBonusManager().onConfigReload();
        }
    }
    
    // Getters para acceso a los valores de configuración
    
    // Database getters
    public boolean isDatabaseEnabled() { return databaseEnabled; }
    public String getDatabaseType() { return databaseType; }
    public String getDatabaseHost() { return databaseHost; }
    public int getDatabasePort() { return databasePort; }
    public String getDatabaseName() { return databaseName; }
    public String getDatabaseUsername() { return databaseUsername; }
    public String getDatabasePassword() { return databasePassword; }
    public String getSqliteFile() { return sqliteFile; }
    public int getDatabasePoolSize() { return databasePoolSize; }
    public int getDatabaseConnectionTimeout() { return databaseConnectionTimeout; }
    
    // Database type helpers
    public boolean isMySQLDatabase() { return "mysql".equals(databaseType); }
    public boolean isSQLiteDatabase() { return "sqlite".equals(databaseType); }
    
    // Getters adicionales para DatabaseManager
    public long getDatabaseConnectionTimeoutLong() {
        return config.getLong("database.connection-timeout", 30000);
    }
    
    // Getters adicionales para sistema de bans
    public long getBanDefaultDurationLong() {
        return config.getLong("ban-system.default-duration", 24); // 24 horas por defecto
    }
    
    public String getBanMessage() {
        return config.getString("ban-system.ban-message", "<red><b>{reason}\n\n<gray>Duración del baneo: <red>{time}<gray>.\n<gray>Este es tu baneo número <red>{count}<gray>.");
    }
    
    public String getBanBroadcastMessage() {
        return config.getString("ban-system.broadcast-message", "<red>{player} ha sido baneado del servidor. Razón: {reason}");
    }
    
    // Events getters
    public boolean isEventsEnabled() { return eventsEnabled; }
    public boolean isEventsAutoStart() { return eventsAutoStart; }
    public int getEventsSaveInterval() { return eventsSaveInterval; }
    public int getEventsMaxConcurrent() { return eventsMaxConcurrent; }
    public int getEventsNotificationRadius() { return eventsNotificationRadius; }
    public List<String> getExcludedWorlds() { return eventsExcludedWorlds; }
    
    // Health getters
    public double getHealthDefaultMax() { return healthDefaultMax; }
    public double getHealthMinimum() { return healthMinimum; }
    public boolean isHealthStealEnabled() { return healthStealEnabled; }
    public boolean isHealthRewardsEnabled() { return healthRewardsEnabled; }
    
    // Ban system getters
    public boolean isBanSystemEnabled() { return banSystemEnabled; }
    public int getBanDefaultDuration() { return banDefaultDuration; }
    public boolean isBanBroadcast() { return banBroadcast; }
    
    // Custom effects getters
    public boolean isCustomEffectsEnabled() { return customEffectsEnabled; }
    public boolean isZombieInfectionEnabled() { return zombieInfectionEnabled; }
    public int getZombieInfectionDuration() { return zombieInfectionDuration; }
    public double getZombieInfectionSpreadChance() { return zombieInfectionSpreadChance; }
    public int getZombieInfectionCureVillagers() { return zombieInfectionCureVillagers; }
    
    // Messages getters
    public String getMessagesPrefix() { return messagesPrefix; }
    public String getMessagesReloadSuccess() { return messagesReloadSuccess; }
    public String getMessagesReloadError() { return messagesReloadError; }
    
    // Optimization getters
    public boolean isOptimizationTaskOptimizerEnabled() { return optimizationTaskOptimizerEnabled; }
    public int getOptimizationMaxThreads() { return optimizationMaxThreads; }
    public int getOptimizationQueueSize() { return optimizationQueueSize; }
    
    // Debug getters
    public boolean isDebugEnabled() { return debugEnabled; }
    public String getDebugLogLevel() { return debugLogLevel; }
    public boolean isDebugVerboseEvents() { return debugVerboseEvents; }
    
    /**
     * Obtiene la configuración raw de Bukkit
     */
    public FileConfiguration getConfig() {
        return config;
    }
}