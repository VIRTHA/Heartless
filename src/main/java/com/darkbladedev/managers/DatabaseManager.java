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
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

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
        this.configManager = HeartlessMain.getInstance().getConfigManager();
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
        plugin.getLogger().info("DatabaseManager: Iniciando inicialización...");
        
        this.databaseEnabled = configManager.isDatabaseEnabled();
        plugin.getLogger().info("DatabaseManager: Base de datos habilitada: " + databaseEnabled);
        
        if (databaseEnabled) {
            try {
                plugin.getLogger().info("DatabaseManager: Configurando HikariCP...");
                setupHikariCP();
                plugin.getLogger().info("DatabaseManager: HikariCP configurado exitosamente");
                
                // Probar la conexión antes de continuar
                plugin.getLogger().info("DatabaseManager: Probando conexión a la base de datos...");
                if (!testConnectionInternal()) {
                    throw new SQLException("No se pudo establecer conexión con la base de datos");
                }
                plugin.getLogger().info("DatabaseManager: Conexión probada exitosamente");
                
                // Establecer databaseConnected = true ANTES de crear tablas
                this.databaseConnected = true;
                
                plugin.getLogger().info("DatabaseManager: Creando tablas...");
                createTables();
                plugin.getLogger().info("DatabaseManager: Tablas creadas exitosamente");
                
                // Verificar que las tablas existan después de crearlas
                verifyTablesExist();
                plugin.getLogger().info("DatabaseManager: Conexión a base de datos establecida exitosamente");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "DatabaseManager: Error conectando a la base de datos, usando fallback JSON", e);
                plugin.getLogger().severe("DatabaseManager: Detalles del error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                if (e.getCause() != null) {
                    plugin.getLogger().severe("DatabaseManager: Causa raíz: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
                }
                this.databaseConnected = false;
                
                // Limpiar recursos si hay error
                if (dataSource != null && !dataSource.isClosed()) {
                    dataSource.close();
                    dataSource = null;
                }
            }
        } else {
            plugin.getLogger().info("DatabaseManager: Base de datos deshabilitada, usando almacenamiento JSON");
            this.databaseConnected = false;
        }
        
        plugin.getLogger().info("DatabaseManager: Inicialización completada. Estado final - Conectado: " + this.databaseConnected);
    }
    
    /**
     * Verifica que todas las tablas necesarias existan en la base de datos
     */
    private void verifyTablesExist() {
        if (!databaseConnected || dataSource == null) {
            plugin.getLogger().warning("DatabaseManager: No se puede verificar tablas - conexión no disponible");
            return;
        }
        
        plugin.getLogger().info("DatabaseManager: Verificando existencia de tablas...");
        
        String[] requiredTables = {"weekly_events", "player_data"};
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            for (String tableName : requiredTables) {
                try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        plugin.getLogger().info("DatabaseManager: Tabla '" + tableName + "' existe correctamente");
                    } else {
                        plugin.getLogger().severe("DatabaseManager: ¡TABLA FALTANTE! '" + tableName + "' no existe");
                        // Intentar recrear la tabla específica
                        recreateTable(tableName, conn);
                    }
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "DatabaseManager: Error verificando tablas", e);
        }
        
        plugin.getLogger().info("DatabaseManager: Verificación de tablas completada");
    }
    
    /**
     * Recrea una tabla específica si no existe
     */
    private void recreateTable(String tableName, Connection conn) {
        plugin.getLogger().warning("DatabaseManager: Intentando recrear tabla: " + tableName);
        
        try {
            if ("weekly_events".equals(tableName)) {
                createWeeklyEventsTable(conn);
            } else if ("player_data".equals(tableName)) {
                createPlayerDataTable(conn);
            }
            plugin.getLogger().info("DatabaseManager: Tabla '" + tableName + "' recreada exitosamente");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "DatabaseManager: Error recreando tabla " + tableName, e);
        }
    }
    
    /**
     * Crea específicamente la tabla weekly_events
     */
    private void createWeeklyEventsTable(Connection conn) throws SQLException {
        String databaseType = configManager.getDatabaseType();
        String sql;
        
        if ("mysql".equalsIgnoreCase(databaseType)) {
            sql = """
                CREATE TABLE IF NOT EXISTS weekly_events (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    event_type VARCHAR(50) NOT NULL UNIQUE,
                    start_time BIGINT NOT NULL,
                    end_time BIGINT NOT NULL,
                    is_active BOOLEAN DEFAULT FALSE,
                    is_paused BOOLEAN DEFAULT FALSE,
                    pause_start_time BIGINT DEFAULT 0,
                    total_paused_time BIGINT DEFAULT 0,
                    event_data TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
        } else {
            sql = """
                CREATE TABLE IF NOT EXISTS weekly_events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    event_type TEXT NOT NULL UNIQUE,
                    start_time INTEGER NOT NULL,
                    end_time INTEGER NOT NULL,
                    is_active INTEGER DEFAULT 0,
                    is_paused INTEGER DEFAULT 0,
                    pause_start_time INTEGER DEFAULT 0,
                    total_paused_time INTEGER DEFAULT 0,
                    event_data TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            plugin.getLogger().info("DatabaseManager: Tabla weekly_events creada/verificada");
        }
    }
    
    /**
     * Crea específicamente la tabla player_data
     */
    private void createPlayerDataTable(Connection conn) throws SQLException {
        String databaseType = configManager.getDatabaseType();
        String sql;
        
        if ("mysql".equalsIgnoreCase(databaseType)) {
            sql = """
                CREATE TABLE IF NOT EXISTS player_data (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    player_uuid VARCHAR(36) NOT NULL UNIQUE,
                    player_name VARCHAR(16) NOT NULL,
                    data_json TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_player_uuid (player_uuid),
                    INDEX idx_player_name (player_name)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
        } else {
            sql = """
                CREATE TABLE IF NOT EXISTS player_data (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid TEXT NOT NULL UNIQUE,
                    player_name TEXT NOT NULL,
                    data_json TEXT NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            plugin.getLogger().info("DatabaseManager: Tabla player_data creada/verificada");
        }
    }
    
    /**
     * Configura HikariCP con los valores del ConfigManager
     */
    private void setupHikariCP() {
        HikariConfig config = new HikariConfig();
        
        // Configuración según el tipo de base de datos
        if (configManager.isMySQLDatabase()) {
            setupMySQLConnection(config);
        } else if (configManager.isSQLiteDatabase()) {
            setupSQLiteConnection(config);
        } else {
            throw new IllegalArgumentException("Tipo de base de datos no soportado: " + configManager.getDatabaseType());
        }
        
        // Configuración común del pool
        config.setMaximumPoolSize(configManager.getDatabasePoolSize());
        config.setConnectionTimeout(configManager.getDatabaseConnectionTimeout());
        config.setIdleTimeout(600000); // 10 minutos
        config.setMaxLifetime(1800000); // 30 minutos
        config.setLeakDetectionThreshold(60000); // 1 minuto
        
        this.dataSource = new HikariDataSource(config);
    }
    
    /**
     * Configura la conexión MySQL
     */
    private void setupMySQLConnection(HikariConfig config) {
        config.setJdbcUrl("jdbc:mysql://" + configManager.getDatabaseHost() + ":" + 
                          configManager.getDatabasePort() + "/" + configManager.getDatabaseName());
        config.setUsername(configManager.getDatabaseUsername());
        config.setPassword(configManager.getDatabasePassword());
        
        // Configuraciones específicas de MySQL para optimización
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
        
        plugin.getLogger().info("DatabaseManager: Configurando conexión MySQL a " + 
                               configManager.getDatabaseHost() + ":" + configManager.getDatabasePort());
    }
    
    /**
     * Configura la conexión SQLite
     */
    private void setupSQLiteConnection(HikariConfig config) {
        File dbFile = new File(plugin.getDataFolder(), configManager.getSqliteFile());
        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("org.sqlite.JDBC");
        
        // Configuraciones específicas de SQLite
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");
        config.addDataSourceProperty("cache_size", "10000");
        config.addDataSourceProperty("foreign_keys", "true");
        config.addDataSourceProperty("busy_timeout", "30000");
        
        // SQLite funciona mejor con menos conexiones concurrentes
        config.setMaximumPoolSize(Math.min(configManager.getDatabasePoolSize(), 5));
        
        plugin.getLogger().info("DatabaseManager: Configurando conexión SQLite a " + dbFile.getAbsolutePath());
    }
    
    /**
     * Crea las tablas necesarias si no existen
     */
    private void createTables() {
        plugin.getLogger().info("DatabaseManager: Iniciando createTables(). Estado databaseConnected: " + databaseConnected);
        
        if (!databaseConnected) {
            plugin.getLogger().warning("DatabaseManager: createTables() cancelado - databaseConnected es false");
            return;
        }
        
        plugin.getLogger().info("DatabaseManager: Obteniendo conexión de dataSource...");
        try (Connection conn = dataSource.getConnection()) {
            plugin.getLogger().info("DatabaseManager: Conexión obtenida exitosamente. Tipo de BD: " + configManager.getDatabaseType());
            String createEventsTable;
            String createPlayersTable;
            String createStatsTable;
            String createConfigTable;
            
            if (configManager.isMySQLDatabase()) {
                // Sintaxis específica de MySQL
                createEventsTable = """
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
                
                createPlayersTable = """
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
                
                createStatsTable = """
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
                
                createConfigTable = """
                    CREATE TABLE IF NOT EXISTS plugin_config (
                        config_key VARCHAR(100) PRIMARY KEY,
                        config_value TEXT,
                        config_type VARCHAR(20) DEFAULT 'STRING',
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )""";
            } else {
                // Sintaxis compatible con SQLite
                createEventsTable = """
                    CREATE TABLE IF NOT EXISTS weekly_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        event_type TEXT NOT NULL,
                        start_time INTEGER NOT NULL,
                        end_time INTEGER,
                        is_active INTEGER DEFAULT 0,
                        is_paused INTEGER DEFAULT 0,
                        pause_start_time INTEGER DEFAULT 0,
                        total_paused_time INTEGER DEFAULT 0,
                        event_data TEXT,
                        created_at INTEGER DEFAULT (strftime('%s', 'now')),
                        updated_at INTEGER DEFAULT (strftime('%s', 'now'))
                    )""";
                
                createPlayersTable = """
                    CREATE TABLE IF NOT EXISTS player_data (
                        uuid TEXT PRIMARY KEY,
                        player_name TEXT,
                        infected INTEGER DEFAULT 0,
                        infection_time INTEGER DEFAULT 0,
                        cured_infections INTEGER DEFAULT 0,
                        red_moon_kills INTEGER DEFAULT 0,
                        player_kills INTEGER DEFAULT 0,
                        consecutive_kills INTEGER DEFAULT 0,
                        last_hostile_kill INTEGER DEFAULT 0,
                        last_player_kill INTEGER DEFAULT 0,
                        ban_count INTEGER DEFAULT 0,
                        health_data TEXT,
                        effects_data TEXT,
                        created_at INTEGER DEFAULT (strftime('%s', 'now')),
                        updated_at INTEGER DEFAULT (strftime('%s', 'now'))
                    )""";
                
                createStatsTable = """
                    CREATE TABLE IF NOT EXISTS event_statistics (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        event_id INTEGER,
                        stat_type TEXT NOT NULL,
                        stat_key TEXT NOT NULL,
                        stat_value TEXT,
                        recorded_at INTEGER DEFAULT (strftime('%s', 'now')),
                        FOREIGN KEY (event_id) REFERENCES weekly_events(id) ON DELETE CASCADE
                    )""";
                
                createConfigTable = """
                    CREATE TABLE IF NOT EXISTS plugin_config (
                        config_key TEXT PRIMARY KEY,
                        config_value TEXT,
                        config_type TEXT DEFAULT 'STRING',
                        updated_at INTEGER DEFAULT (strftime('%s', 'now'))
                    )""";
            }
            
            try (Statement stmt = conn.createStatement()) {
                plugin.getLogger().info("DatabaseManager: Ejecutando creación de tabla weekly_events...");
                stmt.execute(createEventsTable);
                plugin.getLogger().info("DatabaseManager: Tabla weekly_events creada exitosamente");
                
                plugin.getLogger().info("DatabaseManager: Ejecutando creación de tabla player_data...");
                stmt.execute(createPlayersTable);
                plugin.getLogger().info("DatabaseManager: Tabla player_data creada exitosamente");
                
                plugin.getLogger().info("DatabaseManager: Ejecutando creación de tabla event_statistics...");
                stmt.execute(createStatsTable);
                plugin.getLogger().info("DatabaseManager: Tabla event_statistics creada exitosamente");
                
                plugin.getLogger().info("DatabaseManager: Ejecutando creación de tabla plugin_config...");
                stmt.execute(createConfigTable);
                plugin.getLogger().info("DatabaseManager: Tabla plugin_config creada exitosamente");
                
                // Crear triggers para SQLite para simular ON UPDATE CURRENT_TIMESTAMP
                if (configManager.isSQLiteDatabase()) {
                    plugin.getLogger().info("DatabaseManager: Creando triggers para SQLite...");
                    String updateTriggerEvents = """
                        CREATE TRIGGER IF NOT EXISTS update_weekly_events_timestamp 
                        AFTER UPDATE ON weekly_events
                        BEGIN
                            UPDATE weekly_events SET updated_at = strftime('%s', 'now') WHERE id = NEW.id;
                        END""";
                    
                    String updateTriggerPlayers = """
                        CREATE TRIGGER IF NOT EXISTS update_player_data_timestamp 
                        AFTER UPDATE ON player_data
                        BEGIN
                            UPDATE player_data SET updated_at = strftime('%s', 'now') WHERE uuid = NEW.uuid;
                        END""";
                    
                    String updateTriggerConfig = """
                        CREATE TRIGGER IF NOT EXISTS update_plugin_config_timestamp 
                        AFTER UPDATE ON plugin_config
                        BEGIN
                            UPDATE plugin_config SET updated_at = strftime('%s', 'now') WHERE config_key = NEW.config_key;
                        END""";
                    
                    plugin.getLogger().info("DatabaseManager: Ejecutando trigger para weekly_events...");
                    stmt.execute(updateTriggerEvents);
                    plugin.getLogger().info("DatabaseManager: Ejecutando trigger para player_data...");
                    stmt.execute(updateTriggerPlayers);
                    plugin.getLogger().info("DatabaseManager: Ejecutando trigger para plugin_config...");
                    stmt.execute(updateTriggerConfig);
                    plugin.getLogger().info("DatabaseManager: Todos los triggers creados exitosamente");
                    
                    // Crear índices para SQLite
                    plugin.getLogger().info("DatabaseManager: Creando índices para SQLite...");
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_event_stat ON event_statistics(event_id, stat_type)");
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_stat_key ON event_statistics(stat_key)");
                    plugin.getLogger().info("DatabaseManager: Índices creados exitosamente");
                }
                
                plugin.getLogger().info("DatabaseManager: Tablas creadas/verificadas exitosamente para " + 
                                       configManager.getDatabaseType().toUpperCase());
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
        String sql;
        String databaseType = configManager.getDatabaseType();
        
        if ("mysql".equalsIgnoreCase(databaseType)) {
            // Sintaxis MySQL
            sql = """
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
        } else {
            // Sintaxis SQLite
            sql = """
                INSERT OR REPLACE INTO weekly_events (event_type, start_time, end_time, is_active, 
                                                    is_paused, pause_start_time, total_paused_time, event_data)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        }
        
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
            
            // Validaciones null-safe para evitar NullPointerException
            if (eventData.get("eventType") != null) {
                data.eventType = eventData.get("eventType").getAsString();
            } else {
                plugin.getLogger().warning("Campo 'eventType' faltante en event_data.json");
                data.eventType = "empty";
            }
            
            if (eventData.get("startTime") != null) {
                data.startTime = eventData.get("startTime").getAsLong();
            } else {
                data.startTime = 0L;
            }
            
            if (eventData.get("endTime") != null) {
                data.endTime = eventData.get("endTime").getAsLong();
            } else {
                data.endTime = 0L;
            }
            
            if (eventData.get("isActive") != null) {
                data.eventActive = eventData.get("isActive").getAsBoolean();
            } else {
                plugin.getLogger().warning("Campo 'isActive' faltante en event_data.json");
                data.eventActive = false;
            }
            
            if (eventData.get("isPaused") != null) {
                data.isPaused = eventData.get("isPaused").getAsBoolean();
            } else {
                data.isPaused = false;
            }
            
            if (eventData.get("pauseStartTime") != null) {
                data.pauseStartTime = eventData.get("pauseStartTime").getAsLong();
            } else {
                data.pauseStartTime = 0L;
            }
            
            if (eventData.get("totalPausedTime") != null) {
                data.totalPausedTime = eventData.get("totalPausedTime").getAsLong();
            } else {
                data.totalPausedTime = 0L;
            }
            
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
        String sql;
        String databaseType = configManager.getDatabaseType();
        
        if ("mysql".equalsIgnoreCase(databaseType)) {
            // Sintaxis MySQL
            sql = """
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
        } else {
            // Sintaxis SQLite
            sql = """
                INSERT OR REPLACE INTO player_data (uuid, player_name, infected, infection_time, cured_infections,
                                                  red_moon_kills, player_kills, consecutive_kills, last_hostile_kill,
                                                  last_player_kill, ban_count, health_data, effects_data)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        }
        
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
     * Prueba la conexión a la base de datos
     * @return true si la conexión es exitosa
     */
    public boolean testConnection() {
        return testConnectionInternal();
    }
    
    private boolean testConnectionInternal() {
        if (!databaseEnabled || dataSource == null) {
            plugin.getLogger().warning("DatabaseManager: No se puede probar conexión - Base de datos deshabilitada o DataSource nulo");
            return false;
        }
        
        try (Connection connection = dataSource.getConnection()) {
            if (connection == null || connection.isClosed()) {
                plugin.getLogger().warning("DatabaseManager: Conexión nula o cerrada");
                return false;
            }
            
            // Probar con una consulta simple
            try (Statement stmt = connection.createStatement()) {
                if (configManager.isSQLiteDatabase()) {
                    stmt.executeQuery("SELECT 1").close();
                } else {
                    stmt.executeQuery("SELECT 1").close();
                }
            }
            
            plugin.getLogger().info("DatabaseManager: Prueba de conexión exitosa");
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "DatabaseManager: Error probando conexión a la base de datos", e);
            plugin.getLogger().severe("DatabaseManager: Detalles del error de conexión: " + e.getErrorCode() + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica la integridad de los datos almacenados
     * @return CompletableFuture con el resultado de la verificación
     */
    public CompletableFuture<DataIntegrityResult> verifyDataIntegrity() {
        return CompletableFuture.supplyAsync(() -> {
            DataIntegrityResult result = new DataIntegrityResult();
            
            try {
                // Verificar integridad de eventos semanales
                result.eventDataIntegrity = verifyEventDataIntegrity();
                
                // Verificar integridad de datos de jugadores
                result.playerDataIntegrity = verifyPlayerDataIntegrity();
                
                // Verificar consistencia entre base de datos y JSON
                if (databaseConnected) {
                    result.consistencyCheck = verifyDatabaseJsonConsistency();
                }
                
                result.overallIntegrity = result.eventDataIntegrity && 
                                        result.playerDataIntegrity && 
                                        (result.consistencyCheck || !databaseConnected);
                
                plugin.getLogger().info(String.format(
                    "Verificación de integridad completada - Eventos: %s, Jugadores: %s, Consistencia: %s",
                    result.eventDataIntegrity ? "OK" : "ERROR",
                    result.playerDataIntegrity ? "OK" : "ERROR",
                    result.consistencyCheck ? "OK" : "ERROR"
                ));
                
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error durante verificación de integridad", e);
                result.overallIntegrity = false;
                result.errorMessage = e.getMessage();
            }
            
            return result;
        });
    }
    
    /**
     * Verifica la integridad de los datos de eventos
     */
    private boolean verifyEventDataIntegrity() {
        try {
            if (databaseConnected) {
                // Verificar estructura de tabla de eventos con manejo robusto de errores
                try (Connection conn = dataSource.getConnection()) {
                    String checkQuery = "SELECT COUNT(*) FROM weekly_events WHERE event_data IS NOT NULL";
                    try (PreparedStatement stmt = conn.prepareStatement(checkQuery);
                         ResultSet rs = stmt.executeQuery()) {
                        
                        if (rs.next()) {
                            int validRecords = rs.getInt(1);
                            plugin.getLogger().info("Registros válidos de eventos en BD: " + validRecords);
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error de SQL verificando datos de eventos: " + e.getMessage(), e);
                    // Continuar con verificación JSON como fallback
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error inesperado verificando datos de eventos en BD: " + e.getMessage(), e);
                    // Continuar con verificación JSON como fallback
                }
            }
            
            // Verificar archivo JSON de eventos
            if (eventDataFile.exists()) {
                try (FileReader reader = new FileReader(eventDataFile)) {
                    JsonObject eventJson = JsonParser.parseReader(reader).getAsJsonObject();
                    
                    // Verificar campos esenciales
                    boolean hasValidStructure = eventJson.has("eventActive") && 
                                              eventJson.has("eventType") && 
                                              eventJson.has("startTime");
                    
                    if (!hasValidStructure) {
                        plugin.getLogger().warning("Estructura de datos de eventos JSON inválida");
                        return false;
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error leyendo archivo JSON de eventos: " + e.getMessage(), e);
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error general verificando integridad de datos de eventos", e);
            return false;
        }
    }
    
    /**
     * Verifica la integridad de los datos de jugadores
     */
    private boolean verifyPlayerDataIntegrity() {
        try {
            if (databaseConnected) {
                // Verificar estructura de tabla de jugadores con manejo robusto de errores
                try (Connection conn = dataSource.getConnection()) {
                    String checkQuery = "SELECT COUNT(*) FROM player_data WHERE uuid IS NOT NULL";
                    try (PreparedStatement stmt = conn.prepareStatement(checkQuery);
                         ResultSet rs = stmt.executeQuery()) {
                        
                        if (rs.next()) {
                            int validRecords = rs.getInt(1);
                            plugin.getLogger().info("Registros válidos de jugadores en BD: " + validRecords);
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error de SQL verificando datos de jugadores: " + e.getMessage(), e);
                    // Continuar con verificación JSON como fallback
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error inesperado verificando datos de jugadores en BD: " + e.getMessage(), e);
                    // Continuar con verificación JSON como fallback
                }
            }
            
            // Verificar archivo JSON de jugadores
            if (playerDataFile.exists()) {
                try (FileReader reader = new FileReader(playerDataFile)) {
                    JsonObject playerJson = JsonParser.parseReader(reader).getAsJsonObject();
                    
                    // Verificar que cada entrada de jugador tenga UUID válido
                    for (String key : playerJson.keySet()) {
                        try {
                            UUID.fromString(key);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("UUID de jugador inválido encontrado: " + key);
                            return false;
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error leyendo archivo JSON de jugadores: " + e.getMessage(), e);
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error general verificando integridad de datos de jugadores", e);
            return false;
        }
    }
    
    /**
     * Verifica la consistencia entre base de datos y archivos JSON
     */
    private boolean verifyDatabaseJsonConsistency() {
        if (!databaseConnected) {
            return true; // No hay BD para comparar
        }
        
        try {
            // Comparar datos de eventos
            WeeklyEventData dbEventData = loadEventFromDatabase();
            WeeklyEventData jsonEventData = loadEventFromJSON();
            
            if (dbEventData != null && jsonEventData != null) {
                boolean eventsConsistent = Objects.equals(dbEventData.eventType, jsonEventData.eventType) &&
                                         dbEventData.eventActive == jsonEventData.eventActive;
                
                if (!eventsConsistent) {
                    plugin.getLogger().warning("Inconsistencia detectada entre datos de eventos en BD y JSON");
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error verificando consistencia BD-JSON", e);
            return false;
        }
    }
    
    /**
     * Realiza una reparación automática de datos corruptos
     */
    public CompletableFuture<Boolean> repairCorruptedData() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                plugin.getLogger().info("Iniciando reparación automática de datos...");
                
                boolean repaired = false;
                
                // Reparar archivos JSON corruptos
                if (!verifyEventDataIntegrity()) {
                    repaired |= repairEventDataFile();
                }
                
                if (!verifyPlayerDataIntegrity()) {
                    repaired |= repairPlayerDataFile();
                }
                
                // Sincronizar BD con JSON si hay inconsistencias
                if (databaseConnected && !verifyDatabaseJsonConsistency()) {
                    repaired |= synchronizeDatabaseWithJson();
                }
                
                if (repaired) {
                    plugin.getLogger().info("Reparación de datos completada exitosamente");
                } else {
                    plugin.getLogger().info("No se encontraron datos que requieran reparación");
                }
                
                return repaired;
                
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error durante reparación de datos", e);
                return false;
            }
        });
    }
    
    private boolean repairEventDataFile() {
        try {
            // Crear estructura básica de eventos si el archivo está corrupto
            JsonObject defaultEventData = new JsonObject();
            defaultEventData.addProperty("eventActive", false);
            defaultEventData.addProperty("eventType", "NONE");
            defaultEventData.addProperty("startTime", 0L);
            defaultEventData.addProperty("endTime", 0L);
            defaultEventData.addProperty("isPaused", false);
            defaultEventData.addProperty("pauseStartTime", 0L);
            defaultEventData.addProperty("totalPausedTime", 0L);
            
            try (FileWriter writer = new FileWriter(eventDataFile)) {
                gson.toJson(defaultEventData, writer);
            }
            
            plugin.getLogger().info("Archivo de datos de eventos reparado");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error reparando archivo de eventos", e);
            return false;
        }
    }
    
    private boolean repairPlayerDataFile() {
        try {
            // Crear estructura básica de jugadores si el archivo está corrupto
            JsonObject defaultPlayerData = new JsonObject();
            
            try (FileWriter writer = new FileWriter(playerDataFile)) {
                gson.toJson(defaultPlayerData, writer);
            }
            
            plugin.getLogger().info("Archivo de datos de jugadores reparado");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error reparando archivo de jugadores", e);
            return false;
        }
    }
    
    private boolean synchronizeDatabaseWithJson() {
        try {
            // Priorizar datos de la base de datos y actualizar JSON
            WeeklyEventData dbData = loadEventFromDatabase();
            if (dbData != null) {
                saveEventToJSON(createEventFromData(dbData));
                plugin.getLogger().info("Datos de eventos sincronizados desde BD a JSON");
                return true;
            }
            return false;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error sincronizando BD con JSON", e);
            return false;
        }
    }
    
    private WeeklyEvent createEventFromData(WeeklyEventData data) {
        // Crear un evento básico para sincronización
        // Esto es una implementación simplificada
        return new WeeklyEvent(plugin, 3600) { // 1 hora por defecto
            @Override
            public boolean isActive() { return data.eventActive; }
            
            @Override
            public void start() {}
            
            @Override
            public void stop() {}
            
            @Override
            public void pause() {}
            
            @Override
            public void resume() {}
            
            @Override
            public void stopEventTasks() {}
            
            @Override
            public void startEventTasks() {}
            
            @Override
            public String getId() {
                return data.eventType;
            }
        };
    }
    
    /**
     * Clase para encapsular resultados de verificación de integridad
     */
    public static class DataIntegrityResult {
        public boolean eventDataIntegrity = false;
        public boolean playerDataIntegrity = false;
        public boolean consistencyCheck = false;
        public boolean overallIntegrity = false;
        public String errorMessage = null;
        
        public boolean isValid() {
            return overallIntegrity;
        }
        
        public String getStatusReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== Reporte de Integridad de Datos ===\n");
            report.append("Datos de Eventos: ").append(eventDataIntegrity ? "✓ OK" : "✗ ERROR").append("\n");
            report.append("Datos de Jugadores: ").append(playerDataIntegrity ? "✓ OK" : "✗ ERROR").append("\n");
            report.append("Consistencia BD-JSON: ").append(consistencyCheck ? "✓ OK" : "✗ ERROR").append("\n");
            report.append("Estado General: ").append(overallIntegrity ? "✓ VÁLIDO" : "✗ REQUIERE ATENCIÓN").append("\n");
            
            if (errorMessage != null) {
                report.append("Error: ").append(errorMessage).append("\n");
            }
            
            return report.toString();
        }
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