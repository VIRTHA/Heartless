package com.darkbladedev.managers;


import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.darkbladedev.HeartlessMain;

import net.kyori.adventure.key.Key;

public class ContentManager {

    @SuppressWarnings("unused")
    private final HeartlessMain plugin;

    public ContentManager(HeartlessMain plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    public @NotNull Enchantment getEnchantment(String name) {
        return Enchantment.getByName(name);
    }

    @SuppressWarnings("deprecation")
    public @NotNull Enchantment getEnchantment(Key key) {
        return Enchantment.getByKey(NamespacedKey.fromString(key.asString()));
    }

    @SuppressWarnings("deprecation")
    public @NotNull Enchantment getEnchantment(String namespace, String key) {
        return Enchantment.getByKey(new NamespacedKey(namespace, key));
    }
    
    public boolean hasEnchantment(ItemStack item, Enchantment enchantment) {
        return item.getItemMeta().hasEnchant(enchantment);
    }



}
