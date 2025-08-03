package com.darkbladedev.commands.functions.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * Comando para aplicar un efecto personalizado a un jugador
 */
public class Apply implements SubcommandExecutor, TabCompletable {
    
    private final CustomEffectsManager effectsManager;
    
    public Apply() {
        this.effectsManager = HeartlessMain.getCustomEffectsManager();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heartless.effects.apply")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para aplicar efectos."));
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>Uso correcto: /heartless effects apply <efecto> <jugador>"));
            return;
        }
        
        String effectName = args[0];
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        
        if (target == null || !target.isOnline()) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>El jugador " + playerName + " no está en línea."));
            return;
        }
        
        if (effectsManager.applyEffectToPlayer(effectName, target)) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <green>Efecto " + effectName + " aplicado a " + playerName + " correctamente."));
        } else {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>No se pudo aplicar el efecto. Verifica que exista y esté activo."));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        int adjustedIndex = args.length;


        if (adjustedIndex == 1) {
            // Completar con nombres de efectos activos
            for (Map.Entry<String, CustomEffects> entry : effectsManager.getAllEffects().entrySet()) {
                if (effectsManager.getActiveEffects().contains(entry.getKey())) {
                    completions.add(entry.getValue().getid());
                }
            }
        } else if (adjustedIndex == 2) {
            // Completar con nombres de jugadores
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }
        
        return completions;
    }
}
