package com.darkbladedev.mechanics;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.TimeExpression;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * Versión refactorizada de UndeadWeek usando AbstractWeeklyEvent.
 * Demuestra cómo implementar los patrones estandarizados.
 * 
 * Esta implementación corrige los problemas identificados en la auditoría:
 * - Inicialización thread-safe de tareas
 * - Validación consistente de estado
 * - Manejo robusto de errores
 * - Limpieza automática de recursos
 * 
 * @author DarkBladeDev
 * @since 1.0
 */
public class UndeadWeekRefactored extends AbstractWeeklyEvent implements Listener {
    
    // Tareas específicas de UndeadWeek
    private final AtomicReference<BukkitRunnable> zombieSpawnTask = new AtomicReference<>();
    private final AtomicReference<BukkitRunnable> effectTask = new AtomicReference<>();
    
    // Datos específicos del evento
    private final ConcurrentHashMap<Player, Integer> playerZombieKills = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, Long> lastZombieSpawn = new ConcurrentHashMap<>();
    
    // Configuración del evento
    private static final int ZOMBIE_SPAWN_INTERVAL = 20 * 30; // 30 segundos
    private static final int EFFECT_INTERVAL = 20 * 10; // 10 segundos
    private static final int MAX_ZOMBIES_PER_PLAYER = 3;
    
    /**
     * Constructor para UndeadWeek refactorizado.
     * 
     * @param plugin Instancia del plugin principal
     */
    public UndeadWeekRefactored(HeartlessMain plugin) {
        super(plugin);
        
        // Registrar eventos
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public String getName() {
        return "UndeadWeek";
    }
    
    @Override
    protected void initializeEventTasks() {
        // Inicializar tarea de spawn de zombies
        startZombieSpawnTask();
        
        // Inicializar tarea de efectos
        startEffectTask();
        
        logger.info("Tareas de UndeadWeek inicializadas correctamente.");
    }
    
    @Override
    protected void initializePlayerData(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Inicializar datos específicos del jugador
        playerZombieKills.put(player, 0);
        lastZombieSpawn.put(player, System.currentTimeMillis());
        
        // Aplicar efectos iniciales
        applyUndeadEffects(player);
    }
    
    @Override
    protected void pauseEventTasks() {
        // Las tareas se pausarán automáticamente al verificar canExecute()
        logger.info("Tareas de UndeadWeek pausadas.");
    }
    
    @Override
    protected void resumeEventTasks() {
        // Las tareas se reanudarán automáticamente al verificar canExecute()
        logger.info("Tareas de UndeadWeek reanudadas.");
    }
    
    @Override
    protected void stopAdditionalTasks() {
        // Cancelar tareas específicas de UndeadWeek
        cancelZombieSpawnTask();
        cancelEffectTask();
    }
    
    @Override
    protected void cleanupAdditionalData() {
        // Limpiar datos específicos del evento
        playerZombieKills.clear();
        lastZombieSpawn.clear();
        
        // Remover efectos de jugadores
        for (Player player : activePlayers) {
            if (player != null && player.isOnline()) {
                removeUndeadEffects(player);
            }
        }
    }
    
    /**
     * Inicia la tarea de spawn de zombies.
     */
    private void startZombieSpawnTask() {
        // Cancelar tarea existente si existe
        cancelZombieSpawnTask();
        
        try {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!canExecute()) {
                        return;
                    }
                    
                    try {
                        processZombieSpawning();
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error en tarea de spawn de zombies: " + e.getMessage(), e);
                    }
                }
            };
            
            // Programar la tarea
            task.runTaskTimer(plugin, 0L, ZOMBIE_SPAWN_INTERVAL);
            zombieSpawnTask.set(task);
            
            logger.info("Tarea de spawn de zombies iniciada.");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al iniciar tarea de spawn de zombies: " + e.getMessage(), e);
            throw new RuntimeException("Fallo al iniciar tarea de spawn", e);
        }
    }
    
    /**
     * Inicia la tarea de efectos.
     */
    private void startEffectTask() {
        // Cancelar tarea existente si existe
        cancelEffectTask();
        
        try {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!canExecute()) {
                        return;
                    }
                    
                    try {
                        processUndeadEffects();
                        cleanupDisconnectedPlayers();
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error en tarea de efectos: " + e.getMessage(), e);
                    }
                }
            };
            
            // Programar la tarea
            task.runTaskTimer(plugin, 0L, EFFECT_INTERVAL);
            effectTask.set(task);
            
            logger.info("Tarea de efectos iniciada.");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al iniciar tarea de efectos: " + e.getMessage(), e);
            throw new RuntimeException("Fallo al iniciar tarea de efectos", e);
        }
    }
    
    /**
     * Cancela la tarea de spawn de zombies de manera segura.
     */
    private void cancelZombieSpawnTask() {
        BukkitRunnable task = zombieSpawnTask.get();
        if (task != null && !task.isCancelled()) {
            try {
                task.cancel();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error al cancelar tarea de spawn: " + e.getMessage(), e);
            } finally {
                zombieSpawnTask.set(null);
            }
        }
    }
    
    /**
     * Cancela la tarea de efectos de manera segura.
     */
    private void cancelEffectTask() {
        BukkitRunnable task = effectTask.get();
        if (task != null && !task.isCancelled()) {
            try {
                task.cancel();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error al cancelar tarea de efectos: " + e.getMessage(), e);
            } finally {
                effectTask.set(null);
            }
        }
    }
    
    /**
     * Procesa el spawn de zombies para jugadores activos.
     */
    private void processZombieSpawning() {
        for (Player player : activePlayers) {
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            try {
                // Verificar cooldown
                Long lastSpawn = lastZombieSpawn.get(player);
                if (lastSpawn != null && (System.currentTimeMillis() - lastSpawn) < (ZOMBIE_SPAWN_INTERVAL * 50)) {
                    continue;
                }
                
                // Verificar límite de zombies
                long nearbyZombies = player.getNearbyEntities(10, 10, 10).stream()
                    .filter(entity -> entity instanceof Zombie)
                    .count();
                
                if (nearbyZombies >= MAX_ZOMBIES_PER_PLAYER) {
                    continue;
                }
                
                // Spawn zombie
                Zombie zombie = player.getWorld().spawn(player.getLocation().add(0, 0, 2), Zombie.class);
                zombie.setTarget(player);
                zombie.setCustomName("§cZombie de " + player.getName());
                zombie.setCustomNameVisible(true);
                
                // Actualizar timestamp
                lastZombieSpawn.put(player, System.currentTimeMillis());
                
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error al spawn zombie para " + player.getName() + ": " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Procesa los efectos de undead para jugadores activos.
     */
    private void processUndeadEffects() {
        for (Player player : activePlayers) {
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            try {
                applyUndeadEffects(player);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error al aplicar efectos a " + player.getName() + ": " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Aplica efectos de undead a un jugador.
     * 
     * @param player Jugador al que aplicar efectos
     */
    private void applyUndeadEffects(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Aplicar efectos basados en el tiempo (noche = más fuertes)
        long time = player.getWorld().getTime();
        boolean isNight = time > 13000 && time < 23000;
        
        if (isNight) {
            // Efectos nocturnos más fuertes
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, EFFECT_INTERVAL + 20, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_INTERVAL + 20, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, EFFECT_INTERVAL + 20, 0, false, false));
        } else {
            // Efectos diurnos más débiles
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, EFFECT_INTERVAL + 20, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, EFFECT_INTERVAL + 20, 0, false, false));
        }
    }
    
    /**
     * Remueve efectos de undead de un jugador.
     * 
     * @param player Jugador del que remover efectos
     */
    private void removeUndeadEffects(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
    }
    
    // Event Handlers
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isActive()) {
            return;
        }
        
        Player player = event.getPlayer();
        activePlayers.add(player);
        initializePlayerData(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        activePlayers.remove(player);
        playerData.remove(player);
        playerZombieKills.remove(player);
        lastZombieSpawn.remove(player);
    }
    
    @EventHandler
    public void onZombieKill(EntityDamageByEntityEvent event) {
        if (!isActive() || isPaused()) {
            return;
        }
        
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Zombie)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        Zombie zombie = (Zombie) event.getEntity();
        
        // Verificar si el zombie muere
        if (zombie.getHealth() - event.getFinalDamage() <= 0) {
            // Incrementar contador de kills
            int kills = playerZombieKills.getOrDefault(player, 0) + 1;
            playerZombieKills.put(player, kills);
            
            // Recompensa por kill
            if (kills % 5 == 0) {
                // Cada 5 kills, dar recompensa
                ItemStack reward = new ItemStack(Material.GOLDEN_APPLE, 1);
                player.getInventory().addItem(reward);
                player.sendMessage("§6¡Has matado " + kills + " zombies! Recompensa obtenida.");
            }
        }
    }
    
    /**
     * Obtiene el número de zombies matados por un jugador.
     * 
     * @param player Jugador a consultar
     * @return Número de zombies matados
     */
    public int getZombieKills(Player player) {
        return playerZombieKills.getOrDefault(player, 0);
    }
}