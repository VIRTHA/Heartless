package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.MM;

public class Pause implements SubcommandExecutor, TabCompletable {

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // Los args aquí incluyen todos los argumentos del comando, incluyendo grupo y acción
        // Necesitamos ajustar el índice para que coincida con los argumentos específicos de este subcomando
        // args[0] y args[1] son el grupo y la acción, por lo que args[2] sería el primer argumento real del subcomando
        // En este caso no hay argumentos adicionales para este comando
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("heartless.events.pause")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para pausar eventos semanales."));
            return;
        }
        
        // Obtener el gestor de eventos semanales
        WeeklyEventManager eventManager = HeartlessMain.getWeeklyEventManager_();
        
        // Verificar si hay un evento activo
        if (!eventManager.isEventActive()) {
            sender.sendMessage(MM.toComponent("<red>No hay ningún evento activo para pausar."));
            return;
        }
        
        // Verificar si el evento ya está pausado
        if (eventManager.isPaused()) {
            sender.sendMessage(MM.toComponent("<red>El evento ya está pausado."));
            return;
        }
        
        // Pausar el evento
        eventManager.pauseCurrentEvent();
        
        // Notificar al remitente
        sender.sendMessage(MM.toComponent("<green>Has pausado el evento semanal actual."));
    }
    
}
