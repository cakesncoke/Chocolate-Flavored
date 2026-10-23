package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.crop.*;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.FakePlayer;

public final class CropVerification {
    public static void run(ServerLevel level, FakePlayer player) {
        BlockPos pos = new BlockPos(160, 160, 0);
        for (int x = -16; x <= 16; x += 16) for (int z = -16; z <= 16; z += 16) level.getChunk(pos.offset(x, 0, z));
        for (var entry : CropContent.FIELDS) {
            var crop = entry.get();
            var seed = CropContent.seedFor(crop);
            check(!seed.components().has(DataComponents.FOOD) && !seed.components().has(DataComponents.CONSUMABLE), "Seed/produce has no food components: " + seed);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(seed, 2));
            var result = seed.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false)));
            check(result.consumesAction() && level.getBlockState(pos).is(crop), "Plant " + seed);
            check(player.getMainHandItem().getCount() == 1, "Planting consumes one seed");
            var immature = Block.getDrops(crop.defaultBlockState(), level, pos, null, player, ItemStack.EMPTY);
            check(immature.stream().anyMatch(s -> s.is(seed)), "Immature crop returns seed");
            for (int i = 0; i < 16 && !crop.isMaxAge(level.getBlockState(pos)); i++) crop.performBonemeal(level, level.getRandom(), pos, level.getBlockState(pos));
            check(crop.isMaxAge(level.getBlockState(pos)), "Bonemeal matures " + crop);
            check(!crop.isValidBonemealTarget(level, pos, level.getBlockState(pos)), "Mature crop rejects bonemeal");
            var mature = Block.getDrops(level.getBlockState(pos), level, pos, null, player, ItemStack.EMPTY);
            check(!mature.isEmpty(), "Mature crop loot " + crop);
            level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            check(level.getBlockState(pos).isAir(), "Crop removed when support disappears");
            level.setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7));
            level.setBlockAndUpdate(pos, crop.defaultBlockState());
            for (int i = 0; i < 1000 && !crop.isMaxAge(level.getBlockState(pos)); i++) level.getBlockState(pos).randomTick(level, pos, level.getRandom());
            check(crop.isMaxAge(level.getBlockState(pos)), "Natural random-tick growth " + crop);
        }
        var tomatoes = CropContent.TOMATOES.get();
        check(tomatoes.useWithoutItem(level.getBlockState(pos), level, pos, player,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)).consumesAction(), "Pick tomato");
        check(tomatoes.getAge(level.getBlockState(pos)) == 4, "Tomato picking preserves fruiting bush");
        rice(level, player, pos.offset(5, 0, 0));

        var chances = Map.ofEntries(Map.entry("wild_cabbages",120),Map.entry("wild_onions",480),Map.entry("wild_tomatoes",400),
            Map.entry("wild_carrots",480),Map.entry("wild_potatoes",400),Map.entry("wild_beetroots",120),Map.entry("wild_rice",80),
            Map.entry("wild_cotton",128),Map.entry("wild_coffee",60),Map.entry("wild_soybean",320),Map.entry("wild_chili",200));
        var registry = level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
        var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        for (var e : chances.entrySet()) {
            var feature = registry.getValueOrThrow(ResourceKey.create(Registries.PLACED_FEATURE, id("patch_" + e.getKey())));
            var json = PlacedFeature.DIRECT_CODEC.encodeStart(ops, feature).getOrThrow().getAsJsonObject();
            check(json.getAsJsonArray("placement").get(0).getAsJsonObject().get("chance").getAsInt() == e.getValue(), "Quarter upstream wild-patch frequency " + e.getKey());
        }
        // Place every land feature on real soil through its configured codec; also exercise its actual loot table.
        BlockPos origin = new BlockPos(192,160,0);
        for (var p : BlockPos.betweenClosed(origin.offset(-12,-1,-12), origin.offset(12,-1,12))) level.setBlockAndUpdate(p, Blocks.GRASS_BLOCK.defaultBlockState());
        var configured = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE);
        for (String name : chances.keySet()) {
            if (name.equals("wild_rice")) continue;
            for (var p : BlockPos.betweenClosed(origin.offset(-12,0,-12), origin.offset(12,1,12))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
            // Coastal crops require sand. Others use dirt.
            var soil = name.equals("wild_cabbages") || name.equals("wild_beetroots") ? Blocks.SAND : Blocks.GRASS_BLOCK;
            for (var p : BlockPos.betweenClosed(origin.offset(-12,-1,-12), origin.offset(12,-1,12))) level.setBlock(p, soil.defaultBlockState(), 2);
            var feature = configured.getValueOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, id("patch_"+name)));
            check(feature.place(level, level.getChunkSource().getGenerator(), level.getRandom(), origin), "Configured wild patch actually places " + name);
            var block = BuiltInRegistries.BLOCK.getValue(id(name));
            boolean found = false;
            for (var p : BlockPos.betweenClosed(origin.offset(-12,0,-12), origin.offset(12,1,12))) if (level.getBlockState(p).is(block)) { found = true; break; }
            check(found, "Wild patch contains primary crop " + name);
            check(!Block.getDrops(block.defaultBlockState(), level, origin, null, player, ItemStack.EMPTY).isEmpty(), "Wild crop gives harvest/seeds " + name);
            var sheared = Block.getDrops(block.defaultBlockState(), level, origin, null, player, new ItemStack(Items.SHEARS));
            check(sheared.stream().anyMatch(s -> s.is(block.asItem())), "Shears preserve wild plant " + name);
        }
        var summer = TagKey.create(Registries.BLOCK, Identifier.parse("sereneseasons:summer_crops"));
        check(CropContent.SOYBEAN_CROP.get().defaultBlockState().is(summer) && CropContent.RICE_PANICLES.get().defaultBlockState().is(summer), "Serene Seasons tags exist without dependency");
        ChocolateFlavored.LOGGER.info("CROP_VERIFICATION_PASSED: planting, seed consumption, native natural/bonemeal growth, harvests, support loss, tomato regrowth, aquatic rice, real wild patches, 4x rarity, seasonal tags and food exclusions");
    }
    private static void rice(ServerLevel level, FakePlayer player, BlockPos pos) {
        var crop = CropContent.RICE_CROP.get();
        level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CropContent.RICE.get(), 2));
        check(CropContent.RICE.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false))).consumesAction(), "Plant rice underwater");
        check(level.getBlockState(pos).is(crop) && level.getFluidState(pos).isFull(), "Rice retains water");
        for (int i = 0; i < 16; i++) crop.performBonemeal(level, level.getRandom(), pos, level.getBlockState(pos));
        var top = CropContent.RICE_PANICLES.get();
        check(level.getBlockState(pos.above()).is(top) && top.isMaxAge(level.getBlockState(pos.above())), "Rice grows mature upper panicles");
        check(level.getBlockState(pos).getValue(RiceCropBlock.SUPPORTING), "Rice supporting model state");
        var loot = Block.getDrops(level.getBlockState(pos.above()), level, pos.above(), null, player, new ItemStack(com.akiotsukino.chocolateflavored.content.ModContent.IRON_KNIFE.get()));
        check(loot.stream().anyMatch(s -> s.is(CropContent.RICE.get())), "Knife harvest yields plantable rice");
        level.destroyBlock(pos.above(), false);
        for (int i = 0; i < 100; i++) level.getBlockState(pos).randomTick(level, pos, level.getRandom());
        check(level.getBlockState(pos.above()).is(top), "Rice upper regrows from root");
        level.removeBlock(pos.above(), false);level.removeBlock(pos, false);
        level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        DoublePlantBlock.placeAt(level, CropContent.WILD_RICE.get().defaultBlockState(), pos, 3);
        for (var p : List.of(pos,pos.above())) {
            var drops = Block.getDrops(level.getBlockState(p), level, p, null, player, ItemStack.EMPTY);
            check(drops.stream().anyMatch(s -> s.is(CropContent.RICE.get())), "Wild rice can be harvested from either half");
        }
        level.destroyBlock(pos.above(), false);
        check(!level.getBlockState(pos).is(CropContent.WILD_RICE.get()), "Wild rice paired-half cleanup");
    }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("chocolateflavored",path); }
    private static void check(boolean value, String message) { if (!value) throw new IllegalStateException("Crops: "+message); }
    private CropVerification() {}
}
