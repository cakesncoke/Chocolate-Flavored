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
        check(recipes.byKey(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("chocolateflavored", "mud_kiln/bread"))).isEmpty(), "KubeJS removes built-in mud_kiln recipes");
        var mud_kilnRecipe = recipes.getRecipeFor(CookingRecipes.MUD_KILN.get(), new SingleRecipeInput(new ItemStack(Items.CARROT)), level).orElseThrow();
        check(mud_kilnRecipe.id().identifier().toString().equals("kubejs:verification_mud_kiln"), "KubeJS registers custom mud_kiln recipe");
        BlockPos pos = new BlockPos(0, 160, 0);
        level.setBlockAndUpdate(pos, CookingContent.MUD_KILN.get().defaultBlockState().setValue(MudKilnBlock.LIT, true));
        var mud_kiln = (MudKilnBlockEntity)level.getBlockEntity(pos);
        check(mud_kiln.placeFood(level, FakePlayerFactory.getMinecraft(level), new ItemStack(Items.CARROT)), "MudKiln accepts KubeJS ingredient");
        for (int i = 0; i < 4; i++) MudKilnBlockEntity.tick(level, pos, mud_kiln.getBlockState(), mud_kiln);
        check(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).stream().anyMatch(e -> e.getItem().is(Items.GOLDEN_CARROT)), "MudKiln executes KubeJS recipe");
        level.setBlockAndUpdate(pos.above(), CookingContent.COOKING_POT.get().defaultBlockState());
        var pot = (CookingPotBlockEntity)level.getBlockEntity(pos.above());
        Item[] inputs={Items.APPLE,Items.CARROT,Items.POTATO,Items.BEETROOT,Items.WHEAT,Items.SUGAR};
        int[] counts={2,1,3,4,5,6};
        Item[] outputs={Items.GOLDEN_CARROT,Items.APPLE,Items.BREAD,Items.POTATO,Items.BEETROOT,Items.SUGAR};
        for(int i=0;i<6;i++)pot.setItem(i,new ItemStack(inputs[i],counts[i]));
        pot.setItem(11,new ItemStack(Items.DIRT,64));
        for(int i=0;i<200;i++)CookingPotBlockEntity.tick(level,pot.getBlockPos(),pot.getBlockState(),pot);
        for(int i=0;i<6;i++)check(pot.getItem(i).getCount()==counts[i],"One blocked result prevents every input consumption");
        for(int i=6;i<11;i++)check(pot.getItem(i).isEmpty(),"No partial outputs when sixth result is blocked");
        pot.setItem(11,ItemStack.EMPTY);
        for(int i=0;i<199;i++)CookingPotBlockEntity.tick(level,pot.getBlockPos(),pot.getBlockState(),pot);
        check(pot.getItem(6).isEmpty(),"KubeJS pot recipes retain fixed 200 tick timing");
        CookingPotBlockEntity.tick(level,pot.getBlockPos(),pot.getBlockState(),pot);
        for(int i=0;i<6;i++)check(pot.getItem(i).isEmpty()&&pot.getItem(6+i).is(outputs[i])&&pot.getItem(6+i).getCount()==i+2,
                "All six counted inputs and six output stacks execute atomically");
        ChocolateFlavored.LOGGER.info("KUBEJS_COOKING_VERIFICATION_PASSED: real KubeJS recipe removal and custom JSON recipes execute in mud_kiln and pot without Farmer's Delight");
        event.getServer().halt(false);
    }
    private static void check(boolean ok, String message) { if (!ok) throw new IllegalStateException("KubeJS cooking verification failed: " + message); }
}
