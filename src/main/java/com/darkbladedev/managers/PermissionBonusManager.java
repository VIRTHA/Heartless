package com.darkbladedev.managers;

import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.darkbladedev.HeartlessMain;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Gestor de bonificaciones por permisos para recompensas de desafíos.
 * Permite configurar diferentes niveles de bonificación según los permisos del jugador.
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class PermissionBonusManager {
    
    private final HeartlessMain plugin;
    private final Logger logger;
    private final Map<String, Double> bonusPermissions;
    private boolean enabled;
    
    /**
     * Constructor del gestor de bonificaciones por permisos.
     * 
     * @param plugin Instancia principal del plugin
     */
    public PermissionBonusManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.bonusPermissions = new HashMap<>();
        this.enabled = false;
        loadConfiguration();
    }
    
    /**
     * Carga la configuración de bonificaciones desde config.yml
     */
    public void loadConfiguration() {
        // Usar el ConfigManager central para garantizar que se lea el config.yml correcto
        // y no el FileConfiguration por defecto de JavaPlugin, que puede no estar inicializado.
        FileConfiguration config = plugin.getConfigManager().getConfig();
        
        // Verificar si la sección existe, si no, usar valores predeterminados
        if (!config.contains("reward-bonus")) {
            logger.info("No se encontró configuración de bonificaciones por permisos. Usando valores predeterminados.");
            setDefaults();
            return;
        }
        
        // Cargar estado de activación
        enabled = config.getBoolean("reward-bonus.enabled", false);
        
        // Limpiar bonificaciones anteriores
        bonusPermissions.clear();
        
        // Cargar bonificaciones configuradas
        // Nota: En YamlConfiguration las claves con puntos se interpretan como rutas anidadas.
        // Por ello usamos getValues(true) para obtener las claves completas (p.ej. "htl.bonus.10").
        ConfigurationSection bonusSection = config.getConfigurationSection("reward-bonus.permissions");
        if (bonusSection != null) {
            Map<String, Object> values = bonusSection.getValues(true);
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String fullKey = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Number) {
                    double bonusPercentage = ((Number) value).doubleValue();
                    bonusPermissions.put(fullKey, bonusPercentage / 100.0); // Convertir porcentaje a multiplicador
                    logger.info("Cargada bonificación: " + fullKey + " -> " + bonusPercentage + "%");
                }
            }
        }
        
        logger.info("Sistema de bonificaciones por permisos " + (enabled ? "activado" : "desactivado") + 
                   " con " + bonusPermissions.size() + " niveles configurados.");
    }

    /**
     * Método de callback para recarga de configuración.
     * Invocado por el ConfigManager cuando se ejecuta /heartless reload.
     */
    public void onConfigReload() {
        loadConfiguration();
    }
    
    /**
     * Establece valores predeterminados para las bonificaciones
     */
    private void setDefaults() {
        enabled = true;
        bonusPermissions.put("htl.bonus.5", 0.05);    // 5% de bonificación
        bonusPermissions.put("htl.bonus.10", 0.10); // 10% de bonificación
        bonusPermissions.put("htl.bonus.20", 0.20);    // 20% de bonificación
    }
    
    /**
     * Calcula el multiplicador de bonificación para un jugador según sus permisos.
     * Si el jugador tiene múltiples permisos de bonificación, se aplica el de mayor valor.
     * 
     * @param player Jugador para el que calcular la bonificación
     * @return Multiplicador de bonificación (1.0 = sin bonificación, 1.05 = 5% extra, etc.)
     */
    public double getBonusMultiplier(Player player) {
        if (!enabled || player == null) {
            return 1.0; // Sin bonificación
        }
        
        double highestBonus = 0.0;
        
        // Buscar el permiso con mayor bonificación que tenga el jugador
        for (Map.Entry<String, Double> entry : bonusPermissions.entrySet()) {
            if (player.hasPermission(entry.getKey()) && entry.getValue() > highestBonus) {
                highestBonus = entry.getValue();
            }
        }
        
        // Devolver multiplicador (1.0 + bonificación)
        return 1.0 + highestBonus;
    }
    
    /**
     * Aplica la bonificación a un valor numérico según los permisos del jugador.
     * 
     * @param player Jugador para el que aplicar la bonificación
     * @param originalValue Valor original de la recompensa
     * @return Valor con bonificación aplicada
     */
    public int applyBonus(Player player, int originalValue) {
        double multiplier = getBonusMultiplier(player);
        return (int) Math.round(originalValue * multiplier);
    }
    
    /**
     * Verifica si el sistema de bonificaciones está activado.
     * 
     * @return true si el sistema está activado
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Activa o desactiva el sistema de bonificaciones.
     * 
     * @param enabled Estado de activación
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Obtiene el mapa de permisos y sus bonificaciones.
     * 
     * @return Mapa de permisos y bonificaciones
     */
    public Map<String, Double> getBonusPermissions() {
        return new HashMap<>(bonusPermissions);
    }
    
    /**
     * Establece una bonificación para un permiso específico.
     * 
     * @param permission Permiso a configurar
     * @param bonusPercentage Porcentaje de bonificación (0-100)
     */
    public void setBonusPermission(String permission, double bonusPercentage) {
        bonusPermissions.put(permission, bonusPercentage / 100.0);
    }
    
    /**
     * Elimina una bonificación para un permiso específico.
     * 
     * @param permission Permiso a eliminar
     */
    public void removeBonusPermission(String permission) {
        bonusPermissions.remove(permission);
    }
}