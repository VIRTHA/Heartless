package com.darkbladedev.content.semi_custom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.EffectType;
import com.darkbladedev.utils.MM;

/**
 * Clase base abstracta para todos los efectos personalizados que pueden afectar a jugadores.
 * Proporciona la estructura común y funcionalidades básicas que todos los efectos comparten.
 */
public abstract class CustomEffectsBase implements Listener {
    
    protected final HeartlessMain plugin;
    protected boolean isEnabled = true;
    protected EffectType id;
    protected String prefix;
    
    // Conjunto para rastrear jugadores afectados por este efecto
    protected final Set<UUID> affectedPlayers = new HashSet<>();
    
    // Mapa para rastrear tareas asociadas a jugadores
    protected final Map<UUID, BukkitTask> effectTasks = new HashMap<>();
    
    // Mapa para almacenar contadores personalizados por jugador
    protected final Map<UUID, Integer> playerCounters = new HashMap<>();
    
    // Key para almacenar datos persistentes del efecto
    protected NamespacedKey effectKey;
    protected NamespacedKey counterKey;
    
    // Configuración de mundos
    protected final Set<String> excludedWorlds = new HashSet<>();
    protected boolean applyToAllWorlds = true;
    
    // Tarea de verificación periódica
    protected BukkitTask periodicTask;
    
    /**
     * Constructor base para efectos personalizados.
     * 
     * @param plugin El plugin principal
     * @param id Nombre único del efecto (Internal data)
     * @param prefix Prefijo del efecto
     */
    public CustomEffectsBase(HeartlessMain plugin, EffectType id, String prefix) {
        this.plugin = plugin;
        this.id = id;
        this.prefix = prefix;
        
        // Crear keys para datos persistentes
        this.effectKey = new NamespacedKey(plugin, id.getID() + "_effect");
        this.counterKey = new NamespacedKey(plugin, id.getID() + "_counter");
        
        // Registrar eventos
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Iniciar tarea periódica
        startPeriodicTask();
    }    
    
    /**
     * Inicia la tarea periódica para verificar y aplicar efectos
     */
    protected void startPeriodicTask() {
        if (periodicTask != null) {
            periodicTask.cancel();
        }
        
        periodicTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEnabled) return;
                
                // Procesar cada mundo por separado
                for (World world : Bukkit.getWorlds()) {
                    // Omitir mundos excluidos
                    if (excludedWorlds.contains(world.getName())) {
                        continue;
                    }
                    
                    // Aplicar efectos a jugadores afectados en este mundo
                    for (UUID playerId : affectedPlayers) {
                        Player player = Bukkit.getPlayer(playerId);
                        if (player != null && player.isOnline() && 
                            (applyToAllWorlds || player.getWorld().equals(world))) {
                            applyEffectToPlayer(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, getCheckInterval());
    }
    
    /**
     * Aplica el efecto a un jugador específico
     * @param player El jugador al que aplicar el efecto
     */
    protected abstract void applyEffectToPlayer(Player player);
    
    /**
     * Obtiene el intervalo de verificación para la tarea periódica
     * @return Intervalo en ticks
     */
    protected abstract long getCheckInterval();
    
    /**
     * Aplica el efecto a un jugador
     * @param player El jugador al que aplicar el efecto
     */
    public void applyEffect(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Si ya está afectado, no hacer nada
        if (isAffected(player)) return;
        
        // Marcar al jugador como afectado
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(effectKey, PersistentDataType.BOOLEAN, true);
        affectedPlayers.add(playerId);
        
        // Notificar al jugador
        notifyEffectApplied(player);
        
        // Aplicar efectos iniciales
        applyEffectToPlayer(player);
    }
    
    /**
     * Elimina el efecto de un jugador
     * @param player El jugador del que eliminar el efecto
     */
    public void removeEffect(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Si no está afectado, no hacer nada
        if (!isAffected(player)) return;
        
        // Eliminar la marca de efecto
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(effectKey, PersistentDataType.BOOLEAN, false);
        affectedPlayers.remove(playerId);

        // Incrementar el contador si es necesario
        incrementPlayerCounter(player);
        
        // Cancelar tareas asociadas
        if (effectTasks.containsKey(playerId)) {
            effectTasks.get(playerId).cancel();
            effectTasks.remove(playerId);
        }
        
        // Eliminar efectos negativos
        removeEffectsFromPlayer(player);
        
        // Notificar al jugador
        notifyEffectRemoved(player);
    }
    
    /**
     * Elimina los efectos específicos del jugador
     * @param player El jugador del que eliminar los efectos
     */
    protected abstract void removeEffectsFromPlayer(Player player);
    
    /**
     * Notifica al jugador que se le ha aplicado el efecto
     * @param player El jugador a notificar
     */
    protected void notifyEffectApplied(Player player) {
        player.sendMessage(MM.toComponent(" <red>¡Has sido afectado por " + prefix + "!"));
        player.playSound(player.getLocation(), getAppliedSound(), 1.0f, 1.0f);
        
        // Efectos visuales
        player.getWorld().spawnParticle(getAppliedParticle(), 
            player.getLocation().add(0, 1, 0), 
            15, 0.5, 0.5, 0.5, 0.1);
    }
    
    /**
     * Notifica al jugador que se le ha eliminado el efecto
     * @param player El jugador a notificar
     */
    protected void notifyEffectRemoved(Player player) {
        player.sendActionBar(MM.toComponent("<green>¡Te has liberado de " + prefix + "!"));
        player.playSound(player.getLocation(), getRemovedSound(), 0.7f, 1.0f);
        
        // Efectos visuales
        player.getWorld().spawnParticle(getRemovedParticle(), 
            player.getLocation().add(0, 1, 0), 
            20, 0.5, 0.5, 0.5, 0.1);
    }
    
    /**
     * Incrementa el contador del jugador
     * @param player El jugador cuyo contador incrementar
     */
    protected void incrementPlayerCounter(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int currentCount = pdc.getOrDefault(counterKey, PersistentDataType.INTEGER, 0);
        pdc.set(counterKey, PersistentDataType.INTEGER, currentCount + 1);
        
        // Actualizar el mapa de conteo
        playerCounters.put(player.getUniqueId(), currentCount + 1);
    }
    
    /**
     * Obtiene el contador del jugador
     * @param playerId UUID del jugador
     * @return Valor del contador
     */
    public int getPlayerCounter(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            PersistentDataContainer pdc = player.getPersistentDataContainer();
            if (pdc.has(counterKey, PersistentDataType.INTEGER)) {
                return pdc.get(counterKey, PersistentDataType.INTEGER);
            }
        }
        return playerCounters.getOrDefault(playerId, 0);
    }
    
    /**
     * Verifica si un jugador está afectado por este efecto
     * @param player El jugador a verificar
     * @return true si está afectado, false en caso contrario
     */
    public boolean isAffected(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (pdc.has(effectKey, PersistentDataType.BOOLEAN)) {
            return pdc.get(effectKey, PersistentDataType.BOOLEAN);
        }
        return false;
    }
    
    /**
     * Habilita o deshabilita el efecto
     * @param enabled true para habilitar, false para deshabilitar
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        
        if (!enabled) {
            // Eliminar el efecto de todos los jugadores si se deshabilita
            for (UUID playerId : new HashSet<>(affectedPlayers)) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    removeEffect(player);
                }
            }
        }
    }
    
    /**
     * Añade un mundo a la lista de exclusión
     * @param worldName Nombre del mundo a excluir
     * @return true si el mundo fue añadido a la lista de exclusión, false si ya estaba excluido
     */
    public boolean excludeWorld(String worldName) {
        return excludedWorlds.add(worldName);
    }

    /**
     * Elimina un mundo de la lista de exclusión
     * @param worldName Nombre del mundo a incluir
     * @return true si el mundo fue eliminado de la lista de exclusión, false si no estaba excluido
     */
    public boolean includeWorld(String worldName) {
        return excludedWorlds.remove(worldName);
    }

    /**
     * Verifica si un mundo está excluido de los efectos
     * @param worldName Nombre del mundo a verificar
     * @return true si el mundo está excluido, false en caso contrario
     */
    public boolean isWorldExcluded(String worldName) {
        return excludedWorlds.contains(worldName);
    }

    /**
     * Obtiene una copia del conjunto de mundos excluidos
     * @return Conjunto con los nombres de todos los mundos excluidos
     */
    public Set<String> getExcludedWorlds() {
        return new HashSet<>(excludedWorlds);
    }

    /**
     * Establece si aplicar efectos a jugadores en todos los mundos o solo en el mundo que se está procesando
     * @param applyToAll true para aplicar efectos a todos los jugadores independientemente del mundo, false para aplicar solo a jugadores en el mundo actual
     */
    public void setApplyToAllWorlds(boolean applyToAll) {
        this.applyToAllWorlds = applyToAll;
    }

    /**
     * Verifica si los efectos se aplican a jugadores en todos los mundos
     * @return true si los efectos se aplican a todos los jugadores independientemente del mundo, false si solo se aplican a jugadores en el mundo actual
     */
    public boolean isApplyToAllWorlds() {
        return applyToAllWorlds;
    }
    
    /**
     * Limpia los recursos utilizados por este efecto
     */
    public void cleanup() {
        // Cancelar tarea periódica
        if (periodicTask != null) {
            periodicTask.cancel();
            periodicTask = null;
        }
        
        // Eliminar el efecto de todos los jugadores
        for (UUID playerId : new HashSet<>(affectedPlayers)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                removeEffect(player);
            }
        }
        
        // Desregistrar eventos
        HandlerList.unregisterAll(this);
    }
    
    /**
     * @return El nombre del efecto
     */
    public String getid() {
        return id.getID();
    }
    
    /**
     * @return El sonido a reproducir cuando se aplica el efecto
     */
    protected abstract Sound getAppliedSound();
    
    /**
     * @return El sonido a reproducir cuando se elimina el efecto
     */
    protected abstract Sound getRemovedSound();
    
    /**
     * @return La partícula a mostrar cuando se aplica el efecto
     */
    protected abstract Particle getAppliedParticle();
    
    /**
     * @return La partícula a mostrar cuando se elimina el efecto
     */
    protected abstract Particle getRemovedParticle();

    public abstract List<NamespacedKey> getKeys();

    public abstract NamespacedKey getKey();

    public abstract NamespacedKey getCounterKey();

    public abstract String getPrefix();
}
