package com.darkbladedev.events;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import com.darkbladedev.HeartlessMain;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.EventType;

/**
 * Dispatcher de eventos que coordina las acciones del sistema de eventos semanales
 * con el WeeklyEventManager y dispara los eventos personalizados de Bukkit
 */
public class WeeklyEventDispatcher {
    
    private final HeartlessMain plugin;
    private final WeeklyEventManager weeklyEventManager;
    private final PluginManager pluginManager;
    
    /**
     * Constructor del dispatcher de eventos
     * @param plugin Instancia del plugin principal
     * @param weeklyEventManager Gestor de eventos semanales
     */
    public WeeklyEventDispatcher(HeartlessMain plugin, WeeklyEventManager weeklyEventManager) {
        this.plugin = plugin;
        this.weeklyEventManager = weeklyEventManager;
        this.pluginManager = Bukkit.getPluginManager();
    }
    
    /**
     * Dispara un evento de inicio de evento semanal
     * @param eventType Tipo de evento que está iniciando
     * @param duration Duración del evento en milisegundos
     * @param startTime Tiempo de inicio del evento
     * @param world Mundo específico (null para global)
     * @return true si el evento no fue cancelado
     */
    public boolean fireEventStart(EventType eventType, long duration, long startTime, org.bukkit.World world) {
        WeeklyEventStartEvent event = new WeeklyEventStartEvent(eventType, duration, startTime, world);
        pluginManager.callEvent(event);
        
        if (event.isCancelled()) {
            plugin.getLogger().info("El inicio del evento " + eventType + " fue cancelado por un listener.");
            return false;
        }
        
        String location = world != null ? "mundo: " + world.getName() : "global";
        plugin.getLogger().info("Evento de inicio disparado para: " + eventType + " (" + location + ") con duración: " + duration + "ms");
        return true;
    }

    /**
     * Dispara un evento de inicio de evento semanal (Global)
     * @param eventType Tipo de evento que está iniciando
     * @param duration Duración del evento en milisegundos
     * @param startTime Tiempo de inicio del evento
     * @return true si el evento no fue cancelado
     */
    public boolean fireEventStart(EventType eventType, long duration, long startTime) {
        return fireEventStart(eventType, duration, startTime, null);
    }
    
    /**
     * Dispara un evento de detención de evento semanal
     * @param eventType Tipo de evento que se está deteniendo
     * @param endTime Tiempo de finalización del evento
     * @param wasForced true si el evento fue forzado a detenerse
     * @param totalDuration Duración total que el evento estuvo activo
     */
    public void fireEventStop(EventType eventType, long endTime, boolean wasForced, long totalDuration) {
        WeeklyEventStopEvent event = new WeeklyEventStopEvent(eventType, endTime, wasForced, totalDuration);
        pluginManager.callEvent(event);
        
        String stopType = wasForced ? "forzadamente" : "naturalmente";
        plugin.getLogger().info("Evento de detención disparado para: " + eventType + " (detenido " + stopType + ")");
    }
    
    /**
     * Dispara un evento de pausa de evento semanal
     * @param eventType Tipo de evento que se está pausando
     * @param pauseTime Tiempo en que se pausó el evento
     * @param timeRemaining Tiempo restante del evento al momento de pausar
     * @return true si el evento no fue cancelado
     */
    public boolean fireEventPause(EventType eventType, long pauseTime, long timeRemaining) {
        WeeklyEventPauseEvent event = new WeeklyEventPauseEvent(eventType, pauseTime, timeRemaining);
        pluginManager.callEvent(event);
        
        if (event.isCancelled()) {
            plugin.getLogger().info("La pausa del evento " + eventType + " fue cancelada por un listener.");
            return false;
        }
        
        plugin.getLogger().info("Evento de pausa disparado para: " + eventType + " con tiempo restante: " + timeRemaining + "ms");
        return true;
    }
    
    /**
     * Dispara un evento de reanudación de evento semanal
     * @param eventType Tipo de evento que se está reanudando
     * @param resumeTime Tiempo en que se reanudó el evento
     * @param pausedDuration Duración total que el evento estuvo pausado
     * @param timeRemaining Tiempo restante del evento después de reanudar
     * @return true si el evento no fue cancelado
     */
    public boolean fireEventResume(EventType eventType, long resumeTime, long pausedDuration, long timeRemaining) {
        WeeklyEventResumeEvent event = new WeeklyEventResumeEvent(eventType, resumeTime, pausedDuration, timeRemaining);
        pluginManager.callEvent(event);
        
        if (event.isCancelled()) {
            plugin.getLogger().info("La reanudación del evento " + eventType + " fue cancelada por un listener.");
            return false;
        }
        
        plugin.getLogger().info("Evento de reanudación disparado para: " + eventType + " después de " + pausedDuration + "ms pausado");
        return true;
    }
    
    /**
     * Inicia un evento semanal con validación y disparo de eventos
     * @param eventType Tipo de evento a iniciar
     * @param duration Duración del evento en milisegundos
     * @return true si el evento se inició correctamente
     */
    public boolean startEvent(EventType eventType, long duration) {
        if (weeklyEventManager.isEventActive()) {
            plugin.getLogger().warning("No se puede iniciar el evento " + eventType + " porque ya hay un evento activo.");
            return false;
        }
        
        long startTime = System.currentTimeMillis();
        
        // Disparar evento de inicio
        if (!fireEventStart(eventType, duration, startTime)) {
            return false; // El evento fue cancelado
        }
        
        // Iniciar el evento en el WeeklyEventManager
        boolean success = weeklyEventManager.startEventFromCommand(eventType, duration);
        
        if (!success) {
            plugin.getLogger().severe("Error al iniciar el evento " + eventType + " en el WeeklyEventManager.");
        }
        
        return success;
    }
    
    /**
     * Detiene el evento actual con validación y disparo de eventos
     * @param forced true si el evento debe ser forzado a detenerse
     * @return true si el evento se detuvo correctamente
     */
    public boolean stopCurrentEvent(boolean forced) {
        if (!weeklyEventManager.isEventActive()) {
            plugin.getLogger().warning("No hay ningún evento activo para detener.");
            return false;
        }
        
        EventType currentEventType = weeklyEventManager.getCurrentEventType();
        long endTime = System.currentTimeMillis();
        
        // Calcular duración total (considerando pausas)
        long totalDuration = endTime - (endTime - weeklyEventManager.getTimeRemaining());
        
        // Detener el evento en el WeeklyEventManager
        if (forced) {
            weeklyEventManager.forceStopCurrentEvent();
        } else {
            weeklyEventManager.stopCurrentEvent();
        }
        
        // Disparar evento de detención
        fireEventStop(currentEventType, endTime, forced, totalDuration);
        
        return true;
    }
    
    /**
     * Pausa el evento actual con validación y disparo de eventos
     * @return true si el evento se pausó correctamente
     */
    public boolean pauseCurrentEvent() {
        if (!weeklyEventManager.isEventActive()) {
            plugin.getLogger().warning("No hay ningún evento activo para pausar.");
            return false;
        }
        
        if (weeklyEventManager.isPaused()) {
            plugin.getLogger().warning("El evento ya está pausado.");
            return false;
        }
        
        EventType currentEventType = weeklyEventManager.getCurrentEventType();
        long pauseTime = System.currentTimeMillis();
        long timeRemaining = weeklyEventManager.getTimeRemaining();
        
        // Disparar evento de pausa
        if (!fireEventPause(currentEventType, pauseTime, timeRemaining)) {
            return false; // El evento fue cancelado
        }
        
        // Pausar el evento en el WeeklyEventManager
        weeklyEventManager.pauseCurrentEvent();
        
        return true;
    }
    
    /**
     * Reanuda el evento actual con validación y disparo de eventos
     * @return true si el evento se reanudó correctamente
     */
    public boolean resumeCurrentEvent() {
        if (!weeklyEventManager.isEventActive()) {
            plugin.getLogger().warning("No hay ningún evento activo para reanudar.");
            return false;
        }
        
        if (!weeklyEventManager.isPaused()) {
            plugin.getLogger().warning("El evento no está pausado.");
            return false;
        }
        
        EventType currentEventType = weeklyEventManager.getCurrentEventType();
        long resumeTime = System.currentTimeMillis();
        
        // Calcular duración pausada (esto requeriría acceso a pauseStartTime del WeeklyEventManager)
        // Por ahora usamos un valor aproximado
        long pausedDuration = 0; // Esto se podría mejorar con acceso a más datos del WeeklyEventManager
        long timeRemaining = weeklyEventManager.getTimeRemaining();
        
        // Disparar evento de reanudación
        if (!fireEventResume(currentEventType, resumeTime, pausedDuration, timeRemaining)) {
            return false; // El evento fue cancelado
        }
        
        // Reanudar el evento en el WeeklyEventManager
        weeklyEventManager.resumeCurrentEvent();
        
        return true;
    }
}