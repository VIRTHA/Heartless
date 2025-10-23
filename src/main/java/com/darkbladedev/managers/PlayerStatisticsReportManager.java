package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.models.UnifiedEventReport;
import com.darkbladedev.mechanics.AbstractWeeklyEvent;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.PlayerStatisticsValidator;
import com.darkbladedev.utils.PerformanceOptimizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager unificado para generar y mostrar reportes de finalización de eventos semanales.
 * Elimina la duplicación de mensajes y proporciona una plantilla consistente con
 * estadísticas específicas por tipo de evento y funcionalidades avanzadas.
 * 
 * @author DarkBladeDev
 * @version 2.0 - Migrado desde UnifiedEventReportManager
 */
public class PlayerStatisticsReportManager {
    
    private final HeartlessMain plugin;
    private final Logger logger;
    private final PlayerStatisticsValidator validator;
    private final PerformanceOptimizer performanceOptimizer;
    
    // Cache de reportes generados para evitar regeneración innecesaria
    private final Map<String, Map<UUID, UnifiedEventReport>> cachedReports = new ConcurrentHashMap<>();
    
    // Configuración de validación de datos
    private static final int MAX_STAT_VALUE = 1000000; // Valor máximo razonable para estadísticas
    private static final int MIN_SESSION_TIME = 1000; // Tiempo mínimo de sesión en ms
    
    public PlayerStatisticsReportManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.validator = new PlayerStatisticsValidator();
        this.performanceOptimizer = new PerformanceOptimizer(plugin);
    }
    
    /**
     * Genera y envía reportes finales de eventos semanales a todos los jugadores
     * con manejo robusto de errores y logging mejorado
     */
    public void generateAndSendFinalReports(AbstractWeeklyEvent event) {
        generateAndSendFinalReports(event, null);
    }
    
    /**
     * Genera y envía reportes finales para todos los jugadores al completar un evento
     * @param event El evento para el cual generar reportes
     * @param specificPlayers Lista específica de jugadores, o null para usar todos los jugadores online
     */
    public void generateAndSendFinalReports(AbstractWeeklyEvent event, Collection<? extends Player> specificPlayers) {
        if (event == null) {
            logger.severe("Error crítico: Evento nulo pasado a generateAndSendFinalReports");
            return;
        }
        
        try {
            System.out.println("=== DEBUG: Iniciando generateAndSendFinalReports ===");
            System.out.println("Evento: " + event.getId());
            System.out.println("Jugadores específicos: " + (specificPlayers != null ? specificPlayers.size() : "null"));
            
            // Anuncio global del evento
            announceEventCompletion(event);
            
            final Collection<? extends Player> targetPlayers;
            if (specificPlayers != null) {
                targetPlayers = specificPlayers;
            } else {
                // En entorno de pruebas, Bukkit.server puede ser null
                Collection<? extends Player> onlinePlayers;
                try {
                    onlinePlayers = Bukkit.getOnlinePlayers();
                } catch (NullPointerException e) {
                    System.out.println("Bukkit.server es null (entorno de pruebas), usando lista vacía");
                    onlinePlayers = new ArrayList<>();
                }
                targetPlayers = onlinePlayers;
            }
                
            System.out.println("Target players: " + targetPlayers.size());
            
            if (targetPlayers.isEmpty()) {
                System.out.println("No hay jugadores para enviar reportes del evento: " + event.getId());
                // Aún así, generar reportes para todos los jugadores que tienen datos en el evento
                generateReportsForAllEventPlayers(event);
                System.out.println("=== DEBUG: generateAndSendFinalReports completado (sin jugadores online) ===");
                return;
            }
            
            System.out.println("Procesando reportes para " + targetPlayers.size() + " jugadores");
            
            // Mapa para cachear reportes generados
            Map<UUID, UnifiedEventReport> eventReports = new ConcurrentHashMap<>();
            
            // Si tenemos jugadores específicos, procesarlos directamente
            if (specificPlayers != null && !specificPlayers.isEmpty()) {
                System.out.println("Procesando jugadores específicos directamente");
                for (Player player : specificPlayers) {
                    System.out.println("Procesando jugador específico: " + player.getName() + " (UUID: " + player.getUniqueId() + ")");
                    try {
                        UnifiedEventReport report = generateUnifiedReport(player.getUniqueId(), event);
                        System.out.println("Reporte generado para " + player.getName() + ": " + (report != null));
                        if (report != null) {
                            // Cachear el reporte generado
                            eventReports.put(player.getUniqueId(), report);
                            sendUnifiedReport(player, report, event);
                            System.out.println("Reporte enviado exitosamente a: " + player.getName());
                        } else {
                            System.out.println("No se pudo generar reporte para: " + player.getName());
                        }
                    } catch (Exception e) {
                        System.err.println("Error procesando jugador " + player.getName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                // Procesamiento optimizado por lotes usando PerformanceOptimizer para jugadores online
                PerformanceOptimizer optimizer = new PerformanceOptimizer(HeartlessMain.getInstance());
                optimizer.processOnlinePlayers(player -> {
                    try {
                        if (player != null && player.isOnline()) {
                            UnifiedEventReport report = generateUnifiedReport(player.getUniqueId(), event);
                            if (report != null) {
                                // Cachear el reporte generado
                                eventReports.put(player.getUniqueId(), report);
                                sendUnifiedReport(player, report, event);
                                logger.fine("Reporte enviado exitosamente a: " + player.getName());
                                return player.getName(); // Retornar nombre como resultado exitoso
                            } else {
                                logger.warning("No se pudo generar reporte para: " + player.getName());
                            }
                        }
                    } catch (Exception e) {
                        logger.severe("Error procesando jugador " + player.getName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                    return null;
                }, results -> {
                    // Callback ejecutado al completar todos los lotes
                    long successCount = results.stream().filter(Objects::nonNull).count();
                    logger.info("Reportes finales completados: " + successCount + " de " + targetPlayers.size() + " jugadores procesados exitosamente");
                });
            }
            
            // Cachear todos los reportes generados
            for (Map.Entry<UUID, UnifiedEventReport> entry : eventReports.entrySet()) {
                cacheReport(event.getId(), entry.getKey(), entry.getValue());
                System.out.println("Reporte cacheado para UUID: " + entry.getKey());
            }
            
            System.out.println("=== DEBUG: generateAndSendFinalReports completado ===");
            
        } catch (Exception e) {
            System.err.println("Error crítico en generateAndSendFinalReports: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Genera reportes para todos los jugadores que tienen datos en el evento,
     * independientemente de si están online o no
     */
    private void generateReportsForAllEventPlayers(AbstractWeeklyEvent event) {
        logger.info("=== DEBUG: Iniciando generateReportsForAllEventPlayers ===");
        
        // Obtener todos los jugadores con datos en el evento
        Set<UUID> allPlayerIds = new HashSet<>();
        allPlayerIds.addAll(event.getAllPlayersWithStatistics());
        allPlayerIds.addAll(event.getAllCompletedChallenges().keySet());
        allPlayerIds.addAll(event.getAllChallengeProgress().keySet());
        
        logger.info("Total de jugadores con datos: " + allPlayerIds.size());
        logger.info("IDs de jugadores: " + allPlayerIds);
        
        Map<UUID, UnifiedEventReport> reportCache = new ConcurrentHashMap<>();
        
        for (UUID playerId : allPlayerIds) {
            logger.info("Procesando jugador: " + playerId);
            try {
                UnifiedEventReport report = generateUnifiedReport(playerId, event);
                logger.info("Reporte generado para " + playerId + ": " + (report != null));
                if (report != null) {
                    reportCache.put(playerId, report);
                    logger.info("Reporte almacenado en caché para: " + playerId);
                } else {
                    logger.warning("No se pudo generar reporte para jugador: " + playerId);
                }
            } catch (Exception e) {
                logger.severe("Error generando reporte para jugador " + playerId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Cachear todos los reportes generados
        if (!reportCache.isEmpty()) {
            cachedReports.put(event.getId(), reportCache);
            logger.info("Cacheados " + reportCache.size() + " reportes para evento: " + event.getId());
        }
        
        logger.info("=== DEBUG: Finalizando generateReportsForAllEventPlayers ===");
        logger.info("Reportes en caché: " + reportCache.size());
    }
    
    /**
     * Anuncia la finalización del evento a todos los jugadores en línea
     * con manejo robusto de errores
     */
    public void announceEventCompletion(AbstractWeeklyEvent event) {
        if (event == null) {
            logger.severe("Error: Evento nulo en announceEventCompletion");
            return;
        }
        
        try {
            String eventName = getEventDisplayName(event.getId());
            if (eventName == null || eventName.trim().isEmpty()) {
                logger.warning("Nombre de evento vacío o nulo para ID: " + event.getId());
                eventName = "Evento Desconocido";
            }
            
            Component announcement = MM.toComponent(
                "<gradient:#ff6b6b:#4ecdc4>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</gradient>\n" +
                "<center><gradient:#ffd93d:#6bcf7f>🎉 ¡" + eventName + " ha finalizado! 🎉</gradient></center>\n" +
                "<center><white>Revisa tus estadísticas y logros obtenidos durante el evento</white></center>\n" +
                "<gradient:#ff6b6b:#4ecdc4>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</gradient>"
            );
            
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            int playersNotified = 0;
            
            for (Player player : onlinePlayers) {
                try {
                    if (player != null && player.isOnline()) {
                        player.sendMessage(announcement);
                        playersNotified++;
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error enviando anuncio de finalización a jugador: " + 
                              (player != null ? player.getName() : "desconocido"), e);
                }
            }
            
            logger.info("Anuncio de finalización de evento enviado a " + playersNotified + " jugadores para: " + eventName);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error crítico anunciando finalización del evento: " + event.getId(), e);
        }
    }
    
    /**
     * Genera un reporte unificado para un jugador específico.
     * Incluye validación completa de datos antes de crear el reporte.
     * 
     * @param playerId ID del jugador
     * @param event El evento
     * @return Reporte unificado del jugador o null si no hay datos suficientes o válidos
     */
    private UnifiedEventReport generateUnifiedReport(UUID playerId, AbstractWeeklyEvent event) {
        Map<String, Object> playerStats = event.getPlayerStatistics(playerId);
        Set<String> completedChallenges = event.getCompletedChallenges(playerId);
        Map<String, Object> challengeProgress = event.getChallengeProgress(playerId);
        
        // Debug logging
        logger.info("=== DEBUG generateUnifiedReport para jugador: " + playerId + " ===");
        logger.info("Player stats: " + playerStats);
        logger.info("Completed challenges: " + completedChallenges);
        logger.info("Challenge progress: " + challengeProgress);
        
        // Verificar si el jugador tiene datos suficientes
        if (playerStats.isEmpty() && completedChallenges.isEmpty()) {
            logger.warning("No hay datos suficientes para jugador " + playerId + " - stats vacías y sin desafíos completados");
            return null;
        }
        
        // Validar estadísticas del jugador
        PlayerStatisticsValidator.ValidationResult statsValidation = 
            validator.validatePlayerStatistics(playerId, playerStats);
        logger.info("Stats validation result: valid=" + statsValidation.isValid() + ", error=" + statsValidation.getErrorMessage());
        if (!statsValidation.isValid()) {
            logger.warning("Estadísticas inválidas para jugador " + playerId + ": " + statsValidation.getErrorMessage());
            @SuppressWarnings("unchecked")
            Map<String, Object> cleanedStats = statsValidation.getCleanedDataAs(Map.class);
            playerStats = (cleanedStats != null) ? cleanedStats : new HashMap<>();
        } else if (statsValidation.getCleanedData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cleanedStats = statsValidation.getCleanedDataAs(Map.class);
            if (cleanedStats != null) {
                playerStats = cleanedStats;
            }
        }
        
        // Validar desafíos completados
        PlayerStatisticsValidator.ValidationResult challengesValidation = 
            validator.validateCompletedChallenges(playerId, completedChallenges);
        logger.info("Challenges validation result: valid=" + challengesValidation.isValid() + ", error=" + challengesValidation.getErrorMessage());
        if (!challengesValidation.isValid()) {
            logger.warning("Desafíos completados inválidos para jugador " + playerId + ": " + challengesValidation.getErrorMessage());
            @SuppressWarnings("unchecked")
            Set<String> cleanedChallenges = challengesValidation.getCleanedDataAs(Set.class);
            completedChallenges = (cleanedChallenges != null) ? cleanedChallenges : new HashSet<>();
        } else if (challengesValidation.getCleanedData() != null) {
            @SuppressWarnings("unchecked")
            Set<String> cleanedChallenges = challengesValidation.getCleanedDataAs(Set.class);
            if (cleanedChallenges != null) {
                completedChallenges = cleanedChallenges;
            }
        }
        
        // Validar progreso de desafíos
        PlayerStatisticsValidator.ValidationResult progressValidation = 
            validator.validateChallengeProgress(playerId, challengeProgress);
        logger.info("Progress validation result: valid=" + progressValidation.isValid() + ", error=" + progressValidation.getErrorMessage());
        if (!progressValidation.isValid()) {
            logger.warning("Progreso de desafíos inválido para jugador " + playerId + ": " + progressValidation.getErrorMessage());
            @SuppressWarnings("unchecked")
            Map<String, Object> cleanedProgress = progressValidation.getCleanedDataAs(Map.class);
            challengeProgress = (cleanedProgress != null) ? cleanedProgress : new HashMap<>();
        } else if (progressValidation.getCleanedData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cleanedProgress = progressValidation.getCleanedDataAs(Map.class);
            if (cleanedProgress != null) {
                challengeProgress = cleanedProgress;
            }
        }
        
        // Obtener desafíos disponibles
        Map<String, AbstractWeeklyEvent.ChallengeDefinition> availableChallenges = event.getAvailableChallenges();
        logger.info("Available challenges: " + availableChallenges.keySet());
        
        // Detectar anomalías
        Player player = plugin.getServer().getPlayer(playerId);
        List<String> anomalies = validator.detectAnomalies(playerId, playerStats, player);
        if (!anomalies.isEmpty()) {
            logger.info("Anomalías detectadas para jugador " + playerId + ": " + String.join(", ", anomalies));
        }
        
        logger.info("Creando UnifiedEventReport para jugador: " + playerId);
        UnifiedEventReport report = new UnifiedEventReport(
            playerId,
            event.getId(),
            playerStats,
            completedChallenges,
            challengeProgress,
            availableChallenges,
            System.currentTimeMillis()
        );
        logger.info("UnifiedEventReport creado exitosamente: " + (report != null));
        
        return report;
    }
    
    /**
     * Envía el reporte unificado a un jugador específico
     */
    private void sendUnifiedReport(Player player, UnifiedEventReport report, AbstractWeeklyEvent event) {
        try {
            // === ENCABEZADO DEL REPORTE ===
            sendReportHeader(player, event);
            
            // === SECCIÓN DE DESAFÍOS COMPLETADOS ===
            sendChallengeSection(player, report, event);
            
            // === FOOTER CON ESTADÍSTICAS INDIVIDUALES ===
            sendStatisticsFooter(player, report, event);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "[PlayerStatisticsReportManager] Error enviando reporte a " + player.getName(), e);
        }
    }
    
    /**
     * Envía el encabezado del reporte con título claro y descriptivo
     */
    private void sendReportHeader(Player player, AbstractWeeklyEvent event) {
        String eventName = getEventDisplayName(event.getId());
        
        player.sendMessage(MM.toComponent("<gray><b>═══════════════════════════════════</b></gray>"));
        player.sendMessage(MM.toComponent("<green><b>REPORTE FINAL - " + eventName.toUpperCase() + "</b></green>"));
        player.sendMessage(MM.toComponent("<gray><b>═══════════════════════════════════</b></gray>"));
        player.sendMessage(MM.toComponent(""));
    }
    
    /**
     * Envía la sección de desafíos con información detallada
     */
    private void sendChallengeSection(Player player, UnifiedEventReport report, AbstractWeeklyEvent event) {
        player.sendMessage(MM.toComponent("<yellow><b>📋 DESAFÍOS COMPLETADOS:</b></yellow>"));
        player.sendMessage(MM.toComponent(""));
        
        Map<String, AbstractWeeklyEvent.ChallengeDefinition> availableChallenges = report.getAvailableChallenges();
        Set<String> completedChallenges = report.getCompletedChallenges();
        Map<String, Object> challengeProgress = report.getChallengeProgress();
        
        int totalChallenges = availableChallenges.size();
        int completedCount = completedChallenges.size();
        
        // Mostrar cada desafío con su estado
        for (Map.Entry<String, AbstractWeeklyEvent.ChallengeDefinition> entry : availableChallenges.entrySet()) {
            String challengeId = entry.getKey();
            AbstractWeeklyEvent.ChallengeDefinition challenge = entry.getValue();
            
            boolean isCompleted = completedChallenges.contains(challengeId);
            String status = isCompleted ? "<green>✓</green>" : "<red>✗</red>";
            String challengeName = challenge.getDisplayName();
            
            // Obtener progreso real del jugador
            Object currentProgress = challengeProgress.getOrDefault(challengeId + "_current", 0);
            Object maxProgress = challengeProgress.getOrDefault(challengeId + "_max", challenge.getRequiredProgress());
            
            // Crear hover text con descripción y progreso
            String hoverText = "<yellow>Descripción:</yellow>\n<gray>" + challenge.getDescription() + "</gray>\n\n" +
                              "<yellow>Progreso:</yellow> <white>" + currentProgress + "/" + maxProgress + "</white>";
            
            // Crear mensaje con hover para mostrar descripción y progreso
            Component challengeComponent = MM.toComponent(
                "  " + status + " <white>" + challengeName + "</white>"
            ).hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
            
            player.sendMessage(challengeComponent);
        }
        
        player.sendMessage(MM.toComponent(""));
        player.sendMessage(MM.toComponent("<gold>Total completados: <white>" + completedCount + "/" + totalChallenges + "</white></gold>"));
        player.sendMessage(MM.toComponent(""));
    }
    
    /**
     * Envía el footer con estadísticas individuales del jugador
     */
    private void sendStatisticsFooter(Player player, UnifiedEventReport report, AbstractWeeklyEvent event) {
        player.sendMessage(MM.toComponent("<blue><b>📊 TUS ESTADÍSTICAS PERSONALES:</b></blue>"));
        player.sendMessage(MM.toComponent(""));
        
        Map<String, Object> playerStats = report.getPlayerStatistics();
        
        // Mostrar estadísticas específicas del evento
        displayEventSpecificStats(player, playerStats, event.getId());
        
        player.sendMessage(MM.toComponent(""));
        player.sendMessage(MM.toComponent("<gold>═══════════════════════════════════</gold>"));
        player.sendMessage(MM.toComponent("<gray>Reporte generado el " + new Date(report.getGeneratedAt()) + "</gray>"));
    }
    
    /**
     * Muestra estadísticas específicas según el tipo de evento.
     * 
     * @param player El jugador
     * @param playerStats Las estadísticas del jugador
     * @param eventId ID del evento
     */
    private void displayEventSpecificStats(Player player, Map<String, Object> playerStats, String eventId) {
        switch (eventId.toLowerCase()) {
            case "acid_week":
                displayAcidWeekStats(player, playerStats);
                break;
            case "blood_and_iron_week":
                displayBloodAndIronStats(player, playerStats);
                break;
            case "explosive_week":
                displayExplosiveWeekStats(player, playerStats);
                break;
            case "undead_week":
                displayUndeadWeekStats(player, playerStats);
                break;
            default:
                displayGenericEventStats(player, playerStats);
                break;
        }
    }
    
    /**
     * Muestra estadísticas específicas de la Semana Ácida
     */
    private void displayAcidWeekStats(Player player, Map<String, Object> playerStats) {
        Object acidDamage = playerStats.get("acid_damage_received");
        Object fishCollected = playerStats.get("fish_types_collected");
        Object rainSurvivalTime = playerStats.get("rain_survival_time");
        
        if (acidDamage != null) {
            Component hoverText = MM.toComponent(
                "<yellow>Información detallada:</yellow>\n" +
                "<white>Daño total recibido por lluvia ácida</white>\n" +
                "<gray>Valor: " + acidDamage + " puntos de daño</gray>"
            );
            Component message = MM.toComponent("<yellow>🧪 Daño ácido recibido: <white>" + acidDamage + "</white></yellow>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
        
        if (fishCollected != null) {
            Component hoverText = MM.toComponent(
                "<blue>Tipos de pescado:</blue>\n" +
                "<white>Salmón, Bacalao, Pez Tropical, Pez Globo</white>\n" +
                "<gray>Recolectados: " + fishCollected + "/4</gray>"
            );
            Component message = MM.toComponent("<blue>🐟 Tipos de pescado recolectados: <white>" + fishCollected + "/4</white></blue>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
        
        if (rainSurvivalTime != null) {
            String formattedTime = formatTime(((Number) rainSurvivalTime).longValue());
            Component hoverText = MM.toComponent(
                "<green>Supervivencia bajo lluvia ácida:</green>\n" +
                "<white>Tiempo total sobrevivido durante eventos de lluvia ácida</white>\n" +
                "<gray>Duración: " + formattedTime + "</gray>"
            );
            Component message = MM.toComponent("<green>🌧️ Tiempo bajo lluvia ácida: <white>" + formattedTime + "</white></green>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
    }
    
    /**
     * Muestra estadísticas específicas de la Semana de Sangre y Hierro
     */
    private void displayBloodAndIronStats(Player player, Map<String, Object> playerStats) {
        Object mobsKilled = playerStats.get("mobs_killed");
        Object damageDealt = playerStats.get("damage_dealt");
        Object ironCollected = playerStats.get("iron_collected");
        
        if (mobsKilled != null) {
            Component hoverText = MM.toComponent(
                "<red>Eliminaciones de mobs:</red>\n" +
                "<white>Total de criaturas hostiles eliminadas</white>\n" +
                "<gray>Cantidad: " + mobsKilled + " mobs</gray>"
            );
            Component message = MM.toComponent("<red>⚔️ Mobs eliminados: <white>" + mobsKilled + "</white></red>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
        
        if (damageDealt != null) {
            Component hoverText = MM.toComponent(
                "<dark_red>Daño infligido:</dark_red>\n" +
                "<white>Daño total causado a enemigos</white>\n" +
                "<gray>Total: " + damageDealt + " puntos</gray>"
            );
            Component message = MM.toComponent("<dark_red>💥 Daño total infligido: <white>" + damageDealt + "</white></dark_red>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
        
        if (ironCollected != null) {
            Component hoverText = MM.toComponent(
                "<gray>Hierro recolectado:</gray>\n" +
                "<white>Lingotes de hierro obtenidos durante el evento</white>\n" +
                "<gray>Cantidad: " + ironCollected + " lingotes</gray>"
            );
            Component message = MM.toComponent("<gray>⛏️ Hierro recolectado: <white>" + ironCollected + "</white></gray>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
    }
    
    /**
     * Muestra estadísticas específicas de la Semana Explosiva
     */
    private void displayExplosiveWeekStats(Player player, Map<String, Object> playerStats) {
        Object explosionsTriggered = playerStats.get("explosions_triggered");
        Object blocksDestroyed = playerStats.get("blocks_destroyed");
        Object tntUsed = playerStats.get("tnt_used");
        
        if (explosionsTriggered != null) {
            Component hoverText = MM.toComponent(
                "<gold>Explosiones activadas:</gold>\n" +
                "<white>Total de explosiones causadas por el jugador</white>\n" +
                "<gray>Cantidad: " + explosionsTriggered + " explosiones</gray>"
            );
            Component message = MM.toComponent("<gold>💣 Explosiones activadas: <white>" + explosionsTriggered + "</white></gold>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
        
        if (blocksDestroyed != null) {
            Component hoverText = MM.toComponent(
                "<yellow>Bloques destruidos:</yellow>\n" +
                "<white>Bloques destruidos por explosiones</white>\n" +
                "<gray>Total: " + blocksDestroyed + " bloques</gray>"
            );
            Component message = MM.toComponent("<yellow>🧱 Bloques destruidos: <white>" + blocksDestroyed + "</white></yellow>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
        
        if (tntUsed != null) {
            Component hoverText = MM.toComponent(
                "<red>TNT utilizada:</red>\n" +
                "<white>Cantidad de TNT detonada durante el evento</white>\n" +
                "<gray>Cantidad: " + tntUsed + " bloques de TNT</gray>"
            );
            Component message = MM.toComponent("<red>🧨 TNT utilizada: <white>" + tntUsed + "</white></red>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
    }
    
    /**
     * Muestra estadísticas específicas de la Semana de No-Muertos
     */
    private void displayUndeadWeekStats(Player player, Map<String, Object> playerStats) {
        Object undeadKilled = playerStats.get("undead_killed");
        Object nightsSurvived = playerStats.get("nights_survived");
        Object bonesCollected = playerStats.get("bones_collected");
        
        if (undeadKilled != null) {
            Component hoverText = MM.toComponent(
                "<dark_purple>No-muertos eliminados:</dark_purple>\n" +
                "<white>Zombies, esqueletos y otros no-muertos eliminados</white>\n" +
                "<gray>Total: " + undeadKilled + " criaturas</gray>"
            );
            Component message = MM.toComponent("<dark_purple>🧟 No-muertos eliminados: <white>" + undeadKilled + "</white></dark_purple>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
        
        if (nightsSurvived != null) {
            Component hoverText = MM.toComponent(
                "<dark_blue>Noches sobrevividas:</dark_blue>\n" +
                "<white>Noches completas sobrevividas durante el evento</white>\n" +
                "<gray>Total: " + nightsSurvived + " noches</gray>"
            );
            Component message = MM.toComponent("<dark_blue>🌙 Noches sobrevividas: <white>" + nightsSurvived + "</white></dark_blue>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
        
        if (bonesCollected != null) {
            Component hoverText = MM.toComponent(
                "<white>Huesos recolectados:</white>\n" +
                "<white>Huesos obtenidos de esqueletos eliminados</white>\n" +
                "<gray>Cantidad: " + bonesCollected + " huesos</gray>"
            );
            Component message = MM.toComponent("<white>🦴 Huesos recolectados: <white>" + bonesCollected + "</white></white>")
                .hoverEvent(HoverEvent.showText(hoverText));
            player.sendMessage(message);
        }
    }
    
    /**
     * Muestra estadísticas genéricas para eventos no específicos
     */
    private void displayGenericEventStats(Player player, Map<String, Object> playerStats) {
        for (Map.Entry<String, Object> entry : playerStats.entrySet()) {
            String statName = getStatisticDisplayName(entry.getKey());
            Object value = entry.getValue();
            
            if (!entry.getKey().equals("session_time")) {
                player.sendMessage(MM.toComponent("<gray>📈 " + statName + ": <white>" + value + "</white></gray>"));
            }
        }
    }
    
    /**
     * Obtiene el nombre de visualización de una estadística.
     * 
     * @param statKey Clave de la estadística
     * @return Nombre para mostrar
     */
    private String getStatisticDisplayName(String statKey) {
        Map<String, String> statNames = new HashMap<>();
        statNames.put("zombie_kills", "Zombies eliminados");
        statNames.put("player_interactions", "Interacciones con jugadores");
        statNames.put("stat_updates", "Actualizaciones de estadísticas");
        statNames.put("cured_infections", "Infecciones curadas");
        statNames.put("red_moon_kills", "Eliminaciones en luna roja");
        statNames.put("kills", "Eliminaciones");
        statNames.put("deaths", "Muertes");
        statNames.put("playtime", "Tiempo jugado");
        statNames.put("damage_dealt", "Daño infligido");
        statNames.put("damage_taken", "Daño recibido");
        statNames.put("blocks_broken", "Bloques rotos");
        statNames.put("blocks_placed", "Bloques colocados");
        statNames.put("distance_traveled", "Distancia recorrida");
        statNames.put("items_collected", "Objetos recolectados");
        statNames.put("experience_gained", "Experiencia ganada");
        
        return statNames.getOrDefault(statKey, statKey.replace("_", " "));
    }
    
    /**
     * Formatea un tiempo en milisegundos a un formato legible.
     * 
     * @param timeMs Tiempo en milisegundos
     * @return Tiempo formateado
     */
    private String formatTime(long timeMs) {
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Obtiene un reporte cacheado para un jugador y evento específico.
     * 
     * @param eventId ID del evento
     * @param playerId ID del jugador
     * @return Reporte cacheado o null si no existe
     */
    public UnifiedEventReport getCachedReport(String eventId, UUID playerId) {
        Map<UUID, UnifiedEventReport> eventReports = cachedReports.get(eventId);
        if (eventReports != null) {
            return eventReports.get(playerId);
        }
        return null;
    }
    
    /**
     * Limpia reportes cacheados antiguos para liberar memoria.
     * 
     * @param maxAge Edad máxima en milisegundos
     */
    public void cleanupOldReports(long maxAge) {
        long currentTime = System.currentTimeMillis();
        
        cachedReports.entrySet().removeIf(eventEntry -> {
            Map<UUID, UnifiedEventReport> reports = eventEntry.getValue();
            return reports.values().stream()
                .anyMatch(report -> (currentTime - report.getGeneratedAt()) > maxAge);
        });
    }
    
    /**
     * Limpia reportes antiguos del cache
     */
    public void cleanupOldReports() {
        long currentTime = System.currentTimeMillis();
        long maxAge = 24 * 60 * 60 * 1000; // 24 horas
        
        cachedReports.entrySet().removeIf(eventEntry -> {
            Map<UUID, UnifiedEventReport> reports = eventEntry.getValue();
            return reports.values().stream()
                .anyMatch(report -> (currentTime - report.getGeneratedAt()) > maxAge);
        });
    }
    
    /**
     * Cachea un reporte individual para un evento y jugador específico
     * 
     * @param eventId ID del evento
     * @param playerId ID del jugador
     * @param report Reporte a cachear
     */
    private void cacheReport(String eventId, UUID playerId, UnifiedEventReport report) {
        cachedReports.computeIfAbsent(eventId, k -> new ConcurrentHashMap<>()).put(playerId, report);
    }
    
    /**
     * Obtiene el nombre de visualización de un evento.
     * 
     * @param eventId ID del evento
     * @return Nombre para mostrar
     */
    private String getEventDisplayName(String eventId) {
        Map<String, String> eventNames = new HashMap<>();
        eventNames.put("acidweek", "Semana Acida");
        eventNames.put("bloodandironweek", "Blood and Iron Week");
        eventNames.put("explosiveweek", "Explosive Week");
        eventNames.put("undeadweek", "Undead Week");
        
        return eventNames.getOrDefault(eventId.toLowerCase(), eventId);
    }
}