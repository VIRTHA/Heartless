# Actualización de RewardPool.java - Sistema de Recompensas de Eventos Semanales

## Resumen
Se ha actualizado completamente la clase `RewardPool.java` para incluir todas las recompensas específicas y detalladas de cada evento semanal del sistema Heartless.

## Cambios Implementados

### 1. Semana Explosiva (EXPLOSIVE_WEEK)
**Recompensas actualizadas:**
- Encantamiento Carve (por matar ghast en overworld)
- +1 corazón permanente (por conseguir cabezas de mobs hostiles)
- Tag 'TNTómano' (por matar jugador con explosión)
- +1 corazón permanente (por matar warden con creeper eléctrico)

### 2. Semana de No-Muertos (UNDEAD_WEEK)
**Recompensas actualizadas:**
- Encantamiento First Strike (por matar 50 zombies)
- +1 corazón permanente (por sobrevivir horda de 20+ zombies)
- Tag 'Dr. Zomboss' (por matar jugador usando zombies)
- +1 corazón permanente (por convertir 10 aldeanos en zombies)

### 3. Semana de Sangre y Hierro (BLOOD_AND_IRON_WEEK)
**Recompensas actualizadas:**
- Encantamiento Adrenaline (por matar 3 jugadores)
- +1 corazón permanente (por matar jugador con poción de daño)
- Tag 'Pentakill' (por 5 kills seguidos sin morir)
- +1 corazón permanente (por sobrevivir evento con +10 kills)

### 4. Semana Ácida (ACID_WEEK)
**Recompensas completamente nuevas:**
- Poción de resistencia permanente (por sobrevivir 10 min bajo lluvia ácida)
- +1 corazón permanente (por matar jugador en agua ácida)
- Kit de construcción especial (por construir base resistente al ácido)
- Tag 'Químico' (por ayudar a 5 jugadores a sobrevivir al ácido)

## Mejoras Técnicas

### Precisión de Información
- Cada recompensa ahora incluye la condición específica para obtenerla
- Se eliminaron las recompensas genéricas y se reemplazaron por las reales del sistema
- Se corrigió la entrada vacía de ACID_WEEK que causaba problemas

### Consistencia del Sistema
- Todas las recompensas están alineadas con los desafíos definidos en `Challenges.java`
- Se mantiene la estructura del Map<EventType, List<String>> para compatibilidad
- Las descripciones son claras y específicas para cada desafío

## Validación

### Compilación
- ✅ `./gradlew compileJava` - BUILD SUCCESSFUL
- ✅ `./gradlew build` - BUILD SUCCESSFUL

### Fuentes de Información
- Datos extraídos de `Challenges.java` (líneas 83-136)
- Verificación cruzada con las clases de eventos individuales
- Alineación con la mecánica real del sistema de eventos semanales

## Impacto

### Funcionalidad Mejorada
- El comando `hs event status` ahora mostrará recompensas precisas y detalladas
- Los jugadores tendrán información clara sobre qué hacer para obtener cada recompensa
- Se eliminó la confusión causada por recompensas genéricas o vacías

### Mantenibilidad
- Código más legible y autodocumentado
- Fácil actualización cuando se agreguen nuevos eventos o recompensas
- Consistencia con el resto del sistema de eventos

## Archivos Modificados
- `src/main/java/com/darkbladedev/utils/RewardPool.java`

## Fecha de Actualización