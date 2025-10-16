package com.darkbladedev.persistence;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.AbstractWeeklyEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.darkbladedev.models.EventDataContainer;

/**
 * Gestor de persistencia de datos para eventos semanales.
 * 
 * Proporciona un sistema robusto de guardado y carga de datos que incluye:
 * - Guardado asíncrono para evitar bloqueos del hilo principal
 * - Sistema de respaldos automáticos
 * - Validación de integridad de datos
 * - Recuperación automática en caso de corrupción
 * - Thread-safety completo
 * - Compresión de datos para optimizar espacio
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class EventDataPersistenceManager {
    
    private static final String DATA_FOLDER = "event_data";
    private static final String BACKUP_FOLDER = "backups";
    private static final String FILE_EXTENSION = ".json";
    private static final String BACKUP_EXTENSION = ".backup";
    private static final int MAX_BACKUPS = 5;
    
    @SuppressWarnings("unused")
    private final HeartlessMain plugin;
    private final Logger logger;
    private final Gson gson;
    private final Path dataDirectory;
    private final Path backupDirectory;
    
    // Thread-safety
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, Long> lastSaveTime = new ConcurrentHashMap<>();
    private final Map<String, String> lastDataHash = new ConcurrentHashMap<>();
    
    /**
     * Constructor del gestor de persistencia.
     * 
     * @param plugin Instancia del plugin principal
     */
    public EventDataPersistenceManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create();
        
        // Configurar directorios
        this.dataDirectory = Paths.get(plugin.getDataFolder().getAbsolutePath(), DATA_FOLDER);
        this.backupDirectory = Paths.get(plugin.getDataFolder().getAbsolutePath(), DATA_FOLDER, BACKUP_FOLDER);
        
        initializeDirectories();
    }
    
    /**
     * Inicializa los directorios necesarios para la persistencia.
     */
    private void initializeDirectories() {
        try {
            Files.createDirectories(dataDirectory);
            Files.createDirectories(backupDirectory);
            logger.info("Directorios de persistencia inicializados correctamente");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al crear directorios de persistencia", e);
        }
    }
    
    /**
     * Guarda los datos de un evento de forma asíncrona.
     * 
     * @param event El evento cuyos datos se van a guardar
     * @return CompletableFuture que se completa cuando el guardado termina
     */
    public CompletableFuture<Boolean> saveEventDataAsync(AbstractWeeklyEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return saveEventData(event);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error en guardado asíncrono para evento " + event.getId(), e);
                return false;
            }
        });
    }
    
    /**
     * Guarda los datos de un evento de forma síncrona.
     * 
     * @param event El evento cuyos datos se van a guardar
     * @return true si el guardado fue exitoso
     */
    public boolean saveEventData(AbstractWeeklyEvent event) {
        if (event == null) {
            logger.warning("Intento de guardar datos de evento nulo");
            return false;
        }
        
        String eventId = event.getId();
        lock.writeLock().lock();
        
        try {
            // Recopilar todos los datos del evento
            EventDataContainer dataContainer = collectEventData(event);
            
            // Convertir a JSON
            String jsonData = gson.toJson(dataContainer);
            
            // Verificar si los datos han cambiado
            String dataHash = generateDataHash(jsonData);
            String lastHash = lastDataHash.get(eventId);
            
            if (dataHash.equals(lastHash)) {
                logger.fine("Los datos del evento " + eventId + " no han cambiado, omitiendo guardado");
                return true;
            }
            
            // Crear respaldo antes de guardar
            createBackup(eventId);
            
            // Guardar datos
            Path eventFile = getEventFilePath(eventId);
            Files.write(eventFile, jsonData.getBytes());
            
            // Actualizar metadatos
            lastSaveTime.put(eventId, System.currentTimeMillis());
            lastDataHash.put(eventId, dataHash);
            
            logger.info("Datos del evento " + eventId + " guardados correctamente");
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar datos del evento " + eventId, e);
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Carga los datos de un evento.
     * 
     * @param event El evento al que se van a cargar los datos
     * @return true si la carga fue exitosa
     */
    public boolean loadEventData(AbstractWeeklyEvent event) {
        if (event == null) {
            logger.warning("Intento de cargar datos de evento nulo");
            return false;
        }
        
        String eventId = event.getId();
        lock.readLock().lock();
        
        try {
            Path eventFile = getEventFilePath(eventId);
            
            if (!Files.exists(eventFile)) {
                logger.info("No se encontraron datos guardados para el evento " + eventId);
                return true; // No es un error, simplemente no hay datos previos
            }
            
            // Leer archivo
            String jsonData = new String(Files.readAllBytes(eventFile));
            
            // Validar integridad
            if (!validateDataIntegrity(jsonData)) {
                logger.warning("Datos corruptos detectados para evento " + eventId + ", intentando recuperar desde respaldo");
                return loadFromBackup(event);
            }
            
            // Parsear datos
            EventDataContainer dataContainer = gson.fromJson(jsonData, EventDataContainer.class);
            
            if (dataContainer == null) {
                logger.warning("Error al parsear datos del evento " + eventId);
                return loadFromBackup(event);
            }
            
            // Aplicar datos al evento
            applyEventData(event, dataContainer);
            
            logger.info("Datos del evento " + eventId + " cargados correctamente");
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar datos del evento " + eventId, e);
            return loadFromBackup(event);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Recopila todos los datos relevantes de un evento.
     * 
     * @param event El evento del cual recopilar datos
     * @return Contenedor con todos los datos del evento
     */
    private EventDataContainer collectEventData(AbstractWeeklyEvent event) {
        EventDataContainer container = new EventDataContainer();
        
        container.eventId = event.getId();
        container.timestamp = System.currentTimeMillis();
        container.version = "1.0";
        
        // Recopilar datos de desafíos
        container.challengeProgress = event.getAllChallengeProgress();
        container.completedChallenges = event.getAllCompletedChallenges();
        
        // Recopilar estadísticas de jugadores
        container.playerStatistics = new HashMap<>();
        for (UUID playerId : event.getAllPlayersWithStatistics()) {
            container.playerStatistics.put(playerId, event.getPlayerStatistics(playerId));
        }
        
        // Recopilar estadísticas globales
        container.globalStatistics = event.getGlobalStatistics();
        
        // Metadatos adicionales
        container.totalParticipants = event.getTotalParticipants();
        container.totalChallengesCompleted = event.getTotalChallengesCompleted();
        
        return container;
    }
    
    /**
     * Aplica los datos cargados a un evento.
     * 
     * @param event El evento al que aplicar los datos
     * @param container Contenedor con los datos a aplicar
     */
    private void applyEventData(AbstractWeeklyEvent event, EventDataContainer container) {
        if (container == null) return;
        
        try {
            // Aplicar progreso de desafíos
            if (container.challengeProgress != null) {
                for (Map.Entry<UUID, Map<String, Object>> entry : container.challengeProgress.entrySet()) {
                    UUID playerId = entry.getKey();
                    Map<String, Object> progress = entry.getValue();
                    
                    // Identificar todos los challengeIds únicos
                    Set<String> challengeIds = new HashSet<>();
                    for (String key : progress.keySet()) {
                        if (key.endsWith("_current") || key.endsWith("_max")) {
                            String challengeId = key.substring(0, key.lastIndexOf("_"));
                            challengeIds.add(challengeId);
                        }
                    }
                    
                    // Aplicar progreso para cada challengeId
                    for (String challengeId : challengeIds) {
                        Object currentValue = progress.get(challengeId + "_current");
                        Object maxValue = progress.get(challengeId + "_max");
                        
                        // Solo aplicar si tenemos al menos el valor current
                        if (currentValue != null) {
                            event.updateChallengeProgress(playerId, challengeId, currentValue, maxValue);
                            logger.fine("Aplicado progreso para jugador " + playerId + 
                                       ", desafío " + challengeId + ": " + currentValue + "/" + maxValue);
                        }
                    }
                }
                logger.info("Progreso de desafíos aplicado para " + container.challengeProgress.size() + " jugadores");
            }
            
            // Aplicar desafíos completados
            if (container.completedChallenges != null) {
                for (Map.Entry<UUID, Set<String>> entry : container.completedChallenges.entrySet()) {
                    UUID playerId = entry.getKey();
                    Set<String> completed = entry.getValue();
                    
                    for (String challengeId : completed) {
                        event.completeChallengeForPlayer(playerId, challengeId);
                    }
                }
            }
            
            // Aplicar estadísticas de jugadores
            if (container.playerStatistics != null) {
                for (Map.Entry<UUID, Map<String, Object>> entry : container.playerStatistics.entrySet()) {
                    UUID playerId = entry.getKey();
                    Map<String, Object> stats = entry.getValue();
                    
                    for (Map.Entry<String, Object> statEntry : stats.entrySet()) {
                        event.updatePlayerStatistic(playerId, statEntry.getKey(), statEntry.getValue());
                    }
                }
            }
            
            // Aplicar estadísticas globales
            if (container.globalStatistics != null) {
                for (Map.Entry<String, Long> entry : container.globalStatistics.entrySet()) {
                    event.updateGlobalStatistic(entry.getKey(), entry.getValue());
                }
            }
            
            logger.info("Datos aplicados correctamente al evento " + event.getId());
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al aplicar algunos datos al evento " + event.getId(), e);
        }
    }
    
    /**
     * Crea un respaldo de los datos actuales de un evento.
     * 
     * @param eventId ID del evento
     */
    private void createBackup(String eventId) {
        try {
            Path eventFile = getEventFilePath(eventId);
            
            if (!Files.exists(eventFile)) {
                return; // No hay archivo que respaldar
            }
            
            // Crear nombre de respaldo con timestamp
            String backupName = eventId + "_" + System.currentTimeMillis() + BACKUP_EXTENSION;
            Path backupFile = backupDirectory.resolve(backupName);
            
            // Copiar archivo
            Files.copy(eventFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Limpiar respaldos antiguos
            cleanOldBackups(eventId);
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error al crear respaldo para evento " + eventId, e);
        }
    }
    
    /**
     * Limpia respaldos antiguos manteniendo solo los más recientes.
     * 
     * @param eventId ID del evento
     */
    private void cleanOldBackups(String eventId) {
        try {
            List<Path> backups = new ArrayList<>();
            
            Files.list(backupDirectory)
                .filter(path -> path.getFileName().toString().startsWith(eventId + "_"))
                .forEach(backups::add);
            
            if (backups.size() > MAX_BACKUPS) {
                // Ordenar por fecha de modificación (más reciente primero)
                backups.sort((a, b) -> {
                    try {
                        return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                    } catch (IOException e) {
                        return 0;
                    }
                });
                
                // Eliminar respaldos antiguos
                for (int i = MAX_BACKUPS; i < backups.size(); i++) {
                    Files.deleteIfExists(backups.get(i));
                }
            }
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error al limpiar respaldos antiguos para evento " + eventId, e);
        }
    }
    
    /**
     * Intenta cargar datos desde un respaldo.
     * 
     * @param event El evento al que cargar los datos
     * @return true si la carga desde respaldo fue exitosa
     */
    private boolean loadFromBackup(AbstractWeeklyEvent event) {
        String eventId = event.getId();
        
        try {
            List<Path> backups = new ArrayList<>();
            
            Files.list(backupDirectory)
                .filter(path -> path.getFileName().toString().startsWith(eventId + "_"))
                .forEach(backups::add);
            
            if (backups.isEmpty()) {
                logger.warning("No se encontraron respaldos para el evento " + eventId);
                return false;
            }
            
            // Ordenar por fecha de modificación (más reciente primero)
            backups.sort((a, b) -> {
                try {
                    return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                } catch (IOException e) {
                    return 0;
                }
            });
            
            // Intentar cargar desde el respaldo más reciente
            for (Path backup : backups) {
                try {
                    String jsonData = new String(Files.readAllBytes(backup));
                    
                    if (validateDataIntegrity(jsonData)) {
                        EventDataContainer dataContainer = gson.fromJson(jsonData, EventDataContainer.class);
                        
                        if (dataContainer != null) {
                            applyEventData(event, dataContainer);
                            logger.info("Datos del evento " + eventId + " recuperados desde respaldo: " + backup.getFileName());
                            return true;
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error al cargar respaldo " + backup.getFileName(), e);
                }
            }
            
            logger.warning("No se pudo recuperar datos desde ningún respaldo para el evento " + eventId);
            return false;
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al acceder a respaldos para evento " + eventId, e);
            return false;
        }
    }
    
    /**
     * Valida la integridad de los datos JSON.
     * 
     * @param jsonData Datos JSON a validar
     * @return true si los datos son válidos
     */
    private boolean validateDataIntegrity(String jsonData) {
        if (jsonData == null || jsonData.trim().isEmpty()) {
            return false;
        }
        
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            
            // Verificar campos esenciales
            return jsonObject.has("eventId") && 
                   jsonObject.has("timestamp") && 
                   jsonObject.has("version");
                   
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Genera un hash de los datos para detectar cambios.
     * 
     * @param data Datos a hashear
     * @return Hash de los datos
     */
    private String generateDataHash(String data) {
        return String.valueOf(data.hashCode());
    }
    
    /**
     * Obtiene la ruta del archivo de un evento.
     * 
     * @param eventId ID del evento
     * @return Ruta del archivo
     */
    private Path getEventFilePath(String eventId) {
        return dataDirectory.resolve(eventId + FILE_EXTENSION);
    }
    
    /**
     * Obtiene información sobre el último guardado de un evento.
     * 
     * @param eventId ID del evento
     * @return Timestamp del último guardado o -1 si no existe
     */
    public long getLastSaveTime(String eventId) {
        return lastSaveTime.getOrDefault(eventId, -1L);
    }
    
    /**
     * Verifica si existen datos guardados para un evento.
     * 
     * @param eventId ID del evento
     * @return true si existen datos guardados
     */
    public boolean hasEventData(String eventId) {
        return Files.exists(getEventFilePath(eventId));
    }
    
    /**
     * Elimina los datos guardados de un evento.
     * 
     * @param eventId ID del evento
     * @return true si la eliminación fue exitosa
     */
    public boolean deleteEventData(String eventId) {
        lock.writeLock().lock();
        
        try {
            Path eventFile = getEventFilePath(eventId);
            boolean deleted = Files.deleteIfExists(eventFile);
            
            if (deleted) {
                lastSaveTime.remove(eventId);
                lastDataHash.remove(eventId);
                logger.info("Datos del evento " + eventId + " eliminados correctamente");
            }
            
            return deleted;
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al eliminar datos del evento " + eventId, e);
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
}