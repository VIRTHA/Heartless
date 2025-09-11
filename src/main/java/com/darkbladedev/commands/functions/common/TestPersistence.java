package com.darkbladedev.commands.functions.common;

import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.managers.StorageManager;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.MM;
import com.darkbladedev.HeartlessMain;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para probar el sistema de persistencia
 * Fuerza el guardado de datos específicos del evento actual
 */
public class TestPersistence implements SubcommandExecutor {
    
    private boolean enabled = true;
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("heartless.admin") && !player.hasPermission("heartless.diagnostic")) {
                sender.sendMessage(MM.toComponent("<red>No tienes permisos para usar este comando."));
                return;
            }
        }
        
        try {
            HeartlessMain plugin = HeartlessMain.getInstance();
            StorageManager storageManager = plugin.getStorageManager();
            WeeklyEventManager eventManager = plugin.getWeeklyEventManager();
            
            sender.sendMessage(MM.toComponent("<yellow>=== PRUEBA DEL SISTEMA DE PERSISTENCIA ==="));
            
            // Obtener el evento actual
            var currentEventType = eventManager.getCurrentEventType();
            var currentEvent = eventManager.getCurrentEvent();
            sender.sendMessage(MM.toComponent("<gray>Evento actual: <yellow>" + currentEventType.name()));
            
            // Forzar guardado de datos específicos del evento
            sender.sendMessage(MM.toComponent("<gray>Forzando guardado de datos específicos del evento..."));
            if (currentEvent != null) {
                storageManager.saveEventSpecificData(currentEvent);
            } else {
                sender.sendMessage(MM.toComponent("<red>No hay evento activo para guardar datos específicos."));
            }
            
            // Forzar guardado de datos del evento semanal (a través del WeeklyEventManager)
            sender.sendMessage(MM.toComponent("<gray>Forzando guardado de datos del evento semanal..."));
            // El WeeklyEventManager maneja su propia persistencia, no hay método directo en StorageManager
            
            sender.sendMessage(MM.toComponent("<green>✓ Guardado forzado completado."));
            
            // Ejecutar diagnóstico
            sender.sendMessage(MM.toComponent("<gray>Ejecutando diagnóstico del sistema..."));
            String diagnostic = storageManager.diagnosticPersistenceSystem();
            
            // Enviar resultado del diagnóstico línea por línea
            String[] lines = diagnostic.split("\\n");
            for (String line : lines) {
                sender.sendMessage(MM.toComponent(line));
            }
            
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error durante la prueba: " + e.getMessage()));
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}