package com.darkbladedev.commands.functions.bonus;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.PermissionBonusManager;
import com.darkbladedev.utils.MM;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Comando para añadir una bonificación por permiso
 */
public class AddBonus implements SubcommandExecutor, TabCompletable {

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
        if (args.length < 2) {
            sender.sendMessage(MM.toComponent("<red>Uso: /heartless bonus add <permiso> <porcentaje>"));
            sender.sendMessage(MM.toComponent("<gray>Ejemplo: /heartless bonus add heartless.bonus.vip 10"));
            return;
        }
        
        // Obtener el gestor de bonificaciones
        PermissionBonusManager bonusManager = HeartlessMain.getPermissionBonusManager();
        
        // Obtener argumentos
        String permission = args[0];
        double percentage;
        
        try {
            percentage = Double.parseDouble(args[1]);
            if (percentage <= 0) {
                sender.sendMessage(MM.toComponent("<red>El porcentaje debe ser un número positivo."));
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(MM.toComponent("<red>El porcentaje debe ser un número válido."));
            return;
        }
        
        // Añadir la bonificación
        bonusManager.setBonusPermission(permission, percentage);
        
        // Notificar al remitente
        sender.sendMessage(MM.toComponent("<green>Has añadido una bonificación del " + percentage + "% para el permiso " + permission + "."));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("<permiso>");
        } else if (args.length == 2) {
            return Arrays.asList("5", "10", "15", "20", "25");
        }
        return Collections.emptyList();
    }
}