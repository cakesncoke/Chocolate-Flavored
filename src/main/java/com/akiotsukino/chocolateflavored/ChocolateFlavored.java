package com.akiotsukino.chocolateflavored;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Common (client and dedicated server) entry point.
 * Client-only setup belongs in a separate {@code @Mod(value = MODID, dist = Dist.CLIENT)} class once it is actually needed.
 */
@Mod(ChocolateFlavored.MODID)
public class ChocolateFlavored {
    public static final String MODID = "chocolateflavored";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);

    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
    public static final DeferredHolder<RecipeType<?>, RecipeType<KilnRecipe>> KILN_RECIPE_TYPE = RECIPE_TYPES.register("earthen_kiln",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return MODID + ":earthen_kiln";
                }
            });

    public ChocolateFlavored(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Chocolate Flavored");
        ModContent.BLOCKS.register(modEventBus);
        ModContent.ITEMS.register(modEventBus);
        LOOT_MODIFIERS.register("removed_items", () -> RemovedItemsLootModifier.CODEC);
        LOOT_MODIFIERS.register(modEventBus);
        RECIPE_SERIALIZERS.register("tool_shapeless", () -> ToolShapelessRecipe.SERIALIZER);
        RECIPE_SERIALIZERS.register("earthen_kiln", () -> KilnRecipe.SERIALIZER);
        RECIPE_SERIALIZERS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        ModContent.BLOCK_ENTITIES.register(modEventBus);
        VanillaTweaks.register(modEventBus);
        ToolBehavior.register();
    }
}
