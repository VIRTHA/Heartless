package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HealthSteal implements Listener {
    
    private static final String BAN_REASON = "Has alcanzado el mínimo de corazones permitidos";
    private final Map<UUID, Integer> banCountMap = new HashMap<>();
    private final Map<UUID, Double> pendingMaxHealthUpdates = new HashMap<>(); // Almacenar actualizaciones pendientes para respawn
    private final Set<UUID> processedDeaths = new HashSet<>(); // Prevenir doble procesamiento
    private final File banDataFile;
    private final HeartlessMain plugin;
    private final NamespacedKey maxHealthKey;
    private boolean enabled = true;
    
    public HealthSteal(HeartlessMain plugin) {
        this.plugin = plugin;
        this.banDataFile = new File(plugin.getDataFolder(), "ban_data.json");
        this.maxHealthKey = new NamespacedKey(plugin, "max_health");
        
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
    
    @EventHandler(priority = org.bukkit.event.EventPriority.LOW)
    public void onPlayerKill(PlayerDeathEvent event) {
        if (!enabled) return;
        // Validación por mundo: ejecutar solo si el sistema está activo en el mundo de la muerte
        org.bukkit.World deathWorld = event.getEntity().getWorld();
        com.darkbladedev.managers.ConfigManager cfg = com.darkbladedev.HeartlessMain.getInstance().getConfigManager();
        if (cfg == null || !cfg.isHealthStealEnabledInWorld(deathWorld.getName())) {
            return;
        }
        
        Player deadPlayer = event.getEntity();
        UUID deadPlayerId = deadPlayer.getUniqueId();
        
        // Prevenir doble procesamiento de la misma muerte
        if (processedDeaths.contains(deadPlayerId)) {
            return;
        }
        processedDeaths.add(deadPlayerId);
        
        // Limpiar el Set después de un tiempo para evitar memory leaks
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            processedDeaths.remove(deadPlayerId);
        }, 20L); // 1 segundo después
        
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
        double minHealth = cfg.getHealthMinimum();
        double maxHealthCap = 40.0; // Cap hardcodeado o podría venir de config si existiera

        // Obtener atributos actuales
        AttributeInstance killerAttr = killer.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance victimAttr = deadPlayer.getAttribute(Attribute.MAX_HEALTH);
        
        if (killerAttr != null && victimAttr != null) {
            double killerCurrentMax = killerAttr.getValue();
            double victimCurrentMax = victimAttr.getValue();
            
            // Calcular cuánto puede ganar el asesino antes de llegar al cap
            double gainable = Math.max(0, maxHealthCap - killerCurrentMax);
            
            // Calcular cuánto puede perder la víctima antes de llegar al mínimo
            double losable = Math.max(0, victimCurrentMax - minHealth);
            
            // La transferencia real es el mínimo entre:
            // 1. La cantidad estándar a robar (2.0)
            // 2. Lo que el asesino puede recibir
            // 3. Lo que la víctima puede dar
            double actualTransfer = Math.min(healthToSteal, Math.min(gainable, losable));
            
            // Logs de depuración
            plugin.getLogger().info("[HealthSteal Debug] Muerte procesada: " + deadPlayer.getName() + " -> " + killer.getName());
            plugin.getLogger().info("[HealthSteal Debug] Asesino MaxHP: " + killerCurrentMax + " (Cap: " + maxHealthCap + ", Gainable: " + gainable + ")");
            plugin.getLogger().info("[HealthSteal Debug] Víctima MaxHP: " + victimCurrentMax + " (Min: " + minHealth + ", Losable: " + losable + ")");
            plugin.getLogger().info("[HealthSteal Debug] Transferencia calculada: " + actualTransfer);
            
            if (actualTransfer > 0) {
                // Aplicar cambios al asesino
                double killerNewMax = killerCurrentMax + actualTransfer;
                killerAttr.setBaseValue(killerNewMax);
                killer.setHealth(Math.min(killer.getHealth() + actualTransfer, killerNewMax));
                
                if (killer instanceof Player) {
                    PersistentDataContainer pdc = ((Player) killer).getPersistentDataContainer();
                    pdc.set(maxHealthKey, PersistentDataType.DOUBLE, killerNewMax);
                    ((Player) killer).sendMessage(MM.toComponent("<green>¡Has robado " + (actualTransfer/2.0) + " corazones de <dark_aqua>" + deadPlayer.getName() + "</dark_aqua>!</green>"));
                }
                
                // Aplicar cambios a la víctima
                double victimNewMax = victimCurrentMax - actualTransfer;
                victimAttr.setBaseValue(victimNewMax);
                
                // Persistencia y manejo de respawn para víctima
                pendingMaxHealthUpdates.put(deadPlayerId, victimNewMax);
                PersistentDataContainer pdc = deadPlayer.getPersistentDataContainer();
                pdc.set(maxHealthKey, PersistentDataType.DOUBLE, victimNewMax);
                
                deadPlayer.sendMessage(MM.toComponent("<gray>¡<red>" + (killer instanceof Player ? ((Player)killer).getName() : "Un mob") + " ha robado " + (actualTransfer/2.0) + " corazones de tu salud máxima</red>!</gray>"));
            } else {
                plugin.getLogger().info("[HealthSteal Debug] No hubo transferencia de salud (Límites alcanzados).");
                if (killer instanceof Player) {
                    ((Player) killer).sendMessage(MM.toComponent("<yellow>No se pudieron robar corazones (Límite alcanzado por uno de los jugadores)."));
                }
                deadPlayer.sendMessage(MM.toComponent("<yellow>No perdiste corazones (Límite alcanzado)."));
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!enabled) return;
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        
        if (attr == null) return;
        
        double newMaxHealth = -1.0;
        
        // 1. Verificar mapa de actualizaciones pendientes (prioridad máxima para misma sesión)
        if (pendingMaxHealthUpdates.containsKey(playerId)) {
            newMaxHealth = pendingMaxHealthUpdates.remove(playerId);
        } 
        // 2. Verificar persistencia (PDC) si no hay pendiente en memoria
        else if (pdc.has(maxHealthKey, PersistentDataType.DOUBLE)) {
            Double savedMaxHealth = pdc.get(maxHealthKey, PersistentDataType.DOUBLE);
            if (savedMaxHealth != null) {
                newMaxHealth = savedMaxHealth;
            }
        }
        
        // Aplicar salud si se encontró un valor válido
        if (newMaxHealth > 0) {
            // Validar mínimo de nuevo por seguridad
            double minHealth = HeartlessMain.getInstance().getConfigManager().getHealthMinimum();
            if (newMaxHealth < minHealth) newMaxHealth = minHealth;
            
            attr.setBaseValue(newMaxHealth);
            
            // Asegurar que el PDC esté actualizado en la nueva entidad
            pdc.set(maxHealthKey, PersistentDataType.DOUBLE, newMaxHealth);
        }
    }
    
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGH)
    public void onPlayerReachMinimunHealth(PlayerDeathEvent event) {
        if (!enabled) return;
        // Validación por mundo
        org.bukkit.World deathWorld = event.getEntity().getWorld();
        com.darkbladedev.managers.ConfigManager cfg = com.darkbladedev.HeartlessMain.getInstance().getConfigManager();
        if (cfg == null || !cfg.isHealthStealEnabledInWorld(deathWorld.getName())) {
            return;
        }
        
        Player player = event.getEntity();
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;
        
        double currentMaxHealth = attr.getValue();
        double minHealth = cfg.getHealthMinimum();

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
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        
        if (attr == null) return;
        
        // 1. Restaurar desde PDC si existe (prioridad a la persistencia)
        if (pdc.has(maxHealthKey, PersistentDataType.DOUBLE)) {
            Double savedMaxHealth = pdc.get(maxHealthKey, PersistentDataType.DOUBLE);
            if (savedMaxHealth != null) {
                 double minHealth = HeartlessMain.getInstance().getConfigManager().getHealthMinimum();
                 if (savedMaxHealth < minHealth) savedMaxHealth = minHealth;
                 attr.setBaseValue(savedMaxHealth);
            }
        }
        
        // 2. Verificar si este jugador ha sido baneado anteriormente por corazones mínimos
        if (banCountMap.containsKey(playerUUID)) {
            double currentMaxHealth = attr.getValue();
            double resetHealth = 10.0; // 5 corazones
            
            // Solo restablecer si la salud máxima actual es menor a 5 corazones
            // O si acaba de ser desbaneado y su PDC decía 6.0
            if (currentMaxHealth < resetHealth) {
                attr.setBaseValue(resetHealth);
                
                // Ajustar la salud actual si es mayor que la nueva salud máxima
                if (player.getHealth() > resetHealth) {
                    player.setHealth(resetHealth);
                }
                
                // Actualizar PDC con el nuevo valor de reset
                pdc.set(maxHealthKey, PersistentDataType.DOUBLE, resetHealth);
                
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
