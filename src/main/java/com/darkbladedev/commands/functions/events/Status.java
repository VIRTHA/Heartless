package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.utils.MM;

public class Status implements SubcommandExecutor, TabCompletable {

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        StringBuilder status = new StringBuilder();
        status.append("<white>=== " + HeartlessMain.getInstance().getPrefix() + " ===</white>\n");

        // Get current event info
        WeeklyEvent currentEvent = HeartlessMain.getWeeklyEventManager_().getCurrentEvent();
        if (currentEvent == null) {
            status.append("<gray>Evento activo: <red>Ninguno");
            sender.sendMessage(status.toString());
            return;
        }

        // Build status message
        status.append("<gray>Evento activo: <aqua>").append(currentEvent.getName()).append("\n");
        status.append("<gray>Status: <aqua>").append(currentEvent.isPaused() ? "<red><b>Paused" : "<green><b>Running").append("\n");

        // Time remaining
        long remainingTime = currentEvent.getRemainingDuration();
        if (remainingTime > 0) {
            long minutes = remainingTime / 60;
            long seconds = remainingTime % 60;
            status.append("<gray>Tiempo restante: <aqua>").append(String.format("%02d:%02d", minutes, seconds)).append("\n");
        }

        // Additional event-specific info
        status.append("<gray>Recompensas: <aqua>").append(currentEvent.getRewards()).append("\n");

        status.append("<white>==================");
        sender.sendMessage(MM.toComponent(status.toString()));
    }
    
}
