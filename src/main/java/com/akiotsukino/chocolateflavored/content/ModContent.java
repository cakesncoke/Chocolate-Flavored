package com.akiotsukino.chocolateflavored.content;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ChocolateFlavored.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ChocolateFlavored.MODID);
    public static final ResourceKey<TrimMaterial> ROSE_GOLD_TRIM = ResourceKey.create(Registries.TRIM_MATERIAL,
            Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "rose_gold"));
    // Iron's new vanilla durability is 500. Custom materials are deliberately not multiplied again.
    public static final ToolMaterial ROSE_GOLD = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL,
            576, 8.0F, 2.0F, 22, ModTags.ROSE_GOLD_REPAIR);
    public static final ToolMaterial FLINT = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            131, 4.0F, 1.0F, 5, ModTags.FLINT_REPAIR);

    public static final DeferredItem<Item> BREAD_DOUGH = ITEMS.registerSimpleItem("bread_dough");
    public static final DeferredItem<Item> CAKE_SLICE = ITEMS.registerSimpleItem("cake_slice", p -> p.food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(.1F).build()));
    public static final DeferredItem<Item> CHERRY_CAKE_SLICE = ITEMS.registerSimpleItem("cherry_cake_slice", p -> p.food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(.1F).build()));
    public static final DeferredItem<Item> CHOCOLATE_CAKE_SLICE = ITEMS.registerSimpleItem("chocolate_cake_slice", p -> p.food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(.1F).build()));
    public static final DeferredItem<Item> COFFEE_CAKE_SLICE = ITEMS.registerSimpleItem("coffee_cake_slice", p -> p.food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(.1F).build()));
    public static final DeferredItem<Item> POMEGRANATE_CAKE_SLICE = ITEMS.registerSimpleItem("pomegranate_cake_slice", p -> p.food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(.1F).build()));
    public static final DeferredItem<Item> CARBON_IRON = ITEMS.registerSimpleItem("carbon_iron");
    public static final DeferredItem<Item> COCOA_POWDER = ITEMS.registerSimpleItem("cocoa_powder");
    public static final DeferredItem<Item> FLOUR = ITEMS.registerSimpleItem("flour");
    public static final DeferredItem<Item> SALT = ITEMS.registerSimpleItem("salt");
    public static final DeferredItem<Item> UNBAKED_CAKE = ITEMS.registerSimpleItem("unbaked_cake");

    public static final DeferredItem<Item> COAL_DUST = ITEMS.registerSimpleItem("coal_dust");
    public static final DeferredItem<Item> COPPER_ORE_PILE = ITEMS.registerSimpleItem("copper_ore_pile");
    public static final DeferredItem<Item> GOLD_ORE_PILE = ITEMS.registerSimpleItem("gold_ore_pile");
    public static final DeferredItem<Item> IRON_ORE_PILE = ITEMS.registerSimpleItem("iron_ore_pile");
    public static final DeferredItem<Item> STONE_PEBBLE = ITEMS.registerSimpleItem("stone_pebble");
    public static final DeferredItem<Item> DEEPSLATE_PEBBLE = ITEMS.registerSimpleItem("deepslate_pebble");
    public static final DeferredItem<Item> PLANT_FIBER = ITEMS.registerSimpleItem("plant_fiber");
    public static final DeferredItem<Item> PLANT_STRING = ITEMS.registerSimpleItem("plant_string");
    public static final DeferredItem<Item> DIAMOND_INGOT = ITEMS.registerSimpleItem("diamond_ingot");
    public static final DeferredItem<Item> ROSE_GOLD_INGOT = ITEMS.registerSimpleItem("rose_gold_ingot", p -> p.trimMaterial(ROSE_GOLD_TRIM));
    public static final DeferredItem<Item> ROSE_GOLD_NUGGET = ITEMS.registerSimpleItem("rose_gold_nugget");
    public static final DeferredItem<Item> FLINT_KNIFE = knife("flint_knife", FLINT);
    public static final DeferredItem<Item> IRON_KNIFE = knife("iron_knife", ToolMaterial.IRON);
    public static final DeferredItem<Item> ROSE_GOLD_SWORD = ITEMS.registerSimpleItem("rose_gold_sword", p -> p.sword(ROSE_GOLD, 3.0F, -2.4F));
    public static final DeferredItem<Item> ROSE_GOLD_PICKAXE = ITEMS.registerSimpleItem("rose_gold_pickaxe", p -> p.pickaxe(ROSE_GOLD, 1.0F, -2.8F));
    public static final DeferredItem<ShovelItem> ROSE_GOLD_SHOVEL = ITEMS.registerItem("rose_gold_shovel", p -> new ShovelItem(ROSE_GOLD, 1.5F, -3.0F, p));
    public static final DeferredItem<AxeItem> ROSE_GOLD_AXE = ITEMS.registerItem("rose_gold_axe", p -> new AxeItem(ROSE_GOLD, 6.0F, -3.1F, p));
    public static final DeferredItem<HoeItem> ROSE_GOLD_HOE = ITEMS.registerItem("rose_gold_hoe", p -> new HoeItem(ROSE_GOLD, -2.0F, -1.0F, p));
    public static final DeferredItem<SmithingTemplateItem> ROSE_GOLD_UPGRADE = ITEMS.registerItem("rose_gold_upgrade_smithing_template", p -> new SmithingTemplateItem(
            Component.translatable("item.chocolateflavored.rose_gold_upgrade.applies_to"),
            Component.translatable("item.chocolateflavored.rose_gold_upgrade.ingredients"),
            Component.translatable("item.chocolateflavored.rose_gold_upgrade.base_slot"),
            Component.translatable("item.chocolateflavored.rose_gold_upgrade.additions_slot"),
            List.of(Identifier.withDefaultNamespace("container/slot/pickaxe"), Identifier.withDefaultNamespace("container/slot/sword")),
            List.of(Identifier.withDefaultNamespace("container/slot/ingot")), p));
    public static final DeferredBlock<Block> ROSE_GOLD_BLOCK = BLOCKS.registerSimpleBlock("rose_gold_block",
            p -> p.mapColor(MapColor.COLOR_PINK).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> ROSE_GOLD_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ROSE_GOLD_BLOCK);

    private static DeferredItem<Item> knife(String name, ToolMaterial material) {
        // FD: 0.5 + material damage, -2 attack speed, one wear per hit or harvested block.
        return ITEMS.registerSimpleItem(name, p -> p.tool(material, ModTags.KNIFE_MINEABLE, 0.5F, -2.0F, 0.0F)
                .component(DataComponents.WEAPON, new Weapon(1)));
    }
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
    private ModContent() {}
}
