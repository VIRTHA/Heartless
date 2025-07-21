package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.EventType;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.TimeConverter;

public class Start implements SubcommandExecutor, TabCompletable {

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // Los args aquí incluyen todos los argumentos del comando, incluyendo grupo y acción
        // Necesitamos ajustar el índice para que coincida con los argumentos específicos de este subcomando
        // args[0] y args[1] son el grupo y la acción, por lo que args[2] es el primer argumento real del subcomando
        
        // Calculamos el índice real restando 2 (grupo y acción)
        int adjustedIndex = args.length - 2;
        
        switch (adjustedIndex) {
            case 1: // Primer argumento del subcomando (tipo de evento)
                return EventType.getEventNames();
            
            case 2: // Segundo argumento del subcomando (duración)
                return java.util.Arrays.asList(TimeConverter.getTimeCompletions());
            
            case 3:
                return java.util.Arrays.asList("--force");

            default:
                return Collections.emptyList();
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("heartless.events.start")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para reanudar eventos semanales."));
            return;
        }
        
        if (args.length <= 1) {
            sender.sendMessage(MM.toComponent("<red>Uso: <gray>/heartless start <event-type> <duration> [--force]"));
            return;
        }
        
        String eventTypeName = args[0];
        long duration = TimeConverter.parseTimeToTicks(args[1]);
        boolean force = false;
        
        EventType eventType = EventType.getByName(eventTypeName);
        
        if (eventType == null) {
            sender.sendMessage(MM.toComponent("<red>Tipo de evento desconocido: <yellow>" + eventTypeName));
            return;
        }

        if (args.length > 2 && args[2] != null && args[2].equalsIgnoreCase("--force")) {
            force = true;
        }
        
        // Get the needed instances
        HeartlessMain plugin = HeartlessMain.getInstance();
        WeeklyEventManager eventManager = plugin.getWeeklyEventManager();

        if (force) {
            eventManager.startEventFromCommand(eventType, duration);
            return;
        }

        // Verificar si hay un evento activo usando isEventActive() en lugar de getCurrentEvent()
        if (eventManager.isEventActive()) {
            sender.sendMessage(MM.toComponent("<red>Debes detener el evento actual para inciar otro."));
            return;
        }
        
        // Si no hay evento activo, iniciar el nuevo evento
        eventManager.startEventFromCommand(eventType, duration);
    }

}
