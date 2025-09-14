# Guía de Migración - DatabaseManager

## Resumen de Cambios

El plugin Heartless ha sido actualizado con un nuevo **DatabaseManager** que unifica y centraliza todas las operaciones de almacenamiento y persistencia. Este sistema reemplaza los múltiples sistemas de almacenamiento anteriores y proporciona mejor rendimiento, consistencia y mantenibilidad.

## Características Principales

### ✅ Sistema Unificado
- **Centralización**: Todas las operaciones de base de datos ahora pasan por DatabaseManager
- **Compatibilidad**: Mantiene compatibilidad total con el uso actual
- **Fallback**: Sistema automático de respaldo a JSON si la base de datos falla

### ✅ Optimización de Rendimiento
- **HikariCP**: Pool de conexiones de alto rendimiento
- **Cache Inteligente**: Sistema de cache con expiración automática
- **Operaciones Asíncronas**: Todas las operaciones de I/O son no-bloqueantes

### ✅ Consistencia de Configuración
- **Getters Unificados**: Todos los sistemas ahora usan getters de ConfigManager
- **Recarga Automática**: Los cambios de configuración se propagan automáticamente
- **Validación**: Verificación de configuración al inicio

## Migración Automática

### Datos Existentes
El nuevo sistema es **100% compatible** con los datos existentes:

- **JSON**: Los archivos JSON existentes se mantienen como respaldo
- **Configuración**: La configuración actual sigue funcionando
- **Comandos**: Todos los comandos existentes funcionan sin cambios

### Proceso de Migración

1. **Instalación**: Simplemente reemplaza el archivo JAR del plugin
2. **Configuración**: Opcionalmente configura la base de datos en `config.yml`
3. **Reinicio**: Reinicia el servidor
4. **Verificación**: Los datos se migran automáticamente

## Configuración de Base de Datos

### Configuración Básica (config.yml)

```yaml
database:
  enabled: true  # false para usar solo JSON
  host: "localhost"
  port: 3306
  database: "heartless_db"
  username: "heartless_user"
  password: "tu_contraseña"
  pool:
    size: 10
    connection-timeout: 30000
```

### Configuración Avanzada

Ver `database-config-example.yml` para todas las opciones disponibles.

## Sistemas Actualizados

### ConfigManager
**Nuevos Getters Añadidos:**
- `getDatabasePoolSize()`
- `getDatabaseConnectionTimeout()`
- `getBanDefaultDuration()`
- `getBanDefaultDurationLong()`
- `isBanBroadcast()`
- `getBanMessage()`
- `getBanBroadcastMessage()`

### BanManager
**Cambios Implementados:**
- Ahora usa getters de ConfigManager para todas las configuraciones
- Duración de baneo configurable desde config.yml
- Mensajes personalizables
- Verificación automática si el sistema está habilitado

### StorageAdapter
**Nueva Clase:**
- Actúa como puente entre sistemas antiguos y nuevos
- Proporciona compatibilidad hacia atrás
- Maneja fallback automático en caso de errores

## Beneficios del Nuevo Sistema

### 🚀 Rendimiento
- **Pool de Conexiones**: HikariCP optimiza las conexiones a la base de datos
- **Cache Inteligente**: Reduce consultas repetitivas
- **Operaciones Asíncronas**: No bloquea el hilo principal del servidor

### 🔧 Mantenimiento
- **Código Centralizado**: Todas las operaciones de DB en un solo lugar
- **Logs Mejorados**: Sistema de logging más detallado y configurable
- **Tests Incluidos**: Suite completa de tests para garantizar estabilidad

### 🛡️ Confiabilidad
- **Fallback Automático**: Si la DB falla, usa JSON automáticamente
- **Validación de Datos**: Verificación de integridad de datos
- **Manejo de Errores**: Gestión robusta de errores y recuperación

## Comandos de Administración

### Comandos Existentes (Sin Cambios)
- `/heartless reload` - Recarga la configuración
- `/heartless status` - Muestra el estado del plugin

### Nuevas Funcionalidades
- **Recarga Automática**: Los cambios de configuración se aplican automáticamente
- **Diagnósticos**: Información detallada sobre el estado de la base de datos

## Solución de Problemas

### Base de Datos No Conecta
1. Verifica las credenciales en `config.yml`
2. Asegúrate de que el servidor de DB esté ejecutándose
3. Revisa los logs para errores específicos
4. El sistema usará JSON automáticamente como respaldo

### Datos No Se Guardan
1. Verifica permisos de escritura en la carpeta del plugin
2. Revisa los logs para errores de I/O
3. Verifica espacio en disco disponible

### Rendimiento Lento
1. Ajusta el tamaño del pool de conexiones
2. Habilita el cache si está deshabilitado
3. Verifica la latencia de red a la base de datos

## Logs y Debugging

### Configuración de Debug
```yaml
debug:
  enabled: true
  log-database-operations: true
  log-cache-operations: true
  performance-monitoring: true
```

### Interpretación de Logs
- `[DatabaseManager]`: Operaciones de base de datos
- `[Cache]`: Operaciones de cache
- `[Fallback]`: Cuando se usa JSON como respaldo
- `[Performance]`: Métricas de rendimiento

## Compatibilidad

### Versiones Soportadas
- **Minecraft**: 1.16.5 - 1.21+
- **Java**: 21+
- **Spigot/Paper**: Últimas versiones
- **MySQL/MariaDB**: 5.7+

### Plugins Compatibles
- **PlaceholderAPI**: Totalmente compatible
- **Otros plugins**: Sin cambios en la API pública

## Migración de Datos Personalizados

Si tienes datos personalizados o modificaciones:

1. **Backup**: Siempre haz backup antes de actualizar
2. **Formato JSON**: Los datos JSON existentes se mantienen
3. **API**: La API pública no ha cambiado
4. **Consulta**: Contacta al desarrollador para casos específicos

## Soporte

Para soporte técnico:
- Revisa los logs del servidor
- Incluye la configuración (sin contraseñas)
- Describe el comportamiento esperado vs actual
- Menciona la versión del plugin y servidor

---

**Nota**: Esta migración es completamente automática y no requiere intervención manual. El sistema está diseñado para ser "plug and play" manteniendo toda la funcionalidad existente mientras añade las nuevas capacidades.