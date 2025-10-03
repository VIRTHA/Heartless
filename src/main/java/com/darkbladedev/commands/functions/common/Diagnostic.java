package com.darkbladedev.commands.functions.common;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.utils.MM;

/**
 * Comando para diagnosticar el estado del sistema de persistencia
 * Verifica archivos, directorios y permisos del sistema de almacenamiento
 */
public class Diagnostic implements SubcommandExecutor {

    private boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("htl.admin") && !sender.hasPermission("htl.diagnostic")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para usar este comando."));
            return;
        }

        HeartlessMain plugin = HeartlessMain.getInstance();
        
        try {
            // Ejecutar diagnóstico del sistema de persistencia
            String diagnosticResult = plugin.getStorageManager().diagnosticPersistenceSystem();
            
            // Enviar resultado al usuario
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <yellow>Diagnóstico del Sistema de Persistencia:"));
            sender.sendMessage(MM.toComponent(diagnosticResult));
            
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <red>Error al ejecutar el diagnóstico: " + e.getMessage()));
            plugin.getLogger().severe("Error en diagnóstico de persistencia: " + e.getMessage());
            e.printStackTrace();
        }
    }
}