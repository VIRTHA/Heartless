package com.darkbladedev.testing;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase base abstracta para todos los casos de prueba del sistema.
 * Proporciona funcionalidades básicas de testing como assertions, setup y teardown.
 */
public abstract class TestCase {
    
    protected String testName;
    protected List<String> errors;
    protected List<String> warnings;
    protected long startTime;
    protected long endTime;
    protected boolean passed;
    
    public TestCase(String testName) {
        this.testName = testName;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.passed = false;
    }
    
    /**
     * Método que se ejecuta antes de cada prueba.
     * Debe ser implementado por las clases hijas para configuración específica.
     */
    protected abstract void setUp() throws Exception;
    
    /**
     * Método que se ejecuta después de cada prueba.
     * Debe ser implementado por las clases hijas para limpieza específica.
     */
    protected abstract void tearDown() throws Exception;
    
    /**
     * Ejecuta el caso de prueba completo incluyendo setup, test y teardown.
     */
    public final TestResult execute() {
        startTime = System.currentTimeMillis();
        
        try {
            setUp();
            runTest();
            
            if (errors.isEmpty()) {
                passed = true;
            }
            
        } catch (Exception e) {
            addError("Error durante la ejecución: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                tearDown();
            } catch (Exception e) {
                addError("Error durante tearDown: " + e.getMessage());
            }
            endTime = System.currentTimeMillis();
        }
        
        return new TestResult(testName, passed, errors, warnings, getExecutionTime());
    }
    
    /**
     * Método abstracto que contiene la lógica específica de la prueba.
     */
    protected abstract void runTest() throws Exception;
    
    /**
     * Obtiene el tiempo de ejecución en milisegundos.
     */
    public long getExecutionTime() {
        return endTime - startTime;
    }
    
    // === MÉTODOS DE ASSERTION ===
    
    /**
     * Verifica que una condición sea verdadera.
     */
    protected void assertTrue(boolean condition, String message) {
        if (!condition) {
            addError("Assertion failed: " + message);
        }
    }
    
    /**
     * Verifica que una condición sea falsa.
     */
    protected void assertFalse(boolean condition, String message) {
        if (condition) {
            addError("Assertion failed: " + message);
        }
    }
    
    /**
     * Verifica que dos objetos sean iguales.
     */
    protected void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            addError(String.format("Assertion failed: %s. Expected: %s, Actual: %s", 
                    message, expected, actual));
        }
    }
    
    /**
     * Verifica que un objeto no sea nulo.
     */
    protected void assertNotNull(Object object, String message) {
        if (object == null) {
            addError("Assertion failed: " + message + " - Object is null");
        }
    }
    
    /**
     * Verifica que un objeto sea nulo.
     */
    protected void assertNull(Object object, String message) {
        if (object != null) {
            addError("Assertion failed: " + message + " - Object is not null: " + object);
        }
    }
    
    /**
     * Verifica que se lance una excepción específica.
     */
    protected void assertThrows(Class<? extends Exception> expectedType, Runnable code, String message) {
        try {
            code.run();
            addError("Assertion failed: " + message + " - Expected exception " + expectedType.getSimpleName() + " was not thrown");
        } catch (Exception e) {
            if (!expectedType.isInstance(e)) {
                addError(String.format("Assertion failed: %s - Expected %s but got %s", 
                        message, expectedType.getSimpleName(), e.getClass().getSimpleName()));
            }
        }
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    /**
     * Añade un error a la lista de errores.
     */
    protected void addError(String error) {
        errors.add(error);
    }
    
    /**
     * Añade una advertencia a la lista de advertencias.
     */
    protected void addWarning(String warning) {
        warnings.add(warning);
    }
    
    /**
     * Falla la prueba con un mensaje específico.
     */
    protected void fail(String message) {
        addError("Test failed: " + message);
    }
    
    /**
     * Pausa la ejecución por un tiempo determinado.
     */
    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            addWarning("Sleep interrupted: " + e.getMessage());
        }
    }
    
    // === GETTERS ===
    
    public String getTestName() {
        return testName;
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    public boolean isPassed() {
        return passed;
    }
}