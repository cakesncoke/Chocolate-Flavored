package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.lighting.LightingContent;
import com.akiotsukino.chocolateflavored.lighting.LitTorchItem;
import com.akiotsukino.chocolateflavored.lighting.TorchTimerBlockEntity;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

final class LightingInteractionVerification {
    private static final class UsePlayer extends FakePlayer {
        UsePlayer(ServerLevel level) { super(level, new GameProfile(UUID.fromString("522d5a58-f15f-4c1e-a747-100000000001"), "LightingChecks")); }
        void advanceUse() {
            if (!isUsingItem()) return;
            // Same hand-stack refresh and use update as LivingEntity's private updatingUsingItem.
            useItem = getItemInHand(getUsedItemHand());
            super.updateUsingItem(useItem);
        }
    }
    static void run(ServerLevel level) {
        var player = new UsePlayer(level);
        player.setGameMode(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(16, 160, 0);
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        player.setPos(16.5, 161, 0.5); player.setYRot(0); player.setXRot(90);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        for (BlockState unlit : new BlockState[]{LightingContent.UNLIT_TORCH.get().defaultBlockState(),
                Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false),
                Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false),
                com.akiotsukino.chocolateflavored.cooking.CookingContent.MUD_KILN.get().defaultBlockState()}) {
            for (boolean blockClick : new boolean[]{true, false}) {
                level.setBlockAndUpdate(pos, unlit);
                ItemStack starter = new ItemStack(LightingContent.FIRE_STARTER.get());
                player.setItemInHand(InteractionHand.MAIN_HAND, starter);
                var result = blockClick ? player.gameMode.useItemOn(player, level, starter, InteractionHand.MAIN_HAND, hit)
                        : player.gameMode.useItem(player, level, starter, InteractionHand.MAIN_HAND);
                check(result.consumesAction() && player.isUsingItem(), "Actual game-mode right click starts the fire starter");
                player.setItemInHand(InteractionHand.MAIN_HAND, starter.copy());
                for (int i = 0; i < 99; i++) player.advanceUse();
                check(level.getBlockState(pos).equals(unlit), "Actual use lifecycle does not light early");
                player.advanceUse();
                BlockState lit = level.getBlockState(pos);
                check((lit.is(LightingContent.LIT_TORCH.get()) || lit.hasProperty(CampfireBlock.LIT) && lit.getValue(CampfireBlock.LIT))
                                && player.getMainHandItem().getDamageValue() == 1,
                        "Actual use lifecycle completes despite equivalent inventory stack replacement");
                check(!player.isUsingItem(), "Vanilla completes the use action");
            }
        }

        for (boolean wall : new boolean[]{false, true}) {
            if (wall) level.setBlockAndUpdate(pos.west(), Blocks.STONE.defaultBlockState());
            BlockState state = wall ? LightingContent.LIT_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST)
                    : LightingContent.LIT_TORCH.get().defaultBlockState();
            level.setBlockAndUpdate(pos, state);
            var timer = (TorchTimerBlockEntity)level.getBlockEntity(pos);
            timer.reset();
            var tag = timer.saveWithFullMetadata(level.registryAccess());
            tag.putLong("BurnUntil", level.getGameTime() + 21000);
            var half = (TorchTimerBlockEntity)BlockEntity.loadStatic(pos, state, tag, level.registryAccess());
            half.setLevel(level);
            level.setBlockEntity(half);
            var drops = Block.getDrops(state, level, pos, half, player, ItemStack.EMPTY);
            check(drops.size() == 1 && drops.getFirst().is(LightingContent.LIT_TORCH_ITEM.get()), "Broken lit torch remains a lit torch item");
            ItemStack saved = drops.getFirst();
            check(LitTorchItem.remaining(saved) == 21000, "Drop preserves exact remaining fuel");
            check(saved.getItem().isBarVisible(saved) && saved.getItem().getBarWidth(saved) == 7, "Half-fuel durability bar");
            for (int i = 0; i < 400; i++) saved.getItem().inventoryTick(saved, level, player, null);
            check(LitTorchItem.remaining(saved) == 21000, "Inventory does not consume fuel");
            var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            saved = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, saved).getOrThrow()).getOrThrow();
            check(LitTorchItem.remaining(saved) == 21000, "Remaining fuel survives item save/load");
            level.removeBlock(pos, false);
            player.setItemInHand(InteractionHand.MAIN_HAND, saved);
            var support = wall ? pos.west() : pos.below();
            var placeHit = new BlockHitResult(Vec3.atCenterOf(support), wall ? Direction.EAST : Direction.UP, support, false);
            var result = saved.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, placeHit));
            check(result.consumesAction(), "Saved torch can be placed through BlockItem");
            timer = (TorchTimerBlockEntity)level.getBlockEntity(pos);
            check(timer != null && timer.burnUntil() == level.getGameTime() + 21000, "Placing resumes saved timer instead of resetting it");
            if (wall) check(level.getBlockState(pos).getValue(WallTorchBlock.FACING) == Direction.EAST, "Saved wall torch places with correct facing");
        }
        level.removeBlock(pos, false); level.removeBlock(pos.west(), false);
        player.stopUsingItem();
        ChocolateFlavored.LOGGER.info("LIGHTING_INTERACTION_VERIFICATION_PASSED: real right-click/use lifecycle, stack synchronization, fuel drops/save/load/bar and paused inventory/resumed placement");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new IllegalStateException("Lighting interaction verification failed: " + message); }
}
