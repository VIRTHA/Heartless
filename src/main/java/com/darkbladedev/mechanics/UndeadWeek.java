package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.MM;

import java.util.*;

// Add this import at the top with other imports
import org.bukkit.event.entity.EntitySpawnEvent;

public class UndeadWeek extends WeeklyEvent {

    private boolean isRedMoonActive = false;
    private int nightCounter = 0;
    private BukkitTask mainTask;
    
    // Tracking infected players and challenges
    private Map<UUID, Boolean> infectedPlayers = new HashMap<>();
    private Map<UUID, Integer> curedInfectionsCount = new HashMap<>();
    private Map<UUID, Integer> redMoonKillsCount = new HashMap<>();
    private Set<UUID> curedVillagers = new HashSet<>();
    private Set<UUID> witherKilledInRedMoon = new HashSet<>();
    
    // Lista de entidades no-muertas
    private final List<EntityType> undeadEntities = Arrays.asList(
        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITHER_SKELETON,
        EntityType.ZOMBIFIED_PIGLIN, EntityType.WITHER, EntityType.ZOMBIE_VILLAGER,
        EntityType.DROWNED, EntityType.HUSK, EntityType.STRAY, EntityType.PHANTOM,
        EntityType.ZOGLIN
    );
    
    public UndeadWeek(HeartlessMain plugin, long duration) {
        super(plugin, duration);
        this.prefix = "<b><gradient:#58fd90:#56fa96:#54f69b:#51f3a1:#4ff0a7:#4decac:#4be9b2:#49e6b8:#46e2bd:#44dfc3:#42dcc9:#40d9cf:#3dd5d4:#3bd2da:#39cfe0:#37cbe5:#35c8eb:#32c5f1:#30c1f6:#2ebefc>Semana de los No Muertos</gradient></b>";
    }
    
    /**
     * Detiene todas las tareas del evento, restaura el ciclo día/noche normal,
     * elimina efectos de jugadores infectados y otorga recompensas finales
     */
    @Override
    protected void stopEventTasks() {
        try {
            Bukkit.getLogger().info(prefix + " Iniciando detención de tareas del evento...");
            
            // Cancelar la tarea principal con manejo de errores
            if (mainTask != null) {
                try {
                    if (!mainTask.isCancelled()) {
                        mainTask.cancel();
                        Bukkit.getLogger().info(prefix + " Tarea principal cancelada correctamente (ID: " + mainTask.getTaskId() + ")");
                    } else {
                        Bukkit.getLogger().info(prefix + " La tarea principal ya estaba cancelada");
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning(prefix + " Error al cancelar la tarea principal: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    mainTask = null;
                }
            } else {
                Bukkit.getLogger().info(prefix + " No había tarea principal activa para cancelar");
            }
            
            // Cancelar la tarea de finalización si existe
            if (endTask != null) {
                try {
                    if (!endTask.isCancelled()) {
                        endTask.cancel();
                        Bukkit.getLogger().info(prefix + " Tarea de finalización cancelada correctamente");
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning(prefix + " Error al cancelar la tarea de finalización: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    endTask = null;
                }
            }
            
            // Desactivar Luna Roja si está activa
            if (isRedMoonActive) {
                try {
                    deactivateRedMoon();
                    Bukkit.getLogger().info(" Luna Roja desactivada durante la detención de tareas");
                } catch (Exception e) {
                    Bukkit.getLogger().warning(" Error al desactivar Luna Roja: " + e.getMessage());
                    isRedMoonActive = false; // Forzar desactivación en caso de error
                }
            }
            
            // Restaurar ciclo día/noche normal con límite de procesamiento
            int worldsProcessed = 0;
            final int MAX_WORLDS_TO_PROCESS = 10; // Límite para evitar sobrecarga
            
            List<World> worlds = Bukkit.getWorlds();
            if (worlds != null && !worlds.isEmpty()) {
                for (World world : worlds) {
                    // Verificar si el mundo es válido
                    if (world == null) {
                        continue;
                    }
                    
                    // Limitar la cantidad de mundos procesados
                    if (worldsProcessed >= MAX_WORLDS_TO_PROCESS) {
                        Bukkit.getLogger().warning(prefix + " Límite de mundos procesados alcanzado ("+MAX_WORLDS_TO_PROCESS+") al restaurar ciclo día/noche");
                        break;
                    }
                    
                    try {
                        if (world.getEnvironment() == World.Environment.NORMAL) {
                            world.setTime(0); // Establecer a día
                            worldsProcessed++;
                            Bukkit.getLogger().info(prefix + " Ciclo día/noche restaurado en mundo: " + world.getName());
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning(prefix + " Error al restaurar ciclo día/noche en mundo " + world.getName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                Bukkit.getLogger().warning(prefix + " No se encontraron mundos disponibles para restaurar ciclo día/noche");
            }
            
            // Eliminar efectos de jugadores infectados con límite de procesamiento
            if (infectedPlayers != null && !infectedPlayers.isEmpty()) {
                int playersProcessed = 0;
                final int MAX_PLAYERS_TO_PROCESS = 100; // Límite para evitar sobrecarga
                int totalPlayers = infectedPlayers.size();
                
                Bukkit.getLogger().info(prefix + " Eliminando efectos de " + totalPlayers + " jugadores infectados...");
                
                // Crear una copia de las claves para evitar ConcurrentModificationException
                Set<UUID> playerIds = new HashSet<>(infectedPlayers.keySet());
                
                for (UUID playerId : playerIds) {
                    // Verificar si el ID es válido
                    if (playerId == null) {
                        continue;
                    }
                    
                    // Limitar la cantidad de jugadores procesados
                    if (playersProcessed >= MAX_PLAYERS_TO_PROCESS) {
                        Bukkit.getLogger().warning(prefix + " Límite de jugadores procesados alcanzado ("+MAX_PLAYERS_TO_PROCESS+"/"+totalPlayers+") al eliminar efectos");
                        break;
                    }
                    
                    try {
                        Player player = Bukkit.getPlayer(playerId);
                        if (player != null && player.isOnline()) {
                            // Eliminar todos los efectos negativos posibles
                            player.removePotionEffect(PotionEffectType.POISON);
                            player.removePotionEffect(PotionEffectType.WEAKNESS);
                            player.removePotionEffect(PotionEffectType.SLOWNESS);
                            player.removePotionEffect(PotionEffectType.NAUSEA);
                            player.removePotionEffect(PotionEffectType.BLINDNESS);
                            player.removePotionEffect(PotionEffectType.HUNGER);
                            
                            // Efectos visuales y sonoros para indicar que el jugador ha sido curado
                            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f, 1.2f);
                            player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 20, 1, 1, 1, 0.1);
                            player.sendMessage(MM.toComponent(prefix + " <green>¡Has sido curado de la infección! El evento ha finalizado."));
                            
                            playersProcessed++;
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning(prefix + " Error al eliminar efectos del jugador " + playerId + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                Bukkit.getLogger().info(prefix + " Efectos eliminados de " + playersProcessed + " jugadores infectados");
            } else {
                Bukkit.getLogger().info(prefix + " No hay jugadores infectados para curar");
            }
            
            // Dar recompensas finales
            try {
                Bukkit.getLogger().info(prefix + " Otorgando recompensas finales...");
                giveRewards();
                Bukkit.getLogger().info(prefix + " Recompensas finales otorgadas correctamente");
            } catch (Exception e) {
                Bukkit.getLogger().severe(prefix + " Error al otorgar recompensas finales: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Anunciar finalización de tareas
            Bukkit.getLogger().info(prefix + " Todas las tareas del evento han sido detenidas correctamente");
            
            // Reiniciar contadores y estados
            nightCounter = 0;
            isRedMoonActive = false;
        } catch (Exception e) {
            Bukkit.getLogger().severe(prefix + " Error crítico al detener las tareas del evento: " + e.getMessage());
            e.printStackTrace();
            
            // Intentar limpiar recursos críticos en caso de error fatal
            try {
                if (mainTask != null) {
                    mainTask.cancel();
                    mainTask = null;
                }
                if (endTask != null) {
                    endTask.cancel();
                    endTask = null;
                }
                isRedMoonActive = false;
                nightCounter = 0;
                Bukkit.getLogger().info(prefix + " Recursos críticos liberados tras error fatal");
            } catch (Exception ex) {
                Bukkit.getLogger().severe(prefix + " Error al liberar recursos críticos: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Limpia todos los datos del evento para evitar fugas de memoria
     * y preparar el sistema para un nuevo evento.
     * Incluye verificaciones de nulidad, manejo de errores y logging detallado.
     */
    @Override
    protected void cleanupEventData() {
        Bukkit.getLogger().info(prefix + " Iniciando limpieza de datos del evento...");
        
        try {
            // Registrar estadísticas antes de limpiar (para análisis)
            int infectedCount = (infectedPlayers != null) ? infectedPlayers.size() : 0;
            int totalCuredInfections = 0;
            int totalRedMoonKills = 0;
            int totalCuredVillagers = (curedVillagers != null) ? curedVillagers.size() : 0;
            int totalWitherKilled = (witherKilledInRedMoon != null) ? witherKilledInRedMoon.size() : 0;
            
            // Calcular estadísticas totales si las colecciones existen
            if (curedInfectionsCount != null) {
                for (Integer count : curedInfectionsCount.values()) {
                    if (count != null) {
                        totalCuredInfections += count;
                    }
                }
            }
            
            if (redMoonKillsCount != null) {
                for (Integer count : redMoonKillsCount.values()) {
                    if (count != null) {
                        totalRedMoonKills += count;
                    }
                }
            }
            
            // Registrar estadísticas finales del evento
            Bukkit.getLogger().info(prefix + " Estadísticas finales del evento:");
            Bukkit.getLogger().info(prefix + " - Jugadores infectados al finalizar: " + infectedCount);
            Bukkit.getLogger().info(prefix + " - Total de infecciones curadas: " + totalCuredInfections);
            Bukkit.getLogger().info(prefix + " - Total de asesinatos en Luna Roja: " + totalRedMoonKills);
            Bukkit.getLogger().info(prefix + " - Total de aldeanos curados: " + totalCuredVillagers);
            Bukkit.getLogger().info(prefix + " - Total de Withers eliminados en Luna Roja: " + totalWitherKilled);
            
            // Limpiar datos de seguimiento con verificaciones de nulidad
            if (infectedPlayers != null) {
                int size = infectedPlayers.size();
                infectedPlayers.clear();
                Bukkit.getLogger().info(prefix + " Lista de " + size + " jugadores infectados limpiada correctamente");
            } else {
                infectedPlayers = new HashMap<>();
                Bukkit.getLogger().info(prefix + " Creada nueva lista de jugadores infectados");
            }
            
            if (curedInfectionsCount != null) {
                int size = curedInfectionsCount.size();
                curedInfectionsCount.clear();
                Bukkit.getLogger().info(prefix + " Contador de infecciones curadas limpiado (" + size + " registros)");
            } else {
                curedInfectionsCount = new HashMap<>();
                Bukkit.getLogger().info(prefix + " Creado nuevo contador de infecciones curadas");
            }
            
            if (redMoonKillsCount != null) {
                int size = redMoonKillsCount.size();
                redMoonKillsCount.clear();
                Bukkit.getLogger().info(prefix + " Contador de asesinatos en Luna Roja limpiado (" + size + " registros)");
            } else {
                redMoonKillsCount = new HashMap<>();
                Bukkit.getLogger().info(prefix + " Creado nuevo contador de asesinatos en Luna Roja");
            }
            
            if (curedVillagers != null) {
                int size = curedVillagers.size();
                curedVillagers.clear();
                Bukkit.getLogger().info(prefix + " Lista de aldeanos curados limpiada (" + size + " registros)");
            } else {
                curedVillagers = new HashSet<>();
                Bukkit.getLogger().info(prefix + " Creada nueva lista de aldeanos curados");
            }
            
            if (witherKilledInRedMoon != null) {
                int size = witherKilledInRedMoon.size();
                witherKilledInRedMoon.clear();
                Bukkit.getLogger().info(prefix + " Lista de Withers eliminados limpiada (" + size + " registros)");
            } else {
                witherKilledInRedMoon = new HashSet<>();
                Bukkit.getLogger().info(prefix + " Creada nueva lista de Withers eliminados");
            }
            
            // Reiniciar contadores y estados
            nightCounter = 0;
            isRedMoonActive = false;
            Bukkit.getLogger().info(prefix + " Contadores y estados reiniciados correctamente");
            
            // Liberar referencias a tareas
            if (mainTask != null) {
                // Solo registrar, no cancelar (ya debería estar cancelada en stopEventTasks)
                Bukkit.getLogger().info(prefix + " Referencia a tarea principal liberada");
                mainTask = null;
            }
            
            if (endTask != null) {
                // Solo registrar, no cancelar (ya debería estar cancelada en stopEventTasks)
                Bukkit.getLogger().info(prefix + " Referencia a tarea de finalización liberada");
                endTask = null;
            }
            
            // Registrar finalización de limpieza
            Bukkit.getLogger().info(prefix + " Datos del evento limpiados correctamente");
        } catch (Exception e) {
            Bukkit.getLogger().severe(prefix + " Error al limpiar datos del evento: " + e.getMessage());
            e.printStackTrace();
            
            // Intentar inicializar estructuras de datos en caso de error
            try {
                Bukkit.getLogger().warning(prefix + " Intentando recuperación de emergencia...");
                
                infectedPlayers = new HashMap<>();
                curedInfectionsCount = new HashMap<>();
                redMoonKillsCount = new HashMap<>();
                curedVillagers = new HashSet<>();
                witherKilledInRedMoon = new HashSet<>();
                nightCounter = 0;
                isRedMoonActive = false;
                mainTask = null;
                endTask = null;
                
                Bukkit.getLogger().info(prefix + " Recuperación de emergencia completada");
            } catch (Exception ex) {
                Bukkit.getLogger().severe(prefix + " Error crítico durante la recuperación de emergencia: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    @Override
     public String getName() {
         return "Semana de los No Muertos";
     }
    
    /**
     * Verifica el tiempo en los mundos normales para activar/desactivar la Noche Roja
     * Se activa cada 3 noches y se desactiva durante el día
     */
    /**
     * Verifica el tiempo en los mundos normales para controlar el ciclo de la Noche Roja.
     * Este método se ejecuta periódicamente para detectar el inicio de la noche y activar
     * la Noche Roja cada 3 noches, así como desactivarla al amanecer.
     */
    private void checkTime() {
        // Verificar si el evento está activo y no está pausado
        if (!isActive || isPaused) {
            return;
        }
        
        try {
            // Solo verificar en mundos normales
            boolean foundNormalWorld = false;
            int worldsProcessed = 0;
            final int MAX_WORLDS_TO_PROCESS = 10; // Límite de mundos a procesar por ciclo
            
            // Verificar si la lista de mundos es nula
            List<World> worlds = Bukkit.getWorlds();
            if (worlds == null || worlds.isEmpty()) {
                Bukkit.getLogger().warning(prefix + " No se encontraron mundos disponibles para verificar el tiempo");
                return;
            }
            
            for (World world : worlds) {
                // Limitar el número de mundos procesados por ciclo
                if (worldsProcessed >= MAX_WORLDS_TO_PROCESS) {
                    Bukkit.getLogger().info(prefix + " Límite de procesamiento de mundos alcanzado ("+MAX_WORLDS_TO_PROCESS+")");
                    break;
                }
                
                if (world == null) {
                    continue;
                }
                
                try {
                    worldsProcessed++;
                    
                    if (world.getEnvironment() == World.Environment.NORMAL) {
                        foundNormalWorld = true;
                        long time = world.getTime();
                        
                        // Verificar si es de noche (13000-23000)
                        if (time >= 13000 && time <= 23000) {
                            // Verificar si es una nueva noche (inicio de la noche)
                            if (time >= 13000 && time <= 13100 && !isRedMoonActive) {
                                nightCounter++;
                                
                                // Informar sobre el contador de noches
                                if (nightCounter % 3 != 0) {
                                    Bukkit.getLogger().info(prefix + "Noche " + nightCounter + "/3 para la próxima Noche Roja");
                                    
                                    // Mensaje a los jugadores cada noche
                                    String mensaje = switch (nightCounter % 3) {
                                        case 1 -> "<&7gray>La luna comienza a cambiar... <gold>Faltan 2 noches para la Noche Roja.";
                                        case 2 -> "<&cred>La luna se torna rojiza... <&4dark_red>Falta 1 noche para la Noche Roja.";
                                        default -> "";
                                    };
                                    
                                    if (!mensaje.isEmpty()) {
                                        // Limitar el número de jugadores a los que se envía el mensaje
                                        int playersProcessed = 0;
                                        final int MAX_PLAYERS_TO_PROCESS = 50; // Límite de jugadores por ciclo
                                        
                                        List<Player> players = world.getPlayers();
                                        if (players != null && !players.isEmpty()) {
                                            for (Player player : players) {
                                                if (playersProcessed >= MAX_PLAYERS_TO_PROCESS) {
                                                    break;
                                                }
                                                
                                                if (player != null && player.isOnline()) {
                                                    try {
                                                        player.sendMessage(MM.toComponent(mensaje));
                                                        
                                                        // Efectos visuales y sonoros sutiles
                                                        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.5f, 0.5f);
                                                        
                                                        playersProcessed++;
                                                    } catch (Exception e) {
                                                        Bukkit.getLogger().warning(prefix + " Error al enviar mensaje a jugador " 
                                                            + player.getName() + ": " + e.getMessage());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Cada 3 noches, activar la luna roja
                                if (nightCounter % 3 == 0) {
                                    Bukkit.getLogger().info(prefix + "Activando Noche Roja (noche " + nightCounter + ")");
                                    activateRedMoon();
                                }
                            }
                        } else {
                            // Si es de día, desactivar la luna roja
                            if (isRedMoonActive) {
                                Bukkit.getLogger().info(prefix + "Desactivando Noche Roja (amanecer)");
                                deactivateRedMoon();
                            }
                        }
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning(prefix + " Error al verificar tiempo en mundo " 
                        + world.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Si no se encontró un mundo normal, registrar advertencia
            if (!foundNormalWorld) {
                Bukkit.getLogger().warning(prefix + "No se encontró ningún mundo normal para verificar el tiempo");
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe(prefix + " Error crítico al verificar el tiempo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Activa la Noche Roja, aumentando la velocidad y fuerza de los no-muertos
     */
    /**
     * Activa la Noche Roja, aumentando la velocidad y fuerza de los no-muertos
     * y aplicando efectos visuales y sonoros
     */
    private void activateRedMoon() {
        // Verificar si el evento está pausado o ya está activa la Luna Roja
        if (isPaused) {
            Bukkit.getLogger().info("No se puede activar la Noche Roja: el evento está pausado");
            return;
        }
        
        if (isRedMoonActive) {
            Bukkit.getLogger().info("La Noche Roja ya está activa");
            return;
        }
        
        try {
            // Activar la Noche Roja
            isRedMoonActive = true;
            
            // Verificar si el prefijo es válido
            String eventPrefix = prefix != null ? prefix : "&c[Semana No-Muerta]";
            
            // Anunciar el inicio de la Noche Roja
            Bukkit.broadcast(MM.toComponent(eventPrefix + " <red>¡La Noche Roja ha comenzado! Los no-muertos son más rápidos y las camas explotan."));
            Bukkit.broadcast(MM.toComponent(eventPrefix + " <red>¡Mata no-muertos durante este evento para obtener recompensas especiales!"));
            
            // Efectos de sonido globales para anunciar la Noche Roja
            int playersProcessed = 0;
            final int maxPlayersToProcess = 100; // Límite para evitar sobrecarga
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Verificar si el jugador es válido
                if (player == null || !player.isOnline()) {
                    continue;
                }
                
                // Limitar la cantidad de jugadores procesados
                if (playersProcessed >= maxPlayersToProcess) {
                    Bukkit.getLogger().warning("Límite de jugadores procesados alcanzado al aplicar efectos de Noche Roja");
                    break;
                }
                
                try {
                    // Efectos de sonido
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.8f);
                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.7f, 0.5f);
                    
                    // Efectos visuales
                    player.spawnParticle(Particle.CRIMSON_SPORE, player.getLocation().add(0, 1, 0), 50, 3, 3, 3, 0.1);
                    player.spawnParticle(Particle.DRIPPING_LAVA, player.getLocation().add(0, 2, 0), 20, 1, 1, 1, 0.05);
                    
                    // Mensaje personalizado
                    player.sendMessage(MM.toComponent(eventPrefix + " <red>¡Sientes un poder oscuro emanando de la luna!"));
                    
                    playersProcessed++;
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al aplicar efectos de Noche Roja al jugador " + player.getName() + ": " + e.getMessage());
                }
            }
            
            // Contador para limitar el procesamiento por mundo
            int totalProcessed = 0;
            final int maxProcessPerActivation = 500; // Límite para evitar lag
            int worldsProcessed = 0;
            final int maxWorldsToProcess = 10; // Límite de mundos a procesar
            
            // Aumentar velocidad de los no-muertos y asegurar que tengan fuerza
            for (World world : Bukkit.getWorlds()) {
                // Verificar si el mundo es válido
                if (world == null) {
                    continue;
                }
                
                // Limitar la cantidad de mundos procesados
                if (worldsProcessed >= maxWorldsToProcess) {
                    Bukkit.getLogger().warning("Límite de mundos procesados alcanzado durante activación de Noche Roja");
                    break;
                }
                
                try {
                    // Efectos visuales en el cielo solo en mundos normales
                    if (world.getEnvironment() == World.Environment.NORMAL) {
                        // Cambiar el color del cielo (efecto visual)
                        int playersInWorldProcessed = 0;
                        final int maxPlayersInWorldToProcess = 50; // Límite por mundo
                        
                        for (Player player : world.getPlayers()) {
                            if (player == null || !player.isOnline()) {
                                continue;
                            }
                            
                            if (playersInWorldProcessed >= maxPlayersInWorldToProcess) {
                                break;
                            }
                            
                            try {
                                player.sendMessage(MM.toComponent(eventPrefix + " <dark_red>El cielo se tiñe de rojo..."));
                                playersInWorldProcessed++;
                            } catch (Exception e) {
                                Bukkit.getLogger().warning("Error al enviar mensaje de cielo rojo al jugador " + player.getName() + ": " + e.getMessage());
                            }
                        }
                    }
                    
                    // Procesar entidades
                    int entitiesProcessed = 0;
                    final int maxEntitiesToProcess = 200; // Límite por mundo
                    
                    for (LivingEntity entity : world.getLivingEntities()) {
                        // Verificar límites globales y por mundo
                        if (totalProcessed >= maxProcessPerActivation || entitiesProcessed >= maxEntitiesToProcess) {
                            break; // Limitar el procesamiento para evitar lag
                        }
                        
                        if (entity == null) {
                            continue;
                        }
                        
                        try {
                            if (isUndead(entity)) {
                                // Aplicar aumento de velocidad
                                AttributeInstance speedAttribute = entity.getAttribute(Attribute.MOVEMENT_SPEED);
                                if (speedAttribute != null) {
                                    double currentValue = speedAttribute.getBaseValue();
                                    double maxValue = 0.5; // Valor máximo razonable
                                    
                                    // Evitar duplicar el boost si ya se aplicó
                                    if (currentValue < maxValue) {
                                        // Guardar valor original para restaurarlo después
                                        // Aplicar boost con límite máximo
                                        double newValue = Math.min(currentValue * 2, maxValue);
                                        speedAttribute.setBaseValue(newValue);
                                    }
                                }
                                
                                // Asegurar que tengan Fuerza II
                                if (!entity.hasPotionEffect(PotionEffectType.STRENGTH)) {
                                    entity.addPotionEffect(new PotionEffect(
                                        PotionEffectType.STRENGTH,
                                        Integer.MAX_VALUE,
                                        1, // Nivel II
                                        false,
                                        false,
                                        true
                                    ));
                                }
                                
                                // Efectos visuales para los no-muertos (solo para algunos para evitar sobrecarga)
                                if (Math.random() < 0.3) { // 30% de probabilidad para reducir partículas
                                    entity.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, 
                                        entity.getLocation().add(0, 1, 0), 
                                        5, 0.2, 0.2, 0.2, 0.05);
                                }
                                
                                entitiesProcessed++;
                                totalProcessed++;
                            }
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("Error al procesar entidad durante Noche Roja: " + e.getMessage());
                            continue;
                        }
                    }
                    
                    worldsProcessed++;
                    Bukkit.getLogger().info("Mundo " + world.getName() + " procesado: " + entitiesProcessed + " entidades");
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al procesar mundo " + world.getName() + " durante activación de Luna Roja: " + e.getMessage());
                }
            }
            
            Bukkit.getLogger().info("Noche Roja activada. Entidades procesadas: " + totalProcessed + " en " + worldsProcessed + " mundos");
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error crítico al activar la Noche Roja: " + e.getMessage());
            e.printStackTrace();
            
            // Restaurar estado en caso de error
            isRedMoonActive = false;
        }
    }
    
    /**
     * Desactiva la Noche Roja, restaurando la velocidad normal de los no-muertos
     */
    /**
     * Desactiva la Noche Roja, restaurando la velocidad normal de los no-muertos
     * y aplicando efectos visuales y sonoros de finalización
     */
    private void deactivateRedMoon() {
        // Verificar si el evento está pausado o si la Luna Roja no está activa
        if (isPaused) {
            Bukkit.getLogger().info("No se puede desactivar la Noche Roja: el evento está pausado");
            return;
        }
        
        if (!isRedMoonActive) {
            Bukkit.getLogger().info("La Noche Roja ya está desactivada");
            return;
        }
        
        try {
            // Desactivar la Noche Roja
            isRedMoonActive = false;
            
            // Verificar si el prefijo es válido
            String eventPrefix = prefix != null ? prefix : "&c[Semana No-Muerta]";
            
            // Anunciar el fin de la Noche Roja
            Bukkit.broadcast(MM.toComponent(eventPrefix + " <green>La Noche Roja ha terminado."));
            Bukkit.broadcast(MM.toComponent(eventPrefix + " <yellow>Los no-muertos vuelven a su estado normal."));
            
            // Efectos de sonido globales para anunciar el fin de la Noche Roja
            int playersProcessed = 0;
            final int maxPlayersToProcess = 100; // Límite para evitar sobrecarga
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Verificar si el jugador es válido
                if (player == null || !player.isOnline()) {
                    continue;
                }
                
                // Limitar la cantidad de jugadores procesados
                if (playersProcessed >= maxPlayersToProcess) {
                    Bukkit.getLogger().warning("Límite de jugadores procesados alcanzado al aplicar efectos de fin de Noche Roja");
                    break;
                }
                
                try {
                    // Efectos de sonido
                    player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f, 1.0f);
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.3f, 1.2f);
                    
                    // Efectos visuales
                    player.spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 30, 2, 2, 2, 0.05);
                    
                    // Mensaje personalizado
                    player.sendMessage(MM.toComponent(eventPrefix + " <green>La influencia de la luna roja se desvanece..."));
                    
                    // Mostrar estadísticas si el jugador mató no-muertos durante la Noche Roja
                    if (redMoonKillsCount != null && redMoonKillsCount.containsKey(player.getUniqueId())) {
                        int kills = redMoonKillsCount.get(player.getUniqueId());
                        if (kills > 0) {
                            player.sendMessage(MM.toComponent(eventPrefix + " <yellow>Has eliminado <red>" + kills + " <yellow>no-muertos durante la Noche Roja"));
                        }
                    }
                    
                    playersProcessed++;
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al aplicar efectos de fin de Noche Roja al jugador " + player.getName() + ": " + e.getMessage());
                }
            }
            
            // Contador para limitar el procesamiento por mundo
            int totalProcessed = 0;
            final int maxProcessPerDeactivation = 500; // Límite para evitar lag
            int worldsProcessed = 0;
            final int maxWorldsToProcess = 10; // Límite de mundos a procesar
            
            // Restaurar velocidad normal de los no-muertos
            for (World world : Bukkit.getWorlds()) {
                // Verificar si el mundo es válido
                if (world == null) {
                    continue;
                }
                
                // Limitar la cantidad de mundos procesados
                if (worldsProcessed >= maxWorldsToProcess) {
                    Bukkit.getLogger().warning("Límite de mundos procesados alcanzado durante desactivación de Noche Roja");
                    break;
                }
                
                try {
                    // Efectos visuales en el cielo solo en mundos normales
                    if (world.getEnvironment() == World.Environment.NORMAL) {
                        // Cambiar el color del cielo (efecto visual)
                        int playersInWorldProcessed = 0;
                        final int maxPlayersInWorldToProcess = 50; // Límite por mundo
                        
                        for (Player player : world.getPlayers()) {
                            if (player == null || !player.isOnline()) {
                                continue;
                            }
                            
                            if (playersInWorldProcessed >= maxPlayersInWorldToProcess) {
                                break;
                            }
                            
                            try {
                                player.sendMessage(MM.toComponent(eventPrefix + " <aqua>El cielo vuelve a la normalidad..."));
                                playersInWorldProcessed++;
                            } catch (Exception e) {
                                Bukkit.getLogger().warning("Error al enviar mensaje de cielo normal al jugador " + player.getName() + ": " + e.getMessage());
                            }
                        }
                    }
                    
                    // Procesar entidades
                    int entitiesProcessed = 0;
                    final int maxEntitiesToProcess = 200; // Límite por mundo
                    
                    for (LivingEntity entity : world.getLivingEntities()) {
                        // Verificar límites globales y por mundo
                        if (totalProcessed >= maxProcessPerDeactivation || entitiesProcessed >= maxEntitiesToProcess) {
                            break; // Limitar el procesamiento para evitar lag
                        }
                        
                        if (entity == null) {
                            continue;
                        }
                        
                        try {
                            if (isUndead(entity)) {
                                // Restaurar velocidad normal
                                AttributeInstance speedAttribute = entity.getAttribute(Attribute.MOVEMENT_SPEED);
                                if (speedAttribute != null) {
                                    double currentValue = speedAttribute.getBaseValue();
                                    double minValue = 0.1; // Valor mínimo razonable
                                    
                                    // Evitar reducir demasiado la velocidad
                                    if (currentValue > minValue) {
                                        // Reducir a la mitad pero no menos del mínimo
                                        double newValue = Math.max(currentValue / 2, minValue);
                                        speedAttribute.setBaseValue(newValue);
                                    }
                                }
                                
                                // Eliminar efecto de fuerza
                                if (entity.hasPotionEffect(PotionEffectType.STRENGTH)) {
                                    entity.removePotionEffect(PotionEffectType.STRENGTH);
                                }
                                
                                // Efectos visuales para los no-muertos (solo para algunos para evitar sobrecarga)
                                if (Math.random() < 0.3) { // 30% de probabilidad para reducir partículas
                                    entity.getWorld().spawnParticle(Particle.SMOKE, 
                                        entity.getLocation().add(0, 1, 0), 
                                        10, 0.2, 0.2, 0.2, 0.05);
                                }
                                
                                entitiesProcessed++;
                                totalProcessed++;
                            }
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("Error al procesar entidad durante desactivación de Noche Roja: " + e.getMessage());
                            continue;
                        }
                    }
                    
                    worldsProcessed++;
                    Bukkit.getLogger().info("Mundo " + world.getName() + " procesado para desactivación: " + entitiesProcessed + " entidades");
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al procesar mundo " + world.getName() + " durante desactivación de Luna Roja: " + e.getMessage());
                }
            }
            
            Bukkit.getLogger().info("Noche Roja desactivada. Entidades procesadas: " + totalProcessed + " en " + worldsProcessed + " mundos");
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error crítico al desactivar la Noche Roja: " + e.getMessage());
            e.printStackTrace();
            
            // Restaurar estado en caso de error para asegurar que se desactive
            isRedMoonActive = false;
        }
    }
    
    /**
     * Verifica y actualiza el estado de los jugadores infectados
     * Aplica efectos de veneno y evita que mueran por el veneno
     */
    private void checkInfectedPlayers() {
        if (!isActive || isPaused) {
            return;
        }
        
        try {
            // Crear una copia para evitar ConcurrentModificationException
            Map<UUID, Boolean> infectedCopy = new HashMap<>(infectedPlayers);
            
            // Contador para limitar el procesamiento por tick
            int processedCount = 0;
            final int maxProcessPerTick = 30; // Límite para evitar lag
            
            // Limpiar jugadores desconectados
            for (UUID playerId : new HashSet<>(infectedPlayers.keySet())) {
                Player player = Bukkit.getPlayer(playerId);
                if (player == null || !player.isOnline()) {
                    // No eliminar, solo marcar como no infectado para mantener estadísticas
                    infectedPlayers.put(playerId, false);
                }
            }
            
            // Verificar jugadores infectados
            for (Map.Entry<UUID, Boolean> entry : infectedCopy.entrySet()) {
                // Limitar procesamiento por tick
                if (processedCount >= maxProcessPerTick) {
                    break;
                }
                processedCount++;
                
                // Verificar si está infectado
                if (!entry.getValue()) {
                    continue;
                }
                
                // Obtener jugador
                UUID playerId = entry.getKey();
                Player player = Bukkit.getPlayer(playerId);
                
                // Verificar si el jugador está en línea
                if (player == null || !player.isOnline()) {
                    continue;
                }
                
                // Aplicar efecto de veneno que no mata
                if (!player.hasPotionEffect(PotionEffectType.POISON)) {
                    try {
                        player.addPotionEffect(new PotionEffect(
                            PotionEffectType.POISON, 
                            Integer.MAX_VALUE, 
                            0, // Nivel 1
                            false, 
                            true, 
                            true
                        ));
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("Error al aplicar efecto de veneno a " + player.getName() + ": " + e.getMessage());
                    }
                }
                
                // Asegurarse de que el veneno no mate al jugador
                if (player.getHealth() <= 1.5) { // Umbral un poco mayor para seguridad
                    player.removePotionEffect(PotionEffectType.POISON);
                    
                    // Mensaje ocasional para recordar al jugador que está infectado
                    if (Math.random() < 0.2) { // 20% de probabilidad
                        player.sendMessage(MM.toComponent("<red>La infección te está debilitando. Come una zanahoria dorada para curarte."));
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error en checkInfectedPlayers: " + e.getMessage());
        }
    }
    
    /**
     * Verifica la armadura de netherita de los jugadores y aplica efectos de wither
     * según la cantidad de piezas que lleven equipadas
     */
    private void checkNetheriteArmor() {
        if (!isActive || isPaused) {
            return;
        }
        
        try {
            // Contador para limitar el procesamiento por tick
            int processedCount = 0;
            final int maxProcessPerTick = 30; // Límite para evitar lag
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Limitar procesamiento por tick
                if (processedCount >= maxProcessPerTick) {
                    break;
                }
                processedCount++;
                
                // Verificar si el jugador es válido
                if (player == null || !player.isOnline() || player.isDead()) {
                    continue;
                }
                
                // Verificar si el jugador está en un mundo válido
                if (player.getWorld() == null) {
                    continue;
                }
                
                // Contar piezas de netherita
                int netheriteCount = 0;
                ItemStack[] armorContents = player.getInventory().getArmorContents();
                
                if (armorContents == null) {
                    continue;
                }
                
                for (ItemStack item : armorContents) {
                    if (item != null && item.getType() != null && item.getType().toString().contains("NETHERITE")) {
                        netheriteCount++;
                    }
                }
                
                // Aplicar efecto de wither según cantidad de piezas
                if (netheriteCount > 0) {
                    try {
                        int amplifier = netheriteCount - 1; // 0 para 1 pieza, 1 para 2 piezas, etc.
                        
                        // Limitar el nivel máximo del efecto
                        if (amplifier > 3) {
                            amplifier = 3;
                        }
                        
                        player.addPotionEffect(new PotionEffect(
                            PotionEffectType.WITHER, 
                            40, // 2 segundos
                            amplifier,
                            false,
                            true,
                            true
                        ));
                        
                        // Mensaje ocasional para informar al jugador
                        if (netheriteCount >= 3 && Math.random() < 0.1) { // 10% de probabilidad con 3+ piezas
                            player.sendMessage(MM.toComponent("<dark_purple>La netherita atrae la energía oscura de la luna roja..."));
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("Error al aplicar efecto de wither a " + player.getName() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error en checkNetheriteArmor: " + e.getMessage());
        }
    }
    

    /**
     * Maneja el evento de aparición de entidades
     * Aplica efectos a entidades no-muertas y mejoras durante la luna roja
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!isActive || isPaused) {
            return;
        }
        
        try {
            // Verificar si el evento es válido
            if (event == null || event.isCancelled()) {
                return;
            }
            
            Entity entity = event.getEntity();
            
            // Verificar si la entidad es válida
            if (entity == null) {
                return;
            }
            
            // Verificar si el mundo es válido
            if (entity.getWorld() == null) {
                return;
            }
            
            // Limitar la cantidad de entidades no-muertas por chunk para evitar lag
            if (isUndead(entity)) {
                Chunk chunk = entity.getLocation().getChunk();
                int undeadCount = 0;
                
                for (Entity e : chunk.getEntities()) {
                    if (isUndead(e)) {
                        undeadCount++;
                    }
                }
                
                // Si hay demasiadas entidades no-muertas en el chunk, cancelar el spawn
                if (undeadCount > 15) { // Límite razonable para evitar lag
                    event.setCancelled(true);
                    return;
                }
            }
            
            // Verificar si la entidad es no-muerta y es una entidad viva (para aplicar efectos)
            if (isUndead(entity) && entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                
                try {
                    // Aplicar efecto de Fuerza II (duración infinita)
                    livingEntity.addPotionEffect(new PotionEffect(
                        PotionEffectType.STRENGTH,
                        Integer.MAX_VALUE,  // Duración infinita
                        1,                  // Nivel II (basado en 0, así que 1 = nivel II)
                        false,              // Sin partículas ambientales
                        false,              // Sin partículas
                        true                // Mostrar icono
                    ));
                    
                    // Si es noche de luna roja, también aplicar aumento de velocidad
                    if (isRedMoonActive) {
                        if (livingEntity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                            double baseSpeed = livingEntity.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
                            livingEntity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(baseSpeed * 2);
                        }
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al aplicar efectos a entidad no-muerta: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error en onEntitySpawn: " + e.getMessage());
        }
    }


    /**
     * Maneja el evento de daño entre entidades
     * Gestiona infecciones, muertes en luna roja y recompensas por matar al Wither
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isActive || isPaused) {
            return;
        }
        
        try {
            // Verificar si el evento es válido
            if (event == null || event.isCancelled()) {
                return;
            }
            
            Entity damager = event.getDamager();
            Entity victim = event.getEntity();
            
            // Verificar si las entidades son válidas
            if (damager == null || victim == null) {
                return;
            }
            
            // Manejar proyectiles
            if (damager instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) damager).getShooter();
                if (shooter instanceof Entity) {
                    damager = (Entity) shooter;
                } else {
                    return; // No es una entidad quien disparó
                }
            }
            
            // Verificar si el mundo es válido
            if (damager.getWorld() == null || victim.getWorld() == null) {
                return;
            }
            
            // Infectar jugadores si son golpeados por zombies (con probabilidad)
            if (isUndead(damager) && damager.getType() == EntityType.ZOMBIE && victim instanceof Player) {
                Player player = (Player) victim;
                
                // Verificar si el jugador ya está infectado
                if (!infectedPlayers.getOrDefault(player.getUniqueId(), false)) {
                    // 25% de probabilidad de infección por golpe
                    if (Math.random() < 0.25) {
                        try {
                            infectedPlayers.put(player.getUniqueId(), true);
                            player.sendMessage(MM.toComponent("<red>¡Has sido infectado! Come una zanahoria o manzana dorada para curarte."));
                            
                            // Efecto de sonido para la infección
                            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.5f);
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("Error al infectar jugador: " + e.getMessage());
                        }
                    }
                }
            }
            
            // Registrar muertes en noche roja
            if (isRedMoonActive && damager instanceof Player && victim instanceof Player) {
                Player killer = (Player) damager;
                UUID killerId = killer.getUniqueId();
                
                if (killerId == null) {
                    return;
                }
                
                try {
                    // Incrementar contador de muertes
                    int currentKills = redMoonKillsCount.getOrDefault(killerId, 0);
                    redMoonKillsCount.put(killerId, currentKills + 1);
                    
                    // Verificar desafío
                    if (redMoonKillsCount.get(killerId) == 3) {
                        killer.sendMessage(MM.toComponent("<green>¡Desafío completado! Has matado a 3 jugadores en la Noche Roja."));
                        killer.sendMessage(MM.toComponent("<gray>Recompensa: <u>Tag 'Necroestallido'"));
                        
                        // Efecto visual y sonoro para la recompensa
                        killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        killer.spawnParticle(Particle.TOTEM_OF_UNDYING, killer.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
                        
                        // Aquí se aplicaría el tag (comentado para mantener compatibilidad)
                        //eternalAPI.setTag(killer, new Tag("necroestallido", "necroestallido", "&x&1&7&c&e&2&9N&x&1&6&c&9&3&7e&x&1&4&c&4&4&5c&x&1&3&c&0&5&3r&x&1&2&b&b&6&0o&x&1&0&b&6&6&ee&x&0&f&b&1&7&cs&x&0&e&a&d&8&at&x&0&d&a&8&9&8a&x&0&b&a&3&a&6l&x&0&a&9&e&b&3l&x&0&9&9&a&c&1i&x&0&7&9&5&c&fd&x&0&6&9&0&d&do")); //&#49bf40N&#46bd49e&#43bc53c&#41ba5cr&#3eb865o&#3bb76fe&#38b578s&#36b381t&#33b18aa&#30b094l&#2dae9dl&#2baca6i&#28abb0d&#25a9b9o
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al procesar muerte en luna roja: " + e.getMessage());
                }
            }
            
            // Registrar muerte del Wither en noche roja
            if (isRedMoonActive && victim.getType() == EntityType.WITHER && damager instanceof Player) {
                Player killer = (Player) damager;
                UUID killerId = killer.getUniqueId();
                
                if (killerId == null) {
                    return;
                }
                
                try {
                    World world = killer.getWorld();
                    
                    if (world != null && world.getEnvironment() == World.Environment.NORMAL) {
                        // Verificar si ya ha recibido la recompensa
                        if (!witherKilledInRedMoon.contains(killerId)) {
                            witherKilledInRedMoon.add(killerId);
                            killer.sendMessage(MM.toComponent("&a¡Desafío legendario completado! Has derrotado al Wither en la Noche Roja."));
                            
                            // Aumentar corazón máximo
                            AttributeInstance maxHealthAttr = killer.getAttribute(Attribute.MAX_HEALTH);
                            if (maxHealthAttr != null) {
                                double currentMaxHealth = maxHealthAttr.getValue();
                                maxHealthAttr.setBaseValue(currentMaxHealth + 2.0);
                                killer.sendMessage(MM.toComponent("&6Recompensa: +1 corazón máximo"));
                                
                                // Efectos visuales y sonoros para la recompensa
                                killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
                                killer.spawnParticle(Particle.HEART, killer.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                            }
                        }
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al procesar muerte del Wither: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error en onEntityDamage: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el evento de consumo de items por parte de los jugadores
     * Gestiona la curación de infecciones y recompensas por curar múltiples infecciones
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (!isActive || isPaused) {
            return;
        }
        
        try {
            // Verificar si el evento es válido
            if (event == null || event.isCancelled()) {
                return;
            }
            
            Player player = event.getPlayer();
            ItemStack item = event.getItem();
            
            // Verificar si el jugador y el item son válidos
            if (player == null || item == null) {
                return;
            }
            
            // Verificar si el jugador está en línea
            if (!player.isOnline()) {
                return;
            }
            
            UUID playerId = player.getUniqueId();
            if (playerId == null) {
                return;
            }
            
            // Curar infección con zanahoria dorada o manzana dorada
            if ((item.getType() == Material.GOLDEN_CARROT || item.getType() == Material.GOLDEN_APPLE) && 
                infectedPlayers.getOrDefault(playerId, false)) {
                
                try {
                    // Marcar como curado
                    infectedPlayers.put(playerId, false);
                    
                    // Eliminar efecto de veneno
                    if (player.hasPotionEffect(PotionEffectType.POISON)) {
                        player.removePotionEffect(PotionEffectType.POISON);
                    }
                    
                    // Mensaje y efectos
                    player.sendMessage(MM.toComponent("&a¡Te has curado de la infección!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                    player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
                    
                    // Registrar para el desafío
                    int curedCount = curedInfectionsCount.getOrDefault(playerId, 0) + 1;
                    curedInfectionsCount.put(playerId, curedCount);
                    
                    // Mostrar progreso ocasionalmente
                    if (curedCount < 10 && curedCount % 2 == 0) {
                        player.sendMessage(MM.toComponent("&7Has curado &e" + curedCount + "&7/10 infecciones."));
                    }
                    
                    // Verificar desafío completado
                    if (curedCount == 10) {
                        player.sendMessage(MM.toComponent("&a¡Desafío completado! Has curado 10 infecciones."));
                        
                        // Aumentar corazón máximo
                        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
                        if (maxHealthAttr != null) {
                            double currentMaxHealth = maxHealthAttr.getValue();
                            maxHealthAttr.setBaseValue(currentMaxHealth + 2.0);
                            player.sendMessage(MM.toComponent("&6Recompensa: +1 corazón máximo"));
                            
                            // Efectos para la recompensa
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
                            player.spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                        }
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al curar infección: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error en onPlayerConsume: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el evento de transformación de entidades
     * Detecta la curación de aldeanos zombificados y otorga recompensas
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTransform(EntityTransformEvent event) {
        if (!isActive || isPaused) {
            return;
        }
        
        try {
            // Verificar si el evento es válido
            if (event == null || event.isCancelled()) {
                return;
            }
            
            Entity originalEntity = event.getEntity();
            Entity transformedEntity = event.getTransformedEntity();
            
            // Verificar si las entidades son válidas
            if (originalEntity == null || transformedEntity == null) {
                return;
            }
            
            // Verificar si el mundo es válido
            if (originalEntity.getWorld() == null) {
                return;
            }
            
            // Detectar curación de aldeanos zombificados
            if (originalEntity.getType() == EntityType.ZOMBIE_VILLAGER && 
                transformedEntity.getType() == EntityType.VILLAGER) {
                
                Location transformLocation = originalEntity.getLocation();
                if (transformLocation == null) {
                    return;
                }
                
                // Efectos visuales en la ubicación de la transformación
                World world = transformLocation.getWorld();
                if (world != null) {
                    world.spawnParticle(Particle.HAPPY_VILLAGER, transformLocation.add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
                    world.playSound(transformLocation, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
                }
                
                // Buscar al jugador responsable (esto es aproximado, podría mejorarse)
                Player closestPlayer = null;
                double closestDistance = Double.MAX_VALUE;
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Verificar si el jugador es válido
                    if (player == null || !player.isOnline() || player.isDead()) {
                        continue;
                    }
                    
                    // Verificar si el jugador está en el mismo mundo
                    if (player.getWorld() != world) {
                        continue;
                    }
                    
                    try {
                        double distance = player.getLocation().distance(transformLocation);
                        
                        // Encontrar al jugador más cercano dentro del rango
                        if (distance <= 15 && distance < closestDistance) {
                            closestDistance = distance;
                            closestPlayer = player;
                        }
                    } catch (Exception e) {
                        // Ignorar errores de cálculo de distancia
                        continue;
                    }
                }
                
                // Otorgar recompensa al jugador más cercano
                if (closestPlayer != null) {
                    UUID playerId = closestPlayer.getUniqueId();
                    
                    // Verificar si el jugador ya ha recibido la recompensa
                    if (!curedVillagers.contains(playerId)) {
                        try {
                            curedVillagers.add(playerId);
                            closestPlayer.sendMessage(MM.toComponent("&a¡Desafío completado! Has curado a un aldeano zombificado."));
                            closestPlayer.sendMessage(MM.toComponent("&6Recompensa: Encantamiento First Strike"));
                            
                            // Efectos para la recompensa
                            closestPlayer.playSound(closestPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            closestPlayer.spawnParticle(Particle.ENCHANT, closestPlayer.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
                            
                            // Aquí se aplicaría el encantamiento (comentado para mantener compatibilidad)
                            // Implementación pendiente
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("Error al otorgar recompensa por curar aldeano: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error en onEntityTransform: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el evento de jugador intentando dormir en una cama
     * Durante la Noche Roja, las camas explotan al intentar usarlas
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (!isActive || isPaused || !isRedMoonActive) {
            return;
        }
        
        try {
            // Verificar si el evento es válido
            if (event == null || event.isCancelled()) {
                return;
            }
            
            // Verificar si el jugador es válido
            Player player = event.getPlayer();
            if (player == null || !player.isOnline()) {
                return;
            }
            
            // Verificar si la cama es válida
            Block bed = event.getBed();
            if (bed == null) {
                return;
            }
            
            // Verificar si el mundo es válido
            World world = player.getWorld();
            if (world == null) {
                return;
            }
            
            // Cancelar el evento de dormir
            event.setCancelled(true);
            
            // Enviar mensaje al jugador
            player.sendMessage(MM.toComponent("&c¡No puedes dormir durante la Noche Roja! &4¡Tu cama ha explotado!"));
            
            // Efectos de sonido y visuales
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1.5f);
            
            // Crear explosión con un pequeño retraso para efectos dramáticos
            Location bedLocation = bed.getLocation();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    // Verificar si el mundo sigue siendo válido
                    if (world != null && bedLocation != null) {
                        world.spawnParticle(Particle.FLAME, bedLocation.add(0.5, 0.5, 0.5), 30, 0.5, 0.5, 0.5, 0.05);
                        world.createExplosion(bedLocation, 2.0f, false, true);
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al crear explosión de cama: " + e.getMessage());
                }
            }, 10L); // Medio segundo de retraso
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error en onPlayerBedEnter: " + e.getMessage());
        }
    }
    
    private boolean isUndead(Entity entity) {
        return undeadEntities.contains(entity.getType());
    }
    
    /**
     * Otorga recompensas finales a los jugadores basadas en sus logros durante el evento
     * Las recompensas se escalan según el nivel de participación y éxito en los desafíos
     */
    private void giveRewards() {
        try {
            // Verificar si el plugin es válido
            if (plugin == null) {
                Bukkit.getLogger().severe(prefix + " No se pueden dar recompensas: plugin no válido");
                return;
            }
            
            // Verificar si el evento está activo o pausado
            if (!isActive && !isPaused) {
                Bukkit.getLogger().warning(prefix + " No se pueden dar recompensas: el evento no está activo ni pausado");
                return;
            }
            
            // Inicializar estructuras de datos si son nulas
            if (infectedPlayers == null) {
                infectedPlayers = new HashMap<>();
                Bukkit.getLogger().warning(prefix + " Se inicializó la lista de jugadores infectados durante giveRewards");
            }
            
            if (curedInfectionsCount == null) {
                curedInfectionsCount = new HashMap<>();
                Bukkit.getLogger().warning(prefix + " Se inicializó el contador de curaciones durante giveRewards");
            }
            
            if (redMoonKillsCount == null) {
                redMoonKillsCount = new HashMap<>();
                Bukkit.getLogger().warning(prefix + " Se inicializó el contador de eliminaciones durante giveRewards");
            }
            
            if (curedVillagers == null) {
                curedVillagers = new HashSet<>();
                Bukkit.getLogger().warning(prefix + " Se inicializó el registro de aldeanos curados durante giveRewards");
            }
            
            if (witherKilledInRedMoon == null) {
                witherKilledInRedMoon = new HashSet<>();
                Bukkit.getLogger().warning(prefix + " Se inicializó el registro de Withers eliminados durante giveRewards");
            }
            
            // Verificar si hay jugadores que hayan participado
            if (infectedPlayers.isEmpty()) {
                Bukkit.getLogger().info(prefix + " No hay jugadores infectados para dar recompensas");
                return;
            }
            
            // Registrar estadísticas antes de dar recompensas
            Bukkit.getLogger().info(prefix + " Estadísticas del evento antes de dar recompensas:");
            Bukkit.getLogger().info(prefix + " - Jugadores infectados: " + infectedPlayers.size());
            Bukkit.getLogger().info(prefix + " - Total de curaciones realizadas: " + getTotalCuredCount());
            Bukkit.getLogger().info(prefix + " - Total de eliminaciones en Luna Roja: " + getTotalRedMoonKills());
            Bukkit.getLogger().info(prefix + " - Aldeanos curados: " + curedVillagers.size());
            Bukkit.getLogger().info(prefix + " - Withers eliminados en Luna Roja: " + witherKilledInRedMoon.size());
            
            // Contador para limitar el procesamiento
            int playersProcessed = 0;
            final int maxPlayersToProcess = 100; // Límite para evitar sobrecarga
            
            // Crear una copia de las claves para evitar ConcurrentModificationException
            Set<UUID> playerIds = new HashSet<>(infectedPlayers.keySet());
            
            // Procesar cada jugador que haya participado en el evento
            for (UUID playerId : playerIds) {
                // Verificar si el ID es válido
                if (playerId == null) {
                    Bukkit.getLogger().warning(prefix + " Se encontró un UUID nulo en la lista de jugadores infectados");
                    continue;
                }
                
                // Limitar la cantidad de jugadores procesados
                if (playersProcessed >= maxPlayersToProcess) {
                    Bukkit.getLogger().warning(prefix + " Límite de jugadores procesados alcanzado al dar recompensas (" + maxPlayersToProcess + ")");
                    break;
                }
                
                try {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player == null || !player.isOnline()) {
                        // Guardar recompensa para cuando el jugador se conecte
                        saveOfflinePlayerReward(playerId);
                        continue;
                    }
                    
                    // Calcular recompensas basadas en logros
                    int curedCount = curedInfectionsCount.getOrDefault(playerId, 0);
                    int redMoonKills = redMoonKillsCount.getOrDefault(playerId, 0);
                    boolean curedVillager = curedVillagers != null && curedVillagers.contains(playerId);
                    boolean killedWither = witherKilledInRedMoon != null && witherKilledInRedMoon.contains(playerId);
                    
                    // Recompensas base
                    int emeralds = 5;
                    int experience = 500;
                    
                    // Bonificaciones por curar infecciones
                    if (curedCount >= 10) {
                        emeralds += 15;
                        experience += 1000;
                        sendSafeMessage(player, prefix + " <gold>¡Bonificación por curar a 10 o más jugadores!");
                    } else if (curedCount >= 5) {
                        emeralds += 8;
                        experience += 500;
                        sendSafeMessage(player, prefix + " <gold>¡Bonificación por curar a 5 o más jugadores!");
                    } else if (curedCount > 0) {
                        emeralds += 3;
                        experience += 200;
                        sendSafeMessage(player, prefix + " <gold>¡Bonificación por curar jugadores!");
                    }
                    
                    // Bonificaciones por matar durante la Luna Roja
                    if (redMoonKills >= 50) {
                        emeralds += 20;
                        experience += 2000;
                        sendSafeMessage(player, prefix + " <red>¡Bonificación por 50+ eliminaciones durante la Luna Roja!");
                    } else if (redMoonKills >= 20) {
                        emeralds += 10;
                        experience += 1000;
                        sendSafeMessage(player, prefix + " <red>¡Bonificación por 20+ eliminaciones durante la Luna Roja!");
                    } else if (redMoonKills > 0) {
                        emeralds += 5;
                        experience += 500;
                        sendSafeMessage(player, prefix + " <red>¡Bonificación por eliminaciones durante la Luna Roja!");
                    }
                    
                    // Bonificaciones especiales
                    if (curedVillager) {
                        emeralds += 10;
                        experience += 1000;
                        sendSafeMessage(player, prefix + " <green>¡Bonificación por curar a un aldeano zombificado!");
                    }
                    
                    if (killedWither) {
                        emeralds += 25;
                        experience += 3000;
                        sendSafeMessage(player, prefix + " <dark_purple>¡Bonificación por derrotar al Wither durante la Luna Roja!");
                    }
                    
                    // Entregar recompensas
                    givePlayerRewards(player, emeralds, experience);
                    playersProcessed++;
                    
                } catch (Exception e) {
                    Bukkit.getLogger().warning(prefix + " Error al dar recompensas al jugador " + playerId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            Bukkit.getLogger().info(prefix + " Recompensas otorgadas a " + playersProcessed + " jugadores");
            
        } catch (Exception e) {
            Bukkit.getLogger().severe(prefix + " Error crítico al dar recompensas: " + e.getMessage());
            e.printStackTrace();
            
            // Intentar recuperación de emergencia
            try {
                Bukkit.getLogger().info(prefix + " Intentando recuperación de emergencia tras error en recompensas...");
                // Asegurar que las estructuras de datos estén inicializadas para evitar NPEs futuros
                if (infectedPlayers == null) infectedPlayers = new HashMap<>();
                if (curedInfectionsCount == null) curedInfectionsCount = new HashMap<>();
                if (redMoonKillsCount == null) redMoonKillsCount = new HashMap<>();
                if (curedVillagers == null) curedVillagers = new HashSet<>();
                if (witherKilledInRedMoon == null) witherKilledInRedMoon = new HashSet<>();
            } catch (Exception ex) {
                Bukkit.getLogger().severe(prefix + " Error en recuperación de emergencia: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Obtiene el total de curaciones realizadas por todos los jugadores
     * @return Número total de curaciones
     */
    private int getTotalCuredCount() {
        if (curedInfectionsCount == null) return 0;
        return curedInfectionsCount.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Obtiene el total de eliminaciones en Luna Roja por todos los jugadores
     * @return Número total de eliminaciones
     */
    private int getTotalRedMoonKills() {
        if (redMoonKillsCount == null) return 0;
        return redMoonKillsCount.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Guarda las recompensas para un jugador desconectado
     * @param playerId UUID del jugador
     */
    private void saveOfflinePlayerReward(UUID playerId) {
        try {
            // Verificar si el plugin es válido
            if (plugin == null) {
                Bukkit.getLogger().severe("[UndeadWeek] No se pueden guardar recompensas offline: plugin no válido");
                return;
            }
            
            // Verificar si el ID del jugador es válido
            if (playerId == null) {
                Bukkit.getLogger().warning("[UndeadWeek] Intento de guardar recompensa para un jugador con UUID nulo");
                return;
            }
            
            // Verificar si las estructuras de datos son válidas
            if (curedInfectionsCount == null || redMoonKillsCount == null || 
                curedVillagers == null || witherKilledInRedMoon == null) {
                
                Bukkit.getLogger().warning("[UndeadWeek] Estructuras de datos no inicializadas al guardar recompensa offline");
                
                // Inicializar estructuras faltantes
                if (curedInfectionsCount == null) curedInfectionsCount = new HashMap<>();
                if (redMoonKillsCount == null) redMoonKillsCount = new HashMap<>();
                if (curedVillagers == null) curedVillagers = new HashSet<>();
                if (witherKilledInRedMoon == null) witherKilledInRedMoon = new HashSet<>();
            }
            
            // Calcular recompensas basadas en logros
            int curedCount = curedInfectionsCount.getOrDefault(playerId, 0);
            int redMoonKills = redMoonKillsCount.getOrDefault(playerId, 0);
            boolean curedVillager = curedVillagers.contains(playerId);
            boolean killedWither = witherKilledInRedMoon.contains(playerId);
            
            // Recompensas base
            int emeralds = 5;
            int experience = 500;
            
            // Calcular bonificaciones (mismo cálculo que en giveRewards)
            if (curedCount >= 10) {
                emeralds += 15;
                experience += 1000;
            } else if (curedCount >= 5) {
                emeralds += 8;
                experience += 500;
            } else if (curedCount > 0) {
                emeralds += 3;
                experience += 200;
            }
            
            if (redMoonKills >= 50) {
                emeralds += 20;
                experience += 2000;
            } else if (redMoonKills >= 20) {
                emeralds += 10;
                experience += 1000;
            } else if (redMoonKills > 0) {
                emeralds += 5;
                experience += 500;
            }
            
            if (curedVillager) {
                emeralds += 10;
                experience += 1000;
            }
            
            if (killedWither) {
                emeralds += 25;
                experience += 3000;
            }
            
            // Aquí se implementaría la lógica para guardar las recompensas
            // en una base de datos o archivo de configuración
            // Por ahora solo registramos en el log
            Bukkit.getLogger().info(String.format(
                "[UndeadWeek] Recompensa guardada para jugador offline %s: %d esmeraldas, %d experiencia", 
                playerId, emeralds, experience
            ));
            
        } catch (Exception e) {
            Bukkit.getLogger().warning("[UndeadWeek] Error al guardar recompensa para jugador offline " + playerId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Entrega recompensas a un jugador
     * @param player Jugador que recibirá las recompensas
     * @param emeralds Cantidad de esmeraldas a entregar
     * @param experience Cantidad de experiencia a entregar
     */
    /**
     * Envía un mensaje de forma segura a un jugador, manejando posibles errores
     * @param player Jugador que recibirá el mensaje
     * @param message Mensaje a enviar (con formato MiniMessage)
     */
    private void sendSafeMessage(Player player, String message) {
        try {
            if (player == null || !player.isOnline() || message == null) {
                return;
            }
            
            // Verificar si MM (MiniMessage) está disponible
            if (MM.getMiniMessage() != null) {
                player.sendMessage(MM.toComponent(message));
            } else {
                // Fallback a mensaje sin formato si MM no está disponible
                String plainMessage = message.replaceAll("<[^>]+>", "");
                player.sendMessage(plainMessage);
                Bukkit.getLogger().warning("[UndeadWeek] MiniMessage no disponible, enviando mensaje sin formato");
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[UndeadWeek] Error al enviar mensaje a " + player.getName() + ": " + e.getMessage());
        }
    }
    
    private void givePlayerRewards(Player player, int emeralds, int experience) {
        try {
            // Verificar si el plugin es válido
            if (plugin == null) {
                Bukkit.getLogger().severe("[UndeadWeek] No se pueden entregar recompensas: plugin no válido");
                return;
            }
            
            // Verificar si el jugador es válido y está en línea
            if (player == null) {
                Bukkit.getLogger().warning("[UndeadWeek] Intento de entregar recompensas a un jugador nulo");
                return;
            }
            
            if (!player.isOnline()) {
                Bukkit.getLogger().warning("[UndeadWeek] Intento de entregar recompensas a un jugador desconectado: " + player.getName());
                saveOfflinePlayerReward(player.getUniqueId());
                return;
            }
            
            // Verificar si el mundo del jugador es válido
            World world = player.getWorld();
            if (world == null) {
                Bukkit.getLogger().warning("[UndeadWeek] El jugador " + player.getName() + " está en un mundo nulo");
                saveOfflinePlayerReward(player.getUniqueId());
                return;
            }
            
            // Verificar si el inventario del jugador es válido
            if (player.getInventory() == null) {
                Bukkit.getLogger().warning("[UndeadWeek] El jugador " + player.getName() + " tiene un inventario nulo");
                saveOfflinePlayerReward(player.getUniqueId());
                return;
            }
            
            // Validar cantidades de recompensa
            if (emeralds <= 0) {
                Bukkit.getLogger().warning("[UndeadWeek] Cantidad de esmeraldas inválida: " + emeralds + ", ajustando a 1");
                emeralds = 1;
            }
            
            if (experience <= 0) {
                Bukkit.getLogger().warning("[UndeadWeek] Cantidad de experiencia inválida: " + experience + ", ajustando a 100");
                experience = 100;
            }
            
            // Limitar cantidades máximas para evitar problemas
            if (emeralds > 64) {
                Bukkit.getLogger().info("[UndeadWeek] Limitando cantidad de esmeraldas de " + emeralds + " a 64");
                emeralds = 64;
            }
            
            if (experience > 10000) {
                Bukkit.getLogger().info("[UndeadWeek] Limitando cantidad de experiencia de " + experience + " a 10000");
                experience = 10000;
            }
            
            try {
                // Dar esmeraldas
                ItemStack emeraldStack = new ItemStack(Material.EMERALD, emeralds);
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(emeraldStack);
                
                // Si no hay espacio en el inventario, soltar los items en el suelo
                if (!leftover.isEmpty()) {
                    for (ItemStack item : leftover.values()) {
                        if (item != null) {
                            world.dropItemNaturally(player.getLocation(), item);
                        }
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("[UndeadWeek] Error al entregar esmeraldas a " + player.getName() + ": " + e.getMessage());
            }
            
            try {
                // Dar experiencia
                player.giveExp(experience);
            } catch (Exception e) {
                Bukkit.getLogger().warning("[UndeadWeek] Error al entregar experiencia a " + player.getName() + ": " + e.getMessage());
            }
            
            try {
                // Efectos visuales y sonoros
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.1);
                
                // Efectos adicionales para hacer la recompensa más especial
                player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1.5, 0), 15, 0.5, 0.5, 0.5, 0.1);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
                
                // Mensaje de recompensa con prefijo verificado
                String safePrefix = (prefix != null) ? prefix : "&c[Semana No-Muerta]";
                sendSafeMessage(player, safePrefix + " <green>¡Has recibido <gold>" + emeralds + " esmeraldas</gold> y <yellow>" + experience + " puntos de experiencia</yellow> como recompensa!");
            } catch (Exception e) {
                Bukkit.getLogger().warning("[UndeadWeek] Error al mostrar efectos de recompensa a " + player.getName() + ": " + e.getMessage());
            }
            
            Bukkit.getLogger().info(String.format(
                "[UndeadWeek] Recompensas entregadas a %s: %d esmeraldas, %d experiencia", 
                player.getName(), emeralds, experience
            ));
            
        } catch (Exception e) {
            String playerName = (player != null) ? player.getName() : "desconocido";
            Bukkit.getLogger().warning("[UndeadWeek] Error crítico al entregar recompensas al jugador " + playerName + ": " + e.getMessage());
            e.printStackTrace();
            
            // Intentar guardar la recompensa para más tarde si es posible
            try {
                if (player != null) {
                    saveOfflinePlayerReward(player.getUniqueId());
                }
            } catch (Exception ex) {
                Bukkit.getLogger().severe("[UndeadWeek] Error al intentar guardar recompensa tras fallo: " + ex.getMessage());
            }
        }
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public boolean isRedMoonActive() {
        return isRedMoonActive;
    }


    /**
     * Pausa todas las tareas del evento y restaura temporalmente
     * el ciclo día/noche normal y elimina efectos de jugadores infectados
     */
    @Override
    protected void pauseEventTasks() {
        try {
            // Verificar si el plugin es válido
            if (plugin == null) {
                Bukkit.getLogger().severe("[UndeadWeek] No se pueden pausar las tareas: plugin no válido");
                return;
            }
            
            // Verificar si el evento está activo
            if (!isActive) {
                Bukkit.getLogger().warning("[UndeadWeek] No se pueden pausar las tareas: el evento no está activo");
                return;
            }
            
            // Verificar si el evento ya está pausado
            if (isPaused) {
                Bukkit.getLogger().warning("[UndeadWeek] El evento ya está pausado");
                return;
            }
            
            Bukkit.getLogger().info("[UndeadWeek] Iniciando pausa del evento...");
            
            // Establecer el estado de pausa
            isPaused = true;
            
            // Cancelar la tarea principal
            if (mainTask != null) {
                try {
                    if (!mainTask.isCancelled()) {
                        mainTask.cancel();
                        Bukkit.getLogger().info("[UndeadWeek] Tarea principal pausada correctamente (ID: " + mainTask.getTaskId() + ")");
                    } else {
                        Bukkit.getLogger().info("[UndeadWeek] La tarea principal ya estaba cancelada");
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[UndeadWeek] Error al pausar la tarea principal: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    mainTask = null;
                }
            } else {
                Bukkit.getLogger().info("[UndeadWeek] No había tarea principal activa para pausar");
            }
            
            
            // Restaurar temporalmente el ciclo día/noche normal
            if (isRedMoonActive) {
                try {
                    deactivateRedMoon();
                    Bukkit.getLogger().info("[UndeadWeek] Luna Roja desactivada durante la pausa");
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[UndeadWeek] Error al desactivar Luna Roja: " + e.getMessage());
                    isRedMoonActive = false; // Forzar desactivación en caso de error
                }
            }
            
            // Restaurar ciclo día/noche normal con límite de procesamiento
            List<World> worlds = Bukkit.getWorlds();
            if (worlds != null && !worlds.isEmpty()) {
                int worldsProcessed = 0;
                final int MAX_WORLDS_TO_PROCESS = 10; // Límite para evitar sobrecarga
                
                for (World world : worlds) {
                    // Verificar si el mundo es válido
                    if (world == null) continue;
                    
                    try {
                        if (world.getEnvironment() == World.Environment.NORMAL) {
                            world.setTime(0); // Establecer a día
                            worldsProcessed++;
                            Bukkit.getLogger().info("[UndeadWeek] Ciclo día/noche restaurado en mundo: " + world.getName());
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("[UndeadWeek] Error al restaurar el ciclo día/noche en el mundo " + world.getName() + ": " + e.getMessage());
                    }
                    
                    // Limitar el procesamiento para evitar lag
                    if (worldsProcessed >= MAX_WORLDS_TO_PROCESS) {
                        Bukkit.getLogger().info("[UndeadWeek] Límite de mundos procesados alcanzado (" + MAX_WORLDS_TO_PROCESS + ")");
                        break;
                    }
                }
                
                Bukkit.getLogger().info("[UndeadWeek] " + worldsProcessed + " mundos procesados durante la pausa");
            } else {
                Bukkit.getLogger().warning("[UndeadWeek] No se encontraron mundos para procesar");
            }
            
            // Eliminar temporalmente los efectos de los jugadores infectados
            if (infectedPlayers != null && !infectedPlayers.isEmpty()) {
                // Crear una copia de las claves para evitar ConcurrentModificationException
                Set<UUID> playerIds = new HashSet<>(infectedPlayers.keySet());
                int playersProcessed = 0;
                final int MAX_PLAYERS_TO_PROCESS = 100; // Aumentado para procesar más jugadores
                
                for (UUID playerId : playerIds) {
                    if (playerId == null) continue;
                    
                    try {
                        Player player = Bukkit.getPlayer(playerId);
                        if (player != null && player.isOnline()) {
                            // Eliminar todos los efectos negativos
                            player.removePotionEffect(PotionEffectType.POISON);
                            player.removePotionEffect(PotionEffectType.HUNGER);
                            player.removePotionEffect(PotionEffectType.NAUSEA);
                            player.removePotionEffect(PotionEffectType.BLINDNESS);
                            player.removePotionEffect(PotionEffectType.WEAKNESS);
                            player.removePotionEffect(PotionEffectType.SLOWNESS);
                            
                            // Enviar mensaje con verificación de MiniMessage
                            try {
                                if (MM.getMiniMessage() != null) {
                                    player.sendMessage(MM.toComponent(prefix + " <yellow>Tus síntomas de infección han desaparecido temporalmente"));
                                } else {
                                    player.sendMessage("§e[Semana No-Muerta] Tus síntomas de infección han desaparecido temporalmente");
                                }
                            } catch (Exception msgEx) {
                                Bukkit.getLogger().warning("[UndeadWeek] Error al enviar mensaje al jugador " + player.getName() + ": " + msgEx.getMessage());
                                player.sendMessage("§e[Semana No-Muerta] Tus síntomas de infección han desaparecido temporalmente");
                            }
                            
                            playersProcessed++;
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("[UndeadWeek] Error al eliminar efectos del jugador " + playerId + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    // Limitar el procesamiento para evitar lag
                    if (playersProcessed >= MAX_PLAYERS_TO_PROCESS) {
                        Bukkit.getLogger().info("[UndeadWeek] Límite de jugadores procesados alcanzado (" + MAX_PLAYERS_TO_PROCESS + ")");
                        break;
                    }
                }
                
                Bukkit.getLogger().info("[UndeadWeek] " + playersProcessed + " jugadores infectados procesados durante la pausa");
            } else {
                Bukkit.getLogger().info("[UndeadWeek] No hay jugadores infectados para procesar");
            }
            
            // Anunciar la pausa del evento con verificación de MiniMessage
            try {
                if (MM.getMiniMessage() != null) {
                    Bukkit.broadcast(MM.toComponent(prefix + " <yellow>El evento ha sido pausado temporalmente"));
                } else {
                    Bukkit.broadcast(MM.toComponent(prefix + " El evento ha sido pausado temporalmente"));
                }
            } catch (Exception msgEx) {
                Bukkit.getLogger().warning("[UndeadWeek] Error al anunciar pausa del evento: " + msgEx.getMessage());
                // Intentar con método alternativo
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player != null) {
                        player.sendMessage(MM.toComponent(prefix + " El evento ha sido pausado temporalmente"));
                    }
                }
            }
            
            Bukkit.getLogger().info("[UndeadWeek] Evento pausado correctamente");
            
        } catch (Exception e) {
            Bukkit.getLogger().severe("[UndeadWeek] Error crítico al pausar el evento: " + e.getMessage());
            e.printStackTrace();
            
            // Intentar recuperación de emergencia
            try {
                // Asegurar que el estado de pausa esté establecido
                isPaused = true;
                
                // Asegurar que la tarea principal esté cancelada
                if (mainTask != null && !mainTask.isCancelled()) {
                    mainTask.cancel();
                    mainTask = null;
                }
                
                // Desactivar Luna Roja si está activa
                isRedMoonActive = false;
                
                Bukkit.getLogger().info("[UndeadWeek] Recuperación de emergencia completada tras error en pausa");
            } catch (Exception ex) {
                Bukkit.getLogger().severe("[UndeadWeek] Error al intentar recuperación de emergencia: " + ex.getMessage());
            }
        }
    }

    /**
     * Reanuda todas las tareas del evento y reaplica los efectos
     * a los jugadores infectados
     */
    @Override
    protected void resumeEventTasks() {
        try {
            // Verificar si el plugin es válido
            if (plugin == null) {
                Bukkit.getLogger().severe("[UndeadWeek] No se pueden reanudar las tareas: plugin no válido");
                return;
            }
            
            // Verificar si el evento está activo
            if (!isActive) {
                Bukkit.getLogger().warning("[UndeadWeek] No se pueden reanudar las tareas: el evento no está activo");
                return;
            }
            
            // Verificar si el evento está pausado
            if (!isPaused) {
                Bukkit.getLogger().warning("[UndeadWeek] No se pueden reanudar las tareas: el evento no está pausado");
                return;
            }
            
            Bukkit.getLogger().info("[UndeadWeek] Iniciando reanudación del evento...");
            
            // Cambiar el estado de pausa
            isPaused = false;
            
            // Reiniciar la tarea principal
            try {
                boolean taskStarted = startMainTask();
                if (taskStarted) {
                    Bukkit.getLogger().info("[UndeadWeek] Tarea principal reanudada correctamente");
                } else {
                    Bukkit.getLogger().warning("[UndeadWeek] No se pudo reanudar la tarea principal");
                    // Intentar iniciar la tarea principal de forma alternativa
                    mainTask = Bukkit.getScheduler().runTaskTimer(plugin, this::runMainTask, 20L, 20L);
                    Bukkit.getLogger().info("[UndeadWeek] Tarea principal iniciada de forma alternativa (ID: " + mainTask.getTaskId() + ")");
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("[UndeadWeek] Error al reiniciar la tarea principal: " + e.getMessage());
                e.printStackTrace();
                // Intentar iniciar la tarea principal de forma alternativa
                try {
                    mainTask = Bukkit.getScheduler().runTaskTimer(plugin, this::runMainTask, 20L, 20L);
                    Bukkit.getLogger().info("[UndeadWeek] Tarea principal iniciada de forma alternativa tras error (ID: " + mainTask.getTaskId() + ")");
                } catch (Exception ex) {
                    Bukkit.getLogger().severe("[UndeadWeek] Error crítico al intentar iniciar tarea alternativa: " + ex.getMessage());
                }
            }
            
            // Reaplicar efectos a jugadores infectados
            if (infectedPlayers != null && !infectedPlayers.isEmpty()) {
                // Crear una copia de las claves para evitar ConcurrentModificationException
                Set<UUID> playerIds = new HashSet<>(infectedPlayers.keySet());
                int playersProcessed = 0;
                final int MAX_PLAYERS_TO_PROCESS = 100; // Aumentado para procesar más jugadores
                
                Bukkit.getLogger().info("[UndeadWeek] Reaplicando efectos a " + playerIds.size() + " jugadores infectados...");
                
                for (UUID playerId : playerIds) {
                    if (playerId == null) continue;
                    
                    try {
                        Player player = Bukkit.getPlayer(playerId);
                        if (player != null && player.isOnline()) {
                            // Verificar si el jugador está en un mundo válido
                            if (player.getWorld() == null) {
                                Bukkit.getLogger().warning("[UndeadWeek] Jugador " + player.getName() + " está en un mundo nulo, omitiendo");
                                continue;
                            }
                            
                            // Reaplicar efectos de infección
                            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 60, 0, false, true, true));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 60, 0, false, true, true));
                            
                            // Efectos visuales y sonoros
                            try {
                                player.spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                                player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 0.5f, 1.0f);
                            } catch (Exception effectEx) {
                                Bukkit.getLogger().warning("[UndeadWeek] Error al aplicar efectos visuales/sonoros a " + player.getName() + ": " + effectEx.getMessage());
                            }
                            
                            // Mensaje al jugador con verificación de MiniMessage
                            try {
                                if (MM.getMiniMessage() != null) {
                                    player.sendMessage(MM.toComponent(prefix + " <red>Tus síntomas de infección han regresado"));
                                } else {
                                    player.sendMessage("§c[Semana No-Muerta] Tus síntomas de infección han regresado");
                                }
                            } catch (Exception msgEx) {
                                Bukkit.getLogger().warning("[UndeadWeek] Error al enviar mensaje al jugador " + player.getName() + ": " + msgEx.getMessage());
                                player.sendMessage("§c[Semana No-Muerta] Tus síntomas de infección han regresado");
                            }
                            
                            playersProcessed++;
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("[UndeadWeek] Error al reaplicar efectos al jugador " + playerId + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    // Limitar el procesamiento para evitar lag
                    if (playersProcessed >= MAX_PLAYERS_TO_PROCESS) {
                        Bukkit.getLogger().info("[UndeadWeek] Límite de jugadores procesados alcanzado (" + MAX_PLAYERS_TO_PROCESS + ")");
                        break;
                    }
                }
                
                Bukkit.getLogger().info("[UndeadWeek] " + playersProcessed + " jugadores infectados procesados durante la reanudación");
            } else {
                Bukkit.getLogger().info("[UndeadWeek] No hay jugadores infectados para procesar");
            }
            
            // Verificar si es de noche y si debe activarse la Luna Roja
            try {
                checkTime();
                Bukkit.getLogger().info("[UndeadWeek] Verificación de tiempo completada");
            } catch (Exception e) {
                Bukkit.getLogger().warning("[UndeadWeek] Error al verificar el tiempo: " + e.getMessage());
            }
            
            // Anunciar la reanudación del evento con verificación de MiniMessage
            try {
                if (MM.getMiniMessage() != null) {
                    Bukkit.broadcast(MM.toComponent(prefix + " <yellow>El evento ha sido reanudado"));
                } else {
                    Bukkit.broadcast(MM.toComponent(prefix + " El evento ha sido reanudado"));
                }
            } catch (Exception msgEx) {
                Bukkit.getLogger().warning("[UndeadWeek] Error al anunciar reanudación del evento: " + msgEx.getMessage());
                // Intentar con método alternativo
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player != null) {
                        player.sendMessage("§e[Semana No-Muerta] El evento ha sido reanudado");
                    }
                }
            }
            
            Bukkit.getLogger().info("[UndeadWeek] Evento reanudado correctamente");
            
        } catch (Exception e) {
            Bukkit.getLogger().severe("[UndeadWeek] Error crítico al reanudar el evento: " + e.getMessage());
            e.printStackTrace();
            
            // Intentar recuperación de emergencia
            try {
                // Asegurar que el estado de pausa esté desactivado
                isPaused = false;
                
                // Intentar iniciar la tarea principal si es null
                if (mainTask == null) {
                    mainTask = Bukkit.getScheduler().runTaskTimer(plugin, this::runMainTask, 20L, 20L);
                    Bukkit.getLogger().info("[UndeadWeek] Tarea principal iniciada durante recuperación de emergencia");
                }
                
                Bukkit.getLogger().info("[UndeadWeek] Recuperación de emergencia completada tras error en reanudación");
            } catch (Exception ex) {
                Bukkit.getLogger().severe("[UndeadWeek] Error al intentar recuperación de emergencia: " + ex.getMessage());
            }
        }
    }

    /**
     * Inicia la tarea principal del evento que verifica periódicamente
     * el estado de los jugadores infectados, el tiempo para la Luna Roja,
     * y aplica efectos de armadura de netherita
     * 
     * @return true si la tarea se inició correctamente, false en caso contrario
     */
    /**
     * Método que ejecuta las tareas principales del evento en cada tick programado.
     * Este método es utilizado como referencia de método (method reference) en las recuperaciones
     * de emergencia y alternativas de inicio de tareas.
     */
    private void runMainTask() {
        try {
            // Verificar si el evento está activo y no pausado
            if (!isActive || isPaused) {
                return;
            }
            
            // Verificar el tiempo para la Luna Roja
            checkTime();
            
            // Verificar jugadores infectados
            checkInfectedPlayers();
            
            // Verificar armadura de netherita
            checkNetheriteArmor();
            
        } catch (Exception e) {
            // Capturar cualquier excepción para evitar que la tarea se detenga
            Bukkit.getLogger().severe(prefix + " Error en la ejecución de runMainTask: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean startMainTask() {
        // Verificar si el evento está activo
        if (!isActive) {
            Bukkit.getLogger().warning(prefix + " No se puede iniciar la tarea principal: el evento no está activo");
            return false;
        }
        
        // Verificar si el evento está pausado
        if (isPaused) {
            Bukkit.getLogger().warning(prefix + " No se puede iniciar la tarea principal: el evento está pausado");
            return false;
        }
        
        // Verificar si el plugin es válido
        if (plugin == null) {
            Bukkit.getLogger().severe(prefix + " No se pudo iniciar la tarea principal: plugin es null");
            return false;
        }
        
        // Verificar si las estructuras de datos están inicializadas
        if (infectedPlayers == null) {
            infectedPlayers = new HashMap<>();
            Bukkit.getLogger().info(prefix + " Inicializado mapa de jugadores infectados en startMainTask");
        }
        
        if (curedInfectionsCount == null) {
            curedInfectionsCount = new HashMap<>();
            Bukkit.getLogger().info(prefix + " Inicializado contador de curaciones en startMainTask");
        }
        
        if (redMoonKillsCount == null) {
            redMoonKillsCount = new HashMap<>();
            Bukkit.getLogger().info(prefix + " Inicializado contador de muertes en Luna Roja en startMainTask");
        }
        
        // Cancelar tarea existente si hay
        if (mainTask != null) {
            try {
                if (!mainTask.isCancelled()) {
                    mainTask.cancel();
                    Bukkit.getLogger().info(prefix + " Tarea principal anterior cancelada correctamente");
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning(prefix + " Error al cancelar la tarea principal anterior: " + e.getMessage());
                e.printStackTrace();
            } finally {
                mainTask = null;
            }
        }
        
        try {
            // Iniciar la tarea principal con verificaciones periódicas
            mainTask = new BukkitRunnable() {
                private int consecutiveErrors = 0;
                private final int MAX_CONSECUTIVE_ERRORS = 5;
                
                @Override
                public void run() {
                    try {
                        // Verificar si el evento está activo y no pausado
                        if (!isActive || isPaused) {
                            return;
                        }
                        
                        // Verificar el tiempo para la Luna Roja
                        checkTime();
                        
                        // Verificar jugadores infectados
                        checkInfectedPlayers();
                        
                        // Verificar armadura de netherita
                        checkNetheriteArmor();
                        
                        // Resetear contador de errores si todo va bien
                        if (consecutiveErrors > 0) {
                            consecutiveErrors = 0;
                            Bukkit.getLogger().info(prefix + " Tarea principal recuperada después de errores previos");
                        }
                    } catch (Exception e) {
                        // Capturar cualquier excepción para evitar que la tarea se detenga
                        consecutiveErrors++;
                        Bukkit.getLogger().severe(prefix + " Error en la ejecución de la tarea principal (" + 
                            consecutiveErrors + "/" + MAX_CONSECUTIVE_ERRORS + "): " + e.getMessage());
                        e.printStackTrace();
                        
                        // Si hay demasiados errores consecutivos, cancelar la tarea
                        if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                            Bukkit.getLogger().severe(prefix + " Demasiados errores consecutivos, cancelando tarea principal");
                            this.cancel();
                            mainTask = null;
                            
                            // Intentar reiniciar la tarea después de un tiempo
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (isActive && !isPaused) {
                                        Bukkit.getLogger().info(prefix + " Intentando reiniciar la tarea principal después de errores");
                                        startMainTask();
                                    }
                                }
                            }.runTaskLater(plugin, 20L * 60); // Intentar reiniciar después de 1 minuto
                        }
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L * 30); // Verificar cada 30 segundos (20 ticks = 1 segundo)
            
            // Verificar si la tarea se inició correctamente
            if (mainTask != null && mainTask.getTaskId() > 0) {
                Bukkit.getLogger().info(prefix + " Tarea principal iniciada correctamente con ID: " + mainTask.getTaskId());
                return true;
            } else {
                Bukkit.getLogger().warning(prefix + " La tarea principal se creó pero puede no estar funcionando correctamente");
                return false;
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe(prefix + " Error crítico al iniciar la tarea principal: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inicia el evento de la semana de los no muertos
     * Configura las tareas periódicas, inicializa los datos y anuncia el inicio
     * 
     * @return true si el evento se inició correctamente, false en caso contrario
     */
    @Override
    public void start() {
        // Verificar si el evento ya está activo
        if (isActive) {
            Bukkit.getLogger().info("UndeadWeek ya está activo, no se puede iniciar de nuevo");
            return;
        }
        
        // Si está pausado, reanudar en lugar de iniciar
        if (isPaused) {
            Bukkit.getLogger().info("UndeadWeek está pausado, reanudando en lugar de iniciar");
            resume();
            return;
        }
        
        // Verificar si el plugin es válido
        if (plugin == null) {
            Bukkit.getLogger().severe("No se pudo iniciar UndeadWeek: plugin es null");
            return;
        }
        
        try {
            // Cancelar tarea existente si hay
            if (mainTask != null) {
                try {
                    if (!mainTask.isCancelled()) {
                        mainTask.cancel();
                        Bukkit.getLogger().info("Tarea existente cancelada correctamente");
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al cancelar tarea existente: " + e.getMessage());
                } finally {
                    mainTask = null;
                }
            }
            
            // Inicializar colecciones de datos si son nulas
            if (infectedPlayers == null) {
                infectedPlayers = new HashMap<>();
            }
            
            if (curedInfectionsCount == null) {
                curedInfectionsCount = new HashMap<>();
            }
            
            if (redMoonKillsCount == null) {
                redMoonKillsCount = new HashMap<>();
            }
            
            if (witherKilledInRedMoon == null) {
                witherKilledInRedMoon = new HashSet<>();
            }
            
            if (curedVillagers == null) {
                curedVillagers = new HashSet<>();
            }
            
            // Inicializar datos del evento
            isActive = true;
            isPaused = false;
            nightCounter = 0;
            isRedMoonActive = false;
            
            // Iniciar la tarea principal que se ejecuta periódicamente
            mainTask = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        // Verificar si el evento está activo y no pausado
                        if (!isActive || isPaused) {
                            return;
                        }
                        
                        // Ejecutar verificaciones periódicas
                        checkTime();
                        checkInfectedPlayers();
                        checkNetheriteArmor();
                    } catch (Exception e) {
                        // Capturar excepciones para evitar que la tarea se detenga
                        Bukkit.getLogger().severe("Error en la tarea principal de UndeadWeek: " + e.getMessage());
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L); // Cada segundo (20 ticks)
            
            // Verificar si la tarea se inició correctamente
            if (mainTask == null || mainTask.getTaskId() <= 0) {
                throw new RuntimeException("No se pudo iniciar la tarea principal");
            }
            
            // Programar el fin del evento si la duración es válida
            if (duration > 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            if (isActive) {
                                stop();
                                Bukkit.getLogger().info("Evento UndeadWeek finalizado automáticamente después de " + duration + " segundos");
                            }
                        } catch (Exception e) {
                            Bukkit.getLogger().severe("Error al finalizar automáticamente el evento: " + e.getMessage());
                        }
                    }
                }.runTaskLater(plugin, duration * 20L);
                Bukkit.getLogger().info("Fin del evento programado para dentro de " + duration + " segundos");
            }
            
            // Anunciar inicio del evento
            announceEventStart();
            
            // Registrar en el log
            Bukkit.getLogger().info("UndeadWeek iniciado correctamente con ID de tarea: " + mainTask.getTaskId());
            return;
        } catch (Exception e) {
            // Restaurar estado en caso de error
            isActive = false;
            isPaused = false;
            
            // Cancelar tarea si se creó pero hubo error después
            if (mainTask != null && !mainTask.isCancelled()) {
                mainTask.cancel();
                mainTask = null;
            }
            
            // Registrar el error
            Bukkit.getLogger().severe("Error al iniciar UndeadWeek: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    /**
     * Checks if a player has completed a specific challenge
     * @param playerId The UUID of the player
     * @param challengeId The ID of the challenge
     * @return true if the challenge is completed, false otherwise
     */
    public boolean hasChallengeCompleted(UUID playerId, String challengeId) {
        try {
            // Verificar parámetros de entrada
            if (playerId == null) {
                Bukkit.getLogger().warning("Se intentó verificar un desafío con UUID de jugador nulo");
                return false;
            }
            
            if (challengeId == null || challengeId.isEmpty()) {
                Bukkit.getLogger().warning("Se intentó verificar un desafío con ID nulo o vacío para el jugador: " + playerId);
                return false;
            }
            
            // Verificar si el evento está activo
            if (!isActive) {
                return false;
            }
            
            // Verificar el tipo de desafío
            switch (challengeId) {
                case "cure_infection":
                    // Jugador ha curado su infección 10 veces
                    int cures = curedInfectionsCount.getOrDefault(playerId, 0);
                    return cures >= 10;
                case "cure_villager":
                    // Jugador ha curado a un aldeano zombificado
                    return curedVillagers != null && curedVillagers.contains(playerId);
                case "red_moon_kills":
                    // Jugador ha matado 10 no-muertos durante la Luna Roja
                    int kills = redMoonKillsCount.getOrDefault(playerId, 0);
                    return kills >= 10;
                case "wither_red_moon":
                    // Jugador ha matado al Wither durante la Luna Roja
                    return witherKilledInRedMoon != null && witherKilledInRedMoon.contains(playerId);
                default:
                    Bukkit.getLogger().warning("Desafío desconocido: " + challengeId + " para el jugador: " + playerId);
                    return false;
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error al verificar el desafío " + challengeId + " para el jugador " + playerId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Inicia las tareas programadas del evento UndeadWeek.
     * Configura la tarea principal y programa el fin automático del evento.
     */
    @Override
    protected void startEventTasks() {
        try {
            // Verificar si el plugin es válido
            if (plugin == null) {
                Bukkit.getLogger().severe(prefix + " No se pueden iniciar las tareas del evento: plugin es null");
                return;
            }
            
            // Verificar si el evento está activo
            if (!isActive) {
                Bukkit.getLogger().warning(prefix + " No se pueden iniciar las tareas: el evento no está activo");
                return;
            }
            
            // Verificar si el evento está pausado
            if (isPaused) {
                Bukkit.getLogger().warning(prefix + " No se pueden iniciar las tareas: el evento está pausado");
                return;
            }
            
            // Verificar si la duración es válida
            if (duration <= 0) {
                Bukkit.getLogger().warning(prefix + " La duración del evento es inválida: " + duration + ". Estableciendo duración predeterminada de 7 días");
                duration = 7 * 24 * 60 * 60; // 7 días en segundos
            }
            
            // Inicializar estructuras de datos si son nulas
            if (infectedPlayers == null) {
                infectedPlayers = new HashMap<>();
                Bukkit.getLogger().info(prefix + " Inicializado mapa de jugadores infectados");
            }
            
            if (curedInfectionsCount == null) {
                curedInfectionsCount = new HashMap<>();
                Bukkit.getLogger().info(prefix + " Inicializado contador de curaciones");
            }
            
            if (redMoonKillsCount == null) {
                redMoonKillsCount = new HashMap<>();
                Bukkit.getLogger().info(prefix + " Inicializado contador de muertes en Luna Roja");
            }
            
            // Iniciar la tarea principal
            boolean taskStarted = startMainTask();
            if (taskStarted) {
                Bukkit.getLogger().info(prefix + " Tarea principal iniciada correctamente");
            } else {
                Bukkit.getLogger().warning(prefix + " No se pudo iniciar la tarea principal");
                return; // No continuar si la tarea principal no se pudo iniciar
            }
            
            // Programar el fin del evento con manejo de errores
            BukkitTask endTask = null;
            try {
                endTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            Bukkit.getLogger().info(prefix + " Finalizando evento automáticamente después de " + duration + " segundos");
                            stop();
                            Bukkit.getLogger().info(prefix + " Evento finalizado automáticamente con éxito");
                        } catch (Exception e) {
                            Bukkit.getLogger().severe(prefix + " Error crítico al detener el evento: " + e.getMessage());
                            e.printStackTrace();
                            // Intentar detener el evento de forma segura
                            try {
                                isActive = false;
                                isPaused = false;
                                stopEventTasks();
                                Bukkit.getLogger().info(prefix + " Evento detenido de forma segura tras error");
                            } catch (Exception ex) {
                                Bukkit.getLogger().severe(prefix + " Error fatal al intentar detener el evento de forma segura: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        }
                    }
                }.runTaskLater(plugin, duration * 20L); // Convertir segundos a ticks
            } catch (Exception e) {
                Bukkit.getLogger().severe(prefix + " Error al programar la finalización del evento: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Registrar la tarea de finalización
            if (endTask != null && endTask.getTaskId() > 0) {
                Bukkit.getLogger().info(prefix + " Fin del evento programado para dentro de " + duration + " segundos (ID: " + endTask.getTaskId() + ")");
                // Guardar referencia a la tarea para poder cancelarla si es necesario
                this.endTask = endTask;
            } else {
                Bukkit.getLogger().warning(prefix + " No se pudo programar el fin del evento");
            }
            
            // Registrar inicio exitoso de tareas
            Bukkit.getLogger().info(prefix + " Tareas del evento iniciadas correctamente");
        } catch (Exception e) {
            Bukkit.getLogger().severe(prefix + " Error crítico al iniciar las tareas del evento: " + e.getMessage());
            e.printStackTrace();
            
            // Intentar limpiar recursos en caso de error
            try {
                if (mainTask != null) {
                    mainTask.cancel();
                    mainTask = null;
                }
                if (this.endTask != null) {
                    this.endTask.cancel();
                    this.endTask = null;
                }
                Bukkit.getLogger().info(prefix + " Recursos liberados tras error en inicio de tareas");
            } catch (Exception ex) {
                Bukkit.getLogger().severe(prefix + " Error al liberar recursos tras fallo: " + ex.getMessage());
            }
        }
    }

    /**
     * Anuncia el inicio del evento a todos los jugadores
     * Incluye efectos visuales, sonoros y lista de desafíos disponibles
     */
    @Override
    protected void announceEventStart() {
        try {
            // Verificar si el evento está activo
            if (!isActive) {
                Bukkit.getLogger().warning("No se puede anunciar el inicio: el evento no está activo");
                return;
            }
            
            // Verificar si el prefijo es válido
            String eventPrefix = prefix != null ? prefix : "&c[Semana No-Muerta]";
            
            // Anunciar el inicio del evento
            Bukkit.broadcast(MM.toComponent(eventPrefix + " <red>Las hordas de no-muertos dominan el mundo..."));
            Bukkit.broadcast(MM.toComponent(eventPrefix + " <red>¡Cuidado con la infección! Puede propagarse por mordeduras de zombies."));
            
            // Efectos de sonido globales para anunciar el inicio del evento
            int playersProcessed = 0;
            final int maxPlayersToProcess = 100; // Límite para evitar sobrecarga
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Verificar si el jugador es válido
                if (player == null || !player.isOnline()) {
                    continue;
                }
                
                // Limitar la cantidad de jugadores procesados
                if (playersProcessed >= maxPlayersToProcess) {
                    Bukkit.getLogger().warning("Límite de jugadores procesados alcanzado al aplicar efectos de inicio");
                    break;
                }
                
                try {
                    // Efectos de sonido
                    player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.5f);
                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.5f, 1.0f);
                    
                    // Efectos visuales
                    player.spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 50, 3, 3, 3, 0.1);
                    
                    // Mensaje personalizado
                    player.sendMessage(MM.toComponent(eventPrefix + " <red>¡Sientes un escalofrío recorrer tu espalda!"));
                    
                    playersProcessed++;
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al aplicar efectos de inicio al jugador " + player.getName() + ": " + e.getMessage());
                }
            }
            
            // Anunciar los desafíos disponibles
            Bukkit.broadcast(MM.toComponent(eventPrefix + " <yellow>Desafíos disponibles:"));
            Bukkit.broadcast(MM.toComponent("<gray>- <white>Curar tu infección 10 veces <gray>(Recompensa: Encantamiento Curse of Undead)"));
            Bukkit.broadcast(MM.toComponent("<gray>- <white>Curar a 5 aldeano zombificado <gray>(Recompensa: <gray><u>Tag</u> \"Dr. Zomboss\")"));
            Bukkit.broadcast(MM.toComponent("<gray>- <white>Matar 10 no-muertos durante la Noche Roja <gray>(Recompensa: +1 corazón)"));
            Bukkit.broadcast(MM.toComponent("<gray>- <white>Matar al Wither durante la Noche Roja <gray>(Recompensa: +1 corazón)"));
            
            // Registrar en el log
            Bukkit.getLogger().info("Evento UndeadWeek anunciado correctamente a " + playersProcessed + " jugadores");
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error al anunciar el inicio del evento UndeadWeek: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Anuncia el fin del evento a todos los jugadores
     * Incluye efectos visuales y sonoros, y mensajes de finalización
     */
    @Override
    protected void announceEventEnd() {
        try {
            // Verificar si el prefijo es válido
            String eventPrefix = prefix != null ? prefix : "&c[Semana No-Muerta]";
            
            // Anunciar el fin del evento
            Bukkit.broadcast(MM.toComponent(eventPrefix + " <green>¡La amenaza de los no-muertos ha sido contenida!"));
            Bukkit.broadcast(MM.toComponent(eventPrefix + " <yellow>El mundo vuelve a la normalidad... por ahora."));
            
            // Efectos de sonido y visuales para todos los jugadores
            int playersProcessed = 0;
            final int maxPlayersToProcess = 100; // Límite para evitar sobrecarga
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Verificar si el jugador es válido
                if (player == null || !player.isOnline()) {
                    continue;
                }
                
                // Limitar la cantidad de jugadores procesados
                if (playersProcessed >= maxPlayersToProcess) {
                    Bukkit.getLogger().warning("Límite de jugadores procesados alcanzado al aplicar efectos de finalización");
                    break;
                }
                
                try {
                    // Efectos de sonido de victoria
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 0.7f);
                    
                    // Efectos visuales de celebración
                    player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 50, 3, 3, 3, 0.1);
                    player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 2, 0), 20, 1, 1, 1, 0.2);
                    
                    // Mensaje personalizado para el jugador
                    player.sendMessage(MM.toComponent(eventPrefix + " <green>¡Has sobrevivido a la Semana No-Muerta!"));
                    
                    // Mostrar estadísticas personales si el jugador participó
                    if (infectedPlayers != null && infectedPlayers.containsKey(player.getUniqueId())) {
                        int curedCount = curedInfectionsCount.getOrDefault(player.getUniqueId(), 0);
                        int redMoonKills = redMoonKillsCount.getOrDefault(player.getUniqueId(), 0);
                        
                        player.sendMessage(MM.toComponent(eventPrefix + " <yellow>Tus estadísticas:"));
                        player.sendMessage(MM.toComponent("<gray>- <white>Infecciones curadas: <gold>" + curedCount));
                        player.sendMessage(MM.toComponent("<gray>- <white>Eliminaciones en Luna Roja: <red>" + redMoonKills));
                    }
                    
                    playersProcessed++;
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error al aplicar efectos de finalización al jugador " + player.getName() + ": " + e.getMessage());
                }
            }
            
            // Registrar en el log
            Bukkit.getLogger().info("Evento UndeadWeek finalizado correctamente. Efectos aplicados a " + playersProcessed + " jugadores");
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error al anunciar el fin del evento UndeadWeek: " + e.getMessage());
            e.printStackTrace();
        }
    }
}