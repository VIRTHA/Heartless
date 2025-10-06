package com.darkbladedev.commands.functions.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.MM;

public class Stop implements SubcommandExecutor, TabCompletable {

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
        // Por lo que args[0] sería el primer argumento real del subcomando (mundo opcional)
        
        // Si estamos completando el primer argumento (mundo opcional)
        // args.length == 0 significa "/hs event stop " (sin argumentos adicionales, pero con espacio)
        // args.length == 1 significa "/hs event stop <parcial>" (escribiendo el mundo)
        if (args.length == 0 || args.length == 1) {
            List<String> worldNames = new ArrayList<>();
            String currentArg = args.length == 1 ? args[0].toLowerCase() : "";
            
            for (World world : Bukkit.getWorlds()) {
                String worldName = world.getName();
                // Filtrar mundos que coincidan con lo que el usuario está escribiendo
                if (worldName.toLowerCase().startsWith(currentArg)) {
                    worldNames.add(worldName);
                }
            }
            return worldNames;
        }
        
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("htl.events.stop")) {
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
        
        // Verificar si se especificó un mundo
        if (args.length >= 3) {
            String worldName = args[2];
            World world = Bukkit.getWorld(worldName);
            
            if (world == null) {
                sender.sendMessage(MM.toComponent("<red>El mundo '" + worldName + "' no existe o no está cargado."));
                return;
            }
            
            // Detener el evento en el mundo específico
            boolean success = eventManager.stopEventInWorldFromCommand(world);
            
            if (success) {
                sender.sendMessage(MM.toComponent("<green>Has detenido el evento semanal en el mundo '" + worldName + "'."));
            } else {
                sender.sendMessage(MM.toComponent("<red>No se pudo detener el evento en el mundo '" + worldName + "'."));
            }
        } else {
            // Detener el evento en todos los mundos (comportamiento original)
            eventManager.forceStopCurrentEvent();
            sender.sendMessage(MM.toComponent("<green>Has detenido el evento semanal actual en todos los mundos."));
        }
    }

}
