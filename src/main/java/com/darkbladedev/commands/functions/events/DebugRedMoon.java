package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.utils.MM;

/**
 * Comando de debug temporal para forzar la activación de la Luna Roja
 * Solo funciona cuando el evento UndeadWeek está activo
 */
public class DebugRedMoon implements SubcommandExecutor, TabCompletable {
    
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
        // Verificar permisos
        if (!sender.hasPermission("htl.admin")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para usar este comando de debug."));
            return;
        }
        
        try {
            // Obtener el evento actual
            WeeklyEvent currentEvent = HeartlessMain.getInstance().getWeeklyEventManager().getCurrentEvent();
            
            if (currentEvent == null) {
                sender.sendMessage(MM.toComponent("<red>No hay ningún evento activo."));
                return;
            }
            
            // Verificar si es UndeadWeek
            if (!(currentEvent instanceof UndeadWeek)) {
                sender.sendMessage(MM.toComponent("<red>El evento actual no es UndeadWeek. Evento actual: " + currentEvent.getId()));
                return;
            }
            
            UndeadWeek undeadWeek = (UndeadWeek) currentEvent;
            
            // Forzar activación de Luna Roja
            sender.sendMessage(MM.toComponent("<yellow>Forzando activación de Luna Roja..."));
            undeadWeek.forceActivateRedMoon();
            sender.sendMessage(MM.toComponent("<green>Comando ejecutado. Revisa la consola para logs de debug."));
            
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error al ejecutar comando de debug: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}