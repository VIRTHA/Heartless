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
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.content.custom.CustomEnchantments;
import com.darkbladedev.utils.BiomeUtils;
import com.darkbladedev.utils.MM;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AcidWeek extends WeeklyEvent {
    
    private final Set<UUID> playersInWater = new HashSet<>();
    private final Set<UUID> playersInRain = new HashSet<>();

    private BukkitTask acidTask;
    private BukkitTask weatherTask;

    public AcidWeek(HeartlessMain plugin, long duration) {
        super(plugin, duration);
        this.prefix = "<b><gradient:#befd58:#c4fb54:#caf950:#d1f64b:#d7f447:#ddf243:#e3f03f:#e9ee3b:#f0eb36:#f6e932:#fce72e>Semana acida</gradient></b>";
    }
    
    @Override
    protected void startEventTasks() {
        // Start acid damage task
        startAcidDamageTask();
        
        // Start weather control task
        startWeatherControlTask();
    }
    
    @Override
    protected void announceEventStart() {
        Bukkit.broadcast(MM.toComponent(prefix + " <green>¡La lluvia ácida ha comenzado! Busca refugio y protege tu equipo."));
    }
    

    
    @Override
    protected void stopEventTasks() {
        // Cancelar tareas
        if (acidTask != null) {
            acidTask.cancel();
            acidTask = null;
        }
        
        if (weatherTask != null) {
            weatherTask.cancel();
            weatherTask = null;
        }
        
        // Restaurar clima normal en todos los mundos
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(false);
            world.setThundering(false);
        }
    }
    
    @Override
    protected void cleanupEventData() {
        // Limpiar conjuntos
        playersInWater.clear();
        playersInRain.clear();
    }
    
    @Override
    public String getName() {
        return "Semana Ácida";
    }
    
    @Override
    protected void pauseEventTasks() {
        if (acidTask != null) {
            acidTask.cancel();
            acidTask = null;
        }
        
        if (weatherTask != null) {
            weatherTask.cancel();
            weatherTask = null;
        }
    }
    
    @Override
    protected void resumeEventTasks() {
        if (isActive && isPaused) {
            startAcidDamageTask();
            startWeatherControlTask();
        }
    }
    
    /**
     * Verifica si un jugador tiene resistencia al ácido
     * @param player El jugador a verificar
     * @return true si el jugador tiene resistencia al ácido, false en caso contrario
     */
    @SuppressWarnings("deprecation")
    private boolean hasAcidResistance(Player player) {
        if (player == null || !player.isOnline()) {
            return false; // Verificación de nulidad
        }
        
        // Verificar si el jugador tiene el encantamiento en alguna pieza de armadura
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
                // Verificar usando Registry.ENCHANTMENT (método deprecado pero funcional)
                if (item.getItemMeta().hasEnchant(Registry.ENCHANTMENT.get(new NamespacedKey(plugin, "acid_resistance")))) {
                    return true;
                }
                
                // Verificar usando el ContentManager (método alternativo)
                if (HeartlessMain.getContentManager().hasEnchantment(item, CustomEnchantments.ENCHANTMENTS.ACID_RESISTANCE.toEnchantment())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void startAcidDamageTask() {
        // Cancelar tarea existente si hay
        if (acidTask != null) {
            acidTask.cancel();
            acidTask = null;
        }
        
        acidTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || isPaused) return;
                
                // Limpiar UUIDs de jugadores que ya no están en línea para evitar memory leaks
                playersInWater.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
                
                // Daño a jugadores en agua (con límite de procesamiento)
                int processedPlayers = 0;
                int maxPlayersPerTick = 30; // Limitar procesamiento para evitar lag
                
                for (UUID uuid : playersInWater) {
                    if (processedPlayers >= maxPlayersPerTick) break;
                    
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        applyAcidDamage(player, 2.0);
                        processedPlayers++;
                    }
                }
                
                // Daño a jugadores bajo la lluvia (cada 5 segundos)
                if (this.getTaskId() % (5*20/60) == 0) { // Cada 5 segundos (si el task corre cada 3 ticks)
                    for (UUID uuid : playersInRain) {
                        if (processedPlayers >= maxPlayersPerTick) break;
                        
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            applyAcidDamage(player, 2.0);
                            processedPlayers++;
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
        
        // Limitar procesamiento para evitar lag
        int processedPlayers = 0;
        int maxPlayersPerTick = 30;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (processedPlayers >= maxPlayersPerTick) break;
            
            if (player == null || !player.isOnline()) continue;
            
            // Usar el método mejorado isExposedToRain
            if (isExposedToRain(player)) {
                playersInRain.add(player.getUniqueId());
            }
            
            processedPlayers++;
        }
    }
    
    private boolean isPlayerUnderRoof(Player player) {
        if (player == null || player.getLocation() == null || player.getWorld() == null) {
            return false; // Verificación de nulidad
        }
        
        Location loc = player.getLocation();
        World world = player.getWorld();
        
        // Limitar la búsqueda a 20 bloques por encima del jugador para mejorar rendimiento
        int maxCheckHeight = Math.min(loc.getBlockY() + 20, world.getMaxHeight());
        
        // Verificar bloques por encima del jugador
        for (int y = loc.getBlockY() + 2; y < maxCheckHeight; y++) {
            Block block = world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            if (block != null && block.getType().isSolid()) {
                return true; // Hay un bloque sólido encima
            }
        }
        
        return false; // No hay techo en el rango verificado
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isActive || isPaused) return;
        
        Player player = event.getPlayer();
        if (player == null) return; // Verificación de nulidad
        
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
        if (!isActive || isPaused) return;
        
        // Verificar si es un jugador dañado por una poción arrojadiza
        if (event.getEntity() instanceof Player && event.getDamager() instanceof ThrownPotion) {
            Player player = (Player) event.getEntity();
            ThrownPotion potion = (ThrownPotion) event.getDamager();
            
            if (potion == null || potion.getItem() == null) return; // Verificación de nulidad
            
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
        if (!isActive || isPaused) return;
        
        ItemStack item = event.getItem();
        if (item == null) return; // Verificación de nulidad
        
        Material type = item.getType();
        
        // Verificar si es una herramienta o armadura
        if (isToolOrArmor(type)) {
            // Duplicar el daño (2x durabilidad)
            event.setDamage(event.getDamage() * 2);
        }
    }
    
    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (!isActive || isPaused) return;
        
        Block block = event.getBlock();
        if (block == null) return; // Verificación de nulidad
        
        // Verificar si es un cultivo
        if (block.getBlockData() instanceof Ageable) {
            // Verificar si tiene techo
            if (!hasRoof(block)) {
                event.setCancelled(true); // Cancelar crecimiento
            }
        }
    }
    
    private boolean hasRoof(Block block) {
        if (block == null || block.getWorld() == null) {
            return false; // Verificación de nulidad
        }
        
        World world = block.getWorld();
        
        // Limitar la búsqueda a 20 bloques por encima para mejorar rendimiento
        int maxCheckHeight = Math.min(block.getY() + 20, world.getMaxHeight());
        
        // Verificar bloques por encima
        for (int y = block.getY() + 1; y < maxCheckHeight; y++) {
            Block blockAbove = world.getBlockAt(block.getX(), y, block.getZ());
            if (blockAbove != null && blockAbove.getType().isSolid()) {
                return true; // Hay un bloque sólido encima
            }
        }
        
        return false; // No hay techo en el rango verificado
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
     * Verifica si un jugador ha completado un desafío específico
     * @param playerId El UUID del jugador
     * @param challengeId El ID del desafío
     * @return true si el desafío está completado, false en caso contrario
     */
    public boolean hasChallengeCompleted(UUID playerId, String challengeId) {
        if (playerId == null || challengeId == null || challengeId.isEmpty()) {
            return false; // Verificación de nulidad
        }
        
        switch (challengeId) {
            case "acid_swimmer":
                // Jugador ha sobrevivido en agua ácida por cierto tiempo
                return playersInWater.contains(playerId);
                
            case "acid_rain_survivor":
                // Jugador ha sobrevivido bajo la lluvia ácida por cierto tiempo
                return playersInRain.contains(playerId);
                
            case "acid_resistant":
                // Jugador ha obtenido equipo con resistencia al ácido
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
     * Verifica si un jugador tiene equipo con resistencia al ácido
     * @param player El jugador a verificar
     * @return true si el jugador tiene equipo con resistencia al ácido, false en caso contrario
     */
    private boolean hasAcidResistanceGear(Player player) {
        if (player == null || !player.isOnline()) {
            return false; // Verificación de nulidad
        }
        
        // Verificar si el jugador tiene el encantamiento en alguna pieza de armadura
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
                if (HeartlessMain.getContentManager().hasEnchantment(item, CustomEnchantments.ENCHANTMENTS.ACID_RESISTANCE.toEnchantment())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if a player is exposed to rain
     * @param player The player to check
     * @return true if the player is exposed to rain, false otherwise
     */
    private boolean isExposedToRain(Player player) {
        if (player == null || player.getLocation() == null || player.getWorld() == null) {
            return false; // Verificación de nulidad
        }
        
        World world = player.getWorld();
        
        if (!world.hasStorm()) {
            return false;
        }
        
        Location loc = player.getLocation();
        
        // Verificar si el jugador está en un bioma donde llueve
        if (!BiomeUtils.canRain(loc.getBlock().getBiome())) {
            return false;
        }
        
        // Verificar si el jugador está expuesto al cielo
        if (world.getHighestBlockYAt(loc) <= loc.getBlockY() && !isPlayerUnderRoof(player)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Aplica daño por ácido a un jugador
     * @param player El jugador al que aplicar daño
     * @param damage La cantidad de daño a aplicar
     */
    private void applyAcidDamage(Player player, double damage) {
        if (player == null || !player.isOnline() || player.isDead()) {
            return; // Verificación de nulidad y estado del jugador
        }
        
        // Verificar si el jugador tiene resistencia al ácido
        if (!hasAcidResistance(player)) {
            // Aplicar daño y efecto de veneno
            player.damage(damage);
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0, false, true, true));
            
            // Mensaje de efecto (con límite de frecuencia para no spamear)
            if (Math.random() < 0.2) { // 20% de probabilidad para reducir spam
                player.sendMessage(MM.toComponent(prefix + " <red>¡El ácido está dañando tu piel!"));
            }
        }
    }
}