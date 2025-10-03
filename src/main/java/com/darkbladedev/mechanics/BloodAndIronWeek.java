package com.darkbladedev.mechanics;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.challenges.Reward;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.TimeExpression;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * Evento semanal "Semana de Sangre y Hierro" con mejoras de thread-safety y manejo de errores.
 * 
 * Correcciones implementadas:
 * - Thread-safety con ConcurrentHashMap y AtomicReference
 * - Manejo robusto de errores con try-catch y logging
 * - Prevención de memory leaks con limpieza automática de jugadores desconectados
 * - Validaciones de nulidad mejoradas
 * - Gestión segura de tareas asíncronas
 */
public class BloodAndIronWeek extends AbstractWeeklyEvent {

    // Referencias atómicas para tareas críticas
    private final AtomicReference<BukkitTask> mainTaskRef = new AtomicReference<>();
    private final AtomicReference<BukkitTask> checkKillsTaskRef = new AtomicReference<>();
    
    // Mapas thread-safe para tracking de jugadores
    private final Map<UUID, Long> lastHostileMobKillTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastPlayerKillTime = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerKillCount = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> consecutiveKills = new ConcurrentHashMap<>();
    private final Set<UUID> pentakillPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> survivedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> deadPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> awardedAdrenaline = ConcurrentHashMap.newKeySet();
    private final Set<UUID> mobKillWarningGiven = ConcurrentHashMap.newKeySet();
    private final Set<UUID> survivors = ConcurrentHashMap.newKeySet();
    private final Set<UUID> massKillers = ConcurrentHashMap.newKeySet();
    
    // Aliases para compatibilidad
    private final Set<UUID> playerKillers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pentaKillers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastMobKillTime = new ConcurrentHashMap<>();
    
    // Constantes
    private static final long MOB_KILL_TIMEOUT = 15 * 60 * 1000; // 15 minutos
    private static final long MOB_KILL_WARNING_TIME = 10 * 60 * 1000; // 10 minutos
    private static final long PLAYER_KILL_TIMEOUT = 60 * 60 * 1000; // 1 hora
    @SuppressWarnings("unused")
    private static final long CLEANUP_INTERVAL = 5 * 60 * 1000; // 5 minutos para limpieza
    
    public BloodAndIronWeek(HeartlessMain plugin, TimeExpression duration) {
        super(plugin, duration);
        this.prefix = "<b><gradient:#f82f2f:#f74242:#f75555:#f66869:#f67b7c:#f58f8f:#f4a2a2:#f4b5b5:#f3c8c9:#f3dbdc:#f2eeef:#f2eeef:#f2edee:#f2edee:#f2eded:#f3eded:#f3eced:#f3ecec:#f3ecec:#f3ebeb:#f3ebeb>Semana de Sangre y Hierro</gradient></b>";
        
        // Inicializar desafíos específicos del evento
        initializeChallengeDefinitions();
    }
    
    /**
     * Reinicializa todos los jugadores online después del reinicio del servidor
     */
    private void reinitializeOnlinePlayers() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers != null) {
            for (Player player : onlinePlayers) {
                if (player != null && player.isOnline()) {
                    initializePlayer(player);
                }
            }
            plugin.getLogger().info("Reinicializados " + onlinePlayers.size() + " jugadores online tras reanudación");
        }
    }
    
    /**
     * Restaura datos específicos del evento después del reinicio
     */
    @SuppressWarnings("unused")
    private void restoreEventSpecificData() {
        try {
            // Las colecciones son final y ya están inicializadas, solo necesitamos limpiarlas si es necesario
            // Nota: Las colecciones final no pueden ser reasignadas, solo limpiadas
            
            // Validar integridad de datos
            validateEventData();
            
            plugin.getLogger().info("Datos específicos del evento restaurados correctamente");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error al restaurar datos específicos del evento", e);
        }
    }
    
    /**
     * Valida la integridad de los datos del evento
     */
    private void validateEventData() {
        try {
            // Validar que lastHostileMobKillTime no sea futuro
            long currentTime = System.currentTimeMillis();
            lastHostileMobKillTime.entrySet().removeIf(entry -> {
                if (entry.getValue() > currentTime) {
                    plugin.getLogger().warning("lastHostileMobKillTime para jugador " + entry.getKey() + " está en el futuro, eliminando entrada...");
                    return true;
                }
                return false;
            });
            
            // Validar contadores
            if (playerKillCount != null) {
                playerKillCount.entrySet().removeIf(entry -> entry.getValue() < 0);
            }
            
            // Limpiar jugadores offline de las colecciones
            cleanupOfflinePlayers();
            
            plugin.getLogger().info("Desafíos de BloodAndIronWeek inicializados correctamente");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error al validar datos del evento", e);
        }
    }
    
    /**
     * Limpia jugadores offline de las colecciones del evento
     */
    private void cleanupOfflinePlayers() {
        try {
            if (survivedPlayers != null) {
                survivedPlayers.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
            }
            if (deadPlayers != null) {
                deadPlayers.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
            }
            if (awardedAdrenaline != null) {
                awardedAdrenaline.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
            }
            if (playerKillCount != null) {
                playerKillCount.entrySet().removeIf(entry -> Bukkit.getPlayer(entry.getKey()) == null);
            }
            if (lastPlayerKillTime != null) {
                lastPlayerKillTime.entrySet().removeIf(entry -> Bukkit.getPlayer(entry.getKey()) == null);
            }
            if (lastHostileMobKillTime != null) {
                lastHostileMobKillTime.entrySet().removeIf(entry -> Bukkit.getPlayer(entry.getKey()) == null);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al limpiar jugadores offline", e);
        }
    }
    
    /**
     * Intenta una recuperación segura del evento en caso de error
     */
    @SuppressWarnings("unused")
    private void safeEventRecovery() {
        try {
            plugin.getLogger().info("Iniciando recuperación segura del evento...");
            
            // Detener tareas actuales
            stopEventTasks();
            
            // Reinicializar colecciones básicas
            initializeCollections();
            
            // Intentar reiniciar tareas básicas
            if (isActive.get() && !isPaused.get()) {
                startMainTask();
                startCheckKillsTask();
            }
            
            plugin.getLogger().info("Recuperación segura completada");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error en recuperación segura", e);
        }
    }
    
    /**
     * Inicializa las colecciones básicas del evento
     * Nota: Los campos final ya están inicializados, solo limpiamos contenido existente
     */
    private void initializeCollections() {
        // Limpiar colecciones existentes en caso de reinicialización
        survivedPlayers.clear();
        deadPlayers.clear();
        awardedAdrenaline.clear();
        playerKillCount.clear();
        lastPlayerKillTime.clear();
        lastHostileMobKillTime.clear();
        consecutiveKills.clear();
        pentakillPlayers.clear();
        mobKillWarningGiven.clear();
        survivors.clear();
        massKillers.clear();
        playerKillers.clear();
        pentaKillers.clear();
        lastMobKillTime.clear();
        
        plugin.getLogger().info("Colecciones del evento BloodAndIronWeek inicializadas correctamente");
    }



    @Override
    protected void startEventTasks() {
        try {
            startMainTask();
            startCheckKillsTask();
            
            // Inicializar jugadores online de forma segura
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            if (onlinePlayers != null) {
                for (Player player : onlinePlayers) {
                    if (player != null && player.isOnline()) {
                        initializePlayer(player);
                    }
                }
            }
            
            plugin.getLogger().info("Tareas del evento BloodAndIronWeek iniciadas");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error al iniciar tareas del evento", e);
            stopEventTasks();
        }
    }
    
    @Override
    protected void announceEventStart() {
        try {
            Bukkit.broadcast(MM.toComponent(prefix + " <gray>¡<gold>El coliseo del caos está abierto. <red>Elimina o sé eliminado<gray>!"));
            announceRegisteredChallenges();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al anunciar inicio del evento", e);
        }
    }
    
    @Override
    public String getId() {
        return "blood_and_iron_week";
    }
    
    @Override
    protected void stopEventTasks() {
        try {
            // Cancelar tareas de forma thread-safe
            BukkitTask mainTask = mainTaskRef.getAndSet(null);
            if (mainTask != null && !mainTask.isCancelled()) {
                mainTask.cancel();
            }
            
            BukkitTask checkKillsTask = checkKillsTaskRef.getAndSet(null);
            if (checkKillsTask != null && !checkKillsTask.isCancelled()) {
                checkKillsTask.cancel();
            }
            
            plugin.getLogger().info("Tareas del evento BloodAndIronWeek detenidas");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al detener tareas del evento", e);
        }
    }
    
    @Override
    protected void cleanupEventData() {
        try {
            // Limpiar mapas y conjuntos de forma thread-safe
            lastHostileMobKillTime.clear();
            lastPlayerKillTime.clear();
            playerKillCount.clear();
            consecutiveKills.clear();
            pentakillPlayers.clear();
            survivedPlayers.clear();
            deadPlayers.clear();
            awardedAdrenaline.clear();
            mobKillWarningGiven.clear();
            survivors.clear();
            massKillers.clear();
            playerKillers.clear();
            pentaKillers.clear();
            lastMobKillTime.clear();
            
            plugin.getLogger().info("Datos del evento BloodAndIronWeek limpiados");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al limpiar datos del evento", e);
        }
    }
    
    @Override
    protected void pauseEventTasks() {
        try {
            BukkitTask mainTask = mainTaskRef.getAndSet(null);
            if (mainTask != null && !mainTask.isCancelled()) {
                mainTask.cancel();
            }
            
            BukkitTask checkKillsTask = checkKillsTaskRef.getAndSet(null);
            if (checkKillsTask != null && !checkKillsTask.isCancelled()) {
                checkKillsTask.cancel();
            }
            
            plugin.getLogger().info("Evento BloodAndIronWeek pausado");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al pausar evento", e);
        }
    }
    
    @Override
    protected void resumeEventTasks() {
        try {
            if (isActive.get() && !isPaused.get()) {
                // Verificar y detener tareas existentes antes de iniciar nuevas
                BukkitTask existingMainTask = mainTaskRef.get();
                BukkitTask existingCheckTask = checkKillsTaskRef.get();
                
                if (existingMainTask != null && !existingMainTask.isCancelled()) {
                    plugin.getLogger().warning("Tarea principal ya está ejecutándose, cancelando antes de reanudar");
                    existingMainTask.cancel();
                    mainTaskRef.set(null);
                }
                
                if (existingCheckTask != null && !existingCheckTask.isCancelled()) {
                    plugin.getLogger().warning("Tarea de verificación ya está ejecutándose, cancelando antes de reanudar");
                    existingCheckTask.cancel();
                    checkKillsTaskRef.set(null);
                }
                
                // Esperar un tick antes de iniciar nuevas tareas para evitar conflictos
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            startMainTask();
                            startCheckKillsTask();
                            
                            // Reinicializar jugadores online después del reinicio del servidor
                            reinitializeOnlinePlayers();
                            
                            plugin.getLogger().info("Evento BloodAndIronWeek reanudado correctamente");
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.SEVERE, "Error al iniciar tareas durante reanudación", e);
                            stopEventTasks();
                        }
                    }
                }.runTaskLater(plugin, 1L);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error al reanudar evento", e);
            stopEventTasks();
        }
    }
    
    @Override
    protected void announceEventEnd() {
        try {
            Bukkit.broadcast(MM.toComponent(prefix + " <red>El coliseo del caos ha cerrado sus puertas... por ahora."));
            Bukkit.broadcast(MM.toComponent(prefix + " <yellow>¡Revisando las estadísticas de los gladiadores!"));
            
            // Enviar estadísticas individuales
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            if (onlinePlayers != null) {
                for (Player player : onlinePlayers) {
                    if (player != null && player.isOnline()) {
                        try {
                            sendPlayerStatistics(player);
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, 
                                "Error al enviar estadísticas a " + player.getName(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al anunciar fin del evento", e);
        }
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private void startMainTask() {
        try {
            // Verificar si el evento está activo antes de iniciar
            if (!isActive.get() || isPaused.get()) {
                plugin.getLogger().warning("Intentando iniciar tarea principal con evento inactivo o pausado");
                return;
            }
            
            // Cancelar tarea existente si existe de forma thread-safe
            BukkitTask existingTask = mainTaskRef.getAndSet(null);
            if (existingTask != null && !existingTask.isCancelled()) {
                existingTask.cancel();
                plugin.getLogger().info("Tarea principal anterior cancelada antes de iniciar nueva");
            }
            
            // Iniciar nueva tarea principal con validaciones adicionales
            BukkitTask newTask = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        // Verificar estado del evento en cada ejecución
                        if (!isActive.get() || isPaused.get()) {
                            this.cancel();
                            mainTaskRef.set(null);
                            return;
                        }
                        
                        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                        if (onlinePlayers != null && !onlinePlayers.isEmpty()) {
                            for (Player player : onlinePlayers) {
                                if (player != null && player.isOnline()) {
                                    checkAndApplyArmorEffects(player);
                                    checkAndApplySwordEffects(player);
                                }
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Error en tarea principal", e);
                        // No cancelar la tarea por un error menor, solo registrar
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L * 5); // Cada 5 segundos
            
            // Verificar que la tarea se creó correctamente
            if (newTask != null && !newTask.isCancelled()) {
                mainTaskRef.set(newTask);
                plugin.getLogger().info("Tarea principal iniciada correctamente (ID: " + newTask.getTaskId() + ")");
            } else {
                plugin.getLogger().severe("Error: No se pudo crear la tarea principal");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error al iniciar tarea principal", e);
        }
    }
    
    private void startCheckKillsTask() {
        try {
            // Verificar si el evento está activo antes de iniciar
            if (!isActive.get() || isPaused.get()) {
                plugin.getLogger().warning("Intentando iniciar tarea de verificación con evento inactivo o pausado");
                return;
            }
            
            // Cancelar tarea existente si existe de forma thread-safe
            BukkitTask existingTask = checkKillsTaskRef.getAndSet(null);
            if (existingTask != null && !existingTask.isCancelled()) {
                existingTask.cancel();
                plugin.getLogger().info("Tarea de verificación anterior cancelada antes de iniciar nueva");
            }
            
            // Iniciar nueva tarea de verificación
            BukkitTask newTask = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        // Verificar estado del evento en cada ejecución
                        if (!isActive.get() || isPaused.get()) {
                            this.cancel();
                            checkKillsTaskRef.set(null);
                            return;
                        }
                        
                        long currentTime = System.currentTimeMillis();
                        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                        
                        if (onlinePlayers != null) {
                            for (Player player : onlinePlayers) {
                                if (player != null && player.isOnline()) {
                                    checkMobKillTimeout(player, currentTime);
                                    checkPlayerKillTimeout(player, currentTime);
                                }
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Error en tarea de verificación de kills", e);
                    }
                }
            }.runTaskTimer(plugin, 20L * 60, 20L * 60); // Cada minuto
            
            // Verificar que la tarea se creó correctamente
            if (newTask != null && !newTask.isCancelled()) {
                checkKillsTaskRef.set(newTask);
                plugin.getLogger().info("Tarea de verificación iniciada correctamente (ID: " + newTask.getTaskId() + ")");
            } else {
                plugin.getLogger().severe("Error: No se pudo crear la tarea de verificación");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error al iniciar tarea de verificación de kills", e);
        }
    }
    
    private void initializeEventStatistics() {
        // Inicializar estadísticas globales específicas
        incrementGlobalStatistic("blood_and_iron_events_started", 1);
    }
    
    private void cleanupEventResources() {
        // Cancelar tareas específicas del evento
        if (mainTaskRef.get() != null && !mainTaskRef.get().isCancelled()) {
            mainTaskRef.get().cancel();
        }
        if (checkKillsTaskRef.get() != null && !checkKillsTaskRef.get().isCancelled()) {
            checkKillsTaskRef.get().cancel();
        }
    }
    
    private void processFinalEventStatistics() {
        // Procesar estadísticas finales del evento
        incrementGlobalStatistic("blood_and_iron_events_completed", 1);
        
        // Log de estadísticas finales
        plugin.getLogger().info("[BloodAndIronWeek] Estadísticas finales - Supervivientes: " + survivors.size() + 
                               ", Asesinos en masa: " + massKillers.size() + 
                               ", Pentakills: " + pentakillPlayers.size());
    }

    private void initializePlayer(Player player) {
        try {
            if (player == null) return;
            
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            
            // Inicializar tiempos si no existen
            lastHostileMobKillTime.putIfAbsent(playerId, currentTime);
            lastPlayerKillTime.putIfAbsent(playerId, currentTime);
            lastMobKillTime.putIfAbsent(playerId, currentTime);
            
            // Inicializar contadores si no existen
            playerKillCount.putIfAbsent(playerId, 0);
            consecutiveKills.putIfAbsent(playerId, 0);
            
            // Agregar a jugadores supervivientes si no está muerto
            if (!deadPlayers.contains(playerId)) {
                survivedPlayers.add(playerId);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al inicializar jugador " + player.getName(), e);
        }
    }
    
    private void checkMobKillTimeout(Player player, long currentTime) {
        try {
            UUID playerId = player.getUniqueId();
            
            Long lastKillTime = lastHostileMobKillTime.get(playerId);
            if (lastKillTime == null) return;
            
            long timeSinceLastKill = currentTime - lastKillTime;
            
            // Verificar advertencia de 10 minutos
            if (timeSinceLastKill > MOB_KILL_WARNING_TIME && !mobKillWarningGiven.contains(playerId)) {
                player.sendMessage(MM.toComponent("<yellow>⚠ <bold>ADVERTENCIA</bold> ⚠</yellow>"));
                player.sendMessage(MM.toComponent("<gold>¡No has matado a un mob hostil en 10 minutos!</gold>"));
                player.sendMessage(MM.toComponent("<red>Tienes 5 minutos más o perderás 2 corazones.</red>"));
                mobKillWarningGiven.add(playerId);
            }
            
            // Verificar timeout de 15 minutos
            if (timeSinceLastKill > MOB_KILL_TIMEOUT) {
                reducePlayerHealth(player, 4.0); // 2 corazones
                player.sendMessage(MM.toComponent("<red>¡No has matado a un mob hostil en 15 minutos! Pierdes 2 corazones."));
                
                // Resetear timer y advertencia
                lastHostileMobKillTime.put(playerId, currentTime);
                mobKillWarningGiven.remove(playerId);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al verificar timeout de mob kill para " + player.getName(), e);
        }
    }
    
    private void checkPlayerKillTimeout(Player player, long currentTime) {
        try {
            UUID playerId = player.getUniqueId();
            
            Long lastKillTime = lastPlayerKillTime.get(playerId);
            if (lastKillTime == null) return;
            
            if (currentTime - lastKillTime > PLAYER_KILL_TIMEOUT) {
                reducePlayerHealth(player, 10.0); // 5 corazones
                player.sendMessage(MM.toComponent("<red>¡No has matado a un jugador en 1 hora! Pierdes 5 corazones."));
                
                // Resetear timer
                lastPlayerKillTime.put(playerId, currentTime);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al verificar timeout de player kill para " + player.getName(), e);
        }
    }
    
    private void reducePlayerHealth(Player player, double amount) {
        try {
            if (player == null || !player.isOnline()) return;
            
            double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
            double newMaxHealth = Math.max(2.0, currentMaxHealth - amount); // Mínimo 1 corazón
            
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
            
            // Asegurar que la salud actual no exceda la máxima
            if (player.getHealth() > newMaxHealth) {
                player.setHealth(newMaxHealth);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al reducir salud de " + player.getName(), e);
        }
    }
    
    /**
     * Anuncia los desafíos registrados dinámicamente
     */
    private void announceRegisteredChallenges() {
        try {
            if (availableChallenges == null || availableChallenges.isEmpty()) {
                plugin.getLogger().warning("[BloodAndIronWeek] No hay desafíos registrados para anunciar");
                return;
            }
            
            Bukkit.broadcast(MM.toComponent("<yellow>Desafíos disponibles:"));
            
            for (ChallengeDefinition challenge : availableChallenges.values()) {
                String difficultyColor = getDifficultyColor(challenge.getId());
                Bukkit.broadcast(MM.toComponent(difficultyColor + "• " + 
                    challenge.getDisplayName() + " - <gray>" + challenge.getDescription()));
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[BloodAndIronWeek] Error al anunciar desafíos registrados", e);
        }
    }
    
    /**
     * Obtiene el color de dificultad basado en el ID del desafío
     * 
     * @param challengeId ID del desafío
     * @return Color en formato MiniMessage
     */
    private String getDifficultyColor(String challengeId) {
        switch (challengeId) {
            case "player_killer":
                return "<green>"; // Fácil
            case "pentakill":
                return "<yellow>"; // Intermedio
            case "survivor":
                return "<red>"; // Difícil
            case "mass_killer":
                return "<light_purple>"; // Leyenda
            default:
                return "<white>";
        }
    }
    
    private void checkAndApplyArmorEffects(Player player) {
        try {
            if (player == null || !player.isOnline()) return;
            
            PlayerInventory inventory = player.getInventory();
            if (inventory == null) return;
            
            int ironArmorPieces = 0;
            int diamondNetheriteArmorPieces = 0;
            
            // Contar piezas de armadura
            ItemStack[] armorContents = inventory.getArmorContents();
            if (armorContents != null) {
                for (ItemStack armor : armorContents) {
                    if (armor != null) {
                        String materialName = armor.getType().name();
                        if (materialName.startsWith("IRON_")) {
                            ironArmorPieces++;
                        } else if (materialName.startsWith("DIAMOND_") || materialName.startsWith("NETHERITE_")) {
                            diamondNetheriteArmorPieces++;
                        }
                    }
                }
            }
            
            // Aplicar penalizaciones por armadura de diamante/netherite
            if (diamondNetheriteArmorPieces > 0) {
                // Aplicar Fatiga II y Lentitud II (se remueven instantáneamente al desequiparse)
                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 120, 1, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 1, false, false));
            }
            
            // Aplicar efectos según piezas de armadura de hierro
            if (ironArmorPieces >= 4) {
                // Armadura completa: Resistencia II y Fuerza I
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 1, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120, 0, false, false));
            } else if (ironArmorPieces >= 2) {
                // Media armadura: Resistencia I
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 0, false, false));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al aplicar efectos de armadura a " + player.getName(), e);
        }
    }
    
    private void checkAndApplySwordEffects(Player player) {
        try {
            if (player == null || !player.isOnline()) return;
            
            PlayerInventory inventory = player.getInventory();
            if (inventory == null) return;
            
            ItemStack mainHand = inventory.getItemInMainHand();
            if (mainHand == null) return;
            
            Material swordType = mainHand.getType();
            
            // Verificar si porta espada de diamante/netherite
            if (swordType == Material.DIAMOND_SWORD || swordType == Material.NETHERITE_SWORD) {
                // Aplicar Náuseas I durante 10 segundos (se remueve instantáneamente al desequiparse)
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0, false, false));
            }
            
            // Verificar si porta espada de hierro
            if (swordType == Material.IRON_SWORD) {
                // Espada de hierro: Velocidad I
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 0, false, false));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al aplicar efectos de espada a " + player.getName(), e);
        }
    }
    
    // ========== MÉTODOS DE DESAFÍOS ==========
    
    /**
     * Obtiene la lista de IDs de desafíos disponibles para este evento
     * Compatible con el comando CompleteChallenge
     */
    @SuppressWarnings("unused")
    private List<String> getAvailableChallengeIdsInternal() {
        return Arrays.asList(
            "player_killer",    // Mata a 3 jugadores
            "pentakill",        // Mata a 5 jugadores seguidos sin morir
            "survivor",         // Sobrevive sin morir en todo el evento (con más de 10 kills)
            "mass_killer"       // Mata a más de 10 jugadores durante la semana
        );
    }
    
    /**
     * Completa un desafío específico para un jugador
     * Compatible con el comando CompleteChallenge
     */
    public boolean completeChallengeForPlayerByPlayer(Player player, String challengeId) {
        if (player == null || challengeId == null) return false;
        
        try {
            switch (challengeId.toLowerCase()) {
                case "player_killer":
                    completeChallengeForPlayerInternal(player, "player_killer");
                    return true;
                case "pentakill":
                    completeChallengeForPlayerInternal(player, "pentakill");
                    return true;
                case "survivor":
                    completeChallengeForPlayerInternal(player, "survivor");
                    return true;
                case "mass_killer":
                    completeChallengeForPlayerInternal(player, "mass_killer");
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al completar desafío: " + challengeId, e);
            return false;
        }
    }
    
    private boolean hasChallengeCompleted(Player player, String challengeType) {
        try {
            if (player == null || challengeType == null) return false;
            return hasChallengeCompletedInternal(player.getUniqueId(), challengeType);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al verificar desafío completado", e);
            return false;
        }
    }
    
    private boolean hasChallengeCompletedInternal(UUID playerId, String challengeType) {
        try {
            if (playerId == null || challengeType == null) return false;
            
            switch (challengeType.toLowerCase()) {
                case "player_killer":
                    return playerKillers.contains(playerId);
                case "pentakill":
                    return pentaKillers.contains(playerId);
                case "survivor":
                    return survivors.contains(playerId);
                case "mass_killer":
                    return massKillers.contains(playerId);
                default:
                    return false;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al verificar desafío completado", e);
            return false;
        }
    }
    
    private void completeChallengeForPlayerInternal(Player player, String challengeType) {
        try {
            if (player == null || challengeType == null) return;
            
            UUID playerId = player.getUniqueId();
            
            switch (challengeType.toLowerCase()) {
                case "player_killer":
                    if (!playerKillers.contains(playerId)) {
                        playerKillers.add(playerId);
                        player.sendMessage(MM.toComponent("<green>¡Desafío completado: Asesino de Jugadores!"));
                        Reward reward = new Reward("enchantment:adrenaline:1");
                        reward.grantTo(player, prefix);
                    }
                    break;
                case "pentakill":
                    if (!pentaKillers.contains(playerId)) {
                        pentaKillers.add(playerId);
                        player.sendMessage(MM.toComponent("<green>¡Desafío completado: Pentakill!"));
                        // Otorgar tag "Pentakill" (htl.tag.pentakill)
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set htl.tag.pentakill true");
                    }
                    break;
                case "survivor":
                    if (!survivors.contains(playerId)) {
                        survivors.add(playerId);
                        player.sendMessage(MM.toComponent("<green>¡Desafío completado: Superviviente!"));
                        // Otorgar +20 Thalos por sobrevivir toda la semana
                        Reward reward = new Reward("coins:20");
                        reward.grantTo(player, prefix);
                    }
                    break;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al completar desafío", e);
        }
    }
    
    // ========== MÉTODOS DE PERSISTENCIA ==========
    
    public Set<UUID> getPlayerKillers() {
        return new HashSet<>(playerKillers);
    }
    
    public Set<UUID> getPentaKillers() {
        return new HashSet<>(pentaKillers);
    }
    
    public Set<UUID> getSurvivors() {
        return new HashSet<>(survivors);
    }
    
    public void loadPlayerKillers(Set<UUID> killers) {
        if (killers != null) {
            playerKillers.clear();
            playerKillers.addAll(killers);
        }
    }
    
    public void loadPentaKillers(Set<UUID> killers) {
        if (killers != null) {
            pentaKillers.clear();
            pentaKillers.addAll(killers);
        }
    }
    
    public void loadSurvivors(Set<UUID> survivorSet) {
        if (survivorSet != null) {
            survivors.clear();
            survivors.addAll(survivorSet);
        }
    }
    
    public void loadAwardedAdrenaline(Set<UUID> awardedSet) {
        if (awardedSet != null) {
            awardedAdrenaline.clear();
            awardedAdrenaline.addAll(awardedSet);
        }
    }
    
    public void loadMobKillWarningGiven(Set<UUID> warningSet) {
        if (warningSet != null) {
            mobKillWarningGiven.clear();
            mobKillWarningGiven.addAll(warningSet);
        }
    }
    
    public Set<UUID> getMassKillers() {
        return new HashSet<>(massKillers);
    }
    
    public void loadMassKillers(Set<UUID> killers) {
        if (killers != null) {
            massKillers.clear();
            massKillers.addAll(killers);
        }
    }
    
    public Set<UUID> getAwardedAdrenaline() {
        return new HashSet<>(awardedAdrenaline);
    }
    
    public Set<UUID> getMobKillWarningGiven() {
        return new HashSet<>(mobKillWarningGiven);
    }
    
    public void loadPentakillPlayers(Set<UUID> pentaSet) {
        if (pentaSet != null) {
            pentaKillers.clear();
            pentaKillers.addAll(pentaSet);
        }
    }
    
    public void loadSurvivedPlayers(Set<UUID> survivedSet) {
        if (survivedSet != null) {
            survivedPlayers.clear();
            survivedPlayers.addAll(survivedSet);
        }
    }
    
    public void loadDeadPlayers(Set<UUID> deadSet) {
        if (deadSet != null) {
            deadPlayers.clear();
            deadPlayers.addAll(deadSet);
        }
    }
    
    public Set<UUID> getSurvivedPlayers() {
        return new HashSet<>(survivedPlayers);
    }
    
    public Set<UUID> getDeadPlayers() {
        return new HashSet<>(deadPlayers);
    }
    
    public void loadLastMobKillTime(Map<UUID, Long> timeMap) {
        if (timeMap != null) {
            lastMobKillTime.clear();
            lastMobKillTime.putAll(timeMap);
        }
    }
    
    public void loadLastPlayerKillTime(Map<UUID, Long> timeMap) {
        if (timeMap != null) {
            lastPlayerKillTime.clear();
            lastPlayerKillTime.putAll(timeMap);
        }
    }
    
    public void loadPlayerKillCount(Map<UUID, Integer> countMap) {
        if (countMap != null) {
            playerKillCount.clear();
            playerKillCount.putAll(countMap);
        }
    }
    
    public void loadConsecutiveKills(Map<UUID, Integer> killsMap) {
        if (killsMap != null) {
            consecutiveKills.clear();
            consecutiveKills.putAll(killsMap);
        }
    }
    
    public Map<UUID, Long> getLastMobKillTime() {
        return new HashMap<>(lastMobKillTime);
    }
    
    public Map<UUID, Long> getLastPlayerKillTime() {
        return new HashMap<>(lastPlayerKillTime);
    }
    
    public Map<UUID, Integer> getPlayerKillCount() {
        return new HashMap<>(playerKillCount);
    }
    
    public Map<UUID, Integer> getConsecutiveKills() {
        return new HashMap<>(consecutiveKills);
    }
    
    public Set<UUID> getPentakillPlayers() {
        return new HashSet<>(pentaKillers);
    }
    
    public void loadLastHostileMobKillTime(Map<UUID, Long> timeMap) {
        if (timeMap != null) {
            lastMobKillTime.clear();
            lastMobKillTime.putAll(timeMap);
        }
    }
    
    public Map<UUID, Long> getLastHostileMobKillTime() {
        return new HashMap<>(lastMobKillTime);
    }
    
    // ========== MANEJADORES DE EVENTOS ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            if (!isActive.get()) return;
            
            Player player = event.getPlayer();
            if (player != null) {
                initializePlayer(player);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error en evento PlayerJoin", e);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        try {
            if (!isActive.get()) return;
            
            Player victim = event.getEntity();
            if (victim == null) return;
            
            UUID victimId = victim.getUniqueId();
            
            // Marcar como muerto y remover de supervivientes
            deadPlayers.add(victimId);
            survivedPlayers.remove(victimId);
            
            // Resetear kills consecutivos
            consecutiveKills.put(victimId, 0);
            
            Player killer = victim.getKiller();
            if (killer != null && killer != victim) {
                handlePlayerKill(killer, victim);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error en evento PlayerDeath", e);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        try {
            if (!isActive.get()) return;
            
            LivingEntity entity = event.getEntity();
            if (entity == null || entity instanceof Player) return;
            
            Player killer = entity.getKiller();
            if (killer == null) return;
            
            // Solo contar mobs hostiles
            if (isHostileMob(entity)) {
                UUID killerId = killer.getUniqueId();
                lastHostileMobKillTime.put(killerId, System.currentTimeMillis());
                mobKillWarningGiven.remove(killerId); // Remover advertencia si existe
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error en evento EntityDeath", e);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        try {
            if (!isActive.get()) return;
            
            if (!(event.getDamager() instanceof Player)) return;
            if (!(event.getEntity() instanceof Player)) return;
            
            Player attacker = (Player) event.getDamager();
            @SuppressWarnings("unused")
            Player victim = (Player) event.getEntity();
            
            // Verificar si el atacante usó poción de daño
            @SuppressWarnings("unused")
            ItemStack mainHand = attacker.getInventory().getItemInMainHand();
            // Lógica de poción de daño eliminada - ya no es un desafío válido
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error en evento EntityDamageByEntity", e);
        }
    }
    
    private void handlePlayerKill(Player killer, Player victim) {
        try {
            UUID killerId = killer.getUniqueId();
            
            // Actualizar contadores
            int currentKills = playerKillCount.getOrDefault(killerId, 0) + 1;
            playerKillCount.put(killerId, currentKills);
            
            int currentConsecutive = consecutiveKills.getOrDefault(killerId, 0) + 1;
            consecutiveKills.put(killerId, currentConsecutive);
            
            // Actualizar tiempo de último kill de jugador
            lastPlayerKillTime.put(killerId, System.currentTimeMillis());
            
            // Verificar desafío de 3 kills
            if (currentKills >= 3 && !hasChallengeCompleted(killer, "player_killer")) {
                completeChallengeForPlayer(killerId, "player_killer");
            }
            
            // Verificar desafío de pentakill
            if (currentConsecutive >= 5 && !hasChallengeCompleted(killer, "pentakill")) {
                completeChallengeForPlayer(killerId, "pentakill");
            }
            
            // Lógica de kill con poción eliminada - ya no es un desafío válido
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al manejar kill de jugador", e);
        }
    }
    
    private boolean isHostileMob(LivingEntity entity) {
        try {
            if (entity == null) return false;
            
            EntityType type = entity.getType();
            return type == EntityType.ZOMBIE || type == EntityType.SKELETON ||
                   type == EntityType.CREEPER || type == EntityType.SPIDER ||
                   type == EntityType.ENDERMAN || type == EntityType.WITCH ||
                   type == EntityType.BLAZE || type == EntityType.GHAST ||
                   type == EntityType.WITHER_SKELETON || type == EntityType.HUSK ||
                   type == EntityType.STRAY || type == EntityType.PHANTOM ||
                   type == EntityType.DROWNED || type == EntityType.PILLAGER ||
                   type == EntityType.VINDICATOR || type == EntityType.EVOKER ||
                   type == EntityType.VEX || type == EntityType.RAVAGER ||
                   type == EntityType.HOGLIN || type == EntityType.ZOGLIN ||
                   type == EntityType.PIGLIN_BRUTE || type == EntityType.WARDEN;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al verificar mob hostil", e);
            return false;
         }
     }
     
     @SuppressWarnings("unused")
    private void awardSurvivalChallenge(Player player) {
         try {
             if (player == null) return;
             
             UUID playerId = player.getUniqueId();
             
             // Verificar si el jugador tiene más de 10 kills y no ha muerto
             int kills = playerKillCount.getOrDefault(playerId, 0);
             if (kills >= 10 && !deadPlayers.contains(playerId) && !hasChallengeCompleted(player, "survivor")) {
                 completeChallengeForPlayer(playerId, "survivor");
             }
             
             // Verificar desafío de +1 corazón máximo por más de 10 kills
             if (kills > 10 && !massKillers.contains(playerId)) {
                 massKillers.add(playerId);
                 
                 // Otorgar +1 corazón máximo (2.0 de salud)
                 AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
                 if (maxHealthAttr != null) {
                     double currentMaxHealth = maxHealthAttr.getValue();
                     double newMaxHealth = Math.min(currentMaxHealth + 2.0, 40.0); // Máximo 20 corazones
                     maxHealthAttr.setBaseValue(newMaxHealth);
                     
                     // Curar al jugador para que vea el efecto inmediatamente
                     player.setHealth(Math.min(player.getHealth() + 2.0, newMaxHealth));
                     
                     // Mensaje de recompensa
                     Bukkit.broadcast(MM.toComponent("<gold><b>" + player.getName() + "</b> ha obtenido <red>+1 corazón máximo</red> por eliminar a más de 10 jugadores!"));
                     player.sendMessage(MM.toComponent("<green><b>¡Felicidades!</b> Has obtenido <red>+1 corazón máximo</red> por tus habilidades de combate."));
                 }
             }
         } catch (Exception e) {
             plugin.getLogger().log(Level.WARNING, "Error al otorgar desafío de supervivencia", e);
         }
     }
     
     private void sendPlayerStatistics(Player player) {
         try {
             if (player == null) return;
             
             UUID playerId = player.getUniqueId();
             
             player.sendMessage(MM.toComponent("<gray><b>=== <gold>TUS ESTADÍSTICAS</gold> <gray><b>==="));
             player.sendMessage(MM.toComponent("<yellow>Jugadores eliminados: <white>" + playerKillCount.getOrDefault(playerId, 0)));
             player.sendMessage(MM.toComponent("<yellow>Kills consecutivos máximos: <white>" + consecutiveKills.getOrDefault(playerId, 0)));
             
             if (deadPlayers.contains(playerId)) {
                 player.sendMessage(MM.toComponent("<red>Estado: Eliminado"));
             } else {
                 player.sendMessage(MM.toComponent("<green>Estado: Superviviente"));
             }
             
             // Mostrar desafíos completados
             player.sendMessage(MM.toComponent("<gray><b>=== <gold>DESAFÍOS COMPLETADOS</gold> <gray><b>==="));
             
             if (hasChallengeCompleted(player, "player_killer")) {
                 player.sendMessage(MM.toComponent("<green>✓ Asesino de Jugadores"));
             }
             
             if (hasChallengeCompleted(player, "pentakill")) {
                 player.sendMessage(MM.toComponent("<green>✓ Pentakill"));
             }
             
             if (hasChallengeCompleted(player, "survivor")) {
                 player.sendMessage(MM.toComponent("<green>✓ Superviviente"));
             }
         } catch (Exception e) {
             plugin.getLogger().log(Level.WARNING, "Error al enviar estadísticas a " + player.getName(), e);
         }
     }

    // === IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS ===
    
    @Override
    protected void onEventStart() {
        try {
            plugin.getLogger().info("[BloodAndIronWeek] Iniciando evento específico...");
            
            // Configurar desafíos específicos del evento
            setupBloodAndIronChallenges();
            
            // Inicializar estadísticas específicas
            initializeEventStatistics();
            
            plugin.getLogger().info("[BloodAndIronWeek] Evento específico iniciado correctamente");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[BloodAndIronWeek] Error al iniciar evento específico", e);
        }
    }
    
    @Override
    protected void onEventStop() {
        try {
            plugin.getLogger().info("[BloodAndIronWeek] Deteniendo evento específico...");
            
            // Limpiar recursos específicos del evento
            cleanupEventResources();
            
            // Procesar estadísticas finales
            processFinalEventStatistics();
            
            plugin.getLogger().info("[BloodAndIronWeek] Evento específico detenido correctamente");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[BloodAndIronWeek] Error al detener evento específico", e);
        }
    }
    
    @Override
    protected void initializeEventSpecificData() {
        try {
            plugin.getLogger().info("[BloodAndIronWeek] Inicializando datos específicos del evento...");
            
            // Inicializar mapas de tracking
            lastHostileMobKillTime.clear();
            lastPlayerKillTime.clear();
            playerKillCount.clear();
            consecutiveKills.clear();
            pentakillPlayers.clear();
            survivedPlayers.clear();
            deadPlayers.clear();
            awardedAdrenaline.clear();
            mobKillWarningGiven.clear();
            survivors.clear();
            massKillers.clear();
            
            // Configurar datos específicos en el mapa de persistencia
            eventSpecificData.put("event_type", "blood_and_iron_week");
            eventSpecificData.put("start_time", System.currentTimeMillis());
            eventSpecificData.put("total_player_kills", 0L);
            eventSpecificData.put("total_mob_kills", 0L);
            eventSpecificData.put("pentakills_achieved", 0L);
            
            plugin.getLogger().info("[BloodAndIronWeek] Datos específicos inicializados correctamente");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[BloodAndIronWeek] Error al inicializar datos específicos", e);
        }
    }
    
    @Override
    protected void saveEventSpecificData() {
        try {
            plugin.getLogger().info("[BloodAndIronWeek] Guardando datos específicos del evento...");
            
            // Actualizar estadísticas en el mapa de persistencia
            eventSpecificData.put("last_save_time", System.currentTimeMillis());
            eventSpecificData.put("active_players", getActivePlayerCount());
            eventSpecificData.put("total_survivors", survivors.size());
            eventSpecificData.put("total_mass_killers", massKillers.size());
            
            // Guardar progreso de desafíos
            eventSpecificData.put("completed_challenges_count", getTotalChallengesCompleted());
            
            plugin.getLogger().info("[BloodAndIronWeek] Datos específicos guardados correctamente");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[BloodAndIronWeek] Error al guardar datos específicos", e);
        }
    }
    
    @Override
    protected void processEventStatistics() {
        try {
            // Actualizar estadísticas globales
            incrementGlobalStatistic("total_player_kills", playerKillCount.values().stream().mapToInt(Integer::intValue).sum());
            incrementGlobalStatistic("total_pentakills", pentakillPlayers.size());
            incrementGlobalStatistic("total_survivors", survivors.size());
            incrementGlobalStatistic("total_mass_killers", massKillers.size());
            
            // Procesar estadísticas de jugadores activos
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID playerId = player.getUniqueId();
                
                // Actualizar estadísticas del jugador
                updatePlayerStatistic(playerId, "kills", playerKillCount.getOrDefault(playerId, 0));
                updatePlayerStatistic(playerId, "consecutive_kills", consecutiveKills.getOrDefault(playerId, 0));
                updatePlayerStatistic(playerId, "is_survivor", survivors.contains(playerId) ? 1 : 0);
                updatePlayerStatistic(playerId, "is_mass_killer", massKillers.contains(playerId) ? 1 : 0);
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[BloodAndIronWeek] Error al procesar estadísticas del evento", e);
        }
    }
    
    // === MÉTODOS AUXILIARES PARA LOS MÉTODOS ABSTRACTOS ===
    
    private void setupBloodAndIronChallenges() {
        try {
            // Configurar desafíos específicos del evento
            initializeChallengeDefinitions();
            plugin.getLogger().info("[BloodAndIronWeek] Desafíos específicos configurados");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[BloodAndIronWeek] Error al configurar desafíos", e);
        }
    }
    
    /**
     * Inicializa las definiciones de desafíos específicos de la Semana de Sangre y Hierro
     */
    private void initializeChallengeDefinitions() {
        try {
            // Desafío 1: Matar a un jugador (Fácil)
            registerChallenge("player_killer", ChallengeDefinition.fromStringRewards(
                "player_killer",
                "Asesino de Jugadores",
                "Mata a un jugador durante el evento",
                1,
                Collections.singletonList("coins:10")
            ));
            
            // Desafío 2: Conseguir pentakill (5 kills consecutivos) (Intermedio)
            registerChallenge("pentakill", ChallengeDefinition.fromStringRewards(
                "pentakill",
                "Pentakill",
                "Consigue 5 kills consecutivos sin morir",
                5,
                Collections.singletonList("enchant:sharpness:3")
            ));
            
            // Desafío 3: Sobrevivir hasta el final del evento (Difícil)
            registerChallenge("survivor", ChallengeDefinition.fromStringRewards(
                "survivor",
                "Superviviente",
                "Sobrevive hasta el final del evento sin morir",
                1,
                Collections.singletonList("health:1")
            ));
            
            // Desafío 4: Conseguir 10+ kills para obtener +1 corazón permanente (Leyenda)
            registerChallenge("mass_killer", ChallengeDefinition.fromStringRewards(
                "mass_killer",
                "Asesino en Masa",
                "Consigue 10 o más kills durante el evento",
                10,
                Collections.singletonList("health:2")
            ));
            
            plugin.getLogger().info("[BloodAndIronWeek] Desafíos registrados correctamente en el sistema");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[BloodAndIronWeek] Error al inicializar definiciones de desafíos", e);
        }
    }            
}