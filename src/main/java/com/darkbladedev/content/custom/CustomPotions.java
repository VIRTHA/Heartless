package com.darkbladedev.content.custom;

import com.darkbladedev.utils.RegistryUtils;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

public class CustomPotions {
    
    public static Potion FURIOUS_POTION;
    public static Holder<MobEffect> FURY_HOLDER;

    public static void register() {
        // Registramos primero el MobEffect
        var furyId = RegistryUtils.createKey("fury");
        FURY_HOLDER = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                furyId,
                CustomEffects.FURY
            );
        // Ahora sí, creamos el Potion con el Holder
        FURIOUS_POTION = new Potion("Furious", new MobEffectInstance(FURY_HOLDER, 300, 1));

        // Y lo registramos
        var potionId = RegistryUtils.createKey("furious");
        net.minecraft.core.Registry.register(BuiltInRegistries.POTION, potionId, FURIOUS_POTION);
    }
}
