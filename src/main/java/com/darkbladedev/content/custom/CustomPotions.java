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

    public static Potion ZOMBIE_INFECTION_POTION;
    public static Holder<MobEffect> ZOMBIE_INFECTION_HOLDER;


    public static void register() {
        // Registramos primero el MobEffect
        var furyId = RegistryUtils.createKey("fury");
        FURY_HOLDER = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                furyId,
                CustomEffects.FURY
            );

        var zombieInfectionId = RegistryUtils.createKey("zombie_infection");
        ZOMBIE_INFECTION_HOLDER = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                zombieInfectionId,
                CustomEffects.ZOMBIE_INFECTION
            );
        // Ahora sí, creamos el Potion con el Holder
        FURIOUS_POTION = new Potion("Furious", new MobEffectInstance(FURY_HOLDER, 300, 1));
        ZOMBIE_INFECTION_POTION = new Potion("Zombie Infection", new MobEffectInstance(ZOMBIE_INFECTION_HOLDER, Integer.MAX_VALUE, 1));



        // Y lo registramos
        var potionId = RegistryUtils.createKey("fury_potion");
        net.minecraft.core.Registry.register(BuiltInRegistries.POTION, potionId, FURIOUS_POTION);

        var zombieInfectionPotionId = RegistryUtils.createKey("zombie_infection");
        net.minecraft.core.Registry.register(BuiltInRegistries.POTION, zombieInfectionPotionId, ZOMBIE_INFECTION_POTION);
    }
}
