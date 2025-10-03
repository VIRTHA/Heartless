package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.challenges.Reward;
import com.darkbladedev.content.custom.CustomEnchantments;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.TimeExpression;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.UUID;
import java.util.Map;
import java.util.Set;

/**
 * Evento semanal: Semana Ácida
 * 
 * Durante esta semana, el agua y la lluvia se vuelven ácidas, causando daño a los jugadores.
 * Los jugadores deben adaptarse usando equipo especial y completar desafíos únicos.
 * 
 * Características principales:
 * - Lluvia constante que causa daño ácido
 * - Agua convertida en ácido que daña a los jugadores
 * - Desafíos especiales relacionados con supervivencia ácida
 * - Recompensas únicas por completar desafíos
 * 
 * Desafíos disponibles:
 * - Nadador Ácido: Sobrevivir en agua ácida por 5 minutos
 * - Superviviente de Lluvia Ácida: Sobrevivir bajo lluvia ácida por 90 segundos
 * - Resistente al Ácido: Completar desafíos sin tomar daño ácido
 * - Coleccionista de Pescados: Pescar todos los tipos de peces durante la semana
 * - Domador de Ajolotes Azules: Domesticar un ajolote azul
 * 
 * @author DarkBladeDev
 * @version 2.0
 * @since 1.0
 */
public class AcidWeek extends AbstractWeeklyEvent {
    
    // Conjuntos thread-safe para rastrear jugadores
    private final Set<UUID> playersInWater = ConcurrentHashMap.newKeySet();
    private final Set<UUID> playersInRain = ConcurrentHashMap.newKeySet();
    
    // Mapa para rastrear el último atacante con botella de agua de cada jugador
    private final ConcurrentHashMap<UUID, UUID> lastWaterBottleAttacker = new ConcurrentHashMap<>();
    // Mapa para rastrear el tiempo del último ataque con botella de agua (para evitar falsos positivos)
    private final ConcurrentHashMap<UUID, Long> lastWaterBottleAttackTime = new ConcurrentHashMap<>();
    
    // Mapas para rastrear progreso de desafíos
    private final ConcurrentHashMap<UUID, Set<Material>> fishCollected = new ConcurrentHashMap<>();
    private final Set<UUID> acidRainSurvivors = ConcurrentHashMap.newKeySet();
    private final Set<UUID> waterBottleKillers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> blueAxolotlOwners = ConcurrentHashMap.newKeySet();
    
    // Timer para supervivencia bajo lluvia ácida (1.5 minutos = 90 segundos)
    private final ConcurrentHashMap<UUID, Long> rainSurvivalStartTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> rainSurvivalTime = new ConcurrentHashMap<>();
    private final AtomicReference<BukkitTask> rainSurvivalTask = new AtomicReference<>();
    
    // Tipos de pescado requeridos para el desafío
    private static final Set<Material> REQUIRED_FISH = Set.of(
        Material.COD,
        Material.SALMON, 
        Material.TROPICAL_FISH,
        Material.PUFFERFISH
    );
    
    // Estadísticas de daño ácido
    private final AtomicInteger totalAcidDamageDealt = new AtomicInteger(0);
    private final ConcurrentHashMap<UUID, AtomicInteger> playerAcidDamage = new ConcurrentHashMap<>();
    
    // Referencias a tareas programadas
    private final AtomicReference<BukkitTask> rainDamageTask = new AtomicReference<>();
    private final AtomicReference<BukkitTask> waterDamageTask = new AtomicReference<>();
    private final AtomicReference<BukkitTask> weatherTask = new AtomicReference<>();
    
    // Estado de limpieza
    private final AtomicBoolean isCleaningUp = new AtomicBoolean(false);
    
    // Constantes de configuración
    @SuppressWarnings("unused")
    private static final int MAX_PLAYERS_PER_TICK = 30;
    private static final int MAX_ROOF_CHECK_HEIGHT = 20;
    private static final double ACID_DAMAGE_AMOUNT = 3.0;
    private static final double RESISTANCE_DAMAGE_REDUCTION = 0.5;
    private static final long RAIN_DAMAGE_INTERVAL_TICKS = 100L; // 5 segundos
    private static final long WATER_DAMAGE_INTERVAL_TICKS = 60L; // 3 segundos

    public AcidWeek(HeartlessMain plugin, TimeExpression duration) {
        super(plugin, duration);
        this.prefix = "<b><gradient:#befd58:#c4fb54:#caf950:#d1f64b:#d7f447:#ddf243:#e3f03f:#e9ee3b:#f0eb36:#f6e932:#fce72e>Semana acida</gradient></b>";
    }

    @Override
    protected void startEventTasks() {
        try {
            startRainDamageTask();
            startWaterDamageTask();
            startWeatherControlTask();
            startRainSurvivalTask();
            
            plugin.getLogger().info("[AcidWeek] Tareas del evento iniciadas correctamente");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al iniciar tareas del evento", e);
        }
    }
    
    @Override
    protected void announceEventStart() {
        try {
            Bukkit.broadcast(MM.toComponent(prefix + " <yellow>¡La Semana Ácida ha comenzado!"));
            announceAcidChallenges();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al anunciar inicio del evento", e);
        }
    }
    
    @Override
    protected void announceEventEnd() {
        try {
            Bukkit.broadcast(MM.toComponent(prefix + " <green>¡La Semana Ácida ha terminado!"));
            
            // Mostrar estadísticas finales
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != null && player.isOnline()) {
                    try {
                        sendPlayerStatistics(player);
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, 
                            "[AcidWeek] Error al enviar estadísticas a " + player.getName(), e);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al anunciar fin del evento", e);
        }
    }
    
    /**
     * Envía las estadísticas personales del jugador al final del evento
     */
    private void sendPlayerStatistics(Player player) {
        try {
            UUID playerId = player.getUniqueId();
            
            // Separador visual
            player.sendMessage(MM.toComponent("<gray><b>═══════════════════════════════════</b></gray>"));
            player.sendMessage(MM.toComponent("<green><b>TUS ESTADÍSTICAS - SEMANA ÁCIDA</b></green>"));
            player.sendMessage(MM.toComponent("<gray><b>═══════════════════════════════════</b></gray>"));
            
            // Daño ácido recibido
            AtomicInteger damage = playerAcidDamage.get(playerId);
            int totalDamage = damage != null ? damage.get() : 0;
            player.sendMessage(MM.toComponent("<yellow>Daño ácido recibido: <white>" + totalDamage));
            
            // Desafíos completados
            int completedChallenges = 0;
            String[] challengeIds = {"acid_rain_survivor", "chemical_killer", "fish_collector", "blue_axolotl"};
            String[] challengeNames = {"Superviviente de Lluvia", "Asesino Quimico", "Coleccionista de Pescados", "Domador de Ajolotes"};
            
            for (int i = 0; i < challengeIds.length; i++) {
                if (hasChallengeCompleted(playerId, challengeIds[i])) {
                    completedChallenges++;
                    player.sendMessage(MM.toComponent("<green>✓ " + challengeNames[i]));
                } else {
                    player.sendMessage(MM.toComponent("<red>✗ " + challengeNames[i]));
                }
            }
            
            player.sendMessage(MM.toComponent("<gold>Desafíos completados: <white>" + completedChallenges + "/5"));
            
            // Pescados recolectados
            Set<Material> playerFish = fishCollected.get(playerId);
            int fishCount = playerFish != null ? playerFish.size() : 0;
            player.sendMessage(MM.toComponent(prefix + " <blue>Tipos de pescado recolectados: <white>" + fishCount + "/4"));
            
            player.sendMessage(MM.toComponent("<gold>═══════════════════════════════════"));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al generar estadísticas para " + player.getName(), e);
        }
    }
    
    /**
     * Anuncia los desafíos disponibles durante la semana ácida
     */
    private void announceAcidChallenges() {
        try {
            Bukkit.broadcast(MM.toComponent("<yellow>Desafíos disponibles:"));
            Bukkit.broadcast(MM.toComponent("<green>•  Nadador Ácido - Sobrevive en agua ácida"));
            Bukkit.broadcast(MM.toComponent("<green>•  Superviviente de Lluvia - Resiste la lluvia ácida"));
            Bukkit.broadcast(MM.toComponent("<green>•  Coleccionista de Pescados - Pesca todos los tipos"));
            Bukkit.broadcast(MM.toComponent("<green>•  Domador de Ajolotes - Domestica un ajolote azul"));
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al anunciar desafíos", e);
        }
    }
    
    @Override
    protected void stopEventTasks() {
        try {
            // Cancelar tarea de daño por lluvia
            BukkitTask rainTaskRef = rainDamageTask.get();
            if (rainTaskRef != null && !rainTaskRef.isCancelled()) {
                rainTaskRef.cancel();
            }
            
            // Cancelar tarea de daño por agua
            BukkitTask waterTaskRef = waterDamageTask.get();
            if (waterTaskRef != null && !waterTaskRef.isCancelled()) {
                waterTaskRef.cancel();
            }
            
            // Cancelar tarea de control climático
            BukkitTask weatherTaskRef = weatherTask.get();
            if (weatherTaskRef != null && !weatherTaskRef.isCancelled()) {
                weatherTaskRef.cancel();
            }
            
            restoreNormalWeather();
            plugin.getLogger().info("[AcidWeek] Tareas del evento detenidas correctamente");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al detener tareas del evento", e);
        }
    }
    
    /**
     * Restaura el clima normal en todos los mundos
     */
    private void restoreNormalWeather() {
        try {
            for (World world : Bukkit.getWorlds()) {
                if (world != null) {
                    world.setStorm(false);
                    world.setThundering(false);
                    world.setWeatherDuration(0);
                }
            }
            plugin.getLogger().info("[AcidWeek] Clima normal restaurado en todos los mundos");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al restaurar clima normal", e);
        }
    }
    
    @Override
    protected void cleanupEventData() {
        try {
            isCleaningUp.set(true);
            
            playersInWater.clear();
            playersInRain.clear();
            lastWaterBottleAttacker.clear();
            lastWaterBottleAttackTime.clear();
            fishCollected.clear();
            acidRainSurvivors.clear();
            waterBottleKillers.clear();
            blueAxolotlOwners.clear();
            playerAcidDamage.clear();
            totalAcidDamageDealt.set(0);
            
            plugin.getLogger().info("[AcidWeek] Datos del evento limpiados correctamente");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al limpiar datos del evento", e);
        } finally {
            isCleaningUp.set(false);
        }
    }
    
    /**
     * Fuerza la limpieza de todos los datos y tareas
     */
    @SuppressWarnings("unused")
    private void forceCleanup() {
        try {
            // Cancelar todas las tareas
            BukkitTask rainTaskRef = rainDamageTask.getAndSet(null);
            if (rainTaskRef != null && !rainTaskRef.isCancelled()) {
                rainTaskRef.cancel();
            }
            
            BukkitTask waterTaskRef = waterDamageTask.getAndSet(null);
            if (waterTaskRef != null && !waterTaskRef.isCancelled()) {
                waterTaskRef.cancel();
            }
            
            BukkitTask weatherTaskRef = weatherTask.getAndSet(null);
            if (weatherTaskRef != null && !weatherTaskRef.isCancelled()) {
                weatherTaskRef.cancel();
            }
            
            cleanupEventData();
            restoreNormalWeather();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error en limpieza forzada", e);
        }
    }
    
    @Override
    public String getId() {
        return "acid_week";
    }
    
    @Override
    protected void pauseEventTasks() {
        try {
            BukkitTask rainTaskRef = rainDamageTask.get();
            if (rainTaskRef != null && !rainTaskRef.isCancelled()) {
                rainTaskRef.cancel();
            }
            
            BukkitTask waterTaskRef = waterDamageTask.get();
            if (waterTaskRef != null && !waterTaskRef.isCancelled()) {
                waterTaskRef.cancel();
            }
            
            BukkitTask weatherTaskRef = weatherTask.get();
            if (weatherTaskRef != null && !weatherTaskRef.isCancelled()) {
                weatherTaskRef.cancel();
            }
            
            plugin.getLogger().info("[AcidWeek] Tareas del evento pausadas");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al pausar tareas del evento", e);
        }
    }
    
    @Override
    protected void resumeEventTasks() {
        try {
            if (!isPaused.get()) {
                startRainDamageTask();
                startWaterDamageTask();
                startWeatherControlTask();
                plugin.getLogger().info("[AcidWeek] Tareas del evento reanudadas");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al reanudar tareas del evento", e);
        }
    }
    
    /**
     * Verifica si un jugador tiene resistencia al ácido
     */
    private boolean hasAcidResistance(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        try {
            // Verificar efectos de poción
            if (player.hasPotionEffect(PotionEffectType.RESISTANCE)) {
                return true;
            }
            
            // Verificar armadura con encantamientos personalizados
            ItemStack[] armor = player.getInventory().getArmorContents();
            if (armor != null) {
                for (ItemStack piece : armor) {
                    if (piece != null && piece.hasItemMeta()) {
                        try {
                                Enchantment enchantment = HeartlessMain.getContentManager().getEnchantment(CustomEnchantments.ACID_RESISTANCE_KEY);
                                if (enchantment != null && piece.getItemMeta().hasEnchant(enchantment)) {
                                    return true;
                            }
                        } catch (Exception e) {
                            // Continuar verificando otras piezas si hay error
                        }
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al verificar resistencia al ácido para " + player.getName(), e);
            return false;
        }
    }
    
    /**
     * Inicia la tarea de daño ácido que se ejecuta periódicamente
     */
    /**
     * Inicia la tarea de daño por lluvia ácida (cada 5 segundos)
     */
    private void startRainDamageTask() {
        try {
            // Cancelar tarea existente si hay
            BukkitTask existingTask = rainDamageTask.get();
            if (existingTask != null && !existingTask.isCancelled()) {
                existingTask.cancel();
            }
            
            BukkitTask newTask = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (!isActive.get() || isPaused.get() || isCleaningUp.get()) {
                            return;
                        }
                        
                        cleanupDisconnectedPlayers();
                        processRainDamage();
                        
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en tarea de daño por lluvia", e);
                    }
                }
            }.runTaskTimer(plugin, 0L, RAIN_DAMAGE_INTERVAL_TICKS);
            
            rainDamageTask.set(newTask);
            plugin.getLogger().info("[AcidWeek] Tarea de daño por lluvia iniciada (cada 5 segundos)");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al iniciar tarea de daño por lluvia", e);
        }
    }
    
    /**
     * Inicia la tarea de daño por agua ácida (cada 3 segundos)
     */
    private void startWaterDamageTask() {
        try {
            // Cancelar tarea existente si hay
            BukkitTask existingTask = waterDamageTask.get();
            if (existingTask != null && !existingTask.isCancelled()) {
                existingTask.cancel();
            }
            
            BukkitTask newTask = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (!isActive.get() || isPaused.get() || isCleaningUp.get()) {
                            return;
                        }
                        
                        processWaterDamage();
                        
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en tarea de daño por agua", e);
                    }
                }
            }.runTaskTimer(plugin, 0L, WATER_DAMAGE_INTERVAL_TICKS);
            
            waterDamageTask.set(newTask);
            plugin.getLogger().info("[AcidWeek] Tarea de daño por agua iniciada (cada 3 segundos)");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al iniciar tarea de daño por agua", e);
        }
    }
    
    /**
     * Limpia jugadores desconectados de las listas de seguimiento
     */
    private void cleanupDisconnectedPlayers() {
        try {
            playersInWater.removeIf(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                return player == null || !player.isOnline();
            });
            
            playersInRain.removeIf(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                return player == null || !player.isOnline();
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al limpiar jugadores desconectados", e);
        }
    }
    
    /**
     * Procesa el daño por agua ácida
     */
    private void processWaterDamage() {
        try {
            Set<UUID> playersToRemove = new HashSet<>();
            
            for (UUID playerId : playersInWater) {
                Player player = Bukkit.getPlayer(playerId);
                if (player == null || !player.isOnline()) {
                    playersToRemove.add(playerId);
                    continue;
                }
                
                try {
                    Location loc = player.getLocation();
                    Block block = loc.getBlock();
                    
                    // Verificar si el jugador sigue en agua
                    if (block.getType() != Material.WATER) {
                        playersToRemove.add(playerId);
                        continue;
                    }
                    
                    // Aplicar daño ácido
                    applyAcidDamage(player, "agua");
                    
                    // Verificar desafío de nadador ácido
                    checkAcidSwimmerChallenge(player);
                    
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, 
                        "[AcidWeek] Error al procesar daño de agua para " + player.getName(), e);
                }
            }
            
            // Remover jugadores que ya no están en agua
            playersInWater.removeAll(playersToRemove);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al procesar daño de agua", e);
        }
    }
    
    /**
     * Procesa el daño por lluvia ácida
     */
    private void processRainDamage() {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player == null || !player.isOnline()) {
                    continue;
                }
                
                try {
                    if (isPlayerExposedToRain(player)) {
                        UUID playerId = player.getUniqueId();
                        
                        if (!playersInRain.contains(playerId)) {
                            playersInRain.add(playerId);
                            player.sendActionBar(MM.toComponent(prefix + " <yellow>¡Estás expuesto a la lluvia ácida!"));
                        }
                        
                        applyAcidDamage(player, "lluvia");
                    } else {
                        playersInRain.remove(player.getUniqueId());
                    }
                    
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, 
                        "[AcidWeek] Error al procesar lluvia para " + player.getName(), e);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al procesar daño de lluvia", e);
        }
    }
    
    /**
     * Verifica si un jugador está expuesto a la lluvia
     */
    private boolean isPlayerExposedToRain(Player player) {
        try {
            World world = player.getWorld();
            if (!world.hasStorm()) {
                return false;
            }
            
            Location loc = player.getLocation();
            
            // Verificar si el jugador está bajo el agua
            Block playerBlock = world.getBlockAt(loc);
            if (playerBlock.getType() == Material.WATER || 
                playerBlock.getType() == Material.KELP ||
                playerBlock.getType() == Material.KELP_PLANT ||
                playerBlock.getType() == Material.SEAGRASS ||
                playerBlock.getType() == Material.TALL_SEAGRASS) {
                return false; // Jugador está bajo el agua
            }
            
            // Verificar si hay bloques sólidos encima del jugador
            for (int y = 1; y <= MAX_ROOF_CHECK_HEIGHT; y++) {
                Block block = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + y, loc.getBlockZ());
                if (block.getType().isSolid()) {
                    return false; // Hay techo
                }
            }
            
            return true; // Expuesto a la lluvia
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al verificar exposición a lluvia para " + player.getName(), e);
            return false;
        }
    }
    
    /**
     * Aplica daño ácido a un jugador
     */
    private void applyAcidDamage(Player player, String source) {
        try {
            if (player == null || !player.isOnline()) {
                return;
            }
            
            double damage = ACID_DAMAGE_AMOUNT;
            
            // Reducir daño si tiene resistencia
            if (hasAcidResistance(player)) {
                damage *= RESISTANCE_DAMAGE_REDUCTION;
                player.sendActionBar(MM.toComponent(prefix + " <green>Tu resistencia reduce el daño ácido"));
            }
            
            // Aplicar daño
            player.damage(damage);
            
            // Aplicar daño a la armadura
            applyArmorDamage(player);
            
            // Registrar estadísticas
            UUID playerId = player.getUniqueId();
            playerAcidDamage.computeIfAbsent(playerId, k -> new AtomicInteger(0)).addAndGet((int) damage);
            totalAcidDamageDealt.addAndGet((int) damage);
            
            // Mensaje de daño
            player.sendActionBar(MM.toComponent(prefix + " <red>¡El ácido de " + source + " te está dañando!"));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al aplicar daño ácido a " + player.getName(), e);
        }
    }
    
    /**
     * Aplica daño directo a las piezas de armadura del jugador
     */
    private void applyArmorDamage(Player player) {
        try {
            if (player == null || !player.isOnline()) {
                return;
            }
            
            PlayerInventory inventory = player.getInventory();
            ItemStack[] armorContents = inventory.getArmorContents();
            
            if (armorContents != null) {
                boolean armorDamaged = false;
                
                for (int i = 0; i < armorContents.length; i++) {
                    ItemStack armorPiece = armorContents[i];
                    
                    if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                        // Verificar si la pieza tiene resistencia al ácido
                        boolean hasResistance = false;
                        try {
                            if (armorPiece.hasItemMeta()) {
                                Enchantment enchantment = HeartlessMain.getContentManager().getEnchantment(CustomEnchantments.ACID_RESISTANCE_KEY);
                                hasResistance = enchantment != null && armorPiece.getItemMeta().hasEnchant(enchantment);
                            }
                        } catch (Exception e) {
                            // Continuar sin resistencia si hay error
                        }
                        
                        if (!hasResistance) {
                            // Aplicar daño doble a la armadura
                            ItemMeta meta = armorPiece.getItemMeta();
                            if (meta instanceof Damageable) {
                                Damageable damageable = (Damageable) meta;
                                int currentDamage = damageable.getDamage();
                                int maxDurability = armorPiece.getType().getMaxDurability();
                                
                                // Aplicar 2 puntos de daño (doble del normal)
                                int newDamage = currentDamage + 2;
                                
                                if (newDamage >= maxDurability) {
                                    // La armadura se rompe
                                    armorContents[i] = null;
                                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                                } else {
                                    // Aplicar el daño
                                    damageable.setDamage(newDamage);
                                    armorPiece.setItemMeta(meta);
                                    armorContents[i] = armorPiece;
                                }
                                
                                armorDamaged = true;
                            }
                        }
                    }
                }
                
                // Actualizar el inventario con las armaduras dañadas
                inventory.setArmorContents(armorContents);
                
                // Mensaje si se dañó alguna armadura
                if (armorDamaged) {
                    player.sendActionBar(MM.toComponent(prefix + " <red>¡El ácido está corroyendo tu armadura!"));
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al aplicar daño a armadura de " + player.getName(), e);
        }
    }
    
    /**
     * Verifica y completa el desafío de nadador ácido
     */
    private void checkAcidSwimmerChallenge(Player player) {
        try {
            UUID playerId = player.getUniqueId();
            if (!hasChallengeCompleted(playerId, "acid_swimmer")) {
                // Lógica para verificar tiempo en agua (simplificada)
                completeChallengeForPlayer(playerId, "acid_swimmer");
                player.sendActionBar(MM.toComponent(prefix + " <gold>¡Desafío completado: Nadador Ácido!"));
                player.sendActionBar(MM.toComponent("  <gold>>></gold> <green>Recompensa: +1 corazón extra"));
                
                // Dar recompensa de corazón permanente usando Reward
                Reward reward = new Reward("health:2");
                reward.grantTo(player, prefix);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al verificar desafío de nadador ácido para " + player.getName(), e);
        }
    }
    
    /**
     * Verifica y completa el desafío de superviviente de lluvia ácida
     */
    private void checkAcidRainSurvivorChallenge(Player player) {
        try {
            UUID playerId = player.getUniqueId();
            if (!hasChallengeCompleted(playerId, "acid_rain_survivor")) {
                // Actualizar el progreso a 90/90 antes de completar el desafío
                updateChallengeProgress(playerId, "acid_rain_survivor", 90, 90);
                completeChallengeForPlayer(playerId, "acid_rain_survivor");
                
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al verificar desafío de superviviente de lluvia ácida para " + player.getName(), e);
        }
    }
    
    /**
     * Inicia la tarea de control climático
     * Según la documentación: lluvia durante el día, calma durante la noche
     */
    private void startWeatherControlTask() {
        try {
            // Cancelar tarea existente si hay
            BukkitTask existingTask = weatherTask.get();
            if (existingTask != null && !existingTask.isCancelled()) {
                existingTask.cancel();
            }
            
            BukkitTask newTask = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (!isActive.get() || isPaused.get() || isCleaningUp.get()) {
                            return;
                        }
                        
                        // Control climático según el ciclo día/noche
                        for (World world : Bukkit.getWorlds()) {
                            if (world != null) {
                                long time = world.getTime();
                                boolean isDaytime = time >= 0 && time < 12300; // Día: 0-12300 ticks
                                
                                if (isDaytime) {
                                    // Durante el día: lluvia ácida
                                    if (!world.hasStorm()) {
                                        world.setStorm(true);
                                        world.setWeatherDuration(6000); // 5 minutos
                                    }
                                } else {
                                    // Durante la noche: calma (sin lluvia)
                                    if (world.hasStorm()) {
                                        world.setStorm(false);
                                        world.setWeatherDuration(6000); // 5 minutos sin lluvia
                                    }
                                }
                            }
                        }
                        
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en tarea de control climático", e);
                    }
                }
            }.runTaskTimer(plugin, 0L, 600L); // Cada 30 segundos para mejor control
            
            weatherTask.set(newTask);
            plugin.getLogger().info("[AcidWeek] Tarea de control climático iniciada (lluvia día, calma noche)");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al iniciar tarea de control climático", e);
        }
    }
    
    /**
     * Inicia la tarea de supervivencia bajo lluvia ácida
     * Rastrea el tiempo que los jugadores pasan bajo la lluvia ácida
     */
    private void startRainSurvivalTask() {
        try {
            // Cancelar tarea existente si hay
            BukkitTask existingTask = rainSurvivalTask.get();
            if (existingTask != null && !existingTask.isCancelled()) {
                existingTask.cancel();
            }
            
            BukkitTask newTask = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (!isActive.get() || isPaused.get() || isCleaningUp.get()) {
                            return;
                        }
                        
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player == null || !player.isOnline()) {
                                continue;
                            }
                            
                            UUID playerId = player.getUniqueId();
                            
                            // Verificar si el jugador está bajo lluvia ácida
                            if (isPlayerExposedToRain(player)) {
                                // Verificar si no tiene pociones activas o armadura especial
                                if (!hasAcidResistance(player) && !hasActiveProtectionPotions(player)) {
                                    
                                    // Solo procesar si el jugador no ha completado el desafío
                                    if (!hasChallengeCompleted(playerId, "acid_rain_survivor")) {
                                        // Iniciar timer si no existe
                                        if (!rainSurvivalStartTime.containsKey(playerId)) {
                                            rainSurvivalStartTime.put(playerId, System.currentTimeMillis());
                                            rainSurvivalTime.put(playerId, 0);
                                            player.sendActionBar(MM.toComponent("<yellow>¡Supervivencia bajo lluvia ácida iniciada!"));
                                        }
                                        
                                        // Incrementar tiempo
                                        int currentTime = rainSurvivalTime.get(playerId) + 1;
                                        rainSurvivalTime.put(playerId, currentTime);
                                        
                                        // Actualizar progreso del desafío
                                        updateChallengeProgress(playerId, "acid_rain_survivor", currentTime, 90);
                                        
                                        // Mostrar progreso cada 10 segundos
                                        if (currentTime % 10 == 0) {
                                            int remainingTime = 90 - currentTime;
                                            if (remainingTime > 0) {
                                                player.sendActionBar(MM.toComponent("<aqua>Supervivencia: " + 
                                                    currentTime + "s / 90s <gray>(" + remainingTime + "s restantes)"));
                                            }
                                        }
                                        
                                        // Verificar si completó el desafío (90 segundos = 1.5 minutos)
                                        if (currentTime >= 90) {
                                            checkAcidRainSurvivorChallenge(player);
                                            // Limpiar datos del jugador
                                            rainSurvivalStartTime.remove(playerId);
                                            rainSurvivalTime.remove(playerId);
                                        }
                                    }
                                } else {
                                    // El jugador tiene armadura especial o pociones activas - rompe las condiciones
                                    if (rainSurvivalStartTime.containsKey(playerId) && !hasChallengeCompleted(playerId, "acid_rain_survivor")) {
                                        // Determinar qué condición se rompió para mostrar mensaje específico
                                        String reason = "";
                                        if (hasAcidResistance(player) && hasActiveProtectionPotions(player)) {
                                            reason = "armadura y pociones de protección";
                                        } else if (hasAcidResistance(player)) {
                                            reason = "armadura";
                                        } else if (hasActiveProtectionPotions(player)) {
                                            reason = "pociones de protección";
                                        }
                                        
                                        // Limpiar progreso y notificar
                                        rainSurvivalStartTime.remove(playerId);
                                        rainSurvivalTime.remove(playerId);
                                        player.sendActionBar(MM.toComponent(prefix + " <red>¡Desafío interrumpido! Has usado " + reason + ". Todo el progreso se ha perdido."));
                                    }
                                }
                            } else {
                                // Jugador no está bajo lluvia, resetear timer solo si no ha completado el desafío
                                if (rainSurvivalStartTime.containsKey(playerId) && !hasChallengeCompleted(playerId, "acid_rain_survivor")) {
                                    rainSurvivalStartTime.remove(playerId);
                                    rainSurvivalTime.remove(playerId);
                                    player.sendActionBar(MM.toComponent(prefix + " <red>¡Desafio interrumpido! Debes permanecer bajo la lluvia."));
                                }
                            }
                        }
                        
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en tarea de supervivencia de lluvia ácida", e);
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L); // Cada segundo
            
            rainSurvivalTask.set(newTask);
            plugin.getLogger().info("[AcidWeek] Tarea de supervivencia de lluvia ácida iniciada");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al iniciar tarea de supervivencia de lluvia ácida", e);
        }
    }
    
    /**
     * Verifica si el jugador tiene pociones de protección activas
     */
    private boolean hasActiveProtectionPotions(Player player) {
        try {
            if (player == null || !player.isOnline()) {
                return false;
            }
            
            // Verificar pociones de resistencia, regeneración, absorción
            return player.hasPotionEffect(PotionEffectType.RESISTANCE) ||
                   player.hasPotionEffect(PotionEffectType.REGENERATION) ||
                   player.hasPotionEffect(PotionEffectType.ABSORPTION) ||
                   player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE);
                   
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al verificar pociones de protección para " + player.getName(), e);
            return false;
        }
    }
    
    // Event handlers con mejoras de thread-safety
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isActive.get() || isPaused.get() || event == null) {
            return;
        }
        
        try {
            Player player = event.getPlayer();
            if (player == null || !player.isOnline()) {
                return;
            }
            
            Location to = event.getTo();
            if (to == null || to.getWorld() == null) {
                return;
            }
            
            // Verificar si el jugador se movió a agua
            Block block = to.getBlock();
            if (block != null && block.getType() == Material.WATER) {
                UUID playerId = player.getUniqueId();
                if (!playersInWater.contains(playerId)) {
                    playersInWater.add(playerId);
                    player.sendActionBar(MM.toComponent(prefix + " <yellow>¡Has entrado en agua ácida! ¡Ten cuidado!"));
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en evento de movimiento de jugador", e);
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isActive.get() || isPaused.get() || event == null) {
            return;
        }
        
        try {
            if (event.getDamager() instanceof ThrownPotion && event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (player == null || !player.isOnline()) {
                    return;
                }
                
                // Aumentar daño de pociones durante la semana ácida
                ThrownPotion potion = (ThrownPotion) event.getDamager();
                ItemStack potionItem = potion.getItem();
                
                if (potionItem != null && potionItem.getItemMeta() instanceof PotionMeta) {
                    PotionMeta meta = (PotionMeta) potionItem.getItemMeta();
                    PotionType potionType = meta.getBasePotionType();
                    
                    // Aumentar daño de pociones dañinas
                    if (potionType == PotionType.HARMING || potionType == PotionType.POISON) {
                        double currentDamage = event.getDamage();
                        event.setDamage(currentDamage * 1.5); // 50% más daño
                        
                        player.sendActionBar(MM.toComponent(prefix + " <red>¡La poción es más potente durante la semana ácida!"));
                    }
                }
            }
            
            // Verificar si es un ataque con botella de agua
            if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
                Player attacker = (Player) event.getDamager();
                Player victim = (Player) event.getEntity();
                
                ItemStack weapon = attacker.getInventory().getItemInMainHand();
                if (weapon != null && weapon.getType() == Material.POTION) {
                    PotionMeta meta = (PotionMeta) weapon.getItemMeta();
                    if (meta != null && meta.getBasePotionType() == PotionType.WATER) {
                        // Es una botella de agua, verificar desafío
                        checkWaterBottleKillerChallenge(attacker, victim);
                    }
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en evento de daño por entidad", e);
        }
    }
    
    /**
     * Inicializa las definiciones de desafíos específicos de la semana ácida según la documentación
     */
    private void initializeChallengeDefinitions() {
        try {
            // Desafío 1: Conseguir los 4 tipos de pescados en cubetas (Fácil)
            registerChallenge("fish_collector", ChallengeDefinition.fromStringRewards(
                "fish_collector",
                "Coleccionista de Pescados",
                "Conseguir los 4 tipos de pescados en cubetas",
                4, // 4 tipos de peces
                Collections.singletonList("enchant:contagion:1")
            ));
            
            // Desafío 2: Sobrevivir 90 segundos bajo lluvia ácida sin pociones ni armadura especial (Intermedio)
            registerChallenge("acid_rain_survivor", ChallengeDefinition.fromStringRewards(
                "acid_rain_survivor",
                "Superviviente de Lluvia Ácida",
                "Sobrevive 90 segundos bajo la lluvia ácida sin pociones ni armadura especial",
                90, // 90 segundos
                Collections.singletonList("coins:20")
            ));
            
            // Desafío 3: Matar a un jugador con botella de agua arrojadiza (Difícil)
            registerChallenge("chemical_killer", ChallengeDefinition.fromStringRewards(
                "chemical_killer",
                "Asesino Químico",
                "Matar a un jugador con botella de agua arrojadiza",
                1,
                Collections.singletonList("tag:asesinoquimico")
            ));
            
            // Desafío 4: Conseguir un ajolote azul (Leyenda)
            registerChallenge("blue_axolotl", ChallengeDefinition.fromStringRewards(
                "blue_axolotl",
                "Ajolote Azul Legendario",
                "Conseguir un ajolote azul",
                1,
                Collections.singletonList("health:2")
            ));
            
            plugin.getLogger().info("[AcidWeek] Desafíos registrados correctamente en el sistema");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al inicializar definiciones de desafíos", e);
        }
    }
    
    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if (!isActive.get() || isPaused.get() || event == null) {
            return;
        }
        
        try {
            Player player = event.getPlayer();
            if (player == null || !player.isOnline()) {
                return;
            }
            
            // Aumentar daño a items durante la semana ácida
            if (playersInWater.contains(player.getUniqueId()) || playersInRain.contains(player.getUniqueId())) {
                ItemStack item = event.getItem();
                
                // Verificar si el item tiene resistencia al ácido
                if (item != null && item.hasItemMeta()) {
                    try {
                        boolean hasAcidResistance = false;

                            Enchantment enchantment = HeartlessMain.getContentManager().getEnchantment(CustomEnchantments.ACID_RESISTANCE_KEY);
                            hasAcidResistance = enchantment != null && item.getItemMeta().hasEnchant(enchantment);

                        if (!hasAcidResistance) {
                            // Aumentar daño si no tiene resistencia
                            int currentDamage = event.getDamage();
                            event.setDamage(currentDamage * 2); // Doble daño
                            
                            if (currentDamage > 0) {
                                player.sendActionBar(MM.toComponent(prefix + " <red>¡El ácido está corroyendo tu equipo!"));
                            }
                        }
                    } catch (Exception e) {
                        // Continuar con daño normal si hay error
                    }
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al procesar daño de item para " + event.getPlayer().getName(), e);
        }
    }
    
    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (!isActive.get() || isPaused.get() || event == null) {
            return;
        }
        
        try {
            Block block = event.getBlock();
            if (block == null || block.getWorld() == null) {
                return;
            }
            
            World world = block.getWorld();
            
            // Ralentizar crecimiento de plantas durante lluvia ácida
            if (world.hasStorm()) {
                event.setCancelled(true);
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al procesar crecimiento de bloque", e);
        }
    }
    
     // Métodos de gestión de datos del evento
     
     @Override
     protected void initializeEventSpecificData() {
         try {
             // Inicializar estructuras de datos específicas
             playersInWater.clear();
             playersInRain.clear();
             fishCollected.clear();
             acidRainSurvivors.clear();
             waterBottleKillers.clear();
             blueAxolotlOwners.clear();
             playerAcidDamage.clear();
             totalAcidDamageDealt.set(0);
             
             setupAcidWeekChallenges();
             
             plugin.getLogger().info("[AcidWeek] Datos específicos del evento inicializados");
         } catch (Exception e) {
             plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al inicializar datos específicos", e);
         }
     }
     
     private void setupAcidWeekChallenges() {
         try {
             // Configurar desafíos específicos de la semana ácida
             initializeChallengeDefinitions();
         } catch (Exception e) {
             plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al configurar desafíos", e);
         }
     }
     
     @Override
     protected void onEventStart() {
         try {
             // Lógica específica de inicio de semana ácida
             for (World world : Bukkit.getWorlds()) {
                 if (world != null) {
                     world.setStorm(true);
                     world.setWeatherDuration(Integer.MAX_VALUE);
                 }
             }
             
             plugin.getLogger().info("[AcidWeek] Semana Ácida iniciada correctamente");
         } catch (Exception e) {
             plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al iniciar Semana Ácida", e);
         }
     }
     
     @Override
     protected void onEventStop() {
         try {
             // Limpiar efectos de jugadores
             cleanupPlayerEffects();
             
             // Mostrar resumen del evento
             showEventSummary();
             
             plugin.getLogger().info("[AcidWeek] Semana Ácida finalizada correctamente");
         } catch (Exception e) {
             plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al finalizar Semana Ácida", e);
         }
     }
     
     private void cleanupPlayerEffects() {
         try {
             for (Player player : Bukkit.getOnlinePlayers()) {
                 if (player != null && player.isOnline()) {
                     try {
                         // Remover efectos específicos del evento si es necesario
                         // Por ahora, solo limpiar de las listas
                         UUID playerId = player.getUniqueId();
                         playersInWater.remove(playerId);
                         playersInRain.remove(playerId);
                     } catch (Exception e) {
                         plugin.getLogger().log(Level.WARNING, 
                             "[AcidWeek] Error al limpiar efectos de " + player.getName(), e);
                     }
                 }
             }
         } catch (Exception e) {
             plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al limpiar efectos de jugadores", e);
         }
     }
     
     private void showEventSummary() {
         try {
             Bukkit.broadcast(MM.toComponent(prefix + " <gold>═══ Resumen de Semana Ácida ═══"));
             Bukkit.broadcast(MM.toComponent(prefix + " <yellow>Daño ácido total causado: <white>" + totalAcidDamageDealt.get()));
             Bukkit.broadcast(MM.toComponent(prefix + " <yellow>Jugadores que nadaron en ácido: <white>" + playersInWater.size()));
             Bukkit.broadcast(MM.toComponent(prefix + " <yellow>Supervivientes de lluvia ácida: <white>" + acidRainSurvivors.size()));
             Bukkit.broadcast(MM.toComponent(prefix + " <gold>¡Gracias por participar!"));
         } catch (Exception e) {
             plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al mostrar resumen del evento", e);
         }
     }
     
     @Override
     protected void processEventStatistics() {
         try {
             // Procesar estadísticas específicas del evento
             Map<String, Object> stats = new HashMap<>();
             stats.put("totalAcidDamage", totalAcidDamageDealt.get());
             stats.put("playersInWaterCount", playersInWater.size());
             stats.put("playersInRainCount", playersInRain.size());
             stats.put("acidRainSurvivorsCount", acidRainSurvivors.size());
             stats.put("fishCollectedCount", fishCollected.size());
             stats.put("blueAxolotlOwnersCount", blueAxolotlOwners.size());
             
             // Guardar estadísticas usando el sistema de estadísticas del plugin
             if (plugin.getWeeklyEventManager().getStatisticsManager() != null) {
                 plugin.getWeeklyEventManager().getStatisticsManager().recordEventAction(getId(), "event_statistics", stats);
             }
             
         } catch (Exception e) {
             plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al procesar estadísticas del evento", e);
         }
     }
     
     // Métodos de acceso para datos específicos del evento
     
     @SuppressWarnings("unused")
    private int getPlayerAcidDamage(UUID playerId) {
         AtomicInteger damage = playerAcidDamage.get(playerId);
         return damage != null ? damage.get() : 0;
     }
     
     // Getters para acceso externo a datos del evento
     
     public Set<UUID> getPlayersInWater() {
         return new HashSet<>(playersInWater);
     }
     
     public void loadPlayersInWater(Set<UUID> players) {
         if (players != null) {
             playersInWater.clear();
             playersInWater.addAll(players);
         }
     }
     
     public Set<UUID> getPlayersInRain() {
         return new HashSet<>(playersInRain);
     }
     
     public void loadPlayersInRain(Set<UUID> players) {
         if (players != null) {
             playersInRain.clear();
             playersInRain.addAll(players);
         }
     }
     
     public Set<UUID> getAcidSwimmers() {
         return new HashSet<>(playersInWater);
     }
     
     public Set<UUID> getAcidRainSurvivors() {
         return new HashSet<>(acidRainSurvivors);
     }
     
     public Set<UUID> getAcidResistants() {
         // Retornar jugadores que han completado el desafío de resistencia
         return new HashSet<>();
     }
     
     // Métodos para cargar/guardar progreso de desafíos
    
    public void loadChallengeProgressFromString(Map<String, Map<String, Object>> data) {
        try {
            if (data == null) return;
            
            plugin.getLogger().info("[AcidWeek] Cargando progreso de desafíos...");
            
            // Cargar progreso de pesca
            Map<String, Object> fishData = data.get("fishCollected");
            if (fishData != null) {
                for (Map.Entry<String, Object> entry : fishData.entrySet()) {
                    try {
                        UUID playerId = UUID.fromString(entry.getKey());
                        @SuppressWarnings("unchecked")
                        Set<String> materialNames = (Set<String>) entry.getValue();
                        
                        Set<Material> materials = new HashSet<>();
                        for (String materialName : materialNames) {
                            try {
                                Material material = Material.valueOf(materialName);
                                materials.add(material);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("[AcidWeek] Material desconocido al cargar datos de pesca: " + materialName);
                            }
                        }
                        
                        if (!materials.isEmpty()) {
                            fishCollected.put(playerId, materials);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("[AcidWeek] Error al cargar datos de pesca para jugador: " + entry.getKey());
                    }
                }
                plugin.getLogger().info("[AcidWeek] Cargados datos de pesca para " + fishCollected.size() + " jugadores");
            }
            
            // Cargar daño por jugador
            Map<String, Object> damageData = data.get("playerAcidDamage");
            if (damageData != null) {
                for (Map.Entry<String, Object> entry : damageData.entrySet()) {
                    try {
                        UUID playerId = UUID.fromString(entry.getKey());
                        Integer damage = (Integer) entry.getValue();
                        playerAcidDamage.put(playerId, new AtomicInteger(damage));
                    } catch (Exception e) {
                        plugin.getLogger().warning("[AcidWeek] Error al cargar daño para jugador: " + entry.getKey());
                    }
                }
                plugin.getLogger().info("[AcidWeek] Cargados datos de daño para " + playerAcidDamage.size() + " jugadores");
            }
            
            // Cargar tiempos de inicio de supervivencia en lluvia ácida
            Map<String, Object> survivalStartData = data.get("rainSurvivalStartTime");
            if (survivalStartData != null) {
                for (Map.Entry<String, Object> entry : survivalStartData.entrySet()) {
                    try {
                        UUID playerId = UUID.fromString(entry.getKey());
                        Long startTime = ((Number) entry.getValue()).longValue();
                        rainSurvivalStartTime.put(playerId, startTime);
                    } catch (Exception e) {
                        plugin.getLogger().warning("[AcidWeek] Error al cargar tiempo de inicio de supervivencia para jugador: " + entry.getKey());
                    }
                }
                plugin.getLogger().info("[AcidWeek] Cargados tiempos de inicio de supervivencia para " + rainSurvivalStartTime.size() + " jugadores");
            }
            
            // Cargar tiempo acumulado de supervivencia en lluvia ácida
            Map<String, Object> survivalTimeData = data.get("rainSurvivalTime");
            if (survivalTimeData != null) {
                for (Map.Entry<String, Object> entry : survivalTimeData.entrySet()) {
                    try {
                        UUID playerId = UUID.fromString(entry.getKey());
                        Integer survivalTime = (Integer) entry.getValue();
                        rainSurvivalTime.put(playerId, survivalTime);
                    } catch (Exception e) {
                        plugin.getLogger().warning("[AcidWeek] Error al cargar tiempo de supervivencia para jugador: " + entry.getKey());
                    }
                }
                plugin.getLogger().info("[AcidWeek] Cargados tiempos de supervivencia para " + rainSurvivalTime.size() + " jugadores");
            }
            
            plugin.getLogger().info("[AcidWeek] Progreso de desafíos cargado correctamente");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al cargar progreso de desafíos", e);
        }
    }
     
     public void loadCompletedChallengesFromString(Map<String, Set<String>> data) {
         try {
             if (data == null) return;
             
             // Cargar desafíos completados
             for (Map.Entry<String, Set<String>> entry : data.entrySet()) {
                 String playerId = entry.getKey();
                 Set<String> challenges = entry.getValue();
                 
                 // Procesar desafíos completados para este jugador
                 try {
                     UUID playerUUID = UUID.fromString(playerId);
                     
                     // Restaurar cada desafío completado en la estructura de datos heredada
                     for (String challengeId : challenges) {
                         if (challengeId != null && !challengeId.trim().isEmpty()) {
                             // Usar computeIfAbsent para crear el Set si no existe
                             completedChallenges.computeIfAbsent(playerUUID, k -> ConcurrentHashMap.newKeySet())
                                               .add(challengeId);
                             
                             plugin.getLogger().info("[AcidWeek] Desafío restaurado: " + challengeId + " para jugador " + playerUUID);
                         }
                     }
                     
                     plugin.getLogger().info("[AcidWeek] Cargados " + challenges.size() + " desafíos completados para jugador " + playerUUID);
                     
                 } catch (IllegalArgumentException e) {
                     plugin.getLogger().log(Level.WARNING, "[AcidWeek] UUID inválido al cargar desafíos completados: " + playerId, e);
                 }
             }
             
             plugin.getLogger().info("[AcidWeek] Proceso de carga de desafíos completados finalizado. Total de jugadores procesados: " + data.size());
             
         } catch (Exception e) {
             plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al cargar desafíos completados", e);
         }
     }
     
     @Override
    protected void saveEventSpecificData() {
        try {
            // Guardar datos específicos del evento
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("playersInWater", new HashSet<>(playersInWater));
            eventData.put("playersInRain", new HashSet<>(playersInRain));
            eventData.put("acidRainSurvivors", new HashSet<>(acidRainSurvivors));
            eventData.put("waterBottleKillers", new HashSet<>(waterBottleKillers));
            eventData.put("blueAxolotlOwners", new HashSet<>(blueAxolotlOwners));
            eventData.put("totalAcidDamage", totalAcidDamageDealt.get());
            
            // Guardar datos de pesca con nombres de materiales como strings
            Map<String, Set<String>> fishData = new HashMap<>();
            for (Map.Entry<UUID, Set<Material>> entry : fishCollected.entrySet()) {
                Set<String> materialNames = new HashSet<>();
                for (Material material : entry.getValue()) {
                    materialNames.add(material.name());
                }
                fishData.put(entry.getKey().toString(), materialNames);
            }
            eventData.put("fishCollected", fishData);
            
            // Guardar daño por jugador
            Map<String, Integer> damageData = new HashMap<>();
            for (Map.Entry<UUID, AtomicInteger> entry : playerAcidDamage.entrySet()) {
                damageData.put(entry.getKey().toString(), entry.getValue().get());
            }
            eventData.put("playerAcidDamage", damageData);
            
            // Guardar tiempos de supervivencia en lluvia ácida
            Map<String, Long> survivalStartTimes = new HashMap<>();
            for (Map.Entry<UUID, Long> entry : rainSurvivalStartTime.entrySet()) {
                survivalStartTimes.put(entry.getKey().toString(), entry.getValue());
            }
            eventData.put("rainSurvivalStartTime", survivalStartTimes);
            
            // Guardar tiempo acumulado de supervivencia en lluvia ácida
            Map<String, Integer> survivalTimes = new HashMap<>();
            for (Map.Entry<UUID, Integer> entry : rainSurvivalTime.entrySet()) {
                survivalTimes.put(entry.getKey().toString(), entry.getValue());
            }
            eventData.put("rainSurvivalTime", survivalTimes);
            
            // Establecer los datos en el mapa heredado para que el StorageManager los procese
            this.eventSpecificData.clear();
            this.eventSpecificData.putAll(eventData);
            
            // Usar el sistema de almacenamiento del plugin
            if (plugin.getStorageManager() != null) {
                plugin.getStorageManager().saveEventSpecificData(this);
            }
            
            plugin.getLogger().info("[AcidWeek] Datos específicos del evento guardados correctamente");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al guardar datos específicos del evento", e);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (!isActive.get() || isPaused.get() || event == null) {
            return;
        }
        
        try {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Fish) {
                Player player = event.getPlayer();
                if (player == null || !player.isOnline()) {
                    return;
                }
                
                Fish fish = (Fish) event.getCaught();
                Material fishType = getFishTypeFromEntity(fish);
                
                if (fishType != null && REQUIRED_FISH.contains(fishType)) {
                    UUID playerId = player.getUniqueId();
                    
                    // Agregar pescado a la colección del jugador
                    fishCollected.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(fishType);
                    
                    player.sendMessage(MM.toComponent(prefix + " <green>¡Has pescado: " + fishType.name() + "!"));
                    
                    // Verificar si completó el desafío
                    checkFishCollectorChallenge(player);
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al procesar pesca para " + event.getPlayer().getName(), e);
        }
    }
    
    /**
     * Maneja cuando un jugador llena una cubeta con peces (la mecánica real del desafío)
     */
    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (!isActive.get() || isPaused.get() || event == null) {
            return;
        }
        
        try {
            Player player = event.getPlayer();
            if (player == null || !player.isOnline()) {
                return;
            }
            
            Block block = event.getBlockClicked();
            if (block == null || block.getType() != Material.WATER) {
                return;
            }
            
            // Verificar si hay peces en el agua donde se está llenando la cubeta
            Location waterLocation = block.getLocation().add(0.5, 0.5, 0.5);
            Collection<Entity> nearbyEntities = waterLocation.getWorld().getNearbyEntities(waterLocation, 2, 2, 2);
            
            for (Entity entity : nearbyEntities) {
                if (entity instanceof Fish) {
                    Fish fish = (Fish) entity;
                    Material fishType = getFishTypeFromEntity(fish);
                    
                    if (fishType != null && REQUIRED_FISH.contains(fishType)) {
                        UUID playerId = player.getUniqueId();
                        
                        // Agregar pescado en cubeta a la colección del jugador
                        fishCollected.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(fishType);
                        
                        player.sendMessage(MM.toComponent(prefix + " <aqua>¡Has capturado " + fishType.name() + " en cubeta!"));
                        
                        // Verificar si completó el desafío
                        checkFishCollectorChallenge(player);
                        
                        // Remover el pez del agua ya que fue capturado
                        entity.remove();
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al manejar llenado de cubeta para " + event.getPlayer().getName(), e);
        }
    }

    /**
     * Maneja la captura de ajolotes azules en cubos (evento correcto para el desafío)
     */
    @EventHandler
    public void onPlayerBucketEntity(PlayerBucketEntityEvent event) {
        if (!isActive.get() || isPaused.get() || event == null) {
            return;
        }
        
        try {
            Player player = event.getPlayer();
            Entity entity = event.getEntity();
            
            if (player == null || !player.isOnline() || entity == null) {
                return;
            }
            
            // Verificar si la entidad capturada es un ajolote azul
            if (entity instanceof Axolotl) {
                Axolotl axolotl = (Axolotl) entity;
                
                if (axolotl.getVariant() == Axolotl.Variant.BLUE) {
                    UUID playerId = player.getUniqueId();
                    
                    if (!hasChallengeCompleted(playerId, "blue_axolotl")) {
                        completeChallengeForPlayer(playerId, "blue_axolotl");
                        blueAxolotlOwners.add(playerId);                        
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al procesar captura de entidad en cubo", e);
        }
    }
    
    /**
     * Determina el tipo de pescado basado en la entidad Fish
     */
    private Material getFishTypeFromEntity(Fish fish) {
        try {
            EntityType entityType = fish.getType();
            switch (entityType) {
                case COD:
                    return Material.COD;
                case SALMON:
                    return Material.SALMON;
                case TROPICAL_FISH:
                    return Material.TROPICAL_FISH;
                case PUFFERFISH:
                    return Material.PUFFERFISH;
                default:
                    return null;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al determinar tipo de pescado", e);
            return null;
        }
    }
    
    /**
     * Verifica y completa el desafío de coleccionista de pescados
     */
    private void checkFishCollectorChallenge(Player player) {
        try {
            UUID playerId = player.getUniqueId();
            Set<Material> playerFish = fishCollected.get(playerId);
            
            if (playerFish != null && playerFish.size() >= 4 && 
                !hasChallengeCompleted(playerId, "fish_collector")) {
                
                completeChallengeForPlayer(playerId, "fish_collector");
                
                player.sendMessage(MM.toComponent(prefix + " <gold>¡Desafío completado: Coleccionista de Pescados!"));
                player.sendMessage(MM.toComponent(prefix + " <green>Recompensa: Encantamiento 'Contagion'"));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al verificar desafío de coleccionista de pescados para " + player.getName(), e);
        }
    }
    
    /**
     * Registra un ataque con botella de agua para rastrear el atacante
     */
    private void registerWaterBottleAttack(Player attacker, Player victim) {
        UUID victimId = victim.getUniqueId();
        UUID attackerId = attacker.getUniqueId();
        
        // Solo registrar si el atacante es diferente de la víctima
        if (!attackerId.equals(victimId)) {
            lastWaterBottleAttacker.put(victimId, attackerId);
            lastWaterBottleAttackTime.put(victimId, System.currentTimeMillis());
        }
    }
    
    /**
     * Verifica el desafío de asesino con botella de agua
     * NOTA: Este método ya no otorga el desafío directamente, solo registra el ataque
     */
    private void checkWaterBottleKillerChallenge(Player attacker, Player victim) {
        try {
            // Solo registrar el ataque, no otorgar el desafío aquí
            registerWaterBottleAttack(attacker, victim);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al registrar ataque con botella de agua para " + attacker.getName(), e);
        }
    }
    
    /**
     * Maneja las muertes de jugadores para verificar el desafío de Asesino Químico
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        try {
            Player victim = event.getEntity();
            UUID victimId = victim.getUniqueId();
            
            // Verificar si hay un atacante registrado con botella de agua
            UUID attackerId = lastWaterBottleAttacker.get(victimId);
            Long attackTime = lastWaterBottleAttackTime.get(victimId);
            
            if (attackerId != null && attackTime != null) {
                // Verificar que el ataque fue reciente (últimos 10 segundos)
                long timeSinceAttack = System.currentTimeMillis() - attackTime;
                if (timeSinceAttack <= 10000) { // 10 segundos
                    Player attacker = Bukkit.getPlayer(attackerId);
                    
                    if (attacker != null && attacker.isOnline()) {
                        // Verificar que el atacante no haya completado ya el desafío
                        if (!hasChallengeCompleted(attackerId, "chemical_killer")) {
                            completeChallengeForPlayer(attackerId, "chemical_killer");
                            waterBottleKillers.add(attackerId);
                            
                            attacker.sendMessage(MM.toComponent(prefix + " <gold>¡Desafío completado: Asesino Químico!"));
                            attacker.sendMessage(MM.toComponent(prefix + " <green>Recompensa: Tag 'Asesino Químico'"));
                            
                            Reward reward = new Reward("tag:asesinoquimico");
                            reward.grantTo(attacker, prefix);
                        }
                    }
                }
                
                // Limpiar el registro del ataque
                lastWaterBottleAttacker.remove(victimId);
                lastWaterBottleAttackTime.remove(victimId);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al procesar muerte de jugador para desafío chemical_killer", e);
        }
    }
    
    /**
     * Maneja el evento de pociones arrojadizas (splash potions)
     * Aplica daño ácido cuando se usan botellas de agua como proyectiles
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPotionSplash(PotionSplashEvent event) {
        plugin.getLogger().info("[AcidWeek] PotionSplashEvent detectado - Activo: " + isActive.get() + ", Pausado: " + isPaused.get());
        
        if (!isActive.get() || isPaused.get() || event == null) {
            plugin.getLogger().info("[AcidWeek] Evento ignorado - no activo o pausado");
            return;
        }
        
        try {
            ThrownPotion potion = event.getPotion();
            ItemStack potionItem = potion.getItem();
            
            plugin.getLogger().info("[AcidWeek] Poción detectada: " + (potionItem != null ? potionItem.getType() : "null"));
            
            // Verificar si es una botella de agua
            if (potionItem != null && potionItem.getItemMeta() instanceof PotionMeta) {
                PotionMeta meta = (PotionMeta) potionItem.getItemMeta();
                
                plugin.getLogger().info("[AcidWeek] PotionMeta encontrado, tipo base: " + (meta != null ? meta.getBasePotionType() : "null"));
                
                if (meta != null && meta.getBasePotionType() == PotionType.WATER) {
                    plugin.getLogger().info("[AcidWeek] ¡Botella de agua detectada! Entidades afectadas: " + event.getAffectedEntities().size());
                    
                    // Las botellas de agua no generan entidades afectadas automáticamente
                    // Necesitamos buscar manualmente las entidades en el área de impacto
                    Location impactLocation = potion.getLocation();
                    double splashRadius = 4.0; // Radio de splash típico de pociones
                    
                    plugin.getLogger().info("[AcidWeek] Buscando entidades en radio de " + splashRadius + " bloques desde " + impactLocation);
                    
                    // Buscar todas las entidades vivas en el área de impacto
                    for (org.bukkit.entity.Entity nearbyEntity : impactLocation.getWorld().getNearbyEntities(impactLocation, splashRadius, splashRadius, splashRadius)) {
                        if (nearbyEntity instanceof org.bukkit.entity.LivingEntity) {
                            org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) nearbyEntity;
                            
                            // Calcular la distancia para determinar la intensidad del daño
                            double distance = impactLocation.distance(livingEntity.getLocation());
                            double intensity = Math.max(0.0, 1.0 - (distance / splashRadius)); // Intensidad basada en distancia
                            
                            plugin.getLogger().info("[AcidWeek] Entidad encontrada: " + livingEntity.getType() + " a distancia " + distance + ", intensidad: " + intensity);
                            
                            if (intensity > 0.0) {
                                if (livingEntity instanceof Player) {
                                    Player player = (Player) livingEntity;
                                    
                                    // Aplicar daño ácido basado en la intensidad del splash
                                    double acidDamage = ACID_DAMAGE_AMOUNT * intensity;
                                    
                                    plugin.getLogger().info("[AcidWeek] Aplicando " + acidDamage + " de daño ácido a " + player.getName());
                                    
                                    // Aplicar el daño
                                    player.damage(acidDamage);
                                    
                                    // Mensaje visual
                                    player.sendActionBar(MM.toComponent(prefix + " <red>¡El agua ácida te quema!"));
                                    
                                    // Verificar si el lanzador es un jugador para el desafío
                                    if (potion.getShooter() instanceof Player) {
                                        Player shooter = (Player) potion.getShooter();
                                        checkWaterBottleKillerChallenge(shooter, player);
                                    }
                                } else {
                                    // Aplicar daño a otras entidades vivas
                                    double acidDamage = ACID_DAMAGE_AMOUNT * intensity;
                                    plugin.getLogger().info("[AcidWeek] Aplicando " + acidDamage + " de daño ácido a " + livingEntity.getType());
                                    livingEntity.damage(acidDamage);
                                }
                            }
                        }
                    }
                    
                    // También procesar las entidades afectadas normalmente (por si acaso)
                    for (org.bukkit.entity.LivingEntity entity : event.getAffectedEntities()) {
                        double intensity = event.getIntensity(entity);
                        plugin.getLogger().info("[AcidWeek] Procesando entidad afectada normal: " + entity.getType() + ", intensidad: " + intensity);
                        
                        if (entity instanceof Player) {
                            Player player = (Player) entity;
                            
                            // Aplicar daño ácido basado en la intensidad del splash
                            double acidDamage = ACID_DAMAGE_AMOUNT * intensity;
                            
                            plugin.getLogger().info("[AcidWeek] Aplicando " + acidDamage + " de daño ácido a " + player.getName());
                            
                            // Aplicar el daño
                            player.damage(acidDamage);
                            
                            // Mensaje visual
                            player.sendActionBar(MM.toComponent(prefix + " <red>¡El agua ácida te quema!"));
                            
                            // Verificar si el lanzador es un jugador para el desafío
                            if (potion.getShooter() instanceof Player) {
                                Player shooter = (Player) potion.getShooter();
                                checkWaterBottleKillerChallenge(shooter, player);
                            }
                        } else {
                            // Aplicar daño a otras entidades vivas
                            double acidDamage = ACID_DAMAGE_AMOUNT * intensity;
                            plugin.getLogger().info("[AcidWeek] Aplicando " + acidDamage + " de daño ácido a " + entity.getType());
                            entity.damage(acidDamage);
                        }
                    }
                } else {
                    plugin.getLogger().info("[AcidWeek] No es una botella de agua, tipo: " + (meta != null ? meta.getBasePotionType() : "meta null"));
                }
            } else {
                plugin.getLogger().info("[AcidWeek] No es PotionMeta o item es null");
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en evento de poción arrojadiza", e);
        }
    }
}