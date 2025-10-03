package com.darkbladedev.commands.functions.bonus;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.managers.PermissionBonusManager;
import com.darkbladedev.utils.MM;

/**
 * Comando para probar el sistema de bonificaciones por permisos
 * Permite verificar si las bonificaciones se están aplicando correctamente
 */
public class TestBonus implements SubcommandExecutor {

    private boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("htl.admin.bonus")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para probar el sistema de bonificaciones."));
            return;
        }
        
        // Solo jugadores pueden usar este comando
        if (!(sender instanceof Player)) {
            sender.sendMessage(MM.toComponent("<red>Este comando solo puede ser usado por jugadores."));
            return;
        }
        
        Player player = (Player) sender;
        
        // Obtener el gestor de bonificaciones
        PermissionBonusManager bonusManager = HeartlessMain.getInstance().getPermissionBonusManager();
        
        if (bonusManager == null) {
            sender.sendMessage(MM.toComponent("<red>Error: PermissionBonusManager no está disponible."));
            return;
        }
        
        // Verificar si el sistema está habilitado
        if (!bonusManager.isEnabled()) {
            sender.sendMessage(MM.toComponent("<red>El sistema de bonificaciones está deshabilitado."));
            return;
        }
        
        // Mostrar información del sistema
        sender.sendMessage(MM.toComponent("<yellow>========== PRUEBA DE BONIFICACIONES =========="));
        sender.sendMessage(MM.toComponent("<green>Sistema de bonificaciones: <white>HABILITADO"));
        sender.sendMessage(MM.toComponent(""));
        
        // Probar con diferentes valores
        int[] testValues = {100, 500, 1000, 2500};
        
        sender.sendMessage(MM.toComponent("<yellow>Tus permisos de bonificación:"));
        
        // Verificar cada permiso de bonificación
        boolean hasAnyBonus = false;
        for (String permission : bonusManager.getBonusPermissions().keySet()) {
            if (player.hasPermission(permission)) {
                double percentage = bonusManager.getBonusPermissions().get(permission) * 100;
                sender.sendMessage(MM.toComponent("<green>✓ " + permission + " <gray>(" + percentage + "%)"));
                hasAnyBonus = true;
            } else {
                sender.sendMessage(MM.toComponent("<red>✗ " + permission));
            }
        }
        
        if (!hasAnyBonus) {
            sender.sendMessage(MM.toComponent("<red>No tienes ningún permiso de bonificación."));
        }
        
        sender.sendMessage(MM.toComponent(""));
        sender.sendMessage(MM.toComponent("<yellow>Multiplicador actual: <white>" + String.format("%.2f", bonusManager.getBonusMultiplier(player))));
        sender.sendMessage(MM.toComponent(""));
        
        // Mostrar ejemplos de aplicación
        sender.sendMessage(MM.toComponent("<yellow>Ejemplos de aplicación:"));
        for (int value : testValues) {
            int bonusValue = bonusManager.applyBonus(player, value);
            int difference = bonusValue - value;
            
            if (difference > 0) {
                sender.sendMessage(MM.toComponent("<gray>" + value + " → <green>" + bonusValue + " <gray>(+" + difference + ")"));
            } else {
                sender.sendMessage(MM.toComponent("<gray>" + value + " → <white>" + bonusValue + " <gray>(sin bonificación)"));
            }
        }
        
        sender.sendMessage(MM.toComponent("<yellow>============================================="));
    }
}