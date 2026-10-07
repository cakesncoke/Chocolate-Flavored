package com.akiotsukino.chocolateflavored;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import com.akiotsukino.chocolateflavored.registry.ModBlockEntities;
import com.akiotsukino.chocolateflavored.registry.ModBlocks;
import com.akiotsukino.chocolateflavored.registry.ModItems;
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
    }
}
