package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.projectiles.ProjectileSource;
import com.darkbladedev.HeartlessMain;
import com.darkbladedev.managers.BanManager;
import com.darkbladedev.managers.PermissionManager;
import com.darkbladedev.utils.MM;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HealthSteal implements Listener {
    
    private static final String BAN_REASON = "Has alcanzado el mínimo de corazones permitidos";
    private final Map<UUID, Integer> banCountMap = new HashMap<>();
    private final File banDataFile;
    private final HeartlessMain plugin;
    private boolean enabled = true;
    
    public HealthSteal(HeartlessMain plugin) {
        this.plugin = plugin;
        this.banDataFile = new File(plugin.getDataFolder(), "ban_data.json");
        
        // Create data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Load ban data from JSON file
        loadBanData();
        
        // Register this listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (!enabled) return;
        
        Player deadPlayer = event.getEntity();
        EntityDamageEvent lastDamage = deadPlayer.getLastDamageCause();

        if (lastDamage == null || !(lastDamage instanceof EntityDamageByEntityEvent)) return;

        Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();

        // Manejar proyectiles
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof LivingEntity) {
                damager = (Entity) shooter;
            } else {
                return;
            }
        }

        if (!(damager instanceof LivingEntity)) return;
        LivingEntity killer = (LivingEntity) damager;

        // Ajustar salud (1 corazón = 2.0 puntos)
        double healthToSteal = 2.0;
        double currentMaxHealth = killer.getAttribute(Attribute.MAX_HEALTH).getValue();
        double newMaxHealth = currentMaxHealth + healthToSteal;

        // Limitar salud máxima si es necesario
        if (newMaxHealth > 40.0) newMaxHealth = 40.0;

        // Aplicar el aumento de salud al asesino
        killer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
        killer.setHealth(Math.min(killer.getHealth() + healthToSteal, newMaxHealth));

        // Reducir la salud máxima de la víctima cuando reaparezca
        double victimCurrentMaxHealth = deadPlayer.getAttribute(Attribute.MAX_HEALTH).getValue();
        double victimNewMaxHealth = victimCurrentMaxHealth - healthToSteal;
        
        // Evitar que la salud máxima baje de 6.0 (3 corazones)
        if (victimNewMaxHealth < 6.0) victimNewMaxHealth = 6.0;
        
        // Guardar el nuevo valor de salud máxima para aplicarlo cuando el jugador reaparezca
        deadPlayer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(victimNewMaxHealth);
        
        // Mensaje al jugador víctima
        deadPlayer.sendMessage(MM.toComponent("<gray>¡<red>" + (killer instanceof Player ? ((Player)killer).getName() : "Un mob") + " ha robado 1 corazón de tu salud máxima</red>!</gray>"));
        if (killer instanceof Player) {
            ((Player) killer).sendMessage(MM.toComponent("<green>¡Robaste 1 corazón de <dark_aqua>" + deadPlayer.getName() + "</dark_aqua>!</green>"));
        }
    }

    
    @EventHandler
    public void onPlayerReachMinimunHealth(PlayerDeathEvent event) {
        if (!enabled) return;
        
        Player player = event.getEntity();
        double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double minHealth = HeartlessMain.getInstance().getConfigManager().getHealthMinimum();

        if (currentMaxHealth <= minHealth) {
            PenalizePlayer(player);
        }
    }
    
    /**
     * Restablece la salud máxima a 5 corazones cuando un jugador se reconecta
     * después de haber sido baneado por alcanzar el límite mínimo de corazones
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!enabled) return;
        
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Verificar si este jugador ha sido baneado anteriormente por corazones mínimos
        if (banCountMap.containsKey(playerUUID)) {
            double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
            double resetHealth = 10.0; // 5 corazones
            
            // Solo restablecer si la salud máxima actual es menor a 5 corazones
            if (currentMaxHealth < resetHealth) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(resetHealth);
                
                // Ajustar la salud actual si es mayor que la nueva salud máxima
                if (player.getHealth() > resetHealth) {
                    player.setHealth(resetHealth);
                }
                
                // Mensaje informativo al jugador
                player.sendMessage(MM.toComponent("<green>Tu salud máxima ha sido restablecida a 5 corazones.</green>"));
                
                // Log para administradores
                plugin.getLogger().info("Salud máxima restablecida para " + player.getName() + " a 5 corazones");
            }
        }
    }

    public void PenalizePlayer(Player player) {
        // Obtener el contador de baneos para este jugador
        UUID playerUUID = player.getUniqueId();
        int banCount = banCountMap.getOrDefault(playerUUID, 0) + 1;
        banCountMap.put(playerUUID, banCount);
        
        // Save the updated ban count to JSON
        saveBanData();
        
        // Obtener el gestor de permisos
        PermissionManager permManager = PermissionManager.getInstance(plugin);
        BanManager banManager = plugin.getBanManager();

        
        // Duración fija del baneo: 5 horas
        long defaultBanHours = 5L;
        long banHours = permManager.getBanDurationHours(player, banCount, defaultBanHours);
        
        // Si el jugador está exento de baneo (banHours = 0), solo mostrar advertencia
        if (banHours <= 0) {
            player.sendMessage(MM.toComponent("<red>¡Has alcanzado el mínimo de corazones permitidos!"));
            player.sendMessage(MM.toComponent("<green>Estás exento de baneo gracias a tus permisos."));
            return;
        }
                
        // Notificar al jugador antes del baneo
        player.sendMessage(MM.toComponent("<red>¡Has alcanzado el mínimo de corazones permitidos!"));
        player.sendMessage(MM.toComponent("<gray>Serás baneado por <red>" + banHours + " horas</gray>."));
        player.sendMessage(MM.toComponent("<gray>Este es tu baneo número <red>" + banCount));
        
        // Programar el baneo para ejecutarse después de un breve retraso
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            banManager.ban(player, banHours, BAN_REASON);
        }, 40L); // 2 segundos de retraso (40 ticks)
    }
    
    /**
     * Loads ban data from the JSON file
     */
    private void loadBanData() {
        if (!banDataFile.exists()) {
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
            
            plugin.getLogger().info("Ban data loaded successfully: " + banCountMap.size() + " records");
        } catch (IOException | ParseException e) {
            plugin.getLogger().severe("Error loading ban data: " + e.getMessage());
        }
    }
    
    /**
     * Saves ban data to the JSON file
     */
    @SuppressWarnings("unchecked")
    private void saveBanData() {
        JSONObject banData = new JSONObject();
        
        for (Map.Entry<UUID, Integer> entry : banCountMap.entrySet()) {
            banData.put(entry.getKey().toString(), entry.getValue());
        }
        
        try (FileWriter writer = new FileWriter(banDataFile)) {
            writer.write(banData.toJSONString());
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving ban data: " + e.getMessage());
        }
    }
}
