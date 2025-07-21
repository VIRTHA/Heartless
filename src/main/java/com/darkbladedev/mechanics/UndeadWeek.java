package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.MM;

import java.util.*;

// Add this import at the top with other imports
import org.bukkit.event.entity.EntitySpawnEvent;

public class UndeadWeek extends WeeklyEvent {

    private boolean isRedMoonActive = false;
    private int nightCounter = 0;
    private BukkitTask mainTask;
    
    // Tracking infected players and challenges
    private final Map<UUID, Boolean> infectedPlayers = new HashMap<>();
    private final Map<UUID, Integer> curedInfectionsCount = new HashMap<>();
    private final Map<UUID, Integer> redMoonKillsCount = new HashMap<>();
    private final Set<UUID> curedVillagers = new HashSet<>();
    private final Set<UUID> witherKilledInRedMoon = new HashSet<>();
    
    // Lista de entidades no-muertas
    private final List<EntityType> undeadEntities = Arrays.asList(
        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITHER_SKELETON,
        EntityType.ZOMBIFIED_PIGLIN, EntityType.WITHER, EntityType.ZOMBIE_VILLAGER,
        EntityType.DROWNED, EntityType.HUSK, EntityType.STRAY, EntityType.PHANTOM,
        EntityType.ZOGLIN
    );
    
    public UndeadWeek(HeartlessMain plugin, long duration) {
        super(plugin, duration);
        this.prefix = "<b><gradient:#58fd90:#56fa96:#54f69b:#51f3a1:#4ff0a7:#4decac:#4be9b2:#49e6b8:#46e2bd:#44dfc3:#42dcc9:#40d9cf:#3dd5d4:#3bd2da:#39cfe0:#37cbe5:#35c8eb:#32c5f1:#30c1f6:#2ebefc>Semana de los No Muertos</gradient></b>";
    }
    
    @Override
    protected void stopEventTasks() {
        // Cancelar la tarea principal
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        
        // Restaurar ciclo día/noche normal
        isRedMoonActive = false;
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                world.setTime(0); // Día
            }
        }
        
        // Eliminar efectos de jugadores infectados
        for (UUID playerId : infectedPlayers.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.removePotionEffect(PotionEffectType.POISON);
            }
        }
        
        // Dar recompensas finales
        giveRewards();
    }
    
    @Override
    protected void cleanupEventData() {
        // Limpiar datos de seguimiento
        infectedPlayers.clear();
        curedInfectionsCount.clear();
        redMoonKillsCount.clear();
        curedVillagers.clear();
        witherKilledInRedMoon.clear();
        nightCounter = 0;
        isRedMoonActive = false;
    }
    
    @Override
     public String getName() {
         return "Semana de los No Muertos";
     }
    
    private void checkTime() {
        // Solo verificar en el mundo normal
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                long time = world.getTime();
                
                // Verificar si es de noche (13000-23000)
                if (time >= 13000 && time <= 23000) {
                    // Verificar si es una nueva noche
                    if (time >= 13000 && time <= 13100 && !isRedMoonActive) {
                        nightCounter++;
                        
                        // Cada 3 noches, activar la luna roja
                        if (nightCounter % 3 == 0) {
                            activateRedMoon();
                        }
                    }
                } else {
                    // Si es de día, desactivar la luna roja
                    if (isRedMoonActive) {
                        deactivateRedMoon();
                    }
                }
            }
        }
    }
    
    private void activateRedMoon() {
        isRedMoonActive = true;
        Bukkit.broadcast(MM.toComponent(prefix + " <red>¡La Noche Roja ha comenzado! Los no-muertos son más rápidos y las camas explotan."));
        
        // Aumentar velocidad de los no-muertos y asegurar que tengan fuerza
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (isUndead(entity)) {
                    // Apply movement speed boost
                    if (entity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                        entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(
                            entity.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue() * 2
                        );
                    }
                }
                    // Ensure they have Strength II
                    if (!entity.hasPotionEffect(PotionEffectType.STRENGTH)) {
                        entity.addPotionEffect(new PotionEffect(
                            PotionEffectType.STRENGTH,
                            Integer.MAX_VALUE,
                            1,
                            false,
                            false,
                            true
                        ));
                    }
                }
            }
        }
    
    private void deactivateRedMoon() {
        isRedMoonActive = false;
        Bukkit.broadcast(MM.toComponent(prefix + " <green>La Noche Roja ha terminado."));
        
        // Restaurar velocidad normal de los no-muertos
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (isUndead(entity)) {
                    entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(
                        entity.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue() / 2
                    );
                }
            }
        }
    }
    
    private void checkInfectedPlayers() {
        // Verificar jugadores infectados
        for (Map.Entry<UUID, Boolean> entry : new HashMap<>(infectedPlayers).entrySet()) {
            if (entry.getValue()) { // Si está infectado
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    // Aplicar efecto de veneno que no mata
                    if (!player.hasPotionEffect(PotionEffectType.POISON)) {
                        player.addPotionEffect(new PotionEffect(
                            PotionEffectType.POISON, 
                            Integer.MAX_VALUE, 
                            0, // Nivel 1
                            false, 
                            true, 
                            true
                        ));
                    }
                    
                    // Asegurarse de que el veneno no mate al jugador
                    if (player.getHealth() <= 1.0) {
                        player.removePotionEffect(PotionEffectType.POISON);
                    }
                }
            }
        }
    }
    
    private void checkNetheriteArmor() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int netheriteCount = 0;
            
            // Contar piezas de netherite
            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (item != null && item.getType().toString().contains("NETHERITE")) {
                    netheriteCount++;
                }
            }
            
            // Aplicar efecto wither modificado según cantidad de piezas
            if (netheriteCount > 0) {
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.WITHER, 
                    40, // 2 segundos
                    netheriteCount - 1, // Nivel según cantidad de piezas
                    false, 
                    true, 
                    true
                ));
            }
        }
    }
    

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!isActive) return;
        
        Entity entity = event.getEntity();
        
        // Check if the entity is undead and is a LivingEntity (to apply potion effects)
        if (isUndead(entity) && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            
            // Apply Strength II effect (infinite duration)
            livingEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,  // Infinite duration
                1,                  // Level II (0-based, so 1 = level II)
                false,              // No ambient particles
                false,              // No particles
                true                // Show icon
            ));
            
            // If it's red moon night, also apply speed boost as before
            if (isRedMoonActive) {
                if (livingEntity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                    livingEntity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(
                        livingEntity.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue() * 2
                    );
                }
            }
        }
    }


    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isActive) return;
        
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        
        // Manejar proyectiles
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Entity) {
                damager = (Entity) shooter;
            }
        }
        
        // Removed the damage doubling code since we're now using Strength effect
        
        // Infectar jugadores si son golpeados por zombies
        if (isUndead(damager) && damager.getType() == EntityType.ZOMBIE && victim instanceof Player) {
            Player player = (Player) victim;
            infectedPlayers.put(player.getUniqueId(), true);
            player.sendMessage(MM.toComponent("<red>¡Has sido infectado! Come una zanahoria o manzana dorada para curarte."));
        }
        
        // Registrar muertes en noche roja
        if (isRedMoonActive && damager instanceof Player && victim instanceof Player) {
            Player killer = (Player) damager;
            redMoonKillsCount.put(killer.getUniqueId(), 
                redMoonKillsCount.getOrDefault(killer.getUniqueId(), 0) + 1);
            
            // Verificar desafío
            if (redMoonKillsCount.get(killer.getUniqueId()) == 3) {
                killer.sendMessage(MM.toComponent("<green>¡Desafío completado! Has matado a 3 jugadores en la Noche Roja."));
                killer.sendMessage(MM.toComponent("<gray>Recompensa: <u>Tag 'Necroestallido'"));
                // Aquí se aplicaría el tag

                //eternalAPI.setTag(killer, new Tag("necroestallido", "necroestallido", "&x&1&7&c&e&2&9N&x&1&6&c&9&3&7e&x&1&4&c&4&4&5c&x&1&3&c&0&5&3r&x&1&2&b&b&6&0o&x&1&0&b&6&6&ee&x&0&f&b&1&7&cs&x&0&e&a&d&8&at&x&0&d&a&8&9&8a&x&0&b&a&3&a&6l&x&0&a&9&e&b&3l&x&0&9&9&a&c&1i&x&0&7&9&5&c&fd&x&0&6&9&0&d&do")); //&#49bf40N&#46bd49e&#43bc53c&#41ba5cr&#3eb865o&#3bb76fe&#38b578s&#36b381t&#33b18aa&#30b094l&#2dae9dl&#2baca6i&#28abb0d&#25a9b9o
            }
        }
        
        // Registrar muerte del Wither en noche roja
        if (isRedMoonActive && victim.getType() == EntityType.WITHER && damager instanceof Player) {
            Player killer = (Player) damager;
            World world = killer.getWorld();
            
            if (world.getEnvironment() == World.Environment.NORMAL) {
                witherKilledInRedMoon.add(killer.getUniqueId());
                killer.sendMessage(MM.toComponent("&a¡Desafío legendario completado! Has derrotado al Wither en la Noche Roja."));
                
                // Aumentar corazón máximo
                double currentMaxHealth = killer.getAttribute(Attribute.MAX_HEALTH).getValue();
                killer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMaxHealth + 2.0);
                killer.sendMessage(MM.toComponent("&6Recompensa: +1 corazón máximo"));
            }
        }
    }
    
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (!isActive) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Curar infección con zanahoria dorada
        if (item.getType() == Material.GOLDEN_CARROT && infectedPlayers.getOrDefault(player.getUniqueId(), false)) {
            infectedPlayers.put(player.getUniqueId(), false);
            player.removePotionEffect(PotionEffectType.POISON);
            player.sendMessage(MM.toComponent("&a¡Te has curado de la infección!"));
            
            // Registrar para el desafío
            int curedCount = curedInfectionsCount.getOrDefault(player.getUniqueId(), 0) + 1;
            curedInfectionsCount.put(player.getUniqueId(), curedCount);
            
            if (curedCount == 10) {
                player.sendMessage(MM.toComponent("&a¡Desafío completado! Has curado 10 infecciones."));
                
                // Aumentar corazón máximo
                double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMaxHealth + 2.0);
                player.sendMessage(MM.toComponent("&6Recompensa: +1 corazón máximo"));
            }
        }
    }
    
    @EventHandler
    public void onEntityTransform(EntityTransformEvent event) {
        if (!isActive) return;
        
        // Detectar curación de aldeanos zombificados
        if (event.getEntity().getType() == EntityType.ZOMBIE_VILLAGER && 
            event.getTransformedEntity().getType() == EntityType.VILLAGER) {
            
            // Buscar al jugador responsable (esto es aproximado, podría mejorarse)
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getLocation().distance(event.getEntity().getLocation()) <= 10) {
                    curedVillagers.add(player.getUniqueId());
                    player.sendMessage(MM.toComponent("&a¡Desafío completado! Has curado a un aldeano zombificado."));
                    player.sendMessage(MM.toComponent("&6Recompensa: Encantamiento First Strike"));
                    // Aquí se aplicaría el encantamiento
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (!isActive || !isRedMoonActive) return;
        
        // Explotar camas en noche roja
        event.setCancelled(true);
        Player player = event.getPlayer();
        //player.sendMessage(MM.toComponent("&c¡No puedes dormir durante la Noche Roja!"));
        
        // Crear explosión
        player.getWorld().createExplosion(event.getBed().getLocation(), 2.0f, false, true);
    }
    
    private boolean isUndead(Entity entity) {
        return undeadEntities.contains(entity.getType());
    }
    
    private void giveRewards() {
        // Implementar lógica para dar recompensas finales
        // Esto podría incluir persistencia de recompensas, etc.
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public boolean isRedMoonActive() {
        return isRedMoonActive;
    }


    @Override
    protected void pauseEventTasks() {
        // Cancelar la tarea principal
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        
        // Temporarily restore normal day/night cycle
        isRedMoonActive = false;
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                world.setTime(0); // Day
            }
        }
        
        // Temporarily remove effects from infected players
        for (UUID playerId : infectedPlayers.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.removePotionEffect(PotionEffectType.POISON);
            }
        }
    }

    @Override
    protected void resumeEventTasks() {
        // Reiniciar la tarea principal
        startMainTask();
        
        // Reaplicar efectos a jugadores infectados
        for (UUID playerId : infectedPlayers.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 60, 0, false, true, true));
            }
        }
    }

    private void startMainTask() {
        // Cancel existing task if any
        if (mainTask != null) {
            mainTask.cancel();
        }
        
        // Start the main task
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Check infected players
                checkInfectedPlayers();
            }
        }.runTaskTimer(plugin, 0L, 20L * 30); // Check every 30 seconds
    }

    // Método start original ahora es privado y solo contiene la lógica específica
    public void start() {
        // Tarea principal que se ejecuta cada tick para verificar condiciones
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkTime();
                checkInfectedPlayers();
                checkNetheriteArmor();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Cada segundo
    }

    /**
     * Checks if a player has completed a specific challenge
     * @param playerId The UUID of the player
     * @param challengeId The ID of the challenge
     * @return true if the challenge is completed, false otherwise
     */
    public boolean hasChallengeCompleted(UUID playerId, String challengeId) {
        switch (challengeId) {
            case "cure_infection":
                // Player has cured their infection
                return curedInfectionsCount.getOrDefault(playerId, 0) > 0;
            case "cure_villager":
                // Player has cured a zombie villager
                return curedVillagers.contains(playerId);
            case "red_moon_kills":
                // Player has killed a certain number of mobs during red moon
                return redMoonKillsCount.getOrDefault(playerId, 0) >= 10;
            case "wither_red_moon":
                // Player has killed a wither during red moon
                return witherKilledInRedMoon.contains(playerId);
            default:
                return false;
        }
    }

    @Override
    protected void startEventTasks() {
        // Iniciar la tarea principal
        startMainTask();
        
        // Programar el fin del evento
        new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    @Override
    protected void announceEventStart() {
        // Anunciar el inicio del evento
        Bukkit.broadcast(MM.toComponent(prefix + " <red>Las hordas de no-muertos dominan el mundo..."));
        Bukkit.broadcast(MM.toComponent(prefix + " <yellow>Desafíos disponibles:"));
        Bukkit.broadcast(MM.toComponent("<gray>- <white>Curar tu infección 10 veces <gray>(Recompensa: Encantamiento First Strike)"));
        Bukkit.broadcast(MM.toComponent("<gray>- <white>Curar a 5} aldeano zombificado <gray>(Recompensa: <gray><u>Tag</u> \"Dr. Zomboss\")"));
        Bukkit.broadcast(MM.toComponent("<gray>- <white>Matar 10 no-muertos durante la Noche Roja <gray>(Recompensa: +1 corazón)"));
        Bukkit.broadcast(MM.toComponent("<gray>- <white>Matar al Wither durante la Noche Roja <gray>(Recompensa: +1 corazón)"));
    }
    
    @Override
    protected void announceEventEnd() {
        Bukkit.broadcast(MM.toComponent(prefix + " <green>¡La amenaza de los no-muertos ha sido contenida!"));
        Bukkit.broadcast(MM.toComponent(prefix + " <yellow>El mundo vuelve a la normalidad... por ahora."));
    }
}