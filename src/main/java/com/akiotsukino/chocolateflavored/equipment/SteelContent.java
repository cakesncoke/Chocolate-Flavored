package com.akiotsukino.chocolateflavored.equipment;

import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.content.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Material items and primitive hammers; existing iron equipment keeps its vanilla identity. */
public final class SteelContent {
    public static final DeferredItem<Item> STEEL_INGOT = material("steel_ingot");
    public static final DeferredItem<Item> STEEL_NUGGET = material("steel_nugget");
    public static final DeferredItem<Item> STEEL_PLATE = material("steel_plate");
    public static final DeferredItem<Item> DIAMOND_PLATE = material("diamond_plate");
    public static final DeferredItem<Item> TANNED_LEATHER = material("tanned_leather");
    public static final DeferredItem<Item> CUT_TANNED_LEATHER = material("cut_tanned_leather");
    public static final DeferredItem<Item> STRAP = material("strap");
    public static final DeferredItem<Item> PADDING = material("padding");
    public static final DeferredItem<Item> WOOL = material("wool");
    public static final DeferredItem<Item> CREEPER_OYSTERS = material("creeper_oysters");
    public static final DeferredItem<Item> DIAMOND_ARMOR_PLATE = material("diamond_armor_plate");
    public static final DeferredItem<Item> STONE_HAMMER = hammer("stone_hammer", ToolMaterial.STONE, 32, 11, .6F, false);
    public static final DeferredItem<Item> DEEPSLATE_HAMMER = hammer("deepslate_hammer", ToolMaterial.IRON, ToolMaterial.IRON.durability() * 2, 12, .7F, true);
    public static final DeferredBlock<Block> STEEL_BLOCK = ModContent.BLOCKS.registerBlock("steel_block", Block::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).overrideLootTable(Optional.of(ResourceKey.create(
                    Registries.LOOT_TABLE, Identifier.parse("chocolateflavored:blocks/steel_block")))));
    public static final DeferredItem<BlockItem> STEEL_BLOCK_ITEM = ModContent.ITEMS.registerSimpleBlockItem(STEEL_BLOCK);

    private static DeferredItem<Item> material(String id) { return ModContent.ITEMS.registerSimpleItem(id); }
    private static DeferredItem<Item> hammer(String id, ToolMaterial material, int durability, float damage, float speed, boolean pickaxeMining) {
        return ModContent.ITEMS.registerSimpleItem(id, p -> p.durability(durability)
                .repairable(material.repairItems()).enchantable(material.enchantmentValue())
                // Stone retains primitive hand-speed mining; deepslate uses native pickaxe rules.
                .component(DataComponents.TOOL, pickaxeMining ? deepslateTool() : new Tool(List.of(), 1.0F, 0, true))
                .component(DataComponents.WEAPON, new Weapon(1))
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage - 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speed - 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build()));
    }
    private static Tool deepslateTool() {
        var lookup = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        // Converted blocks are explicitly harvestable. Other blocks use the native stone tier.
        return new Tool(List.of(
                Tool.Rule.minesAndDrops(lookup.getOrThrow(ModTags.DEEPSLATE_HAMMER_CONVERSIONS), ToolMaterial.STONE.speed()),
                Tool.Rule.deniesDrops(lookup.getOrThrow(BlockTags.INCORRECT_FOR_STONE_TOOL)),
                Tool.Rule.minesAndDrops(lookup.getOrThrow(BlockTags.MINEABLE_WITH_PICKAXE), ToolMaterial.STONE.speed())), 1.0F, 1, true);
    }
    public static void register(IEventBus bus) {
        // Old saved flint stacks load as stone hammers, with their existing component patch intact.
        // The obsolete item is not registered or shown in creative tabs.
        ModContent.ITEMS.addAlias(Identifier.parse("chocolateflavored:knapped_flint"), STONE_HAMMER.getId());
        ModContent.ITEMS.addAlias(Identifier.parse("chocolateflavored:iron_hammer"), DEEPSLATE_HAMMER.getId());
    }
    private SteelContent() {}
}
