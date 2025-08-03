package com.darkbladedev.commands.functions.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.MM;

/**
 * Comando para resetear/limpiar el estado del gestor de eventos semanales
 * Especialmente útil cuando el sistema se queda atascado en estado 'empty'
 */
public class Reset implements SubcommandExecutor, TabCompletable {

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("heartless.events.reset")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para resetear el estado de eventos semanales."));
            return;
        }
        
        // Obtener el gestor de eventos semanales
        WeeklyEventManager eventManager = HeartlessMain.getWeeklyEventManager_();
        
        // Confirmar la acción si hay argumentos
        if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
            // Forzar limpieza del estado
            eventManager.forceCleanState();
            
            // Notificar al remitente
            sender.sendMessage(MM.toComponent("<green>Estado del gestor de eventos reseteado correctamente."));
            sender.sendMessage(MM.toComponent("<yellow>Puedes iniciar un nuevo evento ahora."));
        } else {
            // Mostrar información y pedir confirmación
            sender.sendMessage(MM.toComponent("<yellow><b>¡ADVERTENCIA!</b>"));
            sender.sendMessage(MM.toComponent("<gray>Este comando forzará la limpieza completa del estado de eventos."));
            sender.sendMessage(MM.toComponent("<gray>Esto detendrá cualquier evento activo y limpiará todos los datos."));
            sender.sendMessage(MM.toComponent("<gray>Úsalo solo si el sistema está atascado (ej: en estado 'empty')."));
            sender.sendMessage(MM.toComponent(""));
            sender.sendMessage(MM.toComponent("<red>Para confirmar, usa: <white>/heartless events reset confirm"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("confirm");
            return completions;
        }
        return Collections.emptyList();
    }
}