package com.darkbladedev.challenges;

import java.util.List;

/**
 * Definición básica de un desafío compatible con el sistema existente.
 * 
 * Esta clase proporciona una estructura simple para desafíos que puede
 * ser utilizada tanto por el sistema nuevo como por el sistema existente
 * de AbstractWeeklyEvent.
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public class ChallengeDefinition {
    
    private final String id;
    private final String title;
    private final String description;
    private final int requiredProgress;
    private final List<String> rewards;
    
    /**
     * Constructor para crear una definición de desafío.
     * 
     * @param id Identificador único del desafío
     * @param title Título del desafío
     * @param description Descripción del desafío
     * @param requiredProgress Progreso requerido para completar
     * @param rewards Lista de recompensas
     */
    public ChallengeDefinition(String id, String title, String description, 
                              int requiredProgress, List<String> rewards) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.requiredProgress = requiredProgress;
        this.rewards = rewards;
    }
    
    /**
     * Obtiene el ID del desafío.
     * 
     * @return ID único
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
     * Obtiene el progreso requerido.
     * 
     * @return Progreso requerido
     */
    public int getRequiredProgress() {
        return requiredProgress;
    }
    
    /**
     * Obtiene las recompensas del desafío.
     * 
     * @return Lista de recompensas
     */
    public List<String> getRewards() {
        return rewards;
    }
    
    @Override
    public String toString() {
        return "ChallengeDefinition{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", requiredProgress=" + requiredProgress +
                ", rewards=" + rewards +
                '}';
    }
}