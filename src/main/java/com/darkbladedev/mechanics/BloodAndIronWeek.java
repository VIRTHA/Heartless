package com.darkbladedev.mechanics;

import com.darkbladedev.utils.MM;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


import java.util.*;

public class BloodAndIronWeek implements Listener {

    
    private final Plugin plugin;
    private final long duration;
    private boolean isActive = false;
    private boolean isPaused = false;
    private BukkitTask mainTask;
    private BukkitTask checkKillsTask;
    private BukkitTask endTask;
    private String prefix = "<b><gradient:#f82f2f:#f74242:#f75555:#f66869:#f67b7c:#f58f8f:#f4a2a2:#f4b5b5:#f3c8c9:#f3dbdc:#f2eeef:#f2eeef:#f2edee:#f2edee:#f2eded:#f3eded:#f3eced:#f3ecec:#f3ecec:#f3ebeb:#f3ebeb>Semana de Sangre y Hierro</gradient></b>";
    
    // Player tracking maps
    private final Map<UUID, Long> lastHostileMobKillTime = new HashMap<>();
    private final Map<UUID, Long> lastPlayerKillTime = new HashMap<>();
    private final Map<UUID, Integer> playerKillCount = new HashMap<>();
    private final Map<UUID, Integer> consecutiveKills = new HashMap<>();
    private final Set<UUID> instantDamageKillers = new HashSet<>();
    private final Set<UUID> pentakillPlayers = new HashSet<>();
    private final Set<UUID> survivedPlayers = new HashSet<>();
    private final Set<UUID> deadPlayers = new HashSet<>();
    private final Set<UUID> awardedAdrenaline = new HashSet<>();
    
    // Constants
    private static final long MOB_KILL_TIMEOUT = 10 * 60 * 1000; // 10 minutes in milliseconds
    private static final long PLAYER_KILL_TIMEOUT = 60 * 60 * 1000; // 1 hour in milliseconds
    
    public BloodAndIronWeek(Plugin plugin, long duration) {
        this.plugin = plugin;
        this.duration = duration;
    }
    
    public void start() {
        if (isActive) return;
        
        isActive = true;
        isPaused = false;

        startMainTask();
        startCheckKillsTask();
            
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Add all online players to tracking
        for (Player player : Bukkit.getOnlinePlayers()) {
            initializePlayer(player);
        }
        
        // Announce the start of the event
        Bukkit.broadcast(MM.toComponent(prefix + " <gray>¡<gold>El coliseo del caos está abierto. <red>Elimina o sé eliminado<gray>!"));
        
        // Announce challenges
        announceChallenges();
        
        // Start the main task that checks conditions every second
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkMobKillTimeout();
                checkPlayerKillTimeout();
                checkArmorEffects();
                checkWeaponEffects();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Every second
        
        // Schedule the end of the event
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        }.runTaskLater(plugin, duration * 20L);
    }
    
    public void stop() {
        if (!isActive) return;
        
        isActive = false;
        
        // Cancel tasks
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        
        if (checkKillsTask != null) {
            checkKillsTask.cancel();
            checkKillsTask = null;
        }
        
        if (endTask != null) {
            endTask.cancel();
            endTask = null;
        }
        
        // Award survival challenge rewards
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            if (!deadPlayers.contains(playerId) && playerKillCount.getOrDefault(playerId, 0) >= 10) {
                awardSurvivalChallenge(player);
            }
        }
        
        // Clear all tracking data
        lastHostileMobKillTime.clear();
        lastPlayerKillTime.clear();
        playerKillCount.clear();
        consecutiveKills.clear();
        instantDamageKillers.clear();
        pentakillPlayers.clear();
        survivedPlayers.clear();
        deadPlayers.clear();
        awardedAdrenaline.clear();

        // Unregister events
        PlayerDeathEvent.getHandlerList().unregister(this);
        EntityDeathEvent.getHandlerList().unregister(this);
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerItemHeldEvent.getHandlerList().unregister(this);
        
        // Announce the end of the event
        Bukkit.broadcast(MM.toComponent("&4[&cSemana de Sangre y Hierro&4] &cEl coliseo del caos ha cerrado sus puertas... por ahora."));
    }

    public void pause() {
        if (!isActive || isPaused) return;
        
        isPaused = true;
        
        // Cancel tasks
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        
        if (checkKillsTask != null) {
            checkKillsTask.cancel();
            checkKillsTask = null;
        }
        
        // Remove temporary effects
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeArmorEffects(player);
        }
    }
    
    public void resume() {
        if (!isActive || !isPaused) return;
        
        isPaused = false;
        
        // Restart tasks
        startMainTask();
        startCheckKillsTask();
        
        // Reapply effects
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkAndApplyArmorEffects(player);
        }
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
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Check and apply armor effects
                    checkAndApplyArmorEffects(player);
                    
                    // Check if player is holding diamond/netherite sword
                    checkAndApplySwordEffects(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 5); // Check every 5 seconds
    }
    
    private void startCheckKillsTask() {
        // Cancel existing task if any
        if (checkKillsTask != null) {
            checkKillsTask.cancel();
        }
        
        // Start the check kills task
        checkKillsTask = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    
                    // Check mob kills
                    Long lastMobKillTime = lastHostileMobKillTime.get(playerId);
                    if (lastMobKillTime == null || (currentTime - lastMobKillTime) > 10 * 60 * 1000) {
                        // No mob kill in 10 minutes
                        reducePlayerHealth(player, 4); // 2 hearts
                    }
                    
                    // Check player kills
                    Long LastPlayerKillTime = lastPlayerKillTime.get(playerId);
                    if (lastPlayerKillTime == null || (currentTime - LastPlayerKillTime) > 60 * 60 * 1000) {
                        // No player kill in 1 hour
                        reducePlayerHealth(player, 10); // 5 hearts
                    }
                }
            }
        }.runTaskTimer(plugin, 20L * 60, 20L * 60); // Check every minute
    }
    
    
    private void initializePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Initialize kill times to current time
        lastHostileMobKillTime.put(playerId, System.currentTimeMillis());
        lastPlayerKillTime.put(playerId, System.currentTimeMillis());
        
        // Initialize kill counts
        if (!playerKillCount.containsKey(playerId)) {
            playerKillCount.put(playerId, 0);
        }
        
        if (!consecutiveKills.containsKey(playerId)) {
            consecutiveKills.put(playerId, 0);
        }
        
        // Add to survived players list if not already dead
        if (!deadPlayers.contains(playerId)) {
            survivedPlayers.add(playerId);
        }
    }
    
    private void checkMobKillTimeout() {
        long currentTime = System.currentTimeMillis();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            
            // Skip players who joined after the event started
            if (!lastHostileMobKillTime.containsKey(playerId)) {
                continue;
            }
            
            long lastKillTime = lastHostileMobKillTime.get(playerId);
            if (currentTime - lastKillTime > MOB_KILL_TIMEOUT) {
                // Player hasn't killed a hostile mob in 10 minutes
                // Reduce health by 2 hearts (4 health points)
                double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                double newMaxHealth = Math.max(2.0, currentMaxHealth - 4.0); // Minimum 1 heart
                
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
                
                // Ensure current health doesn't exceed max health
                if (player.getHealth() > newMaxHealth) {
                    player.setHealth(newMaxHealth);
                }
                
                player.sendMessage(MM.toComponent("&c¡No has matado a un mob hostil en 10 minutos! Pierdes 2 corazones."));
                
                // Reset the timer
                lastHostileMobKillTime.put(playerId, currentTime);
            }
        }
    }
    
    private void checkPlayerKillTimeout() {
        long currentTime = System.currentTimeMillis();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            
            // Skip players who joined after the event started
            if (!lastPlayerKillTime.containsKey(playerId)) {
                continue;
            }
            
            long lastKillTime = lastPlayerKillTime.get(playerId);
            if (currentTime - lastKillTime > PLAYER_KILL_TIMEOUT) {
                // Player hasn't killed another player in 1 hour
                // Reduce health by 5 hearts (10 health points)
                double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                double newMaxHealth = Math.max(2.0, currentMaxHealth - 10.0); // Minimum 1 heart
                
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
                
                // Ensure current health doesn't exceed max health
                if (player.getHealth() > newMaxHealth) {
                    player.setHealth(newMaxHealth);
                }
                
                player.sendMessage(MM.toComponent("&c¡No has matado a un jugador en 1 hora! Pierdes 5 corazones."));
                
                // Reset the timer
                lastPlayerKillTime.put(playerId, currentTime);
            }
        }
    }

        /**
     * Reduces a player's maximum health by the specified amount
     * @param player The player whose health to reduce
     * @param amount The amount to reduce (in health points, 2 = 1 heart)
     */
    private void reducePlayerHealth(Player player, double amount) {
        double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        double newMaxHealth = Math.max(2.0, currentMaxHealth - amount); // Minimum 1 heart
        
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
        
        // Ensure current health doesn't exceed max health
        if (player.getHealth() > newMaxHealth) {
            player.setHealth(newMaxHealth);
        }
    }
    
    private void checkArmorEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean hasHighTierArmor = false;
            
            // Check each armor piece
            ItemStack[] armor = player.getInventory().getArmorContents();
            for (ItemStack item : armor) {
                if (item != null && (
                    item.getType().name().contains("DIAMOND") || 
                    item.getType().name().contains("NETHERITE"))) {
                    hasHighTierArmor = true;
                    break;
                }
            }
            
            // Apply effects if wearing diamond or netherite armor
            if (hasHighTierArmor) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 1)); // Mining Fatigue II
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1)); // Slowness II
            }
        }
    }
    
    private void checkWeaponEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            
            // Check if holding diamond or netherite sword
            if (mainHand != null && (
                mainHand.getType() == Material.DIAMOND_SWORD || 
                mainHand.getType() == Material.NETHERITE_SWORD)) {
                
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0)); // Nausea I for 10 seconds
            }
        }
    }
    
    private void announceChallenges() {
        Bukkit.broadcast(MM.toComponent("<gray><b>=== <gold>DESAFÍOS DE LA SEMANA DE SANGRE Y HIERRO</gold> <gray><b>==="));
        Bukkit.broadcast(MM.toComponent("<yellow>1. <red>Mata</red> a <white>3</white> jugadores</yellow>"));
        Bukkit.broadcast(MM.toComponent("<gray>   <white>Recompensa:</white> Encantamiento <gold><u>Adrenaline</u></gold><gray>"));
        Bukkit.broadcast(MM.toComponent("<yellow>2. <red>Mata<red> a un jugador con poción de daño instantáneo"));
        Bukkit.broadcast(MM.toComponent("&7   Recompensa: +1 corazón permanente"));
        Bukkit.broadcast(MM.toComponent("<yellow>3. <red>Mata<red> a 5 jugadores seguidos sin morir"));
        Bukkit.broadcast(MM.toComponent("&7   Recompensa: Tag \"Pentakill\""));
        Bukkit.broadcast(MM.toComponent("<yellow>4. <green>Sobrevive<green> sin morir en todo el evento (con más de 10 kills)"));
        Bukkit.broadcast(MM.toComponent("&7   Recompensa: +1 corazón permanente"));
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isActive) return;
        
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        // Only process if killed by a player
        if (killer == null) return;
        
        // Check if the killed entity is a hostile mob
        if (isHostileMob(entity.getType())) {
            // Update last hostile mob kill time
            lastHostileMobKillTime.put(killer.getUniqueId(), System.currentTimeMillis());
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isActive) return;
        
        Player victim = event.getEntity();
        UUID victimId = victim.getUniqueId();
        
        // Mark player as dead for survival challenge
        deadPlayers.add(victimId);
        survivedPlayers.remove(victimId);
        
        // Reset consecutive kills
        consecutiveKills.put(victimId, 0);
        
        // Check if killed by another player
        if (victim.getKiller() != null) {
            Player killer = victim.getKiller();
            UUID killerId = killer.getUniqueId();
            
            // Update last player kill time
            lastPlayerKillTime.put(killerId, System.currentTimeMillis());
            
            // Increment kill counts
            int kills = playerKillCount.getOrDefault(killerId, 0) + 1;
            playerKillCount.put(killerId, kills);
            
            int consecutiveKillCount = consecutiveKills.getOrDefault(killerId, 0) + 1;
            consecutiveKills.put(killerId, consecutiveKillCount);
            
            // Check for kill challenges
            checkKillChallenges(killer, kills, consecutiveKillCount);
            
            // Check if killed by instant damage potion
            if (event.getEntity().getLastDamageCause() != null && 
                event.getEntity().getLastDamageCause().getCause() == DamageCause.MAGIC) {
                instantDamageKillers.add(killerId);
                awardInstantDamageKill(killer);
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isActive) return;
        
        // Initialize player data
        initializePlayer(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!isActive) return;
        
        // No special handling needed for now
    }
    
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (!isActive) return;
        
        // This will trigger a check for weapon effects on the next tick
    }
    
    private boolean isHostileMob(EntityType type) {
        switch (type) {
            case ZOMBIE:
            case SKELETON:
            case CREEPER:
            case SPIDER:
            case CAVE_SPIDER:
            case ENDERMAN:
            case WITCH:
            case BLAZE:
            case GHAST:
            case MAGMA_CUBE:
            case SLIME:
            case PHANTOM:
            case DROWNED:
            case HUSK:
            case STRAY:
            case PILLAGER:
            case RAVAGER:
            case VEX:
            case VINDICATOR:
            case EVOKER:
            case WITHER_SKELETON:
            case GUARDIAN:
            case ELDER_GUARDIAN:
            case SHULKER:
            case ENDERMITE:
            case SILVERFISH:
                return true;
            default:
                return false;
        }
    }
    
    private void checkKillChallenges(Player killer, int totalKills, int consecutiveKills) {
        UUID killerId = killer.getUniqueId();
        
        // Challenge 1: Kill 3 players
        if (totalKills >= 3 && !awardedAdrenaline.contains(killerId)) {
            awardAdrenalineEnchantment(killer);
        }
        
        // Challenge 3: Kill 5 players in a row without dying
        if (consecutiveKills >= 5 && !pentakillPlayers.contains(killerId)) {
            awardPentakillTag(killer);
        }
    }
    
    private void awardAdrenalineEnchantment(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (awardedAdrenaline.contains(playerId)) {
            return; // Already awarded
        }
        
        player.sendMessage(MM.toComponent("&a&l¡DESAFÍO COMPLETADO!"));
        player.sendMessage(MM.toComponent("&eHas matado a 3 jugadores."));
        player.sendMessage(MM.toComponent("&6Recompensa: Encantamiento Adrenaline"));
        
        awardedAdrenaline.add(playerId);
        
        // Announce to server
        Bukkit.broadcast(MM.toComponent("&6" + player.getName() + " &eha completado el desafío: &7Matar a 3 jugadores"));
    }
    
    private void awardInstantDamageKill(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (instantDamageKillers.contains(playerId)) {
            return; // Already awarded
        }
        
        player.sendMessage(MM.toComponent("&a&l¡DESAFÍO COMPLETADO!"));
        player.sendMessage(MM.toComponent("&eHas matado a un jugador con poción de daño instantáneo."));
        player.sendMessage(MM.toComponent("&6Recompensa: +1 corazón permanente"));
        
        // Add one heart to player's max health
        double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMaxHealth + 2.0);
        
        instantDamageKillers.add(playerId);
        
        // Announce to server
        Bukkit.broadcast(MM.toComponent("&6" + player.getName() + " &eha completado el desafío: &7Matar a un jugador con poción de daño instantáneo"));
    }
    
    private void awardPentakillTag(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (pentakillPlayers.contains(playerId)) {
            return; // Already awarded
        }
        
        player.sendMessage(MM.toComponent("&a&l¡DESAFÍO COMPLETADO!"));
        player.sendMessage(MM.toComponent("&eHas matado a 5 jugadores seguidos sin morir."));
        player.sendMessage(MM.toComponent("&6Recompensa: Tag \"Pentakill\""));
        
        // Award the tag using EternalTags
        //eternalAPI.setTag(player, new Tag("pentakill", "pentakill", "&x&d&d&0&0&c&7P&x&d&b&0&4&b&2e&x&d&8&0&9&9&en&x&d&6&0&d&8&9t&x&d&3&1&1&7&5a&x&d&1&1&5&6&0k&x&c&f&1&a&4&bi&x&c&c&1&e&3&7l&x&c&a&2&2&2&2l"));
        
        pentakillPlayers.add(playerId);
        
        // Announce to server
        Bukkit.broadcast(MM.toComponent("&6" + player.getName() + " &eha completado el desafío: &7Matar a 5 jugadores seguidos sin morir"));
    }
    
    private void awardSurvivalChallenge(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!survivedPlayers.contains(playerId) || playerKillCount.getOrDefault(playerId, 0) < 10) {
            return; // Didn't meet requirements
        }
        
        player.sendMessage(MM.toComponent("&a&l¡DESAFÍO COMPLETADO!"));
        player.sendMessage(MM.toComponent("&eHas sobrevivido todo el evento sin morir y con más de 10 kills."));
        player.sendMessage(MM.toComponent("&6Recompensa: +1 corazón permanente"));
        
        // Add one heart to player's max health
        double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMaxHealth + 2.0);
        
        // Announce to server
        Bukkit.broadcast(MM.toComponent("&6" + player.getName() + " &eha completado el desafío: &7Sobrevivir todo el evento sin morir con más de 10 kills"));
    }

    // Helper methods
    private void checkAndApplyArmorEffects(Player player) {
        // Check if player is wearing diamond/netherite armor
        boolean hasHeavyArmor = false;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null) {
                Material type = item.getType();
                if (type.name().contains("DIAMOND") || type.name().contains("NETHERITE")) {
                    hasHeavyArmor = true;
                    break;
                }
            }
        }
        
        if (hasHeavyArmor) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20 * 10, 1, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 10, 1, false, false, true));
        }
    }

    private void removeArmorEffects(Player player) {
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
    }

    private void checkAndApplySwordEffects(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null) {
            Material type = item.getType();
            if (type == Material.DIAMOND_SWORD || type == Material.NETHERITE_SWORD) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20 * 10, 0, false, false, true));
            }
        }
    }

    /**
     * Checks if a player has completed a specific challenge
     * @param playerId The UUID of the player
     * @param challengeId The ID of the challenge
     * @return true if the challenge is completed, false otherwise
     */
    public boolean hasChallengeCompleted(UUID playerId, String challengeId) {
        switch (challengeId) {
            case "adrenaline":
                return awardedAdrenaline.contains(playerId);
            case "pentakill":
                return pentakillPlayers.contains(playerId);
            case "instant_damage":
                return instantDamageKillers.contains(playerId);
            case "survival":
                return !deadPlayers.contains(playerId) && playerKillCount.getOrDefault(playerId, 0) >= 10;
            default:
                return false;
        }
    }
}