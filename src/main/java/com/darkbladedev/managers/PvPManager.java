package com.darkbladedev.managers;

import com.darkbladedev.utils.MM;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor global de PvP que maneja características independientes de eventos semanales.
 * 
 * Características implementadas:
 * - Anti-Farmeo de Corazones PvP: Reduce corazones obtenidos después de 3 kills al mismo jugador
 * - Protección para Nuevos: Inmunidad PvP de 3 horas para jugadores con 5 corazones
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class PvPManager implements Listener {
    
    private static final Logger logger = LoggerFactory.getLogger(PvPManager.class);
    
    // === CONSTANTES ===
    private static final int KILL_LIMIT_BEFORE_REDUCTION = 3;
    private static final long ANTI_FARM_COOLDOWN_MS = 24 * 60 * 60 * 1000L; // 24 horas
    private static final long NEW_PLAYER_IMMUNITY_MS = 3 * 60 * 60 * 1000L; // 3 horas
    private static final double NEW_PLAYER_HEALTH_THRESHOLD = 10.0; // 5 corazones = 10 HP
    
    // === SISTEMA ANTI-FARMEO PVP ===
    // Mapa que rastrea kills entre jugadores: Killer UUID -> (Victim UUID -> Kill Count)
    private final Map<UUID, Map<UUID, Integer>> pvpKillTracker = new ConcurrentHashMap<>();
    // Mapa que rastrea el cooldown de 24h para cada par de jugadores: Killer UUID -> (Victim UUID -> Timestamp)
    private final Map<UUID, Map<UUID, Long>> pvpFarmCooldown = new ConcurrentHashMap<>();
    
    // === SISTEMA PROTECCIÓN NUEVOS ===
    // Mapa que rastrea cuando un jugador nuevo obtuvo inmunidad PvP: Player UUID -> Timestamp
    private final Map<UUID, Long> newPlayerImmunity = new ConcurrentHashMap<>();
    
    private final JavaPlugin plugin;
    
    public PvPManager(JavaPlugin plugin) {
        this.plugin = plugin;
        logger.info("[PvPManager] Sistema PvP global inicializado");
    }
    
    /**
     * Registra este manager como listener de eventos.
     */
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        logger.info("[PvPManager] Listeners de PvP registrados");
    }
    
    /**
     * Desregistra los listeners y limpia los datos.
     */
    public void disable() {
        pvpKillTracker.clear();
        pvpFarmCooldown.clear();
        newPlayerImmunity.clear();
        logger.info("[PvPManager] Sistema PvP deshabilitado y datos limpiados");
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Protección de nuevos jugadores deshabilitada
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Solo procesar PvP (Player vs Player)
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        // Protección de nuevos jugadores deshabilitada
        /*
        // Verificar protección de nuevos jugadores
        if (isNewPlayerProtected(attacker) || isNewPlayerProtected(victim)) {
            event.setCancelled(true);
            
            if (isNewPlayerProtected(attacker)) {
                attacker.sendMessage(MM.toComponent("<red>No puedes atacar mientras tienes protección de nuevo jugador.</red>"));
            }
            
            if (isNewPlayerProtected(victim)) {
                attacker.sendMessage(MM.toComponent("<red>Este jugador tiene protección de nuevo jugador.</red>"));
            }
            
            return;
        }
        */
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        // Solo procesar muertes PvP
        if (killer == null || killer.equals(victim)) {
            return;
        }
        
        @SuppressWarnings("unused")
        UUID killerId = killer.getUniqueId();
        @SuppressWarnings("unused")
        UUID victimId = victim.getUniqueId();
        
        // Procesar anti-farmeo de corazones
        processAntiHeartFarming(killer, victim);
        
        logger.info("[PvPManager] PvP registrado: {} mató a {}", killer.getName(), victim.getName());
    }
    
    /**
     * Verifica si un jugador tiene protección de nuevo jugador activa.
     */
    private boolean isNewPlayerProtected(Player player) {
        // Protección deshabilitada
        return false;
    }
    
    /**
     * Procesa el sistema anti-farmeo de corazones PvP.
     */
    private void processAntiHeartFarming(Player killer, Player victim) {
        UUID killerId = killer.getUniqueId();
        UUID victimId = victim.getUniqueId();
        
        // Obtener o crear el mapa de kills del killer
        Map<UUID, Integer> killerStats = pvpKillTracker.computeIfAbsent(killerId, k -> new ConcurrentHashMap<>());
        Map<UUID, Long> killerCooldowns = pvpFarmCooldown.computeIfAbsent(killerId, k -> new ConcurrentHashMap<>());
        
        // Verificar si está en cooldown
        Long cooldownEnd = killerCooldowns.get(victimId);
        long currentTime = System.currentTimeMillis();
        
        if (cooldownEnd != null && currentTime < cooldownEnd) {
            // Está en cooldown, no otorgar corazones
            killer.sendMessage(MM.toComponent("<red>Anti-Farmeo Activo: No obtienes corazones de este jugador por " + 
                formatTimeRemaining(cooldownEnd - currentTime) + "</red>"));
            
            logger.info("[PvPManager] Anti-farmeo aplicado: {} no recibe corazones de {}", 
                killer.getName(), victim.getName());
            return;
        }
        
        // Incrementar contador de kills
        int killCount = killerStats.merge(victimId, 1, Integer::sum);
        
        if (killCount > KILL_LIMIT_BEFORE_REDUCTION) {
            // Ya está en cooldown (esto no debería pasar, pero por seguridad)
            killer.sendMessage(MM.toComponent("<red>Anti-Farmeo Activo: No obtienes corazones de este jugador.</red>"));
            
            logger.info("[PvPManager] Anti-farmeo aplicado: {} no recibe corazones de {} (kill #{} después del límite)", 
                killer.getName(), victim.getName(), killCount);
            return;
        } else if (killCount == KILL_LIMIT_BEFORE_REDUCTION) {
            // Exactamente en el límite - NO otorgar corazón y activar cooldown
            killerCooldowns.put(victimId, currentTime + ANTI_FARM_COOLDOWN_MS);
            
            killer.sendMessage(MM.toComponent("<red><bold>¡Límite de Farmeo Alcanzado!</bold></red>"));
            killer.sendMessage(MM.toComponent("<white>No obtendrás más corazones de este jugador (" + victim.getName() + ") durante 24 horas.</white>"));
            
            logger.info("[PvPManager] Límite de farmeo alcanzado: {} vs {} ({} kills) - Corazón bloqueado", 
                killer.getName(), victim.getName(), killCount);
            return;
        } else {
            // Otorgar corazón normalmente (kills 1 y 2)
            grantHeartToPlayer(killer);
            
            int remaining = KILL_LIMIT_BEFORE_REDUCTION - killCount;
            killer.sendMessage(MM.toComponent("<yellow>Kills restantes antes del límite: " + remaining + "</yellow>"));
            
            logger.info("[PvPManager] Corazón otorgado: {} mató a {} (kill #{}/{})", 
                killer.getName(), victim.getName(), killCount, KILL_LIMIT_BEFORE_REDUCTION);
        }
    }
    
    /**
     * Otorga un corazón al jugador.
     */
    private void grantHeartToPlayer(Player player) {
        AttributeInstance healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            double currentMaxHealth = healthAttribute.getBaseValue();
            healthAttribute.setBaseValue(currentMaxHealth + 2.0); // +1 corazón = +2 HP
            
            // Curar al jugador también
            double newHealth = Math.min(player.getHealth() + 2.0, healthAttribute.getBaseValue());
            player.setHealth(newHealth);
            
            player.sendMessage(MM.toComponent("<green><bold>¡Has ganado un corazón!</bold></green>"));
            
            logger.info("[PvPManager] Corazón otorgado a {}", player.getName());
        }
    }
    
    /**
     * Formatea el tiempo restante en un formato legible.
     */
    private String formatTimeRemaining(long milliseconds) {
        long hours = milliseconds / (60 * 60 * 1000);
        long minutes = (milliseconds % (60 * 60 * 1000)) / (60 * 1000);
        
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }
    
    /**
     * Obtiene estadísticas de PvP para un jugador.
     */
    public Map<UUID, Integer> getPlayerPvPStats(UUID playerId) {
        return pvpKillTracker.getOrDefault(playerId, new ConcurrentHashMap<>());
    }
    
    /**
     * Verifica si un jugador tiene inmunidad de nuevo jugador.
     */
    public boolean hasNewPlayerImmunity(UUID playerId) {
        return isNewPlayerProtected(Bukkit.getPlayer(playerId));
    }
    
    /**
     * Limpia manualmente la inmunidad de un jugador (para comandos admin).
     */
    public void removeNewPlayerImmunity(UUID playerId) {
        newPlayerImmunity.remove(playerId);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.sendMessage(MM.toComponent("<yellow>Tu protección de nuevo jugador ha sido removida por un administrador.</yellow>"));
        }
    }
}