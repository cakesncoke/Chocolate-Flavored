package com.akiotsukino.chocolateflavored.item;

import com.akiotsukino.chocolateflavored.registry.ModTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.state.BlockState;

public final class CopperPickaxeItem extends PickaxeItem {
    public CopperPickaxeItem(Properties properties) { super(ModToolTiers.COPPER, properties); }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        // Zero progress on both client and server; BreakEvent additionally prevents forced harvesting.
        return state.is(ModTags.Blocks.DEEPSLATE_ORES) ? 0.0F : super.getDestroySpeed(stack, state);
    }
}
