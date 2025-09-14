package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.RewardPool;
import com.darkbladedev.utils.TimeExpression;
import com.darkbladedev.utils.WeeklyEventData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase base para todos los eventos semanales del servidor.
 * Proporciona la estructura común y funcionalidades básicas que todos los eventos semanales comparten.
 */
public abstract class WeeklyEvent implements Listener {
    
    // === CONSTANTES ===
    private static final long AUTO_SAVE_INTERVAL = 5 * 60 * 1000L; // 5 minutos
    private static final long CLEANUP_INTERVAL = 10 * 60 * 1000L; // 10 minutos
    
    protected final HeartlessMain plugin;
    protected final Logger logger;
    protected String prefix;
    protected BukkitTask endTask;
    
    // Variables thread-safe
    protected final AtomicBoolean isActive = new AtomicBoolean(false);
    protected final AtomicBoolean isPaused = new AtomicBoolean(false);
    protected final AtomicBoolean isInitialized = new AtomicBoolean(false);
    protected final AtomicBoolean isCleaningUp = new AtomicBoolean(false);
    
    private final AtomicLong startTime = new AtomicLong(0);
    private final AtomicLong endTime = new AtomicLong(0);
    private final AtomicLong pauseStartTime = new AtomicLong(0);
    private final AtomicLong totalPausedTime = new AtomicLong(0);
    private final AtomicLong lastSaveTime = new AtomicLong(0);
    private final AtomicLong saveCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    protected final TimeExpression duration;
    
    protected final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    protected final Map<UUID, Object> playerData = new ConcurrentHashMap<>();
    protected final Map<UUID, Long> playerJoinTimes = new ConcurrentHashMap<>();
    private final List<BukkitTask> eventTasks = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, BukkitTask> customTasks = new ConcurrentHashMap<>();
    
    // === TAREAS DEL SISTEMA ===
    private BukkitTask autoSaveTask;
    private BukkitTask cleanupTask;
    
    /**
     * Constructor base para eventos semanales.
     * 
     * @param plugin El plugin principal
     * @param duration Duración del evento en segundos
     */
    public WeeklyEvent(HeartlessMain plugin, long duration) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.duration = new TimeExpression(duration + "s");
        this.prefix = "<gradient:#ff6b6b:#4ecdc4><bold>" + getName() + "</bold></gradient>";
    }
    
    /**
     * Constructor base para eventos semanales con TimeExpression.
     * 
     * @param plugin El plugin principal
     * @param timeExpression Expresión de tiempo que define la duración del evento
     */
    public WeeklyEvent(HeartlessMain plugin, TimeExpression timeExpression) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.duration = timeExpression;
        this.prefix = "<gradient:#ff6b6b:#4ecdc4><bold>" + getName() + "</bold></gradient>";
    }
    
    /**
     * Inicia el evento
     * Registra los listeners, inicia las tareas necesarias y programa el fin del evento.
     */
    public void start() {
        if (isActive.get()) return;
        
        isActive.set(true);
        isPaused.set(false);
        
        // Asegurar que los eventos estén registrados
        ensureEventHandlersRegistered();
        
        // Inicializar tareas del sistema
        initializeSystemTasks();
        
        // Iniciar tareas específicas del evento
        startEventTasks();
        
        // Anunciar el inicio del evento
        announceEventStart();

        startTime.set(System.currentTimeMillis());
        
        // Calcular tiempo de finalización
        long durationTicks = duration.toTicks();
        endTime.set(System.currentTimeMillis() + (durationTicks * 50)); // Convertir ticks a milisegundos
        
        // Programar el fin del evento
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        }.runTaskLater(plugin, durationTicks);
    }
    
    /**
     * Detiene el evento semanal.
     * Cancela todas las tareas, limpia los datos y desregistra los listeners.
     */
    public void stop() {
        if (!isActive.get()) return;
        
        isActive.set(false);
        
        // Cancelar tarea de finalización si existe
        if (endTask != null) {
            endTask.cancel();
            endTask = null;
        }
        
        // Detener tareas del sistema
        stopSystemTasks();
        
        // Detener tareas específicas del evento
        stopEventTasks();
        
        // Desregistrar listeners
        HandlerList.unregisterAll(this);
        
        // Anunciar el fin del evento
        announceEventEnd();
        
        // Limpiar datos
        cleanupEventData();
    }
    
    /**
     * Pausa el evento semanal.
     */
    public void pause() {
        if (!isActive.get() || isPaused.get()) return;
        
        isPaused.set(true);
        pauseEventTasks();
        isActive.set(false);
        pauseStartTime.set(System.currentTimeMillis());
    }
    
    /**
     * Reanuda el evento semanal.
     */
    public void resume() {
        // Activar el evento si no está activo (para carga desde persistencia)
        if (!isActive.get()) {
            isActive.set(true);
        }
        
        isPaused.set(false);
        
        // Asegurar que los eventos estén registrados
        ensureEventHandlersRegistered();
        
        resumeEventTasks();
        long resumeMoment = System.currentTimeMillis();
        
        totalPausedTime.addAndGet(resumeMoment - pauseStartTime.get());
    }
    
    /**
     * Asegura que los EventHandlers estén registrados sin duplicados
     */
    private void ensureEventHandlersRegistered() {
        try {
            // Primero desregistrar para evitar duplicados
            HandlerList.unregisterAll(this);
            // Luego registrar nuevamente
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("EventHandlers registrados para: " + this.getClass().getSimpleName());
        } catch (Exception e) {
            plugin.getLogger().severe("Error al registrar EventHandlers para " + this.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
    
    /**
     * @return El nombre del evento
     */
    public abstract String getName();
    
    /**
     * Inicia las tareas específicas del evento.
     * Debe ser implementado por cada evento concreto.
     */
    protected abstract void startEventTasks();
    
    /**
     * Detiene las tareas específicas del evento.
     * Debe ser implementado por cada evento concreto.
     */
    protected abstract void stopEventTasks();
    
    /**
     * Pausa las tareas específicas del evento.
     * Puede ser sobrescrito por eventos concretos si es necesario.
     */
    protected void pauseEventTasks() {
        // Implementación por defecto vacía
    }
    
    /**
     * Reanuda las tareas específicas del evento.
     * Puede ser sobrescrito por eventos concretos si es necesario.
     */
    protected void resumeEventTasks() {
        // Implementación por defecto vacía
    }
    
    /**
     * Anuncia el inicio del evento a todos los jugadores.
     * Puede ser sobrescrito por eventos concretos para mensajes personalizados.
     */
    protected void announceEventStart() {
        Bukkit.broadcast(MM.toComponent(prefix + " <green>¡El evento ha comenzado!"));
    }
    
    /**
     * Anuncia el fin del evento a todos los jugadores.
     * Puede ser sobrescrito por eventos concretos para mensajes personalizados.
     */
    protected void announceEventEnd() {
        Bukkit.broadcast(MM.toComponent(prefix + " <yellow>¡El evento ha terminado!"));
    }
    

    
    /**
     * @return true si el evento está activo, false en caso contrario
     */
    public boolean isActive() {
        return isActive.get();
    }
    
    /**
     * @return true si el evento está pausado, false en caso contrario
     */
    public boolean isPaused() {
        return isPaused.get();
    }
    


    public long getRemainingDuration() {
        if (!isActive.get()) return 0;
        long elapsed = System.currentTimeMillis() - startTime.get();
        long totalDuration = duration.toMilliseconds();
        return Math.max(0, totalDuration - elapsed);
    }
    
    /**
     * Establece la duración del evento.
     * @param duration Nueva duración en ticks
     */
    public void setDuration(long duration) {
        // No se puede modificar duration ya que es final
        // Este método se mantiene por compatibilidad
    }

    public List<String> getRewards() {
        if (RewardPool.hasRewards(plugin.getWeeklyEventManager().getCurrentEventType())) {
            return RewardPool.getRewardsForEvent(plugin.getWeeklyEventManager().getCurrentEventType());
        }
        return Collections.emptyList();
    }

    public WeeklyEventData toData() {
        WeeklyEventData data = new WeeklyEventData();
        data.pauseStartTime = this.pauseStartTime.get();
        data.totalPausedTime = this.totalPausedTime.get();
        data.startTime = this.startTime.get();
        data.endTime = this.endTime.get();
        data.eventType = this.getName();
        data.isActive = this.isActive.get();
        data.isPaused = this.isPaused.get();
        return data;
    }

    public long getPauseStartTime() {
        return pauseStartTime.get();
    }

    public long getTotalPausedTime() {
        return totalPausedTime.get();
    }

    public long getStartTime() {
        return startTime.get();
    }

    public long getEndTime() {
        return endTime.get();
    }
    
    /**
     * Establece el tiempo de inicio del evento (usado para restauración tras reinicio)
     * @param startTime Tiempo de inicio en milisegundos
     */
    public void setStartTime(long startTime) {
        this.startTime.set(startTime);
    }
    
    /**
     * Establece el tiempo de fin del evento (usado para restauración tras reinicio)
     * @param endTime Tiempo de fin en milisegundos
     */
    public void setEndTime(long endTime) {
        this.endTime.set(endTime);
    }
    
    /**
     * Establece el tiempo total pausado (usado para restauración tras reinicio)
     * @param totalPausedTime Tiempo total pausado en milisegundos
     */
    public void setTotalPausedTime(long totalPausedTime) {
        this.totalPausedTime.set(totalPausedTime);
    }
    
    /**
     * Establece el momento de inicio de pausa (usado para restauración tras reinicio)
     * @param pauseStartTime Momento de inicio de pausa en milisegundos
     */
    public void setPauseStartTime(long pauseStartTime) {
        this.pauseStartTime.set(pauseStartTime);
    }
    
    // === MÉTODOS THREAD-SAFE Y DE PERSISTENCIA ===
    
    /**
     * Inicializa las tareas del sistema (auto-guardado y limpieza)
     */
    private void initializeSystemTasks() {
        // Auto-guardado cada 5 minutos
        autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                saveEventData();
                lastSaveTime.set(System.currentTimeMillis());
                saveCount.incrementAndGet();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error en auto-guardado del evento " + getName(), e);
                errorCount.incrementAndGet();
            }
        }, 20L * 60 * 5, AUTO_SAVE_INTERVAL); // 5 minutos
        
        // Limpieza cada 10 minutos
        cleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                performPeriodicCleanup();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error en limpieza periódica del evento " + getName(), e);
                errorCount.incrementAndGet();
            }
        }, 20L * 60 * 10, CLEANUP_INTERVAL); // 10 minutos
    }
    
    /**
     * Detiene las tareas del sistema
     */
    private void stopSystemTasks() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        
        // Cancelar todas las tareas personalizadas
        customTasks.values().forEach(BukkitTask::cancel);
        customTasks.clear();
        
        // Cancelar tareas del evento
        synchronized (eventTasks) {
            eventTasks.forEach(BukkitTask::cancel);
            eventTasks.clear();
        }
    }
    
    /**
     * Guarda los datos del evento de forma thread-safe
     */
    protected void saveEventData() {
        // Implementación por defecto - puede ser sobrescrita
        // El WeeklyEventManager maneja el guardado internamente
    }
    
    /**
     * Realiza limpieza periódica de datos
     */
    protected void performPeriodicCleanup() {
        // Limpiar jugadores desconectados hace más de 1 hora
        long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
        
        activePlayers.removeIf(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                Long joinTime = playerJoinTimes.get(uuid);
                if (joinTime != null && joinTime < oneHourAgo) {
                    playerData.remove(uuid);
                    playerJoinTimes.remove(uuid);
                    return true;
                }
            }
            return false;
        });
    }
    
    /**
     * Añade una tarea personalizada con nombre
     */
    protected void addCustomTask(String name, BukkitTask task) {
        BukkitTask oldTask = customTasks.put(name, task);
        if (oldTask != null) {
            oldTask.cancel();
        }
    }
    
    /**
     * Cancela una tarea personalizada por nombre
     */
    protected void cancelCustomTask(String name) {
        BukkitTask task = customTasks.remove(name);
        if (task != null) {
            task.cancel();
        }
    }
    
    /**
     * Maneja la entrada de jugadores al evento
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isActive.get()) return;
        
        UUID playerId = event.getPlayer().getUniqueId();
        activePlayers.add(playerId);
        playerJoinTimes.put(playerId, System.currentTimeMillis());
        
        // Llamar al método específico del evento
        onPlayerJoinEvent(event.getPlayer());
    }
    
    /**
     * Maneja la salida de jugadores del evento
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!isActive.get()) return;
        
        // Llamar al método específico del evento antes de limpiar
        onPlayerQuitEvent(event.getPlayer());
        
        // No remover inmediatamente - se hará en la limpieza periódica
    }
    
    /**
     * Método llamado cuando un jugador se une durante el evento
     * Puede ser sobrescrito por eventos específicos
     */
    protected void onPlayerJoinEvent(Player player) {
        // Implementación por defecto vacía
    }
    
    /**
     * Método llamado cuando un jugador sale durante el evento
     * Puede ser sobrescrito por eventos específicos
     */
    protected void onPlayerQuitEvent(Player player) {
        // Implementación por defecto vacía
    }
    
    /**
      * Obtiene estadísticas del evento
      */
     public Map<String, Object> getEventStats() {
         Map<String, Object> stats = new HashMap<>();
         stats.put("active", isActive.get());
         stats.put("paused", isPaused.get());
         stats.put("startTime", startTime.get());
         stats.put("endTime", endTime.get());
         stats.put("totalPausedTime", totalPausedTime.get());
         stats.put("activePlayers", activePlayers.size());
         stats.put("saveCount", saveCount.get());
         stats.put("errorCount", errorCount.get());
         stats.put("lastSaveTime", lastSaveTime.get());
         return stats;
     }
     
     // === MÉTODOS DE UTILIDAD ===
     
     /**
      * Verifica si el evento puede ejecutar operaciones.
      */
     protected final boolean canExecute() {
         return isActive.get() && !isPaused.get() && !isCleaningUp.get();
     }
     
     /**
      * Verifica si el evento ha expirado.
      */
     public final boolean hasExpired() {
         if (!isActive.get()) return false;
         return System.currentTimeMillis() >= endTime.get();
     }
     
     /**
      * Obtiene el tiempo restante del evento en milisegundos.
      */
     public final long getRemainingTime() {
         if (!isActive.get()) return 0;
         long remaining = endTime.get() - System.currentTimeMillis();
         return Math.max(0, remaining);
     }
     
     /**
      * Fuerza la detención del evento con limpieza completa.
      */
     public final boolean forceStop() {
         if (isCleaningUp.getAndSet(true)) {
             return false; // Ya se está limpiando
         }
         
         try {
             logger.info("Forzando detención del evento: " + getName());
             
             // Detener todas las tareas inmediatamente
             stopSystemTasks();
             
             // Cambiar estados
             isActive.set(false);
             isPaused.set(false);
             
             // Limpiar datos
             cleanupEventData();
             
             // Desregistrar eventos
             HandlerList.unregisterAll(this);
             
             // Hook de fuerza de detención
             onEventForceStop();
             
             logger.info("Evento " + getName() + " detenido forzadamente");
             return true;
             
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Error al forzar detención del evento " + getName(), e);
             errorCount.incrementAndGet();
             return false;
         } finally {
             isCleaningUp.set(false);
         }
     }
     
     /**
     * Limpia todos los datos del evento.
     */
    protected void cleanupEventData() {
        activePlayers.clear();
        playerData.clear();
        playerJoinTimes.clear();
        
        // Permitir limpieza personalizada
        cleanupAdditionalData();
    }
     
     /**
      * Inicializa datos para jugadores online al iniciar el evento.
      */
     protected void initializeOnlinePlayers() {
         for (Player player : Bukkit.getOnlinePlayers()) {
             UUID playerId = player.getUniqueId();
             activePlayers.add(playerId);
             playerJoinTimes.put(playerId, System.currentTimeMillis());
             onPlayerJoinEvent(player);
         }
     }
     
     /**
      * Remueve los datos de un jugador específico.
      */
     protected void removePlayerData(UUID playerId) {
         activePlayers.remove(playerId);
         playerData.remove(playerId);
         playerJoinTimes.remove(playerId);
         onPlayerDataRemoved(playerId);
     }
     
     // === HOOKS DEL CICLO DE VIDA (pueden ser sobrescritos) ===
     
     /**
      * Hook llamado cuando el evento inicia.
      */
     protected void onEventStart() {}
     
     /**
      * Hook llamado cuando el evento se pausa.
      */
     protected void onEventPause() {}
     
     /**
      * Hook llamado cuando el evento se reanuda.
      */
     protected void onEventResume() {}
     
     /**
      * Hook llamado cuando el evento se detiene normalmente.
      */
     protected void onEventStop() {}
     
     /**
      * Hook llamado cuando el evento se detiene forzadamente.
      */
     protected void onEventForceStop() {}
     
     /**
      * Hook llamado cuando se remueven los datos de un jugador.
      */
     protected void onPlayerDataRemoved(UUID playerId) {}
     
     /**
      * Limpia datos adicionales específicos del evento.
      * Debe ser implementado por eventos específicos si necesitan limpieza adicional.
      */
     protected void cleanupAdditionalData() {}
     
     // === GETTERS ADICIONALES ===
     
     public final boolean isInitialized() {
         return isInitialized.get();
     }
     
     public final int getActivePlayerCount() {
         return activePlayers.size();
     }
     
     public final Set<UUID> getActivePlayers() {
         return new HashSet<>(activePlayers);
     }
     
     public final long getLastSaveTime() {
         return lastSaveTime.get();
     }
     
     public final long getSaveCount() {
         return saveCount.get();
     }
     
     public final long getErrorCount() {
         return errorCount.get();
     }
     
     
     
     public final Map<UUID, Long> getPlayerJoinTimes() {
         return new HashMap<>(playerJoinTimes);
     }
     
     public final HeartlessMain getPlugin() {
          return plugin;
      }
      
      public final TimeExpression getDuration() {
          return duration;
      }
  }