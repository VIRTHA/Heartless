package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.EventType;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.TimeConverter;

public class Start implements SubcommandExecutor, TabCompletable {

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
        // El CommandHandler ya ha removido los primeros dos argumentos (grupo y acción)
        // Por lo que args[0] sería el primer argumento real del subcomando (tipo de evento)
        
        switch (args.length) {
            case 1: // Primer argumento del subcomando (tipo de evento)
                return EventType.getEventNames();
            
            case 2: // Segundo argumento del subcomando (duración)
                try {
                    return java.util.Arrays.asList(TimeConverter.getTimeCompletions());
                } catch (NoClassDefFoundError e) {
                    // Fallback en caso de problemas de classloader
                    return java.util.Arrays.asList("30s", "1m", "5m", "10m", "30m", "1h", "2h", "6h", "12h", "1d", "2d", "3d", "7d", "1w", "2w", "1mo");
                }
            
            case 3: // Tercer argumento del subcomando (mundo opcional)
                String currentArg = args[2].toLowerCase();
                return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(worldName -> worldName.toLowerCase().startsWith(currentArg))
                    .collect(java.util.stream.Collectors.toList());
            
            case 4:
                return java.util.Arrays.asList("--force");

            default:
                return Collections.emptyList();
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("htl.events.start")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para reanudar eventos semanales."));
            return;
        }
        
        if (args.length <= 1) {
            sender.sendMessage(MM.toComponent("<red>Uso: <gray>/heartless start <event-type> <duration> [mundo] [--force]"));
            return;
        }
        
        String eventTypeName = args[0];
        long duration = TimeConverter.parseTimeToMillis(args[1]);
        String worldName = null;
        World targetWorld = null;
        boolean force = false;
        
        EventType eventType = EventType.getByName(eventTypeName);
        
        if (eventType == null) {
            sender.sendMessage(MM.toComponent("<red>Tipo de evento desconocido: <yellow>" + eventTypeName));
            return;
        }

        // Verificar si se proporciona un mundo específico
        if (args.length > 2 && !args[2].equalsIgnoreCase("--force")) {
            worldName = args[2];
            targetWorld = Bukkit.getWorld(worldName);
            
            if (targetWorld == null) {
                sender.sendMessage(MM.toComponent("<red>El mundo '" + worldName + "' no existe."));
                return;
            }
        }

        // Verificar el flag --force
        if (args.length > 2) {
            String lastArg = args[args.length - 1];
            if (lastArg.equalsIgnoreCase("--force")) {
                force = true;
            }
        }
        
        // Get the needed instances
        HeartlessMain plugin = HeartlessMain.getInstance();
        WeeklyEventManager eventManager = plugin.getWeeklyEventManager();

        if (force) {
            if (targetWorld != null) {
                eventManager.startEventInWorldFromCommand(eventType, duration, targetWorld);
            } else {
                eventManager.startEventFromCommand(eventType, duration);
            }
            return;
        }

        // Verificar si hay un evento activo usando isEventActive() en lugar de getCurrentEvent()
        if (eventManager.isEventActive()) {
            sender.sendMessage(MM.toComponent("<red>Debes detener el evento actual para inciar otro."));
            return;
        }
        
        // Si no hay evento activo, iniciar el nuevo evento
        if (targetWorld != null) {
            eventManager.startEventInWorldFromCommand(eventType, duration, targetWorld);
            String durationText = TimeConverter.formatTicksToTime(duration / 50L); // Convertir ms a ticks
            sender.sendMessage(MM.toComponent("<green>Evento '" + eventTypeName + "' iniciado exitosamente en el mundo '" + worldName + "' por " + durationText + "."));
        } else {
            eventManager.startEventFromCommand(eventType, duration);
            String durationText = TimeConverter.formatTicksToTime(duration / 50L); // Convertir ms a ticks
            sender.sendMessage(MM.toComponent("<green>Evento '" + eventTypeName + "' iniciado exitosamente en todos los mundos por " + durationText + "."));
        }
    }

}
