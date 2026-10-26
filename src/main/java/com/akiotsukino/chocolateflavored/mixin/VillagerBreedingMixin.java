package com.akiotsukino.chocolateflavored.mixin;

import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(VillagerMakeLove.class)
abstract class VillagerBreedingMixin {
    @ModifyConstant(method = "breed", constant = @Constant(intValue = 6000), require = 2)
    private int chocolateflavored$parentCooldown(int original) { return 35 * 60 * 20; }
    @ModifyConstant(method = "breed", constant = @Constant(intValue = -24000))
    private int chocolateflavored$newbornAge(int original) { return -35 * 60 * 20; }
}
