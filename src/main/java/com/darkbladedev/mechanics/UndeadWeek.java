package com.darkbladedev.mechanics;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.managers.CustomEffectsManager;
import com.darkbladedev.content.semi_custom.effects.ZombieInfection;
import com.darkbladedev.utils.TimeExpression;
import com.darkbladedev.utils.MM;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Evento semanal de No-Muertos que introduce mecánicas de infección zombie,
 * luna roja y desafíos especiales relacionados con zombies.
 * 
 * @author DarkBladeDev
 * @version 2.0
 */
public class UndeadWeek extends AbstractWeeklyEvent {
    
    // === CONSTANTES DEL EVENTO ===
    private static final String EVENT_ID = "undead_week";
    private static final int POISON_DURATION = 30 * 20; // 30 segundos en ticks
    private static final int RED_MOON_DURATION = 7 * 60 * 1000; // 7 minutos en ms
    private static final int RED_MOON_DURATION_TICKS = 8400; // 7 minutos en ticks
    private static final double INFECTION_CHANCE = 0.3; // 30% de probabilidad
    private static final int ZOMBIE_SPAWN_RADIUS = 50;
    private static final int MAX_ZOMBIES_PER_PLAYER = 5;
    
    // === ESTADO DEL EVENTO ===
    private final AtomicBoolean redMoonActive = new AtomicBoolean(false);
    private final AtomicLong redMoonStartTime = new AtomicLong(0);
    private final AtomicLong redMoonEndTime = new AtomicLong(0);
    private final AtomicInteger infectedPlayersCount = new AtomicInteger(0);
    private final AtomicInteger curedInfectionsCount = new AtomicInteger(0);
    private final AtomicInteger currentZombieCount = new AtomicInteger(0);
    private final AtomicInteger totalZombiesSpawned = new AtomicInteger(0);
    
    // === DATOS DE JUGADORES ===
    private final Map<UUID, Boolean> infectedPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> infectedPlayersTime = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> curedInfectionsCountMap = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> redMoonKillsCount = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicInteger> playerZombieKills = new ConcurrentHashMap<>();
    private final Set<UUID> curedVillagers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> curedVillagersCount = new ConcurrentHashMap<>();
    private final Set<UUID> witherKilledInRedMoon = ConcurrentHashMap.newKeySet();
    
    // === TAREAS DEL EVENTO ===
    private BukkitTask redMoonTask;
    private BukkitTask zombieSpawnTask;
    private BukkitTask infectionTask;
    
    // === MANAGERS ===
    private CustomEffectsManager effectsManager;
    private ZombieInfection zombieInfectionEffect;
    
    /**
     * Constructor del evento UndeadWeek.
     * 
     * @param plugin El plugin principal
     * @param duration Duración del evento
     */
    public UndeadWeek(HeartlessMain plugin, TimeExpression duration) {
        super(plugin, duration);
        this.zombieInfectionEffect = (ZombieInfection) effectsManager.getEffect("zombie_infection");
        logger.info("[UndeadWeek] Evento inicializado con duración: " + duration.toString());
    }
    
    @Override
    public String getId() {
        return EVENT_ID;
    }
    
    @Override
    protected void onEventStart() {
        logger.info("[UndeadWeek] Iniciando evento de No-Muertos...");
        
        // Inicializar datos del evento
        initializeEventSpecificData();
        
        // Inicializar jugadores conectados
        for (Player player : Bukkit.getOnlinePlayers()) {
            initializePlayerData(player.getUniqueId());
        }
        
        // Iniciar tareas del evento
        startEventTasks();
        
        // Anunciar inicio del evento
        Bukkit.broadcast(MM.toComponent("<red><bold>¡La Semana de No-Muertos ha comenzado!</bold></red>"));
        Bukkit.broadcast(MM.toComponent("<gray>Los zombies son más peligrosos y pueden infectarte...</gray>"));
        
        logger.info("[UndeadWeek] Evento iniciado correctamente");
    }
    
    @Override
    protected void onEventStop() {
        logger.info("[UndeadWeek] Deteniendo evento de No-Muertos...");
        
        // Detener tareas
        stopEventTasks();
        
        // Limpiar efectos de jugadores
        cleanupPlayerEffects();
        
        // Guardar datos finales
        saveEventSpecificData();
        
        // Anunciar fin del evento
        Bukkit.broadcast(MM.toComponent("<green><bold>¡La Semana de No-Muertos ha terminado!</bold></green>"));
        showEventSummary();
        
        logger.info("[UndeadWeek] Evento detenido correctamente");
    }
    
    @Override
    protected void startEventTasks() {
        // Tarea de luna roja (cada 30 minutos)
        redMoonTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (ThreadLocalRandom.current().nextDouble() < 0.3) { // 30% de probabilidad
                    activateRedMoon();
                }
            }
        }.runTaskTimer(plugin, 20L * 60 * 30, 20L * 60 * 30); // 30 minutos
        
        // Tarea de spawn de zombies
        zombieSpawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnRandomZombies();
            }
        }.runTaskTimer(plugin, 20L * 60, 20L * 60 * 5); // Cada 5 minutos
        
        // Tarea de procesamiento de infecciones
        infectionTask = new BukkitRunnable() {
            @Override
            public void run() {
                processInfections();
            }
        }.runTaskTimer(plugin, 20L * 10, 20L * 10); // Cada 10 segundos
        
        logger.info("[UndeadWeek] Tareas del evento iniciadas");
    }
    
    @Override
    protected void stopEventTasks() {
        if (redMoonTask != null && !redMoonTask.isCancelled()) {
            redMoonTask.cancel();
        }
        if (zombieSpawnTask != null && !zombieSpawnTask.isCancelled()) {
            zombieSpawnTask.cancel();
        }
        if (infectionTask != null && !infectionTask.isCancelled()) {
            infectionTask.cancel();
        }
        
        logger.info("[UndeadWeek] Tareas del evento detenidas");
    }
    
    @Override
    protected void initializeEventSpecificData() {
        // Inicializar estadísticas globales
        globalStatistics.put("total_zombies_killed", new AtomicLong(0));
        globalStatistics.put("total_infections", new AtomicLong(0));
        globalStatistics.put("total_cures", new AtomicLong(0));
        globalStatistics.put("red_moon_activations", new AtomicLong(0));
        
        // Configurar desafíos específicos del evento
        setupUndeadWeekChallenges();
        
        logger.info("[UndeadWeek] Datos específicos del evento inicializados");
    }
    
    @Override
    protected void saveEventSpecificData() {
        try {
            // Guardar estadísticas en eventSpecificData
            eventSpecificData.put("redMoonActive", redMoonActive.get());
            eventSpecificData.put("redMoonStartTime", redMoonStartTime.get());
            eventSpecificData.put("redMoonEndTime", redMoonEndTime.get());
            eventSpecificData.put("infectedPlayersCount", infectedPlayersCount.get());
            eventSpecificData.put("curedInfectionsCount", curedInfectionsCount.get());
            eventSpecificData.put("currentZombieCount", currentZombieCount.get());
            
            // Guardar mapas de jugadores
            eventSpecificData.put("infectedPlayers", new HashMap<>(infectedPlayers));
            eventSpecificData.put("infectedPlayersTime", new HashMap<>(infectedPlayersTime));
            eventSpecificData.put("curedInfectionsCountMap", new HashMap<>(curedInfectionsCountMap));
            eventSpecificData.put("redMoonKillsCount", new HashMap<>(redMoonKillsCount));
            eventSpecificData.put("curedVillagers", new HashSet<>(curedVillagers));
            eventSpecificData.put("curedVillagersCount", new HashMap<>(curedVillagersCount));
            eventSpecificData.put("witherKilledInRedMoon", new HashSet<>(witherKilledInRedMoon));
            
            // Convertir AtomicInteger a Integer para serialización
            Map<UUID, Integer> zombieKillsMap = new HashMap<>();
            playerZombieKills.forEach((uuid, atomic) -> zombieKillsMap.put(uuid, atomic.get()));
            eventSpecificData.put("playerZombieKills", zombieKillsMap);
            
            dataDirty.set(true);
            lastDataSave.set(System.currentTimeMillis());
            
            logger.info("[UndeadWeek] Datos específicos del evento guardados");
        } catch (Exception e) {
            logger.severe("[UndeadWeek] Error al guardar datos específicos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void processEventStatistics() {
        try {
            // Actualizar estadísticas globales
            globalStatistics.get("total_infections").set(infectedPlayersCount.get());
            globalStatistics.get("total_cures").set(curedInfectionsCount.get());
            
            // Calcular total de zombies matados
            long totalZombieKills = playerZombieKills.values().stream()
                .mapToLong(AtomicInteger::get)
                .sum();
            globalStatistics.get("total_zombies_killed").set(totalZombieKills);
            
            // Actualizar estadísticas de jugadores
            for (UUID playerId : playerStatistics.keySet()) {
                Map<String, Object> stats = playerStatistics.get(playerId);
                Player player = Bukkit.getPlayer(playerId);
                
                stats.put("zombie_kills", getPlayerZombieKills(playerId));
                
                // Verificar infección usando el efecto personalizado
                boolean isInfected = false;
                if (player != null && player.isOnline() && zombieInfectionEffect != null) {
                    isInfected = zombieInfectionEffect.isAffected(player);
                }
                stats.put("is_infected", isInfected);
                
                stats.put("cured_infections", curedInfectionsCountMap.getOrDefault(playerId, 0));
                stats.put("red_moon_kills", redMoonKillsCount.getOrDefault(playerId, 0));
            }
            
            logger.fine("[UndeadWeek] Estadísticas del evento procesadas");
        } catch (Exception e) {
            logger.warning("[UndeadWeek] Error al procesar estadísticas: " + e.getMessage());
        }
    }
    
    // === EVENTOS DEL SERVIDOR ===
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        initializePlayerData(player.getUniqueId());
        
        if (redMoonActive.get()) {
            player.sendMessage(MM.toComponent("<red><bold>¡La Luna Roja está activa!</bold></red>"));
            player.sendMessage(MM.toComponent("<gray>Los monstruos son más peligrosos durante este tiempo...</gray>"));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        
        // Guardar datos del jugador antes de que se desconecte
        savePlayerData(playerId);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Zombie)) return;
        
        Player player = (Player) event.getEntity();
        
        // Aplicar infección si no está ya infectado usando el efecto personalizado
        if (zombieInfectionEffect == null || !zombieInfectionEffect.isAffected(player)) {
            if (ThreadLocalRandom.current().nextDouble() < INFECTION_CHANCE) {
                infectPlayer(player);
            }
        }
        
        // Aplicar efecto de veneno durante 30 segundos
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, POISON_DURATION, 0));
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Zombie)) return;
        
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        
        UUID killerId = killer.getUniqueId();
        
        // Incrementar contador de zombies matados
        playerZombieKills.computeIfAbsent(killerId, k -> new AtomicInteger(0)).incrementAndGet();
        currentZombieCount.decrementAndGet();
        
        // Bonus durante luna roja
        if (redMoonActive.get()) {
            redMoonKillsCount.merge(killerId, 1, Integer::sum);
            
            // Bonus de experiencia durante luna roja
            event.setDroppedExp(event.getDroppedExp() * 2);
        }
        
        // Verificar desafíos
        checkZombieKillChallenges(killer);
    }
    
    // === MECÁNICAS DEL EVENTO ===
    
    private void activateRedMoon() {
        if (redMoonActive.get()) return;
        
        redMoonActive.set(true);
        redMoonStartTime.set(System.currentTimeMillis());
        redMoonEndTime.set(System.currentTimeMillis() + RED_MOON_DURATION);
        
        globalStatistics.get("red_moon_activations").incrementAndGet();
        
        // Anunciar luna roja
        Bukkit.broadcast(MM.toComponent("<dark_red><bold>¡LA LUNA ROJA SE ALZA!</bold></dark_red>"));
        Bukkit.broadcast(MM.toComponent("<red>Los monstruos son más fuertes y peligrosos...</red>"));
        
        // Programar fin de luna roja
        new BukkitRunnable() {
            @Override
            public void run() {
                deactivateRedMoon();
            }
        }.runTaskLater(plugin, RED_MOON_DURATION_TICKS); // Convertir ms a ticks
        
        logger.info("[UndeadWeek] Luna Roja activada");
    }
    
    private void deactivateRedMoon() {
        redMoonActive.set(false);
        redMoonStartTime.set(0);
        redMoonEndTime.set(0);
        
        Bukkit.broadcast(MM.toComponent("<green>La Luna Roja se desvanece...</green>"));
        
        logger.info("[UndeadWeek] Luna Roja desactivada");
    }
    
    public void forceActivateRedMoon() {
        activateRedMoon();
    }
    
    private void infectPlayer(Player player) {
        
        // Verificar si ya está infectado usando el efecto personalizado
        if (zombieInfectionEffect != null && zombieInfectionEffect.isAffected(player)) {
            return;
        }
        
        // Aplicar el efecto de infección zombie usando el método de sincronización
        if (zombieInfectionEffect != null) {
            zombieInfectionEffect.applyInfectionFromEvent(player, "zombie_attack");
        }
        
        // Actualizar contadores
        infectedPlayersCount.incrementAndGet();
        
        logger.info("[UndeadWeek] Jugador " + player.getName() + " infectado usando ZombieInfection");
    }
    
    /**
     * Cura a un jugador de la infección zombie
     * @param player El jugador a curar
     */
    public void curePlayer(Player player) {
        
        // Verificar si está infectado usando el efecto personalizado
        if (zombieInfectionEffect == null || !zombieInfectionEffect.isAffected(player)) {
            return;
        }
        
        // Curar usando el método de sincronización
        zombieInfectionEffect.cureInfectionFromEvent(player, "medicine");
        
        // Actualizar contadores
        curedInfectionsCount.incrementAndGet();
        curedInfectionsCountMap.merge(player.getUniqueId(), 1, Integer::sum);
        
        logger.info("[UndeadWeek] Jugador " + player.getName() + " curado usando ZombieInfection");
    }
    
    private void spawnRandomZombies() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (currentZombieCount.get() >= MAX_ZOMBIES_PER_PLAYER * Bukkit.getOnlinePlayers().size()) {
                break;
            }
            
            Location playerLoc = player.getLocation();
            World world = playerLoc.getWorld();
            
            if (world == null) continue;
            
            // Spawn zombie cerca del jugador
            for (int i = 0; i < ThreadLocalRandom.current().nextInt(1, 4); i++) {
                Location spawnLoc = getRandomLocationNear(playerLoc, ZOMBIE_SPAWN_RADIUS);
                
                if (spawnLoc != null && spawnLoc.getBlock().getType() == Material.AIR) {
                    Zombie zombie = (Zombie) world.spawnEntity(spawnLoc, EntityType.ZOMBIE);
                    zombie.setTarget(player);
                    currentZombieCount.incrementAndGet();
                    
                    // Bonus durante luna roja
                    if (redMoonActive.get()) {
                        zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                        zombie.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
                    }
                }
            }
        }
    }
    
    private void processInfections() {
        
        // Iterar sobre todos los jugadores online para verificar infecciones
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            // Verificar que el jugador esté infectado usando el efecto personalizado
            if (zombieInfectionEffect == null || !zombieInfectionEffect.isAffected(player)) {
                continue;
            }
            
            // El efecto ZombieInfection ya maneja los efectos progresivos
            // Solo necesitamos verificar si necesitamos aplicar efectos adicionales del evento
            
            // Aplicar efectos adicionales específicos del evento UndeadWeek si es necesario
            // (Los efectos principales ya los maneja ZombieInfection automáticamente)
        }
    }
    
    private void setupUndeadWeekChallenges() {
        // Desafío: Matar 50 zombies
        availableChallenges.put("zombie_slayer", new ChallengeDefinition(
            "zombie_slayer",
            "Asesino de Zombies",
            "Mata 50 zombies durante el evento",
            50,
            Arrays.asList("experience:500", "item:diamond_sword:1")
        ));
        
        // Desafío: Sobrevivir infectado por 30 minutos
        availableChallenges.put("infection_survivor", new ChallengeDefinition(
            "infection_survivor",
            "Superviviente Infectado",
            "Sobrevive infectado por 30 minutos",
            30, // 30 minutos
            Arrays.asList("experience:1000", "item:golden_apple:5")
        ));
        
        // Desafío: Matar 10 zombies durante luna roja
        availableChallenges.put("red_moon_hunter", new ChallengeDefinition(
            "red_moon_hunter",
            "Cazador de Luna Roja",
            "Mata 10 zombies durante la luna roja",
            10,
            Arrays.asList("experience:750", "item:enchanted_book:1")
        ));
    }
    
    private void checkZombieKillChallenges(Player player) {
        UUID playerId = player.getUniqueId();
        int kills = getPlayerZombieKills(playerId);
        
        // Verificar desafío de asesino de zombies
        if (kills >= 50 && !hasChallengeCompleted(playerId, "zombie_slayer")) {
            completeChallenge(player, "zombie_slayer");
        }
        
        // Verificar desafío de cazador de luna roja
        if (redMoonActive.get()) {
            int redMoonKills = redMoonKillsCount.getOrDefault(playerId, 0);
            if (redMoonKills >= 10 && !hasChallengeCompleted(playerId, "red_moon_hunter")) {
                completeChallenge(player, "red_moon_hunter");
            }
        }
    }
    
    private void completeChallenge(Player player, String challengeId) {
        ChallengeDefinition challenge = availableChallenges.get(challengeId);
        if (challenge == null) return;
        
        UUID playerId = player.getUniqueId();
        Set<String> completed = completedChallenges.computeIfAbsent(playerId, k -> new HashSet<>());
        
        if (!completed.contains(challengeId)) {
            completed.add(challengeId);
            
            // Otorgar recompensas
            for (String reward : challenge.getRewards()) {
                giveReward(player, reward);
            }
            
            player.sendMessage(MM.toComponent("<green>¡Has completado el desafío: <gold>" + 
                             challenge.getDisplayName() + "</gold>!</green>"));
            
            dataDirty.set(true);
        }
        
        logger.info("[UndeadWeek] Jugador " + player.getName() + " completó desafío: " + challengeId);
    }
    
    private void giveReward(Player player, String reward) {
        String[] parts = reward.split(":");
        if (parts.length < 2) return;
        
        switch (parts[0].toLowerCase()) {
            case "experience":
                int exp = Integer.parseInt(parts[1]);
                player.giveExp(exp);
                break;
            case "item":
                if (parts.length >= 3) {
                    Material material = Material.valueOf(parts[1].toUpperCase());
                    int amount = Integer.parseInt(parts[2]);
                    player.getInventory().addItem(new ItemStack(material, amount));
                }
                break;
        }
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    private Location getRandomLocationNear(Location center, int radius) {
        World world = center.getWorld();
        if (world == null) return null;
        
        int x = center.getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        int z = center.getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        int y = world.getHighestBlockYAt(x, z) + 1;
        
        return new Location(world, x, y, z);
    }
    
    private void initializePlayerData(UUID playerId) {
        playerStatistics.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        playerZombieKills.computeIfAbsent(playerId, k -> new AtomicInteger(0));
        challengeProgress.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        
        totalParticipants.incrementAndGet();
    }
    
    private void savePlayerData(UUID playerId) {
        // Los datos ya están en las estructuras thread-safe, no necesita acción adicional
        logger.fine("[UndeadWeek] Datos del jugador " + playerId + " guardados");
    }
    
    private void cleanupPlayerEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Remover el efecto de infección zombie usando el método de sincronización
            if (zombieInfectionEffect != null && zombieInfectionEffect.isAffected(player)) {
                zombieInfectionEffect.cureInfectionFromEvent(player, "event_end");
            }
            
            // Remover efectos adicionales del evento
            player.removePotionEffect(PotionEffectType.HUNGER);
        }
    }
    
    private void showEventSummary() {
        Bukkit.broadcast(MM.toComponent("<gold>========== RESUMEN DEL EVENTO ==========</gold>"));
        Bukkit.broadcast(MM.toComponent("<yellow>Zombies eliminados: " + globalStatistics.get("total_zombies_killed").get() + "</yellow>"));
        Bukkit.broadcast(MM.toComponent("<yellow>Jugadores infectados: " + globalStatistics.get("total_infections").get() + "</yellow>"));
        Bukkit.broadcast(MM.toComponent("<yellow>Infecciones curadas: " + globalStatistics.get("total_cures").get() + "</yellow>"));
        Bukkit.broadcast(MM.toComponent("<yellow>Lunas rojas activadas: " + globalStatistics.get("red_moon_activations").get() + "</yellow>"));
        Bukkit.broadcast(MM.toComponent("<gold>=======================================</gold>"));
    }
    
    // === GETTERS Y SETTERS PÚBLICOS ===
    
    public boolean isRedMoonActive() {
        return redMoonActive.get();
    }
    
    public long getRedMoonStartTime() {
        return redMoonStartTime.get();
    }
    
    public long getRedMoonEndTime() {
        return redMoonEndTime.get();
    }
    
    public int getInfectedPlayersCount() {
        return infectedPlayersCount.get();
    }
    
    public int getTotalCuredInfectionsCount() {
        return curedInfectionsCount.get();
    }
    
    public Map<UUID, Boolean> getInfectedPlayers() {
        return new HashMap<>(infectedPlayers);
    }
    
    public Map<UUID, Long> getInfectedPlayersTime() {
        return new HashMap<>(infectedPlayersTime);
    }
    
    public Map<UUID, Integer> getCuredInfectionsCount() {
        return new HashMap<>(curedInfectionsCountMap);
    }
    
    public Map<UUID, Integer> getRedMoonKillsCount() {
        return new HashMap<>(redMoonKillsCount);
    }
    
    public Set<UUID> getCuredVillagers() {
        return new HashSet<>(curedVillagers);
    }
    
    public Map<UUID, Integer> getCuredVillagersCount() {
        return new HashMap<>(curedVillagersCount);
    }
    
    public Set<UUID> getWitherKilledInRedMoon() {
        return new HashSet<>(witherKilledInRedMoon);
    }
    
    public void setRedMoonActive(boolean active) {
        redMoonActive.set(active);
    }
    
    public void setRedMoonStartTime(long time) {
        redMoonStartTime.set(time);
    }
    
    public void setRedMoonEndTime(long time) {
        redMoonEndTime.set(time);
    }
    
    public void setInfectedPlayersCount(int count) {
        infectedPlayersCount.set(count);
    }
    
    public void setCuredInfectionsCount(int count) {
        curedInfectionsCount.set(count);
    }
    
    public void loadInfectedPlayers(Map<UUID, Boolean> data) {
        infectedPlayers.clear();
        infectedPlayers.putAll(data);
    }
    
    public void loadInfectedPlayersTime(Map<UUID, Long> data) {
        infectedPlayersTime.clear();
        infectedPlayersTime.putAll(data);
    }
    
    public void loadCuredInfectionsCount(Map<UUID, Integer> data) {
        curedInfectionsCountMap.clear();
        curedInfectionsCountMap.putAll(data);
    }
    
    public void loadRedMoonKillsCount(Map<UUID, Integer> data) {
        redMoonKillsCount.clear();
        redMoonKillsCount.putAll(data);
    }
    
    public void loadCuredVillagers(Set<UUID> data) {
        curedVillagers.clear();
        curedVillagers.addAll(data);
    }
    
    public void loadCuredVillagersCount(Map<UUID, Integer> data) {
        curedVillagersCount.clear();
        curedVillagersCount.putAll(data);
    }
    
    public void loadWitherKilledInRedMoon(Set<UUID> data) {
        witherKilledInRedMoon.clear();
        witherKilledInRedMoon.addAll(data);
    }
    
    // Método removido - hasChallengeCompleted es final en AbstractWeeklyEvent
    
    public int getTotalZombiesKilled() {
        return playerZombieKills.values().stream()
            .mapToInt(AtomicInteger::get)
            .sum();
    }
    
    public int getTotalZombiesSpawned() {
        return totalZombiesSpawned.get();
    }
    
    public int getCurrentZombieCount() {
        return currentZombieCount.get();
    }
    
    public int getPlayerZombieKills(UUID playerId) {
        AtomicInteger kills = playerZombieKills.get(playerId);
        return kills != null ? kills.get() : 0;
    }
    
    // Eliminar la clase ChallengeDefinition duplicada ya que usamos la de AbstractWeeklyEvent
}