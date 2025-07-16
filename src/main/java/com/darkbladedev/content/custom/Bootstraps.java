package com.darkbladedev.content.custom;

import org.bukkit.inventory.EquipmentSlotGroup;

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
            // Register Congelation Ench.
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
                    
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                    
                );
        }));
    }
}
