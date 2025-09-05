package com.darkbladedev.testing;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Clase que encapsula los resultados de la ejecución de una suite de pruebas completa.
 * Proporciona estadísticas agregadas y métodos para generar reportes.
 */
public class TestSuiteResult {
    
    private final String suiteName;
    private final List<TestResult> testResults;
    private final long totalExecutionTimeMs;
    private final long timestamp;
    
    public TestSuiteResult(String suiteName, List<TestResult> testResults, long totalExecutionTimeMs) {
        this.suiteName = suiteName;
        this.testResults = new ArrayList<>(testResults);
        this.totalExecutionTimeMs = totalExecutionTimeMs;
        this.timestamp = System.currentTimeMillis();
    }
    
    // === ESTADÍSTICAS BÁSICAS ===
    
    /**
     * Obtiene el nombre de la suite.
     */
    public String getSuiteName() {
        return suiteName;
    }
    
    /**
     * Obtiene todos los resultados de las pruebas.
     */
    public List<TestResult> getTestResults() {
        return new ArrayList<>(testResults);
    }
    
    /**
     * Obtiene el número total de pruebas ejecutadas.
     */
    public int getTotalTests() {
        return testResults.size();
    }
    
    /**
     * Obtiene el número de pruebas que pasaron.
     */
    public int getPassedTests() {
        return (int) testResults.stream().filter(TestResult::isPassed).count();
    }
    
    /**
     * Obtiene el número de pruebas que fallaron.
     */
    public int getFailedTests() {
        return (int) testResults.stream().filter(TestResult::isFailed).count();
    }
    
    /**
     * Obtiene el porcentaje de éxito.
     */
    public double getSuccessRate() {
        if (getTotalTests() == 0) return 0.0;
        return (double) getPassedTests() / getTotalTests() * 100.0;
    }
    
    /**
     * Indica si todas las pruebas pasaron.
     */
    public boolean allTestsPassed() {
        return getFailedTests() == 0 && getTotalTests() > 0;
    }
    
    /**
     * Obtiene el tiempo total de ejecución en milisegundos.
     */
    public long getTotalExecutionTimeMs() {
        return totalExecutionTimeMs;
    }
    
    /**
     * Obtiene el tiempo total de ejecución formateado.
     */
    public String getFormattedTotalTime() {
        return formatTime(totalExecutionTimeMs);
    }
    
    /**
     * Obtiene el tiempo promedio por prueba.
     */
    public long getAverageExecutionTimeMs() {
        if (getTotalTests() == 0) return 0;
        return testResults.stream().mapToLong(TestResult::getExecutionTimeMs).sum() / getTotalTests();
    }
    
    /**
     * Obtiene el tiempo promedio por prueba formateado.
     */
    public String getFormattedAverageTime() {
        return formatTime(getAverageExecutionTimeMs());
    }
    
    // === ESTADÍSTICAS AVANZADAS ===
    
    /**
     * Obtiene el número total de errores en todas las pruebas.
     */
    public int getTotalErrors() {
        return testResults.stream().mapToInt(TestResult::getErrorCount).sum();
    }
    
    /**
     * Obtiene el número total de advertencias en todas las pruebas.
     */
    public int getTotalWarnings() {
        return testResults.stream().mapToInt(TestResult::getWarningCount).sum();
    }
    
    /**
     * Obtiene la prueba más lenta.
     */
    public TestResult getSlowestTest() {
        return testResults.stream()
                .max((a, b) -> Long.compare(a.getExecutionTimeMs(), b.getExecutionTimeMs()))
                .orElse(null);
    }
    
    /**
     * Obtiene la prueba más rápida.
     */
    public TestResult getFastestTest() {
        return testResults.stream()
                .min((a, b) -> Long.compare(a.getExecutionTimeMs(), b.getExecutionTimeMs()))
                .orElse(null);
    }
    
    /**
     * Obtiene todas las pruebas que fallaron.
     */
    public List<TestResult> getFailedTestResults() {
        return testResults.stream()
                .filter(TestResult::isFailed)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las pruebas que pasaron.
     */
    public List<TestResult> getPassedTestResults() {
        return testResults.stream()
                .filter(TestResult::isPassed)
                .collect(Collectors.toList());
    }
    
    // === REPORTES ===
    
    /**
     * Genera un resumen ejecutivo de los resultados.
     */
    public String getExecutiveSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("\n=== RESUMEN EJECUTIVO: ").append(suiteName).append(" ===").append("\n");
        summary.append("Fecha: ").append(new java.util.Date(timestamp)).append("\n");
        summary.append("\n");
        
        // Estadísticas principales
        summary.append("📊 ESTADÍSTICAS:\n");
        summary.append(String.format("  • Total de pruebas: %d\n", getTotalTests()));
        summary.append(String.format("  • Exitosas: %d (%.1f%%)\n", getPassedTests(), getSuccessRate()));
        summary.append(String.format("  • Fallidas: %d\n", getFailedTests()));
        summary.append(String.format("  • Tiempo total: %s\n", getFormattedTotalTime()));
        summary.append(String.format("  • Tiempo promedio: %s\n", getFormattedAverageTime()));
        
        if (getTotalErrors() > 0) {
            summary.append(String.format("  • Total errores: %d\n", getTotalErrors()));
        }
        
        if (getTotalWarnings() > 0) {
            summary.append(String.format("  • Total advertencias: %d\n", getTotalWarnings()));
        }
        
        // Estado general
        summary.append("\n");
        if (allTestsPassed()) {
            summary.append("✅ RESULTADO: TODAS LAS PRUEBAS PASARON\n");
        } else {
            summary.append("❌ RESULTADO: HAY PRUEBAS FALLIDAS\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Genera un reporte detallado de todos los resultados.
     */
    public String getDetailedReport() {
        StringBuilder report = new StringBuilder();
        
        report.append(getExecutiveSummary());
        
        // Detalles de pruebas fallidas
        if (getFailedTests() > 0) {
            report.append("\n❌ PRUEBAS FALLIDAS:\n");
            for (TestResult result : getFailedTestResults()) {
                report.append("\n").append(result.getDetailedReport());
            }
        }
        
        // Lista de pruebas exitosas (solo nombres)
        if (getPassedTests() > 0) {
            report.append("\n✅ PRUEBAS EXITOSAS:\n");
            for (TestResult result : getPassedTestResults()) {
                report.append(String.format("  • %s (%s)\n", 
                        result.getTestName(), result.getFormattedExecutionTime()));
            }
        }
        
        // Estadísticas de rendimiento
        if (getTotalTests() > 1) {
            report.append("\n⚡ RENDIMIENTO:\n");
            TestResult slowest = getSlowestTest();
            TestResult fastest = getFastestTest();
            
            if (slowest != null) {
                report.append(String.format("  • Más lenta: %s (%s)\n", 
                        slowest.getTestName(), slowest.getFormattedExecutionTime()));
            }
            
            if (fastest != null) {
                report.append(String.format("  • Más rápida: %s (%s)\n", 
                        fastest.getTestName(), fastest.getFormattedExecutionTime()));
            }
        }
        
        report.append("\n");
        return report.toString();
    }
    
    /**
     * Genera un reporte compacto para la consola.
     */
    public String getCompactReport() {
        StringBuilder report = new StringBuilder();
        
        report.append(String.format("\n[%s] %d/%d pruebas exitosas (%.1f%%) en %s\n", 
                suiteName, getPassedTests(), getTotalTests(), getSuccessRate(), getFormattedTotalTime()));
        
        if (getFailedTests() > 0) {
            report.append("Fallidas: ");
            List<TestResult> failed = getFailedTestResults();
            for (int i = 0; i < failed.size(); i++) {
                if (i > 0) report.append(", ");
                report.append(failed.get(i).getTestName());
            }
            report.append("\n");
        }
        
        return report.toString();
    }
    
    // === UTILIDADES ===
    
    /**
     * Formatea un tiempo en milisegundos a una representación legible.
     */
    private String formatTime(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2fs", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}