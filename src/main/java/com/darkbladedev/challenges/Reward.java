package com.darkbladedev.challenges;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.MM;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Clase que representa una recompensa para desafíos del sistema Heartless.
 * 
 * Utiliza el esquema rewardType:reward:amount donde:
 * - rewardType: Tipo de recompensa (exp, xp, item, enchantment, tag, money, coins)
 * - reward: Especificación de la recompensa (nombre del item, encantamiento, etc.)
 * - amount: Cantidad o nivel de la recompensa
 * 
 * Tipos de recompensa soportados:
 * - "exp" o "xp": Otorga experiencia al jugador
 * - "item": Otorga un item específico al jugador
 * - "enchantment": Otorga un libro encantado con el encantamiento especificado
 * - "tag": Otorga un permiso de tag al jugador
 * - "money" o "coins": Otorga thalos al jugador mediante comando
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public class Reward {
    
    private static final Logger logger = Logger.getLogger(Reward.class.getName());
    
    // === TIPOS DE RECOMPENSA SOPORTADOS ===
    public enum RewardType {
        EXPERIENCE("exp", "xp"),
        ITEM("item"),
        ENCHANTMENT("enchantment", "enchant"),
        TAG("tag"),
        MONEY("money", "coins"),
        HEALTH("health", "hp");
        
        private final String[] aliases;
        
        RewardType(String... aliases) {
            this.aliases = aliases;
        }
        
        public static RewardType fromString(String type) {
            if (type == null) return null;
            
            String lowerType = type.toLowerCase();
            for (RewardType rewardType : values()) {
                for (String alias : rewardType.aliases) {
                    if (alias.equals(lowerType)) {
                        return rewardType;
                    }
                }
            }
            return null;
        }
        
        public String[] getAliases() {
            return aliases;
        }
    }
    
    // === ATRIBUTOS ===
    private final RewardType type;
    private final String reward;
    private final String amount;
    private final String originalString;
    
    // === CONSTRUCTORES ===
    
    /**
     * Constructor principal que parsea un string con formato rewardType:reward:amount
     * 
     * @param rewardString String con formato "tipo:recompensa:cantidad"
     * @throws IllegalArgumentException si el formato es inválido
     */
    public Reward(String rewardString) {
        if (rewardString == null || rewardString.trim().isEmpty()) {
            throw new IllegalArgumentException("El string de recompensa no puede ser nulo o vacío");
        }
        
        this.originalString = rewardString;
        String[] parts = rewardString.split(":");
        
        if (parts.length < 2) {
            throw new IllegalArgumentException("Formato de recompensa inválido: " + rewardString + 
                ". Formato esperado: tipo:recompensa[:cantidad]");
        }
        
        this.type = RewardType.fromString(parts[0]);
        if (this.type == null) {
            throw new IllegalArgumentException("Tipo de recompensa desconocido: " + parts[0]);
        }
        
        // Para recompensas de dinero (coins/money), el formato es "coins:cantidad"
        // Para otras recompensas, el formato es "tipo:item[:cantidad]"
        if (this.type == RewardType.MONEY) {
            this.reward = "thalos"; // Nombre por defecto para la moneda
            this.amount = parts[1]; // La cantidad está en la segunda posición
        } else {
            this.reward = parts[1];
            this.amount = parts.length >= 3 ? parts[2] : "1";
        }
    }
    
    /**
     * Constructor directo con parámetros separados
     * 
     * @param type Tipo de recompensa
     * @param reward Especificación de la recompensa
     * @param amount Cantidad como string
     */
    public Reward(RewardType type, String reward, String amount) {
        if (type == null) {
            throw new IllegalArgumentException("El tipo de recompensa no puede ser nulo");
        }
        if (reward == null || reward.trim().isEmpty()) {
            throw new IllegalArgumentException("La especificación de recompensa no puede ser nula o vacía");
        }
        
        this.type = type;
        this.reward = reward;
        this.amount = amount != null ? amount : "1";
        this.originalString = type.aliases[0] + ":" + reward + ":" + this.amount;
    }
    
    /**
     * Constructor de conveniencia para recompensas numéricas
     * 
     * @param type Tipo de recompensa
     * @param reward Especificación de la recompensa
     * @param amount Cantidad como número
     */
    public Reward(RewardType type, String reward, int amount) {
        this(type, reward, String.valueOf(amount));
    }
    
    // === MÉTODOS DE OTORGAMIENTO ===
    
    /**
     * Otorga esta recompensa al jugador especificado.
     * 
     * @param player Jugador que recibirá la recompensa
     * @param eventId ID del evento (para logging)
     */
    public void grantTo(Player player, String eventId) {
        if (player == null) {
            logger.warning("[" + eventId + "] No se puede otorgar recompensa: jugador nulo");
            return;
        }
        
        try {
            switch (type) {
                case EXPERIENCE -> grantExperience(player, eventId);
                case ITEM -> grantItem(player, eventId);
                case ENCHANTMENT -> grantEnchantedBook(player, eventId);
                case TAG -> grantTag(player, eventId);
                case MONEY -> grantMoney(player, eventId);
                case HEALTH -> grantHealth(player, eventId);
            }
        } catch (Exception e) {
            logger.warning("[" + eventId + "] Error otorgando recompensa '" + originalString + 
                "' a " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Otorga experiencia al jugador.
     */
    private void grantExperience(Player player, String eventId) {
        try {
            int expAmount = Integer.parseInt(amount);
            player.giveExp(expAmount);
            logger.info("[" + eventId + "] Experiencia otorgada: " + expAmount + " a " + player.getName());
        } catch (NumberFormatException e) {
            logger.warning("[" + eventId + "] Cantidad de experiencia inválida: " + amount);
        }
    }
    
    /**
     * Otorga un item al jugador.
     */
    private void grantItem(Player player, String eventId) {
        try {
            String itemName = reward.toUpperCase();
            int quantity = Integer.parseInt(amount);
            
            Material material = Material.valueOf(itemName);
            ItemStack item = new ItemStack(material, quantity);
            
            // Intentar agregar al inventario
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            
            // Si hay items sobrantes, tirarlos al suelo
            if (!leftover.isEmpty()) {
                for (ItemStack leftoverItem : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
                }
            }
            
            logger.info("[" + eventId + "] Item otorgado: " + quantity + "x " + itemName + " a " + player.getName());
        } catch (IllegalArgumentException e) {
            if (e instanceof NumberFormatException) {
                logger.warning("[" + eventId + "] Cantidad de item inválida: " + amount);
            } else {
                logger.warning("[" + eventId + "] Material inválido: " + reward);
            }
        }
    }
    
    /**
     * Otorga un libro encantado al jugador.
     */
    private void grantEnchantedBook(Player player, String eventId) {
        try {
            String enchantName = reward; // No convertir a mayúsculas
            int level = Integer.parseInt(amount);
            
            // Primero intentar obtener la key usando el mapeo de nombres de display
            net.kyori.adventure.key.Key enchantmentKey = com.darkbladedev.content.custom.CustomEnchantments.getEnchantmentKeyByDisplayName(enchantName);
            
            org.bukkit.enchantments.Enchantment enchantment = null;
            
            if (enchantmentKey != null) {
                // Si encontramos la key usando el mapeo, usarla
                enchantment = HeartlessMain.getContentManager().getEnchantment(enchantmentKey);
            } else {
                // Si no, intentar con el método original (para encantamientos vanilla)
                enchantment = HeartlessMain.getContentManager().getEnchantment(enchantName.toLowerCase());
            }
            
            if (enchantment != null) {
                // Crear libro encantado
                ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
                org.bukkit.inventory.meta.EnchantmentStorageMeta meta = 
                    (org.bukkit.inventory.meta.EnchantmentStorageMeta) enchantedBook.getItemMeta();
                
                if (meta != null) {
                    meta.addStoredEnchant(enchantment, level, true);
                    enchantedBook.setItemMeta(meta);
                    
                    // Agregar al inventario
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(enchantedBook);
                    
                    // Si hay items sobrantes, tirarlos al suelo
                    if (!leftover.isEmpty()) {
                        for (ItemStack leftoverItem : leftover.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
                        }
                    }
                    
                    logger.info("[" + eventId + "] Libro encantado otorgado: " + enchantName + " " + level + " a " + player.getName());
                } else {
                    logger.warning("[" + eventId + "] No se pudo crear el meta del libro encantado");
                }
            } else {
                logger.warning("[" + eventId + "] Encantamiento inválido: " + enchantName);
            }
        } catch (NumberFormatException e) {
            logger.warning("[" + eventId + "] Nivel de encantamiento inválido: " + amount);
        }
    }
    
    /**
     * Otorga un tag al jugador.
     */
    private void grantTag(Player player, String eventId) {
        String tagName = reward;
        // Aquí se integraría con el sistema de tags del plugin
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set htl.tag." + tagName);
        // Por ahora solo loggeamos la acción
        logger.info("[" + eventId + "] Tag otorgado: " + tagName + " a " + player.getName());
        
        // Notificar al jugador
        player.sendMessage(MM.toComponent("<gold>¡Has desbloqueado el tag <yellow>" + tagName + "</yellow>!</gold>"));
    }
    
    /**
     * Otorga thalos (monedas) al jugador.
     */
    private void grantMoney(Player player, String eventId) {
        try {
            // Parsear cantidad base como número y convertir a entero para aplicar bonificación
            double parsedAmount = Double.parseDouble(amount);
            int baseAmount = (int) Math.round(parsedAmount);

            // Aplicar bonificación por permisos si está habilitada
            int finalAmount = baseAmount;
            var bonusManager = HeartlessMain.getInstance().getPermissionBonusManager();
            if (bonusManager != null && bonusManager.isEnabled()) {
                finalAmount = bonusManager.applyBonus(player, baseAmount);
            }

            // Integración con CoinsEngine usando el comando thalos
            if (Bukkit.getPluginManager().getPlugin("CoinsEngine") != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "thalos give " + player.getName() + " " + finalAmount);
                if (finalAmount != baseAmount) {
                    logger.info("[" + eventId + "] Thalos otorgados: " + finalAmount + " (bonificado desde " + baseAmount + ") a " + player.getName());
                } else {
                    logger.info("[" + eventId + "] Thalos otorgados: " + finalAmount + " a " + player.getName());
                }
            } else {
                logger.warning("[" + eventId + "] CoinsEngine no está disponible para otorgar " + finalAmount + " thalos a " + player.getName());
                // Fallback: dar experiencia equivalente
                int expEquivalent = (int)(finalAmount / 10); // 10 thalos = 1 exp
                player.giveExp(expEquivalent);
                player.sendMessage(MM.toComponent("<yellow>¡Has recibido <green>" + expEquivalent + "</green> puntos de experiencia! (CoinsEngine no disponible)</yellow>"));
            }
        } catch (NumberFormatException e) {
            logger.warning("[" + eventId + "] Cantidad de thalos inválida: " + amount);
        }
    }
    
    /**
     * Otorga salud máxima adicional al jugador.
     */
    private void grantHealth(Player player, String eventId) {
        try {
            double healthAmount = Double.parseDouble(amount);
            
            // Obtener la salud máxima actual del jugador
            double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
            double newMaxHealth = currentMaxHealth + healthAmount;
            
            // Verificar si ya ha alcanzado el límite máximo de corazones (40.0 = 20 corazones)
            if (currentMaxHealth >= 40.0) {
                // No mostrar mensaje si ya está en el límite máximo
                logger.info("[" + eventId + "] Jugador " + player.getName() + " ya tiene la salud máxima (20 corazones). No se otorga recompensa.");
                return;
            }
            
            // Limitar la nueva salud máxima al límite de 20 corazones (40.0 puntos de salud)
            newMaxHealth = Math.min(newMaxHealth, 40.0);
            
            // Establecer la nueva salud máxima
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
            
            // Curar al jugador para que aproveche la nueva salud máxima
            player.setHealth(Math.min(player.getHealth() + healthAmount, newMaxHealth));
            
            // Notificar al jugador solo si realmente ganó corazones
            double actualHealthGained = newMaxHealth - currentMaxHealth;
            if (actualHealthGained > 0) {
                player.sendMessage(MM.toComponent("<red>¡Tu salud máxima ha aumentado <yellow>" + actualHealthGained + "</yellow> puntos!</red>"));
                logger.info("[" + eventId + "] Salud máxima otorgada: +" + actualHealthGained + " a " + player.getName() + 
                    " (nueva salud máxima: " + newMaxHealth + ")");
            }
        } catch (NumberFormatException e) {
            logger.warning("[" + eventId + "] Cantidad de salud inválida: " + amount);
        }
    }
    
    // === GETTERS ===
    
    public RewardType getType() {
        return type;
    }
    
    public String getReward() {
        return reward;
    }
    
    public String getAmount() {
        return amount;
    }
    
    public String getOriginalString() {
        return originalString;
    }
    
    /**
     * Convierte la recompensa de vuelta a string para compatibilidad.
     * 
     * @return String en formato rewardType:reward:amount
     */
    @Override
    public String toString() {
        return originalString;
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    /**
     * Valida si el string de recompensa tiene un formato válido.
     * 
     * @param rewardString String a validar
     * @return true si es válido, false en caso contrario
     */
    public static boolean isValidRewardString(String rewardString) {
        try {
            new Reward(rewardString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Crea una recompensa de experiencia.
     * 
     * @param amount Cantidad de experiencia
     * @return Nueva instancia de Reward
     */
    public static Reward experience(int amount) {
        return new Reward(RewardType.EXPERIENCE, "exp", amount);
    }
    
    /**
     * Crea una recompensa de item.
     * 
     * @param material Material del item
     * @param quantity Cantidad del item
     * @return Nueva instancia de Reward
     */
    public static Reward item(Material material, int quantity) {
        return new Reward(RewardType.ITEM, material.name(), quantity);
    }
    
    /**
     * Crea una recompensa de item por nombre.
     * 
     * @param itemName Nombre del item
     * @param quantity Cantidad del item
     * @return Nueva instancia de Reward
     */
    public static Reward item(String itemName, int quantity) {
        return new Reward(RewardType.ITEM, itemName, quantity);
    }
    
    /**
     * Crea una recompensa de encantamiento.
     * 
     * @param enchantmentName Nombre del encantamiento
     * @param level Nivel del encantamiento
     * @return Nueva instancia de Reward
     */
    public static Reward enchantment(String enchantmentName, int level) {
        return new Reward(RewardType.ENCHANTMENT, enchantmentName, level);
    }
    
    /**
     * Crea una recompensa de tag.
     * 
     * @param tagName Nombre del tag
     * @return Nueva instancia de Reward
     */
    public static Reward tag(String tagName) {
        return new Reward(RewardType.TAG, tagName, "1");
    }
    
    /**
     * Crea una recompensa de thalos.
     * 
     * @param amount Cantidad de thalos
     * @return Nueva instancia de Reward
     */
    public static Reward money(double amount) {
        return new Reward(RewardType.MONEY, "thalos", String.valueOf(amount));
    }
    
    /**
     * Crea una recompensa de salud máxima.
     * 
     * @param amount Cantidad de salud máxima a incrementar (en corazones)
     * @return Nueva instancia de Reward
     */
    public static Reward health(double amount) {
        return new Reward(RewardType.HEALTH, "health", String.valueOf(amount));
    }
}