# Corrección de Persistencia de Eventos Semanales

## Problema Identificado

Los datos del evento semanal activo no se guardaban correctamente entre reinicios del servidor, causando que:
- Los eventos activos se perdieran al reiniciar el servidor
- Los jugadores perdieran el progreso de sus desafíos
- El sistema no pudiera reanudar eventos pausados

## Causa del Problema

En el método `onDisable()` de `HeartlessMain.java`, no se estaba llamando al método `shutdown()` del `WeeklyEventManager`, que es responsable de guardar los datos del evento activo antes del cierre del servidor.

### Código Anterior
```java
@Override
public void onDisable() {
    // Plugin shutdown logic
    
    // Guardar datos del ciclo de día
    if (storageManager != null) {
        storageManager.saveDayCycleData();
    }
    
    // ... otros cleanup ...
}
```

## Solución Implementada

Se agregó la llamada al método `shutdown()` del `WeeklyEventManager` en el `onDisable()` para asegurar que los datos del evento se guarden correctamente.

### Código Corregido
```java
@Override
public void onDisable() {
    // Plugin shutdown logic
    
    // Guardar datos del evento semanal activo
    if (weeklyEventManager != null) {
        weeklyEventManager.shutdown();
    }
    
    // Guardar datos del ciclo de día
    if (storageManager != null) {
        storageManager.saveDayCycleData();
    }
    
    // ... otros cleanup ...
}
```

## Funcionalidad del Método `shutdown()`

El método `WeeklyEventManager.shutdown()` realiza las siguientes acciones:

1. **Guarda el estado actual**: Si hay un evento activo, llama a `saveEventData()`
2. **Cancela tareas programadas**: Cancela el `weeklyTask` si está ejecutándose
3. **Persiste datos críticos**:
   - Estado del evento (activo/inactivo)
   - Tipo de evento actual
   - Tiempos de inicio y fin
   - Estado de pausa y tiempo pausado
   - Tiempo total pausado acumulado

## Datos Persistidos

Los siguientes datos se guardan en `weekly_event_data.json`:

```json
{
  "eventActive": true,
  "eventType": "BloodAndIronWeek",
  "startTime": 1640995200000,
  "endTime": 1641600000000,
  "isPaused": false,
  "pauseStartTime": 0,
  "totalPausedTime": 0
}
```

## Beneficios de la Corrección

1. **Continuidad de Eventos**: Los eventos activos se reanudan automáticamente después de un reinicio
2. **Preservación del Progreso**: Los jugadores mantienen su progreso en desafíos
3. **Gestión de Pausas**: Los eventos pausados se reanudan correctamente
4. **Integridad de Datos**: Se evita la pérdida de datos críticos del sistema de eventos

## Archivos Modificados

- `HeartlessMain.java`: Agregada llamada a `weeklyEventManager.shutdown()` en `onDisable()`

## Pruebas Recomendadas

1. **Iniciar un evento semanal**
2. **Reiniciar el servidor**
3. **Verificar que el evento continúe activo**
4. **Pausar un evento y reiniciar**
5. **Verificar que el evento permanezca pausado**
6. **Comprobar que los desafíos mantengan el progreso**

## Notas Técnicas

- La corrección es compatible con versiones anteriores
- No requiere migración de datos existentes
- El sistema maneja automáticamente archivos corruptos o faltantes
- Se mantiene la funcionalidad de eventos vacíos (`EmptyEvent`) para inicialización

---

**Fecha de Implementación**: Enero 2025  
**Versión**: 1.0.0  
**Desarrollador**: DarkBladeDev