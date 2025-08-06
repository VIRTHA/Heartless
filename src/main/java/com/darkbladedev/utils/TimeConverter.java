package com.darkbladedev.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidad para convertir expresiones de tiempo a ticks de Minecraft
 * y viceversa.
 */
public class TimeConverter {
    
    // Constantes de conversión (en ticks)
    private static final long TICK = 1L;
    private static final long SECOND = 20L;
    private static final long MINUTE = SECOND * 60L;
    private static final long HOUR = MINUTE * 60L;
    private static final long DAY = HOUR * 24L;
    private static final long WEEK = DAY * 7L;
    private static final long MONTH = DAY * 30L;
    private static final long YEAR = DAY * 365L;
    
    // Mapa de unidades de tiempo y sus abreviaturas
    private static final Map<String, Long> TIME_UNITS = new HashMap<>();
    
    static {
        // Inicializar el mapa con las unidades de tiempo y sus valores en ticks
        TIME_UNITS.put("t", TICK);
        TIME_UNITS.put("tick", TICK);
        TIME_UNITS.put("ticks", TICK);
        
        TIME_UNITS.put("s", SECOND);
        TIME_UNITS.put("sec", SECOND);
        TIME_UNITS.put("second", SECOND);
        TIME_UNITS.put("seconds", SECOND);
        
        TIME_UNITS.put("m", MINUTE);
        TIME_UNITS.put("min", MINUTE);
        TIME_UNITS.put("minute", MINUTE);
        TIME_UNITS.put("minutes", MINUTE);
        
        TIME_UNITS.put("h", HOUR);
        TIME_UNITS.put("hour", HOUR);
        TIME_UNITS.put("hours", HOUR);
        
        TIME_UNITS.put("d", DAY);
        TIME_UNITS.put("day", DAY);
        TIME_UNITS.put("days", DAY);
        
        TIME_UNITS.put("w", WEEK);
        TIME_UNITS.put("week", WEEK);
        TIME_UNITS.put("weeks", WEEK);
        
        TIME_UNITS.put("mo", MONTH);
        TIME_UNITS.put("month", MONTH);
        TIME_UNITS.put("months", MONTH);
        
        TIME_UNITS.put("y", YEAR);
        TIME_UNITS.put("year", YEAR);
        TIME_UNITS.put("years", YEAR);
    }
    
    /**
     * Convierte una expresión de tiempo a ticks de Minecraft.
     * Ejemplos de expresiones válidas:
     * - "1h" (1 hora)
     * - "2d" (2 días)
     * - "1w 2d 3h" (1 semana, 2 días y 3 horas)
     * - "30s" (30 segundos)
     * - "1.5h" (1 hora y 30 minutos)
     * 
     * @param timeExpression La expresión de tiempo a convertir
     * @return La cantidad de ticks equivalente, o -1 si la expresión es inválida
     */
    public static long parseTimeToTicks(String timeExpression) {
        if (timeExpression == null || timeExpression.trim().isEmpty()) {
            return -1;
        }
        
        // Patrón para capturar números seguidos de unidades de tiempo
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([a-zA-Z]+)");
        Matcher matcher = pattern.matcher(timeExpression);
        
        long totalTicks = 0;
        boolean foundMatch = false;
        
        while (matcher.find()) {
            foundMatch = true;
            double amount = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            
            if (TIME_UNITS.containsKey(unit)) {
                totalTicks += Math.round(amount * TIME_UNITS.get(unit));
            } else {
                // Unidad de tiempo no reconocida
                return -1;
            }
        }
        
        return foundMatch ? totalTicks : -1;
    }
    
    /**
     * Convierte ticks de Minecraft a una representación legible de tiempo.
     * 
     * @param ticks La cantidad de ticks a convertir
     * @return Una cadena que representa el tiempo en formato legible
     */
    public static String formatTicksToTime(long ticks) {
        if (ticks < 0) {
            return "Tiempo inválido";
        }
        
        StringBuilder result = new StringBuilder();
        
        // Convertir ticks a diferentes unidades de tiempo
        long remainingTicks = ticks;
        
        // Años
        long years = remainingTicks / YEAR;
        if (years > 0) {
            result.append(years).append(years == 1 ? " año" : " años");
            remainingTicks %= YEAR;
            if (remainingTicks > 0) result.append(", ");
        }
        
        // Meses
        long months = remainingTicks / MONTH;
        if (months > 0) {
            result.append(months).append(months == 1 ? " mes" : " meses");
            remainingTicks %= MONTH;
            if (remainingTicks > 0) result.append(", ");
        }
        
        // Semanas
        long weeks = remainingTicks / WEEK;
        if (weeks > 0) {
            result.append(weeks).append(weeks == 1 ? " semana" : " semanas");
            remainingTicks %= WEEK;
            if (remainingTicks > 0) result.append(", ");
        }
        
        // Días
        long days = remainingTicks / DAY;
        if (days > 0) {
            result.append(days).append(days == 1 ? " día" : " días");
            remainingTicks %= DAY;
            if (remainingTicks > 0) result.append(", ");
        }
        
        // Horas
        long hours = remainingTicks / HOUR;
        if (hours > 0) {
            result.append(hours).append(hours == 1 ? " hora" : " horas");
            remainingTicks %= HOUR;
            if (remainingTicks > 0) result.append(", ");
        }
        
        // Minutos
        long minutes = remainingTicks / MINUTE;
        if (minutes > 0) {
            result.append(minutes).append(minutes == 1 ? " minuto" : " minutos");
            remainingTicks %= MINUTE;
            if (remainingTicks > 0) result.append(", ");
        }
        
        // Segundos
        long seconds = remainingTicks / SECOND;
        if (seconds > 0 || (result.length() == 0 && remainingTicks < SECOND)) {
            result.append(seconds).append(seconds == 1 ? " segundo" : " segundos");
            remainingTicks %= SECOND;
            if (remainingTicks > 0) result.append(", ");
        }
        
        // Ticks restantes (solo si no hay otras unidades o si es necesario ser preciso)
        if (remainingTicks > 0 && result.length() == 0) {
            result.append(remainingTicks).append(remainingTicks == 1 ? " tick" : " ticks");
        }
        
        return result.toString();
    }
    
    /**
     * Genera sugerencias de tiempo para autocompletado.
     * 
     * @return Un array de sugerencias comunes de tiempo
     */
    public static String[] getTimeCompletions() {
        return new String[] {
            "30s", "1m", "5m", "10m", "30m", 
            "1h", "2h", "6h", "12h", 
            "1d", "2d", "3d", "7d",
            "1w", "2w", "1mo"
        };
    }
}