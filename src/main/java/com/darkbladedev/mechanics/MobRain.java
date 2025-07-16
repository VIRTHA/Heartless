package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobRain {
    
    private final Plugin plugin;
    private final int entityCount;
    private final Random random = new Random();
    private final List<EntityType> possibleEntities;
    
    /**
     * Creates a new MobRain event with custom entity count
     * @param plugin The plugin instance
     * @param entityCount Number of entities to spawn
     */
    public MobRain(Plugin plugin, int entityCount) {
        this.plugin = plugin;
        this.entityCount = entityCount;
        this.possibleEntities = initEntityTypes();
    }
    
    /**
     * Starts the mob rain event
     */
    public void start() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        if (players.isEmpty()) {
            return; // No players online
        }
        
        new BukkitRunnable() {
            int entitiesSpawned = 0;
            
            @Override
            public void run() {
                if (entitiesSpawned >= entityCount) {
                    this.cancel();
                    return;
                }
                
                // Spawn entities near random players
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (entitiesSpawned >= entityCount) {
                        break;
                    }
                    
                    spawnRandomEntityAbovePlayer(player);
                    entitiesSpawned++;
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Spawn every half second (10 ticks)
    }
    
    /**
     * Spawns a random entity above the specified player
     * @param player The player to spawn the entity above
     */
    private void spawnRandomEntityAbovePlayer(Player player) {
        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        
        // Random position within 10 blocks of the player
        int xOffset = random.nextInt(21) - 10;
        int zOffset = random.nextInt(21) - 10;
        
        Location spawnLoc = new Location(
            world,
            playerLoc.getX() + xOffset,
            playerLoc.getY() + 20, // 20 blocks above
            playerLoc.getZ() + zOffset,
            random.nextFloat() * 360, // Random yaw
            0 // Pitch
        );
        
        // Get a random entity type from our list
        EntityType entityType = possibleEntities.get(random.nextInt(possibleEntities.size()));
        
        // Spawn the entity
        Entity entity = world.spawnEntity(spawnLoc, entityType);
        
        // Prevent fall damage if it's a living entity
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setFallDistance(0);
            
            // Set the entity to not take fall damage for a short period
            new BukkitRunnable() {
                int ticks = 0;
                
                @Override
                public void run() {
                    if (ticks > 20 || entity.isDead()) {
                        this.cancel();
                        return;
                    }
                    
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).setFallDistance(0);
                    }
                    
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }
    
    /**
     * Initialize the list of entity types that can spawn
     * @return List of possible entity types
     */
    private List<EntityType> initEntityTypes() {
        List<EntityType> entities = new ArrayList<>();
        
        // Add hostile mobs
        entities.add(EntityType.ZOMBIE);
        entities.add(EntityType.SKELETON);
        entities.add(EntityType.SPIDER);
        entities.add(EntityType.CAVE_SPIDER);
        entities.add(EntityType.CREEPER);
        entities.add(EntityType.WITCH);
        entities.add(EntityType.PILLAGER);
        entities.add(EntityType.ENDERMAN);
        entities.add(EntityType.HOGLIN);
        entities.add(EntityType.RAVAGER);
        entities.add(EntityType.WARDEN);
        
        // Add passive mobs
        entities.add(EntityType.SHEEP);
        entities.add(EntityType.COW);
        entities.add(EntityType.PIG);
        entities.add(EntityType.CHICKEN);
        
        return entities;
    }
}