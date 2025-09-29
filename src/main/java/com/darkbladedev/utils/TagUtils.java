package com.darkbladedev.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;

/**
 * Utilidades para trabajar con TagKey personalizados en PaperAPI.
 * 
 * Esta clase proporciona métodos para:
 * - Verificar la existencia de tags personalizados
 * - Validar items contra tags con fallbacks robustos
 * - Manejar la compatibilidad con datapacks
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public class TagUtils {
    
    /**
     * Verifica si un TagKey existe en el registro.
     * 
     * @param tagKey El TagKey a verificar
     * @return true si el tag existe, false en caso contrario
     */
    public static boolean tagExists(TagKey<?> tagKey) {
        try {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Tag<?> tag = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ITEM)
                .getTag((TagKey) tagKey);
            return tag != null && !tag.values().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica si un ItemStack pertenece a un tag específico.
     * 
     * @param item El ItemStack a verificar
     * @param tagKey El TagKey contra el cual verificar
     * @return true si el item pertenece al tag, false en caso contrario
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean itemMatchesTag(ItemStack item, TagKey<?> tagKey) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        try {
            TypedKey<?> itemKey = TypedKey.create(RegistryKey.ITEM, item.getType().key());
            Tag<?> tag = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ITEM)
                .getTag((TagKey) tagKey);
            
            return tag != null && tag.contains((TypedKey) itemKey);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica si un Material es comida consumible por jugadores.
     * 
     * Este método proporciona un fallback manual cuando los tags personalizados
     * no están disponibles.
     * 
     * @param material El Material a verificar
     * @return true si es comida consumible, false en caso contrario
     */
    public static boolean isConsumableFood(Material material) {
        return switch (material) {
            case BREAD, APPLE, GOLDEN_APPLE, ENCHANTED_GOLDEN_APPLE,
                 COOKED_BEEF, COOKED_PORKCHOP, COOKED_CHICKEN, COOKED_SALMON, COOKED_COD,
                 BAKED_POTATO, COOKIE, CAKE, PUMPKIN_PIE,
                 MUSHROOM_STEW, RABBIT_STEW, BEETROOT_SOUP, SUSPICIOUS_STEW,
                 HONEY_BOTTLE, MILK_BUCKET, MELON_SLICE, SWEET_BERRIES, GLOW_BERRIES,
                 CHORUS_FRUIT, DRIED_KELP, TROPICAL_FISH, PUFFERFISH,
                 COOKED_MUTTON, COOKED_RABBIT, BEETROOT, CARROT, POTATO,
                 POISONOUS_POTATO, SPIDER_EYE, ROTTEN_FLESH -> true;
            default -> false;
        };
    }
    
    /**
     * Verifica si un ItemStack es válido para el encantamiento Condimento.
     * 
     * Este método intenta usar un tag personalizado primero, y si no está disponible,
     * utiliza verificación manual como fallback.
     * 
     * @param item El ItemStack a verificar
     * @return true si es válido para el encantamiento, false en caso contrario
     */
    public static boolean isValidCondimentoTarget(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        // Intentar usar la nueva tag 'all_food' primero
        TagKey<?> allFoodTag = TagKey.create(RegistryKey.ITEM, Key.key("heartless", "all_food"));
        if (tagExists(allFoodTag) && itemMatchesTag(item, allFoodTag)) {
            return true;
        }
        
        // Fallback a tag anterior 'consumable_food'
        TagKey<?> customFoodTag = TagKey.create(RegistryKey.ITEM, Key.key("heartless", "consumable_food"));
        if (tagExists(customFoodTag) && itemMatchesTag(item, customFoodTag)) {
            return true;
        }
        
        // Fallback final a verificación manual
        return isConsumableFood(item.getType());
    }
    
    /**
     * Obtiene el TagKey para todos los alimentos.
     * 
     * @return TagKey para la tag 'all_food'
     */
    public static TagKey<?> getAllFoodTag() {
        return TagKey.create(RegistryKey.ITEM, Key.key("heartless", "all_food"));
    }
    
    /**
     * Crea un TagKey personalizado de forma segura.
     * 
     * @param namespace El namespace del tag
     * @param key La clave del tag
     * @return El TagKey creado
     */
    public static TagKey<?> createCustomTag(String namespace, String key) {
        return TagKey.create(RegistryKey.ITEM, Key.key(namespace, key));
    }
    
    /**
     * Obtiene información de debug sobre un tag.
     * 
     * @param tagKey El TagKey a inspeccionar
     * @return String con información de debug
     */
    public static String getTagDebugInfo(TagKey<?> tagKey) {
        try {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Tag<?> tag = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ITEM)
                .getTag((TagKey) tagKey);
            
            if (tag == null) {
                return "Tag no encontrado: " + tagKey.key();
            }
            
            return String.format("Tag: %s, Elementos: %d", 
                tagKey.key(), tag.values().size());
        } catch (Exception e) {
            return "Error al obtener información del tag: " + e.getMessage();
        }
    }
}