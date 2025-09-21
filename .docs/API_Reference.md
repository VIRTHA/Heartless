# API Reference - Sistema de Estadísticas de Jugadores

## Tabla de Contenidos
1. [PlayerStatisticsReportManager](#playerstatisticsreportmanager)
2. [PlayerStatisticsValidator](#playerstatisticsvalidator)
3. [PerformanceOptimizer](#performanceoptimizer)
4. [AbstractWeeklyEvent](#abstractweeklyevent)
5. [Clases de Datos](#clases-de-datos)
6. [Interfaces y Callbacks](#interfaces-y-callbacks)

---

## PlayerStatisticsReportManager

### Descripción
Gestor central para la generación y envío de reportes de estadísticas de jugadores.

### Constructor
```java
public PlayerStatisticsReportManager(JavaPlugin plugin)
```

### Métodos Principales

#### generateAndSendFinalReports
```java
public void generateAndSendFinalReports(String eventName, Map<UUID, Map<String, Object>> allPlayerStats, Map<UUID, List<String>> completedChallenges, Map<UUID, Map<String, Object>> challengeProgress)
```
**Descripción**: Genera y envía reportes finales para todos los jugadores de un evento.

**Parámetros**:
- `eventName`: Nombre del evento
- `allPlayerStats`: Mapa de estadísticas por jugador
- `completedChallenges`: Desafíos completados por jugador
- `challengeProgress`: Progreso de desafíos por jugador

**Ejemplo**:
```java
reportManager.generateAndSendFinalReports(
    "explosive_week_2024",
    playerStats,
    completedChallenges,
    challengeProgress
);
```

#### generatePlayerReport
```java
public PlayerEventReport generatePlayerReport(UUID playerId, String eventName, Map<String, Object> playerStats, List<String> completedChallenges, Map<String, Object> challengeProgress)
```
**Descripción**: Genera un reporte individual para un jugador específico.

**Retorna**: `PlayerEventReport` o `null` si no hay datos suficientes

#### sendIndividualReport
```java
public void sendIndividualReport(Player player, PlayerEventReport report)
```
**Descripción**: Envía un reporte individual a un jugador.

**Parámetros**:
- `player`: Jugador destinatario
- `report`: Reporte a enviar

#### cleanupOldReports
```java
public void cleanupOldReports(long maxAge)
```
**Descripción**: Limpia reportes antiguos del caché.

**Parámetros**:
- `maxAge`: Edad máxima en milisegundos (0 para limpiar todo)

---

## PlayerStatisticsValidator

### Descripción
Validador de datos de estadísticas con detección de anomalías.

### Constructor
```java
public PlayerStatisticsValidator()
```

### Métodos Principales

#### validatePlayerStatistics
```java
public ValidationResult validatePlayerStatistics(Map<String, Object> stats)
```
**Descripción**: Valida un conjunto de estadísticas de jugador.

**Retorna**: `ValidationResult` con estado de validación y errores

**Ejemplo**:
```java
ValidationResult result = validator.validatePlayerStatistics(playerStats);
if (!result.isValid()) {
    logger.warning("Errores de validación: " + result.getErrors());
}
```

#### validateCompletedChallenges
```java
public ValidationResult validateCompletedChallenges(List<String> challenges)
```
**Descripción**: Valida una lista de desafíos completados.

#### validateChallengeProgress
```java
public ValidationResult validateChallengeProgress(Map<String, Object> progress)
```
**Descripción**: Valida el progreso de desafíos.

#### detectAnomalies
```java
public List<String> detectAnomalies(Map<String, Object> stats)
```
**Descripción**: Detecta anomalías en las estadísticas.

**Retorna**: Lista de descripciones de anomalías encontradas

#### isValidStatValue
```java
public boolean isValidStatValue(String statName, Object value)
```
**Descripción**: Verifica si un valor de estadística es válido.

---

## PerformanceOptimizer

### Descripción
Optimizador de rendimiento para procesamiento por lotes y operaciones asíncronas.

### Constructor
```java
public PerformanceOptimizer(JavaPlugin plugin)
```

### Métodos Principales

#### processPlayersAsync
```java
public <T> void processPlayersAsync(List<UUID> playerIds, Function<List<UUID>, List<T>> processor, Consumer<List<T>> callback)
```
**Descripción**: Procesa jugadores de forma asíncrona en lotes.

**Parámetros**:
- `playerIds`: Lista de IDs de jugadores
- `processor`: Función para procesar cada lote
- `callback`: Callback ejecutado al completar todos los lotes

**Ejemplo**:
```java
optimizer.processPlayersAsync(
    playerIds,
    batch -> processBatch(batch),
    results -> handleAllResults(results)
);
```

#### processPlayersSync
```java
public <T> List<T> processPlayersSync(List<UUID> playerIds, Function<List<UUID>, List<T>> processor)
```
**Descripción**: Procesa jugadores de forma síncrona en lotes.

#### monitorMemoryUsage
```java
public void monitorMemoryUsage()
```
**Descripción**: Monitorea el uso de memoria y ejecuta limpieza si es necesario.

#### getPerformanceMetrics
```java
public PerformanceMetrics getPerformanceMetrics()
```
**Descripción**: Obtiene métricas de rendimiento actuales.

#### cleanup
```java
public void cleanup()
```
**Descripción**: Limpia recursos y cancela tareas programadas.

---

## AbstractWeeklyEvent

### Descripción
Clase base extendida con capacidades de reporte de estadísticas.

### Métodos Extendidos

#### processFinalStatistics
```java
protected void processFinalStatistics()
```
**Descripción**: Procesa estadísticas finales y genera reportes automáticamente.

#### notifyPlayerChallengeCompleted
```java
protected void notifyPlayerChallengeCompleted(Player player, String challengeId)
```
**Descripción**: Notifica a un jugador sobre un desafío completado con información de hover.

#### addPlayerStatistic
```java
protected void addPlayerStatistic(UUID playerId, String statName, Object value)
```
**Descripción**: Agrega una estadística para un jugador.

#### getPlayerStatistics
```java
protected Map<String, Object> getPlayerStatistics(UUID playerId)
```
**Descripción**: Obtiene todas las estadísticas de un jugador.

---

## Clases de Datos

### PlayerEventReport
```java
public static class PlayerEventReport {
    private final UUID playerId;
    private final String eventName;
    private final Component reportMessage;
    private final long timestamp;
    
    // Constructores y getters
}
```

### ValidationResult
```java
public class ValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    
    public boolean isValid() { return valid; }
    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }
}
```

### PerformanceMetrics
```java
public class PerformanceMetrics {
    private final long totalProcessingTime;
    private final int totalPlayersProcessed;
    private final double averageProcessingTime;
    private final long memoryUsed;
    private final int batchesProcessed;
    
    // Getters y métodos de cálculo
}
```

---

## Interfaces y Callbacks

### BatchProcessor
```java
@FunctionalInterface
public interface BatchProcessor<T, R> {
    List<R> process(List<T> batch);
}
```

### ResultCallback
```java
@FunctionalInterface
public interface ResultCallback<T> {
    void onComplete(List<T> results);
}
```

### StatisticsProvider
```java
public interface StatisticsProvider {
    Map<String, Object> getStatistics(UUID playerId);
    void addStatistic(UUID playerId, String name, Object value);
    void clearStatistics(UUID playerId);
}
```

---

## Constantes y Configuración

### Límites de Validación
```java
public static final int MAX_STAT_VALUE = 1000000;
public static final int MIN_SESSION_TIME = 1000;
public static final double ANOMALY_THRESHOLD = 3.0;
```

### Configuración de Rendimiento
```java
public static final int DEFAULT_BATCH_SIZE = 50;
public static final int MAX_CONCURRENT_BATCHES = 4;
public static final long MEMORY_THRESHOLD_MB = 512;
```

### Configuración de Caché
```java
public static final long CACHE_CLEANUP_INTERVAL = 300000; // 5 minutos
public static final long MAX_REPORT_AGE = 3600000; // 1 hora
```

---

## Excepciones

### StatisticsValidationException
```java
public class StatisticsValidationException extends Exception {
    public StatisticsValidationException(String message);
    public StatisticsValidationException(String message, Throwable cause);
}
```

### PerformanceOptimizationException
```java
public class PerformanceOptimizationException extends RuntimeException {
    public PerformanceOptimizationException(String message);
    public PerformanceOptimizationException(String message, Throwable cause);
}
```

---

## Ejemplos de Uso Avanzado

### Implementación de Validador Personalizado
```java
public class CustomStatisticsValidator extends PlayerStatisticsValidator {
    
    @Override
    public ValidationResult validatePlayerStatistics(Map<String, Object> stats) {
        ValidationResult baseResult = super.validatePlayerStatistics(stats);
        
        List<String> customErrors = new ArrayList<>(baseResult.getErrors());
        List<String> customWarnings = new ArrayList<>(baseResult.getWarnings());
        
        // Validación personalizada
        if (stats.containsKey("custom_kills")) {
            Object value = stats.get("custom_kills");
            if (!(value instanceof Number) || ((Number) value).intValue() < 0) {
                customErrors.add("custom_kills debe ser un número positivo");
            }
        }
        
        return new ValidationResult(
            baseResult.isValid() && customErrors.isEmpty(),
            customErrors,
            customWarnings
        );
    }
}
```

### Procesamiento Personalizado con PerformanceOptimizer
```java
public class CustomEventProcessor {
    private final PerformanceOptimizer optimizer;
    
    public void processCustomEvent(List<UUID> participants) {
        optimizer.processPlayersAsync(
            participants,
            this::processPlayerBatch,
            this::handleBatchResults
        );
    }
    
    private List<CustomResult> processPlayerBatch(List<UUID> batch) {
        return batch.stream()
            .map(this::processIndividualPlayer)
            .collect(Collectors.toList());
    }
    
    private void handleBatchResults(List<CustomResult> allResults) {
        // Procesar todos los resultados
        allResults.forEach(this::handleResult);
    }
}
```

---

**Versión de API**: 1.0.0  
**Compatibilidad**: Spigot/Paper 1.20+  
**Java**: 21+