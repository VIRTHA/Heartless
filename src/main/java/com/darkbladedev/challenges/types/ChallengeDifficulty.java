package com.darkbladedev.challenges.types;

/**
 * Enumeración que define los niveles de dificultad estandarizados
 * para los desafíos de eventos semanales.
 * 
 * Cada nivel incluye:
 * - Nombre descriptivo
 * - Color asociado para la interfaz
 * - Multiplicador de recompensas
 * - Tiempo estimado de completado
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public enum ChallengeDifficulty {
    
    /**
     * Dificultad Baja - Desafíos simples y rápidos
     * - Tiempo estimado: 5-15 minutos
     * - Multiplicador de recompensas: 1.0x
     * - Color: Verde
     */
    LOW("Baja", "§a", 1.0, 900000L), // 15 minutos
    
    /**
     * Dificultad Media - Desafíos moderados
     * - Tiempo estimado: 15-45 minutos
     * - Multiplicador de recompensas: 1.5x
     * - Color: Amarillo
     */
    MEDIUM("Media", "§e", 1.5, 2700000L), // 45 minutos
    
    /**
     * Dificultad Alta - Desafíos complejos y desafiantes
     * - Tiempo estimado: 45+ minutos
     * - Multiplicador de recompensas: 2.0x
     * - Color: Rojo
     */
    HIGH("Alta", "§c", 2.0, 5400000L); // 90 minutos
    
    private final String displayName;
    private final String colorCode;
    private final double rewardMultiplier;
    private final long estimatedTimeMs;
    
    /**
     * Constructor del enum de dificultad.
     * 
     * @param displayName Nombre para mostrar
     * @param colorCode Código de color de Minecraft
     * @param rewardMultiplier Multiplicador de recompensas
     * @param estimatedTimeMs Tiempo estimado en milisegundos
     */
    ChallengeDifficulty(String displayName, String colorCode, double rewardMultiplier, long estimatedTimeMs) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.rewardMultiplier = rewardMultiplier;
        this.estimatedTimeMs = estimatedTimeMs;
    }
    
    /**
     * Obtiene el nombre para mostrar de la dificultad.
     * 
     * @return Nombre descriptivo
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Obtiene el código de color asociado.
     * 
     * @return Código de color de Minecraft
     */
    public String getColorCode() {
        return colorCode;
    }
    
    /**
     * Obtiene el multiplicador de recompensas.
     * 
     * @return Multiplicador (1.0 = normal, 2.0 = doble)
     */
    public double getRewardMultiplier() {
        return rewardMultiplier;
    }
    
    /**
     * Obtiene el tiempo estimado de completado.
     * 
     * @return Tiempo en milisegundos
     */
    public long getEstimatedTimeMs() {
        return estimatedTimeMs;
    }
    
    /**
     * Obtiene el nombre coloreado para mostrar.
     * 
     * @return Nombre con código de color
     */
    public String getColoredName() {
        return colorCode + displayName + "§r";
    }
    
    /**
     * Obtiene el tiempo estimado formateado.
     * 
     * @return Tiempo en formato legible
     */
    public String getFormattedTime() {
        long minutes = estimatedTimeMs / 60000L;
        if (minutes < 60) {
            return minutes + " minutos";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " hora" + (hours > 1 ? "s" : "");
            } else {
                return hours + "h " + remainingMinutes + "m";
            }
        }
    }
    
    /**
     * Convierte una cadena a dificultad.
     * 
     * @param difficulty Cadena de dificultad
     * @return ChallengeDifficulty correspondiente
     * @throws IllegalArgumentException si la dificultad no es válida
     */
    public static ChallengeDifficulty fromString(String difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("La dificultad no puede ser nula");
        }
        
        String normalized = difficulty.toLowerCase().trim();
        switch (normalized) {
            case "baja":
            case "low":
            case "facil":
            case "easy":
            case "1":
                return LOW;
            case "media":
            case "medium":
            case "moderada":
            case "moderate":
            case "2":
                return MEDIUM;
            case "alta":
            case "high":
            case "dificil":
            case "hard":
            case "3":
                return HIGH;
            default:
                throw new IllegalArgumentException("Dificultad no válida: " + difficulty + 
                    ". Valores válidos: baja, media, alta");
        }
    }
    
    /**
     * Obtiene todas las dificultades disponibles como cadena.
     * 
     * @return Lista de dificultades formateada
     */
    public static String getAvailableDifficulties() {
        StringBuilder sb = new StringBuilder();
        for (ChallengeDifficulty difficulty : values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(difficulty.getColoredName());
        }
        return sb.toString();
    }
    
    /**
     * Verifica si una dificultad es válida.
     * 
     * @param difficulty Cadena a verificar
     * @return true si es válida
     */
    public static boolean isValid(String difficulty) {
        try {
            fromString(difficulty);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}