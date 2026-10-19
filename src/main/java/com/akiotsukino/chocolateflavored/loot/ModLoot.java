package com.akiotsukino.chocolateflavored.loot;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLoot {
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ChocolateFlavored.MODID);
    static { MODIFIERS.register("survival", () -> SurvivalLootModifier.CODEC); }
    public static void register(IEventBus bus) { MODIFIERS.register(bus); }
    private ModLoot() {}
}
