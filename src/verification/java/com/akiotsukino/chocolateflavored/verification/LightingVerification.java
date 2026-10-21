package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.lighting.FireStarterItem;
import com.akiotsukino.chocolateflavored.lighting.LightingContent;
import com.akiotsukino.chocolateflavored.lighting.LightingIgnition;
import com.akiotsukino.chocolateflavored.lighting.TorchTimerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

final class LightingVerification {
    static void run(ServerLevel level, FakePlayer player) {
        BlockPos pos = new BlockPos(8, 160, 0);
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        player.setPos(8.5, 161, 0.5);
        player.setYRot(0); player.setXRot(90);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var starter = LightingContent.FIRE_STARTER.get();
        check(new ItemStack(starter).getMaxDamage() == 2, "Fire starter durability must remain two");
        check(starter.getUseDuration(new ItemStack(starter), player) == 100, "Five-second use duration");

        for (var campfire : new net.minecraft.world.level.block.Block[]{Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE}) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(campfire));
            level.removeBlock(pos, false);
            var placement = new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false)));
            BlockState state = campfire.getStateForPlacement(placement);
            check(state != null && state.getValue(CampfireBlock.LIT) == (campfire == Blocks.SOUL_CAMPFIRE), "Normal campfire places unlit; soul campfire retains lit placement: " + campfire);
            if (campfire == Blocks.SOUL_CAMPFIRE) state = state.setValue(CampfireBlock.LIT, false);
            level.setBlockAndUpdate(pos, state);
            ItemStack tool = new ItemStack(starter);
            player.setItemInHand(InteractionHand.MAIN_HAND, tool);
            begin(starter, player, pos);
            for (int remaining = 100; remaining >= 2; remaining--) starter.onUseTick(level, player, tool, remaining);
            check(!level.getBlockState(pos).getValue(CampfireBlock.LIT) && tool.getDamageValue() == 0, "No ignition/wear before 100 ticks");
            starter.onUseTick(level, player, tool, 1);
            starter.finishUsingItem(tool, level, player);
            player.stopUsingItem();
            check(level.getBlockState(pos).getValue(CampfireBlock.LIT) && tool.getDamageValue() == 1, "Completed campfire ignition consumes one use");
            check(level.getBlockEntity(pos) instanceof CampfireBlockEntity, "Campfires retain vanilla permanent-burning block entity");
        }

        level.setBlockAndUpdate(pos, LightingContent.UNLIT_TORCH.get().defaultBlockState());
        ItemStack tool = new ItemStack(starter);
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        begin(starter, player, pos);
        starter.onUseTick(level, player, tool, 100);
        player.releaseUsingItem();
        starter.finishUsingItem(tool, level, player);
        check(level.getBlockState(pos).is(LightingContent.UNLIT_TORCH.get()) && tool.getDamageValue() == 0, "Release cancels without wear");
        begin(starter, player, pos);
        player.setXRot(0);
        starter.onUseTick(level, player, tool, 199);
        starter.finishUsingItem(tool, level, player);
        check(level.getBlockState(pos).is(LightingContent.UNLIT_TORCH.get()) && tool.getDamageValue() == 0, "Looking away cancels without wear");
        player.setXRot(90);
        for (int use = 0; use < 2; use++) {
            level.setBlockAndUpdate(pos, LightingContent.UNLIT_TORCH.get().defaultBlockState());
            begin(starter, player, pos);
            for (int remaining = 100; remaining >= 1; remaining--) starter.onUseTick(level, player, tool, remaining);
            starter.finishUsingItem(tool, level, player);
            player.stopUsingItem();
            check(level.getBlockState(pos).is(LightingContent.LIT_TORCH.get()), "Fire starter lights torch");
            TorchTimerBlockEntity timer = (TorchTimerBlockEntity)level.getBlockEntity(pos);
            check(timer != null && timer.burnUntil() == level.getGameTime() + 24000, "Fresh 20-minute timer on ignition");
        }
        check(tool.isEmpty(), "Fire starter breaks on its second successful ignition");

        for (var block : new net.minecraft.world.level.block.Block[]{LightingContent.UNLIT_TORCH.get(), LightingContent.UNLIT_WALL_TORCH.get(), LightingContent.LIT_TORCH.get(), LightingContent.LIT_WALL_TORCH.get()}) {
            var drops = net.minecraft.world.level.block.Block.getDrops(block.defaultBlockState(), level, pos, null, player, ItemStack.EMPTY);
            check(drops.size() == 1 && drops.getFirst().is(TorchTimerBlockEntity.isLit(block.defaultBlockState()) ? LightingContent.LIT_TORCH_ITEM.get() : LightingContent.UNLIT_TORCH_ITEM.get()), "Broken torch retains lit/unlit identity: " + block);
        }

        // Save/load the real block entity, including its absolute deadline, then expire it
        // through the normal server ticker instead of waiting twenty real minutes in CI.
        for (boolean wall : new boolean[]{false, true}) {
            if (wall) level.setBlockAndUpdate(pos.west(), Blocks.STONE.defaultBlockState());
            BlockState state = wall ? LightingContent.LIT_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST)
                    : LightingContent.LIT_TORCH.get().defaultBlockState();
            level.setBlockAndUpdate(pos, state);
            TorchTimerBlockEntity timer = (TorchTimerBlockEntity)level.getBlockEntity(pos);
            timer.reset();
            var tag = timer.saveWithFullMetadata(level.registryAccess());
            var loaded = (TorchTimerBlockEntity)BlockEntity.loadStatic(pos, state, tag, level.registryAccess());
            check(loaded != null && loaded.burnUntil() == timer.burnUntil(), "Torch deadline survives serialization");
            loaded.setLevel(level);
            TorchTimerBlockEntity.serverTick(level, pos, state, loaded);
            check(level.getBlockState(pos).equals(state), "Torch stays lit before its deadline");
            tag.putLong("BurnUntil", level.getGameTime() - 1);
            loaded = (TorchTimerBlockEntity)BlockEntity.loadStatic(pos, state, tag, level.registryAccess());
            loaded.setLevel(level);
            TorchTimerBlockEntity.serverTick(level, pos, state, loaded);
            BlockState expired = level.getBlockState(pos);
            check(expired.is(wall ? LightingContent.UNLIT_WALL_TORCH.get() : LightingContent.UNLIT_TORCH.get()), "Torch extinguishes at deadline");
            if (wall) check(expired.getValue(WallTorchBlock.FACING) == Direction.EAST, "Wall orientation survives expiry");
            check(LightingIgnition.ignite(level, pos, player), "Extinguished torch can be relit");
            timer = (TorchTimerBlockEntity)level.getBlockEntity(pos);
            check(timer.burnUntil() == level.getGameTime() + 24000, "Relighting resets full timer");
            check(Blocks.TORCH.defaultBlockState().getLightEmission() == level.getBlockState(pos).getLightEmission(), "Lit torch light level");
        }

        for (boolean hanging : new boolean[]{false, true}) {
            if (hanging) level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
            else level.removeBlock(pos.above(), false);
            level.setBlockAndUpdate(pos, LightingContent.UNLIT_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, hanging));
            ItemStack igniter = hanging ? new ItemStack(Items.FIRE_CHARGE, 2) : new ItemStack(Items.FLINT_AND_STEEL);
            player.setItemInHand(InteractionHand.MAIN_HAND, igniter);
            LightingIgnition.useIgniter(igniter, level, pos, player, InteractionHand.MAIN_HAND, hit);
            BlockState lit = level.getBlockState(pos);
            check(lit.is(Blocks.LANTERN) && lit.getValue(LanternBlock.HANGING) == hanging, "Lantern lighting preserves hanging state");
            check(level.getBlockEntity(pos) == null && lit.getLightEmission() == 15, "Lit lantern is permanent vanilla lantern");
            check(hanging ? igniter.getCount() == 1 : igniter.getDamageValue() == 1, "Ignition consumes fire charge or flint-and-steel wear");
        }
        level.removeBlock(pos.above(), false);
        level.setBlockAndUpdate(pos, LightingContent.UNLIT_LANTERN.get().defaultBlockState().setValue(LanternBlock.WATERLOGGED, true));
        check(!LightingIgnition.canIgnite(level.getBlockState(pos)), "Cannot ignite waterlogged lantern");
        LightingInteractionVerification.run(level);
        level.removeBlock(pos, false);
        level.removeBlock(pos.west(), false);
        player.stopUsingItem();
        ChocolateFlavored.LOGGER.info("LIGHTING_VERIFICATION_PASSED: placement, 100-tick hold, cancellation, two uses, timer serialization/expiry/relighting, wall facing and lantern ignition");
    }
    private static void begin(FireStarterItem starter, FakePlayer player, BlockPos pos) {
        player.setXRot(90);
        starter.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
        check(player.isUsingItem(), "Fire starter enters use state");
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Lighting verification failed: " + message);
    }
}
