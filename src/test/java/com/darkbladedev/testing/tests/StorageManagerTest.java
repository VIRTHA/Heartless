package com.darkbladedev.testing.tests;

import com.darkbladedev.testing.TestCase;
import com.darkbladedev.managers.StorageManager;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.HeartlessMain;
import com.google.gson.JsonObject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Casos de prueba para la clase StorageManager.
 * Verifica la funcionalidad de guardado, carga y persistencia de datos.
 */
public class StorageManagerTest extends TestCase {
    
    private StorageManager storageManager;
    private Path testDataDirectory;
    private Path testConfigFile;
    private WeeklyEventManager mockEventManager;
    private UndeadWeek mockUndeadWeek;
    private HeartlessMain mockPlugin;
    
    public StorageManagerTest() {
        super("StorageManagerTest");
    }
    
    @Override
    public void setUp() throws Exception {
        // Crear directorio temporal para pruebas
        testDataDirectory = Paths.get("test-data-" + System.currentTimeMillis());
        Files.createDirectories(testDataDirectory);
        
        testConfigFile = testDataDirectory.resolve("test-config.json");
        
        // Crear mocks básicos
        mockEventManager = createMockEventManager();
        mockUndeadWeek = createMockUndeadWeek();
        mockPlugin = createMockPlugin();
        
        // Inicializar StorageManager con configuración de prueba
        storageManager = new StorageManager(mockPlugin);
    }
    
    @Override
    public void tearDown() throws Exception {
        // Limpiar archivos de prueba
        if (Files.exists(testDataDirectory)) {
            deleteDirectory(testDataDirectory.toFile());
        }
    }
    
    @Override
    public void runTest() throws Exception {
        testBasicSaveAndLoad();
        testEventDataPersistence();
        testUndeadWeekDataPersistence();
        testCuredVillagersCountPersistence();
        testCorruptedDataHandling();
        testConcurrentAccess();
        testBackupFunctionality();
    }
    
    /**
     * Prueba básica de guardado y carga de configuración.
     */
    private void testBasicSaveAndLoad() throws Exception {
        // Crear datos de prueba
        JsonObject testData = new JsonObject();
        testData.addProperty("testKey", "testValue");
        testData.addProperty("testNumber", 42);
        testData.addProperty("testBoolean", true);
        
        // Guardar datos
        storageManager.saveConfig(testData, testConfigFile.toString());
        
        // Verificar que el archivo se creó
        assertTrue(Files.exists(testConfigFile), "El archivo de configuración debe existir después de guardar");
        
        // Cargar datos
        JsonObject loadedData = storageManager.loadConfig(testConfigFile.toString());
        
        // Verificar datos cargados
        assertNotNull(loadedData, "LoadedData no debe ser null");
        assertEquals("testValue", loadedData.get("testKey").getAsString(), "El valor de testKey debe ser testValue");
        assertEquals(Integer.valueOf(42), Integer.valueOf(loadedData.get("testNumber").getAsInt()), "El número de prueba debe ser 42");
        assertTrue(loadedData.get("testBoolean").getAsBoolean(), "El valor booleano debe ser true");
    }
    
    /**
     * Prueba la persistencia de datos generales del evento.
     */
    private void testEventDataPersistence() throws Exception {
        // Configurar datos del evento
        mockEventManager.setCurrentEvent(mockUndeadWeek);
        
        // Simular datos del evento
        Map<UUID, Integer> testInfectedPlayers = new HashMap<>();
        UUID testPlayer1 = UUID.randomUUID();
        UUID testPlayer2 = UUID.randomUUID();
        testInfectedPlayers.put(testPlayer1, 5);
        testInfectedPlayers.put(testPlayer2, 3);
        
        // Configurar el mock con datos
        // Simular jugadores infectados directamente en el mapa
        Map<UUID, Boolean> infectedPlayersMap = new HashMap<>();
        for (UUID playerId : testInfectedPlayers.keySet()) {
            infectedPlayersMap.put(playerId, true);
        }
        // Establecer jugadores infectados usando el método setter
        mockUndeadWeek.setInfectedPlayers(infectedPlayersMap);
        mockUndeadWeek.setRedMoonActive(true);
        mockUndeadWeek.setRedMoonStartTime(System.currentTimeMillis());
        
        // Guardar datos del evento
        storageManager.saveEventData(mockEventManager);
        
        // Crear nuevo evento para cargar datos
        UndeadWeek newUndeadWeek = createMockUndeadWeek();
        WeeklyEventManager newEventManager = createMockEventManager();
        newEventManager.setCurrentEvent(newUndeadWeek);
        
        // Cargar datos
        storageManager.loadEventData(newEventManager);
        
        // Verificar que los datos se cargaron correctamente
        Map<UUID, Boolean> loadedInfectedPlayers = newUndeadWeek.getInfectedPlayers();
        assertNotNull(loadedInfectedPlayers, "LoadedInfectedPlayers no debe ser null");
        assertEquals(Integer.valueOf(2), Integer.valueOf(loadedInfectedPlayers.size()), "Debe haber 2 jugadores infectados cargados");
        assertTrue(loadedInfectedPlayers.containsKey(testPlayer1), "Los jugadores infectados cargados deben contener al jugador de prueba 1");
        
        assertTrue(newUndeadWeek.isRedMoonActive(), "La luna roja debe estar activa después de cargar el estado");
        assertTrue(newUndeadWeek.getRedMoonStartTime() > 0, "El tiempo de inicio de luna roja debe ser mayor a 0 después de cargar");
    }
    
    /**
     * Prueba específica para la persistencia del contador de aldeanos curados.
     */
    private void testCuredVillagersCountPersistence() throws Exception {
        // Configurar datos de aldeanos curados
        Map<UUID, Integer> testCuredVillagers = new HashMap<>();
        UUID testPlayer1 = UUID.randomUUID();
        UUID testPlayer2 = UUID.randomUUID();
        UUID testPlayer3 = UUID.randomUUID();
        
        testCuredVillagers.put(testPlayer1, 3);
        testCuredVillagers.put(testPlayer2, 7);
        testCuredVillagers.put(testPlayer3, 1);
        
        // Configurar el mock
        mockUndeadWeek.setCuredVillagersCount(testCuredVillagers);
        mockEventManager.setCurrentEvent(mockUndeadWeek);
        
        // Guardar datos
        storageManager.saveEventData(mockEventManager);
        
        // Crear nuevo evento para cargar
        UndeadWeek newUndeadWeek = createMockUndeadWeek();
        WeeklyEventManager newEventManager = createMockEventManager();
        newEventManager.setCurrentEvent(newUndeadWeek);
        
        // Cargar datos
        storageManager.loadEventData(newEventManager);
        
        // Verificar datos cargados
        Map<UUID, Integer> loadedCuredVillagers = newUndeadWeek.getCuredVillagersCount();
        assertNotNull(loadedCuredVillagers, "LoadedCuredVillagers no debe ser null");
        assertEquals(Integer.valueOf(3), Integer.valueOf(loadedCuredVillagers.size()), "Debe haber 3 jugadores con aldeanos curados");
        
        assertEquals(Integer.valueOf(3), loadedCuredVillagers.get(testPlayer1), "El primer jugador debe tener 3 aldeanos curados");
        assertEquals(Integer.valueOf(7), loadedCuredVillagers.get(testPlayer2), "El segundo jugador debe tener 7 aldeanos curados");
        assertEquals(Integer.valueOf(1), loadedCuredVillagers.get(testPlayer3), "El tercer jugador debe tener 1 aldeano curado");
        
        // Verificar que el total se calcula correctamente
        int expectedTotal = 3 + 7 + 1;
        int actualTotal = loadedCuredVillagers.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(Integer.valueOf(expectedTotal), Integer.valueOf(actualTotal), "El total de aldeanos curados debe coincidir");
    }
    
    /**
     * Prueba el manejo de datos corruptos o inválidos.
     */
    private void testCorruptedDataHandling() throws Exception {
        // Crear archivo con JSON inválido
        Path corruptedFile = testDataDirectory.resolve("corrupted.json");
        Files.write(corruptedFile, "{ invalid json content".getBytes());
        
        // Intentar cargar datos corruptos
        @SuppressWarnings("unused")
        JsonObject result = storageManager.loadConfig(corruptedFile.toString());
        
        // Debe manejar graciosamente los datos corruptos
        // (dependiendo de la implementación, podría retornar null o un objeto vacío)
        // Esta prueba verifica que no se lance una excepción no manejada
        
        // Crear archivo vacío
        Path emptyFile = testDataDirectory.resolve("empty.json");
        Files.createFile(emptyFile);
        
        @SuppressWarnings("unused")
        JsonObject emptyResult = storageManager.loadConfig(emptyFile.toString());
        // Verificar que maneja archivos vacíos correctamente
    }
    
    /**
     * Prueba el acceso concurrente al sistema de almacenamiento.
     */
    private void testConcurrentAccess() throws Exception {
        final int threadCount = 5;
        final int operationsPerThread = 10;
        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
        
        // Crear múltiples hilos que accedan al storage simultáneamente
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        // Operaciones de guardado y carga concurrentes
                        JsonObject data = new JsonObject();
                        data.addProperty("thread", threadId);
                        data.addProperty("operation", j);
                        data.addProperty("timestamp", System.currentTimeMillis());
                        
                        Path threadFile = testDataDirectory.resolve("thread-" + threadId + "-" + j + ".json");
                        storageManager.saveConfig(data, threadFile.toString());
                        
                        JsonObject loaded = storageManager.loadConfig(threadFile.toString());
                        assertNotNull(loaded, "Loaded no debe ser null");
                        assertEquals(Integer.valueOf(threadId), Integer.valueOf(loaded.get("thread").getAsInt()), "El ID del hilo debe coincidir");
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                }
            });
            threads.add(thread);
        }
        
        // Ejecutar todos los hilos
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Esperar a que terminen
        for (Thread thread : threads) {
            thread.join(5000); // Timeout de 5 segundos
        }
        
        // Verificar que no hubo excepciones
        if (!exceptions.isEmpty()) {
            throw new AssertionError("Se produjeron excepciones durante el acceso concurrente: " + exceptions.get(0).getMessage());
        }
    }
    
    /**
     * Prueba la funcionalidad de backup si está implementada.
     */
    private void testBackupFunctionality() throws Exception {
        // Crear datos de prueba
        JsonObject originalData = new JsonObject();
        originalData.addProperty("important", "data");
        originalData.addProperty("timestamp", System.currentTimeMillis());
        
        Path originalFile = testDataDirectory.resolve("original.json");
        storageManager.saveConfig(originalData, originalFile.toString());
        
        // Si el StorageManager tiene funcionalidad de backup, probarla aquí
        // Por ejemplo:
        // storageManager.createBackup(originalFile.toString());
        // Path backupFile = Paths.get(originalFile.toString() + ".backup");
        // assertTrue(Files.exists(backupFile));
        
        // Esta prueba se puede expandir según la implementación específica del backup
    }
    
    /**
     * Prueba la persistencia completa de datos de UndeadWeek.
     */
    private void testUndeadWeekDataPersistence() throws Exception {
        // Configurar todos los datos posibles de UndeadWeek
        Map<UUID, Long> infectedPlayersTime = new HashMap<>();
        Map<UUID, Integer> curedInfections = new HashMap<>();
        Map<UUID, Integer> redMoonKills = new HashMap<>();
        Set<UUID> curedVillagers = new HashSet<>();
        Map<UUID, Integer> curedVillagersCount = new HashMap<>();
        
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        // Configurar datos de prueba
        infectedPlayersTime.put(player1, System.currentTimeMillis());
        infectedPlayersTime.put(player2, System.currentTimeMillis() - 10000);
        
        curedInfections.put(player1, 2);
        curedInfections.put(player2, 5);
        
        redMoonKills.put(player1, 10);
        redMoonKills.put(player2, 15);
        
        curedVillagers.add(player1);
        curedVillagers.add(player2);
        
        curedVillagersCount.put(player1, 3);
        curedVillagersCount.put(player2, 8);
        
        // Configurar el mock
        mockUndeadWeek.setInfectedPlayersTime(infectedPlayersTime);
        mockUndeadWeek.setCuredInfections(curedInfections);
        mockUndeadWeek.setRedMoonKills(redMoonKills);
        mockUndeadWeek.setCuredVillagers(curedVillagers);
        mockUndeadWeek.setCuredVillagersCount(curedVillagersCount);
        mockUndeadWeek.setRedMoonEndTime(System.currentTimeMillis() + 60000);
        
        mockEventManager.setCurrentEvent(mockUndeadWeek);
        
        // Guardar y cargar
        storageManager.saveEventData(mockEventManager);
        
        UndeadWeek newUndeadWeek = createMockUndeadWeek();
        WeeklyEventManager newEventManager = createMockEventManager();
        newEventManager.setCurrentEvent(newUndeadWeek);
        
        storageManager.loadEventData(newEventManager);
        
        // Verificar todos los datos
        assertNotNull(newUndeadWeek.getInfectedPlayersTime(), "Los tiempos de infección no deben ser null después de cargar");
        assertNotNull(newUndeadWeek.getCuredInfections(), "Las infecciones curadas no deben ser null después de cargar");
        assertNotNull(newUndeadWeek.getRedMoonKills(), "Los kills de luna roja no deben ser null después de cargar");
        assertNotNull(newUndeadWeek.getCuredVillagers(), "Los aldeanos curados no deben ser null después de cargar");
        assertNotNull(newUndeadWeek.getCuredVillagersCount(), "El conteo de aldeanos curados no debe ser null después de cargar");
        
        assertEquals(Integer.valueOf(2), Integer.valueOf(newUndeadWeek.getInfectedPlayersTime().size()), "Debe haber 2 entradas en tiempos de infección");
        assertEquals(Integer.valueOf(2), Integer.valueOf(newUndeadWeek.getCuredInfections().size()), "Debe haber 2 entradas en infecciones curadas");
        assertEquals(Integer.valueOf(2), Integer.valueOf(newUndeadWeek.getRedMoonKills().size()), "Debe haber 2 entradas en kills de luna roja");
        assertEquals(Integer.valueOf(2), Integer.valueOf(newUndeadWeek.getCuredVillagers().size()), "Debe haber 2 entradas en aldeanos curados");
        assertEquals(Integer.valueOf(2), Integer.valueOf(newUndeadWeek.getCuredVillagersCount().size()), "Debe haber 2 entradas en conteo de aldeanos curados");
        
        assertTrue(newUndeadWeek.getRedMoonEndTime() > System.currentTimeMillis(), "El tiempo de fin de luna roja debe ser mayor al tiempo actual después de cargar");
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    /**
     * Crea un mock básico de WeeklyEventManager.
     */
    private WeeklyEventManager createMockEventManager() {
        // En una implementación real, esto sería un mock más sofisticado
        // Por ahora, creamos una instancia básica para pruebas
        return new WeeklyEventManager(null) {
            private WeeklyEvent currentEvent;
            
            @Override
            public void setCurrentEvent(WeeklyEvent event) {
                this.currentEvent = event;
            }
            
            @Override
            public WeeklyEvent getCurrentEvent() {
                return currentEvent;
            }
        };
    }
    
    /**
     * Crea un mock básico de UndeadWeek.
     */
    private UndeadWeek createMockUndeadWeek() {
        // En una implementación real, esto sería un mock más sofisticado
        return new UndeadWeek(null, null) {
            @SuppressWarnings("unused")
            private Map<UUID, Integer> infectedPlayersCount = new HashMap<>();
            private Map<UUID, Long> infectedPlayersTime = new HashMap<>();
            private Map<UUID, Integer> curedInfections = new HashMap<>();
            private Map<UUID, Integer> redMoonKills = new HashMap<>();
            private Set<UUID> curedVillagers = new HashSet<>();
            private Map<UUID, Integer> curedVillagersCount = new HashMap<>();
            private boolean redMoonActive = false;
            private long redMoonStartTime = 0;
            private long redMoonEndTime = 0;
            
            // Implementar métodos necesarios para las pruebas
            @Override
            public Map<UUID, Long> getInfectedPlayersTime() { return infectedPlayersTime; }
            @Override
            public void setInfectedPlayersTime(Map<UUID, Long> time) { this.infectedPlayersTime = time; }
            
            @Override
            public Map<UUID, Integer> getCuredInfections() { return curedInfections; }
            @Override
            public void setCuredInfections(Map<UUID, Integer> cured) { this.curedInfections = cured; }
            
            @Override
            public Map<UUID, Integer> getRedMoonKills() { return redMoonKills; }
            @Override
            public void setRedMoonKills(Map<UUID, Integer> kills) { this.redMoonKills = kills; }
            
            @Override
            public Set<UUID> getCuredVillagers() { return curedVillagers; }
            @Override
            public void setCuredVillagers(Set<UUID> cured) { this.curedVillagers = cured; }
            
            @Override
            public Map<UUID, Integer> getCuredVillagersCount() { return curedVillagersCount; }
            @Override
            public void setCuredVillagersCount(Map<UUID, Integer> count) { this.curedVillagersCount = count; }
            
            @Override
            public boolean isRedMoonActive() { return redMoonActive; }
            @Override
            public void setRedMoonActive(boolean active) { this.redMoonActive = active; }
            
            @Override
            public long getRedMoonStartTime() { return redMoonStartTime; }
            @Override
            public void setRedMoonStartTime(long time) { this.redMoonStartTime = time; }
            
            @Override
            public long getRedMoonEndTime() { return redMoonEndTime; }
            @Override
            public void setRedMoonEndTime(long time) { this.redMoonEndTime = time; }
        };
    }
    
    /**
     * Elimina recursivamente un directorio y su contenido.
     */
    private HeartlessMain createMockPlugin() {
        return new HeartlessMain() {
            @Override
            public java.util.logging.Logger getLogger() {
                return java.util.logging.Logger.getLogger("TestLogger");
            }
        };
    }
    
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}