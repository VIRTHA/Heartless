package com.darkbladedev.models;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Modelo de datos que representa las métricas de rendimiento de una tarea.
 * Almacena información sobre tiempos de ejecución, contadores y estadísticas
 * para el análisis y optimización de tareas del sistema.
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class TaskMetrics {
    private final String taskId;
    private final TaskPriority priority;
    private final AtomicLong executionCount;
    private final AtomicLong totalExecutionTime;
    private volatile long lastExecutionTime;
    private volatile long minExecutionTime;
    private volatile long maxExecutionTime;
    
    /**
     * Constructor para crear métricas de una tarea.
     * 
     * @param taskId ID único de la tarea
     * @param priority Prioridad de la tarea
     */
    public TaskMetrics(String taskId, TaskPriority priority) {
        this.taskId = taskId;
        this.priority = priority;
        this.executionCount = new AtomicLong(0);
        this.totalExecutionTime = new AtomicLong(0);
        this.lastExecutionTime = System.currentTimeMillis();
        this.minExecutionTime = Long.MAX_VALUE;
        this.maxExecutionTime = 0L;
    }
    
    /**
     * Registra una ejecución de la tarea con su tiempo de duración.
     * 
     * @param executionTime Tiempo de ejecución en milisegundos
     */
    public void recordExecution(long executionTime) {
        executionCount.incrementAndGet();
        totalExecutionTime.addAndGet(executionTime);
        lastExecutionTime = System.currentTimeMillis();
        
        // Actualizar min/max
        if (executionTime < minExecutionTime) {
            minExecutionTime = executionTime;
        }
        if (executionTime > maxExecutionTime) {
            maxExecutionTime = executionTime;
        }
    }
    
    /**
     * Calcula el tiempo promedio de ejecución.
     * 
     * @return Tiempo promedio de ejecución en milisegundos
     */
    public double getAverageExecutionTime() {
        long count = executionCount.get();
        return count > 0 ? (double) totalExecutionTime.get() / count : 0.0;
    }
    
    /**
     * Obtiene el ID de la tarea.
     * 
     * @return ID de la tarea
     */
    public String getTaskId() { 
        return taskId; 
    }
    
    /**
     * Obtiene la prioridad de la tarea.
     * 
     * @return Prioridad de la tarea
     */
    public TaskPriority getPriority() { 
        return priority; 
    }
    
    /**
     * Obtiene el número total de ejecuciones.
     * 
     * @return Número de ejecuciones
     */
    public long getExecutionCount() { 
        return executionCount.get(); 
    }
    
    /**
     * Obtiene el timestamp de la última ejecución.
     * 
     * @return Timestamp de la última ejecución
     */
    public long getLastExecutionTime() { 
        return lastExecutionTime; 
    }
    
    /**
     * Obtiene el tiempo mínimo de ejecución registrado.
     * 
     * @return Tiempo mínimo de ejecución en milisegundos
     */
    public long getMinExecutionTime() { 
        return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime; 
    }
    
    /**
     * Obtiene el tiempo máximo de ejecución registrado.
     * 
     * @return Tiempo máximo de ejecución en milisegundos
     */
    public long getMaxExecutionTime() { 
        return maxExecutionTime; 
    }
    
    /**
     * Obtiene el tiempo total acumulado de todas las ejecuciones.
     * 
     * @return Tiempo total en milisegundos
     */
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }
    
    @Override
    public String toString() {
        return "TaskMetrics{" +
                "taskId='" + taskId + '\'' +
                ", priority=" + priority +
                ", executionCount=" + executionCount.get() +
                ", averageTime=" + String.format("%.2f", getAverageExecutionTime()) + "ms" +
                ", minTime=" + getMinExecutionTime() + "ms" +
                ", maxTime=" + maxExecutionTime + "ms" +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TaskMetrics that = (TaskMetrics) obj;
        return taskId.equals(that.taskId);
    }
    
    @Override
    public int hashCode() {
        return taskId.hashCode();
    }
}