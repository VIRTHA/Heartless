package com.darkbladedev.testing;

import com.darkbladedev.testing.console.TestConsole;

/**
 * Lanzador principal del sistema de pruebas.
 * Proporciona un punto de entrada unificado para ejecutar las pruebas.
 */
public class TestLauncher {
    
    /**
     * Punto de entrada principal del sistema de pruebas.
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        try {
            // Configurar el sistema antes de ejecutar
            setupTestEnvironment();
            
            // Lanzar la consola de pruebas
            TestConsole.main(args);
            
        } catch (Exception e) {
            System.err.println("Error fatal al inicializar el sistema de pruebas:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Configura el entorno de pruebas antes de la ejecución.
     */
    private static void setupTestEnvironment() {
        // Configurar propiedades del sistema para las pruebas
        System.setProperty("test.environment", "true");
        System.setProperty("java.awt.headless", "true");
        
        // Configurar logging para pruebas
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "WARN");
        System.setProperty("org.slf4j.simpleLogger.log.com.darkbladedev", "INFO");
        
        // Crear directorio de reportes si no existe
        java.io.File reportsDir = new java.io.File("test-reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        
        // Configurar timezone para consistencia en las pruebas
        System.setProperty("user.timezone", "UTC");
        
        System.out.println("Entorno de pruebas configurado correctamente.");
    }
}