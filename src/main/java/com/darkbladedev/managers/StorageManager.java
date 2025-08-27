package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.utils.DayCycleUtils;
import com.darkbladedev.utils.EmptyEvent;
import com.darkbladedev.utils.EventType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;


public class StorageManager {

    private final File dataFile;
    private final File dayCycleDataFile;
    private final File eventSpecificDataFile;

    private final Gson gson;
    private final HeartlessMain plugin;

    public StorageManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.dayCycleDataFile = new File(plugin.getDataFolder(), "day_cycle_data.yml");
        this.dataFile = new File(plugin.getDataFolder(), "weekly_event_data.json");
        this.eventSpecificDataFile = new File(plugin.getDataFolder(), "event_specific_data.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Crear directorio si no existe
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        // Removido createEmptyEventFileIfNeeded() - WeeklyEventManager maneja su propia persistencia
        createDayCycleFileIfNeeded();
        DayCycleUtils.init(YamlConfiguration.loadConfiguration(dayCycleDataFile), plugin.getLogger());
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
    
    public void createDayCycleFileIfNeeded() {
        if (!dayCycleDataFile.exists()) {
            try {
                Files.createDirectories(dayCycleDataFile.getParentFile().toPath());
                YamlConfiguration config = new YamlConfiguration();
                config.save(dayCycleDataFile);
                plugin.getLogger().info("Archivo day_cycle_data.yml creado exitosamente en: " + dayCycleDataFile.getAbsolutePath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "No se pudo crear el archivo day_cycle_data.yml", e);
            }
        }
    }
    
    /**
     * Guarda los datos del ciclo de día
     */
    public void saveDayCycleData() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            DayCycleUtils.save(config);
            config.save(dayCycleDataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando los datos del ciclo de día", e);
        }
    }
    
    /**
     * Recarga los datos del evento desde el archivo
     * @return Los datos del evento recargados o null si hay un error
     */
    public WeeklyEventData reloadEventData() {
        // WeeklyEventManager maneja su propia persistencia, no crear archivo vacío
        // Cargar datos frescos
        return loadEvent();
    }
    
    /**
     * Guarda los datos específicos de un evento (estadísticas, jugadores infectados, etc.)
     * @param event El evento del cual guardar los datos específicos
     */
    public void saveEventSpecificData(WeeklyEvent event) {
        if (event == null) return;
        
        try (Writer writer = new FileWriter(eventSpecificDataFile)) {
            Map<String, Object> eventData = new HashMap<>();
            
            if (event instanceof UndeadWeek) {
                UndeadWeek undeadEvent = (UndeadWeek) event;
                eventData.put("eventType", "UndeadWeek");
                eventData.put("infectedPlayers", undeadEvent.getInfectedPlayers());
                eventData.put("infectedPlayersTime", undeadEvent.getInfectedPlayersTime());
                eventData.put("curedInfectionsCount", undeadEvent.getCuredInfectionsCount());
                eventData.put("redMoonKillsCount", undeadEvent.getRedMoonKillsCount());
                eventData.put("curedVillagers", undeadEvent.getCuredVillagers());
                eventData.put("witherKilledInRedMoon", undeadEvent.getWitherKilledInRedMoon());
                eventData.put("isRedMoonActive", undeadEvent.isRedMoonActive());
                eventData.put("lastRedMoonNight", undeadEvent.getLastRedMoonNight());
            }
            // Aquí se pueden agregar más tipos de eventos en el futuro
            
            gson.toJson(eventData, writer);
            plugin.getLogger().info("Datos específicos del evento guardados correctamente");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando los datos específicos del evento", e);
        }
    }
    
    /**
     * Carga los datos específicos de un evento
     * @param event El evento al cual cargar los datos específicos
     */
    @SuppressWarnings("unchecked")
    public void loadEventSpecificData(WeeklyEvent event) {
        if (event == null || !eventSpecificDataFile.exists()) return;
        
        try (Reader reader = Files.newBufferedReader(eventSpecificDataFile.toPath())) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> eventData = gson.fromJson(reader, type);
            
            if (eventData == null) return;
            
            String eventType = (String) eventData.get("eventType");
            
            if ("UndeadWeek".equals(eventType) && event instanceof UndeadWeek) {
                UndeadWeek undeadEvent = (UndeadWeek) event;
                
                // Cargar mapas y sets con verificación de tipos
                Object infectedPlayersObj = eventData.get("infectedPlayers");
                if (infectedPlayersObj instanceof Map) {
                    undeadEvent.loadInfectedPlayers((Map<String, Object>) infectedPlayersObj);
                }
                
                Object infectedPlayersTimeObj = eventData.get("infectedPlayersTime");
                if (infectedPlayersTimeObj instanceof Map) {
                    undeadEvent.loadInfectedPlayersTime((Map<String, Object>) infectedPlayersTimeObj);
                }
                
                Object curedInfectionsCountObj = eventData.get("curedInfectionsCount");
                if (curedInfectionsCountObj instanceof Map) {
                    undeadEvent.loadCuredInfectionsCount((Map<String, Object>) curedInfectionsCountObj);
                }
                
                Object redMoonKillsCountObj = eventData.get("redMoonKillsCount");
                if (redMoonKillsCountObj instanceof Map) {
                    undeadEvent.loadRedMoonKillsCount((Map<String, Object>) redMoonKillsCountObj);
                }
                
                Object curedVillagersObj = eventData.get("curedVillagers");
                if (curedVillagersObj instanceof List) {
                    undeadEvent.loadCuredVillagers((List<String>) curedVillagersObj);
                }
                
                Object witherKilledInRedMoonObj = eventData.get("witherKilledInRedMoon");
                if (witherKilledInRedMoonObj instanceof List) {
                    undeadEvent.loadWitherKilledInRedMoon((List<String>) witherKilledInRedMoonObj);
                }
                
                Object isRedMoonActiveObj = eventData.get("isRedMoonActive");
                if (isRedMoonActiveObj instanceof Boolean) {
                    undeadEvent.setRedMoonActive((Boolean) isRedMoonActiveObj);
                }
                
                Object lastRedMoonNightObj = eventData.get("lastRedMoonNight");
                if (lastRedMoonNightObj instanceof Number) {
                    undeadEvent.setLastRedMoonNight(((Number) lastRedMoonNightObj).longValue());
                }
                
                plugin.getLogger().info("Datos específicos del evento UndeadWeek cargados correctamente");
            }
            // Aquí se pueden agregar más tipos de eventos en el futuro
            
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando los datos específicos del evento", e);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al procesar los datos específicos del evento: " + e.getMessage());
        }
    }
    
    /**
     * Elimina el archivo de datos específicos del evento
     */
    public void clearEventSpecificData() {
        if (eventSpecificDataFile.exists()) {
            try {
                Files.delete(eventSpecificDataFile.toPath());
                plugin.getLogger().info("Datos específicos del evento eliminados");
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Error al eliminar datos específicos del evento", e);
            }
        }
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
