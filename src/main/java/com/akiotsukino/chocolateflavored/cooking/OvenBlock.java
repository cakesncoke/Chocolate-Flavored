package com.akiotsukino.chocolateflavored.cooking;

import com.akiotsukino.chocolateflavored.lighting.LightingContent;
import com.akiotsukino.chocolateflavored.lighting.LightingIgnition;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class OvenBlock extends BaseEntityBlock {
    public static final MapCodec<OvenBlock> CODEC = simpleCodec(OvenBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public OvenBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false)); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, LIT); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new OvenBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel ? createTickerHelper(type, CookingContent.OVEN_ENTITY.get(), (world, pos, block, oven) -> OvenBlockEntity.tick((ServerLevel)world, pos, block, oven)) : null;
    }
    public static boolean isCovered(Level level, BlockPos pos) {
        var above = level.getBlockState(pos.above());
        return Shapes.joinIsNotEmpty(Block.box(3, 0, 3, 13, 1, 13), above.getShape(level, pos.above()), BooleanOp.AND);
    }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var ignition = LightingIgnition.useIgniter(stack, level, pos, player, hand, hit);
        if (ignition.consumesAction()) return ignition;
        if (state.getValue(LIT) && (stack.is(ItemTags.SHOVELS) || stack.is(Items.WATER_BUCKET))) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(LIT, false), 3);
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                if (stack.is(Items.WATER_BUCKET)) { if (!player.hasInfiniteMaterials()) player.setItemInHand(hand, new ItemStack(Items.BUCKET)); }
                else stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(LightingContent.FIRE_STARTER.get()) || isCovered(level, pos)) return InteractionResult.PASS;
        if (level.isClientSide() && !(stack.getItem() instanceof BlockItem)) return InteractionResult.CONSUME;
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof OvenBlockEntity oven && oven.placeFood(server, player, stack)) {
            level.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.5F, 1);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }
    @Override public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (state.getValue(LIT) && entity instanceof LivingEntity && !entity.isSteppingCarefully()) entity.hurt(level.damageSources().campfire(), 1);
        super.stepOn(level, pos, state, entity);
    }
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        if (random.nextInt(10) == 0) level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, CookingContent.OVEN_CRACKLE.get(), SoundSource.BLOCKS, 0.5F, 1, false);
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5 + facing.getStepX() * 0.52, z = pos.getZ() + 0.5 + facing.getStepZ() * 0.52;
        level.addParticle(ParticleTypes.SMOKE, x, pos.getY() + 0.2, z, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, x, pos.getY() + 0.2, z, 0, 0, 0);
        if (!isCovered(level, pos) && level.getBlockEntity(pos) instanceof OvenBlockEntity oven && oven.items().stream().anyMatch(s -> !s.isEmpty())) level.addParticle(ParticleTypes.SMOKE, pos.getX() + random.nextDouble(), pos.getY() + 1.02, pos.getZ() + random.nextDouble(), 0, 0.003, 0);
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); }
}
