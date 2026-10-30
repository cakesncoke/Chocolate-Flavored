package com.akiotsukino.chocolateflavored.dye;

import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.mob.MobContent;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.*;

/** Native oak mechanics, pale wood rendering, and a fixed sixteen-color palette. */
public final class DyedWood {
    public static final Map<DyeColor, Family> FAMILIES = new EnumMap<>(DyeColor.class);
    public static final Map<Block, DyeColor> BLOCK_COLORS = new IdentityHashMap<>();
    public static final List<String> KINDS = List.of("planks", "door", "trapdoor", "stairs", "slab", "pressure_plate", "fence", "fence_gate", "sign", "hanging_sign", "button", "boat", "shelf");
    public static final class Family {
        public final DyeColor color;
        public final Map<String, DeferredBlock<? extends Block>> blocks = new LinkedHashMap<>();
        public final Map<String, DeferredItem<? extends Item>> items = new LinkedHashMap<>();
        public final DeferredHolder<EntityType<?>, EntityType<Boat>> boat;
        Family(DyeColor color) {
            this.color = color;
            add("planks", Blocks.OAK_PLANKS, Block::new);
            add("door", Blocks.OAK_DOOR, p -> new DoorBlock(BlockSetType.OAK, p));
            add("trapdoor", Blocks.OAK_TRAPDOOR, p -> new TrapDoorBlock(BlockSetType.OAK, p));
            add("stairs", Blocks.OAK_STAIRS, p -> new StairBlock(block("planks").defaultBlockState(), p));
            add("slab", Blocks.OAK_SLAB, SlabBlock::new);
            add("pressure_plate", Blocks.OAK_PRESSURE_PLATE, p -> new PressurePlateBlock(BlockSetType.OAK, p));
            add("fence", Blocks.OAK_FENCE, FenceBlock::new);
            add("fence_gate", Blocks.OAK_FENCE_GATE, p -> new FenceGateBlock(WoodType.OAK, p));
            add("button", Blocks.OAK_BUTTON, p -> new ButtonBlock(BlockSetType.OAK, 30, p));
            add("shelf", Blocks.OAK_SHELF, ShelfBlock::new);
            addBlock("sign", Blocks.OAK_SIGN, p -> new StandingSignBlock(WoodType.PALE_OAK, p));
            addBlock("wall_sign", Blocks.OAK_WALL_SIGN, p -> new WallSignBlock(WoodType.PALE_OAK, p));
            addBlock("hanging_sign", Blocks.OAK_HANGING_SIGN, p -> new CeilingHangingSignBlock(WoodType.PALE_OAK, p));
            addBlock("wall_hanging_sign", Blocks.OAK_WALL_HANGING_SIGN, p -> new WallHangingSignBlock(WoodType.PALE_OAK, p));
            items.put("sign", ModContent.ITEMS.registerItem(id("sign"), p -> new SignItem(block("sign"), block("wall_sign"), p.stacksTo(16).useBlockDescriptionPrefix())));
            items.put("hanging_sign", ModContent.ITEMS.registerItem(id("hanging_sign"), p -> new HangingSignItem(block("hanging_sign"), block("wall_hanging_sign"), p.stacksTo(16).useBlockDescriptionPrefix())));
            boat = MobContent.ENTITIES.registerEntityType(id("boat"), (type, level) -> new Boat(type, level, () -> item("boat")), MobCategory.MISC,
                    b -> b.noLootTable().sized(1.375F, .5625F).eyeHeight(.5625F).clientTrackingRange(10));
            items.put("boat", ModContent.ITEMS.registerItem(id("boat"), p -> new BoatItem(boat.get(), p.stacksTo(1))));
            addBlock("water_cauldron", Blocks.WATER_CAULDRON, p -> new LayeredCauldronBlock(Biome.Precipitation.RAIN, CauldronInteractions.WATER, p));
        }
        public String id(String kind) { return "dyed_" + color.getName() + "_" + kind; }
        public Block block(String kind) { return blocks.get(kind).get(); }
        public Item item(String kind) { return items.get(kind).get(); }
        private void add(String kind, Block template, Function<BlockBehaviour.Properties, Block> factory) {
            addBlock(kind, template, factory);
            items.put(kind, ModContent.ITEMS.registerItem(id(kind), p -> kind.equals("door") ? new DoubleHighBlockItem(block(kind), p.useBlockDescriptionPrefix()) : new BlockItem(block(kind), p.useBlockDescriptionPrefix())));
        }
        private void addBlock(String kind, Block template, Function<BlockBehaviour.Properties, Block> factory) {
            String loot = kind.equals("water_cauldron") ? "minecraft:blocks/cauldron" : "chocolateflavored:blocks/" + id(kind.replace("wall_", ""));
            blocks.put(kind, ModContent.BLOCKS.registerBlock(id(kind), factory,
                    () -> BlockBehaviour.Properties.ofFullCopy(template).mapColor(color.getMapColor())
                            .overrideLootTable(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(loot))))));
        }
    }
    static { for (DyeColor color : DyeColor.values()) FAMILIES.put(color, new Family(color)); }
    public static int pastel(DyeColor color) {
        int rgb = color.getTextureDiffuseColor();
        // Mix 45% white into the native dye color; keep all sixteen shades distinct.
        int r = (int)(((rgb >> 16) & 255) * .55 + 255 * .45);
        int g = (int)(((rgb >> 8) & 255) * .55 + 255 * .45);
        int b = (int)((rgb & 255) * .55 + 255 * .45);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
    public static void register(IEventBus bus) {
        bus.addListener(DyedWood::blockEntities);
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e) -> e.enqueueWork(() -> {
            FAMILIES.forEach((color, family) -> family.blocks.values().forEach(b -> BLOCK_COLORS.put(b.get(), color)));
            for (Family f : FAMILIES.values()) for (String kind : List.of("planks", "slab", "stairs", "fence", "fence_gate", "shelf"))
                ((com.akiotsukino.chocolateflavored.mixin.FireFlammabilityInvoker)Blocks.FIRE).chocolateflavored$setFlammable(f.block(kind), kind.equals("shelf") ? 30 : 5, 20);
            DyeingCauldrons.initialize();
        }));
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, DyeingCauldrons::interact);
    }
    private static void blockEntities(BlockEntityTypeAddBlocksEvent e) {
        for (Family f : FAMILIES.values()) {
            e.modify(BlockEntityTypes.SIGN, f.block("sign"), f.block("wall_sign"));
            e.modify(BlockEntityTypes.HANGING_SIGN, f.block("hanging_sign"), f.block("wall_hanging_sign"));
            e.modify(BlockEntityTypes.SHELF, f.block("shelf"));
        }
    }
    private DyedWood() {}
}
