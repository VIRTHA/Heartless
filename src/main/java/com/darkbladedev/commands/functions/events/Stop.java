package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.MM;

public class Stop implements SubcommandExecutor, TabCompletable {

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // No hay argumentos adicionales para este comando
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("heartless.events.stop")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para detener eventos semanales."));
            return;
        }
        
        // Obtener el gestor de eventos semanales
        WeeklyEventManager eventManager = HeartlessMain.getWeeklyEventManager_();
        
        // Verificar si hay un evento activo
        if (!eventManager.isEventActive()) {
            sender.sendMessage(MM.toComponent("<red>No hay ningún evento activo para detener."));
            return;
        }
        
        // Detener el evento actual
        eventManager.forceStopCurrentEvent();
        
        // Notificar al remitente
        sender.sendMessage(MM.toComponent("<green>Has detenido el evento semanal actual."));
    }

}
