# Fix: Restablecimiento de Salud Máxima después del Baneo

## Problema Identificado

Cuando un jugador era baneado por alcanzar el límite mínimo de corazones (5 corazones o menos) y regresaba al servidor después del baneo, su salud máxima no se restablecía automáticamente a 5 corazones, quedando con la salud reducida que tenía antes del baneo.

## Solución Implementada

Se agregó un nuevo evento `PlayerJoinEvent` en la clase `HealthSteal.java` que:

### Funcionalidad

1. **Detección de Jugadores Previamente Baneados**: Verifica si el jugador que se conecta ha sido baneado anteriormente por corazones mínimos (consultando el `banCountMap`).

2. **Verificación de Salud Actual**: Comprueba si la salud máxima actual del jugador es menor a 10.0 (5 corazones).

3. **Restablecimiento Automático**: Si se cumplen ambas condiciones, restablece la salud máxima a 10.0 (5 corazones).

4. **Ajuste de Salud Actual**: Si la salud actual del jugador es mayor que la nueva salud máxima, la ajusta para evitar inconsistencias.

5. **Notificación**: Informa al jugador que su salud máxima ha sido restablecida.

6. **Logging**: Registra la acción en los logs del servidor para administradores.

### Código Agregado

```java
/**
 * Restablece la salud máxima a 5 corazones cuando un jugador se reconecta
 * después de haber sido baneado por alcanzar el límite mínimo de corazones
 */
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    UUID playerUUID = player.getUniqueId();
    
    // Verificar si este jugador ha sido baneado anteriormente por corazones mínimos
    if (banCountMap.containsKey(playerUUID)) {
        double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double resetHealth = 10.0; // 5 corazones
        
        // Solo restablecer si la salud máxima actual es menor a 5 corazones
        if (currentMaxHealth < resetHealth) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(resetHealth);
            
            // Ajustar la salud actual si es mayor que la nueva salud máxima
            if (player.getHealth() > resetHealth) {
                player.setHealth(resetHealth);
            }
            
            // Mensaje informativo al jugador
            player.sendMessage(MM.toComponent("<green>Tu salud máxima ha sido restablecida a 5 corazones.</green>"));
            
            // Log para administradores
            plugin.getLogger().info("Salud máxima restablecida para " + player.getName() + " a 5 corazones");
        }
    }
}
```

### Archivos Modificados

- `src/main/java/com/darkbladedev/mechanics/HealthSteal.java`
  - Agregado import para `PlayerJoinEvent`
  - Agregado método `onPlayerJoin()` con la lógica de restablecimiento

### Comportamiento Esperado

1. **Jugador Nuevo**: No se ve afectado, mantiene su salud normal.
2. **Jugador con Salud Normal**: Si tiene 5+ corazones, no se modifica nada.
3. **Jugador Previamente Baneado con Salud Baja**: Se restablece automáticamente a 5 corazones al conectarse.

### Validación

- ✅ Compilación exitosa
- ✅ No introduce errores en el código existente
- ✅ Mantiene la funcionalidad original del sistema de baneos
- ✅ Soluciona el problema reportado

### Notas Técnicas

- El restablecimiento solo ocurre para jugadores que han sido baneados previamente (están en `banCountMap`)
- Solo se restablece si la salud actual es menor a 5 corazones
- Se mantiene la integridad de los datos evitando dar más salud de la necesaria
- El sistema es compatible con el mecanismo existente de robo de corazones

---

**Fecha de Implementación**: $(Get-Date -Format "yyyy-MM-dd")
**Desarrollador**: Sistema VIRTHA
**Estado**: ✅ Implementado y Funcional