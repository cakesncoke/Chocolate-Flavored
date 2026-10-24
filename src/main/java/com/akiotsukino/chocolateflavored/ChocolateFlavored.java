package com.akiotsukino.chocolateflavored;

import org.slf4j.Logger;
import com.akiotsukino.chocolateflavored.cooking.CookingContent;
import com.akiotsukino.chocolateflavored.worldgen.ZombieVillagerSpawnModifier;
import com.akiotsukino.chocolateflavored.lighting.LightingContent;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.gameplay.EquipmentRules;
import com.akiotsukino.chocolateflavored.gameplay.AnvilRules;
import com.akiotsukino.chocolateflavored.gameplay.ItemRestrictions;
import com.akiotsukino.chocolateflavored.gameplay.MiningRules;
import com.akiotsukino.chocolateflavored.loot.ModLoot;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(ChocolateFlavored.MODID)
public class ChocolateFlavored {
    public static final String MODID = "chocolateflavored";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    static {
        TABS.register("chocolate_flavored", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.chocolateflavored"))
                .icon(() -> com.akiotsukino.chocolateflavored.cake.CakeContent.CHOCOLATE.item().get().getDefaultInstance())
                .displayItems((parameters, output) -> ModContent.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                .build());
    }

    public ChocolateFlavored(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Chocolate Flavored");
        com.akiotsukino.chocolateflavored.animal.AnimalContent.register(modEventBus);
        com.akiotsukino.chocolateflavored.mob.sunken.SunkenContent.register(modEventBus);
        com.akiotsukino.chocolateflavored.mob.MobContent.register(modEventBus);
        com.akiotsukino.chocolateflavored.mob.tuff.TuffGolemContent.register(modEventBus);
        LightingContent.register(modEventBus);
        CookingContent.register(modEventBus);
        ZombieVillagerSpawnModifier.register(modEventBus);
        com.akiotsukino.chocolateflavored.content.SpawnEggContent.register(modEventBus);
        com.akiotsukino.chocolateflavored.crop.CropContent.register(modEventBus);
        com.akiotsukino.chocolateflavored.cake.CakeContent.register(modEventBus);
        com.akiotsukino.chocolateflavored.dye.DyedWood.register(modEventBus);
        com.akiotsukino.chocolateflavored.equipment.SteelContent.register(modEventBus);
        ModContent.register(modEventBus);
        ModLoot.register(modEventBus);
        TABS.register(modEventBus);
        modEventBus.addListener(EquipmentRules::modifyComponents);
        modEventBus.addListener(ItemRestrictions::creativeTab);
        NeoForge.EVENT_BUS.addListener(ItemRestrictions::recipes);
        NeoForge.EVENT_BUS.addListener(ItemRestrictions::onJoin);
        NeoForge.EVENT_BUS.addListener(ItemRestrictions::entityTick);
        NeoForge.EVENT_BUS.addListener(ItemRestrictions::playerTick);
        NeoForge.EVENT_BUS.addListener(ItemRestrictions::containerOpened);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, MiningRules::breakBlock);
        NeoForge.EVENT_BUS.addListener(MiningRules::speed);
        NeoForge.EVENT_BUS.addListener(MiningRules::harvest);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, AnvilRules::update);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, AnvilRules::repairBlock);
    }
}
