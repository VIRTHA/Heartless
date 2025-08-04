package com.darkbladedev.content.custom;

import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;


import net.kyori.adventure.key.Key;

public class CustomEnchantments {

    private static String namespace = "heartless";

    public static final Key ACID_RESISTANCE_KEY = Key.key(namespace, "acid_resistance");
    public static final Key ACID_INFECTION_KEY = Key.key(namespace, "acid_infection");
    public static final Key TICTAC_KEY = Key.key(namespace, "tictac");
    public static final Key ADRENALINE_KEY = Key.key(namespace, "adrenaline");

    @SuppressWarnings("deprecation")
    public Enchantment getEnchantment(Key key) {
        return Registry.ENCHANTMENT.get(key);
    }

    public enum ENCHANTMENTS {
        ACID_RESISTANCE(ACID_RESISTANCE_KEY, "Proteccion contra el acido", 1),
        ACID_INFECTION(ACID_INFECTION_KEY, "Contagion", 1),
        TICTAC(TICTAC_KEY, "TicTac", 1),
        ADRENALINE(ADRENALINE_KEY, "Adrenalina", 1);

        private final Key key;
        private final String displayName;
        private final int maxLevel;
        
        ENCHANTMENTS(Key key, String displayName, int maxLevel) {
            this.key = key;
            this.displayName = displayName;
            this.maxLevel = maxLevel;
        }
        
        public Key getKey() {
            return key;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getMaxLevel() {
            return maxLevel;
        }

        @SuppressWarnings("deprecation")
        public Enchantment toEnchantment() {
            return Registry.ENCHANTMENT.get(key);
        }
    }

}
