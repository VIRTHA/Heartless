package com.darkbladedev.managers;


import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.content.custom.CustomPotions;
import com.darkbladedev.utils.PotionUtils;

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
 * POTIONS
 */
    public @NotNull ItemStack getFuryPotionItem() {
        // Crear un ItemStack de Bukkit y convertirlo a ItemStack de Minecraft
        org.bukkit.inventory.ItemStack bukkitPotion = new org.bukkit.inventory.ItemStack(Material.POTION);
        net.minecraft.world.item.ItemStack potion = net.minecraft.world.item.ItemStack.fromBukkitCopy(bukkitPotion);

        PotionUtils.setPotion(potion, CustomPotions.FURY_HOLDER);
        return potion.asBukkitCopy();
    }
    public @NotNull ItemStack getZombieInfectionPotionItem() {
        // Crear un ItemStack de Bukkit y convertirlo a ItemStack de Minecraft
        org.bukkit.inventory.ItemStack bukkitPotion = new org.bukkit.inventory.ItemStack(Material.POTION);
        net.minecraft.world.item.ItemStack potion = net.minecraft.world.item.ItemStack.fromBukkitCopy(bukkitPotion);
        
        PotionUtils.setPotion(potion, CustomPotions.ZOMBIE_INFECTION_HOLDER);
        return potion.asBukkitCopy();
    }

    public ItemStack getPotion(String potion) {
        if (potion.equals("fury")) {
            return getFuryPotionItem();
        }
        if (potion.equals("zombie_infection")) {
            return getZombieInfectionPotionItem();
        }
        return null;
    }


}
