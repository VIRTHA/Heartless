package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.WeeklyEvent;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestor de migración para eventos semanales.
 * 
 * Este sistema permite:
 * - Migrar eventos existentes a la nueva estructura mejorada
 * - Mantener compatibilidad hacia atrás con datos existentes
 * - Validar y corregir configuraciones inconsistentes
 * - Realizar backups automáticos antes de migraciones
 * - Proporcionar rollback en caso de errores
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class WeeklyEventMigrationManager {
    
    private static final String MIGRATION_VERSION = "2.0";
    private static final String BACKUP_FOLDER = "event_backups";
    private static final String MIGRATION_CONFIG_FILE = "migration_config.yml";
    
    private final HeartlessMain plugin;
    private final Logger logger;
    private final File backupFolder;
    private final File migrationConfigFile;
    
    // Mapeo de eventos antiguos a nuevos
    private final Map<String, Class<? extends WeeklyEvent>> eventMigrationMap;
    
    public WeeklyEventMigrationManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.backupFolder = new File(plugin.getDataFolder(), BACKUP_FOLDER);
        this.migrationConfigFile = new File(plugin.getDataFolder(), MIGRATION_CONFIG_FILE);
        
        // Inicializar mapeo de migraciones
        this.eventMigrationMap = new HashMap<>();
        initializeMigrationMap();
        
        // Crear carpeta de backups si no existe
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
    }
    
    /**
     * Inicializa el mapeo de eventos antiguos a nuevos.
     */
    private void initializeMigrationMap() {
        // Mapear eventos existentes a sus versiones mejoradas
        eventMigrationMap.put("UndeadWeek", UndeadWeek.class);
        eventMigrationMap.put("UndeadWeekImproved", UndeadWeek.class);
        eventMigrationMap.put("UndeadWeekRefactored", UndeadWeek.class);
        
        // Aquí se pueden agregar más mapeos cuando se creen versiones mejoradas de otros eventos
        // eventMigrationMap.put("BloodAndIronWeek", BloodAndIronWeekImproved.class);
        // eventMigrationMap.put("ExplosiveWeek", ExplosiveWeekImproved.class);
        
        logger.info("Mapeo de migración inicializado con " + eventMigrationMap.size() + " eventos.");
    }
    
    /**
     * Verifica si es necesario realizar migraciones.
     */
    public boolean needsMigration() {
        try {
            // Verificar versión de migración actual
            FileConfiguration migrationConfig = loadMigrationConfig();
            String currentVersion = migrationConfig.getString("migration.version", "1.0");
            
            if (!MIGRATION_VERSION.equals(currentVersion)) {
                logger.info("Migración necesaria. Versión actual: " + currentVersion + ", Versión objetivo: " + MIGRATION_VERSION);
                return true;
            }
            
            // Verificar si existen eventos antiguos activos
            return hasLegacyEventsActive();
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al verificar necesidad de migración", e);
            return false;
        }
    }
    
    /**
     * Verifica si hay eventos antiguos activos.
     */
    private boolean hasLegacyEventsActive() {
        // Verificar en el StorageManager si hay eventos antiguos guardados
        try {
            // Aquí se implementaría la lógica para verificar eventos antiguos
            // Por ahora, asumimos que no hay eventos antiguos activos
            return false;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al verificar eventos antiguos", e);
            return false;
        }
    }
    
    /**
     * Recarga la configuración del sistema de migración.
     * 
     * Este método:
     * - Actualiza el mapa de migraciones de eventos
     * - Verifica nuevos eventos legacy que requieran migración
     * - Recarga la configuración de migración
     * - Mantiene el estado actual del sistema
     */
    public void reload() {
        logger.info("[WeeklyEventMigrationManager] Iniciando recarga del sistema de migración...");
        
        try {
            // Reinicializar el mapa de migraciones
            initializeMigrationMap();
            
            // Recargar configuración del plugin para obtener nuevos valores
            plugin.reloadConfig();
            
            // Verificar si hay nuevos eventos que requieran migración
            boolean needsNewMigration = needsMigration();
            
            if (needsNewMigration) {
                logger.info("[WeeklyEventMigrationManager] Se detectaron eventos que requieren migración después de la recarga.");
            } else {
                logger.info("[WeeklyEventMigrationManager] No se requieren migraciones adicionales.");
            }
            
            logger.info("[WeeklyEventMigrationManager] Sistema de migración recargado exitosamente. " +
                       "Eventos mapeados: " + eventMigrationMap.size());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[WeeklyEventMigrationManager] Error durante la recarga del sistema", e);
            throw new RuntimeException("Error al recargar WeeklyEventMigrationManager", e);
        }
    }
    
    /**
     * Ejecuta el proceso completo de migración.
     */
    public boolean performMigration() {
        logger.info("Iniciando proceso de migración de eventos semanales...");
        
        try {
            // Paso 1: Crear backup de datos actuales
            if (!createBackup()) {
                logger.severe("Error al crear backup. Migración cancelada.");
                return false;
            }
            
            // Paso 2: Migrar configuraciones
            if (!migrateConfigurations()) {
                logger.severe("Error al migrar configuraciones. Intentando rollback...");
                rollbackMigration();
                return false;
            }
            
            // Paso 3: Migrar datos de eventos
            if (!migrateEventData()) {
                logger.severe("Error al migrar datos de eventos. Intentando rollback...");
                rollbackMigration();
                return false;
            }
            
            // Paso 4: Validar migración
            if (!validateMigration()) {
                logger.severe("Validación de migración fallida. Intentando rollback...");
                rollbackMigration();
                return false;
            }
            
            // Paso 5: Actualizar versión de migración
            updateMigrationVersion();
            
            logger.info("Migración de eventos semanales completada exitosamente.");
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error crítico durante la migración", e);
            rollbackMigration();
            return false;
        }
    }
    
    /**
     * Crea un backup completo de los datos actuales.
     */
    private boolean createBackup() {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            File backupDir = new File(backupFolder, "backup_" + timestamp);
            
            if (!backupDir.mkdirs()) {
                logger.warning("No se pudo crear directorio de backup: " + backupDir.getPath());
                return false;
            }
            
            // Backup de configuración principal
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (configFile.exists()) {
                copyFile(configFile, new File(backupDir, "config.yml"));
            }
            
            // Backup de datos de eventos (si existen)
            File eventsDataDir = new File(plugin.getDataFolder(), "events_data");
            if (eventsDataDir.exists()) {
                copyDirectory(eventsDataDir, new File(backupDir, "events_data"));
            }
            
            // Crear archivo de información del backup
            createBackupInfo(backupDir, timestamp);
            
            logger.info("Backup creado exitosamente en: " + backupDir.getPath());
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al crear backup", e);
            return false;
        }
    }
    
    /**
     * Migra las configuraciones de eventos a la nueva estructura.
     */
    private boolean migrateConfigurations() {
        try {
            FileConfiguration config = plugin.getConfig();
            boolean configChanged = false;
            
            // Migrar configuraciones de UndeadWeek
            if (migrateUndeadWeekConfig(config)) {
                configChanged = true;
            }
            
            // Aquí se pueden agregar migraciones para otros eventos
            
            // Guardar configuración si hubo cambios
            if (configChanged) {
                plugin.saveConfig();
                logger.info("Configuraciones migradas exitosamente.");
            }
            
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al migrar configuraciones", e);
            return false;
        }
    }
    
    /**
     * Migra la configuración específica de UndeadWeek.
     */
    private boolean migrateUndeadWeekConfig(FileConfiguration config) {
        boolean changed = false;
        
        // Verificar si existe configuración antigua
        if (config.contains("undead-week")) {
            ConfigurationSection oldSection = config.getConfigurationSection("undead-week");
            
            // Crear nueva sección de configuración
            ConfigurationSection newSection = config.createSection("events.undead-week");
            
            // Migrar valores existentes con valores por defecto mejorados
            newSection.set("zombie-spawn-interval", oldSection.getInt("zombie-spawn-interval", 300));
            newSection.set("effect-interval", oldSection.getInt("effect-interval", 600));
            newSection.set("zombie-spawn-radius", oldSection.getInt("zombie-spawn-radius", 50));
            newSection.set("max-zombies-per-player", oldSection.getInt("max-zombies-per-player", 3));
            newSection.set("zombie-despawn-time", oldSection.getInt("zombie-despawn-time", 6000));
            
            // Migrar configuración de efectos
            ConfigurationSection effectsSection = newSection.createSection("effects");
            effectsSection.set("night-vision", oldSection.getBoolean("effects.night-vision", true));
            effectsSection.set("slowness", oldSection.getBoolean("effects.slowness", true));
            effectsSection.set("weakness", oldSection.getBoolean("effects.weakness", false));
            
            // Remover sección antigua
            config.set("undead-week", null);
            
            changed = true;
            logger.info("Configuración de UndeadWeek migrada a la nueva estructura.");
        } else {
            // Crear configuración por defecto si no existe
            if (!config.contains("events.undead-week")) {
                createDefaultUndeadWeekConfig(config);
                changed = true;
            }
        }
        
        return changed;
    }
    
    /**
     * Crea la configuración por defecto para UndeadWeek.
     */
    private void createDefaultUndeadWeekConfig(FileConfiguration config) {
        ConfigurationSection section = config.createSection("events.undead-week");
        
        section.set("zombie-spawn-interval", 300);
        section.set("effect-interval", 600);
        section.set("zombie-spawn-radius", 50);
        section.set("max-zombies-per-player", 3);
        section.set("zombie-despawn-time", 6000);
        
        ConfigurationSection effectsSection = section.createSection("effects");
        effectsSection.set("night-vision", true);
        effectsSection.set("slowness", true);
        effectsSection.set("weakness", false);
        
        logger.info("Configuración por defecto creada para UndeadWeek.");
    }
    
    /**
     * Migra los datos de eventos existentes.
     */
    private boolean migrateEventData() {
        try {
            // Aquí se implementaría la migración de datos específicos
            // Por ahora, no hay datos específicos que migrar
            
            logger.info("Datos de eventos migrados exitosamente.");
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al migrar datos de eventos", e);
            return false;
        }
    }
    
    /**
     * Valida que la migración se haya completado correctamente.
     */
    private boolean validateMigration() {
        try {
            FileConfiguration config = plugin.getConfig();
            
            // Validar que las nuevas configuraciones existan
            if (!config.contains("events.undead-week")) {
                logger.severe("Validación fallida: configuración de UndeadWeek no encontrada.");
                return false;
            }
            
            // Validar estructura de configuración
            ConfigurationSection undeadSection = config.getConfigurationSection("events.undead-week");
            if (undeadSection == null) {
                logger.severe("Validación fallida: sección de UndeadWeek es nula.");
                return false;
            }
            
            // Validar campos requeridos
            String[] requiredFields = {
                "zombie-spawn-interval", "effect-interval", "zombie-spawn-radius",
                "max-zombies-per-player", "zombie-despawn-time"
            };
            
            for (String field : requiredFields) {
                if (!undeadSection.contains(field)) {
                    logger.severe("Validación fallida: campo requerido no encontrado: " + field);
                    return false;
                }
            }
            
            // Validar sección de efectos
            if (!undeadSection.contains("effects")) {
                logger.severe("Validación fallida: sección de efectos no encontrada.");
                return false;
            }
            
            logger.info("Validación de migración completada exitosamente.");
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error durante la validación de migración", e);
            return false;
        }
    }
    
    /**
     * Actualiza la versión de migración en el archivo de configuración.
     */
    private void updateMigrationVersion() {
        try {
            FileConfiguration migrationConfig = loadMigrationConfig();
            
            migrationConfig.set("migration.version", MIGRATION_VERSION);
            migrationConfig.set("migration.timestamp", System.currentTimeMillis());
            migrationConfig.set("migration.plugin-version", plugin.getPluginMeta().getVersion());
            
            migrationConfig.save(migrationConfigFile);
            
            logger.info("Versión de migración actualizada a: " + MIGRATION_VERSION);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al actualizar versión de migración", e);
        }
    }
    
    /**
     * Realiza rollback de la migración en caso de error.
     */
    private boolean rollbackMigration() {
        try {
            logger.warning("Iniciando rollback de migración...");
            
            // Buscar el backup más reciente
            File latestBackup = findLatestBackup();
            if (latestBackup == null) {
                logger.severe("No se encontró backup para rollback.");
                return false;
            }
            
            // Restaurar configuración
            File backupConfig = new File(latestBackup, "config.yml");
            if (backupConfig.exists()) {
                File currentConfig = new File(plugin.getDataFolder(), "config.yml");
                copyFile(backupConfig, currentConfig);
            }
            
            // Restaurar datos de eventos
            File backupEventsData = new File(latestBackup, "events_data");
            if (backupEventsData.exists()) {
                File currentEventsData = new File(plugin.getDataFolder(), "events_data");
                if (currentEventsData.exists()) {
                    deleteDirectory(currentEventsData);
                }
                copyDirectory(backupEventsData, currentEventsData);
            }
            
            // Recargar configuración
            plugin.reloadConfig();
            
            logger.info("Rollback completado exitosamente.");
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error crítico durante rollback", e);
            return false;
        }
    }
    
    /**
     * Encuentra el backup más reciente.
     */
    private File findLatestBackup() {
        File[] backups = backupFolder.listFiles(file -> 
            file.isDirectory() && file.getName().startsWith("backup_"));
        
        if (backups == null || backups.length == 0) {
            return null;
        }
        
        // Ordenar por timestamp (más reciente primero)
        Arrays.sort(backups, (a, b) -> {
            try {
                long timestampA = Long.parseLong(a.getName().substring(7));
                long timestampB = Long.parseLong(b.getName().substring(7));
                return Long.compare(timestampB, timestampA);
            } catch (NumberFormatException e) {
                return 0;
            }
        });
        
        return backups[0];
    }
    
    /**
     * Carga o crea el archivo de configuración de migración.
     */
    private FileConfiguration loadMigrationConfig() {
        if (!migrationConfigFile.exists()) {
            try {
                migrationConfigFile.createNewFile();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error al crear archivo de configuración de migración", e);
            }
        }
        
        return YamlConfiguration.loadConfiguration(migrationConfigFile);
    }
    
    /**
     * Crea archivo de información del backup.
     */
    private void createBackupInfo(File backupDir, String timestamp) throws IOException {
        File infoFile = new File(backupDir, "backup_info.yml");
        FileConfiguration info = new YamlConfiguration();
        
        info.set("backup.timestamp", timestamp);
        info.set("backup.date", new Date().toString());
        info.set("backup.plugin-version", plugin.getPluginMeta().getVersion());
        info.set("backup.server-version", Bukkit.getVersion());
        info.set("backup.migration-version", MIGRATION_VERSION);
        
        info.save(infoFile);
    }
    
    // === MÉTODOS DE UTILIDAD PARA ARCHIVOS ===
    
    /**
     * Copia un archivo.
     */
    private void copyFile(File source, File destination) throws IOException {
        if (!destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }
        
        java.nio.file.Files.copy(source.toPath(), destination.toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Copia un directorio recursivamente.
     */
    private void copyDirectory(File source, File destination) throws IOException {
        if (!destination.exists()) {
            destination.mkdirs();
        }
        
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File destFile = new File(destination, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, destFile);
                } else {
                    copyFile(file, destFile);
                }
            }
        }
    }
    
    /**
     * Elimina un directorio recursivamente.
     */
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
    
    // === MÉTODOS PÚBLICOS DE UTILIDAD ===
    
    /**
     * Obtiene información sobre backups disponibles.
     */
    public List<String> getAvailableBackups() {
        List<String> backups = new ArrayList<>();
        
        File[] backupDirs = backupFolder.listFiles(file -> 
            file.isDirectory() && file.getName().startsWith("backup_"));
        
        if (backupDirs != null) {
            for (File backupDir : backupDirs) {
                backups.add(backupDir.getName());
            }
        }
        
        return backups;
    }
    
    /**
     * Obtiene la versión actual de migración.
     */
    public String getCurrentMigrationVersion() {
        FileConfiguration migrationConfig = loadMigrationConfig();
        return migrationConfig.getString("migration.version", "1.0");
    }
    
    /**
     * Verifica si un evento específico necesita migración.
     */
    public boolean eventNeedsMigration(String eventName) {
        return eventMigrationMap.containsKey(eventName);
    }
    
    /**
     * Obtiene la clase del evento para un evento específico.
     */
    public Class<? extends WeeklyEvent> getImprovedEventClass(String eventName) {
        return eventMigrationMap.get(eventName);
    }
}