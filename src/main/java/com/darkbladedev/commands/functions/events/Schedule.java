package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.TimeConverter;

public class Schedule implements SubcommandExecutor, TabCompletable {

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of(TimeConverter.getTimeCompletions());
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("heartless.events.schedule")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para programar eventos semanales."));
            return;
        }
        
        // Verificar argumentos
        if (args.length < 1) {
            sender.sendMessage(MM.toComponent("<red>Uso: /heartless events schedule <tiempo>"));
            sender.sendMessage(MM.toComponent("<gray>Ejemplo: /heartless events schedule 1d12h"));
            return;
        }
        
        // Obtener el gestor de eventos semanales
        WeeklyEventManager eventManager = HeartlessMain.getWeeklyEventManager_();
        
        // Verificar si hay un evento activo
        if (!eventManager.isEventActive()) {
            sender.sendMessage(MM.toComponent("<red>No hay ningún evento activo para programar el siguiente."));
            return;
        }
        
        // Programar el siguiente evento
        String timeString = args[0];
        eventManager.scheduleNextEvent(timeString);
        
        // Notificar al remitente
        sender.sendMessage(MM.toComponent("<green>Has programado el siguiente evento para dentro de " + timeString + "."));
    }
    
}
