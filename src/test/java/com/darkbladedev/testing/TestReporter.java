package com.darkbladedev.testing;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Clase encargada de generar reportes detallados de los resultados de las pruebas.
 * Soporta múltiples formatos de salida y configuraciones de reporte.
 */
public class TestReporter {
    
    private final SimpleDateFormat dateFormat;
    private boolean includeStackTraces = true;
    private boolean colorOutput = true;
    private String outputDirectory = "test-reports";
    
    // Códigos de color ANSI
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    @SuppressWarnings("unused")
    private static final String BLUE = "\u001B[34m";
    @SuppressWarnings("unused")
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    @SuppressWarnings("unused")
    private static final String WHITE = "\u001B[37m";
    private static final String BOLD = "\u001B[1m";
    
    public TestReporter() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
    
    // === CONFIGURACIÓN ===
    
    /**
     * Configura si incluir stack traces en los reportes.
     */
    public TestReporter setIncludeStackTraces(boolean include) {
        this.includeStackTraces = include;
        return this;
    }
    
    /**
     * Configura si usar colores en la salida de consola.
     */
    public TestReporter setColorOutput(boolean useColors) {
        this.colorOutput = useColors;
        return this;
    }
    
    /**
     * Configura el directorio de salida para los reportes.
     */
    public TestReporter setOutputDirectory(String directory) {
        this.outputDirectory = directory;
        return this;
    }
    
    /**
     * Obtiene el directorio de salida para los reportes.
     */
    public File getOutputDirectory() {
        return new File(this.outputDirectory);
    }
    
    /**
     * Verifica si se muestran stack traces.
     */
    public boolean isShowStackTrace() {
        return this.includeStackTraces;
    }
    
    /**
     * Configura si mostrar stack traces (alias para setIncludeStackTraces).
     */
    public TestReporter setShowStackTrace(boolean show) {
        return setIncludeStackTraces(show);
    }
    
    /**
     * Configura si usar colores (alias para setColorOutput).
     */
    public TestReporter setUseColors(boolean useColors) {
        return setColorOutput(useColors);
    }
    
    /**
     * Verifica si se están usando colores.
     */
    public boolean isUseColors() {
        return this.colorOutput;
    }
    
    // === REPORTES DE CONSOLA ===
    
    /**
     * Imprime un resultado individual en la consola.
     */
    public void printTestResult(TestResult result) {
        String status = result.isPassed() ? 
            colorize("✅ PASS", GREEN) : 
            colorize("❌ FAIL", RED);
        
        System.out.printf("%s %s (%s)%n", 
            status, 
            colorize(result.getTestName(), BOLD),
            result.getFormattedExecutionTime());
        
        if (result.isFailed() && includeStackTraces) {
            printError(result.getError());
        }
        
        if (result.getWarningCount() > 0) {
            System.out.println(colorize("  ⚠️  " + result.getWarningCount() + " advertencias", YELLOW));
        }
    }
    
    /**
     * Imprime múltiples resultados en la consola.
     */
    public void printTestResults(List<TestResult> results) {
        System.out.println(colorize("\n=== RESULTADOS DE PRUEBAS ===", BOLD));
        System.out.println();
        
        for (TestResult result : results) {
            printTestResult(result);
        }
        
        printSummary(results);
    }
    
    /**
     * Imprime el resultado de una suite en la consola.
     */
    public void printSuiteResult(TestSuiteResult suiteResult) {
        System.out.println(colorize(suiteResult.getDetailedReport(), CYAN));
    }
    
    /**
     * Imprime múltiples resultados de suites.
     */
    public void printSuiteResults(List<TestSuiteResult> suiteResults) {
        System.out.println(colorize("\n=== RESULTADOS DE SUITES ===", BOLD));
        
        for (TestSuiteResult result : suiteResults) {
            printSuiteResult(result);
        }
        
        printOverallSummary(suiteResults);
    }
    
    /**
     * Imprime un resumen de resultados individuales.
     */
    public void printSummary(List<TestResult> results) {
        if (results.isEmpty()) {
            System.out.println(colorize("\n📝 No hay resultados para mostrar", YELLOW));
            return;
        }
        
        int total = results.size();
        int passed = (int) results.stream().filter(TestResult::isPassed).count();
        int failed = total - passed;
        double successRate = (double) passed / total * 100.0;
        
        long totalTime = results.stream().mapToLong(TestResult::getExecutionTimeMs).sum();
        
        System.out.println();
        System.out.println(colorize("📊 RESUMEN:", BOLD));
        System.out.printf("  Total: %d | Exitosas: %s | Fallidas: %s%n",
            total,
            colorize(String.valueOf(passed), GREEN),
            failed > 0 ? colorize(String.valueOf(failed), RED) : "0");
        System.out.printf("  Tasa de éxito: %s | Tiempo total: %s%n",
            colorize(String.format("%.1f%%", successRate), successRate >= 90 ? GREEN : successRate >= 70 ? YELLOW : RED),
            formatTime(totalTime));
        
        if (failed == 0) {
            System.out.println(colorize("\n🎉 ¡TODAS LAS PRUEBAS PASARON!", GREEN + BOLD));
        } else {
            System.out.println(colorize("\n⚠️  HAY PRUEBAS FALLIDAS", RED + BOLD));
        }
        
        System.out.println();
    }
    
    /**
     * Imprime un resumen general de múltiples suites.
     */
    public void printOverallSummary(List<TestSuiteResult> suiteResults) {
        if (suiteResults.isEmpty()) {
            System.out.println(colorize("\n📝 No hay suites para mostrar", YELLOW));
            return;
        }
        
        int totalSuites = suiteResults.size();
        int passedSuites = (int) suiteResults.stream().filter(TestSuiteResult::allTestsPassed).count();
        int totalTests = suiteResults.stream().mapToInt(TestSuiteResult::getTotalTests).sum();
        int totalPassed = suiteResults.stream().mapToInt(TestSuiteResult::getPassedTests).sum();
        int totalFailed = suiteResults.stream().mapToInt(TestSuiteResult::getFailedTests).sum();
        long totalTime = suiteResults.stream().mapToLong(TestSuiteResult::getTotalExecutionTimeMs).sum();
        
        double overallSuccessRate = totalTests > 0 ? (double) totalPassed / totalTests * 100.0 : 0.0;
        
        System.out.println();
        System.out.println(colorize("🏆 RESUMEN GENERAL:", BOLD));
        System.out.printf("  Suites: %d | Exitosas: %s | Con fallos: %s%n",
            totalSuites,
            colorize(String.valueOf(passedSuites), GREEN),
            (totalSuites - passedSuites) > 0 ? colorize(String.valueOf(totalSuites - passedSuites), RED) : "0");
        System.out.printf("  Pruebas totales: %d | Exitosas: %s | Fallidas: %s%n",
            totalTests,
            colorize(String.valueOf(totalPassed), GREEN),
            totalFailed > 0 ? colorize(String.valueOf(totalFailed), RED) : "0");
        System.out.printf("  Tasa de éxito general: %s | Tiempo total: %s%n",
            colorize(String.format("%.1f%%", overallSuccessRate), 
                overallSuccessRate >= 90 ? GREEN : overallSuccessRate >= 70 ? YELLOW : RED),
            formatTime(totalTime));
        
        if (totalFailed == 0 && totalTests > 0) {
            System.out.println(colorize("\n🎉 ¡TODAS LAS PRUEBAS DE TODAS LAS SUITES PASARON!", GREEN + BOLD));
        } else if (totalFailed > 0) {
            System.out.println(colorize("\n⚠️  HAY PRUEBAS FALLIDAS EN LAS SUITES", RED + BOLD));
        }
        
        System.out.println();
    }
    
    // === REPORTES DE ARCHIVO ===
    
    /**
     * Genera un reporte HTML de los resultados.
     */
    public void generateHtmlReport(List<TestResult> results, String filename) {
        try {
            ensureOutputDirectory();
            Path filePath = Paths.get(outputDirectory, filename + ".html");
            
            StringBuilder html = new StringBuilder();
            html.append(generateHtmlHeader("Reporte de Pruebas"));
            html.append(generateHtmlBody(results));
            html.append(generateHtmlFooter());
            
            Files.write(filePath, html.toString().getBytes());
            System.out.println("📄 Reporte HTML generado: " + filePath.toAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Error generando reporte HTML: " + e.getMessage());
        }
    }
    
    /**
     * Genera un reporte HTML de una suite.
     */
    public void generateHtmlReport(TestSuiteResult suiteResult, String filename) {
        generateHtmlReport(suiteResult.getTestResults(), filename);
    }
    
    /**
     * Genera un reporte de texto plano.
     */
    public void generateTextReport(List<TestResult> results, String filename) {
        try {
            ensureOutputDirectory();
            Path filePath = Paths.get(outputDirectory, filename + ".txt");
            
            StringBuilder report = new StringBuilder();
            report.append("=== REPORTE DE PRUEBAS ===").append("\n");
            report.append("Generado: ").append(dateFormat.format(new Date())).append("\n\n");
            
            for (TestResult result : results) {
                report.append(result.getDetailedReport()).append("\n");
            }
            
            // Agregar resumen
            report.append(generateTextSummary(results));
            
            Files.write(filePath, report.toString().getBytes());
            System.out.println("📄 Reporte de texto generado: " + filePath.toAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Error generando reporte de texto: " + e.getMessage());
        }
    }
    
    /**
     * Genera un reporte CSV.
     */
    public void generateCsvReport(List<TestResult> results, String filename) {
        try {
            ensureOutputDirectory();
            Path filePath = Paths.get(outputDirectory, filename + ".csv");
            
            StringBuilder csv = new StringBuilder();
            csv.append("Test Name,Status,Execution Time (ms),Error Count,Warning Count,Error Message\n");
            
            for (TestResult result : results) {
                csv.append(String.format("%s,%s,%d,%d,%d,\"%s\"\n",
                    escapeCSV(result.getTestName()),
                    result.isPassed() ? "PASS" : "FAIL",
                    result.getExecutionTimeMs(),
                    result.getErrorCount(),
                    result.getWarningCount(),
                    result.getError() != null ? escapeCSV(result.getError().getMessage()) : ""));
            }
            
            Files.write(filePath, csv.toString().getBytes());
            System.out.println("📊 Reporte CSV generado: " + filePath.toAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Error generando reporte CSV: " + e.getMessage());
        }
    }
    

    
    // === MÉTODOS PRIVADOS ===
    
    /**
     * Aplica colores a un texto si está habilitado.
     */
    private String colorize(String text, String color) {
        return colorOutput ? color + text + RESET : text;
    }
    
    /**
     * Imprime un error con formato.
     */
    private void printError(Throwable error) {
        if (error == null) return;
        
        System.out.println(colorize("  💥 " + error.getMessage(), RED));
        
        if (includeStackTraces && error.getStackTrace().length > 0) {
            System.out.println(colorize("     en " + error.getStackTrace()[0].toString(), RED));
        }
    }
    
    /**
     * Formatea un tiempo en milisegundos.
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
    
    /**
     * Asegura que el directorio de salida existe.
     */
    private void ensureOutputDirectory() throws IOException {
        Path dir = Paths.get(outputDirectory);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }
    
    /**
     * Genera el encabezado HTML.
     */
    private String generateHtmlHeader(String title) {
        return String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset='UTF-8'>\n" +
            "    <title>%s</title>\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; margin: 20px; }\n" +
            "        .header { background: #f4f4f4; padding: 20px; border-radius: 5px; }\n" +
            "        .test-pass { color: #28a745; }\n" +
            "        .test-fail { color: #dc3545; }\n" +
            "        .test-warning { color: #ffc107; }\n" +
            "        .summary { background: #e9ecef; padding: 15px; margin: 20px 0; border-radius: 5px; }\n" +
            "        .error { background: #f8d7da; padding: 10px; margin: 10px 0; border-radius: 3px; }\n" +
            "        table { width: 100%%; border-collapse: collapse; margin: 20px 0; }\n" +
            "        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n" +
            "        th { background-color: #f2f2f2; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class='header'>\n" +
            "        <h1>%s</h1>\n" +
            "        <p>Generado: %s</p>\n" +
            "    </div>\n",
            title, title, dateFormat.format(new Date()));
    }
    
    /**
     * Genera el cuerpo HTML.
     */
    private String generateHtmlBody(List<TestResult> results) {
        StringBuilder body = new StringBuilder();
        
        // Resumen
        body.append("    <div class='summary'>\n");
        body.append("        <h2>Resumen</h2>\n");
        body.append(generateHtmlSummary(results));
        body.append("    </div>\n");
        
        // Tabla de resultados
        body.append("    <h2>Resultados Detallados</h2>\n");
        body.append("    <table>\n");
        body.append("        <tr><th>Prueba</th><th>Estado</th><th>Tiempo</th><th>Errores</th><th>Advertencias</th></tr>\n");
        
        for (TestResult result : results) {
            String statusClass = result.isPassed() ? "test-pass" : "test-fail";
            body.append(String.format(
                "        <tr>\n" +
                "            <td>%s</td>\n" +
                "            <td class='%s'>%s</td>\n" +
                "            <td>%s</td>\n" +
                "            <td>%d</td>\n" +
                "            <td class='test-warning'>%d</td>\n" +
                "        </tr>\n",
                escapeHtml(result.getTestName()),
                statusClass,
                result.isPassed() ? "✅ PASS" : "❌ FAIL",
                result.getFormattedExecutionTime(),
                result.getErrorCount(),
                result.getWarningCount()));
            
            // Agregar detalles de error si existe
            if (result.isFailed() && result.getError() != null) {
                body.append(String.format(
                    "        <tr>\n" +
                    "            <td colspan='5'>\n" +
                    "                <div class='error'>\n" +
                    "                    <strong>Error:</strong> %s\n" +
                    "                </div>\n" +
                    "            </td>\n" +
                    "        </tr>\n",
                    escapeHtml(result.getError().getMessage())));
            }
        }
        
        body.append("    </table>\n");
        return body.toString();
    }
    
    /**
     * Genera el pie HTML.
     */
    private String generateHtmlFooter() {
        return "</body>\n</html>\n";
    }
    
    /**
     * Genera resumen HTML.
     */
    private String generateHtmlSummary(List<TestResult> results) {
        int total = results.size();
        int passed = (int) results.stream().filter(TestResult::isPassed).count();
        int failed = total - passed;
        double successRate = total > 0 ? (double) passed / total * 100.0 : 0.0;
        long totalTime = results.stream().mapToLong(TestResult::getExecutionTimeMs).sum();
        
        return String.format(
            "        <p><strong>Total:</strong> %d | <strong>Exitosas:</strong> <span class='test-pass'>%d</span> | <strong>Fallidas:</strong> <span class='test-fail'>%d</span></p>\n" +
            "        <p><strong>Tasa de éxito:</strong> %.1f%% | <strong>Tiempo total:</strong> %s</p>\n",
            total, passed, failed, successRate, formatTime(totalTime));
    }
    
    /**
     * Genera resumen de texto.
     */
    private String generateTextSummary(List<TestResult> results) {
        StringBuilder summary = new StringBuilder();
        
        int total = results.size();
        int passed = (int) results.stream().filter(TestResult::isPassed).count();
        int failed = total - passed;
        double successRate = total > 0 ? (double) passed / total * 100.0 : 0.0;
        long totalTime = results.stream().mapToLong(TestResult::getExecutionTimeMs).sum();
        
        summary.append("\n=== RESUMEN ===").append("\n");
        summary.append(String.format("Total: %d | Exitosas: %d | Fallidas: %d\n", total, passed, failed));
        summary.append(String.format("Tasa de éxito: %.1f%% | Tiempo total: %s\n", successRate, formatTime(totalTime)));
        
        if (failed == 0 && total > 0) {
            summary.append("\n¡TODAS LAS PRUEBAS PASARON!\n");
        } else if (failed > 0) {
            summary.append("\nHAY PRUEBAS FALLIDAS\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Escapa caracteres especiales para HTML.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    /**
     * Escapa caracteres especiales para CSV.
     */
    private String escapeCSV(String text) {
        if (text == null) return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}