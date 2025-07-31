package com.darkbladedev.mechanics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.MM;

public class ExplosiveWeek extends WeeklyEvent {
    
    private final Random random = new Random();
    
    // Tasks
    private BukkitTask ghastSpawnTask;
    private BukkitTask ghastAttackTask;
    
    // Challenge tracking
    private final Set<UUID> ghastKillers = new HashSet<>();
    private final Map<UUID, Set<EntityType>> mobHeadCollectors = new HashMap<>();
    private final Set<UUID> playerExplosionKillers = new HashSet<>();
    private final Set<UUID> wardenCreeperKillers = new HashSet<>();
    
    // Set of classic hostile mobs for the head collection challenge
    private final Set<EntityType> classicHostileMobs = new HashSet<>(Arrays.asList(
            EntityType.ZOMBIE, 
            EntityType.SKELETON, 
            EntityType.CREEPER, 
            EntityType.SPIDER, 
            EntityType.ENDERMAN
    ));
    
    private BukkitTask mainTask;
    
    public ExplosiveWeek(HeartlessMain plugin, long duration) {
        super(plugin, duration);
        this.prefix = "<b><gradient:#ed2f2f:#f15c5c:#f58888:#f9b5b5:#fce1e1:#ffffff:#ffffff:#ffffff:#ffffff:#ffffff:#fce1e2:#f8b5b5:#f48989:#f05c5d:#ec3031>Semana Explosiva</gradient></b>";
    }
    
    @Override
    protected void startEventTasks() {
        startMainTask();
        
        // Start ghast spawning in thunderstorms
        startGhastSpawning();
    }
    
    @Override
    protected void announceEventStart() {
        // Announce the start of the event
        Bukkit.broadcast(MM.toComponent("<yellow><b>¡<gradient:#fa4444:#fb6c6c:#fc9494:#fdbcbc:#fee4e4:#ffffff:#ffffff:#ffffff:#ffffff:#ffffff:#fee4e4:#fdbcbc:#fc9494:#fb6c6c:#fa4444>SEMANA EXPLOSIVA</gradient> INICIADA!"));
        Bukkit.broadcast(MM.toComponent("<gray>¡Cuidado con las explosiones! <b>Todo es más volátil..."));
        
        // Announce challenges
        announceExplosiveWeekChallenges();
    }
    
    @Override
    protected void stopEventTasks() {
        // Cancel tasks
        if (ghastSpawnTask != null) {
            ghastSpawnTask.cancel();
            ghastSpawnTask = null;
        }
        
        if (ghastAttackTask != null) {
            ghastAttackTask.cancel();
            ghastAttackTask = null;
        }
        
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        
        // Remove all spawned ghasts in overworld
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == Environment.NORMAL) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getType() == EntityType.GHAST) {
                        entity.remove();
                    }
                }
            }
        }
    }
    
    @Override
    protected void cleanupEventData() {
        // Limpiar datos de seguimiento de desafíos
        ghastKillers.clear();
        mobHeadCollectors.clear();
        playerExplosionKillers.clear();
        wardenCreeperKillers.clear();
    }
    
    @Override
    public String getName() {
        return "Semana Explosiva";
    }
    
    @Override
    protected void pauseEventTasks() {
        if (ghastSpawnTask != null) {
            ghastSpawnTask.cancel();
        }
        
        if (ghastAttackTask != null) {
            ghastAttackTask.cancel();
        }
        
        if (mainTask != null) {
            mainTask.cancel();
        }
    }
    
    @Override
    protected void resumeEventTasks() {
        startGhastSpawning();
        startMainTask();
    }
    
    private void startGhastSpawning() {
        // Check for thunderstorms and spawn ghasts
        ghastSpawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() == Environment.NORMAL && 
                        world.isThundering() && 
                        world.getTime() > 12000) { // Only at night
                        
                        // Spawn ghasts for each player in the world
                        for (Player player : world.getPlayers()) {
                            if (random.nextInt(100) < 15) { // 15% chance per player
                                spawnGhastNearPlayer(player);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 30); // Check every 30 seconds
        
        // Make ghasts shoot more frequently
        ghastAttackTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() == Environment.NORMAL) {
                        for (Entity entity : world.getEntities()) {
                            if (entity.getType() == EntityType.GHAST) {
                                Ghast ghast = (Ghast) entity;
                                
                                // Find nearest player
                                Player target = findNearestPlayer(ghast.getLocation(), 32);
                                if (target != null) {
                                    // Make the ghast look at the player
                                    ghast.setTarget(target);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 5); // Every 5 seconds
    }
    
    private void spawnGhastNearPlayer(Player player) {
        Location playerLoc = player.getLocation();
        
        // Find a safe location 20-30 blocks away and 10-20 blocks up
        Location spawnLoc = playerLoc.clone().add(
                (random.nextDouble() * 20 + 10) * (random.nextBoolean() ? 1 : -1),
                random.nextDouble() * 10 + 10,
                (random.nextDouble() * 20 + 10) * (random.nextBoolean() ? 1 : -1)
        );
        
        // Ensure the location is in air
        if (spawnLoc.getBlock().getType() == Material.AIR) {
            Ghast ghast = (Ghast) player.getWorld().spawnEntity(spawnLoc, EntityType.GHAST);
            
            // Make the ghast target the player
            ghast.setTarget(player);
            
            // Notify nearby players
            for (Player nearby : Bukkit.getOnlinePlayers()) {
                if (nearby.getWorld().equals(player.getWorld()) && 
                    nearby.getLocation().distance(spawnLoc) <= 50) {
                    nearby.sendMessage(MM.toComponent("<red>¡Un ghast ha aparecido cerca tuyo!"));
                }
            }
        }
    }
    
    private Player findNearestPlayer(Location location, double maxDistance) {
        Player nearest = null;
        double nearestDistance = maxDistance;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(location.getWorld())) {
                double distance = player.getLocation().distance(location);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = player;
                }
            }
        }
        
        return nearest;
    }
    
    private void announceExplosiveWeekChallenges() {
        Bukkit.broadcast(MM.toComponent("<yellow><b>=== DESAFÍOS DE LA SEMANA ==="));
        Bukkit.broadcast(MM.toComponent("<yellow>1. Mata a un ghast en el overworld"));
        Bukkit.broadcast(MM.toComponent("<gray>   Recompensa: Encantamiento Carve"));
        Bukkit.broadcast(MM.toComponent("<yellow>2. Consigue la cabeza de todos los mobs hostiles posibles"));
        Bukkit.broadcast(MM.toComponent("<gray>   (Zombie, Esqueleto, Creeper)"));
        Bukkit.broadcast(MM.toComponent("<gray>   Recompensa: +1 corazón"));
        Bukkit.broadcast(MM.toComponent("<yellow>3. Mata a un jugador con una explosión"));
        Bukkit.broadcast(MM.toComponent("<gray>   Recompensa: Tag \"TNTómano\""));
        Bukkit.broadcast(MM.toComponent("<yellow>4. Mata a un warden con la explosión de un creeper eléctrico"));
        Bukkit.broadcast(MM.toComponent("<gray>   Recompensa: +1 corazón"));
    }
    
    // Event Handlers
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isActive) return;
        
        if (event.getEntityType() == EntityType.CREEPER) {
            Creeper creeper = (Creeper) event.getEntity();
            creeper.setPowered(true); // Make all creepers charged
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!isActive) return;
        
        // TNT explosions are handled in ExplosionPrimeEvent
    }
    
    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!isActive) return;
        
        if (event.getEntity() instanceof TNTPrimed) {
            // Double TNT explosion power
            event.setRadius(event.getRadius() * 1.4f); // Approximately doubles damage
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!isActive) return;
        
        if (event.getCause() == DamageCause.BLOCK_EXPLOSION || 
            event.getCause() == DamageCause.ENTITY_EXPLOSION) {
            // Double explosion damage
            if (event.getEntity() instanceof Player) {
                double damage = event.getDamage();
                event.setDamage(damage * 2.0);
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isActive) return;
        
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        
        // Track player kills by explosion for challenge
        if (victim instanceof Player && 
            (event.getCause() == DamageCause.BLOCK_EXPLOSION || event.getCause() == DamageCause.ENTITY_EXPLOSION)) {
            
            Player deadPlayer = (Player) victim;
            
            // Check if the explosion was caused by another player
            Player killer = null;
            
            if (damager instanceof TNTPrimed) {
                TNTPrimed tnt = (TNTPrimed) damager;
                if (tnt.getSource() instanceof Player) {
                    killer = (Player) tnt.getSource();
                }
            } else if (damager instanceof Creeper) {
                // For creepers, we need to check if a player led it to the victim
                // This is approximate and might not be 100% accurate
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player != deadPlayer && 
                        player.getWorld().equals(damager.getWorld()) &&
                        player.getLocation().distance(damager.getLocation()) < 16) {
                        killer = player;
                        break;
                    }
                }
            }
            
            if (killer != null && deadPlayer.getHealth() - event.getFinalDamage() <= 0) {
                // Player will die from this explosion
                playerExplosionKillers.add(killer.getUniqueId());
                
                // Award the challenge reward
                awardExplosionKillChallenge(killer);
            }
        }
        
        // Track warden kills by charged creeper for challenge
        if (victim instanceof Warden && damager instanceof Creeper) {
            Creeper creeper = (Creeper) damager;
            if (creeper.isPowered() && ((Warden) victim).getHealth() - event.getFinalDamage() <= 0) {
                // Find nearby players who might have led the creeper to the warden
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(victim.getWorld()) &&
                        player.getLocation().distance(victim.getLocation()) < 32) {
                        wardenCreeperKillers.add(player.getUniqueId());
                        
                        // Award the challenge reward
                        awardWardenCreeperKillChallenge(player);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isActive) return;
        
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        // Iron Golem death explosion
        if (entity instanceof IronGolem) {
            Location location = entity.getLocation();
            entity.getWorld().createExplosion(location, 4.0f, false, true);
        }
        
        // Ghast kill in overworld challenge
        if (entity instanceof Ghast && 
            entity.getWorld().getEnvironment() == Environment.NORMAL && 
            killer != null) {
            
            ghastKillers.add(killer.getUniqueId());
            
            // Award the challenge reward
            awardGhastKillChallenge(killer);
        }
        
        // Track mob head collection for challenge
        if (killer != null && classicHostileMobs.contains(entity.getType())) {
            // Check if the mob dropped its head
            for (ItemStack drop : event.getDrops()) {
                if (drop.getType() == Material.PLAYER_HEAD || 
                    drop.getType() == Material.ZOMBIE_HEAD || 
                    drop.getType() == Material.SKELETON_SKULL || 
                    drop.getType() == Material.CREEPER_HEAD) {
                    
                    // Add to the player's collection
                    mobHeadCollectors.computeIfAbsent(killer.getUniqueId(), k -> new HashSet<>())
                                    .add(entity.getType());
                    
                    // Check if they've collected all heads
                    if (mobHeadCollectors.get(killer.getUniqueId()).size() >= classicHostileMobs.size()) {
                        // Award the challenge reward
                        awardMobHeadCollectionChallenge(killer);
                    }
                    
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isActive) return;
        
        Block block = event.getBlock();
        Material type = block.getType();
        
        // 10% chance of explosion when mining certain ores
        if ((type == Material.COAL_ORE || type == Material.DEEPSLATE_COAL_ORE ||
             type == Material.IRON_ORE || type == Material.DEEPSLATE_IRON_ORE ||
             type == Material.REDSTONE_ORE || type == Material.DEEPSLATE_REDSTONE_ORE) && 
            random.nextInt(100) < 10) {
            
            // Create a small explosion (2 hearts damage)
            Location location = block.getLocation().add(0.5, 0.5, 0.5);
            block.getWorld().createExplosion(location, 1.0f, false, true);
            
            // Send message to the player
            event.getPlayer().sendMessage(MM.toComponent("<red>¡El mineral ha explotado!"));
        }
    }
    
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!isActive) return;
        
        // If weather is changing to stormy, check for ghast spawning
        if (event.toWeatherState() && event.getWorld().getEnvironment() == Environment.NORMAL) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (isActive && event.getWorld().hasStorm()) {
                        for (Player player : event.getWorld().getPlayers()) {
                            if (random.nextInt(100) < 30) { // 30% chance per player
                                spawnGhastNearPlayer(player);
                            }
                        }
                    }
                }
            }.runTaskLater(plugin, 20L * 10); // Wait 10 seconds after weather change
        }
    }
    
    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        if (!isActive) return;
        
        // If thunder is starting, spawn more ghasts
        if (event.toThunderState() && event.getWorld().getEnvironment() == Environment.NORMAL) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (isActive && event.getWorld().isThundering()) {
                        for (Player player : event.getWorld().getPlayers()) {
                            if (random.nextInt(100) < 50) { // 50% chance per player
                                spawnGhastNearPlayer(player);
                            }
                        }
                    }
                }
            }.runTaskLater(plugin, 20L * 5); // Wait 5 seconds after thunder starts
        }
    }
    
    // Challenge reward methods
    
    private void awardGhastKillChallenge(Player player) {
        if (ghastKillers.contains(player.getUniqueId())) {
            // Already awarded
            return;
        }
        
        player.sendMessage(MM.toComponent("&a&l¡DESAFÍO COMPLETADO!"));
        player.sendMessage(MM.toComponent("&eHas matado a un ghast en el Overworld."));
        player.sendMessage(MM.toComponent("&6Recompensa: Encantamiento Carve"));
        
        // TOD0: Add Carve enchantment to player's held item
        // This would require implementing the Carve enchantment
        
        ghastKillers.add(player.getUniqueId());
        
        // Announce to server
        Bukkit.broadcast(MM.toComponent("&6" + player.getName() + " &eha completado el desafío: &7Matar a un ghast en el Overworld"));
    }
    
    private void awardMobHeadCollectionChallenge(Player player) {
        Set<EntityType> collectedHeads = mobHeadCollectors.getOrDefault(player.getUniqueId(), new HashSet<>());
        
        if (collectedHeads.size() >= classicHostileMobs.size()) {
            player.sendMessage(MM.toComponent("&a&l¡DESAFÍO COMPLETADO!"));
            player.sendMessage(MM.toComponent("&eHas conseguido la cabeza de todos los mobs hostiles clásicos."));
            player.sendMessage(MM.toComponent("&6Recompensa: +1 corazón permanente"));
            
            // Add one heart to player's max health
            double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMaxHealth + 2.0);
            
            // Announce to server
            Bukkit.broadcast(MM.toComponent("&6" + player.getName() + " &eha completado el desafío: &7Coleccionar todas las cabezas de mobs hostiles clásicos"));
        }
    }
    
    private void awardExplosionKillChallenge(Player player) {
        if (playerExplosionKillers.contains(player.getUniqueId())) {
            // Already awarded
            return;
        }
        
        player.sendMessage(MM.toComponent("<green><b>¡DESAFÍO COMPLETADO!"));
        player.sendMessage(MM.toComponent("<yellow>Has matado a un jugador con una explosión."));
        player.sendMessage(MM.toComponent("<gray>Recompensa: <u>Tag \"TNTómano\""));
        
        // Award the tag using EternalTags
        //eternalAPI.setTag(player, new Tag("tntomano", "tntomano", "&x&f&f&f&f&f&fT&x&f&a&d&b&d&bN&x&f&5&b&8&b&8T&x&f&0&9&4&9&4o&x&e&c&7&1&7&1m&x&e&7&4&d&4&da&x&e&2&2&a&2&an&x&d&d&0&6&0&6o")); //&#10dd00T&#2bc205N&#45a80aT&#608d0fo&#7a7213m&#955718a&#af3d1dn&#ca2222o
        
        playerExplosionKillers.add(player.getUniqueId());
        
        // Announce to server
        Bukkit.broadcast(MM.toComponent("&6" + player.getName() + " &eha completado el desafío: &7Matar a un jugador con una explosión"));
    }
    
    private void awardWardenCreeperKillChallenge(Player player) {
        if (wardenCreeperKillers.contains(player.getUniqueId())) {
            // Already awarded
            return;
        }
        
        player.sendMessage(MM.toComponent("&a&l¡DESAFÍO COMPLETADO!"));
        player.sendMessage(MM.toComponent("&eHas matado a un warden con la explosión de un creeper eléctrico."));
        player.sendMessage(MM.toComponent("&6Recompensa: +1 corazón permanente"));
        
        // Add one heart to player's max health
        double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMaxHealth + 2.0);
        
        wardenCreeperKillers.add(player.getUniqueId());
        
        // Announce to server
        Bukkit.broadcast(MM.toComponent("&6" + player.getName() + " &eha completado el desafío: &7Matar a un warden con un creeper eléctrico"));
    }
    
    public boolean isActive() {
        return isActive;
    }

    public void pause() {
        if (!isActive || isPaused) return;
        
        isPaused = true;
        
        // Cancel the main task
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
    }

    public void resume() {
        if (!isActive || !isPaused) return;
        
        isPaused = false;
        
        // Restart the main task
        startMainTask();
    }

    private void startMainTask() {
        // Cancel existing task if any
        if (mainTask != null) {
            mainTask.cancel();
        }
        
        // Start the main task
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Check for players with explosive effects
                for (@SuppressWarnings("unused") Player player : Bukkit.getOnlinePlayers()) {
                    // Apply any ongoing effects or checks here
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 5); // Check every 5 seconds
    }

    /**
     * Checks if a player has completed a specific challenge
     * @param playerId The UUID of the player
     * @param challengeId The ID of the challenge
     * @return true if the challenge is completed, false otherwise
     */
    public boolean hasChallengeCompleted(UUID playerId, String challengeId) {
        switch (challengeId) {
            case "ghast_killer":
                // Player has killed a ghast
                return ghastKillers.contains(playerId);
            case "mob_head_collector":
                // Player has collected all classic hostile mob heads
                Set<EntityType> collectedHeads = mobHeadCollectors.getOrDefault(playerId, new HashSet<>());
                return collectedHeads.containsAll(classicHostileMobs);
            case "explosion_killer":
                // Player has killed another player with an explosion
                return playerExplosionKillers.contains(playerId);
            case "warden_creeper_killer":
                // Player has killed a warden with a creeper
                return wardenCreeperKillers.contains(playerId);
            default:
                return false;
        }
    }
}