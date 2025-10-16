package com.darkbladedev.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Modelo de datos que representa las estadísticas de un evento semanal.
 * Almacena información sobre participantes, muertes, daño recibido,
 * acciones realizadas y métricas de rendimiento del evento.
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class EventStatistics {
    public final String eventName;
    public final String eventType;
    public final LocalDateTime startTime;
    public LocalDateTime endTime;
    
    private final Map<UUID, String> participants = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicInteger> playerDeaths = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicLong> playerDamage = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> actionCounts = new ConcurrentHashMap<>();
    
    /**
     * Constructor para crear estadísticas de evento.
     * 
     * @param eventName Nombre del evento
     * @param eventType Tipo de evento
     */
    public EventStatistics(String eventName, String eventType) {
        this.eventName = eventName;
        this.eventType = eventType;
        this.startTime = LocalDateTime.now();
    }
    
    /**
     * Marca el evento como finalizado.
     */
    public void endEvent() {
        this.endTime = LocalDateTime.now();
    }
    
    /**
     * Añade un participante al evento.
     * 
     * @param playerId UUID del jugador
     * @param playerName Nombre del jugador
     */
    public void addParticipant(UUID playerId, String playerName) {
        participants.put(playerId, playerName);
        playerDeaths.putIfAbsent(playerId, new AtomicInteger(0));
        playerDamage.putIfAbsent(playerId, new AtomicLong(0));
    }
    
    /**
     * Registra una muerte de jugador.
     * 
     * @param playerId UUID del jugador
     * @param cause Causa de la muerte
     */
    public void recordDeath(UUID playerId, String cause) {
        playerDeaths.computeIfAbsent(playerId, k -> new AtomicInteger(0)).incrementAndGet();
        recordAction("death_" + cause);
    }
    
    /**
     * Registra daño recibido por un jugador.
     * 
     * @param playerId UUID del jugador
     * @param damage Cantidad de daño
     */
    public void recordDamage(UUID playerId, double damage) {
        playerDamage.computeIfAbsent(playerId, k -> new AtomicLong(0))
                .addAndGet((long) (damage * 100)); // Almacenar como centésimas
    }
    
    /**
     * Registra una acción realizada en el evento.
     * 
     * @param actionType Tipo de acción
     * @param details Detalles adicionales de la acción
     */
    public void recordAction(String actionType, Object... details) {
        actionCounts.computeIfAbsent(actionType, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * Obtiene el nombre del evento.
     * 
     * @return Nombre del evento
     */
    public String getEventName() { 
        return eventName; 
    }
    
    /**
     * Obtiene el tipo de evento.
     * 
     * @return Tipo de evento
     */
    public String getEventType() { 
        return eventType; 
    }
    
    /**
     * Obtiene el mapa de participantes.
     * 
     * @return Mapa de participantes (UUID -> Nombre)
     */
    public Map<UUID, String> getParticipants() { 
        return new HashMap<>(participants); 
    }
    
    /**
     * Obtiene la duración del evento.
     * 
     * @return Duración del evento
     */
    public Duration getDuration() {
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end);
    }
    
    /**
     * Obtiene el total de muertes en el evento.
     * 
     * @return Total de muertes
     */
    public int getTotalDeaths() {
        return playerDeaths.values().stream().mapToInt(AtomicInteger::get).sum();
    }
    
    /**
     * Obtiene el total de daño recibido en el evento.
     * 
     * @return Total de daño
     */
    public double getTotalDamage() {
        return playerDamage.values().stream().mapToLong(AtomicLong::get).sum() / 100.0;
    }
    
    /**
     * Obtiene el total de acciones realizadas.
     * 
     * @return Total de acciones
     */
    public int getTotalActions() {
        return actionCounts.values().stream().mapToInt(AtomicInteger::get).sum();
    }
    
    /**
     * Obtiene los jugadores que más daño han recibido.
     * 
     * @param limit Límite de resultados
     * @return Lista de jugadores ordenados por daño recibido
     */
    public List<Map.Entry<UUID, Double>> getTopDamageReceivers(int limit) {
        return playerDamage.entrySet().stream()
                .sorted(Map.Entry.<UUID, AtomicLong>comparingByValue(
                        (a, b) -> Long.compare(b.get(), a.get()))) // Mayor daño primero
                .limit(limit)
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().get() / 100.0))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene el tiempo de inicio del evento.
     * 
     * @return Tiempo de inicio
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * Obtiene el tiempo de finalización del evento.
     * 
     * @return Tiempo de finalización (null si no ha terminado)
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    /**
     * Verifica si el evento ha terminado.
     * 
     * @return true si ha terminado, false en caso contrario
     */
    public boolean isEnded() {
        return endTime != null;
    }
    
    /**
     * Obtiene el número de participantes.
     * 
     * @return Número de participantes
     */
    public int getParticipantCount() {
        return participants.size();
    }
    
    /**
     * Obtiene las muertes de un jugador específico.
     * 
     * @param playerId UUID del jugador
     * @return Número de muertes del jugador
     */
    public int getPlayerDeaths(UUID playerId) {
        return playerDeaths.getOrDefault(playerId, new AtomicInteger(0)).get();
    }
    
    /**
     * Obtiene el daño recibido por un jugador específico.
     * 
     * @param playerId UUID del jugador
     * @return Daño recibido por el jugador
     */
    public double getPlayerDamage(UUID playerId) {
        return playerDamage.getOrDefault(playerId, new AtomicLong(0)).get() / 100.0;
    }
    
    @Override
    public String toString() {
        return "EventStatistics{" +
                "eventName='" + eventName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", participants=" + participants.size() +
                ", totalDeaths=" + getTotalDeaths() +
                ", totalDamage=" + String.format("%.2f", getTotalDamage()) +
                ", duration=" + getDuration().toMinutes() + "min" +
                ", ended=" + isEnded() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        EventStatistics that = (EventStatistics) obj;
        return Objects.equals(eventName, that.eventName) &&
                Objects.equals(eventType, that.eventType) &&
                Objects.equals(startTime, that.startTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventName, eventType, startTime);
    }
}