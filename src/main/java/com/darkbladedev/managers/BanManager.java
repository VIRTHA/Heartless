package com.darkbladedev.managers;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.darkbladedev.utils.MM;

import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BanManager implements Listener {
    private final Map<UUID, Integer> banCountMap = new HashMap<>();
    private File banDataFile;
    
    public BanManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("virtha-events");
        if (plugin != null) {
            this.banDataFile = new File(plugin.getDataFolder(), "ban_data.json");
            loadBanData();
        }
    }

    /**
     * Handles player login attempts and provides ban time information
     */
    @SuppressWarnings({ "rawtypes", "deprecation" })
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        // Check if the player is banned
        if (event.getResult() == Result.KICK_BANNED) {
            // Get ban entry from the name ban list
            BanList banList = Bukkit.getBanList(BanList.Type.NAME);
            BanList ipBanList = Bukkit.getBanList(BanList.Type.IP);
            
            String playerName = event.getPlayer().getName();
            String playerIP = event.getAddress().getHostAddress();
            UUID playerUUID = event.getPlayer().getUniqueId();
            
            // Get ban count for this player
            int banCount = banCountMap.getOrDefault(playerUUID, 0);
            
            // Check if player is banned by name
            if (banList.isBanned(playerName)) {
                Date expiration = banList.getBanEntry(playerName).getExpiration();
                
                // If the ban has an expiration date
                if (expiration != null) {
                    // Calculate remaining time
                    long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                    
                    // Only process if there's time remaining
                    if (remainingMillis > 0) {
                        String formattedTime = formatRemainingTime(remainingMillis);
                        String reason = banList.getBanEntry(playerName).getReason();
                        
                        // Create a custom ban message with remaining time
                        String banMessage =
                            "&c&l¡ESTÁS BANEADO!\n\n" +
                            "&fRazón: &e" + (reason != null ? reason : "No especificada") + "\n" +
                            "&fTiempo restante: &e" + formattedTime + "\n" +
                            "&fBaneo número: &e" + banCount + "\n\n" +
                            "&7Si crees que esto es un error, contacta a un administrador.";
                        
                        // Set the kick message
                        event.setKickMessage(banMessage);
                    }
                }
            }
            // Check if player is banned by IP
            else if (ipBanList.isBanned(playerIP)) {
                Date expiration = ipBanList.getBanEntry(playerIP).getExpiration();
                
                // If the ban has an expiration date
                if (expiration != null) {
                    // Calculate remaining time
                    long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                    
                    // Only process if there's time remaining
                    if (remainingMillis > 0) {
                        String formattedTime = formatRemainingTime(remainingMillis);
                        String reason = ipBanList.getBanEntry(playerIP).getReason();
                        
                        // Create a custom ban message with remaining time
                        Component banMessage = MM.toComponent(
                            "<red><b>¡ESTÁS BANEADO!\n\n" +
                            "<white>Razón: <yellow>" + (reason != null ? reason : "No especificada") + "\n" +
                            "<white>Tiempo restante: <yellow>" + formattedTime + "\n" +
                            "<white>Baneo número: <yellow>" + banCount + "\n\n" +
                            "<gray>Si crees que esto es un error, contacta a un administrador."
                        );
                        
                        // Set the kick message
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
            Bukkit.getLogger().severe("Error loading ban data in BanManager: " + e.getMessage());
        }
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
}