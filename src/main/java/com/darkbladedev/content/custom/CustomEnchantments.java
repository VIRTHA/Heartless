package com.darkbladedev.content.custom;

import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;

public class CustomEnchantments {

    private static String namespace = "heartless";

    public static final Key ACID_RESISTANCE_KEY = Key.key(namespace, "acid_resistance");
    public static final Key ACID_INFECTION_KEY = Key.key(namespace, "acid_infection");
    public static final Key TICTAC_KEY = Key.key(namespace, "tictac");
    public static final Key CONDIMENT_KEY = Key.key(namespace, "condiment");
    public static final Key ADRENALINE_KEY = Key.key(namespace, "adrenaline");
    public static final Key FIRST_STRIKE_KEY = Key.key(namespace, "first_strike");

    public Enchantment getEnchantment(Key key) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(key);
    }
    
    /**
     * Mapea nombres de display de encantamientos a sus keys reales.
     * Esto permite usar nombres como "contagion" en lugar de la key técnica.
     */
    public static Key getEnchantmentKeyByDisplayName(String displayName) {
        String lowerName = displayName.toLowerCase();
        
        switch (lowerName) {
            case "contagion":
            case "acid_infection":
                return ACID_INFECTION_KEY;
            case "acid_resistance":
            case "proteccion_acido":
                return ACID_RESISTANCE_KEY;
            case "tictac":
                return TICTAC_KEY;
            case "condiment":
            case "condimento":
                return CONDIMENT_KEY;
            case "adrenaline":
            case "adrenalina":
                return ADRENALINE_KEY;
            case "first_strike":
            case "primer_golpe":
                return FIRST_STRIKE_KEY;
            default:
                return null;
        }
    }

    public enum ENCHANTMENTS {
        ACID_RESISTANCE(ACID_RESISTANCE_KEY, "Proteccion contra el acido", 1),
        ACID_INFECTION(ACID_INFECTION_KEY, "Contagion", 1),
        TICTAC(TICTAC_KEY, "TicTac", 5),
        CONDIMENTO(CONDIMENT_KEY, "Condimento", 3),
        ADRENALINE(ADRENALINE_KEY, "Adrenalina", 1),
        FIRST_STRIKE(FIRST_STRIKE_KEY, "Primer Golpe", 1);

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
