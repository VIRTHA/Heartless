package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.TimeExpression;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.UUID;
import java.util.Map;
import java.util.Set;

/**
 * Evento semanal de lluvia ácida con mejoras de thread-safety y prevención de memory leaks
 * 
 * Características principales:
 * - Daño ácido en agua y lluvia
 * - Control automático del clima
 * - Sistema de desafíos y estadísticas
 * - Resistencia al ácido mediante encantamientos
 * 
 * Mejoras implementadas:
 * - Thread-safety con ConcurrentHashMap
 * - Prevención de memory leaks
 * - Manejo robusto de errores
 * - Validaciones de nulidad mejoradas
 */
public class AcidWeek extends AbstractWeeklyEvent {
    
    // Thread-safe collections para evitar condiciones de carrera
    private final Set<UUID> playersInWater = ConcurrentHashMap.newKeySet();
    private final Set<UUID> playersInRain = ConcurrentHashMap.newKeySet();
    
    // Collections para tracking de desafíos
    private final Set<UUID> acidSwimmers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> acidRainSurvivors = ConcurrentHashMap.newKeySet();
    private final Set<UUID> acidResistants = ConcurrentHashMap.newKeySet();
    
    // Estadísticas del evento
    private final AtomicInteger totalAcidDamageDealt = new AtomicInteger(0);
    private final ConcurrentHashMap<UUID, AtomicInteger> playerAcidDamage = new ConcurrentHashMap<>();
    
    // Referencias atómicas para las tareas para thread-safety
    private final AtomicReference<BukkitTask> acidTask = new AtomicReference<>();
    private final AtomicReference<BukkitTask> weatherTask = new AtomicReference<>();
    
    // Flag atómico para controlar el estado de limpieza
    private final AtomicBoolean isCleaningUp = new AtomicBoolean(false);
    
    // Constantes para configuración
    private static final int MAX_PLAYERS_PER_TICK = 30;
    private static final int MAX_ROOF_CHECK_HEIGHT = 20;
    private static final double ACID_DAMAGE_AMOUNT = 2.0;
    private static final double RESISTANCE_DAMAGE_REDUCTION = 0.5;
    private static final long TASK_INTERVAL_TICKS = 20L; // 1 segundo

    public AcidWeek(HeartlessMain plugin, TimeExpression duration) {
        super(plugin, duration);
        this.prefix = "<b><gradient:#befd58:#c4fb54:#caf950:#d1f64b:#d7f447:#ddf243:#e3f03f:#e9ee3b:#f0eb36:#f6e932:#fce72e>Semana acida</gradient></b>";
    }

    @Override
    protected void startEventTasks() {
        try {
            // Iniciar tareas del evento
            startAcidDamageTask();
            startWeatherControlTask();
            plugin.getLogger().info("[AcidWeek] Tareas del evento iniciadas");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al iniciar tareas del evento", e);
            stopEventTasks(); // Limpieza automática en caso de error
        }
    }
    
    @Override
    protected void announceEventStart() {
        try {
            Bukkit.broadcast(MM.toComponent(prefix + " <green>¡La lluvia ácida ha comenzado! Busca refugio y protege tu equipo."));
            announceAcidChallenges();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al anunciar inicio del evento", e);
        }
    }
    
    @Override
    protected void announceEventEnd() {
        try {
            // Anuncio general del fin del evento
            Bukkit.broadcast(MM.toComponent(prefix + " <green>¡La lluvia ácida ha cesado! El mundo vuelve a la normalidad."));
            Bukkit.broadcast(MM.toComponent(prefix + " <yellow>¡Revisando las estadísticas de supervivencia ácida!"));
            
            // Enviar estadísticas individuales a cada jugador
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
     * Envía las estadísticas individuales del evento a un jugador específico
     * @param player El jugador al que enviar las estadísticas
     */
    private void sendPlayerStatistics(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            UUID playerId = player.getUniqueId();
            
            // Separador visual
            player.sendMessage(MM.toComponent("<gray><b>========================================</b></gray>"));
            player.sendMessage(MM.toComponent("<green><b>TUS ESTADÍSTICAS - SEMANA ÁCIDA</b></green>"));
            player.sendMessage(MM.toComponent("<gray><b>========================================</b></gray>"));
            
            // Estado de supervivencia
            boolean inWater = playersInWater.contains(playerId);
            boolean inRain = playersInRain.contains(playerId);
            boolean hasResistance = hasAcidResistanceGear(player);
            
            player.sendMessage(MM.toComponent("<yellow>🌊 <white>Supervivencia en agua ácida:</white> " + (inWater ? "<green>Logrado</green>" : "<red>No logrado</red>")));
            player.sendMessage(MM.toComponent("<yellow>🌧 <white>Supervivencia bajo lluvia ácida:</white> " + (inRain ? "<green>Logrado</green>" : "<red>No logrado</red>")));
            player.sendMessage(MM.toComponent("<yellow>🛡 <white>Equipo resistente al ácido:</white> " + (hasResistance ? "<green>Obtenido</green>" : "<red>No obtenido</red>")));
            
            // Desafíos completados
            player.sendMessage(MM.toComponent("<gray>----------------------------------------</gray>"));
            player.sendMessage(MM.toComponent("<gold><b>DESAFÍOS COMPLETADOS:</b></gold>"));
            
            // Verificar cada desafío
            boolean acidSwimmer = hasChallengeCompleted(playerId, "acid_swimmer");
            boolean acidRainSurvivor = hasChallengeCompleted(playerId, "acid_rain_survivor");
            boolean acidResistant = hasChallengeCompleted(playerId, "acid_resistant");
            
            player.sendMessage(MM.toComponent("<yellow>🏊 Nadador Ácido:</yellow> " + (acidSwimmer ? "<green>✓ Completado</green>" : "<red>✗ No completado</red>")));
            player.sendMessage(MM.toComponent("<yellow>☔ Superviviente de Lluvia Ácida:</yellow> " + (acidRainSurvivor ? "<green>✓ Completado</green>" : "<red>✗ No completado</red>")));
            player.sendMessage(MM.toComponent("<yellow>🛡 Resistente al Ácido:</yellow> " + (acidResistant ? "<green>✓ Completado</green>" : "<red>✗ No completado</red>")));
            
            // Mensaje final
            int completedChallenges = (acidSwimmer ? 1 : 0) + (acidRainSurvivor ? 1 : 0) + (acidResistant ? 1 : 0);
            player.sendMessage(MM.toComponent("<gray>----------------------------------------</gray>"));
            player.sendMessage(MM.toComponent("<gold>Desafíos completados: <white>" + completedChallenges + "/3</white></gold>"));
            player.sendMessage(MM.toComponent("<gray><b>========================================</b></gray>"));
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al procesar estadísticas para " + player.getName(), e);
        }
    }
    
    /**
     * Anuncia los desafíos disponibles durante la Semana Ácida
     */
    private void announceAcidChallenges() {
        try {
            Bukkit.broadcast(MM.toComponent("<gray><b>=== <green>DESAFÍOS DE LA SEMANA</green> <gray><b>==="));
            Bukkit.broadcast(MM.toComponent("<yellow>1. <green>Sobrevive</green> nadando en agua ácida</yellow>"));
            Bukkit.broadcast(MM.toComponent("<gray>   <white>Recompensa:</white> Resistencia temporal al ácido</gray>"));
            Bukkit.broadcast(MM.toComponent("<yellow>2. <green>Sobrevive</green> bajo la lluvia ácida durante el día</yellow>"));
            Bukkit.broadcast(MM.toComponent("<gray>   <white>Recompensa:</white> Protección mejorada</gray>"));
            Bukkit.broadcast(MM.toComponent("<yellow>3. <green>Obtén</green> equipo con resistencia al ácido</yellow>"));
            Bukkit.broadcast(MM.toComponent("<gray>   <white>Recompensa:</white> Inmunidad temporal</gray>"));
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al anunciar desafíos", e);
        }
    }
    
    @Override
    protected void stopEventTasks() {
        try {
            // Cancelar tareas de forma thread-safe
            BukkitTask currentAcidTask = acidTask.getAndSet(null);
            if (currentAcidTask != null && !currentAcidTask.isCancelled()) {
                currentAcidTask.cancel();
                plugin.getLogger().info("[AcidWeek] Tarea de daño ácido cancelada");
            }
            
            BukkitTask currentWeatherTask = weatherTask.getAndSet(null);
            if (currentWeatherTask != null && !currentWeatherTask.isCancelled()) {
                currentWeatherTask.cancel();
                plugin.getLogger().info("[AcidWeek] Tarea de control climático cancelada");
            }
            
            // Restaurar clima normal en todos los mundos
            restoreNormalWeather();
            
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
                }
            }
            plugin.getLogger().info("[AcidWeek] Clima restaurado a normal");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al restaurar clima normal", e);
        }
    }
    
    @Override
    protected void cleanupEventData() {
        if (isCleaningUp.compareAndSet(false, true)) {
            try {
                // Limpiar conjuntos de forma thread-safe
                int waterPlayers = playersInWater.size();
                int rainPlayers = playersInRain.size();
                
                playersInWater.clear();
                playersInRain.clear();
                
                plugin.getLogger().info(String.format(
                    "[AcidWeek] Datos limpiados: %d jugadores en agua, %d jugadores en lluvia", 
                    waterPlayers, rainPlayers));
                    
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error durante limpieza de datos", e);
            } finally {
                isCleaningUp.set(false);
            }
        }
    }
    
    /**
     * Limpieza forzada en caso de errores críticos
     */
    @SuppressWarnings("unused")
    private void forceCleanup() {
        try {
            plugin.getLogger().warning("[AcidWeek] Ejecutando limpieza forzada");
            
            // Detener todas las tareas
            stopEventTasks();
            
            // Limpiar datos
            cleanupEventData();
            
            // Marcar como inactivo
            isActive.set(false);
            isPaused.set(false);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error durante limpieza forzada", e);
        }
    }
    
    @Override
    public String getId() {
        return "acid_week";
    }
    
    @Override
    protected void pauseEventTasks() {
        try {
            BukkitTask currentAcidTask = acidTask.getAndSet(null);
            if (currentAcidTask != null && !currentAcidTask.isCancelled()) {
                currentAcidTask.cancel();
            }
            
            BukkitTask currentWeatherTask = weatherTask.getAndSet(null);
            if (currentWeatherTask != null && !currentWeatherTask.isCancelled()) {
                currentWeatherTask.cancel();
            }
            
            plugin.getLogger().info("[AcidWeek] Tareas pausadas");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al pausar tareas", e);
        }
    }
    
    @Override
    protected void resumeEventTasks() {
        try {
            if (isActive.get() && !isCleaningUp.get()) {
                startAcidDamageTask();
                startWeatherControlTask();
                isPaused.set(false);
                plugin.getLogger().info("[AcidWeek] Tareas reanudadas");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al reanudar tareas", e);
        }
    }
    
    /**
     * Verifica si un jugador tiene resistencia al ácido (thread-safe)
     */
    @SuppressWarnings("deprecation")
    private boolean hasAcidResistance(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        try {
            // Verificar si el jugador tiene el encantamiento en alguna pieza de armadura
            ItemStack[] armorContents = player.getInventory().getArmorContents();
            if (armorContents == null) {
                return false;
            }
            
            for (ItemStack item : armorContents) {
                if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
                    try {
                        // Verificar usando Registry.ENCHANTMENT
                        if (item.getItemMeta().hasEnchant(Registry.ENCHANTMENT.get(new NamespacedKey(plugin, "acid_resistance")))) {
                            return true;
                        }
                        
                        // Verificar usando el ContentManager
                        if (HeartlessMain.getContentManager() != null && 
                            HeartlessMain.getContentManager().hasEnchantment(item, CustomEnchantments.ENCHANTMENTS.ACID_RESISTANCE.toEnchantment())) {
                            return true;
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, 
                            "[AcidWeek] Error al verificar encantamiento de resistencia", e);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al verificar resistencia al ácido para " + player.getName(), e);
        }
        
        return false;
    }
    
    /**
     * Inicia la tarea de daño ácido con mejoras de rendimiento y thread-safety
     */
    private void startAcidDamageTask() {
        try {
            // Cancelar tarea existente si hay
            BukkitTask existingTask = acidTask.get();
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
                        
                        // Limpiar UUIDs de jugadores desconectados para prevenir memory leaks
                        cleanupDisconnectedPlayers();
                        
                        // Procesar daño a jugadores en agua
                        processWaterDamage();
                        
                        // Procesar daño a jugadores en lluvia (cada 5 segundos)
                        if (this.getTaskId() % 5 == 0) {
                            processRainDamage();
                        }
                        
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en tarea de daño ácido", e);
                    }
                }
            }.runTaskTimer(plugin, 0L, TASK_INTERVAL_TICKS);
            
            acidTask.set(newTask);
            plugin.getLogger().info("[AcidWeek] Tarea de daño ácido iniciada");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al iniciar tarea de daño ácido", e);
        }
    }
    
    /**
     * Limpia jugadores desconectados de las colecciones para prevenir memory leaks
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
     * Procesa el daño ácido a jugadores en agua con optimizaciones de rendimiento
     */
    private void processWaterDamage() {
        try {
            int processedPlayers = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player == null || !player.isOnline() || processedPlayers >= MAX_PLAYERS_PER_TICK) {
                    continue;
                }
                
                try {
                    Location loc = player.getLocation();
                    if (loc == null || loc.getWorld() == null) {
                        continue;
                    }
                    
                    Block block = loc.getBlock();
                    if (block != null && block.getType() == Material.WATER) {
                        UUID playerId = player.getUniqueId();
                        playersInWater.add(playerId);
                        
                        // Aplicar daño ácido
                        applyAcidDamage(player, "agua ácida");
                        
                        // Verificar desafío de nadador ácido
                        checkAcidSwimmerChallenge(player);
                        
                        processedPlayers++;
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, 
                        "[AcidWeek] Error al procesar daño de agua para " + player.getName(), e);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en procesamiento de daño de agua", e);
        }
    }
    
    /**
     * Procesa el daño ácido a jugadores bajo la lluvia con optimizaciones de rendimiento
     */
    private void processRainDamage() {
        try {
            int processedPlayers = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player == null || !player.isOnline() || processedPlayers >= MAX_PLAYERS_PER_TICK) {
                    continue;
                }
                
                try {
                    Location loc = player.getLocation();
                    if (loc == null || loc.getWorld() == null) {
                        continue;
                    }
                    
                    World world = loc.getWorld();
                    if (world.hasStorm() && isPlayerExposedToRain(player)) {
                        UUID playerId = player.getUniqueId();
                        playersInRain.add(playerId);
                        
                        // Aplicar daño ácido
                        applyAcidDamage(player, "lluvia ácida");
                        
                        // Verificar desafío de superviviente de lluvia ácida
                        checkAcidRainSurvivorChallenge(player);
                        
                        processedPlayers++;
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, 
                        "[AcidWeek] Error al procesar daño de lluvia para " + player.getName(), e);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en procesamiento de daño de lluvia", e);
        }
    }
    
    /**
     * Verifica si un jugador está expuesto a la lluvia (sin techo)
     */
    private boolean isPlayerExposedToRain(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        try {
            Location loc = player.getLocation();
            if (loc == null || loc.getWorld() == null) {
                return false;
            }
            
            // Verificar si hay bloques sólidos encima del jugador
            for (int y = 1; y <= MAX_ROOF_CHECK_HEIGHT; y++) {
                Location checkLoc = loc.clone().add(0, y, 0);
                Block block = checkLoc.getBlock();
                
                if (block != null && block.getType().isSolid()) {
                    return false; // Hay techo
                }
            }
            
            return true; // No hay techo, expuesto a la lluvia
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al verificar exposición a lluvia para " + player.getName(), e);
            return false;
        }
    }
    
    /**
     * Aplica daño ácido a un jugador con resistencia considerada
     */
    private void applyAcidDamage(Player player, String source) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            double damage = ACID_DAMAGE_AMOUNT;
            
            // Reducir daño si tiene resistencia al ácido
            if (hasAcidResistance(player)) {
                damage *= RESISTANCE_DAMAGE_REDUCTION;
                player.sendMessage(MM.toComponent(prefix + " <green>Tu equipo resistente al ácido reduce el daño!"));
                
                // Verificar desafío de resistente al ácido
                checkAcidResistantChallenge(player);
            }
            
            // Aplicar daño
            player.damage(damage);
            
            // Mensaje de daño
            player.sendMessage(MM.toComponent(prefix + " <red>¡Estás recibiendo daño por " + source + "!"));
            
            // Efectos de poción
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al aplicar daño ácido a " + player.getName(), e);
        }
    }
    
    /**
     * Verifica y completa el desafío de nadador ácido
     */
    private void checkAcidSwimmerChallenge(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            UUID playerId = player.getUniqueId();
            if (!hasChallengeCompleted(playerId, "acid_swimmer")) {
                completeChallengeForPlayer(playerId, "acid_swimmer");
                player.sendMessage(MM.toComponent(prefix + " <gold>¡Desafío completado: Nadador Ácido!"));
                player.sendMessage(MM.toComponent(prefix + " <green>Recompensa: Resistencia temporal al ácido"));
                
                // Dar resistencia temporal
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1200, 0)); // 1 minuto
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
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            UUID playerId = player.getUniqueId();
            if (!hasChallengeCompleted(playerId, "acid_rain_survivor")) {
                completeChallengeForPlayer(playerId, "acid_rain_survivor");
                player.sendMessage(MM.toComponent(prefix + " <gold>¡Desafío completado: Superviviente de Lluvia Ácida!"));
                player.sendMessage(MM.toComponent(prefix + " <green>Recompensa: Protección mejorada"));
                
                // Dar protección mejorada
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 2400, 1)); // 2 minutos, nivel 2
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al verificar desafío de superviviente de lluvia ácida para " + player.getName(), e);
        }
    }
    
    /**
     * Verifica y completa el desafío de resistente al ácido
     */
    private void checkAcidResistantChallenge(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            UUID playerId = player.getUniqueId();
            if (!hasChallengeCompleted(playerId, "acid_resistant")) {
                completeChallengeForPlayer(playerId, "acid_resistant");
                player.sendMessage(MM.toComponent(prefix + " <gold>¡Desafío completado: Resistente al Ácido!"));
                player.sendMessage(MM.toComponent(prefix + " <green>Recompensa: Inmunidad temporal"));
                
                // Dar inmunidad temporal
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 3600, 2)); // 3 minutos, nivel 3
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "[AcidWeek] Error al verificar desafío de resistente al ácido para " + player.getName(), e);
        }
    }
    
    /**
     * Verifica si un jugador tiene equipo con resistencia al ácido
     */
    private boolean hasAcidResistanceGear(Player player) {
        return hasAcidResistance(player);
    }
    
    /**
     * Inicia la tarea de control climático
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
                        
                        // Mantener lluvia en todos los mundos
                        for (World world : Bukkit.getWorlds()) {
                            if (world != null && !world.hasStorm()) {
                                world.setStorm(true);
                                world.setWeatherDuration(6000); // 5 minutos
                            }
                        }
                        
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en tarea de control climático", e);
                    }
                }
            }.runTaskTimer(plugin, 0L, 1200L); // Cada minuto
            
            weatherTask.set(newTask);
            plugin.getLogger().info("[AcidWeek] Tarea de control climático iniciada");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[AcidWeek] Error al iniciar tarea de control climático", e);
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
                    player.sendMessage(MM.toComponent(prefix + " <yellow>¡Has entrado en agua ácida! ¡Ten cuidado!"));
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
                double currentDamage = event.getDamage();
                event.setDamage(currentDamage * 1.5);
                
                player.sendMessage(MM.toComponent(prefix + " <red>¡Las pociones son más potentes durante la semana ácida!"));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en evento de daño por entidad", e);
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
            
            ItemStack item = event.getItem();
            if (item == null || item.getType() == Material.AIR) {
                return;
            }
            
            UUID playerId = player.getUniqueId();
            
            // Aumentar daño a items si el jugador está en agua o lluvia ácida
            if (playersInWater.contains(playerId) || playersInRain.contains(playerId)) {
                // Verificar si tiene resistencia al ácido
                if (!hasAcidResistance(player)) {
                    int currentDamage = event.getDamage();
                    event.setDamage(currentDamage * 2); // Doble daño a items
                    
                    player.sendMessage(MM.toComponent(prefix + " <red>¡El ácido está dañando tu equipo!"));
                } else {
                    player.sendMessage(MM.toComponent(prefix + " <green>Tu equipo resistente al ácido protege tus items!"));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en evento de daño a item", e);
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
                // 70% de probabilidad de cancelar el crecimiento
                if (Math.random() < 0.7) {
                    event.setCancelled(true);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error en evento de crecimiento de bloque", e);
        }
    }
    
    /**
     * Checks if a player has completed a specific challenge
     * @param playerId The UUID of the player
     * @param challengeId The ID of the challenge
     * @return true if the challenge is completed, false otherwise
     */
     // ========== MÉTODOS DE PERSISTENCIA ==========
     
     @Override
     protected void initializeEventSpecificData() {
         // Inicializar estadísticas globales
         globalStatistics.put("total_acid_damage", new AtomicLong(0));
         globalStatistics.put("players_in_water", new AtomicLong(0));
         globalStatistics.put("players_in_rain", new AtomicLong(0));
         globalStatistics.put("acid_swimmers", new AtomicLong(0));
         globalStatistics.put("acid_rain_survivors", new AtomicLong(0));
         globalStatistics.put("acid_resistants", new AtomicLong(0));
         
         // Configurar desafíos específicos del evento
         setupAcidWeekChallenges();
         
         plugin.getLogger().info("[AcidWeek] Datos específicos del evento inicializados");
     }
     
     /**
      * Configura los desafíos específicos de la semana ácida
      */
     private void setupAcidWeekChallenges() {
          try {
              // Configurar desafíos disponibles para este evento
              availableChallenges.put("acid_swimmer", new ChallengeDefinition(
                  "acid_swimmer", 
                  "Nadador Ácido", 
                  "Sobrevivir nadando en agua ácida",
                  10,
                  Collections.singletonList("experience:100")
              ));
              
              availableChallenges.put("acid_rain_survivor", new ChallengeDefinition(
                  "acid_rain_survivor",
                  "Superviviente de Lluvia Ácida",
                  "Sobrevivir bajo la lluvia ácida",
                  5,
                  Collections.singletonList("experience:200")
              ));
              
              availableChallenges.put("acid_resistant", new ChallengeDefinition(
                  "acid_resistant",
                  "Resistente al Ácido",
                  "Mostrar resistencia al ácido",
                  20,
                  Collections.singletonList("experience:300")
              ));
              
              plugin.getLogger().info("[AcidWeek] Desafíos configurados: " + availableChallenges.size());
          } catch (Exception e) {
              plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al configurar desafíos", e);
          }
      }
     
     @Override
     protected void onEventStart() {
         plugin.getLogger().info("[AcidWeek] Iniciando evento de Semana Ácida...");
         
         // Inicializar datos del evento
          initializeEventSpecificData();
          
          // Iniciar tareas del evento
         startEventTasks();
         
         // Anunciar inicio del evento
         Bukkit.broadcast(MM.toComponent("<green><bold>¡La Semana Ácida ha comenzado!</bold></green>"));
         Bukkit.broadcast(MM.toComponent("<gray>El agua y la lluvia se han vuelto ácidas...</gray>"));
         
         plugin.getLogger().info("[AcidWeek] Evento iniciado correctamente");
     }
     
     @Override
     protected void onEventStop() {
         plugin.getLogger().info("[AcidWeek] Deteniendo evento de Semana Ácida...");
         
         // Detener tareas
         stopEventTasks();
         
         // Limpiar efectos de jugadores
         cleanupPlayerEffects();
         
         // Guardar datos finales
         saveEventSpecificData();
         
         // Anunciar fin del evento
         Bukkit.broadcast(MM.toComponent("<green><bold>¡La Semana Ácida ha terminado!</bold></green>"));
         showEventSummary();
         
         plugin.getLogger().info("[AcidWeek] Evento detenido correctamente");
     }
     
     /**
      * Limpia los efectos de los jugadores
      */
     private void cleanupPlayerEffects() {
         try {
             for (Player player : Bukkit.getOnlinePlayers()) {
                 // Remover efectos de ácido si los tienen
                 player.removePotionEffect(PotionEffectType.POISON);
                 player.removePotionEffect(PotionEffectType.HUNGER);
                 player.removePotionEffect(PotionEffectType.WEAKNESS);
             }
             
             // Limpiar conjuntos de jugadores
             playersInWater.clear();
             playersInRain.clear();
             acidSwimmers.clear();
             acidRainSurvivors.clear();
             acidResistants.clear();
             
             plugin.getLogger().info("[AcidWeek] Efectos de jugadores limpiados");
         } catch (Exception e) {
             plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al limpiar efectos", e);
         }
     }
     
     /**
      * Muestra un resumen del evento
      */
     private void showEventSummary() {
         try {
             long totalDamage = globalStatistics.get("total_acid_damage").get();
             long playersInWaterCount = globalStatistics.get("players_in_water").get();
             long playersInRainCount = globalStatistics.get("players_in_rain").get();
             
             Bukkit.broadcast(MM.toComponent("<yellow>═══════════════════════════════════════</yellow>"));
             Bukkit.broadcast(MM.toComponent("<gold><bold>RESUMEN DE LA SEMANA ÁCIDA</bold></gold>"));
             Bukkit.broadcast(MM.toComponent("<yellow>═══════════════════════════════════════</yellow>"));
             Bukkit.broadcast(MM.toComponent("<green>Daño total por ácido: <white>" + totalDamage + "</white></green>"));
             Bukkit.broadcast(MM.toComponent("<green>Jugadores en agua ácida: <white>" + playersInWaterCount + "</white></green>"));
             Bukkit.broadcast(MM.toComponent("<green>Jugadores en lluvia ácida: <white>" + playersInRainCount + "</white></green>"));
             Bukkit.broadcast(MM.toComponent("<yellow>═══════════════════════════════════════</yellow>"));
         } catch (Exception e) {
             plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al mostrar resumen", e);
         }
     }
     
     @Override
     protected void processEventStatistics() {
         try {
             // Actualizar estadísticas globales
             globalStatistics.get("total_acid_damage").set(totalAcidDamageDealt.get());
             globalStatistics.get("players_in_water").set(playersInWater.size());
             globalStatistics.get("players_in_rain").set(playersInRain.size());
             
             // Actualizar estadísticas de jugadores
             for (UUID playerId : playerStatistics.keySet()) {
                 Map<String, Object> stats = playerStatistics.get(playerId);
                 @SuppressWarnings("unused")
                 Player player = Bukkit.getPlayer(playerId);
                 
                 stats.put("acid_damage_taken", getPlayerAcidDamage(playerId));
                 stats.put("in_acid_water", playersInWater.contains(playerId));
                 stats.put("in_acid_rain", playersInRain.contains(playerId));
                 stats.put("acid_swimmer", acidSwimmers.contains(playerId));
                 stats.put("acid_rain_survivor", acidRainSurvivors.contains(playerId));
                 stats.put("acid_resistant", acidResistants.contains(playerId));
             }
             
             plugin.getLogger().fine("[AcidWeek] Estadísticas del evento procesadas correctamente");
         } catch (Exception e) {
             plugin.getLogger().log(Level.WARNING, "[AcidWeek] Error al procesar estadísticas del evento", e);
         }
     }
     
     /**
      * Obtiene el daño por ácido de un jugador
      * @param playerId UUID del jugador
      * @return Cantidad de daño por ácido recibido
      */
     private int getPlayerAcidDamage(UUID playerId) {
         return playerAcidDamage.getOrDefault(playerId, new AtomicInteger(0)).get();
     }
     
     /**
      * Obtiene el conjunto de jugadores en agua ácida
      * @return Set de UUIDs de jugadores en agua
      */
     public Set<UUID> getPlayersInWater() {
         return Collections.unmodifiableSet(playersInWater);
     }
     
     /**
      * Obtiene el conjunto de jugadores en lluvia ácida
      * @return Set de UUIDs de jugadores en lluvia
      */
     public Set<UUID> getPlayersInRain() {
         return Collections.unmodifiableSet(playersInRain);
     }
     
     /**
      * Carga los jugadores en agua desde persistencia
      * @param players Set de UUIDs de jugadores
      */
     public void loadPlayersInWater(Set<UUID> players) {
         if (players != null) {
             playersInWater.clear();
             playersInWater.addAll(players);
         }
     }
     
     /**
      * Carga los jugadores en lluvia desde persistencia
      * @param players Set de UUIDs de jugadores
      */
     public void loadPlayersInRain(Set<UUID> players) {
         if (players != null) {
             playersInRain.clear();
             playersInRain.addAll(players);
         }
     }
     
     /**
      * Obtiene el conjunto de jugadores que completaron el desafío de nadador ácido
      * @return Set de UUIDs de jugadores
      */
     public Set<UUID> getAcidSwimmers() {
         return Collections.unmodifiableSet(acidSwimmers);
     }
     
     /**
      * Obtiene el conjunto de jugadores que completaron el desafío de superviviente de lluvia ácida
      * @return Set de UUIDs de jugadores
      */
     public Set<UUID> getAcidRainSurvivors() {
         return Collections.unmodifiableSet(acidRainSurvivors);
     }
     
     /**
      * Obtiene el conjunto de jugadores que completaron el desafío de resistente al ácido
      * @return Set de UUIDs de jugadores
      */
     public Set<UUID> getAcidResistants() {
         return Collections.unmodifiableSet(acidResistants);
     }
     
     /**
      * Carga el progreso de desafíos desde los datos persistentes.
      * 
      * @param data Mapa con el progreso de desafíos serializado
      */
     public void loadChallengeProgress(Map<String, Map<String, Object>> data) {
         if (data != null) {
             challengeProgress.clear();
             data.forEach((playerIdStr, progressMap) -> {
                 try {
                     UUID playerId = UUID.fromString(playerIdStr);
                     challengeProgress.put(playerId, new ConcurrentHashMap<>(progressMap));
                 } catch (IllegalArgumentException e) {
                     plugin.getLogger().warning("[AcidWeek] UUID inválido en challengeProgress: " + playerIdStr);
                 }
             });
             plugin.getLogger().info("[AcidWeek] Progreso de desafíos cargado para " + challengeProgress.size() + " jugadores");
         }
     }
     
     /**
      * Carga los desafíos completados desde los datos persistentes.
      * 
      * @param data Mapa con los desafíos completados serializados
      */
     public void loadCompletedChallenges(Map<String, Set<String>> data) {
         if (data != null) {
             completedChallenges.clear();
             data.forEach((playerIdStr, challengeSet) -> {
                 try {
                     UUID playerId = UUID.fromString(playerIdStr);
                     completedChallenges.put(playerId, ConcurrentHashMap.newKeySet());
                     completedChallenges.get(playerId).addAll(challengeSet);
                 } catch (IllegalArgumentException e) {
                     plugin.getLogger().warning("[AcidWeek] UUID inválido en completedChallenges: " + playerIdStr);
                 }
             });
             plugin.getLogger().info("[AcidWeek] Desafíos completados cargados para " + completedChallenges.size() + " jugadores");
         }
     }
     
     @Override
     protected void saveEventSpecificData() {
         try {
             Map<String, Object> eventSpecificData = new HashMap<>();
             
             // Guardar jugadores en agua y lluvia
             eventSpecificData.put("playersInWater", new HashSet<>(playersInWater));
             eventSpecificData.put("playersInRain", new HashSet<>(playersInRain));
             
             // Guardar progreso de desafíos
             Map<String, Map<String, Object>> challengeProgressSerialized = new HashMap<>();
             challengeProgress.forEach((playerId, progressMap) -> {
                 challengeProgressSerialized.put(playerId.toString(), new HashMap<>(progressMap));
             });
             eventSpecificData.put("challengeProgress", challengeProgressSerialized);
             
             // Guardar desafíos completados
             Map<String, Set<String>> completedChallengesSerialized = new HashMap<>();
             completedChallenges.forEach((playerId, challengeSet) -> {
                 completedChallengesSerialized.put(playerId.toString(), new HashSet<>(challengeSet));
             });
             eventSpecificData.put("completedChallenges", completedChallengesSerialized);
             
             // Marcar datos como modificados y actualizar timestamp
             dataDirty.set(true);
             lastDataSave.set(System.currentTimeMillis());
             
             plugin.getLogger().info("[AcidWeek] Datos específicos del evento guardados");
         } catch (Exception e) {
             plugin.getLogger().severe("[AcidWeek] Error al guardar datos específicos: " + e.getMessage());
             e.printStackTrace();
         }
     }
}