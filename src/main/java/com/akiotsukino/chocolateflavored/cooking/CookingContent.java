package com.akiotsukino.chocolateflavored.cooking;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import java.util.Set;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class CookingContent {
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ChocolateFlavored.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ChocolateFlavored.MODID);
    private static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, ChocolateFlavored.MODID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ChocolateFlavored.MODID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ChocolateFlavored.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> MEAL_TEMPLATE = COMPONENTS.registerComponentType("meal_template", b -> b.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> MEAL_CONTAINER = COMPONENTS.registerComponentType("meal_container", b -> b.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));
    public static final DeferredBlock<OvenBlock> OVEN = ModContent.BLOCKS.registerBlock("oven", OvenBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).overrideLootTable(loot("oven")).strength(3.5F).lightLevel(s -> s.getValue(OvenBlock.LIT) ? 13 : 0));
    public static final DeferredBlock<CookingPotBlock> COOKING_POT = ModContent.BLOCKS.registerBlock("cooking_pot", CookingPotBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON).overrideLootTable(loot("cooking_pot")).strength(0.5F).sound(SoundType.LANTERN).noOcclusion());
    public static final DeferredItem<BlockItem> OVEN_ITEM = ModContent.ITEMS.registerSimpleBlockItem(OVEN);
    public static final DeferredItem<BlockItem> COOKING_POT_ITEM = ModContent.ITEMS.registerItem("cooking_pot", p -> new BlockItem(COOKING_POT.get(), p.stacksTo(1).useBlockDescriptionPrefix()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OvenBlockEntity>> OVEN_ENTITY = ENTITIES.register("oven", () -> new BlockEntityType<>(OvenBlockEntity::new, Set.of(OVEN.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CookingPotBlockEntity>> POT_ENTITY = ENTITIES.register("cooking_pot", () -> new BlockEntityType<>(CookingPotBlockEntity::new, Set.of(COOKING_POT.get())));
    public static final DeferredHolder<MenuType<?>, MenuType<CookingPotMenu>> POT_MENU = MENUS.register("cooking_pot", () -> new MenuType<>(CookingPotMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> STEAM = PARTICLES.register("steam", () -> new SimpleParticleType(false));
    public static final DeferredHolder<SoundEvent, SoundEvent> OVEN_CRACKLE = sound("block.oven.crackle");
    public static final DeferredHolder<SoundEvent, SoundEvent> POT_BOIL = sound("block.cooking_pot.boil");
    public static final DeferredHolder<SoundEvent, SoundEvent> POT_SOUP = sound("block.cooking_pot.boil_soup");
    public static final DeferredHolder<SoundEvent, SoundEvent> TAKE_PORTION = sound("block.food.take_portion");
    private static Optional<ResourceKey<LootTable>> loot(String name) { return Optional.of(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "blocks/" + name))); }
    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, name)));
    }
    public static void register(IEventBus bus) { COMPONENTS.register(bus); ENTITIES.register(bus); MENUS.register(bus); PARTICLES.register(bus); SOUNDS.register(bus); CookingRecipes.register(bus); }
    private CookingContent() {}
}
