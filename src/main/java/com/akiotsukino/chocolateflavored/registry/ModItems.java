package com.akiotsukino.chocolateflavored.registry;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import com.akiotsukino.chocolateflavored.item.CookingPotItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ChocolateFlavored.MOD_ID);

    public static final DeferredItem<BlockItem> OVEN = ITEMS.registerSimpleBlockItem(ModBlocks.OVEN);

    public static final DeferredItem<CookingPotItem> COOKING_POT = ITEMS.register("cooking_pot", () ->
            new CookingPotItem(ModBlocks.COOKING_POT.get(), new Item.Properties().stacksTo(1)));

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
