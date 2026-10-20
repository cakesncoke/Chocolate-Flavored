// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from EarthMobs ZombifiedEffect, d5acab9846bd3400ac5f093bfec7439351a1bd61.
package com.akiotsukino.chocolateflavored.mob;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
public final class ZombifiedEffect extends MobEffect {
 public ZombifiedEffect(){super(MobEffectCategory.BENEFICIAL,0x2a5131);}
 @Override public boolean applyEffectTick(ServerLevel level,LivingEntity target,int amplifier){
  if(!target.is(EntityTypeTags.UNDEAD))return false;
  target.hurtServer(level,new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,MobContent.id("zombified")))),2);
  return true;
 }
 @Override public boolean shouldApplyEffectTickThisTick(int ticks,int amplifier){int period=50>>amplifier;return period<=0||ticks%period==0;}
}
