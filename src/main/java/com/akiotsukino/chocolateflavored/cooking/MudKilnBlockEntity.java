package com.akiotsukino.chocolateflavored.cooking;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class MudKilnBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    private final int[] progress = new int[6];
    private final RecipeManager.CachedCheck<SingleRecipeInput, CookingRecipes.MudKilnRecipe> custom = RecipeManager.createCheck(CookingRecipes.MUD_KILN.get());
    private final RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> campfire = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);
    public record CookingResult(ItemStack result, int inputCount, int time, float experience) {}
    public MudKilnBlockEntity(BlockPos pos, BlockState state) { super(CookingContent.MUD_KILN_ENTITY.get(), pos, state); }
    public NonNullList<ItemStack> items() { return items; }
    public int progress(int slot) { return progress[slot]; }
    public Optional<CookingResult> recipe(ServerLevel level, ItemStack stack) {
        var input = new SingleRecipeInput(stack);
        var own = custom.getRecipeFor(input, level);
        if (own.isPresent()) {
            var r = own.get().value();
            return Optional.of(new CookingResult(r.assemble(input), r.inputCount(), r.cookingTime(), r.experience()));
        }
        return campfire.getRecipeFor(input, level).map(holder -> new CookingResult(holder.value().assemble(input), 1, holder.value().cookingTime(), holder.value().experience()));
    }
    public boolean placeFood(ServerLevel level, Player player, ItemStack held) {
        var recipe = recipe(level, held);
        if (recipe.isEmpty() || recipe.get().result().isEmpty()) return false;
        for (int slot = 0; slot < 6; slot++) if (items.get(slot).isEmpty()) {
            items.set(slot, held.consumeAndReturn(recipe.get().inputCount(), player));
            progress[slot] = 0; changed(); return true;
        }
        return false;
    }
    public static void tick(ServerLevel level, BlockPos pos, BlockState state, MudKilnBlockEntity mud_kiln) {
        if (MudKilnBlock.isCovered(level, pos)) {
            if (mud_kiln.items.stream().anyMatch(s -> !s.isEmpty())) { mud_kiln.dropItems(); mud_kiln.changed(); }
            return;
        }
        boolean changed = false;
        for (int slot = 0; slot < 6; slot++) {
            var ingredient = mud_kiln.items.get(slot);
            if (ingredient.isEmpty()) continue;
            changed = true;
            if (!state.getValue(MudKilnBlock.LIT)) { mud_kiln.progress[slot] = Math.max(0, mud_kiln.progress[slot] - 2); continue; }
            var recipe = mud_kiln.recipe(level, ingredient);
            if (recipe.isEmpty()) { Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, ingredient); mud_kiln.items.set(slot, ItemStack.EMPTY); mud_kiln.changed(); continue; }
            if (++mud_kiln.progress[slot] < recipe.get().time()) continue;
            var result = recipe.get();
            if (result.result().isEmpty() || !result.result().isItemEnabled(level.enabledFeatures())) continue;
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, result.result());
            var remainder = ingredient.getCraftingRemainder();
            if (remainder != null) for (int i = 0; i < result.inputCount(); i++) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, remainder.create());
            // A datapack reload may reduce the required batch size while food is already placed.
            if (ingredient.getCount() > result.inputCount()) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, ingredient.copyWithCount(ingredient.getCount() - result.inputCount()));
            float xp = result.experience(); int whole = (int)xp;
            if (level.getRandom().nextFloat() < xp - whole) whole++;
            if (whole > 0) ExperienceOrb.award(level, Vec3.atCenterOf(pos), whole);
            mud_kiln.items.set(slot, ItemStack.EMPTY); mud_kiln.progress[slot] = 0; mud_kiln.changed();
        }
        if (changed) mud_kiln.setChanged();
    }
    private void dropItems() { if (level != null) Containers.dropContents(level, worldPosition.above(), items); items.clear(); Arrays.fill(progress, 0); }
    private void changed() { setChanged(); if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
    @Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); items.clear(); ContainerHelper.loadAllItems(input, items); Arrays.fill(progress, 0); input.getIntArray("CookingTimes").ifPresent(a -> System.arraycopy(a, 0, progress, 0, Math.min(a.length, 6))); }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); ContainerHelper.saveAllItems(output, items); output.putIntArray("CookingTimes", progress); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { dropItems(); }
}
