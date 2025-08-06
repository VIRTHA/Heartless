package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.darkbladedev.utils.MM;

public class HealthRewards implements Listener {
    
    @SuppressWarnings("unused")
    private final Plugin plugin;
    
    public HealthRewards(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onPlayerEatGoldenApple(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        
        // Check if the item is an enchanted golden apple
        if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            // Add one heart (2 health points) to max health
            double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
            double newMaxHealth = Math.min(currentMaxHealth + 2.0, 40.0); // Cap at 20 hearts (40 health)
            
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
            player.sendMessage(MM.toComponent("&6¡Has ganado un corazón extra de vida máxima!"));
            
            // Play a special effect
            player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }
    
    @EventHandler
    public void onEnderDragonDeath(EntityDeathEvent event) {
        // Check if the killed entity is an Ender Dragon
        if (event.getEntityType() == EntityType.ENDER_DRAGON) {
            // Get the killer player (if any)
            if (event.getEntity().getKiller() != null) {
                Player killer = event.getEntity().getKiller();
                
                // Add two hearts (4 health points) to max health
                double currentMaxHealth = killer.getAttribute(Attribute.MAX_HEALTH).getValue();
                double newMaxHealth = Math.min(currentMaxHealth + 4.0, 40.0); // Cap at 20 hearts (40 health)
                
                killer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
                killer.sendMessage(MM.toComponent("&5¡Has derrotado al Dragón del End! &6Ganaste 2 corazones extra de vida máxima."));
                
                // Play a special effect
                killer.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, killer.getLocation().add(0, 1, 0), 50, 1.0, 1.0, 1.0, 0.1);
                killer.playSound(killer.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                
                // Broadcast the achievement to all players
                Bukkit.broadcast(MM.toComponent("&5¡" + killer.getName() + " ha derrotado al Dragón del End!"));
            }
        }
    }
}