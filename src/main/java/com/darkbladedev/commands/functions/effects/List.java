package com.darkbladedev.commands.functions.effects;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.content.semi_custom.CustomEffectsBase;
import com.darkbladedev.managers.CustomEffectsManager;
import com.darkbladedev.utils.MM;

/**
 * Comando para listar todos los efectos personalizados disponibles
 */
public class List implements SubcommandExecutor, TabCompletable {
    
    private final CustomEffectsManager effectsManager;
    
    public List() {
        this.effectsManager = HeartlessMain.getCustomEffectsManager();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heartless.effects.list")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para ver la lista de efectos."));
            return;
        }
        
        Map<String, CustomEffectsBase> allEffects = effectsManager.getAllEffects();
        Set<String> activeEffects = effectsManager.getActiveEffects();
        
        sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <yellow>Efectos personalizados disponibles:"));
        
        if (allEffects.isEmpty()) {
            sender.sendMessage(MM.toComponent("  <gray>No hay efectos personalizados registrados."));
            return;
        }
        
        for (Map.Entry<String, CustomEffectsBase> entry : allEffects.entrySet()) {
            String effectName = entry.getValue().getid();
            boolean isActive = activeEffects.contains(entry.getKey());
            
            String status = isActive ? "<green>Activo</green>" : "<red>Inactivo</red>";
            sender.sendMessage(MM.toComponent("  <gray>- <white>" + effectName + " <gray>|> " + status));
        }
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}