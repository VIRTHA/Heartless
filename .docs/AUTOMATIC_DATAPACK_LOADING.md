# Carga Automática de Datapacks - Plugin Heartless

## Resumen Técnico

El plugin Heartless implementa un sistema de carga automática de datapacks utilizando la Paper API y `LifecycleEvents.DATAPACK_DISCOVERY`. Esta funcionalidad permite que el datapack se incluya directamente en el JAR del plugin y se cargue automáticamente sin intervención manual.

## Implementación Técnica

### Estructura del Proyecto

```
src/main/resources/
└── heartless_datapack/
    ├── pack.mcmeta
    └── data/
        └── heartless/
            └── tags/
                └── items/
                    ├── all_food.json
                    └── consumable_food.json
```

### Código de Implementación

El sistema se implementa en la clase `Bootstraps.java` que actúa como `PluginBootstrap`:

```java
@Override
public void bootstrap(@NotNull BootstrapContext context) {
    // Register automatic datapack loading
    context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY, event -> {
        // Discover and register the heartless datapack from resources
        try {
            URI uri = Objects.requireNonNull(getClass().getResource("/heartless_datapack")).toURI();
            event.registrar().discoverPack(uri, "heartless_datapack");
            context.getLogger().info("Successfully discovered heartless datapack from plugin resources");
        } catch (URISyntaxException | IOException e) {
            context.getLogger().error("Failed to discover heartless datapack: " + e.getMessage());
            throw new RuntimeException("Failed to load heartless datapack", e);
        }
    });
    
    // ... resto del código de registro de encantamientos
}
```

## Ventajas del Sistema

### 1. **Automatización Completa**
- No requiere intervención manual del administrador
- Se carga automáticamente al iniciar el servidor
- Compatible con reinicios y recargas

### 2. **Distribución Simplificada**
- El datapack se incluye en el JAR del plugin
- No hay archivos adicionales que descargar
- Actualizaciones automáticas junto con el plugin

### 3. **Compatibilidad Universal**
- Funciona en todos los mundos del servidor
- Compatible con otros datapacks
- No interfiere con configuraciones existentes

### 4. **Gestión de Errores**
- Logging detallado de la carga del datapack
- Manejo de excepciones robusto
- Información clara en caso de fallos

## Configuración del paper-plugin.yml

El archivo `paper-plugin.yml` debe incluir la referencia al bootstrapper:

```yaml
name: Heartless
version: 1.0.0
main: com.darkbladedev.HeartlessMain
api-version: '1.21'
bootstrapper: com.darkbladedev.content.custom.Bootstraps
```

## Verificación de Funcionamiento

### Logs del Servidor
Al iniciar el servidor, deberías ver:
```
[INFO] Successfully discovered heartless datapack from plugin resources
```

### Comando de Verificación
```
/datapack list
```
Resultado esperado: `[heartless_datapack]` en la lista de datapacks habilitados.

### Verificación de Tags
```
/tag @s add heartless:all_food
```
Si el comando funciona sin errores, el datapack está cargado correctamente.

## Resolución de Problemas

### Error: "Failed to discover heartless datapack"
- **Causa**: El directorio `heartless_datapack` no se encuentra en `src/main/resources/`
- **Solución**: Verificar que la estructura de directorios sea correcta

### Error: "NoClassDefFoundError: LifecycleEvents"
- **Causa**: Versión incompatible de Paper API
- **Solución**: Actualizar a Paper 1.21+ y verificar dependencias

### El datapack no aparece en `/datapack list`
- **Causa**: Error en el formato del `pack.mcmeta` o estructura de archivos
- **Solución**: Verificar que `pack.mcmeta` tenga el formato correcto y `pack_format: 48`

## Migración desde Instalación Manual

Si previamente tenías el datapack instalado manualmente:

1. **Eliminar datapack manual**: Borra la carpeta del datapack de `world/datapacks/`
2. **Actualizar plugin**: Instala la nueva versión con carga automática
3. **Reiniciar servidor**: El datapack se cargará automáticamente
4. **Verificar**: Usar `/datapack list` para confirmar la carga

## Notas para Desarrolladores

- El sistema utiliza `URI` para localizar el datapack en los recursos del JAR
- La carga se realiza durante la fase `DATAPACK_DISCOVERY` del ciclo de vida del plugin
- El manejo de excepciones es crítico para evitar fallos en el inicio del servidor
- El logging permite diagnosticar problemas de carga fácilmente