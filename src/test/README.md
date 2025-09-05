# Sistema de Pruebas Heartless

Sistema de pruebas automatizadas completo para el plugin Heartless de Minecraft, diseñado para validar la funcionalidad de todos los componentes críticos del plugin.

## 🚀 Características

- **Framework de Testing Personalizado**: Sistema de pruebas robusto y extensible
- **Ejecución Flexible**: Pruebas individuales, suites o todas las pruebas
- **Reportes Detallados**: Generación automática de reportes en HTML, texto y CSV
- **Interfaz de Consola**: Modo interactivo y línea de comandos
- **Mocks Completos**: Simulación de entidades y objetos de Bukkit/Spigot
- **Configuración Avanzada**: Ejecución paralela, timeouts y personalización

## 📁 Estructura del Proyecto

```
src/test/
├── java/com/darkbladedev/testing/
│   ├── console/
│   │   └── TestConsole.java          # Interfaz de consola principal
│   ├── mocks/
│   │   ├── MockPlayer.java           # Mock de Player de Bukkit
│   │   ├── MockEntities.java         # Mocks de entidades (Villager, Zombie, etc.)
│   │   └── MockSupport.java          # Mocks de soporte adicionales
│   ├── tests/
│   │   ├── StorageManagerTest.java   # Pruebas de persistencia
│   │   ├── WeeklyEventManagerTest.java # Pruebas de gestión de eventos
│   │   └── UndeadWeekTest.java       # Pruebas del evento UndeadWeek
│   ├── utils/
│   │   └── TestUtils.java            # Utilidades para pruebas
│   ├── TestCase.java                 # Clase base para casos de prueba
│   ├── TestResult.java               # Encapsulación de resultados
│   ├── TestSuite.java                # Agrupación de pruebas
│   ├── TestSuiteResult.java          # Resultados de suites
│   ├── TestRunner.java               # Ejecutor principal
│   ├── TestReporter.java             # Generador de reportes
│   └── TestLauncher.java             # Punto de entrada
└── README.md                         # Esta documentación
```

## 🛠️ Instalación y Configuración

### Prerrequisitos

- Java 17 o superior
- Gradle (incluido wrapper en el proyecto)
- Plugin Heartless compilado

### Compilación

```bash
# Compilar el proyecto completo (incluyendo pruebas)
./gradlew compileTestJava

# O usar el script incluido
./run-tests.sh  # Linux/Mac
run-tests.bat   # Windows
```

## 🎯 Uso del Sistema

### Modo Interactivo

```bash
# Iniciar modo interactivo
java -cp "build/classes/java/test:build/classes/java/main" com.darkbladedev.testing.TestLauncher

# O usar los scripts
./run-tests.sh     # Linux/Mac
run-tests.bat      # Windows
```

### Línea de Comandos

```bash
# Ejecutar todas las pruebas
java -cp "..." com.darkbladedev.testing.TestLauncher run-all

# Ejecutar una suite específica
java -cp "..." com.darkbladedev.testing.TestLauncher run-suite storage

# Ejecutar una prueba específica
java -cp "..." com.darkbladedev.testing.TestLauncher run-test StorageManagerTest

# Listar pruebas disponibles
java -cp "..." com.darkbladedev.testing.TestLauncher list
```

### Usando Scripts

```bash
# Scripts incluidos para facilitar el uso
./run-tests.sh run-all                    # Linux/Mac
run-tests.bat run-suite events             # Windows
```

## 📊 Suites de Pruebas Disponibles

### 1. Storage Suite (`storage`)
**Pruebas de persistencia y almacenamiento**
- Guardado y carga básicos
- Persistencia de datos de eventos
- Contador de aldeanos curados
- Manejo de datos corruptos
- Acceso concurrente
- Sistema de respaldos

### 2. Events Suite (`events`)
**Pruebas de gestión de eventos semanales**
- Registro de eventos
- Inicio y parada de eventos
- Transiciones entre eventos
- Programación automática
- Manejo de múltiples eventos
- Persistencia de estado
- Callbacks y notificaciones

### 3. Undead Suite (`undead`)
**Pruebas específicas del evento UndeadWeek**
- Inicialización del evento
- Infección de jugadores
- Curación de aldeanos
- Mecánicas de luna roja
- Completación de desafíos
- Estadísticas de jugadores
- Persistencia de progreso

### 4. Integration Suite (`integration`)
**Pruebas de integración completa**
- Todas las pruebas anteriores
- Interacciones entre componentes
- Flujos de trabajo completos

## 🎮 Comandos de Consola Interactiva

| Comando | Alias | Descripción |
|---------|-------|-------------|
| `help` | `h` | Muestra ayuda |
| `list` | `l` | Lista pruebas disponibles |
| `run-all` | `ra` | Ejecuta todas las pruebas |
| `run-suite <nombre>` | `rs` | Ejecuta suite específica |
| `run-test <nombre>` | `rt` | Ejecuta prueba específica |
| `config` | `c` | Menú de configuración |
| `reports` | `r` | Gestión de reportes |
| `clear` | `cls` | Limpia pantalla |
| `exit` | `q` | Sale del programa |

### Ejemplos de Uso Interactivo

```
test> list
test> run-suite storage
test> run-test UndeadWeekTest
test> config
test> reports
```

## ⚙️ Configuración Avanzada

### Opciones de Configuración

- **Stack Traces**: Mostrar/ocultar stack traces completos
- **Colores**: Activar/desactivar colores en la salida
- **Ejecución Paralela**: Ejecutar pruebas en paralelo
- **Hilos Máximos**: Número de hilos para ejecución paralela
- **Timeout**: Tiempo máximo de espera por prueba
- **Directorio de Reportes**: Ubicación de los reportes generados

### Configuración desde Código

```java
TestRunner runner = new TestRunner();
runner.setParallelExecution(true);
runner.setMaxThreads(4);
runner.setTimeoutSeconds(30);
runner.setStopOnFirstFailure(false);

TestReporter reporter = new TestReporter();
reporter.setShowStackTrace(true);
reporter.setUseColors(true);
reporter.setOutputDirectory(new File("custom-reports"));
```

## 📈 Reportes y Resultados

### Tipos de Reportes

1. **Reporte HTML**: Interfaz web interactiva con gráficos
2. **Reporte de Texto**: Formato legible para consola
3. **Reporte CSV**: Datos estructurados para análisis

### Ubicación de Reportes

Por defecto: `test-reports/`

### Formato de Nombres

```
[suite-name]_[timestamp].[extension]

Ejemplos:
all-tests_2024-01-15_14-30-25.html
storage_2024-01-15_14-32-10.txt
events_2024-01-15_14-35-45.csv
```

### Gestión de Reportes

```
test> reports
1. Ver reportes existentes
2. Limpiar reportes antiguos
3. Cambiar directorio de reportes
```

## 🧪 Creación de Nuevas Pruebas

### Estructura Básica

```java
public class MiNuevaPrueba extends TestCase {
    
    @Override
    protected void setUp() throws Exception {
        // Configuración inicial
    }
    
    @Override
    protected void tearDown() throws Exception {
        // Limpieza
    }
    
    @Override
    public void runTest() throws Exception {
        // Lógica de la prueba
        assertTrue("Condición debe ser verdadera", condicion);
        assertEquals("Valores deben ser iguales", esperado, actual);
        assertNotNull("Objeto no debe ser null", objeto);
    }
    
    @Override
    public String getTestName() {
        return "Mi Nueva Prueba";
    }
}
```

### Usando Mocks

```java
// Mock de jugador
MockPlayer player = new MockPlayer("TestPlayer");
player.setHealth(20.0);
player.setOnline(true);

// Mock de aldeano
MockVillager villager = new MockVillager();
villager.setProfession(Villager.Profession.FARMER);

// Mock de zombie
MockZombie zombie = new MockZombie();
zombie.setTarget(player);
```

### Utilidades Disponibles

```java
// Crear directorio temporal
File tempDir = TestUtils.createTempDirectory("mi-prueba");

// Generar datos aleatorios
UUID playerId = TestUtils.generateRandomUUID();
String playerName = TestUtils.generateRandomPlayerName();

// Simular retrasos
TestUtils.simulateDelay(1000); // 1 segundo

// Validar rangos
TestUtils.assertInRange(valor, 1, 10);

// Formatear tiempo
String tiempo = TestUtils.formatTime(milisegundos);
```

## 🔧 Solución de Problemas

### Problemas Comunes

#### Error de Compilación
```bash
# Limpiar y recompilar
./gradlew clean compileTestJava
```

#### ClassNotFoundException
```bash
# Verificar classpath
./gradlew -q printClasspath
```

#### Pruebas Lentas
```java
// Activar ejecución paralela
testRunner.setParallelExecution(true);
testRunner.setMaxThreads(Runtime.getRuntime().availableProcessors());
```

#### Memoria Insuficiente
```bash
# Aumentar memoria JVM
export JAVA_OPTS="-Xmx2G -Xms1G"
```

### Logs de Debug

```java
// Activar logs detallados
System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
System.setProperty("test.debug", "true");
```

## 📝 Mejores Prácticas

### Diseño de Pruebas

1. **Una responsabilidad por prueba**: Cada prueba debe validar un aspecto específico
2. **Nombres descriptivos**: `testStorageManagerSavesPlayerDataCorrectly()`
3. **Configuración independiente**: Cada prueba debe ser autocontenida
4. **Limpieza adecuada**: Usar `tearDown()` para limpiar recursos

### Organización

1. **Agrupar por funcionalidad**: Usar suites lógicas
2. **Mocks reutilizables**: Crear mocks base para casos comunes
3. **Utilidades compartidas**: Centralizar funciones comunes en `TestUtils`
4. **Documentación**: Comentar pruebas complejas

### Performance

1. **Ejecución paralela**: Para suites grandes
2. **Timeouts apropiados**: Evitar pruebas colgadas
3. **Limpieza de recursos**: Evitar memory leaks
4. **Datos de prueba mínimos**: Solo lo necesario para validar

## 🤝 Contribución

### Añadir Nuevas Pruebas

1. Crear clase que extienda `TestCase`
2. Implementar métodos requeridos
3. Añadir a suite apropiada en `TestConsole`
4. Documentar en este README

### Reportar Problemas

1. Incluir logs completos
2. Especificar versión de Java
3. Detallar pasos para reproducir
4. Adjuntar reportes generados

## 📄 Licencia

Este sistema de pruebas es parte del plugin Heartless y está sujeto a la misma licencia del proyecto principal.

---

**Desarrollado por DarkBladeDev** | **Versión 1.0.0**