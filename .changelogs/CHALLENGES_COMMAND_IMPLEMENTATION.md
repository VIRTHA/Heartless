# Implementación del Comando de Desafíos de Eventos Semanales

## Resumen
Se ha implementado un nuevo comando para visualizar los desafíos del evento semanal activo, permitiendo a los jugadores consultar fácilmente qué desafíos están disponibles y sus respectivas recompensas.

## Comando Implementado

### Sintaxis
```
/heartless event challenges
/heartless event desafios
```

### Descripción
Muestra los desafíos específicos del evento semanal que esté actualmente en curso, incluyendo:
- Lista detallada de todos los desafíos disponibles
- Descripción clara de cada objetivo
- Recompensas asociadas a cada desafío
- Información contextual sobre el evento

## Archivos Modificados/Creados

### 1. `Challenges.java` (Nuevo)
**Ubicación:** `src/main/java/com/darkbladedev/commands/functions/events/Challenges.java`

**Características principales:**
- Implementa `SubcommandExecutor` y `TabCompletable`
- Verifica si hay un evento activo antes de mostrar desafíos
- Detecta automáticamente el tipo de evento y muestra los desafíos correspondientes
- Soporte para múltiples tipos de eventos semanales

### 2. `EventControl.java` (Modificado)
**Ubicación:** `src/main/java/com/darkbladedev/commands/nodes/EventControl.java`

**Cambios realizados:**
- Agregado subcomando `"challenges"` que instancia la clase `Challenges`
- Agregado alias `"desafios"` para usuarios de habla hispana
- Mantiene compatibilidad con todos los comandos existentes

## Eventos Soportados

### 1. Semana de Sangre y Hierro (BloodAndIronWeek)
- **Desafío 1:** Mata a 3 jugadores → Encantamiento Adrenaline
- **Desafío 2:** Mata a un jugador con poción de daño instantáneo → +1 corazón permanente
- **Desafío 3:** Mata a 5 jugadores seguidos sin morir → Tag "Pentakill"
- **Desafío 4:** Sobrevive sin morir en todo el evento (con más de 10 kills) → +1 corazón permanente

### 2. Semana Explosiva (ExplosiveWeek)
- **Desafío 1:** Mata a un ghast en el overworld → Encantamiento Carve
- **Desafío 2:** Consigue cabezas de mobs hostiles (Zombie, Esqueleto, Creeper) → +1 corazón
- **Desafío 3:** Mata a un jugador con una explosión → Tag "TNTómano"
- **Desafío 4:** Mata a un warden con explosión de creeper eléctrico → +1 corazón

### 3. Semana de No-Muertos (UndeadWeek)
- **Desafío 1:** Mata a 50 zombies durante el evento → Encantamiento First Strike
- **Desafío 2:** Sobrevive a una horda de 20+ zombies → +1 corazón permanente
- **Desafío 3:** Mata a un jugador usando solo zombies → Tag "Dr. Zomboss"
- **Desafío 4:** Convierte a 10 aldeanos en zombies → +1 corazón permanente

### 4. Semana Ácida (AcidWeek)
- **Desafío 1:** Sobrevive 10 minutos bajo lluvia ácida → Poción de resistencia permanente
- **Desafío 2:** Mata a un jugador mientras estás en agua ácida → +1 corazón permanente
- **Desafío 3:** Construye una base resistente al ácido → Kit de construcción especial
- **Desafío 4:** Ayuda a 5 jugadores a sobrevivir al ácido → Tag "Químico"

## Características Técnicas

### Validación de Estado
- Verifica automáticamente si hay un evento activo
- Muestra mensaje informativo si no hay eventos en curso
- Detecta el tipo de evento usando `instanceof` para mostrar desafíos específicos

### Manejo de Errores
- Gestión segura de eventos nulos o inactivos
- Mensaje de fallback para eventos sin desafíos definidos
- Integración con el sistema de mensajes MM (MiniMessage)

### Extensibilidad
- Estructura modular que permite agregar fácilmente nuevos tipos de eventos
- Métodos separados para cada tipo de evento facilitan el mantenimiento
- Patrón consistente para agregar futuros eventos semanales

## Mensajes del Sistema

### Cuando no hay evento activo:
```
[Heartless] No hay ningún evento semanal activo en este momento.
```

### Formato de desafíos:
```
=== DESAFÍOS DE [NOMBRE DEL EVENTO] ===
1. [Descripción del desafío]
   Recompensa: [Descripción de la recompensa]
...
Usa /heartless event status para ver el estado del evento.
```

## Integración con Sistema Existente

### Compatibilidad
- Totalmente compatible con el sistema de comandos existente
- No interfiere con otros subcomandos de eventos
- Utiliza las mismas clases de eventos ya implementadas

### Dependencias
- `WeeklyEventManager` para obtener el evento actual
- Clases específicas de eventos (`BloodAndIronWeek`, `ExplosiveWeek`, etc.)
- Sistema de mensajes `MM` para formateo consistente

## Uso Recomendado

### Para Jugadores
1. Usar `/heartless event challenges` para ver desafíos disponibles
2. Combinar con `/heartless event status` para información completa del evento
3. Consultar regularmente durante eventos activos para planificar estrategias

### Para Administradores
- El comando es seguro para uso público
- No requiere permisos especiales
- Proporciona información útil sin revelar datos sensibles del servidor

## Notas de Desarrollo

### Futuras Mejoras Posibles
1. **Estado de Progreso:** Mostrar qué desafíos ha completado cada jugador
2. **Tiempo Restante:** Integrar información de tiempo restante del evento
3. **Recompensas Dinámicas:** Sistema de recompensas configurable desde archivos
4. **Localización:** Soporte para múltiples idiomas

### Consideraciones de Rendimiento
- Comando ligero que no impacta el rendimiento del servidor
- Información estática que no requiere consultas complejas
- Respuesta instantánea para mejor experiencia de usuario

## Validación

✅ **Compilación:** Proyecto compila sin errores  
✅ **Integración:** Comando registrado correctamente en EventControl  
✅ **Funcionalidad:** Lógica implementada para todos los tipos de eventos  
✅ **Documentación:** Código comentado y documentado  
✅ **Compatibilidad:** No rompe funcionalidad existente  

---

**Fecha de Implementación:** Enero 2025  
**Desarrollador:** Sistema de IA especializado en plugins de Minecraft  
**Estado:** Completado y listo para uso