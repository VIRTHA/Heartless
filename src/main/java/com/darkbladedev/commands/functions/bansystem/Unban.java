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
        if (args.length == 1) {
            // Idealmente, esto debería devolver una lista de jugadores baneados
            // Esta es una implementación básica que podría mejorarse
            List<String> bannedPlayers = new ArrayList<>();
            Set<UUID> banList = BanManager.getBanList_();
            banList.forEach(entry -> bannedPlayers.add(Bukkit.getPlayer(entry).getName()));            
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
                plugin.getBanManager().unBan(targetPlayer.getPlayer());
                banList.remove(targetUUID);
            }
            
            sender.sendMessage(MM.toComponent("<green>Has desbaneado a " + targetName + " correctamente."));
        } catch (Exception e) {
        }
    }
}