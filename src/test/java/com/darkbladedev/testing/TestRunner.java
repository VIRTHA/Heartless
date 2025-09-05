package com.darkbladedev.testing;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.io.File;
import java.net.URL;

/**
 * Clase principal del framework de testing que coordina la ejecución de pruebas.
 * Permite ejecutar pruebas individuales, suites completas y proporciona configuración avanzada.
 */
public class TestRunner {
    
    private final Map<String, TestCase> registeredTests;
    private final Map<String, TestSuite> registeredSuites;
    private final ExecutorService executorService;
    private final TestReporter reporter;
    
    // Configuración
    private boolean parallelExecution = false;
    private int maxThreads = Runtime.getRuntime().availableProcessors();
    private long timeoutMs = 30000; // 30 segundos por defecto
    private boolean stopOnFirstFailure = false;
    private boolean verboseOutput = false;
    
    public TestRunner() {
        this.registeredTests = new ConcurrentHashMap<>();
        this.registeredSuites = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.reporter = new TestReporter();
    }
    
    // === CONFIGURACIÓN ===
    
    /**
     * Configura la ejecución en paralelo.
     */
    public TestRunner setParallelExecution(boolean parallel) {
        this.parallelExecution = parallel;
        return this;
    }
    
    /**
     * Verifica si la ejecución en paralelo está habilitada.
     */
    public boolean isParallelExecution() {
        return this.parallelExecution;
    }
    
    /**
     * Configura el número máximo de hilos para ejecución paralela.
     */
    public TestRunner setMaxThreads(int threads) {
        this.maxThreads = Math.max(1, threads);
        return this;
    }
    
    /**
     * Obtiene el número máximo de hilos.
     */
    public int getMaxThreads() {
        return this.maxThreads;
    }
    
    /**
     * Configura el timeout por prueba en milisegundos.
     */
    public TestRunner setTimeout(long timeoutMs) {
        this.timeoutMs = Math.max(1000, timeoutMs);
        return this;
    }
    
    /**
     * Configura el timeout para las pruebas en segundos.
     */
    public TestRunner setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutMs = timeoutSeconds * 1000;
        return this;
    }
    
    /**
     * Obtiene el timeout en segundos.
     */
    public long getTimeoutSeconds() {
        return this.timeoutMs / 1000;
    }
    
    /**
     * Configura si detener la ejecución al primer fallo.
     */
    public TestRunner setStopOnFirstFailure(boolean stopOnFirstFailure) {
        this.stopOnFirstFailure = stopOnFirstFailure;
        return this;
    }
    
    /**
     * Configura la salida verbosa.
     */
    public TestRunner setVerboseOutput(boolean verbose) {
        this.verboseOutput = verbose;
        return this;
    }
    
    // === REGISTRO DE PRUEBAS ===
    
    /**
     * Registra una prueba individual.
     */
    public TestRunner registerTest(String name, TestCase testCase) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la prueba no puede estar vacío");
        }
        if (testCase == null) {
            throw new IllegalArgumentException("El caso de prueba no puede ser null");
        }
        
        registeredTests.put(name, testCase);
        if (verboseOutput) {
            System.out.println("[TestRunner] Registrada prueba: " + name);
        }
        return this;
    }
    
    /**
     * Registra una suite de pruebas.
     */
    public TestRunner registerSuite(String name, TestSuite suite) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la suite no puede estar vacío");
        }
        if (suite == null) {
            throw new IllegalArgumentException("La suite no puede ser null");
        }
        
        registeredSuites.put(name, suite);
        if (verboseOutput) {
            System.out.println("[TestRunner] Registrada suite: " + name + " (" + suite.getTestCount() + " pruebas)");
        }
        return this;
    }
    
    /**
     * Registra automáticamente todas las clases de prueba en un paquete.
     */
    public TestRunner autoRegisterTests(String packageName) {
        try {
            List<Class<?>> testClasses = findTestClasses(packageName);
            
            for (Class<?> clazz : testClasses) {
                if (TestCase.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                    try {
                        Constructor<?> constructor = clazz.getDeclaredConstructor();
                        TestCase testCase = (TestCase) constructor.newInstance();
                        registerTest(clazz.getSimpleName(), testCase);
                    } catch (Exception e) {
                        System.err.println("[TestRunner] Error al instanciar " + clazz.getSimpleName() + ": " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("[TestRunner] Error en auto-registro: " + e.getMessage());
        }
        
        return this;
    }
    
    // === EJECUCIÓN DE PRUEBAS ===
    
    /**
     * Ejecuta una prueba específica por nombre.
     */
    public TestResult runTest(String testName) {
        TestCase testCase = registeredTests.get(testName);
        if (testCase == null) {
            return TestResult.createFailedResult(testName, 
                    new RuntimeException("Prueba no encontrada: " + testName), 0);
        }
        
        if (verboseOutput) {
            System.out.println("[TestRunner] Ejecutando prueba: " + testName);
        }
        
        return executeTestCase(testName, testCase);
    }
    
    /**
     * Ejecuta múltiples pruebas por nombre.
     */
    public List<TestResult> runTests(String... testNames) {
        return runTests(Arrays.asList(testNames));
    }
    
    /**
     * Ejecuta múltiples pruebas por nombre.
     */
    public List<TestResult> runTests(List<String> testNames) {
        List<TestResult> results = new ArrayList<>();
        
        if (parallelExecution) {
            results = runTestsParallel(testNames);
        } else {
            results = runTestsSequential(testNames);
        }
        
        return results;
    }
    
    /**
     * Ejecuta todas las pruebas registradas.
     */
    public List<TestResult> runAllTests() {
        return runTests(new ArrayList<>(registeredTests.keySet()));
    }
    
    /**
     * Ejecuta una suite específica.
     */
    public TestSuiteResult runSuite(String suiteName) {
        TestSuite suite = registeredSuites.get(suiteName);
        if (suite == null) {
            throw new IllegalArgumentException("Suite no encontrada: " + suiteName);
        }
        
        if (verboseOutput) {
            System.out.println("[TestRunner] Ejecutando suite: " + suiteName + " (" + suite.getTestCount() + " pruebas)");
        }
        
        // Configurar la suite con nuestras opciones
        suite.setParallelExecution(parallelExecution)
             .setMaxThreads(maxThreads)
             .setTimeout(timeoutMs)
             .setStopOnFirstFailure(stopOnFirstFailure);
        
        return suite.run();
    }
    
    /**
     * Ejecuta una suite de pruebas directamente (sin registro previo).
     */
    public TestSuiteResult runTestSuite(TestSuite suite) {
        if (suite == null) {
            throw new IllegalArgumentException("La suite no puede ser null");
        }
        
        if (verboseOutput) {
            System.out.println("[TestRunner] Ejecutando suite: " + suite.getSuiteName() + " (" + suite.getTestCount() + " pruebas)");
        }
        
        // Configurar la suite con nuestras opciones
        suite.setParallelExecution(parallelExecution)
             .setMaxThreads(maxThreads)
             .setTimeout(timeoutMs)
             .setStopOnFirstFailure(stopOnFirstFailure);
        
        return suite.run();
    }
    
    /**
     * Ejecuta un caso de prueba individual directamente (sin registro previo).
     */
    public TestResult runSingleTest(TestCase testCase) {
        if (testCase == null) {
            throw new IllegalArgumentException("El caso de prueba no puede ser null");
        }
        
        if (verboseOutput) {
            System.out.println("[TestRunner] Ejecutando prueba: " + testCase.getTestName());
        }
        
        return executeTestCase(testCase.getTestName(), testCase);
    }
    
    /**
     * Ejecuta todas las suites registradas.
     */
    public List<TestSuiteResult> runAllSuites() {
        List<TestSuiteResult> results = new ArrayList<>();
        
        for (String suiteName : registeredSuites.keySet()) {
            try {
                TestSuiteResult result = runSuite(suiteName);
                results.add(result);
                
                // Detener si hay fallos y está configurado
                if (stopOnFirstFailure && !result.allTestsPassed()) {
                    if (verboseOutput) {
                        System.out.println("[TestRunner] Deteniendo ejecución por fallo en suite: " + suiteName);
                    }
                    break;
                }
                
            } catch (Exception e) {
                System.err.println("[TestRunner] Error ejecutando suite " + suiteName + ": " + e.getMessage());
            }
        }
        
        return results;
    }
    
    /**
     * Ejecuta pruebas que coincidan con un patrón.
     */
    public List<TestResult> runTestsMatching(String pattern) {
        List<String> matchingTests = registeredTests.keySet().stream()
                .filter(name -> name.toLowerCase().contains(pattern.toLowerCase()))
                .collect(Collectors.toList());
        
        if (matchingTests.isEmpty()) {
            System.out.println("[TestRunner] No se encontraron pruebas que coincidan con: " + pattern);
            return new ArrayList<>();
        }
        
        if (verboseOutput) {
            System.out.println("[TestRunner] Ejecutando " + matchingTests.size() + " pruebas que coinciden con: " + pattern);
        }
        
        return runTests(matchingTests);
    }
    
    // === MÉTODOS PRIVADOS ===
    
    /**
     * Ejecuta un caso de prueba individual.
     */
    private TestResult executeTestCase(String testName, TestCase testCase) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Configurar timeout si es necesario
            Future<TestResult> future = executorService.submit(() -> {
                try {
                    testCase.setUp();
                    testCase.runTest();
                    testCase.tearDown();
                    
                    long executionTime = System.currentTimeMillis() - startTime;
                    return TestResult.createSuccessResult(testName, executionTime);
                    
                } catch (Exception e) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    return TestResult.createFailedResult(testName, e, executionTime);
                }
            });
            
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
            
        } catch (TimeoutException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return TestResult.createFailedResult(testName, 
                    new RuntimeException("Prueba excedió el timeout de " + timeoutMs + "ms"), executionTime);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return TestResult.createFailedResult(testName, e, executionTime);
        }
    }
    
    /**
     * Ejecuta pruebas secuencialmente.
     */
    private List<TestResult> runTestsSequential(List<String> testNames) {
        List<TestResult> results = new ArrayList<>();
        
        for (String testName : testNames) {
            TestResult result = runTest(testName);
            results.add(result);
            
            if (stopOnFirstFailure && result.isFailed()) {
                if (verboseOutput) {
                    System.out.println("[TestRunner] Deteniendo ejecución por fallo en: " + testName);
                }
                break;
            }
        }
        
        return results;
    }
    
    /**
     * Ejecuta pruebas en paralelo.
     */
    private List<TestResult> runTestsParallel(List<String> testNames) {
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        List<Future<TestResult>> futures = new ArrayList<>();
        
        // Enviar todas las pruebas
        for (String testName : testNames) {
            Future<TestResult> future = executor.submit(() -> runTest(testName));
            futures.add(future);
        }
        
        // Recopilar resultados
        List<TestResult> results = new ArrayList<>();
        for (Future<TestResult> future : futures) {
            try {
                TestResult result = future.get();
                results.add(result);
                
                if (stopOnFirstFailure && result.isFailed()) {
                    // Cancelar pruebas pendientes
                    for (Future<TestResult> pendingFuture : futures) {
                        pendingFuture.cancel(true);
                    }
                    break;
                }
                
            } catch (Exception e) {
                System.err.println("[TestRunner] Error obteniendo resultado: " + e.getMessage());
            }
        }
        
        executor.shutdown();
        return results;
    }
    
    /**
     * Encuentra clases de prueba en un paquete.
     */
    private List<Class<?>> findTestClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);
        
        if (resource != null) {
            File directory = new File(resource.getFile());
            if (directory.exists()) {
                for (File file : directory.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                        try {
                            Class<?> clazz = Class.forName(className);
                            classes.add(clazz);
                        } catch (ClassNotFoundException e) {
                            // Ignorar clases que no se pueden cargar
                        }
                    }
                }
            }
        }
        
        return classes;
    }
    
    // === INFORMACIÓN Y UTILIDADES ===
    
    /**
     * Obtiene información sobre las pruebas registradas.
     */
    public String getRegisteredTestsInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== PRUEBAS REGISTRADAS ===").append("\n");
        info.append("Total: ").append(registeredTests.size()).append("\n\n");
        
        for (String testName : registeredTests.keySet()) {
            info.append("• ").append(testName).append("\n");
        }
        
        info.append("\n=== SUITES REGISTRADAS ===").append("\n");
        info.append("Total: ").append(registeredSuites.size()).append("\n\n");
        
        for (Map.Entry<String, TestSuite> entry : registeredSuites.entrySet()) {
            info.append("• ").append(entry.getKey())
                .append(" (").append(entry.getValue().getTestCount()).append(" pruebas)\n");
        }
        
        return info.toString();
    }
    
    /**
     * Limpia todos los recursos.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
    
    /**
     * Obtiene el reporter para generar informes.
     */
    public TestReporter getReporter() {
        return reporter;
    }
}