package com.darkbladedev.testing;

import java.util.List;
import java.util.ArrayList;

/**
 * Clase que encapsula el resultado de la ejecución de un caso de prueba.
 * Contiene información sobre el éxito/fallo, errores, advertencias y tiempo de ejecución.
 */
public class TestResult {
    
    private final String testName;
    private final boolean passed;
    private final List<String> errors;
    private final List<String> warnings;
    private final long executionTimeMs;
    private final long timestamp;
    private final Exception error; // Excepción original si existe
    
    public TestResult(String testName, boolean passed, List<String> errors, 
                     List<String> warnings, long executionTimeMs) {
        this(testName, passed, errors, warnings, executionTimeMs, null);
    }
    
    public TestResult(String testName, boolean passed, List<String> errors, 
                     List<String> warnings, long executionTimeMs, Exception error) {
        this.testName = testName;
        this.passed = passed;
        this.errors = new ArrayList<>(errors);
        this.warnings = new ArrayList<>(warnings);
        this.executionTimeMs = executionTimeMs;
        this.timestamp = System.currentTimeMillis();
        this.error = error;
    }
    
    /**
     * Obtiene el nombre del test.
     */
    public String getTestName() {
        return testName;
    }
    
    /**
     * Indica si el test pasó exitosamente.
     */
    public boolean isPassed() {
        return passed;
    }
    
    /**
     * Indica si el test falló.
     */
    public boolean isFailed() {
        return !passed;
    }
    
    /**
     * Obtiene la lista de errores encontrados durante la ejecución.
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * Obtiene la excepción original si existe.
     */
    public Exception getError() {
        return error;
    }
    
    /**
     * Obtiene la lista de advertencias.
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    /**
     * Obtiene el tiempo de ejecución en milisegundos.
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    /**
     * Obtiene el tiempo de ejecución formateado como string.
     */
    public String getFormattedExecutionTime() {
        if (executionTimeMs < 1000) {
            return executionTimeMs + "ms";
        } else if (executionTimeMs < 60000) {
            return String.format("%.2fs", executionTimeMs / 1000.0);
        } else {
            long minutes = executionTimeMs / 60000;
            long seconds = (executionTimeMs % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    /**
     * Obtiene el timestamp de cuando se ejecutó el test.
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Indica si el test tiene errores.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Indica si el test tiene advertencias.
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * Obtiene el número total de errores.
     */
    public int getErrorCount() {
        return errors.size();
    }
    
    /**
     * Obtiene el número total de advertencias.
     */
    public int getWarningCount() {
        return warnings.size();
    }
    
    /**
     * Obtiene un resumen del resultado como string.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(testName).append(": ");
        
        if (passed) {
            summary.append("PASSED");
        } else {
            summary.append("FAILED");
        }
        
        summary.append(" (").append(getFormattedExecutionTime()).append(")");
        
        if (hasErrors()) {
            summary.append(" - ").append(getErrorCount()).append(" error(s)");
        }
        
        if (hasWarnings()) {
            summary.append(" - ").append(getWarningCount()).append(" warning(s)");
        }
        
        return summary.toString();
    }
    
    /**
     * Obtiene un reporte detallado del resultado.
     */
    public String getDetailedReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("=== Test Result: ").append(testName).append(" ===").append("\n");
        report.append("Status: ").append(passed ? "PASSED" : "FAILED").append("\n");
        report.append("Execution Time: ").append(getFormattedExecutionTime()).append("\n");
        report.append("Timestamp: ").append(new java.util.Date(timestamp)).append("\n");
        
        if (hasErrors()) {
            report.append("\nErrors (").append(getErrorCount()).append("):\n");
            for (int i = 0; i < errors.size(); i++) {
                report.append("  ").append(i + 1).append(". ").append(errors.get(i)).append("\n");
            }
        }
        
        if (hasWarnings()) {
            report.append("\nWarnings (").append(getWarningCount()).append("):\n");
            for (int i = 0; i < warnings.size(); i++) {
                report.append("  ").append(i + 1).append(". ").append(warnings.get(i)).append("\n");
            }
        }
        
        report.append("\n");
        return report.toString();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
    
    /**
     * Crea un resultado de test fallido con excepción
     */
    public static TestResult createFailedResult(String testName, Exception error, long executionTime) {
        List<String> errors = new ArrayList<>();
        if (error != null) {
            errors.add(error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName());
        }
        return new TestResult(testName, false, errors, new ArrayList<>(), executionTime, error);
    }
    
    /**
     * Crea un resultado de test fallido con RuntimeException
     */
    public static TestResult createFailedResult(String testName, RuntimeException error, long executionTime) {
        List<String> errors = new ArrayList<>();
        if (error != null) {
            errors.add(error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName());
        }
        return new TestResult(testName, false, errors, new ArrayList<>(), executionTime, error);
    }
    
    /**
     * Crea un resultado de test exitoso
     */
    public static TestResult createSuccessResult(String testName, long executionTime) {
        return new TestResult(testName, true, new ArrayList<>(), new ArrayList<>(), executionTime, null);
    }
}