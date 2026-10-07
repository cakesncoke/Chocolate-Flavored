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

    private ModSounds() {}

    public static void register(IEventBus bus) { SOUNDS.register(bus); }
}
