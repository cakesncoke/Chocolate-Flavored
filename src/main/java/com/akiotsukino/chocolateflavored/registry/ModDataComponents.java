package com.akiotsukino.chocolateflavored.registry;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.item.component.ItemStackWrapper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Adapted from Farmer's Delight (MIT); see THIRD_PARTY_NOTICES.md.
public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ChocolateFlavored.MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStackWrapper>> MEAL =
            DATA_COMPONENTS.registerComponentType("meal", builder -> builder.persistent(ItemStackWrapper.CODEC)
                    .networkSynchronized(ItemStackWrapper.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStackWrapper>> CONTAINER =
            DATA_COMPONENTS.registerComponentType("container", builder -> builder.persistent(ItemStackWrapper.CODEC)
                    .networkSynchronized(ItemStackWrapper.STREAM_CODEC).cacheEncoding());
    private ModDataComponents() {}
}
