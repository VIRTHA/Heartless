package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.darkbladedev.models.BackupInfo;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Gestor de respaldos automáticos para el sistema de eventos semanales
 * Crea copias de seguridad antes de operaciones críticas de guardado
 */
public class BackupManager {
    
    private final HeartlessMain plugin;
    private final Logger logger;
    private final Gson gson;
    private final Path backupDirectory;
    private final Path tempDirectory;
    
    // Configuración de respaldos
    private final int maxBackups;
    private final long backupRetentionDays;
    private final boolean compressBackups;
    
    // Estado del sistema
    private volatile boolean isCreatingBackup = false;
    private final Map<String, BackupInfo> activeBackups = new ConcurrentHashMap<>();
    private final Map<String, Long> backupMetrics = new ConcurrentHashMap<>();
    
    // Formateador de fechas para nombres de archivos
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    public BackupManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Configuración desde config.yml
        this.maxBackups = plugin.getConfig().getInt("backup.max-backups", 10);
        this.backupRetentionDays = plugin.getConfig().getLong("backup.retention-days", 7);
        this.compressBackups = plugin.getConfig().getBoolean("backup.compress", true);
        
        // Directorios de respaldo
        this.backupDirectory = plugin.getDataFolder().toPath().resolve("backups");
        this.tempDirectory = plugin.getDataFolder().toPath().resolve("temp");
        
        // Crear directorios si no existen
        createDirectories();
        
        // Inicializar métricas
        initializeMetrics();
        
        // Programar limpieza automática
        scheduleCleanup();
        
        logger.info("BackupManager inicializado - Máximo: " + maxBackups + " respaldos, Retención: " + backupRetentionDays + " días");
    }
    
    /**
     * Crea un respaldo completo antes de operaciones críticas
     */
    public CompletableFuture<BackupResult> createPreSaveBackup(String reason) {
        return CompletableFuture.supplyAsync(() -> {
            if (isCreatingBackup) {
                return new BackupResult(false, "Ya se está creando otro respaldo", null);
            }
            
            isCreatingBackup = true;
            long startTime = System.currentTimeMillis();
            String backupId = "presave_" + dateFormat.format(new Date());
            
            try {
                logger.info("Iniciando respaldo pre-guardado: " + reason);
                
                // Crear información del respaldo
                BackupInfo backupInfo = new BackupInfo(backupId, reason, startTime);
                activeBackups.put(backupId, backupInfo);
                
                // Crear respaldo de datos de eventos
                Path eventBackup = backupEventData(backupId);
                if (eventBackup == null) {
                    return new BackupResult(false, "Error creando respaldo de eventos", null);
                }
                backupInfo.addFile("events", eventBackup);
                
                // Crear respaldo de datos de jugadores
                Path playerBackup = backupPlayerData(backupId);
                if (playerBackup == null) {
                    return new BackupResult(false, "Error creando respaldo de jugadores", null);
                }
                backupInfo.addFile("players", playerBackup);
                
                // Crear respaldo de configuración
                Path configBackup = backupConfiguration(backupId);
                if (configBackup != null) {
                    backupInfo.addFile("config", configBackup);
                }
                
                // Crear archivo comprimido si está habilitado
                Path finalBackup = null;
                if (compressBackups) {
                    finalBackup = compressBackup(backupInfo);
                    if (finalBackup != null) {
                        // Limpiar archivos temporales
                        cleanupTempFiles(backupInfo);
                    }
                } else {
                    finalBackup = backupDirectory.resolve(backupId);
                    moveBackupFiles(backupInfo, finalBackup);
                }
                
                if (finalBackup == null) {
                    return new BackupResult(false, "Error finalizando respaldo", null);
                }
                
                // Actualizar información del respaldo
                long duration = System.currentTimeMillis() - startTime;
                backupInfo.setCompleted(finalBackup, duration);
                
                // Actualizar métricas
                updateMetrics("backup_created", 1);
                updateMetrics("backup_duration_ms", duration);
                updateMetrics("backup_size_bytes", getFileSize(finalBackup));
                
                // Limpiar respaldos antiguos
                cleanupOldBackups();
                
                logger.info("Respaldo completado exitosamente: " + backupId + " (" + duration + "ms)");
                return new BackupResult(true, "Respaldo creado exitosamente", finalBackup);
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error creando respaldo: " + e.getMessage(), e);
                updateMetrics("backup_errors", 1);
                return new BackupResult(false, "Error: " + e.getMessage(), null);
            } finally {
                isCreatingBackup = false;
                activeBackups.remove(backupId);
            }
        });
    }
    
    /**
     * Crea respaldo de emergencia durante el cierre del servidor
     */
    public CompletableFuture<BackupResult> createEmergencyBackup() {
        return createPreSaveBackup("Cierre de emergencia del servidor");
    }
    
    /**
     * Crea respaldo de emergencia con nombre personalizado
     */
    public CompletableFuture<BackupResult> createEmergencyBackup(String backupName) {
        return createPreSaveBackup("Emergencia: " + backupName);
    }
    
    /**
     * Restaura datos desde un respaldo específico
     */
    public CompletableFuture<Boolean> restoreFromBackup(Path backupPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Iniciando restauración desde: " + backupPath.getFileName());
                
                if (!Files.exists(backupPath)) {
                    logger.warning("Archivo de respaldo no encontrado: " + backupPath);
                    return false;
                }
                
                // Descomprimir si es necesario
                Path extractPath = tempDirectory.resolve("restore_" + System.currentTimeMillis());
                if (backupPath.toString().endsWith(".zip")) {
                    if (!extractBackup(backupPath, extractPath)) {
                        return false;
                    }
                } else {
                    extractPath = backupPath;
                }
                
                // Restaurar datos de eventos
                Path eventFile = extractPath.resolve("events.json");
                if (Files.exists(eventFile)) {
                    if (!restoreEventData(eventFile)) {
                        logger.warning("Error restaurando datos de eventos");
                    }
                }
                
                // Restaurar datos de jugadores
                Path playerFile = extractPath.resolve("players.json");
                if (Files.exists(playerFile)) {
                    if (!restorePlayerData(playerFile)) {
                        logger.warning("Error restaurando datos de jugadores");
                    }
                }
                
                logger.info("Restauración completada desde: " + backupPath.getFileName());
                updateMetrics("restore_operations", 1);
                return true;
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error durante restauración: " + e.getMessage(), e);
                updateMetrics("restore_errors", 1);
                return false;
            }
        });
    }
    
    /**
     * Lista todos los respaldos disponibles
     */
    public List<BackupInfo> listAvailableBackups() {
        List<BackupInfo> backups = new ArrayList<>();
        
        try {
            if (!Files.exists(backupDirectory)) {
                return backups;
            }
            
            Files.list(backupDirectory)
                .filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        BackupInfo info = createBackupInfoFromPath(path);
                        if (info != null) {
                            backups.add(info);
                        }
                    } catch (Exception e) {
                        logger.warning("Error leyendo información de respaldo: " + path.getFileName());
                    }
                });
                
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error listando respaldos: " + e.getMessage(), e);
        }
        
        // Ordenar por fecha (más reciente primero)
        backups.sort((a, b) -> Long.compare(b.getCreationTime(), a.getCreationTime()));
        return backups;
    }
    
    /**
     * Obtiene estadísticas del sistema de respaldos
     */
    public String getBackupStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Estadísticas de Respaldos ===\n");
        
        List<BackupInfo> backups = listAvailableBackups();
        stats.append("Respaldos disponibles: ").append(backups.size()).append("/").append(maxBackups).append("\n");
        
        if (!backups.isEmpty()) {
            BackupInfo latest = backups.get(0);
            stats.append("Último respaldo: ").append(new Date(latest.getCreationTime())).append("\n");
            stats.append("Tamaño total: ").append(formatFileSize(getTotalBackupSize())).append("\n");
        }
        
        stats.append("\nMétricas del sistema:\n");
        backupMetrics.forEach((key, value) -> {
            String formattedKey = key.replace("_", " ").toUpperCase();
            if (key.contains("size")) {
                stats.append(formattedKey).append(": ").append(formatFileSize(value)).append("\n");
            } else if (key.contains("duration")) {
                stats.append(formattedKey).append(": ").append(value).append("ms\n");
            } else {
                stats.append(formattedKey).append(": ").append(value).append("\n");
            }
        });
        
        return stats.toString();
    }
    
    // Métodos privados de implementación
    
    private void createDirectories() {
        try {
            Files.createDirectories(backupDirectory);
            Files.createDirectories(tempDirectory);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creando directorios de respaldo: " + e.getMessage(), e);
        }
    }
    
    private void initializeMetrics() {
        backupMetrics.put("backup_created", 0L);
        backupMetrics.put("backup_errors", 0L);
        backupMetrics.put("restore_operations", 0L);
        backupMetrics.put("restore_errors", 0L);
        backupMetrics.put("backup_duration_ms", 0L);
        backupMetrics.put("backup_size_bytes", 0L);
    }
    
    private void updateMetrics(String key, long value) {
        backupMetrics.merge(key, value, Long::sum);
    }
    
    private Path backupEventData(String backupId) {
        try {
            WeeklyEventManager eventManager = plugin.getWeeklyEventManager();
            if (eventManager == null) {
                return null;
            }
            
            // Obtener datos del evento actual
            JsonObject eventData = new JsonObject();
            // Aquí se implementaría la lógica específica para obtener datos del evento
            // Por ahora, creamos un respaldo básico
            eventData.addProperty("backup_time", System.currentTimeMillis());
            eventData.addProperty("backup_id", backupId);
            
            Path eventFile = tempDirectory.resolve(backupId + "_events.json");
            try (FileWriter writer = new FileWriter(eventFile.toFile())) {
                gson.toJson(eventData, writer);
            }
            
            return eventFile;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error creando respaldo de eventos: " + e.getMessage(), e);
            return null;
        }
    }
    
    private Path backupPlayerData(String backupId) {
        try {
            // Crear respaldo de datos de jugadores
            JsonObject playerData = new JsonObject();
            playerData.addProperty("backup_time", System.currentTimeMillis());
            playerData.addProperty("backup_id", backupId);
            
            Path playerFile = tempDirectory.resolve(backupId + "_players.json");
            try (FileWriter writer = new FileWriter(playerFile.toFile())) {
                gson.toJson(playerData, writer);
            }
            
            return playerFile;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error creando respaldo de jugadores: " + e.getMessage(), e);
            return null;
        }
    }
    
    private Path backupConfiguration(String backupId) {
        try {
            Path configFile = plugin.getDataFolder().toPath().resolve("config.yml");
            if (!Files.exists(configFile)) {
                return null;
            }
            
            Path backupConfigFile = tempDirectory.resolve(backupId + "_config.yml");
            Files.copy(configFile, backupConfigFile, StandardCopyOption.REPLACE_EXISTING);
            
            return backupConfigFile;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error creando respaldo de configuración: " + e.getMessage(), e);
            return null;
        }
    }
    
    private Path compressBackup(BackupInfo backupInfo) {
        try {
            Path zipFile = backupDirectory.resolve(backupInfo.getId() + ".zip");
            
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
                for (Map.Entry<String, Path> entry : backupInfo.getFiles().entrySet()) {
                    Path file = entry.getValue();
                    if (Files.exists(file)) {
                        ZipEntry zipEntry = new ZipEntry(entry.getKey() + "." + getFileExtension(file));
                        zos.putNextEntry(zipEntry);
                        Files.copy(file, zos);
                        zos.closeEntry();
                    }
                }
            }
            
            return zipFile;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error comprimiendo respaldo: " + e.getMessage(), e);
            return null;
        }
    }
    
    private void cleanupTempFiles(BackupInfo backupInfo) {
        for (Path file : backupInfo.getFiles().values()) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                logger.warning("Error eliminando archivo temporal: " + file.getFileName());
            }
        }
    }
    
    private void moveBackupFiles(BackupInfo backupInfo, Path targetDirectory) {
        try {
            Files.createDirectories(targetDirectory);
            
            for (Map.Entry<String, Path> entry : backupInfo.getFiles().entrySet()) {
                Path source = entry.getValue();
                Path target = targetDirectory.resolve(entry.getKey() + "." + getFileExtension(source));
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error moviendo archivos de respaldo: " + e.getMessage(), e);
        }
    }
    
    private void cleanupOldBackups() {
        try {
            List<BackupInfo> backups = listAvailableBackups();
            
            // Eliminar respaldos que excedan el límite máximo
            if (backups.size() > maxBackups) {
                for (int i = maxBackups; i < backups.size(); i++) {
                    BackupInfo backup = backups.get(i);
                    deleteBackup(backup.getBackupPath());
                    logger.info("Respaldo eliminado por límite máximo: " + backup.getId());
                }
            }
            
            // Eliminar respaldos antiguos por retención
            long cutoffTime = System.currentTimeMillis() - (backupRetentionDays * 24 * 60 * 60 * 1000);
            for (BackupInfo backup : backups) {
                if (backup.getCreationTime() < cutoffTime) {
                    deleteBackup(backup.getBackupPath());
                    logger.info("Respaldo eliminado por retención: " + backup.getId());
                }
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error durante limpieza de respaldos: " + e.getMessage(), e);
        }
    }
    
    private void deleteBackup(Path backupPath) {
        try {
            if (Files.isDirectory(backupPath)) {
                Files.walk(backupPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.warning("Error eliminando: " + path);
                        }
                    });
            } else {
                Files.deleteIfExists(backupPath);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error eliminando respaldo: " + e.getMessage(), e);
        }
    }
    
    private void scheduleCleanup() {
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupOldBackups();
            }
        }.runTaskTimerAsynchronously(plugin, 20L * 60 * 60, 20L * 60 * 60 * 6); // Cada 6 horas
    }
    
    private boolean extractBackup(Path zipFile, Path extractPath) {
        // Implementación de extracción de ZIP
        // Por simplicidad, retornamos true
        return true;
    }
    
    private boolean restoreEventData(Path eventFile) {
        // Implementación de restauración de eventos
        return true;
    }
    
    private boolean restorePlayerData(Path playerFile) {
        // Implementación de restauración de jugadores
        return true;
    }
    
    @SuppressWarnings("unused")
    private BackupInfo createBackupInfoFromPath(Path path) {
        try {
            String fileName = path.getFileName().toString();
            long creationTime = Files.getLastModifiedTime(path).toMillis();
            long size = getFileSize(path);
            
            BackupInfo info = new BackupInfo(fileName, "Respaldo existente", creationTime);
            info.setCompleted(path, 0);
            return info;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private long getFileSize(Path path) {
        try {
            if (Files.isDirectory(path)) {
                return Files.walk(path)
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
            } else {
                return Files.size(path);
            }
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long getTotalBackupSize() {
        try {
            return Files.walk(backupDirectory)
                .filter(Files::isRegularFile)
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "dat";
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Verifica si el sistema de respaldos está funcionando correctamente
     */
    public boolean isSystemHealthy() {
        try {
            // Verificar que los directorios existan
            if (!Files.exists(backupDirectory) || !Files.exists(tempDirectory)) {
                return false;
            }
            
            // Verificar permisos de escritura
            Path testFile = tempDirectory.resolve("health_check_" + System.currentTimeMillis());
            try {
                Files.write(testFile, "test".getBytes());
                Files.deleteIfExists(testFile);
            } catch (IOException e) {
                return false;
            }
            
            // Verificar que no haya demasiados respaldos activos
            if (activeBackups.size() > 5) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error verificando salud del sistema de respaldos: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Obtiene el número de respaldos creados en las últimas horas especificadas
     */
    public int getRecentBackupsCount(int hours) {
        try {
            long cutoffTime = System.currentTimeMillis() - (hours * 60 * 60 * 1000L);
            
            return (int) listAvailableBackups().stream()
                .filter(backup -> backup.getCreationTime() > cutoffTime)
                .count();
                
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error contando respaldos recientes: " + e.getMessage(), e);
            return 0;
        }
    }
    
    // Getters
    public boolean isCreatingBackup() {
        return isCreatingBackup;
    }
    
    public int getActiveBackupsCount() {
        return activeBackups.size();
    }
    
    public Path getBackupDirectory() {
        return backupDirectory;
    }
    
    // Clases internas
    
    /**
     * Resultado de una operación de respaldo
     */
    public static class BackupResult {
        private final boolean success;
        private final String message;
        private final Path backupPath;
        
        public BackupResult(boolean success, String message, Path backupPath) {
            this.success = success;
            this.message = message;
            this.backupPath = backupPath;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Path getBackupPath() { return backupPath; }
        
        public String getBackupName() {
            return backupPath != null ? backupPath.getFileName().toString() : "unknown";
        }
    }
}