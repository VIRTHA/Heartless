package com.darkbladedev.testing.mocks;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerKickEvent.Cause;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.*;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.block.TargetBlockInfo.FluidMode;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import com.destroystokyo.paper.profile.PlayerProfile;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.entity.PlayerGiveResult;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.math.Position;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.world.damagesource.CombatTracker;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import net.md_5.bungee.api.chat.BaseComponent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Implementación mock de la interfaz Player para pruebas.
 * Proporciona una implementación básica funcional para testing.
 */
public class MockPlayer implements Player {
    
    private final String name;
    private final UUID uniqueId;
    private final Map<String, Object> metadata;
    private final List<String> sentMessages;
    private final List<PotionEffect> activePotionEffects;
    private final PlayerInventory inventory;
    private final Inventory enderChest;
    
    private boolean online;
    private boolean op;
    private GameMode gameMode;
    private Location location;
    private World world;
    private double health;
    private double maxHealth;
    private int foodLevel;
    private float saturation;
    private int level;
    private float exp;
    private long firstPlayed;
    private long lastPlayed;
    private boolean allowFlight;
    private boolean flying;
    private float flySpeed;
    private float walkSpeed;
    
    public MockPlayer(String name) {
        this.name = name;
        this.uniqueId = UUID.randomUUID();
        this.metadata = new HashMap<>();
        this.sentMessages = new ArrayList<>();
        this.activePotionEffects = new ArrayList<>();
        this.inventory = new SimpleInventory();
        this.enderChest = new SimpleInventory(27); // EnderChest tiene 27 slots
        
        // Valores por defecto
        this.online = true;
        this.op = false;
        this.gameMode = GameMode.SURVIVAL;
        this.health = 20.0;
        this.maxHealth = 20.0;
        this.foodLevel = 20;
        this.saturation = 5.0f;
        this.level = 0;
        this.exp = 0.0f;
        this.firstPlayed = System.currentTimeMillis();
        this.lastPlayed = System.currentTimeMillis();
        this.allowFlight = false;
        this.flying = false;
        this.flySpeed = 0.1f;
        this.walkSpeed = 0.2f;
        
        // Crear mundo y ubicación mock
        this.world = null; // Mock world - se puede configurar externamente
        this.location = new Location(world, 0, 64, 0);
    }
    
    // === MÉTODOS PRINCIPALES ===
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    @Override
    public boolean isOnline() {
        return online;
    }
    
    public void setOnline(boolean online) {
        this.online = online;
        if (online) {
            this.lastPlayed = System.currentTimeMillis();
        }
    }
    
    @Override
    public boolean isOp() {
        return op;
    }
    
    @Override
    public void setOp(boolean value) {
        this.op = value;
    }
    
    @Override
    public void setDeathScreenScore(int score) {
        // Mock implementation
    }
    
    // === MENSAJES ===
    
    @Override
    public void sendMessage(String message) {
        sentMessages.add(message);
    }
    
    @Override
    public void sendMessage(String[] messages) {
        Collections.addAll(sentMessages, messages);
    }
    
    @Override
    public void sendMessage(UUID sender, String message) {
        sentMessages.add("[" + sender + "] " + message);
    }
    
    @Override
    public void sendMessage(UUID sender, String[] messages) {
        for (String message : messages) {
            sendMessage(sender, message);
        }
    }
    
    /**
     * Obtiene todos los mensajes enviados al jugador.
     * 
     * @return Lista de mensajes
     */
    public List<String> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }
    
    /**
     * Limpia la lista de mensajes enviados.
     */
    public void clearSentMessages() {
        sentMessages.clear();
    }
    
    /**
     * Verifica si se envió un mensaje específico.
     * 
     * @param message Mensaje a buscar
     * @return true si se encontró el mensaje
     */
    public boolean hasReceivedMessage(String message) {
        return sentMessages.contains(message);
    }
    
    /**
     * Verifica si se envió un mensaje que contenga el texto especificado.
     * 
     * @param text Texto a buscar
     * @return true si se encontró un mensaje con el texto
     */
    public boolean hasReceivedMessageContaining(String text) {
        return sentMessages.stream().anyMatch(msg -> msg.contains(text));
    }
    
    // === UBICACIÓN Y MUNDO ===
    
    @Override
    public Location getLocation() {
        return location.clone();
    }
    
    @Override
    public Location getLocation(Location loc) {
        if (loc != null) {
            loc.setWorld(location.getWorld());
            loc.setX(location.getX());
            loc.setY(location.getY());
            loc.setZ(location.getZ());
            loc.setYaw(location.getYaw());
            loc.setPitch(location.getPitch());
        }
        return loc;
    }
    
    @Override
    public boolean teleport(Location location) {
        this.location = location.clone();
        this.world = location.getWorld();
        return true;
    }
    
    @Override
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause) {
        return teleport(location);
    }
    
    @Override
    public boolean teleport(Entity destination) {
        return teleport(destination.getLocation());
    }
    
    @Override
    public boolean teleport(Entity destination, PlayerTeleportEvent.TeleportCause cause) {
        return teleport(destination.getLocation(), cause);
    }
    
    // === SALUD Y COMIDA ===
    
    @Override
    public double getHealth() {
        return health;
    }
    
    @Override
    public void setHealth(double health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }
    
    @Override
    public double getMaxHealth() {
        return maxHealth;
    }
    
    @Override
    public void setMaxHealth(double health) {
        this.maxHealth = Math.max(0, health);
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }
    
    @Override
    public int getFoodLevel() {
        return foodLevel;
    }
    
    @Override
    public void setFoodLevel(int foodLevel) {
        this.foodLevel = Math.max(0, Math.min(foodLevel, 20));
    }
    
    @Override
    public float getSaturation() {
        return saturation;
    }
    
    @Override
    public void setSaturation(float saturation) {
        this.saturation = Math.max(0, saturation);
    }
    
    // === EFECTOS DE POCIÓN ===
    
    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        // Remover efecto existente del mismo tipo
        removePotionEffect(effect.getType());
        activePotionEffects.add(effect);
        return true;
    }
    
    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force) {
        return addPotionEffect(effect);
    }
    
    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects) {
        boolean result = true;
        for (PotionEffect effect : effects) {
            result &= addPotionEffect(effect);
        }
        return result;
    }
    
    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        return activePotionEffects.stream().anyMatch(effect -> effect.getType().equals(type));
    }
    
    @Override
    public PotionEffect getPotionEffect(PotionEffectType type) {
        return activePotionEffects.stream()
            .filter(effect -> effect.getType().equals(type))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public void removePotionEffect(PotionEffectType type) {
        activePotionEffects.removeIf(effect -> effect.getType().equals(type));
    }
    
    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        return new ArrayList<>(activePotionEffects);
    }
    
    // === GAMEMODE Y EXPERIENCIA ===
    
    @Override
    public GameMode getGameMode() {
        return gameMode;
    }
    
    @Override
    public void setGameMode(GameMode mode) {
        this.gameMode = mode;
    }
    
    @Override
    public int getLevel() {
        return level;
    }
    
    @Override
    public void setLevel(int level) {
        this.level = Math.max(0, level);
    }
    
    @Override
    public float getExp() {
        return exp;
    }
    
    @Override
    public void setExp(float exp) {
        this.exp = Math.max(0, Math.min(exp, 1.0f));
    }
    
    @Override
    public int getTotalExperience() {
        return level * 100 + (int)(exp * 100); // Simplificado
    }
    
    @Override
    public void setTotalExperience(int exp) {
        this.level = exp / 100;
        this.exp = (exp % 100) / 100.0f;
    }
    
    @Override
    public void giveExp(int amount) {
        setTotalExperience(getTotalExperience() + amount);
    }
    
    @Override
    public void giveExpLevels(int amount) {
        setLevel(getLevel() + amount);
    }
    
    // === INVENTARIO ===
    
    @Override
    public PlayerInventory getInventory() {
        return inventory;
    }
    
    @Override
    public Inventory getEnderChest() {
        return enderChest;
    }
    
    @Override
    public MainHand getMainHand() {
        return MainHand.RIGHT;
    }
    
    // === VUELO ===
    
    @Override
    public boolean getAllowFlight() {
        return allowFlight;
    }
    
    @Override
    public void setAllowFlight(boolean flight) {
        this.allowFlight = flight;
        if (!flight) {
            this.flying = false;
        }
    }
    
    @Override
    public boolean isFlying() {
        return flying;
    }
    
    @Override
    public void setFlying(boolean value) {
        this.flying = value && allowFlight;
    }
    
    @Override
    public float getFlySpeed() {
        return flySpeed;
    }
    
    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException {
        if (value < -1.0f || value > 1.0f) {
            throw new IllegalArgumentException("Fly speed must be between -1.0 and 1.0");
        }
        this.flySpeed = value;
    }
    
    @Override
    public float getWalkSpeed() {
        return walkSpeed;
    }
    
    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException {
        if (value < -1.0f || value > 1.0f) {
            throw new IllegalArgumentException("Walk speed must be between -1.0 and 1.0");
        }
        this.walkSpeed = value;
    }
    
    // === TIEMPOS ===
    
    @Override
    public long getFirstPlayed() {
        return firstPlayed;
    }
    
    public void setFirstPlayed(long firstPlayed) {
        this.firstPlayed = firstPlayed;
    }
    
    @Override
    public long getLastPlayed() {
        return lastPlayed;
    }
    
    public void setLastPlayed(long lastPlayed) {
        this.lastPlayed = lastPlayed;
    }
    
    @Override
    public boolean hasPlayedBefore() {
        return firstPlayed > 0;
    }
    
    // === MÉTODOS DE UTILIDAD PARA PRUEBAS ===
    
    /**
     * Simula daño al jugador.
     * 
     * @param damage Cantidad de daño
     */
    public void simulateDamage(double damage) {
        setHealth(getHealth() - damage);
    }
    
    /**
     * Simula curación del jugador.
     * 
     * @param healing Cantidad de curación
     */
    public void simulateHealing(double healing) {
        setHealth(getHealth() + healing);
    }
    
    /**
     * Verifica si el jugador está muerto.
     * 
     * @return true si la salud es 0 o menor
     */
    @Override
    public boolean isDead() {
        return health <= 0;
    }
    
    /**
     * Establece metadatos personalizados para pruebas.
     * 
     * @param key Clave del metadato
     * @param value Valor del metadato
     */
    public void setTestMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    /**
     * Obtiene metadatos personalizados de pruebas.
     * 
     * @param key Clave del metadato
     * @return Valor del metadato o null
     */
    public Object getTestMetadata(String key) {
        return metadata.get(key);
    }
    
    // === MÉTODOS NO IMPLEMENTADOS (NO-OP) ===
    // Estos métodos no son necesarios para las pruebas actuales
    
    @Override
    public String getDisplayName() { return name; }
    
    @Override
    public void setDisplayName(String name) {}
    
    @Override
    public String getPlayerListName() { return name; }
    
    @Override
    public void setPlayerListName(String name) {}
    
    @Override
    public void setCompassTarget(Location loc) {}
    
    @Override
    public Location getCompassTarget() { return location; }
    
    @Override
    public InetSocketAddress getAddress() { return null; }
    
    @Override
    public void kickPlayer(String message) { setOnline(false); }
    
    @Override
    public void chat(String msg) {}
    
    @Override
    public boolean performCommand(String command) { return false; }
    
    @Override
    public boolean isSneaking() { return false; }
    
    @Override
    public void setSneaking(boolean sneak) {}
    
    @Override
    public boolean isSprinting() { return false; }
    
    @Override
    public void setSprinting(boolean sprinting) {}
    
    @Override
    public void saveData() {}
    
    @Override
    public void loadData() {}
    
    @Override
    public boolean isSleepingIgnored() { return false; }
    
    @Override
    public void setSleepingIgnored(boolean isSleeping) {}
    
    @Override
    public Location getBedSpawnLocation() { return null; }
    
    @Override
    public void setBedSpawnLocation(Location location) {}
    
    @Override
    public void setBedSpawnLocation(Location location, boolean force) {}
    
    @Override
    public void playNote(Location loc, byte instrument, byte note) {}
    
    @Override
    public void playNote(Location loc, Instrument instrument, Note note) {}
    
    @Override
    public void playSound(Location location, Sound sound, float volume, float pitch) {}
    
    @Override
    public void playSound(Location location, String sound, float volume, float pitch) {}
    
    @Override
    public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch) {}
    
    @Override
    public void playSound(Location location, String sound, SoundCategory category, float volume, float pitch) {}
    
    @Override
    public void stopSound(Sound sound) {}
    
    @Override
    public void stopSound(String sound) {}
    
    @Override
    public void stopSound(Sound sound, SoundCategory category) {}
    
    @Override
    public void stopSound(String sound, SoundCategory category) {}
    
    // Continúa con más métodos no implementados...
    // (Por brevedad, se omiten muchos métodos que simplemente retornan valores por defecto)
    
    // === IMPLEMENTACIONES BÁSICAS REQUERIDAS ===
    
    @Override
    public boolean isValid() { return online; }
    
    @Override
    public Server getServer() { return null; }
    
    @Override
    public EntityType getType() { return EntityType.PLAYER; }
    
    @Override
    public int getEntityId() { return uniqueId.hashCode(); }
    
    @Override
    public List<Entity> getNearbyEntities(double x, double y, double z) { return new ArrayList<>(); }
    
    @Override
    public void remove() { setOnline(false); }
    
    @Override
    public Vector getVelocity() { return new Vector(0, 0, 0); }
    
    @Override
    public void setVelocity(Vector velocity) {}
    
    @Override
    public boolean isOnGround() { return true; }
    
    @Override
    public boolean isInWater() { return false; }
    
    @Override
    public World getWorld() { return world; }
    
    @Override
    public void setRotation(float yaw, float pitch) {
        location.setYaw(yaw);
        location.setPitch(pitch);
    }
    
    @Override
    public boolean hasPermission(String name) { return op; }
    
    @Override
    public boolean hasPermission(Permission perm) { return op; }
    
    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) { return null; }
    
    @Override
    public PermissionAttachment addAttachment(Plugin plugin) { return null; }
    
    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) { return null; }
    
    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) { return null; }
    
    @Override
    public void removeAttachment(PermissionAttachment attachment) {}
    
    @Override
    public void recalculatePermissions() {}
    
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() { return new HashSet<>(); }
    
    @Override
    public boolean isPermissionSet(String name) { return false; }
    
    @Override
    public boolean isPermissionSet(Permission perm) { return false; }
    
    // Más métodos no implementados se pueden añadir según sea necesario...
    
    /**
     * Implementación simple de Inventory para testing
     */
    private static class SimpleInventory implements org.bukkit.inventory.PlayerInventory {
        private final ItemStack[] contents;
        private final int size;
        
        public SimpleInventory() {
            this(36); // Inventario de jugador por defecto tiene 36 slots
        }
        
        public SimpleInventory(int size) {
            this.size = size;
            this.contents = new ItemStack[size];
        }
        
        @Override
        public int getSize() { return size; }
        
        @Override
        public int getMaxStackSize() { return 64; }
        
        @Override
        public void setMaxStackSize(int size) {}
        
        @Override
        public ItemStack getItem(int index) {
            return index >= 0 && index < contents.length ? contents[index] : null;
        }
        
        @Override
        public void setItem(int index, ItemStack item) {
            if (index >= 0 && index < contents.length) {
                contents[index] = item;
            }
        }
        
        @Override
        public HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException {
            return new HashMap<>();
        }
        
        @Override
        public HashMap<Integer, ItemStack> removeItem(ItemStack... items) throws IllegalArgumentException {
            return new HashMap<>();
        }
        
        @Override
        public ItemStack[] getContents() {
            return contents.clone();
        }
        
        @Override
        public void setContents(ItemStack[] items) throws IllegalArgumentException {
            System.arraycopy(items, 0, contents, 0, Math.min(items.length, contents.length));
        }
        
        @Override
        public ItemStack[] getStorageContents() {
            return getContents();
        }
        
        @Override
        public void setStorageContents(ItemStack[] items) throws IllegalArgumentException {
            setContents(items);
        }
        
        @Override
        public boolean contains(Material material) throws IllegalArgumentException {
            return false;
        }
        
        @Override
        public boolean contains(ItemStack item) {
            return false;
        }
        
        @Override
        public boolean contains(Material material, int amount) throws IllegalArgumentException {
            return false;
        }
        
        @Override
        public boolean contains(ItemStack item, int amount) {
            return false;
        }
        
        @Override
        public boolean containsAtLeast(ItemStack item, int amount) {
            return false;
        }
        
        @Override
        public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException {
            return new HashMap<>();
        }
        
        @Override
        public HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
            return new HashMap<>();
        }
        
        @Override
        public int first(Material material) throws IllegalArgumentException {
            return -1;
        }
        
        @Override
        public int first(ItemStack item) {
            return -1;
        }
        
        @Override
        public int firstEmpty() {
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] == null) return i;
            }
            return -1;
        }
        
        @Override
        public boolean isEmpty() {
            for (ItemStack item : contents) {
                if (item != null) return false;
            }
            return true;
        }
        
        @Override
        public void remove(Material material) throws IllegalArgumentException {}
        
        @Override
        public void remove(ItemStack item) {}
        
        @Override
        public void clear(int index) {
            if (index >= 0 && index < contents.length) {
                contents[index] = null;
            }
        }
        
        @Override
        public void clear() {
            for (int i = 0; i < contents.length; i++) {
                contents[i] = null;
            }
        }
        
        @Override
        public int close() {
            return 0;
        }
        
        @Override
        public List<org.bukkit.entity.HumanEntity> getViewers() {
            return new ArrayList<>();
        }
        
        @Override
        public InventoryType getType() {
            return InventoryType.CHEST;
        }
        
        @Override
        public org.bukkit.entity.HumanEntity getHolder() {
            return null; // No-op para pruebas
        }
        
        @Override
        public InventoryHolder getHolder(boolean useSnapshot) {
            return null;
        }
        
        @Override
        public ListIterator<ItemStack> iterator() {
            return Arrays.asList(contents).listIterator();
        }
        
        @Override
        public ListIterator<ItemStack> iterator(int index) {
            return Arrays.asList(contents).listIterator(index);
        }
        
        @Override
        public Location getLocation() {
            return null;
        }
        
        // Métodos específicos de PlayerInventory
        @Override
        public HashMap<Integer, ItemStack> removeItemAnySlot(ItemStack... items) throws IllegalArgumentException {
            return new HashMap<>();
        }
        
        @Override
        public ItemStack[] getArmorContents() {
            return new ItemStack[4];
        }
        
        @Override
        public ItemStack[] getExtraContents() {
            return new ItemStack[1];
        }
        
        @Override
        public ItemStack getHelmet() { return null; }
        
        @Override
        public ItemStack getChestplate() { return null; }
        
        @Override
        public ItemStack getLeggings() { return null; }
        
        @Override
        public ItemStack getBoots() { return null; }
        
        @Override
        public void setArmorContents(ItemStack[] items) {}
        
        @Override
        public void setExtraContents(ItemStack[] items) {}
        
        @Override
        public void setHelmet(ItemStack helmet) {}
        
        @Override
        public void setChestplate(ItemStack chestplate) {}
        
        @Override
        public void setLeggings(ItemStack leggings) {}
        
        @Override
        public void setBoots(ItemStack boots) {}
        
        @Override
        public ItemStack getItemInMainHand() { return null; }
        
        @Override
        public void setItemInMainHand(ItemStack item) {}
        
        @Override
        public ItemStack getItemInOffHand() { return null; }
        
        @Override
        public void setItemInOffHand(ItemStack item) {}
        
        @Override
        public int getHeldItemSlot() { return 0; }
        
        @Override
        public void setHeldItemSlot(int slot) {}
        
        @Override
        public ItemStack getItemInHand() {
            return getItemInMainHand();
        }
        
        @Override
        public void setItemInHand(ItemStack stack) {
            setItemInMainHand(stack);
        }
        
        @Override
        public ItemStack getItem(org.bukkit.inventory.EquipmentSlot slot) {
            switch (slot) {
                case HAND:
                    return getItemInMainHand();
                case OFF_HAND:
                    return getItemInOffHand();
                case HEAD:
                    return getHelmet();
                case CHEST:
                    return getChestplate();
                case LEGS:
                    return getLeggings();
                case FEET:
                    return getBoots();
                default:
                    return null;
            }
        }
        
        @Override
        public void setItem(org.bukkit.inventory.EquipmentSlot slot, ItemStack item) {
            switch (slot) {
                case HAND:
                    setItemInMainHand(item);
                    break;
                case OFF_HAND:
                    setItemInOffHand(item);
                    break;
                case HEAD:
                    setHelmet(item);
                    break;
                case CHEST:
                    setChestplate(item);
                    break;
                case LEGS:
                    setLeggings(item);
                    break;
                case FEET:
                    setBoots(item);
                    break;
                case BODY:
                    break;
                case SADDLE:
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public EntityEquipment getEquipment() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getEquipment'");
    }

    @Override
    public boolean setWindowProperty(@SuppressWarnings("removal") Property prop, int value) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setWindowProperty'");
    }

    @Override
    public int getEnchantmentSeed() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getEnchantmentSeed'");
    }

    @Override
    public void setEnchantmentSeed(int seed) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setEnchantmentSeed'");
    }

    @Override
    public InventoryView getOpenInventory() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getOpenInventory'");
    }

    @Override
    public @Nullable InventoryView openInventory(Inventory inventory) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openInventory'");
    }

    @Override
    public @Nullable InventoryView openWorkbench(@Nullable Location location, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openWorkbench'");
    }

    @Override
    public @Nullable InventoryView openEnchanting(@Nullable Location location, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openEnchanting'");
    }

    @Override
    public void openInventory(InventoryView inventory) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openInventory'");
    }

    @Override
    public @Nullable InventoryView openMerchant(Villager trader, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openMerchant'");
    }

    @Override
    public @Nullable InventoryView openMerchant(Merchant merchant, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openMerchant'");
    }

    @Override
    public @Nullable InventoryView openAnvil(@Nullable Location location, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openAnvil'");
    }

    @Override
    public @Nullable InventoryView openCartographyTable(@Nullable Location location, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openCartographyTable'");
    }

    @Override
    public @Nullable InventoryView openGrindstone(@Nullable Location location, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openGrindstone'");
    }

    @Override
    public @Nullable InventoryView openLoom(@Nullable Location location, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openLoom'");
    }

    @Override
    public @Nullable InventoryView openSmithingTable(@Nullable Location location, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openSmithingTable'");
    }

    @Override
    public @Nullable InventoryView openStonecutter(@Nullable Location location, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openStonecutter'");
    }

    @Override
    public void closeInventory(Reason reason) {
        
        throw new UnsupportedOperationException("Unimplemented method 'closeInventory'");
    }

    @Override
    public ItemStack getItemInHand() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getItemInHand'");
    }

    @Override
    public void setItemInHand(@Nullable ItemStack item) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setItemInHand'");
    }

    @Override
    public ItemStack getItemOnCursor() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getItemOnCursor'");
    }

    @Override
    public void setItemOnCursor(@Nullable ItemStack item) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setItemOnCursor'");
    }

    @Override
    public boolean hasCooldown(Material material) {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasCooldown'");
    }

    @Override
    public int getCooldown(Material material) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getCooldown'");
    }

    @Override
    public void setCooldown(Material material, int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setCooldown'");
    }

    @Override
    public void setHurtDirection(float hurtDirection) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setHurtDirection'");
    }

    @Override
    public boolean isDeeplySleeping() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isDeeplySleeping'");
    }

    @Override
    public boolean hasCooldown(ItemStack item) {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasCooldown'");
    }

    @Override
    public int getCooldown(ItemStack item) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getCooldown'");
    }

    @Override
    public void setCooldown(ItemStack item, int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setCooldown'");
    }

    @Override
    public int getCooldown(Key key) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getCooldown'");
    }

    @Override
    public void setCooldown(Key key, int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setCooldown'");
    }

    @Override
    public int getSleepTicks() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSleepTicks'");
    }

    @Override
    public @Nullable Location getPotentialRespawnLocation() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPotentialRespawnLocation'");
    }

    @Override
    public @Nullable FishHook getFishHook() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getFishHook'");
    }

    @Override
    public boolean sleep(Location location, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sleep'");
    }

    @Override
    public void wakeup(boolean setSpawnLocation) {
        
        throw new UnsupportedOperationException("Unimplemented method 'wakeup'");
    }

    @Override
    public void startRiptideAttack(int duration, float attackStrength, @Nullable ItemStack attackItem) {
        
        throw new UnsupportedOperationException("Unimplemented method 'startRiptideAttack'");
    }

    @Override
    public Location getBedLocation() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getBedLocation'");
    }

    @Override
    public boolean isBlocking() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isBlocking'");
    }

    @Override
    public boolean isHandRaised() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isHandRaised'");
    }

    @Override
    public int getExpToLevel() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getExpToLevel'");
    }

    @Override
    public @Nullable Entity releaseLeftShoulderEntity() {
        
        throw new UnsupportedOperationException("Unimplemented method 'releaseLeftShoulderEntity'");
    }

    @Override
    public @Nullable Entity releaseRightShoulderEntity() {
        
        throw new UnsupportedOperationException("Unimplemented method 'releaseRightShoulderEntity'");
    }

    @Override
    public float getAttackCooldown() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getAttackCooldown'");
    }

    @Override
    public boolean discoverRecipe(NamespacedKey recipe) {
        
        throw new UnsupportedOperationException("Unimplemented method 'discoverRecipe'");
    }

    @Override
    public int discoverRecipes(Collection<NamespacedKey> recipes) {
        
        throw new UnsupportedOperationException("Unimplemented method 'discoverRecipes'");
    }

    @Override
    public boolean undiscoverRecipe(NamespacedKey recipe) {
        
        throw new UnsupportedOperationException("Unimplemented method 'undiscoverRecipe'");
    }

    @Override
    public int undiscoverRecipes(Collection<NamespacedKey> recipes) {
        
        throw new UnsupportedOperationException("Unimplemented method 'undiscoverRecipes'");
    }

    @Override
    public boolean hasDiscoveredRecipe(NamespacedKey recipe) {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasDiscoveredRecipe'");
    }

    @Override
    public Set<NamespacedKey> getDiscoveredRecipes() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getDiscoveredRecipes'");
    }

    @Override
    public @Nullable Entity getShoulderEntityLeft() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getShoulderEntityLeft'");
    }

    @Override
    public void setShoulderEntityLeft(@Nullable Entity entity) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setShoulderEntityLeft'");
    }

    @Override
    public @Nullable Entity getShoulderEntityRight() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getShoulderEntityRight'");
    }

    @Override
    public void setShoulderEntityRight(@Nullable Entity entity) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setShoulderEntityRight'");
    }

    @Override
    public boolean dropItem(boolean dropAll) {
        
        throw new UnsupportedOperationException("Unimplemented method 'dropItem'");
    }

    @Override
    public @Nullable Item dropItem(int slot, int amount, boolean throwRandomly,
            @Nullable Consumer<Item> entityOperation) {
        
        throw new UnsupportedOperationException("Unimplemented method 'dropItem'");
    }

    @Override
    public @Nullable Item dropItem(EquipmentSlot slot, int amount, boolean throwRandomly,
            @Nullable Consumer<Item> entityOperation) {
        
        throw new UnsupportedOperationException("Unimplemented method 'dropItem'");
    }

    @Override
    public @Nullable Item dropItem(ItemStack itemStack, boolean throwRandomly,
            @Nullable Consumer<Item> entityOperation) {
        
        throw new UnsupportedOperationException("Unimplemented method 'dropItem'");
    }

    @Override
    public float getExhaustion() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getExhaustion'");
    }

    @Override
    public void setExhaustion(float value) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setExhaustion'");
    }

    @Override
    public int getSaturatedRegenRate() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSaturatedRegenRate'");
    }

    @Override
    public void setSaturatedRegenRate(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setSaturatedRegenRate'");
    }

    @Override
    public int getUnsaturatedRegenRate() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getUnsaturatedRegenRate'");
    }

    @Override
    public void setUnsaturatedRegenRate(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setUnsaturatedRegenRate'");
    }

    @Override
    public int getStarvationRate() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getStarvationRate'");
    }

    @Override
    public void setStarvationRate(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setStarvationRate'");
    }

    @Override
    public @Nullable Location getLastDeathLocation() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getLastDeathLocation'");
    }

    @Override
    public void setLastDeathLocation(@Nullable Location location) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setLastDeathLocation'");
    }

    @Override
    public @Nullable Firework fireworkBoost(ItemStack fireworkItemStack) {
        
        throw new UnsupportedOperationException("Unimplemented method 'fireworkBoost'");
    }

    @Override
    public double getEyeHeight() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getEyeHeight'");
    }

    @Override
    public double getEyeHeight(boolean ignorePose) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getEyeHeight'");
    }

    @Override
    public @NotNull Location getEyeLocation() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getEyeLocation'");
    }

    @Override
    public @NotNull List<Block> getLineOfSight(@org.jetbrains.annotations.Nullable Set<Material> transparent,
            int maxDistance) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getLineOfSight'");
    }

    @Override
    public @NotNull Block getTargetBlock(@org.jetbrains.annotations.Nullable Set<Material> transparent,
            int maxDistance) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTargetBlock'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Block getTargetBlock(int maxDistance, @SuppressWarnings("removal") @NotNull FluidMode fluidMode) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTargetBlock'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockFace getTargetBlockFace(int maxDistance,
            @SuppressWarnings("removal") @NotNull FluidMode fluidMode) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockFace'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockFace getTargetBlockFace(int maxDistance,
            @NotNull FluidCollisionMode fluidMode) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockFace'");
    }

    @SuppressWarnings("removal")
    @Override
    public @org.jetbrains.annotations.Nullable TargetBlockInfo getTargetBlockInfo(int maxDistance,
            @NotNull FluidMode fluidMode) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockInfo'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTargetEntity'");
    }

    @SuppressWarnings("removal")
    @Override
    public @org.jetbrains.annotations.Nullable TargetEntityInfo getTargetEntityInfo(int maxDistance,
            boolean ignoreBlocks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTargetEntityInfo'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable RayTraceResult rayTraceEntities(int maxDistance, boolean ignoreBlocks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
    }

    @Override
    public @NotNull List<Block> getLastTwoTargetBlocks(@org.jetbrains.annotations.Nullable Set<Material> transparent,
            int maxDistance) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getLastTwoTargetBlocks'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Block getTargetBlockExact(int maxDistance) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockExact'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Block getTargetBlockExact(int maxDistance,
            @NotNull FluidCollisionMode fluidCollisionMode) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockExact'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable RayTraceResult rayTraceBlocks(double maxDistance) {
        
        throw new UnsupportedOperationException("Unimplemented method 'rayTraceBlocks'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable RayTraceResult rayTraceBlocks(double maxDistance,
            @NotNull FluidCollisionMode fluidCollisionMode) {
        
        throw new UnsupportedOperationException("Unimplemented method 'rayTraceBlocks'");
    }

    @Override
    public int getRemainingAir() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getRemainingAir'");
    }

    @Override
    public void setRemainingAir(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setRemainingAir'");
    }

    @Override
    public int getMaximumAir() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getMaximumAir'");
    }

    @Override
    public void setMaximumAir(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setMaximumAir'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable ItemStack getItemInUse() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getItemInUse'");
    }

    @Override
    public int getItemInUseTicks() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getItemInUseTicks'");
    }

    @Override
    public void setItemInUseTicks(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setItemInUseTicks'");
    }

    @Override
    public int getArrowCooldown() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getArrowCooldown'");
    }

    @Override
    public void setArrowCooldown(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setArrowCooldown'");
    }

    @Override
    public int getArrowsInBody() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getArrowsInBody'");
    }

    @Override
    public void setArrowsInBody(int count, boolean fireEvent) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setArrowsInBody'");
    }

    @Override
    public void setNextArrowRemoval(@Range(from = 0, to = 2147483647) int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setNextArrowRemoval'");
    }

    @Override
    public int getNextArrowRemoval() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getNextArrowRemoval'");
    }

    @Override
    public int getBeeStingerCooldown() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getBeeStingerCooldown'");
    }

    @Override
    public void setBeeStingerCooldown(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setBeeStingerCooldown'");
    }

    @Override
    public int getBeeStingersInBody() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getBeeStingersInBody'");
    }

    @Override
    public void setBeeStingersInBody(int count) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setBeeStingersInBody'");
    }

    @Override
    public void setNextBeeStingerRemoval(@Range(from = 0, to = 2147483647) int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setNextBeeStingerRemoval'");
    }

    @Override
    public int getNextBeeStingerRemoval() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getNextBeeStingerRemoval'");
    }

    @Override
    public int getMaximumNoDamageTicks() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getMaximumNoDamageTicks'");
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setMaximumNoDamageTicks'");
    }

    @Override
    public double getLastDamage() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getLastDamage'");
    }

    @Override
    public void setLastDamage(double damage) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setLastDamage'");
    }

    @Override
    public int getNoDamageTicks() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getNoDamageTicks'");
    }

    @Override
    public void setNoDamageTicks(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setNoDamageTicks'");
    }

    @Override
    public int getNoActionTicks() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getNoActionTicks'");
    }

    @Override
    public void setNoActionTicks(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setNoActionTicks'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Player getKiller() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getKiller'");
    }

    @Override
    public void setKiller(@org.jetbrains.annotations.Nullable Player killer) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setKiller'");
    }

    @Override
    public boolean clearActivePotionEffects() {
        
        throw new UnsupportedOperationException("Unimplemented method 'clearActivePotionEffects'");
    }

    @Override
    public boolean hasLineOfSight(@NotNull Entity other) {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasLineOfSight'");
    }

    @Override
    public boolean hasLineOfSight(@NotNull Location location) {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasLineOfSight'");
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getRemoveWhenFarAway'");
    }

    @Override
    public void setRemoveWhenFarAway(boolean remove) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setRemoveWhenFarAway'");
    }

    @Override
    public void setCanPickupItems(boolean pickup) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setCanPickupItems'");
    }

    @Override
    public boolean getCanPickupItems() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getCanPickupItems'");
    }

    @Override
    public boolean isLeashed() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isLeashed'");
    }

    @Override
    public @NotNull Entity getLeashHolder() throws IllegalStateException {
        
        throw new UnsupportedOperationException("Unimplemented method 'getLeashHolder'");
    }

    @Override
    public boolean setLeashHolder(@org.jetbrains.annotations.Nullable Entity holder) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setLeashHolder'");
    }

    @Override
    public boolean isGliding() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isGliding'");
    }

    @Override
    public void setGliding(boolean gliding) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setGliding'");
    }

    @Override
    public boolean isSwimming() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isSwimming'");
    }

    @Override
    public void setSwimming(boolean swimming) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setSwimming'");
    }

    @Override
    public boolean isRiptiding() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isRiptiding'");
    }

    @Override
    public void setRiptiding(boolean riptiding) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setRiptiding'");
    }

    @Override
    public boolean isSleeping() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isSleeping'");
    }

    @Override
    public boolean isClimbing() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isClimbing'");
    }

    @Override
    public void setAI(boolean ai) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setAI'");
    }

    @Override
    public boolean hasAI() {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasAI'");
    }

    @Override
    public void attack(@NotNull Entity target) {
        
        throw new UnsupportedOperationException("Unimplemented method 'attack'");
    }

    @Override
    public void swingMainHand() {
        
        throw new UnsupportedOperationException("Unimplemented method 'swingMainHand'");
    }

    @Override
    public void swingOffHand() {
        
        throw new UnsupportedOperationException("Unimplemented method 'swingOffHand'");
    }

    @Override
    public void playHurtAnimation(float yaw) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playHurtAnimation'");
    }

    @Override
    public void setCollidable(boolean collidable) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setCollidable'");
    }

    @Override
    public boolean isCollidable() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isCollidable'");
    }

    @Override
    public @NotNull Set<UUID> getCollidableExemptions() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getCollidableExemptions'");
    }

    @Override
    public <T> @org.jetbrains.annotations.Nullable T getMemory(@NotNull MemoryKey<T> memoryKey) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getMemory'");
    }

    @Override
    public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @org.jetbrains.annotations.Nullable T memoryValue) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setMemory'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Sound getHurtSound() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getHurtSound'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Sound getDeathSound() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getDeathSound'");
    }

    @Override
    public @NotNull Sound getFallDamageSound(int fallHeight) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getFallDamageSound'");
    }

    @Override
    public @NotNull Sound getFallDamageSoundSmall() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getFallDamageSoundSmall'");
    }

    @Override
    public @NotNull Sound getFallDamageSoundBig() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getFallDamageSoundBig'");
    }

    @Override
    public @NotNull Sound getDrinkingSound(@NotNull ItemStack itemStack) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getDrinkingSound'");
    }

    @Override
    public @NotNull Sound getEatingSound(@NotNull ItemStack itemStack) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getEatingSound'");
    }

    @Override
    public boolean canBreatheUnderwater() {
        
        throw new UnsupportedOperationException("Unimplemented method 'canBreatheUnderwater'");
    }

    @Override
    public @NotNull EntityCategory getCategory() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getCategory'");
    }

    @Override
    public int getArrowsStuck() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getArrowsStuck'");
    }

    @Override
    public void setArrowsStuck(int arrows) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setArrowsStuck'");
    }

    @Override
    public int getShieldBlockingDelay() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getShieldBlockingDelay'");
    }

    @Override
    public void setShieldBlockingDelay(int delay) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setShieldBlockingDelay'");
    }

    @Override
    public float getSidewaysMovement() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSidewaysMovement'");
    }

    @Override
    public float getUpwardsMovement() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getUpwardsMovement'");
    }

    @Override
    public float getForwardsMovement() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getForwardsMovement'");
    }

    @Override
    public void startUsingItem(@NotNull EquipmentSlot hand) {
        
        throw new UnsupportedOperationException("Unimplemented method 'startUsingItem'");
    }

    @Override
    public void completeUsingActiveItem() {
        
        throw new UnsupportedOperationException("Unimplemented method 'completeUsingActiveItem'");
    }

    @Override
    public @NotNull ItemStack getActiveItem() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getActiveItem'");
    }

    @Override
    public void clearActiveItem() {
        
        throw new UnsupportedOperationException("Unimplemented method 'clearActiveItem'");
    }

    @Override
    public int getActiveItemRemainingTime() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getActiveItemRemainingTime'");
    }

    @Override
    public void setActiveItemRemainingTime(@Range(from = 0, to = 2147483647) int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setActiveItemRemainingTime'");
    }

    @Override
    public boolean hasActiveItem() {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasActiveItem'");
    }

    @Override
    public int getActiveItemUsedTime() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getActiveItemUsedTime'");
    }

    @Override
    public @NotNull EquipmentSlot getActiveItemHand() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getActiveItemHand'");
    }

    @Override
    public boolean isJumping() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isJumping'");
    }

    @Override
    public void setJumping(boolean jumping) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setJumping'");
    }

    @Override
    public void playPickupItemAnimation(@NotNull Item item, int quantity) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playPickupItemAnimation'");
    }

    @Override
    public float getHurtDirection() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getHurtDirection'");
    }

    @Override
    public void knockback(double strength, double directionX, double directionZ) {
        
        throw new UnsupportedOperationException("Unimplemented method 'knockback'");
    }

    @Override
    public void broadcastSlotBreak(@NotNull EquipmentSlot slot) {
        
        throw new UnsupportedOperationException("Unimplemented method 'broadcastSlotBreak'");
    }

    @Override
    public void broadcastSlotBreak(@NotNull EquipmentSlot slot, @NotNull Collection<Player> players) {
        
        throw new UnsupportedOperationException("Unimplemented method 'broadcastSlotBreak'");
    }

    @Override
    public @NotNull ItemStack damageItemStack(@NotNull ItemStack stack, int amount) {
        
        throw new UnsupportedOperationException("Unimplemented method 'damageItemStack'");
    }

    @Override
    public void damageItemStack(@NotNull EquipmentSlot slot, int amount) {
        
        throw new UnsupportedOperationException("Unimplemented method 'damageItemStack'");
    }

    @Override
    public float getBodyYaw() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getBodyYaw'");
    }

    @Override
    public void setBodyYaw(float bodyYaw) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setBodyYaw'");
    }

    @Override
    public boolean canUseEquipmentSlot(@NotNull EquipmentSlot slot) {
        
        throw new UnsupportedOperationException("Unimplemented method 'canUseEquipmentSlot'");
    }

    @Override
    public @NotNull CombatTracker getCombatTracker() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getCombatTracker'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable AttributeInstance getAttribute(@NotNull Attribute attribute) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getAttribute'");
    }

    @Override
    public void registerAttribute(@NotNull Attribute attribute) {
        
        throw new UnsupportedOperationException("Unimplemented method 'registerAttribute'");
    }

    @Override
    public void damage(double amount) {
        
        throw new UnsupportedOperationException("Unimplemented method 'damage'");
    }

    @Override
    public void damage(double amount, @org.jetbrains.annotations.Nullable Entity source) {
        
        throw new UnsupportedOperationException("Unimplemented method 'damage'");
    }

    @Override
    public void damage(double amount, @NotNull DamageSource damageSource) {
        
        throw new UnsupportedOperationException("Unimplemented method 'damage'");
    }

    @Override
    public void heal(double amount, @NotNull RegainReason reason) {
        
        throw new UnsupportedOperationException("Unimplemented method 'heal'");
    }

    @Override
    public double getAbsorptionAmount() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getAbsorptionAmount'");
    }

    @Override
    public void setAbsorptionAmount(double amount) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setAbsorptionAmount'");
    }

    @Override
    public void resetMaxHealth() {
        
        throw new UnsupportedOperationException("Unimplemented method 'resetMaxHealth'");
    }

    @Override
    public double getHeight() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getHeight'");
    }

    @Override
    public double getWidth() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getWidth'");
    }

    @Override
    public @NotNull BoundingBox getBoundingBox() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getBoundingBox'");
    }

    @Override
    public boolean teleport(@NotNull Location location, @NotNull TeleportCause cause,
            @NotNull TeleportFlag @NotNull... teleportFlags) {
        
        throw new UnsupportedOperationException("Unimplemented method 'teleport'");
    }

    @Override
    public void lookAt(double x, double y, double z, @NotNull LookAnchor entityAnchor) {
        
        throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location loc, @NotNull TeleportCause cause,
            @NotNull TeleportFlag @NotNull... teleportFlags) {
        
        throw new UnsupportedOperationException("Unimplemented method 'teleportAsync'");
    }

    @Override
    public int getFireTicks() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getFireTicks'");
    }

    @Override
    public int getMaxFireTicks() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getMaxFireTicks'");
    }

    @Override
    public void setFireTicks(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setFireTicks'");
    }

    @Override
    public void setVisualFire(boolean fire) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setVisualFire'");
    }

    @Override
    public void setVisualFire(@NotNull TriState fire) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setVisualFire'");
    }

    @Override
    public boolean isVisualFire() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isVisualFire'");
    }

    @Override
    public @NotNull TriState getVisualFire() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getVisualFire'");
    }

    @Override
    public int getFreezeTicks() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getFreezeTicks'");
    }

    @Override
    public int getMaxFreezeTicks() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getMaxFreezeTicks'");
    }

    @Override
    public void setFreezeTicks(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setFreezeTicks'");
    }

    @Override
    public boolean isFrozen() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isFrozen'");
    }

    @Override
    public void setInvisible(boolean invisible) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setInvisible'");
    }

    @Override
    public boolean isInvisible() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isInvisible'");
    }

    @Override
    public void setNoPhysics(boolean noPhysics) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setNoPhysics'");
    }

    @Override
    public boolean hasNoPhysics() {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasNoPhysics'");
    }

    @Override
    public boolean isFreezeTickingLocked() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isFreezeTickingLocked'");
    }

    @Override
    public void lockFreezeTicks(boolean locked) {
        
        throw new UnsupportedOperationException("Unimplemented method 'lockFreezeTicks'");
    }

    @Override
    public boolean isPersistent() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isPersistent'");
    }

    @Override
    public void setPersistent(boolean persistent) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPersistent'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Entity getPassenger() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPassenger'");
    }

    @Override
    public boolean setPassenger(@NotNull Entity passenger) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPassenger'");
    }

    @Override
    public @NotNull List<Entity> getPassengers() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPassengers'");
    }

    @Override
    public boolean addPassenger(@NotNull Entity passenger) {
        
        throw new UnsupportedOperationException("Unimplemented method 'addPassenger'");
    }

    @Override
    public boolean removePassenger(@NotNull Entity passenger) {
        
        throw new UnsupportedOperationException("Unimplemented method 'removePassenger'");
    }

    @Override
    public boolean isEmpty() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }

    @Override
    public boolean eject() {
        
        throw new UnsupportedOperationException("Unimplemented method 'eject'");
    }

    @Override
    public @NotNull ItemStack getPickItemStack() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPickItemStack'");
    }

    @Override
    public float getFallDistance() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getFallDistance'");
    }

    @Override
    public void setFallDistance(float distance) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setFallDistance'");
    }

    @Override
    public void setLastDamageCause(@org.jetbrains.annotations.Nullable EntityDamageEvent event) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setLastDamageCause'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable EntityDamageEvent getLastDamageCause() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getLastDamageCause'");
    }

    @Override
    public int getTicksLived() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTicksLived'");
    }

    @Override
    public void setTicksLived(int value) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setTicksLived'");
    }

    @Override
    public void playEffect(@NotNull EntityEffect effect) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playEffect'");
    }

    @Override
    public @NotNull Sound getSwimSound() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSwimSound'");
    }

    @Override
    public @NotNull Sound getSwimSplashSound() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSwimSplashSound'");
    }

    @Override
    public @NotNull Sound getSwimHighSpeedSplashSound() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSwimHighSpeedSplashSound'");
    }

    @Override
    public boolean isInsideVehicle() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isInsideVehicle'");
    }

    @Override
    public boolean leaveVehicle() {
        
        throw new UnsupportedOperationException("Unimplemented method 'leaveVehicle'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Entity getVehicle() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getVehicle'");
    }

    @Override
    public void setCustomNameVisible(boolean flag) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setCustomNameVisible'");
    }

    @Override
    public boolean isCustomNameVisible() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isCustomNameVisible'");
    }

    @Override
    public void setVisibleByDefault(boolean visible) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setVisibleByDefault'");
    }

    @Override
    public boolean isVisibleByDefault() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isVisibleByDefault'");
    }

    @Override
    public @NotNull Set<Player> getTrackedBy() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTrackedBy'");
    }

    @Override
    public boolean isTrackedBy(@NotNull Player player) {
        
        throw new UnsupportedOperationException("Unimplemented method 'isTrackedBy'");
    }

    @Override
    public void setGlowing(boolean flag) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setGlowing'");
    }

    @Override
    public boolean isGlowing() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isGlowing'");
    }

    @Override
    public void setInvulnerable(boolean flag) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setInvulnerable'");
    }

    @Override
    public boolean isInvulnerable() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isInvulnerable'");
    }

    @Override
    public boolean isSilent() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isSilent'");
    }

    @Override
    public void setSilent(boolean flag) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setSilent'");
    }

    @Override
    public boolean hasGravity() {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasGravity'");
    }

    @Override
    public void setGravity(boolean gravity) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setGravity'");
    }

    @Override
    public int getPortalCooldown() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPortalCooldown'");
    }

    @Override
    public void setPortalCooldown(int cooldown) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPortalCooldown'");
    }

    @Override
    public @NotNull Set<String> getScoreboardTags() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getScoreboardTags'");
    }

    @Override
    public boolean addScoreboardTag(@NotNull String tag) {
        
        throw new UnsupportedOperationException("Unimplemented method 'addScoreboardTag'");
    }

    @Override
    public boolean removeScoreboardTag(@NotNull String tag) {
        
        throw new UnsupportedOperationException("Unimplemented method 'removeScoreboardTag'");
    }

    @Override
    public @NotNull PistonMoveReaction getPistonMoveReaction() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPistonMoveReaction'");
    }

    @Override
    public @NotNull BlockFace getFacing() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getFacing'");
    }

    @Override
    public @NotNull Pose getPose() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPose'");
    }

    @Override
    public void setPose(@NotNull Pose pose, boolean fixed) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPose'");
    }

    @Override
    public boolean hasFixedPose() {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasFixedPose'");
    }

    @Override
    public @NotNull SpawnCategory getSpawnCategory() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSpawnCategory'");
    }

    @Override
    public boolean isInWorld() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isInWorld'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable String getAsString() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getAsString'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable EntitySnapshot createSnapshot() {
        
        throw new UnsupportedOperationException("Unimplemented method 'createSnapshot'");
    }

    @Override
    public @NotNull Entity copy() {
        
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    @Override
    public @NotNull Entity copy(@NotNull Location to) {
        
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    @Override
    public @NotNull Component teamDisplayName() {
        
        throw new UnsupportedOperationException("Unimplemented method 'teamDisplayName'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Location getOrigin() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getOrigin'");
    }

    @Override
    public boolean fromMobSpawner() {
        
        throw new UnsupportedOperationException("Unimplemented method 'fromMobSpawner'");
    }

    @Override
    public @NotNull SpawnReason getEntitySpawnReason() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getEntitySpawnReason'");
    }

    @Override
    public boolean isUnderWater() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isUnderWater'");
    }

    @Override
    public boolean isInRain() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isInRain'");
    }

    @Override
    public boolean isInLava() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isInLava'");
    }

    @Override
    public boolean isTicking() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isTicking'");
    }

    @Override
    public @NotNull Set<Player> getTrackedPlayers() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTrackedPlayers'");
    }

    @Override
    public boolean spawnAt(@NotNull Location location, @NotNull SpawnReason reason) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnAt'");
    }

    @Override
    public boolean isInPowderedSnow() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isInPowderedSnow'");
    }

    @Override
    public double getX() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getX'");
    }

    @Override
    public double getY() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getY'");
    }

    @Override
    public double getZ() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getZ'");
    }

    @Override
    public float getPitch() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPitch'");
    }

    @Override
    public float getYaw() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getYaw'");
    }

    @Override
    public boolean collidesAt(@NotNull Location location) {
        
        throw new UnsupportedOperationException("Unimplemented method 'collidesAt'");
    }

    @Override
    public boolean wouldCollideUsing(@NotNull BoundingBox boundingBox) {
        
        throw new UnsupportedOperationException("Unimplemented method 'wouldCollideUsing'");
    }

    @Override
    public @NotNull EntityScheduler getScheduler() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getScheduler'");
    }

    @Override
    public @NotNull String getScoreboardEntryName() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getScoreboardEntryName'");
    }

    @Override
    public void broadcastHurtAnimation(@NotNull Collection<Player> players) {
        
        throw new UnsupportedOperationException("Unimplemented method 'broadcastHurtAnimation'");
    }

    @Override
    public void setMetadata(@NotNull String metadataKey, @NotNull MetadataValue newMetadataValue) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setMetadata'");
    }

    @Override
    public @NotNull List<MetadataValue> getMetadata(@NotNull String metadataKey) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getMetadata'");
    }

    @Override
    public boolean hasMetadata(@NotNull String metadataKey) {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasMetadata'");
    }

    @Override
    public void removeMetadata(@NotNull String metadataKey, @NotNull Plugin owningPlugin) {
        
        throw new UnsupportedOperationException("Unimplemented method 'removeMetadata'");
    }

    @Override
    public @NotNull Component name() {
        
        throw new UnsupportedOperationException("Unimplemented method 'name'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Component customName() {
        
        throw new UnsupportedOperationException("Unimplemented method 'customName'");
    }

    @Override
    public void customName(@org.jetbrains.annotations.Nullable Component customName) {
        
        throw new UnsupportedOperationException("Unimplemented method 'customName'");
    }

    @Override
    public @org.jetbrains.annotations.Nullable String getCustomName() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getCustomName'");
    }

    @Override
    public void setCustomName(@org.jetbrains.annotations.Nullable String name) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setCustomName'");
    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPersistentDataContainer'");
    }

    @Override
    public <T> @org.jetbrains.annotations.Nullable T getData(@NotNull Valued<T> type) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getData'");
    }

    @Override
    public <T> @org.jetbrains.annotations.Nullable T getDataOrDefault(@NotNull Valued<? extends T> type,
            @org.jetbrains.annotations.Nullable T fallback) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getDataOrDefault'");
    }

    @Override
    public boolean hasData(@NotNull DataComponentType type) {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasData'");
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile) {
        
        throw new UnsupportedOperationException("Unimplemented method 'launchProjectile'");
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile,
            @org.jetbrains.annotations.Nullable Vector velocity) {
        
        throw new UnsupportedOperationException("Unimplemented method 'launchProjectile'");
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile,
            @org.jetbrains.annotations.Nullable Vector velocity,
            @org.jetbrains.annotations.Nullable Consumer<? super T> function) {
        
        throw new UnsupportedOperationException("Unimplemented method 'launchProjectile'");
    }

    @Override
    public TriState getFrictionState() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getFrictionState'");
    }

    @Override
    public void setFrictionState(TriState state) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setFrictionState'");
    }

    @Override
    public boolean isConversing() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isConversing'");
    }

    @Override
    public void acceptConversationInput(@NotNull String input) {
        
        throw new UnsupportedOperationException("Unimplemented method 'acceptConversationInput'");
    }

    @Override
    public boolean beginConversation(@NotNull Conversation conversation) {
        
        throw new UnsupportedOperationException("Unimplemented method 'beginConversation'");
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation) {
        
        throw new UnsupportedOperationException("Unimplemented method 'abandonConversation'");
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation, @NotNull ConversationAbandonedEvent details) {
        
        throw new UnsupportedOperationException("Unimplemented method 'abandonConversation'");
    }

    @Override
    public void sendRawMessage(@org.jetbrains.annotations.Nullable UUID sender, @NotNull String message) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendRawMessage'");
    }

    @Override
    public boolean isConnected() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isConnected'");
    }

    @Override
    public boolean isBanned() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isBanned'");
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable Date expires,
            @Nullable String source) {
        
        throw new UnsupportedOperationException("Unimplemented method 'ban'");
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason,
            @Nullable Instant expires, @Nullable String source) {
        
        throw new UnsupportedOperationException("Unimplemented method 'ban'");
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason,
            @Nullable Duration duration, @Nullable String source) {
        
        throw new UnsupportedOperationException("Unimplemented method 'ban'");
    }

    @Override
    public boolean isWhitelisted() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isWhitelisted'");
    }

    @Override
    public void setWhitelisted(boolean value) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setWhitelisted'");
    }

    @Override
    public @Nullable Player getPlayer() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPlayer'");
    }

    @Override
    public long getLastLogin() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getLastLogin'");
    }

    @Override
    public long getLastSeen() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getLastSeen'");
    }

    @Override
    public @Nullable Location getRespawnLocation(boolean loadLocationAndValidate) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getRespawnLocation'");
    }

    @Override
    public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'incrementStatistic'");
    }

    @Override
    public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'decrementStatistic'");
    }

    @Override
    public void incrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'incrementStatistic'");
    }

    @Override
    public void decrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'decrementStatistic'");
    }

    @Override
    public void setStatistic(Statistic statistic, int newValue) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'setStatistic'");
    }

    @Override
    public int getStatistic(Statistic statistic) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'getStatistic'");
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'incrementStatistic'");
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'decrementStatistic'");
    }

    @Override
    public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'getStatistic'");
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'incrementStatistic'");
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'decrementStatistic'");
    }

    @Override
    public void setStatistic(Statistic statistic, Material material, int newValue) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'setStatistic'");
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'incrementStatistic'");
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'decrementStatistic'");
    }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'getStatistic'");
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int amount)
            throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'incrementStatistic'");
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        
        throw new UnsupportedOperationException("Unimplemented method 'decrementStatistic'");
    }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setStatistic'");
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        
        throw new UnsupportedOperationException("Unimplemented method 'serialize'");
    }

    @Override
    public void sendPluginMessage(@NotNull Plugin source, @NotNull String channel, byte @NotNull [] message) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendPluginMessage'");
    }

    @Override
    public @NotNull Set<String> getListeningPluginChannels() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getListeningPluginChannels'");
    }

    @Override
    public int getProtocolVersion() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getProtocolVersion'");
    }

    @Override
    public @Nullable InetSocketAddress getVirtualHost() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getVirtualHost'");
    }

    @Override
    public @UnmodifiableView Iterable<? extends BossBar> activeBossBars() {
        
        throw new UnsupportedOperationException("Unimplemented method 'activeBossBars'");
    }

    @Override
    public Component displayName() {
        
        throw new UnsupportedOperationException("Unimplemented method 'displayName'");
    }

    @Override
    public void displayName(@Nullable Component displayName) {
        
        throw new UnsupportedOperationException("Unimplemented method 'displayName'");
    }

    @Override
    public void playerListName(@Nullable Component name) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playerListName'");
    }

    @Override
    public Component playerListName() {
        
        throw new UnsupportedOperationException("Unimplemented method 'playerListName'");
    }

    @Override
    public @Nullable Component playerListHeader() {
        
        throw new UnsupportedOperationException("Unimplemented method 'playerListHeader'");
    }

    @Override
    public @Nullable Component playerListFooter() {
        
        throw new UnsupportedOperationException("Unimplemented method 'playerListFooter'");
    }

    @Override
    public int getPlayerListOrder() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerListOrder'");
    }

    @Override
    public void setPlayerListOrder(int order) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerListOrder'");
    }

    @Override
    public @Nullable String getPlayerListHeader() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerListHeader'");
    }

    @Override
    public @Nullable String getPlayerListFooter() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerListFooter'");
    }

    @Override
    public void setPlayerListHeader(@Nullable String header) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerListHeader'");
    }

    @Override
    public void setPlayerListFooter(@Nullable String footer) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerListFooter'");
    }

    @Override
    public void setPlayerListHeaderFooter(@Nullable String header, @Nullable String footer) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerListHeaderFooter'");
    }

    @Override
    public @Nullable InetSocketAddress getHAProxyAddress() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getHAProxyAddress'");
    }

    @Override
    public boolean isTransferred() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isTransferred'");
    }

    @Override
    public CompletableFuture<byte @Nullable []> retrieveCookie(NamespacedKey key) {
        
        throw new UnsupportedOperationException("Unimplemented method 'retrieveCookie'");
    }

    @Override
    public void storeCookie(NamespacedKey key, byte[] value) {
        
        throw new UnsupportedOperationException("Unimplemented method 'storeCookie'");
    }

    @Override
    public void transfer(String host, int port) {
        
        throw new UnsupportedOperationException("Unimplemented method 'transfer'");
    }

    @Override
    public void sendRawMessage(String message) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendRawMessage'");
    }

    @Override
    public void kick() {
        
        throw new UnsupportedOperationException("Unimplemented method 'kick'");
    }

    @Override
    public void kick(@Nullable Component message, Cause cause) {
        
        throw new UnsupportedOperationException("Unimplemented method 'kick'");
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable Date expires,
            @Nullable String source, boolean kickPlayer) {
        
        throw new UnsupportedOperationException("Unimplemented method 'ban'");
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason,
            @Nullable Instant expires, @Nullable String source, boolean kickPlayer) {
        
        throw new UnsupportedOperationException("Unimplemented method 'ban'");
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason,
            @Nullable Duration duration, @Nullable String source, boolean kickPlayer) {
        
        throw new UnsupportedOperationException("Unimplemented method 'ban'");
    }

    @Override
    public @Nullable BanEntry<InetAddress> banIp(@Nullable String reason, @Nullable Date expires,
            @Nullable String source, boolean kickPlayer) {
        
        throw new UnsupportedOperationException("Unimplemented method 'banIp'");
    }

    @Override
    public @Nullable BanEntry<InetAddress> banIp(@Nullable String reason, @Nullable Instant expires,
            @Nullable String source, boolean kickPlayer) {
        
        throw new UnsupportedOperationException("Unimplemented method 'banIp'");
    }

    @Override
    public @Nullable BanEntry<InetAddress> banIp(@Nullable String reason, @Nullable Duration duration,
            @Nullable String source, boolean kickPlayer) {
        
        throw new UnsupportedOperationException("Unimplemented method 'banIp'");
    }

    @Override
    public void setRespawnLocation(@Nullable Location location) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setRespawnLocation'");
    }

    @Override
    public void setRespawnLocation(@Nullable Location location, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setRespawnLocation'");
    }

    @Override
    public Collection<EnderPearl> getEnderPearls() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getEnderPearls'");
    }

    @Override
    public Input getCurrentInput() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentInput'");
    }

    @Override
    public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch,
            long seed) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playSound'");
    }

    @Override
    public void playSound(Location location, String sound, SoundCategory category, float volume, float pitch,
            long seed) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playSound'");
    }

    @Override
    public void playSound(Entity entity, Sound sound, float volume, float pitch) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playSound'");
    }

    @Override
    public void playSound(Entity entity, String sound, float volume, float pitch) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playSound'");
    }

    @Override
    public void playSound(Entity entity, Sound sound, SoundCategory category, float volume, float pitch) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playSound'");
    }

    @Override
    public void playSound(Entity entity, String sound, SoundCategory category, float volume, float pitch) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playSound'");
    }

    @Override
    public void playSound(Entity entity, Sound sound, SoundCategory category, float volume, float pitch, long seed) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playSound'");
    }

    @Override
    public void playSound(Entity entity, String sound, SoundCategory category, float volume, float pitch, long seed) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playSound'");
    }

    @Override
    public void stopSound(SoundCategory category) {
        
        throw new UnsupportedOperationException("Unimplemented method 'stopSound'");
    }

    @Override
    public void stopAllSounds() {
        
        throw new UnsupportedOperationException("Unimplemented method 'stopAllSounds'");
    }

    @Override
    public void playEffect(Location loc, Effect effect, int data) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playEffect'");
    }

    @Override
    public <T> void playEffect(Location loc, Effect effect, @Nullable T data) {
        
        throw new UnsupportedOperationException("Unimplemented method 'playEffect'");
    }

    @Override
    public boolean breakBlock(Block block) {
        
        throw new UnsupportedOperationException("Unimplemented method 'breakBlock'");
    }

    @Override
    public void sendBlockChange(Location loc, Material material, byte data) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendBlockChange'");
    }

    @Override
    public void sendBlockChange(Location loc, BlockData block) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendBlockChange'");
    }

    @Override
    public void sendBlockChanges(Collection<BlockState> blocks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendBlockChanges'");
    }

    @Override
    public void sendBlockDamage(Location loc, float progress) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendBlockDamage'");
    }

    @Override
    public void sendMultiBlockChange(Map<? extends Position, BlockData> blockChanges) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendMultiBlockChange'");
    }

    @Override
    public void sendBlockDamage(Location loc, float progress, Entity source) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendBlockDamage'");
    }

    @Override
    public void sendBlockDamage(Location loc, float progress, int sourceId) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendBlockDamage'");
    }

    @Override
    public void sendEquipmentChange(LivingEntity entity, EquipmentSlot slot, @Nullable ItemStack item) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendEquipmentChange'");
    }

    @Override
    public void sendEquipmentChange(LivingEntity entity, Map<EquipmentSlot, @Nullable ItemStack> items) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendEquipmentChange'");
    }

    @Override
    public void sendSignChange(Location loc, @Nullable List<? extends Component> lines, DyeColor dyeColor,
            boolean hasGlowingText) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendSignChange'");
    }

    @Override
    public void sendSignChange(Location loc, @Nullable String @Nullable [] lines) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendSignChange'");
    }

    @Override
    public void sendSignChange(Location loc, @Nullable String @Nullable [] lines, DyeColor dyeColor)
            throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendSignChange'");
    }

    @Override
    public void sendSignChange(Location loc, @Nullable String @Nullable [] lines, DyeColor dyeColor,
            boolean hasGlowingText) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendSignChange'");
    }

    @Override
    public void sendBlockUpdate(Location loc, TileState tileState) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendBlockUpdate'");
    }

    @Override
    public void sendPotionEffectChange(LivingEntity entity, PotionEffect effect) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendPotionEffectChange'");
    }

    @Override
    public void sendPotionEffectChangeRemove(LivingEntity entity, PotionEffectType type) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendPotionEffectChangeRemove'");
    }

    @Override
    public void sendMap(MapView map) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendMap'");
    }

    @Override
    public void showWinScreen() {
        
        throw new UnsupportedOperationException("Unimplemented method 'showWinScreen'");
    }

    @Override
    public boolean hasSeenWinScreen() {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasSeenWinScreen'");
    }

    @Override
    public void setHasSeenWinScreen(boolean hasSeenWinScreen) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setHasSeenWinScreen'");
    }

    @Override
    public void sendActionBar(String message) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendActionBar'");
    }

    @Override
    public void sendActionBar(char alternateChar, String message) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendActionBar'");
    }

    @Override
    public void sendActionBar(BaseComponent... message) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendActionBar'");
    }

    @Override
    public void setPlayerListHeaderFooter(BaseComponent @Nullable [] header, BaseComponent @Nullable [] footer) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerListHeaderFooter'");
    }

    @Override
    public void setPlayerListHeaderFooter(@Nullable BaseComponent header, @Nullable BaseComponent footer) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerListHeaderFooter'");
    }

    @Override
    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setTitleTimes'");
    }

    @Override
    public void setSubtitle(BaseComponent[] subtitle) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setSubtitle'");
    }

    @Override
    public void setSubtitle(BaseComponent subtitle) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setSubtitle'");
    }

    @Override
    public void showTitle(@Nullable BaseComponent[] title) {
        
        throw new UnsupportedOperationException("Unimplemented method 'showTitle'");
    }

    @Override
    public void showTitle(@Nullable BaseComponent title) {
        
        throw new UnsupportedOperationException("Unimplemented method 'showTitle'");
    }

    @Override
    public void showTitle(@Nullable BaseComponent[] title, @Nullable BaseComponent[] subtitle, int fadeInTicks,
            int stayTicks, int fadeOutTicks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'showTitle'");
    }

    @Override
    public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int fadeInTicks,
            int stayTicks, int fadeOutTicks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'showTitle'");
    }

    @Override
    public void sendTitle(@SuppressWarnings("deprecation") Title title) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendTitle'");
    }

    @Override
    public void updateTitle(@SuppressWarnings("deprecation") Title title) {
        
        throw new UnsupportedOperationException("Unimplemented method 'updateTitle'");
    }

    @Override
    public void hideTitle() {
        
        throw new UnsupportedOperationException("Unimplemented method 'hideTitle'");
    }

    @Override
    public void sendHurtAnimation(float yaw) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendHurtAnimation'");
    }

    @Override
    public void sendLinks(ServerLinks links) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendLinks'");
    }

    @Override
    public void addCustomChatCompletions(Collection<String> completions) {
        
        throw new UnsupportedOperationException("Unimplemented method 'addCustomChatCompletions'");
    }

    @Override
    public void removeCustomChatCompletions(Collection<String> completions) {
        
        throw new UnsupportedOperationException("Unimplemented method 'removeCustomChatCompletions'");
    }

    @Override
    public void setCustomChatCompletions(Collection<String> completions) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setCustomChatCompletions'");
    }

    @Override
    public void updateInventory() {
        
        throw new UnsupportedOperationException("Unimplemented method 'updateInventory'");
    }

    @Override
    public @Nullable GameMode getPreviousGameMode() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPreviousGameMode'");
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerTime'");
    }

    @Override
    public long getPlayerTime() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerTime'");
    }

    @Override
    public long getPlayerTimeOffset() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerTimeOffset'");
    }

    @Override
    public boolean isPlayerTimeRelative() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isPlayerTimeRelative'");
    }

    @Override
    public void resetPlayerTime() {
        
        throw new UnsupportedOperationException("Unimplemented method 'resetPlayerTime'");
    }

    @Override
    public void setPlayerWeather(WeatherType type) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerWeather'");
    }

    @Override
    public @Nullable WeatherType getPlayerWeather() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerWeather'");
    }

    @Override
    public void resetPlayerWeather() {
        
        throw new UnsupportedOperationException("Unimplemented method 'resetPlayerWeather'");
    }

    @Override
    public int getExpCooldown() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getExpCooldown'");
    }

    @Override
    public void setExpCooldown(int ticks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setExpCooldown'");
    }

    @Override
    public void giveExp(int amount, boolean applyMending) {
        
        throw new UnsupportedOperationException("Unimplemented method 'giveExp'");
    }

    @Override
    public int applyMending(int amount) {
        
        throw new UnsupportedOperationException("Unimplemented method 'applyMending'");
    }

    @Override
    public @Range(from = 0, to = 2147483647) int calculateTotalExperiencePoints() {
        
        throw new UnsupportedOperationException("Unimplemented method 'calculateTotalExperiencePoints'");
    }

    @Override
    public void setExperienceLevelAndProgress(@Range(from = 0, to = 2147483647) int totalExperience) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setExperienceLevelAndProgress'");
    }

    @Override
    public int getExperiencePointsNeededForNextLevel() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getExperiencePointsNeededForNextLevel'");
    }

    @Override
    public void sendExperienceChange(float progress) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendExperienceChange'");
    }

    @Override
    public void sendExperienceChange(float progress, int level) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendExperienceChange'");
    }

    @Override
    public void setFlyingFallDamage(TriState flyingFallDamage) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setFlyingFallDamage'");
    }

    @Override
    public TriState hasFlyingFallDamage() {
        
        throw new UnsupportedOperationException("Unimplemented method 'hasFlyingFallDamage'");
    }

    @Override
    public void hidePlayer(Player player) {
        
        throw new UnsupportedOperationException("Unimplemented method 'hidePlayer'");
    }

    @Override
    public void hidePlayer(Plugin plugin, Player player) {
        
        throw new UnsupportedOperationException("Unimplemented method 'hidePlayer'");
    }

    @Override
    public void showPlayer(Player player) {
        
        throw new UnsupportedOperationException("Unimplemented method 'showPlayer'");
    }

    @Override
    public void showPlayer(Plugin plugin, Player player) {
        
        throw new UnsupportedOperationException("Unimplemented method 'showPlayer'");
    }

    @Override
    public boolean canSee(Player player) {
        
        throw new UnsupportedOperationException("Unimplemented method 'canSee'");
    }

    @Override
    public void hideEntity(Plugin plugin, Entity entity) {
        
        throw new UnsupportedOperationException("Unimplemented method 'hideEntity'");
    }

    @Override
    public void showEntity(Plugin plugin, Entity entity) {
        
        throw new UnsupportedOperationException("Unimplemented method 'showEntity'");
    }

    @Override
    public boolean canSee(Entity entity) {
        
        throw new UnsupportedOperationException("Unimplemented method 'canSee'");
    }

    @Override
    public boolean isListed(Player other) {
        
        throw new UnsupportedOperationException("Unimplemented method 'isListed'");
    }

    @Override
    public boolean unlistPlayer(Player other) {
        
        throw new UnsupportedOperationException("Unimplemented method 'unlistPlayer'");
    }

    @Override
    public boolean listPlayer(Player other) {
        
        throw new UnsupportedOperationException("Unimplemented method 'listPlayer'");
    }

    @Override
    public void setTexturePack(String url) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setTexturePack'");
    }

    @Override
    public void setResourcePack(String url) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setResourcePack'");
    }

    @Override
    public void setResourcePack(String url, byte @Nullable [] hash) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setResourcePack'");
    }

    @Override
    public void setResourcePack(String url, byte @Nullable [] hash, @Nullable String prompt) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setResourcePack'");
    }

    @Override
    public void setResourcePack(String url, byte @Nullable [] hash, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setResourcePack'");
    }

    @Override
    public void setResourcePack(String url, byte @Nullable [] hash, @Nullable String prompt, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setResourcePack'");
    }

    @Override
    public void setResourcePack(UUID id, String url, byte @Nullable [] hash, @Nullable String prompt, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setResourcePack'");
    }

    @Override
    public void setResourcePack(UUID uuid, String url, byte @Nullable [] hash, @Nullable Component prompt,
            boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setResourcePack'");
    }

    @Override
    public @Nullable Status getResourcePackStatus() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getResourcePackStatus'");
    }

    @Override
    public void addResourcePack(UUID id, String url, byte @Nullable [] hash, @Nullable String prompt, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'addResourcePack'");
    }

    @Override
    public void removeResourcePack(UUID id) {
        
        throw new UnsupportedOperationException("Unimplemented method 'removeResourcePack'");
    }

    @Override
    public void removeResourcePacks() {
        
        throw new UnsupportedOperationException("Unimplemented method 'removeResourcePacks'");
    }

    @Override
    public Scoreboard getScoreboard() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getScoreboard'");
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {
        
        throw new UnsupportedOperationException("Unimplemented method 'setScoreboard'");
    }

    @Override
    public @Nullable WorldBorder getWorldBorder() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getWorldBorder'");
    }

    @Override
    public void setWorldBorder(@Nullable WorldBorder border) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setWorldBorder'");
    }

    @Override
    public void sendHealthUpdate(double health, int foodLevel, float saturation) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendHealthUpdate'");
    }

    @Override
    public void sendHealthUpdate() {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendHealthUpdate'");
    }

    @Override
    public boolean isHealthScaled() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isHealthScaled'");
    }

    @Override
    public void setHealthScaled(boolean scale) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setHealthScaled'");
    }

    @Override
    public void setHealthScale(double scale) throws IllegalArgumentException {
        
        throw new UnsupportedOperationException("Unimplemented method 'setHealthScale'");
    }

    @Override
    public double getHealthScale() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getHealthScale'");
    }

    @Override
    public @Nullable Entity getSpectatorTarget() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSpectatorTarget'");
    }

    @Override
    public void setSpectatorTarget(@Nullable Entity entity) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setSpectatorTarget'");
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendTitle'");
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendTitle'");
    }

    @Override
    public void resetTitle() {
        
        throw new UnsupportedOperationException("Unimplemented method 'resetTitle'");
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, @Nullable T data) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, @Nullable T data) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
            double offsetZ) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
            double offsetZ, @Nullable T data) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ, @Nullable T data) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
            double offsetZ, double extra) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ, double extra) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
            double offsetZ, double extra, @Nullable T data) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ, double extra, @Nullable T data) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
            double offsetZ, double extra, @Nullable T data, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ, double extra, @Nullable T data, boolean force) {
        
        throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
    }

    @Override
    public AdvancementProgress getAdvancementProgress(Advancement advancement) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getAdvancementProgress'");
    }

    @Override
    public int getClientViewDistance() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getClientViewDistance'");
    }

    @Override
    public Locale locale() {
        
        throw new UnsupportedOperationException("Unimplemented method 'locale'");
    }

    @Override
    public int getPing() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPing'");
    }

    @Override
    public String getLocale() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getLocale'");
    }

    @Override
    public boolean getAffectsSpawning() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getAffectsSpawning'");
    }

    @Override
    public void setAffectsSpawning(boolean affects) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setAffectsSpawning'");
    }

    @Override
    public int getViewDistance() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getViewDistance'");
    }

    @Override
    public void setViewDistance(int viewDistance) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setViewDistance'");
    }

    @Override
    public int getSimulationDistance() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSimulationDistance'");
    }

    @Override
    public void setSimulationDistance(int simulationDistance) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setSimulationDistance'");
    }

    @Override
    public int getSendViewDistance() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSendViewDistance'");
    }

    @Override
    public void setSendViewDistance(int viewDistance) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setSendViewDistance'");
    }

    @Override
    public void updateCommands() {
        
        throw new UnsupportedOperationException("Unimplemented method 'updateCommands'");
    }

    @Override
    public void openBook(ItemStack book) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openBook'");
    }

    @Override
    public void openSign(Sign sign) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openSign'");
    }

    @Override
    public void openSign(Sign sign, Side side) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openSign'");
    }

    @Override
    public void openVirtualSign(Position block, Side side) {
        
        throw new UnsupportedOperationException("Unimplemented method 'openVirtualSign'");
    }

    @Override
    public void showDemoScreen() {
        
        throw new UnsupportedOperationException("Unimplemented method 'showDemoScreen'");
    }

    @Override
    public boolean isAllowingServerListings() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isAllowingServerListings'");
    }

    @Override
    public PlayerProfile getPlayerProfile() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerProfile'");
    }

    @Override
    public void setPlayerProfile(PlayerProfile profile) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerProfile'");
    }

    @Override
    public float getCooldownPeriod() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getCooldownPeriod'");
    }

    @Override
    public float getCooledAttackStrength(float adjustTicks) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getCooledAttackStrength'");
    }

    @Override
    public void resetCooldown() {
        
        throw new UnsupportedOperationException("Unimplemented method 'resetCooldown'");
    }

    @Override
    public <T> T getClientOption(ClientOption<T> option) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getClientOption'");
    }

    @Override
    public void sendOpLevel(byte level) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendOpLevel'");
    }

    @Override
    public void addAdditionalChatCompletions(Collection<String> completions) {
        
        throw new UnsupportedOperationException("Unimplemented method 'addAdditionalChatCompletions'");
    }

    @Override
    public void removeAdditionalChatCompletions(Collection<String> completions) {
        
        throw new UnsupportedOperationException("Unimplemented method 'removeAdditionalChatCompletions'");
    }

    @Override
    public @Nullable String getClientBrandName() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getClientBrandName'");
    }

    @Override
    public void lookAt(Entity entity, LookAnchor playerAnchor, LookAnchor entityAnchor) {
        
        throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
    }

    @Override
    public void showElderGuardian(boolean silent) {
        
        throw new UnsupportedOperationException("Unimplemented method 'showElderGuardian'");
    }

    @Override
    public int getWardenWarningCooldown() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getWardenWarningCooldown'");
    }

    @Override
    public void setWardenWarningCooldown(int cooldown) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setWardenWarningCooldown'");
    }

    @Override
    public int getWardenTimeSinceLastWarning() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getWardenTimeSinceLastWarning'");
    }

    @Override
    public void setWardenTimeSinceLastWarning(int time) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setWardenTimeSinceLastWarning'");
    }

    @Override
    public int getWardenWarningLevel() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getWardenWarningLevel'");
    }

    @Override
    public void setWardenWarningLevel(int warningLevel) {
        
        throw new UnsupportedOperationException("Unimplemented method 'setWardenWarningLevel'");
    }

    @Override
    public void increaseWardenWarningLevel() {
        
        throw new UnsupportedOperationException("Unimplemented method 'increaseWardenWarningLevel'");
    }

    @Override
    public Duration getIdleDuration() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getIdleDuration'");
    }

    @Override
    public void resetIdleDuration() {
        
        throw new UnsupportedOperationException("Unimplemented method 'resetIdleDuration'");
    }

    @Override
    public @Unmodifiable Set<Long> getSentChunkKeys() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSentChunkKeys'");
    }

    @Override
    public @Unmodifiable Set<Chunk> getSentChunks() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSentChunks'");
    }

    @Override
    public boolean isChunkSent(long chunkKey) {
        
        throw new UnsupportedOperationException("Unimplemented method 'isChunkSent'");
    }

    @Override
    public Spigot spigot() {
        
        throw new UnsupportedOperationException("Unimplemented method 'spigot'");
    }

    @Override
    public void sendEntityEffect(EntityEffect effect, Entity target) {
        
        throw new UnsupportedOperationException("Unimplemented method 'sendEntityEffect'");
    }

    @Override
    public PlayerGiveResult give(Collection<ItemStack> items, boolean dropIfFull) {
        
        throw new UnsupportedOperationException("Unimplemented method 'give'");
    }

    @Override
    public int getDeathScreenScore() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getDeathScreenScore'");
    }
}