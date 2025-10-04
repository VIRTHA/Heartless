package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.AbstractWeeklyEvent;
import com.darkbladedev.mechanics.AcidWeek;
import com.darkbladedev.mechanics.ExplosiveWeek;
import com.darkbladedev.mechanics.ToxicFog;
import com.darkbladedev.mechanics.BloodAndIronWeek;
import com.darkbladedev.persistence.EventDataPersistenceManager;
import com.darkbladedev.utils.DayCycleUtils;
import com.darkbladedev.utils.EmptyEvent;
import com.darkbladedev.utils.EventType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
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
     * Carga los datos del ciclo de día desde el archivo
     */
    public void loadDayCycleData() {
        try {
            if (dayCycleDataFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(dayCycleDataFile);
                DayCycleUtils.init(config, plugin.getLogger());
                plugin.getLogger().info("Datos del ciclo de día cargados correctamente desde: " + dayCycleDataFile.getAbsolutePath());
            } else {
                plugin.getLogger().warning("Archivo de datos del ciclo de día no existe: " + dayCycleDataFile.getAbsolutePath());
                // Crear el archivo si no existe
                createDayCycleFileIfNeeded();
                // Inicializar DayCycleUtils con configuración vacía
                DayCycleUtils.init(new YamlConfiguration(), plugin.getLogger());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando los datos del ciclo de día", e);
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
        
        // Validación null-safe para EventType.getByName()
        EventType eventType = EventType.getByName(event.getId());
        if (eventType != null) {
            data.eventType = eventType.getEventName();
        } else {
            // Fallback: usar el ID del evento directamente si no se encuentra en el enum
            plugin.getLogger().warning("EventType no encontrado para: " + event.getId() + ". Usando ID directo.");
            data.eventType = event.getId();
        }
        
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
                
                // Guardar contador de eliminaciones en Luna Roja
                JsonObject redMoonKillsJson = new JsonObject();
                for (Map.Entry<UUID, Integer> entry : undeadWeek.getRedMoonKillsCount().entrySet()) {
                    redMoonKillsJson.addProperty(entry.getKey().toString(), entry.getValue());
                }
                eventData.add("redMoonKills", redMoonKillsJson);
                
                // Guardar conjunto de aldeanos curados
                JsonObject curedVillagersJson = new JsonObject();
                for (UUID playerId : undeadWeek.getCuredVillagers()) {
                    curedVillagersJson.addProperty(playerId.toString(), true);
                }
                eventData.add("curedVillagers", curedVillagersJson);
                
                // Guardar contador de aldeanos curados
                JsonObject curedVillagersCountJson = new JsonObject();
                for (Map.Entry<UUID, Integer> entry : undeadWeek.getCuredVillagersCount().entrySet()) {
                    curedVillagersCountJson.addProperty(entry.getKey().toString(), entry.getValue());
                }
                eventData.add("curedVillagersCount", curedVillagersCountJson);
                
                // Guardar conjunto de Withers eliminados en Luna Roja
                JsonObject witherKilledJson = new JsonObject();
                for (UUID playerId : undeadWeek.getWitherKilledInRedMoon()) {
                    witherKilledJson.addProperty(playerId.toString(), true);
                }
                eventData.add("witherKilledInRedMoon", witherKilledJson);
                
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
                
                // Validar y guardar mapas de conteos
                JsonObject playerKillCountJson = new JsonObject();
                Map<UUID, Integer> killCounts = bloodAndIronWeek.getPlayerKillCount();
                if (killCounts != null) {
                    for (Map.Entry<UUID, Integer> entry : killCounts.entrySet()) {
                        if (entry.getKey() != null && entry.getValue() != null && entry.getValue() >= 0) {
                            // Validar que el conteo sea razonable (máximo 1000 kills)
                            if (entry.getValue() <= 1000) {
                                playerKillCountJson.addProperty(entry.getKey().toString(), entry.getValue());
                            } else {
                                plugin.getLogger().warning("Conteo de kills excesivo para jugador " + entry.getKey() + ": " + entry.getValue() + ", limitando a 1000");
                                playerKillCountJson.addProperty(entry.getKey().toString(), 1000);
                            }
                        }
                    }
                }
                eventData.add("playerKillCount", playerKillCountJson);
                
                JsonObject consecutiveKillsJson = new JsonObject();
                Map<UUID, Integer> consecutiveKills = bloodAndIronWeek.getConsecutiveKills();
                if (consecutiveKills != null) {
                    for (Map.Entry<UUID, Integer> entry : consecutiveKills.entrySet()) {
                        if (entry.getKey() != null && entry.getValue() != null && entry.getValue() >= 0) {
                            // Validar que los kills consecutivos sean razonables (máximo 100)
                            if (entry.getValue() <= 100) {
                                consecutiveKillsJson.addProperty(entry.getKey().toString(), entry.getValue());
                            } else {
                                plugin.getLogger().warning("Kills consecutivos excesivos para jugador " + entry.getKey() + ": " + entry.getValue() + ", limitando a 100");
                                consecutiveKillsJson.addProperty(entry.getKey().toString(), 100);
                            }
                        }
                    }
                }
                eventData.add("consecutiveKills", consecutiveKillsJson);
                
                // Validar y guardar conjuntos de jugadores
                JsonObject pentakillPlayersJson = new JsonObject();
                Set<UUID> pentakillPlayers = bloodAndIronWeek.getPentakillPlayers();
                if (pentakillPlayers != null) {
                    for (UUID playerId : pentakillPlayers) {
                        if (playerId != null) {
                            pentakillPlayersJson.addProperty(playerId.toString(), true);
                        }
                    }
                }
                eventData.add("pentakillPlayers", pentakillPlayersJson);
                
                JsonObject survivedPlayersJson = new JsonObject();
                Set<UUID> survivedPlayers = bloodAndIronWeek.getSurvivedPlayers();
                if (survivedPlayers != null) {
                    for (UUID playerId : survivedPlayers) {
                        if (playerId != null) {
                            survivedPlayersJson.addProperty(playerId.toString(), true);
                        }
                    }
                }
                eventData.add("survivedPlayers", survivedPlayersJson);
                
                JsonObject deadPlayersJson = new JsonObject();
                Set<UUID> deadPlayers = bloodAndIronWeek.getDeadPlayers();
                if (deadPlayers != null) {
                    for (UUID playerId : deadPlayers) {
                        if (playerId != null) {
                            deadPlayersJson.addProperty(playerId.toString(), true);
                        }
                    }
                }
                eventData.add("deadPlayers", deadPlayersJson);
                
                JsonObject awardedAdrenalineJson = new JsonObject();
                Set<UUID> awardedAdrenaline = bloodAndIronWeek.getAwardedAdrenaline();
                if (awardedAdrenaline != null) {
                    for (UUID playerId : awardedAdrenaline) {
                        if (playerId != null) {
                            awardedAdrenalineJson.addProperty(playerId.toString(), true);
                        }
                    }
                }
                eventData.add("awardedAdrenaline", awardedAdrenalineJson);
                
                JsonObject mobKillWarningGivenJson = new JsonObject();
                Set<UUID> mobKillWarningGiven = bloodAndIronWeek.getMobKillWarningGiven();
                if (mobKillWarningGiven != null) {
                    for (UUID playerId : mobKillWarningGiven) {
                        if (playerId != null) {
                            mobKillWarningGivenJson.addProperty(playerId.toString(), true);
                        }
                    }
                }
                eventData.add("mobKillWarningGiven", mobKillWarningGivenJson);
                
                // Validar y guardar massKillers (jugadores con 10+ kills)
                JsonObject massKillersJson = new JsonObject();
                Set<UUID> massKillers = bloodAndIronWeek.getMassKillers();
                if (massKillers != null) {
                    for (UUID playerId : massKillers) {
                        if (playerId != null) {
                            massKillersJson.addProperty(playerId.toString(), true);
                        }
                    }
                }
                eventData.add("massKillers", massKillersJson);
                
                // Validar y guardar playerKillers (jugadores que mataron 3+ jugadores)
                JsonObject playerKillersJson = new JsonObject();
                Set<UUID> playerKillers = bloodAndIronWeek.getPlayerKillers();
                if (playerKillers != null) {
                    for (UUID playerId : playerKillers) {
                        if (playerId != null) {
                            playerKillersJson.addProperty(playerId.toString(), true);
                        }
                    }
                }
                eventData.add("playerKillers", playerKillersJson);
                
                // Validar y guardar survivors (jugadores supervivientes)
                JsonObject survivorsJson = new JsonObject();
                Set<UUID> survivors = bloodAndIronWeek.getSurvivors();
                if (survivors != null) {
                    for (UUID playerId : survivors) {
                        if (playerId != null) {
                            survivorsJson.addProperty(playerId.toString(), true);
                        }
                    }
                }
                eventData.add("survivors", survivorsJson);
            }
            
            // Guardar challengeProgress y completedChallenges para todos los eventos que extienden AbstractWeeklyEvent
            if (event instanceof com.darkbladedev.mechanics.AbstractWeeklyEvent) {
                com.darkbladedev.mechanics.AbstractWeeklyEvent abstractEvent = (com.darkbladedev.mechanics.AbstractWeeklyEvent) event;
                
                // Guardar challengeProgress
                JsonObject challengeProgressJson = new JsonObject();
                Map<UUID, Map<String, Object>> allChallengeProgress = abstractEvent.getAllChallengeProgress();
                if (allChallengeProgress != null) {
                    for (Map.Entry<UUID, Map<String, Object>> playerEntry : allChallengeProgress.entrySet()) {
                        if (playerEntry.getKey() != null && playerEntry.getValue() != null) {
                            JsonObject playerProgressJson = new JsonObject();
                            for (Map.Entry<String, Object> challengeEntry : playerEntry.getValue().entrySet()) {
                                if (challengeEntry.getKey() != null && challengeEntry.getValue() != null) {
                                    // Convertir el valor del progreso a JSON apropiado
                                    if (challengeEntry.getValue() instanceof Number) {
                                        playerProgressJson.addProperty(challengeEntry.getKey(), (Number) challengeEntry.getValue());
                                    } else if (challengeEntry.getValue() instanceof Boolean) {
                                        playerProgressJson.addProperty(challengeEntry.getKey(), (Boolean) challengeEntry.getValue());
                                    } else if (challengeEntry.getValue() instanceof String) {
                                        playerProgressJson.addProperty(challengeEntry.getKey(), (String) challengeEntry.getValue());
                                    } else {
                                        // Para otros tipos, convertir a string
                                        playerProgressJson.addProperty(challengeEntry.getKey(), challengeEntry.getValue().toString());
                                    }
                                }
                            }
                            challengeProgressJson.add(playerEntry.getKey().toString(), playerProgressJson);
                        }
                    }
                }
                eventData.add("challengeProgress", challengeProgressJson);
                
                // Guardar completedChallenges
                JsonObject completedChallengesJson = new JsonObject();
                Map<UUID, Set<String>> allCompletedChallenges = abstractEvent.getAllCompletedChallenges();
                if (allCompletedChallenges != null) {
                    for (Map.Entry<UUID, Set<String>> playerEntry : allCompletedChallenges.entrySet()) {
                        if (playerEntry.getKey() != null && playerEntry.getValue() != null) {
                            JsonArray challengesArray = new JsonArray();
                            for (String challengeId : playerEntry.getValue()) {
                                if (challengeId != null && !challengeId.trim().isEmpty()) {
                                    challengesArray.add(challengeId);
                                }
                            }
                            completedChallengesJson.add(playerEntry.getKey().toString(), challengesArray);
                        }
                    }
                }
                eventData.add("completedChallenges", completedChallengesJson);
                
                plugin.getLogger().info("Guardados " + allChallengeProgress.size() + " registros de progreso de desafíos y " + 
                    allCompletedChallenges.size() + " registros de desafíos completados");
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
            plugin.getLogger().warning("Evento es null, no se pueden cargar datos específicos");
            return;
        }
        
        // Validaciones completas de estado del evento antes de cargar datos
        if (!event.isActive()) {
            plugin.getLogger().warning("Intentando cargar datos para evento inactivo: " + event.getClass().getSimpleName());
            return;
        }
        
        if (event.isPaused()) {
            plugin.getLogger().info("Evento está pausado, cargando datos para reanudación: " + event.getClass().getSimpleName());
        }
        
        // Validar que el evento no esté en un estado inconsistente
        if (event.getStartTime() <= 0) {
            plugin.getLogger().warning("Evento tiene tiempo de inicio inválido: " + event.getStartTime());
            return;
        }
        
        if (event.getEndTime() <= event.getStartTime()) {
            plugin.getLogger().warning("Evento tiene tiempo de fin inválido: " + event.getEndTime() + " (inicio: " + event.getStartTime() + ")");
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
                
                // Validar coincidencia de tipo de evento
                String expectedEventType = getExpectedEventType(event);
                if (expectedEventType == null) {
                    plugin.getLogger().warning("Tipo de evento no reconocido: " + event.getClass().getSimpleName());
                    return;
                }
                
                if (eventType.isEmpty()) {
                    plugin.getLogger().warning("Archivo de datos no contiene tipo de evento válido");
                    return;
                }
                
                if (!expectedEventType.equals(eventType)) {
                    plugin.getLogger().warning("Tipo de evento no coincide. Esperado: " + expectedEventType + ", Encontrado: " + eventType);
                    return;
                }
                
                plugin.getLogger().info("Validación de tipo de evento exitosa: " + eventType);
                
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
                        Map<UUID, Boolean> infectedPlayers = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : infectedPlayersJson.entrySet()) {
                            infectedPlayers.put(UUID.fromString(entry.getKey()), entry.getValue().getAsBoolean());
                        }
                        undeadWeek.loadInfectedPlayers(infectedPlayers);
                    }
                    
                    if (eventData.has("infectedPlayersTime")) {
                        JsonObject infectedPlayersTimeJson = eventData.getAsJsonObject("infectedPlayersTime");
                        Map<UUID, Long> infectedPlayersTime = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : infectedPlayersTimeJson.entrySet()) {
                            infectedPlayersTime.put(UUID.fromString(entry.getKey()), entry.getValue().getAsLong());
                        }
                        undeadWeek.loadInfectedPlayersTime(infectedPlayersTime);
                    }
                        
                    if (eventData.has("curedInfections")) {
                        JsonObject curedInfectionsJson = eventData.getAsJsonObject("curedInfections");
                        Map<UUID, Integer> curedInfections = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : curedInfectionsJson.entrySet()) {
                            curedInfections.put(UUID.fromString(entry.getKey()), entry.getValue().getAsInt());
                        }
                        undeadWeek.loadCuredInfectionsCount(curedInfections);
                    }
                    
                    // Cargar contador de eliminaciones en Luna Roja
                    if (eventData.has("redMoonKills")) {
                        JsonObject redMoonKillsJson = eventData.getAsJsonObject("redMoonKills");
                        Map<UUID, Integer> redMoonKills = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : redMoonKillsJson.entrySet()) {
                            redMoonKills.put(UUID.fromString(entry.getKey()), entry.getValue().getAsInt());
                        }
                        undeadWeek.loadRedMoonKillsCount(redMoonKills);
                    }
                    
                    // Cargar conjunto de aldeanos curados
                    if (eventData.has("curedVillagers")) {
                        JsonObject curedVillagersJson = eventData.getAsJsonObject("curedVillagers");
                        Set<UUID> curedVillagers = new HashSet<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : curedVillagersJson.entrySet()) {
                            if (entry.getValue().getAsBoolean()) {
                                curedVillagers.add(UUID.fromString(entry.getKey()));
                            }
                        }
                        undeadWeek.loadCuredVillagers(curedVillagers);
                    }
                    
                    // Cargar contador de aldeanos curados
                    if (eventData.has("curedVillagersCount")) {
                        JsonObject curedVillagersCountJson = eventData.getAsJsonObject("curedVillagersCount");
                        Map<UUID, Integer> curedVillagersCount = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : curedVillagersCountJson.entrySet()) {
                            curedVillagersCount.put(UUID.fromString(entry.getKey()), entry.getValue().getAsInt());
                        }
                        undeadWeek.loadCuredVillagersCount(curedVillagersCount);
                    }
                    
                    // Cargar conjunto de Withers eliminados en Luna Roja
                    if (eventData.has("witherKilledInRedMoon")) {
                        JsonObject witherKilledJson = eventData.getAsJsonObject("witherKilledInRedMoon");
                        Set<UUID> witherKilled = new HashSet<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : witherKilledJson.entrySet()) {
                            if (entry.getValue().getAsBoolean()) {
                                witherKilled.add(UUID.fromString(entry.getKey()));
                            }
                        }
                        undeadWeek.loadWitherKilledInRedMoon(witherKilled);
                    }
                    
                    // *** NUEVO: Cargar datos de desafíos ***
                    // Cargar progreso de desafíos
                    if (eventData.has("challengeProgress")) {
                        JsonObject challengeProgressJson = eventData.getAsJsonObject("challengeProgress");
                        Map<String, Map<String, Object>> challengeProgressData = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> playerEntry : challengeProgressJson.entrySet()) {
                            String playerId = playerEntry.getKey();
                            JsonObject playerProgressJson = playerEntry.getValue().getAsJsonObject();
                            Map<String, Object> playerProgress = new HashMap<>();
                            for (Map.Entry<String, com.google.gson.JsonElement> progressEntry : playerProgressJson.entrySet()) {
                                String challengeId = progressEntry.getKey();
                                com.google.gson.JsonElement progressValue = progressEntry.getValue();
                                
                                // Convertir el valor JSON al tipo apropiado
                                if (progressValue.isJsonPrimitive()) {
                                    if (progressValue.getAsJsonPrimitive().isNumber()) {
                                        playerProgress.put(challengeId, progressValue.getAsInt());
                                    } else if (progressValue.getAsJsonPrimitive().isBoolean()) {
                                        playerProgress.put(challengeId, progressValue.getAsBoolean());
                                    } else {
                                        playerProgress.put(challengeId, progressValue.getAsString());
                                    }
                                } else {
                                    playerProgress.put(challengeId, progressValue.toString());
                                }
                            }
                            challengeProgressData.put(playerId, playerProgress);
                        }
                        undeadWeek.loadChallengeProgressFromString(challengeProgressData);
                    }
                    
                    // Cargar desafíos completados
                    if (eventData.has("completedChallenges")) {
                        JsonObject completedChallengesJson = eventData.getAsJsonObject("completedChallenges");
                        Map<String, Set<String>> completedChallengesData = new HashMap<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> playerEntry : completedChallengesJson.entrySet()) {
                            String playerId = playerEntry.getKey();
                            JsonArray challengesArray = playerEntry.getValue().getAsJsonArray();
                            Set<String> playerCompletedChallenges = new HashSet<>();
                            for (com.google.gson.JsonElement challengeElement : challengesArray) {
                                playerCompletedChallenges.add(challengeElement.getAsString());
                            }
                            completedChallengesData.put(playerId, playerCompletedChallenges);
                        }
                        undeadWeek.loadCompletedChallengesFromString(completedChallengesData);
                    }
                    
                    plugin.getLogger().info("Datos específicos de UndeadWeek cargados correctamente");
                }
                
                // AcidWeek
                else if (event instanceof AcidWeek && "acid_week".equals(eventType)) {
                    AcidWeek acidWeek = (AcidWeek) event;
                    
                    if (eventData.has("playersInWater")) {
                        JsonObject playersInWaterJson = eventData.getAsJsonObject("playersInWater");
                        Set<UUID> playersInWater = new HashSet<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : playersInWaterJson.entrySet()) {
                            playersInWater.add(UUID.fromString(entry.getKey()));
                        }
                        acidWeek.loadPlayersInWater(playersInWater);
                    }
                    
                    if (eventData.has("playersInRain")) {
                        JsonObject playersInRainJson = eventData.getAsJsonObject("playersInRain");
                        Set<UUID> playersInRain = new HashSet<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : playersInRainJson.entrySet()) {
                            playersInRain.add(UUID.fromString(entry.getKey()));
                        }
                        acidWeek.loadPlayersInRain(playersInRain);
                    }
                    
                    // *** NUEVO: Cargar datos de desafíos ***
                    // Cargar progreso de desafíos
                    if (eventData.has("challengeProgress")) {
                        JsonElement challengeProgressElement = eventData.get("challengeProgress");
                        Map<String, Map<String, Object>> challengeProgressData = new HashMap<>();
                        
                        if (challengeProgressElement.isJsonObject()) {
                            JsonObject challengeProgressJson = challengeProgressElement.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> playerEntry : challengeProgressJson.entrySet()) {
                                String playerId = playerEntry.getKey();
                                JsonElement playerProgressElement = playerEntry.getValue();
                                
                                if (playerProgressElement.isJsonObject()) {
                                    JsonObject playerProgressJson = playerProgressElement.getAsJsonObject();
                                    Map<String, Object> playerProgress = new HashMap<>();
                                    
                                    for (Map.Entry<String, JsonElement> progressEntry : playerProgressJson.entrySet()) {
                                        String challengeId = progressEntry.getKey();
                                        JsonElement progressValue = progressEntry.getValue();
                                        
                                        // Convertir diferentes tipos de JSON a Object
                                        if (progressValue.isJsonPrimitive()) {
                                            JsonPrimitive primitive = progressValue.getAsJsonPrimitive();
                                            if (primitive.isNumber()) {
                                                playerProgress.put(challengeId, primitive.getAsNumber());
                                            } else if (primitive.isBoolean()) {
                                                playerProgress.put(challengeId, primitive.getAsBoolean());
                                            } else if (primitive.isString()) {
                                                playerProgress.put(challengeId, primitive.getAsString());
                                            }
                                        } else {
                                            playerProgress.put(challengeId, progressValue.toString());
                                        }
                                    }
                                    challengeProgressData.put(playerId, playerProgress);
                                }
                            }
                        }
                        acidWeek.loadChallengeProgressFromString(challengeProgressData);
                    }
                    
                    // Cargar desafíos completados
                    if (eventData.has("completedChallenges")) {
                        JsonElement completedChallengesElement = eventData.get("completedChallenges");
                        Map<String, Set<String>> completedChallengesData = new HashMap<>();
                        
                        if (completedChallengesElement.isJsonObject()) {
                            JsonObject completedChallengesJson = completedChallengesElement.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> playerEntry : completedChallengesJson.entrySet()) {
                                String playerId = playerEntry.getKey();
                                JsonElement playerChallengesElement = playerEntry.getValue();
                                
                                Set<String> playerCompletedChallenges = new HashSet<>();
                                if (playerChallengesElement.isJsonArray()) {
                                    JsonArray challengesArray = playerChallengesElement.getAsJsonArray();
                                    for (JsonElement challengeElement : challengesArray) {
                                        if (challengeElement.isJsonPrimitive()) {
                                            playerCompletedChallenges.add(challengeElement.getAsString());
                                        }
                                    }
                                }
                                completedChallengesData.put(playerId, playerCompletedChallenges);
                            }
                        }
                        acidWeek.loadCompletedChallengesFromString(completedChallengesData);
                    }
                    
                    plugin.getLogger().info("Datos específicos de AcidWeek cargados correctamente (incluyendo progreso de desafíos)");
                }
                
                // ExplosiveWeek
                else if (event instanceof ExplosiveWeek && "explosive_week".equals(eventType)) {
                    ExplosiveWeek explosiveWeek = (ExplosiveWeek) event;
                    
                    if (eventData.has("ghastKillers")) {
                        JsonObject ghastKillersJson = eventData.getAsJsonObject("ghastKillers");
                        Set<UUID> ghastKillers = new HashSet<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : ghastKillersJson.entrySet()) {
                            ghastKillers.add(UUID.fromString(entry.getKey()));
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
                        JsonObject playerExplosionKillersJson = eventData.getAsJsonObject("playerExplosionKillers");
                        Set<UUID> playerExplosionKillers = new HashSet<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : playerExplosionKillersJson.entrySet()) {
                            playerExplosionKillers.add(UUID.fromString(entry.getKey()));
                        }
                        explosiveWeek.loadPlayerExplosionKillers(playerExplosionKillers);
                    }
                    
                    plugin.getLogger().info("Datos específicos de ExplosiveWeek cargados correctamente");
                }
                
                // ToxicFog
                else if (event instanceof ToxicFog && "toxic_fog".equals(eventType)) {
                    ToxicFog toxicFog = (ToxicFog) event;
                    
                    if (eventData.has("affectedPlayers")) {
                        JsonObject affectedPlayersJson = eventData.getAsJsonObject("affectedPlayers");
                        Set<UUID> affectedPlayers = new HashSet<>();
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : affectedPlayersJson.entrySet()) {
                            affectedPlayers.add(UUID.fromString(entry.getKey()));
                        }
                        toxicFog.loadAffectedPlayers(affectedPlayers);
                    }
                    
                    plugin.getLogger().info("Datos específicos de ToxicFog cargados correctamente");
                }
                
                // BloodAndIronWeek
                else if (event instanceof BloodAndIronWeek && "blood_and_iron_week".equals(eventType)) {
                    BloodAndIronWeek bloodAndIronWeek = (BloodAndIronWeek) event;
                    
                    try {
                        if (eventData.has("lastHostileMobKillTime")) {
                            JsonObject lastHostileMobKillTimeJson = eventData.getAsJsonObject("lastHostileMobKillTime");
                            if (lastHostileMobKillTimeJson != null && lastHostileMobKillTimeJson.size() > 0) {
                                Map<UUID, Long> lastHostileMobKillTime = new HashMap<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : lastHostileMobKillTimeJson.entrySet()) {
                                    try {
                                        UUID playerId = UUID.fromString(entry.getKey());
                                        long killTime = entry.getValue().getAsLong();
                                        // Validar que el tiempo no sea futuro
                                        if (killTime <= System.currentTimeMillis()) {
                                            lastHostileMobKillTime.put(playerId, killTime);
                                        } else {
                                            plugin.getLogger().warning("Tiempo de kill futuro detectado para jugador " + playerId + ", ignorando");
                                        }
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en lastHostileMobKillTime: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadLastHostileMobKillTime(lastHostileMobKillTime);
                            }
                        }
                        
                        if (eventData.has("lastPlayerKillTime")) {
                            JsonObject lastPlayerKillTimeJson = eventData.getAsJsonObject("lastPlayerKillTime");
                            if (lastPlayerKillTimeJson != null && lastPlayerKillTimeJson.size() > 0) {
                                Map<UUID, Long> lastPlayerKillTime = new HashMap<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : lastPlayerKillTimeJson.entrySet()) {
                                    try {
                                        UUID playerId = UUID.fromString(entry.getKey());
                                        long killTime = entry.getValue().getAsLong();
                                        // Validar que el tiempo no sea futuro
                                        if (killTime <= System.currentTimeMillis()) {
                                            lastPlayerKillTime.put(playerId, killTime);
                                        } else {
                                            plugin.getLogger().warning("Tiempo de kill de jugador futuro detectado para " + playerId + ", ignorando");
                                        }
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en lastPlayerKillTime: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadLastPlayerKillTime(lastPlayerKillTime);
                            }
                        }
                        
                        if (eventData.has("playerKillCount")) {
                            JsonObject playerKillCountJson = eventData.getAsJsonObject("playerKillCount");
                            if (playerKillCountJson != null && playerKillCountJson.size() > 0) {
                                Map<UUID, Integer> playerKillCount = new HashMap<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : playerKillCountJson.entrySet()) {
                                    try {
                                        UUID playerId = UUID.fromString(entry.getKey());
                                        int killCount = entry.getValue().getAsInt();
                                        // Validar que el conteo no sea negativo
                                        if (killCount >= 0) {
                                            playerKillCount.put(playerId, killCount);
                                        } else {
                                            plugin.getLogger().warning("Conteo de kills negativo detectado para " + playerId + ", ignorando");
                                        }
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en playerKillCount: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadPlayerKillCount(playerKillCount);
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Error al cargar datos básicos de BloodAndIronWeek", e);
                    }
                    
                        if (eventData.has("consecutiveKills")) {
                            JsonObject consecutiveKillsJson = eventData.getAsJsonObject("consecutiveKills");
                            if (consecutiveKillsJson != null && consecutiveKillsJson.size() > 0) {
                                Map<UUID, Integer> consecutiveKills = new HashMap<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : consecutiveKillsJson.entrySet()) {
                                    try {
                                        UUID playerId = UUID.fromString(entry.getKey());
                                        int kills = entry.getValue().getAsInt();
                                        if (kills >= 0) {
                                            consecutiveKills.put(playerId, kills);
                                        } else {
                                            plugin.getLogger().warning("Kills consecutivos negativos para " + playerId + ", ignorando");
                                        }
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en consecutiveKills: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadConsecutiveKills(consecutiveKills);
                            }
                        }
                        
                        if (eventData.has("pentakillPlayers")) {
                            JsonObject pentakillPlayersJson = eventData.getAsJsonObject("pentakillPlayers");
                            if (pentakillPlayersJson != null) {
                                Set<UUID> pentakillPlayers = new HashSet<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : pentakillPlayersJson.entrySet()) {
                                    try {
                                        pentakillPlayers.add(UUID.fromString(entry.getKey()));
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en pentakillPlayers: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadPentakillPlayers(pentakillPlayers);
                            }
                        }
                        
                        if (eventData.has("survivedPlayers")) {
                            JsonObject survivedPlayersJson = eventData.getAsJsonObject("survivedPlayers");
                            if (survivedPlayersJson != null) {
                                Set<UUID> survivedPlayers = new HashSet<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : survivedPlayersJson.entrySet()) {
                                    try {
                                        survivedPlayers.add(UUID.fromString(entry.getKey()));
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en survivedPlayers: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadSurvivedPlayers(survivedPlayers);
                            }
                        }
                        
                        if (eventData.has("deadPlayers")) {
                            JsonObject deadPlayersJson = eventData.getAsJsonObject("deadPlayers");
                            if (deadPlayersJson != null) {
                                Set<UUID> deadPlayers = new HashSet<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : deadPlayersJson.entrySet()) {
                                    try {
                                        deadPlayers.add(UUID.fromString(entry.getKey()));
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en deadPlayers: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadDeadPlayers(deadPlayers);
                            }
                        }
                        
                        if (eventData.has("awardedAdrenaline")) {
                            JsonObject awardedAdrenalineJson = eventData.getAsJsonObject("awardedAdrenaline");
                            if (awardedAdrenalineJson != null) {
                                Set<UUID> awardedAdrenaline = new HashSet<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : awardedAdrenalineJson.entrySet()) {
                                    try {
                                        awardedAdrenaline.add(UUID.fromString(entry.getKey()));
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en awardedAdrenaline: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadAwardedAdrenaline(awardedAdrenaline);
                            }
                        }
                        
                        if (eventData.has("mobKillWarningGiven")) {
                            JsonObject mobKillWarningGivenJson = eventData.getAsJsonObject("mobKillWarningGiven");
                            if (mobKillWarningGivenJson != null) {
                                Set<UUID> mobKillWarningGiven = new HashSet<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : mobKillWarningGivenJson.entrySet()) {
                                    try {
                                        mobKillWarningGiven.add(UUID.fromString(entry.getKey()));
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en mobKillWarningGiven: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadMobKillWarningGiven(mobKillWarningGiven);
                            }
                        }
                        
                        // Cargar massKillers (jugadores con 10+ kills)
                        if (eventData.has("massKillers")) {
                            JsonObject massKillersJson = eventData.getAsJsonObject("massKillers");
                            if (massKillersJson != null) {
                                Set<UUID> massKillers = new HashSet<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : massKillersJson.entrySet()) {
                                    try {
                                        massKillers.add(UUID.fromString(entry.getKey()));
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en massKillers: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadMassKillers(massKillers);
                            }
                        }
                        
                        // Cargar playerKillers (jugadores que mataron 3+ jugadores)
                        if (eventData.has("playerKillers")) {
                            JsonObject playerKillersJson = eventData.getAsJsonObject("playerKillers");
                            if (playerKillersJson != null) {
                                Set<UUID> playerKillers = new HashSet<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : playerKillersJson.entrySet()) {
                                    try {
                                        playerKillers.add(UUID.fromString(entry.getKey()));
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en playerKillers: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadPlayerKillers(playerKillers);
                            }
                        }
                        
                        // Cargar survivors (jugadores supervivientes)
                        if (eventData.has("survivors")) {
                            JsonObject survivorsJson = eventData.getAsJsonObject("survivors");
                            if (survivorsJson != null) {
                                Set<UUID> survivors = new HashSet<>();
                                for (Map.Entry<String, com.google.gson.JsonElement> entry : survivorsJson.entrySet()) {
                                    try {
                                        survivors.add(UUID.fromString(entry.getKey()));
                                    } catch (IllegalArgumentException e) {
                                        plugin.getLogger().warning("UUID inválido en survivors: " + entry.getKey());
                                    }
                                }
                                bloodAndIronWeek.loadSurvivors(survivors);
                            }
                        }
                    
                    plugin.getLogger().info("Datos específicos de BloodAndIronWeek cargados correctamente");
                }
                
                // Cargar challengeProgress y completedChallenges para todos los eventos que extienden AbstractWeeklyEvent
                if (event instanceof com.darkbladedev.mechanics.AbstractWeeklyEvent) {
                    com.darkbladedev.mechanics.AbstractWeeklyEvent abstractEvent = (com.darkbladedev.mechanics.AbstractWeeklyEvent) event;
                    
                    // CORRECCIÓN CRÍTICA: Implementar carga exclusiva para evitar conflictos de datos
                    EventDataPersistenceManager persistenceManager = HeartlessMain.getEventDataPersistenceManager();
                    boolean persistenceLoaded = false;
                    
                    if (persistenceManager != null) {
                        try {
                            persistenceLoaded = persistenceManager.loadEventData(abstractEvent);
                            if (persistenceLoaded) {
                                plugin.getLogger().info("Challenge data loaded successfully with EventDataPersistenceManager for event: " + abstractEvent.getId());
                                plugin.getLogger().info("EXCLUSIVE LOADING: Skipping legacy StorageManager to prevent data conflicts");
                                // RETORNAR INMEDIATAMENTE para evitar sobrescritura por el método legacy
                                return;
                            } else {
                                plugin.getLogger().info("EventDataPersistenceManager returned false for event: " + abstractEvent.getId());
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to load challenge data with EventDataPersistenceManager: " + e.getMessage());
                        }
                    } else {
                        plugin.getLogger().warning("EventDataPersistenceManager is null, falling back to legacy method");
                    }
                    
                    // Solo usar el método legacy si EventDataPersistenceManager falló completamente
                    plugin.getLogger().info("FALLBACK: Loading challenge data with legacy StorageManager method for event: " + abstractEvent.getId());
                    
                    // Cargar challengeProgress (método legacy)
                    if (eventData.has("challengeProgress")) {
                        JsonObject challengeProgressJson = eventData.getAsJsonObject("challengeProgress");
                        Map<UUID, Map<String, Object>> challengeProgressData = new HashMap<>();
                        
                        for (Map.Entry<String, JsonElement> playerEntry : challengeProgressJson.entrySet()) {
                            try {
                                UUID playerId = UUID.fromString(playerEntry.getKey());
                                JsonObject playerProgressJson = playerEntry.getValue().getAsJsonObject();
                                Map<String, Object> playerProgress = new HashMap<>();
                                
                                for (Map.Entry<String, JsonElement> challengeEntry : playerProgressJson.entrySet()) {
                                    String challengeId = challengeEntry.getKey();
                                    JsonElement progressElement = challengeEntry.getValue();
                                    
                                    // Convertir el progreso según su tipo
                                    Object progressValue;
                                    if (progressElement.isJsonPrimitive()) {
                                        JsonPrimitive primitive = progressElement.getAsJsonPrimitive();
                                        if (primitive.isNumber()) {
                                            // Intentar mantener el tipo numérico original
                                            if (primitive.getAsString().contains(".")) {
                                                progressValue = primitive.getAsDouble();
                                            } else {
                                                progressValue = primitive.getAsInt();
                                            }
                                        } else if (primitive.isBoolean()) {
                                            progressValue = primitive.getAsBoolean();
                                        } else {
                                            progressValue = primitive.getAsString();
                                        }
                                    } else {
                                        progressValue = progressElement.toString();
                                    }
                                    
                                    playerProgress.put(challengeId, progressValue);
                                }
                                challengeProgressData.put(playerId, playerProgress);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("UUID inválido en challengeProgress: " + playerEntry.getKey());
                            } catch (Exception e) {
                                plugin.getLogger().warning("Error cargando progreso de desafío para jugador " + playerEntry.getKey() + ": " + e.getMessage());
                            }
                        }
                        
                        // Usar el método de carga centralizado
                        abstractEvent.loadChallengeProgress(challengeProgressData);
                    }
                    
                    // Cargar completedChallenges (método legacy)
                    if (eventData.has("completedChallenges")) {
                        JsonObject completedChallengesJson = eventData.getAsJsonObject("completedChallenges");
                        Map<UUID, Set<String>> completedChallengesData = new HashMap<>();
                        
                        for (Map.Entry<String, JsonElement> playerEntry : completedChallengesJson.entrySet()) {
                            try {
                                UUID playerId = UUID.fromString(playerEntry.getKey());
                                JsonArray challengesArray = playerEntry.getValue().getAsJsonArray();
                                Set<String> playerCompletedChallenges = new HashSet<>();
                                
                                for (JsonElement challengeElement : challengesArray) {
                                    if (challengeElement.isJsonPrimitive()) {
                                        playerCompletedChallenges.add(challengeElement.getAsString());
                                    }
                                }
                                completedChallengesData.put(playerId, playerCompletedChallenges);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("UUID inválido en completedChallenges: " + playerEntry.getKey());
                            } catch (Exception e) {
                                plugin.getLogger().warning("Error cargando desafíos completados para jugador " + playerEntry.getKey() + ": " + e.getMessage());
                            }
                        }
                        
                        // Usar el método de carga centralizado
                        abstractEvent.loadCompletedChallenges(completedChallengesData);
                    }
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

    /**
     * Guarda los datos del evento desde WeeklyEventManager (método para testing)
     * @param eventManager El manager del evento
     */
    public void saveEventData(WeeklyEventManager eventManager) {
        if (eventManager == null) {
            plugin.getLogger().warning("Intento de guardar datos de eventManager nulo");
            return;
        }
        
        AbstractWeeklyEvent currentEvent = eventManager.getCurrentEvent();
        if (currentEvent != null) {
            saveEvent(currentEvent);
        }
    }

    /**
     * Carga los datos del evento en WeeklyEventManager (método para testing)
     * @param eventManager El manager del evento
     */
    public void loadEventData(WeeklyEventManager eventManager) {
        if (eventManager == null) {
            plugin.getLogger().warning("Intento de cargar datos en eventManager nulo");
            return;
        }
        
        WeeklyEventData eventData = loadEvent();
        if (eventData != null && eventManager.getCurrentEvent() != null) {
            loadEventSpecificData(eventManager.getCurrentEvent());
        }
    }

    /**
     * Guarda configuración en formato JSON (método para testing)
     * @param data Los datos JSON a guardar
     * @param filePath La ruta del archivo
     */
    public void saveConfig(JsonObject data, String filePath) {
        try (Writer writer = new FileWriter(new File(filePath))) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando configuración en " + filePath, e);
        }
    }

    /**
     * Carga configuración desde archivo JSON (método para testing)
     * @param filePath La ruta del archivo
     * @return Los datos JSON cargados o null si hay error
     */
    public JsonObject loadConfig(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        
        try (Reader reader = Files.newBufferedReader(file.toPath())) {
            return gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando configuración desde " + filePath, e);
            return null;
        }
    }

    /**
     * Obtiene el tipo de evento esperado basado en la instancia del evento
     * @param event La instancia del evento
     * @return El tipo de evento esperado o null si no se reconoce
     */
    private String getExpectedEventType(WeeklyEvent event) {
        if (event instanceof UndeadWeek) {
            return "undead_week";
        } else if (event instanceof AcidWeek) {
            return "acid_week";
        } else if (event instanceof ExplosiveWeek) {
            return "explosive_week";
        } else if (event instanceof ToxicFog) {
            return "toxic_fog";
        } else if (event instanceof BloodAndIronWeek) {
            return "blood_and_iron_week";
        } else if (event instanceof EmptyEvent) {
            return "empty_event";
        }
        return null;
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
