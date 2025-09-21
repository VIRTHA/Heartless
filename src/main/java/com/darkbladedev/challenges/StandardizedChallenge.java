package com.darkbladedev.challenges;

import com.darkbladedev.challenges.types.ChallengeDifficulty;
import com.darkbladedev.challenges.types.ChallengeType;

import java.util.*;
import java.util.function.Predicate;

/**
 * Representa un desafío estandarizado con estructura completa
 * y características avanzadas para eventos semanales.
 * 
 * Esta clase encapsula toda la información necesaria para
 * un desafío, incluyendo metadatos, requisitos técnicos,
 * condiciones de completado y configuraciones avanzadas.
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public class StandardizedChallenge {
    
    // === ATRIBUTOS OBLIGATORIOS ===
    private final String id;
    private final String title;
    private final String description;
    private final ChallengeDifficulty difficulty;
    private final List<String> rewards;
    
    // === ATRIBUTOS OPCIONALES ===
    private final ChallengeType type;
    private final Map<String, Object> technicalRequirements;
    private final int requiredProgress;
    private final Map<String, String> metadata;
    private final List<String> prerequisites;
    private final Predicate<UUID> completionCondition;
    private final boolean isRepeatable;
    private final long cooldownTime;
    
    // === METADATOS DE SISTEMA ===
    private final long creationTime;
    private final String version;
    
    /**
     * Constructor completo del desafío estandarizado.
     * 
     * @param id ID único del desafío
     * @param title Título del desafío
     * @param description Descripción detallada
     * @param difficulty Nivel de dificultad
     * @param rewards Lista de recompensas
     * @param type Tipo de desafío
     * @param technicalRequirements Requisitos técnicos
     * @param requiredProgress Progreso requerido
     * @param metadata Metadatos adicionales
     * @param prerequisites Prerequisitos
     * @param completionCondition Condición de completado
     * @param isRepeatable Si es repetible
     * @param cooldownTime Tiempo de cooldown
     */
    public StandardizedChallenge(String id, String title, String description,
                               ChallengeDifficulty difficulty, List<String> rewards,
                               ChallengeType type, Map<String, Object> technicalRequirements,
                               int requiredProgress, Map<String, String> metadata,
                               List<String> prerequisites, Predicate<UUID> completionCondition,
                               boolean isRepeatable, long cooldownTime) {
        
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.rewards = Collections.unmodifiableList(new ArrayList<>(rewards));
        this.type = type;
        this.technicalRequirements = Collections.unmodifiableMap(new HashMap<>(technicalRequirements));
        this.requiredProgress = requiredProgress;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
        this.prerequisites = Collections.unmodifiableList(new ArrayList<>(prerequisites));
        this.completionCondition = completionCondition;
        this.isRepeatable = isRepeatable;
        this.cooldownTime = cooldownTime;
        
        // Metadatos de sistema
        this.creationTime = System.currentTimeMillis();
        this.version = "1.0";
    }
    
    // === GETTERS PARA ATRIBUTOS OBLIGATORIOS ===
    
    /**
     * Obtiene el ID único del desafío.
     * 
     * @return ID del desafío
     */
    public String getId() {
        return id;
    }
    
    /**
     * Obtiene el título del desafío.
     * 
     * @return Título
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Obtiene la descripción del desafío.
     * 
     * @return Descripción
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Obtiene la dificultad del desafío.
     * 
     * @return Nivel de dificultad
     */
    public ChallengeDifficulty getDifficulty() {
        return difficulty;
    }
    
    /**
     * Obtiene las recompensas del desafío.
     * 
     * @return Lista inmutable de recompensas
     */
    public List<String> getRewards() {
        return rewards;
    }
    
    // === GETTERS PARA ATRIBUTOS OPCIONALES ===
    
    /**
     * Obtiene el tipo de desafío.
     * 
     * @return Tipo de desafío o null si no está definido
     */
    public ChallengeType getType() {
        return type;
    }
    
    /**
     * Obtiene los requisitos técnicos.
     * 
     * @return Mapa inmutable de requisitos técnicos
     */
    public Map<String, Object> getTechnicalRequirements() {
        return technicalRequirements;
    }
    
    /**
     * Obtiene el progreso requerido.
     * 
     * @return Progreso requerido para completar
     */
    public int getRequiredProgress() {
        return requiredProgress;
    }
    
    /**
     * Obtiene los metadatos del desafío.
     * 
     * @return Mapa inmutable de metadatos
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /**
     * Obtiene los prerequisitos del desafío.
     * 
     * @return Lista inmutable de IDs de prerequisitos
     */
    public List<String> getPrerequisites() {
        return prerequisites;
    }
    
    /**
     * Obtiene la condición de completado personalizada.
     * 
     * @return Predicado de completado o null si no está definido
     */
    public Predicate<UUID> getCompletionCondition() {
        return completionCondition;
    }
    
    /**
     * Verifica si el desafío es repetible.
     * 
     * @return true si es repetible
     */
    public boolean isRepeatable() {
        return isRepeatable;
    }
    
    /**
     * Obtiene el tiempo de cooldown.
     * 
     * @return Tiempo de cooldown en milisegundos
     */
    public long getCooldownTime() {
        return cooldownTime;
    }
    
    // === GETTERS PARA METADATOS DE SISTEMA ===
    
    /**
     * Obtiene el tiempo de creación del desafío.
     * 
     * @return Timestamp de creación
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * Obtiene la versión del desafío.
     * 
     * @return Versión
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Crea un nuevo builder para construir un StandardizedChallenge.
     * 
     * @return Nueva instancia de ChallengeBuilder
     */
    public static ChallengeBuilder builder() {
        return new ChallengeBuilder();
    }
    
    /**
     * Obtiene un requisito técnico específico.
     * 
     * @param key Clave del requisito
     * @return Valor del requisito o null si no existe
     */
    public Object getTechnicalRequirement(String key) {
        return technicalRequirements.get(key);
    }
    
    /**
     * Obtiene un requisito técnico como tipo específico.
     * 
     * @param key Clave del requisito
     * @param type Clase del tipo esperado
     * @param <T> Tipo de retorno
     * @return Valor tipado o null si no existe o no es del tipo correcto
     */
    @SuppressWarnings("unchecked")
    public <T> T getTechnicalRequirement(String key, Class<T> type) {
        Object value = technicalRequirements.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Verifica si tiene un requisito técnico específico.
     * 
     * @param key Clave del requisito
     * @return true si existe el requisito
     */
    public boolean hasTechnicalRequirement(String key) {
        return technicalRequirements.containsKey(key);
    }
    
    /**
     * Obtiene un metadato específico.
     * 
     * @param key Clave del metadato
     * @return Valor del metadato o null si no existe
     */
    public String getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Verifica si tiene un metadato específico.
     * 
     * @param key Clave del metadato
     * @return true si existe el metadato
     */
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    /**
     * Verifica si tiene prerequisitos.
     * 
     * @return true si tiene prerequisitos
     */
    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty();
    }
    
    /**
     * Verifica si tiene una condición de completado personalizada.
     * 
     * @return true si tiene condición personalizada
     */
    public boolean hasCustomCompletionCondition() {
        return completionCondition != null;
    }
    
    /**
     * Calcula las recompensas ajustadas por dificultad.
     * 
     * @return Lista de recompensas con multiplicador aplicado
     */
    public List<String> getAdjustedRewards() {
        if (difficulty == null) {
            return rewards;
        }
        
        List<String> adjustedRewards = new ArrayList<>();
        double multiplier = difficulty.getRewardMultiplier();
        
        for (String reward : rewards) {
            // Si la recompensa contiene números, aplicar multiplicador
            if (reward.matches(".*\\d+.*") && multiplier != 1.0) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
                java.util.regex.Matcher matcher = pattern.matcher(reward);
                StringBuffer sb = new StringBuffer();
                
                while (matcher.find()) {
                    int originalValue = Integer.parseInt(matcher.group(1));
                    int adjustedValue = (int)(originalValue * multiplier);
                    matcher.appendReplacement(sb, String.valueOf(adjustedValue));
                }
                matcher.appendTail(sb);
                
                adjustedRewards.add(sb.toString());
            } else {
                adjustedRewards.add(reward);
            }
        }
        
        return adjustedRewards;
    }
    
    /**
     * Obtiene el título formateado con dificultad.
     * 
     * @return Título con color de dificultad
     */
    public String getFormattedTitle() {
        if (difficulty != null) {
            return difficulty.getColorCode() + title + "§r";
        }
        return title;
    }
    
    /**
     * Obtiene una descripción completa del desafío.
     * 
     * @return Descripción formateada con todos los detalles
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("§6").append(title).append("§r\n");
        sb.append("§7").append(description).append("§r\n\n");
        
        if (difficulty != null) {
            sb.append("§eDificultad: ").append(difficulty.getColoredName()).append("§r\n");
            sb.append("§eProgreso requerido: §f").append(requiredProgress).append("§r\n");
        }
        
        if (type != null) {
            sb.append("§eTipo: §f").append(type.getFormattedName()).append("§r\n");
        }
        
        if (!rewards.isEmpty()) {
            sb.append("§eRecompensas:§r\n");
            for (String reward : getAdjustedRewards()) {
                sb.append("  §a+ ").append(reward).append("§r\n");
            }
        }
        
        if (hasPrerequisites()) {
            sb.append("§ePrerequisitos: §f").append(String.join(", ", prerequisites)).append("§r\n");
        }
        
        if (isRepeatable) {
            sb.append("§e✓ Repetible");
            if (cooldownTime > 0) {
                sb.append(" (Cooldown: ").append(formatTime(cooldownTime)).append(")");
            }
            sb.append("§r\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Formatea un tiempo en milisegundos a formato legible.
     * 
     * @param timeMs Tiempo en milisegundos
     * @return Tiempo formateado
     */
    private String formatTime(long timeMs) {
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
    
    // === MÉTODOS DE COMPARACIÓN Y HASH ===
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        StandardizedChallenge that = (StandardizedChallenge) obj;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "StandardizedChallenge{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", difficulty=" + difficulty +
                ", type=" + type +
                ", requiredProgress=" + requiredProgress +
                ", isRepeatable=" + isRepeatable +
                '}';
    }
}