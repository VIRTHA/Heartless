package com.darkbladedev.commands.functions.common;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.utils.MM;

public class Reload implements SubcommandExecutor, TabCompletable {

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
        if (!sender.hasPermission("heartless.reload")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso recargar el plugin."));
            return;
        }

        HeartlessMain plugin = HeartlessMain.getInstance();

        try {
            // Recargar configuración principal usando ConfigManager
            if (HeartlessMain.getConfigManager() != null) {
                HeartlessMain.getConfigManager().reloadConfig();
                sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Configuración <green>recargada</green>."));
            }
            
            // Reinicializar los sistemas principales
            reloadSystems(plugin, sender);
            
            // Mensaje de éxito
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <green>Plugin recargado correctamente."));
        } catch (Exception e) {
            // Mensaje de error
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <red>Error al recargar el plugin: " + e.getMessage()));
            e.printStackTrace();
        }
    }
    
    private void reloadSystems(HeartlessMain plugin, CommandSender sender) {
        // Recargar EventManager (que incluye WeeklyEventManager)
        try {
            plugin.getEventManager().reload();
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Sistema de eventos <green>recargado</green>."));
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Sistema de eventos <red>no recargado: " + e.getMessage() + "</red>"));
        }
        
        // Recargar BanManager
        try {
            plugin.getBanManager().reloadBanData();
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Sistema de baneos <green>recargado</green>."));
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Sistema de baneos <red>no recargado: " + e.getMessage() + "</red>"));
        }
        
        // Recargar StorageManager
        try {
            plugin.getStorageManager().reloadEventData();
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Sistema de almacenamiento <green>recargado</green>."));
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Sistema de almacenamiento <red>no recargado: " + e.getMessage() + "</red>"));
        }
        
        // Recargar PlaceholderApiManager
        try {
            if (plugin.getPapiManager() != null) {
                // Asegurar que los placeholders estén registrados
                plugin.getPapiManager().register();
                sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Sistema de placeholders <green>recargado</green>."));
            }
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Sistema de placeholders <red>no recargado: " + e.getMessage() + "</red>"));
        }
        
        // Recargar WeeklyEventTaskOptimizer
        try {
            if (HeartlessMain.getTaskOptimizer() != null) {
                HeartlessMain.getTaskOptimizer().reload();
                sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Optimizador de tareas <green>recargado</green>."));
            }
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Optimizador de tareas <red>no recargado: " + e.getMessage() + "</red>"));
        }
        
        // Recargar WeeklyEventMigrationManager
        try {
            if (HeartlessMain.getMigrationManager() != null) {
                HeartlessMain.getMigrationManager().reload();
                sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Sistema de migración <green>recargado</green>."));
            }
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <gray>Sistema de migración <red>no recargado: " + e.getMessage() + "</red>"));
        }
    }
}
