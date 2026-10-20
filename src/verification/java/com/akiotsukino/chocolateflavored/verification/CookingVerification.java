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
        var state = CookingContent.OVEN.get().defaultBlockState();
        check(!state.getValue(OvenBlock.LIT), "Oven defaults unlit");
        level.setBlockAndUpdate(pos, state);
        var oven = (OvenBlockEntity)level.getBlockEntity(pos);
        ItemStack wheat = new ItemStack(Items.WHEAT, 20);
        for (int slot = 0; slot < 6; slot++) check(oven.placeFood(level, player, wheat), "Six oven positions accept custom recipe");
        check(wheat.getCount() == 2 && !oven.placeFood(level, player, new ItemStack(Items.WHEAT, 3)), "Oven consumes recipe input count and rejects seventh batch");
        for (int i = 0; i < 200; i++) OvenBlockEntity.tick(level, pos, state, oven);
        check(oven.progress(0) == 0 && oven.items().getFirst().getCount() == 3, "Unlit oven does not cook");
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var steel = new ItemStack(Items.FLINT_AND_STEEL);
        player.setItemInHand(InteractionHand.MAIN_HAND, steel);
        check(player.gameMode.useItemOn(player, level, steel, InteractionHand.MAIN_HAND, hit).consumesAction(), "Flint and steel lights oven through block interaction");
        state = level.getBlockState(pos);
        check(state.getValue(OvenBlock.LIT) && steel.getDamageValue() == 1, "Ignition and one flint-and-steel wear");
        for (int i = 0; i < 100; i++) OvenBlockEntity.tick(level, pos, state, oven);
        var saved = oven.saveWithFullMetadata(level.registryAccess());
        oven = (OvenBlockEntity)BlockEntity.loadStatic(pos, state, saved, level.registryAccess()); oven.setLevel(level); level.setBlockEntity(oven);
        check(oven.progress(0) == 100 && oven.items().get(5).is(Items.WHEAT), "Oven inventory and cooking progress survive save/load");
        for (int i = 0; i < 100; i++) OvenBlockEntity.tick(level, pos, state, oven);
        check(count(level, pos, Items.BREAD) == 6 && oven.items().stream().allMatch(ItemStack::isEmpty), "Six custom recipes finish and output one bread per three wheat");
        clear(level, pos);
        check(oven.recipe(level, new ItemStack(Items.POTATO)).orElseThrow().time() == 200, "Custom oven recipes take precedence over campfire fallback");
        check(oven.recipe(level, new ItemStack(Items.BEEF)).orElseThrow().time() == 600, "Campfire recipe fallback remains available");
        check(oven.placeFood(level, player, new ItemStack(Items.BEEF)), "Campfire-compatible food can be placed");
        for (int i = 0; i < 20; i++) OvenBlockEntity.tick(level, pos, state, oven);
        var shovel = new ItemStack(Items.IRON_SHOVEL); player.setItemInHand(InteractionHand.MAIN_HAND, shovel);
        player.gameMode.useItemOn(player, level, shovel, InteractionHand.MAIN_HAND, hit);
        state = level.getBlockState(pos); check(!state.getValue(OvenBlock.LIT), "Shovel extinguishes oven");
        OvenBlockEntity.tick(level, pos, state, oven); check(oven.progress(0) == 18, "Unlit food cools by two ticks");
        var charge = new ItemStack(Items.FIRE_CHARGE, 2); player.setItemInHand(InteractionHand.MAIN_HAND, charge);
        player.gameMode.useItemOn(player, level, charge, InteractionHand.MAIN_HAND, hit);
        check(level.getBlockState(pos).getValue(OvenBlock.LIT) && charge.getCount() == 1, "Fire charge lights oven and consumes one charge");
        level.setBlockAndUpdate(pos.above(), CookingContent.COOKING_POT.get().defaultBlockState());
        OvenBlockEntity.tick(level, pos, level.getBlockState(pos), oven);
        check(oven.items().stream().allMatch(ItemStack::isEmpty) && count(level, pos, Items.BEEF) == 1, "Covering oven returns its raw food instead of consuming it");
        clear(level, pos);
        var potPos = pos.above();
        var pot = (CookingPotBlockEntity)level.getBlockEntity(potPos);
        check(pot.isHeated(), "Lit oven heats pot");
        level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(OvenBlock.LIT, false));
        pot.setItem(0, new ItemStack(Items.RED_MUSHROOM, 2)); pot.setItem(1, new ItemStack(Items.BROWN_MUSHROOM, 2));
        tickPot(level, pot, 200); check(pot.meal().isEmpty(), "Unlit oven does not heat cooking pot");
        check(LightingIgnition.ignite(level, pos, player), "Shared ignition supports oven");
        tickPot(level, pot, 400);
        check(pot.meal().getCount() == 2 && pot.getItem(8).isEmpty(), "Pot stores multiple unstackable meals until bowls are supplied");
        var menu = new CookingPotMenu(0, player.getInventory(), pot, pot.data);
        check(!((Object)menu instanceof RecipeBookMenu), "Cooking pot has no recipe-book menu");
        check(!menu.getSlot(6).mayPickup(player) && !menu.getSlot(6).mayPlace(new ItemStack(Items.DIRT)) && menu.quickMoveStack(player, 6).isEmpty(), "Meal display cannot be taken or inserted into");
        pot.setItem(7, new ItemStack(Items.BOWL, 2)); tickPot(level, pot, 1);
        check(pot.getItem(8).getCount() == 1 && pot.meal().getCount() == 1 && pot.getItem(7).getCount() == 1, "One bowl produces one normally stacked serving");
        var serving = menu.getSlot(8).remove(1); menu.getSlot(8).onTake(player, serving);
        check(ItemStack.isSameItemSameComponents(serving, new ItemStack(Items.MUSHROOM_STEW)) && serving.getMaxStackSize() == 1, "Served food retains vanilla components and stack limit");
        tickPot(level, pot, 1); check(pot.meal().isEmpty() && pot.getItem(8).getCount() == 1, "Second stored serving consumes second bowl");
        pot.setItem(8, ItemStack.EMPTY); pot.setItem(7, ItemStack.EMPTY);
        pot.setItem(0, new ItemStack(Items.RED_MUSHROOM, 64)); pot.setItem(1, new ItemStack(Items.BROWN_MUSHROOM, 64));
        tickPot(level, pot, 200 * 64);
        check(pot.meal().getCount() == 64 && pot.getItem(0).isEmpty(), "Meal buffer holds 64 completed portions");
        pot.setItem(0, new ItemStack(Items.RED_MUSHROOM)); pot.setItem(1, new ItemStack(Items.BROWN_MUSHROOM)); tickPot(level, pot, 200);
        check(pot.meal().getCount() == 64 && pot.getItem(0).getCount() == 1, "Full buffer does not consume more ingredients");
        var tag = pot.saveWithFullMetadata(level.registryAccess());
        pot = (CookingPotBlockEntity)BlockEntity.loadStatic(potPos, pot.getBlockState(), tag, level.registryAccess()); pot.setLevel(level); level.setBlockEntity(pot);
        check(pot.meal().getCount() == 64 && pot.servingContainer().is(Items.BOWL), "64-portion buffer survives block-entity save/load");
        var drops = Block.getDrops(pot.getBlockState(), level, potPos, pot, player, ItemStack.EMPTY);
        check(drops.size() == 1 && drops.getFirst().is(CookingContent.COOKING_POT_ITEM.get()), "Pot loot drops pot item");
        var packed = drops.getFirst();
        var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        packed = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, packed).getOrThrow()).getOrThrow();
        check(packed.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).allItemsCopyStream().skip(6).findFirst().orElseThrow().getCount() == 64, "Dropped pot and serialized item preserve 64 portions");
        level.removeBlock(potPos, false);
        check(count(level, potPos, Items.RED_MUSHROOM) == 1 && count(level, potPos, Items.MUSHROOM_STEW) == 0, "Breaking pot drops ingredients separately without duplicating its stored meals");
        player.setItemInHand(InteractionHand.MAIN_HAND, packed);
        packed.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        pot = (CookingPotBlockEntity)level.getBlockEntity(potPos);
        check(pot != null && pot.meal().getCount() == 64, "Replacing filled pot restores its meals");
        var bowl = new ItemStack(Items.BOWL, 2); serving = pot.serve(player, bowl);
        check(bowl.getCount() == 1 && pot.meal().getCount() == 63 && ItemStack.isSameItemSameComponents(serving, new ItemStack(Items.MUSHROOM_STEW)), "Held bowl serves preserved meal with normal components");
        check(!pot.canTakeItemThroughFace(6, pot.meal(), Direction.DOWN) && pot.canTakeItemThroughFace(8, serving, Direction.DOWN), "Automation cannot extract the unserved meal buffer");
        check(!pot.canPlaceItemThroughFace(7, bowl, Direction.UP) && pot.canPlaceItemThroughFace(7, bowl, Direction.EAST), "Automation separates ingredients from serving containers");
        // A hopper conducts heat one block upward, as in Farmer's Delight.
        level.setBlockAndUpdate(pos.below(), CookingContent.OVEN.get().defaultBlockState().setValue(OvenBlock.LIT, true));
        level.setBlockAndUpdate(pos, Blocks.HOPPER.defaultBlockState()); check(pot.isHeated(), "Hopper conducts oven heat");
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState()); check(!pot.isHeated(), "Unheated conductor does not create heat");
        level.removeBlock(potPos, false); level.removeBlock(pos, false); level.removeBlock(pos.below(), false); clear(level, pos);
        ChocolateFlavored.LOGGER.info("COOKING_VERIFICATION_PASSED: custom/campfire oven recipes, six positions, ignition/dousing/cooldown, persistence, heated pot recipes, meal serving and 64-portion save/drop/replace, no recipe book, sided automation and heat conductors");
    }
    private static void codecs(ServerLevel level) {
        var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        String json = "{\"type\":\"chocolateflavored:oven\",\"ingredient\":\"minecraft:wheat\",\"input_count\":3,\"result\":{\"id\":\"minecraft:bread\"},\"cookingtime\":20}";
        var oven = (CookingRecipes.OvenRecipe)Recipe.CODEC.parse(ops, JsonParser.parseString(json)).getOrThrow();
        check(oven.matches(new SingleRecipeInput(new ItemStack(Items.WHEAT, 3)), level) && !oven.matches(new SingleRecipeInput(new ItemStack(Items.WHEAT, 2)), level), "Oven JSON count matching");
        var bytes = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        try {
            CookingRecipes.OVEN_SERIALIZER.get().streamCodec().encode(bytes, oven);
            var decoded = CookingRecipes.OVEN_SERIALIZER.get().streamCodec().decode(bytes);
            check(decoded.inputCount() == 3 && decoded.cookingTime() == 20, "Recipe network codec round trip");
        } finally { bytes.release(); }
        check(Recipe.CODEC.parse(ops, JsonParser.parseString(json.replace("\"cookingtime\":20", "\"cookingtime\":0"))).error().isPresent(), "Invalid processing time is rejected");
        var recipe = new CookingRecipes.PotRecipe(List.of(Ingredient.of(Items.CARROT, Items.POTATO), Ingredient.of(Items.CARROT)), new ItemStackTemplate(Items.BREAD), Optional.empty(), 20, 0);
        check(recipe.matches(new CookingRecipes.PotInput(List.of(new ItemStack(Items.CARROT), new ItemStack(Items.POTATO))), level), "Shapeless matching handles overlapping ingredients without a greedy mismatch");
        check(!recipe.matches(new CookingRecipes.PotInput(List.of(new ItemStack(Items.CARROT, 2))), level), "Separate recipe ingredients require separate input slots");
    }
    private static void tickPot(ServerLevel level, CookingPotBlockEntity pot, int ticks) { for (int i = 0; i < ticks; i++) CookingPotBlockEntity.tick(level, pot.getBlockPos(), pot.getBlockState(), pot); }
    private static int count(ServerLevel level, BlockPos pos, Item item) { return level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4)).stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum(); }
    private static void clear(ServerLevel level, BlockPos pos) { level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4)).forEach(ItemEntity::discard); }
    private static void check(boolean ok, String message) { if (!ok) throw new IllegalStateException("Cooking verification failed: " + message); }
}
