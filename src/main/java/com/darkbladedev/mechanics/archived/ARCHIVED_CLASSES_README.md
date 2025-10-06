# Clases Archivadas - Mechanics

Este directorio contiene clases que han sido archivadas temporalmente para su posterior desactivación o revisión en el proyecto Heartless.

## Estado de las Clases Archivadas

### MobRain.java
- **Fecha de Archivo**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
- **Propósito Original**: Mecánica de evento que genera lluvia de entidades (mobs) alrededor de los jugadores
- **Funcionalidad Principal**: 
  - Spawneo controlado de entidades hostiles y pasivas
  - Sistema de límites por jugador y por tick para optimización de rendimiento
  - Compatibilidad con múltiples versiones de Minecraft
- **Razón del Archivo**: Desactivación temporal para revisión de rendimiento y balance
- **Estado**: INACTIVO - No se utiliza en el sistema de eventos actual
- **Dependencias**: Plugin principal, BukkitRunnable, EntityType

### ParanoiaEffect.java
- **Fecha de Archivo**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
- **Propósito Original**: Efecto especial que aplica paranoia a los jugadores mediante efectos visuales y auditivos
- **Funcionalidad Principal**:
  - Aplicación de efectos de poción (náusea) y brillo a jugadores
  - Reproducción de sonidos aleatorios inquietantes en posiciones aleatorias
  - Sistema de pausa/reanudación de efectos
  - Duración e intervalo configurables
- **Razón del Archivo**: Desactivación temporal para evaluación de impacto en la experiencia del jugador
- **Estado**: INACTIVO - No se utiliza en el sistema de eventos actual
- **Dependencias**: Plugin principal, BukkitRunnable, PotionEffect, Sound API, MM (MiniMessage)

## Notas Importantes

1. **Restauración**: Para reactivar cualquiera de estas clases, moverlas de vuelta al directorio `mechanics` principal y actualizar las referencias correspondientes.

2. **Compatibilidad**: Ambas clases fueron diseñadas para ser compatibles con versiones modernas de Minecraft (1.16+).

3. **Rendimiento**: Estas clases contienen optimizaciones específicas:
   - MobRain: Límites de entidades por tick y por jugador
   - ParanoiaEffect: Tareas separadas para efectos y sonidos

4. **Integración**: Ninguna de estas clases está actualmente integrada en el sistema de eventos semanales principal.

## Historial de Cambios

- **Inicial**: Clases archivadas desde el directorio principal de mechanics
- **Motivo**: Limpieza del código base y desactivación temporal de funcionalidades no utilizadas

---
*Generado automáticamente durante el proceso de archivado*