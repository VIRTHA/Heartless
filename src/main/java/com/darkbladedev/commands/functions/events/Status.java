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
        status.append("<white>Evento activo: <aqua>").append(currentEvent.getId()).append("\n");
        status.append("<white>Estado: <aqua>").append(currentEvent.isPaused() ? "<red><b>Paused" : "<green><b>Running").append("\n");

        // Time remaining - obtener tiempo restante del WeeklyEventManager
        long remainingTimeMs = plugin.getWeeklyEventManager().getRemainingTime();
        if (remainingTimeMs > 0) {
            String formattedTime = formatDuration(remainingTimeMs);
            status.append("<white>Tiempo restante: <aqua>").append(formattedTime).append("\n");
        } else {
            status.append("<white>Tiempo restante: <red>Finalizando...\n");
        }

        // Additional event-specific info
        status.append("<white>Recompensas: <aqua>").append("\n");
        for (String reward : currentEvent.getRewards()) {
            status.append("   <green><b>|></b></green> ").append(reward + "\n");
        }

        status.append("<gray>==============================");
        sender.sendMessage(MM.toComponent(status.toString()));
    }
    
    /**
     * Formatea una duración en milisegundos a texto legible
     * @param duration Duración en milisegundos
     * @return Texto formateado de la duración
     */
    private String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        
        if (weeks > 0) {
            long remainingDays = days % 7;
            if (remainingDays > 0) {
                return weeks + "w " + remainingDays + "d " + (hours % 24) + "h";
            } else {
                return weeks + "w " + (hours % 24) + "h " + (minutes % 60) + "m";
            }
        } else if (days > 0) {
            return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m " + (seconds % 60) + "s";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
    
}
