package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.equipment.SteelContent;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Bundle limits, Nether ice, horse-armor Frost Walker and beacon payments (docs/survival-tweaks.md). */
final class SurvivalTweaksVerification {
    static void run(ServerLevel overworld) {
        bundles();
        netherIce(overworld);
        horseFrostWalker(overworld);
        check(new ItemStack(ModContent.ROSE_GOLD_INGOT.get()).is(ItemTags.BEACON_PAYMENT_ITEMS)
                && new ItemStack(SteelContent.STEEL_INGOT.get()).is(ItemTags.BEACON_PAYMENT_ITEMS),
                "Rose gold and steel ingots are beacon payments");
        ChocolateFlavored.LOGGER.info("SURVIVAL_TWEAKS_VERIFICATION_PASSED: bundle weights and mixing, nested bundles, Nether ice by crying obsidian, horse-armor Frost Walker, beacon payments");
    }

    private static void bundles() {
        var bundle = new BundleContents.Mutable();
        for (int i = 0; i < 4; i++) check(bundle.tryInsert(new ItemStack(Items.DIAMOND_SWORD)) == 1, "Bundle accepts unstackable item " + (i + 1) + " of 4");
        check(bundle.tryInsert(new ItemStack(Items.DIAMOND_SWORD)) == 0, "Bundle holds at most four unstackable items");
        check(bundle.toImmutable().weight().getOrThrow().doubleValue() == 1.0, "Four unstackable items fill a bundle");
        check(bundle.removeOne() != null && bundle.toImmutable().weight().getOrThrow().doubleValue() == 0.75, "Removing one unstackable item frees a quarter");
        check(bundle.tryInsert(new ItemStack(Items.DIRT)) == 0, "Stackable items cannot join unstackable ones");

        var stackables = new BundleContents.Mutable();
        check(stackables.tryInsert(new ItemStack(Items.DIRT, 16)) == 16, "Bundle accepts stackable items");
        check(stackables.tryInsert(new ItemStack(Items.IRON_SWORD)) == 0, "Unstackable items cannot join stackable ones");

        var full = new BundleContents.Mutable();
        check(full.tryInsert(new ItemStack(Items.DIRT, 64)) == 64, "Bundle fills with 64 stackable items");
        var fullBundle = new ItemStack(Items.BUNDLE);
        fullBundle.set(DataComponents.BUNDLE_CONTENTS, full.toImmutable());
        check(new BundleContents.Mutable().tryInsert(fullBundle) == 0, "A full bundle keeps its nested weight and cannot be nested");
    }

    private static void netherIce(ServerLevel overworld) {
        ServerLevel nether = overworld.getServer().getLevel(Level.NETHER);
        check(nether != null, "Nether level available");
        BlockPos pos = new BlockPos(0, 100, 0);
        nether.getChunkAt(pos);
        clear(nether, pos);
        nether.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
        melt(nether, pos);
        check(nether.getBlockState(pos).isAir(), "Nether ice evaporates without crying obsidian");

        for (Direction side : Direction.values()) {
            clear(nether, pos);
            nether.setBlockAndUpdate(pos.relative(side), Blocks.CRYING_OBSIDIAN.defaultBlockState());
            nether.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
            melt(nether, pos);
            check(nether.getBlockState(pos).is(Blocks.WATER), "Nether ice melts to water with crying obsidian " + side);
        }
        clear(nether, pos);

        BlockPos overworldPos = new BlockPos(40, 170, 40);
        overworld.setBlockAndUpdate(overworldPos, Blocks.ICE.defaultBlockState());
        melt(overworld, overworldPos);
        check(overworld.getBlockState(overworldPos).is(Blocks.WATER), "Overworld ice melting is unchanged");
        overworld.setBlockAndUpdate(overworldPos, Blocks.AIR.defaultBlockState());
    }

    private static void horseFrostWalker(ServerLevel level) {
        BlockPos center = new BlockPos(60, 170, 60);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-4, -1, -4), center.offset(4, -1, 4))) level.setBlock(p, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-4, 0, -4), center.offset(4, 2, 4))) level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        var plain = spawnHorse(level, center, new ItemStack(Items.DIAMOND_HORSE_ARMOR));
        check(level.getBlockState(center.below()).is(Blocks.WATER), "Unenchanted horse armor does not freeze water");
        plain.discard();

        var armor = new ItemStack(Items.DIAMOND_HORSE_ARMOR);
        armor.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FROST_WALKER), 1);
        check(armor.is(net.minecraft.tags.TagKey.create(Registries.ITEM, net.minecraft.resources.Identifier.parse("chocolateflavored:frost_walker_enchantable"))),
                "Horse armor is Frost Walker enchantable");
        var horse = spawnHorse(level, center, armor);
        check(level.getBlockState(center.below()).is(Blocks.FROSTED_ICE), "Frost Walker horse armor freezes water when the horse changes block");
        horse.discard();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-4, -1, -4), center.offset(4, -1, 4))) level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static net.minecraft.world.entity.animal.equine.Horse spawnHorse(ServerLevel level, BlockPos center, ItemStack armor) {
        var horse = EntityTypes.HORSE.create(level, EntitySpawnReason.COMMAND);
        check(horse != null, "Horse created");
        horse.setItemSlot(EquipmentSlot.BODY, armor);
        horse.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
        level.addFreshEntity(horse);
        // The vanilla location-changed hook runs from baseTick when the block position changes.
        horse.setOnGround(true);
        horse.tick();
        return horse;
    }

    private static void clear(ServerLevel level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
    }

    private static void melt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        try {
            Method melt = IceBlock.class.getDeclaredMethod("melt", BlockState.class, Level.class, BlockPos.class);
            melt.setAccessible(true);
            melt.invoke(state.getBlock(), state, level, pos);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot invoke IceBlock.melt", e);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Verification failed: " + message);
    }

    private SurvivalTweaksVerification() {}
}
