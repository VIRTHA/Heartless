package com.darkbladedev.models;

import java.util.*;

/**
 * Modelo de datos que actúa como contenedor para la persistencia de datos de eventos.
 * Almacena toda la información relacionada con un evento semanal, incluyendo
 * progreso de desafíos, estadísticas de jugadores y datos globales.
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class EventDataContainer {
    
    public String eventId;
    public long timestamp;
    public String version;
    
    public Map<UUID, Map<String, Object>> challengeProgress;
    public Map<UUID, Set<String>> completedChallenges;
    public Map<UUID, Map<String, Object>> playerStatistics;
    public Map<String, Long> globalStatistics;
    
    public long totalParticipants;
    public long totalChallengesCompleted;
    
    /**
     * Constructor por defecto para serialización.
     */
    public EventDataContainer() {
        this.challengeProgress = new HashMap<>();
        this.completedChallenges = new HashMap<>();
        this.playerStatistics = new HashMap<>();
        this.globalStatistics = new HashMap<>();
    }
    
    /**
     * Constructor completo para crear un contenedor de datos de evento.
     * 
     * @param eventId ID del evento
     * @param timestamp Timestamp del evento
     * @param version Versión del formato de datos
     * @param challengeProgress Progreso de desafíos por jugador
     * @param completedChallenges Desafíos completados por jugador
     * @param playerStatistics Estadísticas por jugador
     * @param globalStatistics Estadísticas globales
     * @param totalParticipants Total de participantes
     * @param totalChallengesCompleted Total de desafíos completados
     */
    public EventDataContainer(String eventId, long timestamp, String version,
                            Map<UUID, Map<String, Object>> challengeProgress,
                            Map<UUID, Set<String>> completedChallenges,
                            Map<UUID, Map<String, Object>> playerStatistics,
                            Map<String, Long> globalStatistics,
                            long totalParticipants, long totalChallengesCompleted) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.version = version;
        this.challengeProgress = new HashMap<>(challengeProgress);
        this.completedChallenges = new HashMap<>(completedChallenges);
        this.playerStatistics = new HashMap<>(playerStatistics);
        this.globalStatistics = new HashMap<>(globalStatistics);
        this.totalParticipants = totalParticipants;
        this.totalChallengesCompleted = totalChallengesCompleted;
    }
    
    // Getters
    public String getEventId() { return eventId; }
    public long getTimestamp() { return timestamp; }
    public String getVersion() { return version; }
    public Map<UUID, Map<String, Object>> getChallengeProgress() { return challengeProgress; }
    public Map<UUID, Set<String>> getCompletedChallenges() { return completedChallenges; }
    public Map<UUID, Map<String, Object>> getPlayerStatistics() { return playerStatistics; }
    public Map<String, Long> getGlobalStatistics() { return globalStatistics; }
    public long getTotalParticipants() { return totalParticipants; }
    public long getTotalChallengesCompleted() { return totalChallengesCompleted; }
    
    // Setters
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setVersion(String version) { this.version = version; }
    public void setChallengeProgress(Map<UUID, Map<String, Object>> challengeProgress) { 
        this.challengeProgress = challengeProgress; 
    }
    public void setCompletedChallenges(Map<UUID, Set<String>> completedChallenges) { 
        this.completedChallenges = completedChallenges; 
    }
    public void setPlayerStatistics(Map<UUID, Map<String, Object>> playerStatistics) { 
        this.playerStatistics = playerStatistics; 
    }
    public void setGlobalStatistics(Map<String, Long> globalStatistics) { 
        this.globalStatistics = globalStatistics; 
    }
    public void setTotalParticipants(long totalParticipants) { 
        this.totalParticipants = totalParticipants; 
    }
    public void setTotalChallengesCompleted(long totalChallengesCompleted) { 
        this.totalChallengesCompleted = totalChallengesCompleted; 
    }
    
    @Override
    public String toString() {
        return "EventDataContainer{" +
                "eventId='" + eventId + '\'' +
                ", timestamp=" + timestamp +
                ", version='" + version + '\'' +
                ", totalParticipants=" + totalParticipants +
                ", totalChallengesCompleted=" + totalChallengesCompleted +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        EventDataContainer that = (EventDataContainer) obj;
        return timestamp == that.timestamp &&
                totalParticipants == that.totalParticipants &&
                totalChallengesCompleted == that.totalChallengesCompleted &&
                Objects.equals(eventId, that.eventId) &&
                Objects.equals(version, that.version);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId, timestamp, version, totalParticipants, totalChallengesCompleted);
    }
}