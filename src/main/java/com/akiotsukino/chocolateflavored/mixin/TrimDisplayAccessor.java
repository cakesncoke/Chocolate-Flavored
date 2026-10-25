package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(Display.BlockDisplay.class)
public interface TrimDisplayAccessor { @Invoker("setBlockState") void chocolateflavored$state(BlockState state); }
