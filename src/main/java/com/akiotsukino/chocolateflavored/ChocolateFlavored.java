package com.akiotsukino.chocolateflavored;

import com.akiotsukino.chocolateflavored.animal.AnimalContent;
import com.akiotsukino.chocolateflavored.cake.CakeContent;
import com.akiotsukino.chocolateflavored.content.CreativeOrder;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.content.SpawnEggContent;
import com.akiotsukino.chocolateflavored.cooking.CookingContent;
import com.akiotsukino.chocolateflavored.crop.CropContent;
import com.akiotsukino.chocolateflavored.dye.DyedWood;
import com.akiotsukino.chocolateflavored.echo.EchoLinkPayload;
import com.akiotsukino.chocolateflavored.equipment.SteelContent;
import com.akiotsukino.chocolateflavored.gameplay.AnvilRules;
import com.akiotsukino.chocolateflavored.gameplay.EquipmentRules;
import com.akiotsukino.chocolateflavored.gameplay.ItemRestrictions;
import com.akiotsukino.chocolateflavored.gameplay.MiningRules;
import com.akiotsukino.chocolateflavored.lighting.LightingContent;
import com.akiotsukino.chocolateflavored.loot.ModLoot;
import com.akiotsukino.chocolateflavored.mob.MobContent;
import com.akiotsukino.chocolateflavored.mob.sunken.SunkenContent;
import com.akiotsukino.chocolateflavored.mob.tuff.TuffGolemContent;
import com.akiotsukino.chocolateflavored.trim.TrimBasalt;
import com.akiotsukino.chocolateflavored.trim.TrimOutlinePayload;
import com.akiotsukino.chocolateflavored.worldgen.ZombieVillagerSpawnModifier;
import com.mojang.logging.LogUtils;
import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(ChocolateFlavored.MODID)
public class ChocolateFlavored {
    public static final String MODID = "chocolateflavored";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    static {
        TABS.register("chocolate_flavored", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.chocolateflavored"))
                .icon(() -> ModContent.CHOCOLATE_CAKE_SLICE.get().getDefaultInstance())
                .displayItems(ChocolateFlavored::displayItems)
                .build());
    }

    public ChocolateFlavored(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Chocolate Flavored");

        // Feature content. Several classes add entries to ModContent's shared registers from their
        // static initializers, so each must be touched here, before ModContent.register below.
        AnimalContent.register(modEventBus);
        SunkenContent.register(modEventBus);
        MobContent.register(modEventBus);
        TuffGolemContent.register(modEventBus);
        LightingContent.register(modEventBus);
        CookingContent.register(modEventBus);
        ZombieVillagerSpawnModifier.register(modEventBus);
        SpawnEggContent.register(modEventBus);
        CropContent.register(modEventBus);
        CakeContent.register(modEventBus);
        DyedWood.register(modEventBus);
        SteelContent.register(modEventBus);
        TrimBasalt.register();
        modEventBus.addListener(TrimOutlinePayload::register);
        modEventBus.addListener(EchoLinkPayload::register);
        ModContent.register(modEventBus);
        ModLoot.register(modEventBus);
        TABS.register(modEventBus);

        // Equipment, item restriction, mining and anvil rules.
        modEventBus.addListener(EquipmentRules::modifyComponents);
        NeoForge.EVENT_BUS.addListener(EquipmentRules::protectFromBerryBushes);
        modEventBus.addListener(ItemRestrictions::creativeTab);
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

    /** Mod items grouped by {@link CreativeOrder}, then by display name; Copper Horns are listed once per goat-horn instrument. */
    private static void displayItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        ModContent.ITEMS.getEntries().stream()
                .map(entry -> (Item)entry.get())
                .sorted(Comparator.comparingInt(CreativeOrder::group)
                        .thenComparing(item -> item.getName(item.getDefaultInstance()).getString()))
                .forEach(item -> {
                    if (item == ModContent.COPPER_HORN.get()) {
                        parameters.holders().lookupOrThrow(Registries.INSTRUMENT).listElements()
                                .filter(instrument -> instrument.is(InstrumentTags.GOAT_HORNS))
                                .forEach(instrument -> output.accept(InstrumentItem.create(item, instrument)));
                    } else {
                        output.accept(item);
                    }
                });
    }
}
