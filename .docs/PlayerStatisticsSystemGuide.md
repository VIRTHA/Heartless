# Sistema de Estadísticas de Jugadores - Guía Completa

## Descripción General

El Sistema de Estadísticas de Jugadores es un componente avanzado del plugin Heartless que proporciona captura, validación, procesamiento y reporte de estadísticas detalladas durante los eventos semanales. Este sistema está diseñado para manejar alto volumen de jugadores con optimizaciones de rendimiento y validación robusta de datos.

## Arquitectura del Sistema

### Componentes Principales

1. **PlayerStatisticsReportManager** - Gestor central de reportes
2. **PlayerStatisticsValidator** - Validador de datos y detector de anomalías
3. **PerformanceOptimizer** - Optimizador de rendimiento para procesamiento por lotes
4. **AbstractWeeklyEvent** - Clase base extendida con capacidades de reporte

### Flujo de Datos

```
Evento Semanal → Captura de Estadísticas → Validación → Procesamiento por Lotes → Generación de Reportes → Envío a Jugadores
```

## Características Principales

### 1. Captura Automática de Estadísticas
- **Estadísticas de jugador**: Kills, muertes, tiempo de supervivencia, bloques rotos, etc.
- **Progreso de desafíos**: Seguimiento en tiempo real del progreso hacia objetivos
- **Desafíos completados**: Registro automático de logros alcanzados

### 2. Validación Robusta de Datos
- **Validación de rangos**: Verificación de valores dentro de límites razonables
- **Detección de anomalías**: Identificación de patrones sospechosos o valores extremos
- **Validación de consistencia**: Verificación de coherencia entre estadísticas relacionadas

### 3. Optimización de Rendimiento
- **Procesamiento por lotes**: Manejo eficiente de grandes volúmenes de jugadores
- **Operaciones asíncronas**: Evita bloqueos del hilo principal del servidor
- **Gestión de memoria**: Limpieza automática de cachés y datos antiguos
- **Monitoreo de recursos**: Seguimiento del uso de CPU y memoria

### 4. Reportes Personalizados
- **Formato MiniMessage**: Mensajes ricos con colores y efectos
- **Hover text**: Información adicional al pasar el cursor
- **Reportes individuales**: Estadísticas personalizadas por jugador
- **Cacheo inteligente**: Evita regeneración innecesaria de reportes

## Configuración e Instalación

### Dependencias Requeridas
```xml
<!-- En build.gradle -->
dependencies {
    implementation 'net.kyori:adventure-api:4.14.0'
    implementation 'net.kyori:adventure-text-minimessage:4.14.0'
    implementation 'org.junit.jupiter:junit-jupiter:5.9.2'
    testImplementation 'org.mockito:mockito-core:4.11.0'
}
```

### Inicialización en el Plugin Principal
```java
public class HeartlessMain extends JavaPlugin {
    private PlayerStatisticsReportManager reportManager;
    
    @Override
    public void onEnable() {
        // Inicializar el gestor de reportes
        this.reportManager = new PlayerStatisticsReportManager(this);
        
        // Configurar eventos semanales con el sistema de reportes
        // Los eventos automáticamente utilizarán el sistema
    }
}
```

## Uso del Sistema

### 1. En Eventos Semanales

Los eventos que extienden `AbstractWeeklyEvent` automáticamente tienen acceso al sistema:

```java
public class MiEventoPersonalizado extends AbstractWeeklyEvent {
    
    @Override
    public void processFinalStatistics() {
        // El sistema automáticamente:
        // 1. Procesa estadísticas finales
        // 2. Genera reportes individuales
        // 3. Envía reportes a jugadores online
        super.processFinalStatistics();
    }
    
    // Notificaciones mejoradas con hover text
    @Override
    protected void notifyPlayerChallengeCompleted(Player player, String challengeId) {
        // Automáticamente incluye información de progreso en hover
        super.notifyPlayerChallengeCompleted(player, challengeId);
    }
}
```

### 2. Validación Manual de Datos

```java
PlayerStatisticsValidator validator = new PlayerStatisticsValidator();

// Validar estadísticas de jugador
Map<String, Object> playerStats = getPlayerStatistics(playerId);
ValidationResult result = validator.validatePlayerStatistics(playerStats);

if (!result.isValid()) {
    logger.warning("Estadísticas inválidas: " + result.getErrors());
}

// Detectar anomalías
List<String> anomalies = validator.detectAnomalies(playerStats);
if (!anomalies.isEmpty()) {
    logger.info("Anomalías detectadas: " + anomalies);
}
```

### 3. Procesamiento Optimizado

```java
PerformanceOptimizer optimizer = new PerformanceOptimizer(plugin);

// Procesamiento asíncrono por lotes
optimizer.processPlayersAsync(
    playerIds,
    (batch) -> {
        // Procesar lote de jugadores
        return processBatch(batch);
    },
    (allResults) -> {
        // Callback al completar todos los lotes
        handleResults(allResults);
    }
);
```

## Configuración Avanzada

### Parámetros de Validación

```java
// En PlayerStatisticsValidator
private static final int MAX_STAT_VALUE = 1000000;
private static final int MIN_SESSION_TIME = 1000;
private static final double ANOMALY_THRESHOLD = 3.0; // Desviaciones estándar
```

### Configuración de Rendimiento

```java
// En PerformanceOptimizer
private static final int DEFAULT_BATCH_SIZE = 50;
private static final int MAX_CONCURRENT_BATCHES = 4;
private static final long MEMORY_THRESHOLD_MB = 512;
```

### Configuración de Caché

```java
// En PlayerStatisticsReportManager
private static final long CACHE_CLEANUP_INTERVAL = 300000; // 5 minutos
private static final long MAX_REPORT_AGE = 3600000; // 1 hora
```

## Monitoreo y Métricas

### Logs del Sistema

El sistema genera logs detallados para monitoreo:

```
[PlayerStatisticsReportManager] Generando reportes finales para evento: explosive_week_2024
[PlayerStatisticsReportManager] Reportes generados y enviados para 150 jugadores
[PerformanceOptimizer] Procesando 150 jugadores en 3 lotes
[PlayerStatisticsValidator] Detectadas 2 anomalías en estadísticas de jugador
```

### Métricas de Rendimiento

- **Throughput**: Jugadores procesados por segundo
- **Latencia**: Tiempo de procesamiento por lote
- **Uso de memoria**: Memoria utilizada durante el procesamiento
- **Tasa de errores**: Porcentaje de validaciones fallidas

## Pruebas y Validación

### Pruebas de Integración

```bash
# Ejecutar pruebas completas del sistema
./gradlew test --tests "StatisticsSystemIntegrationTest"

# Ejecutar benchmarks de rendimiento
./gradlew test --tests "PerformanceBenchmarkTest"
```

### Pruebas de Carga

El sistema incluye pruebas automatizadas para:
- **10-1000 jugadores**: Verificación de escalabilidad
- **Procesamiento concurrente**: Múltiples eventos simultáneos
- **Validación masiva**: Miles de conjuntos de datos
- **Limpieza de memoria**: Gestión eficiente de recursos

## Solución de Problemas

### Problemas Comunes

1. **Alto uso de memoria**
   - Verificar configuración de lotes
   - Ejecutar limpieza de caché más frecuente
   - Reducir tamaño de lote si es necesario

2. **Procesamiento lento**
   - Verificar carga del servidor
   - Ajustar número de lotes concurrentes
   - Revisar logs de anomalías

3. **Reportes no enviados**
   - Verificar que los jugadores estén online
   - Comprobar permisos de mensajería
   - Revisar logs de errores

### Comandos de Diagnóstico

```java
// Verificar estado del caché
reportManager.getCachedReportCount();

// Limpiar caché manualmente
reportManager.cleanupOldReports(0);

// Verificar métricas de rendimiento
performanceOptimizer.getPerformanceMetrics();
```

## Extensibilidad

### Agregar Nuevas Estadísticas

```java
// En tu evento personalizado
@EventHandler
public void onCustomEvent(CustomEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    
    // Agregar nueva estadística
    addPlayerStatistic(playerId, "custom_stat", event.getValue());
    
    // El sistema automáticamente la incluirá en reportes
}
```

### Validadores Personalizados

```java
public class CustomValidator extends PlayerStatisticsValidator {
    
    @Override
    public ValidationResult validatePlayerStatistics(Map<String, Object> stats) {
        ValidationResult baseResult = super.validatePlayerStatistics(stats);
        
        // Agregar validaciones personalizadas
        if (stats.containsKey("custom_stat")) {
            // Validar estadística personalizada
        }
        
        return baseResult;
    }
}
```

## Mejores Prácticas

1. **Captura de Datos**
   - Capturar estadísticas en tiempo real
   - Usar tipos de datos apropiados
   - Evitar duplicación de datos

2. **Rendimiento**
   - Procesar en lotes para alto volumen
   - Usar operaciones asíncronas cuando sea posible
   - Limpiar cachés regularmente

3. **Validación**
   - Validar datos antes de procesamiento
   - Manejar anomalías apropiadamente
   - Registrar errores para análisis

4. **Monitoreo**
   - Revisar logs regularmente
   - Monitorear métricas de rendimiento
   - Configurar alertas para problemas críticos

## Changelog y Versiones

### Versión 1.0.0
- Implementación inicial del sistema
- Captura automática de estadísticas
- Validación robusta de datos
- Optimización de rendimiento
- Reportes personalizados con MiniMessage
- Pruebas de integración completas

---

**Autor**: DarkBladeDev  
**Versión**: 1.0.0  
**Fecha**: 2025
**Plugin**: Heartless Minecraft Plugin