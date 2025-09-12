# Test de Inicialización de WeeklyEventTaskOptimizer y WeeklyEventMigrationManager

## Cambios Realizados

### 1. Agregadas las importaciones necesarias en HeartlessMain.java:
```java
import com.darkbladedev.managers.WeeklyEventTaskOptimizer;
import com.darkbladedev.managers.WeeklyEventMigrationManager;
```

### 2. Declaradas las variables estáticas:
```java
private static WeeklyEventTaskOptimizer taskOptimizer;
private static WeeklyEventMigrationManager migrationManager;
```

### 3. Inicializadas las instancias en onEnable():
```java
taskOptimizer = new WeeklyEventTaskOptimizer(instance);
migrationManager = new WeeklyEventMigrationManager(instance);
```

### 4. Agregada la inicialización en initializeSystems():
```java
// Inicializar optimizador de tareas
taskOptimizer.start();

// Verificar y ejecutar migraciones si es necesario
if (migrationManager.needsMigration()) {
    getLogger().info("Ejecutando migraciones de eventos semanales...");
    migrationManager.performMigration();
}
```

### 5. Agregados métodos getter:
```java
public static WeeklyEventTaskOptimizer getTaskOptimizer() {
    return taskOptimizer;
}

public static WeeklyEventMigrationManager getMigrationManager() {
    return migrationManager;
}
```

### 6. Agregada limpieza en onDisable():
```java
// Detener optimizador de tareas
if (taskOptimizer != null) {
    taskOptimizer.stop();
}
```

## Funcionalidades Ahora Activas

### WeeklyEventTaskOptimizer:
- ✅ Pool de hilos optimizado (2-8 hilos)
- ✅ Monitoreo de rendimiento cada 30 segundos
- ✅ Limpieza automática cada 5 minutos
- ✅ Gestión de memoria y CPU
- ✅ Métricas de tareas ejecutadas
- ✅ Prevención de memory leaks

### WeeklyEventMigrationManager:
- ✅ Verificación automática de migraciones necesarias
- ✅ Backup automático antes de migraciones
- ✅ Mapeo de eventos antiguos a nuevos
- ✅ Validación de configuraciones
- ✅ Rollback en caso de errores

## Verificación

1. **Compilación**: ✅ Exitosa
2. **Tests**: ✅ Todos los tests pasan
3. **JAR generado**: ✅ heartless-1.0.0.jar creado

## Próximos Pasos

1. Probar el plugin en un servidor de desarrollo
2. Verificar logs de inicialización
3. Confirmar que las migraciones se ejecuten si es necesario
4. Monitorear el rendimiento del optimizador de tareas