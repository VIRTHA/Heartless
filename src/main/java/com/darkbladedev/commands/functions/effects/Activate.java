package com.darkbladedev.commands.functions.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.content.semi_custom.CustomEffects;
import com.darkbladedev.managers.CustomEffectsManager;
import com.darkbladedev.utils.MM;

/**
 * Comando para activar un efecto personalizado
 */
public class Activate implements SubcommandExecutor, TabCompletable {
    
    private final CustomEffectsManager effectsManager;
    
    public Activate() {
        this.effectsManager = HeartlessMain.getCustomEffectsManager();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heartless.effects.activate")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para activar efectos."));
            return;
        }
        
        if (args.length < 1) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>Debes especificar un efecto para activar."));
            return;
        }
        
        String effectName = args[0];
        
        if (effectsManager.activateEffect(effectName)) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <green>Efecto " + effectName + " activado correctamente."));
        } else {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>No se pudo activar el efecto. Verifica que exista y no esté ya activo."));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length - 2 == 1) {
            List<String> completions = new ArrayList<>();
            
            for (Map.Entry<String, CustomEffects> entry : effectsManager.getAllEffects().entrySet()) {
                if (!effectsManager.getActiveEffects().contains(entry.getKey())) {
                    completions.add(entry.getValue().getid());
                }
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
}