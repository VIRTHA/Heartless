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
 * Comando para eliminar un efecto personalizado de un jugador
 */
public class Clear implements SubcommandExecutor, TabCompletable {
    
    private final CustomEffectsManager effectsManager;
    
    public Clear() {
        this.effectsManager = HeartlessMain.getCustomEffectsManager();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heartless.effects.clear")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para eliminar efectos."));
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>Uso correcto: /heartless effects clear <efecto> <jugador>"));
            return;
        }
        
        String effectName = args[0];
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        
        if (target == null || !target.isOnline()) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>El jugador " + playerName + " no está en línea."));
            return;
        }
        
        if (effectsManager.removeEffectFromPlayer(effectName, target)) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <green>Efecto " + effectName + " eliminado de " + playerName + " correctamente."));
        } else {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>No se pudo eliminar el efecto. Verifica que exista y que el jugador esté afectado."));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        int adjustedIndex = args.length - 2;

        if (adjustedIndex == 1) {
            // Completar con nombres de efectos activos
            for (Map.Entry<String, CustomEffects> entry : effectsManager.getAllEffects().entrySet()) {
                if (effectsManager.getActiveEffects().contains(entry.getKey())) {
                    completions.add(entry.getValue().getid());
                }
            }
        } else if (adjustedIndex == 2 && args[0] != null && !args[0].isEmpty()) {
            // Completar con nombres de jugadores afectados por el efecto
            String effectKey = args[0].toLowerCase();
            Set<UUID> affectedPlayers = effectsManager.getAffectedPlayers(effectKey);
            
            if (affectedPlayers != null && !affectedPlayers.isEmpty()) {
                for (UUID playerId : affectedPlayers) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        completions.add(player.getName());
                    }
                }
            } else {
                // Si no hay jugadores afectados, mostrar todos los jugadores
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}
