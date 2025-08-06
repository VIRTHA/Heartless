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
        // Los args aquí incluyen todos los argumentos del comando, incluyendo grupo y acción
        // Necesitamos ajustar el índice para que coincida con los argumentos específicos de este subcomando
        // args[0] y args[1] son el grupo y la acción, por lo que args[2] es el primer argumento real del subcomando
        
        // Calculamos el índice real restando 2 (grupo y acción)
        int adjustedIndex = args.length;
        
        if (adjustedIndex == 1) { // Primer argumento del subcomando (tiempo)
            return java.util.Arrays.asList(TimeConverter.getTimeCompletions());
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
