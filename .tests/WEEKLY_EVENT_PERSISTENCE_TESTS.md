# Pruebas de Persistencia - Eventos Semanales

## Resumen de Implementación

El sistema de persistencia de eventos semanales utiliza dos archivos principales:
- **`weekly_event_data.json`** - Datos del evento actual (WeeklyEventManager)
- **`day_cycle_data.yml`** - Datos del ciclo de día (StorageManager)

## Estructura de Datos JSON

```json
{
  "eventActive": true,
  "eventType": "undead_week",
  "startTime": 1640995200000,
  "endTime": 1641600000000,
  "isPaused": false,
  "pauseStartTime": 0,
  "totalPausedTime": 0
}
```

## Casos de Prueba

### 1. Guardado de Eventos ✅
**Objetivo:** Verificar que los eventos se guarden correctamente al archivo JSON.

**Pasos de Prueba:**
1. Iniciar un evento semanal (ej: `/weeklyevent start undead_week 7d`)
2. Verificar que se cree el archivo `weekly_event_data.json`
3. Comprobar que contenga todos los campos requeridos
4. Validar que `eventActive` sea `true`

**Resultado Esperado:**
- Archivo JSON creado con estructura correcta
- Mensaje de confirmación: "Evento semanal guardado correctamente"
- Logs sin errores

### 2. Carga de Eventos al Reinicio ✅
**Objetivo:** Verificar que los eventos se restauren correctamente tras reinicio.

**Pasos de Prueba:**
1. Iniciar un evento semanal
2. Reiniciar el servidor (`/reload` o reinicio completo)
3. Verificar que el evento continúe activo
4. Comprobar que mantenga su funcionalidad completa

**Resultado Esperado:**
- Evento restaurado automáticamente
- Funcionalidades específicas del evento operativas
- Tiempo restante calculado correctamente
- Log: "Evento cargado correctamente: [tipo_evento]"

### 3. Manejo de Eventos Vacíos ✅
**Objetivo:** Verificar que los eventos 'empty' no se carguen tras reinicio.

**Pasos de Prueba:**
1. Crear manualmente un archivo JSON con `eventType: "empty"`
2. Reiniciar el servidor
3. Verificar que no se cargue el evento vacío
4. Comprobar que el archivo se limpie automáticamente

**Resultado Esperado:**
- Evento 'empty' ignorado
- Log: "Evento 'empty' encontrado en datos guardados, ignorando..."
- Archivo JSON limpiado automáticamente

### 4. Manejo de Eventos Expirados ✅
**Objetivo:** Verificar que los eventos con tiempo vencido se eliminen.

**Pasos de Prueba:**
1. Crear archivo JSON con `endTime` en el pasado
2. Reiniciar el servidor
3. Verificar que el evento no se cargue
4. Comprobar limpieza automática del archivo

**Resultado Esperado:**
- Evento expirado ignorado
- Log: "Evento expirado encontrado en datos guardados, ignorando..."
- Datos limpiados automáticamente

### 5. Manejo de Archivos Corruptos ✅
**Objetivo:** Verificar recuperación ante archivos JSON malformados.

**Pasos de Prueba:**
1. Corromper manualmente el archivo JSON
2. Reiniciar el servidor
3. Verificar que se maneje el error graciosamente
4. Comprobar que se elimine el archivo corrupto

**Resultado Esperado:**
- Error manejado sin crash del servidor
- Log: "Error de formato en el archivo de datos del evento"
- Archivo corrupto eliminado automáticamente

### 6. Persistencia de Estado de Pausa ✅
**Objetivo:** Verificar que el estado de pausa se mantenga tras reinicio.

**Pasos de Prueba:**
1. Iniciar evento y pausarlo (`/weeklyevent pause`)
2. Reiniciar el servidor
3. Verificar que el evento se cargue en estado pausado
4. Comprobar que se pueda reanudar correctamente

**Resultado Esperado:**
- Estado de pausa preservado
- Tiempo de pausa acumulado correctamente
- Funcionalidad de reanudación operativa

## Métodos de Implementación

### WeeklyEventManager.java
- **`saveEventData()`** - Guarda estado actual en JSON
- **`loadSavedEventData()`** - Carga y valida datos guardados
- **`clearEventData()`** - Limpia archivo cuando no hay evento activo

### Validaciones Implementadas
1. **Verificación de existencia de archivo**
2. **Validación de campos requeridos** (eventType, startTime, endTime)
3. **Manejo de valores null** con defaults seguros
4. **Verificación de tiempo de expiración**
5. **Filtrado de eventos 'empty'**
6. **Manejo de excepciones** (IOException, ParseException, ClassCastException)

## Flujo de Persistencia

```
1. Evento Iniciado → saveEventData() → JSON creado
2. Servidor Reiniciado → loadSavedEventData() → Validaciones
3. Si válido → Crear instancia del evento → Iniciar
4. Si inválido → clearEventData() → Limpiar archivo
```

## Comandos de Prueba

```bash
# Iniciar evento para pruebas
/weeklyevent start undead_week 1h

# Pausar evento
/weeklyevent pause

# Reanudar evento
/weeklyevent resume

# Detener evento
/weeklyevent stop

# Ver estado actual
/weeklyevent status
```

## Archivos de Datos

- **Ubicación:** `plugins/HeartlessMain/weekly_event_data.json`
- **Formato:** JSON con campos específicos
- **Backup:** Recomendado hacer backup antes de pruebas

## Notas Técnicas

1. **TimeExpression Integration:** Los eventos cargados utilizan `TimeExpression.fromMilliseconds()` para calcular duración restante
2. **Thread Safety:** El sistema incluye locks para prevenir múltiples eventos simultáneos
3. **Error Recovery:** Archivos corruptos se eliminan automáticamente para evitar problemas futuros
4. **Memory Management:** Los eventos se limpian correctamente al finalizar

## Estado de Pruebas

- ✅ **Guardado de eventos** - Implementado y funcional
- ✅ **Carga tras reinicio** - Implementado con validaciones
- ✅ **Manejo de eventos vacíos** - Filtrado automático
- ✅ **Eventos expirados** - Limpieza automática
- ✅ **Archivos corruptos** - Recuperación automática
- ✅ **Estado de pausa** - Persistencia completa

---
*Pruebas de persistencia para eventos semanales*
*Sistema: WeeklyEventManager + StorageManager*
*Formato: JSON + YAML*