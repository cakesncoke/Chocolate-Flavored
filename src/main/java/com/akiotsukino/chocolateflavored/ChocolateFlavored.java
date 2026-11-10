package com.akiotsukino.chocolateflavored;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * Common (client and dedicated server) entry point.
 * Register future content on {@code modEventBus} here. Client-only setup belongs in a separate
 * {@code @Mod(value = MODID, dist = Dist.CLIENT)} class once it is actually needed.
 */
@Mod(ChocolateFlavored.MODID)
public class ChocolateFlavored {
    public static final String MODID = "chocolateflavored";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChocolateFlavored(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Chocolate Flavored");
    }
}
