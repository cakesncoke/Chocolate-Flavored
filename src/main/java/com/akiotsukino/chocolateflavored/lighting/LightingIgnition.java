package com.akiotsukino.chocolateflavored.lighting;

import net.minecraft.core.BlockPos;
import com.akiotsukino.chocolateflavored.cooking.CookingContent;
import com.akiotsukino.chocolateflavored.cooking.MudKilnBlock;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public final class LightingIgnition {
    public static boolean unlitTorch(BlockState state) {
        return state.is(LightingContent.UNLIT_TORCH.get()) || state.is(LightingContent.UNLIT_WALL_TORCH.get());
    }
    public static boolean fireStarterTarget(BlockState state) { return unlitTorch(state) || CampfireBlock.canLight(state) || state.is(CookingContent.MUD_KILN.get()) && !state.getValue(MudKilnBlock.LIT); }
    public static boolean canIgnite(BlockState state) {
        return fireStarterTarget(state) || state.is(LightingContent.UNLIT_LANTERN.get()) && !state.getValue(LanternBlock.WATERLOGGED);
    }
    public static boolean permitted(Player player, Level level, BlockPos pos, Direction face, ItemStack stack) {
        return !player.isSpectator() && player.mayUseItemAt(pos, face, stack) && level.mayInteract(player, pos)
                && player.isWithinBlockInteractionRange(pos, 0.0);
    }
    public static boolean ignite(Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (!canIgnite(state)) return false;
        BlockState lit;
        if (state.is(LightingContent.UNLIT_WALL_TORCH.get())) {
            lit = LightingContent.LIT_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, state.getValue(WallTorchBlock.FACING));
        } else if (state.is(LightingContent.UNLIT_TORCH.get())) lit = LightingContent.LIT_TORCH.get().defaultBlockState();
        else if (state.is(LightingContent.UNLIT_LANTERN.get())) {
            lit = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, state.getValue(LanternBlock.HANGING))
                    .setValue(LanternBlock.WATERLOGGED, state.getValue(LanternBlock.WATERLOGGED));
        } else if (state.is(CookingContent.MUD_KILN.get())) lit = state.setValue(MudKilnBlock.LIT, true);
        else lit = state.setValue(CampfireBlock.LIT, true);
        if (!level.setBlock(pos, lit, Block.UPDATE_ALL)) return false;
        if (level.getBlockEntity(pos) instanceof TorchTimerBlockEntity timer) timer.reset();
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        return true;
    }
    public static InteractionResult useIgniter(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) || !canIgnite(level.getBlockState(pos))) return InteractionResult.PASS;
        if (!permitted(player, level, pos, hit.getDirection(), stack)) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!ignite(level, pos, player)) return InteractionResult.FAIL;
        boolean charge = stack.is(Items.FIRE_CHARGE);
        level.playSound(null, pos, charge ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
        if (charge) stack.consume(1, player);
        else stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
        return InteractionResult.SUCCESS;
    }
    private LightingIgnition() {}
}
