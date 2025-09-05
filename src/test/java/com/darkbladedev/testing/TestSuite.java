package com.darkbladedev.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Clase que representa una suite de pruebas que puede contener múltiples TestCase.
 * Permite ejecutar pruebas en paralelo o secuencialmente.
 */
public class TestSuite {
    
    private final String suiteName;
    private final List<TestCase> testCases;
    private final List<TestResult> results;
    private boolean parallelExecution;
    private int maxThreads;
    private long timeoutMs = 30000; // 30 segundos por defecto
    private boolean stopOnFirstFailure = false;
    
    public TestSuite(String suiteName) {
        this.suiteName = suiteName;
        this.testCases = new ArrayList<>();
        this.results = new ArrayList<>();
        this.parallelExecution = false;
        this.maxThreads = Runtime.getRuntime().availableProcessors();
    }
    
    /**
     * Añade un caso de prueba a la suite.
     */
    public TestSuite addTest(TestCase testCase) {
        testCases.add(testCase);
        return this;
    }
    
    /**
     * Configura la ejecución en paralelo.
     */
    public TestSuite setParallelExecution(boolean parallel) {
        this.parallelExecution = parallel;
        return this;
    }
    
    /**
     * Configura el número máximo de hilos para ejecución paralela.
     */
    public TestSuite setMaxThreads(int maxThreads) {
        this.maxThreads = Math.max(1, maxThreads);
        return this;
    }
    
    /**
     * Configura el timeout por prueba en milisegundos.
     */
    public TestSuite setTimeout(long timeoutMs) {
        this.timeoutMs = Math.max(1000, timeoutMs);
        return this;
    }
    
    /**
     * Configura si debe detenerse en el primer fallo.
     */
    public TestSuite setStopOnFirstFailure(boolean stopOnFirstFailure) {
        this.stopOnFirstFailure = stopOnFirstFailure;
        return this;
    }
    
    /**
     * Ejecuta todos los casos de prueba en la suite.
     */
    public TestSuiteResult execute() {
        return run();
    }
    
    /**
     * Ejecuta todos los casos de prueba en la suite (método alias para execute).
     */
    public TestSuiteResult run() {
        results.clear();
        long startTime = System.currentTimeMillis();
        
        System.out.println("\n=== Ejecutando Suite: " + suiteName + " ===");
        System.out.println("Casos de prueba: " + testCases.size());
        System.out.println("Modo: " + (parallelExecution ? "Paralelo (" + maxThreads + " hilos)" : "Secuencial"));
        System.out.println("\n");
        
        if (parallelExecution) {
            executeParallel();
        } else {
            executeSequential();
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        return new TestSuiteResult(suiteName, results, totalTime);
    }
    
    /**
     * Ejecuta los casos de prueba secuencialmente.
     */
    private void executeSequential() {
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            System.out.printf("[%d/%d] Ejecutando: %s...\n", i + 1, testCases.size(), testCase.getTestName());
            
            TestResult result = testCase.execute();
            results.add(result);
            
            // Mostrar resultado inmediato
            if (result.isPassed()) {
                System.out.println("  ✓ PASSED (" + result.getFormattedExecutionTime() + ")");
            } else {
                System.out.println("  ✗ FAILED (" + result.getFormattedExecutionTime() + ") - " + 
                                 result.getErrorCount() + " error(s)");
            }
            
            // Detener si está configurado para parar en el primer fallo
            if (stopOnFirstFailure && !result.isPassed()) {
                System.out.println("Deteniendo ejecución por fallo en: " + testCase.getTestName());
                break;
            }
        }
    }
    
    /**
     * Ejecuta los casos de prueba en paralelo.
     */
    private void executeParallel() {
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        List<CompletableFuture<TestResult>> futures = new ArrayList<>();
        
        // Lanzar todas las pruebas
        for (TestCase testCase : testCases) {
            CompletableFuture<TestResult> future = CompletableFuture.supplyAsync(() -> {
                System.out.println("Ejecutando: " + testCase.getTestName() + " [" + Thread.currentThread().getName() + "]");
                return testCase.execute();
            }, executor);
            futures.add(future);
        }
        
        // Recopilar resultados
        for (CompletableFuture<TestResult> future : futures) {
            try {
                TestResult result = future.get(timeoutMs, TimeUnit.MILLISECONDS); // Usar timeout configurado
                results.add(result);
                
                // Mostrar resultado inmediato
                if (result.isPassed()) {
                    System.out.println("  ✓ " + result.getTestName() + " PASSED (" + result.getFormattedExecutionTime() + ")");
                } else {
                    System.out.println("  ✗ " + result.getTestName() + " FAILED (" + result.getFormattedExecutionTime() + 
                                     ") - " + result.getErrorCount() + " error(s)");
                }
            } catch (Exception e) {
                System.err.println("Error ejecutando prueba: " + e.getMessage());
                // Crear un resultado de fallo para la prueba que no se pudo ejecutar
                List<String> errors = new ArrayList<>();
                errors.add("Timeout o error de ejecución: " + e.getMessage());
                TestResult failedResult = new TestResult("Unknown Test", false, errors, new ArrayList<>(), 0);
                results.add(failedResult);
            }
        }
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Ejecuta solo los casos de prueba que coincidan con el patrón dado.
     */
    public TestSuiteResult executeFiltered(String namePattern) {
        List<TestCase> filteredTests = new ArrayList<>();
        
        for (TestCase testCase : testCases) {
            if (testCase.getTestName().toLowerCase().contains(namePattern.toLowerCase())) {
                filteredTests.add(testCase);
            }
        }
        
        if (filteredTests.isEmpty()) {
            System.out.println("No se encontraron pruebas que coincidan con el patrón: " + namePattern);
            return new TestSuiteResult(suiteName + " (Filtered)", new ArrayList<>(), 0);
        }
        
        // Crear una suite temporal con los tests filtrados
        TestSuite filteredSuite = new TestSuite(suiteName + " (Filtered: " + namePattern + ")");
        filteredSuite.parallelExecution = this.parallelExecution;
        filteredSuite.maxThreads = this.maxThreads;
        
        for (TestCase testCase : filteredTests) {
            filteredSuite.addTest(testCase);
        }
        
        return filteredSuite.execute();
    }
    
    // === GETTERS ===
    
    public String getSuiteName() {
        return suiteName;
    }
    
    public List<TestCase> getTestCases() {
        return new ArrayList<>(testCases);
    }
    
    public int getTestCount() {
        return testCases.size();
    }
    
    public boolean isParallelExecution() {
        return parallelExecution;
    }
    
    public int getMaxThreads() {
        return maxThreads;
    }
}