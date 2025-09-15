package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.EventType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validador que se ejecuta después del reinicio del servidor
 * para verificar la integridad y recuperación correcta de los datos
 */
public class PostRestartValidator {
    
    private final HeartlessMain plugin;
    private final Logger logger;
    private final File validationFile;
    private final File reportFile;
    
    // Configuración de validación
    private static final int VALIDATION_DELAY_SECONDS = 30; // Esperar 30 segundos después del inicio
    private static final int MAX_VALIDATION_ATTEMPTS = 3;
    private static final int RETRY_DELAY_SECONDS = 10;
    
    public PostRestartValidator(HeartlessMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        
        File dataDir = new File(plugin.getDataFolder(), "validation");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        this.validationFile = new File(dataDir, "pre_restart_state.json");
        this.reportFile = new File(dataDir, "post_restart_report.json");
    }
    
    /**
     * Guarda el estado actual antes del reinicio para comparación posterior
     */
    public void savePreRestartState() {
        try {
            JsonObject preRestartState = new JsonObject();
            
            // Timestamp del guardado
            preRestartState.addProperty("timestamp", System.currentTimeMillis());
            preRestartState.addProperty("server_version", Bukkit.getVersion());
            preRestartState.addProperty("plugin_version", plugin.getPluginMeta().getVersion());
            
            // Estado de los eventos semanales
            WeeklyEventManager eventManager = HeartlessMain.getWeeklyEventManager_();
            if (eventManager != null) {
                JsonObject eventState = new JsonObject();
                eventState.addProperty("has_active_event", eventManager.isEventActive());
                
                if (eventManager.isEventActive()) {
                    eventState.addProperty("event_type", eventManager.getCurrentEventType().getEventName());
                    eventState.addProperty("event_start_time", eventManager.getEventStartTime());
                    eventState.addProperty("is_paused", eventManager.isPaused());
                }
                
                preRestartState.add("weekly_event_state", eventState);
            }
            
            // Estado de la base de datos
            DatabaseManager dbManager = HeartlessMain.getDatabaseManager();
            if (dbManager != null) {
                JsonObject dbState = new JsonObject();
                dbState.addProperty("database_connected", dbManager.isDatabaseConnected());
                
                // Contar registros importantes
                try {
                    CompletableFuture<DatabaseManager.DataIntegrityResult> integrityCheck = 
                        dbManager.verifyDataIntegrity();
                    DatabaseManager.DataIntegrityResult result = 
                        integrityCheck.get(30, TimeUnit.SECONDS);
                    
                    dbState.addProperty("data_integrity_valid", result.isValid());
                    dbState.addProperty("event_data_ok", result.eventDataIntegrity);
                    dbState.addProperty("player_data_ok", result.playerDataIntegrity);
                    dbState.addProperty("consistency_ok", result.consistencyCheck);
                    
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error obteniendo estado de integridad pre-reinicio", e);
                    dbState.addProperty("integrity_check_failed", true);
                }
                
                preRestartState.add("database_state", dbState);
            }
            
            // Información del sistema de guardado automático
            AutoSaveManager autoSaveManager = HeartlessMain.getAutoSaveManager();
            if (autoSaveManager != null) {
                JsonObject autoSaveState = new JsonObject();
                autoSaveState.addProperty("auto_save_enabled", autoSaveManager.isEnabled());
                autoSaveState.addProperty("last_save_time", autoSaveManager.getLastSaveTime());
                autoSaveState.addProperty("total_saves", autoSaveManager.getSaveCount());
                
                preRestartState.add("auto_save_state", autoSaveState);
            }
            
            // Guardar estado
            try (FileWriter writer = new FileWriter(validationFile)) {
                plugin.getGson().toJson(preRestartState, writer);
            }
            
            logger.info("Estado pre-reinicio guardado para validación posterior");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error guardando estado pre-reinicio", e);
        }
    }
    
    /**
     * Inicia la validación post-reinicio
     */
    public void startPostRestartValidation() {
        // Programar validación después de un retraso para permitir inicialización completa
        new BukkitRunnable() {
            @Override
            public void run() {
                performValidationWithRetries();
            }
        }.runTaskLaterAsynchronously(plugin, VALIDATION_DELAY_SECONDS * 20L);
        
        logger.info("Validación post-reinicio programada para ejecutarse en " + 
                   VALIDATION_DELAY_SECONDS + " segundos");
    }
    
    /**
     * Realiza la validación con reintentos en caso de fallo
     */
    private void performValidationWithRetries() {
        for (int attempt = 1; attempt <= MAX_VALIDATION_ATTEMPTS; attempt++) {
            logger.info("Iniciando validación post-reinicio (intento " + attempt + "/" + MAX_VALIDATION_ATTEMPTS + ")");
            
            ValidationResult result = performValidation();
            
            if (result.isSuccessful()) {
                logger.info("✓ Validación post-reinicio completada exitosamente");
                saveValidationReport(result, attempt);
                return;
            } else {
                logger.warning("✗ Validación post-reinicio falló en intento " + attempt);
                logger.warning("Errores encontrados: " + result.getErrorSummary());
                
                if (attempt < MAX_VALIDATION_ATTEMPTS) {
                    logger.info("Reintentando en " + RETRY_DELAY_SECONDS + " segundos...");
                    try {
                        Thread.sleep(RETRY_DELAY_SECONDS * 1000L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    logger.severe("✗ Validación post-reinicio falló después de " + MAX_VALIDATION_ATTEMPTS + " intentos");
                    saveValidationReport(result, attempt);
                    
                    // Notificar a administradores si están en línea
                    notifyAdministrators(result);
                }
            }
        }
    }
    
    /**
     * Realiza la validación comparando el estado actual con el pre-reinicio
     */
    private ValidationResult performValidation() {
        ValidationResult result = new ValidationResult();
        
        try {
            // Verificar si existe el archivo de estado pre-reinicio
            if (!validationFile.exists()) {
                result.addError("No se encontró archivo de estado pre-reinicio");
                return result;
            }
            
            // Cargar estado pre-reinicio
            JsonObject preRestartState;
            try (FileReader reader = new FileReader(validationFile)) {
                preRestartState = JsonParser.parseReader(reader).getAsJsonObject();
            }
            
            // Validar integridad de datos actual
            result.addCheck("Verificando integridad de datos actual...");
            DatabaseManager dbManager = HeartlessMain.getDatabaseManager();
            if (dbManager != null) {
                try {
                    CompletableFuture<DatabaseManager.DataIntegrityResult> integrityCheck = 
                        dbManager.verifyDataIntegrity();
                    DatabaseManager.DataIntegrityResult integrityResult = 
                        integrityCheck.get(60, TimeUnit.SECONDS);
                    
                    if (integrityResult.isValid()) {
                        result.addSuccess("✓ Integridad de datos actual: VÁLIDA");
                    } else {
                        result.addError("✗ Integridad de datos actual: INVÁLIDA");
                        result.addError(integrityResult.getStatusReport());
                    }
                    
                } catch (Exception e) {
                    result.addError("Error verificando integridad actual: " + e.getMessage());
                }
            }
            
            // Comparar estado de eventos semanales
            result.addCheck("Comparando estado de eventos semanales...");
            validateWeeklyEventState(preRestartState, result);
            
            // Validar estado de base de datos
            result.addCheck("Validando estado de base de datos...");
            validateDatabaseState(preRestartState, result);
            
            // Validar sistema de guardado automático
            result.addCheck("Validando sistema de guardado automático...");
            validateAutoSaveState(preRestartState, result);
            
            // Verificar que no hay pérdida de datos críticos
            result.addCheck("Verificando ausencia de pérdida de datos...");
            validateDataConsistency(preRestartState, result);
            
        } catch (Exception e) {
            result.addError("Error durante validación: " + e.getMessage());
            logger.log(Level.SEVERE, "Error durante validación post-reinicio", e);
        }
        
        return result;
    }
    
    private void validateWeeklyEventState(JsonObject preRestartState, ValidationResult result) {
        try {
            if (!preRestartState.has("weekly_event_state")) {
                result.addWarning("No hay estado de eventos semanales para comparar");
                return;
            }
            
            JsonObject preEventState = preRestartState.getAsJsonObject("weekly_event_state");
            WeeklyEventManager eventManager = plugin.getWeeklyEventManager();
            
            if (eventManager == null) {
                result.addError("WeeklyEventManager no está disponible");
                return;
            }
            
            boolean hadActiveEvent = preEventState.get("has_active_event").getAsBoolean();
            boolean hasActiveEvent = eventManager.isEventActive();
            
            if (hadActiveEvent && !hasActiveEvent) {
                result.addError("Se perdió el evento activo después del reinicio");
            } else if (hadActiveEvent && hasActiveEvent) {
                // Verificar que el tipo de evento se mantuvo
                String preEventType = preEventState.get("event_type").getAsString();
                EventType eventType = eventManager.getCurrentEventType();
                String currentEventType = eventType != null ? eventType.getEventName() : null;
                
                if (!preEventType.equals(currentEventType)) {
                    result.addError("Tipo de evento cambió: " + preEventType + " -> " + currentEventType);
                } else {
                    result.addSuccess("✓ Estado de evento semanal preservado correctamente");
                }
            } else {
                result.addSuccess("✓ Estado de evento semanal consistente (sin evento activo)");
            }
            
        } catch (Exception e) {
            result.addError("Error validando estado de eventos: " + e.getMessage());
        }
    }
    
    private void validateDatabaseState(JsonObject preRestartState, ValidationResult result) {
        try {
            if (!preRestartState.has("database_state")) {
                result.addWarning("No hay estado de base de datos para comparar");
                return;
            }
            
            JsonObject preDbState = preRestartState.getAsJsonObject("database_state");
            DatabaseManager dbManager = HeartlessMain.getDatabaseManager();
            
            if (dbManager == null) {
                result.addError("DatabaseManager no está disponible");
                return;
            }
            
            boolean wasConnected = preDbState.get("database_connected").getAsBoolean();
            boolean isConnected = dbManager.isDatabaseConnected();
            
            if (wasConnected && !isConnected) {
                result.addError("Se perdió la conexión a la base de datos");
            } else if (isConnected) {
                result.addSuccess("✓ Conexión a base de datos restaurada");
                
                // Verificar integridad si estaba válida antes
                if (preDbState.has("data_integrity_valid") && 
                    preDbState.get("data_integrity_valid").getAsBoolean()) {
                    
                    try {
                        CompletableFuture<DatabaseManager.DataIntegrityResult> integrityCheck = 
                            dbManager.verifyDataIntegrity();
                        DatabaseManager.DataIntegrityResult integrityResult = 
                            integrityCheck.get(30, TimeUnit.SECONDS);
                        
                        if (integrityResult.isValid()) {
                            result.addSuccess("✓ Integridad de datos mantenida después del reinicio");
                        } else {
                            result.addError("✗ Integridad de datos comprometida después del reinicio");
                        }
                        
                    } catch (Exception e) {
                        result.addError("Error verificando integridad post-reinicio: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            result.addError("Error validando estado de base de datos: " + e.getMessage());
        }
    }
    
    private void validateAutoSaveState(JsonObject preRestartState, ValidationResult result) {
        try {
            AutoSaveManager autoSaveManager = HeartlessMain.getAutoSaveManager();
            
            if (autoSaveManager == null) {
                result.addError("AutoSaveManager no está disponible");
                return;
            }
            
            if (autoSaveManager.isEnabled()) {
                result.addSuccess("✓ Sistema de guardado automático activo");
            } else {
                result.addWarning("Sistema de guardado automático deshabilitado");
            }
            
        } catch (Exception e) {
            result.addError("Error validando sistema de guardado automático: " + e.getMessage());
        }
    }
    
    private void validateDataConsistency(JsonObject preRestartState, ValidationResult result) {
        try {
            // Esta validación se enfoca en verificar que no hay pérdida de datos críticos
            // comparando checksums o conteos de registros importantes
            
            DatabaseManager dbManager = HeartlessMain.getDatabaseManager();
            if (dbManager != null && dbManager.isDatabaseConnected()) {
                // Verificar que las tablas principales existen y tienen datos
                CompletableFuture<DatabaseManager.DataIntegrityResult> integrityCheck = 
                    dbManager.verifyDataIntegrity();
                DatabaseManager.DataIntegrityResult integrityResult = 
                    integrityCheck.get(30, TimeUnit.SECONDS);
                
                if (integrityResult.eventDataIntegrity && integrityResult.playerDataIntegrity) {
                    result.addSuccess("✓ Consistencia de datos verificada");
                } else {
                    result.addError("✗ Inconsistencias detectadas en los datos");
                }
            }
            
        } catch (Exception e) {
            result.addError("Error validando consistencia de datos: " + e.getMessage());
        }
    }
    
    private void saveValidationReport(ValidationResult result, int attempts) {
        try {
            JsonObject report = new JsonObject();
            report.addProperty("validation_timestamp", System.currentTimeMillis());
            report.addProperty("validation_date", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            report.addProperty("attempts_required", attempts);
            report.addProperty("successful", result.isSuccessful());
            report.addProperty("server_version", Bukkit.getVersion());
            report.addProperty("plugin_version", plugin.getPluginMeta().getVersion());
            
            report.add("checks", plugin.getGson().toJsonTree(result.getChecks()));
            report.add("successes", plugin.getGson().toJsonTree(result.getSuccesses()));
            report.add("warnings", plugin.getGson().toJsonTree(result.getWarnings()));
            report.add("errors", plugin.getGson().toJsonTree(result.getErrors()));
            
            try (FileWriter writer = new FileWriter(reportFile)) {
                plugin.getGson().toJson(report, writer);
            }
            
            logger.info("Reporte de validación guardado en: " + reportFile.getAbsolutePath());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error guardando reporte de validación", e);
        }
    }
    
    private void notifyAdministrators(ValidationResult result) {
        // Notificar a administradores en línea sobre problemas de validación
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission("heartless.admin.notifications"))
                .forEach(admin -> {
                    admin.sendMessage("§c[Heartless] ¡ATENCIÓN! Validación post-reinicio falló");
                    admin.sendMessage("§e" + result.getErrorSummary());
                    admin.sendMessage("§7Usa /heartless diagnostic para más detalles");
                });
        });
    }
    
    /**
     * Limpia archivos de validación antiguos
     */
    public void cleanup() {
        try {
            if (validationFile.exists()) {
                validationFile.delete();
            }
            
            // Mantener solo los últimos 5 reportes
            File validationDir = reportFile.getParentFile();
            File[] reports = validationDir.listFiles((dir, name) -> 
                name.startsWith("post_restart_report") && name.endsWith(".json"));
            
            if (reports != null && reports.length > 5) {
                java.util.Arrays.sort(reports, (a, b) -> 
                    Long.compare(b.lastModified(), a.lastModified()));
                
                for (int i = 5; i < reports.length; i++) {
                    reports[i].delete();
                }
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error durante limpieza de archivos de validación", e);
        }
    }
    
    /**
     * Obtiene el último reporte de validación
     */
    public JsonObject getLastValidationReport() {
        try {
            if (reportFile.exists()) {
                try (FileReader reader = new FileReader(reportFile)) {
                    return JsonParser.parseReader(reader).getAsJsonObject();
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error leyendo último reporte de validación", e);
        }
        return null;
    }
    
    /**
     * Clase para encapsular resultados de validación
     */
    public static class ValidationResult {
        private final java.util.List<String> checks = new java.util.ArrayList<>();
        private final java.util.List<String> successes = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        
        public void addCheck(String check) { checks.add(check); }
        public void addSuccess(String success) { successes.add(success); }
        public void addWarning(String warning) { warnings.add(warning); }
        public void addError(String error) { errors.add(error); }
        
        public boolean isSuccessful() { return errors.isEmpty(); }
        
        public java.util.List<String> getChecks() { return new java.util.ArrayList<>(checks); }
        public java.util.List<String> getSuccesses() { return new java.util.ArrayList<>(successes); }
        public java.util.List<String> getWarnings() { return new java.util.ArrayList<>(warnings); }
        public java.util.List<String> getErrors() { return new java.util.ArrayList<>(errors); }
        
        public String getErrorSummary() {
            if (errors.isEmpty()) {
                return "Sin errores";
            }
            return errors.size() + " error(es): " + String.join(", ", errors);
        }
        
        public String getFullReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== Reporte de Validación Post-Reinicio ===\n");
            
            if (!successes.isEmpty()) {
                report.append("\n✓ ÉXITOS (" + successes.size() + "):\n");
                successes.forEach(s -> report.append("  ").append(s).append("\n"));
            }
            
            if (!warnings.isEmpty()) {
                report.append("\n⚠ ADVERTENCIAS (" + warnings.size() + "):\n");
                warnings.forEach(w -> report.append("  ").append(w).append("\n"));
            }
            
            if (!errors.isEmpty()) {
                report.append("\n✗ ERRORES (" + errors.size() + "):\n");
                errors.forEach(e -> report.append("  ").append(e).append("\n"));
            }
            
            report.append("\nEstado General: ").append(isSuccessful() ? "✓ EXITOSO" : "✗ FALLÓ");
            
            return report.toString();
        }
    }
}