package com.darkbladedev.commands.functions.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.content.semi_custom.CustomEffects;
import com.darkbladedev.managers.CustomEffectsManager;
import com.darkbladedev.utils.MM;

/**
 * Comando para mostrar información detallada sobre un efecto personalizado
 */
public class Info implements SubcommandExecutor, TabCompletable {
    
    private final CustomEffectsManager effectsManager;
    
    public Info() {
        this.effectsManager = HeartlessMain.getCustomEffectsManager();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heartless.effects.info")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para ver información de efectos."));
            return;
        }
        
        if (args.length < 1) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>Debes especificar un efecto para ver su información."));
            return;
        }
        
        String effectName = args[0];
        CustomEffects effect = effectsManager.getEffect(effectName);
        
        if (effect == null) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>El efecto " + effectName + " no existe."));
            return;
        }
        
        Set<String> activeEffects = effectsManager.getActiveEffects();
        boolean isActive = activeEffects.contains(effectName.toLowerCase());
        
        sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <yellow>Información del efecto " + effect.getPrefix() + ":"));
        sender.sendMessage(MM.toComponent("  <gray>Estado: " + (isActive ? "<green>Activo</green>" : "<red>Inactivo</red>")));
        
        // Obtener jugadores afectados
        Set<UUID> affectedPlayers = effectsManager.getAffectedPlayers(effectName);
        sender.sendMessage(MM.toComponent("  <gray>Jugadores afectados: <white>" + affectedPlayers.size()));
        
        if (!affectedPlayers.isEmpty()) {
            StringBuilder playerList = new StringBuilder();
            int count = 0;
            
            for (UUID playerId : affectedPlayers) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    if (count > 0) {
                        playerList.append(", ");
                    }
                    playerList.append(player.getName());
                    count++;
                    
                    // Limitar a 5 jugadores para no saturar el chat
                    if (count >= 5 && affectedPlayers.size() > 5) {
                        playerList.append(" y ").append(affectedPlayers.size() - 5).append(" más");
                        break;
                    }
                }
            }
            
            sender.sendMessage(MM.toComponent("  <gray>Lista: <white>" + playerList.toString()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length - 2 == 1) {
            List<String> completions = new ArrayList<>();
            
            for (Map.Entry<String, CustomEffects> entry : effectsManager.getAllEffects().entrySet()) {
                completions.add(entry.getValue().getid());
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
}
