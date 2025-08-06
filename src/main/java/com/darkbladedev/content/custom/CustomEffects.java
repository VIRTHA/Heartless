package com.darkbladedev.content.custom;
import com.darkbladedev.utils.RegistryUtils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class CustomEffects {

    public static final MobEffect FURY = new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public boolean applyEffectTick(ServerLevel level, net.minecraft.world.entity.LivingEntity entity, int amplifier) {
            entity.setHealth(entity.getHealth() + 1);
            return true;
        }
    };

    public static void register() {
        Registry.register(BuiltInRegistries.MOB_EFFECT, RegistryUtils.createKey("fury"), FURY);
    }
}