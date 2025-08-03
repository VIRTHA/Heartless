package com.darkbladedev.commands.functions.bansystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.BanManager;
import com.darkbladedev.utils.MM;

public class Unban implements SubcommandExecutor, TabCompletable {

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // Los args aquí incluyen todos los argumentos del comando, incluyendo grupo y acción
        // Necesitamos ajustar el índice para que coincida con los argumentos específicos de este subcomando
        // args[0] y args[1] son el grupo y la acción, por lo que args[2] es el primer argumento real del subcomando
        
        // Calculamos el índice real restando 2 (grupo y acción)
        int adjustedIndex = args.length;
        
        if (adjustedIndex == 1) {
            // Idealmente, esto debería devolver una lista de jugadores baneados
            // Esta es una implementación básica que podría mejorarse
            List<String> bannedPlayers = new ArrayList<>();
            Set<UUID> banList = BanManager.getBanList_();
            
            // Filtramos por el argumento actual (args[2])
            String currentArg = args.length > 2 ? args[2].toLowerCase() : "";
            
            banList.forEach(entry -> {
                OfflinePlayer player = Bukkit.getOfflinePlayer(entry);
                if (player != null && player.getName() != null && 
                    player.getName().toLowerCase().startsWith(currentArg)) {
                    bannedPlayers.add(player.getName());
                }
            });
            
            return bannedPlayers;
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("heartless.bansystem.unban")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para desbanear jugadores."));
            return;
        }
        
        // Verificar argumentos
        if (args.length < 1) {
            sender.sendMessage(MM.toComponent("<red>Uso: /heartless bansystem unban <jugador>"));
            return;
        }
        
        String targetName = args[0];
        
        try {
            HeartlessMain plugin = HeartlessMain.getInstance();
            Set<UUID> banList = plugin.getBanManager().getBanList();
            UUID targetUUID = null;
            OfflinePlayer targetPlayer = null;
            try {
                Player onlinePlayer = Bukkit.getPlayerExact(targetName);
                if (onlinePlayer != null) {
                    targetPlayer = onlinePlayer;
                } else {
                    // Si no está online, buscar por nombre exacto entre los jugadores offline
                    for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                        if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(targetName)) {
                            targetPlayer = offlinePlayer;
                            break;
                        }
                    }
                targetUUID = targetPlayer.getUniqueId();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (banList.contains(targetUUID)) {
                if (!targetPlayer.isBanned()) {
                    sender.sendMessage(MM.toComponent("<red>El jugador " + targetPlayer.getName() + " no está baneado</red>"));
                    return;
                } else {
                    plugin.getBanManager().unBan(targetPlayer.getPlayer());
                    banList.remove(targetUUID);
                }
            }
            
            sender.sendMessage(MM.toComponent("<green>Has desbaneado a " + targetName + " correctamente."));
        } catch (Exception e) {
        }
    }
}