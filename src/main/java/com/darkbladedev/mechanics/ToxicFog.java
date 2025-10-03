package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.TimeExpression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ToxicFog extends AbstractWeeklyEvent {
    
    private final Set<UUID> affectedPlayers;
    private BukkitTask toxicFogTask;
    
    public ToxicFog(HeartlessMain plugin, TimeExpression duration) {
        super(plugin, duration);
        this.affectedPlayers = new HashSet<>();
        this.prefix = "<b><gradient:#7b0c69:#7f1064:#83145f:#87185a:#8b1c55:#8f2050:#93254b:#972946:#9b2d41:#9f313c:#a33537:#a73932>Niebla Toxica</gradient></b>";
    }
    
    @Override
    protected void onEventStart() {
        // Lógica específica de inicio para ToxicFog
    }

    @Override
    protected void onEventStop() {
        // Lógica específica de parada para ToxicFog
    }
    
    @Override
    protected void initializeEventSpecificData() {
        // Inicializar datos específicos de ToxicFog
    }
    
    @Override
    protected void saveEventSpecificData() {
        // Guardar datos específicos de ToxicFog
    }
    
    @Override
    protected void processEventStatistics() {
        // Procesar estadísticas específicas de ToxicFog
    }
    

    
    private boolean isPlayerSafe(Player player) {
        // Verificar si el jugador está en agua
        Material blockType = player.getLocation().getBlock().getType();
        if (blockType == Material.WATER) {
            return true;
        }
        
        // Verificar si el jugador está bajo techo (no tiene acceso directo al cielo)
        if (player.getLocation().getBlock().getLightFromSky() > 0) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isActive() {
        // Use base class state
        return super.isActive();
    }

    private List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        // Usar una duración muy larga (30 minutos = 36000 ticks) para que sea efectivamente permanente
        potionEffects.add(new PotionEffect(PotionEffectType.POISON, 40, 2, false, true, true));
        // Hacer el efecto de oscuridad permanente durante el evento
        potionEffects.add(new PotionEffect(PotionEffectType.DARKNESS, 80, 2, true, false, true));
        return potionEffects;
    }

    @Override
    public void pause() {
        // Delegate pause state handling to base class
        super.pause();
    }

    @Override
    public void resume() {
        // Ensure handlers are (re)registered and state flags updated
        super.resume();
    }

    /**
     * Determines if a player should be affected by the toxic fog
     * @param player The player to check
     * @return true if the player should be affected, false otherwise
     */
    @SuppressWarnings("unused")
    private boolean shouldAffectPlayer(Player player) {
        return !isPlayerSafe(player);
    }

    private void startToxicFogTask() {
        // Cancel existing task if any
        if (toxicFogTask != null) {
            toxicFogTask.cancel();
        }
        
        toxicFogTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    
                    if (!isPlayerSafe(player)) {
                        // Player is not safe, apply toxic effects
                        if (!affectedPlayers.contains(playerId)) {
                            affectedPlayers.add(playerId);
                        }
                        
                        // Apply poison and darkness effects
                        for (PotionEffect effect : getPotionEffects()) {
                            player.addPotionEffect(effect);
                        }
                    } else {
                        // Player is safe, remove from affected players
                        affectedPlayers.remove(playerId);
                        
                        // Remove toxic effects
                        player.removePotionEffect(PotionEffectType.POISON);
                        player.removePotionEffect(PotionEffectType.DARKNESS);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    // ========== MÉTODOS DE PERSISTENCIA ==========
    
    /**
     * Obtiene el conjunto de jugadores afectados por la niebla tóxica
     * @return Set de UUIDs de jugadores afectados
     */
    public Set<UUID> getAffectedPlayers() {
        return new HashSet<>(affectedPlayers);
    }
    
    /**
     * Carga los jugadores afectados por la niebla tóxica (para carga desde persistencia)
     * @param affectedPlayers Set con UUIDs de jugadores afectados
     */
    public void loadAffectedPlayers(Set<UUID> affectedPlayers) {
        this.affectedPlayers.clear();
        this.affectedPlayers.addAll(affectedPlayers);
    }




    @Override
    public String getId() {
        return "toxic_fog";
    }


    @Override
    protected void startEventTasks() {
        // Start only the event-specific repeating task
        startToxicFogTask();
    }


    @Override
    protected void stopEventTasks() {
        // Cancel running task
        if (toxicFogTask != null) {
            toxicFogTask.cancel();
            toxicFogTask = null;
        }
        
        // Remove all effects from affected players
        for (UUID uuid : affectedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.removePotionEffect(PotionEffectType.POISON);
                player.removePotionEffect(PotionEffectType.DARKNESS);
            }
        }
    }

    @Override
    protected void pauseEventTasks() {
        // Cancel the toxic fog task
        if (toxicFogTask != null) {
            toxicFogTask.cancel();
            toxicFogTask = null;
        }
        
        // Remove effects temporarily
        for (UUID uuid : affectedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.removePotionEffect(PotionEffectType.POISON);
                player.removePotionEffect(PotionEffectType.DARKNESS);
            }
        }
    }

    @Override
    protected void resumeEventTasks() {
        // Restart the toxic fog task
        startToxicFogTask();
    }


    @Override
    protected void cleanupEventData() {
        affectedPlayers.clear();
    }
}
