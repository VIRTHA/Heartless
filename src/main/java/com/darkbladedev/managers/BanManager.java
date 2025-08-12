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
import java.net.InetAddress;
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
    
    public BanManager(HeartlessMain plugin) {
        this.plugin = plugin;
        if (plugin != null) {
            this.banDataFile = new File(plugin.getDataFolder(), "ban_data.json");
            loadBanData();
        }
    }

    /**
     * Handles player login attempts and provides ban time information
     */
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        // Check if the player is banned
        if (event.getResult() == Result.KICK_BANNED) {
            // Get ban entry from the name ban list
            BanList<PlayerProfile> banList = Bukkit.getBanList(BanListType.PROFILE);
            BanList<InetAddress> ipBanList = Bukkit.getBanList(BanListType.IP);
            
            InetAddress playerIP = event.getPlayer().getAddress().getAddress();
            UUID playerUUID = event.getPlayer().getUniqueId();
            PlayerProfile playerProfile = event.getPlayer().getPlayerProfile();

            
            // Get ban count for this player
            int banCount = banCountMap.getOrDefault(playerUUID, 0);
            
            // Check if player is banned by name using modern API
            var nameBanEntry = banList.getBanEntry(playerProfile);
            if (nameBanEntry != null) {
                Date expiration = nameBanEntry.getExpiration();
                
                // If the ban has an expiration date
                if (expiration != null) {
                    // Calculate remaining time
                    long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                    
                    // Only process if there's time remaining
                    if (remainingMillis > 0) {
                        String formattedTime = formatRemainingTime(remainingMillis);
                        String reason = nameBanEntry.getReason();
                        
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
            // Check if player is banned by IP using modern API
            else {
                var ipBanEntry = ipBanList.getBanEntry(playerIP);

                if (ipBanEntry != null) {
                    Date expiration = ipBanEntry.getExpiration();
                
                    // If the ban has an expiration date
                    if (expiration != null) {
                        // Calculate remaining time
                        long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                        
                        // Only process if there's time remaining
                        if (remainingMillis > 0) {
                            String formattedTime = formatRemainingTime(remainingMillis);
                            String reason = ipBanEntry.getReason();
                            
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
            
            // Unban by IP using modern API if player has an address
            if (targetPlayer.getAddress() != null) {
                BanList<InetAddress> ipBanList = Bukkit.getBanList(BanListType.IP);
                ipBanList.pardon(targetPlayer.getAddress().getAddress());
            }
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
}