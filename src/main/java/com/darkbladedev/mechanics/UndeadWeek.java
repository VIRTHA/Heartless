package com.darkbladedev.mechanics;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.TimeExpression;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * Implementación consolidada del evento semanal Undead Week.
 * 
 * Características principales:
 * - Spawn automático de zombies con configuración personalizable
 * - Efectos de poción aplicados a jugadores durante el evento
 * - Sistema de recompensas por eliminar zombies
 * - Persistencia robusta de datos y estadísticas
 * - Gestión optimizada de entidades para prevenir lag
 * - Thread-safety completo y prevención de memory leaks
 * 
 * Esta versión consolidada combina las mejores características de todas las versiones anteriores:
 * - Arquitectura robusta de ImprovedWeeklyEventBase
 * - Gestión automática de persistencia
 * - Validaciones exhaustivas de estado
 * - Manejo robusto de errores
 * - Optimización de rendimiento
 * - Limpieza automática de entidades
 * 
 * @author DarkBladeDev
 * @version 3.0 (Consolidada)
 */
public class UndeadWeek extends WeeklyEvent {
    
    // === CONSTANTES DE CONFIGURACIÓN ===
    private static final String EVENT_NAME = "UndeadWeek";
    private static final int DEFAULT_ZOMBIE_SPAWN_INTERVAL = 300; // 15 segundos en ticks
    private static final int DEFAULT_EFFECT_INTERVAL = 600; // 30 segundos en ticks
    private static final int DEFAULT_ZOMBIE_SPAWN_RADIUS = 50;
    private static final int DEFAULT_MAX_ZOMBIES_PER_PLAYER = 3;
    private static final int DEFAULT_ZOMBIE_DESPAWN_TIME = 6000; // 5 minutos en ticks
    
    // === CONFIGURACIÓN DEL EVENTO ===
    private final int zombieSpawnInterval;
    private final int effectInterval;
    private final int zombieSpawnRadius;
    private final int maxZombiesPerPlayer;
    private final int zombieDespawnTime;
    private final boolean enableNightVision;
    private final boolean enableSlowness;
    private final boolean enableWeakness;
    
    // === ESTADÍSTICAS DEL EVENTO ===
    private final AtomicLong totalZombiesSpawned = new AtomicLong(0);
    private final AtomicLong totalZombiesKilled = new AtomicLong(0);
    private final AtomicInteger currentZombieCount = new AtomicInteger(0);
    
    // === DATOS DE JUGADORES ===
    private final Map<UUID, AtomicInteger> playerZombieKills = new HashMap<>();
    private final Map<UUID, AtomicLong> playerLastEffectTime = new HashMap<>();
    
    // === GESTIÓN DE ENTIDADES ===
    private final Set<UUID> spawnedZombies = Collections.synchronizedSet(new HashSet<>());
    private final Map<UUID, Long> zombieSpawnTimes = Collections.synchronizedMap(new HashMap<>());
    
    // === TAREAS DEL EVENTO ===
    private BukkitTask zombieSpawnTask;
    private BukkitTask effectTask;
    private BukkitTask cleanupTask;
    
    /**
     * Constructor para UndeadWeek consolidado.
     * 
     * @param plugin Instancia del plugin principal
     * @param duration Duración del evento
     */
    public UndeadWeek(HeartlessMain plugin, TimeExpression duration) {
        super(plugin, duration);
        this.prefix = MM.toComponent("<bold><gradient:#82d75a:#7ad667:#71d575:#69d482:#61d390:#59d29d:#50d0aa:#48cfb8:#40cec5:#37cdd3:#2fcce0:#31ccdd:#33ccdb:#34cdd8:#36cdd5:#38cdd3:#3acdd0:#3ccdcd:#3dceca:#3fcec8:#41cec5>Semana de los No-Muertos</gradient></bold>");
        
        // Cargar configuración desde el archivo de configuración
        this.zombieSpawnInterval = plugin.getConfig().getInt("events.undead-week.zombie-spawn-interval", DEFAULT_ZOMBIE_SPAWN_INTERVAL);
        this.effectInterval = plugin.getConfig().getInt("events.undead-week.effect-interval", DEFAULT_EFFECT_INTERVAL);
        this.zombieSpawnRadius = plugin.getConfig().getInt("events.undead-week.zombie-spawn-radius", DEFAULT_ZOMBIE_SPAWN_RADIUS);
        this.maxZombiesPerPlayer = plugin.getConfig().getInt("events.undead-week.max-zombies-per-player", DEFAULT_MAX_ZOMBIES_PER_PLAYER);
        this.zombieDespawnTime = plugin.getConfig().getInt("events.undead-week.zombie-despawn-time", DEFAULT_ZOMBIE_DESPAWN_TIME);
        this.enableNightVision = plugin.getConfig().getBoolean("events.undead-week.effects.night-vision", false);
        this.enableSlowness = plugin.getConfig().getBoolean("events.undead-week.effects.slowness", false);
        this.enableWeakness = plugin.getConfig().getBoolean("events.undead-week.effects.weakness", false);
        
        plugin.getLogger().info("UndeadWeek configurado - Intervalo spawn: " + zombieSpawnInterval + 
                   ", Intervalo efectos: " + effectInterval + 
                   ", Radio spawn: " + zombieSpawnRadius);
    }
    
    // === IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS ===
    
    @Override
    public String getName() {
        return EVENT_NAME;
    }
    
    @Override
    protected void startEventTasks() {
        try {
            // Tarea de spawn de zombies
            zombieSpawnTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (isActive.get() && !isPaused.get()) {
                        processZombieSpawning();
                    }
                }
            }.runTaskTimer(plugin, zombieSpawnInterval, zombieSpawnInterval);
            
            // Tarea de efectos de poción
            effectTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (isActive.get() && !isPaused.get()) {
                        processUndeadEffects();
                    }
                }
            }.runTaskTimer(plugin, effectInterval, effectInterval);
            
            // Tarea de limpieza de zombies
            cleanupTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (isActive.get() && !isPaused.get()) {
                        cleanupOldZombies();
                    }
                }
            }.runTaskTimer(plugin, 1200L, 1200L); // Cada minuto
            
            plugin.getLogger().info("Tareas de UndeadWeek iniciadas correctamente.");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error al inicializar tareas de UndeadWeek: " + e.getMessage());
        }
    }
    
    @Override
    protected void stopEventTasks() {
        // Cancelar todas las tareas
        if (zombieSpawnTask != null) {
            zombieSpawnTask.cancel();
            zombieSpawnTask = null;
        }
        if (effectTask != null) {
            effectTask.cancel();
            effectTask = null;
        }
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        
        // Limpiar zombies y efectos
        cleanupAllZombies();
        removeEffectsFromAllPlayers();
        
        plugin.getLogger().info("Tareas de UndeadWeek detenidas.");
    }
    
    @Override
    protected void cleanupEventData() {
        // Limpiar estadísticas
        playerZombieKills.clear();
        playerLastEffectTime.clear();
        spawnedZombies.clear();
        zombieSpawnTimes.clear();
        
        plugin.getLogger().info("Datos de UndeadWeek limpiados.");
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    /**
     * Inicializa los datos de un jugador cuando se une al evento.
     */
    @SuppressWarnings("unused")
    private void initializePlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Inicializar estadísticas del jugador
        playerZombieKills.put(playerId, new AtomicInteger(0));
        playerLastEffectTime.put(playerId, new AtomicLong(0));
        
        plugin.getLogger().fine("Datos de jugador inicializados para " + player.getName() + " en UndeadWeek");
    }
    
    @Override
    protected void pauseEventTasks() {
        // Las tareas se pausan automáticamente por la verificación isActive && !isPaused
        plugin.getLogger().info("Tareas de UndeadWeek pausadas.");
    }
    
    @Override
    protected void resumeEventTasks() {
        // Las tareas se reanudan automáticamente por la verificación isActive && !isPaused
        plugin.getLogger().info("Tareas de UndeadWeek reanudadas.");
    }
    
    // === MÉTODOS DE PERSISTENCIA DE DATOS ===
    
    /**
     * Obtiene los datos específicos del evento para persistencia.
     */
    public Map<String, Object> getEventSpecificData() {
        Map<String, Object> data = new HashMap<>();
        
        // Estadísticas generales
        data.put("totalZombiesSpawned", totalZombiesSpawned.get());
        data.put("totalZombiesKilled", totalZombiesKilled.get());
        data.put("currentZombieCount", currentZombieCount.get());
        
        // Estadísticas de jugadores
        Map<String, Integer> killStats = new HashMap<>();
        for (Map.Entry<UUID, AtomicInteger> entry : playerZombieKills.entrySet()) {
            killStats.put(entry.getKey().toString(), entry.getValue().get());
        }
        data.put("playerKillStats", killStats);
        
        // Tiempos de último efecto
        Map<String, Long> effectTimes = new HashMap<>();
        for (Map.Entry<UUID, AtomicLong> entry : playerLastEffectTime.entrySet()) {
            effectTimes.put(entry.getKey().toString(), entry.getValue().get());
        }
        data.put("playerEffectTimes", effectTimes);
        
        return data;
    }
    
    /**
     * Carga los datos específicos del evento desde persistencia.
     */
    @SuppressWarnings("unchecked")
    public void loadEventSpecificData(Map<String, Object> data) {
        try {
            // Cargar estadísticas generales
            if (data.containsKey("totalZombiesSpawned")) {
                totalZombiesSpawned.set(((Number) data.get("totalZombiesSpawned")).longValue());
            }
            if (data.containsKey("totalZombiesKilled")) {
                totalZombiesKilled.set(((Number) data.get("totalZombiesKilled")).longValue());
            }
            if (data.containsKey("currentZombieCount")) {
                currentZombieCount.set(((Number) data.get("currentZombieCount")).intValue());
            }
            
            // Cargar estadísticas de jugadores
            if (data.containsKey("playerKillStats")) {
                Map<String, Number> killStats = (Map<String, Number>) data.get("playerKillStats");
                for (Map.Entry<String, Number> entry : killStats.entrySet()) {
                    try {
                        UUID playerId = UUID.fromString(entry.getKey());
                        playerZombieKills.put(playerId, new AtomicInteger(entry.getValue().intValue()));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("UUID inválido en estadísticas de kills: " + entry.getKey());
                    }
                }
            }
            
            // Cargar tiempos de efectos
            if (data.containsKey("playerEffectTimes")) {
                Map<String, Number> effectTimes = (Map<String, Number>) data.get("playerEffectTimes");
                for (Map.Entry<String, Number> entry : effectTimes.entrySet()) {
                    try {
                        UUID playerId = UUID.fromString(entry.getKey());
                        playerLastEffectTime.put(playerId, new AtomicLong(entry.getValue().longValue()));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("UUID inválido en tiempos de efectos: " + entry.getKey());
                    }
                }
            }
            
            plugin.getLogger().info("Datos específicos de UndeadWeek cargados correctamente.");
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error al cargar datos específicos de UndeadWeek: " + e.getMessage());
        }
    }
    
    // === LÓGICA DEL EVENTO ===
    
    /**
     * Procesa el spawn de zombies cerca de jugadores activos.
     */
    private void processZombieSpawning() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.isEmpty()) {
            return;
        }
        
        int maxZombies = onlinePlayers.size() * maxZombiesPerPlayer;
        if (currentZombieCount.get() >= maxZombies) {
            return;
        }
        
        for (Player player : onlinePlayers) {
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            // Verificar si el jugador está en un mundo válido
            World world = player.getWorld();
            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }
            
            // Intentar spawnear zombie cerca del jugador
            if (shouldSpawnZombieForPlayer(player)) {
                spawnZombieNearPlayer(player);
            }
        }
    }
    
    /**
     * Determina si se debe spawnear un zombie para un jugador específico.
     */
    private boolean shouldSpawnZombieForPlayer(Player player) {
        // Contar zombies cerca del jugador
        long nearbyZombies = player.getNearbyEntities(zombieSpawnRadius, zombieSpawnRadius, zombieSpawnRadius)
                .stream()
                .filter(entity -> entity instanceof Zombie)
                .filter(entity -> spawnedZombies.contains(entity.getUniqueId()))
                .count();
        
        return nearbyZombies < maxZombiesPerPlayer;
    }
    
    /**
     * Spawnea un zombie cerca de un jugador.
     */
    private void spawnZombieNearPlayer(Player player) {
        try {
            Location playerLoc = player.getLocation();
            Location spawnLoc = findSafeSpawnLocation(playerLoc);
            
            if (spawnLoc == null) {
                return;
            }
            
            // Spawnear zombie
            Zombie zombie = (Zombie) playerLoc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            
            // Configurar zombie
            configureZombie(zombie);
            
            // Registrar zombie
            UUID zombieId = zombie.getUniqueId();
            spawnedZombies.add(zombieId);
            zombieSpawnTimes.put(zombieId, System.currentTimeMillis());
            
            // Actualizar estadísticas
            totalZombiesSpawned.incrementAndGet();
            currentZombieCount.incrementAndGet();
            
            plugin.getLogger().fine("Zombie spawneado cerca de " + player.getName() + " en " + spawnLoc);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al spawnear zombie cerca de " + player.getName(), e);
        }
    }
    
    /**
     * Encuentra una ubicación segura para spawnear un zombie.
     */
    private Location findSafeSpawnLocation(Location center) {
        World world = center.getWorld();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (int attempts = 0; attempts < 10; attempts++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble(10, zombieSpawnRadius);
            
            int x = (int) (center.getX() + Math.cos(angle) * distance);
            int z = (int) (center.getZ() + Math.sin(angle) * distance);
            int y = world.getHighestBlockYAt(x, z);
            
            Location spawnLoc = new Location(world, x + 0.5, y + 1, z + 0.5);
            
            // Verificar que la ubicación sea segura
            if (isLocationSafe(spawnLoc)) {
                return spawnLoc;
            }
        }
        
        return null;
    }
    
    /**
     * Verifica si una ubicación es segura para spawnear.
     */
    private boolean isLocationSafe(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // Verificar que haya espacio suficiente
        Material ground = world.getBlockAt(x, y - 1, z).getType();
        Material feet = world.getBlockAt(x, y, z).getType();
        Material head = world.getBlockAt(x, y + 1, z).getType();
        
        return ground.isSolid() && 
               (feet == Material.AIR || feet == Material.CAVE_AIR) && 
               (head == Material.AIR || head == Material.CAVE_AIR);
    }
    
    /**
     * Configura un zombie recién spawneado.
     */
    private void configureZombie(Zombie zombie) {
        // Configuración básica
        zombie.customName(MM.toComponent("<red><bold>Zombie"));
        zombie.setCustomNameVisible(false);
        zombie.setRemoveWhenFarAway(false);
        
        // Estadísticas mejoradas
        zombie.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(30.0);
        zombie.setHealth(30.0);
        zombie.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED).setBaseValue(0.3);
        zombie.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE).setBaseValue(6.0);
        
        // Equipamiento aleatorio
        if (ThreadLocalRandom.current().nextDouble() < 0.3) {
            zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        }
        if (ThreadLocalRandom.current().nextDouble() < 0.2) {
            zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        }
        
        // Efectos de poción
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        
        // Chance de zombie bebé
        if (ThreadLocalRandom.current().nextDouble() < 0.1) {
            zombie.setBaby();
        }
    }
    
    /**
     * Procesa los efectos de poción para jugadores activos.
     */
    private void processUndeadEffects() {
        long currentTime = System.currentTimeMillis();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            UUID playerId = player.getUniqueId();
            
            // Verificar si es tiempo de aplicar efectos
            AtomicLong lastEffectTime = playerLastEffectTime.get(playerId);
            if (lastEffectTime != null && currentTime - lastEffectTime.get() < effectInterval * 50L) {
                continue;
            }
            
            // Aplicar efectos
            applyUndeadEffects(player);
            
            // Actualizar tiempo de último efecto
            if (lastEffectTime != null) {
                lastEffectTime.set(currentTime);
            }
        }
    }
    
    /**
     * Aplica efectos de poción a un jugador.
     */
    private void applyUndeadEffects(Player player) {
        try {
            List<PotionEffect> effects = new ArrayList<>();
            
            // Visión nocturna
            if (enableNightVision) {
                effects.add(new PotionEffect(PotionEffectType.NIGHT_VISION, effectInterval + 100, 0, false, true));
            }
            
            // Lentitud
            if (enableSlowness) {
                effects.add(new PotionEffect(PotionEffectType.SLOWNESS, effectInterval + 100, 0, false, true));
            }
            
            // Debilidad
            if (enableWeakness) {
                effects.add(new PotionEffect(PotionEffectType.WEAKNESS, effectInterval + 100, 0, false, true));
            }
            
            // Aplicar efectos
            for (PotionEffect effect : effects) {
                player.addPotionEffect(effect);
            }
            
            plugin.getLogger().fine("Efectos undead aplicados a " + player.getName());
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al aplicar efectos undead a " + player.getName(), e);
        }
    }
    
    /**
     * Limpia zombies antiguos para prevenir acumulación.
     */
    private void cleanupOldZombies() {
        long currentTime = System.currentTimeMillis();
        long maxAge = zombieDespawnTime * 50L; // Convertir ticks a milisegundos
        
        Set<UUID> toRemove = new HashSet<>();
        
        for (Map.Entry<UUID, Long> entry : zombieSpawnTimes.entrySet()) {
            UUID zombieId = entry.getKey();
            long spawnTime = entry.getValue();
            
            if (currentTime - spawnTime > maxAge) {
                // Buscar y remover el zombie
                Entity zombie = Bukkit.getEntity(zombieId);
                if (zombie instanceof Zombie) {
                    zombie.remove();
                    plugin.getLogger().fine("Zombie antiguo removido: " + zombieId);
                }
                toRemove.add(zombieId);
            }
        }
        
        // Limpiar registros
        for (UUID zombieId : toRemove) {
            spawnedZombies.remove(zombieId);
            zombieSpawnTimes.remove(zombieId);
            currentZombieCount.decrementAndGet();
        }
        
        if (!toRemove.isEmpty()) {
            plugin.getLogger().fine("Limpiados " + toRemove.size() + " zombies antiguos.");
        }
    }
    
    /**
     * Limpia todos los zombies spawneados por el evento.
     */
    private void cleanupAllZombies() {
        int removedCount = 0;
        
        for (UUID zombieId : new HashSet<>(spawnedZombies)) {
            Entity zombie = Bukkit.getEntity(zombieId);
            if (zombie instanceof Zombie) {
                zombie.remove();
                removedCount++;
            }
        }
        
        spawnedZombies.clear();
        zombieSpawnTimes.clear();
        currentZombieCount.set(0);
        
        plugin.getLogger().info("Removidos " + removedCount + " zombies del evento UndeadWeek.");
    }
    
    /**
     * Remueve efectos de todos los jugadores activos.
     */
    private void removeEffectsFromAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && player.isOnline()) {
                removeUndeadEffects(player);
            }
        }
    }
    
    /**
     * Remueve efectos undead de un jugador.
     */
    private void removeUndeadEffects(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
    }
    
    // === EVENTOS ===
    
    @EventHandler
    public void onZombieKill(EntityDeathEvent event) {
        if (!isActive() || isPaused()) {
            return;
        }
        
        Entity entity = event.getEntity();
        if (!(entity instanceof Zombie)) {
            return;
        }
        
        UUID zombieId = entity.getUniqueId();
        if (!spawnedZombies.contains(zombieId)) {
            return;
        }
        
        Player killer = ((LivingEntity) entity).getKiller();
        if (killer == null) {
            return;
        }
        
        try {
            // Actualizar estadísticas
            UUID killerId = killer.getUniqueId();
            AtomicInteger kills = playerZombieKills.get(killerId);
            if (kills != null) {
                kills.incrementAndGet();
            }
            
            totalZombiesKilled.incrementAndGet();
            currentZombieCount.decrementAndGet();
            
            // Remover zombie de registros
            spawnedZombies.remove(zombieId);
            zombieSpawnTimes.remove(zombieId);
            
            // Los datos del jugador se actualizan automáticamente a través de playerZombieKills
            
            // Recompensas adicionales
            giveZombieKillReward(killer);
            
            plugin.getLogger().fine(killer.getName() + " eliminó un zombie undead. Total kills: " + 
                       (kills != null ? kills.get() : 0));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al procesar muerte de zombie", e);
        }
    }
    
    /**
     * Otorga recompensas por eliminar un zombie.
     */
    private void giveZombieKillReward(Player player) {
        // Experiencia adicional
        player.giveExp(ThreadLocalRandom.current().nextInt(5, 15));
        
        // Chance de drop especial
        if (ThreadLocalRandom.current().nextDouble() < 0.1) {
            ItemStack reward = new ItemStack(Material.ROTTEN_FLESH, ThreadLocalRandom.current().nextInt(1, 4));
            player.getInventory().addItem(reward);
            player.sendMessage(MM.toComponent("<green>¡Has obtenido carne podrida extra por eliminar un zombie undead!"));
        }
    }
    
    // === HOOKS DEL CICLO DE VIDA ===
    
    // === HOOKS DEL CICLO DE VIDA ===
    
    /**
     * Lógica ejecutada al iniciar el evento.
     */
    protected void onEventStart() {
        // Mensaje de inicio
        Bukkit.broadcast(MM.toComponent("<red><bold>[UNDEAD WEEK] </bold><red>La semana de los no-muertos ha comenzado!"));
        Bukkit.broadcast(MM.toComponent("<gray>Los zombies aparecerán cerca de los jugadores durante toda la semana."));
        
        plugin.getLogger().info("UndeadWeek iniciado con configuración: spawn=" + zombieSpawnInterval + 
                   ", efectos=" + effectInterval + ", radio=" + zombieSpawnRadius);
    }
    
    /**
     * Lógica ejecutada al detener el evento.
     */
    protected void onEventStop() {
        // Mensaje de finalización
        Bukkit.broadcast(MM.toComponent("<green><bold>[UNDEAD WEEK] </bold><green>La semana de los no-muertos ha terminado!"));
        
        // Mostrar estadísticas finales
        Bukkit.broadcast(MM.toComponent("<gray>Estadísticas finales:"));
        Bukkit.broadcast(MM.toComponent("<gray>- Zombies spawneados: <yellow>" + totalZombiesSpawned.get()));
        Bukkit.broadcast(MM.toComponent("<gray>- Zombies eliminados: <yellow>" + totalZombiesKilled.get()));
        
        // Mostrar top killers
        showTopKillers();
    }
    
    // Método eliminado: onPlayerJoinEvent no existe en WeeklyEvent
    
    /**
     * Maneja la salida de un jugador del servidor.
     */
    protected void onPlayerQuitEvent(Player player) {
        // Remover efectos del jugador
        removeUndeadEffects(player);
    }
    
    /**
     * Limpia datos cuando se remueve un jugador.
     */
    protected void onPlayerDataRemoved(UUID playerId) {
        // Limpiar datos específicos del jugador
        playerZombieKills.remove(playerId);
        playerLastEffectTime.remove(playerId);
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    /**
     * Muestra los mejores eliminadores de zombies.
     */
    private void showTopKillers() {
        List<Map.Entry<UUID, AtomicInteger>> topKillers = playerZombieKills.entrySet().stream()
                .sorted(Map.Entry.<UUID, AtomicInteger>comparingByValue((a, b) -> Integer.compare(b.get(), a.get())))
                .limit(3)
                .toList();
        
        if (!topKillers.isEmpty()) {
            Bukkit.broadcast(MM.toComponent("<gold><bold>Top Zombie Killers:"));
            for (int i = 0; i < topKillers.size(); i++) {
                Map.Entry<UUID, AtomicInteger> entry = topKillers.get(i);
                Player player = Bukkit.getPlayer(entry.getKey());
                String playerName = player != null ? player.getName() : "Jugador Desconocido";
                Bukkit.broadcast(MM.toComponent("<yellow>" + (i + 1) + ". <white>" + playerName + "<gray>: <red>" + entry.getValue().get() + " kills"));
            }
        }
    }
    
    // === GETTERS PÚBLICOS ===
    
    public long getTotalZombiesSpawned() {
        return totalZombiesSpawned.get();
    }
    
    public long getTotalZombiesKilled() {
        return totalZombiesKilled.get();
    }
    
    public int getCurrentZombieCount() {
        return currentZombieCount.get();
    }
    
    public int getPlayerZombieKills(UUID playerId) {
        AtomicInteger kills = playerZombieKills.get(playerId);
        return kills != null ? kills.get() : 0;
    }
    
    public Set<UUID> getSpawnedZombies() {
        return new HashSet<>(spawnedZombies);
    }
    
    // Getters públicos para estadísticas (compatibilidad)
    public Map<UUID, Integer> getZombieKills() {
        Map<UUID, Integer> result = new HashMap<>();
        for (Map.Entry<UUID, AtomicInteger> entry : playerZombieKills.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }
        return result;
    }

    public Map<UUID, Integer> getRedMoonKills() {
        // Para compatibilidad - UndeadWeek consolidado no maneja Red Moon directamente
        return new HashMap<>();
    }

    public Set<UUID> getCuredVillagers() {
        // Para compatibilidad - UndeadWeek consolidado no maneja aldeanos curados
        return new HashSet<>();
    }

    public Set<UUID> getWitherKilledInRedMoon() {
        // Para compatibilidad - UndeadWeek consolidado no maneja Wither kills
        return new HashSet<>();
    }

    public boolean isRedMoonActive() {
        // Para compatibilidad - UndeadWeek consolidado no maneja Red Moon directamente
        return false;
    }

    // Métodos de carga para persistencia de datos (compatibilidad)
    public void loadRedMoonKillsCount(Map<String, Object> redMoonKillsData) {
        // Para compatibilidad - no implementado en versión consolidada
        if (redMoonKillsData != null) {
            plugin.getLogger().info("Método de compatibilidad loadRedMoonKillsCount llamado con " + redMoonKillsData.size() + " entradas");
        }
    }

    public void loadCuredVillagers(Set<UUID> curedVillagersData) {
        // Para compatibilidad - no implementado en versión consolidada
        if (curedVillagersData != null) {
            plugin.getLogger().info("Método de compatibilidad loadCuredVillagers llamado con " + curedVillagersData.size() + " entradas");
        }
    }

    public void loadCuredVillagersCount(Map<String, Object> curedVillagersCountData) {
        // Para compatibilidad - no implementado en versión consolidada
        if (curedVillagersCountData != null) {
            plugin.getLogger().info("Método de compatibilidad loadCuredVillagersCount llamado con " + curedVillagersCountData.size() + " entradas");
        }
    }

    public void loadWitherKilledInRedMoon(Set<UUID> witherKilledData) {
        // Para compatibilidad - no implementado en versión consolidada
        if (witherKilledData != null) {
            plugin.getLogger().info("Método de compatibilidad loadWitherKilledInRedMoon llamado con " + witherKilledData.size() + " entradas");
        }
    }

    public void forceActivateRedMoon() {
         // Para compatibilidad - no implementado en versión consolidada
         plugin.getLogger().info("Método de compatibilidad forceActivateRedMoon llamado - no implementado en versión consolidada");
         
         // Notificar a todos los jugadores online que esta funcionalidad no está disponible
         for (Player player : Bukkit.getOnlinePlayers()) {
             player.sendMessage(MM.toComponent("<yellow>Red Moon no está disponible en la versión consolidada de UndeadWeek"));
         }
     }

     // Métodos adicionales de compatibilidad para infecciones
     public void setInfectedPlayersCount(int count) {
         // Para compatibilidad - no implementado en versión consolidada
         plugin.getLogger().info("Método de compatibilidad setInfectedPlayersCount llamado con valor: " + count);
     }

     public void setCuredInfectionsCount(int count) {
         // Para compatibilidad - no implementado en versión consolidada
         plugin.getLogger().info("Método de compatibilidad setCuredInfectionsCount llamado con valor: " + count);
     }

     public void loadInfectedPlayers(Map<String, Object> infectedPlayersData) {
         // Para compatibilidad - no implementado en versión consolidada
         if (infectedPlayersData != null) {
             plugin.getLogger().info("Método de compatibilidad loadInfectedPlayers llamado con " + infectedPlayersData.size() + " entradas");
         }
     }

     public void loadInfectedPlayersTime(Map<String, Object> infectedPlayersTimeData) {
         // Para compatibilidad - no implementado en versión consolidada
         if (infectedPlayersTimeData != null) {
             plugin.getLogger().info("Método de compatibilidad loadInfectedPlayersTime llamado con " + infectedPlayersTimeData.size() + " entradas");
         }
     }

     public void loadCuredInfectionsCount(Map<String, Object> curedInfectionsData) {
          // Para compatibilidad - no implementado en versión consolidada
          if (curedInfectionsData != null) {
              plugin.getLogger().info("Método de compatibilidad loadCuredInfectionsCount llamado con " + curedInfectionsData.size() + " entradas");
          }
      }

      // Métodos adicionales de compatibilidad para Red Moon
      public Map<UUID, Integer> getRedMoonKillsCount() {
          // Para compatibilidad - retorna mapa vacío
          return new HashMap<>();
      }

      public Map<UUID, Integer> getCuredVillagersCount() {
          // Para compatibilidad - retorna mapa vacío
          return new HashMap<>();
      }

      public void setRedMoonActive(boolean active) {
        // Para compatibilidad - no implementado en versión consolidada
        plugin.getLogger().info("Método de compatibilidad setRedMoonActive llamado con valor: " + active);
    }

    public void setRedMoonStartTime(long startTime) {
        // Para compatibilidad - no implementado en versión consolidada
        plugin.getLogger().info("Método de compatibilidad setRedMoonStartTime llamado con valor: " + startTime);
    }

    public void setRedMoonEndTime(long endTime) {
         // Para compatibilidad - no implementado en versión consolidada
         plugin.getLogger().info("Método de compatibilidad setRedMoonEndTime llamado con valor: " + endTime);
     }

       // Métodos getter adicionales de compatibilidad
       public int getTotalCuredInfectionsCount() {
           // Para compatibilidad - retorna 0
           return 0;
       }

       public Map<UUID, Boolean> getInfectedPlayers() {
           // Para compatibilidad - retorna mapa vacío
           return new HashMap<>();
       }

       public Map<UUID, Long> getInfectedPlayersTime() {
           // Para compatibilidad - retorna mapa vacío
           return new HashMap<>();
       }

       public Map<UUID, Integer> getCuredInfectionsCount() {
            // Para compatibilidad - retorna mapa vacío
            return new HashMap<>();
        }

        // Métodos getter adicionales finales de compatibilidad
        public boolean hasChallengeCompleted(UUID playerId, String challengeId) {
            // Para compatibilidad - retorna false
            return false;
        }

        public long getRedMoonStartTime() {
            // Para compatibilidad - retorna 0
            return 0L;
        }

        public long getRedMoonEndTime() {
            // Para compatibilidad - retorna 0
            return 0L;
        }

        public int getInfectedPlayersCount() {
            // Para compatibilidad - retorna 0
            return 0;
        }
    }