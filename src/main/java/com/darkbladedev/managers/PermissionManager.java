package com.darkbladedev.managers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Clase que gestiona los permisos del plugin VIRTHA-Events
 * Permite verificar si un jugador tiene permisos específicos y obtener valores
 * asociados a esos permisos.
 */
public class PermissionManager {
    
    private static PermissionManager instance;
    @SuppressWarnings("unused")
    private final Plugin plugin;
    
    // Permisos del plugin
    public static final String PERM_BAN_DURATION = "virtha.ban.duration.";
    
    private PermissionManager(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Obtiene la instancia única del gestor de permisos (patrón Singleton)
     * @param plugin Instancia del plugin principal
     * @return La instancia del gestor de permisos
     */
    public static PermissionManager getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new PermissionManager(plugin);
        }
        return instance;
    }
    
    /**
     * Verifica si un jugador tiene un permiso específico
     * @param player El jugador a verificar
     * @param permission El permiso a comprobar
     * @return true si tiene el permiso, false en caso contrario
     */
    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }
    
    /**
     * Obtiene la duración del baneo en horas para un jugador según sus permisos
     * @param player El jugador a verificar
     * @param banCount El número de veces que ha sido baneado
     * @param defaultHours Las horas por defecto si no tiene ningún permiso específico
     * @return La duración del baneo en horas
     */
    public long getBanDurationHours(Player player, int banCount, long defaultHours) {
        // Verificar permisos específicos para diferentes duraciones
        if (player.hasPermission(PERM_BAN_DURATION + "exempt")) {
            return 0; // Exento de baneo
        }
        
        // Comprobar permisos de duración específica (de menor a mayor)
        int[] hourOptions = {1, 2, 3, 4, 6, 12, 24};
        
        for (int hours : hourOptions) {
            if (player.hasPermission(PERM_BAN_DURATION + hours)) {
                return hours;
            }
        }
        
        // Si no tiene ningún permiso específico, usar la duración por defecto
        return defaultHours;
    }
}