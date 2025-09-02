package com.darkbladedev.managers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import com.darkbladedev.utils.EventType;
import com.darkbladedev.utils.MM;
import com.darkbladedev.HeartlessMain;
import com.darkbladedev.events.WeeklyEventDispatcher;
import com.darkbladedev.mechanics.AcidWeek;
import com.darkbladedev.mechanics.BloodAndIronWeek;
import com.darkbladedev.mechanics.ExplosiveWeek;
import com.darkbladedev.mechanics.ToxicFog;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.utils.TimeConverter;
import com.darkbladedev.utils.TimeExpression;

/**
 * Thread-safe manager for weekly events with improved synchronization and error handling.
 * 
 * Key improvements:
 * - Thread-safe operations using ReentrantReadWriteLock
 * - Atomic variables for critical state management
 * - Enhanced error handling and validation
 * - Consistent data persistence patterns
 * - Memory leak prevention
 * 
 * @author DarkBladeDev
 * @version 2.0 - Thread-Safe Edition
 */
public class WeeklyEventManager {
    private static final long WEEK_IN_MILLIS = TimeUnit.DAYS.toMillis(7);
    private static final String DATA_FILENAME = "weekly_event_data.json";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    // Thread-safe components
    private final ReentrantReadWriteLock eventLock = new ReentrantReadWriteLock();
    private final AtomicBoolean isEventStarting = new AtomicBoolean(false);
    private final AtomicBoolean isEventActive = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicLong eventStartTime = new AtomicLong(0);
    private final AtomicLong eventEndTime = new AtomicLong(0);
    private final AtomicLong pauseStartTime = new AtomicLong(0);
    private final AtomicLong totalPausedTime = new AtomicLong(0);
    
    // Core components
    private final HeartlessMain plugin;
    private final Random random = new Random();
    private final File dataFile;
    private final Gson gson;
    private final WeeklyEventDispatcher eventDispatcher;
    
    // Volatile references for thread visibility
    private volatile BukkitTask weeklyTask;
    private volatile EventType currentEventType;
    private volatile WeeklyEvent currentEvent;
    
    public WeeklyEventManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), DATA_FILENAME);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.eventDispatcher = new WeeklyEventDispatcher(plugin, this);
        
        // Ensure data directory exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }
    
    /**
     * Thread-safe initialization of the event manager
     */
    public void initialize() {
        eventLock.writeLock().lock();
        try {
            // Prevent multiple simultaneous initializations
            if (isEventStarting.get()) {
                plugin.getLogger().warning("Event manager is already initializing, skipping duplicate initialization");
                return;
            }
            
            if (!isEventStarting.compareAndSet(false, true)) {
                plugin.getLogger().warning("Failed to acquire initialization lock");
                return;
            }
            
            try {
                if (loadSavedEventData()) {
                    handleExistingEvent();
                } else {
                    startRandomEvent();
                }
            } finally {
                isEventStarting.set(false);
            }
        } finally {
            eventLock.writeLock().unlock();
        }
    }
    
    /**
     * Handles existing event restoration after server restart
     */
    private void handleExistingEvent() {
        long currentTime = System.currentTimeMillis();
        long remainingTime = eventEndTime.get() - currentTime;
        
        if (remainingTime > 0) {
            try {
                if (isPaused.get()) {
                    resumeCurrentEvent();
                } else {
                    if (currentEvent != null) {
                        currentEvent.resume();
                    }
                }
                
                long adjustedRemaining = Math.max(0L, eventEndTime.get() - System.currentTimeMillis());
                if (adjustedRemaining > 0L) {
                    scheduleNextEvent(adjustedRemaining);
                } else {
                    startRandomEvent();
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error resuming existing event: " + e.getMessage());
                startRandomEvent();
            }
        } else {
            startRandomEvent();
        }
    }
    
    /**
     * Thread-safe random event starter
     */
    private void startRandomEvent() {
        eventLock.writeLock().lock();
        try {
            if (isEventActive.get() || isEventStarting.get()) {
                plugin.getLogger().warning("Cannot start random event: event already active or starting");
                return;
            }
            
            if (!isEventStarting.compareAndSet(false, true)) {
                plugin.getLogger().warning("Failed to acquire event starting lock");
                return;
            }
            
            try {
                List<EventType> availableEvents = getAvailableEvents();
                
                if (!availableEvents.isEmpty()) {
                    EventType selectedEvent = availableEvents.get(random.nextInt(availableEvents.size()));
                    startEvent(selectedEvent, WEEK_IN_MILLIS);
                    scheduleNextEvent(WEEK_IN_MILLIS);
                } else {
                    plugin.getLogger().warning("No available events to start");
                }
            } finally {
                isEventStarting.set(false);
            }
        } finally {
            eventLock.writeLock().unlock();
        }
    }
    
    /**
     * Gets list of available events for weekly rotation
     */
    private List<EventType> getAvailableEvents() {
        List<EventType> availableEvents = new ArrayList<>();
        for (EventType type : EventType.values()) {
            String eventName = type.getEventName();
            boolean isUnsuitableEvent = eventName.equals("mob_rain") || 
                                      eventName.equals("size_randomizer") || 
                                      eventName.equals("paranoia_effect") || 
                                      eventName.equals("empty");
            
            if (!isUnsuitableEvent) {
                availableEvents.add(type);
            }
        }
        return availableEvents;
    }
    
    /**
     * Thread-safe event starter from command
     */
    public boolean startEventFromCommand(EventType eventType, long duration) {
        eventLock.writeLock().lock();
        try {
            if (isEventActive.get()) {
                Bukkit.getConsoleSender().sendMessage(
                    MM.toComponent("<red>Ya hay un evento activo. Detén el evento actual antes de iniciar uno nuevo.")
                );
                return false;
            }
            
            // Cancel scheduled tasks safely
            cancelWeeklyTask();
            
            // Start the event
            startEvent(eventType, duration);
            scheduleNextEvent(duration);
            
            return true;
        } finally {
            eventLock.writeLock().unlock();
        }
    }
    
    /**
     * Thread-safe task cancellation
     */
    private void cancelWeeklyTask() {
        BukkitTask task = weeklyTask;
        if (task != null) {
            task.cancel();
            weeklyTask = null;
        }
    }
    
    /**
     * Thread-safe stop of current event (public method)
     */
    public void stopCurrentEvent() {
        eventLock.writeLock().lock();
        try {
            if (!isEventActive.get() || currentEvent == null) {
                return;
            }
            
            // Fire system event before stopping
            long totalDuration = System.currentTimeMillis() - eventStartTime.get();
            eventDispatcher.fireEventStop(currentEventType, System.currentTimeMillis(), false, totalDuration);
            
            // Stop the event safely
            try {
                currentEvent.stop();
            } catch (Exception e) {
                plugin.getLogger().severe("Error stopping current event: " + e.getMessage());
            }
            
            // Reset state atomically
            resetEventState();
            
            // Clear data and cancel tasks
            clearEventData();
            cancelWeeklyTask();
            
            // Announcement
            Bukkit.broadcast(MM.toComponent("<green><b>¡EVENTO SEMANAL FINALIZADO!"));
        } finally {
            eventLock.writeLock().unlock();
        }
    }
    
    /**
     * Thread-safe force stop of current event
     */
    public void forceStopCurrentEvent() {
        eventLock.writeLock().lock();
        try {
            if (!isEventActive.get() || currentEvent == null) {
                return;
            }
            
            // Fire system event before stopping
            long totalDuration = System.currentTimeMillis() - eventStartTime.get();
            eventDispatcher.fireEventStop(currentEventType, System.currentTimeMillis(), true, totalDuration);
            
            // Stop the current event safely
            try {
                currentEvent.stop();
            } catch (Exception e) {
                plugin.getLogger().severe("Error stopping current event: " + e.getMessage());
            }
            
            // Reset state atomically
            resetEventState();
            
            // Clear data and cancel tasks
            clearEventData();
            cancelWeeklyTask();
            
            // Announcement
            Bukkit.broadcast(MM.toComponent("<red><b>¡EVENTO SEMANAL DETENIDO MANUALMENTE!"));
        } finally {
            eventLock.writeLock().unlock();
        }
    }
    
    /**
     * Atomically resets all event state variables
     */
    private void resetEventState() {
        isEventActive.set(false);
        isPaused.set(false);
        eventStartTime.set(0);
        eventEndTime.set(0);
        pauseStartTime.set(0);
        totalPausedTime.set(0);
        currentEvent = null;
        currentEventType = null;
    }
    
    /**
     * Thread-safe event scheduling with timer string
     */
    public void scheduleNextEvent(String timer) {
        eventLock.readLock().lock();
        try {
            if (!isEventActive.get() || currentEvent == null) {
                return;
            }
            
            try {
                long ticks = TimeConverter.parseTimeToTicks(timer);
                scheduleNextEventInternal(ticks * 50L); // Convert ticks to milliseconds
            } catch (Exception e) {
                plugin.getLogger().severe("Error parsing timer for next event: " + e.getMessage());
            }
        } finally {
            eventLock.readLock().unlock();
        }
    }
    
    /**
     * Thread-safe data clearing with retry mechanism
     */
    private void clearEventData() {
        for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            try (FileWriter writer = new FileWriter(dataFile)) {
                JsonObject data = new JsonObject();
                data.addProperty("eventActive", false);
                gson.toJson(data, writer);
                writer.flush();
                plugin.getLogger().info("Event data cleared successfully");
                return;
            } catch (IOException e) {
                plugin.getLogger().warning("Attempt " + (attempt + 1) + " failed to clear event data: " + e.getMessage());
                if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        plugin.getLogger().severe("Failed to clear event data after " + MAX_RETRY_ATTEMPTS + " attempts");
    }
    
    /**
     * Thread-safe event starter with enhanced validation
     */
    private void startEvent(EventType eventType, long duration) {
        if (eventType == null) {
            plugin.getLogger().severe("Cannot start event: eventType is null");
            return;
        }
        
        if (duration <= 0) {
            plugin.getLogger().severe("Cannot start event: invalid duration " + duration);
            return;
        }
        
        if (isEventActive.get()) {
            plugin.getLogger().warning("Cannot start event: another event is already active");
            return;
        }
        
        try {
            // Fire system event before starting
            eventDispatcher.fireEventStart(eventType, duration, System.currentTimeMillis());
            
            // Set event information atomically
            currentEventType = eventType;
            long startTime = System.currentTimeMillis();
            eventStartTime.set(startTime);
            eventEndTime.set(startTime + duration);
            
            // Create event instance
            WeeklyEvent event = createEventInstance(eventType, duration);
            if (event == null) {
                plugin.getLogger().severe("Failed to create event instance for: " + eventType.getEventName());
                return;
            }
            
            currentEvent = event;
            isEventActive.set(true);
            
            // Start the event
            currentEvent.start();
            saveEventData();
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error starting event " + eventType.getEventName() + ": " + e.getMessage());
            resetEventState();
        }
    }
    
    /**
     * Creates event instance based on type with proper error handling
     */
    private WeeklyEvent createEventInstance(EventType eventType, long duration) {
        try {
            TimeExpression durationExpression = TimeExpression.fromMilliseconds(duration);
            
            switch (eventType.getEventName()) {
                case "acid_week":
                    return new AcidWeek(plugin, durationExpression);
                case "toxic_fog":
                    return new ToxicFog(plugin, durationExpression);
                case "undead_week":
                    return new UndeadWeek(plugin, durationExpression);
                case "explosive_week":
                    return new ExplosiveWeek(plugin, durationExpression);
                case "blood_and_iron_week":
                    return new BloodAndIronWeek(plugin, durationExpression);
                case "empty":
                    plugin.getLogger().warning("Cannot create empty event instance");
                    return null;
                default:
                    plugin.getLogger().warning("Unknown event type: " + eventType.getEventName());
                    return null;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating event instance: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Thread-safe event scheduling with millisecond precision
     */
    private void scheduleNextEvent(long delay) {
        cancelWeeklyTask();
        
        long adjustedDelay = delay;
        if (isPaused.get()) {
            adjustedDelay += System.currentTimeMillis() - pauseStartTime.get();
        }
        
        try {
            weeklyTask = new BukkitRunnable() {
                @Override
                public void run() {
                    stopCurrentEvent();
                    startRandomEvent();
                }
            }.runTaskLater(plugin, Math.max(1L, adjustedDelay / 50L)); // Ensure minimum 1 tick
        } catch (Exception e) {
            plugin.getLogger().severe("Error scheduling next event: " + e.getMessage());
        }
    }
    
    /**
     * Internal scheduling method for consistency
     */
    private void scheduleNextEventInternal(long delayMs) {
        scheduleNextEvent(delayMs);
    }
    
    /**
     * Thread-safe event pausing
     */
    public void pauseCurrentEvent() {
        eventLock.writeLock().lock();
        try {
            if (!isEventActive.get() || isPaused.get() || currentEvent == null) {
                return;
            }
            
            // Fire system event before pausing
            long timeRemaining = eventEndTime.get() - System.currentTimeMillis();
            eventDispatcher.fireEventPause(currentEventType, System.currentTimeMillis(), timeRemaining);
            
            isPaused.set(true);
            pauseStartTime.set(System.currentTimeMillis());
            
            // Pause the event safely
            try {
                currentEvent.pause();
            } catch (Exception e) {
                plugin.getLogger().severe("Error pausing event: " + e.getMessage());
            }
            
            saveEventData();
            
            // Announcements
            Bukkit.broadcast(MM.toComponent("<gold><b>¡EVENTO SEMANAL PAUSADO!"));
            Bukkit.broadcast(MM.toComponent("<gray><u>El evento se reanudará cuando un administrador lo indique."));
        } finally {
            eventLock.writeLock().unlock();
        }
    }
    
    /**
     * Thread-safe event resuming
     */
    public void resumeCurrentEvent() {
        eventLock.writeLock().lock();
        try {
            if (!isEventActive.get() || !isPaused.get() || currentEvent == null) {
                return;
            }
            
            // Calculate pause duration atomically
            long currentTime = System.currentTimeMillis();
            long pauseDuration = currentTime - pauseStartTime.get();
            totalPausedTime.addAndGet(pauseDuration);
            
            // Fire system event before resuming
            long timeRemaining = eventEndTime.get() - currentTime;
            eventDispatcher.fireEventResume(currentEventType, currentTime, pauseDuration, timeRemaining);
            
            // Adjust end time atomically
            eventEndTime.addAndGet(pauseDuration);
            isPaused.set(false);
            
            // Resume the event safely
            try {
                currentEvent.resume();
            } catch (Exception e) {
                plugin.getLogger().severe("Error resuming event: " + e.getMessage());
            }
            
            saveEventData();
            
            // Announcements
            Bukkit.broadcast(MM.toComponent("<gold><b>EVENTO SEMANAL REANUDADO!"));
            Bukkit.broadcast(MM.toComponent("<gray><u>El evento continua su ejecución normal nuevamente."));
            
            plugin.getLogger().info("Evento semanal reanudado correctamente.");
        } finally {
            eventLock.writeLock().unlock();
        }
    }
    
    /**
     * Thread-safe data saving with retry mechanism
     */
    private void saveEventData() {
        eventLock.readLock().lock();
        try {
            JsonObject data = new JsonObject();
            data.addProperty("eventActive", isEventActive.get());
            
            if (isEventActive.get() && currentEventType != null) {
                data.addProperty("eventType", currentEventType.getEventName());
                data.addProperty("startTime", eventStartTime.get());
                data.addProperty("endTime", eventEndTime.get());
                data.addProperty("isPaused", isPaused.get());
                data.addProperty("pauseStartTime", pauseStartTime.get());
                data.addProperty("totalPausedTime", totalPausedTime.get());
            }
            
            for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
                try (FileWriter writer = new FileWriter(dataFile)) {
                    gson.toJson(data, writer);
                    writer.flush();
                    return;
                } catch (IOException e) {
                    plugin.getLogger().warning("Attempt " + (attempt + 1) + " failed to save event data: " + e.getMessage());
                    if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            plugin.getLogger().severe("Failed to save event data after " + MAX_RETRY_ATTEMPTS + " attempts");
        } finally {
            eventLock.readLock().unlock();
        }
    }
    
    /**
     * Thread-safe data loading with enhanced error handling
     */
    private boolean loadSavedEventData() {
        if (!dataFile.exists()) {
            return false;
        }
        
        eventLock.writeLock().lock();
        try {
            for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
                try (FileReader reader = new FileReader(dataFile)) {
                    JsonObject data = gson.fromJson(reader, JsonObject.class);
                    
                    if (data == null || !data.has("eventActive") || !data.get("eventActive").getAsBoolean()) {
                        return false;
                    }
                    
                    // Load event data atomically
                    String eventTypeName = data.has("eventType") ? data.get("eventType").getAsString() : null;
                    if (eventTypeName == null) {
                        return false;
                    }
                    
                    EventType eventType = findEventTypeByName(eventTypeName);
                    if (eventType == null) {
                        plugin.getLogger().warning("Unknown event type in saved data: " + eventTypeName);
                        return false;
                    }
                    
                    // Restore state atomically
                    currentEventType = eventType;
                    eventStartTime.set(data.has("startTime") ? data.get("startTime").getAsLong() : System.currentTimeMillis());
                    eventEndTime.set(data.has("endTime") ? data.get("endTime").getAsLong() : System.currentTimeMillis() + WEEK_IN_MILLIS);
                    isPaused.set(data.has("isPaused") && data.get("isPaused").getAsBoolean());
                    pauseStartTime.set(data.has("pauseStartTime") ? data.get("pauseStartTime").getAsLong() : 0);
                    totalPausedTime.set(data.has("totalPausedTime") ? data.get("totalPausedTime").getAsLong() : 0);
                    
                    // Create event instance
                    long duration = eventEndTime.get() - eventStartTime.get();
                    currentEvent = createEventInstance(eventType, duration);
                    
                    if (currentEvent != null) {
                        isEventActive.set(true);
                        plugin.getLogger().info("Loaded saved event data: " + eventTypeName);
                        return true;
                    } else {
                        plugin.getLogger().warning("Failed to create event instance for loaded data: " + eventTypeName);
                        return false;
                    }
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("Attempt " + (attempt + 1) + " failed to load event data: " + e.getMessage());
                    if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            plugin.getLogger().severe("Failed to load event data after " + MAX_RETRY_ATTEMPTS + " attempts");
            return false;
        } finally {
            eventLock.writeLock().unlock();
        }
    }
    
    /**
     * Finds EventType by name with null safety
     */
    private EventType findEventTypeByName(String eventName) {
        if (eventName == null) {
            return null;
        }
        
        for (EventType type : EventType.values()) {
            if (eventName.equals(type.getEventName())) {
                return type;
            }
        }
        return null;
    }
    

    
    /**
     * Thread-safe cleanup method
     */
    public void cleanup() {
        eventLock.writeLock().lock();
        try {
            // Cancel tasks
            cancelWeeklyTask();
            
            // Stop current event if active
            if (isEventActive.get() && currentEvent != null) {
                try {
                    currentEvent.stop();
                } catch (Exception e) {
                    plugin.getLogger().severe("Error stopping event during cleanup: " + e.getMessage());
                }
            }
            
            // Reset all state
            resetEventState();
            
            plugin.getLogger().info("WeeklyEventManager cleanup completed");
        } finally {
            eventLock.writeLock().unlock();
        }
    }
    
    // Thread-safe getters
    public boolean isEventActive() {
        return isEventActive.get();
    }
    
    public boolean isPaused() {
        return isPaused.get();
    }
    
    public EventType getCurrentEventType() {
        eventLock.readLock().lock();
        try {
            return currentEventType;
        } finally {
            eventLock.readLock().unlock();
        }
    }
    
    public WeeklyEvent getCurrentEvent() {
        eventLock.readLock().lock();
        try {
            return currentEvent;
        } finally {
            eventLock.readLock().unlock();
        }
    }
    
    public long getEventStartTime() {
        return eventStartTime.get();
    }
    
    public long getEventEndTime() {
        return eventEndTime.get();
    }
    
    public long getTotalPausedTime() {
        return totalPausedTime.get();
    }
    
    public long getRemainingTime() {
        if (!isEventActive.get()) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long remaining = eventEndTime.get() - currentTime;
        
        if (isPaused.get()) {
            // Don't subtract pause time from remaining time when paused
            return Math.max(0, remaining);
        }
        
        return Math.max(0, remaining);
    }
    
    /**
     * Alias method for compatibility with existing code
     * @return remaining time in milliseconds
     */
    public long getTimeRemaining() {
        return getRemainingTime();
    }
    
    /**
     * Force clean state - resets all event data and state
     * Used by reset command for emergency cleanup
     */
    public void forceCleanState() {
        eventLock.writeLock().lock();
        try {
            // Stop current event if active
            if (isEventActive.get() && currentEvent != null) {
                try {
                    currentEvent.stop();
                } catch (Exception e) {
                    plugin.getLogger().severe("Error stopping event during force clean: " + e.getMessage());
                }
            }
            
            // Cancel all tasks
            cancelWeeklyTask();
            
            // Reset all atomic state
            resetEventState();
            
            // Clear data file
            clearEventData();
            
            plugin.getLogger().info("Force clean state completed - all event data cleared");
        } finally {
            eventLock.writeLock().unlock();
        }
    }
    
    /**
     * Shutdown method for plugin disable
     */
    public void shutdown() {
        cleanup();
    }
    
    /**
     * Stop specific event type (compatibility method)
     * @param eventType the event type to stop
     */
    public void stopSpecificEvent(EventType eventType) {
        eventLock.readLock().lock();
        try {
            if (isEventActive.get() && currentEventType != null && currentEventType.equals(eventType)) {
                stopCurrentEvent();
            }
        } finally {
            eventLock.readLock().unlock();
        }
    }
    
    /**
     * Reload method for configuration changes
     */
    public void reload() {
        eventLock.writeLock().lock();
        try {
            plugin.getLogger().info("Reloading WeeklyEventManager...");
            
            // Save current state if event is active
            if (isEventActive.get()) {
                saveEventData();
            }
            
            // Reload configuration or reinitialize if needed
            // This is a placeholder for future configuration reloading
            
            plugin.getLogger().info("WeeklyEventManager reloaded successfully");
        } finally {
            eventLock.writeLock().unlock();
        }
    }
    
    /**
     * Gets event progress as percentage (0.0 to 1.0)
     */
    public double getEventProgress() {
        if (!isEventActive.get()) {
            return 0.0;
        }
        
        long totalDuration = eventEndTime.get() - eventStartTime.get();
        long elapsed = System.currentTimeMillis() - eventStartTime.get() - totalPausedTime.get();
        
        if (totalDuration <= 0) {
            return 1.0;
        }
        
        return Math.min(1.0, Math.max(0.0, (double) elapsed / totalDuration));
    }
}