package com.darkbladedev.commands.functions.bonus;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.managers.PermissionBonusManager;
import com.darkbladedev.utils.MM;

import java.util.Map;

/**
 * Comando para listar las bonificaciones por permisos configuradas
 */
public class ListBonus implements SubcommandExecutor {

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
        
        // Verificar si el sistema está habilitado
        boolean enabled = bonusManager.isEnabled();
        
        // Obtener las bonificaciones
        Map<String, Double> bonuses = bonusManager.getBonusPermissions();
        
        // Mostrar información
        sender.sendMessage(MM.toComponent("<gray>----- <gold>Sistema de Bonificaciones</gold> -----"));
        sender.sendMessage(MM.toComponent("<gray>Estado: " + (enabled ? "<green>Habilitado" : "<red>Deshabilitado")));
        
        if (bonuses.isEmpty()) {
            sender.sendMessage(MM.toComponent("<gray>No hay bonificaciones configuradas."));
        } else {
            sender.sendMessage(MM.toComponent("<gray>Bonificaciones configuradas:"));
            for (Map.Entry<String, Double> entry : bonuses.entrySet()) {
                sender.sendMessage(MM.toComponent("<gray>- <yellow>" + entry.getKey() + "</yellow>: <green>" + entry.getValue() + "%"));
            }
        }
        
        sender.sendMessage(MM.toComponent("<gray>--------------------------------"));
    }
}