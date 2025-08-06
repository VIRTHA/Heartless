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
    
    private BukkitRunnable spawnTask;
    private final int maxEntitiesPerPlayer = 5; // Límite de entidades por jugador
    private final int maxEntitiesPerTick = 10; // Límite de entidades por tick
    
    /**
     * Starts the mob rain event
     * @return true if the event started successfully, false otherwise
     */
    public boolean start() {
        // Verificar si hay jugadores en línea
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        if (players.isEmpty()) {
            Bukkit.getLogger().info("MobRain no pudo iniciar: no hay jugadores en línea");
            return false; // No players online
        }
        
        // Verificar si hay entidades disponibles
        if (possibleEntities.isEmpty()) {
            Bukkit.getLogger().warning("MobRain no pudo iniciar: no hay entidades disponibles");
            return false;
        }
        
        // Cancelar tarea existente si hay
        if (spawnTask != null && !spawnTask.isCancelled()) {
            spawnTask.cancel();
        }
        
        try {
            spawnTask = new BukkitRunnable() {
                int entitiesSpawned = 0;
                int failedAttempts = 0;
                final int maxFailedAttempts = 50; // Límite de intentos fallidos
                
                @Override
                public void run() {
                    // Verificar si se completó la generación o hay demasiados fallos
                    if (entitiesSpawned >= entityCount || failedAttempts >= maxFailedAttempts) {
                        Bukkit.getLogger().info("MobRain completado: " + entitiesSpawned + " entidades generadas");
                        this.cancel();
                        return;
                    }
                    
                    // Verificar si aún hay jugadores en línea
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        Bukkit.getLogger().info("MobRain cancelado: no hay jugadores en línea");
                        this.cancel();
                        return;
                    }
                    
                    // Limitar entidades por tick para evitar lag
                    int entitiesThisTick = 0;
                    int successfulSpawns = 0;
                    
                    // Spawn entities near random players
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (entitiesSpawned >= entityCount || entitiesThisTick >= maxEntitiesPerTick) {
                            break;
                        }
                        
                        // Verificar si el jugador es válido
                        if (player == null || !player.isOnline() || player.isDead()) {
                            continue;
                        }
                        
                        // Limitar entidades por jugador
                        int entitiesForThisPlayer = Math.min(
                            maxEntitiesPerPlayer,
                            Math.min(entityCount - entitiesSpawned, maxEntitiesPerTick - entitiesThisTick)
                        );
                        
                        for (int i = 0; i < entitiesForThisPlayer; i++) {
                            if (spawnRandomEntityAbovePlayer(player)) {
                                entitiesSpawned++;
                                entitiesThisTick++;
                                successfulSpawns++;
                            } else {
                                failedAttempts++;
                            }
                        }
                    }
                    
                    // Si no se pudo generar ninguna entidad en este tick, incrementar contador de fallos
                    if (successfulSpawns == 0) {
                        failedAttempts += 5; // Incrementar más rápido si no hay éxitos
                    } else {
                        // Resetear parcialmente los fallos si hubo éxito
                        failedAttempts = Math.max(0, failedAttempts - successfulSpawns);
                    }
                }
            };
            
            spawnTask.runTaskTimer(plugin, 0L, 10L); // Spawn every half second (10 ticks)
            Bukkit.getLogger().info("MobRain iniciado: generando " + entityCount + " entidades");
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error al iniciar MobRain: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Spawns a random entity above the specified player
     * @param player The player to spawn the entity above
     * @return true if the entity was spawned successfully, false otherwise
     */
    private boolean spawnRandomEntityAbovePlayer(Player player) {
        // Verificaciones de nulidad
        if (player == null || !player.isOnline() || player.isDead()) {
            return false;
        }
        
        World world = player.getWorld();
        if (world == null) {
            return false;
        }
        
        Location playerLoc = player.getLocation();
        if (playerLoc == null) {
            return false;
        }
        
        // Verificar si el mundo permite generar entidades
        if (!world.getAllowMonsters() && !world.getAllowAnimals()) {
            return false;
        }
        
        // Random position within 10 blocks of the player
        int xOffset = random.nextInt(21) - 10;
        int zOffset = random.nextInt(21) - 10;
        
        // Verificar que la lista de entidades no esté vacía
        if (possibleEntities.isEmpty()) {
            return false;
        }
        
        // Calcular altura segura para generar
        int spawnHeight = Math.min(playerLoc.getBlockY() + 20, world.getMaxHeight() - 10);
        
        Location spawnLoc = new Location(
            world,
            playerLoc.getX() + xOffset,
            spawnHeight, // Altura segura
            playerLoc.getZ() + zOffset,
            random.nextFloat() * 360, // Random yaw
            0 // Pitch
        );
        
        // Get a random entity type from our list
        EntityType entityType = possibleEntities.get(random.nextInt(possibleEntities.size()));
        
        try {
            // Spawn the entity
            Entity entity = world.spawnEntity(spawnLoc, entityType);
            
            if (entity == null) {
                return false;
            }
            
            // Prevent fall damage if it's a living entity
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.setFallDistance(0);
                
                // Set the entity to not take fall damage for a short period
                new BukkitRunnable() {
                    int ticks = 0;
                    final int maxTicks = 40; // Aumentar a 2 segundos (40 ticks)
                    
                    @Override
                    public void run() {
                        if (ticks > maxTicks || entity.isDead() || !entity.isValid()) {
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
            
            return true;
        } catch (Exception e) {
            // Manejar cualquier excepción durante la generación
            Bukkit.getLogger().warning("Error al generar entidad en MobRain: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initialize the list of entity types that can spawn
     * @return List of possible entity types that are compatible with the current server version
     */
    private List<EntityType> initEntityTypes() {
        List<EntityType> entities = new ArrayList<>();
        
        // Obtener la versión del servidor para compatibilidad
        String version = Bukkit.getBukkitVersion();
        boolean isModernVersion = version.contains("1.16") || version.contains("1.17") || 
                                 version.contains("1.18") || version.contains("1.19") || 
                                 version.contains("1.20");
        
        // Entidades hostiles comunes (compatibles con todas las versiones)
        addEntitySafely(entities, EntityType.ZOMBIE);
        addEntitySafely(entities, EntityType.SKELETON);
        addEntitySafely(entities, EntityType.SPIDER);
        addEntitySafely(entities, EntityType.CAVE_SPIDER);
        addEntitySafely(entities, EntityType.CREEPER);
        addEntitySafely(entities, EntityType.WITCH);
        
        // Entidades que requieren versiones más recientes
        if (isModernVersion) {
            addEntitySafely(entities, EntityType.PILLAGER);
            addEntitySafely(entities, EntityType.ENDERMAN);
            
            // Entidades de la 1.16+
            try {
                addEntitySafely(entities, EntityType.valueOf("HOGLIN"));
                addEntitySafely(entities, EntityType.valueOf("RAVAGER"));
            } catch (IllegalArgumentException ignored) {
                // Estas entidades no están disponibles en esta versión
            }
            
            // Warden solo disponible en 1.19+, pero lo excluimos por ser muy poderoso
            // y potencialmente causar problemas de rendimiento
            // No incluimos WARDEN intencionalmente
        }
        
        // Entidades pasivas (compatibles con todas las versiones)
        addEntitySafely(entities, EntityType.SHEEP);
        addEntitySafely(entities, EntityType.COW);
        addEntitySafely(entities, EntityType.PIG);
        addEntitySafely(entities, EntityType.CHICKEN);
        
        // Verificar que tengamos al menos algunas entidades
        if (entities.isEmpty()) {
            // Fallback a entidades básicas si no se pudo agregar ninguna
            entities.add(EntityType.ZOMBIE);
            entities.add(EntityType.SKELETON);
        }
        
        return entities;
    }
    
    /**
     * Agrega una entidad a la lista solo si es válida y puede generarse
     * @param entities Lista de entidades
     * @param type Tipo de entidad a agregar
     */
    private void addEntitySafely(List<EntityType> entities, EntityType type) {
        try {
            if (type != null && type.isSpawnable() && type.isAlive()) {
                entities.add(type);
            }
        } catch (Exception e) {
            // Ignorar entidades que causan errores
            Bukkit.getLogger().warning("No se pudo agregar la entidad " + type + ": " + e.getMessage());
        }
    }
}