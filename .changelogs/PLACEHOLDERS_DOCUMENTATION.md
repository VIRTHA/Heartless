# Documentación de Placeholders - Heartless Plugin

## Descripción General

El plugin Heartless proporciona una integración completa con PlaceholderAPI que permite mostrar información en tiempo real sobre eventos semanales, estado de jugadores y progreso de desafíos.

**Identificador del plugin:** `heartless`

---

## Placeholders Básicos de Eventos

### Estado del Evento

| Placeholder | Descripción | Valores Posibles |
|-------------|-------------|------------------|
| `%heartless_event_active%` | Indica si hay un evento activo | `Sí` / `No` |
| `%heartless_event_paused%` | Indica si el evento está pausado | `Sí` / `No` |
| `%heartless_event_name%` | Nombre del evento actual | `Ácida`, `Niebla Tóxica`, `No-Muertos`, `Paranoia`, `Explosiva`, `Sangre y Hierro`, `Ninguno` |

### Tiempo y Progreso

| Placeholder | Descripción | Ejemplo |
|-------------|-------------|----------|
| `%heartless_event_time_remaining%` | Tiempo restante del evento (formato largo) | `2 días, 5 horas, 30 minutos` |
| `%heartless_event_time_remaining_short%` | Tiempo restante del evento (formato corto) | `2d 5h` / `5h 30m` / `30m` |
| `%heartless_event_progress_percent%` | Porcentaje de progreso del evento | `45%` |

---

## Placeholders Específicos por Evento

### Formato General
`%heartless_event_specific_[identificador]%`

### Eventos Disponibles

#### 1. Semana No-Muertos (`undead_week`)
- `%heartless_event_specific_[identificador]%` - Actualmente devuelve `N/A` (pendiente de implementación)

#### 2. Semana Ácida (`acid_week`)
- `%heartless_event_specific_[identificador]%` - Actualmente devuelve `N/A` (pendiente de implementación)

#### 3. Semana Explosiva (`explosive_week`)
- `%heartless_event_specific_challenges_completed%` - Desafíos completados (pendiente de implementación)

#### 4. Semana Sangre y Hierro (`blood_and_iron_week`)
- `%heartless_event_specific_player_kills%` - Jugadores eliminados (pendiente de implementación)
- `%heartless_event_specific_consecutive_kills%` - Eliminaciones consecutivas (pendiente de implementación)

---

## Placeholders de Desafíos

### Formato General
`%heartless_challenge_completed_[id_desafio]%`

### Descripción
- **Función:** Verifica si un jugador ha completado un desafío específico
- **Valores:** `true` / `false` / `error`
- **Eventos compatibles:** Todos los eventos que implementen el sistema de desafíos

### Ejemplos
```
%heartless_challenge_completed_first_kill%
%heartless_challenge_completed_survival_master%
%heartless_challenge_completed_infection_curer%
```

---

## Placeholders de Estado del Jugador

### Estado de Infección

| Placeholder | Descripción | Valores Posibles |
|-------------|-------------|------------------|
| `%heartless_status_infection%` | Estado de infección zombie del jugador | `Infectado` / `Sano` |
| `%heartless_status_infection_cure_count%` | Número de infecciones curadas por el jugador | Número entero (ej: `5`) |

---

## Mapeo de Nombres de Eventos

| Nombre Interno | Nombre Mostrado |
|----------------|------------------|
| `acid_week` | Ácida |
| `toxic_fog` | Niebla Tóxica |
| `undead_week` | No-Muertos |
| `paranoia_effect` | Paranoia |
| `explosive_week` | Explosiva |
| `blood_and_iron_week` | Sangre y Hierro |

---

## Características Técnicas

### Actualización en Tiempo Real
- **Frecuencia:** Los placeholders se actualizan constantemente sin caché
- **Método de refresco:** `refreshPlaceholders()` disponible para actualización manual
- **Persistencia:** Los placeholders persisten durante recargas del servidor

### Manejo de Errores
- **Jugador nulo:** Devuelve cadena vacía `""`
- **Evento inactivo:** Devuelve valores por defecto (`"No hay evento activo"`, `"N/A"`, etc.)
- **Placeholder no encontrado:** Devuelve `null`
- **Error en desafíos:** Devuelve `"error"`

### Formato de Tiempo

#### Formato Largo (`event_time_remaining`)
- **Días:** `X día` / `X días`
- **Horas:** `X hora` / `X horas`
- **Minutos:** `X minuto` / `X minutos`
- **Segundos:** `X segundo` / `X segundos` (solo si no hay días ni horas)
- **Ejemplo:** `"2 días, 5 horas, 30 minutos"`

#### Formato Corto (`event_time_remaining_short`)
- **Con días:** `XdYh` (ej: `"2d 5h"`)
- **Con horas:** `XhYm` (ej: `"5h 30m"`)
- **Solo minutos:** `Xm` (ej: `"30m"`)

---

## Ejemplos de Uso

### En Chat/Mensajes
```
¡Evento actual: %heartless_event_name%!
Tiempo restante: %heartless_event_time_remaining%
Progreso: %heartless_event_progress_percent%
```

### En Scoreboards
```
&6Evento: &f%heartless_event_name%
&6Tiempo: &f%heartless_event_time_remaining_short%
&6Estado: &f%heartless_status_infection%
```

### En GUIs/Menús
```
&7Desafío completado: %heartless_challenge_completed_first_kill%
&7Infecciones curadas: %heartless_status_infection_cure_count%
```

---

## Notas de Desarrollo

### Placeholders Pendientes de Implementación
- Estadísticas específicas de eventos (marcadas como `N/A`)
- Métricas detalladas de jugadores por evento
- Sistema de rankings y leaderboards

### Dependencias
- **PlaceholderAPI:** Requerida para el funcionamiento
- **Bukkit/Spigot:** API base del servidor
- **Plugin Heartless:** Debe estar activo y funcionando

### Compatibilidad
- ✅ **Spigot/Paper:** Totalmente compatible
- ✅ **Versiones de MC:** 1.16+ (según dependencias del plugin principal)
- ✅ **PlaceholderAPI:** Versiones modernas

---

**Última actualización:** Enero 2025  
**Versión del plugin:** 1.0.0  
**Autor:** DarkBladeDev