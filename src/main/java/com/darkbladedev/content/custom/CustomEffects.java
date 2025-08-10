package com.darkbladedev.content.custom;
import org.bukkit.entity.Player;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.content.semi_custom.effects.ZombieInfection;
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
            entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).getBaseValue() + amplifier);
            entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).getBaseValue() + amplifier);
            return true;
        }
    };

    public static final MobEffect ZOMBIE_INFECTION = new MobEffect(MobEffectCategory.HARMFUL, 0x00FF00) {
        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public boolean applyEffectTick(ServerLevel level, net.minecraft.world.entity.LivingEntity entity, int amplifier) {
            ZombieInfection infection = new ZombieInfection(HeartlessMain.getInstance());
            if (entity instanceof Player) {
                infection.applyEffect((Player) entity);
            }
            return true;
        }
    };




    public static void register() {
        Registry.register(BuiltInRegistries.MOB_EFFECT, RegistryUtils.createKey("fury"), FURY);
        Registry.register(BuiltInRegistries.MOB_EFFECT, RegistryUtils.createKey("zombie_infection"), ZOMBIE_INFECTION);
    }
}