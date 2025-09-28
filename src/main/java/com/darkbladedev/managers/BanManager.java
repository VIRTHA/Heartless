package com.darkbladedev.managers;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.exceptions.CustomException;
import com.darkbladedev.exceptions.ExceptionBuilder;
import com.darkbladedev.exceptions.NoPlayerFoundedException;
import com.darkbladedev.utils.MM;
import com.destroystokyo.paper.profile.PlayerProfile;

import io.papermc.paper.ban.BanListType;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BanManager implements Listener {
    private final Map<UUID, Integer> banCountMap = new HashMap<>();
    private final static Set<UUID> banList = new HashSet<>();
    private final HeartlessMain plugin;
    private File banDataFile;
    private boolean enabled = true;
    
    public BanManager(HeartlessMain plugin) {
        this.plugin = plugin;
        if (plugin != null) {
            this.banDataFile = new File(plugin.getDataFolder(), "ban_data.json");
            loadBanData();
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Handles player login attempts and provides ban time information
     */
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!enabled) {
            return;
        }
        
        // Check if the player is banned
        if (event.getResult() == Result.KICK_BANNED) {
            // Get ban entry from the profile ban list
            BanList<PlayerProfile> banList = Bukkit.getBanList(BanListType.PROFILE);
            
            UUID playerUUID = event.getPlayer().getUniqueId();
            PlayerProfile playerProfile = event.getPlayer().getPlayerProfile();

            // Get ban count for this player
            int banCount = banCountMap.getOrDefault(playerUUID, 0);
            
            // Check if player is banned by profile using modern API
            var profileBanEntry = banList.getBanEntry(playerProfile);
            if (profileBanEntry != null) {
                Date expiration = profileBanEntry.getExpiration();
                
                // If the ban has an expiration date
                if (expiration != null) {
                    // Calculate remaining time
                    long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                    
                    // Only process if there's time remaining
                    if (remainingMillis > 0) {
                        String formattedTime = formatRemainingTime(remainingMillis);
                        String reason = profileBanEntry.getReason();
                        
                        // Create a custom ban message with remaining time using modern Component API
                        Component banMessage = MM.toComponent(
                            "<red><b>¡ESTÁS BANEADO!</b></red>\n\n" +
                            "<white>Razón: <yellow>" + (reason != null ? reason : "No especificada") + "</yellow></white>\n" +
                            "<white>Tiempo restante: <yellow>" + formattedTime + "</yellow></white>\n" +
                            "<white>Baneo número: <yellow>" + banCount + "</yellow></white>\n\n" +
                            "<gray>Si crees que esto es un error, contacta a un administrador.</gray>"
                        );
                        
                        // Set the kick message using modern API
                        event.kickMessage(banMessage);
                    }
                }
            }
        }
    }
    
    /**
     * Loads ban data from the JSON file
     */
    private void loadBanData() {
        if (banDataFile == null || !banDataFile.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(banDataFile)) {
            JSONParser parser = new JSONParser();
            JSONObject banData = (JSONObject) parser.parse(reader);
            
            for (Object key : banData.keySet()) {
                String uuidString = (String) key;
                UUID uuid = UUID.fromString(uuidString);
                Long banCount = (Long) banData.get(uuidString);
                
                banCountMap.put(uuid, banCount.intValue());
            }
        } catch (IOException | ParseException e) {
            if (plugin != null) {
                plugin.getLogger().severe("Error loading ban data: " + e.getMessage());
            }
        }
    }
    
    /**
     * Reloads ban data from the JSON file
     * This method is public and can be called to refresh ban data
     */
    public void reloadBanData() {
        // Clear existing data
        banCountMap.clear();
        // Load fresh data
        loadBanData();
    }
    
    /**
     * Método llamado cuando la configuración se recarga
     */
    public void onConfigReload() {
        plugin.getLogger().info("BanManager: Aplicando cambios de configuración...");
        
        // Obtener nuevos valores de configuración
        ConfigManager configManager = HeartlessMain.getConfigManager();
        
        // Aplicar configuraciones específicas del sistema de baneos
        if (!configManager.isBanSystemEnabled()) {
            plugin.getLogger().info("BanManager: Sistema de baneos deshabilitado por configuración");
        } else {
            // Recargar datos de baneos
            reloadBanData();
        }
        
        plugin.getLogger().info("BanManager: Configuración actualizada exitosamente");
    }
    
    /**
     * Formats milliseconds into a human-readable time format
     * @param millis Time in milliseconds
     * @return Formatted time string (e.g., "2 días, 5 horas, 30 minutos")
     */
    private String formatRemainingTime(long millis) {
        if (millis <= 0) {
            return "0 minutos";
        }
        
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        
        StringBuilder sb = new StringBuilder();
        
        if (days > 0) {
            sb.append(days).append(days == 1 ? " día" : " días");
        }
        
        if (hours > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(hours).append(hours == 1 ? " hora" : " horas");
        }
        
        if (minutes > 0 || (days == 0 && hours == 0)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(minutes).append(minutes == 1 ? " minuto" : " minutos");
        }
        
        return sb.toString();
    }

    public void unBan(Player targetPlayer) {
        if (targetPlayer == null || (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline())) {
            CustomException e = ExceptionBuilder.build(NoPlayerFoundedException.class, this, "<red>Se ha intentado desbanear a un jugador pero parece no existir o no estar baneado</red>");
            ExceptionBuilder.sendToConsole(e);
            return;
        }
        try {
            // Unban by profile (UUID) using modern API
            BanList<PlayerProfile> profileBanList = Bukkit.getBanList(BanListType.PROFILE);
            profileBanList.pardon(targetPlayer.getPlayerProfile());
        } catch (Exception e) {
            CustomException ce = ExceptionBuilder.build(e.getClass(), this, "<red><b>Ha ocurrido un error indefinido al desbanear al jugador " + "<aqua><u>" + targetPlayer.getName() + "</u></aqua>" + "</b></red>");
            ExceptionBuilder.sendToConsole(ce);
        }
    }


    public static Set<UUID> getBanList_() {
        return banList;
    }

    public Set<UUID> getBanList() {
        return banList;
    }

    /**
     * Banea a un jugador por el tiempo especificado con la razón dada
     * @param player El jugador a banear
     * @param banHours Duración del baneo en horas
     * @param banReason Razón del baneo
     */
    public void ban(Player player, long banHours, String banReason) {
        if (!enabled) {
            plugin.getLogger().info("BanManager está deshabilitado, no se puede banear a " + player.getName());
            return;
        }
        
        if (player == null) {
            plugin.getLogger().warning("Intento de banear un jugador nulo");
            return;
        }
        
        ConfigManager configManager = HeartlessMain.getConfigManager();
        
        // Verificar si el sistema de baneos está habilitado
        if (!configManager.isBanSystemEnabled()) {
            plugin.getLogger().info("Intento de baneo cancelado: sistema de baneos deshabilitado");
            return;
        }
        
        UUID playerUUID = player.getUniqueId();
        
        // Actualizar contador de baneos
        int banCount = banCountMap.getOrDefault(playerUUID, 0) + 1;
        banCountMap.put(playerUUID, banCount);
        
        // Agregar a la lista de baneados
        banList.add(playerUUID);
        
        // Usar duración por defecto si no se especifica o es 0
        if (banHours <= 0) {
            final int configBanHours = configManager.getBanDefaultDuration();
            if (configBanHours <= 0) {
                player.sendMessage(MM.toComponent("<red>" + banReason));
                player.sendMessage(MM.toComponent("<green>Estás exento de baneo gracias a tus permisos."));
                return;
            }
        }
        
        // Calcular fecha de expiración
        Date expirationDate = new Date(System.currentTimeMillis() + (banHours * 60 * 60 * 1000));
        
        // Obtener mensaje de baneo desde configuración
        String banMessageText = configManager.getBanMessage()
            .replace("{reason}", banReason)
            .replace("{time}", banHours + " horas")
            .replace("{count}", String.valueOf(banCount));
        
        Component banMessage = MM.toComponent(banMessageText);
        
        // Notificar al jugador antes del baneo
        player.sendMessage(MM.toComponent("<red>" + banReason));
        player.sendMessage(MM.toComponent("<gray>Serás baneado por <red>" + banHours + " horas</gray>."));
        player.sendMessage(MM.toComponent("<gray>Este es tu baneo número <red>" + banCount));
        
        // Programar el baneo para ejecutarse después de un breve retraso
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                // Banear al jugador por perfil usando la API moderna
                BanList<PlayerProfile> banListProfile = Bukkit.getBanList(BanListType.PROFILE);
                banListProfile.addBan(
                    player.getPlayerProfile(),
                    banReason,
                    expirationDate,
                    "VIRTHA System"
                );

                // Expulsar al jugador
                player.kick(banMessage);
                
                // Broadcast si está habilitado
                if (configManager.isBanBroadcast()) {
                    String broadcastMessage = configManager.getBanBroadcastMessage()
                        .replace("{player}", player.getName())
                        .replace("{reason}", banReason)
                        .replace("{time}", banHours + " horas");
                    Bukkit.getConsoleSender().sendMessage(MM.toComponent(broadcastMessage));
                }
                
                // Notificar a los administradores
                Bukkit.getConsoleSender().sendMessage(MM.toComponent(
                    "<white>" + player.getName() + " ha sido baneado por " + banHours + " horas " +
                    "(Baneo #" + banCount + ") - Razón: " + banReason
                ));
                
            } catch (Exception e) {
                plugin.getLogger().severe("Error al banear al jugador " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, 60L); // 3 segundos de retraso (60 ticks)    
    }
}