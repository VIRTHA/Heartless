package com.darkbladedev.testing.mocks;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.*;
import org.bukkit.loot.LootTable;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BiomeSearchResult;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.StructureSearchResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.block.TargetBlockInfo.FluidMode;
import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import com.destroystokyo.paper.entity.villager.Reputation;

import io.papermc.paper.block.fluid.FluidData;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.math.Position;
import io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.world.MoonPhase;
import io.papermc.paper.world.damagesource.CombatTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.darkbladedev.testing.mocks.MockSupport.MockItem;
import static com.darkbladedev.testing.mocks.MockSupport.MockArrow;
import static com.darkbladedev.testing.mocks.MockSupport.MockOfflinePlayer;
import static com.darkbladedev.testing.mocks.MockSupport.MockBlock;
import static com.darkbladedev.testing.mocks.MockSupport.MockChunk;

/**
 * Colección de clases mock para entidades y objetos de Bukkit necesarios para las pruebas.
 */
public class MockEntities {
    
    /**
     * Mock de World para pruebas.
     */
    static class MockWorld implements World {
        private final String name;
        private final UUID uniqueId;
        private final Environment environment;
        @SuppressWarnings("unused")
        private final Map<String, Object> metadata;
        
        public MockWorld(String name) {
            this.name = name;
            this.uniqueId = UUID.randomUUID();
            this.environment = Environment.NORMAL;
            this.metadata = new HashMap<>();
        }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public UUID getUID() { return uniqueId; }
        
        @Override
        public Environment getEnvironment() { return environment; }
        
        @Override
        public Location getSpawnLocation() {
            return new Location(this, 0, 64, 0);
        }
        
        @Override
        public boolean setSpawnLocation(Location location) { return true; }
        
        @Override
        public boolean setSpawnLocation(int x, int y, int z) { return true; }
        
        @Override
        public List<Entity> getEntities() { return new ArrayList<>(); }
        
        @Override
        public List<LivingEntity> getLivingEntities() { return new ArrayList<>(); }
        
        @Override
        public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> cls) {
            return new ArrayList<>();
        }
        
        @Override
        public <T extends Entity> Collection<T> getEntitiesByClass(@SuppressWarnings("unchecked") Class<T>... classes) {
            return new ArrayList<>();
        }
        
        @Override
        public Collection<Entity> getEntitiesByClasses(Class<?>... classes) {
            return new ArrayList<>();
        }
        
        @Override
        public List<Player> getPlayers() { return new ArrayList<>(); }
        
        @Override
        public Block getBlockAt(int x, int y, int z) {
            return new MockBlock(this, x, y, z);
        }
        
        @Override
        public Block getBlockAt(Location location) {
            return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }
        
        @Override
        public Material getType(int x, int y, int z) {
            return Material.AIR;
        }
        
        @Override
        public BlockData getBlockData(int x, int y, int z) {
            return Material.AIR.createBlockData();
        }
        
        @Override
        public int getHighestBlockYAt(int x, int z) { return 64; }
        
        @Override
        public int getHighestBlockYAt(Location location) { return 64; }
        
        @Override
        public Block getHighestBlockAt(int x, int z) {
            return getBlockAt(x, getHighestBlockYAt(x, z), z);
        }
        
        @Override
        public Block getHighestBlockAt(Location location) {
            return getHighestBlockAt(location.getBlockX(), location.getBlockZ());
        }
        
        @Override
        public Chunk getChunkAt(int x, int z) {
            return new MockChunk(this, x, z);
        }
        
        @Override
        public Chunk getChunkAt(Location location) {
            return getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        }
        
        @Override
        public Chunk getChunkAt(Block block) {
            return getChunkAt(block.getLocation());
        }
        
        @Override
        public boolean isChunkLoaded(Chunk chunk) { return true; }
        
        @Override
        public boolean isChunkLoaded(int x, int z) { return true; }
        
        @Override
        public boolean isChunkInUse(int x, int z) { return false; }
        
        @Override
        public void loadChunk(Chunk chunk) {}
        
        @Override
        public void loadChunk(int x, int z) {}
        
        @Override
        public boolean loadChunk(int x, int z, boolean generate) { return true; }
        
        @Override
        public boolean unloadChunk(Chunk chunk) { return true; }
        
        @Override
        public boolean unloadChunk(int x, int z) { return true; }
        
        @Override
        public boolean unloadChunk(int x, int z, boolean save) { return true; }
        
        @Override
        public boolean unloadChunkRequest(int x, int z) { return true; }
        
        @Override
        public boolean regenerateChunk(int x, int z) { return true; }
        
        @Override
        public boolean refreshChunk(int x, int z) { return true; }
        
        @Override
        public boolean isChunkGenerated(int x, int z) { return true; }
        
        @Override
        public Item dropItem(Location location, ItemStack item) {
            return new MockItem(location, item);
        }
        
        @Override
        public Item dropItemNaturally(Location location, ItemStack item) {
            return dropItem(location, item);
        }
        
        @Override
        public Arrow spawnArrow(Location location, Vector direction, float speed, float spread) {
            return new MockArrow(location);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <T extends AbstractArrow> T spawnArrow(Location location, Vector direction, float speed, float spread, Class<T> clazz) {
            return (T) spawnArrow(location, direction, speed, spread);
        }
        
        @Override
        public boolean generateTree(Location location, TreeType type) { return true; }
        
        @Override
        public boolean generateTree(Location loc, TreeType type, BlockChangeDelegate delegate) { return true; }
        
        @Override
        public Entity spawnEntity(Location loc, EntityType type) {
            switch (type) {
                case PLAYER:
                    return new MockPlayer("TestPlayer");
                case VILLAGER:
                    return new MockVillager(loc);
                case ZOMBIE_VILLAGER:
                    return new MockZombieVillager(loc);
                case ZOMBIE:
                    return new MockZombie(loc);
                default:
                    return new MockEntity(loc, type);
            }
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <T extends Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException {
            if (clazz == Player.class) {
                return (T) new MockPlayer("TestPlayer");
            } else if (clazz == Villager.class) {
                return (T) new MockVillager(location);
            } else if (clazz == ZombieVillager.class) {
                return (T) new MockZombieVillager(location);
            } else if (clazz == Zombie.class) {
                return (T) new MockZombie(location);
            }
            throw new IllegalArgumentException("Unsupported entity class: " + clazz.getName());
        }
        
        @SuppressWarnings({ "deprecation", "removal" })
        public <T extends Entity> T spawn(Location location, Class<T> clazz, org.bukkit.util.Consumer<T> function) throws IllegalArgumentException {
            T entity = spawn(location, clazz);
            if (function != null) {
                function.accept(entity);
            }
            return entity;
        }
        
        @Override
        public LightningStrike strikeLightning(Location loc) {
            return new MockSupport.MockLightningStrike(loc);
        }
        
        @Override
        public LightningStrike strikeLightningEffect(Location loc) {
            return strikeLightning(loc);
        }
        
        @Override
        public long getTime() { return 6000; } // Mediodía
        
        @Override
        public void setTime(long time) {}
        
        @Override
        public long getFullTime() { return getTime(); }
        
        @Override
        public void setFullTime(long time) {}
        
        @Override
        public boolean hasStorm() { return false; }
        
        @Override
        public void setStorm(boolean hasStorm) {}
        
        @Override
        public int getWeatherDuration() { return 0; }
        
        @Override
        public void setWeatherDuration(int duration) {}
        
        @Override
        public boolean isThundering() { return false; }
        
        @Override
        public void setThundering(boolean thundering) {}
        
        @Override
        public int getThunderDuration() { return 0; }
        
        @Override
        public void setThunderDuration(int duration) {}
        
        @Override
        public boolean createExplosion(double x, double y, double z, float power) { return true; }
        
        @Override
        public boolean createExplosion(double x, double y, double z, float power, boolean setFire) { return true; }
        
        @Override
        public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks) { return true; }
        
        @Override
        public boolean createExplosion(Location loc, float power) { return true; }
        
        @Override
        public boolean createExplosion(Location loc, float power, boolean setFire) { return true; }
        
        @Override
        public boolean createExplosion(Location loc, float power, boolean setFire, boolean breakBlocks) { return true; }
        
        @Override
        public Difficulty getDifficulty() { return Difficulty.NORMAL; }
        
        @Override
        public void setDifficulty(Difficulty difficulty) {}
        
        @Override
        public File getWorldFolder() { return new File("test-world"); }
        
        @Override
        public WorldType getWorldType() { return WorldType.NORMAL; }
        
        @Override
        public boolean canGenerateStructures() { return true; }
        
        @Override
        public long getTicksPerAnimalSpawns() { return 400; }
        
        @Override
        public void setTicksPerAnimalSpawns(int ticksPerAnimalSpawns) {}
        
        @Override
        public long getTicksPerMonsterSpawns() { return 1; }
        
        @Override
        public void setTicksPerMonsterSpawns(int ticksPerMonsterSpawns) {}
        
        @Override
        public int getMonsterSpawnLimit() { return 70; }
        
        @Override
        public void setMonsterSpawnLimit(int limit) {}
        
        @Override
        public int getAnimalSpawnLimit() { return 15; }
        
        @Override
        public void setAnimalSpawnLimit(int limit) {}
        
        @Override
        public int getWaterAnimalSpawnLimit() { return 5; }
        
        @Override
        public void setWaterAnimalSpawnLimit(int limit) {}
        
        @Override
        public int getAmbientSpawnLimit() { return 15; }
        
        @Override
        public void setAmbientSpawnLimit(int limit) {}
        
        @Override
        public void playSound(Location location, Sound sound, float volume, float pitch) {}
        
        @Override
        public void playSound(Location location, String sound, float volume, float pitch) {}
        
        @Override
        public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch) {}
        
        @Override
        public void playSound(Location location, String sound, SoundCategory category, float volume, float pitch) {}
        
        @Override
        public String[] getGameRules() { return new String[0]; }
        
        @Override
        public String getGameRuleValue(String rule) { return null; }
        
        @Override
        public boolean setGameRuleValue(String rule, String value) { return false; }
        
        @Override
        public boolean isGameRule(String rule) { return false; }
        
        @Override
        public <T> T getGameRuleValue(GameRule<T> rule) { return null; }
        
        @Override
        public <T> T getGameRuleDefault(GameRule<T> rule) { return null; }
        
        @Override
        public <T> boolean setGameRule(GameRule<T> rule, T newValue) { return false; }
        
        @Override
        public WorldBorder getWorldBorder() { return new MockSupport.MockWorldBorder(); }
        
        @Override
        public void spawnParticle(Particle particle, Location location, int count) {}
        
        @Override
        public void spawnParticle(Particle particle, double x, double y, double z, int count) {}
        
        @Override
        public <T> void spawnParticle(Particle particle, Location location, int count, T data) {}
        
        @Override
        public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {}
        
        @Override
        public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ) {}
        
        @Override
        public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {}
        
        @Override
        public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {}
        
        @Override
        public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {}
        
        @Override
        public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {}
        
        @Override
        public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {}
        
        @Override
        public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {}
        
        @Override
        public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {}
        
        @Override
        public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {}
        
        @Override
        public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {}
        
        @Override
        public Location locateNearestStructure(Location origin, @SuppressWarnings("deprecation") StructureType structureType, int radius, boolean findUnexplored) {
            return null;
        }
        
        @Override
        public Chunk[] getLoadedChunks() { return new Chunk[0]; }
        
        @Override
        public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {}
        
        @Override
        public List<MetadataValue> getMetadata(String metadataKey) { return new ArrayList<>(); }
        
        @Override
        public boolean hasMetadata(String metadataKey) { return false; }
        
        @Override
        public void removeMetadata(String metadataKey, Plugin owningPlugin) {}
        
        @Override
        public void sendPluginMessage(Plugin source, String channel, byte[] message) {}
        
        @Override
        public Set<String> getListeningPluginChannels() { return new HashSet<>(); }
        
        // Métodos adicionales que pueden ser necesarios
        @Override
        public ChunkGenerator getGenerator() { return null; }
        
        @Override
        public void save() {}
        
        @Override
        public boolean isAutoSave() { return true; }
        
        @Override
        public void setAutoSave(boolean value) {}
        
        @Override
        public long getSeed() { return 0; }
        
        @Override
        public boolean getPVP() { return true; }
        
        @Override
        public void setPVP(boolean pvp) {}
        
        @Override
        public boolean getKeepSpawnInMemory() { return true; }
        
        @Override
        public void setKeepSpawnInMemory(boolean keepLoaded) {}
        
        @Override
        public Collection<org.bukkit.generator.structure.GeneratedStructure> getStructures(int x, int z, org.bukkit.generator.structure.Structure structure) {
            return new ArrayList<>();
        }
        
        @Override
        public Collection<org.bukkit.generator.structure.GeneratedStructure> getStructures(int x, int z) {
            return new ArrayList<>();
        }
        
        @Override
        public void setSendViewDistance(int viewDistance) {}
        
        @Override
        public int getSendViewDistance() { return 10; }
        
        @Override
        public void setSimulationDistance(int simulationDistance) {}

        @Override
        public @NotNull Biome getBiome(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getBiome'");
        }

        @Override
        public @NotNull Biome getBiome(int x, int y, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getBiome'");
        }

        @Override
        public @NotNull Biome getComputedBiome(int x, int y, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getComputedBiome'");
        }

        @Override
        public void setBiome(@NotNull Location location, @NotNull Biome biome) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBiome'");
        }

        @Override
        public void setBiome(int x, int y, int z, @NotNull Biome biome) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBiome'");
        }

        @Override
        public @NotNull BlockState getBlockState(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getBlockState'");
        }

        @Override
        public @NotNull BlockState getBlockState(int x, int y, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getBlockState'");
        }

        @Override
        public @NotNull FluidData getFluidData(int x, int y, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getFluidData'");
        }

        @Override
        public @NotNull BlockData getBlockData(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getBlockData'");
        }

        @Override
        public @NotNull Material getType(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getType'");
        }

        @Override
        public void setBlockData(@NotNull Location location, @NotNull BlockData blockData) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBlockData'");
        }

        @Override
        public void setBlockData(int x, int y, int z, @NotNull BlockData blockData) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBlockData'");
        }

        @Override
        public void setType(@NotNull Location location, @NotNull Material material) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setType'");
        }

        @Override
        public void setType(int x, int y, int z, @NotNull Material material) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setType'");
        }

        @Override
        public boolean generateTree(@NotNull Location location, @NotNull Random random, @NotNull TreeType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'generateTree'");
        }

        @Override
        public boolean generateTree(@NotNull Location location, @NotNull Random random, @NotNull TreeType type,
                @Nullable Consumer<? super BlockState> stateConsumer) {
            
            throw new UnsupportedOperationException("Unimplemented method 'generateTree'");
        }

        @Override
        public boolean generateTree(@NotNull Location location, @NotNull Random random, @NotNull TreeType type,
                @Nullable Predicate<? super BlockState> statePredicate) {
            
            throw new UnsupportedOperationException("Unimplemented method 'generateTree'");
        }

        @Override
        public @NotNull Entity spawnEntity(@NotNull Location loc, @NotNull EntityType type, boolean randomizeData) {
            
            throw new UnsupportedOperationException("Unimplemented method 'spawnEntity'");
        }

        @Override
        public <T extends Entity> @NotNull T createEntity(@NotNull Location location, @NotNull Class<T> clazz) {
            
            throw new UnsupportedOperationException("Unimplemented method 'createEntity'");
        }

        @Override
        public <T extends Entity> @NotNull T spawn(@NotNull Location location, @NotNull Class<T> clazz,
                @Nullable Consumer<? super T> function, @NotNull SpawnReason reason) throws IllegalArgumentException {
            
            throw new UnsupportedOperationException("Unimplemented method 'spawn'");
        }

        @Override
        public <T extends Entity> @NotNull T spawn(@NotNull Location location, @NotNull Class<T> clazz,
                boolean randomizeData, @Nullable Consumer<? super T> function) throws IllegalArgumentException {
            
            throw new UnsupportedOperationException("Unimplemented method 'spawn'");
        }

        @Override
        public int getHighestBlockYAt(int x, int z, @NotNull HeightMap heightMap) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHighestBlockYAt'");
        }

        @Override
        public int getHighestBlockYAt(@NotNull Location location, @NotNull HeightMap heightMap) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHighestBlockYAt'");
        }

        @Override
        public <T extends Entity> @NotNull T addEntity(@NotNull T entity) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addEntity'");
        }

        @Override
        public @NotNull MoonPhase getMoonPhase() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMoonPhase'");
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getKey'");
        }

        @Override
        public boolean lineOfSightExists(@NotNull Location from, @NotNull Location to) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lineOfSightExists'");
        }

        @Override
        public boolean hasCollisionsIn(@NotNull BoundingBox boundingBox) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasCollisionsIn'");
        }

        @Override
        public int getMinHeight() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMinHeight'");
        }

        @Override
        public int getMaxHeight() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMaxHeight'");
        }

        @Override
        public @NotNull BiomeProvider vanillaBiomeProvider() {
            
            throw new UnsupportedOperationException("Unimplemented method 'vanillaBiomeProvider'");
        }

        @Override
        public @NotNull PersistentDataContainer getPersistentDataContainer() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPersistentDataContainer'");
        }

        @Override
        public boolean isVoidDamageEnabled() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isVoidDamageEnabled'");
        }

        @Override
        public void setVoidDamageEnabled(boolean enabled) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setVoidDamageEnabled'");
        }

        @Override
        public float getVoidDamageAmount() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getVoidDamageAmount'");
        }

        @Override
        public void setVoidDamageAmount(float voidDamageAmount) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setVoidDamageAmount'");
        }

        @Override
        public double getVoidDamageMinBuildHeightOffset() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getVoidDamageMinBuildHeightOffset'");
        }

        @Override
        public void setVoidDamageMinBuildHeightOffset(double minBuildHeightOffset) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setVoidDamageMinBuildHeightOffset'");
        }

        @Override
        public int getEntityCount() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEntityCount'");
        }

        @Override
        public int getTileEntityCount() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTileEntityCount'");
        }

        @Override
        public int getTickableTileEntityCount() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTickableTileEntityCount'");
        }

        @Override
        public int getChunkCount() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getChunkCount'");
        }

        @Override
        public int getPlayerCount() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPlayerCount'");
        }

        @Override
        public boolean hasStructureAt(@NotNull Position position, @NotNull Structure structure) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasStructureAt'");
        }

        @Override
        public @NotNull Block getHighestBlockAt(int x, int z, @NotNull HeightMap heightMap) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHighestBlockAt'");
        }

        @Override
        public @NotNull Block getHighestBlockAt(@NotNull Location location, @NotNull HeightMap heightMap) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHighestBlockAt'");
        }

        @Override
        public @NotNull Chunk getChunkAt(int x, int z, boolean generate) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getChunkAt'");
        }

        @Override
        public @NotNull Collection<Player> getPlayersSeeingChunk(@NotNull Chunk chunk) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPlayersSeeingChunk'");
        }

        @Override
        public @NotNull Collection<Player> getPlayersSeeingChunk(int x, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPlayersSeeingChunk'");
        }

        @Override
        public boolean isChunkForceLoaded(int x, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'isChunkForceLoaded'");
        }

        @Override
        public void setChunkForceLoaded(int x, int z, boolean forced) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setChunkForceLoaded'");
        }

        @Override
        public @NotNull Collection<Chunk> getForceLoadedChunks() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getForceLoadedChunks'");
        }

        @Override
        public boolean addPluginChunkTicket(int x, int z, @NotNull Plugin plugin) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addPluginChunkTicket'");
        }

        @Override
        public boolean removePluginChunkTicket(int x, int z, @NotNull Plugin plugin) {
            
            throw new UnsupportedOperationException("Unimplemented method 'removePluginChunkTicket'");
        }

        @Override
        public void removePluginChunkTickets(@NotNull Plugin plugin) {
            
            throw new UnsupportedOperationException("Unimplemented method 'removePluginChunkTickets'");
        }

        @Override
        public @NotNull Collection<Plugin> getPluginChunkTickets(int x, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPluginChunkTickets'");
        }

        @Override
        public @NotNull Map<Plugin, Collection<Chunk>> getPluginChunkTickets() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPluginChunkTickets'");
        }

        @Override
        public @NotNull Collection<Chunk> getIntersectingChunks(@NotNull BoundingBox box) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getIntersectingChunks'");
        }

        @Override
        public @NotNull Item dropItem(@NotNull Location location, @NotNull ItemStack item,
                @Nullable Consumer<? super Item> function) {
            
            throw new UnsupportedOperationException("Unimplemented method 'dropItem'");
        }

        @Override
        public @NotNull Item dropItemNaturally(@NotNull Location location, @NotNull ItemStack item,
                @Nullable Consumer<? super Item> function) {
            
            throw new UnsupportedOperationException("Unimplemented method 'dropItemNaturally'");
        }

        @Override
        public @Nullable Location findLightningRod(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'findLightningRod'");
        }

        @Override
        public @Nullable Location findLightningTarget(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'findLightningTarget'");
        }

        @Override
        public void getChunkAtAsync(int x, int z, boolean gen, boolean urgent, @NotNull Consumer<? super Chunk> cb) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getChunkAtAsync'");
        }

        @Override
        public void getChunksAtAsync(int minX, int minZ, int maxX, int maxZ, boolean urgent, @NotNull Runnable cb) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getChunksAtAsync'");
        }

        @Override
        public @NotNull Collection<Entity> getNearbyEntities(@NotNull Location location, double x, double y, double z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getNearbyEntities'");
        }

        @Override
        public @Nullable Entity getEntity(@NotNull UUID uuid) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEntity'");
        }

        @Override
        public @NotNull Collection<Entity> getNearbyEntities(@NotNull Location location, double x, double y, double z,
                @Nullable Predicate<? super Entity> filter) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getNearbyEntities'");
        }

        @Override
        public @NotNull Collection<Entity> getNearbyEntities(@NotNull BoundingBox boundingBox) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getNearbyEntities'");
        }

        @Override
        public @NotNull Collection<Entity> getNearbyEntities(@NotNull BoundingBox boundingBox,
                @Nullable Predicate<? super Entity> filter) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getNearbyEntities'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceEntities(@NotNull Location start, @NotNull Vector direction,
                double maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceEntities(@NotNull Location start, @NotNull Vector direction,
                double maxDistance, double raySize) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceEntities(@NotNull Location start, @NotNull Vector direction,
                double maxDistance, @Nullable Predicate<? super Entity> filter) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceEntities(@NotNull Location start, @NotNull Vector direction,
                double maxDistance, double raySize, @Nullable Predicate<? super Entity> filter) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceEntities(@NotNull Position start, @NotNull Vector direction,
                double maxDistance, double raySize, @Nullable Predicate<? super Entity> filter) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceBlocks(@NotNull Location start, @NotNull Vector direction,
                double maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceBlocks'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceBlocks(@NotNull Location start, @NotNull Vector direction,
                double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceBlocks'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceBlocks(@NotNull Location start, @NotNull Vector direction,
                double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceBlocks'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceBlocks(@NotNull Position start, @NotNull Vector direction,
                double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks,
                @Nullable Predicate<? super Block> canCollide) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceBlocks'");
        }

        @Override
        public @Nullable RayTraceResult rayTrace(@NotNull Location start, @NotNull Vector direction, double maxDistance,
                @NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize,
                @Nullable Predicate<? super Entity> filter) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTrace'");
        }

        @Override
        public @Nullable RayTraceResult rayTrace(@NotNull Position start, @NotNull Vector direction, double maxDistance,
                @NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize,
                @Nullable Predicate<? super Entity> filter, @Nullable Predicate<? super Block> canCollide) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTrace'");
        }

        @Override
        public @Nullable RayTraceResult rayTrace(
                @NotNull Consumer<PositionedRayTraceConfigurationBuilder> builderConsumer) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTrace'");
        }

        @Override
        public boolean setSpawnLocation(int x, int y, int z, float angle) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setSpawnLocation'");
        }

        @Override
        public boolean isDayTime() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isDayTime'");
        }

        @Override
        public long getGameTime() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getGameTime'");
        }

        @Override
        public boolean isClearWeather() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isClearWeather'");
        }

        @Override
        public void setClearWeatherDuration(int duration) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setClearWeatherDuration'");
        }

        @Override
        public int getClearWeatherDuration() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getClearWeatherDuration'");
        }

        @Override
        public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks,
                @Nullable Entity source) {
            
            throw new UnsupportedOperationException("Unimplemented method 'createExplosion'");
        }

        @Override
        public boolean createExplosion(@Nullable Entity source, @NotNull Location loc, float power, boolean setFire,
                boolean breakBlocks, boolean excludeSourceFromDamage) {
            
            throw new UnsupportedOperationException("Unimplemented method 'createExplosion'");
        }

        @Override
        public boolean createExplosion(@NotNull Location loc, float power, boolean setFire, boolean breakBlocks,
                @Nullable Entity source) {
            
            throw new UnsupportedOperationException("Unimplemented method 'createExplosion'");
        }

        @Override
        public @Nullable BiomeProvider getBiomeProvider() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getBiomeProvider'");
        }

        @Override
        public void save(boolean flush) {
            
            throw new UnsupportedOperationException("Unimplemented method 'save'");
        }

        @Override
        public @NotNull List<BlockPopulator> getPopulators() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPopulators'");
        }

        @Override
        public <T extends LivingEntity> @NotNull T spawn(@NotNull Location location, @NotNull Class<T> clazz,
                @NotNull SpawnReason spawnReason, boolean randomizeData, @Nullable Consumer<? super T> function)
                throws IllegalArgumentException {
            
            throw new UnsupportedOperationException("Unimplemented method 'spawn'");
        }

        @Override
        public @NotNull FallingBlock spawnFallingBlock(@NotNull Location location, @SuppressWarnings("removal") @NotNull MaterialData data)
                throws IllegalArgumentException {
            
            throw new UnsupportedOperationException("Unimplemented method 'spawnFallingBlock'");
        }

        @Override
        public @NotNull FallingBlock spawnFallingBlock(@NotNull Location location, @NotNull BlockData data)
                throws IllegalArgumentException {
            
            throw new UnsupportedOperationException("Unimplemented method 'spawnFallingBlock'");
        }

        @Override
        public @NotNull FallingBlock spawnFallingBlock(@NotNull Location location, @NotNull Material material,
                byte data) throws IllegalArgumentException {
            
            throw new UnsupportedOperationException("Unimplemented method 'spawnFallingBlock'");
        }

        @Override
        public void playEffect(@NotNull Location location, @NotNull Effect effect, int data) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playEffect'");
        }

        @Override
        public void playEffect(@NotNull Location location, @NotNull Effect effect, int data, int radius) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playEffect'");
        }

        @Override
        public <T> void playEffect(@NotNull Location location, @NotNull Effect effect, @Nullable T data) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playEffect'");
        }

        @Override
        public <T> void playEffect(@NotNull Location location, @NotNull Effect effect, @Nullable T data, int radius) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playEffect'");
        }

        @Override
        public @NotNull ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome,
                boolean includeBiomeTemp) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEmptyChunkSnapshot'");
        }

        @Override
        public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setSpawnFlags'");
        }

        @Override
        public boolean getAllowAnimals() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAllowAnimals'");
        }

        @Override
        public boolean getAllowMonsters() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAllowMonsters'");
        }

        @Override
        public @NotNull Biome getBiome(int x, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getBiome'");
        }

        @Override
        public void setBiome(int x, int z, @NotNull Biome bio) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBiome'");
        }

        @Override
        public double getTemperature(int x, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTemperature'");
        }

        @Override
        public double getTemperature(int x, int y, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTemperature'");
        }

        @Override
        public double getHumidity(int x, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHumidity'");
        }

        @Override
        public double getHumidity(int x, int y, int z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHumidity'");
        }

        @Override
        public int getLogicalHeight() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLogicalHeight'");
        }

        @Override
        public boolean isNatural() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isNatural'");
        }

        @Override
        public boolean isBedWorks() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isBedWorks'");
        }

        @Override
        public boolean hasSkyLight() {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasSkyLight'");
        }

        @Override
        public boolean hasCeiling() {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasCeiling'");
        }

        @Override
        public boolean isPiglinSafe() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isPiglinSafe'");
        }

        @Override
        public boolean isRespawnAnchorWorks() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isRespawnAnchorWorks'");
        }

        @Override
        public boolean hasRaids() {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasRaids'");
        }

        @Override
        public boolean isUltraWarm() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isUltraWarm'");
        }

        @Override
        public int getSeaLevel() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getSeaLevel'");
        }

        @Override
        public int getViewDistance() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getViewDistance'");
        }

        @Override
        public int getSimulationDistance() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getSimulationDistance'");
        }

        @Override
        public boolean hasBonusChest() {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasBonusChest'");
        }

        @Override
        public boolean isHardcore() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isHardcore'");
        }

        @Override
        public void setHardcore(boolean hardcore) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHardcore'");
        }

        @Override
        public long getTicksPerWaterSpawns() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTicksPerWaterSpawns'");
        }

        @Override
        public void setTicksPerWaterSpawns(int ticksPerWaterSpawns) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setTicksPerWaterSpawns'");
        }

        @Override
        public long getTicksPerWaterAmbientSpawns() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTicksPerWaterAmbientSpawns'");
        }

        @Override
        public void setTicksPerWaterAmbientSpawns(int ticksPerAmbientSpawns) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setTicksPerWaterAmbientSpawns'");
        }

        @Override
        public long getTicksPerWaterUndergroundCreatureSpawns() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTicksPerWaterUndergroundCreatureSpawns'");
        }

        @Override
        public void setTicksPerWaterUndergroundCreatureSpawns(int ticksPerWaterUndergroundCreatureSpawns) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setTicksPerWaterUndergroundCreatureSpawns'");
        }

        @Override
        public long getTicksPerAmbientSpawns() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTicksPerAmbientSpawns'");
        }

        @Override
        public void setTicksPerAmbientSpawns(int ticksPerAmbientSpawns) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setTicksPerAmbientSpawns'");
        }

        @Override
        public long getTicksPerSpawns(@NotNull SpawnCategory spawnCategory) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTicksPerSpawns'");
        }

        @Override
        public void setTicksPerSpawns(@NotNull SpawnCategory spawnCategory, int ticksPerCategorySpawn) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setTicksPerSpawns'");
        }

        @Override
        public int getWaterUndergroundCreatureSpawnLimit() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getWaterUndergroundCreatureSpawnLimit'");
        }

        @Override
        public void setWaterUndergroundCreatureSpawnLimit(int limit) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setWaterUndergroundCreatureSpawnLimit'");
        }

        @Override
        public int getWaterAmbientSpawnLimit() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getWaterAmbientSpawnLimit'");
        }

        @Override
        public void setWaterAmbientSpawnLimit(int limit) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setWaterAmbientSpawnLimit'");
        }

        @Override
        public int getSpawnLimit(@NotNull SpawnCategory spawnCategory) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getSpawnLimit'");
        }

        @Override
        public void setSpawnLimit(@NotNull SpawnCategory spawnCategory, int limit) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setSpawnLimit'");
        }

        @Override
        public void playNote(@NotNull Location loc, @NotNull Instrument instrument, @NotNull Note note) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playNote'");
        }

        @Override
        public void playSound(@NotNull Location location, @NotNull Sound sound, @NotNull SoundCategory category,
                float volume, float pitch, long seed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void playSound(@NotNull Location location, @NotNull String sound, @NotNull SoundCategory category,
                float volume, float pitch, long seed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void playSound(@NotNull Entity entity, @NotNull Sound sound, float volume, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void playSound(@NotNull Entity entity, @NotNull String sound, float volume, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void playSound(@NotNull Entity entity, @NotNull Sound sound, @NotNull SoundCategory category,
                float volume, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void playSound(@NotNull Entity entity, @NotNull String sound, @NotNull SoundCategory category,
                float volume, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void playSound(@NotNull Entity entity, @NotNull Sound sound, @NotNull SoundCategory category,
                float volume, float pitch, long seed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void playSound(@NotNull Entity entity, @NotNull String sound, @NotNull SoundCategory category,
                float volume, float pitch, long seed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public <T> void spawnParticle(@NotNull Particle particle, @Nullable List<Player> receivers,
                @Nullable Player source, double x, double y, double z, int count, double offsetX, double offsetY,
                double offsetZ, double extra, @Nullable T data, boolean force) {
            
            throw new UnsupportedOperationException("Unimplemented method 'spawnParticle'");
        }

        @Override
        public @Nullable StructureSearchResult locateNearestStructure(@NotNull Location origin,
                org.bukkit.generator.structure.@NotNull StructureType structureType, int radius,
                boolean findUnexplored) {
            
            throw new UnsupportedOperationException("Unimplemented method 'locateNearestStructure'");
        }

        @Override
        public @Nullable StructureSearchResult locateNearestStructure(@NotNull Location origin,
                @NotNull Structure structure, int radius, boolean findUnexplored) {
            
            throw new UnsupportedOperationException("Unimplemented method 'locateNearestStructure'");
        }

        @Override
        public double getCoordinateScale() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getCoordinateScale'");
        }

        @Override
        public boolean isFixedTime() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isFixedTime'");
        }

        @Override
        public @NotNull Collection<Material> getInfiniburn() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getInfiniburn'");
        }

        @Override
        public void sendGameEvent(@Nullable Entity sourceEntity, @NotNull GameEvent gameEvent,
                @NotNull Vector position) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendGameEvent'");
        }

        @SuppressWarnings("removal")
        @Override
        public @NotNull Spigot spigot() {
            
            throw new UnsupportedOperationException("Unimplemented method 'spigot'");
        }

        @Override
        public @Nullable BiomeSearchResult locateNearestBiome(@NotNull Location origin, int radius,
                @NotNull Biome... biomes) {
            
            throw new UnsupportedOperationException("Unimplemented method 'locateNearestBiome'");
        }

        @Override
        public @Nullable BiomeSearchResult locateNearestBiome(@NotNull Location origin, int radius,
                int horizontalInterval, int verticalInterval, @NotNull Biome... biomes) {
            
            throw new UnsupportedOperationException("Unimplemented method 'locateNearestBiome'");
        }

        @Override
        public @Nullable Raid locateNearestRaid(@NotNull Location location, int radius) {
            
            throw new UnsupportedOperationException("Unimplemented method 'locateNearestRaid'");
        }

        @Override
        public @Nullable Raid getRaid(int id) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getRaid'");
        }

        @Override
        public @NotNull List<Raid> getRaids() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getRaids'");
        }

        @Override
        public @Nullable DragonBattle getEnderDragonBattle() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEnderDragonBattle'");
        }

        @Override
        public @NotNull Set<FeatureFlag> getFeatureFlags() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getFeatureFlags'");
        }

        @Override
        public void setViewDistance(int viewDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setViewDistance'");
        }
    }
    
    /**
     * Mock básico de Entity.
     */
    static class MockEntity implements Entity {
        protected final UUID uniqueId;
        protected final EntityType type;
        protected Location location;
        protected boolean valid;
        
        public MockEntity(Location location, EntityType type) {
            this.uniqueId = UUID.randomUUID();
            this.type = type;
            this.location = location.clone();
            this.valid = true;
        }
        
        @Override
        public UUID getUniqueId() { return uniqueId; }
        
        @Override
        public EntityType getType() { return type; }
        
        @Override
        public Location getLocation() { return location.clone(); }
        
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
        public World getWorld() { return location.getWorld(); }
        
        @Override
        public boolean teleport(Location location) {
            this.location = location.clone();
            return true;
        }
        
        @Override
        public boolean teleport(Location location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
            return teleport(location);
        }
        
        @Override
        public boolean teleport(Entity destination) {
            return teleport(destination.getLocation());
        }
        
        @Override
        public boolean teleport(Entity destination, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
            return teleport(destination.getLocation(), cause);
        }
        
        @Override
        public List<Entity> getNearbyEntities(double x, double y, double z) {
            return new ArrayList<>();
        }
        
        @Override
        public int getEntityId() { return uniqueId.hashCode(); }
        
        @Override
        public int getFireTicks() { return 0; }
        
        @Override
        public void setFireTicks(int ticks) {}
        
        @Override
        public int getMaxFireTicks() { return 20; }
        
        @Override
        public void remove() { valid = false; }
        
        @Override
        public boolean isDead() { return !valid; }
        
        @Override
        public boolean isValid() { return valid; }
        
        @Override
        public Server getServer() { return null; }
        
        @Override
        public Entity getPassenger() { return null; }
        
        @Override
        public boolean setPassenger(Entity passenger) { return false; }
        
        @Override
        public List<Entity> getPassengers() { return new ArrayList<>(); }
        
        @Override
        public boolean addPassenger(Entity passenger) { return false; }
        
        @Override
        public boolean removePassenger(Entity passenger) { return false; }
        
        @Override
        public boolean isEmpty() { return true; }
        
        @Override
        public boolean eject() { return false; }
        
        @Override
        public float getFallDistance() { return 0; }
        
        @Override
        public void setFallDistance(float distance) {}
        
        @Override
        public void setLastDamageCause(org.bukkit.event.entity.EntityDamageEvent event) {}
        
        @Override
        public org.bukkit.event.entity.EntityDamageEvent getLastDamageCause() { return null; }
        
        @Override
        public Vector getVelocity() { return new Vector(0, 0, 0); }
        
        @Override
        public void setVelocity(Vector velocity) {}
        
        @Override
        public double getHeight() { return 1.8; }
        
        @Override
        public double getWidth() { return 0.6; }
        
        @Override
        public BoundingBox getBoundingBox() {
            return new BoundingBox(location.getX() - 0.3, location.getY(), location.getZ() - 0.3,
                                 location.getX() + 0.3, location.getY() + 1.8, location.getZ() + 0.3);
        }
        
        @Override
        public boolean isOnGround() { return true; }
        
        @Override
        public boolean isInWater() { return false; }
        
        @Override
        public void setRotation(float yaw, float pitch) {
            location.setYaw(yaw);
            location.setPitch(pitch);
        }
        
        @Override
        public boolean hasGravity() { return true; }
        
        @Override
        public void setGravity(boolean gravity) {}
        
        @Override
        public int getTicksLived() { return 100; }
        
        @Override
        public void setTicksLived(int value) {}
        
        @Override
        public void playEffect(EntityEffect type) {}
        
        @Override
        public boolean isInsideVehicle() { return false; }
        
        @Override
        public boolean leaveVehicle() { return false; }
        
        @Override
        public Entity getVehicle() { return null; }
        
        @Override
        public void setCustomNameVisible(boolean flag) {}
        
        @Override
        public boolean isCustomNameVisible() { return false; }
        
        @Override
        public void setGlowing(boolean flag) {}
        
        @Override
        public boolean isGlowing() { return false; }
        
        @Override
        public void setInvulnerable(boolean flag) {}
        
        @Override
        public boolean isInvulnerable() { return false; }
        
        @Override
        public boolean isSilent() { return false; }
        
        @Override
        public void setSilent(boolean flag) {}
        
        @Override
        public int getPortalCooldown() { return 0; }
        
        @Override
        public void setPortalCooldown(int cooldown) {}
        
        @Override
        public Set<String> getScoreboardTags() { return new HashSet<>(); }
        
        @Override
        public boolean addScoreboardTag(String tag) { return false; }
        
        @Override
        public boolean removeScoreboardTag(String tag) { return false; }
        
        @Override
        public org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() {
            return new MockSupport.MockPersistentDataContainer();
        }
        
        @Override
        public void sendMessage(String message) {}
        
        @Override
        public void sendMessage(String[] messages) {}
        
        @Override
        public void sendMessage(UUID sender, String message) {}
        
        @Override
        public void sendMessage(UUID sender, String[] messages) {}
        
        @Override
        public String getCustomName() { return null; }
        
        @Override
        public void setCustomName(String name) {}
        
        @Override
        public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {}
        
        @Override
        public List<MetadataValue> getMetadata(String metadataKey) { return new ArrayList<>(); }
        
        @Override
        public boolean hasMetadata(String metadataKey) { return false; }
        
        @Override
        public void removeMetadata(String metadataKey, Plugin owningPlugin) {}
        
        @Override
        public boolean isPermissionSet(String name) { return false; }
        
        @Override
        public boolean isPermissionSet(org.bukkit.permissions.Permission perm) { return false; }
        
        @Override
        public boolean hasPermission(String name) { return false; }
        
        @Override
        public boolean hasPermission(org.bukkit.permissions.Permission perm) { return false; }
        
        @Override
        public org.bukkit.permissions.PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) { return null; }
        
        @Override
        public org.bukkit.permissions.PermissionAttachment addAttachment(Plugin plugin) { return null; }
        
        @Override
        public org.bukkit.permissions.PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) { return null; }
        
        @Override
        public org.bukkit.permissions.PermissionAttachment addAttachment(Plugin plugin, int ticks) { return null; }
        
        @Override
        public void removeAttachment(org.bukkit.permissions.PermissionAttachment attachment) {}
        
        @Override
        public void recalculatePermissions() {}
        
        @Override
        public Set<org.bukkit.permissions.PermissionAttachmentInfo> getEffectivePermissions() { return new HashSet<>(); }
        
        @Override
        public boolean isOp() { return false; }
        
        @Override
        public void setOp(boolean value) {}
        
        public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) { return null; }
        
        public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) { return null; }
        
        @Override
        public void broadcastHurtAnimation(java.util.Collection<org.bukkit.entity.Player> players) {}
        
        @Override
        public String getScoreboardEntryName() { return getName(); }

        @Override
        public @NotNull String getName() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getName'");
        }

        @Override
        public @NotNull Component name() {
            
            throw new UnsupportedOperationException("Unimplemented method 'name'");
        }

        @Override
        public @Nullable Component customName() {
            
            throw new UnsupportedOperationException("Unimplemented method 'customName'");
        }

        @Override
        public void customName(@Nullable Component customName) {
            
            throw new UnsupportedOperationException("Unimplemented method 'customName'");
        }

        @Override
        public <T> @Nullable T getData(@NotNull Valued<T> type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getData'");
        }

        @Override
        public <T> @Nullable T getDataOrDefault(@NotNull Valued<? extends T> type, @Nullable T fallback) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getDataOrDefault'");
        }

        @Override
        public boolean hasData(@NotNull DataComponentType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasData'");
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
        public @NotNull ItemStack getPickItemStack() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPickItemStack'");
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
        public boolean isSneaking() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isSneaking'");
        }

        @Override
        public void setSneaking(boolean sneak) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setSneaking'");
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
        public @Nullable String getAsString() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAsString'");
        }

        @Override
        public @Nullable EntitySnapshot createSnapshot() {
            
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
        public @NotNull Spigot spigot() {
            
            throw new UnsupportedOperationException("Unimplemented method 'spigot'");
        }

        @Override
        public @NotNull Component teamDisplayName() {
            
            throw new UnsupportedOperationException("Unimplemented method 'teamDisplayName'");
        }

        @Override
        public @Nullable Location getOrigin() {
            
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
    }
    
    /**
     * Mock de Villager.
     */
    static class MockVillager extends MockEntity implements Villager {
        private Profession profession;
        private Type villagerType;
        private int villagerLevel;
        
        public MockVillager(Location location) {
            super(location, EntityType.VILLAGER);
            this.profession = Profession.FARMER;
            this.villagerType = Type.PLAINS;
            this.villagerLevel = 1;
        }
        
        @Override
        public Profession getProfession() { return profession; }
        
        @Override
        public void setProfession(Profession profession) { this.profession = profession; }
        
        @Override
        public Type getVillagerType() { return villagerType; }
        
        @Override
        public void setVillagerType(Type type) { this.villagerType = type; }
        
        @Override
        public int getVillagerLevel() { return villagerLevel; }
        
        @Override
        public void updateDemand() {}
        
        @Override
        public void clearReputations() {}
        
        @Override
        public void setReputations(java.util.Map<java.util.UUID, Reputation> reputations) {}
        
        @Override
        public java.util.Map<java.util.UUID, Reputation> getReputations() { return new java.util.HashMap<>(); }
        
        @Override
        public Reputation getReputation(java.util.UUID uuid) { return null; }
        
        @Override
        public void setRestocksToday(int restocksToday) {}
        
        @Override
        public void setVillagerLevel(int level) { this.villagerLevel = Math.max(1, Math.min(5, level)); }
        
        @Override
        public int getVillagerExperience() { return villagerLevel * 100; }
        
        @Override
        public void setVillagerExperience(int experience) {
            this.villagerLevel = Math.max(1, Math.min(5, experience / 100 + 1));
        }
        
        @Override
        public boolean sleep(Location location) { return true; }
        
        @Override
        public void wakeup() {}
        
        @Override
        public void shakeHead() {}
        
        @Override
        public ZombieVillager zombify() {
            MockZombieVillager zombie = new MockZombieVillager(getLocation());
            zombie.setVillagerProfession(getProfession());
            zombie.setVillagerType(getVillagerType());
            return zombie;
        }
        
        // Implementar métodos de LivingEntity
        @Override
        public double getHealth() { return 20.0; }
        
        @Override
        public void setHealth(double health) {}
        
        @Override
        public double getMaxHealth() { return 20.0; }
        
        @Override
        public void setMaxHealth(double health) {}
        
        @Override
        public void resetMaxHealth() {}
        
        @Override
        public void restock() {}

        @Override
        public @NotNull Inventory getInventory() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getInventory'");
        }

        @Override
        public void resetOffers() {
            
            throw new UnsupportedOperationException("Unimplemented method 'resetOffers'");
        }

        @Override
        public void setAgeLock(boolean lock) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAgeLock'");
        }

        @Override
        public boolean getAgeLock() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAgeLock'");
        }

        @Override
        public boolean canBreed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'canBreed'");
        }

        @Override
        public void setBreed(boolean breed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBreed'");
        }

        @Override
        public int getAge() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAge'");
        }

        @Override
        public void setAge(int age) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAge'");
        }

        @Override
        public void setBaby() {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBaby'");
        }

        @Override
        public void setAdult() {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAdult'");
        }

        @Override
        public boolean isAdult() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isAdult'");
        }

        @Override
        public @NotNull EntityEquipment getEquipment() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEquipment'");
        }

        @Override
        public @NotNull Pathfinder getPathfinder() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPathfinder'");
        }

        @Override
        public boolean isInDaylight() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isInDaylight'");
        }

        @Override
        public void lookAt(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public void lookAt(@NotNull Location location, float headRotationSpeed, float maxHeadPitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public void lookAt(@NotNull Entity entity) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public void lookAt(@NotNull Entity entity, float headRotationSpeed, float maxHeadPitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public void lookAt(double x, double y, double z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public void lookAt(double x, double y, double z, float headRotationSpeed, float maxHeadPitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public int getHeadRotationSpeed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHeadRotationSpeed'");
        }

        @Override
        public int getMaxHeadPitch() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMaxHeadPitch'");
        }

        @Override
        public void setTarget(@Nullable LivingEntity target) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setTarget'");
        }

        @Override
        public @Nullable LivingEntity getTarget() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTarget'");
        }

        @Override
        public void setAware(boolean aware) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAware'");
        }

        @Override
        public boolean isAware() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isAware'");
        }

        @Override
        public @Nullable Sound getAmbientSound() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAmbientSound'");
        }

        @Override
        public boolean isAggressive() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isAggressive'");
        }

        @Override
        public void setAggressive(boolean aggressive) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAggressive'");
        }

        @Override
        public boolean isLeftHanded() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isLeftHanded'");
        }

        @Override
        public void setLeftHanded(boolean leftHanded) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setLeftHanded'");
        }

        @Override
        public int getPossibleExperienceReward() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPossibleExperienceReward'");
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
        public @NotNull List<Block> getLineOfSight(@Nullable Set<Material> transparent, int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLineOfSight'");
        }

        @Override
        public @NotNull Block getTargetBlock(@Nullable Set<Material> transparent, int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlock'");
        }

        @Override
        public @Nullable Block getTargetBlock(int maxDistance, @SuppressWarnings("removal") @NotNull FluidMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlock'");
        }

        @Override
        public @Nullable BlockFace getTargetBlockFace(int maxDistance, @SuppressWarnings("removal") @NotNull FluidMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockFace'");
        }

        @Override
        public @Nullable BlockFace getTargetBlockFace(int maxDistance, @NotNull FluidCollisionMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockFace'");
        }

        @SuppressWarnings("removal")
        @Override
        public @Nullable TargetBlockInfo getTargetBlockInfo(int maxDistance, @NotNull FluidMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockInfo'");
        }

        @Override
        public @Nullable Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetEntity'");
        }

        @SuppressWarnings("removal")
        @Override
        public @Nullable TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetEntityInfo'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceEntities(int maxDistance, boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @NotNull List<Block> getLastTwoTargetBlocks(@Nullable Set<Material> transparent, int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLastTwoTargetBlocks'");
        }

        @Override
        public @Nullable Block getTargetBlockExact(int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockExact'");
        }

        @Override
        public @Nullable Block getTargetBlockExact(int maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockExact'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceBlocks(double maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceBlocks'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceBlocks(double maxDistance,
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
        public @Nullable ItemStack getItemInUse() {
            
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
        public @Nullable Player getKiller() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getKiller'");
        }

        @Override
        public void setKiller(@Nullable Player killer) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setKiller'");
        }

        @Override
        public boolean addPotionEffect(@NotNull PotionEffect effect) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addPotionEffect'");
        }

        @Override
        public boolean addPotionEffect(@NotNull PotionEffect effect, boolean force) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addPotionEffect'");
        }

        @Override
        public boolean addPotionEffects(@NotNull Collection<PotionEffect> effects) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addPotionEffects'");
        }

        @Override
        public boolean hasPotionEffect(@NotNull PotionEffectType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPotionEffect'");
        }

        @Override
        public @Nullable PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPotionEffect'");
        }

        @Override
        public void removePotionEffect(@NotNull PotionEffectType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'removePotionEffect'");
        }

        @Override
        public @NotNull Collection<PotionEffect> getActivePotionEffects() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getActivePotionEffects'");
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
        public boolean setLeashHolder(@Nullable Entity holder) {
            
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
        public <T> @Nullable T getMemory(@NotNull MemoryKey<T> memoryKey) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMemory'");
        }

        @Override
        public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T memoryValue) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setMemory'");
        }

        @Override
        public @Nullable Sound getHurtSound() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHurtSound'");
        }

        @Override
        public @Nullable Sound getDeathSound() {
            
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
        public void setHurtDirection(float hurtDirection) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHurtDirection'");
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
        public @Nullable AttributeInstance getAttribute(@NotNull Attribute attribute) {
            
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
        public void damage(double amount, @Nullable Entity source) {
            
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
        public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile,
                @Nullable Vector velocity, @Nullable Consumer<? super T> function) {
            
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
        public void setLootTable(@Nullable LootTable table) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setLootTable'");
        }

        @Override
        public @Nullable LootTable getLootTable() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLootTable'");
        }

        @Override
        public void setSeed(long seed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setSeed'");
        }

        @Override
        public long getSeed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getSeed'");
        }

        @Override
        public @NotNull List<MerchantRecipe> getRecipes() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getRecipes'");
        }

        @Override
        public void setRecipes(@NotNull List<MerchantRecipe> recipes) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setRecipes'");
        }

        @Override
        public @NotNull MerchantRecipe getRecipe(int i) throws IndexOutOfBoundsException {
            
            throw new UnsupportedOperationException("Unimplemented method 'getRecipe'");
        }

        @Override
        public void setRecipe(int i, @NotNull MerchantRecipe recipe) throws IndexOutOfBoundsException {
            
            throw new UnsupportedOperationException("Unimplemented method 'setRecipe'");
        }

        @Override
        public int getRecipeCount() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getRecipeCount'");
        }

        @Override
        public boolean isTrading() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isTrading'");
        }

        @Override
        public @Nullable HumanEntity getTrader() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTrader'");
        }

        @Override
        public boolean increaseLevel(int amount) {
            
            throw new UnsupportedOperationException("Unimplemented method 'increaseLevel'");
        }

        @Override
        public boolean addTrades(int amount) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addTrades'");
        }

        @Override
        public int getRestocksToday() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getRestocksToday'");
        }

        @Override
        public void setReputation(@NotNull UUID uniqueId, @NotNull Reputation reputation) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setReputation'");
        }
        
        // Más métodos de LivingEntity como no-op...
    }
    
    /**
     * Mock de ZombieVillager.
     */
    static class MockZombieVillager extends MockEntity implements ZombieVillager {
        private Villager.Profession profession;
        private Villager.Type villagerType;
        private int conversionTime;
        private UUID converter;
        
        public MockZombieVillager(Location location) {
            super(location, EntityType.ZOMBIE_VILLAGER);
            this.profession = Villager.Profession.FARMER;
            this.villagerType = Villager.Type.PLAINS;
            this.conversionTime = -1;
        }
        
        @Override
        public Villager.Profession getVillagerProfession() { return profession; }
        
        @Override
        public void setVillagerProfession(Villager.Profession profession) { this.profession = profession; }
        
        @Override
        public Villager.Type getVillagerType() { return villagerType; }
        
        @Override
        public void setVillagerType(Villager.Type type) { this.villagerType = type; }
        
        @Override
        public boolean isConverting() { return conversionTime > 0; }
        
        @Override
        public int getConversionTime() { return conversionTime; }
        
        @Override
        public void setConversionTime(int time) { this.conversionTime = time; }
        
        @Override
        public OfflinePlayer getConversionPlayer() {
            return converter != null ? new MockOfflinePlayer(converter) : null;
        }
        
        @Override
        public void setConversionPlayer(OfflinePlayer conversionPlayer) {
            this.converter = conversionPlayer != null ? conversionPlayer.getUniqueId() : null;
        }
        
        // Implementar métodos de Zombie
        @Override
        public boolean isBaby() { return false; }
        
        @Override
        public void setBaby(boolean flag) {}

        @Override
        public void setCanBreakDoors(boolean flag) {}
        
        // Implementar métodos de LivingEntity
        @Override
        public double getHealth() { return 20.0; }
        
        @Override
        public void setHealth(double health) {}
        
        @Override
        public double getMaxHealth() { return 20.0; }
        
        @Override
        public void setMaxHealth(double health) {}
        
        @Override
        public void resetMaxHealth() {}
        
        // EntityScheduler no disponible en esta versión de Bukkit
        // @Override
        // public org.bukkit.scheduler.EntityScheduler getScheduler() { return null; }
        
        @Override
        public void setConversionTime(int time, boolean flag) { this.conversionTime = time; }
        
        @Override
        public boolean supportsBreakingDoors() { return false; }
        
        @Override
        public void setShouldBurnInDay(boolean shouldBurnInDay) {}
        
        @Override
        public boolean shouldBurnInDay() { return true; }
        
        @Override
        public boolean isArmsRaised() { return false; }
        
        @Override
        public void setArmsRaised(boolean raised) {}
        
        @Override
        public void stopDrowning() {}
        
        @Override
        public void startDrowning(int drownedConversionTime) {}
        
        @Override
        public boolean isDrowning() { return false; }
        
        @Override
        public boolean canBreakDoors() { return false; }
        
        @Override
        public void setVillager(boolean villager) {}
        
        @Override
        public boolean isVillager() { return false; }

        @Override
        public @NotNull EntityEquipment getEquipment() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEquipment'");
        }

        @Override
        public @NotNull Pathfinder getPathfinder() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPathfinder'");
        }

        @Override
        public boolean isInDaylight() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isInDaylight'");
        }

        @Override
        public void lookAt(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public void lookAt(@NotNull Location location, float headRotationSpeed, float maxHeadPitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public void lookAt(@NotNull Entity entity) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public void lookAt(@NotNull Entity entity, float headRotationSpeed, float maxHeadPitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public void lookAt(double x, double y, double z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public void lookAt(double x, double y, double z, float headRotationSpeed, float maxHeadPitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'lookAt'");
        }

        @Override
        public int getHeadRotationSpeed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHeadRotationSpeed'");
        }

        @Override
        public int getMaxHeadPitch() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMaxHeadPitch'");
        }

        @Override
        public void setTarget(@Nullable LivingEntity target) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setTarget'");
        }

        @Override
        public @Nullable LivingEntity getTarget() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTarget'");
        }

        @Override
        public void setAware(boolean aware) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAware'");
        }

        @Override
        public boolean isAware() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isAware'");
        }

        @Override
        public @Nullable Sound getAmbientSound() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAmbientSound'");
        }

        @Override
        public boolean isAggressive() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isAggressive'");
        }

        @Override
        public void setAggressive(boolean aggressive) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAggressive'");
        }

        @Override
        public boolean isLeftHanded() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isLeftHanded'");
        }

        @Override
        public void setLeftHanded(boolean leftHanded) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setLeftHanded'");
        }

        @Override
        public int getPossibleExperienceReward() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPossibleExperienceReward'");
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
        public @NotNull List<Block> getLineOfSight(@Nullable Set<Material> transparent, int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLineOfSight'");
        }

        @Override
        public @NotNull Block getTargetBlock(@Nullable Set<Material> transparent, int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlock'");
        }

        @Override
        public @Nullable Block getTargetBlock(int maxDistance, @SuppressWarnings("removal") @NotNull FluidMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlock'");
        }

        @Override
        public @Nullable BlockFace getTargetBlockFace(int maxDistance, @SuppressWarnings("removal") @NotNull FluidMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockFace'");
        }

        @Override
        public @Nullable BlockFace getTargetBlockFace(int maxDistance, @NotNull FluidCollisionMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockFace'");
        }

        @SuppressWarnings("removal")
        @Override
        public @Nullable TargetBlockInfo getTargetBlockInfo(int maxDistance, @NotNull FluidMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockInfo'");
        }

        @Override
        public @Nullable Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetEntity'");
        }

        @SuppressWarnings("removal")
        @Override
        public @Nullable TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetEntityInfo'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceEntities(int maxDistance, boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @NotNull List<Block> getLastTwoTargetBlocks(@Nullable Set<Material> transparent, int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLastTwoTargetBlocks'");
        }

        @Override
        public @Nullable Block getTargetBlockExact(int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockExact'");
        }

        @Override
        public @Nullable Block getTargetBlockExact(int maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockExact'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceBlocks(double maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceBlocks'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceBlocks(double maxDistance,
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
        public @Nullable ItemStack getItemInUse() {
            
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
        public @Nullable Player getKiller() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getKiller'");
        }

        @Override
        public void setKiller(@Nullable Player killer) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setKiller'");
        }

        @Override
        public boolean addPotionEffect(@NotNull PotionEffect effect) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addPotionEffect'");
        }

        @Override
        public boolean addPotionEffect(@NotNull PotionEffect effect, boolean force) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addPotionEffect'");
        }

        @Override
        public boolean addPotionEffects(@NotNull Collection<PotionEffect> effects) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addPotionEffects'");
        }

        @Override
        public boolean hasPotionEffect(@NotNull PotionEffectType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPotionEffect'");
        }

        @Override
        public @Nullable PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPotionEffect'");
        }

        @Override
        public void removePotionEffect(@NotNull PotionEffectType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'removePotionEffect'");
        }

        @Override
        public @NotNull Collection<PotionEffect> getActivePotionEffects() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getActivePotionEffects'");
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
        public boolean setLeashHolder(@Nullable Entity holder) {
            
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
        public <T> @Nullable T getMemory(@NotNull MemoryKey<T> memoryKey) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMemory'");
        }

        @Override
        public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T memoryValue) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setMemory'");
        }

        @Override
        public @Nullable Sound getHurtSound() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHurtSound'");
        }

        @Override
        public @Nullable Sound getDeathSound() {
            
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
        public void setHurtDirection(float hurtDirection) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHurtDirection'");
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
        public @Nullable AttributeInstance getAttribute(@NotNull Attribute attribute) {
            
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
        public void damage(double amount, @Nullable Entity source) {
            
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
        public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile,
                @Nullable Vector velocity, @Nullable Consumer<? super T> function) {
            
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
        public void setLootTable(@Nullable LootTable table) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setLootTable'");
        }

        @Override
        public @Nullable LootTable getLootTable() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLootTable'");
        }

        @Override
        public void setSeed(long seed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setSeed'");
        }

        @Override
        public long getSeed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getSeed'");
        }

        @Override
        public int getAge() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAge'");
        }

        @Override
        public void setAge(int age) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAge'");
        }

        @Override
        public void setAgeLock(boolean lock) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAgeLock'");
        }

        @Override
        public boolean getAgeLock() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAgeLock'");
        }

        @Override
        public void setBaby() {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBaby'");
        }

        @Override
        public void setAdult() {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAdult'");
        }

        @Override
        public boolean isAdult() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isAdult'");
        }

        @Override
        public boolean canBreed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'canBreed'");
        }

        @Override
        public void setBreed(boolean breed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBreed'");
        }
        
        // Más métodos como no-op...
    }
    
    /**
     * Mock de Zombie.
     */
    static class MockZombie extends MockEntity implements Zombie {
        private boolean baby;
        private boolean canBreakDoors;
        
        public MockZombie(Location location) {
            super(location, EntityType.ZOMBIE);
            this.baby = false;
            this.canBreakDoors = false;
        }
        
        @Override
        public boolean isBaby() { return baby; }
        
        @Override
        public void setBaby(boolean flag) { this.baby = flag; }
        
        public boolean getCanBreakDoors() { return canBreakDoors; }
        
        @Override
        public void setCanBreakDoors(boolean flag) { this.canBreakDoors = flag; }
        
        @Override
        public void setShouldBurnInDay(boolean shouldBurnInDay) {}
        
        @Override
        public boolean shouldBurnInDay() { return true; }
        
        @Override
        public boolean isArmsRaised() { return false; }
        
        @Override
        public void setArmsRaised(boolean raised) {}
        
        @Override
        public void stopDrowning() {}
        
        @Override
        public void startDrowning(int drownedConversionTime) {}
        
        // Implementar métodos de LivingEntity
        @Override
        public double getHealth() { return 20.0; }
        
        @Override
        public void setHealth(double health) {}
        
        @Override
        public double getMaxHealth() { return 20.0; }
        
        @Override
        public void setMaxHealth(double health) {}
        
        @Override
        public void resetMaxHealth() {}
        
        @Override
        public boolean isDrowning() { return false; }
        
        @Override
        public boolean canBreakDoors() { return false; }
        
        @Override
        public boolean supportsBreakingDoors() { return canBreakDoors; }
        
        @Override
        public void setConversionTime(int time) {}
        
        @Override
        public int getConversionTime() { return 0; }
        
        @Override
        public boolean isConverting() { return false; }
        
        @Override
        public org.bukkit.entity.Villager.Profession getVillagerProfession() {
            return org.bukkit.entity.Villager.Profession.NONE;
        }
        
        @Override
        public void setVillagerProfession(org.bukkit.entity.Villager.Profession profession) {
            // No-op para pruebas
        }
        
        @Override
        public void setVillager(boolean villager) {
            // No-op para pruebas
        }
        
        @Override
        public boolean isVillager() {
            return false; // Valor por defecto para pruebas
        }
        
        @Override
        public int getPossibleExperienceReward() {
            return 5; // Valor por defecto para pruebas
        }
        
        @Override
        public void setLeftHanded(boolean leftHanded) {
            // No-op para pruebas
        }
        
        @Override
        public boolean isLeftHanded() {
            return false; // Valor por defecto para pruebas
        }
        
        @Override
        public void setAggressive(boolean aggressive) {
            // No-op para pruebas
        }
        
        @Override
        public boolean isAggressive() {
            return false; // Valor por defecto para pruebas
        }
        
        @Override
        public org.bukkit.Sound getAmbientSound() {
            return null; // Valor por defecto para pruebas
        }
        
        @Override
        public boolean isAware() {
            return true; // Valor por defecto para pruebas
        }
        
        @Override
        public void setAware(boolean aware) {
            // No-op para pruebas
        }
        
        @Override
        public org.bukkit.entity.LivingEntity getTarget() {
            return null; // Valor por defecto para pruebas
        }
        
        @Override
        public void setTarget(org.bukkit.entity.LivingEntity target) {
            // No-op para pruebas
        }
        
        @Override
        public int getMaxHeadPitch() {
            return 0; // Valor por defecto para pruebas
        }
        
        @Override
        public int getHeadRotationSpeed() {
            return 0; // Valor por defecto para pruebas
        }
        
        @Override
        public void lookAt(double x, double y, double z, float headRotationSpeed, float maxHeadPitch) {
            // No-op para pruebas
        }
        
        @Override
        public void lookAt(double x, double y, double z) {
            // No-op para pruebas
        }
        
        @Override
        public void lookAt(org.bukkit.entity.Entity entity, float headRotationSpeed, float maxHeadPitch) {
            // No-op para pruebas
        }
        
        @Override
        public void lookAt(org.bukkit.entity.Entity entity) {
            // No-op para pruebas
        }
        
        @Override
        public void lookAt(org.bukkit.Location location, float headRotationSpeed, float maxHeadPitch) {
            // No-op para pruebas
        }
        
        @Override
        public void lookAt(org.bukkit.Location location) {
            // No-op para pruebas
        }

        @Override
        public @NotNull EntityEquipment getEquipment() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEquipment'");
        }

        @Override
        public @NotNull Pathfinder getPathfinder() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPathfinder'");
        }

        @Override
        public boolean isInDaylight() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isInDaylight'");
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
        public @NotNull List<Block> getLineOfSight(@Nullable Set<Material> transparent, int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLineOfSight'");
        }

        @Override
        public @NotNull Block getTargetBlock(@Nullable Set<Material> transparent, int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlock'");
        }

        @Override
        public @Nullable Block getTargetBlock(int maxDistance, @SuppressWarnings("removal") @NotNull FluidMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlock'");
        }

        @Override
        public @Nullable BlockFace getTargetBlockFace(int maxDistance, @SuppressWarnings("removal") @NotNull FluidMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockFace'");
        }

        @Override
        public @Nullable BlockFace getTargetBlockFace(int maxDistance, @NotNull FluidCollisionMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockFace'");
        }

        @SuppressWarnings("removal")
        @Override
        public @Nullable TargetBlockInfo getTargetBlockInfo(int maxDistance, @NotNull FluidMode fluidMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockInfo'");
        }

        @Override
        public @Nullable Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetEntity'");
        }

        @SuppressWarnings("removal")
        @Override
        public @Nullable TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetEntityInfo'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceEntities(int maxDistance, boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @NotNull List<Block> getLastTwoTargetBlocks(@Nullable Set<Material> transparent, int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLastTwoTargetBlocks'");
        }

        @Override
        public @Nullable Block getTargetBlockExact(int maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockExact'");
        }

        @Override
        public @Nullable Block getTargetBlockExact(int maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTargetBlockExact'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceBlocks(double maxDistance) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceBlocks'");
        }

        @Override
        public @Nullable RayTraceResult rayTraceBlocks(double maxDistance,
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
        public @Nullable ItemStack getItemInUse() {
            
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
        public @Nullable Player getKiller() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getKiller'");
        }

        @Override
        public void setKiller(@Nullable Player killer) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setKiller'");
        }

        @Override
        public boolean addPotionEffect(@NotNull PotionEffect effect) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addPotionEffect'");
        }

        @Override
        public boolean addPotionEffect(@NotNull PotionEffect effect, boolean force) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addPotionEffect'");
        }

        @Override
        public boolean addPotionEffects(@NotNull Collection<PotionEffect> effects) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addPotionEffects'");
        }

        @Override
        public boolean hasPotionEffect(@NotNull PotionEffectType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPotionEffect'");
        }

        @Override
        public @Nullable PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPotionEffect'");
        }

        @Override
        public void removePotionEffect(@NotNull PotionEffectType type) {
            
            throw new UnsupportedOperationException("Unimplemented method 'removePotionEffect'");
        }

        @Override
        public @NotNull Collection<PotionEffect> getActivePotionEffects() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getActivePotionEffects'");
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
        public boolean setLeashHolder(@Nullable Entity holder) {
            
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
        public <T> @Nullable T getMemory(@NotNull MemoryKey<T> memoryKey) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMemory'");
        }

        @Override
        public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T memoryValue) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setMemory'");
        }

        @Override
        public @Nullable Sound getHurtSound() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHurtSound'");
        }

        @Override
        public @Nullable Sound getDeathSound() {
            
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
        public void setHurtDirection(float hurtDirection) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHurtDirection'");
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
        public @Nullable AttributeInstance getAttribute(@NotNull Attribute attribute) {
            
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
        public void damage(double amount, @Nullable Entity source) {
            
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
        public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile,
                @Nullable Vector velocity, @Nullable Consumer<? super T> function) {
            
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
        public void setLootTable(@Nullable LootTable table) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setLootTable'");
        }

        @Override
        public @Nullable LootTable getLootTable() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLootTable'");
        }

        @Override
        public void setSeed(long seed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setSeed'");
        }

        @Override
        public long getSeed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getSeed'");
        }

        @Override
        public int getAge() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAge'");
        }

        @Override
        public void setAge(int age) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAge'");
        }

        @Override
        public void setAgeLock(boolean lock) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAgeLock'");
        }

        @Override
        public boolean getAgeLock() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAgeLock'");
        }

        @Override
        public void setBaby() {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBaby'");
        }

        @Override
        public void setAdult() {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAdult'");
        }

        @Override
        public boolean isAdult() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isAdult'");
        }

        @Override
        public boolean canBreed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'canBreed'");
        }

        @Override
        public void setBreed(boolean breed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBreed'");
        }
        
        // Más métodos como no-op...
    }
    
    // Más clases mock pueden ser añadidas según sea necesario...
}