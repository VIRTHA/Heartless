package com.darkbladedev.mechanics;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.RewardPool;
import com.darkbladedev.utils.WeeklyEventData;

/**
 * Clase base para todos los eventos semanales del servidor.
 * Proporciona la estructura común y funcionalidades básicas que todos los eventos semanales comparten.
 */
public abstract class WeeklyEvent implements Listener {
    
    protected final HeartlessMain plugin;
    protected boolean isActive = false;
    protected boolean isPaused = false;
    protected String prefix;
    protected BukkitTask endTask;
    
    protected long duration;
    protected long startTime;
    protected long endTime;
    protected long millisDuration = duration / 50;
    protected long remaining = duration - startTime;;
    protected long elapsed;
    protected long pauseMoment;
    protected long resumeMoment;
    protected long totalPausedTime;
    
    /**
     * Constructor base para eventos semanales.
     * 
     * @param plugin El plugin principal
     * @param duration Duración del evento en ticks
     */
    public WeeklyEvent(HeartlessMain plugin, long duration) {
        this.plugin = plugin;
        this.duration = duration;
    }
    
    /**
     * Inicia el evento
     * Registra los listeners, inicia las tareas necesarias y programa el fin del evento.
     */
    public void start() {
        if (isActive) return;
        
        isActive = true;
        isPaused = false;
        
        // Registrar eventos
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Iniciar tareas específicas del evento
        startEventTasks();
        
        // Anunciar el inicio del evento
        announceEventStart();

        startTime = System.currentTimeMillis();
        
        // Programar el fin del evento
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                stop();
                endTime = System.currentTimeMillis();
            }
        }.runTaskLater(plugin, duration);
    }
    
    /**
     * Detiene el evento semanal.
     * Cancela todas las tareas, limpia los datos y desregistra los listeners.
     */
    public void stop() {
        if (!isActive) return;
        
        isActive = false;
        
        // Cancelar tarea de finalización si existe
        if (endTask != null) {
            endTask.cancel();
            endTask = null;
        }
        
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
        if (!isActive || isPaused) return;
        
        isPaused = true;
        pauseEventTasks();
        isActive = false;
        pauseMoment = System.currentTimeMillis();
    }
    
    /**
     * Reanuda el evento semanal.
     */
    public void resume() {
        if (!isActive || !isPaused) return;
        
        isPaused = false;
        resumeEventTasks();
        resumeMoment = System.currentTimeMillis();
        
        totalPausedTime = resumeMoment - pauseMoment;
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
     * Limpia los datos específicos del evento.
     * Debe ser implementado por cada evento concreto.
     */
    protected abstract void cleanupEventData();
    
    /**
     * @return true si el evento está activo, false en caso contrario
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * @return true si el evento está pausado, false en caso contrario
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * @return La duración del evento en ticks
     */
    public long getDuration() {
        return duration;
    }

    public long getRemainingDuration() {
        return remaining;
    }
    
    /**
     * Establece la duración del evento.
     * @param duration Nueva duración en ticks
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<String> getRewards() {
        if (RewardPool.hasRewards(plugin.getWeeklyEventManager().getCurrentEventType())) {
            return RewardPool.getRewardsForEvent(plugin.getWeeklyEventManager().getCurrentEventType());
        }
        return Collections.emptyList();
    }

    public WeeklyEventData toData() {
        WeeklyEventData data = new WeeklyEventData();
        data.pauseStartTime = this.pauseMoment;
        data.totalPausedTime = this.totalPausedTime;
        data.startTime = this.startTime;
        data.endTime = this.endTime;
        data.eventType = this.getName();
        data.isActive = this.isActive;
        data.isPaused = this.isPaused;
        return data;
    }

    public long getPauseStartTime() {
        return pauseMoment;
    }

    public long getTotalPausedTime() {
        return totalPausedTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}