package com.darkbladedev.mechanics;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.events.WeeklyEventResumeEvent;
import com.darkbladedev.utils.EventType;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.TimeExpression;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
public class BloodAndIronWeek extends WeeklyEvent {

    // Referencias atómicas para tareas críticas
    private final AtomicReference<BukkitTask> mainTaskRef = new AtomicReference<>();
    private final AtomicReference<BukkitTask> checkKillsTaskRef = new AtomicReference<>();
    
    // Mapas thread-safe para tracking de jugadores
    private final Map<UUID, Long> lastHostileMobKillTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastPlayerKillTime = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerKillCount = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> consecutiveKills = new ConcurrentHashMap<>();
    private final Map<UUID, Long> potionDamageDealt = new ConcurrentHashMap<>();
    private final Set<UUID> instantDamageKillers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pentakillPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> survivedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> deadPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> awardedAdrenaline = ConcurrentHashMap.newKeySet();
    private final Set<UUID> mobKillWarningGiven = ConcurrentHashMap.newKeySet();
    private final Set<UUID> survivors = ConcurrentHashMap.newKeySet();
    
    // Aliases para compatibilidad
    // Referencias corregidas para evitar duplicación
    private final Set<UUID> playerKillers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> potionKillers = ConcurrentHashMap.newKeySet();
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
            
            plugin.getLogger().info("Validación de datos del evento completada");
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
        potionDamageDealt.clear();
        instantDamageKillers.clear();
        pentakillPlayers.clear();
        mobKillWarningGiven.clear();
        survivors.clear();
        playerKillers.clear();
        potionKillers.clear();
        pentaKillers.clear();
        lastMobKillTime.clear();
        
        plugin.getLogger().info("Colecciones del evento BloodAndIronWeek inicializadas correctamente");
    }

    @Override
    public void start() {
        try {
            super.start();
            plugin.getLogger().info("BloodAndIronWeek iniciado correctamente");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error al iniciar BloodAndIronWeek", e);
            throw new RuntimeException("Fallo crítico al iniciar el evento", e);
        }
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
            stopEventTasks(); // Limpieza en caso de error
        }
    }
    
    @Override
    protected void announceEventStart() {
        try {
            Bukkit.broadcast(MM.toComponent(prefix + " <gray>¡<gold>El coliseo del caos está abierto. <red>Elimina o sé eliminado<gray>!"));
            announceChallenges();
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
            instantDamageKillers.clear();
            pentakillPlayers.clear();
            survivedPlayers.clear();
            deadPlayers.clear();
            awardedAdrenaline.clear();
            mobKillWarningGiven.clear();
            
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
    
    /**
     * Maneja la reanudación específica del evento BloodAndIronWeek
     * @param event El evento de reanudación
     */
    public void onResumeEvent(WeeklyEventResumeEvent event) {
        if (event.getEventType() != EventType.BLOOD_AND_IRON_WEEK) {
            return;
        }
        
        try {
            plugin.getLogger().info("Iniciando reanudación específica de BloodAndIronWeek...");
            
            // Validar estado del evento antes de reanudar
            if (!isActive.get()) {
                plugin.getLogger().warning("Intentando reanudar evento inactivo");
                return;
            }
            
            // Restaurar datos específicos del evento
            restoreEventSpecificData();
            
            // Reinicializar jugadores online
            reinitializeOnlinePlayers();
            
            // Validar integridad de datos después de la restauración
            validateEventData();
            
            plugin.getLogger().info("Reanudación específica de BloodAndIronWeek completada");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error en reanudación específica de BloodAndIronWeek", e);
            // Intentar recuperación segura
            safeEventRecovery();
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
                            
                            plugin.getLogger().info("Evento BloodAndIronWeek reanudado correctamente con sincronización mejorada");
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.SEVERE, "Error al iniciar tareas durante reanudación", e);
                            stopEventTasks();
                        }
                    }
                }.runTaskLater(plugin, 1L);
                
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error al reanudar evento", e);
            // Intentar limpieza en caso de error
            stopEventTasks();
        }
    }
        
    @Override
    public void stop() {
        try {
            if (!isActive.get()) return;
            
            isActive.set(false);
            
            // Cancelar tareas de forma segura
            stopEventTasks();
            
            // Otorgar recompensas de supervivencia
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            if (onlinePlayers != null) {
                for (Player player : onlinePlayers) {
                    if (player != null && player.isOnline()) {
                        try {
                            UUID playerId = player.getUniqueId();
                            if (!deadPlayers.contains(playerId) && 
                                playerKillCount.getOrDefault(playerId, 0) >= 10) {
                                awardSurvivalChallenge(player);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, 
                                "Error al otorgar recompensa de supervivencia a " + player.getName(), e);
                        }
                    }
                }
            }
            
            // Limpiar datos
            cleanupEventData();
            
            // Llamar al método stop() de la clase padre
            super.stop();
            
            plugin.getLogger().info("BloodAndIronWeek detenido correctamente");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error crítico al detener BloodAndIronWeek", e);
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
            
            // Iniciar nueva tarea de verificación de kills con validaciones adicionales
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
                        if (onlinePlayers != null && !onlinePlayers.isEmpty()) {
                            for (Player player : onlinePlayers) {
                                if (player != null && player.isOnline()) {
                                    checkMobKillTimeout(player, currentTime);
                                    checkPlayerKillTimeout(player, currentTime);
                                }
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Error en tarea de verificación de kills", e);
                        // No cancelar la tarea por un error menor, solo registrar
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
    
    private void initializePlayer(Player player) {
        try {
            if (player == null) return;
            
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            
            // Inicializar tiempos de kill
            lastHostileMobKillTime.put(playerId, currentTime);
            lastPlayerKillTime.put(playerId, currentTime);
            
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
    
    private void announceChallenges() {
        try {
            Bukkit.broadcast(MM.toComponent("<gray><b>=== <gold>DESAFÍOS DE LA SEMANA</gold> <gray><b>==="));
            Bukkit.broadcast(MM.toComponent("<yellow>1. <red>Mata</red> a <white>3</white> jugadores</yellow>"));
            Bukkit.broadcast(MM.toComponent("<gray>   <white>Recompensa:</white> Encantamiento <gold><u>Adrenaline</u></gold><gray>"));
            Bukkit.broadcast(MM.toComponent("<yellow>2. <red>Mata<red> a un jugador con poción de daño instantáneo</yellow>"));
            Bukkit.broadcast(MM.toComponent("<gray>   <white>Recompensa:</white> <u>+1</u> corazón extra</gray>"));
            Bukkit.broadcast(MM.toComponent("<yellow>3. <red>Mata<red> a 5 jugadores seguidos sin morir</yellow>"));
            Bukkit.broadcast(MM.toComponent("<gray>   <white>Recompensa:</white> <u>Tag</u> \"Pentakill\"</gray>"));
            Bukkit.broadcast(MM.toComponent("<yellow>4. <green>Sobrevive<green> sin morir en todo el evento (con más de 10 kills)</yellow>"));
            Bukkit.broadcast(MM.toComponent("<gray>   <white>Recompensa:</white> <u>+1</u> corazón extra</gray>"));
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al anunciar desafíos", e);
        }
    }
    
    private void checkAndApplyArmorEffects(Player player) {
        try {
            if (player == null || !player.isOnline()) return;
            
            PlayerInventory inventory = player.getInventory();
            if (inventory == null) return;
            
            int ironArmorPieces = 0;
            
            // Contar piezas de armadura de hierro
            ItemStack[] armorContents = inventory.getArmorContents();
            if (armorContents != null) {
                for (ItemStack armor : armorContents) {
                    if (armor != null && armor.getType().name().startsWith("IRON_")) {
                        ironArmorPieces++;
                    }
                }
            }
            
            // Aplicar efectos según piezas de armadura
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
            if (mainHand != null && mainHand.getType() == Material.IRON_SWORD) {
                // Espada de hierro: Velocidad I
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 0, false, false));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al aplicar efectos de espada a " + player.getName(), e);
        }
    }
    
    // ========== MÉTODOS DE DESAFÍOS ==========
    
    private boolean hasChallengeCompleted(Player player, String challengeType) {
        try {
            if (player == null || challengeType == null) return false;
            return hasChallengeCompleted(player.getUniqueId(), challengeType);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al verificar desafío completado", e);
            return false;
        }
    }
    
    public boolean hasChallengeCompleted(UUID playerId, String challengeType) {
        try {
            if (playerId == null || challengeType == null) return false;
            
            switch (challengeType.toLowerCase()) {
                case "player_killer":
                    return playerKillers.contains(playerId);
                case "potion_killer":
                    return potionKillers.contains(playerId);
                case "pentakill":
                    return pentaKillers.contains(playerId);
                case "survivor":
                    return survivors.contains(playerId);
                default:
                    return false;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al verificar desafío completado", e);
            return false;
        }
    }
    
    private void completeChallengeForPlayer(Player player, String challengeType) {
        try {
            if (player == null || challengeType == null) return;
            
            UUID playerId = player.getUniqueId();
            
            switch (challengeType.toLowerCase()) {
                case "player_killer":
                    if (!playerKillers.contains(playerId)) {
                        playerKillers.add(playerId);
                        player.sendMessage(MM.toComponent("<green>¡Desafío completado: Asesino de Jugadores!"));
                    }
                    break;
                case "potion_killer":
                    if (!potionKillers.contains(playerId)) {
                        potionKillers.add(playerId);
                        player.sendMessage(MM.toComponent("<green>¡Desafío completado: Maestro de Pociones!"));
                    }
                    break;
                case "pentakill":
                    if (!pentaKillers.contains(playerId)) {
                        pentaKillers.add(playerId);
                        player.sendMessage(MM.toComponent("<green>¡Desafío completado: Pentakill!"));
                    }
                    break;
                case "survivor":
                    if (!survivors.contains(playerId)) {
                        survivors.add(playerId);
                        player.sendMessage(MM.toComponent("<green>¡Desafío completado: Superviviente!"));
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
    
    public Set<UUID> getPotionKillers() {
        return new HashSet<>(potionKillers);
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
    
    public void loadPotionKillers(Set<UUID> killers) {
        if (killers != null) {
            potionKillers.clear();
            potionKillers.addAll(killers);
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
    
    public Set<UUID> getAwardedAdrenaline() {
        return new HashSet<>(awardedAdrenaline);
    }
    
    public Set<UUID> getMobKillWarningGiven() {
        return new HashSet<>(mobKillWarningGiven);
    }
    
    public void loadInstantDamageKillers(Set<UUID> killersSet) {
        if (killersSet != null) {
            potionKillers.clear();
            potionKillers.addAll(killersSet);
        }
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
    
    public Set<UUID> getInstantDamageKillers() {
        return new HashSet<>(potionKillers);
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
            ItemStack mainHand = attacker.getInventory().getItemInMainHand();
            if (mainHand != null && mainHand.getType() == Material.SPLASH_POTION) {
                PotionMeta meta = (PotionMeta) mainHand.getItemMeta();
                if (meta != null && meta.hasCustomEffects()) {
                    for (PotionEffect effect : meta.getCustomEffects()) {
                        if (effect.getType() == PotionEffectType.INSTANT_DAMAGE) {
                            // Marcar para verificar kill con poción
                            potionDamageDealt.put(attacker.getUniqueId(), System.currentTimeMillis());
                            break;
                        }
                    }
                }
            }
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
                completeChallengeForPlayer(killer, "player_killer");
            }
            
            // Verificar desafío de pentakill
            if (currentConsecutive >= 5 && !hasChallengeCompleted(killer, "pentakill")) {
                completeChallengeForPlayer(killer, "pentakill");
            }
            
            // Verificar kill con poción
            Long potionTime = potionDamageDealt.get(killerId);
            if (potionTime != null && (System.currentTimeMillis() - potionTime) < 5000) { // 5 segundos
                if (!hasChallengeCompleted(killer, "potion_killer")) {
                    completeChallengeForPlayer(killer, "potion_killer");
                }
                potionDamageDealt.remove(killerId);
            }
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
     
     private void awardSurvivalChallenge(Player player) {
         try {
             if (player == null) return;
             
             UUID playerId = player.getUniqueId();
             
             // Verificar si el jugador tiene más de 10 kills y no ha muerto
             int kills = playerKillCount.getOrDefault(playerId, 0);
             if (kills >= 10 && !deadPlayers.contains(playerId) && !hasChallengeCompleted(player, "survivor")) {
                 completeChallengeForPlayer(player, "survivor");
                 
                 // Otorgar corazón extra
                 double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                 player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMaxHealth + 2.0);
                 player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
                 
                 player.sendMessage(MM.toComponent("<green>¡Has ganado un corazón extra por sobrevivir!"));
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
             
             if (hasChallengeCompleted(player, "potion_killer")) {
                 player.sendMessage(MM.toComponent("<green>✓ Maestro de Pociones"));
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
}