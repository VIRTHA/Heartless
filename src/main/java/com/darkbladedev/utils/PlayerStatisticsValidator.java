package com.darkbladedev.utils;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Validador de estadísticas de jugadores para eventos semanales.
 * Proporciona validación de datos, detección de anomalías y limpieza de estadísticas.
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 1.0
 */
public class PlayerStatisticsValidator {
    
    // Patrones de validación
    private static final Pattern VALID_STATISTIC_NAME = Pattern.compile("^[a-zA-Z0-9_]{1,50}$");
    private static final Pattern VALID_CHALLENGE_ID = Pattern.compile("^[a-zA-Z0-9_]{1,30}$");
    
    // Límites de validación
    private static final long MAX_NUMERIC_VALUE = 1_000_000_000L; // 1 billón
    private static final int MAX_STRING_LENGTH = 500;
    private static final int MAX_STATISTICS_PER_PLAYER = 100;
    private static final int MAX_CHALLENGES_PER_PLAYER = 50;
    
    // Cache de validaciones para optimización
    private final Map<String, Boolean> validationCache = new ConcurrentHashMap<>();
    
    /**
     * Valida las estadísticas de un jugador.
     * 
     * @param playerId UUID del jugador
     * @param statistics Mapa de estadísticas del jugador
     * @return ValidationResult con el resultado de la validación
     */
    public ValidationResult validatePlayerStatistics(UUID playerId, Map<String, Object> statistics) {
        if (playerId == null) {
            return ValidationResult.failure("UUID del jugador no puede ser null");
        }
        
        if (statistics == null) {
            return ValidationResult.success(); // Estadísticas vacías son válidas
        }
        
        // Verificar límite de estadísticas
        if (statistics.size() > MAX_STATISTICS_PER_PLAYER) {
            return ValidationResult.failure("Demasiadas estadísticas para el jugador: " + statistics.size());
        }
        
        List<String> errors = new ArrayList<>();
        Map<String, Object> cleanedStatistics = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : statistics.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Validar nombre de estadística
            ValidationResult keyValidation = validateStatisticName(key);
            if (!keyValidation.isValid()) {
                errors.add("Nombre de estadística inválido '" + key + "': " + keyValidation.getErrorMessage());
                continue;
            }
            
            // Validar valor de estadística
            ValidationResult valueValidation = validateStatisticValue(key, value);
            if (!valueValidation.isValid()) {
                errors.add("Valor de estadística inválido para '" + key + "': " + valueValidation.getErrorMessage());
                continue;
            }
            
            // Limpiar y normalizar el valor
            Object cleanedValue = cleanStatisticValue(value);
            cleanedStatistics.put(key, cleanedValue);
        }
        
        if (!errors.isEmpty()) {
            return ValidationResult.failure("Errores de validación: " + String.join(", ", errors));
        }
        
        return ValidationResult.success(cleanedStatistics);
    }
    
    /**
     * Valida los desafíos completados de un jugador.
     * 
     * @param playerId UUID del jugador
     * @param completedChallenges Set de IDs de desafíos completados
     * @return ValidationResult con el resultado de la validación
     */
    public ValidationResult validateCompletedChallenges(UUID playerId, Set<String> completedChallenges) {
        if (playerId == null) {
            return ValidationResult.failure("UUID del jugador no puede ser null");
        }
        
        if (completedChallenges == null) {
            return ValidationResult.success(); // Set vacío es válido
        }
        
        // Verificar límite de desafíos
        if (completedChallenges.size() > MAX_CHALLENGES_PER_PLAYER) {
            return ValidationResult.failure("Demasiados desafíos completados: " + completedChallenges.size());
        }
        
        List<String> errors = new ArrayList<>();
        Set<String> cleanedChallenges = new HashSet<>();
        
        for (String challengeId : completedChallenges) {
            ValidationResult validation = validateChallengeId(challengeId);
            if (!validation.isValid()) {
                errors.add("ID de desafío inválido '" + challengeId + "': " + validation.getErrorMessage());
                continue;
            }
            
            cleanedChallenges.add(challengeId.toLowerCase().trim());
        }
        
        if (!errors.isEmpty()) {
            return ValidationResult.failure("Errores de validación: " + String.join(", ", errors));
        }
        
        return ValidationResult.success(cleanedChallenges);
    }
    
    /**
     * Valida el progreso de desafíos de un jugador.
     * 
     * @param playerId UUID del jugador
     * @param challengeProgress Mapa de progreso de desafíos
     * @return ValidationResult con el resultado de la validación
     */
    public ValidationResult validateChallengeProgress(UUID playerId, Map<String, Object> challengeProgress) {
        if (playerId == null) {
            return ValidationResult.failure("UUID del jugador no puede ser null");
        }
        
        if (challengeProgress == null) {
            return ValidationResult.success(); // Progreso vacío es válido
        }
        
        // Verificar límite de desafíos en progreso
        if (challengeProgress.size() > MAX_CHALLENGES_PER_PLAYER) {
            return ValidationResult.failure("Demasiados desafíos en progreso: " + challengeProgress.size());
        }
        
        List<String> errors = new ArrayList<>();
        Map<String, Object> cleanedProgress = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : challengeProgress.entrySet()) {
            String challengeId = entry.getKey();
            Object progress = entry.getValue();
            
            // Validar ID de desafío
            ValidationResult idValidation = validateChallengeId(challengeId);
            if (!idValidation.isValid()) {
                errors.add("ID de desafío inválido '" + challengeId + "': " + idValidation.getErrorMessage());
                continue;
            }
            
            // Validar progreso
            ValidationResult progressValidation = validateProgressValue(progress);
            if (!progressValidation.isValid()) {
                errors.add("Progreso inválido para desafío '" + challengeId + "': " + progressValidation.getErrorMessage());
                continue;
            }
            
            cleanedProgress.put(challengeId.toLowerCase().trim(), cleanProgressValue(progress));
        }
        
        if (!errors.isEmpty()) {
            return ValidationResult.failure("Errores de validación: " + String.join(", ", errors));
        }
        
        return ValidationResult.success(cleanedProgress);
    }
    
    /**
     * Detecta anomalías en las estadísticas de un jugador.
     * 
     * @param playerId UUID del jugador
     * @param statistics Estadísticas del jugador
     * @param player Instancia del jugador (opcional, para validaciones adicionales)
     * @return Lista de anomalías detectadas
     */
    public List<String> detectAnomalies(UUID playerId, Map<String, Object> statistics, Player player) {
        List<String> anomalies = new ArrayList<>();
        
        if (statistics == null || statistics.isEmpty()) {
            return anomalies;
        }
        
        // Detectar valores extremadamente altos
        for (Map.Entry<String, Object> entry : statistics.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Number) {
                long numValue = ((Number) value).longValue();
                if (numValue > MAX_NUMERIC_VALUE) {
                    anomalies.add("Valor sospechosamente alto para '" + key + "': " + numValue);
                }
                
                // Detectar valores negativos donde no deberían existir
                if (numValue < 0 && isPositiveOnlyStatistic(key)) {
                    anomalies.add("Valor negativo en estadística que debe ser positiva '" + key + "': " + numValue);
                }
            }
        }
        
        // Validaciones específicas si el jugador está disponible
        if (player != null) {
            // Verificar coherencia con el estado actual del jugador
            validatePlayerStateCoherence(statistics, player, anomalies);
        }
        
        return anomalies;
    }
    
    /**
     * Valida el nombre de una estadística.
     */
    private ValidationResult validateStatisticName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.failure("Nombre de estadística no puede estar vacío");
        }
        
        String trimmedName = name.trim();
        if (trimmedName.length() > 50) {
            return ValidationResult.failure("Nombre de estadística demasiado largo");
        }
        
        if (!VALID_STATISTIC_NAME.matcher(trimmedName).matches()) {
            return ValidationResult.failure("Nombre de estadística contiene caracteres inválidos");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Valida el valor de una estadística.
     */
    private ValidationResult validateStatisticValue(Object value) {
        if (value == null) {
            return ValidationResult.success(); // Valores null son válidos
        }
        
        if (value instanceof Number) {
            Number numValue = (Number) value;
            if (numValue.longValue() > MAX_NUMERIC_VALUE) {
                return ValidationResult.failure("Valor numérico demasiado grande");
            }
            return ValidationResult.success();
        }
        
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.length() > MAX_STRING_LENGTH) {
                return ValidationResult.failure("Valor de texto demasiado largo");
            }
            return ValidationResult.success();
        }
        
        if (value instanceof Boolean) {
            return ValidationResult.success();
        }
        
        return ValidationResult.failure("Tipo de valor no soportado: " + value.getClass().getSimpleName());
    }
    
    /**
     * Valida el valor de una estadística con su nombre para verificaciones adicionales.
     */
    private ValidationResult validateStatisticValue(String name, Object value) {
        ValidationResult basicValidation = validateStatisticValue(value);
        if (!basicValidation.isValid()) {
            return basicValidation;
        }
        
        // Verificar valores negativos en estadísticas que deben ser positivas
        if (value instanceof Number) {
            Number numValue = (Number) value;
            if (numValue.longValue() < 0 && isPositiveOnlyStatistic(name)) {
                return ValidationResult.failure("Valor negativo en estadística que debe ser positiva");
            }
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Valida un ID de desafío.
     */
    private ValidationResult validateChallengeId(String challengeId) {
        if (challengeId == null || challengeId.trim().isEmpty()) {
            return ValidationResult.failure("ID de desafío no puede estar vacío");
        }
        
        String trimmedId = challengeId.trim();
        if (trimmedId.length() > 30) {
            return ValidationResult.failure("ID de desafío demasiado largo");
        }
        
        if (!VALID_CHALLENGE_ID.matcher(trimmedId).matches()) {
            return ValidationResult.failure("ID de desafío contiene caracteres inválidos");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Valida un valor de progreso.
     */
    private ValidationResult validateProgressValue(Object progress) {
        if (progress == null) {
            return ValidationResult.success();
        }
        
        if (progress instanceof Number) {
            Number numProgress = (Number) progress;
            if (numProgress.longValue() < 0) {
                return ValidationResult.failure("El progreso no puede ser negativo");
            }
            if (numProgress.longValue() > MAX_NUMERIC_VALUE) {
                return ValidationResult.failure("Valor de progreso demasiado grande");
            }
            return ValidationResult.success();
        }
        
        // Validar formato de string como "X/Y" (formato inválido)
        if (progress instanceof String) {
            return ValidationResult.failure("El progreso no puede ser un string. Use un número o un mapa con 'current' y 'max'");
        }
        
        // Validar formato de mapa {current: X, max: Y}
        if (progress instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> progressMap = (Map<String, Object>) progress;
            
            // Verificar que tenga las claves requeridas
            if (!progressMap.containsKey("current") || !progressMap.containsKey("max")) {
                return ValidationResult.failure("El mapa de progreso debe contener 'current' y 'max'");
            }
            
            // Validar valor 'current'
            Object current = progressMap.get("current");
            if (!(current instanceof Number)) {
                return ValidationResult.failure("El valor 'current' debe ser numérico");
            }
            if (((Number) current).longValue() < 0) {
                return ValidationResult.failure("El valor 'current' no puede ser negativo");
            }
            if (((Number) current).longValue() > MAX_NUMERIC_VALUE) {
                return ValidationResult.failure("El valor 'current' es demasiado grande");
            }
            
            // Validar valor 'max'
            Object max = progressMap.get("max");
            if (!(max instanceof Number)) {
                return ValidationResult.failure("El valor 'max' debe ser numérico");
            }
            if (((Number) max).longValue() <= 0) {
                return ValidationResult.failure("El valor 'max' debe ser positivo");
            }
            if (((Number) max).longValue() > MAX_NUMERIC_VALUE) {
                return ValidationResult.failure("El valor 'max' es demasiado grande");
            }
            
            return ValidationResult.success();
        }
        
        return ValidationResult.failure("El progreso debe ser un valor numérico o un mapa con 'current' y 'max'");
    }
    
    /**
     * Limpia y normaliza un valor de estadística.
     */
    private Object cleanStatisticValue(Object value) {
        if (value instanceof String) {
            return ((String) value).trim();
        }
        return value;
    }
    
    /**
     * Limpia y normaliza un valor de progreso.
     */
    private Object cleanProgressValue(Object progress) {
        if (progress instanceof Number) {
            // Asegurar que el progreso no sea negativo
            long longValue = ((Number) progress).longValue();
            return Math.max(0, longValue);
        }
        
        // Limpiar formato de mapa {current: X, max: Y}
        if (progress instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> progressMap = (Map<String, Object>) progress;
            
            Map<String, Object> cleanedMap = new HashMap<>();
            
            // Limpiar valor 'current'
            Object current = progressMap.get("current");
            if (current instanceof Number) {
                long currentValue = ((Number) current).longValue();
                cleanedMap.put("current", Math.max(0, currentValue));
            }
            
            // Limpiar valor 'max'
            Object max = progressMap.get("max");
            if (max instanceof Number) {
                long maxValue = ((Number) max).longValue();
                cleanedMap.put("max", Math.max(1, maxValue)); // Mínimo 1 para evitar división por cero
            }
            
            return cleanedMap;
        }
        
        return progress;
    }
    
    /**
     * Verifica si una estadística debe ser solo positiva.
     */
    private boolean isPositiveOnlyStatistic(String statisticName) {
        return statisticName.contains("kills") || 
               statisticName.contains("deaths") || 
               statisticName.contains("score") ||
               statisticName.contains("count") ||
               statisticName.contains("total") ||
               statisticName.contains("collected") ||
               statisticName.contains("heads");
    }
    
    /**
     * Valida la coherencia entre las estadísticas y el estado actual del jugador.
     */
    private void validatePlayerStateCoherence(Map<String, Object> statistics, Player player, List<String> anomalies) {
        // Verificar coherencia con el nivel del jugador
        Object levelStat = statistics.get("level");
        if (levelStat instanceof Number && player.getLevel() != ((Number) levelStat).intValue()) {
            // Esta no es necesariamente una anomalía, pero puede ser útil para debugging
        }
        
        // Verificar coherencia con la salud del jugador
        Object healthStat = statistics.get("health");
        if (healthStat instanceof Number) {
            double statHealth = ((Number) healthStat).doubleValue();
            double currentHealth = player.getHealth();
            if (Math.abs(statHealth - currentHealth) > 20.0) { // Diferencia significativa
                anomalies.add("Discrepancia significativa en salud: estadística=" + statHealth + ", actual=" + currentHealth);
            }
        }
    }
    
    /**
     * Limpia el cache de validaciones.
     */
    public void clearValidationCache() {
        validationCache.clear();
    }
    
    /**
     * Clase para representar el resultado de una validación.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final Object cleanedData;
        
        private ValidationResult(boolean valid, String errorMessage, Object cleanedData) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.cleanedData = cleanedData;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null, null);
        }
        
        public static ValidationResult success(Object cleanedData) {
            return new ValidationResult(true, null, cleanedData);
        }
        
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage, null);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public Object getCleanedData() {
            return cleanedData;
        }
        
        @SuppressWarnings("unchecked")
        public <T> T getCleanedDataAs(Class<T> type) {
            if (cleanedData == null) {
                return null;
            }
            
            // Verificación de tipo más estricta
            if (type.isAssignableFrom(cleanedData.getClass())) {
                return (T) cleanedData;
            }
            
            // Manejo especial para tipos genéricos comunes
            if (type == Map.class && cleanedData instanceof Map) {
                return (T) cleanedData;
            }
            
            if (type == Set.class && cleanedData instanceof Set) {
                return (T) cleanedData;
            }
            
            if (type == List.class && cleanedData instanceof List) {
                return (T) cleanedData;
            }
            
            return null;
        }
    }
}