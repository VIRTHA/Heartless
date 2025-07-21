package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.utils.EmptyEvent;
import com.darkbladedev.utils.EventType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.util.logging.Level;


public class StorageManager {

    private final File dataFile;
    private final Gson gson;
    private final HeartlessMain plugin;

    public StorageManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "weekly_event_data.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Crear directorio si no existe
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        createEmptyEventFileIfNeeded();
    }

    public void saveEvent(WeeklyEvent event) {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(toData(event), writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando los datos del evento semanal", e);
        }
    }

    public WeeklyEventData loadEvent() {
        if (!dataFile.exists()) return null;
        try (Reader reader = Files.newBufferedReader(dataFile.toPath())) {
            return gson.fromJson(reader, WeeklyEventData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createEmptyEventFileIfNeeded() {
        if (!dataFile.exists()) {
            try {
                Files.createDirectories(dataFile.getParentFile().toPath());
                saveEvent(new EmptyEvent(plugin, 1L));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "No se pudo crear el archivo de datos del evento", e);
            }
        }
    }
    
    /**
     * Recarga los datos del evento desde el archivo
     * @return Los datos del evento recargados o null si hay un error
     */
    public WeeklyEventData reloadEventData() {
        // Asegurar que el archivo existe
        createEmptyEventFileIfNeeded();
        // Cargar datos frescos
        return loadEvent();
    }

    private WeeklyEventData toData(WeeklyEvent event) {
        WeeklyEventData data = new WeeklyEventData();
        data.isPaused = event.isPaused();
        data.pauseStartTime = event.getPauseStartTime();
        data.totalPausedTime = event.getTotalPausedTime();
        data.startTime = event.getStartTime();
        data.endTime = event.getEndTime();
        data.eventType = EventType.getByName(event.getName()).getEventName();
        data.eventActive = event.isActive();
        return data;
    }

    public static class WeeklyEventData {
        public boolean isPaused;
        public long pauseStartTime;
        public long totalPausedTime;
        public long startTime;
        public long endTime;
        public String eventType;
        public boolean eventActive;
    }
}
