package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

/**
 * Gestor responsable de controlar la densidad de mobs y optimizar el rendimiento
 * mediante limpieza periódica y límites de spawn personalizados.
 */
public class MobSpawnManager {

    private final HeartlessMain plugin;
    private boolean enabled;
    private long checkInterval;
    
    // Límites
    private int hostilePerPlayer;
    private int minSpawnRadius;
    private int maxSpawnRadius;
    private int minDistanceBetweenMobs;
    
    // Limpieza
    private boolean cleanupEnabled;
    private boolean removeInactive;
    private int inactiveTimeSeconds;
    private int cleanupRadius;
    private boolean protectNamedMobs;
    
    private BukkitTask cleanupTask;
    
    public MobSpawnManager(HeartlessMain plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("mob-control");
        
        if (config == null) {
            // Valores por defecto si no existe la configuración
            this.enabled = true;
            this.checkInterval = 600L;
            this.hostilePerPlayer = 25;
            this.minSpawnRadius = 24;
            this.maxSpawnRadius = 48;
            this.minDistanceBetweenMobs = 5;
            this.cleanupEnabled = true;
            this.removeInactive = true;
            this.inactiveTimeSeconds = 45;
            this.cleanupRadius = 64;
            this.protectNamedMobs = true;
            plugin.getLogger().warning("Sección mob-control no encontrada en config.yml. Usando valores por defecto.");
        } else {
            this.enabled = config.getBoolean("enabled", true);
            this.checkInterval = config.getLong("check-interval", 600L);
            
            this.hostilePerPlayer = config.getInt("spawn-limits.hostile-per-player", 25);
            this.minSpawnRadius = config.getInt("spawn-limits.min-spawn-radius", 24);
            this.maxSpawnRadius = config.getInt("spawn-limits.max-spawn-radius", 48);
            this.minDistanceBetweenMobs = config.getInt("spawn-limits.min-distance-between-mobs", 5);
            
            this.cleanupEnabled = config.getBoolean("cleanup.enabled", true);
            this.removeInactive = config.getBoolean("cleanup.remove-inactive", true);
            this.inactiveTimeSeconds = config.getInt("cleanup.inactive-time", 45);
            this.cleanupRadius = config.getInt("cleanup.cleanup-radius", 64);
            this.protectNamedMobs = config.getBoolean("cleanup.protect-named-mobs", true);
        }
        
        // Reiniciar tarea si estaba corriendo
        startCleanupTask();
    }
    
    public void onConfigReload() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        loadConfig();
    }
    
    public void disable() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
    }
    
    private void startCleanupTask() {
        if (!enabled || !cleanupEnabled) return;
        
        if (cleanupTask != null) cleanupTask.cancel();
        
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, this::performCleanup, checkInterval, checkInterval);
        plugin.getLogger().info("[MobSpawnManager] Tarea de limpieza de mobs iniciada (Intervalo: " + checkInterval + " ticks)");
    }
    
    /**
     * Verifica si se puede spawnear un mob en la ubicación dada.
     * @param location Ubicación del spawn
     * @return true si se permite el spawn, false si se debe cancelar
     */
    public boolean canSpawn(Location location) {
        if (!enabled) return true;
        
        // Verificar radio mínimo/máximo desde jugadores
        Player nearestPlayer = getNearestPlayer(location);
        if (nearestPlayer == null) return false; // No spawnear si no hay jugadores cerca
        
        double distance = location.distance(nearestPlayer.getLocation());
        if (distance < minSpawnRadius || distance > maxSpawnRadius) {
            return false;
        }
        
        // Verificar densidad local (mobs muy cerca unos de otros)
        if (minDistanceBetweenMobs > 0) {
            Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, minDistanceBetweenMobs, minDistanceBetweenMobs, minDistanceBetweenMobs);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof Monster) {
                    return false; // Demasiado cerca de otro mob
                }
            }
        }
        
        // Verificar límite por jugador
        if (hostilePerPlayer > 0) {
            int nearbyMobs = 0;
            for (Entity entity : nearestPlayer.getNearbyEntities(maxSpawnRadius, maxSpawnRadius, maxSpawnRadius)) {
                if (entity instanceof Monster) {
                    nearbyMobs++;
                }
            }
            if (nearbyMobs >= hostilePerPlayer) {
                return false; // Límite por jugador alcanzado
            }
        }
        
        return true;
    }
    
    private void performCleanup() {
        if (!removeInactive) return;
        
        int removedCount = 0;
        
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                // Solo limpiar monstruos
                if (!(entity instanceof Monster)) continue;
                
                Monster monster = (Monster) entity;
                
                // Proteger mobs con nombre (etiquetados o jefes custom)
                if (protectNamedMobs && (monster.customName() != null || monster.isCustomNameVisible())) {
                    continue;
                }
                
                // Verificar inactividad (sin objetivo y lejos de jugadores)
                if (monster.getTarget() == null) {
                    Player nearest = getNearestPlayer(monster.getLocation());
                    
                    // Si no hay jugadores en el mundo o el más cercano está muy lejos
                    if (nearest == null || monster.getLocation().distance(nearest.getLocation()) > cleanupRadius) {
                        // Verificar tiempo de vida si es posible (usando ticks lived)
                        if (monster.getTicksLived() > inactiveTimeSeconds * 20) {
                            monster.remove();
                            removedCount++;
                        }
                    }
                }
            }
        }
        
        if (removedCount > 0 && plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("[MobSpawnManager] Limpieza completada. Mobs eliminados: " + removedCount);
        }
    }
    
    private Player getNearestPlayer(Location location) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(location);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = player;
            }
        }
        
        return nearest;
    }
    
    // Getters para configuración
    public int getMinSpawnRadius() { return minSpawnRadius; }
    public int getMaxSpawnRadius() { return maxSpawnRadius; }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!enabled) return;
        
        // Solo controlar spawns naturales y de spawners
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL ||
            event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            
            // Solo controlar monstruos
            if (event.getEntity() instanceof Monster) {
                if (!canSpawn(event.getLocation())) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
