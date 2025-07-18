package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ToxicFog extends WeeklyEvent {
    
    private final Set<UUID> affectedPlayers;
    private BukkitTask toxicFogTask;
    private boolean isActive = false;
    private boolean isPaused = false;

    
    public ToxicFog(HeartlessMain plugin, long duration) {
        super(plugin, duration);
        this.affectedPlayers = new HashSet<>();
    }
    
    
    public void start() {
        if (isActive) return;
        
        isActive = true;
        isPaused = false;
        
        // Start the toxic fog task
        startToxicFogTask();
        
        // Schedule the end of the event
        new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    public void stop() {
        if (!isActive) return;
        
        isActive = false;
        
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
        
        affectedPlayers.clear();
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
    
    public boolean isActive() {
        return isActive;
    }

    private List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        // Usar una duración muy larga (30 minutos = 36000 ticks) para que sea efectivamente permanente
        potionEffects.add(new PotionEffect(PotionEffectType.POISON, 40, 2, false, true, true));
        // Hacer el efecto de oscuridad permanente durante el evento
        potionEffects.add(new PotionEffect(PotionEffectType.DARKNESS, 40, 2, true, false, true));
        return potionEffects;
    }

    public void pause() {
        if (!isActive || isPaused) return;
        
        isPaused = true;
        
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

    public void resume() {
        if (!isActive || !isPaused) return;
        
        isPaused = false;
        
        // Restart the toxic fog task
        startToxicFogTask();
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

        // Start toxic fog task
        toxicFogTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Verificar si el jugador está en agua o bajo techo
                    if (!isPlayerSafe(player)) {
                        // Aplicar los efectos
                        player.addPotionEffects(getPotionEffects());
                        
                        affectedPlayers.add(player.getUniqueId());
                    } else {
                        // Remover los efectos si el jugador está en un entorno seguro
                        player.removePotionEffect(PotionEffectType.POISON);
                        player.removePotionEffect(PotionEffectType.DARKNESS);

                        affectedPlayers.remove(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Verificar cada 1 segundo (20 ticks)
    }


    @Override
    public String getName() {
        return "Niebla Tóxica";
    }


    @Override
    protected void startEventTasks() {
        start();
    }


    @Override
    protected void stopEventTasks() {
        stop();
    }


    @Override
    protected void cleanupEventData() {
        affectedPlayers.clear();
    }
}
