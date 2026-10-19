package com.akiotsukino.chocolateflavored.lighting;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import java.util.Set;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LightingContent {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ChocolateFlavored.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TORCH_FUEL = COMPONENTS.registerComponentType("torch_burn_ticks",
            builder -> builder.persistent(Codec.intRange(0, (int)TorchTimerBlockEntity.BURN_TICKS)).networkSynchronized(ByteBufCodecs.VAR_INT));
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ChocolateFlavored.MODID);
    public static final DeferredBlock<TimedTorchBlock> UNLIT_TORCH = ModContent.BLOCKS.registerBlock("unlit_torch", p -> new TimedTorchBlock(false, p),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH).overrideLootTable(loot("unlit_torch")).lightLevel(s -> 0));
    public static final DeferredBlock<TimedWallTorchBlock> UNLIT_WALL_TORCH = ModContent.BLOCKS.registerBlock("unlit_wall_torch", p -> new TimedWallTorchBlock(false, p),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WALL_TORCH).overrideLootTable(loot("unlit_wall_torch")).lightLevel(s -> 0));
    public static final DeferredBlock<TimedTorchBlock> LIT_TORCH = ModContent.BLOCKS.registerBlock("lit_torch", p -> new TimedTorchBlock(true, p),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH).overrideLootTable(loot("lit_torch")));
    public static final DeferredBlock<TimedWallTorchBlock> LIT_WALL_TORCH = ModContent.BLOCKS.registerBlock("lit_wall_torch", p -> new TimedWallTorchBlock(true, p),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WALL_TORCH).overrideLootTable(loot("lit_wall_torch")));
    public static final DeferredBlock<UnlitLanternBlock> UNLIT_LANTERN = ModContent.BLOCKS.registerBlock("unlit_lantern", UnlitLanternBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).overrideLootTable(loot("unlit_lantern")).lightLevel(s -> 0));
    public static final DeferredItem<StandingAndWallBlockItem> UNLIT_TORCH_ITEM = ModContent.ITEMS.registerItem("unlit_torch",
            p -> new StandingAndWallBlockItem(UNLIT_TORCH.get(), UNLIT_WALL_TORCH.get(), Direction.DOWN, p.useBlockDescriptionPrefix()));
    public static final DeferredItem<LitTorchItem> LIT_TORCH_ITEM = ModContent.ITEMS.registerItem("lit_torch", p -> new LitTorchItem(p.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> UNLIT_LANTERN_ITEM = ModContent.ITEMS.registerSimpleBlockItem(UNLIT_LANTERN);
    public static final DeferredItem<FireStarterItem> FIRE_STARTER = ModContent.ITEMS.registerItem("fire_starter", FireStarterItem::new, p -> p.durability(2));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TorchTimerBlockEntity>> TORCH_TIMER = ENTITIES.register("torch_timer",
            () -> new BlockEntityType<>(TorchTimerBlockEntity::new, Set.of(UNLIT_TORCH.get(), UNLIT_WALL_TORCH.get(), LIT_TORCH.get(), LIT_WALL_TORCH.get())));
    private static Optional<ResourceKey<LootTable>> loot(String block) {
        return Optional.of(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "blocks/" + block)));
    }
    public static void register(IEventBus bus) { COMPONENTS.register(bus); ENTITIES.register(bus); }
    private LightingContent() {}
}
