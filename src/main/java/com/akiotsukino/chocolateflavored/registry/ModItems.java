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

    public static final DeferredItem<Item> BAT_WING = ITEMS.registerSimpleItem("bat_wing");
    public static final DeferredItem<Item> BREAD_DOUGH = ITEMS.registerSimpleItem("bread_dough");
    public static final DeferredItem<Item> COAL_DUST = ITEMS.registerSimpleItem("coal_dust");
    public static final DeferredItem<Item> COCOA_POWDER = ITEMS.registerSimpleItem("cocoa_powder");
    public static final DeferredItem<Item> COOKIE_DOUGH = ITEMS.registerSimpleItem("cookie_dough");
    public static final DeferredItem<Item> CREEPER_OYSTERS = ITEMS.registerSimpleItem("creeper_oysters");
    public static final DeferredItem<Item> CUT_TANNED_LEATHER = ITEMS.registerSimpleItem("cut_tanned_leather");
    public static final DeferredItem<Item> DEEPSLATE_PEBBLE = ITEMS.registerSimpleItem("deepslate_pebble");
    public static final DeferredItem<Item> DIAMOND_INGOT = ITEMS.registerSimpleItem("diamond_ingot");
    public static final DeferredItem<Item> DIRT_PILE = ITEMS.registerSimpleItem("dirt_pile");
    public static final DeferredItem<Item> FLOUR = ITEMS.registerSimpleItem("flour");
    public static final DeferredItem<Item> GRAVEL_PILE = ITEMS.registerSimpleItem("gravel_pile");
    public static final DeferredItem<Item> IRON_ORE_PILE = ITEMS.registerSimpleItem("iron_ore_pile");
    public static final DeferredItem<Item> PADDING = ITEMS.registerSimpleItem("padding");
    public static final DeferredItem<Item> PLANT_FIBER = ITEMS.registerSimpleItem("plant_fiber");
    public static final DeferredItem<Item> PLANT_STRING = ITEMS.registerSimpleItem("plant_string");
    public static final DeferredItem<Item> SALT = ITEMS.registerSimpleItem("salt");
    public static final DeferredItem<Item> SAND_PILE = ITEMS.registerSimpleItem("sand_pile");
    public static final DeferredItem<Item> STONE_PEBBLE = ITEMS.registerSimpleItem("stone_pebble");
    public static final DeferredItem<Item> STRAP = ITEMS.registerSimpleItem("strap");
    public static final DeferredItem<Item> TANNED_LEATHER = ITEMS.registerSimpleItem("tanned_leather");
    public static final DeferredItem<Item> UNBAKED_CAKE = ITEMS.registerSimpleItem("unbaked_cake");
    public static final DeferredItem<Item> UNBAKED_PUMPKIN_PIE = ITEMS.registerSimpleItem("unbaked_pumpkin_pie");
    public static final DeferredItem<Item> UNFIRED_BRICK = ITEMS.registerSimpleItem("unfired_brick");
    public static final DeferredItem<Item> WOOL = ITEMS.registerSimpleItem("wool");
    public static final DeferredItem<Item> RED_SAND_PILE = ITEMS.registerSimpleItem("red_sand_pile");
    public static final DeferredItem<com.akiotsukino.chocolateflavored.item.FlintKnifeItem> FLINT_KNIFE =
            ITEMS.register("flint_knife", com.akiotsukino.chocolateflavored.item.FlintKnifeItem::new);

    public static final java.util.List<DeferredItem<Item>> MATERIALS = java.util.List.of(
            BAT_WING, BREAD_DOUGH, COAL_DUST, COCOA_POWDER, COOKIE_DOUGH, CREEPER_OYSTERS, CUT_TANNED_LEATHER, DEEPSLATE_PEBBLE, DIAMOND_INGOT, DIRT_PILE, FLOUR, GRAVEL_PILE, IRON_ORE_PILE, PADDING, PLANT_FIBER, PLANT_STRING, SALT, SAND_PILE, STONE_PEBBLE, STRAP, TANNED_LEATHER, UNBAKED_CAKE, UNBAKED_PUMPKIN_PIE, UNFIRED_BRICK, WOOL, RED_SAND_PILE);

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
