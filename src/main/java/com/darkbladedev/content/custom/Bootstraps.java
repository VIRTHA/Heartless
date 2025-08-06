package com.darkbladedev.content.custom;

import org.bukkit.inventory.EquipmentSlotGroup;
import com.darkbladedev.utils.MM;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;


import net.kyori.adventure.text.minimessage.MiniMessage;

public class Bootstraps implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
    
        // Register Enchantments handler
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
                b -> b.maxLevel(1)
                    .anvilCost(15)
                    .activeSlots(EquipmentSlotGroup.MAINHAND)
                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 5))
                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(20, 8))
                    .weight(3)
                    
                    .description(MM.toComponent("<red>TicTac</red>"))
                    
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                );
                
            // Register Adrenaline Enchantment
            event.registry().register(
                EnchantmentKeys.create(CustomEnchantments.ADRENALINE_KEY),
                b -> b.maxLevel(1)
                    .anvilCost(20)
                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 8))
                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 12))
                    .weight(2)
                    
                    .description(MM.toComponent("<gold>Adrenalina</gold>"))
                    
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.CHEST_ARMOR))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.LEG_ARMOR))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.FOOT_ARMOR))
                );

        }));

        
        CustomPotions.register();
    }
}
