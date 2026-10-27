package com.akiotsukino.chocolateflavored.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;

/** Hearth and Harvest's thornless bush, adapted to native 26.1.2 growth and interaction APIs. */
public final class BlueberryBushBlock extends SweetBerryBushBlock {
    public static final MapCodec<SweetBerryBushBlock> CODEC = simpleCodec(BlueberryBushBlock::new);
    public BlueberryBushBlock(Properties properties) { super(properties); }
    @Override public MapCodec<SweetBerryBushBlock> codec() { return CODEC; }
    @Override protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(CropContent.BLUEBERRIES.get());
    }
    @Override public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) < 3) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            popResource(level, pos, new ItemStack(CropContent.BLUEBERRIES.get(), 2 + server.getRandom().nextInt(2)));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1, .8F + server.getRandom().nextFloat() * .4F);
            var next = state.setValue(AGE, 1);
            level.setBlock(pos, next, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, next));
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
            InsideBlockEffectApplier effects, boolean precise) {
        if (entity instanceof LivingEntity && !entity.is(EntityType.FOX) && !entity.is(EntityType.BEE))
            entity.makeStuckInBlock(state, new Vec3(.8F, .75F, .8F));
    }
}
