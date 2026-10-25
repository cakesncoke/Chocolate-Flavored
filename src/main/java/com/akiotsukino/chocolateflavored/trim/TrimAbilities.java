package com.akiotsukino.chocolateflavored.trim;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Stateless queries read the actual equipment, including custom armor; no cached full sets. */
public final class TrimAbilities {
    public static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static final TagKey<Block> NETHER_TERRAIN = blocks("trim/nether_terrain");
    public static final TagKey<Block> DESERT_TERRAIN = blocks("trim/desert_terrain");
    public static final TagKey<Block> STONE_LANDINGS = blocks("trim/stone_landings");
    public static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores"));
    public static final TagKey<Item> BOLT_WEAPONS = items("trim/bolt_weapons"), BOLT_TOOLS = items("trim/bolt_tools");
    public static final TagKey<EntityType<?>> AQUATIC = entities("trim/aquatic"), NETHER_HOSTILES = entities("trim/nether_hostiles"), CAMELS = entities("trim/camels");
    private static TagKey<Block> blocks(String id) { return TagKey.create(Registries.BLOCK, id(id)); }
    private static TagKey<Item> items(String id) { return TagKey.create(Registries.ITEM, id(id)); }
    private static TagKey<EntityType<?>> entities(String id) { return TagKey.create(Registries.ENTITY_TYPE, id(id)); }
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath("chocolateflavored", path); }
    public static String pattern(ItemStack stack) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        return trim == null ? "" : trim.pattern().unwrapKey().map(k -> k.identifier().toString()).orElse("");
    }
    public static boolean has(LivingEntity entity, EquipmentSlot slot, String pattern) {
        if (!(entity instanceof Player)) return false;
        ItemStack stack = entity.getItemBySlot(slot);
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot() == slot && pattern(stack).equals("minecraft:" + pattern);
    }
    public static boolean full(LivingEntity entity, String pattern) {
        for (var slot : ARMOR) if (!has(entity, slot, pattern)) return false;
        return true;
    }
    public static boolean trail(LivingEntity entity, EquipmentSlot slot) {
        return has(entity, slot, "wayfinder") || has(entity, slot, "raiser") || has(entity, slot, "shaper") || has(entity, slot, "host");
    }
    public static boolean trailFull(LivingEntity entity) {
        return full(entity, "wayfinder") || full(entity, "raiser") || full(entity, "shaper") || full(entity, "host");
    }
    public static boolean gold(LivingEntity entity) {
        for (var slot : ARMOR) {
            var stack = entity.getItemBySlot(slot);
            var eq = stack.get(DataComponents.EQUIPPABLE);
            var trim = stack.get(DataComponents.TRIM);
            if (eq != null && eq.slot() == slot && trim != null && trim.material().unwrapKey().map(k -> k.identifier().equals(Identifier.withDefaultNamespace("gold"))).orElse(false)) return true;
        }
        return false;
    }
    public static boolean storm(Player player) { return player.level().dimension() == Level.OVERWORLD && player.level().isThundering(); }
    public static Player rider(Entity entity) {
        if (entity instanceof Player p) return p;
        return entity.getControllingPassenger() instanceof Player p ? p : null;
    }
    public static void modifier(LivingEntity entity, Holder<Attribute> type, String name, double amount, AttributeModifier.Operation op) {
        AttributeInstance instance = entity.getAttribute(type);
        if (instance == null) return;
        Identifier id = id("trim/" + name);
        AttributeModifier old = instance.getModifier(id);
        if (old != null && old.amount() == amount && old.operation() == op) return;
        instance.removeModifier(id);
        if (amount != 0) instance.addTransientModifier(new AttributeModifier(id, amount, op));
    }
    private TrimAbilities() {}
}
