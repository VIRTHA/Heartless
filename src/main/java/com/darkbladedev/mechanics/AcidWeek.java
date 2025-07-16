package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.content.custom.CustomEnchantments;
import com.darkbladedev.utils.MM;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AcidWeek implements Listener {
    
    private final Plugin plugin;
    private final Set<UUID> playersInWater = new HashSet<>();
    private final Set<UUID> playersInRain = new HashSet<>();

    private BukkitTask acidTask;
    private BukkitTask weatherTask;
    private boolean isActive = false;
    private boolean isPaused = false;
    private long duration;
    private String prefix = "<b><gradient:#befd58:#c4fb54:#caf950:#d1f64b:#d7f447:#ddf243:#e3f03f:#e9ee3b:#f0eb36:#f6e932:#fce72e>Semana acida</gradient></b>";

    public AcidWeek(Plugin plugin, long duration) {
        this.plugin = plugin;
        this.duration = duration;
    }
    
    public void start() {
        if (isActive) return;
        
        isActive = true;
        isPaused = false;
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Start acid damage task
        startAcidDamageTask();
        
        // Start weather control task
        startWeatherControlTask();
        
        // Announce the start of the event
        Bukkit.broadcast(MM.toComponent(prefix + " <green>¡La lluvia ácida ha comenzado! Busca refugio y protege tu equipo."));
        
        // Schedule the end of the event
        new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        }.runTaskLater(plugin, duration);
    }
    

    
    public void stop() {
        if (!isActive) return;
        
        // Cancelar tareas
        if (acidTask != null) {
            acidTask.cancel();
            acidTask = null;
        }
        
        if (weatherTask != null) {
            weatherTask.cancel();
            weatherTask = null;
        }
        
        // Limpiar conjuntos
        playersInWater.clear();
        playersInRain.clear();
        
        // Restaurar clima normal en todos los mundos
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(false);
            world.setThundering(false);
        }
        
        isActive = false;
    }
    
    @SuppressWarnings("deprecation")
    private boolean hasAcidResistance(Player player) {
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasEnchant(Registry.ENCHANTMENT.get(new NamespacedKey(plugin, "acid_resistance")))) {

                return true;
            }
        }
        return false;
    }
    
    private void startAcidDamageTask() {
        acidTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Daño a jugadores en agua (cada 3 segundos)
                for (UUID uuid : playersInWater) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        applyAcidDamage(player, 2.0);
                    }
                }
                
                // Daño a jugadores bajo la lluvia (cada 5 segundos)
                if (this.getTaskId() % (5*20/60) == 0) { // Cada 5 segundos (si el task corre cada 3 ticks)
                    for (UUID uuid : playersInRain) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            applyAcidDamage(player, 2.0);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Ejecutar cada 1 segundos (20 ticks)
    }
    
    private void startWeatherControlTask() {
        weatherTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    long time = world.getTime();
                    
                    // Día: 0-12000, Noche: 12001-24000
                    if (time >= 0 && time < 12000) {
                        // Durante el día: siempre llueve
                        world.setStorm(true);
                        world.setThundering(true);
                    } else {
                        // Durante la noche: se calma
                        world.setStorm(false);
                        world.setThundering(false);
                    }
                }
                
                // Actualizar jugadores en lluvia
                updatePlayersInRain();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Verificar cada 1 segundos
    }
    
    private void updatePlayersInRain() {
        playersInRain.clear();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();
            
            // Verificar si está lloviendo en el mundo
            if (world.hasStorm()) {
                Location loc = player.getLocation();
                
                // Verificar si el jugador está expuesto al cielo
                if (world.getHighestBlockYAt(loc) <= loc.getBlockY() && !isPlayerUnderRoof(player)) {
                    playersInRain.add(player.getUniqueId());
                }
            }
        }
    }
    
    private boolean isPlayerUnderRoof(Player player) {
        Location loc = player.getLocation();
        
        // Verificar bloques por encima del jugador hasta el límite del mundo
        for (int y = loc.getBlockY() + 2; y < player.getWorld().getMaxHeight(); y++) {
            Block block = player.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            if (block.getType().isSolid()) {
                return true; // Hay un bloque sólido encima
            }
        }
        
        return false; // No hay techo
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isActive) return;
        
        Player player = event.getPlayer();
        Block block = player.getLocation().getBlock();
        
        // Verificar si el jugador está en agua
        if (block.getType() == Material.WATER) {
            playersInWater.add(player.getUniqueId());
        } else {
            playersInWater.remove(player.getUniqueId());
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isActive) return;
        
        // Verificar si es un jugador dañado por una poción arrojadiza
        if (event.getEntity() instanceof Player && event.getDamager() instanceof ThrownPotion) {
            Player player = (Player) event.getEntity();
            ThrownPotion potion = (ThrownPotion) event.getDamager();
            
            // Verificar si es una botella de agua (sin efectos)
            if (potion.getItem().getType() == Material.SPLASH_POTION && 
                potion.getEffects().isEmpty()) {
                
                // Verificar si el jugador tiene el encantamiento de resistencia al ácido
                if (!hasAcidResistance(player)) {
                    // 3 puntos = 1.5 corazones
                    player.damage(3.0); // Ignora armadura
                    event.setCancelled(true); // Cancelar el evento original
                } else {
                    event.setCancelled(true); // Cancelar el evento si tiene resistencia
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if (!isActive) return;
        
        ItemStack item = event.getItem();
        Material type = item.getType();
        
        // Verificar si es una herramienta o armadura
        if (isToolOrArmor(type)) {
            // Duplicar el daño (2x durabilidad)
            event.setDamage(event.getDamage() * 2);
        }
    }
    
    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (!isActive) return;
        
        Block block = event.getBlock();
        
        // Verificar si es un cultivo
        if (block.getBlockData() instanceof Ageable) {
            // Verificar si tiene techo
            if (!hasRoof(block)) {
                event.setCancelled(true); // Cancelar crecimiento
            }
        }
    }
    
    private boolean hasRoof(Block block) {
        // Verificar si hay bloques sólidos encima
        for (int y = block.getY() + 1; y < block.getWorld().getMaxHeight(); y++) {
            Block blockAbove = block.getWorld().getBlockAt(block.getX(), y, block.getZ());
            if (blockAbove.getType().isSolid()) {
                return true; // Hay un bloque sólido encima
            }
        }
        
        return false; // No hay techo
    }
    
    private boolean isToolOrArmor(Material material) {
        String name = material.name();
        return name.endsWith("_PICKAXE") || 
               name.endsWith("_AXE") || 
               name.endsWith("_SHOVEL") || 
               name.endsWith("_HOE") || 
               name.endsWith("_SWORD") || 
               name.endsWith("_HELMET") || 
               name.endsWith("_CHESTPLATE") || 
               name.endsWith("_LEGGINGS") || 
               name.endsWith("_BOOTS");
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void pause() {
        if (!isActive || isPaused) return;
        
        isPaused = true;
        
        // Cancel tasks
        if (acidTask != null) {
            acidTask.cancel();
            acidTask = null;
        }
        
        if (weatherTask != null) {
            weatherTask.cancel();
            weatherTask = null;
        }
        
        // Temporarily restore normal weather
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(false);
            world.setThundering(false);
        }
    }
    
    public void resume() {
        if (!isActive || !isPaused) return;
        
        isPaused = false;
        
        // Restart tasks
        startAcidDamageTask();
        startWeatherControlTask();
    }
    
    /**
     * Checks if a player has completed a specific challenge
     * @param playerId The UUID of the player
     * @param challengeId The ID of the challenge
     * @return true if the challenge is completed, false otherwise
     */
    public boolean hasChallengeCompleted(UUID playerId, String challengeId) {
        switch (challengeId) {
            case "acid_swimmer":
                // Player has survived in acid water for a certain amount of time
                return playersInWater.contains(playerId);
            case "acid_rain_survivor":
                // Player has survived in acid rain for a certain amount of time
                return playersInRain.contains(playerId);
            case "acid_resistant":
                // Player has obtained acid resistant gear
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    return hasAcidResistanceGear(player);
                }
                return false;
            default:
                return false;
        }
    }
    
    /**
     * Checks if a player has acid resistance gear
     * @param player The player to check
     * @return true if the player has acid resistance gear, false otherwise
     */
    private boolean hasAcidResistanceGear(Player player) {
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.hasItemMeta() && HeartlessMain.getContentManager().hasEnchantment(item, CustomEnchantments.ENCHANTMENTS.ACID_RESISTANCE.toEnchantment())) {
                return true;
            }
        }
        return false;
    }
    
    // Missing method implementation
    @SuppressWarnings("unused")
    private boolean isExposedToRain(Player player, World world) {
        if (!world.hasStorm()) {
            return false;
        }
        
        Location loc = player.getLocation();
        
        // Check if player is exposed to the sky
        if (world.getHighestBlockYAt(loc) <= loc.getBlockY() && !isPlayerUnderRoof(player)) {
            return true;
        }
        
        return false;
    }
    
    private void applyAcidDamage(Player player, double damage) {
        // Check if player has acid resistance
        if (!hasAcidResistance(player)) {
            player.damage(damage);
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0, false, true, true));
        }
    }
}