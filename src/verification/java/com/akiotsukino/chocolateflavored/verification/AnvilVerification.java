package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Exercises real vanilla/NeoForge menu calculation, result pickup and block interaction. */
final class AnvilVerification {
    static void run(ServerLevel level, FakePlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        player.experienceLevel = 0;
        for (Item[] pair : new Item[][]{{Items.IRON_PICKAXE, Items.IRON_INGOT},
                {Items.IRON_CHESTPLATE, Items.IRON_INGOT}, {Items.COPPER_PICKAXE, Items.COPPER_INGOT},
                {Items.DIAMOND_PICKAXE, Items.DIAMOND}, {Items.ELYTRA, Items.PHANTOM_MEMBRANE},
                {ModContent.ROSE_GOLD_PICKAXE.get(), ModContent.ROSE_GOLD_INGOT.get()}}) {
            ItemStack input = new ItemStack(pair[0]);
            input.setDamageValue(input.getMaxDamage() - 1);
            input.set(DataComponents.REPAIR_COST, Integer.MAX_VALUE);
            var menu = menu(player, input, new ItemStack(pair[1], 3));
            check(menu.getCost() == 0 && menu.getSlot(2).mayPickup(player), "Material repair is available at zero levels: " + pair[0]);
            int expected = input.getDamageValue() - (input.getMaxDamage() / 4) * 3;
            var repaired = take(menu, player);
            check(repaired.getDamageValue() == expected && player.experienceLevel == 0, "Vanilla material repair amount, no XP");
            check(menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty(), "Material repair consumes inputs");
        }
        var menu = menu(player, new ItemStack(Items.STICK, 12), ItemStack.EMPTY);
        menu.setItemName("Named sticks");
        check(menu.getCost() == 0 && menu.getSlot(2).mayPickup(player), "Rename is available at zero levels");
        var named = take(menu, player);
        check(named.getCount() == 12 && named.getHoverName().getString().equals("Named sticks") && player.experienceLevel == 0, "Free naming preserves count");
        menu = menu(player, named, ItemStack.EMPTY);
        menu.setItemName("");
        check(menu.getCost() == 0 && !take(menu, player).has(DataComponents.CUSTOM_NAME), "Removing names is free");

        var first = new ItemStack(Items.IRON_PICKAXE); first.setDamageValue(400);
        var second = new ItemStack(Items.IRON_PICKAXE); second.setDamageValue(400);
        menu = menu(player, first, second);
        check(menu.getCost() == 0 && take(menu, player).getDamageValue() < 400, "Combining tools without enchantment gains is free");
        first = new ItemStack(Items.IRON_PICKAXE); first.setDamageValue(400);
        menu = menu(player, first, new ItemStack(Items.IRON_INGOT, 8));
        menu.setItemName("Repaired pick");
        check(menu.getCost() == 0 && take(menu, player).getDamageValue() == 0, "Combined repair and rename is free");
        check(menu.getSlot(1).getItem().getCount() == 4, "Only required repair materials are consumed");

        ItemStack enchanted = new ItemStack(Items.DIAMOND_PICKAXE);
        enchanted.set(DataComponents.REPAIR_COST, Integer.MAX_VALUE);
        for (var key : java.util.List.of(Enchantments.EFFICIENCY, Enchantments.EFFICIENCY, Enchantments.UNBREAKING, Enchantments.MENDING)) {
            menu = menu(player, enchanted, book(level, key, 1));
            menu.setItemName("Enchanted pick");
            player.experienceLevel = 2;
            check(menu.getCost() == 3 && menu.getSlot(2).hasItem() && !menu.getSlot(2).mayPickup(player), "Enchantment needs three levels even without a cap");
            player.experienceLevel = 9;
            enchanted = take(menu, player);
            check(player.experienceLevel == 6, "Repeated enchantment/rename deducts exactly three levels");
            check(menu.getSlot(1).getItem().isEmpty(), "Enchantment consumes book");
        }
        var efficiency = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
        check(EnchantmentHelper.getEnchantmentsForCrafting(enchanted).getLevel(efficiency) == 2, "Vanilla equal-level enchantment upgrades retained");
        menu = menu(player, book(level, Enchantments.EFFICIENCY, 1), book(level, Enchantments.UNBREAKING, 1));
        check(menu.getCost() == 3 && take(menu, player).has(DataComponents.STORED_ENCHANTMENTS), "Combining enchanted books costs three levels total");
        first = new ItemStack(Items.DIAMOND_PICKAXE); first.setDamageValue(400);
        second = new ItemStack(Items.DIAMOND_PICKAXE); second.setDamageValue(400); second.enchant(efficiency, 1);
        menu = menu(player, first, second);
        check(menu.getCost() == 3 && take(menu, player).getDamageValue() < 400, "Tool repair plus enchantment transfer costs three levels");
        enchanted.setDamageValue(200);
        menu = menu(player, enchanted, new ItemStack(Items.DIAMOND));
        menu.setItemName("Free enchanted rename");
        check(menu.getCost() == 0, "Existing enchantments alone do not charge XP");
        player.experienceLevel = 0;
        take(menu, player);
        menu = menu(player, new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.DIRT));
        check(!menu.getSlot(2).hasItem() && !menu.getSlot(2).mayPickup(player), "Invalid inputs remain invalid");
        menu = menu(player, ItemStack.EMPTY, ItemStack.EMPTY);
        check(!menu.getSlot(2).mayPickup(player), "Empty zero-cost result cannot be taken");
        var silk = new ItemStack(Items.DIAMOND_PICKAXE);
        silk.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        menu = menu(player, silk, book(level, Enchantments.FORTUNE, 1));
        check(!menu.getSlot(2).hasItem(), "Incompatible enchantments remain invalid");

        BlockPos pos = new BlockPos(24, 160, 0);
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        player.setPos(24.5, 161, 0.5);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.IRON_INGOT, 3));
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(pos, Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, facing));
            for (var expected : new net.minecraft.world.level.block.Block[]{Blocks.CHIPPED_ANVIL, Blocks.ANVIL}) {
                int before = player.getOffhandItem().getCount();
                var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                player.gameMode.useItemOn(player, level, player.getOffhandItem(), InteractionHand.OFF_HAND, hit);
                check(level.getBlockState(pos).is(expected) && level.getBlockState(pos).getValue(AnvilBlock.FACING) == facing,
                        "Right click repairs one anvil stage and retains facing");
                check(player.getOffhandItem().getCount() == before - 1 && player.experienceLevel == 0, "Anvil repair consumes one ingot, no XP");
            }
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.IRON_INGOT, 3));
        }
        player.setGameMode(GameType.CREATIVE);
        level.setBlockAndUpdate(pos, Blocks.CHIPPED_ANVIL.defaultBlockState());
        player.gameMode.useItemOn(player, level, player.getOffhandItem(), InteractionHand.OFF_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        check(level.getBlockState(pos).is(Blocks.ANVIL) && player.getOffhandItem().getCount() == 3, "Creative repairs do not consume ingots");
        player.setGameMode(GameType.SURVIVAL);
        level.removeBlock(pos, false); level.removeBlock(pos.below(), false);
        ChocolateFlavored.LOGGER.info("ANVIL_VERIFICATION_PASSED: free repairs/naming and pickup at zero XP, fixed repeated enchanting, no cost cap, invalid inputs, material consumption and anvil-stage repair");
    }
    private static AnvilMenu menu(FakePlayer player, ItemStack left, ItemStack right) {
        var menu = new AnvilMenu(0, player.getInventory());
        menu.getSlot(0).set(left.copy()); menu.getSlot(1).set(right.copy());
        menu.createResult();
        return menu;
    }
    private static ItemStack book(ServerLevel level, ResourceKey<Enchantment> key, int value) {
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        book.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), value);
        return book;
    }
    private static ItemStack take(AnvilMenu menu, FakePlayer player) {
        var slot = menu.getSlot(2);
        check(slot.hasItem() && slot.mayPickup(player), "Result can be taken");
        var result = slot.remove(slot.getItem().getCount());
        slot.onTake(player, result);
        return result;
    }
    private static void check(boolean condition, String message) { if (!condition) throw new IllegalStateException("Anvil verification failed: " + message); }
}
