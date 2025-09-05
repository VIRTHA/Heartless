package com.darkbladedev.testing.utils;

import com.darkbladedev.testing.mocks.MockPlayer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utilidades comunes para las pruebas del plugin Heartless.
 * Proporciona métodos helper, mocks y configuración de entorno de pruebas.
 */
public class TestUtils {
    
    @SuppressWarnings("unused")
    private static final String TEST_DATA_DIR = "test-data";
    private static final String TEMP_DIR_PREFIX = "heartless-test-";
    
    /**
     * Crea un directorio temporal para pruebas.
     * 
     * @return Path del directorio temporal creado
     * @throws IOException si no se puede crear el directorio
     */
    public static Path createTempDirectory() throws IOException {
        return Files.createTempDirectory(TEMP_DIR_PREFIX);
    }
    
    /**
     * Limpia recursivamente un directorio.
     * 
     * @param directory Directorio a limpiar
     * @throws IOException si ocurre un error durante la limpieza
     */
    public static void cleanDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }
    
    /**
     * Crea un archivo de configuración de prueba.
     * 
     * @param directory Directorio donde crear el archivo
     * @param filename Nombre del archivo
     * @param content Contenido del archivo
     * @return Path del archivo creado
     * @throws IOException si no se puede crear el archivo
     */
    public static Path createTestConfigFile(Path directory, String filename, String content) throws IOException {
        Path configFile = directory.resolve(filename);
        Files.write(configFile, content.getBytes());
        return configFile;
    }
    
    /**
     * Genera un UUID aleatorio para pruebas.
     * 
     * @return UUID aleatorio
     */
    public static UUID randomUUID() {
        return UUID.randomUUID();
    }
    
    /**
     * Genera un nombre de jugador aleatorio para pruebas.
     * 
     * @return Nombre de jugador aleatorio
     */
    public static String randomPlayerName() {
        String[] prefixes = {"Test", "Mock", "Demo", "Player", "User"};
        String[] suffixes = {"Alpha", "Beta", "Gamma", "Delta", "Epsilon"};
        
        String prefix = prefixes[ThreadLocalRandom.current().nextInt(prefixes.length)];
        String suffix = suffixes[ThreadLocalRandom.current().nextInt(suffixes.length)];
        int number = ThreadLocalRandom.current().nextInt(1000);
        
        return prefix + suffix + number;
    }
    
    /**
     * Crea múltiples jugadores mock para pruebas.
     * 
     * @param count Número de jugadores a crear
     * @return Lista de jugadores mock
     */
    public static List<MockPlayer> createMockPlayers(int count) {
        List<MockPlayer> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(new MockPlayer(randomPlayerName()));
        }
        return players;
    }
    
    /**
     * Simula un retraso en las pruebas.
     * 
     * @param milliseconds Milisegundos a esperar
     */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Verifica si un valor está dentro de un rango.
     * 
     * @param value Valor a verificar
     * @param min Valor mínimo (inclusivo)
     * @param max Valor máximo (inclusivo)
     * @return true si está dentro del rango
     */
    public static boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }
    
    /**
     * Verifica si un tiempo está dentro de un margen de error.
     * 
     * @param actualTime Tiempo actual
     * @param expectedTime Tiempo esperado
     * @param toleranceMs Tolerancia en milisegundos
     * @return true si está dentro de la tolerancia
     */
    public static boolean isTimeWithinTolerance(long actualTime, long expectedTime, long toleranceMs) {
        return Math.abs(actualTime - expectedTime) <= toleranceMs;
    }
    
    /**
     * Crea datos de configuración YAML de prueba.
     * 
     * @return Contenido YAML para pruebas
     */
    public static String createTestYamlConfig() {
        return "# Configuración de prueba\n" +
               "database:\n" +
               "  host: localhost\n" +
               "  port: 3306\n" +
               "  name: heartless_test\n" +
               "  username: test_user\n" +
               "  password: test_pass\n" +
               "\n" +
               "events:\n" +
               "  enabled: true\n" +
               "  auto-start: false\n" +
               "  save-interval: 300\n" +
               "\n" +
               "undead-week:\n" +
               "  red-moon-duration: 300\n" +
               "  infection-effects:\n" +
               "    - WEAKNESS\n" +
               "    - SLOWNESS\n" +
               "  cure-requirements:\n" +
               "    villagers: 1\n";
    }
    
    /**
     * Crea un archivo JSON de prueba con datos de evento.
     * 
     * @return Contenido JSON para pruebas
     */
    public static String createTestEventJson() {
        return "{\n" +
               "  \"eventName\": \"UndeadWeek\",\n" +
               "  \"isActive\": true,\n" +
               "  \"startTime\": " + System.currentTimeMillis() + ",\n" +
               "  \"isRedMoonActive\": false,\n" +
               "  \"redMoonStartTime\": 0,\n" +
               "  \"redMoonEndTime\": 0,\n" +
               "  \"infectedPlayersCount\": {},\n" +
               "  \"infectedPlayersTime\": {},\n" +
               "  \"curedInfections\": {},\n" +
               "  \"redMoonKills\": {},\n" +
               "  \"curedVillagers\": [],\n" +
               "  \"curedVillagersCount\": {}\n" +
               "}";
    }
    
    /**
     * Valida que una excepción contenga un mensaje específico.
     * 
     * @param exception Excepción a validar
     * @param expectedMessage Mensaje esperado
     * @return true si el mensaje coincide
     */
    public static boolean validateExceptionMessage(Exception exception, String expectedMessage) {
        return exception != null && 
               exception.getMessage() != null && 
               exception.getMessage().contains(expectedMessage);
    }
    
    /**
     * Crea un mapa de estadísticas de prueba.
     * 
     * @param curedInfections Número de infecciones curadas
     * @param curedVillagers Número de aldeanos curados
     * @param redMoonKills Número de kills en luna roja
     * @param challengeCompleted Si el desafío está completado
     * @return Mapa de estadísticas
     */
    public static Map<String, Object> createTestStatistics(int curedInfections, int curedVillagers, 
                                                          int redMoonKills, boolean challengeCompleted) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("curedInfections", curedInfections);
        stats.put("curedVillagers", curedVillagers);
        stats.put("redMoonKills", redMoonKills);
        stats.put("challengeCompleted", challengeCompleted);
        return stats;
    }
    
    /**
     * Compara dos mapas de estadísticas.
     * 
     * @param expected Estadísticas esperadas
     * @param actual Estadísticas actuales
     * @return true si son iguales
     */
    public static boolean compareStatistics(Map<String, Object> expected, Map<String, Object> actual) {
        if (expected.size() != actual.size()) {
            return false;
        }
        
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            Object expectedValue = entry.getValue();
            Object actualValue = actual.get(entry.getKey());
            
            if (!Objects.equals(expectedValue, actualValue)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Genera un conjunto de UUIDs aleatorios.
     * 
     * @param count Número de UUIDs a generar
     * @return Set de UUIDs
     */
    public static Set<UUID> generateRandomUUIDs(int count) {
        Set<UUID> uuids = new HashSet<>();
        for (int i = 0; i < count; i++) {
            uuids.add(UUID.randomUUID());
        }
        return uuids;
    }
    
    /**
     * Crea un mapa con datos aleatorios de jugadores.
     * 
     * @param playerCount Número de jugadores
     * @param maxValue Valor máximo para los datos
     * @return Mapa UUID -> Integer
     */
    public static Map<UUID, Integer> createRandomPlayerData(int playerCount, int maxValue) {
        Map<UUID, Integer> data = new HashMap<>();
        for (int i = 0; i < playerCount; i++) {
            UUID playerId = UUID.randomUUID();
            int value = ThreadLocalRandom.current().nextInt(maxValue + 1);
            data.put(playerId, value);
        }
        return data;
    }
    
    /**
     * Formatea un tiempo en milisegundos a una cadena legible.
     * 
     * @param milliseconds Tiempo en milisegundos
     * @return Cadena formateada
     */
    public static String formatTime(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2fs", milliseconds / 1000.0);
        } else {
            long seconds = milliseconds / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    /**
     * Verifica si una colección contiene elementos duplicados.
     * 
     * @param collection Colección a verificar
     * @return true si hay duplicados
     */
    public static <T> boolean hasDuplicates(Collection<T> collection) {
        Set<T> seen = new HashSet<>();
        for (T item : collection) {
            if (!seen.add(item)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Crea un estado de evento simulado para pruebas de persistencia.
     * 
     * @return Mapa con estado de evento
     */
    public static Map<String, Object> createMockEventState() {
        Map<String, Object> state = new HashMap<>();
        
        // Datos básicos del evento
        state.put("eventName", "UndeadWeek");
        state.put("isActive", true);
        state.put("startTime", System.currentTimeMillis());
        
        // Datos de luna roja
        state.put("isRedMoonActive", false);
        state.put("redMoonStartTime", 0L);
        state.put("redMoonEndTime", 0L);
        
        // Datos de jugadores
        Map<UUID, Integer> infectedPlayers = createRandomPlayerData(3, 5);
        Map<UUID, Long> infectedTimes = new HashMap<>();
        Map<UUID, Integer> curedInfections = createRandomPlayerData(2, 3);
        Map<UUID, Integer> redMoonKills = createRandomPlayerData(2, 10);
        Set<UUID> curedVillagers = generateRandomUUIDs(2);
        Map<UUID, Integer> curedVillagersCount = createRandomPlayerData(2, 5);
        
        // Generar tiempos de infección
        long currentTime = System.currentTimeMillis();
        for (UUID playerId : infectedPlayers.keySet()) {
            infectedTimes.put(playerId, currentTime - ThreadLocalRandom.current().nextLong(3600000)); // Hasta 1 hora atrás
        }
        
        state.put("infectedPlayersCount", infectedPlayers);
        state.put("infectedPlayersTime", infectedTimes);
        state.put("curedInfections", curedInfections);
        state.put("redMoonKills", redMoonKills);
        state.put("curedVillagers", curedVillagers);
        state.put("curedVillagersCount", curedVillagersCount);
        
        return state;
    }
    
    /**
     * Valida la estructura de un estado de evento.
     * 
     * @param state Estado a validar
     * @return true si la estructura es válida
     */
    public static boolean validateEventState(Map<String, Object> state) {
        String[] requiredKeys = {
            "eventName", "isActive", "startTime", "isRedMoonActive",
            "redMoonStartTime", "redMoonEndTime", "infectedPlayersCount",
            "infectedPlayersTime", "curedInfections", "redMoonKills",
            "curedVillagers", "curedVillagersCount"
        };
        
        for (String key : requiredKeys) {
            if (!state.containsKey(key)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Crea un reporte de prueba simple.
     * 
     * @param testName Nombre de la prueba
     * @param passed Si la prueba pasó
     * @param duration Duración en milisegundos
     * @param details Detalles adicionales
     * @return Cadena con el reporte
     */
    public static String createSimpleTestReport(String testName, boolean passed, long duration, String details) {
        StringBuilder report = new StringBuilder();
        report.append("=== ").append(testName).append(" ===").append("\n");
        report.append("Estado: ").append(passed ? "PASÓ" : "FALLÓ").append("\n");
        report.append("Duración: ").append(formatTime(duration)).append("\n");
        if (details != null && !details.isEmpty()) {
            report.append("Detalles: ").append(details).append("\n");
        }
        report.append("\n");
        return report.toString();
    }
    
    /**
     * Obtiene un timestamp actual formateado.
     * 
     * @return Timestamp en formato yyyy-MM-dd_HH-mm-ss
     */
    public static String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
    }
    
    /**
     * Convierte una excepción a una cadena con stack trace.
     * 
     * @param exception Excepción a convertir
     * @return Cadena con la excepción y stack trace
     */
    public static String exceptionToString(Exception exception) {
        if (exception == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(exception.getClass().getSimpleName()).append(": ").append(exception.getMessage()).append("\n");
        
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        
        return sb.toString();
    }
}