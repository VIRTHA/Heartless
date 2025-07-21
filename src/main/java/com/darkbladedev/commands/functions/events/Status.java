package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.exceptions.CustomException;
import com.darkbladedev.exceptions.ExceptionBuilder;
import com.darkbladedev.exceptions.NullEventException;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.utils.MM;

public class Status implements SubcommandExecutor, TabCompletable {

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        HeartlessMain plugin = HeartlessMain.getInstance();
        StringBuilder status = new StringBuilder();
        status.append("<gray>========== " + HeartlessMain.getInstance().getPrefix() + " ==========</gray>\n");

        // Get current event info
        WeeklyEvent currentEvent = null;
        try {
            currentEvent = plugin.getWeeklyEventManager().getCurrentEvent();
        } catch (Exception e) {
            CustomException ce = ExceptionBuilder.build(NullEventException.class, currentEvent, "El evento solicitado parece ser nulo.");
            ExceptionBuilder.sendToConsole(ce);

            // Informamos al usuario que no hay evento activo
            status.append("<white>Evento activo: <red>Ninguno");
            sender.sendMessage(MM.toComponent(status.toString()));
            return;
        }
        
        if (currentEvent == null) {
            status.append("<white>Evento activo: <red>Ninguno");
            sender.sendMessage(MM.toComponent(status.toString()));
            return;
        }

        // Build status message
        status.append("<white>Evento activo: <aqua>").append(currentEvent.getName()).append("\n");
        status.append("<white>Estado: <aqua>").append(currentEvent.isPaused() ? "<red><b>Paused" : "<green><b>Running").append("\n");

        // Time remaining
        long remainingTime = currentEvent.getRemainingDuration();
        if (remainingTime > 0) {
            long minutes = remainingTime / 60;
            long seconds = remainingTime % 60;
            status.append("<white>Tiempo restante: <aqua>").append(String.format("%02d:%02d", minutes, seconds)).append("\n");
        }

        // Additional event-specific info
        status.append("<white>Recompensas: <aqua>").append("\n");
        for (String reward : currentEvent.getRewards()) {
            status.append("   <green><b>|></b></green> ").append(reward + "\n");
        }

        status.append("<gray>==============================");
        sender.sendMessage(MM.toComponent(status.toString()));
    }
    
}
