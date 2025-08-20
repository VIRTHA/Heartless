# Fix: Evento se muestra como 'empty' tras reinicio del servidor

## Problema Identificado

Después del reinicio del servidor, aunque el archivo `weekly_event_data.json` contenía datos válidos del evento activo (como `UndeadWeek`), el sistema reportaba que el evento actual era 'empty'.

## Causa Raíz

El método `loadSavedEventData()` en `WeeklyEventManager` cargaba correctamente el `currentEventType` desde el archivo JSON, pero **no creaba la instancia real del evento** (`currentEvent`). Esto causaba que:

1. `currentEventType` se estableciera correctamente (ej: `UNDEAD_WEEK`)
2. `currentEvent` permaneciera como `null`
3. El método `getCurrentEvent()` devolviera un `EmptyEvent` cuando `currentEvent` era `null`

## Solución Implementada

Se modificó el método `loadSavedEventData()` para:

1. **Crear la instancia del evento**: Después de cargar el tipo de evento, se crea la instancia correspondiente usando un switch statement
2. **Validar tiempo restante**: Se verifica que el evento no haya expirado antes de crear la instancia
3. **Manejo de errores**: Si el tipo de evento no está implementado, se limpia el estado
4. **Logging mejorado**: Se agregan mensajes informativos para el seguimiento

## Cambios Realizados

### Archivo Modificado
- `src/main/java/com/darkbladedev/managers/WeeklyEventManager.java`

### Código Agregado
```java
// Crear la instancia del evento si el tipo es válido
if (currentEventType != null) {
    long currentTime = System.currentTimeMillis();
    long remainingTime = eventEndTime - currentTime;
    
    if (remainingTime > 0) {
        // Convertir duración de milisegundos a segundos para los constructores
        long durationInSeconds = remainingTime / 1000L;
        
        // Crear la instancia del evento según su tipo
        switch (currentEventType.getEventName()) {
            case "acid_week":
                currentEvent = new AcidWeek(plugin, durationInSeconds);
                break;
            case "toxic_fog":
                currentEvent = new ToxicFog(plugin, durationInSeconds);
                break;
            case "undead_week":
                currentEvent = new UndeadWeek(plugin, durationInSeconds);
                break;
            case "explosive_week":
                currentEvent = new ExplosiveWeek(plugin, durationInSeconds);
                break;
            case "blood_and_iron_week":
                currentEvent = new BloodAndIronWeek(plugin, durationInSeconds);
                break;
            default:
                plugin.getLogger().warning("Evento no implementado para carga: " + currentEventType.getEventName());
                currentEventType = null;
                isEventActive = false;
                return false;
        }
        
        isEventActive = true;
        plugin.getLogger().info("Evento cargado correctamente: " + currentEventType.getEventName());
    } else {
        // El evento ya debería haber terminado
        plugin.getLogger().info("Evento expirado encontrado en datos guardados, ignorando...");
        currentEventType = null;
        isEventActive = false;
        clearEventData();
        return false;
    }
}
```

## Beneficios

1. **Persistencia completa**: Los eventos ahora se cargan completamente tras reinicio
2. **Consistencia de estado**: `currentEventType` y `currentEvent` están sincronizados
3. **Validación de tiempo**: Se evita cargar eventos expirados
4. **Mejor debugging**: Logs informativos para seguimiento
5. **Manejo robusto de errores**: Limpieza automática de estados inválidos

## Pruebas Recomendadas

1. **Iniciar un evento semanal**
2. **Reiniciar el servidor**
3. **Verificar que el evento sigue activo** (no 'empty')
4. **Comprobar que las funcionalidades del evento funcionan correctamente**
5. **Verificar logs del servidor** para mensajes de carga exitosa

## Notas Técnicas

- La duración se calcula como tiempo restante desde el momento de carga
- Se mantiene la conversión de milisegundos a segundos para compatibilidad con constructores
- El sistema sigue manejando eventos expirados limpiando automáticamente los datos
- No se requieren cambios en el formato del archivo JSON

## Estado

✅ **Implementado y compilado exitosamente**

---

**Fecha**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**Desarrollador**: Assistant
**Versión**: 1.0.0