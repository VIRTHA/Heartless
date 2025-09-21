package com.darkbladedev.mechanics;

import java.util.ArrayList;
import java.util.List;

/**
 * Definición de un desafío del evento semanal.
 * 
 * Esta clase representa la estructura básica de un desafío que puede ser
 * utilizado tanto por el sistema de eventos semanales como por el nuevo
 * sistema de desafíos estandarizados.
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public class ChallengeDefinition {
    private final String id;
    private final String displayName;
    private final String description;
    private final int requiredProgress;
    private final List<String> rewards;
    
    /**
     * Constructor para crear una nueva definición de desafío.
     * 
     * @param id ID único del desafío
     * @param displayName Nombre para mostrar del desafío
     * @param description Descripción del desafío
     * @param requiredProgress Progreso requerido para completar el desafío
     * @param rewards Lista de recompensas del desafío
     */
    public ChallengeDefinition(String id, String displayName, String description, 
                             int requiredProgress, List<String> rewards) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.requiredProgress = requiredProgress;
        this.rewards = rewards != null ? new ArrayList<>(rewards) : new ArrayList<>();
    }
    
    /**
     * Obtiene el ID del desafío.
     * 
     * @return ID único del desafío
     */
    public String getId() { 
        return id; 
    }
    
    /**
     * Obtiene el nombre para mostrar del desafío.
     * 
     * @return Nombre del desafío
     */
    public String getDisplayName() { 
        return displayName; 
    }
    
    /**
     * Obtiene la descripción del desafío.
     * 
     * @return Descripción del desafío
     */
    public String getDescription() { 
        return description; 
    }
    
    /**
     * Obtiene el progreso requerido para completar el desafío.
     * 
     * @return Progreso requerido
     */
    public int getRequiredProgress() { 
        return requiredProgress; 
    }
    
    /**
     * Obtiene una copia de la lista de recompensas.
     * 
     * @return Lista de recompensas
     */
    public List<String> getRewards() { 
        return new ArrayList<>(rewards); 
    }
    
    @Override
    public String toString() {
        return String.format("ChallengeDefinition{id='%s', displayName='%s', requiredProgress=%d, rewards=%d}", 
                           id, displayName, requiredProgress, rewards.size());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ChallengeDefinition that = (ChallengeDefinition) obj;
        return requiredProgress == that.requiredProgress &&
               id.equals(that.id) &&
               displayName.equals(that.displayName) &&
               description.equals(that.description) &&
               rewards.equals(that.rewards);
    }
    
    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + displayName.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + requiredProgress;
        result = 31 * result + rewards.hashCode();
        return result;
    }
}