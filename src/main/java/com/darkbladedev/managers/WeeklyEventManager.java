package com.darkbladedev.managers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.darkbladedev.utils.EventType;
import com.darkbladedev.utils.MM;
import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.AcidWeek;
import com.darkbladedev.mechanics.BloodAndIronWeek;
import com.darkbladedev.mechanics.ExplosiveWeek;
import com.darkbladedev.mechanics.ToxicFog;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.utils.TimeConverter;

public class WeeklyEventManager {
    private static final long WEEK_IN_MILLIS = TimeUnit.DAYS.toMillis(7);
    private static final String DATA_FILE = "weekly_event_data.json";
    
    private final HeartlessMain plugin;
    private final Random random = new Random();
    private final File dataFile;
    
    private BukkitTask weeklyTask;
    private long eventStartTime;
    private long eventEndTime;
    private EventType currentEventType;
    private WeeklyEvent currentEvent;
    private boolean isEventActive = false;
    private boolean isPaused = false;
    private long pauseStartTime = 0;
    private long totalPausedTime = 0;
    
    // Add a lock to prevent multiple events from starting simultaneously
    private boolean isEventStarting = false;
    
    public WeeklyEventManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), DATA_FILE);
        
        // Crear directorio si no existe
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }
    
    public void initialize() {
        // Check if an event is already starting
        if (isEventStarting) {
            Bukkit.getConsoleSender().sendMessage(
                MM.toComponent("<red>Ya hay un evento inicializándose. Operación cancelada.")
            );
            return;
        }
        
        // Set the lock
        isEventStarting = true;
        
        try {
            // Cargar datos guardados o iniciar un nuevo evento
            if (loadSavedEventData()) {
                Bukkit.getConsoleSender().sendMessage(
                    MM.toComponent("&6Reanudando evento semanal: &e" + currentEventType.getEventName())
                );
                
                // Calcular tiempo restante
                long currentTime = System.currentTimeMillis();
                long remainingTime = eventEndTime - currentTime;
                
                if (remainingTime > 0) {
                    // Reanudar el evento actual
                    startEvent(currentEventType, remainingTime);
                    
                    // Programar el siguiente evento cuando termine este
                    scheduleNextEvent(remainingTime);
                } else {
                    // Si el evento ya debería haber terminado, iniciar uno nuevo
                    startRandomEvent();
                }
            } else {
                // No hay datos guardados, iniciar un nuevo evento aleatorio
                startRandomEvent();
            }
        } finally {
            // Release the lock
            isEventStarting = false;
        }
    }
    
    private void startRandomEvent() {
        // Check if an event is already active or starting
        if (isEventActive || isEventStarting) {
            Bukkit.getConsoleSender().sendMessage(
                MM.toComponent("&cYa hay un evento activo o inicializándose. No se puede iniciar otro evento.")
            );
            return;
        }
        
        // Set the lock
        isEventStarting = true;
        
        try {
            // Obtener lista de eventos disponibles
            List<EventType> availableEvents = new ArrayList<>();
            for (EventType type : EventType.values()) {
                // Filtrar eventos que no son adecuados para ser semanales
                if (!type.getEventName().equals("mob_rain") || !type.getEventName().equals("size_randomizer") || !type.getEventName().equals("paranoia_effect")) { // Excluir eventos instantáneos
                    availableEvents.add(type);
                }
            }
            
            // Seleccionar un evento aleatorio
            if (!availableEvents.isEmpty()) {
                EventType selectedEvent = availableEvents.get(random.nextInt(availableEvents.size()));
                
                // Iniciar el evento seleccionado por una semana
                startEvent(selectedEvent, WEEK_IN_MILLIS);
                
                // Programar el siguiente evento
                scheduleNextEvent(WEEK_IN_MILLIS);
            }
        } finally {
            // Release the lock
            isEventStarting = false;
        }
    }
    
    /**
     * Starts an event from a command with proper synchronization
     * @param eventType The type of event to start
     * @param duration The duration in milliseconds
     * @return true if the event was started successfully, false otherwise
     */
    public boolean startEventFromCommand(EventType eventType, long duration) {
        // Check if an event is already active or starting
        if (isEventActive) {
            Bukkit.getConsoleSender().sendMessage(
                MM.toComponent("&cYa hay un evento activo. Detén el evento actual antes de iniciar uno nuevo.")
            );
            return false;
        }
        
        // Cancel any scheduled tasks to prevent automatic events from starting
        if (weeklyTask != null) {
            weeklyTask.cancel();
            weeklyTask = null;
        }
        
        // Start the event
        startEvent(eventType, duration);
        
        // Schedule the next event after this one ends
        scheduleNextEvent(duration);
        
        return true;
    }
    
    /**
     * Stops the current event and clears event data
     */
    public void forceStopCurrentEvent() {
        if (!isEventActive || currentEvent == null) {
            return;
        }
        
        // Stop the current event
        currentEvent.stop();
        
        isEventActive = false;
        isPaused = false;
        currentEvent = null;
        currentEventType = null;
        
        // Clear the JSON file to indicate no active event
        clearEventData();
        
        // Cancel any scheduled tasks
        if (weeklyTask != null) {
            weeklyTask.cancel();
            weeklyTask = null;
        }
        
        // Announce the end of the event
        Bukkit.broadcast(MM.toComponent("<gold><b>¡EVENTO SEMANAL FINALIZADO!"));
    }
    
    /**
     * Schedules the next event
     */
    public void scheduleNextEvent(String timer) {
        if (!isEventActive || currentEvent == null) {
            return;
        }

        // Programar el siguiente evento
        weeklyTask = new BukkitRunnable() {
            @Override
            public void run() {
                startRandomEvent();
            }
        }.runTaskLater(plugin, TimeConverter.parseTimeToTicks(timer));
    }

    /**
     * Clears the event data file
     */
    @SuppressWarnings("unchecked")
    private void clearEventData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            JSONObject data = new JSONObject();
            data.put("eventActive", false);
            writer.write(data.toJSONString());
            writer.flush();
            plugin.getLogger().info("Event data cleared successfully");
        } catch (IOException e) {
            plugin.getLogger().severe("Error al limpiar datos del evento: " + e.getMessage());
        }
    }
    
    private void startEvent(EventType eventType, long duration) {
        // Check if an event is already active
        if (isEventActive) {
            Bukkit.getConsoleSender().sendMessage(
                MM.toComponent("&cYa hay un evento activo. Detén el evento actual antes de iniciar uno nuevo.")
            );
            return;
        }
        
        // Guardar información del evento actual
        currentEventType = eventType;
        eventStartTime = System.currentTimeMillis();
        eventEndTime = eventStartTime + duration;
        isEventActive = true;
        

        
        // Iniciar el evento según su tipo
        switch (eventType.getEventName()) {
            case "acid_week":
                currentEvent = new AcidWeek(plugin, duration);
                break;
                
            case "toxic_fog":
                currentEvent = new ToxicFog(plugin, duration);
                break;
                
            case "undead_week":
                currentEvent = new UndeadWeek(plugin, duration);
                break;
                
            case "explosive_week":
                currentEvent = new ExplosiveWeek(plugin, duration);
                break;
                
            case "blood_and_iron_week":
                currentEvent = new BloodAndIronWeek(plugin, duration);
                break;
                
            default:
                Bukkit.getConsoleSender().sendMessage(
                    MM.toComponent("&cEvento no implementado para ejecución semanal: " + eventType.getEventName())
                );
                isEventActive = false;
                return;
        }
    
    // Iniciar el evento
    currentEvent.start();
    saveEventData();
    }
    
    @SuppressWarnings("unused")
    private String getEventDisplayName(EventType eventType) {
        switch (eventType.getEventName()) {
            case "acid_week": return "Ácida";
            case "toxic_fog": return "Niebla Tóxica";
            case "undead_week": return "No-Muertos";
            case "paranoia_effect": return "Paranoia";
            case "explosive_week": return "Explosiva";
            case "blood_and_iron_week": return "Sangre y Hierro";
            default: return eventType.getEventName();
        }
    }
    
    private void scheduleNextEvent(long delay) {
        // Cancelar tarea anterior si existe
        if (weeklyTask != null) {
            weeklyTask.cancel();
        }
        
        // Adjust delay if the event is paused
        long adjustedDelay = delay;
        if (isPaused) {
            // If paused, we need to account for the time already paused
            adjustedDelay += System.currentTimeMillis() - pauseStartTime;
        }
        
        // Programar el próximo evento
        weeklyTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Finalizar evento actual si es necesario
                stopCurrentEvent();
                
                // Iniciar nuevo evento aleatorio
                startRandomEvent();
            }
        }.runTaskLater(plugin, TimeUnit.MILLISECONDS.toSeconds(adjustedDelay) * 20); // Convertir a ticks
    }


    public void pauseCurrentEvent() {
        if (!isEventActive || isPaused || currentEvent == null) {
            return;
        }
        
        isPaused = true;
        pauseStartTime = System.currentTimeMillis();
        
        // Pause the event
        currentEvent.pause();
        
        // Save the updated event data with pause information
        saveEventData();
        
        // Announce event pause
        Bukkit.broadcast(MM.toComponent("<gold><b>¡EVENTO SEMANAL PAUSADO!"));
        Bukkit.broadcast(MM.toComponent("<yellow>El evento se reanudará cuando un administrador lo indique."));
    }
    
    public void resumeCurrentEvent() {
        if (!isEventActive || !isPaused || currentEvent == null) {
            return;
        }
        
        // Calculate how long the event was paused
        long currentTime = System.currentTimeMillis();
        long pauseDuration = currentTime - pauseStartTime;
        totalPausedTime += pauseDuration;
        
        // Adjust the end time to account for the pause
        eventEndTime += pauseDuration;
        
        isPaused = false;
        
        // Resume the event
        currentEvent.resume();
        
        // Save the updated event data after resuming
        saveEventData();
        
        // Announce event resume
        Bukkit.broadcast(MM.toComponent("<gold><b>¡EVENTO SEMANAL REANUDADO!"));
        Bukkit.broadcast(MM.toComponent("<yellow>El evento continuará por el tiempo restante."));
    }
    
    
    @SuppressWarnings("unchecked")
    private void saveEventData() {
        JSONObject data = new JSONObject();
        data.put("eventActive", isEventActive);
        
        if (isEventActive && currentEventType != null) {
            data.put("eventType", currentEventType.getEventName());
            data.put("startTime", eventStartTime);
            data.put("endTime", eventEndTime);
            data.put("isPaused", isPaused);
            data.put("pauseStartTime", pauseStartTime);
            data.put("totalPausedTime", totalPausedTime);
        }
        
        try (FileWriter writer = new FileWriter(dataFile)) {
            writer.write(data.toJSONString());
            writer.flush();
            plugin.getLogger().info("§aEvent data saved successfully");
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar datos del evento semanal: " + e.getMessage());
        }
    }
    
    private boolean loadSavedEventData() {
        if (!dataFile.exists()) {
            return false;
        }
        
        try {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(new FileReader(dataFile));
            
            // Check if the event is active
            Boolean eventActive = (Boolean) data.get("eventActive");
            if (eventActive == null || !eventActive) {
                // No active event or explicitly marked as inactive
                return false;
            }
            
            // Get event type
            String eventTypeName = (String) data.get("eventType");
            if (eventTypeName == null) {
                plugin.getLogger().warning("Event data file exists but has no event type");
                return false;
            }
            
            // Get time values with null checks
            Long startTime = (Long) data.get("startTime");
            Long endTime = (Long) data.get("endTime");
            
            if (startTime == null || endTime == null) {
                plugin.getLogger().warning("Event data file has missing time values");
                return false;
            }
            
            eventStartTime = startTime;
            eventEndTime = endTime;
            
            // Load pause state if available (with null checks)
            if (data.containsKey("isPaused")) {
                Boolean paused = (Boolean) data.get("isPaused");
                isPaused = (paused != null) ? paused : false;
            }
            
            if (data.containsKey("pauseStartTime")) {
                Long pauseStart = (Long) data.get("pauseStartTime");
                pauseStartTime = (pauseStart != null) ? pauseStart : 0L;
            }
            
            if (data.containsKey("totalPausedTime")) {
                Long totalPaused = (Long) data.get("totalPausedTime");
                totalPausedTime = (totalPaused != null) ? totalPaused : 0L;
            }
            
            // Get event type from name
            currentEventType = EventType.getByName(eventTypeName);
            isEventActive = (currentEventType != null);
            
            return currentEventType != null;
        } catch (IOException | ParseException e) {
            plugin.getLogger().severe("Error al cargar datos del evento semanal: " + e.getMessage());
            // If there's an error, try to delete the corrupted file
            dataFile.delete();
            return false;
        } catch (ClassCastException e) {
            plugin.getLogger().severe("Error de formato en el archivo de datos del evento: " + e.getMessage());
            // If there's a format error, delete the corrupted file
            dataFile.delete();
            return false;
        }
    }
    
    public void shutdown() {
        // Guardar el estado actual antes de apagar
        if (isEventActive) {
            saveEventData();
        }
        
        // Cancelar tareas programadas
        if (weeklyTask != null) {
            weeklyTask.cancel();
        }
    }
    
    public boolean isEventActive() {
        return isEventActive;
    }
    
    public EventType getCurrentEventType() {
        return currentEventType;
    }
    
    public void stopCurrentEvent() {
        if (!isEventActive || currentEvent == null) {
            return;
        }
        
        // Detener el evento
        currentEvent.stop();
        
        isEventActive = false;
        currentEvent = null;
        currentEventType = null; // Clear the event type
        
        // Clear the JSON file to indicate no active event
        clearEventData();
        
        // Anunciar fin del evento
        Bukkit.broadcast(MM.toComponent("<gold><b>¡EVENTO SEMANAL FINALIZADO!"));
        Bukkit.broadcast(MM.toComponent("<yellow>El próximo evento comenzará pronto..."));
    }

    // Update getTimeRemaining to account for paused time
    public long getTimeRemaining() {
        if (!isEventActive) {
            return 0;
        }
        
        if (isPaused) {
            return eventEndTime - pauseStartTime;
        }
        
        return Math.max(0, eventEndTime - System.currentTimeMillis());
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Gets the current event object
     * @return The current Weeklyevent object, or null if no event is active
     */
    public WeeklyEvent getCurrentEvent() {
        return currentEvent;
    }

    
    /**
     * Detiene un evento específico si está activo y coincide con el tipo proporcionado.
     * @param eventType El tipo de evento a detener
     */
    public void stopSpecificEvent(EventType eventType) {
        if (!isEventActive || currentEvent == null || currentEventType == null) {
            return;
        }
        
        // Verificar si el evento actual coincide con el tipo solicitado
        if (currentEventType.equals(eventType)) {
            // Detener el evento actual
            currentEvent.stop();
            
            // Actualizar el estado del gestor
            isEventActive = false;
            currentEvent = null;
            currentEventType = null;
            
            // Limpiar los datos del evento
            clearEventData();
            
            // Anunciar que el evento ha sido detenido
            Bukkit.broadcast(MM.toComponent("<red><b>¡EVENTO SEMANAL DETENIDO!</b>"));
            Bukkit.broadcast(MM.toComponent("<yellow>El evento '" + eventType.getEventName() + "' ha sido detenido manualmente."));
            
            plugin.getLogger().info("Evento '" + eventType.getEventName() + "' detenido manualmente.");
        } else {
            plugin.getLogger().warning("Intento de detener un evento que no está activo: " + eventType.getEventName());
        }
    }
}