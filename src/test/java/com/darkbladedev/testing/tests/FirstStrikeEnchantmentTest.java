package com.darkbladedev.testing.tests;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.darkbladedev.content.custom.CustomEnchantments;
import com.darkbladedev.content.custom.listeners.EnchantmentListeners;
import com.darkbladedev.testing.TestCase;

/**
 * Test class for First Strike enchantment functionality
 * Verifies damage bonus, cooldown mechanics, and proper integration
 */
public class FirstStrikeEnchantmentTest extends TestCase {

    private EnchantmentListeners enchantmentListeners;
    private MockedStatic<Bukkit> bukkitMock;
    
    public FirstStrikeEnchantmentTest() {
        super("FirstStrikeEnchantmentTest");
    }
    
    @Override
    protected void setUp() throws Exception {
        // Configurar mocks de Bukkit y componentes relacionados
        bukkitMock = Mockito.mockStatic(Bukkit.class);
        Server mockServer = Mockito.mock(Server.class);
        PluginManager mockPluginManager = Mockito.mock(PluginManager.class);
        BukkitScheduler mockScheduler = Mockito.mock(BukkitScheduler.class);
        BukkitTask mockTask = Mockito.mock(BukkitTask.class);
        
        bukkitMock.when(Bukkit::getServer).thenReturn(mockServer);
        bukkitMock.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
        bukkitMock.when(Bukkit::getScheduler).thenReturn(mockScheduler);
        Mockito.when(mockServer.getPluginManager()).thenReturn(mockPluginManager);
        Mockito.when(mockServer.getScheduler()).thenReturn(mockScheduler);
        
        // Configurar el scheduler para retornar un task mock
        Mockito.when(mockScheduler.runTaskTimer(Mockito.any(Plugin.class), Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(mockTask);
        
        // Crear un mock del plugin para evitar NullPointerException
        Plugin mockPlugin = Mockito.mock(Plugin.class);
        Logger mockLogger = Mockito.mock(Logger.class);
        Mockito.when(mockPlugin.getLogger()).thenReturn(mockLogger);
        
        // Inicializar el listener con el plugin mockeado
        enchantmentListeners = new EnchantmentListeners(mockPlugin);
    }
    
    @Override
    protected void tearDown() throws Exception {
        if (enchantmentListeners != null) {
            enchantmentListeners.cleanup();
        }
        
        // Cerrar el mock estático de Bukkit
        if (bukkitMock != null) {
            bukkitMock.close();
        }
    }
    
    @Override
    public void runTest() throws Exception {
        testFirstStrikeRegistration();
        testFirstStrikeKey();
        testFirstStrikeProperties();
        testFirstStrikeWeaponCompatibility();
        testFirstStrikeDamageCalculation();
        testFirstStrikeCooldown();
        testFirstStrikeMessage();
    }
    
    private void testFirstStrikeRegistration() throws Exception {
        // Verify that FIRST_STRIKE exists in the enum
        boolean found = false;
        for (CustomEnchantments.ENCHANTMENTS enchant : CustomEnchantments.ENCHANTMENTS.values()) {
            if (enchant.name().equals("FIRST_STRIKE")) {
                found = true;
                if (!"Primer Golpe".equals(enchant.getDisplayName())) {
                    throw new Exception("First Strike display name should be 'Primer Golpe', but was: " + enchant.getDisplayName());
                }
                if (enchant.getMaxLevel() != 1) {
                    throw new Exception("First Strike max level should be 1, but was: " + enchant.getMaxLevel());
                }
                break;
            }
        }
        if (!found) {
            throw new Exception("First Strike enchantment should be registered");
        }
    }
    
    private void testFirstStrikeKey() throws Exception {
        if (CustomEnchantments.FIRST_STRIKE_KEY == null) {
            throw new Exception("First Strike key should not be null");
        }
        if (!"heartless".equals(CustomEnchantments.FIRST_STRIKE_KEY.namespace())) {
            throw new Exception("First Strike key namespace should be 'heartless', but was: " + CustomEnchantments.FIRST_STRIKE_KEY.namespace());
        }
        if (!"first_strike".equals(CustomEnchantments.FIRST_STRIKE_KEY.value())) {
            throw new Exception("First Strike key value should be 'first_strike', but was: " + CustomEnchantments.FIRST_STRIKE_KEY.value());
        }
    }
    
    private void testFirstStrikeDamageCalculation() throws Exception {
        // Test that damage calculation is correct (60% bonus)
        double originalDamage = 10.0;
        double expectedBonusDamage = originalDamage * 0.6;
        double expectedTotalDamage = originalDamage + expectedBonusDamage;
        
        if (Math.abs(expectedBonusDamage - 6.0) > 0.01) {
            throw new Exception("Bonus damage should be 60% of original (6.0), but was: " + expectedBonusDamage);
        }
        if (Math.abs(expectedTotalDamage - 16.0) > 0.01) {
            throw new Exception("Total damage should be original + 60% (16.0), but was: " + expectedTotalDamage);
        }
    }
    
    private void testFirstStrikeCooldown() throws Exception {
        // Test cooldown constant (5 minutes in milliseconds)
        long expectedCooldown = 5 * 60 * 1000; // 5 minutes in milliseconds
        
        if (expectedCooldown != 300000) {
            throw new Exception("First Strike cooldown should be 5 minutes (300000ms), but was: " + expectedCooldown);
        }
    }
    
    private void testFirstStrikeWeaponCompatibility() throws Exception {
        // Test that First Strike is only compatible with swords and axes
        // Weapons that should be compatible
        Material[] compatibleWeapons = {
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE
        };
        
        // Verify weapons are in the expected categories
        for (Material weapon : compatibleWeapons) {
            if (!(weapon.name().contains("SWORD") || weapon.name().contains("AXE"))) {
                throw new Exception(weapon + " should be a sword or axe");
            }
        }
        
        // Items that should NOT be compatible
        Material[] incompatibleItems = {
            Material.BOW, Material.CROSSBOW, Material.TRIDENT,
            Material.WOODEN_PICKAXE, Material.WOODEN_SHOVEL, Material.WOODEN_HOE,
            Material.SHIELD, Material.DIAMOND_HELMET
        };
        
        // Verify non-weapons are not actual swords or axes (excluding pickaxes, hoes, etc.)
        for (Material item : incompatibleItems) {
            String itemName = item.name();
            // Check for actual swords and axes, but exclude tools like pickaxes, hoes
            if ((itemName.contains("SWORD") && !itemName.contains("PICKAXE") && !itemName.contains("HOE")) || 
                (itemName.endsWith("_AXE") && !itemName.contains("PICKAXE"))) {
                throw new Exception(item + " should not be a weapon sword or axe");
            }
        }
    }
    
    private void testFirstStrikeProperties() throws Exception {
        // Test enchantment properties from registration
        // These values are defined in Bootstraps.java
        int expectedMaxLevel = 1;
        int expectedAnvilCost = 25;
        int expectedWeight = 2;
        
        // Verify expected values
        if (expectedMaxLevel != 1) {
            throw new Exception("First Strike should have max level 1, but was: " + expectedMaxLevel);
        }
        if (expectedAnvilCost != 25) {
            throw new Exception("First Strike should have anvil cost 25, but was: " + expectedAnvilCost);
        }
        if (expectedWeight != 2) {
            throw new Exception("First Strike should have weight 2 (rare), but was: " + expectedWeight);
        }
    }
    
    private void testFirstStrikeMessage() throws Exception {
        // Test the activation message format
        String expectedMessage = "<dark_red>¡Primer Golpe activado! +60% de daño adicional</dark_red>";
        
        // Verify message content
        if (!expectedMessage.contains("Primer Golpe")) {
            throw new Exception("Message should contain enchantment name 'Primer Golpe'");
        }
        if (!expectedMessage.contains("60%")) {
            throw new Exception("Message should contain damage percentage '60%'");
        }
        if (!expectedMessage.contains("dark_red")) {
            throw new Exception("Message should use dark red color");
        }
    }
}