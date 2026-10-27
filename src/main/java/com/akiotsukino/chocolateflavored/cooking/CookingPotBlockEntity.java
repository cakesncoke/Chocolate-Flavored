package com.akiotsukino.chocolateflavored.cooking;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.*;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;

public final class CookingPotBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int INPUTS = 6, OUTPUT = 6, SIZE = 12;
    public static final TagKey<Block> HEAT_SOURCES = tag("heat_sources"), HEAT_CONDUCTORS = tag("heat_conductors"), TRAY_HEAT_SOURCES = tag("tray_heat_sources");
    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    // Only used to return contents migrated from the previous nine-slot/meal-buffer design.
    private final List<ItemStack> legacyReturns = new ArrayList<>();
    private int progress;
    private float experience;
    private String activeRecipe = "";
    private CookingRecipes.PotRecipe lastRecipe;
    private final RecipeManager.CachedCheck<CookingRecipes.PotInput, CookingRecipes.PotRecipe> recipes = RecipeManager.createCheck(CookingRecipes.COOKING_POT.get());
    public final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return switch (index) { case 0 -> progress; case 1 -> CookingRecipes.PotRecipe.COOKING_TIME; case 2 -> isHeated() ? 1 : 0; default -> 0; }; }
        @Override public void set(int index, int value) { if (index == 0) progress = value; }
        @Override public int getCount() { return 3; }
    };
    public CookingPotBlockEntity(BlockPos pos, BlockState state) { super(CookingContent.POT_ENTITY.get(), pos, state); }
    private static TagKey<Block> tag(String path) { return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("chocolateflavored", path)); }
    @Override public int getContainerSize() { return SIZE; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> value) { items = value; }
    @Override protected Component getDefaultName() { return Component.translatable("container.chocolateflavored.cooking_pot"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new CookingPotMenu(id, inventory, this, data); }
    public boolean hasOutputs() { return !legacyReturns.isEmpty() || items.subList(OUTPUT, SIZE).stream().anyMatch(stack -> !stack.isEmpty()); }
    public CookingRecipes.PotInput input() { return new CookingRecipes.PotInput(items.subList(0, INPUTS)); }
    @Override public void setItem(int slot, ItemStack stack) {
        boolean changed = slot < INPUTS && !ItemStack.isSameItemSameComponents(items.get(slot), stack);
        items.set(slot, stack); stack.limitSize(Math.min(64, stack.getMaxStackSize()));
        if (changed) progress = 0;
        markUpdated();
    }
    @Override public ItemStack removeItem(int slot, int count) {
        var taken = super.removeItem(slot, count);
        if (slot < INPUTS && items.get(slot).isEmpty()) progress = 0;
        if (!taken.isEmpty()) markUpdated();
        return taken;
    }
    public static boolean isHeatSource(BlockState state) { return state.is(HEAT_SOURCES) && (!state.hasProperty(BlockStateProperties.LIT) || state.getValue(BlockStateProperties.LIT)); }
    public boolean isHeated() {
        if (level == null) return false;
        var below = level.getBlockState(worldPosition.below());
        return isHeatSource(below) || below.is(HEAT_CONDUCTORS) && isHeatSource(level.getBlockState(worldPosition.below(2)));
    }
    public static void tick(ServerLevel level, BlockPos pos, BlockState state, CookingPotBlockEntity pot) {
        boolean changed = pot.flushLegacy();
        var matching = pot.recipes.getRecipeFor(pot.input(), level);
        if (matching.isPresent() && pot.isHeated() && pot.legacyReturns.isEmpty()) {
            var holder = matching.get(); var recipe = holder.value();
            var plan = pot.planOutputs(recipe);
            if (plan != null) {
                String id = holder.id().identifier().toString();
                if (!id.equals(pot.activeRecipe) || pot.lastRecipe != null && pot.lastRecipe != recipe) pot.progress = 0;
                pot.activeRecipe = id; pot.lastRecipe = recipe;
                if (++pot.progress >= CookingRecipes.PotRecipe.COOKING_TIME) { pot.cook(recipe, plan); pot.progress = 0; }
                changed = true;
            } else if (pot.progress > 0) { pot.progress = Math.max(0, pot.progress - 2); changed = true; }
        } else if (pot.progress > 0) { pot.progress = Math.max(0, pot.progress - 2); changed = true; }
        if (changed) pot.setChanged();
    }
    /** Simulate all outputs before consuming any input; blocked batches are atomic. */
    private List<ItemStack> planOutputs(CookingRecipes.PotRecipe recipe) {
        if (level == null) return null;
        var plan = new ArrayList<ItemStack>();
        for (int slot = OUTPUT; slot < SIZE; slot++) plan.add(items.get(slot).copy());
        for (var result : recipe.outputs()) {
            if (result.isEmpty() || !result.isItemEnabled(level.enabledFeatures()) || result.getCount() > Math.min(64, result.getMaxStackSize())) return null;
            if (insert(plan, result) > 0) return null;
        }
        return plan;
    }
    private static int insert(List<ItemStack> slots, ItemStack value) {
        int remaining = value.getCount();
        for (int pass = 0; pass < 2; pass++) for (int slot = 0; slot < slots.size(); slot++) {
            var existing = slots.get(slot);
            if (pass == 0 && !existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, value)) {
                int amount = Math.min(remaining, Math.min(64, existing.getMaxStackSize()) - existing.getCount());
                if (amount > 0) { existing.grow(amount); remaining -= amount; }
            } else if (pass == 1 && existing.isEmpty()) {
                int amount = Math.min(remaining, Math.min(64, value.getMaxStackSize()));
                if (amount > 0) { slots.set(slot, value.copyWithCount(amount)); remaining -= amount; }
            }
            if (remaining == 0) return 0;
        }
        return remaining;
    }
    private void cook(CookingRecipes.PotRecipe recipe, List<ItemStack> plan) {
        int[] assignment = recipe.assignment(input());
        if (assignment == null) return;
        for (int i = 0; i < assignment.length; i++) {
            var ingredient = items.get(assignment[i]);
            var remainder = ingredient.getCraftingRemainder();
            if (remainder == null) {
                var use = ingredient.get(DataComponents.USE_REMAINDER);
                if (use != null) remainder = use.convertInto();
            }
            int count = recipe.ingredients().get(i).count();
            ingredient.shrink(count);
            if (remainder != null) for (int used = 0; used < count; used++)
                Containers.dropItemStack(level, worldPosition.getX() + .5, worldPosition.getY() + .7, worldPosition.getZ() + .5, remainder.create());
        }
        for (int slot = 0; slot < INPUTS; slot++) items.set(OUTPUT + slot, plan.get(slot));
        experience += recipe.experience(); markUpdated();
    }
    private boolean flushLegacy() {
        if (legacyReturns.isEmpty()) return false;
        var plan = new ArrayList<ItemStack>();
        for (int slot = OUTPUT; slot < SIZE; slot++) plan.add(items.get(slot).copy());
        boolean changed = false;
        var iterator = legacyReturns.iterator();
        while (iterator.hasNext()) {
            var stack = iterator.next();
            int left = insert(plan, stack);
            if (left != stack.getCount()) { changed = true; stack.setCount(left); }
            if (stack.isEmpty()) iterator.remove();
        }
        if (changed) for (int slot = 0; slot < INPUTS; slot++) items.set(OUTPUT + slot, plan.get(slot));
        return changed;
    }
    public void awardExperience(Player player) {
        if (level instanceof ServerLevel server && experience > 0) {
            int whole = (int)experience; if (server.getRandom().nextFloat() < experience - whole) whole++;
            if (whole > 0) ExperienceOrb.award(server, player == null ? Vec3.atCenterOf(worldPosition) : player.position(), whole);
            experience = 0; setChanged();
        }
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot >= 0 && slot < INPUTS; }
    @Override public int[] getSlotsForFace(Direction direction) { return direction == Direction.DOWN ? new int[]{6,7,8,9,10,11} : new int[]{0,1,2,3,4,5}; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction face) { return face != Direction.DOWN && canPlaceItem(slot, stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) { return slot >= OUTPUT && slot < SIZE; }
    private void markUpdated() {
        setChanged(); if (level != null) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock()); }
    }
    private void queue(ItemStack template, int count) {
        while (count > 0 && !template.isEmpty()) {
            int amount = Math.min(count, template.getMaxStackSize());
            legacyReturns.add(template.copyWithCount(amount)); count -= amount;
        }
    }
    private void migrateLegacy(ItemStack template) {
        var meal = items.get(6);
        if (!meal.isEmpty()) {
            if (template.isEmpty()) { template = meal.copyWithCount(1); template.remove(DataComponents.MAX_STACK_SIZE); }
            queue(template, meal.getCount());
        }
        queue(items.get(7), items.get(7).getCount()); queue(items.get(8), items.get(8).getCount());
        for (int i = 6; i < SIZE; i++) items.set(i, ItemStack.EMPTY);
        progress = 0;
        flushLegacy();
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); items.clear(); ContainerHelper.loadAllItems(input, items);
        legacyReturns.clear(); legacyReturns.addAll(input.read("LegacyReturns", ItemStack.CODEC.listOf()).orElse(List.of()));
        progress = Math.clamp(input.getIntOr("CookTime", 0), 0, CookingRecipes.PotRecipe.COOKING_TIME - 1);
        experience = Math.max(0, input.getFloatOr("Experience", 0)); activeRecipe = input.getStringOr("ActiveRecipe", ""); lastRecipe = null;
        if (input.getIntOr("PotInventoryVersion", 1) < 2)
            migrateLegacy(input.read("MealTemplate", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output); ContainerHelper.saveAllItems(output, items);
        output.putInt("PotInventoryVersion", 2); output.putInt("CookTime", progress);
        output.putFloat("Experience", experience); output.putString("ActiveRecipe", activeRecipe);
        output.store("LegacyReturns", ItemStack.CODEC.listOf(), legacyReturns);
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) {
            for (var stack : items) Containers.dropItemStack(level, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, stack);
            for (var stack : legacyReturns) Containers.dropItemStack(level, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, stack);
            items.clear(); legacyReturns.clear(); awardExperience(null);
        }
    }
    @Override protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        // Contents drop separately, just as in a vanilla furnace; never also copy them into the pot item.
        components.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
    }
    @Override protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        var old = components.get(CookingContent.MEAL_TEMPLATE.get());
        if (old != null || components.get(CookingContent.MEAL_CONTAINER.get()) != null)
            migrateLegacy(old == null ? ItemStack.EMPTY : old.copyOne());
    }
}
