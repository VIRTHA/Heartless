package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.AcidWeek;
import com.darkbladedev.mechanics.ExplosiveWeek;
import com.darkbladedev.mechanics.ToxicFog;
import com.darkbladedev.mechanics.BloodAndIronWeek;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

/**
 * DatabaseManager unificado que centraliza todas las operaciones de base de datos
 * y reemplaza los sistemas actuales de almacenamiento y persistencia.
 * 
 * Características:
 * - Compatibilidad con el uso actual
 * - Centralización de operaciones de base de datos
 * - Consistencia en el manejo de datos
 * - Optimización del rendimiento con HikariCP
 * - Mantenimiento simplificado
 * - Fallback a JSON si la base de datos no está disponible
 */
public class DatabaseManager {
    
    private final HeartlessMain plugin;
    private final ConfigManager configManager;
    private final Gson gson;
    
    // HikariCP Connection Pool
    private HikariDataSource dataSource;
    private boolean databaseEnabled;
    private boolean databaseConnected;
    
    // Fallback JSON storage
    private final File dataFolder;
    private final File eventDataFile;
    private final File playerDataFile;
    @SuppressWarnings("unused")
    private final File statisticsFile;
    
    // Cache para optimizar consultas frecuentes
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final long cacheExpirationTime = 300000; // 5 minutos
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    
    public DatabaseManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.configManager = HeartlessMain.getConfigManager();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Configurar archivos de fallback
        this.dataFolder = plugin.getDataFolder();
        this.eventDataFile = new File(dataFolder, "event_data.json");
        this.playerDataFile = new File(dataFolder, "player_data.json");
        this.statisticsFile = new File(dataFolder, "statistics.json");
        
        // Asegurar que el directorio existe
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        initialize();
    }
    
    /**
     * Inicializa el sistema de base de datos
     */
    private void initialize() {
        this.databaseEnabled = configManager.isDatabaseEnabled();
        
        if (databaseEnabled) {
            try {
                setupHikariCP();
                createTables();
                this.databaseConnected = true;
                plugin.getLogger().info("DatabaseManager: Conexión a base de datos establecida exitosamente");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "DatabaseManager: Error conectando a la base de datos, usando fallback JSON", e);
                this.databaseConnected = false;
            }
        } else {
            plugin.getLogger().info("DatabaseManager: Base de datos deshabilitada, usando almacenamiento JSON");
            this.databaseConnected = false;
        }
    }
    
    /**
     * Configura HikariCP con los valores del ConfigManager
     */
    private void setupHikariCP() {
        HikariConfig config = new HikariConfig();
        
        // Configuración desde ConfigManager
        config.setJdbcUrl("jdbc:mysql://" + configManager.getDatabaseHost() + ":" + 
                          configManager.getDatabasePort() + "/" + configManager.getDatabaseName());
        config.setUsername(configManager.getDatabaseUsername());
        config.setPassword(configManager.getDatabasePassword());
        
        // Configuración del pool
        config.setMaximumPoolSize(configManager.getDatabasePoolSize());
        config.setConnectionTimeout(configManager.getDatabaseConnectionTimeout());
        config.setIdleTimeout(600000); // 10 minutos
        config.setMaxLifetime(1800000); // 30 minutos
        config.setLeakDetectionThreshold(60000); // 1 minuto
        
        // Configuraciones adicionales para optimización
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        this.dataSource = new HikariDataSource(config);
    }
    
    /**
     * Crea las tablas necesarias si no existen
     */
    private void createTables() {
        if (!databaseConnected) return;
        
        try (Connection conn = dataSource.getConnection()) {
            // Tabla para eventos semanales
            String createEventsTable = """
                CREATE TABLE IF NOT EXISTS weekly_events (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    event_type VARCHAR(50) NOT NULL,
                    start_time BIGINT NOT NULL,
                    end_time BIGINT,
                    is_active BOOLEAN DEFAULT FALSE,
                    is_paused BOOLEAN DEFAULT FALSE,
                    pause_start_time BIGINT DEFAULT 0,
                    total_paused_time BIGINT DEFAULT 0,
                    event_data JSON,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )""";
            
            // Tabla para datos de jugadores
            String createPlayersTable = """
                CREATE TABLE IF NOT EXISTS player_data (
                    uuid VARCHAR(36) PRIMARY KEY,
                    player_name VARCHAR(16),
                    infected BOOLEAN DEFAULT FALSE,
                    infection_time BIGINT DEFAULT 0,
                    cured_infections INT DEFAULT 0,
                    red_moon_kills INT DEFAULT 0,
                    player_kills INT DEFAULT 0,
                    consecutive_kills INT DEFAULT 0,
                    last_hostile_kill BIGINT DEFAULT 0,
                    last_player_kill BIGINT DEFAULT 0,
                    ban_count INT DEFAULT 0,
                    health_data JSON,
                    effects_data JSON,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )""";
            
            // Tabla para estadísticas
            String createStatsTable = """
                CREATE TABLE IF NOT EXISTS event_statistics (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    event_id INT,
                    stat_type VARCHAR(50) NOT NULL,
                    stat_key VARCHAR(100) NOT NULL,
                    stat_value TEXT,
                    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (event_id) REFERENCES weekly_events(id) ON DELETE CASCADE,
                    INDEX idx_event_stat (event_id, stat_type),
                    INDEX idx_stat_key (stat_key)
                )""";
            
            // Tabla para configuraciones
            String createConfigTable = """
                CREATE TABLE IF NOT EXISTS plugin_config (
                    config_key VARCHAR(100) PRIMARY KEY,
                    config_value TEXT,
                    config_type VARCHAR(20) DEFAULT 'STRING',
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )""";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createEventsTable);
                stmt.execute(createPlayersTable);
                stmt.execute(createStatsTable);
                stmt.execute(createConfigTable);
                
                plugin.getLogger().info("DatabaseManager: Tablas creadas/verificadas exitosamente");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "DatabaseManager: Error creando tablas", e);
            throw new RuntimeException("Error inicializando base de datos", e);
        }
    }
    
    /**
     * Guarda un evento semanal
     */
    public CompletableFuture<Boolean> saveWeeklyEvent(WeeklyEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            if (databaseConnected) {
                return saveEventToDatabase(event);
            } else {
                return saveEventToJSON(event);
            }
        });
    }
    
    /**
     * Guarda evento en base de datos
     */
    private boolean saveEventToDatabase(WeeklyEvent event) {
        String sql = """
            INSERT INTO weekly_events (event_type, start_time, end_time, is_active, 
                                     is_paused, pause_start_time, total_paused_time, event_data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                end_time = VALUES(end_time),
                is_active = VALUES(is_active),
                is_paused = VALUES(is_paused),
                pause_start_time = VALUES(pause_start_time),
                total_paused_time = VALUES(total_paused_time),
                event_data = VALUES(event_data)
            """;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, getEventTypeName(event));
            stmt.setLong(2, event.getStartTime());
            stmt.setLong(3, event.getEndTime());
            stmt.setBoolean(4, event.isActive());
            stmt.setBoolean(5, event.isPaused());
            stmt.setLong(6, event.getPauseStartTime());
            stmt.setLong(7, event.getTotalPausedTime());
            stmt.setString(8, serializeEventData(event));
            
            int result = stmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando evento en base de datos", e);
            return false;
        }
    }
    
    /**
     * Guarda evento en JSON como fallback
     */
    private boolean saveEventToJSON(WeeklyEvent event) {
        try {
            JsonObject eventData = new JsonObject();
            eventData.addProperty("eventType", getEventTypeName(event));
            eventData.addProperty("startTime", event.getStartTime());
            eventData.addProperty("endTime", event.getEndTime());
            eventData.addProperty("isActive", event.isActive());
            eventData.addProperty("isPaused", event.isPaused());
            eventData.addProperty("pauseStartTime", event.getPauseStartTime());
            eventData.addProperty("totalPausedTime", event.getTotalPausedTime());
            eventData.addProperty("specificData", serializeEventData(event));
            
            try (FileWriter writer = new FileWriter(eventDataFile)) {
                gson.toJson(eventData, writer);
                return true;
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando evento en JSON", e);
            return false;
        }
    }
    
    /**
     * Carga el evento semanal actual
     */
    public CompletableFuture<WeeklyEventData> loadWeeklyEvent() {
        return CompletableFuture.supplyAsync(() -> {
            if (databaseConnected) {
                return loadEventFromDatabase();
            } else {
                return loadEventFromJSON();
            }
        });
    }
    
    /**
     * Carga evento desde base de datos
     */
    private WeeklyEventData loadEventFromDatabase() {
        String sql = "SELECT * FROM weekly_events WHERE is_active = TRUE ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                WeeklyEventData data = new WeeklyEventData();
                data.eventType = rs.getString("event_type");
                data.startTime = rs.getLong("start_time");
                data.endTime = rs.getLong("end_time");
                data.eventActive = rs.getBoolean("is_active");
                data.isPaused = rs.getBoolean("is_paused");
                data.pauseStartTime = rs.getLong("pause_start_time");
                data.totalPausedTime = rs.getLong("total_paused_time");
                return data;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando evento desde base de datos", e);
        }
        
        return null;
    }
    
    /**
     * Carga evento desde JSON
     */
    private WeeklyEventData loadEventFromJSON() {
        if (!eventDataFile.exists()) return null;
        
        try (FileReader reader = new FileReader(eventDataFile)) {
            JsonObject eventData = JsonParser.parseReader(reader).getAsJsonObject();
            
            WeeklyEventData data = new WeeklyEventData();
            data.eventType = eventData.get("eventType").getAsString();
            data.startTime = eventData.get("startTime").getAsLong();
            data.endTime = eventData.get("endTime").getAsLong();
            data.eventActive = eventData.get("isActive").getAsBoolean();
            data.isPaused = eventData.get("isPaused").getAsBoolean();
            data.pauseStartTime = eventData.get("pauseStartTime").getAsLong();
            data.totalPausedTime = eventData.get("totalPausedTime").getAsLong();
            
            return data;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando evento desde JSON", e);
            return null;
        }
    }
    
    /**
     * Guarda datos de jugador
     */
    public CompletableFuture<Boolean> savePlayerData(UUID playerUUID, String playerName, Map<String, Object> data) {
        return CompletableFuture.supplyAsync(() -> {
            if (databaseConnected) {
                return savePlayerToDatabase(playerUUID, playerName, data);
            } else {
                return savePlayerToJSON(playerUUID, playerName, data);
            }
        });
    }
    
    /**
     * Guarda datos de jugador en base de datos
     */
    private boolean savePlayerToDatabase(UUID playerUUID, String playerName, Map<String, Object> data) {
        String sql = """
            INSERT INTO player_data (uuid, player_name, infected, infection_time, cured_infections,
                                   red_moon_kills, player_kills, consecutive_kills, last_hostile_kill,
                                   last_player_kill, ban_count, health_data, effects_data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                player_name = VALUES(player_name),
                infected = VALUES(infected),
                infection_time = VALUES(infection_time),
                cured_infections = VALUES(cured_infections),
                red_moon_kills = VALUES(red_moon_kills),
                player_kills = VALUES(player_kills),
                consecutive_kills = VALUES(consecutive_kills),
                last_hostile_kill = VALUES(last_hostile_kill),
                last_player_kill = VALUES(last_player_kill),
                ban_count = VALUES(ban_count),
                health_data = VALUES(health_data),
                effects_data = VALUES(effects_data)
            """;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, playerName);
            stmt.setBoolean(3, (Boolean) data.getOrDefault("infected", false));
            stmt.setLong(4, (Long) data.getOrDefault("infectionTime", 0L));
            stmt.setInt(5, (Integer) data.getOrDefault("curedInfections", 0));
            stmt.setInt(6, (Integer) data.getOrDefault("redMoonKills", 0));
            stmt.setInt(7, (Integer) data.getOrDefault("playerKills", 0));
            stmt.setInt(8, (Integer) data.getOrDefault("consecutiveKills", 0));
            stmt.setLong(9, (Long) data.getOrDefault("lastHostileKill", 0L));
            stmt.setLong(10, (Long) data.getOrDefault("lastPlayerKill", 0L));
            stmt.setInt(11, (Integer) data.getOrDefault("banCount", 0));
            stmt.setString(12, gson.toJson(data.getOrDefault("healthData", new HashMap<>())));
            stmt.setString(13, gson.toJson(data.getOrDefault("effectsData", new HashMap<>())));
            
            int result = stmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando datos de jugador en base de datos", e);
            return false;
        }
    }
    
    /**
     * Guarda datos de jugador en JSON
     */
    private boolean savePlayerToJSON(UUID playerUUID, String playerName, Map<String, Object> data) {
        try {
            JsonObject allPlayerData;
            
            // Cargar datos existentes
            if (playerDataFile.exists()) {
                try (FileReader reader = new FileReader(playerDataFile)) {
                    allPlayerData = JsonParser.parseReader(reader).getAsJsonObject();
                }
            } else {
                allPlayerData = new JsonObject();
            }
            
            // Agregar/actualizar datos del jugador
            JsonObject playerData = new JsonObject();
            playerData.addProperty("playerName", playerName);
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (entry.getValue() instanceof String) {
                    playerData.addProperty(entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() instanceof Number) {
                    playerData.addProperty(entry.getKey(), (Number) entry.getValue());
                } else if (entry.getValue() instanceof Boolean) {
                    playerData.addProperty(entry.getKey(), (Boolean) entry.getValue());
                } else {
                    playerData.addProperty(entry.getKey(), gson.toJson(entry.getValue()));
                }
            }
            
            allPlayerData.add(playerUUID.toString(), playerData);
            
            // Guardar archivo
            try (FileWriter writer = new FileWriter(playerDataFile)) {
                gson.toJson(allPlayerData, writer);
                return true;
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando datos de jugador en JSON", e);
            return false;
        }
    }
    
    /**
     * Carga datos de jugador
     */
    public CompletableFuture<Map<String, Object>> loadPlayerData(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            // Verificar cache primero
            String cacheKey = "player_" + playerUUID.toString();
            if (isCacheValid(cacheKey)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cachedData = (Map<String, Object>) cache.get(cacheKey);
                return cachedData;
            }
            
            Map<String, Object> data;
            if (databaseConnected) {
                data = loadPlayerFromDatabase(playerUUID);
            } else {
                data = loadPlayerFromJSON(playerUUID);
            }
            
            // Guardar en cache
            if (data != null) {
                cache.put(cacheKey, data);
                cacheTimestamps.put(cacheKey, System.currentTimeMillis());
            }
            
            return data;
        });
    }
    
    /**
     * Carga datos de jugador desde base de datos
     */
    private Map<String, Object> loadPlayerFromDatabase(UUID playerUUID) {
        String sql = "SELECT * FROM player_data WHERE uuid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, playerUUID.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("playerName", rs.getString("player_name"));
                    data.put("infected", rs.getBoolean("infected"));
                    data.put("infectionTime", rs.getLong("infection_time"));
                    data.put("curedInfections", rs.getInt("cured_infections"));
                    data.put("redMoonKills", rs.getInt("red_moon_kills"));
                    data.put("playerKills", rs.getInt("player_kills"));
                    data.put("consecutiveKills", rs.getInt("consecutive_kills"));
                    data.put("lastHostileKill", rs.getLong("last_hostile_kill"));
                    data.put("lastPlayerKill", rs.getLong("last_player_kill"));
                    data.put("banCount", rs.getInt("ban_count"));
                    
                    String healthDataJson = rs.getString("health_data");
                    String effectsDataJson = rs.getString("effects_data");
                    
                    if (healthDataJson != null) {
                        data.put("healthData", gson.fromJson(healthDataJson, Map.class));
                    }
                    if (effectsDataJson != null) {
                        data.put("effectsData", gson.fromJson(effectsDataJson, Map.class));
                    }
                    
                    return data;
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando datos de jugador desde base de datos", e);
        }
        
        return null;
    }
    
    /**
     * Carga datos de jugador desde JSON
     */
    private Map<String, Object> loadPlayerFromJSON(UUID playerUUID) {
        if (!playerDataFile.exists()) return null;
        
        try (FileReader reader = new FileReader(playerDataFile)) {
            JsonObject allPlayerData = JsonParser.parseReader(reader).getAsJsonObject();
            
            if (allPlayerData.has(playerUUID.toString())) {
                JsonObject playerData = allPlayerData.getAsJsonObject(playerUUID.toString());
                Map<String, Object> data = new HashMap<>();
                
                playerData.entrySet().forEach(entry -> {
                    String key = entry.getKey();
                    if (entry.getValue().isJsonPrimitive()) {
                        if (entry.getValue().getAsJsonPrimitive().isString()) {
                            data.put(key, entry.getValue().getAsString());
                        } else if (entry.getValue().getAsJsonPrimitive().isNumber()) {
                            data.put(key, entry.getValue().getAsNumber());
                        } else if (entry.getValue().getAsJsonPrimitive().isBoolean()) {
                            data.put(key, entry.getValue().getAsBoolean());
                        }
                    } else {
                        data.put(key, gson.fromJson(entry.getValue(), Map.class));
                    }
                });
                
                return data;
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando datos de jugador desde JSON", e);
        }
        
        return null;
    }
    
    /**
     * Método llamado cuando la configuración se recarga
     */
    public void onConfigReload() {
        plugin.getLogger().info("DatabaseManager: Aplicando cambios de configuración...");
        
        boolean newDatabaseEnabled = configManager.isDatabaseEnabled();
        
        // Si el estado de la base de datos cambió
        if (newDatabaseEnabled != databaseEnabled) {
            if (newDatabaseEnabled) {
                // Habilitar base de datos
                try {
                    setupHikariCP();
                    createTables();
                    this.databaseConnected = true;
                    plugin.getLogger().info("DatabaseManager: Base de datos habilitada y conectada");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "DatabaseManager: Error habilitando base de datos", e);
                    this.databaseConnected = false;
                }
            } else {
                // Deshabilitar base de datos
                if (dataSource != null && !dataSource.isClosed()) {
                    dataSource.close();
                }
                this.databaseConnected = false;
                plugin.getLogger().info("DatabaseManager: Base de datos deshabilitada, usando JSON");
            }
            
            this.databaseEnabled = newDatabaseEnabled;
        }
        
        // Limpiar cache
        cache.clear();
        cacheTimestamps.clear();
        
        plugin.getLogger().info("DatabaseManager: Configuración actualizada exitosamente");
    }
    
    /**
     * Cierra las conexiones y limpia recursos
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("DatabaseManager: Conexiones cerradas exitosamente");
        }
        
        cache.clear();
        cacheTimestamps.clear();
    }
    
    // Métodos de utilidad
    
    private String getEventTypeName(WeeklyEvent event) {
        if (event instanceof UndeadWeek) return "undead_week";
        if (event instanceof AcidWeek) return "acid_week";
        if (event instanceof ExplosiveWeek) return "explosive_week";
        if (event instanceof ToxicFog) return "toxic_fog";
        if (event instanceof BloodAndIronWeek) return "blood_and_iron_week";
        return "unknown";
    }
    
    private String serializeEventData(WeeklyEvent event) {
        // Serializar datos específicos del evento
        JsonObject eventData = new JsonObject();
        
        if (event instanceof UndeadWeek) {
            UndeadWeek undeadWeek = (UndeadWeek) event;
            eventData.addProperty("infectedPlayersCount", undeadWeek.getInfectedPlayersCount());
            eventData.addProperty("curedInfectionsCount", undeadWeek.getTotalCuredInfectionsCount());
            // Agregar más datos específicos según sea necesario
        }
        // Agregar más tipos de eventos según sea necesario
        
        return gson.toJson(eventData);
    }
    
    private boolean isCacheValid(String key) {
        if (!cache.containsKey(key) || !cacheTimestamps.containsKey(key)) {
            return false;
        }
        
        long timestamp = cacheTimestamps.get(key);
        return (System.currentTimeMillis() - timestamp) < cacheExpirationTime;
    }
    
    // Getters para compatibilidad
    
    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }
    
    public boolean isDatabaseConnected() {
        return databaseConnected;
    }
    
    public HikariDataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * Clase de datos para eventos semanales (compatibilidad con StorageManager)
     */
    public static class WeeklyEventData {
        public boolean isPaused;
        public long pauseStartTime;
        public long totalPausedTime;
        public long startTime;
        public long endTime;
        public String eventType;
        public boolean eventActive;
    }
}