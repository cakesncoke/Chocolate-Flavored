package com.akiotsukino.chocolateflavored.animal;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.animal.entity.*;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

public final class AnimalContent {
    private static final DeferredRegister<net.neoforged.neoforge.attachment.AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ChocolateFlavored.MODID);
    /** Immutable bits: muddy, sheared, flower color and current 40-tick shake animation. */
    public static final DeferredHolder<net.neoforged.neoforge.attachment.AttachmentType<?>, net.neoforged.neoforge.attachment.AttachmentType<Integer>> MUDDY_PIG_STATE = ATTACHMENTS.register("muddy_pig_state", () -> net.neoforged.neoforge.attachment.AttachmentType.builder(() -> 0)
            .serialize(com.mojang.serialization.Codec.INT.fieldOf("state"))
            .sync(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ChocolateFlavored.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<EarthCow>> DAIRY_COW = ENTITIES.registerEntityType("dairy_cow", EarthCow::new, MobCategory.CREATURE, b -> b.sized(.9F, 1.4F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Moobloom>> MOOBLOOM = ENTITIES.registerEntityType("moobloom", Moobloom::new, MobCategory.CREATURE, b -> b.sized(.9F, 1.4F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Moolip>> MOOLIP = ENTITIES.registerEntityType("moolip", Moolip::new, MobCategory.CREATURE, b -> b.sized(.9F, 1.4F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<EarthPig>> PALE_PIG = ENTITIES.registerEntityType("pale_pig", EarthPig::new, MobCategory.CREATURE, b -> b.sized(.9F, .9F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<EarthChicken>> BRONZED_CHICKEN = ENTITIES.registerEntityType("bronzed_chicken", EarthChicken::new, MobCategory.CREATURE, b -> b.sized(.4F, .7F).eyeHeight(.644F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<FancyChicken>> FANCY_CHICKEN = ENTITIES.registerEntityType("fancy_chicken", FancyChicken::new, MobCategory.CREATURE, b -> b.sized(.6F, .85F).eyeHeight(.799F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<EarthSheep>> FUZZY_SHEEP = sheep("fuzzy_sheep");
    public static final DeferredHolder<EntityType<?>, EntityType<EarthSheep>> LONG_NOSE_SHEEP = sheep("long_nose_sheep");
    public static final DeferredHolder<EntityType<?>, EntityType<EarthSheep>> PATCHED_SHEEP = sheep("patched_sheep");
    public static final DeferredHolder<EntityType<?>, EntityType<HornedSheep>> HORNED_SHEEP = ENTITIES.registerEntityType("horned_sheep", HornedSheep::new, MobCategory.CREATURE, b -> b.sized(.9F, 1.3F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<JumboRabbit>> JUMBO_RABBIT = ENTITIES.registerEntityType("jumbo_rabbit", JumboRabbit::new, MobCategory.CREATURE, b -> b.sized(.7F, .9F).eyeHeight(.89F).clientTrackingRange(8));
    public static final DeferredBlock<FlowerBlock> BUTTERCUP = ModContent.BLOCKS.registerBlock("buttercup", p -> new FlowerBlock(MobEffects.ABSORPTION, 30, p), () -> flowerProperties("buttercup"));
    public static final DeferredBlock<FlowerBlock> PINK_DAISY = ModContent.BLOCKS.registerBlock("pink_daisy", p -> new FlowerBlock(MobEffects.REGENERATION, 10, p), () -> flowerProperties("pink_daisy"));
    public static final DeferredItem<BlockItem> BUTTERCUP_ITEM = ModContent.ITEMS.registerSimpleBlockItem(BUTTERCUP);
    public static final DeferredItem<BlockItem> PINK_DAISY_ITEM = ModContent.ITEMS.registerSimpleBlockItem(PINK_DAISY);

    private static BlockBehaviour.Properties flowerProperties(String name) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).overrideLootTable(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "blocks/" + name))));
    }
    private static DeferredHolder<EntityType<?>, EntityType<EarthSheep>> sheep(String name) {
        return ENTITIES.registerEntityType(name, EarthSheep::new, MobCategory.CREATURE, b -> b.sized(.9F, 1.3F).clientTrackingRange(10));
    }
    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(DAIRY_COW.get(), Cow.createAttributes().build());
        event.put(MOOBLOOM.get(), Cow.createAttributes().build());
        event.put(MOOLIP.get(), Cow.createAttributes().build());
        event.put(PALE_PIG.get(), Pig.createAttributes().build());
        event.put(BRONZED_CHICKEN.get(), Chicken.createAttributes().build());
        event.put(FANCY_CHICKEN.get(), Chicken.createAttributes().build());
        event.put(FUZZY_SHEEP.get(), Sheep.createAttributes().build());
        event.put(LONG_NOSE_SHEEP.get(), Sheep.createAttributes().build());
        event.put(PATCHED_SHEEP.get(), Sheep.createAttributes().build());
        event.put(HORNED_SHEEP.get(), HornedSheep.createAttributes().build());
        event.put(JUMBO_RABBIT.get(), JumboRabbit.createAttributes().build());
    }
    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
        ENTITIES.register(bus);
        bus.addListener(AnimalContent::attributes);
        NeoForge.EVENT_BUS.addListener(AnimalSpawns::join);
        NeoForge.EVENT_BUS.addListener(MuddyPigRules::tick);
    }
    private AnimalContent() {}
}
