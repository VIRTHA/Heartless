package com.darkbladedev.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Optimizador de rendimiento para el sistema de estadísticas de eventos semanales.
 * Proporciona herramientas para manejar eficientemente grandes volúmenes de jugadores
 * y operaciones de estadísticas sin afectar el rendimiento del servidor.
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 1.0
 */
public class PerformanceOptimizer {
    
    private static final Logger logger = Logger.getLogger(PerformanceOptimizer.class.getName());
    
    // Configuración de rendimiento
    private static final int DEFAULT_BATCH_SIZE = 50; // Jugadores por lote
    private static final long DEFAULT_BATCH_DELAY = 50L; // Delay entre lotes en ticks (2.5 segundos)
    private static final int MAX_CONCURRENT_OPERATIONS = 4; // Máximo de operaciones concurrentes
    private static final long MEMORY_CHECK_INTERVAL = 200L; // Verificar memoria cada 10 segundos
    
    // Pool de hilos para operaciones asíncronas
    private final ExecutorService asyncExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    
    // Métricas de rendimiento
    private final AtomicLong totalOperations = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicInteger activeOperations = new AtomicInteger(0);
    
    // Control de memoria
    private final Runtime runtime = Runtime.getRuntime();
    private volatile boolean memoryPressure = false;
    
    private final Plugin plugin;
    
    public PerformanceOptimizer(Plugin plugin) {
        this.plugin = plugin;
        
        // Configurar pool de hilos optimizado
        this.asyncExecutor = Executors.newFixedThreadPool(
            Math.min(MAX_CONCURRENT_OPERATIONS, Runtime.getRuntime().availableProcessors()),
            r -> {
                Thread t = new Thread(r, "HeartlessStats-Worker");
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY - 1); // Prioridad ligeramente menor
                return t;
            }
        );
        
        this.scheduledExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "HeartlessStats-Scheduler");
            t.setDaemon(true);
            return t;
        });
        
        // Iniciar monitoreo de memoria
        startMemoryMonitoring();
    }
    
    /**
     * Procesa una lista de jugadores en lotes para evitar sobrecarga del servidor.
     * 
     * @param players Lista de jugadores a procesar
     * @param processor Función que procesa cada jugador
     * @param onComplete Callback ejecutado al completar todos los lotes
     * @param <T> Tipo de resultado del procesamiento
     */
    public <T> void processBatched(List<UUID> players, Function<UUID, T> processor, 
                                   Consumer<List<T>> onComplete) {
        processBatched(players, processor, onComplete, DEFAULT_BATCH_SIZE, DEFAULT_BATCH_DELAY);
    }
    
    /**
     * Procesa una lista de jugadores en lotes con configuración personalizada.
     * 
     * @param players Lista de jugadores a procesar
     * @param processor Función que procesa cada jugador
     * @param onComplete Callback ejecutado al completar todos los lotes
     * @param batchSize Tamaño de cada lote
     * @param delayTicks Delay entre lotes en ticks
     * @param <T> Tipo de resultado del procesamiento
     */
    public <T> void processBatched(List<UUID> players, Function<UUID, T> processor, 
                                   Consumer<List<T>> onComplete, int batchSize, long delayTicks) {
        
        if (players.isEmpty()) {
            onComplete.accept(new ArrayList<>());
            return;
        }
        
        // Ajustar tamaño de lote basado en presión de memoria
        int effectiveBatchSize = memoryPressure ? Math.max(1, batchSize / 2) : batchSize;
        
        List<List<UUID>> batches = createBatches(players, effectiveBatchSize);
        List<T> allResults = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger completedBatches = new AtomicInteger(0);
        
        logger.info("Procesando " + players.size() + " jugadores en " + batches.size() + 
                   " lotes de " + effectiveBatchSize + " jugadores cada uno");
        
        // Procesar cada lote
        for (int i = 0; i < batches.size(); i++) {
            final List<UUID> batch = batches.get(i);
            final int batchIndex = i;
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    long startTime = System.currentTimeMillis();
                    activeOperations.incrementAndGet();
                    
                    try {
                        // Procesar lote de forma asíncrona
                        CompletableFuture<List<T>> batchFuture = CompletableFuture.supplyAsync(() -> {
                            List<T> batchResults = new ArrayList<>();
                            for (UUID playerId : batch) {
                                try {
                                    T result = processor.apply(playerId);
                                    if (result != null) {
                                        batchResults.add(result);
                                    }
                                } catch (Exception e) {
                                    logger.warning("Error procesando jugador " + playerId + ": " + e.getMessage());
                                }
                            }
                            return batchResults;
                        }, asyncExecutor);
                        
                        // Manejar resultado del lote
                        batchFuture.thenAccept(batchResults -> {
                            allResults.addAll(batchResults);
                            
                            long processingTime = System.currentTimeMillis() - startTime;
                            totalOperations.incrementAndGet();
                            totalProcessingTime.addAndGet(processingTime);
                            
                            logger.fine("Lote " + (batchIndex + 1) + "/" + batches.size() + 
                                       " completado en " + processingTime + "ms (" + batch.size() + " jugadores)");
                            
                            // Verificar si todos los lotes están completos
                            if (completedBatches.incrementAndGet() == batches.size()) {
                                // Ejecutar callback en el hilo principal
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        onComplete.accept(allResults);
                                        logger.info("Procesamiento completo: " + allResults.size() + 
                                                   " resultados de " + players.size() + " jugadores");
                                    }
                                }.runTask(plugin);
                            }
                        }).exceptionally(throwable -> {
                            logger.severe("Error en lote " + (batchIndex + 1) + ": " + throwable.getMessage());
                            completedBatches.incrementAndGet();
                            return null;
                        }).whenComplete((result, throwable) -> {
                            activeOperations.decrementAndGet();
                        });
                        
                    } catch (Exception e) {
                        logger.severe("Error crítico en lote " + (batchIndex + 1) + ": " + e.getMessage());
                        activeOperations.decrementAndGet();
                        completedBatches.incrementAndGet();
                    }
                }
            }.runTaskLaterAsynchronously(plugin, delayTicks * i);
        }
    }
    
    /**
     * Procesa jugadores online de forma optimizada.
     * 
     * @param processor Función que procesa cada jugador online
     * @param onComplete Callback ejecutado al completar
     * @param <T> Tipo de resultado del procesamiento
     */
    public <T> void processOnlinePlayers(Function<Player, T> processor, Consumer<List<T>> onComplete) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        List<UUID> playerIds = onlinePlayers.stream()
            .map(Player::getUniqueId)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        processBatched(playerIds, playerId -> {
            Player player = Bukkit.getPlayer(playerId);
            return player != null ? processor.apply(player) : null;
        }, onComplete);
    }
    
    /**
     * Procesa una colección de jugadores de forma asíncrona con procesamiento por lotes.
     * 
     * @param playerIds Colección de IDs de jugadores
     * @param batchProcessor Función que procesa cada lote de jugadores
     * @param onComplete Callback ejecutado al completar todos los lotes
     * @param <T> Tipo de resultado del procesamiento por lote
     */
    public <T> void processPlayersAsync(Collection<UUID> playerIds, 
                                       Function<Collection<UUID>, T> batchProcessor,
                                       Consumer<T> onComplete) {
        if (playerIds == null || playerIds.isEmpty()) {
            onComplete.accept(null);
            return;
        }
        
        List<UUID> playerList = new ArrayList<>(playerIds);
        
        executeAsync(() -> {
            long startTime = System.currentTimeMillis();
            activeOperations.incrementAndGet();
            
            try {
                T result = batchProcessor.apply(playerList);
                
                // Actualizar métricas
                totalOperations.incrementAndGet();
                totalProcessingTime.addAndGet(System.currentTimeMillis() - startTime);
                
                return result;
            } finally {
                activeOperations.decrementAndGet();
            }
        }, onComplete, error -> {
            logger.severe("Error procesando jugadores de forma asíncrona: " + error.getMessage());
            error.printStackTrace();
        });
    }
    
    /**
     * Procesa una lista de jugadores de forma síncrona con procesamiento por lotes.
     * 
     * @param playerIds Lista de IDs de jugadores
     * @param batchProcessor Función que procesa cada lote de jugadores
     */
    public void processPlayersSync(List<UUID> playerIds, Consumer<Collection<UUID>> batchProcessor) {
        if (playerIds == null || playerIds.isEmpty()) {
            return;
        }
        
        long startTime = System.currentTimeMillis();
        activeOperations.incrementAndGet();
        
        try {
            // Ajustar tamaño de lote basado en presión de memoria
            int batchSize = memoryPressure ? DEFAULT_BATCH_SIZE / 2 : DEFAULT_BATCH_SIZE;
            List<List<UUID>> batches = createBatches(playerIds, batchSize);
            
            for (List<UUID> batch : batches) {
                batchProcessor.accept(batch);
                
                // Pequeña pausa entre lotes para no sobrecargar el servidor
                if (batches.size() > 1) {
                    try {
                        Thread.sleep(10); // 10ms de pausa
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            // Actualizar métricas
            totalOperations.incrementAndGet();
            totalProcessingTime.addAndGet(System.currentTimeMillis() - startTime);
            
        } finally {
            activeOperations.decrementAndGet();
        }
    }
    
    /**
     * Ejecuta una operación de forma asíncrona con manejo de errores.
     * 
     * @param operation Operación a ejecutar
     * @param onSuccess Callback de éxito
     * @param onError Callback de error
     * @param <T> Tipo de resultado
     */
    public <T> void executeAsync(Callable<T> operation, Consumer<T> onSuccess, Consumer<Exception> onError) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return operation.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, asyncExecutor).thenAccept(result -> {
            // Ejecutar callback de éxito en el hilo principal
            new BukkitRunnable() {
                @Override
                public void run() {
                    onSuccess.accept(result);
                }
            }.runTask(plugin);
        }).exceptionally(throwable -> {
            // Ejecutar callback de error en el hilo principal
            new BukkitRunnable() {
                @Override
                public void run() {
                    onError.accept(throwable.getCause() instanceof Exception ? 
                                  (Exception) throwable.getCause() : new Exception(throwable));
                }
            }.runTask(plugin);
            return null;
        });
    }
    
    /**
     * Crea lotes de jugadores para procesamiento.
     */
    private <T> List<List<T>> createBatches(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            batches.add(new ArrayList<>(items.subList(i, end)));
        }
        return batches;
    }
    
    /**
     * Inicia el monitoreo de memoria para ajustar el rendimiento dinámicamente.
     */
    private void startMemoryMonitoring() {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = (double) usedMemory / totalMemory;
            
            // Activar presión de memoria si el uso supera el 80%
            boolean previousPressure = memoryPressure;
            memoryPressure = memoryUsage > 0.8;
            
            if (memoryPressure && !previousPressure) {
                logger.warning("Presión de memoria detectada (" + String.format("%.1f", memoryUsage * 100) + 
                              "%). Reduciendo tamaño de lotes para optimizar rendimiento.");
            } else if (!memoryPressure && previousPressure) {
                logger.info("Presión de memoria aliviada (" + String.format("%.1f", memoryUsage * 100) + 
                           "%). Restaurando tamaño normal de lotes.");
            }
            
        }, MEMORY_CHECK_INTERVAL, MEMORY_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Obtiene métricas de rendimiento actuales.
     * 
     * @return Mapa con métricas de rendimiento
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        long operations = totalOperations.get();
        long processingTime = totalProcessingTime.get();
        
        metrics.put("total_operations", operations);
        metrics.put("total_processing_time_ms", processingTime);
        metrics.put("average_processing_time_ms", operations > 0 ? processingTime / operations : 0);
        metrics.put("active_operations", activeOperations.get());
        metrics.put("memory_pressure", memoryPressure);
        
        // Información de memoria
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        metrics.put("memory_total_mb", totalMemory / (1024 * 1024));
        metrics.put("memory_used_mb", usedMemory / (1024 * 1024));
        metrics.put("memory_free_mb", freeMemory / (1024 * 1024));
        metrics.put("memory_usage_percent", (double) usedMemory / totalMemory * 100);
        
        return metrics;
    }
    
    /**
     * Limpia recursos y detiene todos los hilos.
     */
    public void shutdown() {
        logger.info("Cerrando PerformanceOptimizer...");
        
        asyncExecutor.shutdown();
        scheduledExecutor.shutdown();
        
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("PerformanceOptimizer cerrado correctamente");
    }
    
    /**
     * Verifica si el sistema está bajo presión de memoria.
     * 
     * @return true si hay presión de memoria
     */
    public boolean isUnderMemoryPressure() {
        return memoryPressure;
    }
    
    /**
     * Obtiene el número de operaciones activas.
     * 
     * @return Número de operaciones activas
     */
    public int getActiveOperations() {
        return activeOperations.get();
    }
    
    /**
     * Fuerza la recolección de basura si es necesario.
     */
    public void forceGarbageCollection() {
        if (memoryPressure) {
            logger.info("Forzando recolección de basura debido a presión de memoria");
            System.gc();
        }
    }
}