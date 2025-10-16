package com.darkbladedev.models;

import com.darkbladedev.utils.TimeConverter;

/**
 * Modelo de datos que representa una expresión de tiempo que puede ser parseada
 * a diferentes unidades (ticks, milisegundos, segundos).
 * 
 * Permite usar expresiones como "1w", "2d 3h", "30m", etc.
 * y convertirlas automáticamente a las unidades necesarias.
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class TimeExpression {
    
    private final String expression;
    private final long ticks;
    
    /**
     * Constructor que parsea una expresión de tiempo.
     * 
     * @param expression La expresión de tiempo (ej: "1w", "2d 3h", "30m")
     * @throws IllegalArgumentException Si la expresión no es válida
     */
    public TimeExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("La expresión de tiempo no puede estar vacía");
        }
        
        this.expression = expression.trim();
        this.ticks = TimeConverter.parseTimeToTicks(this.expression);
        
        if (this.ticks == -1) {
            throw new IllegalArgumentException("Expresión de tiempo inválida: " + expression);
        }
    }
    
    /**
     * Constructor que crea una TimeExpression desde ticks.
     * 
     * @param ticks La cantidad de ticks
     */
    public TimeExpression(long ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("Los ticks no pueden ser negativos");
        }
        
        this.ticks = ticks;
        this.expression = TimeConverter.formatTicksToTime(ticks);
    }
    
    /**
     * Obtiene la duración en ticks de Minecraft.
     * 
     * @return La duración en ticks
     */
    public long toTicks() {
        return ticks;
    }
    
    /**
     * Obtiene la duración en milisegundos.
     * 
     * @return La duración en milisegundos
     */
    public long toMilliseconds() {
        return ticks * 50L; // 1 tick = 50ms en Minecraft
    }
    
    /**
     * Obtiene la duración en segundos.
     * 
     * @return La duración en segundos
     */
    public long toSeconds() {
        return ticks / 20L; // 20 ticks = 1 segundo en Minecraft
    }
    
    /**
     * Obtiene la expresión de tiempo original o formateada.
     * 
     * @return La expresión de tiempo
     */
    public String getExpression() {
        return expression;
    }
    
    /**
     * Obtiene una representación legible del tiempo.
     * 
     * @return Una cadena que representa el tiempo en formato legible
     */
    public String toReadableString() {
        return TimeConverter.formatTicksToTime(ticks);
    }
    
    /**
     * Crea una TimeExpression desde una expresión de texto.
     * 
     * @param expression La expresión de tiempo
     * @return Una nueva instancia de TimeExpression
     * @throws IllegalArgumentException Si la expresión no es válida
     */
    public static TimeExpression parse(String expression) {
        return new TimeExpression(expression);
    }
    
    /**
     * Crea una TimeExpression desde ticks.
     * 
     * @param ticks La cantidad de ticks
     * @return Una nueva instancia de TimeExpression
     */
    public static TimeExpression fromTicks(long ticks) {
        return new TimeExpression(ticks);
    }
    
    /**
     * Crea una TimeExpression desde milisegundos.
     * 
     * @param milliseconds La cantidad de milisegundos
     * @return Una nueva instancia de TimeExpression
     */
    public static TimeExpression fromMilliseconds(long milliseconds) {
        return new TimeExpression(milliseconds / 50L); // Convertir ms a ticks
    }
    
    /**
     * Crea una TimeExpression desde segundos.
     * 
     * @param seconds La cantidad de segundos
     * @return Una nueva instancia de TimeExpression
     */
    public static TimeExpression fromSeconds(long seconds) {
        return new TimeExpression(seconds * 20L); // Convertir segundos a ticks
    }
    
    /**
     * Valida si una expresión de tiempo es válida.
     * 
     * @param expression La expresión a validar
     * @return true si es válida, false en caso contrario
     */
    public static boolean isValid(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        return TimeConverter.parseTimeToTicks(expression.trim()) != -1;
    }
    
    @Override
    public String toString() {
        return "TimeExpression{" +
                "expression='" + expression + '\'' +
                ", ticks=" + ticks +
                ", readable='" + toReadableString() + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TimeExpression that = (TimeExpression) obj;
        return ticks == that.ticks;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(ticks);
    }
}