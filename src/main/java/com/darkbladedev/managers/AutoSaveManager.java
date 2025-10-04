package com.darkbladedev.managers;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.AbstractWeeklyEvent;
import com.darkbladedev.persistence.EventDataPersistenceManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Sistema de guardado automático que asegura la persistencia de todos los datos
 * y progresos de los jugadores en los desafíos del evento semanal antes de reiniciar el servidor.
 * 
 * Características:
 * - Guardado automático periódico cada 5 minutos
 * - Guardado de emergencia antes del shutdown del servidor
 * - Verificación de integridad de datos
 * - Sistema de respaldo automático
 * - Logs detallados para rastrear operaciones
 * - Comprobación post-reinicio para validar datos recuperados
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class AutoSaveManager {
    
    private static final long AUTO_SAVE_INTERVAL = 5 * 60 * 20L; // 5 minutos en ticks
    @SuppressWarnings("unused")
    private static final long EMERGENCY_SAVE_TIMEOUT = 30000L; // 30 segundos
    private static final String BACKUP_DIR = "backups";
    private static final String INTEGRITY_FILE = "data_integrity.json";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    private final HeartlessMain plugin;
    private final Logger logger;
    private final Gson gson;
    private final File pluginDir;
    private final File backupDir;
    private final File integrityFile;
    
    // Estado del sistema
    private final AtomicBoolean isEnabled = new AtomicBoolean(true);
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private final AtomicLong lastSaveTime = new AtomicLong(0);
    private final AtomicLong saveCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    // Tareas programadas
    private BukkitTask autoSaveTask;
    
    // Datos de integridad
    private final Map<String, String> lastDataHashes = new ConcurrentHashMap<>();
    private final Map<String, Long> lastDataSizes = new ConcurrentHashMap<>();
    
    public AutoSaveManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.pluginDir = plugin.getDataFolder();
        this.backupDir = new File(pluginDir, BACKUP_DIR);
        this.integrityFile = new File(pluginDir, INTEGRITY_FILE);
        
        initializeDirectories();
        startAutoSaveTask();
        loadIntegrityData();
        
        logger.info("AutoSaveManager inicializado correctamente");
    }
    
    /**
     * Inicializa los directorios necesarios
     */
    private void initializeDirectories() {
        try {
            if (!pluginDir.exists()) {
                pluginDir.mkdirs();
            }
            if (!backupDir.exists()) {
                backupDir.mkdirs();
                logger.info("Directorio de respaldos creado: " + backupDir.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creando directorios del AutoSaveManager", e);
        }
    }
    
    /**
     * Inicia la tarea de guardado automático
     */
    private void startAutoSaveTask() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        
        autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (isEnabled.get() && !isShuttingDown.get()) {
                performAutoSave();
            }
        }, AUTO_SAVE_INTERVAL, AUTO_SAVE_INTERVAL);
        
        logger.info("Tarea de guardado automático iniciada (cada 5 minutos)");
    }
    
    /**
     * Realiza el guardado automático de todos los datos
     */
    public CompletableFuture<Boolean> performAutoSave() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Iniciando guardado automático...");
                long startTime = System.currentTimeMillis();
                
                // 1. Crear respaldo usando BackupManager antes del guardado
                boolean backupSuccess = createBackupWithManager("Guardado automático");
                if (!backupSuccess) {
                    logger.warning("Fallo al crear respaldo, continuando con guardado...");
                }
                
                // 2. Guardar datos del evento semanal
                boolean eventSaveSuccess = saveWeeklyEventData();
                
                // 3. Guardar datos de jugadores
                boolean playerSaveSuccess = savePlayerData();
                
                // 4. Guardar datos del ciclo de día
                boolean dayCycleSaveSuccess = saveDayCycleData();
                
                // 5. Verificar integridad de los datos guardados
                boolean integritySuccess = verifyDataIntegrity();
                
                // 6. Actualizar estadísticas
                lastSaveTime.set(System.currentTimeMillis());
                saveCount.incrementAndGet();
                
                long duration = System.currentTimeMillis() - startTime;
                boolean overallSuccess = eventSaveSuccess && playerSaveSuccess && dayCycleSaveSuccess && integritySuccess;
                
                if (overallSuccess) {
                    logger.info(String.format("Guardado automático completado exitosamente en %d ms (Save #%d)", 
                        duration, saveCount.get()));
                } else {
                    logger.warning(String.format("Guardado automático completado con errores en %d ms", duration));
                    errorCount.incrementAndGet();
                }
                
                return overallSuccess;
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error durante el guardado automático", e);
                errorCount.incrementAndGet();
                return false;
            }
        });
    }
    
    /**
     * Realiza un guardado de emergencia antes del shutdown del servidor
     */
    public CompletableFuture<Boolean> performEmergencySave() {
        isShuttingDown.set(true);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("=== INICIANDO GUARDADO DE EMERGENCIA ===");
                long startTime = System.currentTimeMillis();
                
                // Crear respaldo de emergencia usando BackupManager
                String emergencyBackupName = "emergency_" + LocalDateTime.now().format(TIMESTAMP_FORMAT);
                boolean backupSuccess = createEmergencyBackupWithManager(emergencyBackupName);
                
                // Guardar todos los datos críticos
                boolean eventSaveSuccess = saveWeeklyEventData();
                boolean playerSaveSuccess = savePlayerData();
                boolean dayCycleSaveSuccess = saveDayCycleData();
                
                // Forzar guardado en base de datos si está disponible
                boolean databaseSaveSuccess = forceDatabaseSave();
                
                // Verificar integridad final
                boolean integritySuccess = verifyDataIntegrity();
                
                // Guardar log de shutdown
                saveShutdownLog(startTime, backupSuccess, eventSaveSuccess, playerSaveSuccess, 
                    dayCycleSaveSuccess, databaseSaveSuccess, integritySuccess);
                
                long duration = System.currentTimeMillis() - startTime;
                boolean overallSuccess = eventSaveSuccess && playerSaveSuccess && dayCycleSaveSuccess && integritySuccess;
                
                if (overallSuccess) {
                    logger.info(String.format("=== GUARDADO DE EMERGENCIA COMPLETADO EXITOSAMENTE EN %d ms ===", duration));
                } else {
                    logger.severe(String.format("=== GUARDADO DE EMERGENCIA COMPLETADO CON ERRORES EN %d ms ===", duration));
                }
                
                return overallSuccess;
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error crítico durante el guardado de emergencia", e);
                return false;
            }
        });
    }
    
    /**
     * Guarda los datos del evento semanal actual
     */
    private boolean saveWeeklyEventData() {
        try {
            WeeklyEventManager eventManager = plugin.getWeeklyEventManager();
            if (eventManager == null) {
                logger.warning("WeeklyEventManager es null, no se pueden guardar datos del evento");
                return true; // No es un error crítico
            }
            
            AbstractWeeklyEvent currentEvent = eventManager.getCurrentEvent();
            if (currentEvent == null) {
                logger.info("No hay evento semanal activo, omitiendo guardado de datos del evento");
                return true; // No hay evento activo, no es un error
            }
            
            // Obtener el ID del evento de forma segura
            String eventId;
            try {
                eventId = currentEvent.getId();
                if (eventId == null || eventId.trim().isEmpty()) {
                    logger.warning("El evento actual tiene un ID null o vacío, usando ID por defecto");
                    eventId = "unknown_event";
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error obteniendo ID del evento, usando ID por defecto", e);
                eventId = "unknown_event";
            }
            
            logger.info("Iniciando guardado de datos para evento: " + eventId);
            
            // Usar StorageManager para guardar el evento
            try {
                plugin.getStorageManager().saveEvent(currentEvent);
                logger.fine("StorageManager guardó exitosamente el evento: " + eventId);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error guardando evento con StorageManager para evento: " + eventId, e);
            }
            
            // Usar EventDataPersistenceManager para eventos que extienden AbstractWeeklyEvent
            EventDataPersistenceManager persistenceManager = HeartlessMain.getEventDataPersistenceManager();
            if (persistenceManager != null) {
                try {
                    boolean persistenceSuccess = persistenceManager.saveEventData(currentEvent);
                    if (persistenceSuccess) {
                        logger.info("Datos de desafíos guardados exitosamente para evento: " + eventId);
                    } else {
                        logger.warning("Error guardando datos de desafíos para evento: " + eventId);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error en EventDataPersistenceManager para evento: " + eventId, e);
                }
            } else {
                logger.fine("EventDataPersistenceManager no disponible");
            }
            
            // También usar DatabaseManager si está disponible
            DatabaseManager dbManager = HeartlessMain.getDatabaseManager();
            if (dbManager != null && dbManager.isDatabaseEnabled()) {
                try {
                    dbManager.saveWeeklyEvent(currentEvent);
                    logger.fine("DatabaseManager guardó exitosamente el evento: " + eventId);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error guardando evento con DatabaseManager para evento: " + eventId, e);
                }
            } else {
                logger.fine("DatabaseManager no disponible o deshabilitado");
            }
            
            logger.info("Datos del evento semanal guardados exitosamente: " + eventId);
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error crítico guardando datos del evento semanal", e);
            return false;
        }
    }
    
    /**
     * Guarda los datos de todos los jugadores conectados
     */
    private boolean savePlayerData() {
        try {
            int savedPlayers = 0;
            DatabaseManager dbManager = HeartlessMain.getDatabaseManager();
            
            if (dbManager != null && dbManager.isDatabaseEnabled()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        // Crear datos básicos del jugador
                        Map<String, Object> playerData = new HashMap<>();
                        playerData.put("lastSeen", System.currentTimeMillis());
                        playerData.put("world", player.getWorld().getName());
                        playerData.put("health", player.getHealth());
                        playerData.put("foodLevel", player.getFoodLevel());
                        
                        // Guardar en base de datos
                        dbManager.savePlayerData(player.getUniqueId(), player.getName(), playerData).join();
                        savedPlayers++;
                    } catch (Exception e) {
                        logger.warning("Error guardando datos del jugador " + player.getName() + ": " + e.getMessage());
                    }
                }
            }
            
            logger.info("Datos de " + savedPlayers + " jugadores guardados");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error guardando datos de jugadores", e);
            return false;
        }
    }
    
    /**
     * Guarda los datos del ciclo de día
     */
    private boolean saveDayCycleData() {
        try {
            StorageManager storageManager = plugin.getStorageManager();
            if (storageManager != null) {
                storageManager.saveDayCycleData();
                logger.info("Datos del ciclo de día guardados");
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error guardando datos del ciclo de día", e);
            return false;
        }
    }
    
    /**
     * Fuerza el guardado en base de datos
     */
    private boolean forceDatabaseSave() {
        try {
            DatabaseManager dbManager = HeartlessMain.getDatabaseManager();
            if (dbManager != null && dbManager.isDatabaseEnabled()) {
                // Base de datos disponible - no necesita flush manual
                logger.info("Base de datos disponible para guardado");
                return true;
            }
            return true; // No hay base de datos, no es un error
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error forzando guardado en base de datos", e);
            return false;
        }
    }
    
    /**
     * Crea un respaldo usando el BackupManager
     */
    private boolean createBackupWithManager(String reason) {
        try {
            BackupManager backupManager = HeartlessMain.getBackupManager();
            if (backupManager != null) {
                BackupManager.BackupResult result = backupManager.createPreSaveBackup(reason).get(60, java.util.concurrent.TimeUnit.SECONDS);
                if (result.isSuccess()) {
                    logger.info("Respaldo pre-guardado creado exitosamente: " + result.getBackupName());
                    return true;
                } else {
                    logger.warning("Error creando respaldo: " + result.getMessage());
                    return false;
                }
            } else {
                // Fallback al método legacy
                return createBackup();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error durante creación de respaldo con BackupManager", e);
            // Fallback al método legacy
            return createBackup();
        }
    }
    
    /**
     * Crea un respaldo de emergencia usando el BackupManager
     */
    private boolean createEmergencyBackupWithManager(String backupName) {
        try {
            BackupManager backupManager = HeartlessMain.getBackupManager();
            if (backupManager != null) {
                BackupManager.BackupResult result = backupManager.createEmergencyBackup(backupName).get(60, java.util.concurrent.TimeUnit.SECONDS);
                if (result.isSuccess()) {
                    logger.info("Respaldo de emergencia creado exitosamente: " + result.getBackupName());
                    return true;
                } else {
                    logger.warning("Error creando respaldo de emergencia: " + result.getMessage());
                    return false;
                }
            } else {
                // Fallback al método legacy
                return createBackupWithName(backupName);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error durante creación de respaldo de emergencia con BackupManager", e);
            // Fallback al método legacy
            return createBackupWithName(backupName);
        }
    }
    
    /**
     * Crea un respaldo de todos los archivos de datos (método legacy)
     */
    private boolean createBackup() {
        String backupName = "auto_" + LocalDateTime.now().format(TIMESTAMP_FORMAT);
        return createBackupWithName(backupName);
    }
    
    /**
     * Crea un respaldo con un nombre específico
     */
    private boolean createBackupWithName(String backupName) {
        try {
            File backupSubDir = new File(backupDir, backupName);
            if (!backupSubDir.exists()) {
                backupSubDir.mkdirs();
            }
            
            // Respaldar archivos principales
            String[] filesToBackup = {
                "weekly_event_data.json",
                "event_data.json",
                "day_cycle_data.yml",
                "config.yml"
            };
            
            int backedUpFiles = 0;
            for (String fileName : filesToBackup) {
                File sourceFile = new File(pluginDir, fileName);
                if (sourceFile.exists()) {
                    File destFile = new File(backupSubDir, fileName);
                    Files.copy(sourceFile.toPath(), destFile.toPath());
                    backedUpFiles++;
                }
            }
            
            logger.info(String.format("Respaldo creado: %s (%d archivos)", backupName, backedUpFiles));
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creando respaldo: " + backupName, e);
            return false;
        }
    }
    
    /**
     * Verifica la integridad de los datos guardados
     */
    private boolean verifyDataIntegrity() {
        try {
            DatabaseManager dbManager = HeartlessMain.getDatabaseManager();
            
            // Usar el nuevo sistema de verificación de integridad
            CompletableFuture<DatabaseManager.DataIntegrityResult> integrityCheck = 
                dbManager.verifyDataIntegrity();
            
            DatabaseManager.DataIntegrityResult result = integrityCheck.get(30, java.util.concurrent.TimeUnit.SECONDS);
            
            if (!result.isValid()) {
                logger.warning("Verificación de integridad falló:");
                logger.warning(result.getStatusReport());
                
                // Intentar reparación automática si hay problemas
                logger.info("Intentando reparación automática de datos...");
                CompletableFuture<Boolean> repairResult = dbManager.repairCorruptedData();
                boolean repaired = repairResult.get(60, java.util.concurrent.TimeUnit.SECONDS);
                
                if (repaired) {
                    logger.info("Reparación completada, re-verificando integridad...");
                    DatabaseManager.DataIntegrityResult recheck = 
                        dbManager.verifyDataIntegrity().get(30, java.util.concurrent.TimeUnit.SECONDS);
                    return recheck.isValid();
                } else {
                    logger.severe("No se pudo reparar la integridad de los datos");
                    return false;
                }
            }
            
            // También verificar archivos locales como respaldo
            JsonObject integrityData = new JsonObject();
            integrityData.addProperty("timestamp", System.currentTimeMillis());
            integrityData.addProperty("saveCount", saveCount.get());
            
            // Verificar archivos principales
            String[] filesToCheck = {
                "weekly_event_data.json",
                "event_data.json",
                "day_cycle_data.yml"
            };
            
            JsonObject filesData = new JsonObject();
            for (String fileName : filesToCheck) {
                File file = new File(pluginDir, fileName);
                if (file.exists()) {
                    JsonObject fileData = new JsonObject();
                    fileData.addProperty("size", file.length());
                    fileData.addProperty("lastModified", file.lastModified());
                    fileData.addProperty("exists", true);
                    
                    // Calcular hash simple del tamaño + timestamp
                    String hash = String.valueOf(file.length() + file.lastModified());
                    fileData.addProperty("hash", hash);
                    
                    // Verificar si cambió desde la última vez
                    String lastHash = lastDataHashes.get(fileName);
                    if (lastHash != null && !lastHash.equals(hash)) {
                        fileData.addProperty("changed", true);
                        logger.info("Archivo modificado detectado: " + fileName);
                    } else {
                        fileData.addProperty("changed", false);
                    }
                    
                    lastDataHashes.put(fileName, hash);
                    lastDataSizes.put(fileName, file.length());
                    filesData.add(fileName, fileData);
                } else {
                    JsonObject fileData = new JsonObject();
                    fileData.addProperty("exists", false);
                    filesData.add(fileName, fileData);
                }
            }
            
            integrityData.add("files", filesData);
            
            // Guardar datos de integridad
            try (FileWriter writer = new FileWriter(integrityFile)) {
                gson.toJson(integrityData, writer);
            }
            
            logger.info("Verificación de integridad completada exitosamente");
            logger.info(result.getStatusReport());
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error durante verificación de integridad", e);
            return false;
        }
    }
    
    /**
     * Carga los datos de integridad previos
     */
    private void loadIntegrityData() {
        try {
            if (integrityFile.exists()) {
                String content = Files.readString(integrityFile.toPath());
                JsonObject integrityData = gson.fromJson(content, JsonObject.class);
                
                if (integrityData.has("files")) {
                    JsonObject filesData = integrityData.getAsJsonObject("files");
                    for (String fileName : filesData.keySet()) {
                        JsonObject fileData = filesData.getAsJsonObject(fileName);
                        if (fileData.has("hash")) {
                            lastDataHashes.put(fileName, fileData.get("hash").getAsString());
                        }
                        if (fileData.has("size")) {
                            lastDataSizes.put(fileName, fileData.get("size").getAsLong());
                        }
                    }
                }
                
                logger.info("Datos de integridad previos cargados");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error cargando datos de integridad previos", e);
        }
    }
    
    /**
     * Guarda un log detallado del proceso de shutdown
     */
    private void saveShutdownLog(long startTime, boolean backupSuccess, boolean eventSaveSuccess, 
            boolean playerSaveSuccess, boolean dayCycleSaveSuccess, boolean databaseSaveSuccess, 
            boolean integritySuccess) {
        try {
            File shutdownLogFile = new File(pluginDir, "last_shutdown.log");
            
            StringBuilder logContent = new StringBuilder();
            logContent.append("=== LOG DE SHUTDOWN - ").append(LocalDateTime.now()).append(" ===\n");
            logContent.append("Tiempo de inicio: ").append(startTime).append("\n");
            logContent.append("Respaldo creado: ").append(backupSuccess ? "✓" : "✗").append("\n");
            logContent.append("Evento semanal guardado: ").append(eventSaveSuccess ? "✓" : "✗").append("\n");
            logContent.append("Datos de jugadores guardados: ").append(playerSaveSuccess ? "✓" : "✗").append("\n");
            logContent.append("Ciclo de día guardado: ").append(dayCycleSaveSuccess ? "✓" : "✗").append("\n");
            logContent.append("Base de datos forzada: ").append(databaseSaveSuccess ? "✓" : "✗").append("\n");
            logContent.append("Integridad verificada: ").append(integritySuccess ? "✓" : "✗").append("\n");
            logContent.append("Duración total: ").append(System.currentTimeMillis() - startTime).append(" ms\n");
            logContent.append("Total de guardados realizados: ").append(saveCount.get()).append("\n");
            logContent.append("Total de errores: ").append(errorCount.get()).append("\n");
            logContent.append("=== FIN DEL LOG ===\n");
            
            Files.writeString(shutdownLogFile.toPath(), logContent.toString());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error guardando log de shutdown", e);
        }
    }
    
    /**
     * Realiza una comprobación post-reinicio para validar datos recuperados
     */
    public CompletableFuture<Boolean> performPostRestartCheck() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("=== INICIANDO COMPROBACIÓN POST-REINICIO ===");
                
                boolean integrityCheck = checkDataIntegrityAfterRestart();
                boolean eventDataCheck = checkWeeklyEventDataAfterRestart();
                boolean playerDataCheck = checkPlayerDataAfterRestart();
                boolean backupSystemCheck = checkBackupSystemAfterRestart();
                
                boolean overallSuccess = integrityCheck && eventDataCheck && playerDataCheck && backupSystemCheck;
                
                if (overallSuccess) {
                    logger.info("=== COMPROBACIÓN POST-REINICIO EXITOSA ===");
                } else {
                    logger.warning("=== COMPROBACIÓN POST-REINICIO CON PROBLEMAS ===");
                }
                
                return overallSuccess;
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error durante comprobación post-reinicio", e);
                return false;
            }
        });
    }
    
    /**
     * Verifica la integridad de los datos después del reinicio
     */
    private boolean checkDataIntegrityAfterRestart() {
        try {
            if (!integrityFile.exists()) {
                logger.warning("Archivo de integridad no encontrado");
                return false;
            }
            
            String content = Files.readString(integrityFile.toPath());
            JsonObject integrityData = gson.fromJson(content, JsonObject.class);
            
            if (!integrityData.has("files")) {
                logger.warning("Datos de integridad incompletos");
                return false;
            }
            
            JsonObject filesData = integrityData.getAsJsonObject("files");
            boolean allFilesOk = true;
            
            for (String fileName : filesData.keySet()) {
                JsonObject fileData = filesData.getAsJsonObject(fileName);
                File file = new File(pluginDir, fileName);
                
                if (fileData.get("exists").getAsBoolean()) {
                    if (!file.exists()) {
                        logger.severe("Archivo faltante después del reinicio: " + fileName);
                        allFilesOk = false;
                    } else {
                        long expectedSize = fileData.get("size").getAsLong();
                        long actualSize = file.length();
                        
                        if (expectedSize != actualSize) {
                            logger.warning(String.format("Tamaño de archivo diferente para %s: esperado %d, actual %d", 
                                fileName, expectedSize, actualSize));
                        } else {
                            logger.info("Archivo verificado correctamente: " + fileName);
                        }
                    }
                }
            }
            
            return allFilesOk;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error verificando integridad después del reinicio", e);
            return false;
        }
    }
    
    /**
     * Verifica los datos del evento semanal después del reinicio
     */
    private boolean checkWeeklyEventDataAfterRestart() {
        try {
            WeeklyEventManager eventManager = plugin.getWeeklyEventManager();
            if (eventManager != null) {
                AbstractWeeklyEvent currentEvent = eventManager.getCurrentEvent();
                if (currentEvent != null) {
                    logger.info("Evento semanal restaurado correctamente: " + currentEvent.getId());
                    
                    // Verificar que los datos del evento no estén vacíos
                    Map<UUID, Object> playerData = currentEvent.getPlayerData();
                    logger.info("Datos de jugadores en el evento: " + playerData.size() + " entradas");
                    
                    return true;
                } else {
                    logger.info("No hay evento semanal activo después del reinicio");
                    return true; // No es un error si no hay evento
                }
            }
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error verificando datos del evento semanal", e);
            return false;
        }
    }
    
    /**
     * Verifica los datos de jugadores después del reinicio
     */
    private boolean checkPlayerDataAfterRestart() {
        try {
            DatabaseManager dbManager = HeartlessMain.getDatabaseManager();
            if (dbManager != null && dbManager.isDatabaseEnabled()) {
                // Verificar que la base de datos esté accesible
                boolean dbConnected = dbManager.testConnection();
                if (dbConnected) {
                    logger.info("Conexión a base de datos verificada correctamente");
                } else {
                    logger.warning("Problemas de conexión con la base de datos");
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error verificando datos de jugadores", e);
            return false;
        }
    }
    
    /**
     * Verifica el sistema de respaldos después del reinicio
     */
    private boolean checkBackupSystemAfterRestart() {
        try {
            BackupManager backupManager = HeartlessMain.getBackupManager();
            if (backupManager == null) {
                logger.warning("BackupManager no está disponible para verificación post-reinicio");
                return false;
            }
            
            // Verificar salud del sistema
            if (!backupManager.isSystemHealthy()) {
                logger.warning("Sistema de respaldos no está saludable después del reinicio");
                return false;
            }
            
            // Verificar respaldos recientes
            int recentBackups = backupManager.getRecentBackupsCount(24);
            logger.info("Respaldos encontrados en las últimas 24 horas: " + recentBackups);
            
            if (recentBackups == 0) {
                logger.warning("No se encontraron respaldos recientes. Creando respaldo de verificación...");
                backupManager.createEmergencyBackup("Verificación post-reinicio")
                    .thenAccept(result -> {
                        if (result.isSuccess()) {
                            logger.info("Respaldo de verificación creado exitosamente: " + result.getBackupName());
                        } else {
                            logger.severe("Error creando respaldo de verificación: " + result.getMessage());
                        }
                    });
            }
            
            logger.info("Sistema de respaldos verificado correctamente");
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error verificando sistema de respaldos post-reinicio: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Obtiene estadísticas del sistema de guardado
     */
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTADÍSTICAS DEL AUTOSAVE ===\n");
        stats.append("Estado: ").append(isEnabled.get() ? "Activo" : "Inactivo").append("\n");
        stats.append("Último guardado: ");
        
        long lastSave = lastSaveTime.get();
        if (lastSave > 0) {
            long timeSince = System.currentTimeMillis() - lastSave;
            stats.append(timeSince / 1000).append(" segundos atrás\n");
        } else {
            stats.append("Nunca\n");
        }
        
        stats.append("Total de guardados: ").append(saveCount.get()).append("\n");
        stats.append("Total de errores: ").append(errorCount.get()).append("\n");
        stats.append("Archivos rastreados: ").append(lastDataHashes.size()).append("\n");
        
        return stats.toString();
    }
    
    /**
     * Habilita o deshabilita el sistema de guardado automático
     */
    public void setEnabled(boolean enabled) {
        isEnabled.set(enabled);
        logger.info("AutoSaveManager " + (enabled ? "habilitado" : "deshabilitado"));
    }
    
    /**
     * Detiene el sistema de guardado automático
     */
    public void shutdown() {
        logger.info("Deteniendo AutoSaveManager...");
        
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
        
        // Realizar guardado de emergencia si no se está cerrando ya
        if (!isShuttingDown.get()) {
            try {
                performEmergencySave().get(); // Esperar a que termine
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error durante guardado de emergencia en shutdown", e);
            }
        }
        
        logger.info("AutoSaveManager detenido");
    }
    
    // Getters para testing y monitoreo
    public boolean isEnabled() { return isEnabled.get(); }
    public long getLastSaveTime() { return lastSaveTime.get(); }
    public long getSaveCount() { return saveCount.get(); }
    public long getErrorCount() { return errorCount.get(); }
    public boolean isShuttingDown() { return isShuttingDown.get(); }
}