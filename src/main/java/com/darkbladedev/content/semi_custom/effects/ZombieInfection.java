package com.darkbladedev.content.semi_custom.effects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.content.semi_custom.CustomEffects;
import com.darkbladedev.utils.EffectType;
import com.darkbladedev.utils.MM;

/**
 * Efecto personalizado que simula una infección zombie.
 * Los jugadores infectados experimentan diferentes efectos según la hora del día.
 */
public class ZombieInfection extends CustomEffects {

    /**
     * Constructor para el efecto de infección zombie.
     * 
     * @param plugin El plugin principal
     */
    public ZombieInfection(HeartlessMain plugin) {
        super(plugin, EffectType.ZOMBIE_INFECTION, "<gradient:#44f307:#3ee72a:#39da4c:#33ce6f:#2dc191:#27b5b4:#22a8d6:#1c9cf9:#1d9ef4:#1e9ff0:#1fa1eb:#20a3e7:#21a5e2:#22a6de:#23a8d9>Infeccion zombie</gradient>");
    }
    
    @Override
    protected void applyEffectToPlayer(Player player) {
        // Obtener el tiempo del mundo del jugador
        World world = player.getWorld();
        long time = world.getTime();
        
        // Aplicar efectos basados en el tiempo
        applyTimeBasedEffects(player, time);
    }
    
    @Override
    protected long getCheckInterval() {
        return 20L; // Verificar cada 1 segundo
    }
    
    @Override
    protected void removeEffectsFromPlayer(Player player) {
        // Eliminar todos los efectos negativos
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.setFireTicks(0);
    }
    
    @Override
    protected Sound getAppliedSound() {
        return Sound.ENTITY_ZOMBIE_AMBIENT;
    }
    
    @Override
    protected Sound getRemovedSound() {
        return Sound.ENTITY_ZOMBIE_VILLAGER_CURE;
    }
    
    @Override
    protected Particle getAppliedParticle() {
        return Particle.ANGRY_VILLAGER;
    }
    
    @Override
    protected Particle getRemovedParticle() {
        return Particle.HAPPY_VILLAGER;
    }
    
    /**
     * Verifica si hay bloques sólidos sobre el jugador que bloqueen la luz solar
     * @param player El jugador a verificar
     * @return true si hay bloques sobre el jugador, false si está expuesto al cielo
     */
    private boolean hasBlockAbove(Player player) {
        Location loc = player.getLocation();
        int maxHeight = player.getWorld().getMaxHeight();
        int playerY = loc.getBlockY();
        
        // Verificar desde la posición del jugador hasta el límite del mundo
        for (int y = playerY + 2; y < maxHeight; y++) {
            Block block = player.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            if (block.getType().isOccluding() || block.getType().toString().contains("LEAVES")) {
                return true; // Hay un bloque sólido o hojas sobre el jugador
            }
        }
        
        return false; // No hay bloques sobre el jugador
    }
    
    /**
     * Aplica efectos basados en la hora del día
     * @param player El jugador infectado
     * @param time El tiempo actual del mundo (0-24000)
     */
    private void applyTimeBasedEffects(Player player, long time) {
        // Determinar el período del día basado en el tiempo
        String timePeriod = null;
        
        if (time >= 0 && time <= 3000) {
            timePeriod = "MORNING_DUSK";
        } else if (time >= 3001 && time <= 4999) {
            timePeriod = "TRANSITION";
        } else if (time >= 5000 && time <= 11000) {
            timePeriod = "MIDDAY";
        } else if (time >= 11001 && time <= 11999) {
            timePeriod = "TRANSITION";
        } else if (time >= 12000 && time <= 14000) {
            timePeriod = "MORNING_DUSK";
        } else if (time >= 14001 && time <= 24000) {
            timePeriod = "NIGHT";
        }

        
        // Aplicar efectos según el período del día usando switch
        switch (timePeriod) {
            case "MORNING_DUSK":
                // Eliminar efectos de otros períodos
                player.removePotionEffect(PotionEffectType.STRENGTH);
                player.setFireTicks(0);
                
                if (!player.hasPotionEffect(PotionEffectType.NAUSEA)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 40, 0, false, true, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0, false, true, true));
                    player.sendActionBar(MM.toComponent("<gray>La infección zombie te debilita durante este momento del día..."));
                }
                break;
                
            case "MIDDAY":
                // Eliminar efectos de otros períodos
                player.removePotionEffect(PotionEffectType.STRENGTH);
                
                // Verificar si el jugador está expuesto a la luz directa del sol
                boolean isInDirectSunlight = player.getLocation().getBlock().getLightFromSky() == 15 && 
                                            player.getWorld().isClearWeather() &&
                                            player.getLocation().getWorld().getTime() < 12000 &&
                                            player.getLocation().getBlock().getLightFromBlocks() < 14 &&
                                            !hasBlockAbove(player);
                
                if (isInDirectSunlight) {
                    // Aplicar efecto de wither y fuego solo si está en luz directa del sol
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20, 0, false, true, true));
                    player.setFireTicks(Math.max(player.getFireTicks(), 100)); // Asegurar al menos 5 segundos de fuego
                    player.sendActionBar(MM.toComponent("<red>¡La luz directa del sol quema tu piel infectada!"));
                    
                    // Efectos visuales adicionales para enfatizar el fuego
                    player.getWorld().spawnParticle(Particle.LAVA,
                        player.getLocation().add(0, 1, 0),
                        10, 0.3, 0.5, 0.3, 0.01);
                } else {
                    // Si no está en luz directa, quitar el fuego pero mantener debilidad
                    player.setFireTicks(0);
                    player.removePotionEffect(PotionEffectType.WITHER);
                    player.sendActionBar(MM.toComponent("<yellow>La infección te debilita, pero estás a salvo de la luz solar directa."));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 40, 0, false, true, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0, false, true, true));
                }
                break;
                
            case "NIGHT":
                // Eliminar efectos de otros períodos
                player.removePotionEffect(PotionEffectType.NAUSEA);
                player.removePotionEffect(PotionEffectType.HUNGER);
                player.removePotionEffect(PotionEffectType.WITHER);
                player.setFireTicks(0);
                
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20, 0, false, true, true));
                player.sendActionBar(MM.toComponent("<dark_gray>La oscuridad de la noche <dark_red>fortalece <dark_gray>tu infección..."));
                
                break;
                
            case "TRANSITION":
            default:
                // Eliminar todos los efectos en períodos de transición
                player.removePotionEffect(PotionEffectType.NAUSEA);
                player.removePotionEffect(PotionEffectType.HUNGER);
                player.removePotionEffect(PotionEffectType.WITHER);
                player.removePotionEffect(PotionEffectType.STRENGTH);
                player.setFireTicks(0);
                break;
        }
        
        // Efectos visuales constantes para mostrar que está infectado
        player.getWorld().spawnParticle(Particle.SMOKE, 
            player.getLocation().add(0, 1, 0), 
            3, 0.3, 0.5, 0.3, 0.01);
    }
    
    /**
     * Verifica si el jugador tiene resistencia a la infección (por haber sido curado 5+ veces)
     * @param player El jugador a verificar
     * @return true si tiene resistencia parcial
     */
    public boolean hasInfectionResistance(Player player) {
        int curedCount = getPlayerCounter(player.getUniqueId());
        return curedCount >= 5;
    }
    
    /**
     * Verifica si el jugador tiene un arma en la mano
     * @param player El jugador a verificar
     * @return true si tiene un arma
     */
    private boolean hasWeaponInHand(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        Material type = mainHand.getType();
        
        // Verificar si es un arma
        return type.toString().contains("SWORD") || 
               type.toString().contains("AXE") || 
               type.toString().contains("TRIDENT") ||
               type == Material.BOW || 
               type == Material.CROSSBOW;
    }
    
    // Eventos
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isEnabled) return;
        
        Player player = event.getPlayer();
        
        // Verificar si el jugador estaba infectado al desconectarse
        if (isAffected(player)) {
            player.sendMessage(MM.toComponent(prefix + " <red>Sigues infectado con el virus zombie. <gray>Come una manzana dorada para curarte."));
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled) return;
        
        // Verificar si un jugador infectado golpea a otro jugador
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            
            // Si el atacante está infectado y no tiene arma en la mano
            if (isAffected(attacker) && !hasWeaponInHand(attacker)) {
                // Probabilidad de infección (50% base, reducida si tiene resistencia)
                double infectionChance = hasInfectionResistance(victim) ? 0.25 : 0.5;
                
                if (Math.random() < infectionChance) {
                    // Infectar al jugador golpeado
                    applyEffect(victim);
                    victim.sendMessage(MM.toComponent("<red>¡" + attacker.getName() + " te ha infectado con el virus zombie!"));
                    attacker.sendMessage(MM.toComponent("<green>¡Has infectado a " + victim.getName() + " con el virus zombie!"));
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!isEnabled) return;
        
        // Verificar si un zombie está apuntando a un jugador
        if (event.getEntity().getType() == EntityType.ZOMBIE && event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            
            // Si el jugador está infectado, hay una probabilidad de que el zombie lo ignore
            if (isAffected(player) && Math.random() < 0.7) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (!isEnabled) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Verificar si el jugador está comiendo una manzana dorada y está infectado
        if (isAffected(player) && (item.getType() == Material.GOLDEN_APPLE || item.getType() == Material.ENCHANTED_GOLDEN_APPLE)) {
            // Curar al jugador
            removeEffect(player);
        }
    }

    @Override
    public List<NamespacedKey> getKeys() {
        List<NamespacedKey> keys = new ArrayList<>();

        keys.add(counterKey);
        keys.add(effectKey);
        return keys;
    }

    @Override
    public NamespacedKey getKey() {
        return effectKey;
    }

    @Override
    public NamespacedKey getCounterKey() {
        return counterKey;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }
}