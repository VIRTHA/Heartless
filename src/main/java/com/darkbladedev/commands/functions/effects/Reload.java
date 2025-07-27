package com.darkbladedev.commands.functions.effects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.CustomEffectsManager;
import com.darkbladedev.utils.MM;

/**
 * Comando para recargar el sistema de efectos personalizados
 */
public class Reload implements SubcommandExecutor, TabCompletable {
    
    private final CustomEffectsManager effectsManager;
    
    public Reload() {
        this.effectsManager = HeartlessMain.getCustomEffectsManager();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heartless.effects.reload")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para recargar el sistema de efectos."));
            return;
        }
        
        effectsManager.reload();
        sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <green>Sistema de efectos personalizados recargado correctamente."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}