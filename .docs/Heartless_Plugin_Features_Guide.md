# Guía de Características del Plugin Heartless
**Documento para Beta-Testers**

---

## 📋 Información General

El plugin **Heartless** es un sistema de eventos semanales que transforma completamente la experiencia de supervivencia en Minecraft. Cada semana se activa automáticamente un evento aleatorio que modifica las mecánicas del juego, creando desafíos únicos y emocionantes para los jugadores.

### Sistema de Eventos
- Los eventos se activan automáticamente de forma aleatoria
- Cada evento tiene una duración configurable
- Los eventos pueden pausarse y reanudarse
- El progreso se guarda entre reinicios del servidor
- Solo un evento puede estar activo a la vez

---

## 🌟 Eventos Semanales Disponibles

### 1. **Semana Ácida** (Acid Week)
**Descripción:** El mundo se vuelve tóxico y corrosivo, afectando tanto a jugadores como a sus equipos.

**Mecánicas principales:**
- **Daño por agua:** Estar en contacto con agua causa daño constante (2 corazones por segundo)
- **Lluvia ácida:** Durante tormentas, los jugadores reciben daño cada 5 segundos si están expuestos
- **Deterioro acelerado:** Todas las herramientas y armaduras se desgastan 2 veces más rápido
- **Crecimiento acelerado:** Los cultivos crecen más rápido debido a la "fertilización" ácida
- **Protección:** Los jugadores pueden refugiarse bajo techo para evitar la lluvia ácida

**Estrategias de supervivencia:**
- Construir refugios impermeables
- Evitar nadar o caminar por agua
- Stockear herramientas y armaduras extra
- Aprovechar el crecimiento acelerado de cultivos

---

### 2. **Niebla Tóxica** (Toxic Fog)
**Descripción:** Una niebla venenosa envuelve el mundo, afectando a todos los jugadores expuestos.

**Mecánicas principales:**
- **Efectos de veneno:** Los jugadores reciben efectos de veneno constantes
- **Visibilidad reducida:** La niebla limita la visión
- **Daño progresivo:** El daño aumenta cuanto más tiempo estés expuesto
- **Refugios seguros:** Ciertas estructuras o biomas pueden ofrecer protección

**Estrategias de supervivencia:**
- Buscar refugios en cuevas o estructuras cerradas
- Llevar pociones de curación
- Moverse en grupos para mayor seguridad
- Construir bases herméticas

---

### 3. **Semana de los No Muertos** (Undead Week)
**Descripción:** Los muertos vivientes dominan el mundo con mecánicas especiales y lunas rojas.

**Mecánicas principales:**
- **Luna Roja:** Evento especial que ocurre cada cierto número de noches
- **Spawneo aumentado:** Mayor cantidad de zombies y esqueletos
- **Infección zombie:** Los jugadores pueden infectarse con un virus zombie especial
- **Resistencia de no-muertos:** Los mobs zombies son más resistentes
- **Efectos nocturnos:** Las noches son más peligrosas y largas

**Características de la Luna Roja:**
- Spawneo masivo de mobs hostiles
- Efectos visuales especiales en el cielo
- Mayor dificultad durante toda la noche
- Recompensas especiales por sobrevivir

**Estrategias de supervivencia:**
- Fortificar bases antes de las noches
- Preparar suministros médicos para la infección
- Evitar salir durante las lunas rojas
- Trabajar en equipo para defenderse

---

### 4. **Semana Explosiva** (Explosive Week)
**Descripción:** El mundo se vuelve extremadamente volátil con explosiones por todas partes.

**Mecánicas principales:**
- **Creepers cargados:** Todos los creepers spawean como creepers cargados (más poderosos)
- **Ghasts en el overworld:** Los ghasts pueden aparecer durante tormentas nocturnas
- **TNT potenciado:** Las explosiones de TNT son 40% más poderosas
- **Minerales explosivos:** 10% de probabilidad de que los minerales exploten al minarlos
- **Iron Golems explosivos:** Los Iron Golems explotan al morir
- **Ataques de ghast frecuentes:** Los ghasts atacan más seguido

**Desafíos especiales:**
- Matar un ghast en el overworld
- Coleccionar cabezas de mobs clásicos
- Matar jugadores con explosiones
- Hacer que un creeper cargado mate a un warden

**Estrategias de supervivencia:**
- Construir bases a prueba de explosiones
- Evitar minar durante tormentas
- Llevar armadura de protección contra explosiones
- Mantenerse alejado de creepers y ghasts

---

### 5. **Semana de Sangre y Hierro** (Blood and Iron Week)
**Descripción:** Un evento centrado en combate PvP con mecánicas que favorecen la agresión.

**Mecánicas principales:**
- **Penalización por armadura pesada:** Diamante y netherite causan lentitud y fatiga minera
- **Penalización por espadas pesadas:** Espadas de diamante y netherite causan náuseas
- **Sistema de rachas de kills:** Matar jugadores consecutivamente otorga beneficios
- **Timeout de kills:** Los kills de mobs y jugadores tienen tiempo límite para mantener rachas
- **Efectos de adrenalina:** Los jugadores activos en combate reciben beneficios

**Sistema de desafíos:**
- Matar 3 jugadores para obtener encantamiento de adrenalina
- Sobrevivir todo el evento sin morir
- Conseguir 5 kills consecutivos (pentakill)
- Matar con daño instantáneo

**Estrategias de supervivencia:**
- Usar armadura ligera (hierro o menos)
- Preferir armas ligeras y rápidas
- Mantener distancia en combate
- Formar alianzas temporales

---

## 🧟 Efecto Especial: Infección Zombie

### Descripción General
La **Infección Zombie** es un efecto personalizado que simula estar infectado con un virus zombie. Los efectos cambian según la hora del día, creando una experiencia dinámica y desafiante.

### Mecánicas por Tiempo del Día

#### 🌅 **Amanecer/Atardecer** (0-3000 y 12000-14000 ticks)
- **Efectos:** Náuseas y hambre leves
- **Duración:** Efectos constantes pero manejables
- **Mensaje:** "La infección zombie te debilita durante este momento del día..."

#### ☀️ **Mediodía** (5000-11000 ticks)
- **Luz solar directa:**
  - Efecto de marchitamiento
  - El jugador se prende fuego
  - Partículas de lava
  - **Mensaje:** "¡La luz directa del sol quema tu piel infectada!"
- **Bajo sombra:**
  - Solo náuseas y hambre
  - Sin daño por fuego
  - **Mensaje:** "La infección te debilita, pero estás a salvo de la luz solar directa."

#### 🌙 **Noche** (14001-24000 ticks)
- **Efectos positivos:** Fuerza nivel 1
- **Sin efectos negativos:** Se eliminan náuseas, hambre y fuego
- **Mensaje:** "La oscuridad de la noche fortalece tu infección..."

#### 🌤️ **Transición** (3001-4999 y 11001-11999 ticks)
- **Efectos leves:** Náuseas y hambre reducidas
- **Sin efectos extremos:** No hay fuego ni fuerza
- **Mensaje:** "La infección fluctúa durante este período de transición..."

### Mecánicas de Contagio
- **Transmisión:** Golpear a otro jugador sin arma en la mano
- **Probabilidad base:** 50% de infectar al golpear
- **Resistencia:** Jugadores curados 5+ veces tienen 25% de probabilidad
- **Protección zombie:** 60% de probabilidad de que los zombies ignoren a jugadores infectados

### Sistema de Curación
- **Método:** Consumir manzana dorada o manzana dorada encantada
- **Efecto:** Eliminación inmediata de todos los efectos
- **Contador:** Se registra cada curación para el sistema de resistencia
- **Sonido:** Sonido de curación de aldeano zombie

### Efectos Visuales
- **Aplicación:** Partículas de aldeano enojado + sonido de zombie
- **Constantes:** Partículas de humo sobre el jugador
- **Curación:** Partículas de aldeano feliz + sonido de curación
- **Fuego solar:** Partículas de lava adicionales

### Sistema de Resistencia
- **Requisito:** Ser curado 5 o más veces
- **Beneficio:** Reducción del 50% en probabilidad de reinfección
- **Persistencia:** Se mantiene entre sesiones del servidor

---

## 🎯 Sistema de Desafíos

Cada evento incluye desafíos específicos que los jugadores pueden completar para obtener recompensas especiales. Los desafíos varían según el evento activo y pueden incluir:

- Sobrevivir condiciones extremas
- Eliminar mobs específicos
- Coleccionar objetos raros
- Completar objetivos de combate
- Lograr hazañas de supervivencia

---

## 📊 Comandos y Placeholders

El plugin incluye integración con PlaceholderAPI para mostrar información del evento actual:

- **%heartless_event_active%** - Si hay un evento activo
- **%heartless_event_name%** - Nombre del evento actual
- **%heartless_event_time_remaining%** - Tiempo restante del evento
- **%heartless_event_paused%** - Si el evento está pausado

---

## 🔧 Notas para Beta-Testing

### Aspectos a Probar
1. **Persistencia:** Verificar que los eventos se guarden correctamente entre reinicios
2. **Transiciones:** Comprobar que los cambios de tiempo del día funcionen suavemente
3. **Balance:** Evaluar si los eventos son desafiantes pero justos
4. **Rendimiento:** Monitorear el impacto en el rendimiento del servidor
5. **Bugs:** Reportar cualquier comportamiento inesperado o errores

### Reportes Importantes
- Errores de consola relacionados con eventos
- Problemas de sincronización entre jugadores
- Efectos que no se aplican o remueven correctamente
- Problemas de balance en dificultad
- Sugerencias de mejora para mecánicas existentes

---

**¡Gracias por participar en las pruebas beta del plugin Heartless!**

*Recuerda reportar cualquier problema o sugerencia para mejorar la experiencia de juego.*