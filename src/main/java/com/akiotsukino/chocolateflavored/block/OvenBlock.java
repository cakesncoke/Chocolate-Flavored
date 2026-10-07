package com.akiotsukino.chocolateflavored.block;

// Shape checks, stove interaction, heat damage and particles adapted from Farmer's Delight.
// Copyright (c) 2020 vectorwing. MIT; see THIRD_PARTY_NOTICES.md and licenses/FarmersDelight-MIT.txt.

import com.akiotsukino.chocolateflavored.block.entity.OvenBlockEntity;
import com.akiotsukino.chocolateflavored.registry.ModBlockEntities;
import com.akiotsukino.chocolateflavored.registry.ModSounds;
import com.akiotsukino.chocolateflavored.registry.ModTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;

public class OvenBlock extends BaseEntityBlock {
    public static final MapCodec<OvenBlock> CODEC = simpleCodec(OvenBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape GRILLING_AREA = Block.box(3, 0, 3, 13, 1, 13);

    public OvenBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new OvenBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.OVEN.get(), level.isClientSide
                ? OvenBlockEntity::particleTick : OvenBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                             Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof OvenBlockEntity oven) || !player.mayBuild()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        boolean damageIgniter = held.is(ModTags.OVEN_IGNITERS_DAMAGE);
        boolean consumeIgniter = held.is(ModTags.OVEN_IGNITERS_CONSUME);
        if (damageIgniter || consumeIgniter) {
            // Always handle tagged igniters here, including failure, so they cannot set fire next to the Oven.
            if (!level.isClientSide && !state.getValue(LIT)) {
                if (oven.ignite()) {
                    level.playSound(null, pos, held.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE,
                            SoundSource.BLOCKS, 1, level.random.nextFloat() * 0.4F + 0.8F);
                    if (!player.getAbilities().instabuild) {
                        if (damageIgniter) held.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                        else held.shrink(1);
                    }
                } else player.displayClientMessage(Component.translatable("message.chocolateflavored.oven.needs_fuel"), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (state.getValue(LIT) && (held.canPerformAction(ItemAbilities.SHOVEL_DIG) || held.is(Tags.Items.BUCKETS_WATER))) {
            if (!level.isClientSide) {
                oven.extinguish();
                level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1, 1);
                if (!player.getAbilities().instabuild) {
                    if (held.is(Tags.Items.BUCKETS_WATER)) {
                        ItemStack remainder = held.getCraftingRemainingItem();
                        held.shrink(1);
                        if (held.isEmpty()) player.setItemInHand(hand, remainder);
                        else giveToPlayer(player, remainder);
                    } else held.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        boolean cookingIngredient = !isTopCovered(level, pos) && oven.findRecipe(held).isPresent();
        // Top face prefers recipes; other faces prefer fuel for items which serve both purposes.
        if (cookingIngredient && hit.getDirection() == Direction.UP) return placeFood(level, pos, player, held, oven);
        if (OvenBlockEntity.fuelTicks(held) > 0) {
            if (!level.isClientSide) {
                int added = oven.addFuel(held, player.isShiftKeyDown(), player.getAbilities().instabuild);
                player.displayClientMessage(Component.translatable(added > 0
                        ? "message.chocolateflavored.oven.fuel_added" : "message.chocolateflavored.oven.fuel_full", oven.getFuel().getCount()), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (cookingIngredient) return placeFood(level, pos, player, held, oven);
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static ItemInteractionResult placeFood(Level level, BlockPos pos, Player player, ItemStack held, OvenBlockEntity oven) {
        if (!level.isClientSide && oven.placeFood(held, player.getAbilities().instabuild)) {
            level.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.5F, 1);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof OvenBlockEntity oven) || !player.mayBuild()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack removed = player.isShiftKeyDown() ? oven.takeFuel() : oven.takeFood();
            giveToPlayer(player, removed);
            if (removed.isEmpty()) player.displayClientMessage(Component.translatable("message.chocolateflavored.oven.status",
                    oven.getFuel().getCount(), (oven.getBurnTime() + 19) / 20), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void giveToPlayer(Player player, ItemStack stack) {
        if (!stack.isEmpty() && !player.getInventory().add(stack)) player.drop(stack, false);
    }

    public static boolean isTopCovered(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        return Shapes.joinIsNotEmpty(GRILLING_AREA, level.getBlockState(above).getShape(level, above), BooleanOp.AND);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(LIT, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, LIT); }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && state.getValue(LIT) && entity instanceof LivingEntity && !entity.isSteppingCarefully()
                && entity.getBoundingBox().intersects(GRILLING_AREA.bounds().move(pos.above()))) {
            entity.hurt(level.damageSources().hotFloor(), 1);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, Mob entity) {
        return state.getValue(LIT) ? PathType.DAMAGE_FIRE : null;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && !level.isClientSide && level.getBlockEntity(pos) instanceof OvenBlockEntity oven) oven.dropContents();
        super.onRemove(state, level, pos, next, moving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        double x = pos.getX() + 0.5, y = pos.getY(), z = pos.getZ() + 0.5;
        if (random.nextInt(10) == 0) level.playLocalSound(x, y, z, ModSounds.OVEN_CRACKLE.get(), SoundSource.BLOCKS, 1, 1, false);
        Direction direction = state.getValue(FACING);
        double side = random.nextDouble() * 0.6 - 0.3;
        double dx = direction.getAxis() == Direction.Axis.X ? direction.getStepX() * 0.52 : side;
        double dy = random.nextDouble() * 6 / 16;
        double dz = direction.getAxis() == Direction.Axis.Z ? direction.getStepZ() * 0.52 : side;
        level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, x + dx, y + dy, z + dz, 0, 0, 0);
    }
}
