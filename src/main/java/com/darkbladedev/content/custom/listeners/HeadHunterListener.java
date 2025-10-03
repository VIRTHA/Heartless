package com.darkbladedev.content.custom.listeners;

import com.darkbladedev.content.custom.CustomEnchantments;
import com.darkbladedev.utils.MM;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener que maneja el encantamiento "Cazador de Cabezas" (Head Hunter).
 * Este encantamiento garantiza el drop de cabezas de mobs elegibles cuando son asesinados
 * con un arma que tenga este encantamiento.
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class HeadHunterListener implements Listener {

    // Mapeo de tipos de entidad a sus materiales de cabeza correspondientes
    private static final Map<EntityType, Material> HEAD_MATERIALS = new HashMap<>();
    
    static {
        // Mobs hostiles
        HEAD_MATERIALS.put(EntityType.ZOMBIE, Material.ZOMBIE_HEAD);
        HEAD_MATERIALS.put(EntityType.SKELETON, Material.SKELETON_SKULL);
        HEAD_MATERIALS.put(EntityType.WITHER_SKELETON, Material.WITHER_SKELETON_SKULL);
        HEAD_MATERIALS.put(EntityType.CREEPER, Material.CREEPER_HEAD);
        HEAD_MATERIALS.put(EntityType.ENDER_DRAGON, Material.DRAGON_HEAD);
        HEAD_MATERIALS.put(EntityType.PIGLIN, Material.PIGLIN_HEAD);
    }

    /**
     * Maneja el evento de muerte de entidades para aplicar el efecto del encantamiento Head Hunter.
     * 
     * @param event El evento de muerte de la entidad
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        // Verificar que hay un jugador asesino
        if (killer == null) {
            return;
        }
        
        // Obtener el arma utilizada
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType().isAir()) {
            return;
        }
        
        // Verificar si el arma tiene el encantamiento Head Hunter
        Enchantment headHunterEnchantment = getHeadHunterEnchantment();
        if (headHunterEnchantment == null || !weapon.containsEnchantment(headHunterEnchantment)) {
            return;
        }
        
        // Obtener el nivel del encantamiento (aunque sea nivel 1, útil para futuras expansiones)
        int enchantmentLevel = weapon.getEnchantmentLevel(headHunterEnchantment);
        
        // Verificar si la entidad puede dropear una cabeza
        Material headMaterial = getHeadMaterial(entity.getType());
        if (headMaterial == null) {
            return;
        }
        
        // Crear y dropear la cabeza
        ItemStack head = createHead(headMaterial, entity, enchantmentLevel);
        if (head != null) {
            // Remover cualquier cabeza que ya haya dropeado naturalmente para evitar duplicados
            removeNaturalHeadDrops(event, headMaterial);
            
            // Agregar la cabeza garantizada a los drops
            event.getDrops().add(head);
        }
    }
    
    /**
     * Obtiene el encantamiento Head Hunter del registro.
     * 
     * @return El encantamiento Head Hunter o null si no está registrado
     */
    private Enchantment getHeadHunterEnchantment() {
        try {
            return new CustomEnchantments().getEnchantment(CustomEnchantments.HEAD_DROPPER_KEY);
        } catch (Exception e) {
            // El encantamiento no está registrado o hay un error
            return null;
        }
    }
    
    /**
     * Obtiene el material de cabeza correspondiente para un tipo de entidad.
     * 
     * @param entityType El tipo de entidad
     * @return El material de la cabeza o null si no tiene cabeza disponible
     */
    private Material getHeadMaterial(EntityType entityType) {
        return HEAD_MATERIALS.get(entityType);
    }
    
    /**
     * Crea una cabeza personalizada basada en el tipo de entidad y nivel de encantamiento.
     * 
     * @param headMaterial El material de la cabeza
     * @param entity La entidad que fue asesinada
     * @param enchantmentLevel El nivel del encantamiento Head Hunter
     * @return La cabeza creada o null si hay un error
     */
    private ItemStack createHead(Material headMaterial, LivingEntity entity, int enchantmentLevel) {
        ItemStack head = new ItemStack(headMaterial, 1);
        
        // Para cabezas de jugador, establecer el propietario
        if (headMaterial == Material.PLAYER_HEAD && entity instanceof Player) {
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer((Player) entity);
                
                // Agregar lore personalizado indicando que fue obtenida con Head Hunter
                skullMeta.displayName(MM.toComponent("<green>Cabeza de " + entity.getName()));
                head.setItemMeta(skullMeta);
            }
        }
        
        return head;
    }
    
    /**
     * Remueve drops naturales de cabezas para evitar duplicados.
     * 
     * @param event El evento de muerte
     * @param headMaterial El material de cabeza a remover
     */
    private void removeNaturalHeadDrops(EntityDeathEvent event, Material headMaterial) {
        event.getDrops().removeIf(drop -> drop.getType() == headMaterial);
    }
    
    /**
     * Verifica si una entidad es elegible para dropear cabezas con Head Hunter.
     * 
     * @param entityType El tipo de entidad
     * @return true si la entidad puede dropear cabezas, false en caso contrario
     */
    public static boolean isEligibleForHeadDrop(EntityType entityType) {
        return HEAD_MATERIALS.containsKey(entityType);
    }
    
    /**
     * Obtiene todos los tipos de entidad elegibles para drops de cabezas.
     * 
     * @return Un conjunto de tipos de entidad elegibles
     */
    public static java.util.Set<EntityType> getEligibleEntityTypes() {
        return HEAD_MATERIALS.keySet();
    }
}