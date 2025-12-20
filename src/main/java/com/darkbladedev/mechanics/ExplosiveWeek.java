package com.darkbladedev.mechanics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.MM;
import com.darkbladedev.models.TimeExpression;

public class ExplosiveWeek extends AbstractWeeklyEvent {
    
    // === CAMPOS ESPECÍFICOS DEL EVENTO ===
    
    private final Random random = new Random();
    
    // Tareas del evento
    private BukkitTask ghastSpawnTask;
    private BukkitTask ghastAttackTask;
    private BukkitTask mainTask;
    
    // Seguimiento de jugadores para desafíos
    private final Set<UUID> ghastKillers = new HashSet<>();
    private final Map<UUID, Set<EntityType>> mobHeadCollectors = new HashMap<>();
    private final Set<UUID> playerExplosionKillers = new HashSet<>();
    private final Set<UUID> wardenKillers = new HashSet<>();
    private final Set<UUID> stormPvpKillers = new HashSet<>();
    
    // Configuración de mobs hostiles clásicos
    private final Set<EntityType> classicHostileMobs = new HashSet<>(Arrays.asList(
            EntityType.ZOMBIE, 
            EntityType.SKELETON, 
            EntityType.CREEPER
    ));
    
    
    public ExplosiveWeek(HeartlessMain plugin, TimeExpression duration) {
        super(plugin, duration);
        this.prefix = "<b><gradient:#ed2f2f:#f15c5c:#f58888:#f9b5b5:#fce1e1:#ffffff:#ffffff:#ffffff:#ffffff:#ffffff:#fce1e2:#f8b5b5:#f48989:#f05c5d:#ec3031>Semana Explosiva</gradient></b>";
        
        // Inicializar el sistema de desafíos
        setupExplosiveWeekChallenges();
    }
    
    // === IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS ===
    
    @Override
    protected void onEventStart() {
        // Inicializar tareas específicas del evento
        startEventTasks();
        
        // Anunciar el inicio del evento
        announceEventStart();
        
        logger.info("[ExplosiveWeek] Evento iniciado - Todas las explosiones son más poderosas");
    }
    
    @Override
    protected void onWorldEventStart(World world) {
        logger.info("[ExplosiveWeek] Iniciando evento en mundo: " + world.getName());
        
        // Inicializar jugadores específicos de este mundo
        for (Player player : world.getPlayers()) {
            if (!isPlayerInExcludedWorld(player)) {
                initializePlayerData(player.getUniqueId());
            }
        }
        
        // Configurar efectos específicos del mundo si es necesario
        // (por ejemplo, spawns de ghasts específicos del mundo)
        
        logger.info("[ExplosiveWeek] Evento iniciado en mundo: " + world.getName() + 
                   " con " + world.getPlayers().size() + " jugadores");
    }
    
    @Override
    protected void onWorldEventStop(World world) {
        logger.info("[ExplosiveWeek] Deteniendo evento en mundo: " + world.getName());
        
        // Limpiar efectos específicos del mundo si es necesario
        // (por ejemplo, remover ghasts específicos del mundo)
        for (Entity entity : world.getEntities()) {
            if (entity.getType() == EntityType.GHAST && entity.getLocation().getWorld().getEnvironment() == Environment.NORMAL) {
                entity.remove();
            }
        }
        
        logger.info("[ExplosiveWeek] Evento detenido en mundo: " + world.getName());
    }
    
    /**
     * Inicializa los datos específicos de un jugador para el evento ExplosiveWeek.
     * 
     * @param playerId UUID del jugador a inicializar
     */
    private void initializePlayerData(UUID playerId) {
        if (playerId == null) return;
        
        // Inicializar estadísticas del jugador en el mapa heredado
        playerStatistics.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        
        // Inicializar contadores específicos del evento
        Map<String, Object> stats = playerStatistics.get(playerId);
        stats.putIfAbsent("ghasts_killed", 0);
        stats.putIfAbsent("explosions_survived", 0);
        stats.putIfAbsent("players_killed_with_explosions", 0);
        stats.putIfAbsent("wardens_killed", 0);
        stats.putIfAbsent("storm_pvp_kills", 0);
        stats.putIfAbsent("mob_heads_collected", 0);
        
        logger.info("[ExplosiveWeek] Datos del jugador " + playerId + " inicializados");
    }
    
    @Override
    protected void onEventStop() {
        // Detener tareas específicas del evento
        stopEventTasks();
        
        // Anunciar el fin del evento
        announceEventEnd();
        
        // Limpiar datos del evento
        cleanupEventData();
    }
    
    @Override
    protected void initializeEventSpecificData() {
        // Inicializar estructuras de datos específicas del evento
        ghastKillers.clear();
        mobHeadCollectors.clear();
        playerExplosionKillers.clear();
        wardenKillers.clear();
        
        // Inicializar estadísticas específicas del evento
        updateGlobalStatistic("ghasts_spawned", 0);
        updateGlobalStatistic("explosions_caused", 0);
        updateGlobalStatistic("players_killed_by_explosion", 0);
        updateGlobalStatistic("wardens_killed", 0);
        
        logger.info("[ExplosiveWeek] Datos específicos del evento inicializados");
    }
    
    @Override
    protected void saveEventSpecificData() {
        // Guardar datos específicos del evento
        // Los datos de desafíos se guardan automáticamente por AbstractWeeklyEvent
        
        // Actualizar estadísticas finales
        updateGlobalStatistic("total_ghast_killers", ghastKillers.size());
        updateGlobalStatistic("total_explosion_killers", playerExplosionKillers.size());
        updateGlobalStatistic("total_warden_killers", wardenKillers.size());
        
        // Calcular estadísticas de colección de cabezas
        long totalHeadCollectors = mobHeadCollectors.values().stream()
            .mapToLong(Set::size)
            .sum();
        updateGlobalStatistic("total_heads_collected", totalHeadCollectors);
        
        logger.info("[ExplosiveWeek] Datos específicos del evento guardados");
    }
    
    @Override
    protected void processEventStatistics() {
        // Procesar estadísticas específicas del evento
        for (UUID playerId : getActivePlayers()) {
            // Actualizar estadísticas de jugador
            updatePlayerStatistic(playerId, "ghast_kills", ghastKillers.contains(playerId) ? 1 : 0);
            updatePlayerStatistic(playerId, "explosion_kills", playerExplosionKillers.contains(playerId) ? 1 : 0);
            updatePlayerStatistic(playerId, "warden_kills", wardenKillers.contains(playerId) ? 1 : 0);
            
            // Estadísticas de colección de cabezas
            Set<EntityType> playerHeads = mobHeadCollectors.get(playerId);
            updatePlayerStatistic(playerId, "heads_collected", playerHeads != null ? playerHeads.size() : 0);
        }
    }



    @Override
    protected void startEventTasks() {
        startMainTask();
        
        // Start ghast spawning in thunderstorms
        startGhastSpawning();
    }
    
    @Override
    protected void announceEventStart() {
        // Announce the start of the event
        Bukkit.broadcast(MM.toComponent("<yellow><b>¡<gradient:#fa4444:#fb6c6c:#fc9494:#fdbcbc:#fee4e4:#ffffff:#ffffff:#ffffff:#ffffff:#ffffff:#fee4e4:#fdbcbc:#fc9494:#fb6c6c:#fa4444>SEMANA EXPLOSIVA</gradient> INICIADA!"));
        Bukkit.broadcast(MM.toComponent("<gray>¡Cuidado con las explosiones! <b>Todo es más volátil..."));
        
        // Announce challenges
        announceRegisteredChallenges();
    }
    
    /**
     * Anuncia el fin del evento y muestra estadísticas individuales a cada jugador
     */
    @Override
    protected void announceEventEnd() {
        // Anuncio general del fin del evento
        Bukkit.broadcast(MM.toComponent(prefix + " <red>¡La semana explosiva ha terminado! El mundo vuelve a la calma."));
        Bukkit.broadcast(MM.toComponent("<yellow>¡Revisando las estadísticas de supervivencia explosiva!"));
    }
    
    
    @Override
    protected void stopEventTasks() {
        // Cancel ghast spawn task with proper verification
        if (ghastSpawnTask != null && !ghastSpawnTask.isCancelled()) {
            ghastSpawnTask.cancel();
            logger.info("[ExplosiveWeek] Ghast spawn task cancelled");
        }
        ghastSpawnTask = null;
        
        // Cancel ghast attack task with proper verification
        if (ghastAttackTask != null && !ghastAttackTask.isCancelled()) {
            ghastAttackTask.cancel();
            logger.info("[ExplosiveWeek] Ghast attack task cancelled");
        }
        ghastAttackTask = null;
        
        // Cancel main task with proper verification
        if (mainTask != null && !mainTask.isCancelled()) {
            mainTask.cancel();
            logger.info("[ExplosiveWeek] Main task cancelled");
        }
        mainTask = null;
        
        // Remove all spawned ghasts in overworld
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == Environment.NORMAL) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getType() == EntityType.GHAST) {
                        entity.remove();
                    }
                }
            }
        }
        
        logger.info("[ExplosiveWeek] All event tasks stopped successfully");
    }
    
    @Override
    protected void cleanupEventData() {
        ghastKillers.clear();
        mobHeadCollectors.clear();
        playerExplosionKillers.clear();
        wardenKillers.clear();
    }
    
    @Override
    public String getId() {
        return "explosive_week";
    }
    
    @Override
    protected void pauseEventTasks() {
        // Cancel ghast spawn task with proper verification
        if (ghastSpawnTask != null && !ghastSpawnTask.isCancelled()) {
            ghastSpawnTask.cancel();
            logger.info("[ExplosiveWeek] Ghast spawn task paused");
        }
        
        // Cancel ghast attack task with proper verification
        if (ghastAttackTask != null && !ghastAttackTask.isCancelled()) {
            ghastAttackTask.cancel();
            logger.info("[ExplosiveWeek] Ghast attack task paused");
        }
        
        // Cancel main task with proper verification
        if (mainTask != null && !mainTask.isCancelled()) {
            mainTask.cancel();
            logger.info("[ExplosiveWeek] Main task paused");
        }
        
        logger.info("[ExplosiveWeek] All event tasks paused successfully");
    }
    
    @Override
    protected void resumeEventTasks() {
        logger.info("[ExplosiveWeek] Resuming event tasks...");
        startGhastSpawning();
        startMainTask();
        logger.info("[ExplosiveWeek] All event tasks resumed successfully");
    }
    
    private void startGhastSpawning() {
        // Cancel existing ghast spawn task if any
        if (ghastSpawnTask != null && !ghastSpawnTask.isCancelled()) {
            ghastSpawnTask.cancel();
            logger.info("[ExplosiveWeek] Previous ghast spawn task cancelled before starting new one");
        }
        
        // Cancel existing ghast attack task if any
        if (ghastAttackTask != null && !ghastAttackTask.isCancelled()) {
            ghastAttackTask.cancel();
            logger.info("[ExplosiveWeek] Previous ghast attack task cancelled before starting new one");
        }
        
        // Check for thunderstorms and spawn ghasts
        ghastSpawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() == Environment.NORMAL && 
                        world.isThundering()) {
                        
                        // Spawn ghasts for each player in the world
                        for (Player player : world.getPlayers()) {
                            if (random.nextInt(100) < 30) { //30% chance per player
                                spawnGhastNearPlayer(player);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 30); // Check every 30 seconds
        
        // Make ghasts shoot more frequently
        ghastAttackTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() == Environment.NORMAL) {
                        for (Entity entity : world.getEntities()) {
                            if (entity.getType() == EntityType.GHAST) {
                                Ghast ghast = (Ghast) entity;
                                
                                // Find nearest player
                                Player target = findNearestPlayer(ghast.getLocation(), 32);
                                if (target != null) {
                                    // Make the ghast look at the player
                                    ghast.setTarget(target);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 5); // Every 5 seconds
        
        logger.info("[ExplosiveWeek] Ghast spawning tasks started successfully");
    }
    
    private void spawnGhastNearPlayer(Player player) {
        Location playerLoc = player.getLocation();
        
        // Find a safe location 20-30 blocks away and 10-20 blocks up
        Location spawnLoc = playerLoc.clone().add(
                (random.nextDouble() * 20 + 10) * (random.nextBoolean() ? 1 : -1),
                random.nextDouble() * 10 + 10,
                (random.nextDouble() * 20 + 10) * (random.nextBoolean() ? 1 : -1)
        );
        
        // Ensure the location is in air
        if (spawnLoc.getBlock().getType() == Material.AIR) {
            Ghast ghast = (Ghast) player.getWorld().spawnEntity(spawnLoc, EntityType.GHAST);
            
            // Make the ghast target the player
            ghast.setTarget(player);
            
            // Notify nearby players
            for (Player nearby : Bukkit.getOnlinePlayers()) {
                if (nearby.getWorld().equals(player.getWorld()) && 
                    nearby.getLocation().distance(spawnLoc) <= 50) {
                    nearby.sendMessage(MM.toComponent("<red>¡Un ghast ha aparecido cerca tuyo!"));
                }
            }
        }
    }
    
    private Player findNearestPlayer(Location location, double maxDistance) {
        Player nearest = null;
        double nearestDistance = maxDistance;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(location.getWorld())) {
                double distance = player.getLocation().distance(location);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = player;
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * Configura e inicializa los desafíos específicos de la Semana Explosiva
     */
    /**
     * Configura los desafíos específicos de la Semana Explosiva usando el sistema AbstractWeeklyEvent
     */
    private void setupExplosiveWeekChallenges() {
        try {
            // Desafío 1: Matar un Ghast
            registerChallenge("ghast_killer", 
                ChallengeDefinition.fromStringRewards(
                    "ghast_killer",
                    "Cazador de Ghasts", 
                    "Mata un Ghast durante una tormenta", 
                    1, 
                    Arrays.asList("enchant:tictac:1")
                )
            );
            
            // Desafío 2: Coleccionar cabezas de mobs hostiles clásicos
            registerChallenge("mob_head_collector", 
                ChallengeDefinition.fromStringRewards(
                    "mob_head_collector",
                    "Coleccionista de Cabezas", 
                    "Colecciona cabezas de Zombie, Skeleton y Creeper", 
                    3, 
                    Arrays.asList("coins:20")
                )
            );
            
            // Desafío 3: Matar a un jugador con una explosión
            registerChallenge("storm_killer", 
                ChallengeDefinition.fromStringRewards(
                    "storm_killer",
                    "Intormentable", 
                    "Mata a un jugador durante una tormenta", 
                    1, 
                    Arrays.asList("tag:tntomano")
                )
            );
            
            // Desafío 4: Matar un Warden durante una tormenta
            registerChallenge("warden_storm_killer", 
                ChallengeDefinition.fromStringRewards(
                    "warden_storm_killer",
                    "Electro Warden",
                    "Mata un Warden durante una tormenta", 
                    1, 
                    Arrays.asList("health:2")
                )
            );
            
            logger.info("[ExplosiveWeek] Desafíos configurados correctamente");
        } catch (Exception e) {
            logger.severe("[ExplosiveWeek] Error al configurar desafíos: " + e.getMessage());
        }
    }
    
    /**
     * Anuncia todos los desafíos registrados a los jugadores usando el sistema AbstractWeeklyEvent
     */
    public void announceRegisteredChallenges() {
        try {
            Map<String, AbstractWeeklyEvent.ChallengeDefinition> challenges = getAvailableChallenges();
            if (challenges == null || challenges.isEmpty()) {
                logger.warning("[ExplosiveWeek] No hay desafíos registrados para anunciar");
                return;
            }
            
            Bukkit.broadcast(MM.toComponent("<aqua>Desafíos disponibles:"));
            
            for (AbstractWeeklyEvent.ChallengeDefinition challenge : challenges.values()) {
                String difficultyColor = getDifficultyColor(challenge.getId());
                Bukkit.broadcast(MM.toComponent(difficultyColor + "• " + 
                    challenge.getDisplayName() + " - <gray>" + challenge.getDescription()));
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "[ExplosiveWeek] Error al anunciar desafíos registrados", e);
        }
    }
    
    /**
     * Obtiene el color de dificultad para un desafío específico
     * 
     * @param challengeId ID del desafío
     * @return Color en formato MiniMessage
     */
    private String getDifficultyColor(String challengeId) {
        switch (challengeId) {
            case "ghast_killer":
                return "<green>"; // Fácil
            case "mob_head_collector":
                return "<yellow>"; // Intermedio
            case "storm_killer":
                return "<red>"; // Difícil
            case "warden_storm_killer":
                return "<red>"; // Difícil
            default:
                return "<white>"; // Por defecto
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isActive.get()) return;
        
        // Verificar si el spawn es en un mundo excluido
        if (isWorldExcluded(event.getLocation().getWorld())) return;
        
        if (event.getEntityType() == EntityType.CREEPER) {
            Creeper creeper = (Creeper) event.getEntity();
            creeper.setPowered(true); // Make all creepers charged
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!isActive.get()) return;
        
        // Verificar si la explosión es en un mundo excluido
        if (isWorldExcluded(event.getLocation().getWorld())) return;
        
        // TNT explosions are handled in ExplosionPrimeEvent
    }
    
    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!isActive.get()) return;
        
        if (event.getEntity() instanceof TNTPrimed) {
            // Set TNT explosion power to cause 12 hearts damage at epicenter
            // Power 6.0f causes approximately 12 hearts (24 HP) damage at epicenter
            event.setRadius(6.0f);
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!isActive.get()) return;
        
        if (event.getCause() == DamageCause.BLOCK_EXPLOSION || 
            event.getCause() == DamageCause.ENTITY_EXPLOSION) {
            // TNT explosions already have increased power, no need to double damage here
            // Only apply extra damage to non-TNT explosions
            if (event.getEntity() instanceof Player) {
                // Check if this is from TNT by looking at nearby TNT entities
                boolean isTNTExplosion = false;
                Location playerLoc = event.getEntity().getLocation();
                
                for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, 10, 10, 10)) {
                    if (entity instanceof TNTPrimed) {
                        isTNTExplosion = true;
                        break;
                    }
                }
                
                // Only double damage for non-TNT explosions (creepers, etc.)
                if (!isTNTExplosion) {
                    double damage = event.getDamage();
                    event.setDamage(damage * 2.0);
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isActive.get()) return;
        
        Entity victim = event.getEntity();
        Entity damager = event.getDamager();
        
        // Verificar si el evento es en un mundo excluido
        if (victim instanceof Player && isPlayerInExcludedWorld((Player) victim)) return;
        
        // Track player kills by explosion for challenge (during storm)
        if (victim instanceof Player && 
            (event.getCause() == DamageCause.BLOCK_EXPLOSION || event.getCause() == DamageCause.ENTITY_EXPLOSION) &&
            victim.getWorld().hasStorm()) {
            
            Player deadPlayer = (Player) victim;
            
            // Check if the explosion was caused by another player
            Player killer = null;
            
            if (damager instanceof TNTPrimed) {
                TNTPrimed tnt = (TNTPrimed) damager;
                if (tnt.getSource() instanceof Player) {
                    killer = (Player) tnt.getSource();
                }
            } else if (damager instanceof Creeper) {
                // For creepers, we need to check if a player led it to the victim
                // This is approximate and might not be 100% accurate
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player != deadPlayer && 
                        player.getWorld().equals(damager.getWorld()) &&
                        player.getLocation().distance(damager.getLocation()) < 16) {
                        killer = player;
                        break;
                    }
                }
            }
            
            if (killer != null && deadPlayer.getHealth() - event.getFinalDamage() <= 0) {
                // Player will die from this explosion
                if (!playerExplosionKillers.contains(killer.getUniqueId())) {
                    playerExplosionKillers.add(killer.getUniqueId());
                    
                    // Award the challenge reward
                    awardExplosionKillChallenge(killer);
                }
            }
        }
        
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Debug log para verificar que el método se ejecuta
        plugin.getLogger().info("[DEBUG] onPlayerDeath ejecutado");
        plugin.getLogger().info("[DEBUG] ExplosiveWeek isActive(): " + isActive());
        plugin.getLogger().info("[DEBUG] ExplosiveWeek isPaused(): " + isPaused());
        plugin.getLogger().info("[DEBUG] ExplosiveWeek canExecute(): " + canExecute());
        
        // Verificar estado del WeeklyEventManager
        if (plugin.getWeeklyEventManager() != null) {
            plugin.getLogger().info("[DEBUG] WeeklyEventManager isEventActive(): " + plugin.getWeeklyEventManager().isEventActive());
            plugin.getLogger().info("[DEBUG] WeeklyEventManager getCurrentEventType(): " + plugin.getWeeklyEventManager().getCurrentEventType());
            plugin.getLogger().info("[DEBUG] WeeklyEventManager getCurrentEvent(): " + plugin.getWeeklyEventManager().getCurrentEvent());
        } else {
            plugin.getLogger().warning("[DEBUG] WeeklyEventManager es null!");
        }
        
        if (!isActive()) {
            plugin.getLogger().info("[DEBUG] ExplosiveWeek no está activo, saltando evento de muerte");
            return;
        }

        Player victim = event.getEntity();
        
        // Verificar si el jugador está en un mundo excluido
        if (isPlayerInExcludedWorld(victim)) return;
        Player killer = victim.getKiller();
        
        plugin.getLogger().info("[DEBUG] Víctima: " + (victim != null ? victim.getName() : "null") + 
                               ", Asesino: " + (killer != null ? killer.getName() : "null"));

        if (killer == null || killer.equals(victim)) {
            return; // No es PvP o es suicidio
        }

        World world = victim.getWorld();
        UUID killerUUID = killer.getUniqueId();

        // Verificar si hay tormenta (lluvia + truenos O solo truenos)
        boolean isStormy = world.isThundering() || (world.hasStorm() && world.isThundering());
        plugin.getLogger().info("[DEBUG] Condiciones climáticas - hasStorm: " + world.hasStorm() + 
                               ", isThundering: " + world.isThundering() + ", isStormy: " + isStormy);

        if (isStormy) {
             // Verificar si el jugador ya completó el desafío
             if (hasChallengeCompleted(killerUUID, "storm_killer")) {
                 plugin.getLogger().info("[DEBUG] El jugador " + killer.getName() + " ya completó el desafío storm_killer");
                 return;
             }
            
            // Actualizar progreso del desafío antes de completarlo
            updateChallengeProgress(killerUUID, "storm_killer", 1, 1);
            
            // Agregar al conjunto de asesinos en tormenta
            stormPvpKillers.add(killerUUID);
            plugin.getLogger().info("[DEBUG] Agregado " + killer.getName() + " a stormPvpKillers. Total: " + stormPvpKillers.size());
            
            // Completar el desafío
            completeChallengeForPlayer(killerUUID, "storm_killer");
            plugin.getLogger().info("[DEBUG] Desafío storm_killer completado para " + killer.getName());
            
            // Enviar mensaje al jugador
            killer.sendMessage(MM.toComponent("<gold><b>¡DESAFÍO COMPLETADO!</b> <gray>Has matado a un jugador durante una tormenta."));
        } else {
            plugin.getLogger().info("[DEBUG] Kill PvP fuera de tormenta: " + killer.getName() + " mató a " + victim.getName());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isActive.get()) return;
        
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        // Verificar si el evento es en un mundo excluido
        if (isWorldExcluded(entity.getWorld())) return;
        
        // Iron Golem death explosion
        if (entity instanceof IronGolem) {
            Location location = entity.getLocation();
            entity.getWorld().createExplosion(location, 4.0f, false, true);
        }
        
        // Ghast kill in overworld challenge (during storm)
        if (entity instanceof Ghast && 
            entity.getWorld().getEnvironment() == Environment.NORMAL && 
            killer != null &&
            entity.getWorld().hasStorm()) {
            
            if (!ghastKillers.contains(killer.getUniqueId())) {
                // Actualizar progreso del desafío antes de completarlo
                updateChallengeProgress(killer.getUniqueId(), "ghast_killer", 1, 1);
                
                ghastKillers.add(killer.getUniqueId());
                
                // Award the challenge reward
                awardGhastKillChallenge(killer);
            }
        }
        
        // Warden kill challenge (during storm)
        if (entity instanceof Warden && 
            killer != null &&
            entity.getWorld().hasStorm()) {
            
            if (!wardenKillers.contains(killer.getUniqueId())) {
                // Actualizar progreso del desafío antes de completarlo
                updateChallengeProgress(killer.getUniqueId(), "warden_storm_killer", 1, 1);
                
                wardenKillers.add(killer.getUniqueId());
                
                // Award the challenge reward
                awardWardenKillChallenge(killer);
            }
        }
        
        // Track mob head collection for challenge
        if (killer != null && classicHostileMobs.contains(entity.getType())) {
            // Check if the mob dropped its head
            for (ItemStack drop : event.getDrops()) {
                if (drop.getType() == Material.ZOMBIE_HEAD || 
                    drop.getType() == Material.SKELETON_SKULL || 
                    drop.getType() == Material.CREEPER_HEAD) {
                    
                    // Add to the player's collection
                    mobHeadCollectors.computeIfAbsent(killer.getUniqueId(), k -> new HashSet<>())
                                    .add(entity.getType());
                    
                    // Actualizar progreso del desafío incrementalmente
                    Set<EntityType> collectedHeads = mobHeadCollectors.get(killer.getUniqueId());
                    updateChallengeProgress(killer.getUniqueId(), "mob_head_collector", collectedHeads.size(), 3);
                    
                    // Check if they've collected all heads
                    if (collectedHeads.size() >= classicHostileMobs.size()) {
                        // Award the challenge reward
                        awardMobHeadCollectionChallenge(killer);
                    }
                    
                    break;
                }
            }
        }
        
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isActive.get()) return;
        
        // Verificar si el jugador está en un mundo excluido
        if (isPlayerInExcludedWorld(event.getPlayer())) return;
        
        Block block = event.getBlock();
        Material type = block.getType();
        
        // 10% chance of explosion when mining certain ores
        if ((type == Material.COAL_ORE || type == Material.DEEPSLATE_COAL_ORE ||
             type == Material.IRON_ORE || type == Material.DEEPSLATE_IRON_ORE ||
             type == Material.REDSTONE_ORE || type == Material.DEEPSLATE_REDSTONE_ORE) && 
            random.nextInt(100) < 10) {
            
            // Create a small explosion (2 hearts damage)
            Location location = block.getLocation().add(0.5, 0.5, 0.5);
            block.getWorld().createExplosion(location, 1.0f, false, true);
            
            // Send message to the player
            event.getPlayer().sendMessage(MM.toComponent("<red>¡El mineral ha explotado!"));
        }
    }
    
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!isActive.get()) return;
        
        // If weather is changing to stormy, check for ghast spawning
        if (event.toWeatherState() && event.getWorld().getEnvironment() == Environment.NORMAL) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (isActive.get() && event.getWorld().hasStorm()) {
                        for (Player player : event.getWorld().getPlayers()) {
                            if (random.nextInt(100) < 30) { // 30% chance per player
                                spawnGhastNearPlayer(player);
                            }
                        }
                    }
                }
            }.runTaskLater(plugin, 20L * 10); // Wait 10 seconds after weather change
        }
    }
    
    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        if (!isActive.get()) return;
        
        // If thunder is starting, spawn more ghasts
        if (event.toThunderState() && event.getWorld().getEnvironment() == Environment.NORMAL) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (isActive.get() && event.getWorld().isThundering()) {
                        for (Player player : event.getWorld().getPlayers()) {
                            if (random.nextInt(100) < 50) { // 50% chance per player
                                spawnGhastNearPlayer(player);
                            }
                        }
                    }
                }
            }.runTaskLater(plugin, 20L * 5); // Wait 5 seconds after thunder starts
        }
    }
    
    // Challenge reward methods
    
    private void awardGhastKillChallenge(Player player) {
        if (hasChallengeCompleted(player.getUniqueId(), "ghast_killer")) {
            // Already awarded
            return;
        }
        
        // Actualizar progreso del desafío antes de completarlo
        updateChallengeProgress(player.getUniqueId(), "ghast_killer", 1, 1);
        
        // Usar el sistema oficial de desafíos
        completeChallengeForPlayer(player.getUniqueId(), "ghast_killer");
        
        // Mantener el tracking local para compatibilidad
        ghastKillers.add(player.getUniqueId());
    }
    
    private void awardMobHeadCollectionChallenge(Player player) {
        Set<EntityType> collectedHeads = mobHeadCollectors.getOrDefault(player.getUniqueId(), new HashSet<>());
        
        if (collectedHeads.size() >= classicHostileMobs.size()) {
            if (hasChallengeCompleted(player.getUniqueId(), "mob_head_collector")) {
                // Already awarded
                return;
            }
            
            // Actualizar progreso del desafío antes de completarlo
            updateChallengeProgress(player.getUniqueId(), "mob_head_collector", collectedHeads.size(), 3);
            
            // Usar el sistema oficial de desafíos
            completeChallengeForPlayer(player.getUniqueId(), "mob_head_collector");
            
        }
    }
    
    private void awardExplosionKillChallenge(Player player) {
        if (hasChallengeCompleted(player.getUniqueId(), "storm_killer")) {
            // Already awarded
            return;
        }
        
        // Actualizar progreso del desafío antes de completarlo
        updateChallengeProgress(player.getUniqueId(), "storm_killer", 1, 1);
        
        // Usar el sistema oficial de desafíos
        completeChallengeForPlayer(player.getUniqueId(), "storm_killer");
    }
    

    
    public boolean isActive() {
        return isActive.get();
    }

    public void pause() {
        if (!isActive.get() || isPaused.get()) return;
         
         isPaused.set(true);
        
        // Cancel the main task
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
    }

    public void resume() {
        if (!isActive.get() || !isPaused.get()) return;
        
        // Call parent resume to register event handlers
        super.resume();
        
        // Restart the main task
        startMainTask();
    }

    private void startMainTask() {
        // Cancel existing task if any with proper verification
        if (mainTask != null && !mainTask.isCancelled()) {
            mainTask.cancel();
            logger.info("[ExplosiveWeek] Previous main task cancelled before starting new one");
        }
        
        // Start the main task
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Check for players with explosive effects
                for (@SuppressWarnings("unused") Player player : Bukkit.getOnlinePlayers()) {
                    // Apply any ongoing effects or checks here
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 5); // Check every 5 seconds
        
        logger.info("[ExplosiveWeek] Main task started successfully");
    }
    
    // ========== MÉTODOS DE PERSISTENCIA ==========
    
    /**
     * Obtiene el conjunto de jugadores que han matado ghasts
     * @return Set de UUIDs de jugadores que mataron ghasts
     */
    public Set<UUID> getGhastKillers() {
        return new HashSet<>(ghastKillers);
    }
    
    /**
     * Obtiene el mapa de jugadores y las cabezas de mobs que han recolectado
     * @return Map de UUID a Set de EntityType
     */
    public Map<UUID, Set<EntityType>> getMobHeadCollectors() {
        Map<UUID, Set<EntityType>> copy = new HashMap<>();
        for (Map.Entry<UUID, Set<EntityType>> entry : mobHeadCollectors.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }
    
    /**
     * Obtiene el conjunto de jugadores que han matado a otros con explosiones
     * @return Set de UUIDs de jugadores que mataron con explosiones
     */
    public Set<UUID> getPlayerExplosionKillers() {
        return new HashSet<>(playerExplosionKillers);
    }
    
    /**
     * Carga los jugadores que han matado ghasts (para carga desde persistencia)
     * @param ghastKillers Set con UUIDs de jugadores que mataron ghasts
     */
    public void loadGhastKillers(Set<UUID> ghastKillers) {
        this.ghastKillers.clear();
        this.ghastKillers.addAll(ghastKillers);
    }
    
    /**
     * Carga los jugadores que han matado a otros con explosiones (para carga desde persistencia)
     * @param playerExplosionKillers Set con UUIDs de jugadores que mataron con explosiones
     */
    public void loadPlayerExplosionKillers(Set<UUID> playerExplosionKillers) {
        this.playerExplosionKillers.clear();
        this.playerExplosionKillers.addAll(playerExplosionKillers);
    }
    
    /**
     * Carga los coleccionistas de cabezas de mobs (para carga desde persistencia)
     * @param mobHeadCollectors Map con datos de coleccionistas de cabezas
     */
    public void loadMobHeadCollectors(Map<UUID, Set<EntityType>> mobHeadCollectors) {
        this.mobHeadCollectors.clear();
        this.mobHeadCollectors.putAll(mobHeadCollectors);
    }
    
    /**
     * Carga los jugadores que mataron wardens durante tormentas
     * @param wardenKillers Set con UUIDs de jugadores que mataron wardens durante tormentas
     */
    public void loadWardenKillers(Set<UUID> wardenKillers) {
        this.wardenKillers.clear();
        this.wardenKillers.addAll(wardenKillers);
    }
    
    /**
     * Obtiene el conjunto de jugadores que mataron wardens durante tormentas
     * @return Set de UUIDs de jugadores que mataron wardens durante tormentas
     */
    public Set<UUID> getWardenKillers() {
        return new HashSet<>(wardenKillers);
    }
    
    /**
     * Carga los jugadores que mataron a otros jugadores durante tormentas
     * @param stormPvpKillers Set con UUIDs de jugadores que mataron en PvP durante tormentas
     */
    public void loadStormPvpKillers(Set<UUID> stormPvpKillers) {
        this.stormPvpKillers.clear();
        this.stormPvpKillers.addAll(stormPvpKillers);
    }
    
    /**
     * Obtiene el conjunto de jugadores que mataron a otros jugadores durante tormentas
     * @return Set de UUIDs de jugadores que mataron en PvP durante tormentas
     */
    public Set<UUID> getStormPvpKillers() {
        return new HashSet<>(stormPvpKillers);
    }

    private void awardWardenKillChallenge(Player player) {
        if (hasChallengeCompleted(player.getUniqueId(), "warden_storm_killer")) {
            // Already awarded
            return;
        }
        
        // Actualizar progreso del desafío antes de completarlo
        updateChallengeProgress(player.getUniqueId(), "warden_storm_killer", 1, 1);
        
        // Usar el sistema oficial de desafíos
        completeChallengeForPlayer(player.getUniqueId(), "warden_storm_killer");
        
        // Mantener el tracking local para compatibilidad
        wardenKillers.add(player.getUniqueId());
    }
}