package com.darkbladedev.challenges;

import com.darkbladedev.challenges.types.ChallengeDifficulty;
import com.darkbladedev.challenges.types.ChallengeType;
import com.darkbladedev.challenges.validation.ChallengeValidator;
import java.util.*;
import java.util.function.Predicate;

/**
 * Sistema estandarizado para la creación de desafíos en eventos semanales.
 * 
 * Proporciona una interfaz fluida y consistente para crear desafíos con
 * validaciones automáticas y estructura estandarizada.
 * 
 * Características:
 * - Validación automática de atributos obligatorios
 * - Plantillas predefinidas para diferentes tipos de desafíos
 * - Sistema de dificultad estandarizado
 * - Gestión automática de recompensas
 * - Compatibilidad completa con el sistema existente
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public class ChallengeBuilder {
    
    // === ATRIBUTOS OBLIGATORIOS ===
    private String id;
    private String title;
    private String description;
    private ChallengeDifficulty difficulty;
    private List<String> rewards;
    
    // === ATRIBUTOS OPCIONALES ===
    private ChallengeType type;
    private Map<String, Object> technicalRequirements;
    private int requiredProgress;
    private Map<String, String> metadata;
    private List<String> prerequisites;
    private Predicate<UUID> completionCondition;
    private boolean isRepeatable;
    private long cooldownTime;

    
    // === VALIDADOR ===
    private final ChallengeValidator validator;
    
    /**
     * Constructor del builder de desafíos.
     */
    public ChallengeBuilder() {
        this.validator = new ChallengeValidator();
        this.rewards = new ArrayList<>();
        this.technicalRequirements = new HashMap<>();
        this.metadata = new HashMap<>();
        this.prerequisites = new ArrayList<>();
        this.requiredProgress = 1;
        this.isRepeatable = false;
        this.cooldownTime = 0L;
    }
    
    // === MÉTODOS PARA ATRIBUTOS OBLIGATORIOS ===
    
    /**
     * Establece el ID único del desafío.
     * 
     * @param id ID único del desafío (obligatorio)
     * @return Builder para encadenamiento
     * @throws IllegalArgumentException si el ID es inválido
     */
    public ChallengeBuilder withId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del desafío no puede ser nulo o vacío");
        }
        if (!id.matches("^[a-zA-Z0-9_]{3,30}$")) {
            throw new IllegalArgumentException("El ID debe contener solo letras, números y guiones bajos (3-30 caracteres)");
        }
        this.id = id.toLowerCase().trim();
        return this;
    }
    
    /**
     * Establece el título del desafío.
     * 
     * @param title Título descriptivo del desafío (obligatorio)
     * @return Builder para encadenamiento
     * @throws IllegalArgumentException si el título es inválido
     */
    public ChallengeBuilder withTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título del desafío no puede ser nulo o vacío");
        }
        if (title.length() > 50) {
            throw new IllegalArgumentException("El título no puede exceder 50 caracteres");
        }
        this.title = title.trim();
        return this;
    }
    
    /**
     * Establece la descripción detallada del desafío.
     * 
     * @param description Descripción detallada del desafío (obligatorio)
     * @return Builder para encadenamiento
     * @throws IllegalArgumentException si la descripción es inválida
     */
    public ChallengeBuilder withDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del desafío no puede ser nula o vacía");
        }
        if (description.length() > 200) {
            throw new IllegalArgumentException("La descripción no puede exceder 200 caracteres");
        }
        this.description = description.trim();
        return this;
    }
    
    /**
     * Establece la dificultad del desafío.
     * 
     * @param difficulty Nivel de dificultad (obligatorio)
     * @return Builder para encadenamiento
     * @throws IllegalArgumentException si la dificultad es nula
     */
    public ChallengeBuilder withDifficulty(ChallengeDifficulty difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("La dificultad del desafío no puede ser nula");
        }
        this.difficulty = difficulty;
        return this;
    }
    
    /**
     * Establece las recompensas del desafío.
     * 
     * @param rewards Lista de recompensas (obligatorio, al menos una)
     * @return Builder para encadenamiento
     * @throws IllegalArgumentException si las recompensas son inválidas
     */
    public ChallengeBuilder withRewards(List<String> rewards) {
        if (rewards == null || rewards.isEmpty()) {
            throw new IllegalArgumentException("El desafío debe tener al menos una recompensa");
        }
        this.rewards = new ArrayList<>(rewards);
        return this;
    }
    
    /**
     * Añade una recompensa individual al desafío.
     * 
     * @param reward Recompensa a añadir
     * @return Builder para encadenamiento
     */
    public ChallengeBuilder addReward(String reward) {
        if (reward != null && !reward.trim().isEmpty()) {
            this.rewards.add(reward.trim());
        }
        return this;
    }
    
    // === MÉTODOS PARA ATRIBUTOS OPCIONALES ===
    
    /**
     * Establece el tipo de desafío.
     * 
     * @param type Tipo de desafío
     * @return Builder para encadenamiento
     */
    public ChallengeBuilder withType(ChallengeType type) {
        this.type = type;
        return this;
    }
    
    /**
     * Establece los requisitos técnicos del desafío.
     * 
     * @param requirements Mapa con requisitos técnicos
     * @return Builder para encadenamiento
     */
    public ChallengeBuilder withTechnicalRequirements(Map<String, Object> requirements) {
        if (requirements != null) {
            this.technicalRequirements = new HashMap<>(requirements);
        }
        return this;
    }
    
    /**
     * Añade un requisito técnico específico.
     * 
     * @param key Clave del requisito
     * @param value Valor del requisito
     * @return Builder para encadenamiento
     */
    public ChallengeBuilder addTechnicalRequirement(String key, Object value) {
        if (key != null && value != null) {
            this.technicalRequirements.put(key, value);
        }
        return this;
    }
    
    /**
     * Establece el progreso requerido para completar el desafío.
     * 
     * @param requiredProgress Progreso requerido (por defecto: 1)
     * @return Builder para encadenamiento
     */
    public ChallengeBuilder withRequiredProgress(int requiredProgress) {
        if (requiredProgress < 1) {
            throw new IllegalArgumentException("El progreso requerido debe ser mayor a 0");
        }
        this.requiredProgress = requiredProgress;
        return this;
    }
    
    /**
     * Añade metadatos al desafío.
     * 
     * @param key Clave del metadato
     * @param value Valor del metadato
     * @return Builder para encadenamiento
     */
    public ChallengeBuilder addMetadata(String key, String value) {
        if (key != null && value != null) {
            this.metadata.put(key, value);
        }
        return this;
    }
    
    /**
     * Establece los prerequisitos del desafío.
     * 
     * @param prerequisites Lista de IDs de desafíos prerequisitos
     * @return Builder para encadenamiento
     */
    public ChallengeBuilder withPrerequisites(List<String> prerequisites) {
        if (prerequisites != null) {
            this.prerequisites = new ArrayList<>(prerequisites);
        }
        return this;
    }

    /**
     * Añade un prerequisito al desafío.
     * 
     * @param prerequisiteId ID del desafío prerequisito
     * @return Builder para encadenamiento
     */
    public ChallengeBuilder addPrerequisite(String prerequisiteId) {
        if (prerequisiteId != null && !prerequisiteId.trim().isEmpty()) {
            this.prerequisites.add(prerequisiteId.trim());
        }
        return this;
    }
    
    /**
     * Establece si el desafío es repetible.
     * 
     * @param repeatable true si es repetible
     * @return Builder para encadenamiento
     */
    public ChallengeBuilder setRepeatable(boolean repeatable) {
        this.isRepeatable = repeatable;
        return this;
    }
    
    /**
     * Establece el tiempo de cooldown entre repeticiones.
     * 
     * @param cooldownTime Tiempo en milisegundos
     * @return Builder para encadenamiento
     */
    public ChallengeBuilder withCooldown(long cooldownTime) {
        this.cooldownTime = Math.max(0, cooldownTime);
        return this;
    }
    
    /**
     * Establece una condición personalizada de completado.
     * 
     * @param condition Predicado que determina si el desafío está completado
     * @return Builder para encadenamiento
     */
    public ChallengeBuilder withCompletionCondition(Predicate<UUID> condition) {
        this.completionCondition = condition;
        return this;
    }
    
    // === MÉTODOS DE CONSTRUCCIÓN ===
    
    /**
     * Construye el desafío con validación completa.
     * 
     * @return ChallengeDefinition compatible con el sistema existente
     * @throws IllegalStateException si faltan atributos obligatorios
     * @throws IllegalArgumentException si algún atributo es inválido
     */
    public ChallengeDefinition build() {
        validateRequiredFields();
        
        StandardizedChallenge challenge = new StandardizedChallenge(
            id, title, description, difficulty, rewards,
            type, technicalRequirements, requiredProgress,
            metadata, prerequisites, completionCondition,
            isRepeatable, cooldownTime
        );
        
        validator.validate(challenge);
        
        // Crear ChallengeDefinition compatible con el sistema existente
        return new ChallengeDefinition(id, title, description, requiredProgress, rewards);
    }
    
    /**
     * Construye el desafío estandarizado completo.
     * 
     * @return StandardizedChallenge con todas las características avanzadas
     * @throws IllegalStateException si faltan atributos obligatorios
     */
    public StandardizedChallenge buildStandardized() {
        validateRequiredFields();
        
        StandardizedChallenge challenge = new StandardizedChallenge(
            id, title, description, difficulty, rewards,
            type, technicalRequirements, requiredProgress,
            metadata, prerequisites, completionCondition,
            isRepeatable, cooldownTime
        );
        
        validator.validate(challenge);
        return challenge;
    }
    
    /**
     * Valida que todos los campos obligatorios estén presentes.
     * 
     * @throws IllegalStateException si falta algún campo obligatorio
     */
    private void validateRequiredFields() {
        List<String> missingFields = new ArrayList<>();
        
        if (id == null || id.trim().isEmpty()) {
            missingFields.add("ID");
        }
        if (title == null || title.trim().isEmpty()) {
            missingFields.add("Título");
        }
        if (description == null || description.trim().isEmpty()) {
            missingFields.add("Descripción");
        }
        if (difficulty == null) {
            missingFields.add("Dificultad");
        }
        if (rewards == null || rewards.isEmpty()) {
            missingFields.add("Recompensas");
        }
        
        if (!missingFields.isEmpty()) {
            throw new IllegalStateException("Faltan los siguientes campos obligatorios: " + 
                                          String.join(", ", missingFields));
        }
    }
    
    /**
     * Resetea el builder para crear un nuevo desafío.
     * 
     * @return Builder limpio
     */
    public ChallengeBuilder reset() {
        this.id = null;
        this.title = null;
        this.description = null;
        this.difficulty = null;
        this.rewards = new ArrayList<>();
        this.type = null;
        this.technicalRequirements = new HashMap<>();
        this.requiredProgress = 1;
        this.metadata = new HashMap<>();
        this.prerequisites = new ArrayList<>();
        this.completionCondition = null;
        this.isRepeatable = false;
        this.cooldownTime = 0L;
        return this;
    }
    
    /**
     * Crea una copia del builder actual.
     * 
     * @return Nueva instancia del builder con los mismos valores
     */
    public ChallengeBuilder copy() {
        ChallengeBuilder copy = new ChallengeBuilder();
        copy.id = this.id;
        copy.title = this.title;
        copy.description = this.description;
        copy.difficulty = this.difficulty;
        copy.rewards = new ArrayList<>(this.rewards);
        copy.type = this.type;
        copy.technicalRequirements = new HashMap<>(this.technicalRequirements);
        copy.requiredProgress = this.requiredProgress;
        copy.metadata = new HashMap<>(this.metadata);
        copy.prerequisites = new ArrayList<>(this.prerequisites);
        copy.completionCondition = this.completionCondition;
        copy.isRepeatable = this.isRepeatable;
        copy.cooldownTime = this.cooldownTime;
        return copy;
    }
}