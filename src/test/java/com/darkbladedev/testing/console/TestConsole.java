package com.darkbladedev.testing.console;

import com.darkbladedev.testing.*;
import com.darkbladedev.testing.tests.*;
import com.darkbladedev.testing.utils.TestUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Interfaz de consola principal para ejecutar el sistema de pruebas.
 * Permite ejecutar pruebas individuales, suites completas y generar reportes.
 */
public class TestConsole {
    
    private static final String VERSION = "1.0.0";
    private static final String BANNER = 
        "\n" +
        "██╗  ██╗███████╗ █████╗ ██████╗ ████████╗██╗     ███████╗███████╗███████╗\n" +
        "██║  ██║██╔════╝██╔══██╗██╔══██╗╚══██╔══╝██║     ██╔════╝██╔════╝██╔════╝\n" +
        "███████║█████╗  ███████║██████╔╝   ██║   ██║     █████╗  ███████╗███████╗\n" +
        "██╔══██║██╔══╝  ██╔══██║██╔══██╗   ██║   ██║     ██╔══╝  ╚════██║╚════██║\n" +
        "██║  ██║███████╗██║  ██║██║  ██║   ██║   ███████╗███████╗███████║███████║\n" +
        "╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝   ╚══════╝╚══════╝╚══════╝╚══════╝\n" +
        "                    Sistema de Pruebas v" + VERSION + "\n";
    
    private final TestRunner testRunner;
    private final TestReporter reporter;
    private final Scanner scanner;
    private boolean running;
    
    public TestConsole() {
        this.testRunner = new TestRunner();
        this.reporter = new TestReporter();
        this.scanner = new Scanner(System.in);
        this.running = true;
        
        // Configurar reporter
        reporter.setShowStackTrace(true);
        reporter.setUseColors(true);
        reporter.setOutputDirectory("test-reports");
    }
    
    /**
     * Punto de entrada principal.
     */
    public static void main(String[] args) {
        TestConsole console = new TestConsole();
        
        if (args.length > 0) {
            // Modo comando directo
            console.executeCommand(args);
        } else {
            // Modo interactivo
            console.runInteractiveMode();
        }
    }
    
    /**
     * Ejecuta un comando directo desde argumentos de línea de comandos.
     */
    private void executeCommand(String[] args) {
        String command = args[0].toLowerCase();
        
        switch (command) {
            case "run-all":
                runAllTests();
                break;
            case "run-suite":
                if (args.length > 1) {
                    runTestSuite(args[1]);
                } else {
                    System.err.println("Error: Se requiere especificar el nombre de la suite.");
                    showUsage();
                }
                break;
            case "run-test":
                if (args.length > 1) {
                    runSingleTest(args[1]);
                } else {
                    System.err.println("Error: Se requiere especificar el nombre de la prueba.");
                    showUsage();
                }
                break;
            case "list":
                listAvailableTests();
                break;
            case "help":
                showUsage();
                break;
            default:
                System.err.println("Comando desconocido: " + command);
                showUsage();
        }
    }
    
    /**
     * Ejecuta el modo interactivo de la consola.
     */
    private void runInteractiveMode() {
        System.out.println(BANNER);
        System.out.println("Bienvenido al Sistema de Pruebas Heartless");
        System.out.println("Escribe 'help' para ver los comandos disponibles.\n");
        
        while (running) {
            System.out.print("test> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            processInteractiveCommand(input);
        }
        
        scanner.close();
    }
    
    /**
     * Procesa un comando en modo interactivo.
     */
    private void processInteractiveCommand(String input) {
        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();
        
        try {
            switch (command) {
                case "help":
                case "h":
                    showInteractiveHelp();
                    break;
                case "list":
                case "l":
                    listAvailableTests();
                    break;
                case "run-all":
                case "ra":
                    runAllTests();
                    break;
                case "run-suite":
                case "rs":
                    if (parts.length > 1) {
                        runTestSuite(parts[1]);
                    } else {
                        System.out.println("Uso: run-suite <nombre-suite>");
                        listAvailableSuites();
                    }
                    break;
                case "run-test":
                case "rt":
                    if (parts.length > 1) {
                        runSingleTest(parts[1]);
                    } else {
                        System.out.println("Uso: run-test <nombre-prueba>");
                        listAvailableTests();
                    }
                    break;
                case "config":
                case "c":
                    showConfigurationMenu();
                    break;
                case "reports":
                case "r":
                    showReportsMenu();
                    break;
                case "clear":
                case "cls":
                    clearScreen();
                    break;
                case "exit":
                case "quit":
                case "q":
                    System.out.println("¡Hasta luego!");
                    running = false;
                    break;
                default:
                    System.out.println("Comando desconocido: " + command);
                    System.out.println("Escribe 'help' para ver los comandos disponibles.");
            }
        } catch (Exception e) {
            System.err.println("Error ejecutando comando: " + e.getMessage());
            if (reporter.isShowStackTrace()) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Ejecuta todas las pruebas disponibles.
     */
    private void runAllTests() {
        System.out.println("\n=== Ejecutando todas las pruebas ===");
        
        TestSuite mainSuite = createMainTestSuite();
        
        long startTime = System.currentTimeMillis();
        TestSuiteResult result = testRunner.runTestSuite(mainSuite);
        long endTime = System.currentTimeMillis();
        
        System.out.println("\n=== Resultados de todas las pruebas ===");
        reporter.printSuiteResult(result);
        
        System.out.printf("\nTiempo total de ejecución: %s\n", 
                         TestUtils.formatTime(endTime - startTime));
        
        // Generar reportes
        generateReports(result, "all-tests");
    }
    
    /**
     * Ejecuta una suite específica de pruebas.
     */
    private void runTestSuite(String suiteName) {
        System.out.println("\n=== Ejecutando suite: " + suiteName + " ===");
        
        TestSuite suite = createTestSuite(suiteName);
        if (suite == null) {
            System.err.println("Suite no encontrada: " + suiteName);
            listAvailableSuites();
            return;
        }
        
        long startTime = System.currentTimeMillis();
        TestSuiteResult result = testRunner.runTestSuite(suite);
        long endTime = System.currentTimeMillis();
        
        System.out.println("\n=== Resultados de la suite: " + suiteName + " ===");
        reporter.printSuiteResult(result);
        
        System.out.printf("\nTiempo de ejecución: %s\n", 
                         TestUtils.formatTime(endTime - startTime));
        
        // Generar reportes
        generateReports(result, suiteName);
    }
    
    /**
     * Ejecuta una prueba individual.
     */
    private void runSingleTest(String testName) {
        System.out.println("\n=== Ejecutando prueba: " + testName + " ===");
        
        TestCase testCase = createTestCase(testName);
        if (testCase == null) {
            System.err.println("Prueba no encontrada: " + testName);
            listAvailableTests();
            return;
        }
        
        long startTime = System.currentTimeMillis();
        TestResult result = testRunner.runSingleTest(testCase);
        long endTime = System.currentTimeMillis();
        
        System.out.println("\n=== Resultado de la prueba: " + testName + " ===");
        reporter.printTestResult(result);
        
        System.out.printf("\nTiempo de ejecución: %s\n", 
                         TestUtils.formatTime(endTime - startTime));
    }
    
    /**
     * Lista todas las pruebas disponibles.
     */
    private void listAvailableTests() {
        System.out.println("\n=== Pruebas Disponibles ===");
        
        Map<String, List<String>> testsByCategory = getAvailableTestsByCategory();
        
        for (Map.Entry<String, List<String>> entry : testsByCategory.entrySet()) {
            System.out.println("\n" + entry.getKey() + ":");
            for (String test : entry.getValue()) {
                System.out.println("  - " + test);
            }
        }
        
        System.out.println("\nTotal: " + testsByCategory.values().stream()
                                                    .mapToInt(List::size)
                                                    .sum() + " pruebas");
    }
    
    /**
     * Lista las suites disponibles.
     */
    private void listAvailableSuites() {
        System.out.println("\n=== Suites Disponibles ===");
        System.out.println("  - storage (Pruebas de StorageManager)");
        System.out.println("  - events (Pruebas de WeeklyEventManager)");
        System.out.println("  - undead (Pruebas de UndeadWeek)");
        System.out.println("  - integration (Pruebas de integración)");
    }
    
    /**
     * Muestra el menú de configuración.
     */
    private void showConfigurationMenu() {
        System.out.println("\n=== Configuración ===");
        System.out.println("1. Stack traces: " + (reporter.isShowStackTrace() ? "Activado" : "Desactivado"));
        System.out.println("2. Colores: " + (reporter.isUseColors() ? "Activado" : "Desactivado"));
        System.out.println("3. Ejecución paralela: " + (testRunner.isParallelExecution() ? "Activada" : "Desactivada"));
        System.out.println("4. Hilos máximos: " + testRunner.getMaxThreads());
        System.out.println("5. Timeout: " + testRunner.getTimeoutSeconds() + " segundos");
        System.out.println("6. Directorio de reportes: " + reporter.getOutputDirectory().getAbsolutePath());
        
        System.out.print("\nSelecciona una opción (1-6) o 'back' para volver: ");
        String input = scanner.nextLine().trim();
        
        switch (input) {
            case "1":
                reporter.setShowStackTrace(!reporter.isShowStackTrace());
                System.out.println("Stack traces " + (reporter.isShowStackTrace() ? "activado" : "desactivado"));
                break;
            case "2":
                reporter.setUseColors(!reporter.isUseColors());
                System.out.println("Colores " + (reporter.isUseColors() ? "activado" : "desactivado"));
                break;
            case "3":
                testRunner.setParallelExecution(!testRunner.isParallelExecution());
                System.out.println("Ejecución paralela " + (testRunner.isParallelExecution() ? "activada" : "desactivada"));
                break;
            case "4":
                System.out.print("Nuevo número de hilos máximos: ");
                try {
                    int threads = Integer.parseInt(scanner.nextLine().trim());
                    testRunner.setMaxThreads(threads);
                    System.out.println("Hilos máximos establecidos a: " + threads);
                } catch (NumberFormatException e) {
                    System.out.println("Número inválido.");
                }
                break;
            case "5":
                System.out.print("Nuevo timeout en segundos: ");
                try {
                    long timeout = Long.parseLong(scanner.nextLine().trim());
                    testRunner.setTimeout(timeout);
                    System.out.println("Timeout establecido a: " + timeout + " segundos");
                } catch (NumberFormatException e) {
                    System.out.println("Número inválido.");
                }
                break;
            case "6":
                System.out.print("Nuevo directorio de reportes: ");
                String dir = scanner.nextLine().trim();
                reporter.setOutputDirectory(dir);
                System.out.println("Directorio establecido a: " + dir);
                break;
            case "back":
                break;
            default:
                System.out.println("Opción inválida.");
        }
    }
    
    /**
     * Muestra el menú de reportes.
     */
    private void showReportsMenu() {
        System.out.println("\n=== Gestión de Reportes ===");
        System.out.println("1. Ver reportes existentes");
        System.out.println("2. Limpiar reportes antiguos");
        System.out.println("3. Cambiar directorio de reportes");
        
        System.out.print("\nSelecciona una opción (1-3) o 'back' para volver: ");
        String input = scanner.nextLine().trim();
        
        switch (input) {
            case "1":
                listExistingReports();
                break;
            case "2":
                cleanOldReports();
                break;
            case "3":
                System.out.print("Nuevo directorio: ");
                String dir = scanner.nextLine().trim();
                reporter.setOutputDirectory(dir);
                System.out.println("Directorio cambiado a: " + dir);
                break;
            case "back":
                break;
            default:
                System.out.println("Opción inválida.");
        }
    }
    
    /**
     * Lista los reportes existentes.
     */
    private void listExistingReports() {
        File reportsDir = reporter.getOutputDirectory();
        if (!reportsDir.exists()) {
            System.out.println("No existe el directorio de reportes: " + reportsDir.getAbsolutePath());
            return;
        }
        
        File[] reports = reportsDir.listFiles((dir, name) -> 
            name.endsWith(".html") || name.endsWith(".txt") || name.endsWith(".csv"));
        
        if (reports == null || reports.length == 0) {
            System.out.println("No se encontraron reportes.");
            return;
        }
        
        System.out.println("\nReportes encontrados:");
        Arrays.sort(reports, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        
        for (File report : reports) {
            long ageMs = System.currentTimeMillis() - report.lastModified();
            String age = TestUtils.formatTime(ageMs);
            System.out.printf("  - %s (hace %s)\n", report.getName(), age);
        }
    }
    
    /**
     * Limpia reportes antiguos.
     */
    private void cleanOldReports() {
        System.out.print("¿Eliminar reportes más antiguos de cuántos días? (default: 7): ");
        String input = scanner.nextLine().trim();
        
        int days = 7;
        if (!input.isEmpty()) {
            try {
                days = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Número inválido, usando 7 días.");
            }
        }
        
        File reportsDir = reporter.getOutputDirectory();
        if (!reportsDir.exists()) {
            System.out.println("No existe el directorio de reportes.");
            return;
        }
        
        long cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days);
        File[] oldReports = reportsDir.listFiles((dir, name) -> {
            File file = new File(dir, name);
            return file.lastModified() < cutoffTime && 
                   (name.endsWith(".html") || name.endsWith(".txt") || name.endsWith(".csv"));
        });
        
        if (oldReports == null || oldReports.length == 0) {
            System.out.println("No se encontraron reportes antiguos para eliminar.");
            return;
        }
        
        System.out.printf("Se eliminarán %d reportes. ¿Continuar? (y/N): ", oldReports.length);
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            int deleted = 0;
            for (File report : oldReports) {
                if (report.delete()) {
                    deleted++;
                }
            }
            System.out.printf("Se eliminaron %d reportes.\n", deleted);
        } else {
            System.out.println("Operación cancelada.");
        }
    }
    
    /**
     * Genera reportes para los resultados de las pruebas.
     */
    private void generateReports(TestSuiteResult result, String baseName) {
        try {
            String timestamp = TestUtils.getCurrentTimestamp();
            String fileName = baseName + "_" + timestamp;
            
            // Generar reporte HTML
            reporter.generateHtmlReport(result, fileName);
            
            // Generar reporte de texto
            reporter.generateTextReport(result.getTestResults(), fileName + ".txt");
            
            // Generar reporte CSV
            reporter.generateCsvReport(result.getTestResults(), fileName + ".csv");
            
            System.out.println("\nReportes generados en: " + reporter.getOutputDirectory().getAbsolutePath());
            System.out.println("  - " + fileName + ".html");
            System.out.println("  - " + fileName + ".txt");
            System.out.println("  - " + fileName + ".csv");
            
        } catch (Exception e) {
            System.err.println("Error generando reportes: " + e.getMessage());
            if (reporter.isShowStackTrace()) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Crea la suite principal con todas las pruebas.
     */
    private TestSuite createMainTestSuite() {
        TestSuite mainSuite = new TestSuite("Todas las Pruebas");
        
        // Añadir todas las pruebas
        mainSuite.addTest(new StorageManagerTest());
        mainSuite.addTest(new WeeklyEventManagerTest());
        mainSuite.addTest(new UndeadWeekTest("UndeadWeekTest"));
        
        return mainSuite;
    }
    
    /**
     * Crea una suite específica por nombre.
     */
    private TestSuite createTestSuite(String suiteName) {
        switch (suiteName.toLowerCase()) {
            case "storage":
                TestSuite storageSuite = new TestSuite("Pruebas de Storage");
                storageSuite.addTest(new StorageManagerTest());
                return storageSuite;
                
            case "events":
                TestSuite eventsSuite = new TestSuite("Pruebas de Eventos");
                eventsSuite.addTest(new WeeklyEventManagerTest());
                return eventsSuite;
                
            case "undead":
                TestSuite undeadSuite = new TestSuite("Pruebas de UndeadWeek");
                undeadSuite.addTest(new UndeadWeekTest("UndeadWeekTest"));
                return undeadSuite;
                
            case "integration":
                TestSuite integrationSuite = new TestSuite("Pruebas de Integración");
                integrationSuite.addTest(new StorageManagerTest());
                integrationSuite.addTest(new WeeklyEventManagerTest());
                integrationSuite.addTest(new UndeadWeekTest("UndeadWeekTest"));
                return integrationSuite;
                
            default:
                return null;
        }
    }
    
    /**
     * Crea una instancia de prueba específica por nombre.
     */
    private TestCase createTestCase(String testName) {
        switch (testName.toLowerCase()) {
            case "storagemanagertest":
            case "storage":
                return new StorageManagerTest();
            case "weeklyeventmanagertest":
            case "events":
                return new WeeklyEventManagerTest();
            case "undeadweektest":
            case "undead":
                return new UndeadWeekTest("UndeadWeekTest");
            default:
                return null;
        }
    }
    
    /**
     * Obtiene las pruebas disponibles organizadas por categoría.
     */
    private Map<String, List<String>> getAvailableTestsByCategory() {
        Map<String, List<String>> tests = new LinkedHashMap<>();
        
        tests.put("Persistencia", Arrays.asList(
            "StorageManagerTest"
        ));
        
        tests.put("Gestión de Eventos", Arrays.asList(
            "WeeklyEventManagerTest"
        ));
        
        tests.put("Eventos Específicos", Arrays.asList(
            "UndeadWeekTest"
        ));
        
        return tests;
    }
    
    /**
     * Limpia la pantalla de la consola.
     */
    private void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[2J\033[H");
            }
        } catch (Exception e) {
            // Si no se puede limpiar, imprimir líneas vacías
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * Muestra la ayuda para el modo interactivo.
     */
    private void showInteractiveHelp() {
        System.out.println("\n=== Comandos Disponibles ===");
        System.out.println("  help, h           - Muestra esta ayuda");
        System.out.println("  list, l           - Lista todas las pruebas disponibles");
        System.out.println("  run-all, ra       - Ejecuta todas las pruebas");
        System.out.println("  run-suite, rs     - Ejecuta una suite específica");
        System.out.println("  run-test, rt      - Ejecuta una prueba específica");
        System.out.println("  config, c         - Configuración del sistema");
        System.out.println("  reports, r        - Gestión de reportes");
        System.out.println("  clear, cls        - Limpia la pantalla");
        System.out.println("  exit, quit, q     - Sale del programa");
        System.out.println();
        System.out.println("Ejemplos:");
        System.out.println("  run-suite storage");
        System.out.println("  run-test StorageManagerTest");
    }
    
    /**
     * Muestra el uso para el modo de línea de comandos.
     */
    private void showUsage() {
        System.out.println("\nUso: java TestConsole [comando] [argumentos]");
        System.out.println();
        System.out.println("Comandos disponibles:");
        System.out.println("  run-all                    - Ejecuta todas las pruebas");
        System.out.println("  run-suite <nombre>         - Ejecuta una suite específica");
        System.out.println("  run-test <nombre>          - Ejecuta una prueba específica");
        System.out.println("  list                       - Lista todas las pruebas disponibles");
        System.out.println("  help                       - Muestra esta ayuda");
        System.out.println();
        System.out.println("Si no se especifica comando, se inicia el modo interactivo.");
        System.out.println();
        System.out.println("Ejemplos:");
        System.out.println("  java TestConsole run-all");
        System.out.println("  java TestConsole run-suite storage");
        System.out.println("  java TestConsole run-test StorageManagerTest");
    }
}