package com.darkbladedev.utils;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WeeklyEventData {
    public boolean isPaused;
    public boolean isActive;
    public long pauseStartTime;
    public long totalPausedTime;
    public long startTime;
    public long endTime;
    public String eventType;
    public String eventName;
    private Set<UUID> activePlayers;
    private Map<String, Object> specificData;
    
    public WeeklyEventData() {
        this.activePlayers = ConcurrentHashMap.newKeySet();
        this.specificData = new ConcurrentHashMap<>();
    }
    
    // Getters y Setters
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    public Set<UUID> getActivePlayers() {
        return activePlayers;
    }
    
    public void setActivePlayers(Set<UUID> activePlayers) {
        this.activePlayers = activePlayers != null ? activePlayers : ConcurrentHashMap.newKeySet();
    }
    
    public Map<String, Object> getSpecificData() {
        return specificData;
    }
    
    public void setSpecificData(Map<String, Object> specificData) {
        this.specificData = specificData != null ? specificData : new ConcurrentHashMap<>();
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }
    
    public long getPauseStartTime() {
        return pauseStartTime;
    }
    
    public void setPauseStartTime(long pauseStartTime) {
        this.pauseStartTime = pauseStartTime;
    }
    
    public long getTotalPausedTime() {
        return totalPausedTime;
    }
    
    public void setTotalPausedTime(long totalPausedTime) {
        this.totalPausedTime = totalPausedTime;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
