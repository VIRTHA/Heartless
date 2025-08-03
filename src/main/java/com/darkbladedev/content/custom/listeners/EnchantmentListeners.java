package com.darkbladedev.content.custom.listeners;

import com.darkbladedev.content.custom.CustomEnchantments;
import com.darkbladedev.utils.MM;

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
 * - Carve: Explodes mobs on attack
 * - Adrenaline: Grants speed and strength when at low health
 * - Acid Infection: Infects enemies with acid that deals damage over time
 * - Acid Resistance: Provides protection against acid damage
 */
public class EnchantmentListeners implements Listener {

    private final Plugin plugin;
    private final Logger logger;
    private final Map<UUID, Long> adrenalineCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> acidInfectionCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> acidInfectedEntities = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> scheduledTasks = new HashMap<>();

    public EnchantmentListeners(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        logger.info("EnchantmentListeners registered successfully");
    }

    /**
     * Handles the Carve and Acid Infection enchantment logic when a player attacks an entity
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
        
        // Handle Carve enchantment
        if (hasEnchantment(weapon, CustomEnchantments.CARVE_KEY)) {
            // Don't apply to players
            if (!(target instanceof Player)) {
                handleCarveEnchantment(player, target);
            }
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
    }
    
    /**
     * Handles the Carve enchantment logic
     * Carve: Explodes mobs on attack with a 2-second delay
     */
    private void handleCarveEnchantment(Player player, LivingEntity target) {
        Location targetLocation = target.getLocation();
        World world = target.getWorld();

        // Schedule an explosion after 2 seconds
        UUID targetUUID = target.getUniqueId();
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Check if the entity is still alive and valid
            if (target.isValid() && !target.isDead()) {                
                // Damage the entity
                target.damage(10);
                
                // Create a non-destructive explosion effect
                world.spawnParticle(Particle.EXPLOSION, targetLocation, 4, 0.5, 0.5, 0.5, 0.1);
                world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
                
                // Notify the player
                // player.sendMessage(MM.toComponent("<red>¡Tu encantamiento Carve ha hecho explotar a tu objetivo!"));
            }
            // Remove the task from the map
            scheduledTasks.remove(targetUUID);
        }, 40L); // 40 ticks = 2 seconds
        
        // Store the task in case we need to cancel it later
        scheduledTasks.put(targetUUID, task);
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
        logger.info("EnchantmentListeners cleaned up successfully");
    }
}