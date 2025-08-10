package com.darkbladedev.utils;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;

import de.tr7zw.changeme.nbtapi.NBTItem;

public class PotionUtils {

    /**
     * Establece un efecto de poción personalizado en un ItemStack de tipo poción
     * 
     * @param potion ItemStack de Minecraft (net.minecraft.world.item.ItemStack)
     * @param holder Holder del efecto de poción a aplicar
     * @throws IllegalArgumentException si el item no es una poción válida
     */
    @SuppressWarnings({ "deprecation" })
    public static void setPotion(ItemStack potion, Holder<MobEffect> holder) {
        // Verificar que el item sea una poción
        if (potion.getItem() != Items.POTION && potion.getItem() != Items.SPLASH_POTION && potion.getItem() != Items.LINGERING_POTION) {
            throw new IllegalArgumentException("El ItemStack debe ser una poción (POTION, SPLASH_POTION o LINGERING_POTION)");
        }
        
        // Obtener el ID del efecto de poción del registro
        String potionId = BuiltInRegistries.MOB_EFFECT.getKey(holder.value()).getNamespace() + ":" + BuiltInRegistries.MOB_EFFECT.getKey(holder.value()).getPath();
        
        // Convertir a ItemStack de Bukkit para usar PersistentDataContainer
        org.bukkit.inventory.ItemStack bukkitPotion = potion.asBukkitCopy();
        

        NBTItem nbti = new NBTItem(bukkitPotion);
        nbti.setString("Potion", potionId);

        
        // // Guardar el ID de la poción en el PersistentDataContainer y configurar metadatos de poción
        // bukkitPotion.editMeta(meta -> {
        //     // Guardar el ID de la poción en el PersistentDataContainer
        //     meta.getPersistentDataContainer().set(potionKey, PersistentDataType.STRING, potionId);
            
        //     // Si es un ItemMeta de poción, establecer datos básicos de poción para compatibilidad
        //     if (meta instanceof org.bukkit.inventory.meta.PotionMeta potionMeta) {
        //         potionMeta.setBasePotionData(new PotionData(PotionType.WATER));
                
        //         // Establecer un nombre personalizado para la poción si es necesario
        //         // potionMeta.setDisplayName(Component.text("Poción de " + potionId));
        //     }
        // });
            
        ItemStack newPotion = ItemStack.fromBukkitCopy(bukkitPotion);
        
        // Copiar el item y sus propiedades al ItemStack original
        potion.setItem(newPotion.getItem());
        potion.setCount(newPotion.getCount());
        
        // Nota: No necesitamos hacer nada más aquí porque ContentManager.java
        // llama a potion.asBukkitCopy() después de llamar a este método
    }
}
 