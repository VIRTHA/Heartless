package com.darkbladedev.content.custom.listeners;

import com.darkbladedev.content.custom.CustomEnchantments;
import com.darkbladedev.utils.MM;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.text.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Listener class that handles the logic for custom enchantments.
 * Currently implements:
 * - TicTac: Explodes mobs on attack
 * - Adrenaline: Grants speed and strength when at low health
 * - Acid Infection: Infects enemies with acid that deals damage over time
 * - Acid Resistance: Provides protection against acid damage
 * - Condimento: Creates explosion when consuming enchanted food
 */
public class EnchantmentListeners implements Listener {

    private final Plugin plugin;
    private final Logger logger;
    private final Map<UUID, Long> adrenalineCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> acidInfectionCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> acidInfectedEntities = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> scheduledTasks = new HashMap<>();
    
    // First Strike tracking - tracks players who have already attacked
    private final Map<UUID, Long> firstStrikeUsed = new ConcurrentHashMap<>();
    private static final long FIRST_STRIKE_RESET_TIME = 20000; // 20 seconds in milliseconds

    public EnchantmentListeners(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Start periodic cleanup task for expired First Strike cooldowns
        startPeriodicCleanup();
        
        logger.info("Custom enchantment registered successfully");
    }
    
    /**
     * Starts a periodic task to clean up expired First Strike cooldowns
     */
    private void startPeriodicCleanup() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            firstStrikeUsed.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > FIRST_STRIKE_RESET_TIME
            );
        }, 6000L, 6000L); // Run every 5 minutes (6000 ticks)
    }

    /**
     * Handles the TicTac and Acid Infection enchantment logic when a player attacks an entity
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if the damager is a player
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        // Check if the damaged entity is a living entity
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity target = (LivingEntity) event.getEntity();
        @SuppressWarnings("unused")
        UUID targetUUID = target.getUniqueId();
        
        // Handle TicTac enchantment - now works on all living entities
        if (hasEnchantment(weapon, CustomEnchantments.TICTAC_KEY)) {
            handleTictacEnchantment(player, target, weapon);
        }
        
        // Handle Acid Infection enchantment
        if (hasEnchantment(weapon, CustomEnchantments.ACID_INFECTION_KEY)) {
            // Check cooldown
            UUID playerUUID = player.getUniqueId();
            if (acidInfectionCooldowns.containsKey(playerUUID)) {
                long timeLeft = acidInfectionCooldowns.get(playerUUID) - System.currentTimeMillis();
                if (timeLeft > 0) {
                    // Still on cooldown
                    return;
                }
            }
            
            // Apply acid infection
            handleAcidInfection(player, target);
            
            // Set cooldown (5 seconds)
            acidInfectionCooldowns.put(playerUUID, System.currentTimeMillis() + 5000);
        }
        
        // Handle First Strike enchantment
        if (hasEnchantment(weapon, CustomEnchantments.FIRST_STRIKE_KEY)) {
            handleFirstStrike(player, event);
        }
    }
    
    /**
     * Handles the TicTac enchantment logic
     * TicTac: Creates a delayed explosion that affects all living entities with dynamic damage calculation
     * Damage is based on enchantment level and target's resistance attributes
     */
    private void handleTictacEnchantment(Player player, LivingEntity target, ItemStack weapon) {
        Location targetLocation = target.getLocation().clone();
        World world = target.getWorld();
        UUID targetUUID = target.getUniqueId();
        
        // Get enchantment level for dynamic damage calculation
        org.bukkit.NamespacedKey namespacedKey = org.bukkit.NamespacedKey.fromString(CustomEnchantments.TICTAC_KEY.asString());
        org.bukkit.enchantments.Enchantment tictacEnchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(namespacedKey);
        int enchantmentLevel = weapon.getItemMeta().getEnchantLevel(tictacEnchantment);
        
        // Enhanced pre-explosion visual effects
        createPreExplosionEffects(world, targetLocation, enchantmentLevel);
        
        // Schedule an explosion after 2 seconds
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Check if the entity is still alive and valid
            if (target.isValid() && !target.isDead()) {
                // Calculate dynamic damage based on enchantment power and target resistance
                double damage = calculateTictacDamage(enchantmentLevel, target);
                
                // Apply damage
                target.damage(damage);
                
                // Create enhanced explosion effects
                createExplosionEffects(world, targetLocation, enchantmentLevel, damage);
                
                // Send feedback to player
                Component message = MM.toComponent(String.format(
                    "<dark_red>¡TicTac activado! %.1f de daño</dark_red>", 
                    damage
                ));
                player.sendActionBar(message);
                
                logger.info(String.format("TicTac enchantment activated by %s on %s. Level: %d, Damage: %.1f", 
                    player.getName(), target.getType().name(), enchantmentLevel, damage));
            }
            // Remove the task from the map
            scheduledTasks.remove(targetUUID);
        }, 40L); // 40 ticks = 2 seconds
        
        // Store the task in case we need to cancel it later
        scheduledTasks.put(targetUUID, task);
    }
    
    /**
     * Calculates dynamic damage for TicTac enchantment based on level and target resistance
     * @param enchantmentLevel The level of the TicTac enchantment
     * @param target The target entity
     * @return The calculated damage amount
     */
    private double calculateTictacDamage(int enchantmentLevel, LivingEntity target) {
        // Base damage starts at 8 and increases by 4 per level
        double baseDamage = 8.0 + (enchantmentLevel * 4.0);
        
        // Calculate resistance factor based on target's armor and resistance effects
        double resistanceFactor = 1.0;
        
        // Check for resistance potion effects
        if (target.hasPotionEffect(PotionEffectType.RESISTANCE)) {
            PotionEffect resistance = target.getPotionEffect(PotionEffectType.RESISTANCE);
            if (resistance != null) {
                // Resistance reduces damage by 20% per level
                resistanceFactor -= (resistance.getAmplifier() + 1) * 0.2;
            }
        }
        
        // Factor in armor value for players and mobs with armor
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            double armorValue = targetPlayer.getAttribute(Attribute.ARMOR).getValue();
            // Each armor point reduces damage by 2%
            resistanceFactor -= (armorValue * 0.02);
        }
        
        // Ensure resistance factor doesn't go below 0.1 (minimum 10% damage)
        resistanceFactor = Math.max(0.1, resistanceFactor);
        
        // Calculate final damage
        double finalDamage = baseDamage * resistanceFactor;
        
        // Cap maximum damage at 30 to prevent one-shots
        return Math.min(finalDamage, 30.0);
    }
    
    /**
     * Creates enhanced pre-explosion visual effects
     * @param world The world where effects will be displayed
     * @param location The location for the effects
     * @param enchantmentLevel The enchantment level for scaling effects
     */
    private void createPreExplosionEffects(World world, Location location, int enchantmentLevel) {
        // Warning particles that scale with enchantment level
        int particleCount = 5 + (enchantmentLevel * 3);
        
        // Red dust particles to indicate incoming explosion
        world.spawnParticle(Particle.DUST, location.add(0, 1, 0), particleCount, 
            0.3, 0.3, 0.3, 0.1, new Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
        
        // Flame particles for dramatic effect
        world.spawnParticle(Particle.FLAME, location, particleCount / 2, 0.2, 0.2, 0.2, 0.05);
    }
    
    /**
     * Creates enhanced explosion visual and sound effects
     * @param world The world where effects will be displayed
     * @param location The location for the effects
     * @param enchantmentLevel The enchantment level for scaling effects
     * @param damage The damage dealt for effect intensity
     */
    private void createExplosionEffects(World world, Location location, int enchantmentLevel, double damage) {
        // Scale particle count based on enchantment level and damage
        int baseParticles = 8 + (enchantmentLevel * 4);
        double damageMultiplier = Math.min(damage / 15.0, 2.0); // Cap at 2x multiplier
        int totalParticles = (int) (baseParticles * damageMultiplier);
        
        // Main explosion particles
        world.spawnParticle(Particle.EXPLOSION, location, totalParticles / 2, 0.8, 0.8, 0.8, 0.2);
        
        // Large explosion particle for high-level enchantments
        if (enchantmentLevel >= 3) {
            world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 1, 0, 0, 0, 0);
        }
        
        // Fire particles for burning effect
        world.spawnParticle(Particle.FLAME, location, totalParticles, 1.0, 1.0, 1.0, 0.15);
        
        // Smoke particles for realistic explosion
        world.spawnParticle(Particle.SMOKE, location, totalParticles / 2, 0.6, 0.6, 0.6, 0.1);
        
        // Lava particles for high damage explosions
        if (damage > 20) {
            world.spawnParticle(Particle.LAVA, location, totalParticles / 4, 0.5, 0.5, 0.5, 0.1);
        }
        
        // Dynamic sound effects based on damage
        float volume = Math.min(1.0f, (float) (0.6f + (damage / 30.0f)));
        float pitch = Math.max(0.5f, (float) (1.0f - (enchantmentLevel * 0.1f)));
        
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, volume, pitch);
        
        // Additional dramatic sound for high-level enchantments
        if (enchantmentLevel >= 4) {
            world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, volume * 0.3f, pitch + 0.2f);
        }
    }
    
    /**
     * Handles the Acid Infection enchantment logic
     * Acid Infection: Infects enemies with acid that deals damage over time
     */
    private void handleAcidInfection(Player player, LivingEntity target) {
        UUID targetUUID = target.getUniqueId();
        World world = target.getWorld();
        
        // Check if entity is already infected
        if (acidInfectedEntities.containsKey(targetUUID)) {
            return;
        }
        
        // Apply acid infection effect
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1)); // Poison II for 5 seconds
        
        // Visual effects
        world.spawnParticle(Particle.LAVA, target.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
        world.playSound(target.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 0.8f);
        
        // Notify the player
        // player.sendMessage(MM.toComponent("<green>¡Has infectado a tu objetivo con ácido!"));
        
        // Schedule acid damage over time (every 1 second for 10 seconds)
        BukkitTask acidTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            private int ticks = 0;
            private final int maxTicks = 10; // 10 seconds
            
            @Override
            public void run() {
                ticks++;
                
                // Check if entity is still valid and alive
                if (!target.isValid() || target.isDead()) {
                    // Cancel task if entity is dead or invalid
                    acidInfectedEntities.remove(targetUUID);
                    BukkitTask task = scheduledTasks.remove(targetUUID);
                    if (task != null) {
                        task.cancel();
                    }
                    return;
                }
                
                // Apply acid damage
                double damage = 1.0; // 1 heart of damage per second
                target.damage(damage);
                
                // Visual effects
                world.spawnParticle(Particle.LAVA, target.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.05);
                
                // End effect after max duration
                if (ticks >= maxTicks) {
                    acidInfectedEntities.remove(targetUUID);
                    BukkitTask task = scheduledTasks.remove(targetUUID);
                    if (task != null) {
                        task.cancel();
                    }
                }
            }
        }, 20L, 20L); // Start after 1 second, repeat every 1 second
        
        // Store the task and mark entity as infected
        scheduledTasks.put(targetUUID, acidTask);
        acidInfectedEntities.put(targetUUID, player.getUniqueId());
    }
    
    /**
     * Handles the First Strike enchantment logic
     * First Strike: Deals 60% additional damage on the first attack, then goes on cooldown
     */
    private void handleFirstStrike(Player player, EntityDamageByEntityEvent event) {
        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Check if player has used First Strike recently
        if (firstStrikeUsed.containsKey(playerUUID)) {
            long timeLeft = firstStrikeUsed.get(playerUUID) + FIRST_STRIKE_RESET_TIME - currentTime;
            if (timeLeft > 0) {
                // Still on cooldown, no bonus damage
                return;
            }
        }
        
        // Apply First Strike bonus damage (60% additional)
        double originalDamage = event.getDamage();
        double bonusDamage = originalDamage * 0.6;
        event.setDamage(originalDamage + bonusDamage);
        
        // Visual and sound effects
        World world = player.getWorld();
        world.spawnParticle(Particle.CRIT, event.getEntity().getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.DAMAGE_INDICATOR, event.getEntity().getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.1);
        world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
        
        // Send message to player
        Component message = MM.toComponent("<dark_red>¡Primer Golpe activado! +60% de daño adicional</dark_red>");
        player.sendActionBar(message);
        
        // Set cooldown
        firstStrikeUsed.put(playerUUID, currentTime);
        
        logger.info("First Strike activated for player " + player.getName() + 
                   ". Original damage: " + originalDamage + ", Bonus damage: " + bonusDamage + 
                   ", Total damage: " + (originalDamage + bonusDamage));
    }

    /**
     * Handles the Adrenaline enchantment logic when a player takes damage
     * Adrenaline: Grants Speed II and Strength II for 10 seconds when health is below 30%
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        // Check if the entity is a player
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        UUID playerUUID = player.getUniqueId();
        
        // Handle Acid Resistance for acid damage
        if (event.getCause() == EntityDamageEvent.DamageCause.POISON) {
            // Check if player has Acid Resistance enchantment on any armor piece
            boolean hasAcidResistance = false;
            for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
                if (armorPiece != null && hasEnchantment(armorPiece, CustomEnchantments.ACID_RESISTANCE_KEY)) {
                    hasAcidResistance = true;
                    break;
                }
            }
            
            if (hasAcidResistance) {
                // Reduce damage by 50% if player has acid resistance
                event.setDamage(event.getDamage() * 0.5);
                
                // Visual effect to show resistance
                player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        // Check cooldown for Adrenaline
        if (adrenalineCooldowns.containsKey(playerUUID)) {
            long timeLeft = adrenalineCooldowns.get(playerUUID) - System.currentTimeMillis();
            if (timeLeft > 0) {
                // Still on cooldown
                return;
            }
        }

        // Check if player has Adrenaline enchantment on any armor piece
        boolean hasAdrenaline = false;
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece != null && hasEnchantment(armorPiece, CustomEnchantments.ADRENALINE_KEY)) {
                hasAdrenaline = true;
                break;
            }
        }

        if (!hasAdrenaline) {
            return;
        }

        // Calculate health percentage after damage
        double healthAfterDamage = player.getHealth() - event.getFinalDamage();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double healthPercentage = (healthAfterDamage / maxHealth) * 100;

        // Trigger Adrenaline if health falls below 30%
        if (healthPercentage <= 30 && healthAfterDamage > 0) {
            // Apply effects
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1)); // Speed II for 10 seconds
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1)); // Resistance II for 10 seconds
            
            // Visual and sound effects
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
            
            // Send message to player
            Component message = MM.toComponent("<gold>¡Adrenalina se ha activado! +Velocidad II y +Resistencia II por 10 segundos</gold>");
            player.sendMessage(message);
            
            // Set cooldown (60 seconds)
            adrenalineCooldowns.put(playerUUID, System.currentTimeMillis() + 60000);
        }
    }

    /**
     * Handles the Condimento enchantment logic when a player consumes food
     * Condimento: Creates an explosion when consuming enchanted food, with power scaling by level
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack consumedItem = event.getItem();
        
        // Check if the consumed item has the Condimento enchantment
        if (!hasEnchantment(consumedItem, CustomEnchantments.CONDIMENT_KEY)) {
            return;
        }
        
        // Verify if the item is valid for Condimento enchantment using custom tag logic
        if (!com.darkbladedev.utils.TagUtils.isValidCondimentoTarget(consumedItem)) {
            // Item is not valid for Condimento, but has the enchantment - this shouldn't happen normally
            // but we'll handle it gracefully by not triggering the effect
            logger.warning(String.format("Player %s consumed item %s with Condimento enchantment but item is not valid for this enchantment", 
                player.getName(), consumedItem.getType().name()));
            return;
        }
        
        // Get enchantment level for explosion power scaling
        org.bukkit.NamespacedKey namespacedKey = org.bukkit.NamespacedKey.fromString(CustomEnchantments.CONDIMENT_KEY.asString());
        org.bukkit.enchantments.Enchantment condimentoEnchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(namespacedKey);
        int enchantmentLevel = consumedItem.getItemMeta().getEnchantLevel(condimentoEnchantment);
        
        // Handle the explosion effect
        handleCondimentoExplosion(player, enchantmentLevel);
    }
    
    /**
     * Handles the explosion effect for the Condimento enchantment
     * @param player The player who consumed the enchanted food
     * @param enchantmentLevel The level of the Condimento enchantment
     */
    private void handleCondimentoExplosion(Player player, int enchantmentLevel) {
        Location playerLocation = player.getLocation().clone();
        World world = player.getWorld();
        
        // Calculate explosion power based on enchantment level
        // Level 1: 1.0 power, Level 2: 1.5 power, Level 3: 2.0 power
        float explosionPower = 1.0f + (enchantmentLevel - 1) * 0.5f;
        
        // Create pre-explosion warning effects
        createCondimentoPreExplosionEffects(world, playerLocation, enchantmentLevel);
        
        // Schedule explosion after a short delay (1 second)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Create explosion at player's location
            world.createExplosion(playerLocation, explosionPower, false, false);
            
            // Create additional visual effects
            createCondimentoExplosionEffects(world, playerLocation, enchantmentLevel, explosionPower);
            
            // Send feedback to player
            Component message = MM.toComponent(String.format(
                "<gold>¡Condimento activado! Explosión de poder %.1f</gold>", 
                explosionPower
            ));
            player.sendActionBar(message);
            
            logger.info(String.format("Condimento enchantment activated by %s. Level: %d, Power: %.1f", 
                player.getName(), enchantmentLevel, explosionPower));
        }, 20L); // 20 ticks = 1 second
    }
    
    /**
     * Creates pre-explosion warning effects for Condimento enchantment
     * @param world The world where effects will be displayed
     * @param location The location for the effects
     * @param enchantmentLevel The enchantment level for scaling effects
     */
    private void createCondimentoPreExplosionEffects(World world, Location location, int enchantmentLevel) {
        // Warning particles that scale with enchantment level
        int particleCount = 8 + (enchantmentLevel * 4);
        
        // Orange dust particles to indicate spicy condiment
        world.spawnParticle(Particle.DUST, location.add(0, 1, 0), particleCount, 
            0.4, 0.4, 0.4, 0.1, new Particle.DustOptions(org.bukkit.Color.ORANGE, 1.2f));
        
        // Flame particles for spicy effect
        world.spawnParticle(Particle.FLAME, location, particleCount / 2, 0.3, 0.3, 0.3, 0.05);
        
        // Sound effect for warning
        world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 0.8f, 1.2f);
    }
    
    /**
     * Creates explosion visual and sound effects for Condimento enchantment
     * @param world The world where effects will be displayed
     * @param location The location for the effects
     * @param enchantmentLevel The enchantment level for scaling effects
     * @param explosionPower The power of the explosion for effect intensity
     */
    private void createCondimentoExplosionEffects(World world, Location location, int enchantmentLevel, float explosionPower) {
        // Scale particle count based on enchantment level and explosion power
        int baseParticles = 10 + (enchantmentLevel * 5);
        double powerMultiplier = Math.min(explosionPower / 1.5, 2.0); // Cap at 2x multiplier
        int totalParticles = (int) (baseParticles * powerMultiplier);
        
        // Spicy explosion particles (orange and red)
        world.spawnParticle(Particle.DUST, location, totalParticles, 0.8, 0.8, 0.8, 0.2, 
            new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 140, 0), 1.5f)); // Orange
        world.spawnParticle(Particle.DUST, location, totalParticles / 2, 0.6, 0.6, 0.6, 0.15, 
            new Particle.DustOptions(org.bukkit.Color.RED, 1.3f)); // Red
        
        // Fire particles for spicy burning effect
        world.spawnParticle(Particle.FLAME, location, totalParticles, 1.2, 1.2, 1.2, 0.2);
        
        // Lava particles for high-level enchantments
        if (enchantmentLevel >= 2) {
            world.spawnParticle(Particle.LAVA, location, totalParticles / 3, 0.7, 0.7, 0.7, 0.1);
        }
        
        // Smoke particles for realistic explosion
        world.spawnParticle(Particle.SMOKE, location, totalParticles / 2, 0.8, 0.8, 0.8, 0.15);
        
        // Dynamic sound effects based on explosion power
        float volume = Math.min(1.0f, 0.7f + (explosionPower / 3.0f));
        float pitch = Math.max(0.8f, 1.2f - (enchantmentLevel * 0.1f));
        
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, volume, pitch);
        
        // Additional spicy sound for higher levels
        if (enchantmentLevel >= 3) {
            world.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, volume * 0.6f, pitch + 0.3f);
        }
    }

    /**
     * Helper method to check if an item has a specific enchantment
     */
    @SuppressWarnings("deprecation")
    private boolean hasEnchantment(ItemStack item, net.kyori.adventure.key.Key enchantmentKey) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        org.bukkit.NamespacedKey namespacedKey = org.bukkit.NamespacedKey.fromString(enchantmentKey.asString());
        if (namespacedKey == null) {
            return false;
        }
        
        org.bukkit.enchantments.Enchantment enchantment = org.bukkit.Registry.ENCHANTMENT.get(namespacedKey);
        return enchantment != null && item.getItemMeta().hasEnchant(enchantment);
    }

    /**
     * Cleanup method to cancel all scheduled tasks
     * Should be called when the plugin is disabled
     */
    public void cleanup() {
        for (BukkitTask task : scheduledTasks.values()) {
            task.cancel();
        }
        scheduledTasks.clear();
        adrenalineCooldowns.clear();
        acidInfectionCooldowns.clear();
        acidInfectedEntities.clear();
        firstStrikeUsed.clear();
        logger.info("EnchantmentListeners cleaned up successfully");
    }
}