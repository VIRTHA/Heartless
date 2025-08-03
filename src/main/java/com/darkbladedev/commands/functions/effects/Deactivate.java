package com.darkbladedev.commands.functions.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.content.semi_custom.CustomEffects;
import com.darkbladedev.managers.CustomEffectsManager;
import com.darkbladedev.utils.MM;

/**
 * Comando para desactivar un efecto personalizado
 */
public class Deactivate implements SubcommandExecutor, TabCompletable {
    
    private final CustomEffectsManager effectsManager;
    
    public Deactivate() {
        this.effectsManager = HeartlessMain.getCustomEffectsManager();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heartless.effects.deactivate")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para desactivar efectos."));
            return;
        }
        
        if (args.length < 1) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>Debes especificar un efecto para desactivar."));
            return;
        }
        
        String effectName = args[0];
        
        if (effectsManager.deactivateEffect(effectName)) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <green>Efecto " + effectName + " desactivado correctamente."));
        } else {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>No se pudo desactivar el efecto. Verifica que exista y esté activo."));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            Set<String> activeEffects = effectsManager.getActiveEffects();
            
            for (Map.Entry<String, CustomEffects> entry : effectsManager.getAllEffects().entrySet()) {
                if (activeEffects.contains(entry.getKey())) {
                    completions.add(entry.getValue().getid());
                }
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
}