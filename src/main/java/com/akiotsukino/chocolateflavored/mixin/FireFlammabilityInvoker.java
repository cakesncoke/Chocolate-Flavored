package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(FireBlock.class)
public interface FireFlammabilityInvoker {
    @Invoker("setFlammable") void chocolateflavored$setFlammable(Block block, int igniteOdds, int burnOdds);
}
