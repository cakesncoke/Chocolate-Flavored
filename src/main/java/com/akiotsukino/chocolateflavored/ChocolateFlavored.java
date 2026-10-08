package com.akiotsukino.chocolateflavored;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import com.akiotsukino.chocolateflavored.registry.ModBlockEntities;
import com.akiotsukino.chocolateflavored.registry.ModBlocks;
import com.akiotsukino.chocolateflavored.registry.ModItems;
import com.akiotsukino.chocolateflavored.registry.ModRecipes;
import com.akiotsukino.chocolateflavored.registry.ModSounds;
import com.akiotsukino.chocolateflavored.registry.ModMenuTypes;
import com.akiotsukino.chocolateflavored.registry.ModDataComponents;
import com.akiotsukino.chocolateflavored.registry.ModParticleTypes;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ChocolateFlavored.MOD_ID)
public class ChocolateFlavored {
    public static final String MOD_ID = "chocolateflavored";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChocolateFlavored(IEventBus modEventBus, ModContainer container) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,
                com.akiotsukino.chocolateflavored.event.PrimitiveMining::onBreak);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,
                com.akiotsukino.chocolateflavored.event.PrimitiveMining::onDrops);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModSounds.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModParticleTypes.PARTICLE_TYPES.register(modEventBus);
        container.registerConfig(ModConfig.Type.CLIENT, Configuration.CLIENT_SPEC);
        modEventBus.addListener(ChocolateFlavored::addCreativeContent);
    }

    private static void addCreativeContent(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(ModItems.OVEN.get());
            event.accept(ModItems.COOKING_POT.get());
        } else if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(ModItems.FLINT_KNIFE.get());
            event.accept(ModItems.COPPER_SHOVEL.get());
            event.accept(ModItems.COPPER_PICKAXE.get());
            event.accept(ModItems.COPPER_AXE.get());
            event.accept(ModItems.COPPER_HOE.get());
        } else if (event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
            event.accept(ModItems.FLINT_KNIFE.get());
            event.accept(ModItems.COPPER_SWORD.get());
            event.accept(ModItems.COPPER_AXE.get());
        } else if (event.getTabKey().equals(CreativeModeTabs.INGREDIENTS)) {
            event.accept(ModItems.COPPER_NUGGET.get());
            ModItems.MATERIALS.forEach(item -> event.accept(item.get()));
        }
    }
}
