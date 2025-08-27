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

public class ToxicFog extends WeeklyEvent {
    
    private final Set<UUID> affectedPlayers;
    private BukkitTask toxicFogTask;
    
    public ToxicFog(HeartlessMain plugin, TimeExpression duration) {
        super(plugin, duration);
        this.affectedPlayers = new HashSet<>();
    }
    
    @Override
    public void start() {
        // Delegate lifecycle to base class (registers handlers and schedules end)
        super.start();
    }

    @Override
    public void stop() {
        // Delegate lifecycle to base class (cancels end task, unregisters handlers)
        super.stop();
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
