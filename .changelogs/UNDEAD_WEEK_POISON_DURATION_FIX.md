# Fix: Duración del Efecto de Veneno en UndeadWeek

## Problema
El efecto de veneno en jugadores infectados durante la Semana de los No Muertos se aplicaba de forma permanente, reaplicándose constantemente cada 5 segundos, lo que resultaba en una experiencia de juego demasiado penalizante.

## Solución Implementada
Se modificó `UndeadWeek.java` para que el efecto de veneno tenga una duración fija de 30 segundos sin reaplicación automática:

### Cambios Realizados

1. **Nuevo Sistema de Seguimiento de Tiempo**
   - Agregado `Map<UUID, Long> infectedPlayersTime` para rastrear cuándo se aplicó el veneno a cada jugador
   - Permite controlar la duración del efecto sin reaplicación constante

2. **Modificación en `checkInfectedPlayers()`**
   - Cambiada la duración del efecto de veneno de 100 ticks (5 segundos) a 600 ticks (30 segundos)
   - Implementada lógica para evitar reaplicar el veneno si ya está activo
   - El veneno solo se aplica una vez por infección

3. **Modificación en `resumeEventTasks()`**
   - Cambiada la duración del efecto de veneno de 1200 ticks (60 segundos) a 600 ticks (30 segundos)
   - Mantiene consistencia con la nueva duración establecida

4. **Limpieza de Memoria**
   - Agregada limpieza del mapa `infectedPlayersTime` cuando los jugadores son curados
   - Agregada limpieza del mapa en `cleanupEventData()` para prevenir acumulación de memoria

### Código Modificado

#### En `checkInfectedPlayers()`:
```java
// Solo aplicar veneno si no lo tiene actualmente o si han pasado más de 30 segundos
long currentTime = System.currentTimeMillis();
Long lastPoisonTime = infectedPlayersTime.get(playerId);
boolean shouldApplyPoison = lastPoisonTime == null || 
    (currentTime - lastPoisonTime) >= 30000; // 30 segundos

if (shouldApplyPoison && !player.hasPotionEffect(PotionEffectType.POISON)) {
    player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 600, 0)); // 30 segundos
    infectedPlayersTime.put(playerId, currentTime);
}
```

#### En `resumeEventTasks()`:
```java
player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 600, 0)); // 30 segundos
```

#### Limpieza al curar jugadores:
```java
infectedPlayersTime.remove(playerId); // Limpiar tiempo de infección
```

## Beneficios

1. **Experiencia de Juego Mejorada**: El veneno ya no es permanente, permitiendo períodos de recuperación
2. **Duración Balanceada**: 30 segundos proporciona un desafío significativo sin ser excesivamente punitivo
3. **Gestión de Memoria**: Previene acumulación de datos innecesarios
4. **Consistencia**: Todas las aplicaciones de veneno ahora usan la misma duración

## Archivos Modificados
- `src/main/java/com/darkbladedev/mechanics/UndeadWeek.java`

## Pruebas Recomendadas
1. Verificar que el veneno se aplique por 30 segundos exactos
2. Confirmar que no se reaplique automáticamente
3. Probar que la limpieza de memoria funcione correctamente
4. Verificar que el comportamiento sea consistente tras reinicios del servidor

## Notas Técnicas
- La duración se especifica en ticks (600 ticks = 30 segundos)
- El sistema usa `System.currentTimeMillis()` para rastrear el tiempo de aplicación
- La limpieza de memoria se realiza tanto al curar como al finalizar el evento