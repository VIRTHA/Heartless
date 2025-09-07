package com.darkbladedev.mechanics;

import com.darkbladedev.HeartlessMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase base abstracta para todos los eventos semanales.
 * Proporciona patrones estandarizados para inicialización, manejo de tareas,
 * validación de datos y limpieza de recursos.
 * 
 * Esta clase implementa las mejores prácticas identificadas durante la auditoría
 * de eventos existentes, incluyendo:
 * - Manejo thread-safe de tareas y datos de jugadores
 * - Validación consistente de estado del evento
 * - Limpieza automática de recursos
 * - Manejo robusto de errores
 * 
 * @author DarkBladeDev
 * @since 1.0
 */
public abstract class AbstractWeeklyEvent {
    
    // Referencias thread-safe para el manejo de tareas
    protected final AtomicReference<BukkitTask> mainTask = new AtomicReference<>();
    protected final AtomicReference<BukkitTask> secondaryTask = new AtomicReference<>();
    protected final AtomicReference<BukkitTask> cleanupTask = new AtomicReference<>();
    
    // Estado del evento
    protected final AtomicBoolean isActive = new AtomicBoolean(false);
    protected final AtomicBoolean isPaused = new AtomicBoolean(false);
    
    // Colecciones thread-safe para datos de jugadores
    protected final Set<Player> activePlayers = ConcurrentHashMap.newKeySet();
    protected final ConcurrentHashMap<Player, Object> playerData = new ConcurrentHashMap<>();
    
    // Referencias del plugin
    protected final HeartlessMain plugin;
    protected final Logger logger;
    
    /**
     * Constructor base para eventos semanales.
     * 
     * @param plugin Instancia del plugin principal
     */
    protected AbstractWeeklyEvent(HeartlessMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Inicia el evento semanal.
     * Implementa validaciones de estado y manejo de errores estandarizado.
     */
    public final void startEvent() {
        // Validar que el evento no esté ya activo
        if (isActive.get()) {
            logger.warning(getName() + " ya está activo. Ignorando llamada a startEvent().");
            return;
        }
        
        try {
            // Marcar como activo antes de inicializar
            isActive.set(true);
            isPaused.set(false);
            
            // Reinicializar jugadores online
            reinitializeOnlinePlayers();
            
            // Inicializar tareas específicas del evento
            initializeEventTasks();
            
            logger.info(getName() + " iniciado correctamente.");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al iniciar " + getName() + ": " + e.getMessage(), e);
            // Limpiar estado en caso de error
            forceCleanup();
            throw new RuntimeException("Fallo al iniciar evento: " + getName(), e);
        }
    }
    
    /**
     * Detiene el evento semanal de manera segura.
     */
    public final void stopEvent() {
        if (!isActive.get()) {
            logger.warning(getName() + " no está activo. Ignorando llamada a stopEvent().");
            return;
        }
        
        try {
            // Detener todas las tareas
            stopEventTasks();
            
            // Limpiar datos del evento
            cleanupEventData();
            
            // Marcar como inactivo
            isActive.set(false);
            isPaused.set(false);
            
            logger.info(getName() + " detenido correctamente.");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al detener " + getName() + ": " + e.getMessage(), e);
            // Forzar limpieza en caso de error
            forceCleanup();
        }
    }
    
    /**
     * Pausa el evento temporalmente.
     */
    public final void pauseEvent() {
        if (!isActive.get()) {
            logger.warning("No se puede pausar " + getName() + " porque no está activo.");
            return;
        }
        
        isPaused.set(true);
        pauseEventTasks();
        logger.info(getName() + " pausado.");
    }
    
    /**
     * Reanuda el evento después de una pausa.
     */
    public final void resumeEvent() {
        if (!isActive.get()) {
            logger.warning("No se puede reanudar " + getName() + " porque no está activo.");
            return;
        }
        
        isPaused.set(false);
        resumeEventTasks();
        logger.info(getName() + " reanudado.");
    }
    
    /**
     * Reinicializa los datos de jugadores online.
     * Útil después de reinicios del servidor.
     */
    protected final void reinitializeOnlinePlayers() {
        activePlayers.clear();
        playerData.clear();
        
        // Agregar jugadores online actuales
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && player.isOnline()) {
                activePlayers.add(player);
                initializePlayerData(player);
            }
        }
        
        logger.info("Reinicializados " + activePlayers.size() + " jugadores para " + getName());
    }
    
    /**
     * Cancela una tarea de manera thread-safe.
     * 
     * @param taskRef Referencia atómica a la tarea
     */
    protected final void cancelTaskSafely(AtomicReference<BukkitTask> taskRef) {
        BukkitTask task = taskRef.get();
        if (task != null && !task.isCancelled()) {
            try {
                task.cancel();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error al cancelar tarea en " + getName() + ": " + e.getMessage(), e);
            } finally {
                taskRef.set(null);
            }
        }
    }
    
    /**
     * Detiene todas las tareas del evento.
     */
    protected final void stopEventTasks() {
        cancelTaskSafely(mainTask);
        cancelTaskSafely(secondaryTask);
        cancelTaskSafely(cleanupTask);
        
        // Permitir que las subclases detengan tareas adicionales
        stopAdditionalTasks();
    }
    
    /**
     * Limpia todos los datos del evento.
     */
    protected final void cleanupEventData() {
        activePlayers.clear();
        playerData.clear();
        
        // Permitir que las subclases limpien datos adicionales
        cleanupAdditionalData();
    }
    
    /**
     * Limpieza forzada en caso de errores críticos.
     */
    protected final void forceCleanup() {
        try {
            stopEventTasks();
            cleanupEventData();
            isActive.set(false);
            isPaused.set(false);
            logger.info("Limpieza forzada completada para " + getName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error durante limpieza forzada de " + getName(), e);
        }
    }
    
    /**
     * Verifica si el evento está activo y no pausado.
     * 
     * @return true si el evento puede ejecutar operaciones
     */
    protected final boolean canExecute() {
        return isActive.get() && !isPaused.get();
    }
    
    /**
     * Limpia jugadores desconectados de las colecciones.
     * Debe llamarse periódicamente para evitar memory leaks.
     */
    protected final void cleanupDisconnectedPlayers() {
        activePlayers.removeIf(player -> player == null || !player.isOnline());
        playerData.entrySet().removeIf(entry -> {
            Player player = entry.getKey();
            return player == null || !player.isOnline();
        });
    }
    
    // Métodos abstractos que deben implementar las subclases
    
    /**
     * Obtiene el nombre del evento.
     * 
     * @return Nombre del evento
     */
    public abstract String getName();
    
    /**
     * Inicializa las tareas específicas del evento.
     * Llamado durante startEvent().
     */
    protected abstract void initializeEventTasks();
    
    /**
     * Inicializa los datos específicos de un jugador.
     * 
     * @param player Jugador a inicializar
     */
    protected abstract void initializePlayerData(Player player);
    
    /**
     * Pausa las tareas específicas del evento.
     * Llamado durante pauseEvent().
     */
    protected abstract void pauseEventTasks();
    
    /**
     * Reanuda las tareas específicas del evento.
     * Llamado durante resumeEvent().
     */
    protected abstract void resumeEventTasks();
    
    /**
     * Detiene tareas adicionales específicas del evento.
     * Llamado durante stopEventTasks().
     */
    protected abstract void stopAdditionalTasks();
    
    /**
     * Limpia datos adicionales específicos del evento.
     * Llamado durante cleanupEventData().
     */
    protected abstract void cleanupAdditionalData();
    
    // Getters para el estado del evento
    
    public final boolean isActive() {
        return isActive.get();
    }
    
    public final boolean isPaused() {
        return isPaused.get();
    }
    
    public final int getActivePlayerCount() {
        return activePlayers.size();
    }
}