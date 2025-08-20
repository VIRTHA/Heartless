# Implementación de TimeExpression para Eventos Semanales

## Resumen
Se ha implementado exitosamente la clase `TimeExpression` que permite a los eventos semanales aceptar expresiones de tiempo legibles (como "1w", "2d 3h", "30m") en lugar de solo valores numéricos en segundos.

## Archivos Modificados

### 1. Nueva Clase: `TimeExpression.java`
- **Ubicación**: `src/main/java/com/darkbladedev/utils/TimeExpression.java`
- **Funcionalidad**: 
  - Encapsula expresiones de tiempo y las convierte a ticks, milisegundos y segundos
  - Utiliza `TimeConverter.java` para el parseo de expresiones
  - Proporciona métodos estáticos para crear instancias desde diferentes unidades de tiempo
  - Incluye validación de expresiones y manejo de errores

### 2. Clase Base Actualizada: `WeeklyEvent.java`
- **Cambios**:
  - Añadido import de `TimeExpression`
  - Nuevo constructor que acepta `TimeExpression` como parámetro de duración
  - Mantiene compatibilidad con el constructor original que acepta `long`

### 3. Eventos Semanales Actualizados
Todos los eventos semanales ahora soportan `TimeExpression`:

#### `UndeadWeek.java`
- Añadido import de `TimeExpression`
- Nuevo constructor con parámetro `TimeExpression`
- Mantiene funcionalidad existente

#### `AcidWeek.java`
- Añadido import de `TimeExpression`
- Nuevo constructor con parámetro `TimeExpression`
- Mantiene funcionalidad existente

#### `BloodAndIronWeek.java`
- Añadido import de `TimeExpression`
- Nuevo constructor con parámetro `TimeExpression`
- Mantiene configuración de prefix en ambos constructores

#### `ExplosiveWeek.java`
- Añadido import de `TimeExpression`
- Nuevo constructor con parámetro `TimeExpression`
- Mantiene configuración de prefix en ambos constructores

#### `ToxicFog.java`
- Añadido import de `TimeExpression`
- Nuevo constructor con parámetro `TimeExpression`
- Mantiene inicialización de `affectedPlayers` en ambos constructores

### 4. Gestión de Eventos Actualizada

#### `WeeklyEventManager.java`
- **Cambios**:
  - Añadido import de `TimeExpression`
  - Actualizado método `startEvent()` para usar `TimeExpression.fromMilliseconds()`
  - Actualizado método de carga de eventos para usar `TimeExpression.fromMilliseconds()`
  - Todos los eventos ahora se crean usando el nuevo constructor con `TimeExpression`

#### `EventType.java`
- **Cambios**:
  - Añadido import de `TimeExpression`
  - Actualizado método `toEvent()` para usar `TimeExpression.fromSeconds()`
  - Mantiene compatibilidad con la interfaz existente que recibe `long duration`

## Funcionalidades de TimeExpression

### Métodos Estáticos de Creación
```java
// Desde expresión de tiempo (ej: "1w 2d 3h")
TimeExpression expr1 = new TimeExpression("1w 2d 3h");

// Desde diferentes unidades
TimeExpression expr2 = TimeExpression.fromTicks(6000L);
TimeExpression expr3 = TimeExpression.fromSeconds(300L);
TimeExpression expr4 = TimeExpression.fromMilliseconds(15000L);
```

### Métodos de Conversión
```java
TimeExpression expr = new TimeExpression("30m");
long ticks = expr.getTicks();           // Obtiene ticks de Minecraft
long seconds = expr.getSeconds();       // Obtiene segundos
long millis = expr.getMilliseconds();   // Obtiene milisegundos
boolean valid = expr.isValid();         // Verifica si la expresión es válida
```

### Expresiones Soportadas
- `s` - segundos
- `m` - minutos  
- `h` - horas
- `d` - días
- `w` - semanas
- Combinaciones: `"1w 2d 3h 30m 45s"`

## Compatibilidad
- **Retrocompatibilidad**: Todos los constructores originales que aceptan `long` se mantienen
- **Sin cambios de API**: Las interfaces públicas existentes no han cambiado
- **Migración gradual**: Se puede migrar gradualmente a usar `TimeExpression`

## Beneficios
1. **Legibilidad**: Las duraciones son más fáciles de leer y entender
2. **Flexibilidad**: Soporte para múltiples formatos de tiempo
3. **Validación**: Verificación automática de expresiones válidas
4. **Consistencia**: Uso uniforme del sistema `TimeConverter` existente
5. **Mantenibilidad**: Código más limpio y fácil de mantener

## Pruebas Recomendadas
1. Crear eventos con expresiones de tiempo (ej: `/event start undead_week 1w`)
2. Verificar que las duraciones se calculen correctamente
3. Probar expresiones complejas (ej: `"2d 12h 30m"`)
4. Confirmar que los eventos existentes siguen funcionando
5. Verificar persistencia de eventos con `TimeExpression`

## Notas Técnicas
- La conversión se realiza internamente usando `TimeConverter.parseTimeToTicks()`
- Los ticks se almacenan como valor base para máxima precisión
- La validación se realiza durante la construcción del objeto
- Los métodos `equals()` y `hashCode()` están implementados correctamente

## Estado
✅ **COMPLETADO** - Implementación exitosa con compilación sin errores

Todos los eventos semanales ahora soportan `TimeExpression` manteniendo compatibilidad completa con el sistema existente.