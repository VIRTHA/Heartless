package com.darkbladedev.models;

import java.util.*;

import com.darkbladedev.mechanics.AbstractWeeklyEvent;

/**
 * Modelo de datos que representa un reporte unificado de eventos semanales.
 * Contiene información completa sobre el progreso de un jugador en un evento específico,
 * incluyendo estadísticas, desafíos completados y progreso actual.
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class UnifiedEventReport {
    private final UUID playerId;
    private final String eventId;
    private final Map<String, Object> playerStatistics;
    private final Set<String> completedChallenges;
    private final Map<String, Object> challengeProgress;
    private final Map<String, AbstractWeeklyEvent.ChallengeDefinition> availableChallenges;
    private final long generatedAt;
    
    /**
     * Constructor para crear un reporte unificado de evento.
     * 
     * @param playerId UUID del jugador
     * @param eventId ID del evento
     * @param playerStatistics Estadísticas del jugador
     * @param completedChallenges Desafíos completados
     * @param challengeProgress Progreso de desafíos
     * @param availableChallenges Desafíos disponibles
     * @param generatedAt Timestamp de generación
     */
    public UnifiedEventReport(UUID playerId, String eventId, Map<String, Object> playerStatistics,
                            Set<String> completedChallenges, Map<String, Object> challengeProgress,
                            Map<String, AbstractWeeklyEvent.ChallengeDefinition> availableChallenges,
                            long generatedAt) {
        this.playerId = playerId;
        this.eventId = eventId;
        this.playerStatistics = new HashMap<>(playerStatistics);
        this.completedChallenges = new HashSet<>(completedChallenges);
        this.challengeProgress = new HashMap<>(challengeProgress);
        this.availableChallenges = new HashMap<>(availableChallenges);
        this.generatedAt = generatedAt;
    }
    
    /**
     * Obtiene el UUID del jugador.
     * 
     * @return UUID del jugador
     */
    public UUID getPlayerId() { 
        return playerId; 
    }
    
    /**
     * Obtiene el ID del evento.
     * 
     * @return ID del evento
     */
    public String getEventId() { 
        return eventId; 
    }
    
    /**
     * Obtiene las estadísticas del jugador.
     * 
     * @return Mapa con las estadísticas del jugador
     */
    public Map<String, Object> getPlayerStatistics() { 
        return new HashMap<>(playerStatistics); 
    }
    
    /**
     * Obtiene los desafíos completados.
     * 
     * @return Set con los IDs de desafíos completados
     */
    public Set<String> getCompletedChallenges() { 
        return new HashSet<>(completedChallenges); 
    }
    
    /**
     * Obtiene el progreso de los desafíos.
     * 
     * @return Mapa con el progreso de desafíos
     */
    public Map<String, Object> getChallengeProgress() { 
        return new HashMap<>(challengeProgress); 
    }
    
    /**
     * Obtiene los desafíos disponibles.
     * 
     * @return Mapa con las definiciones de desafíos disponibles
     */
    public Map<String, AbstractWeeklyEvent.ChallengeDefinition> getAvailableChallenges() { 
        return new HashMap<>(availableChallenges); 
    }
    
    /**
     * Obtiene el timestamp de generación del reporte.
     * 
     * @return Timestamp de generación
     */
    public long getGeneratedAt() { 
        return generatedAt; 
    }
    
    @Override
    public String toString() {
        return "UnifiedEventReport{" +
                "playerId=" + playerId +
                ", eventId='" + eventId + '\'' +
                ", completedChallenges=" + completedChallenges.size() +
                ", generatedAt=" + generatedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UnifiedEventReport that = (UnifiedEventReport) obj;
        return generatedAt == that.generatedAt &&
                Objects.equals(playerId, that.playerId) &&
                Objects.equals(eventId, that.eventId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(playerId, eventId, generatedAt);
    }
}