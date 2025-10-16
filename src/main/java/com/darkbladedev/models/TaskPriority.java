package com.darkbladedev.models;

/**
 * Enumeración de prioridades de tareas para el optimizador de eventos semanales.
 * 
 * Define los diferentes niveles de prioridad que pueden tener las tareas
 * en el sistema de optimización, cada una con un valor numérico asociado
 * que determina su orden de ejecución.
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 1.0
 */
public enum TaskPriority {
    /**
     * Prioridad baja - Para tareas no críticas que pueden ejecutarse cuando hay recursos disponibles.
     */
    LOW(1),
    
    /**
     * Prioridad normal - Para tareas regulares del sistema.
     */
    NORMAL(5),
    
    /**
     * Prioridad alta - Para tareas importantes que requieren ejecución prioritaria.
     */
    HIGH(10),
    
    /**
     * Prioridad crítica - Para tareas esenciales que deben ejecutarse inmediatamente.
     */
    CRITICAL(15);
    
    private final int value;
    
    /**
     * Constructor de la enumeración.
     * 
     * @param value El valor numérico de la prioridad
     */
    TaskPriority(int value) {
        this.value = value;
    }
    
    /**
     * Obtiene el valor numérico de la prioridad.
     * 
     * @return El valor numérico de la prioridad
     */
    public int getValue() {
        return value;
    }
}