package com.akiotsukino.chocolateflavored;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.IEventBus;

/**
 * Changes to vanilla equipment (stats, innate gold silk touch) and the creative inventory layout of
 * both vanilla removals and Chocolate Flavored additions.
 */
public final class VanillaTweaks {
    /** Items removed from the game: no recipes, loot, trades or creative entries (see data/ and RemovedItemsLootModifier). */
    public static final List<Item> REMOVED = List.of(
            Items.WOODEN_SHOVEL, Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_HOE, Items.WOODEN_SPEAR,
            Items.STONE_SWORD, Items.STONE_SHOVEL, Items.STONE_PICKAXE, Items.STONE_HOE, Items.STONE_SPEAR,
            Items.COPPER_SPEAR, Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS,
            Items.COPPER_HORSE_ARMOR, Items.COPPER_NAUTILUS_ARMOR);

    private static final Set<Item> GOLD_TOOLS = Set.of(
            Items.GOLDEN_SWORD, Items.GOLDEN_SHOVEL, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_HOE, Items.GOLDEN_SPEAR);

    private record Tier(ToolMaterial material, Item sword, Item shovel, Item pickaxe, Item axe, Item hoe, Item spear, float durabilityFactor,
            float miningSpeedBonus) {
        List<Item> all() {
            return List.of(sword, shovel, pickaxe, axe, hoe, spear);
        }
    }

    private static final List<Tier> TIERS = List.of(
            new Tier(ToolMaterial.COPPER, Items.COPPER_SWORD, Items.COPPER_SHOVEL, Items.COPPER_PICKAXE, Items.COPPER_AXE, Items.COPPER_HOE,
                    Items.COPPER_SPEAR, 2.5F, 0.0F),
            new Tier(ToolMaterial.IRON, Items.IRON_SWORD, Items.IRON_SHOVEL, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_HOE,
                    Items.IRON_SPEAR, 2.5F, 0.0F),
            new Tier(ToolMaterial.GOLD, Items.GOLDEN_SWORD, Items.GOLDEN_SHOVEL, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_HOE,
                    Items.GOLDEN_SPEAR, 1.0F, 0.0F),
            new Tier(ToolMaterial.DIAMOND, Items.DIAMOND_SWORD, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_HOE,
                    Items.DIAMOND_SPEAR, 2.0F, 1.0F),
            new Tier(ToolMaterial.NETHERITE, Items.NETHERITE_SWORD, Items.NETHERITE_SHOVEL, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE,
                    Items.NETHERITE_HOE, Items.NETHERITE_SPEAR, 2.0F, 1.0F));

    /** Player armor that gets x2 durability. Gold is excluded; copper armor is removed. */
    private static final List<Item> ARMOR = List.of(
            Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
            Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
            Items.TURTLE_HELMET);

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(VanillaTweaks::modifyComponents);
        modEventBus.addListener(VanillaTweaks::buildCreativeTabs);
        NeoForge.EVENT_BUS.addListener(VanillaTweaks::innateSilkTouch);
        NeoForge.EVENT_BUS.addListener(VanillaTweaks::tooltip);
    }

    // ---- Stats ------------------------------------------------------------------------------

    private static void modifyComponents(ModifyDefaultComponentsEvent event) {
        for (Item armor : ARMOR) {
            scaleDurability(event, armor, 2.0F);
        }

        setDurability(event, Items.WOODEN_SWORD, 16);
        setDurability(event, Items.STONE_AXE, 32);

        scaleDurability(event, Items.TRIDENT, 3.0F);
        scaleDurability(event, Items.FISHING_ROD, 3.0F);
        scaleDurability(event, Items.BOW, 1.5F);
        scaleDurability(event, Items.CROSSBOW, 1.5F);

        for (Tier tier : TIERS) {
            for (Item item : tier.all()) {
                if (tier.durabilityFactor() != 1.0F) {
                    scaleDurability(event, item, tier.durabilityFactor());
                }
                if (tier.miningSpeedBonus() != 0.0F) {
                    event.modify(item, (components, context, it) -> {
                        Tool tool = components.get(DataComponents.TOOL);
                        if (tool != null) {
                            components.set(DataComponents.TOOL, withMiningSpeedBonus(tool, tier.material().speed(), tier.miningSpeedBonus()));
                        }
                    });
                }
            }
            // Hoe attack damage: its pickaxe counterpart's minus 1, plus 0.5. Pickaxes use a 1.0 baseline.
            float hoeDamage = 1.0F + tier.material().attackDamageBonus() - 1.0F + 0.5F;
            event.modify(tier.hoe(), (components, context, it) -> {
                ItemAttributeModifiers modifiers = components.get(DataComponents.ATTRIBUTE_MODIFIERS);
                if (modifiers != null) {
                    components.set(DataComponents.ATTRIBUTE_MODIFIERS, withAttackDamage(modifiers, hoeDamage));
                }
            });
        }
    }

    private static void scaleDurability(ModifyDefaultComponentsEvent event, Item item, float factor) {
        event.modify(item, (components, context, it) -> {
            Integer current = components.get(DataComponents.MAX_DAMAGE);
            if (current != null) {
                components.set(DataComponents.MAX_DAMAGE, Math.round(current * factor));
            }
        });
    }

    private static void setDurability(ModifyDefaultComponentsEvent event, Item item, int durability) {
        event.modify(item, (components, context, it) -> components.set(DataComponents.MAX_DAMAGE, durability));
    }

    /** Raises only the rules that use the material's mining speed (cobweb and similar special rules are untouched). */
    private static Tool withMiningSpeedBonus(Tool tool, float materialSpeed, float bonus) {
        List<Tool.Rule> rules = new ArrayList<>();
        for (Tool.Rule rule : tool.rules()) {
            if (rule.speed().isPresent() && rule.speed().get() == materialSpeed) {
                rules.add(new Tool.Rule(rule.blocks(), Optional.of(materialSpeed + bonus), rule.correctForDrops()));
            } else {
                rules.add(rule);
            }
        }
        return new Tool(rules, tool.defaultMiningSpeed(), tool.damagePerBlock(), tool.canDestroyBlocksInCreative());
    }

    private static ItemAttributeModifiers withAttackDamage(ItemAttributeModifiers modifiers, float damage) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                builder.add(entry.attribute(),
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), entry.slot());
            } else {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
        return builder.build();
    }

    // ---- Gold: innate Silk Touch ------------------------------------------------------------

    private static void innateSilkTouch(GetEnchantmentLevelEvent event) {
        if (event.isTargetting(Enchantments.SILK_TOUCH) && GOLD_TOOLS.contains(event.getStack().typeHolder().value())) {
            event.getHolder(Enchantments.SILK_TOUCH).ifPresent(silkTouch -> event.getEnchantments().upgrade(silkTouch, 1));
        }
    }

    private static void tooltip(ItemTooltipEvent event) {
        if (GOLD_TOOLS.contains(event.getItemStack().getItem())) {
            event.getToolTip().add(Component.translatable("tooltip.chocolateflavored.innate_silk_touch").withStyle(ChatFormatting.GRAY));
        }
    }

    // ---- Creative inventory -----------------------------------------------------------------

    private static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        for (Item removed : REMOVED) {
            event.remove(removed.getDefaultInstance(), TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        var tab = event.getTabKey();
        if (tab == CreativeModeTabs.BUILDING_BLOCKS) {
            after(event, Items.IRON_BLOCK, ModContent.STEEL_BLOCK.asItem());
            after(event, Items.GOLD_BLOCK, ModContent.ROSE_GOLD_BLOCK.asItem());
            List<Item> cobbled = new ArrayList<>();
            for (ModContent.Family family : ModContent.COBBLED_FAMILIES) {
                cobbled.addAll(List.of(family.base().asItem(), family.stairs().asItem(), family.slab().asItem(), family.wall().asItem()));
            }
            after(event, Items.COBBLESTONE_WALL, cobbled.toArray(Item[]::new));
        } else if (tab == CreativeModeTabs.NATURAL_BLOCKS) {
            after(event, Items.GRAVEL, ModContent.GRAVELLY_GRANITE.asItem(), ModContent.GRAVELLY_DIORITE.asItem(),
                    ModContent.GRAVELLY_ANDESITE.asItem(), ModContent.GRAVELLY_BASALT.asItem(), ModContent.GRAVELLY_DEEPSLATE.asItem());
        } else if (tab == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            after(event, Items.STONE_AXE, ModContent.FLINT_KNIFE.get(), ModContent.STONE_HAMMER.get(), ModContent.HEAVY_HAMMER.get());
            after(event, Items.IRON_HOE, ModContent.IRON_KNIFE.get());
            after(event, Items.GOLDEN_HOE, ModContent.ROSE_GOLD_SHOVEL.get(), ModContent.ROSE_GOLD_PICKAXE.get(),
                    ModContent.ROSE_GOLD_AXE.get(), ModContent.ROSE_GOLD_HOE.get());
        } else if (tab == CreativeModeTabs.COMBAT) {
            after(event, Items.GOLDEN_SWORD, ModContent.ROSE_GOLD_SWORD.get());
            after(event, Items.WOODEN_SWORD, ModContent.FLINT_KNIFE.get());
            after(event, Items.IRON_SWORD, ModContent.IRON_KNIFE.get());
            after(event, Items.STONE_AXE, ModContent.STONE_HAMMER.get(), ModContent.HEAVY_HAMMER.get());
            after(event, Items.GOLDEN_AXE, ModContent.ROSE_GOLD_AXE.get());
        } else if (tab == CreativeModeTabs.INGREDIENTS) {
            after(event, Items.IRON_NUGGET, ModContent.STEEL_NUGGET.get());
            after(event, Items.IRON_INGOT, ModContent.IRON_BLOOM.get(), ModContent.STEEL_INGOT.get(), ModContent.STEEL_PLATE.get());
            after(event, Items.GOLD_INGOT, ModContent.ROSE_GOLD_INGOT.get());
            after(event, Items.GOLD_NUGGET, ModContent.ROSE_GOLD_NUGGET.get());
            after(event, Items.DIAMOND, ModContent.DIAMOND_INGOT.get(), ModContent.DIAMOND_PLATE.get());
            after(event, Items.COAL, ModContent.COAL_DUST.get());
            after(event, Items.RAW_COPPER, ModContent.COPPER_ORE_PILE.get());
            after(event, Items.RAW_IRON, ModContent.IRON_ORE_PILE.get());
            after(event, Items.RAW_GOLD, ModContent.GOLD_ORE_PILE.get());
            after(event, Items.FLINT, ModContent.STONE_PEBBLE.get(), ModContent.ANDESITE_PEBBLE.get(), ModContent.DIORITE_PEBBLE.get(),
                    ModContent.GRANITE_PEBBLE.get(), ModContent.BASALT_PEBBLE.get(), ModContent.DEEPSLATE_PEBBLE.get());
            after(event, Items.STICK, ModContent.PLANT_FIBER.get(), ModContent.PLANT_STRING.get());
            after(event, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, ModContent.ROSE_GOLD_UPGRADE_SMITHING_TEMPLATE.get());
            after(event, Items.PHANTOM_MEMBRANE, ModContent.CREEPER_OYSTERS.get());
        }
    }

    /** Inserts {@code items} in order directly after {@code anchor}, or at the end of the tab if the anchor is not shown. */
    private static void after(BuildCreativeModeTabContentsEvent event, Item anchor, Item... items) {
        ItemStack previous = anchor.getDefaultInstance();
        boolean anchored = event.getParentEntries().contains(previous) && event.getSearchEntries().contains(previous);
        for (Item item : items) {
            ItemStack stack = item.getDefaultInstance();
            if (event.getParentEntries().contains(stack) || event.getSearchEntries().contains(stack)) {
                previous = stack;
                continue;
            }
            if (anchored) {
                event.insertAfter(previous, stack, TabVisibility.PARENT_AND_SEARCH_TABS);
            } else {
                event.accept(stack, TabVisibility.PARENT_AND_SEARCH_TABS);
            }
            previous = stack;
        }
    }

    private VanillaTweaks() {
    }
}
