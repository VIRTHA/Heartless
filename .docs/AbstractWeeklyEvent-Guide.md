# Guía de AbstractWeeklyEvent

## Introducción

`AbstractWeeklyEvent` es una clase base avanzada diseñada para simplificar la creación de eventos semanales en el plugin Heartless. Proporciona una arquitectura robusta con funcionalidades comunes como sistema de desafíos, persistencia de datos automática, estadísticas de jugadores y manejo thread-safe.

## Características Principales

### 🎯 Sistema de Desafíos Unificado
- Registro automático de desafíos personalizados
- Verificación automática de progreso
- Recompensas configurables
- Persistencia automática del progreso

### 📊 Sistema de Estadísticas Avanzado
- Estadísticas globales del evento
- Estadísticas individuales por jugador
- Procesamiento automático y periódico
- Persistencia thread-safe

### 💾 Persistencia Automática de Datos
- Guardado automático cada 5 minutos
- Guardado al detener el evento
- Datos específicos del evento personalizables
- Manejo de errores robusto

### 🔒 Thread Safety
- Todos los componentes son thread-safe
- Uso de `AtomicBoolean` y `ConcurrentHashMap`
- Sincronización automática de datos

## Arquitectura

```
AbstractWeeklyEvent (Clase Base)
├── WeeklyEvent (Clase Padre)
├── Sistema de Desafíos
├── Sistema de Estadísticas  
├── Persistencia de Datos
└── Manejo de Jugadores Activos
```

## Implementación Básica

### 1. Extender la Clase

```java
public class MiEventoPersonalizado extends AbstractWeeklyEvent implements Listener {
    
    private static final String EVENT_ID = "mi_evento";
    private static final String EVENT_PREFIX = "<gradient:#ff6b6b:#4ecdc4>[Mi Evento]</gradient>";
    
    public MiEventoPersonalizado(HeartlessMain plugin, TimeExpression duration) {
        super(plugin, duration);
        this.prefix = EVENT_PREFIX;
        
        // Registrar listener si es necesario
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public String getId() {
        return EVENT_ID;
    }
}
```

### 2. Implementar Métodos del Ciclo de Vida

```java
@Override
protected void onEventStart() {
    // Lógica de inicio específica del evento
    setupEventSpecificChallenges();
    initializeEventStatistics();
    
    logger.info("[" + getId() + "] Evento iniciado correctamente");
}

@Override
protected void onEventStop() {
    // Lógica de finalización específica del evento
    processFinalEventStatistics();
    cleanupEventResources();
    
    logger.info("[" + getId() + "] Evento detenido correctamente");
}
```

### 3. Configurar Datos Específicos

```java
@Override
protected void initializeEventSpecificData() {
    // Datos que se guardarán automáticamente
    eventSpecificData.put("start_time", System.currentTimeMillis());
    eventSpecificData.put("initial_players", getActivePlayerCount());
    eventSpecificData.put("event_version", "1.0");
    
    // Marcar como modificado para activar guardado automático
    dataDirty.set(true);
}

@Override
protected void saveEventSpecificData() {
    // Actualizar datos antes del guardado
    eventSpecificData.put("last_save", System.currentTimeMillis());
    
    // Implementar guardado en base de datos
    // Ejemplo: databaseManager.saveEventData(getId(), eventSpecificData);
    
    logger.info("[" + getId() + "] Datos guardados: " + eventSpecificData.size() + " entradas");
}
```

## Sistema de Desafíos

### Registrar Desafíos

```java
private void setupEventSpecificChallenges() {
    // Desafío simple
    registerChallenge("kill_mobs", new ChallengeDefinition(
        "kill_mobs",
        "Cazador",
        "Elimina 50 mobs durante el evento",
        50,
        Arrays.asList("heartless:hunter_reward", "heartless:bonus_xp")
    ));
    
    // Desafío de tiempo
    registerChallenge("survive_hour", new ChallengeDefinition(
        "survive_hour",
        "Superviviente",
        "Sobrevive una hora sin morir",
        3600, // 1 hora en segundos
        Collections.singletonList("heartless:survival_reward")
    ));
}
```

### Completar Desafíos

```java
// Completar desafío para un jugador específico
completeChallengeForPlayer(playerId, "kill_mobs");

// Verificar si un jugador completó un desafío
if (hasChallengeCompleted(playerId, "survive_hour")) {
    // Lógica adicional
}
```

### Verificación Personalizada

```java
@Override
protected void checkPlayerChallenges(UUID playerId) {
    // Llamar verificación base
    super.checkPlayerChallenges(playerId);
    
    // Verificación personalizada
    int mobKills = (Integer) getPlayerStatistic(playerId, "mob_kills");
    if (mobKills >= 50 && !hasChallengeCompleted(playerId, "kill_mobs")) {
        completeChallengeForPlayer(playerId, "kill_mobs");
    }
}
```

## Sistema de Estadísticas

### Estadísticas Globales

```java
// Actualizar estadística global
updateGlobalStatistic("total_kills", 1500);

// Incrementar estadística global
incrementGlobalStatistic("messages_sent", 1);

// Obtener estadística global
Object totalKills = getGlobalStatistic("total_kills");
```

### Estadísticas de Jugadores

```java
// Actualizar estadística de jugador
updatePlayerStatistic(playerId, "kills", 25);

// Incrementar estadística de jugador
incrementPlayerStatistic(playerId, "deaths", 1);

// Obtener estadística de jugador
Object playerKills = getPlayerStatistic(playerId, "kills");
```

### Procesamiento Automático

```java
@Override
protected void processEventStatistics() {
    // Actualizar estadísticas globales
    updateGlobalStatistic("current_players", getActivePlayerCount());
    updateGlobalStatistic("uptime_minutes", getCurrentDurationMinutes());
    
    // Procesar estadísticas de jugadores activos
    for (UUID playerId : getActivePlayers()) {
        processPlayerStatistics(playerId);
    }
}

private void processPlayerStatistics(UUID playerId) {
    // Calcular tiempo de sesión
    Object joinTime = getPlayerStatistic(playerId, "join_time");
    if (joinTime instanceof Long) {
        long sessionTime = System.currentTimeMillis() - (Long) joinTime;
        updatePlayerStatistic(playerId, "session_time", sessionTime);
    }
}
```

## Manejo de Eventos de Jugadores

```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    if (!isActive()) return;
    
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    
    // Inicializar estadísticas del jugador
    updatePlayerStatistic(playerId, "join_time", System.currentTimeMillis());
    updatePlayerStatistic(playerId, "kills", 0);
    updatePlayerStatistic(playerId, "deaths", 0);
    
    // Agregar a jugadores activos
    addActivePlayer(playerId);
}

@EventHandler
public void onPlayerQuit(PlayerQuitEvent event) {
    if (!isActive()) return;
    
    UUID playerId = event.getPlayer().getUniqueId();
    
    // Procesar estadísticas finales
    processPlayerFinalStats(playerId);
    
    // Remover de jugadores activos
    removeActivePlayer(playerId);
}
```

## Mejores Prácticas

### 1. Inicialización Correcta

```java
public MiEvento(HeartlessMain plugin, TimeExpression duration) {
    super(plugin, duration);
    
    // Configurar prefix personalizado
    this.prefix = "<gradient:#ff6b6b:#4ecdc4>[Mi Evento]</gradient>";
    
    // Registrar listeners solo si es necesario
    if (needsEventListeners()) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
```

### 2. Manejo de Errores

```java
@Override
protected void saveEventSpecificData() {
    try {
        // Lógica de guardado
        databaseManager.saveEventData(getId(), eventSpecificData);
        logger.info("[" + getId() + "] Datos guardados correctamente");
    } catch (Exception e) {
        logger.error("[" + getId() + "] Error al guardar datos: " + e.getMessage());
    }
}
```

### 3. Limpieza de Recursos

```java
@Override
protected void onEventStop() {
    try {
        // Procesar estadísticas finales
        processFinalEventStatistics();
        
        // Limpiar recursos específicos
        cleanupEventResources();
        
        // Desregistrar listeners si fueron registrados
        HandlerList.unregisterAll(this);
        
    } catch (Exception e) {
        logger.error("[" + getId() + "] Error durante la limpieza: " + e.getMessage());
    }
}
```

### 4. Validación de Datos

```java
private void validatePlayerStatistic(UUID playerId, String key, Object value) {
    if (playerId == null || key == null || key.isEmpty()) {
        logger.warn("[" + getId() + "] Intento de actualizar estadística con datos inválidos");
        return;
    }
    
    updatePlayerStatistic(playerId, key, value);
}
```

## Configuración Avanzada

### Intervalos Personalizados

```java
// Usar constantes predefinidas
private static final long CUSTOM_SAVE_INTERVAL = SAVE_INTERVAL_MS * 2; // 10 minutos
private static final long CUSTOM_STATS_INTERVAL = STATS_INTERVAL_MS / 2; // 15 segundos

// O definir intervalos específicos del evento
private void setupCustomIntervals() {
    // Implementar lógica de intervalos personalizados si es necesario
}
```

### Desafíos Complejos

```java
private void setupComplexChallenges() {
    // Desafío con múltiples condiciones
    registerChallenge("master_survivor", new ChallengeDefinition(
        "master_survivor",
        "Maestro Superviviente",
        "Sobrevive 2 horas, mata 100 mobs y no mueras",
        1, // Se verifica manualmente
        Arrays.asList("heartless:master_reward", "heartless:legendary_item")
    ));
}

@Override
protected void checkPlayerChallenges(UUID playerId) {
    super.checkPlayerChallenges(playerId);
    
    // Verificar desafío complejo
    if (!hasChallengeCompleted(playerId, "master_survivor")) {
        long sessionTime = getSessionTime(playerId);
        int kills = (Integer) getPlayerStatistic(playerId, "kills");
        int deaths = (Integer) getPlayerStatistic(playerId, "deaths");
        
        if (sessionTime >= 7200000 && kills >= 100 && deaths == 0) {
            completeChallengeForPlayer(playerId, "master_survivor");
        }
    }
}
```

## Migración desde WeeklyEvent

### Paso 1: Cambiar la Herencia

```java
// Antes
public class MiEvento extends WeeklyEvent {

// Después  
public class MiEvento extends AbstractWeeklyEvent {
```

### Paso 2: Implementar Métodos Requeridos

```java
// Agregar métodos requeridos por AbstractWeeklyEvent
@Override
protected void initializeEventSpecificData() {
    // Migrar datos de inicialización existentes
}

@Override
protected void saveEventSpecificData() {
    // Migrar lógica de guardado existente
}

@Override
protected void processEventStatistics() {
    // Migrar procesamiento de estadísticas existente
}
```

### Paso 3: Aprovechar Nuevas Funcionalidades

```java
@Override
protected void onEventStart() {
    // Lógica existente de inicio
    super.onEventStart(); // Llamar al método padre si es necesario
    
    // Agregar configuración de desafíos
    setupEventSpecificChallenges();
}
```

## Ejemplo Completo

Ver el archivo `ExampleAbstractEvent.java` para un ejemplo completo de implementación que demuestra todas las funcionalidades disponibles.

## Solución de Problemas

### Problema: Los datos no se guardan automáticamente
**Solución:** Asegúrate de llamar `dataDirty.set(true)` después de modificar `eventSpecificData`.

### Problema: Las estadísticas no se actualizan
**Solución:** Verifica que el evento esté activo (`isActive()`) antes de actualizar estadísticas.

### Problema: Los desafíos no se completan automáticamente
**Solución:** Implementa `checkPlayerChallenges()` con la lógica específica de verificación.

### Problema: Memory leaks o recursos no liberados
**Solución:** Implementa correctamente `onEventStop()` y limpia todos los recursos específicos del evento.

## Conclusión

`AbstractWeeklyEvent` proporciona una base sólida para crear eventos semanales complejos con funcionalidades avanzadas. Su arquitectura modular permite una fácil extensión y personalización mientras mantiene la consistencia y robustez del sistema.

Para más información o soporte, consulta la documentación del plugin principal o contacta al equipo de desarrollo.