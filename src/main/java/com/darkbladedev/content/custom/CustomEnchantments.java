package com.darkbladedev.content.custom;

import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

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
    public static final Key HEAD_DROPPER_KEY = Key.key(namespace, "head_dropper");

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
            case "head_hunter":
            case "cazador_cabezas":
                return HEAD_DROPPER_KEY;
            default:
                return null;
        }
    }

    /**
     * Verifica si un encantamiento personalizado puede ser aplicado a un item específico.
     * @param enchantmentKey La key del encantamiento
     * @param item El item al que se quiere aplicar el encantamiento
     * @return true si el encantamiento puede ser aplicado, false en caso contrario
     */
    public static boolean canApplyEnchantment(Key enchantmentKey, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        
        Material material = item.getType();
        
        // Verificar restricciones específicas para cada encantamiento personalizado
        if (enchantmentKey.equals(ACID_INFECTION_KEY) || enchantmentKey.equals(TICTAC_KEY) || 
            enchantmentKey.equals(FIRST_STRIKE_KEY) || enchantmentKey.equals(HEAD_DROPPER_KEY)) {
            // Solo armas (espadas y hachas)
            return isWeapon(material);
        } else if (enchantmentKey.equals(ACID_RESISTANCE_KEY) || enchantmentKey.equals(ADRENALINE_KEY)) {
            // Solo armaduras
            return isArmor(material);
        } else if (enchantmentKey.equals(CONDIMENT_KEY)) {
            // Solo comida
            return isFood(material);
        }
        
        // Para encantamientos no reconocidos, permitir aplicación
        return true;
    }
    
    /**
     * Verifica si un encantamiento puede ser aplicado a un item específico.
     * Sobrecarga que acepta un objeto Enchantment.
     * @param enchantment El encantamiento
     * @param item El item al que se quiere aplicar el encantamiento
     * @return true si el encantamiento puede ser aplicado, false en caso contrario
     */
    public static boolean canApplyEnchantment(Enchantment enchantment, ItemStack item) {
        if (enchantment == null) {
            return false;
        }
        
        // Convertir el Enchantment a Key y usar el método principal
        Key enchantmentKey = enchantment.getKey();
        return canApplyEnchantment(enchantmentKey, item);
    }
    
    /**
     * Verifica si un material es un arma (espada o hacha).
     */
    private static boolean isWeapon(Material material) {
        return material.name().endsWith("_SWORD") || material.name().endsWith("_AXE");
    }
    
    /**
     * Verifica si un material es una armadura.
     */
    private static boolean isArmor(Material material) {
        return material.name().endsWith("_HELMET") || 
               material.name().endsWith("_CHESTPLATE") || 
               material.name().endsWith("_LEGGINGS") || 
               material.name().endsWith("_BOOTS");
    }
    
    /**
     * Verifica si un material es comida.
     */
    private static boolean isFood(Material material) {
        return material.isEdible();
    }

    public enum ENCHANTMENTS {
        ACID_RESISTANCE(ACID_RESISTANCE_KEY, "Proteccion contra el acido", 1),
        ACID_INFECTION(ACID_INFECTION_KEY, "Contagion", 1),
        TICTAC(TICTAC_KEY, "TicTac", 5),
        CONDIMENT(CONDIMENT_KEY, "Condimento", 3),
        ADRENALINE(ADRENALINE_KEY, "Adrenalina", 1),
        FIRST_STRIKE(FIRST_STRIKE_KEY, "Primer Golpe", 1),
        HEAD_DROPPER(HEAD_DROPPER_KEY, "Cazador de Cabezas", 1);

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
