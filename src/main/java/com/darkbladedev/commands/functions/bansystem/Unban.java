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

    private boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // Verificar permisos primero
        if (!sender.hasPermission("htl.bansystem.unban")) {
            return Collections.emptyList();
        }
        
        // args[0] es el primer argumento del subcomando (nombre del jugador)
        if (args.length == 1) {
            List<String> bannedPlayers = new ArrayList<>();
            Set<UUID> banList = BanManager.getBanList_();
            
            // Obtener el argumento actual para filtrar
            String currentArg = args[0].toLowerCase();
            
            // Iterar sobre la lista de jugadores baneados
            for (UUID uuid : banList) {
                try {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    if (player != null && player.getName() != null) {
                        String playerName = player.getName();
                        // Verificar que el jugador esté realmente baneado y coincida con el filtro
                        if (player.isBanned() && playerName.toLowerCase().startsWith(currentArg)) {
                            bannedPlayers.add(playerName);
                        }
                    }
                } catch (Exception e) {
                    // Ignorar errores al obtener información del jugador
                }
            }
            
            // Ordenar la lista alfabéticamente
            bannedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
            return bannedPlayers;
        }
        
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("htl.bansystem.unban")) {
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
            
            // Buscar jugador online primero
            Player onlinePlayer = Bukkit.getPlayerExact(targetName);
            if (onlinePlayer != null) {
                targetPlayer = onlinePlayer;
                targetUUID = onlinePlayer.getUniqueId();
            } else {
                // Si no está online, buscar por nombre exacto entre los jugadores offline
                for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                    if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(targetName)) {
                        targetPlayer = offlinePlayer;
                        targetUUID = offlinePlayer.getUniqueId();
                        break;
                    }
                }
            }
            
            // Verificar si se encontró el jugador
            if (targetPlayer == null || targetUUID == null) {
                sender.sendMessage(MM.toComponent("<red>No se pudo encontrar al jugador " + targetName + "."));
                return;
            }
            
            // Verificar si el jugador está en la lista de baneos del plugin
            if (!banList.contains(targetUUID)) {
                sender.sendMessage(MM.toComponent("<red>El jugador " + targetPlayer.getName() + " no está en la lista de baneos del plugin."));
                return;
            }
            
            // Verificar si el jugador está realmente baneado en el servidor
            if (!targetPlayer.isBanned()) {
                // El jugador está en la lista pero no baneado realmente, limpiar la lista
                banList.remove(targetUUID);
                sender.sendMessage(MM.toComponent("<yellow>El jugador " + targetPlayer.getName() + " no estaba baneado, pero se ha limpiado de la lista."));
                return;
            }
            
            // Desbanear al jugador
            if (targetPlayer instanceof Player) {
                // Jugador online
                plugin.getBanManager().unBan((Player) targetPlayer);
            } else {
                // Jugador offline - usar la API directamente
                 try {
                     // Usar la API de Bukkit directamente para jugadores offline
                     var profileBanList = Bukkit.getBanList(io.papermc.paper.ban.BanListType.PROFILE);
                     profileBanList.pardon(targetPlayer.getPlayerProfile());
                     
                     // También intentar desbanear por IP si es posible (aunque es limitado para jugadores offline)
                     // Esto se manejará cuando el jugador se conecte
                 } catch (Exception e) {
                     sender.sendMessage(MM.toComponent("<red>Error al desbanear al jugador offline: " + e.getMessage()));
                     return;
                 }
            }
            
            // Remover de la lista del plugin
            banList.remove(targetUUID);
            
            sender.sendMessage(MM.toComponent("<green>Has desbaneado a " + targetName + " correctamente."));
            
            // Log para administradores
            plugin.getLogger().info("El jugador " + targetName + " ha sido desbaneado por " + sender.getName());
            
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error al desbanear al jugador: " + e.getMessage()));
            HeartlessMain.getInstance().getLogger().severe("Error en comando unban: " + e.getMessage());
            e.printStackTrace();
        }
    }
}