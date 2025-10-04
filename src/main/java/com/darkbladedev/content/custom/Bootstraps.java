package com.darkbladedev.content.custom;

import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;

import com.darkbladedev.utils.MM;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.util.Objects;

public class Bootstraps implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        
        // Register automatic datapack loading
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY, event -> {
            // Discover and register the heartless datapack from resources
            try {
                URI uri = Objects.requireNonNull(getClass().getResource("/heartless_datapack")).toURI();
                event.registrar().discoverPack(uri, "heartless_datapack");
                context.getLogger().info("Successfully discovered heartless datapack from plugin resources");
            } catch (URISyntaxException | IOException e) {
                context.getLogger().error("Failed to discover heartless datapack: " + e.getMessage());
                throw new RuntimeException("Failed to load heartless datapack", e);
            }
        });

        // Register Tags handler BEFORE enchantment registration
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM).newHandler(event -> {
            context.getLogger().info("Tags preFlatten event - custom tags should be available now");
            // Verificar si el tag personalizado existe
            try {
                @SuppressWarnings("unused")
                TagKey<ItemType> customFoodTag = TagKey.create(RegistryKey.ITEM, Key.key("heartless", "all_food"));
                context.getLogger().info("Custom tag heartless:all_food is available for use");
            } catch (Exception e) {
                context.getLogger().warn("Custom tag heartless:all_food not available: " + e.getMessage());
            }
        }));
    
        // Register Enchantments handler AFTER tags are processed
        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> {
            // Register Acid Resistance Enchantment
            event.registry().register(
                EnchantmentKeys.create(CustomEnchantments.ACID_RESISTANCE_KEY),
                b -> b.maxLevel(1)
                    .anvilCost(10)
                    .activeSlots(EquipmentSlotGroup.MAINHAND)
                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(5, 3))
                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 5))
                    .weight(5)
                    
                    .description(MiniMessage.miniMessage().deserialize(
                        "<aqua>Proteccion contra el acido</aqua>"
                        ))
                    
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.CHEST_ARMOR))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.LEG_ARMOR))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.FOOT_ARMOR))
                    
                );

            // Register Acid Infection Enchantment
            event.registry().register(
                EnchantmentKeys.create(CustomEnchantments.ACID_INFECTION_KEY),
                b -> b.maxLevel(1)
                    .anvilCost(10)
                    .activeSlots(EquipmentSlotGroup.MAINHAND)
                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(5, 3))
                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 5))
                    .weight(5)
                    
                    .description(MiniMessage.miniMessage().deserialize(
                        "<gradient:#e8ff59:#d7fe5e:#c6fc63:#b5fb68:#a4f96d:#93f872:#82f677:#71f47c:#60f381>Contagion</gradient>"
                        ))
                    
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                );
                
            // Register Carve Enchantment
            event.registry().register(
                EnchantmentKeys.create(CustomEnchantments.TICTAC_KEY),
                b -> b.maxLevel(4)
                    .anvilCost(15)
                    .activeSlots(EquipmentSlotGroup.MAINHAND)
                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 5))
                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(20, 8))
                    .weight(3)
                    
                    .description(MM.toComponent("<gradient:#ea2c2c:#e53553:#e03e7a:#dc48a2:#d751c9:#d25af0>TicTac</gradient>"))
                    
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                );
                
            // Register Condimento Enchantment with fallback mechanism
            try {
                TagKey<ItemType> customFoodTag = TagKey.create(RegistryKey.ITEM, Key.key("heartless", "all_food"));
                event.registry().register(
                    EnchantmentKeys.create(CustomEnchantments.CONDIMENT_KEY),
                    b -> b.maxLevel(1)
                        .anvilCost(15)
                        .activeSlots(EquipmentSlotGroup.ANY)
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 5))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 5))
                        .weight(2)
                        .description(MM.toComponent("<gradient:#ff6b35:#f7931e:#ffd700>Condimento</gradient>"))
                        .supportedItems(event.getOrCreateTag(customFoodTag))
                );
                context.getLogger().info("Successfully registered Condimento enchantment with custom tag");
            } catch (Exception e) {
                context.getLogger().warn("Failed to use custom tag heartless:all_food, using fallback: " + e.getMessage());
                // Fallback to existing animal food tags
                event.registry().register(
                    EnchantmentKeys.create(CustomEnchantments.CONDIMENT_KEY),
                    b -> b.maxLevel(1)
                        .anvilCost(15)
                        .activeSlots(EquipmentSlotGroup.ANY)
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 5))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 5))
                        .weight(2)
                        .description(MM.toComponent("<gradient:#ff6b35:#f7931e:#ffd700>Condimento</gradient>"))
                        .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.CHICKEN_FOOD))
                );
                context.getLogger().info("Successfully registered Condimento enchantment with fallback tag");
            }
                
            // Register Adrenaline Enchantment
            event.registry().register(
                EnchantmentKeys.create(CustomEnchantments.ADRENALINE_KEY),
                b -> b.maxLevel(1)
                    .anvilCost(20)
                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 8))
                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 12))
                    .weight(2)
                    
                    .description(MM.toComponent("<gradient:#71f65b:#6bf26d:#65ee7f:#60e992:#5ae5a4:#54e1b6:#4eddc8:#49d8db:#43d4ed:#3dd0ff>Adrenalina</gradient>"))
                    
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.CHEST_ARMOR))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.LEG_ARMOR))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.FOOT_ARMOR))
                );
                
            // Register First Strike Enchantment
            event.registry().register(
                EnchantmentKeys.create(CustomEnchantments.FIRST_STRIKE_KEY),
                b -> b.maxLevel(1)
                    .anvilCost(25)
                    .activeSlots(EquipmentSlotGroup.MAINHAND)
                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(20, 10))
                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 15))
                    .weight(2)
                    
                    .description(MM.toComponent("<gradient:#ff3d3d:#f63c3c:#ee3a3a:#e53939:#dd3737:#d43636:#cb3434:#c33333:#ba3131:#b23030:#a92e2e>Primer Golpe</gradient>"))
                    
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                );
                
            // Register Head Hunter Enchantment
            event.registry().register(
                EnchantmentKeys.create(CustomEnchantments.HEAD_DROPPER_KEY),
                b -> b.maxLevel(1)
                    .anvilCost(30)
                    .activeSlots(EquipmentSlotGroup.MAINHAND)
                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 12))
                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(35, 18))
                    .weight(1)
                    
                    .description(MM.toComponent("<gradient:#8e7cf9:#8f82f7:#8f88f6:#908ff4:#9095f3:#919bf1:#92a1ef:#92a7ee:#93aeec:#93b4eb:#94bae9:#95c0e7:#95c6e6:#96cde4:#96d3e3:#97d9e1>Cazador de Cabezas</gradient>"))
                    
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                );

        }));

        
    }
}
