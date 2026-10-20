package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.cooking.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = ChocolateFlavored.MODID)
public final class KubeCookingVerification {
    @SubscribeEvent public static void started(ServerStartedEvent event) {
        if (!Boolean.getBoolean("chocolateflavored.verify.kubejs")) return;
        var level = event.getServer().overworld();
        check(ModList.get().isLoaded("kubejs") && !ModList.get().isLoaded("farmersdelight"), "KubeJS loaded without Farmer's Delight");
        var recipes = event.getServer().getRecipeManager();
        check(recipes.byKey(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("chocolateflavored", "oven/bread"))).isEmpty(), "KubeJS removes built-in oven recipes");
        var ovenRecipe = recipes.getRecipeFor(CookingRecipes.OVEN.get(), new SingleRecipeInput(new ItemStack(Items.CARROT)), level).orElseThrow();
        check(ovenRecipe.id().identifier().toString().equals("kubejs:verification_oven"), "KubeJS registers custom oven recipe");
        BlockPos pos = new BlockPos(0, 160, 0);
        level.setBlockAndUpdate(pos, CookingContent.OVEN.get().defaultBlockState().setValue(OvenBlock.LIT, true));
        var oven = (OvenBlockEntity)level.getBlockEntity(pos);
        check(oven.placeFood(level, FakePlayerFactory.getMinecraft(level), new ItemStack(Items.CARROT)), "Oven accepts KubeJS ingredient");
        for (int i = 0; i < 4; i++) OvenBlockEntity.tick(level, pos, oven.getBlockState(), oven);
        check(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).stream().anyMatch(e -> e.getItem().is(Items.GOLDEN_CARROT)), "Oven executes KubeJS recipe");
        level.setBlockAndUpdate(pos.above(), CookingContent.COOKING_POT.get().defaultBlockState());
        var pot = (CookingPotBlockEntity)level.getBlockEntity(pos.above());
        pot.setItem(0, new ItemStack(Items.APPLE)); pot.setItem(1, new ItemStack(Items.CARROT)); pot.setItem(7, new ItemStack(Items.BOWL));
        for (int i = 0; i < 4; i++) CookingPotBlockEntity.tick(level, pot.getBlockPos(), pot.getBlockState(), pot);
        check(pot.getItem(8).is(Items.MUSHROOM_STEW) && pot.getItem(7).isEmpty(), "Cooking pot executes KubeJS recipe and consumes serving bowl");
        ChocolateFlavored.LOGGER.info("KUBEJS_COOKING_VERIFICATION_PASSED: real KubeJS recipe removal and custom JSON recipes execute in oven and pot without Farmer's Delight");
        event.getServer().halt(false);
    }
    private static void check(boolean ok, String message) { if (!ok) throw new IllegalStateException("KubeJS cooking verification failed: " + message); }
}
