package com.akiotsukino.chocolateflavored.cooking;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;

public final class CookingPotBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int MEAL = 6, CONTAINER = 7, OUTPUT = 8;
    public static final TagKey<Block> HEAT_SOURCES = tag("heat_sources"), HEAT_CONDUCTORS = tag("heat_conductors"), TRAY_HEAT_SOURCES = tag("tray_heat_sources");
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private ItemStack servingContainer = ItemStack.EMPTY;
    private ItemStack servingTemplate = ItemStack.EMPTY;
    private int progress, total = 200;
    private float experience;
    private String activeRecipe = "";
    private CookingRecipes.PotRecipe lastRecipe;
    private final RecipeManager.CachedCheck<CookingRecipes.PotInput, CookingRecipes.PotRecipe> recipes = RecipeManager.createCheck(CookingRecipes.COOKING_POT.get());
    public final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return switch (index) { case 0 -> progress; case 1 -> total; case 2 -> isHeated() ? 1 : 0; default -> 0; }; }
        @Override public void set(int index, int value) { if (index == 0) progress = value; if (index == 1) total = value; }
        @Override public int getCount() { return 3; }
    };
    public CookingPotBlockEntity(BlockPos pos, BlockState state) { super(CookingContent.POT_ENTITY.get(), pos, state); }
    private static TagKey<Block> tag(String path) { return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("chocolateflavored", path)); }
    @Override public int getContainerSize() { return 9; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> value) { items = value; }
    @Override public int getMaxStackSize(ItemStack stack) { return 64; }
    @Override protected Component getDefaultName() { return Component.translatable("container.chocolateflavored.cooking_pot"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new CookingPotMenu(id, inventory, this, data); }
    public ItemStack meal() { return items.get(MEAL); }
    public ItemStack servingContainer() { return servingContainer.copy(); }
    public CookingRecipes.PotInput input() { return new CookingRecipes.PotInput(items.subList(0, 6)); }
    @Override public void setItem(int slot, ItemStack stack) {
        boolean changed = slot < 6 && !ItemStack.isSameItemSameComponents(items.get(slot), stack);
        items.set(slot, stack); stack.limitSize(slot == MEAL ? 64 : Math.min(64, stack.getMaxStackSize()));
        if (changed) progress = 0;
        markUpdated();
    }
    @Override public ItemStack removeItem(int slot, int count) {
        var taken = super.removeItem(slot, count);
        if (slot < 6 && items.get(slot).isEmpty()) progress = 0;
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
        var matching = pot.recipes.getRecipeFor(pot.input(), level);
        boolean changed = false;
        if (matching.isPresent() && pot.isHeated() && pot.canCook(matching.get().value())) {
            var holder = matching.get(); var recipe = holder.value();
            String id = holder.id().identifier().toString();
            if (!id.equals(pot.activeRecipe) || pot.lastRecipe != null && pot.lastRecipe != recipe) pot.progress = 0;
            pot.activeRecipe = id; pot.lastRecipe = recipe; pot.total = recipe.cookingTime();
            if (++pot.progress >= pot.total) { pot.cook(recipe); pot.progress = 0; }
            changed = true;
        } else if (pot.progress > 0) { pot.progress = Math.max(0, pot.progress - 2); changed = true; }
        if (pot.serveStored()) changed = true;
        if (changed) pot.setChanged();
    }
    private boolean canCook(CookingRecipes.PotRecipe recipe) {
        var result = recipe.assemble(input());
        if (result.isEmpty() || level == null || !result.isItemEnabled(level.enabledFeatures()) || result.getCount() > 64 || result.has(DataComponents.MAX_DAMAGE)) return false;
        return meal().isEmpty() || ItemStack.isSameItemSameComponents(servingTemplate, result) && meal().getCount() + result.getCount() <= 64
                && ItemStack.matches(servingContainer, recipe.servingContainer());
    }
    private void cook(CookingRecipes.PotRecipe recipe) {
        var result = recipe.assemble(input());
        if (meal().isEmpty()) {
            servingTemplate = result.copyWithCount(1);
            result.set(DataComponents.MAX_STACK_SIZE, 64);
            items.set(MEAL, result);
        } else meal().grow(result.getCount());
        servingContainer = recipe.servingContainer();
        for (int slot = 0; slot < 6; slot++) {
            var ingredient = items.get(slot);
            if (ingredient.isEmpty()) continue;
            var remainder = ingredient.getCraftingRemainder();
            if (remainder == null) {
                var use = ingredient.get(DataComponents.USE_REMAINDER);
                if (use != null) remainder = use.convertInto();
            }
            ingredient.shrink(1);
            if (remainder != null) Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.7, worldPosition.getZ() + 0.5, remainder.create());
        }
        experience += recipe.experience(); markUpdated();
    }
    public boolean validContainer(ItemStack stack) {
        return !servingContainer.isEmpty() && ItemStack.isSameItemSameComponents(servingContainer, stack);
    }
    private boolean serveStored() {
        if (meal().isEmpty()) return false;
        var output = items.get(OUTPUT);
        if (!output.isEmpty() && !ItemStack.isSameItemSameComponents(servingTemplate, output)) return false;
        int space = servingTemplate.getMaxStackSize() - output.getCount();
        int amount = Math.min(space, meal().getCount());
        var bowls = items.get(CONTAINER);
        if (!servingContainer.isEmpty()) amount = validContainer(bowls) ? Math.min(amount, bowls.getCount()) : 0;
        if (amount <= 0) return false;
        if (!servingContainer.isEmpty()) bowls.shrink(amount);
        if (output.isEmpty()) items.set(OUTPUT, servingTemplate.copyWithCount(amount)); else output.grow(amount);
        meal().shrink(amount);
        if (meal().isEmpty()) { servingContainer = ItemStack.EMPTY; servingTemplate = ItemStack.EMPTY; }
        markUpdated(); return true;
    }
    public ItemStack serve(Player player, ItemStack held) {
        if (meal().isEmpty() || !validContainer(held)) return ItemStack.EMPTY;
        held.consume(1, player); var result = servingTemplate.copy(); meal().shrink(1);
        if (meal().isEmpty()) { servingContainer = ItemStack.EMPTY; servingTemplate = ItemStack.EMPTY; }
        awardExperience(player); markUpdated(); return result;
    }
    public void awardExperience(Player player) {
        if (level instanceof ServerLevel server && experience > 0) {
            int whole = (int)experience; if (server.getRandom().nextFloat() < experience - whole) whole++;
            if (whole > 0) ExperienceOrb.award(server, player == null ? Vec3.atCenterOf(worldPosition) : player.position(), whole);
            experience = 0; setChanged();
        }
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot < 6 || slot == CONTAINER && (meal().isEmpty() || validContainer(stack)); }
    @Override public int[] getSlotsForFace(Direction direction) { return direction == Direction.UP ? new int[]{0, 1, 2, 3, 4, 5} : direction == Direction.DOWN ? new int[]{OUTPUT} : new int[]{CONTAINER, OUTPUT}; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction face) { return face == Direction.UP ? slot < 6 : face != Direction.DOWN && slot == CONTAINER && canPlaceItem(slot, stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) { return slot == OUTPUT; }
    private void markUpdated() {
        setChanged(); if (level != null) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock()); }
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); items.clear(); ContainerHelper.loadAllItems(input, items);
        progress = Math.max(0, input.getIntOr("CookTime", 0)); total = Math.max(1, input.getIntOr("CookTimeTotal", 200));
        servingContainer = input.read("ServingContainer", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        servingTemplate = input.read("MealTemplate", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        experience = Math.max(0, input.getFloatOr("Experience", 0)); activeRecipe = input.getStringOr("ActiveRecipe", ""); lastRecipe = null;
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output); ContainerHelper.saveAllItems(output, items);
        output.putInt("CookTime", progress); output.putInt("CookTimeTotal", total); output.store("ServingContainer", ItemStack.OPTIONAL_CODEC, servingContainer);
        output.store("MealTemplate", ItemStack.OPTIONAL_CODEC, servingTemplate);
        output.putFloat("Experience", experience); output.putString("ActiveRecipe", activeRecipe);
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) {
            for (int slot = 0; slot < 9; slot++) if (slot != MEAL) { Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, items.get(slot)); items.set(slot, ItemStack.EMPTY); }
            awardExperience(null);
        }
    }
    @Override protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        var packed = NonNullList.withSize(9, ItemStack.EMPTY); packed.set(MEAL, meal().copy());
        components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(packed));
        components.set(CookingContent.MEAL_TEMPLATE.get(), ItemContainerContents.fromItems(List.of(servingTemplate)));
        components.set(CookingContent.MEAL_CONTAINER.get(), ItemContainerContents.fromItems(List.of(servingContainer)));
    }
    @Override protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        servingContainer = components.getOrDefault(CookingContent.MEAL_CONTAINER.get(), ItemContainerContents.EMPTY).copyOne();
        servingTemplate = components.getOrDefault(CookingContent.MEAL_TEMPLATE.get(), ItemContainerContents.EMPTY).copyOne();
    }
    @Override public void removeComponentsFromTag(ValueOutput output) { super.removeComponentsFromTag(output); output.discard("ServingContainer"); output.discard("MealTemplate"); }
}
