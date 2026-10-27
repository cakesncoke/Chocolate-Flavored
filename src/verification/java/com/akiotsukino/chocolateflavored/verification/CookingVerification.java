package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.cooking.*;
import com.akiotsukino.chocolateflavored.lighting.LightingIgnition;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayer;

final class CookingVerification {
    static void run(ServerLevel level, FakePlayer player) {
        codecs(level);
        BlockPos pos = new BlockPos(128, 160, 0);
        player.setPos(128.5, 162, 0.5); player.setXRot(90);
        var state = CookingContent.MUD_KILN.get().defaultBlockState();
        check(!state.getValue(MudKilnBlock.LIT), "MudKiln defaults unlit");
        level.setBlockAndUpdate(pos, state);
        var mud_kiln = (MudKilnBlockEntity)level.getBlockEntity(pos);
        ItemStack wheat = new ItemStack(Items.WHEAT, 20);
        for (int slot = 0; slot < 6; slot++) check(mud_kiln.placeFood(level, player, wheat), "Six mud_kiln positions accept custom recipe");
        check(wheat.getCount() == 2 && !mud_kiln.placeFood(level, player, new ItemStack(Items.WHEAT, 3)), "MudKiln consumes recipe input count and rejects seventh batch");
        for (int i = 0; i < 200; i++) MudKilnBlockEntity.tick(level, pos, state, mud_kiln);
        check(mud_kiln.progress(0) == 0 && mud_kiln.items().getFirst().getCount() == 3, "Unlit mud_kiln does not cook");
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var steel = new ItemStack(Items.FLINT_AND_STEEL);
        player.setItemInHand(InteractionHand.MAIN_HAND, steel);
        check(player.gameMode.useItemOn(player, level, steel, InteractionHand.MAIN_HAND, hit).consumesAction(), "Flint and steel lights mud_kiln through block interaction");
        state = level.getBlockState(pos);
        check(state.getValue(MudKilnBlock.LIT) && steel.getDamageValue() == 1, "Ignition and one flint-and-steel wear");
        for (int i = 0; i < 100; i++) MudKilnBlockEntity.tick(level, pos, state, mud_kiln);
        var saved = mud_kiln.saveWithFullMetadata(level.registryAccess());
        mud_kiln = (MudKilnBlockEntity)BlockEntity.loadStatic(pos, state, saved, level.registryAccess()); mud_kiln.setLevel(level); level.setBlockEntity(mud_kiln);
        check(mud_kiln.progress(0) == 100 && mud_kiln.items().get(5).is(Items.WHEAT), "MudKiln inventory and cooking progress survive save/load");
        for (int i = 0; i < 100; i++) MudKilnBlockEntity.tick(level, pos, state, mud_kiln);
        check(count(level, pos, Items.BREAD) == 6 && mud_kiln.items().stream().allMatch(ItemStack::isEmpty), "Six custom recipes finish and output one bread per three wheat");
        clear(level, pos);
        check(mud_kiln.recipe(level, new ItemStack(Items.POTATO)).orElseThrow().time() == 200, "Custom mud_kiln recipes take precedence over campfire fallback");
        mud_kiln.items().set(0, new ItemStack(Items.POTATO, 3));
        for (int i = 0; i < 200; i++) MudKilnBlockEntity.tick(level, pos, state, mud_kiln);
        check(count(level, pos, Items.POTATO) == 2 && count(level, pos, Items.BAKED_POTATO) == 1, "Existing oversized batch returns ingredients beyond current recipe requirements");
        clear(level, pos);
        check(mud_kiln.recipe(level, new ItemStack(Items.BEEF)).orElseThrow().time() == 600, "Campfire recipe fallback remains available");
        check(mud_kiln.placeFood(level, player, new ItemStack(Items.BEEF)), "Campfire-compatible food can be placed");
        for (int i = 0; i < 20; i++) MudKilnBlockEntity.tick(level, pos, state, mud_kiln);
        var shovel = new ItemStack(Items.IRON_SHOVEL); player.setItemInHand(InteractionHand.MAIN_HAND, shovel);
        player.gameMode.useItemOn(player, level, shovel, InteractionHand.MAIN_HAND, hit);
        state = level.getBlockState(pos); check(!state.getValue(MudKilnBlock.LIT), "Shovel extinguishes mud_kiln");
        MudKilnBlockEntity.tick(level, pos, state, mud_kiln); check(mud_kiln.progress(0) == 18, "Unlit food cools by two ticks");
        var charge = new ItemStack(Items.FIRE_CHARGE, 2); player.setItemInHand(InteractionHand.MAIN_HAND, charge);
        player.gameMode.useItemOn(player, level, charge, InteractionHand.MAIN_HAND, hit);
        check(level.getBlockState(pos).getValue(MudKilnBlock.LIT) && charge.getCount() == 1, "Fire charge lights mud_kiln and consumes one charge");
        level.setBlockAndUpdate(pos.above(), CookingContent.COOKING_POT.get().defaultBlockState());
        MudKilnBlockEntity.tick(level, pos, level.getBlockState(pos), mud_kiln);
        check(mud_kiln.items().stream().allMatch(ItemStack::isEmpty) && count(level, pos, Items.BEEF) == 1, "Covering mud_kiln returns its raw food instead of consuming it");
        clear(level, pos);
        var potPos = pos.above();
        var pot = (CookingPotBlockEntity)level.getBlockEntity(potPos);
        check(pot.isHeated(), "Lit mud_kiln heats pot");
        level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(MudKilnBlock.LIT, false));
        pot.setItem(0, new ItemStack(Items.RED_MUSHROOM, 2)); pot.setItem(1, new ItemStack(Items.BROWN_MUSHROOM, 2));
        pot.setItem(2, new ItemStack(Items.BOWL, 2));
        tickPot(level, pot, 200); check(!pot.hasOutputs(), "Unlit kiln does not heat cooking pot");
        check(LightingIgnition.ignite(level, pos, player), "Shared ignition supports kiln");
        tickPot(level, pot, 199); check(!pot.hasOutputs(), "Pot needs full vanilla furnace cooking time");
        tickPot(level, pot, 1); check(pot.getItem(6).is(Items.MUSHROOM_STEW) && pot.getItem(2).getCount()==1, "First batch completes at 200 ticks and consumes bowl ingredient");
        tickPot(level, pot, 200); check(pot.getItem(7).is(Items.MUSHROOM_STEW) && pot.getItem(0).isEmpty(), "Unstackable results fill separate output slots");
        var menu = new CookingPotMenu(0, player.getInventory(), pot, pot.data);
        check(!((Object)menu instanceof RecipeBookMenu), "Cooking pot has no recipe-book menu");
        for(int i=0;i<12;i++) {
            var slot=menu.getSlot(i); int j=i%6;
            check(slot.x==(i<6?7:115)+j%3*18 && slot.y==19+j/3*18, "Exact slot layout");
            if(i>=6)check(slot.mayPickup(player)&&!slot.mayPlace(new ItemStack(Items.DIRT)), "All outputs extractable and insertion protected");
        }
        check(menu.getSlot(12).x==8 && menu.getSlot(12).y==84, "Player inventory unchanged");
        check(!menu.quickMoveStack(player,6).isEmpty() && pot.getItem(6).isEmpty(), "Shift-click output extraction");
        for(int i=0;i<12;i++)pot.setItem(i,ItemStack.EMPTY);
        pot.setItem(0,new ItemStack(Items.RED_MUSHROOM));pot.setItem(1,new ItemStack(Items.BROWN_MUSHROOM));pot.setItem(2,new ItemStack(Items.BOWL));
        for(int i=6;i<12;i++)pot.setItem(i,new ItemStack(Items.DIRT,64));
        tickPot(level,pot,400);check(pot.getItem(0).getCount()==1 && pot.getItem(2).getCount()==1,"Blocked output does not consume any ingredients");
        pot.setItem(6,ItemStack.EMPTY);tickPot(level,pot,200);check(pot.getItem(6).is(Items.MUSHROOM_STEW),"Cleared output resumes cooking");
        check(pot.canPlaceItemThroughFace(0,new ItemStack(Items.BOWL),Direction.EAST)
                && !pot.canPlaceItemThroughFace(6,new ItemStack(Items.BOWL),Direction.UP)
                && pot.canTakeItemThroughFace(11,pot.getItem(11),Direction.DOWN),"Sided automation separates six inputs and six outputs");
        // Simulate a saved v1 pot with 64 buffered, normally unstackable meals.
        for(int i=0;i<12;i++)pot.setItem(i,ItemStack.EMPTY);
        var legacyMeal=new ItemStack(Items.MUSHROOM_STEW);legacyMeal.set(DataComponents.MAX_STACK_SIZE,64);legacyMeal.setCount(64);
        pot.setItem(6,legacyMeal);pot.setItem(7,new ItemStack(Items.BOWL,2));pot.setItem(8,new ItemStack(Items.BREAD,3));
        var tag=pot.saveWithFullMetadata(level.registryAccess());tag.putInt("PotInventoryVersion",1);
        var ops=level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        tag.put("MealTemplate",ItemStack.CODEC.encodeStart(ops,new ItemStack(Items.MUSHROOM_STEW)).getOrThrow());
        pot=(CookingPotBlockEntity)BlockEntity.loadStatic(potPos,pot.getBlockState(),tag,level.registryAccess());pot.setLevel(level);level.setBlockEntity(pot);
        var savedMigration=pot.saveWithFullMetadata(level.registryAccess());
        pot=(CookingPotBlockEntity)BlockEntity.loadStatic(potPos,pot.getBlockState(),savedMigration,level.registryAccess());pot.setLevel(level);level.setBlockEntity(pot);
        int meals=0,bowls=0,bread=0;
        for(int round=0;round<20;round++) {
            for(int i=6;i<12;i++) {
                var stack=pot.removeItem(i,64);
                if(stack.is(Items.MUSHROOM_STEW)){check(stack.getCount()==1&&stack.getMaxStackSize()==1,"Legacy meals restore normal stack limits");meals+=stack.getCount();}
                if(stack.is(Items.BOWL))bowls+=stack.getCount();if(stack.is(Items.BREAD))bread+=stack.getCount();
            }
            tickPot(level,pot,1);
        }
        check(meals==64&&bowls==2&&bread==3,"Legacy migration and queued-return reload lose or duplicate no items");
        pot.setItem(0,new ItemStack(Items.CARROT,4));pot.setItem(11,new ItemStack(Items.BREAD,3));
        var drops=Block.getDrops(pot.getBlockState(),level,potPos,pot,player,ItemStack.EMPTY);
        check(drops.size()==1&&drops.getFirst().is(CookingContent.COOKING_POT_ITEM.get()),"Pot drops itself");
        check(drops.getFirst().getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).allItemsCopyStream().allMatch(ItemStack::isEmpty),"Pot item does not duplicate separately dropped inventory");
        level.removeBlock(potPos,false);
        check(count(level,potPos,Items.CARROT)==4&&count(level,potPos,Items.BREAD)==3,"Breaking pot drops both input and output inventory");
        clear(level,potPos);
        var packed=new ItemStack(CookingContent.COOKING_POT_ITEM.get());
        var oldContents=NonNullList.withSize(9,ItemStack.EMPTY);oldContents.set(6,legacyMeal.copyWithCount(3));
        packed.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(oldContents));
        packed.set(CookingContent.MEAL_TEMPLATE.get(),ItemContainerContents.fromItems(List.of(new ItemStack(Items.MUSHROOM_STEW))));
        packed.set(CookingContent.MEAL_CONTAINER.get(),ItemContainerContents.fromItems(List.of(new ItemStack(Items.BOWL))));
        packed=ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,packed).getOrThrow()).getOrThrow();
        player.setItemInHand(InteractionHand.MAIN_HAND,packed);
        check(packed.getItem().useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit)).consumesAction(),"Old packed pot remains placeable");
        pot=(CookingPotBlockEntity)level.getBlockEntity(potPos);
        check(pot!=null&&pot.getItem(6).is(Items.MUSHROOM_STEW)&&pot.getItem(7).is(Items.MUSHROOM_STEW)&&pot.getItem(8).is(Items.MUSHROOM_STEW)
                &&pot.getItem(6).getMaxStackSize()==1,"Item-packed legacy meals migrate into normal outputs");
        // A hopper conducts heat one block upward, as in Farmer's Delight.
        level.setBlockAndUpdate(pos.below(), CookingContent.MUD_KILN.get().defaultBlockState().setValue(MudKilnBlock.LIT, true));
        level.setBlockAndUpdate(pos, Blocks.HOPPER.defaultBlockState()); check(pot.isHeated(), "Hopper conducts mud_kiln heat");
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState()); check(!pot.isHeated(), "Unheated conductor does not create heat");
        level.removeBlock(potPos, false); level.removeBlock(pos, false); level.removeBlock(pos.below(), false); clear(level, pos);
        ChocolateFlavored.LOGGER.info("COOKING_VERIFICATION_PASSED: custom/campfire mud_kiln recipes, six positions, ignition/dousing/cooldown, persistence, counted stack recipes, 200-tick cooking, normal outputs, atomic capacity, legacy migration/save/drop, no recipe book, sided automation and heat conductors");
    }
    private static void codecs(ServerLevel level) {
        var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        String json = "{\"type\":\"chocolateflavored:mud_kiln\",\"ingredient\":\"minecraft:wheat\",\"input_count\":3,\"result\":{\"id\":\"minecraft:bread\"},\"cookingtime\":20}";
        var mud_kiln = (CookingRecipes.MudKilnRecipe)Recipe.CODEC.parse(ops, JsonParser.parseString(json)).getOrThrow();
        check(mud_kiln.matches(new SingleRecipeInput(new ItemStack(Items.WHEAT, 3)), level) && !mud_kiln.matches(new SingleRecipeInput(new ItemStack(Items.WHEAT, 2)), level), "MudKiln JSON count matching");
        var bytes = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        try {
            CookingRecipes.MUD_KILN_SERIALIZER.get().streamCodec().encode(bytes, mud_kiln);
            var decoded = CookingRecipes.MUD_KILN_SERIALIZER.get().streamCodec().decode(bytes);
            check(decoded.inputCount() == 3 && decoded.cookingTime() == 20, "Recipe network codec round trip");
        } finally { bytes.release(); }
        check(Recipe.CODEC.parse(ops, JsonParser.parseString(json.replace("\"cookingtime\":20", "\"cookingtime\":0"))).error().isPresent(), "Invalid processing time is rejected");
        var recipe = new CookingRecipes.PotRecipe(List.of(
                new CookingRecipes.PotIngredient(Ingredient.of(Items.CARROT,Items.POTATO),2),
                new CookingRecipes.PotIngredient(Ingredient.of(Items.CARROT),1)),
                List.of(new ItemStackTemplate(Items.BREAD),new ItemStackTemplate(Items.APPLE)),0);
        check(recipe.matches(new CookingRecipes.PotInput(List.of(new ItemStack(Items.CARROT),new ItemStack(Items.POTATO,2))),level),"Counted overlapping ingredients backtrack");
        check(!recipe.matches(new CookingRecipes.PotInput(List.of(new ItemStack(Items.CARROT),new ItemStack(Items.POTATO))),level),"Count shortfall rejected");
        var encoded=CookingRecipes.PotRecipe.CODEC.codec().encodeStart(ops,recipe).getOrThrow();
        var decoded=CookingRecipes.PotRecipe.CODEC.codec().parse(ops,encoded).getOrThrow();
        check(decoded.results().size()==2&&decoded.ingredients().getFirst().count()==2&&decoded.cookingTime()==200,"Multi-output JSON round trip and fixed furnace duration");
        var potBytes=new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());
        try{CookingRecipes.POT_SERIALIZER.get().streamCodec().encode(potBytes,recipe);
            check(CookingRecipes.POT_SERIALIZER.get().streamCodec().decode(potBytes).results().size()==2,"Multi-output recipe network round trip");
        }finally{potBytes.release();}

    }
    private static void tickPot(ServerLevel level, CookingPotBlockEntity pot, int ticks) { for (int i = 0; i < ticks; i++) CookingPotBlockEntity.tick(level, pot.getBlockPos(), pot.getBlockState(), pot); }
    private static int count(ServerLevel level, BlockPos pos, Item item) { return level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4)).stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum(); }
    private static void clear(ServerLevel level, BlockPos pos) { level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4)).forEach(ItemEntity::discard); }
    private static void check(boolean ok, String message) { if (!ok) throw new IllegalStateException("Cooking verification failed: " + message); }
}
