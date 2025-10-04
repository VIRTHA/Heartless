package com.darkbladedev.managers;


import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.darkbladedev.HeartlessMain;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;

public class ContentManager {

    @SuppressWarnings("unused")
    private final HeartlessMain plugin;

    public ContentManager(HeartlessMain plugin) {
        this.plugin = plugin;
    }

    /*
     * ENCHANTMENTS SECTION
     */

    public @NotNull Enchantment getEnchantment(String name) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(Key.key(name));
    }

    public @NotNull Enchantment getEnchantment(Key key) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(key);

    }

    public @NotNull Enchantment getEnchantment(String namespace, String key) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(Key.key(namespace, key));
    }
    
    public boolean hasEnchantment(ItemStack item, Enchantment enchantment) {
        return item.getItemMeta().hasEnchant(enchantment);
    }

/*
 * ITEMS SECTION
 * ENCHANTMENTS
 */

    public @NotNull ItemStack getEnchantmentItem(Enchantment enchantment, int level) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        item.getItemMeta().addEnchant(enchantment, level, true);
        return item;
    }


/*
 * ITEMS SECTION
 * POTIONS - Funcionalidad temporalmente deshabilitada (requiere NMS)
 */
    // Los métodos de pociones personalizadas han sido eliminados temporalmente
    // debido a dependencias con NMS que no están disponibles sin paperweight.userdev

    public ItemStack getPotion(String potion) {
        // Funcionalidad de pociones personalizadas temporalmente deshabilitada
        // debido a dependencias con NMS que no están disponibles sin paperweight.userdev
        switch (potion.toLowerCase()) {
            case "fury":
                // return getFuryPotionItem(); // Deshabilitado temporalmente
                return new ItemStack(Material.POTION); // Poción básica como fallback
            case "zombie_infection":
                // return getZombieInfectionPotionItem(); // Deshabilitado temporalmente
                return new ItemStack(Material.POTION); // Poción básica como fallback
            default:
                return new ItemStack(Material.POTION);
        }
    }


}
