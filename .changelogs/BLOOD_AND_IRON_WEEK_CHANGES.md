# Cambios en BloodAndIronWeek.java - Mecánica lastHostileMobKillTime

## Resumen de Modificaciones

Se han realizado modificaciones en el archivo `BloodAndIronWeek.java` para cambiar la mecánica `lastHostileMobKillTime` de 10 a 15 minutos y añadir una advertencia 5 minutos antes del límite.

## Cambios Implementados

### 1. Constantes Actualizadas

```java
// Antes:
private static final long MOB_KILL_TIMEOUT = 10 * 60 * 1000; // 10 minutos

// Después:
private static final long MOB_KILL_TIMEOUT = 15 * 60 * 1000; // 15 minutos
private static final long MOB_KILL_WARNING_TIME = 10 * 60 * 1000; // 10 minutos (advertencia)
```

### 2. Nuevo Sistema de Advertencias

- **Nueva variable**: `Set<UUID> mobKillWarningGiven` para rastrear jugadores que han recibido la advertencia
- **Advertencia a los 10 minutos**: Los jugadores reciben un mensaje de advertencia cuando han pasado 10 minutos sin matar un mob hostil
- **Penalización a los 15 minutos**: La pérdida de salud ocurre después de 15 minutos

### 3. Lógica de Verificación Actualizada

#### En `checkMobKillTimeout()`:
- Verifica si han pasado 10 minutos para enviar advertencia
- Verifica si han pasado 15 minutos para aplicar penalización
- Resetea la bandera de advertencia cuando se aplica la penalización

#### En `startCheckKillsTask()`:
- Implementa la misma lógica de advertencia y penalización
- Maneja el estado de advertencias por jugador

### 4. Reset de Advertencias

#### En `onEntityDeath()`:
- Cuando un jugador mata un mob hostil, se resetea su bandera de advertencia
- Esto permite que reciba una nueva advertencia en el próximo ciclo si es necesario

#### En métodos de limpieza:
- `cleanupEventData()`: Limpia el conjunto de advertencias
- `stop()`: Limpia el conjunto de advertencias al finalizar el evento

## Mensajes del Sistema

### Mensaje de Advertencia (10 minutos):
```
⚠️ ¡ADVERTENCIA! Tienes 5 minutos para matar un mob hostil o perderás 2 corazones.
```

### Mensaje de Penalización (15 minutos):
```
💀 Has perdido 2 corazones por no matar mobs hostiles en 15 minutos. ¡Mantente activo!
```

## Beneficios de los Cambios

1. **Mayor tiempo de gracia**: Los jugadores tienen 15 minutos en lugar de 10 para matar mobs hostiles
2. **Sistema de advertencia**: Los jugadores reciben una advertencia 5 minutos antes de la penalización
3. **Mejor experiencia de usuario**: Más tiempo para reaccionar y evitar la pérdida de salud
4. **Gestión inteligente**: Las advertencias se resetean correctamente cuando el jugador mata un mob
5. **Limpieza adecuada**: Todos los datos se limpian correctamente al finalizar el evento

## Impacto en el Gameplay

- **Menos presión**: Los jugadores tienen más tiempo para encontrar y matar mobs hostiles
- **Mejor comunicación**: El sistema avisa antes de aplicar penalizaciones
- **Jugabilidad más equilibrada**: Reduce la frustración por penalizaciones inesperadas
- **Mantiene la mecánica**: Conserva el objetivo original de mantener a los jugadores activos

## Estado de la Implementación

✅ **Completado**: Todos los cambios han sido implementados y compilados exitosamente
✅ **Probado**: La compilación del proyecto fue exitosa sin errores
✅ **Documentado**: Cambios completamente documentados para referencia futura

---

**Fecha de modificación**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**Archivos modificados**: `BloodAndIronWeek.java`
**Compilación**: Exitosa ✅