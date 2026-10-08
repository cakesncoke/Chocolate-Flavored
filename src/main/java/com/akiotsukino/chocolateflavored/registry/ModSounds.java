package com.akiotsukino.chocolateflavored.registry;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ChocolateFlavored.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> OVEN_CRACKLE = SOUNDS.register("block.oven.crackle", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "block.oven.crackle")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BLOCK_COOKING_POT_BOIL = SOUNDS.register("block.cooking_pot.boil", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "block.cooking_pot.boil")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BLOCK_COOKING_POT_BOIL_SOUP = SOUNDS.register("block.cooking_pot.boil_soup", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "block.cooking_pot.boil_soup")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BLOCK_FOOD_TAKE_PORTION = SOUNDS.register("block.food.take_portion", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "block.food.take_portion")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SPEAR_ATTACK = SOUNDS.register("item.spear.attack", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "item.spear.attack")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SPEAR_HIT = SOUNDS.register("item.spear.hit", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "item.spear.hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SPEAR_LUNGE_1 = SOUNDS.register("item.spear.lunge_1", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "item.spear.lunge_1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SPEAR_LUNGE_2 = SOUNDS.register("item.spear.lunge_2", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "item.spear.lunge_2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SPEAR_LUNGE_3 = SOUNDS.register("item.spear.lunge_3", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "item.spear.lunge_3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SPEAR_USE = SOUNDS.register("item.spear.use", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "item.spear.use")));

    private ModSounds() {}

    public static void register(IEventBus bus) { SOUNDS.register(bus); }
}
