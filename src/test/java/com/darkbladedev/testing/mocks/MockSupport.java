package com.darkbladedev.testing.mocks;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import com.destroystokyo.paper.block.BlockSoundGroup;
import com.destroystokyo.paper.profile.PlayerProfile;

import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.util.TriState;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Clases de soporte mock adicionales para el sistema de pruebas.
 */
public class MockSupport {
    
    /**
     * Mock de Block.
     */
    static class MockBlock implements Block {
        private final World world;
        private final int x, y, z;
        private Material type;
        private BlockData blockData;
        
        public MockBlock(World world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = Material.AIR;
            this.blockData = type.createBlockData();
        }
        
        @Override
        public Material getType() { return type; }
        
        @Override
        public void setType(Material type) {
            this.type = type;
            this.blockData = type.createBlockData();
        }
        
        @Override
        public void setType(Material type, boolean applyPhysics) {
            setType(type);
        }
        
        @Override
        public BlockData getBlockData() { return blockData; }
        
        @Override
        public void setBlockData(BlockData data) {
            this.blockData = data;
            this.type = data.getMaterial();
        }
        
        @Override
        public void setBlockData(BlockData data, boolean applyPhysics) {
            setBlockData(data);
        }
        
        @Override
        public World getWorld() { return world; }
        
        @Override
        public int getX() { return x; }
        
        @Override
        public int getY() { return y; }
        
        @Override
        public int getZ() { return z; }
        
        @Override
        public Location getLocation() {
            return new Location(world, x, y, z);
        }
        
        @Override
        public Location getLocation(Location loc) {
            if (loc != null) {
                loc.setWorld(world);
                loc.setX(x);
                loc.setY(y);
                loc.setZ(z);
            }
            return loc;
        }
        
        @Override
        public Chunk getChunk() {
            return world.getChunkAt(x >> 4, z >> 4);
        }
        
        @Override
        public org.bukkit.block.BlockState getState() {
            return new MockBlockState(this);
        }
        
        @Override
        public Biome getBiome() { return Biome.PLAINS; }
        
        @Override
        public void setBiome(Biome bio) {}
        
        @Override
        public boolean isBlockPowered() { return false; }
        
        @Override
        public boolean isBlockIndirectlyPowered() { return false; }
        
        @Override
        public boolean isBlockFacePowered(BlockFace face) { return false; }
        
        @Override
        public boolean isBlockFaceIndirectlyPowered(BlockFace face) { return false; }
        
        @Override
        public int getBlockPower(BlockFace face) { return 0; }
        
        @Override
        public int getBlockPower() { return 0; }
        
        @Override
        public boolean isEmpty() { return type == Material.AIR; }
        
        @Override
        public boolean isLiquid() {
            return type == Material.WATER || type == Material.LAVA;
        }
        
        @Override
        public double getTemperature() { return 0.8; }
        
        @Override
        public double getHumidity() { return 0.4; }
        
        @Override
        public PistonMoveReaction getPistonMoveReaction() {
            return PistonMoveReaction.BLOCK;
        }
        
        @Override
        public boolean breakNaturally() { return true; }
        
        @Override
        public boolean breakNaturally(ItemStack tool) { return true; }
        
        @Override
        public Collection<ItemStack> getDrops() {
            return Arrays.asList(new ItemStack(type));
        }
        
        @Override
        public Collection<ItemStack> getDrops(ItemStack tool) {
            return getDrops();
        }
        
        @Override
        public Collection<ItemStack> getDrops(ItemStack tool, Entity entity) {
            return getDrops();
        }
        
        @Override
        public Block getRelative(int modX, int modY, int modZ) {
            return world.getBlockAt(x + modX, y + modY, z + modZ);
        }
        
        @Override
        public Block getRelative(BlockFace face) {
            return getRelative(face.getModX(), face.getModY(), face.getModZ());
        }
        
        @Override
        public Block getRelative(BlockFace face, int distance) {
            return getRelative(face.getModX() * distance, face.getModY() * distance, face.getModZ() * distance);
        }
        
        @Override
        public BlockFace getFace(Block block) {
            int dx = block.getX() - x;
            int dy = block.getY() - y;
            int dz = block.getZ() - z;
            
            if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) {
                return null;
            }
            
            if (dx == 1) return BlockFace.EAST;
            if (dx == -1) return BlockFace.WEST;
            if (dy == 1) return BlockFace.UP;
            if (dy == -1) return BlockFace.DOWN;
            if (dz == 1) return BlockFace.SOUTH;
            if (dz == -1) return BlockFace.NORTH;
            
            return null;
        }
        
        @Override
        public byte getLightLevel() { return 15; }
        
        @Override
        public byte getLightFromSky() { return 15; }
        
        @Override
        public byte getLightFromBlocks() { return 0; }
        
        @Override
        public boolean canPlace(BlockData data) { return true; }
        
        @Override
        public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
            return null;
        }
        
        @Override
        public BoundingBox getBoundingBox() {
            return new org.bukkit.util.BoundingBox(x, y, z, x + 1, y + 1, z + 1);
        }
        
        @Override
        public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {}
        
        @Override
        public List<MetadataValue> getMetadata(String metadataKey) { return new ArrayList<>(); }
        
        @Override
        public boolean hasMetadata(String metadataKey) { return false; }
        
        @Override
        public void removeMetadata(String metadataKey, Plugin owningPlugin) {}
        
        @Override
        public boolean isSuffocating() { return false; }

        @Override
        public @NotNull String translationKey() {
            
            throw new UnsupportedOperationException("Unimplemented method 'translationKey'");
        }

        @Override
        public boolean isValidTool(@NotNull ItemStack itemStack) {
            
            throw new UnsupportedOperationException("Unimplemented method 'isValidTool'");
        }

        @Override
        public @NotNull BlockState getState(boolean useSnapshot) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getState'");
        }

        @Override
        public @NotNull Biome getComputedBiome() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getComputedBiome'");
        }

        @Override
        public boolean isBuildable() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isBuildable'");
        }

        @Override
        public boolean isBurnable() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isBurnable'");
        }

        @Override
        public boolean isReplaceable() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isReplaceable'");
        }

        @Override
        public boolean isSolid() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isSolid'");
        }

        @Override
        public boolean isCollidable() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isCollidable'");
        }

        @Override
        public boolean breakNaturally(boolean triggerEffect, boolean dropExperience) {
            
            throw new UnsupportedOperationException("Unimplemented method 'breakNaturally'");
        }

        @Override
        public boolean breakNaturally(@NotNull ItemStack tool, boolean triggerEffect, boolean dropExperience) {
            
            throw new UnsupportedOperationException("Unimplemented method 'breakNaturally'");
        }

        @Override
        public void tick() {
            
            throw new UnsupportedOperationException("Unimplemented method 'tick'");
        }

        @Override
        public void fluidTick() {
            
            throw new UnsupportedOperationException("Unimplemented method 'fluidTick'");
        }

        @Override
        public void randomTick() {
            
            throw new UnsupportedOperationException("Unimplemented method 'randomTick'");
        }

        @Override
        public boolean applyBoneMeal(@NotNull BlockFace face) {
            
            throw new UnsupportedOperationException("Unimplemented method 'applyBoneMeal'");
        }

        @Override
        public boolean isPreferredTool(@NotNull ItemStack tool) {
            
            throw new UnsupportedOperationException("Unimplemented method 'isPreferredTool'");
        }

        @Override
        public float getBreakSpeed(@NotNull Player player) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getBreakSpeed'");
        }

        @Override
        public boolean isPassable() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isPassable'");
        }

        @Override
        public @NotNull VoxelShape getCollisionShape() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getCollisionShape'");
        }

        @SuppressWarnings("removal")
        @Override
        public @NotNull BlockSoundGroup getSoundGroup() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getSoundGroup'");
        }

        @Override
        public @NotNull SoundGroup getBlockSoundGroup() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getBlockSoundGroup'");
        }

        @Override
        public @NotNull String getTranslationKey() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTranslationKey'");
        }
        
        @Override
        public byte getData() {
            return 0; // Método deprecado, retorna valor por defecto
        }
    }
    
    /**
     * Mock de BlockState.
     */
    static class MockBlockState implements BlockState {
        private final Block block;
        private Material type;
        private BlockData blockData;
        
        public MockBlockState(Block block) {
            this.block = block;
            this.type = block.getType();
            this.blockData = block.getBlockData();
        }
        
        @Override
        public Block getBlock() { return block; }
        
        @Override
        public Material getType() { return type; }
        
        @Override
        public void setType(Material type) {
            this.type = type;
            this.blockData = type.createBlockData();
        }
        
        @Override
        public BlockData getBlockData() { return blockData; }
        
        @Override
        public void setBlockData(BlockData data) {
            this.blockData = data;
            this.type = data.getMaterial();
        }
        
        @Override
        public boolean update() { return true; }
        
        @Override
        public boolean update(boolean force) { return true; }
        
        @Override
        public boolean update(boolean force, boolean applyPhysics) { return true; }
        
        @Override
        public byte getRawData() { return 0; }
        
        @Override
        public void setRawData(byte data) {}
        
        @Override
        public boolean isSuffocating() { return false; }
        
        @Override
        public Location getLocation() { return block.getLocation(); }
        
        @Override
        public Location getLocation(Location loc) { return block.getLocation(loc); }
        
        @Override
        public World getWorld() { return block.getWorld(); }
        
        @Override
        public int getX() { return block.getX(); }
        
        @Override
        public int getY() { return block.getY(); }
        
        @Override
        public int getZ() { return block.getZ(); }
        
        @Override
        public Chunk getChunk() { return block.getChunk(); }
        
        @Override
        public boolean isPlaced() { return true; }
        
        @Override
        public boolean isCollidable() { return true; }
        
        @Override
        public Collection<ItemStack> getDrops(ItemStack tool, Entity entity) {
            return new ArrayList<>();
        }
        
        @Override
        public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {}
        
        @Override
        public List<MetadataValue> getMetadata(String metadataKey) { return new ArrayList<>(); }
        
        @Override
        public boolean hasMetadata(String metadataKey) { return false; }
        
        @Override
        public void removeMetadata(String metadataKey, Plugin owningPlugin) {}
        
        @SuppressWarnings("removal")
        @Override
        public org.bukkit.material.MaterialData getData() {
            return new org.bukkit.material.MaterialData(org.bukkit.Material.STONE); // Valor por defecto para pruebas
        }
        
        @Override
        public void setData(@SuppressWarnings("removal") org.bukkit.material.MaterialData data) {
            // No-op para pruebas
        }
        
        @Override
        public byte getLightLevel() {
            return 15; // Valor por defecto para pruebas
        }
        
        @Override
        public BlockState copy(Location location) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public BlockState copy() {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Mock de Chunk.
     */
    static class MockChunk implements Chunk {
        private final World world;
        private final int x, z;
        
        public MockChunk(World world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }
        
        @Override
        public int getX() { return x; }
        
        @Override
        public int getZ() { return z; }
        
        @Override
        public World getWorld() { return world; }
        
        @Override
        public Block getBlock(int x, int y, int z) {
            return world.getBlockAt((this.x << 4) + x, y, (this.z << 4) + z);
        }
        
        @Override
        public ChunkSnapshot getChunkSnapshot() {
            return getChunkSnapshot(true, false, false);
        }
        
        @Override
        public ChunkSnapshot getChunkSnapshot(boolean includeMaxblocky, boolean includeBiome, boolean includeBiomeTempRain) {
            return new MockChunkSnapshot();
        }
        
        @Override
        public Entity[] getEntities() { return new Entity[0]; }
        
        @Override
        public BlockState[] getTileEntities() { return new BlockState[0]; }
        
        @Override
        public BlockState[] getTileEntities(boolean useSnapshot) { return new BlockState[0]; }
        
        @Override
        public boolean isLoaded() { return true; }
        
        @Override
        public boolean load() { return true; }
        
        @Override
        public boolean load(boolean generate) { return true; }
        
        @Override
        public boolean unload() { return true; }
        
        @Override
        public boolean unload(boolean save) { return true; }
        
        @Override
        public boolean isSlimeChunk() { return false; }
        
        @Override
        public boolean isForceLoaded() { return false; }
        
        @Override
        public void setForceLoaded(boolean forced) {}
        
        @Override
        public boolean addPluginChunkTicket(Plugin plugin) { return true; }
        
        @Override
        public boolean removePluginChunkTicket(Plugin plugin) { return true; }
        
        @Override
        public Collection<Plugin> getPluginChunkTickets() { return new ArrayList<>(); }
        
        @Override
        public long getInhabitedTime() { return 0; }
        
        @Override
        public void setInhabitedTime(long ticks) {}
        
        public boolean contains(BlockData block) { return false; }
        
        @Override
        public boolean contains(Biome biome) { return false; }
        
        @Override
        public Collection<org.bukkit.entity.Player> getPlayersSeeingChunk() { return new ArrayList<>(); }
        
        @Override
        public Collection<org.bukkit.generator.structure.GeneratedStructure> getStructures(org.bukkit.generator.structure.Structure structure) { return new ArrayList<>(); }
        
        @Override
        public Collection<org.bukkit.generator.structure.GeneratedStructure> getStructures() { return new ArrayList<>(); }
        
        @Override
        public org.bukkit.Chunk.LoadLevel getLoadLevel() { return org.bukkit.Chunk.LoadLevel.ENTITY_TICKING; }
        
        @Override
        public boolean isGenerated() { return true; }
        
        @Override
        public java.util.Collection<org.bukkit.block.BlockState> getTileEntities(java.util.function.Predicate<? super org.bukkit.block.Block> blockPredicate, boolean useSnapshot) {
            return new java.util.ArrayList<>();
        }
        
        @Override
        public boolean isEntitiesLoaded() { return true; }
        
        @Override
        public org.bukkit.ChunkSnapshot getChunkSnapshot(boolean includeMaxblocky, boolean includeBiome, boolean includeBiomeTempRain, boolean includeStructures) {
            return new MockChunkSnapshot();
        }
        
        @Override
        public org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() {
            return new MockSupport.MockPersistentDataContainer();
        }
    }
    
    /**
     * Mock de ChunkSnapshot.
     */
    static class MockChunkSnapshot implements ChunkSnapshot {
        @Override
        public int getX() { return 0; }
        
        @Override
        public int getZ() { return 0; }
        
        @Override
        public String getWorldName() { return "test-world"; }
        
        @Override
        public Material getBlockType(int x, int y, int z) { return Material.AIR; }
        
        @Override
        public BlockData getBlockData(int x, int y, int z) { return Material.AIR.createBlockData(); }
        
        @Override
        public int getData(int x, int y, int z) { return 0; }
        
        @Override
        public int getBlockSkyLight(int x, int y, int z) { return 15; }
        
        @Override
        public int getBlockEmittedLight(int x, int y, int z) { return 0; }
        
        @Override
        public int getHighestBlockYAt(int x, int z) { return 64; }
        
        @Override
        public Biome getBiome(int x, int z) { return Biome.PLAINS; }
        
        @Override
        public Biome getBiome(int x, int y, int z) { return Biome.PLAINS; }
        
        public double getRawBiomeTemperature(int x, int z) { return 0.8; }
        
        @Override
        public double getRawBiomeTemperature(int x, int y, int z) { return 0.8; }
        
        public double getRawBiomeRainfall(int x, int z) { return 0.4; }
        
        @Override
        public long getCaptureFullTime() { return System.currentTimeMillis(); }
        
        @Override
        public boolean isSectionEmpty(int sy) { return sy < 0 || sy > 15; }
        
        @Override
        public boolean contains(Biome biome) { return false; }
        
        @Override
        public boolean contains(BlockData block) { return false; }
    }
    
    /**
     * Mock de Item (entidad).
     */
    static class MockItem extends MockEntities.MockEntity implements Item {
        private ItemStack itemStack;
        private int pickupDelay;
        
        public MockItem(Location location, ItemStack itemStack) {
            super(location, EntityType.ITEM);
            this.itemStack = itemStack.clone();
            this.pickupDelay = 10;
        }
        
        @Override
        public ItemStack getItemStack() { return itemStack.clone(); }
        
        @Override
        public void setItemStack(ItemStack stack) { this.itemStack = stack.clone(); }
        
        @Override
        public int getPickupDelay() { return pickupDelay; }
        
        @Override
        public void setPickupDelay(int delay) { this.pickupDelay = delay; }
        
        @Override
        public void setOwner(UUID owner) {}
        
        @Override
        public UUID getOwner() { return null; }
        
        @Override
        public void setThrower(UUID uuid) {}
        
        @Override
        public UUID getThrower() { return null; }
        
        @Override
        public boolean canMobPickup() { return true; }
        
        @Override
        public void setCanMobPickup(boolean canMobPickup) {}
        
        @Override
        public boolean canPlayerPickup() { return true; }
        
        @Override
        public void setCanPlayerPickup(boolean canPlayerPickup) {}
        
        @Override
        public boolean willAge() { return true; }
        
        @Override
        public void setWillAge(boolean willAge) {}

        @Override
        public TriState getFrictionState() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getFrictionState'");
        }

        @Override
        public void setFrictionState(TriState state) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setFrictionState'");
        }

        @Override
        public void setUnlimitedLifetime(boolean unlimited) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setUnlimitedLifetime'");
        }

        @Override
        public boolean isUnlimitedLifetime() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isUnlimitedLifetime'");
        }

        @Override
        public int getHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHealth'");
        }

        @Override
        public void setHealth(int health) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHealth'");
        }
    }
    
    /**
     * Mock de Arrow.
     */
    static class MockArrow extends MockEntities.MockEntity implements Arrow {
        private int knockbackStrength;
        private boolean critical;
        private double damage;
        
        public MockArrow(Location location) {
            super(location, EntityType.ARROW);
            this.knockbackStrength = 0;
            this.critical = false;
            this.damage = 2.0;
        }
        
        @Override
        public int getKnockbackStrength() { return knockbackStrength; }
        
        @Override
        public void setKnockbackStrength(int knockbackStrength) {
            this.knockbackStrength = knockbackStrength;
        }
        
        @Override
        public boolean isCritical() { return critical; }
        
        @Override
        public void setCritical(boolean critical) { this.critical = critical; }
        
        @Override
        public ProjectileSource getShooter() { return null; }
        
        @Override
        public void setShooter(ProjectileSource source) {}
        
        @Override
        public boolean doesBounce() { return false; }
        
        @Override
        public void setBounce(boolean doesBounce) {}
        
        @Override
        public double getDamage() { return damage; }
        
        @Override
        public void setDamage(double damage) { this.damage = damage; }
        
        @Override
        public int getPierceLevel() { return 0; }
        
        @Override
        public void setPierceLevel(int pierceLevel) {}
        
        @Override
        public boolean isShotFromCrossbow() { return false; }
        
        @Override
        public void setShotFromCrossbow(boolean shotFromCrossbow) {}
        
        @Override
        public PickupStatus getPickupStatus() { return PickupStatus.ALLOWED; }
        
        @Override
        public void setPickupStatus(PickupStatus status) {}
        
        @Override
        public boolean isInBlock() { return false; }
        
        @Override
        public Block getAttachedBlock() { return null; }

        @Override
        public @NotNull @Unmodifiable List<Block> getAttachedBlocks() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAttachedBlocks'");
        }

        @Override
        public @NotNull ItemStack getItem() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getItem'");
        }

        @Override
        public void setItem(@NotNull ItemStack item) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setItem'");
        }

        @Override
        public @Nullable ItemStack getWeapon() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getWeapon'");
        }

        @Override
        public void setWeapon(@NotNull ItemStack item) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setWeapon'");
        }

        @Override
        public @NotNull ItemStack getItemStack() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getItemStack'");
        }

        @Override
        public void setItemStack(@NotNull ItemStack stack) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setItemStack'");
        }

        @Override
        public void setLifetimeTicks(int ticks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setLifetimeTicks'");
        }

        @Override
        public int getLifetimeTicks() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLifetimeTicks'");
        }

        @Override
        public @NotNull Sound getHitSound() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHitSound'");
        }

        @Override
        public void setHitSound(@NotNull Sound sound) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHitSound'");
        }

        @Override
        public void setShooter(@Nullable ProjectileSource source, boolean resetPickupStatus) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setShooter'");
        }

        @Override
        public boolean hasLeftShooter() {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasLeftShooter'");
        }

        @Override
        public void setHasLeftShooter(boolean leftShooter) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHasLeftShooter'");
        }

        @Override
        public boolean hasBeenShot() {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasBeenShot'");
        }

        @Override
        public void setHasBeenShot(boolean beenShot) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHasBeenShot'");
        }

        @Override
        public boolean canHitEntity(@NotNull Entity entity) {
            
            throw new UnsupportedOperationException("Unimplemented method 'canHitEntity'");
        }

        @Override
        public void hitEntity(@NotNull Entity entity) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hitEntity'");
        }

        @Override
        public void hitEntity(@NotNull Entity entity, @NotNull Vector vector) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hitEntity'");
        }

        @Override
        public @Nullable UUID getOwnerUniqueId() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getOwnerUniqueId'");
        }

        @Override
        public void setBasePotionData(@SuppressWarnings("removal") @Nullable PotionData data) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBasePotionData'");
        }

        @SuppressWarnings("removal")
        @Override
        public @Nullable PotionData getBasePotionData() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getBasePotionData'");
        }

        @Override
        public void setBasePotionType(@Nullable PotionType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBasePotionType'");
        }

        @Override
        public @Nullable PotionType getBasePotionType() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getBasePotionType'");
        }

        @Override
        public @Nullable Color getColor() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getColor'");
        }

        @Override
        public void setColor(@Nullable Color color) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setColor'");
        }

        @Override
        public boolean hasCustomEffects() {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasCustomEffects'");
        }

        @Override
        public @NotNull List<PotionEffect> getCustomEffects() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getCustomEffects'");
        }

        @Override
        public boolean addCustomEffect(@NotNull PotionEffect effect, boolean overwrite) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addCustomEffect'");
        }

        @Override
        public boolean removeCustomEffect(@NotNull PotionEffectType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'removeCustomEffect'");
        }

        @Override
        public boolean hasCustomEffect(@Nullable PotionEffectType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasCustomEffect'");
        }

        @Override
        public void clearCustomEffects() {
            
            throw new UnsupportedOperationException("Unimplemented method 'clearCustomEffects'");
        }
    }
    
    /**
     * Mock de LightningStrike.
     */
    static class MockLightningStrike extends MockEntities.MockEntity implements LightningStrike {
        private boolean effect;
        
        public MockLightningStrike(Location location) {
            super(location, EntityType.LIGHTNING_BOLT);
            this.effect = false;
        }
        
        @Override
        public boolean isEffect() { return effect; }
                
        @Override
        public int getFlashCount() { return 1; }
        
        @Override
        public void setFlashCount(int flashes) {}
        
        @Override
        public int getLifeTicks() { return 2; }
        
        @Override
        public void setLifeTicks(int ticks) {}
        
        @Override
        public Player getCausingPlayer() { return null; }
        
        @Override
        public void setCausingPlayer(Player player) {}
        
        @SuppressWarnings("removal")
        @Override
        public LightningStrike.Spigot spigot() {
            return new LightningStrike.Spigot() {};
        }

        @Override
        public int getFlashes() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getFlashes'");
        }

        @Override
        public void setFlashes(int flashes) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setFlashes'");
        }

        @Override
        public @Nullable Entity getCausingEntity() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getCausingEntity'");
        }
    }
    
    /**
     * Mock de WorldBorder.
     */
    static class MockWorldBorder implements WorldBorder {
        private double size = 60000000;
        private Location center = new Location(null, 0, 0, 0);
        private double damageAmount = 0.2;
        private double damageBuffer = 5.0;
        private int warningDistance = 5;
        private int warningTime = 15;
        
        @Override
        public void reset() {
            size = 60000000;
            center = new Location(null, 0, 0, 0);
        }
        
        @Override
        public double getSize() { return size; }
        
        @Override
        public void setSize(double newSize) { this.size = newSize; }
        
        @Override
        public void setSize(double newSize, long seconds) { this.size = newSize; }
        
        @Override
        public Location getCenter() { return center.clone(); }
        
        @Override
        public void setCenter(double x, double z) {
            center.setX(x);
            center.setZ(z);
        }
        
        @Override
        public void setCenter(Location location) {
            center = location.clone();
        }
        
        @Override
        public double getDamageAmount() { return damageAmount; }
        
        @Override
        public void setDamageAmount(double damage) { this.damageAmount = damage; }
        
        @Override
        public double getDamageBuffer() { return damageBuffer; }
        
        @Override
        public void setDamageBuffer(double blocks) { this.damageBuffer = blocks; }
        
        @Override
        public int getWarningTime() { return warningTime; }
        
        @Override
        public void setWarningTime(int seconds) { this.warningTime = seconds; }
        
        @Override
        public int getWarningDistance() { return warningDistance; }
        
        @Override
        public void setWarningDistance(int distance) { this.warningDistance = distance; }
        
        @Override
        public boolean isInside(Location location) {
            double dx = Math.abs(location.getX() - center.getX());
            double dz = Math.abs(location.getZ() - center.getZ());
            return dx <= size / 2 && dz <= size / 2;
        }

        @Override
        public @Nullable World getWorld() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getWorld'");
        }

        @Override
        public void setSize(double newSize, @NotNull TimeUnit unit, long time) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setSize'");
        }

        @Override
        public double getMaxSize() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMaxSize'");
        }

        @Override
        public double getMaxCenterCoordinate() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMaxCenterCoordinate'");
        }
    }
    
    /**
     * Mock de PersistentDataContainer.
     */
    static class MockPersistentDataContainer implements PersistentDataContainer {
        private final Map<NamespacedKey, Object> data = new HashMap<>();
        
        @Override
        public <T, Z> void set(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
            data.put(key, value);
        }
        
        @Override
        public <T, Z> boolean has(NamespacedKey key, PersistentDataType<T, Z> type) {
            return data.containsKey(key);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <T, Z> Z get(NamespacedKey key, PersistentDataType<T, Z> type) {
            return (Z) data.get(key);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <T, Z> Z getOrDefault(NamespacedKey key, PersistentDataType<T, Z> type, Z defaultValue) {
            return data.containsKey(key) ? (Z) data.get(key) : defaultValue;
        }
        
        @Override
        public Set<NamespacedKey> getKeys() {
            return new HashSet<>(data.keySet());
        }
        
        @Override
        public void remove(NamespacedKey key) {
            data.remove(key);
        }
        
        @Override
        public boolean isEmpty() {
            return data.isEmpty();
        }
        
        @Override
        public PersistentDataAdapterContext getAdapterContext() {
            return new MockPersistentDataAdapterContext();
        }

        @Override
        public boolean has(NamespacedKey key) {
            
            throw new UnsupportedOperationException("Unimplemented method 'has'");
        }

        @Override
        public void copyTo(PersistentDataContainer other, boolean replace) {
            
            throw new UnsupportedOperationException("Unimplemented method 'copyTo'");
        }

        @Override
        public byte[] serializeToBytes() throws IOException {
            
            throw new UnsupportedOperationException("Unimplemented method 'serializeToBytes'");
        }

        @Override
        public void readFromBytes(byte @NotNull [] bytes, boolean clear) throws IOException {
            
            throw new UnsupportedOperationException("Unimplemented method 'readFromBytes'");
        }
    }
    
    /**
     * Mock de PersistentDataAdapterContext.
     */
    static class MockPersistentDataAdapterContext implements PersistentDataAdapterContext {
        @Override
        public PersistentDataContainer newPersistentDataContainer() {
            return new MockPersistentDataContainer();
        }
    }
    
    /**
     * Mock de OfflinePlayer.
     */
    static class MockOfflinePlayer implements OfflinePlayer {
        private final UUID uniqueId;
        private final String name;
        private boolean banned;
        private boolean whitelisted;
        private boolean online;
        private long firstPlayed;
        private long lastPlayed;
        private boolean hasPlayedBefore;
        
        public MockOfflinePlayer(UUID uniqueId) {
            this.uniqueId = uniqueId;
            this.name = "TestPlayer_" + uniqueId.toString().substring(0, 8);
            this.banned = false;
            this.whitelisted = false;
            this.online = false;
            this.firstPlayed = System.currentTimeMillis() - 86400000; // 1 día atrás
            this.lastPlayed = System.currentTimeMillis() - 3600000; // 1 hora atrás
            this.hasPlayedBefore = true;
        }
        
        public MockOfflinePlayer(String name) {
            this.uniqueId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
            this.name = name;
            this.banned = false;
            this.whitelisted = false;
            this.online = false;
            this.firstPlayed = System.currentTimeMillis() - 86400000;
            this.lastPlayed = System.currentTimeMillis() - 3600000;
            this.hasPlayedBefore = true;
        }
        
        @Override
        public boolean isOnline() { return online; }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public UUID getUniqueId() { return uniqueId; }
        
        @Override
        public boolean isBanned() { return banned; }
        
        @Override
        public boolean isWhitelisted() { return whitelisted; }
        
        @Override
        public void setWhitelisted(boolean value) { this.whitelisted = value; }
        
        @Override
        public Player getPlayer() { return online ? new MockPlayer(name) : null; }
        
        @Override
        public long getFirstPlayed() { return firstPlayed; }
        
        @Override
        public long getLastPlayed() { return lastPlayed; }
        
        @Override
        public boolean hasPlayedBefore() { return hasPlayedBefore; }
        
        @Override
        public Location getBedSpawnLocation() { return null; }
        
        @Override
        public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {}
        
        @Override
        public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {}
        
        @Override
        public void incrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {}
        
        @Override
        public void decrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {}
        
        @Override
        public void setStatistic(Statistic statistic, int newValue) throws IllegalArgumentException {}
        
        @Override
        public int getStatistic(Statistic statistic) throws IllegalArgumentException { return 0; }
        
        @Override
        public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {}
        
        @Override
        public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {}
        
        @Override
        public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException { return 0; }
        
        @Override
        public void incrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {}
        
        @Override
        public void decrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {}
        
        @Override
        public void setStatistic(Statistic statistic, Material material, int newValue) throws IllegalArgumentException {}
        
        @Override
        public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {}
        
        @Override
        public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {}
        
        @Override
        public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException { return 0; }
        
        @Override
        public void incrementStatistic(Statistic statistic, EntityType entityType, int amount) throws IllegalArgumentException {}
        
        @Override
        public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) throws IllegalArgumentException {}
        
        @Override
        public void setStatistic(Statistic statistic, EntityType entityType, int newValue) throws IllegalArgumentException {}
        
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();
            result.put("UUID", uniqueId.toString());
            result.put("name", name);
            return result;
        }
        
        @Override
        public boolean isOp() { return false; }
        
        @Override
        public void setOp(boolean value) {}

        @Override
        public boolean isConnected() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isConnected'");
        }

        @Override
        public PlayerProfile getPlayerProfile() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPlayerProfile'");
        }

        @Override
        public <E extends BanEntry<? super PlayerProfile>> @org.jspecify.annotations.Nullable E ban(
                @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Date expires,
                @org.jspecify.annotations.Nullable String source) {
            
            throw new UnsupportedOperationException("Unimplemented method 'ban'");
        }

        @Override
        public <E extends BanEntry<? super PlayerProfile>> @org.jspecify.annotations.Nullable E ban(
                @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Instant expires,
                @org.jspecify.annotations.Nullable String source) {
            
            throw new UnsupportedOperationException("Unimplemented method 'ban'");
        }

        @Override
        public <E extends BanEntry<? super PlayerProfile>> @org.jspecify.annotations.Nullable E ban(
                @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Duration duration,
                @org.jspecify.annotations.Nullable String source) {
            
            throw new UnsupportedOperationException("Unimplemented method 'ban'");
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
        public @org.jspecify.annotations.Nullable Location getRespawnLocation(boolean loadLocationAndValidate) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getRespawnLocation'");
        }

        @Override
        public @org.jspecify.annotations.Nullable Location getLastDeathLocation() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLastDeathLocation'");
        }

        @Override
        public @org.jspecify.annotations.Nullable Location getLocation() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLocation'");
        }

        @Override
        public PersistentDataContainerView getPersistentDataContainer() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPersistentDataContainer'");
        }
    }
}