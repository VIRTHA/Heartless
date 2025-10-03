package com.darkbladedev.commands.functions.bonus;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.managers.PermissionBonusManager;
import com.darkbladedev.utils.MM;

/**
 * Comando para habilitar el sistema de bonificaciones por permisos
 */
public class EnableBonus implements SubcommandExecutor {

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
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para gestionar el sistema de bonificaciones."));
            return;
        }
        
        // Obtener el gestor de bonificaciones
        PermissionBonusManager bonusManager = HeartlessMain.getPermissionBonusManager();
        
        // Verificar si ya está habilitado
        if (bonusManager.isEnabled()) {
            sender.sendMessage(MM.toComponent("<red>El sistema de bonificaciones ya está habilitado."));
            return;
        }
        
        // Habilitar el sistema
        bonusManager.setEnabled(true);
        
        // Notificar al remitente
        sender.sendMessage(MM.toComponent("<green>Has habilitado el sistema de bonificaciones por permisos."));
    }
}