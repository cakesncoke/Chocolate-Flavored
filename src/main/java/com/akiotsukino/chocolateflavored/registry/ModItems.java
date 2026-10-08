package com.akiotsukino.chocolateflavored.registry;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import com.akiotsukino.chocolateflavored.item.ModToolTiers;
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

    public static final DeferredItem<Item> COPPER_NUGGET = ITEMS.registerSimpleItem("copper_nugget");
    public static final DeferredItem<SwordItem> COPPER_SWORD = ITEMS.register("copper_sword", () ->
            new SwordItem(ModToolTiers.COPPER, new Item.Properties().attributes(
                    SwordItem.createAttributes(ModToolTiers.COPPER, 3, -2.4F))));
    public static final DeferredItem<PickaxeItem> COPPER_PICKAXE = ITEMS.register("copper_pickaxe", () ->
            new PickaxeItem(ModToolTiers.COPPER, new Item.Properties().attributes(
                    PickaxeItem.createAttributes(ModToolTiers.COPPER, 1, -2.8F))));
    public static final DeferredItem<AxeItem> COPPER_AXE = ITEMS.register("copper_axe", () ->
            new AxeItem(ModToolTiers.COPPER, new Item.Properties().attributes(
                    AxeItem.createAttributes(ModToolTiers.COPPER, 7, -3.2F))));
    public static final DeferredItem<ShovelItem> COPPER_SHOVEL = ITEMS.register("copper_shovel", () ->
            new ShovelItem(ModToolTiers.COPPER, new Item.Properties().attributes(
                    ShovelItem.createAttributes(ModToolTiers.COPPER, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> COPPER_HOE = ITEMS.register("copper_hoe", () ->
            new HoeItem(ModToolTiers.COPPER, new Item.Properties().attributes(
                    HoeItem.createAttributes(ModToolTiers.COPPER, -1, -2.0F))));

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
