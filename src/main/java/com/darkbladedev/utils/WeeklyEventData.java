package com.darkbladedev.utils;

public class WeeklyEventData {
    public boolean isPaused;
    public boolean isActive;
    public long pauseStartTime;
    public long totalPausedTime;
    public long startTime;
    public long endTime;
    public String eventType;

    public WeeklyEventData() {} // Constructor vacío requerido por Gson
}
