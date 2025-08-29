package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.AcidWeek;
import com.darkbladedev.mechanics.ExplosiveWeek;
import com.darkbladedev.mechanics.ToxicFog;
import com.darkbladedev.mechanics.BloodAndIronWeek;
import com.darkbladedev.utils.DayCycleUtils;
import com.darkbladedev.utils.EmptyEvent;
import com.darkbladedev.utils.EventType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import java.io.*;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import org.bukkit.entity.EntityType;

import org.bukkit.configuration.file.YamlConfiguration;


public class StorageManager {

    private final File dataFile;
    private final File dayCycleDataFile;

    private final Gson gson;
    private final HeartlessMain plugin;

    public StorageManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.dayCycleDataFile = new File(plugin.getDataFolder(), "day_cycle_data.yml");
        this.dataFile = new File(plugin.getDataFolder(), "weekly_event_data.json");
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
        
        // Guardar también los datos específicos del evento
        saveEventSpecificData(event);
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

    /**
     * Guarda datos específicos del evento en un archivo JSON separado
     */
    public void saveEventSpecificData(WeeklyEvent event) {
        if (event == null) {
            plugin.getLogger().warning("Intento de guardar datos de evento nulo");
            return;
        }
        
        try {
            // Asegurar que el directorio existe
            if (!plugin.getDataFolder().exists()) {
                boolean created = plugin.getDataFolder().mkdirs();
                if (!created) {
                    plugin.getLogger().severe("No se pudo crear el directorio de datos del plugin");
                    return;
                }
            }
            
            File eventDataFile = new File(plugin.getDataFolder(), "event_specific_data.json");
            plugin.getLogger().info("Guardando datos específicos del evento: " + event.getClass().getSimpleName() + " en " + eventDataFile.getAbsolutePath());
            JsonObject eventData = new JsonObject();
            
            if (event instanceof UndeadWeek) {
                UndeadWeek undeadWeek = (UndeadWeek) event;
                eventData.addProperty("eventType", "undead_week");
                eventData.addProperty("isRedMoonActive", undeadWeek.isRedMoonActive());
                eventData.addProperty("redMoonStartTime", undeadWeek.getRedMoonStartTime());
                eventData.addProperty("redMoonEndTime", undeadWeek.getRedMoonEndTime());
                eventData.addProperty("infectedPlayersCount", undeadWeek.getInfectedPlayersCount());
                eventData.addProperty("curedInfectionsCount", undeadWeek.getTotalCuredInfectionsCount());
                
                // Guardar mapas como objetos JSON
                JsonObject infectedPlayersJson = new JsonObject();
                for (Map.Entry<UUID, Boolean> entry : undeadWeek.getInfectedPlayers().entrySet()) {
                    infectedPlayersJson.addProperty(entry.getKey().toString(), entry.getValue());
                }
                eventData.add("infectedPlayers", infectedPlayersJson);
                
                JsonObject infectedPlayersTimeJson = new JsonObject();
                for (Map.Entry<UUID, Long> entry : undeadWeek.getInfectedPlayersTime().entrySet()) {
                    infectedPlayersTimeJson.addProperty(entry.getKey().toString(), entry.getValue());
                }
                eventData.add("infectedPlayersTime", infectedPlayersTimeJson);
                
                JsonObject curedInfectionsJson = new JsonObject();
                 for (Map.Entry<UUID, Integer> entry : undeadWeek.getCuredInfectionsCount().entrySet()) {
                     curedInfectionsJson.addProperty(entry.getKey().toString(), entry.getValue());
                 }
                 eventData.add("curedInfections", curedInfectionsJson);
                eventData.addProperty("isPaused", undeadWeek.isPaused());
                
            } else if (event instanceof AcidWeek) {
                AcidWeek acidWeek = (AcidWeek) event;
                eventData.addProperty("eventType", "acid_week");
                
                // Guardar conjuntos de jugadores como objetos JSON
                JsonObject playersInWaterJson = new JsonObject();
                for (UUID playerId : acidWeek.getPlayersInWater()) {
                    playersInWaterJson.addProperty(playerId.toString(), true);
                }
                eventData.add("playersInWater", playersInWaterJson);
                
                JsonObject playersInRainJson = new JsonObject();
                for (UUID playerId : acidWeek.getPlayersInRain()) {
                    playersInRainJson.addProperty(playerId.toString(), true);
                }
                eventData.add("playersInRain", playersInRainJson);
                
            } else if (event instanceof ExplosiveWeek) {
                ExplosiveWeek explosiveWeek = (ExplosiveWeek) event;
                eventData.addProperty("eventType", "explosive_week");
                
                // Guardar conjuntos de jugadores
                JsonObject ghastKillersJson = new JsonObject();
                for (UUID playerId : explosiveWeek.getGhastKillers()) {
                    ghastKillersJson.addProperty(playerId.toString(), true);
                }
                eventData.add("ghastKillers", ghastKillersJson);
                
                JsonObject playerExplosionKillersJson = new JsonObject();
                for (UUID playerId : explosiveWeek.getPlayerExplosionKillers()) {
                    playerExplosionKillersJson.addProperty(playerId.toString(), true);
                }
                eventData.add("playerExplosionKillers", playerExplosionKillersJson);
                
                JsonObject wardenCreeperKillersJson = new JsonObject();
                for (UUID playerId : explosiveWeek.getWardenCreeperKillers()) {
                    wardenCreeperKillersJson.addProperty(playerId.toString(), true);
                }
                eventData.add("wardenCreeperKillers", wardenCreeperKillersJson);
                
                // Guardar mapa de coleccionistas de cabezas de mobs (más complejo)
                JsonObject mobHeadCollectorsJson = new JsonObject();
                for (Map.Entry<UUID, Set<org.bukkit.entity.EntityType>> entry : explosiveWeek.getMobHeadCollectors().entrySet()) {
                    JsonArray entityTypesArray = new JsonArray();
                    for (org.bukkit.entity.EntityType entityType : entry.getValue()) {
                        entityTypesArray.add(entityType.name());
                    }
                    mobHeadCollectorsJson.add(entry.getKey().toString(), entityTypesArray);
                }
                eventData.add("mobHeadCollectors", mobHeadCollectorsJson);
                
            } else if (event instanceof ToxicFog) {
                ToxicFog toxicFog = (ToxicFog) event;
                eventData.addProperty("eventType", "toxic_fog");
                
                // Guardar jugadores afectados
                JsonObject affectedPlayersJson = new JsonObject();
                for (UUID playerId : toxicFog.getAffectedPlayers()) {
                    affectedPlayersJson.addProperty(playerId.toString(), true);
                }
                eventData.add("affectedPlayers", affectedPlayersJson);
                
            } else if (event instanceof BloodAndIronWeek) {
                BloodAndIronWeek bloodAndIronWeek = (BloodAndIronWeek) event;
                eventData.addProperty("eventType", "blood_and_iron_week");
                
                // Guardar mapas de tiempo
                JsonObject lastHostileMobKillTimeJson = new JsonObject();
                for (Map.Entry<UUID, Long> entry : bloodAndIronWeek.getLastHostileMobKillTime().entrySet()) {
                    lastHostileMobKillTimeJson.addProperty(entry.getKey().toString(), entry.getValue());
                }
                eventData.add("lastHostileMobKillTime", lastHostileMobKillTimeJson);
                
                JsonObject lastPlayerKillTimeJson = new JsonObject();
                for (Map.Entry<UUID, Long> entry : bloodAndIronWeek.getLastPlayerKillTime().entrySet()) {
                    lastPlayerKillTimeJson.addProperty(entry.getKey().toString(), entry.getValue());
                }
                eventData.add("lastPlayerKillTime", lastPlayerKillTimeJson);
                
                // Guardar mapas de conteos
                JsonObject playerKillCountJson = new JsonObject();
                for (Map.Entry<UUID, Integer> entry : bloodAndIronWeek.getPlayerKillCount().entrySet()) {
                    playerKillCountJson.addProperty(entry.getKey().toString(), entry.getValue());
                }
                eventData.add("playerKillCount", playerKillCountJson);
                
                JsonObject consecutiveKillsJson = new JsonObject();
                for (Map.Entry<UUID, Integer> entry : bloodAndIronWeek.getConsecutiveKills().entrySet()) {
                    consecutiveKillsJson.addProperty(entry.getKey().toString(), entry.getValue());
                }
                eventData.add("consecutiveKills", consecutiveKillsJson);
                
                // Guardar conjuntos de jugadores
                JsonObject instantDamageKillersJson = new JsonObject();
                for (UUID playerId : bloodAndIronWeek.getInstantDamageKillers()) {
                    instantDamageKillersJson.addProperty(playerId.toString(), true);
                }
                eventData.add("instantDamageKillers", instantDamageKillersJson);
                
                JsonObject pentakillPlayersJson = new JsonObject();
                for (UUID playerId : bloodAndIronWeek.getPentakillPlayers()) {
                    pentakillPlayersJson.addProperty(playerId.toString(), true);
                }
                eventData.add("pentakillPlayers", pentakillPlayersJson);
                
                JsonObject survivedPlayersJson = new JsonObject();
                for (UUID playerId : bloodAndIronWeek.getSurvivedPlayers()) {
                    survivedPlayersJson.addProperty(playerId.toString(), true);
                }
                eventData.add("survivedPlayers", survivedPlayersJson);
                
                JsonObject deadPlayersJson = new JsonObject();
                for (UUID playerId : bloodAndIronWeek.getDeadPlayers()) {
                    deadPlayersJson.addProperty(playerId.toString(), true);
                }
                eventData.add("deadPlayers", deadPlayersJson);
                
                JsonObject awardedAdrenalineJson = new JsonObject();
                for (UUID playerId : bloodAndIronWeek.getAwardedAdrenaline()) {
                    awardedAdrenalineJson.addProperty(playerId.toString(), true);
                }
                eventData.add("awardedAdrenaline", awardedAdrenalineJson);
                
                JsonObject mobKillWarningGivenJson = new JsonObject();
                for (UUID playerId : bloodAndIronWeek.getMobKillWarningGiven()) {
                    mobKillWarningGivenJson.addProperty(playerId.toString(), true);
                }
                eventData.add("mobKillWarningGiven", mobKillWarningGivenJson);
            }
            
            try (FileWriter writer = new FileWriter(eventDataFile)) {
                gson.toJson(eventData, writer);
                writer.flush(); // Asegurar que se escriba al disco
                plugin.getLogger().info("Datos específicos del evento guardados exitosamente en: " + eventDataFile.getAbsolutePath());
                plugin.getLogger().info("Tamaño del archivo: " + eventDataFile.length() + " bytes");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando datos específicos del evento en: " + 
                (new File(plugin.getDataFolder(), "event_specific_data.json")).getAbsolutePath(), e);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error inesperado guardando datos específicos del evento", e);
        }
    }
    
    /**
     * Carga datos específicos del evento desde el archivo JSON
     */
    public void loadEventSpecificData(WeeklyEvent event) {
        if (event == null) {
            plugin.getLogger().warning("Intento de cargar datos de evento nulo");
            return;
        }
        
        try {
            File eventDataFile = new File(plugin.getDataFolder(), "event_specific_data.json");
            if (!eventDataFile.exists()) {
                plugin.getLogger().info("Archivo de datos específicos del evento no existe: " + eventDataFile.getAbsolutePath());
                return;
            }
            
            plugin.getLogger().info("Cargando datos específicos del evento desde: " + eventDataFile.getAbsolutePath());
            plugin.getLogger().info("Tamaño del archivo: " + eventDataFile.length() + " bytes");
            
            try (FileReader reader = new FileReader(eventDataFile)) {
                JsonObject eventData = JsonParser.parseReader(reader).getAsJsonObject();
                String eventType = eventData.has("eventType") ? eventData.get("eventType").getAsString() : "";
                
                // UndeadWeek
                if (event instanceof UndeadWeek && "undead_week".equals(eventType)) {
                    UndeadWeek undeadWeek = (UndeadWeek) event;
                    
                    if (eventData.has("isRedMoonActive")) {
                        undeadWeek.setRedMoonActive(eventData.get("isRedMoonActive").getAsBoolean());
                    }
                    if (eventData.has("redMoonStartTime")) {
                        undeadWeek.setRedMoonStartTime(eventData.get("redMoonStartTime").getAsLong());
                    }
                    if (eventData.has("redMoonEndTime")) {
                        undeadWeek.setRedMoonEndTime(eventData.get("redMoonEndTime").getAsLong());
                    }
                    if (eventData.has("infectedPlayersCount")) {
                        undeadWeek.setInfectedPlayersCount(eventData.get("infectedPlayersCount").getAsInt());
                    }
                    if (eventData.has("curedInfectionsCount")) {
                        undeadWeek.setCuredInfectionsCount(eventData.get("curedInfectionsCount").getAsInt());
                    }
                    
                    // Cargar mapas desde JSON
                    if (eventData.has("infectedPlayers")) {
                        JsonObject infectedPlayersJson = eventData.getAsJsonObject("infectedPlayers");
                        Map<String, Object> infectedPlayers = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : infectedPlayersJson.entrySet()) {
                            infectedPlayers.put(entry.getKey(), entry.getValue().getAsBoolean());
                        }
                        undeadWeek.loadInfectedPlayers(infectedPlayers);
                    }
                    
                    if (eventData.has("infectedPlayersTime")) {
                        JsonObject infectedPlayersTimeJson = eventData.getAsJsonObject("infectedPlayersTime");
                        Map<String, Object> infectedPlayersTime = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : infectedPlayersTimeJson.entrySet()) {
                            infectedPlayersTime.put(entry.getKey(), entry.getValue().getAsLong());
                        }
                        undeadWeek.loadInfectedPlayersTime(infectedPlayersTime);
                    }
                        
                    if (eventData.has("curedInfections")) {
                        JsonObject curedInfectionsJson = eventData.getAsJsonObject("curedInfections");
                        Map<String, Object> curedInfections = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : curedInfectionsJson.entrySet()) {
                            curedInfections.put(entry.getKey(), entry.getValue().getAsInt());
                        }
                        undeadWeek.loadCuredInfectionsCount(curedInfections);
                    }
                    
                    plugin.getLogger().info("Datos específicos de UndeadWeek cargados correctamente");
                }
                
                // AcidWeek
                else if (event instanceof AcidWeek && "acid_week".equals(eventType)) {
                    AcidWeek acidWeek = (AcidWeek) event;
                    
                    if (eventData.has("playersInWater")) {
                        JsonArray playersInWaterArray = eventData.getAsJsonArray("playersInWater");
                        Set<UUID> playersInWater = new HashSet<>();
                        for (com.google.gson.JsonElement element : playersInWaterArray) {
                            playersInWater.add(UUID.fromString(element.getAsString()));
                        }
                        acidWeek.loadPlayersInWater(playersInWater);
                    }
                    
                    if (eventData.has("playersInRain")) {
                        JsonArray playersInRainArray = eventData.getAsJsonArray("playersInRain");
                        Set<UUID> playersInRain = new HashSet<>();
                        for (com.google.gson.JsonElement element : playersInRainArray) {
                            playersInRain.add(UUID.fromString(element.getAsString()));
                        }
                        acidWeek.loadPlayersInRain(playersInRain);
                    }
                    
                    plugin.getLogger().info("Datos específicos de AcidWeek cargados correctamente");
                }
                
                // ExplosiveWeek
                else if (event instanceof ExplosiveWeek && "explosive_week".equals(eventType)) {
                    ExplosiveWeek explosiveWeek = (ExplosiveWeek) event;
                    
                    if (eventData.has("ghastKillers")) {
                        JsonArray ghastKillersArray = eventData.getAsJsonArray("ghastKillers");
                        Set<UUID> ghastKillers = new HashSet<>();
                        for (com.google.gson.JsonElement element : ghastKillersArray) {
                            ghastKillers.add(UUID.fromString(element.getAsString()));
                        }
                        explosiveWeek.loadGhastKillers(ghastKillers);
                    }
                    
                    if (eventData.has("mobHeadCollectors")) {
                        JsonObject mobHeadCollectorsJson = eventData.getAsJsonObject("mobHeadCollectors");
                        Map<UUID, Set<EntityType>> mobHeadCollectors = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : mobHeadCollectorsJson.entrySet()) {
                            UUID playerId = UUID.fromString(entry.getKey());
                            Set<EntityType> entityTypes = new HashSet<>();
                            JsonArray entityTypesArray = entry.getValue().getAsJsonArray();
                            for (com.google.gson.JsonElement element : entityTypesArray) {
                                entityTypes.add(EntityType.valueOf(element.getAsString()));
                            }
                            mobHeadCollectors.put(playerId, entityTypes);
                        }
                        explosiveWeek.loadMobHeadCollectors(mobHeadCollectors);
                    }
                    
                    if (eventData.has("playerExplosionKillers")) {
                        JsonArray playerExplosionKillersArray = eventData.getAsJsonArray("playerExplosionKillers");
                        Set<UUID> playerExplosionKillers = new HashSet<>();
                        for (com.google.gson.JsonElement element : playerExplosionKillersArray) {
                            playerExplosionKillers.add(UUID.fromString(element.getAsString()));
                        }
                        explosiveWeek.loadPlayerExplosionKillers(playerExplosionKillers);
                    }
                    
                    if (eventData.has("wardenCreeperKillers")) {
                        JsonArray wardenCreeperKillersArray = eventData.getAsJsonArray("wardenCreeperKillers");
                        Set<UUID> wardenCreeperKillers = new HashSet<>();
                        for (com.google.gson.JsonElement element : wardenCreeperKillersArray) {
                            wardenCreeperKillers.add(UUID.fromString(element.getAsString()));
                        }
                        explosiveWeek.loadWardenCreeperKillers(wardenCreeperKillers);
                    }
                    
                    plugin.getLogger().info("Datos específicos de ExplosiveWeek cargados correctamente");
                }
                
                // ToxicFog
                else if (event instanceof ToxicFog && "toxic_fog".equals(eventType)) {
                    ToxicFog toxicFog = (ToxicFog) event;
                    
                    if (eventData.has("affectedPlayers")) {
                        JsonArray affectedPlayersArray = eventData.getAsJsonArray("affectedPlayers");
                        Set<UUID> affectedPlayers = new HashSet<>();
                        for (com.google.gson.JsonElement element : affectedPlayersArray) {
                            affectedPlayers.add(UUID.fromString(element.getAsString()));
                        }
                        toxicFog.loadAffectedPlayers(affectedPlayers);
                    }
                    
                    plugin.getLogger().info("Datos específicos de ToxicFog cargados correctamente");
                }
                
                // BloodAndIronWeek
                else if (event instanceof BloodAndIronWeek && "blood_and_iron_week".equals(eventType)) {
                    BloodAndIronWeek bloodAndIronWeek = (BloodAndIronWeek) event;
                    
                    if (eventData.has("lastHostileMobKillTime")) {
                        JsonObject lastHostileMobKillTimeJson = eventData.getAsJsonObject("lastHostileMobKillTime");
                        Map<UUID, Long> lastHostileMobKillTime = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : lastHostileMobKillTimeJson.entrySet()) {
                            lastHostileMobKillTime.put(UUID.fromString(entry.getKey()), entry.getValue().getAsLong());
                        }
                        bloodAndIronWeek.loadLastHostileMobKillTime(lastHostileMobKillTime);
                    }
                    
                    if (eventData.has("lastPlayerKillTime")) {
                        JsonObject lastPlayerKillTimeJson = eventData.getAsJsonObject("lastPlayerKillTime");
                        Map<UUID, Long> lastPlayerKillTime = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : lastPlayerKillTimeJson.entrySet()) {
                            lastPlayerKillTime.put(UUID.fromString(entry.getKey()), entry.getValue().getAsLong());
                        }
                        bloodAndIronWeek.loadLastPlayerKillTime(lastPlayerKillTime);
                    }
                    
                    if (eventData.has("playerKillCount")) {
                        JsonObject playerKillCountJson = eventData.getAsJsonObject("playerKillCount");
                        Map<UUID, Integer> playerKillCount = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : playerKillCountJson.entrySet()) {
                            playerKillCount.put(UUID.fromString(entry.getKey()), entry.getValue().getAsInt());
                        }
                        bloodAndIronWeek.loadPlayerKillCount(playerKillCount);
                    }
                    
                    if (eventData.has("consecutiveKills")) {
                        JsonObject consecutiveKillsJson = eventData.getAsJsonObject("consecutiveKills");
                        Map<UUID, Integer> consecutiveKills = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : consecutiveKillsJson.entrySet()) {
                            consecutiveKills.put(UUID.fromString(entry.getKey()), entry.getValue().getAsInt());
                        }
                        bloodAndIronWeek.loadConsecutiveKills(consecutiveKills);
                    }
                    
                    if (eventData.has("instantDamageKillers")) {
                        JsonArray instantDamageKillersArray = eventData.getAsJsonArray("instantDamageKillers");
                        Set<UUID> instantDamageKillers = new HashSet<>();
                        for (com.google.gson.JsonElement element : instantDamageKillersArray) {
                            instantDamageKillers.add(UUID.fromString(element.getAsString()));
                        }
                        bloodAndIronWeek.loadInstantDamageKillers(instantDamageKillers);
                    }
                    
                    if (eventData.has("pentakillPlayers")) {
                        JsonArray pentakillPlayersArray = eventData.getAsJsonArray("pentakillPlayers");
                        Set<UUID> pentakillPlayers = new HashSet<>();
                        for (com.google.gson.JsonElement element : pentakillPlayersArray) {
                            pentakillPlayers.add(UUID.fromString(element.getAsString()));
                        }
                        bloodAndIronWeek.loadPentakillPlayers(pentakillPlayers);
                    }
                    
                    if (eventData.has("survivedPlayers")) {
                        JsonArray survivedPlayersArray = eventData.getAsJsonArray("survivedPlayers");
                        Set<UUID> survivedPlayers = new HashSet<>();
                        for (com.google.gson.JsonElement element : survivedPlayersArray) {
                            survivedPlayers.add(UUID.fromString(element.getAsString()));
                        }
                        bloodAndIronWeek.loadSurvivedPlayers(survivedPlayers);
                    }
                    
                    if (eventData.has("deadPlayers")) {
                        JsonArray deadPlayersArray = eventData.getAsJsonArray("deadPlayers");
                        Set<UUID> deadPlayers = new HashSet<>();
                        for (com.google.gson.JsonElement element : deadPlayersArray) {
                            deadPlayers.add(UUID.fromString(element.getAsString()));
                        }
                        bloodAndIronWeek.loadDeadPlayers(deadPlayers);
                    }
                    
                    if (eventData.has("awardedAdrenaline")) {
                        JsonArray awardedAdrenalineArray = eventData.getAsJsonArray("awardedAdrenaline");
                        Set<UUID> awardedAdrenaline = new HashSet<>();
                        for (com.google.gson.JsonElement element : awardedAdrenalineArray) {
                            awardedAdrenaline.add(UUID.fromString(element.getAsString()));
                        }
                        bloodAndIronWeek.loadAwardedAdrenaline(awardedAdrenaline);
                    }
                    
                    if (eventData.has("mobKillWarningGiven")) {
                        JsonArray mobKillWarningGivenArray = eventData.getAsJsonArray("mobKillWarningGiven");
                        Set<UUID> mobKillWarningGiven = new HashSet<>();
                        for (com.google.gson.JsonElement element : mobKillWarningGivenArray) {
                            mobKillWarningGiven.add(UUID.fromString(element.getAsString()));
                        }
                        bloodAndIronWeek.loadMobKillWarningGiven(mobKillWarningGiven);
                    }
                    
                    plugin.getLogger().info("Datos específicos de BloodAndIronWeek cargados correctamente");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando datos específicos del evento", e);
        }
    }
    
    /**
     * Limpia los datos específicos del evento
     */
    public void clearEventSpecificData() {
        try {
            File eventDataFile = new File(plugin.getDataFolder(), "event_specific_data.json");
            if (eventDataFile.exists()) {
                boolean deleted = eventDataFile.delete();
                if (deleted) {
                    plugin.getLogger().info("Archivo de datos específicos del evento eliminado exitosamente");
                } else {
                    plugin.getLogger().warning("No se pudo eliminar el archivo de datos específicos del evento");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error eliminando archivo de datos específicos del evento", e);
        }
    }
    
    /**
     * Método de diagnóstico para verificar el estado del sistema de persistencia
     */
    public String diagnosticPersistenceSystem() {
        StringBuilder result = new StringBuilder();
        result.append("<gray>=== DIAGNÓSTICO DEL SISTEMA DE PERSISTENCIA ===</gray>\n");
        
        // Verificar directorio del plugin
        File pluginDir = plugin.getDataFolder();
        result.append("<white>Directorio del plugin:</white> <yellow>").append(pluginDir.getAbsolutePath()).append("</yellow>\n");
        result.append("<white>Directorio existe:</white> ").append(pluginDir.exists() ? "<green>✓ Sí</green>" : "<red>✗ No</red>").append("\n");
        result.append("<white>Directorio es escribible:</white> ").append(pluginDir.canWrite() ? "<green>✓ Sí</green>" : "<red>✗ No</red>").append("\n\n");
        
        // Verificar archivos de datos
        File eventDataFile = new File(pluginDir, "event_specific_data.json");
        result.append("<white>Archivo de datos específicos:</white> <yellow>").append(eventDataFile.getName()).append("</yellow>\n");
        result.append("<white>Ruta completa:</white> <gray>").append(eventDataFile.getAbsolutePath()).append("</gray>\n");
        result.append("<white>Archivo existe:</white> ").append(eventDataFile.exists() ? "<green>✓ Sí</green>" : "<red>✗ No</red>").append("\n");
        if (eventDataFile.exists()) {
            result.append("<white>Tamaño del archivo:</white> <aqua>").append(eventDataFile.length()).append(" bytes</aqua>\n");
            result.append("<white>Última modificación:</white> <aqua>").append(new java.util.Date(eventDataFile.lastModified())).append("</aqua>\n");
        }
        result.append("\n");
        
        File weeklyEventFile = new File(pluginDir, "weekly_event_data.json");
        result.append("<white>Archivo de eventos semanales:</white> <yellow>").append(weeklyEventFile.getName()).append("</yellow>\n");
        result.append("<white>Ruta completa:</white> <gray>").append(weeklyEventFile.getAbsolutePath()).append("</gray>\n");
        result.append("<white>Archivo existe:</white> ").append(weeklyEventFile.exists() ? "<green>✓ Sí</green>" : "<red>✗ No</red>").append("\n");
        if (weeklyEventFile.exists()) {
            result.append("<white>Tamaño del archivo:</white> <aqua>").append(weeklyEventFile.length()).append(" bytes</aqua>\n");
            result.append("<white>Última modificación:</white> <aqua>").append(new java.util.Date(weeklyEventFile.lastModified())).append("</aqua>\n");
        }
        
        result.append("\n<gray>=== FIN DEL DIAGNÓSTICO ===</gray>");
        
        // También hacer logging para el servidor
        plugin.getLogger().info("Diagnóstico del sistema de persistencia ejecutado por comando");
        
        return result.toString();
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
