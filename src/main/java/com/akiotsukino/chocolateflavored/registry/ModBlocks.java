package com.akiotsukino.chocolateflavored.registry;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.block.OvenBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ChocolateFlavored.MOD_ID);

    public static final DeferredBlock<OvenBlock> OVEN = BLOCKS.register("oven", () ->
            new OvenBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS)
                    .lightLevel(state -> state.getValue(OvenBlock.LIT) ? 13 : 0)));

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
