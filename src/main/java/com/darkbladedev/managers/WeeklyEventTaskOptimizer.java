package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.darkbladedev.models.TaskMetrics;
import com.darkbladedev.models.TaskPriority;

/**
 * Optimizador de tareas para eventos semanales.
 * 
 * Este sistema proporciona:
 * - Pool de hilos optimizado para tareas de eventos
 * - Gestión inteligente de recursos y memoria
 * - Monitoreo de rendimiento en tiempo real
 * - Balanceador de carga automático
 * - Sistema de prioridades para tareas críticas
 * - Prevención de memory leaks y deadlocks
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class WeeklyEventTaskOptimizer {
    
    // Configuración del pool de hilos
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 8;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 100;
    
    // Configuración de monitoreo
    private static final long MONITORING_INTERVAL = 60000L; // 60 segundos
    private static final long CLEANUP_INTERVAL = 300000L;   // 5 minutos
    @SuppressWarnings("unused")
    private static final double CPU_THRESHOLD = 0.8;       // 80% CPU
    private static final long MEMORY_THRESHOLD = 700 * 1024 * 1024; // 700MB
    
    private final HeartlessMain plugin;
    private final Logger logger;
    
    // Pool de hilos personalizado
    private final ThreadPoolExecutor taskExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    
    // Gestión de tareas Bukkit
    private final Map<String, BukkitTask> bukkitTasks;
    private final Map<String, TaskMetrics> taskMetrics;
    
    // Monitoreo y estadísticas
    private final AtomicLong totalTasksExecuted;
    private final AtomicLong totalExecutionTime;
    private final AtomicInteger activeTasks;
    private final AtomicInteger failedTasks;
    
    // Tareas de sistema
    private BukkitTask monitoringTask;
    private BukkitTask cleanupTask;
    
    // Estado del optimizador
    private volatile boolean isRunning;
    private volatile boolean isOptimizationEnabled;
    
    public WeeklyEventTaskOptimizer(HeartlessMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        
        // Inicializar pool de hilos
        this.taskExecutor = createOptimizedThreadPool();
        this.scheduledExecutor = Executors.newScheduledThreadPool(2, 
            r -> {
                Thread t = new Thread(r, "WeeklyEvent-Scheduler");
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            });
        
        // Inicializar colecciones thread-safe
        this.bukkitTasks = new ConcurrentHashMap<>();
        this.taskMetrics = new ConcurrentHashMap<>();
        
        // Inicializar contadores atómicos
        this.totalTasksExecuted = new AtomicLong(0);
        this.totalExecutionTime = new AtomicLong(0);
        this.activeTasks = new AtomicInteger(0);
        this.failedTasks = new AtomicInteger(0);
        
        // Estado inicial
        this.isRunning = false;
        this.isOptimizationEnabled = true;
        
        logger.info("WeeklyEventTaskOptimizer inicializado con pool de " + 
                   CORE_POOL_SIZE + "-" + MAX_POOL_SIZE + " hilos.");
    }
    
    /**
     * Crea un pool de hilos optimizado para eventos semanales.
     */
    private ThreadPoolExecutor createOptimizedThreadPool() {
        // Cola de prioridad personalizada
        BlockingQueue<Runnable> workQueue = new PriorityBlockingQueue<>(QUEUE_CAPACITY, 
            (r1, r2) -> {
                if (r1 instanceof PriorityTask && r2 instanceof PriorityTask) {
                    return Integer.compare(((PriorityTask) r2).getPriority(), 
                                         ((PriorityTask) r1).getPriority());
                }
                return 0;
            });
        
        // Factory de hilos personalizada
        ThreadFactory threadFactory = r -> {
            Thread t = new Thread(r, "WeeklyEvent-Worker-" + 
                                 Thread.currentThread().threadId());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY);
            t.setUncaughtExceptionHandler((thread, ex) -> 
                logger.log(Level.SEVERE, "Error no capturado en hilo de evento: " + 
                          thread.getName(), ex));
            return t;
        };
        
        // Crear pool con configuración optimizada
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            workQueue,
            threadFactory,
            new ThreadPoolExecutor.CallerRunsPolicy() // Política de rechazo
        );
        
        // Permitir que los hilos core expiren
        executor.allowCoreThreadTimeOut(true);
        
        return executor;
    }
    
    /**
     * Inicia el optimizador de tareas.
     */
    public void start() {
        if (isRunning) {
            logger.warning("TaskOptimizer ya está ejecutándose.");
            return;
        }
        
        isRunning = true;
        
        // Iniciar tarea de monitoreo
        startMonitoringTask();
        
        // Iniciar tarea de limpieza
        startCleanupTask();
        
        logger.info("WeeklyEventTaskOptimizer iniciado exitosamente.");
    }
    
    /**
     * Recarga la configuración del optimizador sin afectar las tareas en ejecución.
     * 
     * Este método:
     * - Actualiza los parámetros de configuración
     * - Ajusta los pools de hilos si es necesario
     * - Reinicia el monitoreo con nuevos valores
     * - Mantiene las tareas activas funcionando
     */
    public void reload() {
        logger.info("[WeeklyEventTaskOptimizer] Iniciando recarga del sistema...");
        
        try {
            // Actualizar configuración de monitoreo
            if (isRunning) {
                // Cancelar tareas de monitoreo actuales
                if (monitoringTask != null) {
                    monitoringTask.cancel();
                }
                
                if (cleanupTask != null) {
                    cleanupTask.cancel();
                }
                
                // Reiniciar tareas de monitoreo con configuración actualizada
                startMonitoringTask();
                startCleanupTask();
                
                // Limpiar métricas para empezar fresh
                totalTasksExecuted.set(0);
                totalExecutionTime.set(0);
                
                logger.info("[WeeklyEventTaskOptimizer] Sistema recargado exitosamente. Tareas activas: " + 
                           taskExecutor.getActiveCount());
            } else {
                logger.info("[WeeklyEventTaskOptimizer] Sistema no estaba en ejecución, recarga omitida.");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[WeeklyEventTaskOptimizer] Error durante la recarga del sistema", e);
            throw new RuntimeException("Error al recargar WeeklyEventTaskOptimizer", e);
        }
    }
    
    /**
     * Método llamado cuando la configuración se recarga
     */
    public void onConfigReload() {
        logger.info("WeeklyEventTaskOptimizer: Aplicando cambios de configuración...");
        
        // Obtener nuevos valores de configuración
        ConfigManager configManager = HeartlessMain.getInstance().getConfigManager();
        
        // Aplicar configuraciones específicas del optimizador
        if (configManager.isOptimizationTaskOptimizerEnabled()) {
            // Si la optimización está habilitada, recargar el sistema
            if (!isRunning) {
                start();
            } else {
                reload();
            }
            setOptimizationEnabled(true);
        } else {
            // Si está deshabilitada, detener optimización pero mantener funcionalidad básica
            setOptimizationEnabled(false);
        }
        
        logger.info("WeeklyEventTaskOptimizer: Configuración actualizada exitosamente");
    }
    
    /**
     * Detiene el optimizador de tareas.
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        // Cancelar tareas de sistema
        if (monitoringTask != null) {
            monitoringTask.cancel();
        }
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        
        // Cancelar todas las tareas Bukkit
        bukkitTasks.values().forEach(BukkitTask::cancel);
        bukkitTasks.clear();
        
        // Shutdown de pools de hilos
        shutdownExecutor(taskExecutor, "TaskExecutor");
        shutdownExecutor(scheduledExecutor, "ScheduledExecutor");
        
        logger.info("WeeklyEventTaskOptimizer detenido exitosamente.");
    }
    
    /**
     * Programa una tarea Bukkit optimizada.
     */
    public BukkitTask scheduleTask(String taskId, Runnable task, long delay, long period, TaskPriority priority) {
        if (!isRunning) {
            throw new IllegalStateException("TaskOptimizer no está ejecutándose.");
        }
        
        // Cancelar tarea existente si existe
        cancelTask(taskId);
        
        // Crear tarea optimizada
        OptimizedBukkitTask optimizedTask = new OptimizedBukkitTask(taskId, task, priority);
        
        // Programar tarea
        BukkitTask bukkitTask;
        if (period > 0) {
            bukkitTask = optimizedTask.runTaskTimer(plugin, delay, period);
        } else {
            bukkitTask = optimizedTask.runTaskLater(plugin, delay);
        }
        
        // Registrar tarea
        bukkitTasks.put(taskId, bukkitTask);
        taskMetrics.put(taskId, new TaskMetrics(taskId, priority));
        
        logger.fine("Tarea programada: " + taskId + " (prioridad: " + priority + ")");
        return bukkitTask;
    }
    
    /**
     * Programa una tarea asíncrona optimizada.
     */
    public CompletableFuture<Void> scheduleAsyncTask(String taskId, Runnable task, TaskPriority priority) {
        if (!isRunning) {
            throw new IllegalStateException("TaskOptimizer no está ejecutándose.");
        }
        
        // Crear tarea con prioridad
        PriorityTask priorityTask = new PriorityTask(taskId, task, priority);
        
        // Ejecutar de forma asíncrona
        return CompletableFuture.runAsync(priorityTask, taskExecutor)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.log(Level.WARNING, "Error en tarea asíncrona: " + taskId, throwable);
                    failedTasks.incrementAndGet();
                }
            });
    }
    
    /**
     * Cancela una tarea específica.
     */
    public boolean cancelTask(String taskId) {
        BukkitTask task = bukkitTasks.remove(taskId);
        if (task != null) {
            task.cancel();
            taskMetrics.remove(taskId);
            logger.fine("Tarea cancelada: " + taskId);
            return true;
        }
        return false;
    }
    
    /**
     * Cancela todas las tareas de un evento específico.
     */
    public void cancelEventTasks(String eventName) {
        String prefix = eventName + "_";
        
        bukkitTasks.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(prefix)) {
                entry.getValue().cancel();
                taskMetrics.remove(entry.getKey());
                logger.fine("Tarea de evento cancelada: " + entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Inicia la tarea de monitoreo del sistema.
     */
    private void startMonitoringTask() {
        monitoringTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    performSystemMonitoring();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error en monitoreo del sistema", e);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, MONITORING_INTERVAL / 50L); // Convertir a ticks
    }
    
    /**
     * Inicia la tarea de limpieza del sistema.
     */
    private void startCleanupTask() {
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    performSystemCleanup();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error en limpieza del sistema", e);
                }
            }
        }.runTaskTimerAsynchronously(plugin, CLEANUP_INTERVAL / 50L, CLEANUP_INTERVAL / 50L);
    }
    
    /**
     * Realiza monitoreo del sistema y optimizaciones automáticas.
     */
    private void performSystemMonitoring() {
        if (!isOptimizationEnabled) {
            return;
        }
        
        // Obtener métricas del sistema
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        // Obtener métricas del pool de hilos
        int activeThreads = taskExecutor.getActiveCount();
        int poolSize = taskExecutor.getPoolSize();
        long completedTasks = taskExecutor.getCompletedTaskCount();
        int queueSize = taskExecutor.getQueue().size();
        
        // Verificar umbrales críticos
        if (usedMemory > MEMORY_THRESHOLD) {
            logger.warning("Uso de memoria alto detectado: " + (usedMemory / 1024 / 1024) + "MB");
            performEmergencyCleanup();
        }
        
        if (queueSize > QUEUE_CAPACITY * 0.8) {
            logger.warning("Cola de tareas saturada: " + queueSize + "/" + QUEUE_CAPACITY);
            optimizeTaskExecution();
        }
        
        // Log de estadísticas (cada 5 minutos)
        if (completedTasks % 100 == 0) {
            logSystemStatistics(usedMemory, activeThreads, poolSize, queueSize);
        }
    }
    
    /**
     * Realiza limpieza del sistema.
     */
    private void performSystemCleanup() {
        // Limpiar métricas antiguas
        long currentTime = System.currentTimeMillis();
        taskMetrics.entrySet().removeIf(entry -> {
            TaskMetrics metrics = entry.getValue();
            return currentTime - metrics.getLastExecutionTime() > 600000L; // 10 minutos
        });
        
        // Limpiar tareas completadas
        bukkitTasks.entrySet().removeIf(entry -> entry.getValue().isCancelled());
        
        // Sugerir garbage collection si es necesario
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        if (usedMemory > MEMORY_THRESHOLD) {
            System.gc();
            logger.fine("Garbage collection sugerido debido a alto uso de memoria.");
        }
        
        logger.fine("Limpieza del sistema completada. Tareas activas: " + bukkitTasks.size());
    }
    
    /**
     * Realiza limpieza de emergencia.
     */
    private void performEmergencyCleanup() {
        logger.warning("Iniciando limpieza de emergencia...");
        
        // Cancelar tareas de baja prioridad
        bukkitTasks.entrySet().removeIf(entry -> {
            String taskId = entry.getKey();
            TaskMetrics metrics = taskMetrics.get(taskId);
            
            if (metrics != null && metrics.getPriority() == TaskPriority.LOW) {
                entry.getValue().cancel();
                taskMetrics.remove(taskId);
                logger.fine("Tarea de baja prioridad cancelada: " + taskId);
                return true;
            }
            return false;
        });
        
        // Forzar garbage collection
        System.gc();
        
        logger.warning("Limpieza de emergencia completada.");
    }
    
    /**
     * Optimiza la ejecución de tareas.
     */
    private void optimizeTaskExecution() {
        logger.info("Optimizando ejecución de tareas...");
        
        // Aumentar temporalmente el tamaño del pool si es posible
        if (taskExecutor.getPoolSize() < MAX_POOL_SIZE) {
            taskExecutor.setCorePoolSize(Math.min(CORE_POOL_SIZE + 2, MAX_POOL_SIZE));
            logger.fine("Tamaño del pool aumentado temporalmente.");
        }
        
        // Programar reducción del pool después de un tiempo
        scheduledExecutor.schedule(() -> {
            taskExecutor.setCorePoolSize(CORE_POOL_SIZE);
            logger.fine("Tamaño del pool restaurado.");
        }, 5, TimeUnit.MINUTES);
    }
    
    /**
     * Registra estadísticas del sistema.
     */
    private void logSystemStatistics(long usedMemory, int activeThreads, int poolSize, int queueSize) {
        logger.info(String.format(
            "Estadísticas del TaskOptimizer - Memoria: %dMB, Hilos activos: %d/%d, Cola: %d, " +
            "Tareas ejecutadas: %d, Tareas fallidas: %d, Tiempo promedio: %.2fms",
            usedMemory / 1024 / 1024,
            activeThreads,
            poolSize,
            queueSize,
            totalTasksExecuted.get(),
            failedTasks.get(),
            getAverageExecutionTime()
        ));
    }
    
    /**
     * Calcula el tiempo promedio de ejecución.
     */
    private double getAverageExecutionTime() {
        long totalTasks = totalTasksExecuted.get();
        if (totalTasks == 0) {
            return 0.0;
        }
        return (double) totalExecutionTime.get() / totalTasks;
    }
    
    /**
     * Cierra un executor de forma segura.
     */
    private void shutdownExecutor(ExecutorService executor, String name) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warning(name + " no se cerró en 10 segundos, forzando cierre...");
                executor.shutdownNow();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.severe(name + " no se pudo cerrar completamente.");
                }
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupción durante cierre de " + name, e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // === CLASES INTERNAS ===
    
    /**
     * Tarea Bukkit optimizada con métricas.
     */
    private class OptimizedBukkitTask extends BukkitRunnable {
        private final String taskId;
        private final Runnable task;
        @SuppressWarnings("unused")
        private final TaskPriority priority;
        
        public OptimizedBukkitTask(String taskId, Runnable task, TaskPriority priority) {
            this.taskId = taskId;
            this.task = task;
            this.priority = priority;
        }
        
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            activeTasks.incrementAndGet();
            
            try {
                task.run();
                
                // Actualizar métricas
                long executionTime = System.currentTimeMillis() - startTime;
                totalTasksExecuted.incrementAndGet();
                totalExecutionTime.addAndGet(executionTime);
                
                TaskMetrics metrics = taskMetrics.get(taskId);
                if (metrics != null) {
                    metrics.recordExecution(executionTime);
                }
                
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error en tarea optimizada: " + taskId, e);
                failedTasks.incrementAndGet();
            } finally {
                activeTasks.decrementAndGet();
            }
        }
    }
    
    /**
     * Tarea con prioridad para el pool de hilos.
     */
    private static class PriorityTask implements Runnable {
        private final String taskId;
        private final Runnable task;
        private final TaskPriority priority;
        
        public PriorityTask(String taskId, Runnable task, TaskPriority priority) {
            this.taskId = taskId;
            this.task = task;
            this.priority = priority;
        }
        
        @Override
        public void run() {
            task.run();
        }
        
        public int getPriority() {
            return priority.getValue();
        }
        
        @SuppressWarnings("unused")
        public String getTaskId() {
            return taskId;
        }
    }
    
    // === MÉTODOS PÚBLICOS DE INFORMACIÓN ===
    
    /**
     * Obtiene estadísticas actuales del optimizador.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("isRunning", isRunning);
        stats.put("isOptimizationEnabled", isOptimizationEnabled);
        stats.put("activeTasks", activeTasks.get());
        stats.put("totalTasksExecuted", totalTasksExecuted.get());
        stats.put("failedTasks", failedTasks.get());
        stats.put("averageExecutionTime", getAverageExecutionTime());
        stats.put("activeThreads", taskExecutor.getActiveCount());
        stats.put("poolSize", taskExecutor.getPoolSize());
        stats.put("queueSize", taskExecutor.getQueue().size());
        stats.put("completedTasks", taskExecutor.getCompletedTaskCount());
        
        return stats;
    }
    
    /**
     * Obtiene métricas de una tarea específica.
     */
    public TaskMetrics getTaskMetrics(String taskId) {
        return taskMetrics.get(taskId);
    }
    
    /**
     * Obtiene lista de tareas activas.
     */
    public Set<String> getActiveTasks() {
        return new HashSet<>(bukkitTasks.keySet());
    }
    
    /**
     * Habilita o deshabilita la optimización automática.
     */
    public void setOptimizationEnabled(boolean enabled) {
        this.isOptimizationEnabled = enabled;
        logger.info("Optimización automática " + (enabled ? "habilitada" : "deshabilitada"));
    }
    
    /**
     * Verifica si el optimizador está ejecutándose.
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Verifica si la optimización está habilitada.
     */
    public boolean isOptimizationEnabled() {
        return isOptimizationEnabled;
    }
}