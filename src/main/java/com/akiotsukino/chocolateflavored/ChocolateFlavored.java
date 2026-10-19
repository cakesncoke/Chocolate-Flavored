package com.akiotsukino.chocolateflavored;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(ChocolateFlavored.MODID)
public class ChocolateFlavored {
    public static final String MODID = "chocolateflavored";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChocolateFlavored(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Chocolate Flavored");
    }
}
