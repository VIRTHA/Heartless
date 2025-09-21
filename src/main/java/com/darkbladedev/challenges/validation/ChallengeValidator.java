package com.darkbladedev.challenges.validation;

import com.darkbladedev.challenges.StandardizedChallenge;
import com.darkbladedev.challenges.types.ChallengeDifficulty;
import com.darkbladedev.challenges.types.ChallengeType;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Validador para desafíos estandarizados que asegura
 * la integridad, consistencia y calidad de los desafíos
 * creados en el sistema.
 * 
 * Realiza validaciones de:
 * - Estructura y formato
 * - Consistencia de datos
 * - Reglas de negocio
 * - Compatibilidad con el sistema
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public class ChallengeValidator {
    
    // === PATRONES DE VALIDACIÓN ===
    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final Pattern REWARD_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\s\\-:]+$");
    
    // === LÍMITES DE VALIDACIÓN ===
    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final int MAX_REWARDS = 10;
    private static final int MAX_PREREQUISITES = 5;
    private static final int MAX_TECHNICAL_REQUIREMENTS = 20;
    private static final int MIN_REQUIRED_PROGRESS = 1;
    private static final int MAX_REQUIRED_PROGRESS = 10000;
    
    // === PALABRAS PROHIBIDAS ===
    private static final Set<String> FORBIDDEN_WORDS = Set.of(
        "hack", "cheat", "exploit", "bug", "glitch", "admin", "op"
    );
    
    // === RECOMPENSAS VÁLIDAS ===
    private static final Set<String> VALID_REWARD_PREFIXES = Set.of(
        "money:", "item:", "xp:", "permission:", "command:", "title:"
    );
    
    /**
     * Valida un desafío estandarizado completo.
     * 
     * @param challenge El desafío a validar
     * @throws ValidationException si el desafío no es válido
     */
    public static void validateChallenge(StandardizedChallenge challenge) throws ValidationException {
        if (challenge == null) {
            throw new ValidationException("El desafío no puede ser null");
        }
        
        // Validar estructura básica
        validateStructure(challenge);
        
        // Validar consistencia de datos
        validateDataConsistency(challenge);
        
        // Validar reglas de negocio
        validateBusinessRules(challenge);
        
        // Validar compatibilidad del sistema
        validateSystemCompatibility(challenge);
    }
    
    /**
     * Valida la estructura básica del desafío.
     * 
     * @param challenge Desafío a validar
     * @throws IllegalArgumentException si la estructura es inválida
     */
    private static void validateStructure(StandardizedChallenge challenge) {
        if (challenge.getId() == null || challenge.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del desafío no puede ser nulo o vacío");
        }
        
        if (challenge.getTitle() == null || challenge.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("El título del desafío no puede ser nulo o vacío");
        }
        
        if (challenge.getDescription() == null || challenge.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del desafío no puede ser nula o vacía");
        }
        
        if (challenge.getDifficulty() == null) {
            throw new IllegalArgumentException("La dificultad del desafío no puede ser nula");
        }
    }
    
    /**
     * Valida la consistencia de los datos del desafío.
     * 
     * @param challenge Desafío a validar
     * @throws IllegalArgumentException si los datos son inconsistentes
     */
    private static void validateDataConsistency(StandardizedChallenge challenge) {
        // Validar que las recompensas no estén vacías
        if (challenge.getRewards() == null || challenge.getRewards().isEmpty()) {
            throw new IllegalArgumentException("El desafío debe tener al menos una recompensa");
        }
        
        // Validar progreso requerido
        if (challenge.getRequiredProgress() < 1) {
            throw new IllegalArgumentException("El progreso requerido debe ser mayor a 0");
        }
    }
    
    /**
     * Valida las reglas de negocio del desafío.
     * 
     * @param challenge Desafío a validar
     * @throws IllegalArgumentException si las reglas de negocio no se cumplen
     */
    private static void validateBusinessRules(StandardizedChallenge challenge) {
        // Validar que el ID sea único (simulado)
        if (challenge.getId().length() < 3) {
            throw new IllegalArgumentException("El ID del desafío debe tener al menos 3 caracteres");
        }
        
        // Validar longitud de descripción
        if (challenge.getDescription().length() > 500) {
            throw new IllegalArgumentException("La descripción no puede exceder 500 caracteres");
        }
    }
    
    /**
     * Valida la compatibilidad del desafío con el sistema.
     * 
     * @param challenge Desafío a validar
     * @throws IllegalArgumentException si hay problemas de compatibilidad
     */
    private static void validateSystemCompatibility(StandardizedChallenge challenge) {
        // Validar que el tipo sea compatible
        if (challenge.getType() != null) {
            // Validación específica por tipo
            switch (challenge.getType()) {
                case COMBAT:
                    if (challenge.getRequiredProgress() > 1000) {
                        throw new IllegalArgumentException("Los desafíos de combate no pueden requerir más de 1000 eliminaciones");
                    }
                    break;
                case BUILDING:
                    if (challenge.getRequiredProgress() > 10000) {
                        throw new IllegalArgumentException("Los desafíos de construcción no pueden requerir más de 10000 bloques");
                    }
                    break;
                default:
                    // Otros tipos son válidos por defecto
                    break;
            }
        }
    }
    
    /**
     * Valida un desafío estandarizado de forma detallada.
     * 
     * @param challenge Desafío a validar
     * @throws ValidationException si la validación falla
     */
    public void validate(StandardizedChallenge challenge) throws ValidationException {
        if (challenge == null) {
            throw new ValidationException("El desafío no puede ser nulo");
        }
        
        List<String> errors = new ArrayList<>();
        
        // Validar atributos obligatorios
        validateId(challenge.getId(), errors);
        validateTitle(challenge.getTitle(), errors);
        validateDescription(challenge.getDescription(), errors);
        validateDifficulty(challenge.getDifficulty(), errors);
        validateRewards(challenge.getRewards(), errors);
        
        // Validar atributos opcionales
        validateType(challenge.getType(), errors);
        validateTechnicalRequirements(challenge.getTechnicalRequirements(), errors);
        validateRequiredProgress(challenge.getRequiredProgress(), errors);
        validatePrerequisites(challenge.getPrerequisites(), errors);
        validateRepeatableSettings(challenge.isRepeatable(), challenge.getCooldownTime(), errors);
        
        // Validar consistencia general
        validateConsistency(challenge, errors);
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Errores de validación encontrados:\n" + 
                                        String.join("\n", errors));
        }
    }
    
    /**
     * Valida el ID del desafío.
     */
    private void validateId(String id, List<String> errors) {
        if (id == null || id.trim().isEmpty()) {
            errors.add("- El ID del desafío es obligatorio");
            return;
        }
        
        if (!ID_PATTERN.matcher(id).matches()) {
            errors.add("- El ID debe contener solo letras, números y guiones bajos (3-30 caracteres)");
        }
        
        if (containsForbiddenWords(id)) {
            errors.add("- El ID contiene palabras no permitidas");
        }
    }
    
    /**
     * Valida el título del desafío.
     */
    private void validateTitle(String title, List<String> errors) {
        if (title == null || title.trim().isEmpty()) {
            errors.add("- El título del desafío es obligatorio");
            return;
        }
        
        if (title.length() > MAX_TITLE_LENGTH) {
            errors.add("- El título no puede exceder " + MAX_TITLE_LENGTH + " caracteres");
        }
        
        if (containsForbiddenWords(title)) {
            errors.add("- El título contiene palabras no permitidas");
        }
        
        if (title.trim().length() < 3) {
            errors.add("- El título debe tener al menos 3 caracteres");
        }
    }
    
    /**
     * Valida la descripción del desafío.
     */
    private void validateDescription(String description, List<String> errors) {
        if (description == null || description.trim().isEmpty()) {
            errors.add("- La descripción del desafío es obligatoria");
            return;
        }
        
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            errors.add("- La descripción no puede exceder " + MAX_DESCRIPTION_LENGTH + " caracteres");
        }
        
        if (containsForbiddenWords(description)) {
            errors.add("- La descripción contiene palabras no permitidas");
        }
        
        if (description.trim().length() < 10) {
            errors.add("- La descripción debe tener al menos 10 caracteres");
        }
    }
    
    /**
     * Valida la dificultad del desafío.
     */
    private void validateDifficulty(ChallengeDifficulty difficulty, List<String> errors) {
        if (difficulty == null) {
            errors.add("- La dificultad del desafío es obligatoria");
        }
    }
    
    /**
     * Valida las recompensas del desafío.
     */
    private void validateRewards(List<String> rewards, List<String> errors) {
        if (rewards == null || rewards.isEmpty()) {
            errors.add("- El desafío debe tener al menos una recompensa");
            return;
        }
        
        if (rewards.size() > MAX_REWARDS) {
            errors.add("- No se pueden tener más de " + MAX_REWARDS + " recompensas");
        }
        
        for (int i = 0; i < rewards.size(); i++) {
            String reward = rewards.get(i);
            if (reward == null || reward.trim().isEmpty()) {
                errors.add("- La recompensa #" + (i + 1) + " no puede estar vacía");
                continue;
            }
            
            if (!REWARD_PATTERN.matcher(reward).matches()) {
                errors.add("- La recompensa #" + (i + 1) + " contiene caracteres no válidos");
            }
            
            if (containsForbiddenWords(reward)) {
                errors.add("- La recompensa #" + (i + 1) + " contiene palabras no permitidas");
            }
            
            // Validar formato de recompensa
            if (!isValidRewardFormat(reward)) {
                errors.add("- La recompensa #" + (i + 1) + " no tiene un formato válido");
            }
        }
    }
    
    /**
     * Valida el tipo de desafío.
     */
    private void validateType(ChallengeType type, List<String> errors) {
        // El tipo es opcional, no hay validación específica necesaria
        // ya que el enum garantiza valores válidos
    }
    
    /**
     * Valida los requisitos técnicos.
     */
    private void validateTechnicalRequirements(Map<String, Object> requirements, List<String> errors) {
        if (requirements == null) {
            return; // Es opcional
        }
        
        if (requirements.size() > MAX_TECHNICAL_REQUIREMENTS) {
            errors.add("- No se pueden tener más de " + MAX_TECHNICAL_REQUIREMENTS + " requisitos técnicos");
        }
        
        for (Map.Entry<String, Object> entry : requirements.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (key == null || key.trim().isEmpty()) {
                errors.add("- Las claves de requisitos técnicos no pueden estar vacías");
            }
            
            if (value == null) {
                errors.add("- El requisito técnico '" + key + "' no puede tener valor nulo");
            }
            
            if (containsForbiddenWords(key)) {
                errors.add("- El requisito técnico '" + key + "' contiene palabras no permitidas");
            }
        }
    }
    
    /**
     * Valida el progreso requerido.
     */
    private void validateRequiredProgress(int requiredProgress, List<String> errors) {
        if (requiredProgress < MIN_REQUIRED_PROGRESS) {
            errors.add("- El progreso requerido debe ser al menos " + MIN_REQUIRED_PROGRESS);
        }
        
        if (requiredProgress > MAX_REQUIRED_PROGRESS) {
            errors.add("- El progreso requerido no puede exceder " + MAX_REQUIRED_PROGRESS);
        }
    }
    
    /**
     * Valida los prerequisitos.
     */
    private void validatePrerequisites(List<String> prerequisites, List<String> errors) {
        if (prerequisites == null) {
            return; // Es opcional
        }
        
        if (prerequisites.size() > MAX_PREREQUISITES) {
            errors.add("- No se pueden tener más de " + MAX_PREREQUISITES + " prerequisitos");
        }
        
        Set<String> uniquePrereqs = new HashSet<>();
        for (int i = 0; i < prerequisites.size(); i++) {
            String prereq = prerequisites.get(i);
            
            if (prereq == null || prereq.trim().isEmpty()) {
                errors.add("- El prerequisito #" + (i + 1) + " no puede estar vacío");
                continue;
            }
            
            if (!ID_PATTERN.matcher(prereq).matches()) {
                errors.add("- El prerequisito #" + (i + 1) + " no tiene un formato de ID válido");
            }
            
            if (!uniquePrereqs.add(prereq)) {
                errors.add("- El prerequisito '" + prereq + "' está duplicado");
            }
        }
    }
    
    /**
     * Valida la configuración de repetibilidad.
     */
    private void validateRepeatableSettings(boolean isRepeatable, long cooldownTime, List<String> errors) {
        if (!isRepeatable && cooldownTime > 0) {
            errors.add("- No se puede establecer cooldown en un desafío no repetible");
        }
        
        if (isRepeatable && cooldownTime < 0) {
            errors.add("- El tiempo de cooldown no puede ser negativo");
        }
    }
    
    /**
     * Valida la consistencia general del desafío.
     */
    private void validateConsistency(StandardizedChallenge challenge, List<String> errors) {
        // Validar consistencia entre dificultad y progreso requerido
        if (challenge.getDifficulty() != null) {
            int expectedMinProgress = getMinProgressForDifficulty(challenge.getDifficulty());
            if (challenge.getRequiredProgress() < expectedMinProgress) {
                errors.add("- El progreso requerido es muy bajo para la dificultad " + 
                          challenge.getDifficulty().getDisplayName());
            }
        }
        
        // Validar consistencia entre tipo y requisitos técnicos
        if (challenge.getType() != null && challenge.getTechnicalRequirements() != null) {
            validateTypeRequirementConsistency(challenge.getType(), 
                                             challenge.getTechnicalRequirements(), errors);
        }
        
        // Validar que las recompensas sean apropiadas para la dificultad
        if (challenge.getDifficulty() != null && challenge.getRewards() != null) {
            validateRewardDifficultyConsistency(challenge.getDifficulty(), 
                                              challenge.getRewards(), errors);
        }
    }
    
    /**
     * Obtiene el progreso mínimo esperado para una dificultad.
     */
    private int getMinProgressForDifficulty(ChallengeDifficulty difficulty) {
        switch (difficulty) {
            case LOW: return 1;
            case MEDIUM: return 3;
            case HIGH: return 5;
            default: return 1;
        }
    }
    
    /**
     * Valida la consistencia entre tipo y requisitos técnicos.
     */
    private void validateTypeRequirementConsistency(ChallengeType type, 
                                                  Map<String, Object> requirements, 
                                                  List<String> errors) {
        // Validaciones específicas por tipo
        switch (type) {
            case TIMED:
                if (!requirements.containsKey("timeLimit")) {
                    errors.add("- Los desafíos temporales deben especificar 'timeLimit'");
                }
                break;
            case COMBAT:
                if (!requirements.containsKey("mobType") && !requirements.containsKey("damageType")) {
                    errors.add("- Los desafíos de combate deben especificar 'mobType' o 'damageType'");
                }
                break;
            case BUILDING:
                if (!requirements.containsKey("blockType") && !requirements.containsKey("structure")) {
                    errors.add("- Los desafíos de construcción deben especificar 'blockType' o 'structure'");
                }
                break;
            case EXPLORATION:
                // Los desafíos de exploración pueden tener requisitos opcionales
                break;
            case SPECIAL:
                // Los desafíos especiales tienen requisitos flexibles
                break;
            case CRAFTING:
                if (!requirements.containsKey("itemType") && !requirements.containsKey("recipe")) {
                    errors.add("- Los desafíos de crafting deben especificar 'itemType' o 'recipe'");
                }
                break;
            case SURVIVAL:
                // Los desafíos de supervivencia pueden tener requisitos variados
                break;
            case SOCIAL:
                if (!requirements.containsKey("playerCount") && !requirements.containsKey("interaction")) {
                    errors.add("- Los desafíos sociales deben especificar 'playerCount' o 'interaction'");
                }
                break;
            case FARMING:
                if (!requirements.containsKey("cropType") && !requirements.containsKey("animalType")) {
                    errors.add("- Los desafíos de farming deben especificar 'cropType' o 'animalType'");
                }
                break;
            case TRADING:
                if (!requirements.containsKey("villagerType") && !requirements.containsKey("tradeType")) {
                    errors.add("- Los desafíos de trading deben especificar 'villagerType' o 'tradeType'");
                }
                break;
            case MINING:
                if (!requirements.containsKey("blockType") && !requirements.containsKey("depth")) {
                    errors.add("- Los desafíos de mining deben especificar 'blockType' o 'depth'");
                }
                break;
            case COLLECTION:
                if (!requirements.containsKey("itemType") && !requirements.containsKey("quantity")) {
                    errors.add("- Los desafíos de collection deben especificar 'itemType' o 'quantity'");
                }
                break;
        }
    }
    
    /**
     * Valida la consistencia entre recompensas y dificultad.
     */
    private void validateRewardDifficultyConsistency(ChallengeDifficulty difficulty, 
                                                   List<String> rewards, 
                                                   List<String> errors) {
        int rewardCount = rewards.size();
        int expectedMinRewards = getMinRewardsForDifficulty(difficulty);
        
        if (rewardCount < expectedMinRewards) {
            errors.add("- La dificultad " + difficulty.getDisplayName() + 
                      " requiere al menos " + expectedMinRewards + " recompensas");
        }
    }
    
    /**
     * Obtiene el número mínimo de recompensas para una dificultad.
     */
    private int getMinRewardsForDifficulty(ChallengeDifficulty difficulty) {
        switch (difficulty) {
            case LOW: return 1;
            case MEDIUM: return 2;
            case HIGH: return 3;
            default: return 1;
        }
    }
    
    /**
     * Verifica si un texto contiene palabras prohibidas.
     */
    private boolean containsForbiddenWords(String text) {
        if (text == null) return false;
        
        String lowerText = text.toLowerCase();
        return FORBIDDEN_WORDS.stream().anyMatch(word -> {
            // Verificar que la palabra prohibida esté como palabra completa, no como parte de otra palabra
            String pattern = "\\b" + word + "\\b";
            return lowerText.matches(".*" + pattern + ".*");
        });
    }
    
    /**
     * Verifica si una recompensa tiene un formato válido.
     */
    private boolean isValidRewardFormat(String reward) {
        if (reward == null || reward.trim().isEmpty()) {
            return false;
        }
        
        // Verificar si tiene un prefijo válido
        String lowerReward = reward.toLowerCase();
        boolean hasValidPrefix = VALID_REWARD_PREFIXES.stream()
                .anyMatch(lowerReward::startsWith);
        
        // Si no tiene prefijo válido, asumir que es texto libre (también válido)
        return hasValidPrefix || !reward.contains(":");
    }
    
    /**
     * Valida rápidamente solo los campos obligatorios.
     * 
     * @param id ID del desafío
     * @param title Título
     * @param description Descripción
     * @param difficulty Dificultad
     * @param rewards Recompensas
     * @throws ValidationException si la validación falla
     */
    public void validateRequired(String id, String title, String description, 
                               ChallengeDifficulty difficulty, List<String> rewards) 
                               throws ValidationException {
        List<String> errors = new ArrayList<>();
        
        validateId(id, errors);
        validateTitle(title, errors);
        validateDescription(description, errors);
        validateDifficulty(difficulty, errors);
        validateRewards(rewards, errors);
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Errores en campos obligatorios:\n" + 
                                        String.join("\n", errors));
        }
    }
    
    /**
     * Verifica si un desafío es válido sin lanzar excepciones.
     * 
     * @param challenge El desafío a validar
     * @return true si el desafío es válido, false en caso contrario
     */
    public static boolean isValid(StandardizedChallenge challenge) {
        try {
            validateChallenge(challenge);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Excepción personalizada para errores de validación.
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
        
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}