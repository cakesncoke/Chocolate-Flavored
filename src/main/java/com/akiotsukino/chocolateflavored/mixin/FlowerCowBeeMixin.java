// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from EarthMobs' BeeMixin; retain nectar-driven growth and flower spreading.
package com.akiotsukino.chocolateflavored.mixin;
import com.akiotsukino.chocolateflavored.animal.entity.goal.BeeGlowFlowerMobGoal;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Bee.class)
public abstract class FlowerCowBeeMixin extends Animal {
    protected FlowerCowBeeMixin(EntityType<? extends Animal> type, Level level) { super(type,level); }
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void chocolateflavored$flowers(CallbackInfo ci) { goalSelector.addGoal(0, new BeeGlowFlowerMobGoal((Bee) (Object) this)); }
}
