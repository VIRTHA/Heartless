package com.darkbladedev.content.semi_custom.effects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.utils.MM;

import org.bukkit.block.Block;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ZombieInfection implements Listener {
    
    private static ZombieInfection instance;
    private final Plugin plugin;
    
    // Key para almacenar la infección en los datos persistentes del jugador
    private static NamespacedKey zombieInfectionKey;
    private static NamespacedKey InfectionCureCountKey;
    
    // Mapa para rastrear jugadores infectados y sus tareas
    private final Map<UUID, BukkitTask> infectionTasks = new HashMap<>();
    private final Set<UUID> infectedPlayers = new HashSet<>();
    
    // Contador de infecciones curadas por jugador
    private final Map<UUID, Integer> curedInfectionsCount = new HashMap<>();

    // World exclusion fields
    private final Set<String> excludedWorlds = new HashSet<>();
    private boolean applyToAllWorlds = true;
    
    private BukkitTask timeCheckTask;
    private boolean isEnabled = true;
    
    public ZombieInfection(Plugin plugin) {
        this.plugin = plugin;
        ZombieInfection.zombieInfectionKey = new NamespacedKey(plugin, "is_infected");
        ZombieInfection.InfectionCureCountKey = new NamespacedKey(plugin, "infection_cure_count");
        
        // Registrar eventos
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Iniciar tarea de verificación de tiempo
        startTimeCheckTask();
    }
    
    public static ZombieInfection getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new ZombieInfection(plugin);
        }
        return instance;
    }

    public static NamespacedKey getInfectionKey() {
        return zombieInfectionKey;
    }

    public static NamespacedKey getInfectionCureCountKey() {
        return InfectionCureCountKey;
    }

        
    /**
     * Obtiene el contador de curas para un jugador
     * @param playerId UUID del jugador
     * @return Número de veces que el jugador se ha curado
     */
    public int getCuredCount(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            PersistentDataContainer pdc = player.getPersistentDataContainer();
            if (pdc.has(InfectionCureCountKey, PersistentDataType.INTEGER)) {
                return pdc.get(InfectionCureCountKey, PersistentDataType.INTEGER);
            }
        }
        return curedInfectionsCount.getOrDefault(playerId, 0);
    }
    
    /**
     * Inicia la tarea que verifica el tiempo del día para aplicar los efectos correspondientes
     */
    private void startTimeCheckTask() {
        if (timeCheckTask != null) {
            timeCheckTask.cancel();
        }
        
        timeCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEnabled) return;
                
                // Process each world separately
                for (World world : Bukkit.getWorlds()) {
                    // Skip excluded worlds
                    if (excludedWorlds.contains(world.getName())) {
                        continue;
                    }
                    
                    long time = world.getTime();
                    
                    // Apply effects to infected players in this world
                    for (UUID playerId : infectedPlayers) {
                        Player player = Bukkit.getPlayer(playerId);
                        if (player != null && player.isOnline() && 
                            (applyToAllWorlds || player.getWorld().equals(world))) {
                            applyTimeBasedEffects(player, time);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Verificar cada 5 segundos (100 ticks)
    }
    
    
    /**
     * Verifica si hay bloques sólidos sobre el jugador que bloqueen la luz solar
     * @param player El jugador a verificar
     * @return true si hay bloques sobre el jugador, false si está expuesto al cielo
     */
    private boolean hasBlockAbove(Player player) {
        Location loc = player.getLocation();
        int maxHeight = player.getWorld().getMaxHeight();
        int playerY = loc.getBlockY();
        
        // Verificar desde la posición del jugador hasta el límite del mundo
        for (int y = playerY + 2; y < maxHeight; y++) {
            Block block = player.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            if (block.getType().isOccluding() || block.getType().toString().contains("LEAVES")) {
                return true; // Hay un bloque sólido o hojas sobre el jugador
            }
        }
        
        return false; // No hay bloques sobre el jugador
    }
    
    /**
     * Aplica efectos basados en la hora del día
     * @param player El jugador infectado
     * @param time El tiempo actual del mundo (0-24000)
     */
    private void applyTimeBasedEffects(Player player, long time) {
        // Determinar el período del día basado en el tiempo
        String timePeriod;
        
        if ((time >= 0 && time <= 3000) || (time >= 12000 && time <= 14000)) {
            timePeriod = "MORNING_DUSK";
        } else if (time >= 5000 && time <= 11000) {
            timePeriod = "MIDDAY";
        } else if (time > 14000 && time <= 24000) {
            timePeriod = "NIGHT";
        } else {
            timePeriod = "TRANSITION";
        }
        
        // Aplicar efectos según el período del día usando switch
        switch (timePeriod) {
            case "MORNING_DUSK":
                // Eliminar efectos de otros períodos
                player.removePotionEffect(PotionEffectType.STRENGTH);
                player.setFireTicks(0);
                
                if (!player.hasPotionEffect(PotionEffectType.NAUSEA)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 140, 0, false, true, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 0, false, true, true));
                    player.sendActionBar(MM.toComponent("<gray>La infección zombie te debilita durante este momento del día..."));
                }
                break;
                
            case "MIDDAY":
                // Eliminar efectos de otros períodos
                player.removePotionEffect(PotionEffectType.STRENGTH);
                
                // Verificar si el jugador está expuesto a la luz directa del sol
                boolean isInDirectSunlight = player.getLocation().getBlock().getLightFromSky() == 15 && 
                                            player.getWorld().isClearWeather() &&
                                            player.getLocation().getWorld().getTime() < 12000 &&
                                            player.getLocation().getBlock().getLightFromBlocks() < 14 &&
                                            !hasBlockAbove(player);
                
                if (isInDirectSunlight) {
                    // Aplicar efecto de wither y fuego solo si está en luz directa del sol
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0, false, true, true));
                    player.setFireTicks(Math.max(player.getFireTicks(), 100)); // Asegurar al menos 5 segundos de fuego
                    player.sendActionBar(MM.toComponent("<red>¡La luz directa del sol quema tu piel infectada!"));
                    
                    // Efectos visuales adicionales para enfatizar el fuego
                    player.getWorld().spawnParticle(Particle.LAVA,
                        player.getLocation().add(0, 1, 0),
                        10, 0.3, 0.5, 0.3, 0.01);
                } else {
                    // Si no está en luz directa, quitar el fuego pero mantener debilidad
                    player.setFireTicks(0);
                    player.removePotionEffect(PotionEffectType.WITHER);
                    player.sendActionBar(MM.toComponent("<yellow>La infección te debilita, pero estás a salvo de la luz solar directa."));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 140, 0, false, true, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 0, false, true, true));
                }
                break;
                
            case "NIGHT":
                // Eliminar efectos de otros períodos
                player.removePotionEffect(PotionEffectType.NAUSEA);
                player.removePotionEffect(PotionEffectType.HUNGER);
                player.removePotionEffect(PotionEffectType.WITHER);
                player.setFireTicks(0);
                
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 0, false, true, true));
                player.sendActionBar(MM.toComponent("<dark_gray>La oscuridad de la noche <dark_red>fortalece <dark_gray>tu infección..."));
                
                break;
                
            case "TRANSITION":
            default:
                // Eliminar todos los efectos en períodos de transición
                player.removePotionEffect(PotionEffectType.NAUSEA);
                player.removePotionEffect(PotionEffectType.HUNGER);
                player.removePotionEffect(PotionEffectType.WITHER);
                player.removePotionEffect(PotionEffectType.STRENGTH);
                player.setFireTicks(0);
                break;
        }
        
        // Efectos visuales constantes para mostrar que está infectado
        player.getWorld().spawnParticle(Particle.SMOKE, 
            player.getLocation().add(0, 1, 0), 
            3, 0.3, 0.5, 0.3, 0.01);
    }

    /**
     * Adds a world to the exclusion list
     * @param worldName The name of the world to exclude
     * @return true if the world was added to the exclusion list, false if it was already excluded
     */
    public boolean excludeWorld(String worldName) {
        return excludedWorlds.add(worldName);
    }

    /**
     * Removes a world from the exclusion list
     * @param worldName The name of the world to include
     * @return true if the world was removed from the exclusion list, false if it wasn't excluded
     */
    public boolean includeWorld(String worldName) {
        return excludedWorlds.remove(worldName);
    }

    /**
     * Checks if a world is excluded from zombie infection mechanics
     * @param worldName The name of the world to check
     * @return true if the world is excluded, false otherwise
     */
    public boolean isWorldExcluded(String worldName) {
        return excludedWorlds.contains(worldName);
    }

    /**
     * Gets a copy of the set of excluded worlds
     * @return A set containing the names of all excluded worlds
     */
    public Set<String> getExcludedWorlds() {
        return new HashSet<>(excludedWorlds);
    }

    /**
     * Sets whether to apply effects to players in all worlds or only in the world being processed
     * @param applyToAll true to apply effects to all players regardless of world, false to only apply to players in the current world
     */
    public void setApplyToAllWorlds(boolean applyToAll) {
        this.applyToAllWorlds = applyToAll;
    }

    /**
     * Checks if effects are applied to players in all worlds
     * @return true if effects are applied to all players regardless of world, false if only applied to players in the current world
     */
    public boolean isApplyToAllWorlds() {
        return applyToAllWorlds;
    }
   
    // /**
    //  * Envía un mensaje al action bar del jugador con control de cooldown
    //  * @param player El jugador al que enviar el mensaje
    //  * @param message El mensaje a enviar
    //  */
    // private void sendActionBar(Player player, String message) {
    //     UUID playerId = player.getUniqueId();
    //     long currentTime = System.currentTimeMillis();
        
    //     // Verificar si ha pasado suficiente tiempo desde el último mensaje
    //     if (!lastActionBarMessageTime.containsKey(playerId) || 
    //         currentTime - lastActionBarMessageTime.get(playerId) >= ACTION_BAR_COOLDOWN) {
            
    //         player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
    //             TextComponent.fromLegacyText(MM.toComponent(message)));
            
    //         // Actualizar el tiempo del último mensaje
    //         lastActionBarMessageTime.put(playerId, currentTime);
    //     }
    // }
    
    /**
     * Infecta a un jugador con el virus zombie
     * @param player El jugador a infectar
     */
    public void infectPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Si ya está infectado, no hacer nada
        if (isInfected(player)) return;
        
        // Marcar al jugador como infectado
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(zombieInfectionKey, PersistentDataType.BOOLEAN, true);
        infectedPlayers.add(playerId);
        
        // Notificar al jugador
        player.sendMessage(MM.toComponent("<red>¡Has sido infectado con el virus zombie! <gray>Come una manzana dorada para curarte."));
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.5f);
        
        // Efectos visuales iniciales
        player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, 
            player.getLocation().add(0, 1, 0), 
            15, 0.5, 0.5, 0.5, 0.1);
        
        // Aplicar efectos iniciales
        // World world = player.getWorld();
        // applyTimeBasedEffects(player, world.getTime());
    }
    
    /**
     * Cura a un jugador de la infección zombie
     * @param player El jugador a curar
     */
    public void curePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Si no está infectado, no hacer nada
        if (!isInfected(player)) return;
        
        // Eliminar la marca de infección
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(zombieInfectionKey, PersistentDataType.BOOLEAN, false);
        infectedPlayers.remove(playerId);

        // Incrementar el contador de curas
        int currentCures = pdc.getOrDefault(InfectionCureCountKey, PersistentDataType.INTEGER, 0);
        pdc.set(InfectionCureCountKey, PersistentDataType.INTEGER, currentCures + 1);
        
        // Actualizar el mapa de conteo de curaciones
        curedInfectionsCount.put(playerId, currentCures + 1);
        
        // Cancelar tareas asociadas
        if (infectionTasks.containsKey(playerId)) {
            infectionTasks.get(playerId).cancel();
            infectionTasks.remove(playerId);
        }
        
        // Eliminar efectos negativos
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.setFireTicks(0);
        
        // Notificar al jugador
        player.sendActionBar(MM.toComponent("<green>¡Te has curado de la infección zombie!"));
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.7f, 1.0f);
        
        // Efectos visuales de curación
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, 
            player.getLocation().add(0, 1, 0), 
            20, 0.5, 0.5, 0.5, 0.1);
        
        // Registrar curación para recompensas
        int curedCount = curedInfectionsCount.getOrDefault(playerId, 0) + 1;
        curedInfectionsCount.put(playerId, curedCount);
        
        // Recompensa por curarse múltiples veces
        if (curedCount == 5) {
            player.sendMessage(MM.toComponent("<green>¡Has sobrevivido a 5 infecciones zombie!"));
            player.sendMessage(MM.toComponent("<gold>Recompensa: Resistencia <u>parcial</u> a futuras infecciones"));
        }
    }
    
    /**
     * Verifica si un jugador está infectado
     * @param player El jugador a verificar
     * @return true si está infectado, false en caso contrario
     */
    public boolean isInfected(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (pdc.has(zombieInfectionKey, PersistentDataType.BOOLEAN)) {
            return pdc.get(zombieInfectionKey, PersistentDataType.BOOLEAN);
        }
        return false;
    }
    
    /**
     * Habilita o deshabilita el sistema de infección zombie
     * @param enabled true para habilitar, false para deshabilitar
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        
        if (!enabled) {
            // Curar a todos los jugadores si se deshabilita
            for (UUID playerId : new HashSet<>(infectedPlayers)) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    curePlayer(player);
                }
            }
        }
    }
    
    /**
     * Verifica si el jugador tiene resistencia a la infección (por haber sido curado 5+ veces)
     * @param player El jugador a verificar
     * @return true si tiene resistencia parcial
     */
    private boolean hasInfectionResistance(Player player) {
        int curedCount = curedInfectionsCount.getOrDefault(player.getUniqueId(), 0);
        return curedCount >= 5;
    }
    
    /**
     * Verifica si el jugador tiene un arma en la mano
     * @param player El jugador a verificar
     * @return true si tiene un arma
     */
    private boolean hasWeaponInHand(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        Material type = mainHand.getType();
        
        // Verificar si es un arma
        return type.toString().contains("SWORD") || 
               type.toString().contains("AXE") || 
               type.toString().contains("TRIDENT") ||
               type == Material.BOW || 
               type == Material.CROSSBOW;
    }
    
    // Eventos
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isEnabled) return;
        
        Player player = event.getPlayer();
        
        // Verificar si el jugador estaba infectado al desconectarse
        if (isInfected(player)) {
            infectedPlayers.add(player.getUniqueId());
            player.sendMessage(MM.toComponent("<red>Sigues infectado con el virus zombie. <gray>Come una manzana dorada para curarte."));
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled) return;
        
        // Verificar si un jugador infectado golpea a otro jugador
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            
            // Si el atacante está infectado y no tiene arma en la mano
            if (isInfected(attacker) && !hasWeaponInHand(attacker)) {
                // Probabilidad de infección (50% base, reducida si tiene resistencia)
                double infectionChance = hasInfectionResistance(victim) ? 0.25 : 0.5;
                
                if (Math.random() < infectionChance) {
                    infectPlayer(victim);
                    attacker.sendMessage(MM.toComponent("<aqua>¡Has infectado a " + victim.getName() + "!"));
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (!isEnabled) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Curar infección con manzana dorada
        if ((item.getType() == Material.GOLDEN_APPLE || item.getType() == Material.ENCHANTED_GOLDEN_APPLE) 
                && isInfected(player)) {
            curePlayer(player);
            
            /*
            // Bonus de salud temporal por usar manzana encantada
            if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1, false, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 6000, 0, false, true, true));
                player.sendMessage(MM.toComponent("<&6>La manzana encantada te ha dado protección adicional contra futuras infecciones."));
            }
            */
        }
    }
    

    
    /**
     * Establece el número de veces que un jugador se ha curado de la infección
     * @param playerId UUID del jugador
     * @param count Nuevo número de curaciones
     */
    public void setCuredCount(UUID playerId, int count) {
        if (count < 0) count = 0; // Asegurar que el contador no sea negativo
        curedInfectionsCount.put(playerId, count);
        
        // Actualizar también el valor en los datos persistentes del jugador
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            PersistentDataContainer pdc = player.getPersistentDataContainer();
            pdc.set(InfectionCureCountKey, PersistentDataType.INTEGER, count);
        }
    }
    
    // Add this event handler after the other event handlers
    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!isEnabled) return;
        
        // Check if a zombie is targeting a player
        if ((event.getEntity().getType() == EntityType.ZOMBIE || 
             event.getEntity().getType() == EntityType.ZOMBIE_VILLAGER || 
             event.getEntity().getType() == EntityType.DROWNED || 
             event.getEntity().getType() == EntityType.HUSK) && 
            event.getTarget() instanceof Player) {
            
            Player targetPlayer = (Player) event.getTarget();
            
            // If the player is infected, cancel the targeting
            if (isInfected(targetPlayer)) {
                event.setCancelled(true);
                
                // Occasionally show particles to indicate zombie is ignoring the player
                if (Math.random() < 0.2) { // 20% chance to show particles
                    event.getEntity().getWorld().spawnParticle(
                        Particle.HAPPY_VILLAGER,
                        event.getEntity().getLocation().add(0, 1.5, 0),
                        5, 0.2, 0.2, 0.2, 0.01
                    );
                }
            }
        }
    }
}