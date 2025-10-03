package com.darkbladedev.commands.functions.bonus;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.PermissionBonusManager;
import com.darkbladedev.utils.MM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Comando para eliminar una bonificación por permiso
 */
public class RemoveBonus implements SubcommandExecutor, TabCompletable {

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
        
        // Verificar argumentos
        if (args.length < 1) {
            sender.sendMessage(MM.toComponent("<red>Uso: /heartless bonus remove <permiso>"));
            return;
        }
        
        // Obtener el gestor de bonificaciones
        PermissionBonusManager bonusManager = HeartlessMain.getPermissionBonusManager();
        
        // Obtener argumentos
        String permission = args[0];
        
        // Verificar si existe la bonificación
        if (!bonusManager.getBonusPermissions().containsKey(permission)) {
            sender.sendMessage(MM.toComponent("<red>No existe una bonificación para el permiso " + permission + "."));
            return;
        }
        
        // Eliminar la bonificación
        bonusManager.removeBonusPermission(permission);
        
        // Notificar al remitente
        sender.sendMessage(MM.toComponent("<green>Has eliminado la bonificación para el permiso " + permission + "."));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Devolver la lista de permisos con bonificación
            PermissionBonusManager bonusManager = HeartlessMain.getPermissionBonusManager();
            return new ArrayList<>(bonusManager.getBonusPermissions().keySet());
        }
        return Collections.emptyList();
    }
}