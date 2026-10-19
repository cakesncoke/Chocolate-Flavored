package com.akiotsukino.chocolateflavored.gameplay;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ItemRestrictions {
    public static final Set<String> REMOVED = Set.of(
            "wooden_pickaxe", "wooden_axe", "wooden_shovel", "wooden_hoe",
            "stone_sword", "stone_pickaxe", "stone_shovel", "stone_hoe",
            "wooden_spear", "stone_spear", "copper_spear",
            "copper_helmet", "copper_chestplate", "copper_leggings", "copper_boots",
            "copper_horse_armor", "copper_nautilus_armor");
    private static final Set<String> GOLD_EQUIPMENT = Set.of("golden_sword", "golden_pickaxe", "golden_shovel",
            "golden_axe", "golden_hoe", "golden_spear", "golden_helmet", "golden_chestplate", "golden_leggings",
            "golden_boots", "golden_horse_armor", "golden_nautilus_armor");

    public static boolean removed(ItemStack stack) {
        return !stack.isEmpty() && removed(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
    private static boolean removed(Identifier id) {
        return id.getNamespace().equals("minecraft") && REMOVED.contains(id.getPath());
    }
    public static boolean forbiddenTrade(MerchantOffer offer) {
        return removed(offer.getResult()) || removed(offer.getCostA()) || removed(offer.getCostB());
    }
    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        event.removeIf(ItemRestrictions::removed, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
    public static void recipes(ModifyRecipeJsonsEvent event) {
        // Filter outputs, not IDs: this also catches datapack recipes with different filenames.
        // No recipes are added; gold smelting/recycling recipes are retained.
        event.getRecipeJsons().entrySet().removeIf(entry -> {
            if (!entry.getValue().isJsonObject()) return false;
            JsonObject recipe = entry.getValue().getAsJsonObject();
            JsonElement result = recipe.get("result");
            if (result == null) return false;
            String name = result.isJsonPrimitive() ? result.getAsString()
                    : result.isJsonObject() && result.getAsJsonObject().has("id")
                    ? result.getAsJsonObject().get("id").getAsString() : "";
            Identifier id = Identifier.tryParse(name);
            if (id == null) return false;
            if (removed(id)) return true;
            return id.getNamespace().equals("minecraft") && GOLD_EQUIPMENT.contains(id.getPath())
                    && recipe.has("type") && recipe.get("type").getAsString().startsWith("minecraft:crafting");
        });
    }
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof ItemEntity item && removed(item.getItem())) event.setCanceled(true);
        if (event.getEntity() instanceof LivingEntity living) stripEquipment(living);
    }
    public static void entityTick(EntityTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof ItemEntity item && removed(item.getItem())) item.discard();
        if (event.getEntity() instanceof LivingEntity living) stripEquipment(living);
    }
    public static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        purge(player.getInventory());
        purge(player.getEnderChestInventory());
        if (removed(player.containerMenu.getCarried())) player.containerMenu.setCarried(ItemStack.EMPTY);
    }
    public static void containerOpened(PlayerContainerEvent.Open event) {
        if (event.getEntity().level().isClientSide()) return;
        event.getContainer().slots.forEach(slot -> {
            if (removed(slot.getItem())) slot.set(ItemStack.EMPTY);
        });
    }
    private static void purge(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (removed(container.getItem(i))) container.setItem(i, ItemStack.EMPTY);
        }
    }
    private static void stripEquipment(LivingEntity living) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (removed(living.getItemBySlot(slot))) living.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
    private ItemRestrictions() {}
}
