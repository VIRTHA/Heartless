package com.darkbladedev.testing.tests;

import com.darkbladedev.testing.TestCase;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.HeartlessMain;
import com.darkbladedev.utils.TimeExpression;
import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.block.TargetBlockInfo.FluidMode;
import com.destroystokyo.paper.entity.Pathfinder;
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

import org.bukkit.BanEntry;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Input;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.ServerLinks;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
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
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.event.player.PlayerKickEvent.Cause;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.loot.LootTable;
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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Casos de prueba para la clase UndeadWeek.
 * Verifica la funcionalidad específica del evento de la semana de no-muertos.
 */
public class UndeadWeekTest extends TestCase {
    
    public UndeadWeekTest(String testName) {
        super(testName);
    }

    private UndeadWeek undeadWeek;
    private MockPlayer mockPlayer1;
    private MockPlayer mockPlayer2;
    private MockVillager mockVillager;
    private MockZombieVillager mockZombieVillager;
    private List<String> eventLog;
    
    @Override
    public void setUp() throws Exception {
        // Crear mocks para los parámetros del constructor
        HeartlessMain mockPlugin = MockHeartlessMain.create();
        TimeExpression mockTimeExpression = new MockTimeExpression();
        undeadWeek = new UndeadWeek(mockPlugin, mockTimeExpression);
        mockPlayer1 = new MockPlayer("TestPlayer1");
        mockPlayer2 = new MockPlayer("TestPlayer2");
        mockVillager = new MockVillager();
        mockZombieVillager = new MockZombieVillager();
        eventLog = new ArrayList<>();
    }
    
    @Override
    public void tearDown() throws Exception {
        if (undeadWeek != null) {
            undeadWeek.stop();
        }
        eventLog.clear();
    }
    
    @Override
    public void runTest() throws Exception {
        testEventInitialization();
        testPlayerInfection();
        testVillagerCuring();
        testCuredVillagersCount();
        testRedMoonMechanics();
        testChallengeCompletion();
        testPlayerStatistics();
        testEventPersistence();
        testConcurrentPlayerActions();
        testEdgeCases();
    }
    
    /**
     * Prueba la inicialización correcta del evento.
     */
    private void testEventInitialization() throws Exception {
        // Verificar estado inicial
        assertNotNull(undeadWeek, "UndeadWeek no debe ser null");
        assertEquals("UndeadWeek", undeadWeek.getName(), "El nombre del evento debe ser 'UndeadWeek'");
        assertFalse(undeadWeek.isActive(), "El evento no debe estar activo inicialmente");
        assertFalse(undeadWeek.isRedMoonActive(), "La luna roja no debe estar activa inicialmente");
        
        // Verificar mapas inicializados
        assertNotNull(undeadWeek.getInfectedPlayersCount(), "InfectedPlayersCount no debe ser null");
        assertNotNull(undeadWeek.getInfectedPlayersTime(), "InfectedPlayersTime no debe ser null");
        assertNotNull(undeadWeek.getCuredInfections(), "CuredInfections no debe ser null");
        assertNotNull(undeadWeek.getRedMoonKills(), "RedMoonKills no debe ser null");
        assertNotNull(undeadWeek.getCuredVillagers(), "CuredVillagers no debe ser null");
        assertNotNull(undeadWeek.getCuredVillagersCount(), "CuredVillagersCount no debe ser null");
        
        // Verificar que están vacíos
        assertEquals(Integer.valueOf(0), Integer.valueOf(undeadWeek.getInfectedPlayersCount()), "No debe haber jugadores infectados inicialmente");
        assertEquals(Integer.valueOf(0), Integer.valueOf(undeadWeek.getCuredVillagers().size()), "No debe haber aldeanos curados inicialmente");
        assertEquals(Integer.valueOf(0), Integer.valueOf(undeadWeek.getCuredVillagersCount().size()), "No debe haber conteo de aldeanos curados inicialmente");
    }
    
    /**
     * Prueba el sistema de infección de jugadores.
     */
    private void testPlayerInfection() throws Exception {
        undeadWeek.start();
        
        // Infectar jugador
        undeadWeek.infectPlayer(mockPlayer1);
        
        // Verificar infección
        assertTrue(undeadWeek.isPlayerInfected(mockPlayer1), "El jugador 1 debe estar infectado después de la infección");
        assertEquals(Integer.valueOf(1), Integer.valueOf(undeadWeek.getInfectedPlayersCount()), "Debe haber 1 jugador infectado");
        assertTrue(undeadWeek.getInfectedPlayers().containsKey(mockPlayer1.getUniqueId()), "El mapa de jugadores infectados debe contener al jugador 1");
        
        // Verificar tiempo de infección
        assertTrue(undeadWeek.getInfectedPlayersTime().containsKey(mockPlayer1.getUniqueId()), "El mapa de tiempos de infección debe contener al jugador 1");
        long infectionTime = undeadWeek.getInfectedPlayersTime().get(mockPlayer1.getUniqueId());
        assertTrue(System.currentTimeMillis() - infectionTime < 1000, "El tiempo de infección debe ser reciente (menos de 1 segundo)");
        
        // Infectar segundo jugador
        undeadWeek.infectPlayer(mockPlayer2);
        assertEquals(Integer.valueOf(2), Integer.valueOf(undeadWeek.getInfectedPlayersCount()), "Debe haber 2 jugadores infectados");
        
        // Curar primer jugador
        undeadWeek.curePlayerInfection(mockPlayer1);
        assertFalse(undeadWeek.isPlayerInfected(mockPlayer1), "El jugador 1 no debe estar infectado después de curarse");
        assertTrue(undeadWeek.isPlayerInfected(mockPlayer2), "El jugador 2 debe seguir infectado");
        
        // Verificar estadísticas de curación
        assertTrue(undeadWeek.getCuredInfections().containsKey(mockPlayer1.getUniqueId()), "El mapa de infecciones curadas debe contener al jugador 1");
        assertEquals(Integer.valueOf(1), undeadWeek.getCuredInfections().get(mockPlayer1.getUniqueId()), "El jugador debe tener 1 infección curada");
    }
    
    /**
     * Prueba el sistema de curación de aldeanos.
     */
    private void testVillagerCuring() throws Exception {
        undeadWeek.start();
        
        // Simular transformación de zombie villager a villager
        MockEntityTransformEvent transformEvent = new MockEntityTransformEvent(mockZombieVillager, mockVillager);
        transformEvent.setPlayer(mockPlayer1);
        
        // Procesar evento de transformación
        undeadWeek.onEntityTransform(transformEvent);
        
        // Verificar que se registró la curación
        assertTrue(undeadWeek.getCuredVillagers().contains(mockPlayer1.getUniqueId()), "La lista de aldeanos curados debe contener al jugador 1");
        assertTrue(undeadWeek.getCuredVillagersCount().containsKey(mockPlayer1.getUniqueId()), "El mapa de conteo de aldeanos curados debe contener al jugador 1");
        assertEquals(Integer.valueOf(1), undeadWeek.getCuredVillagersCount().get(mockPlayer1.getUniqueId()), "El jugador debe tener 1 aldeano curado");
        
        // Curar otro aldeano con el mismo jugador
        MockZombieVillager anotherZombieVillager = new MockZombieVillager();
        MockVillager anotherVillager = new MockVillager();
        MockEntityTransformEvent secondTransform = new MockEntityTransformEvent(anotherZombieVillager, anotherVillager);
        secondTransform.setPlayer(mockPlayer1);
        
        undeadWeek.onEntityTransform(secondTransform);
        
        // Verificar incremento del contador
        assertEquals(Integer.valueOf(2), undeadWeek.getCuredVillagersCount().get(mockPlayer1.getUniqueId()), "El jugador debe tener 2 aldeanos curados");
        
        // Curar aldeano con segundo jugador
        MockEntityTransformEvent thirdTransform = new MockEntityTransformEvent(new MockZombieVillager(), new MockVillager());
        thirdTransform.setPlayer(mockPlayer2);
        
        undeadWeek.onEntityTransform(thirdTransform);
        
        // Verificar que ambos jugadores están registrados
        assertEquals(Integer.valueOf(2), Integer.valueOf(undeadWeek.getCuredVillagers().size()), "Debe haber 2 jugadores con aldeanos curados");
        assertEquals(Integer.valueOf(2), Integer.valueOf(undeadWeek.getCuredVillagersCount().size()), "El mapa de conteo debe tener 2 entradas");
        assertEquals(Integer.valueOf(1), undeadWeek.getCuredVillagersCount().get(mockPlayer2.getUniqueId()), "El segundo jugador debe tener 1 aldeano curado");
    }
    
    /**
     * Prueba específica del contador de aldeanos curados.
     */
    private void testCuredVillagersCount() throws Exception {
        undeadWeek.start();
        
        // Verificar estado inicial
        assertEquals(Integer.valueOf(0), undeadWeek.getCuredVillagersCount().getOrDefault(mockPlayer1.getUniqueId(), 0), "El conteo inicial debe ser 0");
        
        // Simular múltiples curaciones
        for (int i = 0; i < 5; i++) {
            MockEntityTransformEvent event = new MockEntityTransformEvent(new MockZombieVillager(), new MockVillager());
            event.setPlayer(mockPlayer1);
            undeadWeek.onEntityTransform(event);
        }
        
        // Verificar contador
        assertEquals(Integer.valueOf(5), undeadWeek.getCuredVillagersCount().getOrDefault(mockPlayer1.getUniqueId(), 0), "El jugador debe tener 5 aldeanos curados");
        
        // Verificar que el método hasChallengeCompleted funciona correctamente
        assertTrue(undeadWeek.hasChallengeCompleted(mockPlayer1), "El jugador 1 debe haber completado el desafío con 5 aldeanos curados");
        
        // Probar con jugador que no ha curado aldeanos
        assertFalse(undeadWeek.hasChallengeCompleted(mockPlayer2), "El jugador 2 no debe haber completado el desafío sin aldeanos curados");
        
        // Curar un aldeano con el segundo jugador
        MockEntityTransformEvent event = new MockEntityTransformEvent(new MockZombieVillager(), new MockVillager());
        event.setPlayer(mockPlayer2);
        undeadWeek.onEntityTransform(event);
        
        assertTrue(undeadWeek.hasChallengeCompleted(mockPlayer2), "El jugador 2 debe haber completado el desafío después de curar 1 aldeano");
    }
    
    /**
     * Prueba las mecánicas de luna roja.
     */
    private void testRedMoonMechanics() throws Exception {
        undeadWeek.start();
        
        // Verificar estado inicial
        assertFalse(undeadWeek.isRedMoonActive(), "La luna roja no debe estar activa inicialmente");
        assertEquals(Long.valueOf(0), Long.valueOf(undeadWeek.getRedMoonStartTime()), "El tiempo de inicio debe ser 0");
        assertEquals(Long.valueOf(0), Long.valueOf(undeadWeek.getRedMoonEndTime()), "El tiempo de fin debe ser 0");
        
        // Activar luna roja
        long startTime = System.currentTimeMillis();
        long duration = 60000; // 1 minuto
        undeadWeek.startRedMoon(duration);
        
        // Verificar activación
        assertTrue(undeadWeek.isRedMoonActive(), "La luna roja debe estar activa después de iniciarla");
        assertTrue(undeadWeek.getRedMoonStartTime() >= startTime, "El tiempo de inicio debe ser mayor o igual al tiempo de activación");
        assertTrue(undeadWeek.getRedMoonEndTime() > System.currentTimeMillis(), "El tiempo de fin debe ser mayor al tiempo actual");
        
        // Simular kill durante luna roja
        undeadWeek.recordRedMoonKill(mockPlayer1);
        
        // Verificar registro de kill
        assertTrue(undeadWeek.getRedMoonKills().containsKey(mockPlayer1.getUniqueId()), "El mapa de kills de luna roja debe contener al jugador 1");
        assertEquals(Integer.valueOf(1), undeadWeek.getRedMoonKills().get(mockPlayer1.getUniqueId()), "El jugador debe tener 1 kill en luna roja");
        
        // Registrar más kills
        undeadWeek.recordRedMoonKill(mockPlayer1);
        undeadWeek.recordRedMoonKill(mockPlayer1);
        assertEquals(Integer.valueOf(3), undeadWeek.getRedMoonKills().get(mockPlayer1.getUniqueId()), "El jugador debe tener 3 kills en luna roja");
        
        // Terminar luna roja
        undeadWeek.endRedMoon();
        assertFalse(undeadWeek.isRedMoonActive(), "La luna roja no debe estar activa después de terminar");
        
        // Verificar que los kills se mantienen
        assertEquals(Integer.valueOf(3), undeadWeek.getRedMoonKills().get(mockPlayer1.getUniqueId()), "El conteo debe mantenerse en 3 kills");
    }
    
    /**
     * Prueba la lógica de completación de desafíos.
     */
    private void testChallengeCompletion() throws Exception {
        undeadWeek.start();
        
        // Verificar que ningún jugador ha completado el desafío inicialmente
        assertFalse(undeadWeek.hasChallengeCompleted(mockPlayer1), "El jugador 1 no debe haber completado el desafío inicialmente");
        assertFalse(undeadWeek.hasChallengeCompleted(mockPlayer2), "El jugador 2 no debe haber completado el desafío inicialmente");
        
        // Completar desafío para jugador 1 (curar al menos 1 aldeano)
        MockEntityTransformEvent event = new MockEntityTransformEvent(new MockZombieVillager(), new MockVillager());
        event.setPlayer(mockPlayer1);
        undeadWeek.onEntityTransform(event);
        
        // Verificar completación
        assertTrue(undeadWeek.hasChallengeCompleted(mockPlayer1), "El jugador 1 debe haber completado el desafío");
        assertFalse(undeadWeek.hasChallengeCompleted(mockPlayer2), "El jugador 2 no debe haber completado el desafío");
        
        // Obtener lista de jugadores que completaron el desafío
        List<UUID> completedPlayers = undeadWeek.getPlayersWhoCompletedChallenge();
        assertEquals(Integer.valueOf(1), Integer.valueOf(completedPlayers.size()), "Debe haber 1 jugador que completó el desafío");
        assertTrue(completedPlayers.contains(mockPlayer1.getUniqueId()), "La lista debe contener al jugador 1");
        
        // Completar desafío para jugador 2
        MockEntityTransformEvent event2 = new MockEntityTransformEvent(new MockZombieVillager(), new MockVillager());
        event2.setPlayer(mockPlayer2);
        undeadWeek.onEntityTransform(event2);
        
        // Verificar que ambos completaron
        assertTrue(undeadWeek.hasChallengeCompleted(mockPlayer2), "El jugador 2 debe haber completado el desafío después de la transformación");
        
        completedPlayers = undeadWeek.getPlayersWhoCompletedChallenge();
        assertEquals(Integer.valueOf(2), Integer.valueOf(completedPlayers.size()), "Deben haber 2 jugadores que completaron el desafío");
    }
    
    /**
     * Prueba la generación de estadísticas de jugadores.
     */
    private void testPlayerStatistics() throws Exception {
        undeadWeek.start();
        
        // Configurar datos de prueba
        undeadWeek.infectPlayer(mockPlayer1);
        undeadWeek.curePlayerInfection(mockPlayer1);
        undeadWeek.infectPlayer(mockPlayer1);
        undeadWeek.curePlayerInfection(mockPlayer1);
        
        // Curar aldeanos
        for (int i = 0; i < 3; i++) {
            MockEntityTransformEvent event = new MockEntityTransformEvent(new MockZombieVillager(), new MockVillager());
            event.setPlayer(mockPlayer1);
            undeadWeek.onEntityTransform(event);
        }
        
        // Activar luna roja y registrar kills
        undeadWeek.startRedMoon(60000);
        for (int i = 0; i < 5; i++) {
            undeadWeek.recordRedMoonKill(mockPlayer1);
        }
        
        // Obtener estadísticas
        Map<String, Object> stats = undeadWeek.getPlayerStatistics(mockPlayer1);
        
        // Verificar estadísticas
        assertNotNull(stats, "Stats no debe ser null");
        assertEquals(Integer.valueOf(2), stats.get("curedInfections"), "Las infecciones curadas deben ser 2");
        assertEquals(Integer.valueOf(3), stats.get("curedVillagers"), "Los aldeanos curados deben ser 3");
        assertEquals(Integer.valueOf(5), stats.get("redMoonKills"), "Los kills en luna roja deben ser 5");
        assertTrue((Boolean) stats.get("challengeCompleted"), "El desafío debe estar completado");
        
        // Verificar estadísticas de jugador sin actividad
        Map<String, Object> emptyStats = undeadWeek.getPlayerStatistics(mockPlayer2);
        assertEquals(Integer.valueOf(0), emptyStats.get("curedInfections"), "Las infecciones curadas deben ser 0");
        assertEquals(Integer.valueOf(0), emptyStats.get("curedVillagers"), "Los aldeanos curados deben ser 0");
        assertEquals(Integer.valueOf(0), emptyStats.get("redMoonKills"), "Los kills en luna roja deben ser 0");
        assertFalse((Boolean) emptyStats.get("challengeCompleted"), "El desafío no debe estar completado para jugador sin actividad");
    }
    
    /**
     * Prueba la persistencia de datos del evento.
     */
    private void testEventPersistence() throws Exception {
        undeadWeek.start();
        
        // Configurar datos
        undeadWeek.infectPlayer(mockPlayer1);
        undeadWeek.startRedMoon(60000);
        undeadWeek.recordRedMoonKill(mockPlayer1);
        
        MockEntityTransformEvent event = new MockEntityTransformEvent(new MockZombieVillager(), new MockVillager());
        event.setPlayer(mockPlayer1);
        undeadWeek.onEntityTransform(event);
        
        // Guardar estado
        Map<String, Object> savedState = undeadWeek.saveState();
        assertNotNull(savedState, "SavedState no debe ser null");
        assertTrue(savedState.size() > 0, "El estado guardado debe contener datos");
        
        // Crear nuevo evento y cargar estado
        HeartlessMain mockPlugin2 = MockHeartlessMain.create();
        TimeExpression mockTimeExpression2 = new MockTimeExpression();
        UndeadWeek newUndeadWeek = new UndeadWeek(mockPlugin2, mockTimeExpression2);
        newUndeadWeek.loadState(savedState);
        
        // Verificar que los datos se cargaron correctamente
        assertTrue(newUndeadWeek.isPlayerInfected(mockPlayer1), "El jugador debe estar infectado después de cargar");
        assertTrue(newUndeadWeek.isRedMoonActive(), "La luna roja debe estar activa después de cargar");
        assertEquals(Integer.valueOf(1), newUndeadWeek.getRedMoonKills().get(mockPlayer1.getUniqueId()), "El jugador debe tener 1 kill en luna roja después de cargar");
        assertEquals(Integer.valueOf(1), newUndeadWeek.getCuredVillagersCount().get(mockPlayer1.getUniqueId()), "El jugador debe tener 1 aldeano curado después de cargar");
        assertTrue(newUndeadWeek.hasChallengeCompleted(mockPlayer1), "El jugador debe haber completado el desafío después de cargar");
    }
    
    /**
     * Prueba el acceso concurrente a los datos del evento.
     */
    private void testConcurrentPlayerActions() throws Exception {
        undeadWeek.start();
        
        final int threadCount = 3;
        final int actionsPerThread = 10;
        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startLatch = new CountDownLatch(threadCount);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        
        // Crear jugadores mock para cada hilo
        List<MockPlayer> players = Arrays.asList(
            new MockPlayer("ConcurrentPlayer1"),
            new MockPlayer("ConcurrentPlayer2"),
            new MockPlayer("ConcurrentPlayer3")
        );
        
        // Crear hilos que realicen acciones concurrentes
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            final MockPlayer player = players.get(threadId);
            
            Thread thread = new Thread(() -> {
                try {
                    startLatch.countDown();
                    startLatch.await();
                    
                    for (int j = 0; j < actionsPerThread; j++) {
                        // Alternar entre diferentes acciones
                        switch (j % 4) {
                            case 0:
                                undeadWeek.infectPlayer(player);
                                break;
                            case 1:
                                undeadWeek.curePlayerInfection(player);
                                break;
                            case 2:
                                MockEntityTransformEvent event = new MockEntityTransformEvent(new MockZombieVillager(), new MockVillager());
                                event.setPlayer(player);
                                undeadWeek.onEntityTransform(event);
                                break;
                            case 3:
                                undeadWeek.recordRedMoonKill(player);
                                break;
                        }
                        
                        // Pequeña pausa para simular tiempo real
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    finishLatch.countDown();
                }
            });
            threads.add(thread);
        }
        
        // Activar luna roja para permitir kills
        undeadWeek.startRedMoon(60000);
        
        // Ejecutar hilos
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Esperar finalización
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS), "Todas las tareas concurrentes deben completarse en 10 segundos");
        
        // Verificar que no hubo excepciones
        if (!exceptions.isEmpty()) {
            throw new AssertionError("Excepciones durante acceso concurrente: " + exceptions.get(0).getMessage());
        }
        
        // Verificar integridad de datos
        for (MockPlayer player : players) {
            Map<String, Object> stats = undeadWeek.getPlayerStatistics(player);
            assertNotNull(stats, "Stats no debe ser null");
            
            // Los valores exactos pueden variar debido a la concurrencia,
            // pero deben ser números válidos
            assertTrue((Integer) stats.get("curedInfections") >= 0, "Las infecciones curadas deben ser >= 0");
            assertTrue((Integer) stats.get("curedVillagers") >= 0, "Los aldeanos curados deben ser >= 0");
            assertTrue((Integer) stats.get("redMoonKills") >= 0, "Los kills en luna roja deben ser >= 0");
        }
    }
    
    /**
     * Prueba casos extremos y situaciones límite.
     */
    private void testEdgeCases() throws Exception {
        // Probar con jugador null
        assertThrows(IllegalArgumentException.class, () -> {
            undeadWeek.infectPlayer(null);
        }, "Debe lanzar excepción con jugador null");
        
        // Probar curación sin infección previa
        undeadWeek.start();
        undeadWeek.curePlayerInfection(mockPlayer1); // No debe fallar
        
        // Verificar que no se registró curación inválida
        assertFalse(undeadWeek.getCuredInfections().containsKey(mockPlayer1.getUniqueId()), "No debe registrar curación inválida");
        
        // Probar múltiples infecciones del mismo jugador
        undeadWeek.infectPlayer(mockPlayer1);
        undeadWeek.infectPlayer(mockPlayer1);
        undeadWeek.infectPlayer(mockPlayer1);
        
        // Debe seguir siendo 1 infección
        assertTrue(undeadWeek.isPlayerInfected(mockPlayer1), "El jugador debe seguir infectado después de múltiples infecciones");
        
        // Probar kills de luna roja sin luna roja activa
        undeadWeek.endRedMoon(); // Asegurar que no está activa
        undeadWeek.recordRedMoonKill(mockPlayer1);
        
        // No debe registrar el kill
        assertFalse(undeadWeek.getRedMoonKills().containsKey(mockPlayer1.getUniqueId()), "No debe registrar kill sin luna roja activa");
        
        // Probar transformación con entidades incorrectas
        MockEntityTransformEvent invalidEvent = new MockEntityTransformEvent(new MockZombie(), new MockVillager());
        invalidEvent.setPlayer(mockPlayer1);
        
        int initialCount = undeadWeek.getCuredVillagersCount().getOrDefault(mockPlayer1.getUniqueId(), 0);
        undeadWeek.onEntityTransform(invalidEvent);
        
        // No debe incrementar el contador
        assertEquals(Integer.valueOf(initialCount), undeadWeek.getCuredVillagersCount().getOrDefault(mockPlayer1.getUniqueId(), 0), "El conteo debe coincidir con el valor inicial");
        
        // Probar duración negativa de luna roja
        try {
            undeadWeek.startRedMoon(-1000);
            fail("Debe lanzar IllegalArgumentException con duración negativa");
        } catch (IllegalArgumentException e) {
            assertEquals("Debe lanzar excepción con duración negativa", "Duration cannot be negative", e.getMessage());
        }
    }
    
    // === CLASES MOCK PARA PRUEBAS ===
    
    private static class MockPlayer implements Player {
        private final String name;
        private final UUID uuid;
        
        public MockPlayer(String name) {
            this.name = name;
            this.uuid = UUID.randomUUID();
        }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public UUID getUniqueId() { return uuid; }
        
        // Implementar otros métodos necesarios como no-op
        @Override
        public boolean isOnline() { return true; }
        
        @Override
        public void sendMessage(String message) {
            // No-op para pruebas
        }
        
        @Override
        public void setDeathScreenScore(int score) {
            // No-op para pruebas
        }
        
        @Override
        public int getDeathScreenScore() {
            return 0; // Valor por defecto para pruebas
        }

        @Override
        public EntityEquipment getEquipment() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEquipment'");
        }

        @Override
        public PlayerInventory getInventory() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getInventory'");
        }

        @Override
        public Inventory getEnderChest() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEnderChest'");
        }

        @Override
        public MainHand getMainHand() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMainHand'");
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
        public GameMode getGameMode() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getGameMode'");
        }

        @Override
        public void setGameMode(GameMode mode) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setGameMode'");
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
        public float getSaturation() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getSaturation'");
        }

        @Override
        public void setSaturation(float value) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setSaturation'");
        }

        @Override
        public int getFoodLevel() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getFoodLevel'");
        }

        @Override
        public void setFoodLevel(int value) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setFoodLevel'");
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
        public @org.jetbrains.annotations.Nullable RayTraceResult rayTraceEntities(int maxDistance,
                boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @NotNull List<Block> getLastTwoTargetBlocks(
                @org.jetbrains.annotations.Nullable Set<Material> transparent, int maxDistance) {
            
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
        public @org.jetbrains.annotations.Nullable PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
            
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
        public double getHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHealth'");
        }

        @Override
        public void setHealth(double health) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHealth'");
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
        public double getMaxHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMaxHealth'");
        }

        @Override
        public void setMaxHealth(double health) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setMaxHealth'");
        }

        @Override
        public void resetMaxHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'resetMaxHealth'");
        }

        @Override
        public @NotNull Location getLocation() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLocation'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable Location getLocation(
                @org.jetbrains.annotations.Nullable Location loc) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLocation'");
        }

        @Override
        public void setVelocity(@NotNull Vector velocity) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setVelocity'");
        }

        @Override
        public @NotNull Vector getVelocity() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getVelocity'");
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
        public boolean isInWater() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isInWater'");
        }

        @Override
        public @NotNull World getWorld() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getWorld'");
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
        public boolean teleport(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Location location, @NotNull TeleportCause cause) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Entity destination) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Entity destination, @NotNull TeleportCause cause) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location loc, @NotNull TeleportCause cause,
                @NotNull TeleportFlag @NotNull... teleportFlags) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleportAsync'");
        }

        @Override
        public @NotNull List<Entity> getNearbyEntities(double x, double y, double z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getNearbyEntities'");
        }

        @Override
        public int getEntityId() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEntityId'");
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
        public void remove() {
            
            throw new UnsupportedOperationException("Unimplemented method 'remove'");
        }

        @Override
        public boolean isDead() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isDead'");
        }

        @Override
        public boolean isValid() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isValid'");
        }

        @Override
        public @NotNull Server getServer() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getServer'");
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
        public @NotNull EntityType getType() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getType'");
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
        public void sendMessage(@NotNull String... messages) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public void sendMessage(@org.jetbrains.annotations.Nullable UUID sender, @NotNull String message) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public void sendMessage(@org.jetbrains.annotations.Nullable UUID sender, @NotNull String... messages) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public @NotNull Component name() {
            
            throw new UnsupportedOperationException("Unimplemented method 'name'");
        }

        @Override
        public boolean isPermissionSet(@NotNull String name) {
            
            throw new UnsupportedOperationException("Unimplemented method 'isPermissionSet'");
        }

        @Override
        public boolean isPermissionSet(@NotNull Permission perm) {
            
            throw new UnsupportedOperationException("Unimplemented method 'isPermissionSet'");
        }

        @Override
        public boolean hasPermission(@NotNull String name) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPermission'");
        }

        @Override
        public boolean hasPermission(@NotNull Permission perm) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPermission'");
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name,
                boolean value) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin,
                @NotNull String name, boolean value, int ticks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin,
                int ticks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public void removeAttachment(@NotNull PermissionAttachment attachment) {
            
            throw new UnsupportedOperationException("Unimplemented method 'removeAttachment'");
        }

        @Override
        public void recalculatePermissions() {
            
            throw new UnsupportedOperationException("Unimplemented method 'recalculatePermissions'");
        }

        @Override
        public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEffectivePermissions'");
        }

        @Override
        public boolean isOp() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isOp'");
        }

        @Override
        public void setOp(boolean value) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setOp'");
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
        public void abandonConversation(@NotNull Conversation conversation,
                @NotNull ConversationAbandonedEvent details) {
            
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
        public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason,
                @Nullable Date expires, @Nullable String source) {
            
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
        public long getFirstPlayed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getFirstPlayed'");
        }

        @Override
        public long getLastPlayed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLastPlayed'");
        }

        @Override
        public boolean hasPlayedBefore() {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPlayedBefore'");
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
        public void incrementStatistic(Statistic statistic, Material material, int amount)
                throws IllegalArgumentException {
            
            throw new UnsupportedOperationException("Unimplemented method 'incrementStatistic'");
        }

        @Override
        public void decrementStatistic(Statistic statistic, Material material, int amount)
                throws IllegalArgumentException {
            
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
        public String getDisplayName() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getDisplayName'");
        }

        @Override
        public void setDisplayName(@Nullable String name) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setDisplayName'");
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
        public String getPlayerListName() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getPlayerListName'");
        }

        @Override
        public void setPlayerListName(@Nullable String name) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setPlayerListName'");
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
        public void setCompassTarget(Location loc) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setCompassTarget'");
        }

        @Override
        public Location getCompassTarget() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getCompassTarget'");
        }

        @Override
        public @Nullable InetSocketAddress getAddress() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAddress'");
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
        public void kickPlayer(@Nullable String message) {
            
            throw new UnsupportedOperationException("Unimplemented method 'kickPlayer'");
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
        public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason,
                @Nullable Date expires, @Nullable String source, boolean kickPlayer) {
            
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
        public void chat(String msg) {
            
            throw new UnsupportedOperationException("Unimplemented method 'chat'");
        }

        @Override
        public boolean performCommand(String command) {
            
            throw new UnsupportedOperationException("Unimplemented method 'performCommand'");
        }

        @Override
        public boolean isOnGround() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isOnGround'");
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
        public boolean isSprinting() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isSprinting'");
        }

        @Override
        public void setSprinting(boolean sprinting) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setSprinting'");
        }

        @Override
        public void saveData() {
            
            throw new UnsupportedOperationException("Unimplemented method 'saveData'");
        }

        @Override
        public void loadData() {
            
            throw new UnsupportedOperationException("Unimplemented method 'loadData'");
        }

        @Override
        public void setSleepingIgnored(boolean isSleeping) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setSleepingIgnored'");
        }

        @Override
        public boolean isSleepingIgnored() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isSleepingIgnored'");
        }

        @Override
        public void setBedSpawnLocation(@Nullable Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBedSpawnLocation'");
        }

        @Override
        public void setRespawnLocation(@Nullable Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setRespawnLocation'");
        }

        @Override
        public void setBedSpawnLocation(@Nullable Location location, boolean force) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setBedSpawnLocation'");
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
        public void playNote(Location loc, byte instrument, byte note) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playNote'");
        }

        @Override
        public void playNote(Location loc, Instrument instrument, Note note) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playNote'");
        }

        @Override
        public void playSound(Location location, Sound sound, float volume, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void playSound(Location location, String sound, float volume, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void playSound(Location location, String sound, SoundCategory category, float volume, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
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
        public void playSound(Entity entity, Sound sound, SoundCategory category, float volume, float pitch,
                long seed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void playSound(Entity entity, String sound, SoundCategory category, float volume, float pitch,
                long seed) {
            
            throw new UnsupportedOperationException("Unimplemented method 'playSound'");
        }

        @Override
        public void stopSound(Sound sound) {
            
            throw new UnsupportedOperationException("Unimplemented method 'stopSound'");
        }

        @Override
        public void stopSound(String sound) {
            
            throw new UnsupportedOperationException("Unimplemented method 'stopSound'");
        }

        @Override
        public void stopSound(Sound sound, @Nullable SoundCategory category) {
            
            throw new UnsupportedOperationException("Unimplemented method 'stopSound'");
        }

        @Override
        public void stopSound(String sound, @Nullable SoundCategory category) {
            
            throw new UnsupportedOperationException("Unimplemented method 'stopSound'");
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
        public void giveExpLevels(int amount) {
            
            throw new UnsupportedOperationException("Unimplemented method 'giveExpLevels'");
        }

        @Override
        public float getExp() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getExp'");
        }

        @Override
        public void setExp(float exp) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setExp'");
        }

        @Override
        public int getLevel() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLevel'");
        }

        @Override
        public void setLevel(int level) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setLevel'");
        }

        @Override
        public int getTotalExperience() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTotalExperience'");
        }

        @Override
        public void setTotalExperience(int exp) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setTotalExperience'");
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
        public boolean getAllowFlight() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getAllowFlight'");
        }

        @Override
        public void setAllowFlight(boolean flight) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setAllowFlight'");
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
        public boolean isFlying() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isFlying'");
        }

        @Override
        public void setFlying(boolean value) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setFlying'");
        }

        @Override
        public void setFlySpeed(float value) throws IllegalArgumentException {
            
            throw new UnsupportedOperationException("Unimplemented method 'setFlySpeed'");
        }

        @Override
        public void setWalkSpeed(float value) throws IllegalArgumentException {
            
            throw new UnsupportedOperationException("Unimplemented method 'setWalkSpeed'");
        }

        @Override
        public float getFlySpeed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getFlySpeed'");
        }

        @Override
        public float getWalkSpeed() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getWalkSpeed'");
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
        public void setResourcePack(UUID id, String url, byte @Nullable [] hash, @Nullable String prompt,
                boolean force) {
            
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
        public void addResourcePack(UUID id, String url, byte @Nullable [] hash, @Nullable String prompt,
                boolean force) {
            
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
        public void setRotation(float yaw, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setRotation'");
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
        
        // ... otros métodos de Player como no-op
    }
    
    private static class MockVillager implements Villager {
        private final UUID uuid = UUID.randomUUID();
        
        @Override
        public UUID getUniqueId() { return uuid; }
        
        @SuppressWarnings("unused")
        public boolean supportsBreakingDoors() { return true; }
        
        @Override
        public void restock() {
            // No-op para pruebas
        }
        
        @Override
        public void updateDemand() {
            // No-op para pruebas
        }
        
        @Override
        public java.util.Map<java.util.UUID, com.destroystokyo.paper.entity.villager.Reputation> getReputations() {
            return new java.util.HashMap<>(); // Valor por defecto para pruebas
        }
        
        @Override
        public void clearReputations() {
            // No-op para pruebas
        }
        
        @Override
        public void setReputations(java.util.Map<java.util.UUID, com.destroystokyo.paper.entity.villager.Reputation> reputations) {
            // No-op para pruebas
        }
        
        @Override
        public void setReputation(java.util.UUID uuid, com.destroystokyo.paper.entity.villager.Reputation reputation) {
            // No-op para pruebas
        }
        
        @Override
        public com.destroystokyo.paper.entity.villager.Reputation getReputation(java.util.UUID uuid) {
            return null; // No-op para pruebas
        }
        
        @Override
        public org.bukkit.entity.ZombieVillager zombify() {
            return null; // No-op para pruebas
        }
        
        @Override
        public void shakeHead() {
            // No-op para pruebas
        }
        
        @Override
        public void wakeup() {
            // No-op para pruebas
        }
        
        @Override
        public boolean sleep(org.bukkit.Location bedLocation) {
            return false; // No-op para pruebas
        }
        
        @Override
        public void setRestocksToday(int restocksToday) {
            // No-op para pruebas
        }
        
        @Override
        public int getRestocksToday() {
            return 0; // Valor por defecto para pruebas
        }
        
        @Override
        public boolean addTrades(int level) {
            // No-op para pruebas - retorna true por defecto
            return true;
        }
        
        @Override
        public boolean increaseLevel(int amount) {
            // No-op para pruebas - retorna true por defecto
            return true;
        }
        
        @Override
        public void setVillagerExperience(int experience) {
            // No-op para pruebas
        }
        
        @Override
        public int getVillagerExperience() {
            return 0; // Valor por defecto para pruebas
        }
        
        @Override
        public void setVillagerLevel(int level) {
            // No-op para pruebas
        }
        
        @Override
        public int getVillagerLevel() {
            return 1; // Valor por defecto para pruebas
        }
        
        @Override
        public void setVillagerType(org.bukkit.entity.Villager.Type type) {
            // No-op para pruebas
        }
        
        @Override
        public org.bukkit.entity.Villager.Type getVillagerType() {
            return org.bukkit.entity.Villager.Type.PLAINS; // Valor por defecto para pruebas
        }
        
        @Override
        public void setProfession(org.bukkit.entity.Villager.Profession profession) {
            // No-op para pruebas
        }
        
        @Override
        public org.bukkit.entity.Villager.Profession getProfession() {
            return org.bukkit.entity.Villager.Profession.NONE; // Valor por defecto para pruebas
        }
        
        @Override
        public void resetOffers() {
            // No-op para pruebas
        }
        
        @Override
        public org.bukkit.inventory.Inventory getInventory() {
            return null; // Valor por defecto para pruebas
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
        public void setTarget(@org.jetbrains.annotations.Nullable LivingEntity target) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setTarget'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable LivingEntity getTarget() {
            
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
        public @org.jetbrains.annotations.Nullable Sound getAmbientSound() {
            
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
        public @org.jetbrains.annotations.Nullable RayTraceResult rayTraceEntities(int maxDistance,
                boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @NotNull List<Block> getLastTwoTargetBlocks(
                @org.jetbrains.annotations.Nullable Set<Material> transparent, int maxDistance) {
            
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
        public @org.jetbrains.annotations.Nullable PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
            
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
        public double getHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHealth'");
        }

        @Override
        public void setHealth(double health) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHealth'");
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
        public double getMaxHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMaxHealth'");
        }

        @Override
        public void setMaxHealth(double health) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setMaxHealth'");
        }

        @Override
        public void resetMaxHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'resetMaxHealth'");
        }

        @Override
        public @NotNull Location getLocation() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLocation'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable Location getLocation(
                @org.jetbrains.annotations.Nullable Location loc) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLocation'");
        }

        @Override
        public void setVelocity(@NotNull Vector velocity) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setVelocity'");
        }

        @Override
        public @NotNull Vector getVelocity() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getVelocity'");
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
        public boolean isOnGround() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isOnGround'");
        }

        @Override
        public boolean isInWater() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isInWater'");
        }

        @Override
        public @NotNull World getWorld() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getWorld'");
        }

        @Override
        public void setRotation(float yaw, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setRotation'");
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
        public boolean teleport(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Location location, @NotNull TeleportCause cause) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Entity destination) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Entity destination, @NotNull TeleportCause cause) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location loc, @NotNull TeleportCause cause,
                @NotNull TeleportFlag @NotNull... teleportFlags) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleportAsync'");
        }

        @Override
        public @NotNull List<Entity> getNearbyEntities(double x, double y, double z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getNearbyEntities'");
        }

        @Override
        public int getEntityId() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEntityId'");
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
        public void remove() {
            
            throw new UnsupportedOperationException("Unimplemented method 'remove'");
        }

        @Override
        public boolean isDead() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isDead'");
        }

        @Override
        public boolean isValid() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isValid'");
        }

        @Override
        public @NotNull Server getServer() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getServer'");
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
        public @NotNull EntityType getType() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getType'");
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
        public @NotNull Spigot spigot() {
            
            throw new UnsupportedOperationException("Unimplemented method 'spigot'");
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
        public void sendMessage(@NotNull String message) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public void sendMessage(@NotNull String... messages) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public void sendMessage(@org.jetbrains.annotations.Nullable UUID sender, @NotNull String message) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public void sendMessage(@org.jetbrains.annotations.Nullable UUID sender, @NotNull String... messages) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public @NotNull String getName() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getName'");
        }

        @Override
        public @NotNull Component name() {
            
            throw new UnsupportedOperationException("Unimplemented method 'name'");
        }

        @Override
        public boolean isPermissionSet(@NotNull String name) {
            
            throw new UnsupportedOperationException("Unimplemented method 'isPermissionSet'");
        }

        @Override
        public boolean isPermissionSet(@NotNull Permission perm) {
            
            throw new UnsupportedOperationException("Unimplemented method 'isPermissionSet'");
        }

        @Override
        public boolean hasPermission(@NotNull String name) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPermission'");
        }

        @Override
        public boolean hasPermission(@NotNull Permission perm) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPermission'");
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name,
                boolean value) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin,
                @NotNull String name, boolean value, int ticks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin,
                int ticks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public void removeAttachment(@NotNull PermissionAttachment attachment) {
            
            throw new UnsupportedOperationException("Unimplemented method 'removeAttachment'");
        }

        @Override
        public void recalculatePermissions() {
            
            throw new UnsupportedOperationException("Unimplemented method 'recalculatePermissions'");
        }

        @Override
        public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEffectivePermissions'");
        }

        @Override
        public boolean isOp() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isOp'");
        }

        @Override
        public void setOp(boolean value) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setOp'");
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
        public void setLootTable(@org.jetbrains.annotations.Nullable LootTable table) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setLootTable'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable LootTable getLootTable() {
            
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
        public @org.jetbrains.annotations.Nullable HumanEntity getTrader() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getTrader'");
        }
    }
    
    private static class MockZombieVillager implements ZombieVillager {
        private final UUID uuid = UUID.randomUUID();
        
        @Override
        public UUID getUniqueId() { return uuid; }
        
        @Override
        public void setConversionTime(int time, boolean flag) {
            // No-op para pruebas
        }
        
        @Override
        public void setConversionTime(int time) {
            // No-op para pruebas
        }
        
        @Override
        public int getConversionTime() {
            return 0; // Valor por defecto para pruebas
        }
        
        @Override
        public boolean isConverting() {
            return false; // Valor por defecto para pruebas
        }
        
        @Override
        public void setConversionPlayer(org.bukkit.OfflinePlayer player) {
            // No-op para pruebas
        }
        
        @Override
        public org.bukkit.OfflinePlayer getConversionPlayer() {
            return null; // No-op para pruebas
        }
        
        @Override
        public void setVillagerType(Villager.Type type) {
            // No-op para pruebas
        }
        
        @Override
        public Villager.Type getVillagerType() {
            return Villager.Type.PLAINS; // Valor por defecto para pruebas
        }
        
        @Override
        public Villager.Profession getVillagerProfession() {
            return Villager.Profession.NONE; // Valor por defecto para pruebas
        }
        
        @Override
        public void setVillagerProfession(Villager.Profession profession) {
            // No-op para pruebas
        }
        
        @Override
        public boolean supportsBreakingDoors() {
            return false; // Valor por defecto para pruebas
        }
        
        @Override
        public void setShouldBurnInDay(boolean shouldBurnInDay) {
            // No-op para pruebas
        }
        
        @Override
        public boolean shouldBurnInDay() {
            return true; // Valor por defecto para pruebas
        }
        
        @Override
        public boolean isArmsRaised() {
            return false; // Valor por defecto para pruebas
        }
        
        @Override
        public void setArmsRaised(boolean raised) {
            // No-op para pruebas
        }
        
        @Override
        public void stopDrowning() {
            // No-op para pruebas
        }
        
        @Override
        public void startDrowning(int ticks) {
            // No-op para pruebas
        }
        
        @Override
        public boolean isDrowning() {
            return false; // Valor por defecto para pruebas
        }
        
        @Override
        public void setCanBreakDoors(boolean canBreakDoors) {
            // No-op para pruebas
        }
        
        @Override
        public boolean canBreakDoors() {
            return false; // Valor por defecto para pruebas
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
        public void setBaby(boolean baby) {
            // No-op para pruebas
        }
        
        @Override
        public boolean isBaby() {
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
            return null; // No-op para pruebas
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
        public @org.jetbrains.annotations.Nullable RayTraceResult rayTraceEntities(int maxDistance,
                boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @NotNull List<Block> getLastTwoTargetBlocks(
                @org.jetbrains.annotations.Nullable Set<Material> transparent, int maxDistance) {
            
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
        public @org.jetbrains.annotations.Nullable PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
            
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
        public double getHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHealth'");
        }

        @Override
        public void setHealth(double health) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHealth'");
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
        public double getMaxHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMaxHealth'");
        }

        @Override
        public void setMaxHealth(double health) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setMaxHealth'");
        }

        @Override
        public void resetMaxHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'resetMaxHealth'");
        }

        @Override
        public @NotNull Location getLocation() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLocation'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable Location getLocation(
                @org.jetbrains.annotations.Nullable Location loc) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLocation'");
        }

        @Override
        public void setVelocity(@NotNull Vector velocity) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setVelocity'");
        }

        @Override
        public @NotNull Vector getVelocity() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getVelocity'");
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
        public boolean isOnGround() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isOnGround'");
        }

        @Override
        public boolean isInWater() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isInWater'");
        }

        @Override
        public @NotNull World getWorld() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getWorld'");
        }

        @Override
        public void setRotation(float yaw, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setRotation'");
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
        public boolean teleport(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Location location, @NotNull TeleportCause cause) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Entity destination) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Entity destination, @NotNull TeleportCause cause) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location loc, @NotNull TeleportCause cause,
                @NotNull TeleportFlag @NotNull... teleportFlags) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleportAsync'");
        }

        @Override
        public @NotNull List<Entity> getNearbyEntities(double x, double y, double z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getNearbyEntities'");
        }

        @Override
        public int getEntityId() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEntityId'");
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
        public void remove() {
            
            throw new UnsupportedOperationException("Unimplemented method 'remove'");
        }

        @Override
        public boolean isDead() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isDead'");
        }

        @Override
        public boolean isValid() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isValid'");
        }

        @Override
        public @NotNull Server getServer() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getServer'");
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
        public @NotNull EntityType getType() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getType'");
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
        public @NotNull Spigot spigot() {
            
            throw new UnsupportedOperationException("Unimplemented method 'spigot'");
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
        public void sendMessage(@NotNull String message) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public void sendMessage(@NotNull String... messages) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public void sendMessage(@org.jetbrains.annotations.Nullable UUID sender, @NotNull String message) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public void sendMessage(@org.jetbrains.annotations.Nullable UUID sender, @NotNull String... messages) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public @NotNull String getName() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getName'");
        }

        @Override
        public @NotNull Component name() {
            
            throw new UnsupportedOperationException("Unimplemented method 'name'");
        }

        @Override
        public boolean isPermissionSet(@NotNull String name) {
            
            throw new UnsupportedOperationException("Unimplemented method 'isPermissionSet'");
        }

        @Override
        public boolean isPermissionSet(@NotNull Permission perm) {
            
            throw new UnsupportedOperationException("Unimplemented method 'isPermissionSet'");
        }

        @Override
        public boolean hasPermission(@NotNull String name) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPermission'");
        }

        @Override
        public boolean hasPermission(@NotNull Permission perm) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPermission'");
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name,
                boolean value) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin,
                @NotNull String name, boolean value, int ticks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin,
                int ticks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public void removeAttachment(@NotNull PermissionAttachment attachment) {
            
            throw new UnsupportedOperationException("Unimplemented method 'removeAttachment'");
        }

        @Override
        public void recalculatePermissions() {
            
            throw new UnsupportedOperationException("Unimplemented method 'recalculatePermissions'");
        }

        @Override
        public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEffectivePermissions'");
        }

        @Override
        public boolean isOp() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isOp'");
        }

        @Override
        public void setOp(boolean value) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setOp'");
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
        public void setLootTable(@org.jetbrains.annotations.Nullable LootTable table) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setLootTable'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable LootTable getLootTable() {
            
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
        
        // ... otros métodos como no-op
    }
    
    private static class MockZombie implements Zombie {
        private final UUID uuid = UUID.randomUUID();
        
        @Override
        public UUID getUniqueId() { return uuid; }
        
        @Override
        public int getMaxHeadPitch() {
            return 0; // Valor por defecto para pruebas
        }
        
        @Override
        public int getHeadRotationSpeed() {
            return 10; // Valor por defecto para pruebas
        }
        
        @Override
        public void lookAt(double x, double y, double z, float headYaw, float headPitch) {
            // No-op para pruebas
        }
        
        @Override
        public void lookAt(double x, double y, double z) {
            // No-op para pruebas
        }
        
        @Override
        public void lookAt(org.bukkit.entity.Entity entity, float headYaw, float headPitch) {
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
        public boolean isInDaylight() {
            return false; // Valor por defecto para pruebas
        }
        
        public com.destroystokyo.paper.entity.Pathfinder getPathfinder() {
            return null; // Valor por defecto para pruebas
        }
        
        @Override
        public boolean supportsBreakingDoors() {
            return false; // Valor por defecto para pruebas
        }
        
        @Override
        public int getConversionTime() {
            return -1; // No en conversión para pruebas
        }
        
        @Override
        public void setConversionTime(int time) {
            // No-op para pruebas
        }
        
        @Override
        public boolean isArmsRaised() {
            return false; // Valor por defecto para pruebas
        }
        
        @Override
        public void setArmsRaised(boolean raised) {
            // No-op para pruebas
        }
        
        @Override
        public void setShouldBurnInDay(boolean shouldBurnInDay) {
            // No-op para pruebas
        }
        
        @Override
        public boolean shouldBurnInDay() {
            return true; // Valor por defecto para pruebas
        }
        
        @Override
        public void stopDrowning() {
            // No-op para pruebas
        }
        
        @Override
        public boolean isConverting() {
            return false; // Valor por defecto para pruebas
        }
        
        @Override
        public void startDrowning(int drownedConversionTime) {
            // No-op para pruebas
        }
        
        @Override
        public boolean isDrowning() {
            return false; // Valor por defecto para pruebas
        }
        
        @Override
        public void setCanBreakDoors(boolean canBreakDoors) {
            // No-op para pruebas
        }
        
        @Override
        public Villager.Profession getVillagerProfession() {
            return Villager.Profession.NONE; // Valor por defecto para pruebas
        }
        
        @Override
        public void setVillagerProfession(Villager.Profession profession) {
            // No-op para pruebas
        }
        
        @Override
        public boolean canBreakDoors() {
            return false; // Valor por defecto para pruebas
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
        public void setBaby(boolean flag) {
            // No-op para pruebas
        }
        
        @Override
        public boolean isBaby() {
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
        public org.bukkit.inventory.EntityEquipment getEquipment() {
            return null; // Valor por defecto para pruebas
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
        public @org.jetbrains.annotations.Nullable RayTraceResult rayTraceEntities(int maxDistance,
                boolean ignoreBlocks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'rayTraceEntities'");
        }

        @Override
        public @NotNull List<Block> getLastTwoTargetBlocks(
                @org.jetbrains.annotations.Nullable Set<Material> transparent, int maxDistance) {
            
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
        public @org.jetbrains.annotations.Nullable PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
            
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
        public double getHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getHealth'");
        }

        @Override
        public void setHealth(double health) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setHealth'");
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
        public double getMaxHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getMaxHealth'");
        }

        @Override
        public void setMaxHealth(double health) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setMaxHealth'");
        }

        @Override
        public void resetMaxHealth() {
            
            throw new UnsupportedOperationException("Unimplemented method 'resetMaxHealth'");
        }

        @Override
        public @NotNull Location getLocation() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLocation'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable Location getLocation(
                @org.jetbrains.annotations.Nullable Location loc) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getLocation'");
        }

        @Override
        public void setVelocity(@NotNull Vector velocity) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setVelocity'");
        }

        @Override
        public @NotNull Vector getVelocity() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getVelocity'");
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
        public boolean isOnGround() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isOnGround'");
        }

        @Override
        public boolean isInWater() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isInWater'");
        }

        @Override
        public @NotNull World getWorld() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getWorld'");
        }

        @Override
        public void setRotation(float yaw, float pitch) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setRotation'");
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
        public boolean teleport(@NotNull Location location) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Location location, @NotNull TeleportCause cause) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Entity destination) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public boolean teleport(@NotNull Entity destination, @NotNull TeleportCause cause) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleport'");
        }

        @Override
        public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location loc, @NotNull TeleportCause cause,
                @NotNull TeleportFlag @NotNull... teleportFlags) {
            
            throw new UnsupportedOperationException("Unimplemented method 'teleportAsync'");
        }

        @Override
        public @NotNull List<Entity> getNearbyEntities(double x, double y, double z) {
            
            throw new UnsupportedOperationException("Unimplemented method 'getNearbyEntities'");
        }

        @Override
        public int getEntityId() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEntityId'");
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
        public void remove() {
            
            throw new UnsupportedOperationException("Unimplemented method 'remove'");
        }

        @Override
        public boolean isDead() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isDead'");
        }

        @Override
        public boolean isValid() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isValid'");
        }

        @Override
        public @NotNull Server getServer() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getServer'");
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
        public @NotNull EntityType getType() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getType'");
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
        public @NotNull Spigot spigot() {
            
            throw new UnsupportedOperationException("Unimplemented method 'spigot'");
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
        public void sendMessage(@NotNull String message) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public void sendMessage(@NotNull String... messages) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public void sendMessage(@org.jetbrains.annotations.Nullable UUID sender, @NotNull String message) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public void sendMessage(@org.jetbrains.annotations.Nullable UUID sender, @NotNull String... messages) {
            
            throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
        }

        @Override
        public @NotNull String getName() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getName'");
        }

        @Override
        public @NotNull Component name() {
            
            throw new UnsupportedOperationException("Unimplemented method 'name'");
        }

        @Override
        public boolean isPermissionSet(@NotNull String name) {
            
            throw new UnsupportedOperationException("Unimplemented method 'isPermissionSet'");
        }

        @Override
        public boolean isPermissionSet(@NotNull Permission perm) {
            
            throw new UnsupportedOperationException("Unimplemented method 'isPermissionSet'");
        }

        @Override
        public boolean hasPermission(@NotNull String name) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPermission'");
        }

        @Override
        public boolean hasPermission(@NotNull Permission perm) {
            
            throw new UnsupportedOperationException("Unimplemented method 'hasPermission'");
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name,
                boolean value) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin,
                @NotNull String name, boolean value, int ticks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin,
                int ticks) {
            
            throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
        }

        @Override
        public void removeAttachment(@NotNull PermissionAttachment attachment) {
            
            throw new UnsupportedOperationException("Unimplemented method 'removeAttachment'");
        }

        @Override
        public void recalculatePermissions() {
            
            throw new UnsupportedOperationException("Unimplemented method 'recalculatePermissions'");
        }

        @Override
        public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getEffectivePermissions'");
        }

        @Override
        public boolean isOp() {
            
            throw new UnsupportedOperationException("Unimplemented method 'isOp'");
        }

        @Override
        public void setOp(boolean value) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setOp'");
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
        public void setLootTable(@org.jetbrains.annotations.Nullable LootTable table) {
            
            throw new UnsupportedOperationException("Unimplemented method 'setLootTable'");
        }

        @Override
        public @org.jetbrains.annotations.Nullable LootTable getLootTable() {
            
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
        
        // ... otros métodos como no-op
    }
    
    private static class MockEntityTransformEvent extends EntityTransformEvent {
        private Player player;
        
        public MockEntityTransformEvent(org.bukkit.entity.Entity original, org.bukkit.entity.Entity transformed) {
            super(original, Arrays.asList(transformed), EntityTransformEvent.TransformReason.CURED);
        }
        
        public void setPlayer(Player player) {
            this.player = player;
        }
        
        @SuppressWarnings("unused")
        public Player getPlayer() {
            return player;
        }
        
        @Override
        public boolean isCancelled() { return false; }
        
        @Override
        public void setCancelled(boolean cancelled) {}
    }

    private static class MockHeartlessMain {
        public static HeartlessMain create() {
            HeartlessMain mockPlugin = org.mockito.Mockito.mock(HeartlessMain.class);
            org.mockito.Mockito.when(mockPlugin.getLogger())
                .thenReturn(java.util.logging.Logger.getLogger("MockHeartlessMain"));
            org.mockito.Mockito.when(mockPlugin.getDataFolder())
                .thenReturn(new java.io.File(System.getProperty("java.io.tmpdir"), "heartless-test"));
            return mockPlugin;
        }
    }
    
    private static class MockTimeExpression extends TimeExpression {
        public MockTimeExpression() {
            super("7d"); // Expresión de tiempo por defecto para pruebas
        }
    }
}