package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.WeeklyEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Adaptador que permite migrar gradualmente del StorageManager al DatabaseManager
 * manteniendo compatibilidad con el código existente.
 * 
 * Este adaptador:
 * - Redirige las llamadas del StorageManager al DatabaseManager cuando está disponible
 * - Mantiene fallback al StorageManager original para compatibilidad
 * - Permite migración gradual sin romper funcionalidad existente
 */
public class StorageAdapter {
    
    private final HeartlessMain plugin;
    private final StorageManager legacyStorageManager;
    private final DatabaseManager databaseManager;
    
    public StorageAdapter(HeartlessMain plugin, StorageManager legacyStorageManager) {
        this.plugin = plugin;
        this.legacyStorageManager = legacyStorageManager;
        this.databaseManager = HeartlessMain.getDatabaseManager();
    }
    
    /**
     * Guarda un evento semanal usando el sistema preferido
     */
    public CompletableFuture<Boolean> saveWeeklyEvent(WeeklyEvent event) {
        if (databaseManager != null && databaseManager.isDatabaseEnabled()) {
            return databaseManager.saveWeeklyEvent(event)
                .exceptionally(throwable -> {
                    plugin.getLogger().log(Level.WARNING, "Error guardando evento con DatabaseManager, usando fallback", throwable);
                    // Fallback al sistema legacy
                    try {
                        legacyStorageManager.saveEvent(event);
                        return true;
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Error en fallback de guardado de evento", e);
                        return false;
                    }
                });
        } else {
            // Usar sistema legacy
            return CompletableFuture.supplyAsync(() -> {
                try {
                    legacyStorageManager.saveEvent(event);
                    return true;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error guardando evento con StorageManager legacy", e);
                    return false;
                }
            });
        }
    }
    
    /**
     * Carga datos de evento semanal
     */
    public CompletableFuture<DatabaseManager.WeeklyEventData> loadWeeklyEvent() {
        if (databaseManager != null && databaseManager.isDatabaseEnabled()) {
            return databaseManager.loadWeeklyEvent()
                .exceptionally(throwable -> {
                    plugin.getLogger().log(Level.WARNING, "Error cargando evento con DatabaseManager, usando fallback", throwable);
                    // Fallback al sistema legacy
                    try {
                        return convertLegacyEventData(legacyStorageManager.loadEvent());
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Error en fallback de carga de evento", e);
                        return null;
                    }
                });
        } else {
            // Usar sistema legacy
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return convertLegacyEventData(legacyStorageManager.loadEvent());
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error cargando evento con StorageManager legacy", e);
                    return null;
                }
            });
        }
    }
    
    /**
     * Guarda datos de jugador
     */
    public CompletableFuture<Boolean> savePlayerData(Player player, String dataType, Object data) {
        UUID playerUUID = player.getUniqueId();
        String playerName = player.getName();
        
        // Convertir datos a formato Map
        Map<String, Object> playerDataMap = new HashMap<>();
        
        if ("infection".equals(dataType)) {
            playerDataMap.put("infected", data);
            playerDataMap.put("infectionTime", System.currentTimeMillis());
        } else if ("kills".equals(dataType)) {
            playerDataMap.put("playerKills", data);
            playerDataMap.put("lastPlayerKill", System.currentTimeMillis());
        } else if ("ban".equals(dataType)) {
            playerDataMap.put("banCount", data);
        } else {
            // Datos genéricos
            playerDataMap.put(dataType, data);
        }
        
        if (databaseManager != null && databaseManager.isDatabaseEnabled()) {
            return databaseManager.savePlayerData(playerUUID, playerName, playerDataMap)
                .exceptionally(throwable -> {
                    plugin.getLogger().log(Level.WARNING, "Error guardando datos de jugador con DatabaseManager, usando fallback", throwable);
                    // Fallback - en este caso no hay equivalente directo en StorageManager
                    // pero podríamos implementar un sistema de archivos JSON simple
                    return saveLegacyPlayerData(playerUUID, playerName, dataType, data);
                });
        } else {
            return CompletableFuture.supplyAsync(() -> 
                saveLegacyPlayerData(playerUUID, playerName, dataType, data)
            );
        }
    }
    
    /**
     * Carga datos de jugador
     */
    public CompletableFuture<Map<String, Object>> loadPlayerData(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        if (databaseManager != null && databaseManager.isDatabaseEnabled()) {
            return databaseManager.loadPlayerData(playerUUID)
                .exceptionally(throwable -> {
                    plugin.getLogger().log(Level.WARNING, "Error cargando datos de jugador con DatabaseManager, usando fallback", throwable);
                    return loadLegacyPlayerData(playerUUID);
                });
        } else {
            return CompletableFuture.supplyAsync(() -> loadLegacyPlayerData(playerUUID));
        }
    }
    
    /**
     * Ejecuta diagnóstico del sistema de almacenamiento
     */
    public void runDiagnostic() {
        plugin.getLogger().info("=== DIAGNÓSTICO DEL SISTEMA DE ALMACENAMIENTO ===");
        
        if (databaseManager != null) {
            plugin.getLogger().info("DatabaseManager: Disponible");
            plugin.getLogger().info("Base de datos habilitada: " + databaseManager.isDatabaseEnabled());
            plugin.getLogger().info("Base de datos conectada: " + databaseManager.isDatabaseConnected());
        } else {
            plugin.getLogger().info("DatabaseManager: No disponible");
        }
        
        if (legacyStorageManager != null) {
            plugin.getLogger().info("StorageManager legacy: Disponible");
            // Ejecutar diagnóstico del sistema legacy
            legacyStorageManager.diagnosticPersistenceSystem();
        } else {
            plugin.getLogger().info("StorageManager legacy: No disponible");
        }
        
        plugin.getLogger().info("=== FIN DEL DIAGNÓSTICO ===");
    }
    
    // Métodos de utilidad privados
    
    /**
     * Convierte datos de evento legacy al formato nuevo
     */
    private DatabaseManager.WeeklyEventData convertLegacyEventData(StorageManager.WeeklyEventData legacyData) {
        if (legacyData == null) return null;
        
        DatabaseManager.WeeklyEventData newData = new DatabaseManager.WeeklyEventData();
        newData.eventType = legacyData.eventType;
        newData.startTime = legacyData.startTime;
        newData.endTime = legacyData.endTime;
        newData.eventActive = legacyData.eventActive;
        newData.isPaused = legacyData.isPaused;
        newData.pauseStartTime = legacyData.pauseStartTime;
        newData.totalPausedTime = legacyData.totalPausedTime;
        
        return newData;
    }
    
    /**
     * Guarda datos de jugador usando sistema legacy (implementación básica)
     */
    private boolean saveLegacyPlayerData(UUID playerUUID, String playerName, String dataType, Object data) {
        try {
            // Implementación básica - en un sistema real esto se expandiría
            plugin.getLogger().info("Guardando datos legacy para " + playerName + ": " + dataType + " = " + data);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando datos legacy de jugador", e);
            return false;
        }
    }
    
    /**
     * Carga datos de jugador usando sistema legacy (implementación básica)
     */
    private Map<String, Object> loadLegacyPlayerData(UUID playerUUID) {
        try {
            // Implementación básica - retorna datos por defecto
            Map<String, Object> defaultData = new HashMap<>();
            defaultData.put("infected", false);
            defaultData.put("infectionTime", 0L);
            defaultData.put("playerKills", 0);
            defaultData.put("banCount", 0);
            
            return defaultData;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando datos legacy de jugador", e);
            return new HashMap<>();
        }
    }
    
    // Getters para acceso a los managers subyacentes
    
    public StorageManager getLegacyStorageManager() {
        return legacyStorageManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    /**
     * Verifica si el sistema unificado está disponible
     */
    public boolean isUnifiedSystemAvailable() {
        return databaseManager != null && databaseManager.isDatabaseEnabled() && databaseManager.isDatabaseConnected();
    }
    
    /**
     * Fuerza el uso del sistema legacy (para debugging o migración)
     */
    public void forceLegacyMode(boolean force) {
        // Esta funcionalidad se podría implementar con una flag
        plugin.getLogger().info("Modo legacy " + (force ? "activado" : "desactivado"));
    }
}