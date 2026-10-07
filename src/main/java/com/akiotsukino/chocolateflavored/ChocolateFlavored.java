package com.akiotsukino.chocolateflavored;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import com.akiotsukino.chocolateflavored.registry.ModBlockEntities;
import com.akiotsukino.chocolateflavored.registry.ModBlocks;
import com.akiotsukino.chocolateflavored.registry.ModItems;
import com.akiotsukino.chocolateflavored.registry.ModRecipes;
import com.akiotsukino.chocolateflavored.registry.ModSounds;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ChocolateFlavored.MOD_ID)
public class ChocolateFlavored {
    public static final String MOD_ID = "chocolateflavored";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChocolateFlavored(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModSounds.register(modEventBus);
        modEventBus.addListener(ChocolateFlavored::addCreativeContent);
    }

    private static void addCreativeContent(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) event.accept(ModItems.OVEN.get());
    }
}
