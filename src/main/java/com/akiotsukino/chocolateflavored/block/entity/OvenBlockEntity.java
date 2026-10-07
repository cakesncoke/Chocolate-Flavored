package com.akiotsukino.chocolateflavored.block.entity;

// Cooking layout, cooling, output motion and smoke adapted from Farmer's Delight.
// Copyright (c) 2020 vectorwing. MIT; see THIRD_PARTY_NOTICES.md and licenses/FarmersDelight-MIT.txt.

import com.akiotsukino.chocolateflavored.block.OvenBlock;
import com.akiotsukino.chocolateflavored.recipe.OvenRecipe;
import com.akiotsukino.chocolateflavored.registry.ModBlockEntities;
import com.akiotsukino.chocolateflavored.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Optional;

public class OvenBlockEntity extends BlockEntity {
    public static final int COOKING_SLOTS = 6;
    private static final Vec2[] OFFSETS = {
            new Vec2(0.3F, 0.2F), new Vec2(0, 0.2F), new Vec2(-0.3F, 0.2F),
            new Vec2(0.3F, -0.2F), new Vec2(0, -0.2F), new Vec2(-0.3F, -0.2F)
    };
    private final ItemStackHandler items = new ItemStackHandler(COOKING_SLOTS) {
        @Override
        public int getSlotLimit(int slot) { return 1; }
    };
    private final ItemStackHandler fuel = new ItemStackHandler(1);
    private final int[] progress = new int[COOKING_SLOTS];
    private final int[] totalTime = new int[COOKING_SLOTS];
    private int burnTime;
    private final RecipeManager.CachedCheck<SingleRecipeInput, OvenRecipe> ovenRecipes = RecipeManager.createCheck(ModRecipes.OVEN_TYPE.get());
    private final RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> campfireRecipes = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);

    public OvenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OVEN.get(), pos, state);
    }

    public ItemStackHandler getItems() { return items; }
    public Vec2 getItemOffset(int index) { return OFFSETS[index]; }
    public int getBurnTime() { return burnTime; }
    public ItemStack getFuel() { return fuel.getStackInSlot(0); }

    public static int fuelTicks(ItemStack stack) {
        return stack.isEmpty() ? 0 : Math.max(0, stack.getBurnTime(RecipeType.SMELTING));
    }

    /** Resolves Oven recipes first, then any loaded campfire recipe. Never imports furnace recipes. */
    public Optional<CookingResult> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) return Optional.empty();
        SingleRecipeInput input = new SingleRecipeInput(stack);
        var custom = ovenRecipes.getRecipeFor(input, level);
        if (custom.isPresent()) {
            OvenRecipe recipe = custom.get().value();
            return Optional.of(new CookingResult(recipe.assemble(input, level.registryAccess()), recipe.cookingTime()));
        }
        return campfireRecipes.getRecipeFor(input, level).map(holder -> new CookingResult(
                holder.value().assemble(input, level.registryAccess()), holder.value().getCookingTime()));
    }

    public boolean placeFood(ItemStack held, boolean creative) {
        if (level == null || level.isClientSide || OvenBlock.isTopCovered(level, worldPosition)) return false;
        var recipe = findRecipe(held);
        if (recipe.isEmpty() || !recipe.get().result().isItemEnabled(level.enabledFeatures())) return false;
        for (int i = 0; i < COOKING_SLOTS; i++) {
            if (!items.getStackInSlot(i).isEmpty()) continue;
            items.setStackInSlot(i, held.copyWithCount(1));
            if (!creative) held.shrink(1);
            progress[i] = 0;
            totalTime[i] = Math.max(1, recipe.get().ticks());
            inventoryChanged();
            return true;
        }
        return false;
    }

    public int addFuel(ItemStack held, boolean wholeStack, boolean creative) {
        if (level == null || level.isClientSide || fuelTicks(held) == 0) return 0;
        int requested = wholeStack ? held.getCount() : 1;
        ItemStack remainder = fuel.insertItem(0, held.copyWithCount(requested), false);
        int inserted = requested - remainder.getCount();
        if (inserted > 0) {
            if (!creative) held.shrink(inserted);
            inventoryChanged();
        }
        return inserted;
    }

    /** Called only by an allowed ignition item; fuel insertion never calls this. */
    public boolean ignite() {
        if (level == null || level.isClientSide || getBlockState().getValue(OvenBlock.LIT)) return false;
        if (!consumeNextFuel()) return false;
        setLit(true);
        return true;
    }

    public void extinguish() {
        if (level == null || level.isClientSide) return;
        // Extinguishing discards the currently burning unit, but keeps queued fuel.
        burnTime = 0;
        setLit(false);
    }

    private boolean consumeNextFuel() {
        ItemStack stack = getFuel();
        int ticks = fuelTicks(stack);
        if (ticks <= 0) return false;
        ItemStack consumed = fuel.extractItem(0, 1, false);
        burnTime = ticks;
        ItemStack remainder = consumed.getCraftingRemainingItem();
        if (!remainder.isEmpty()) spawnOutput(remainder); // e.g. lava bucket -> empty bucket
        inventoryChanged();
        return true;
    }

    private void setLit(boolean lit) {
        if (level == null) return;
        level.setBlock(worldPosition, getBlockState().setValue(OvenBlock.LIT, lit), Block.UPDATE_ALL);
        level.gameEvent(GameEvent.BLOCK_CHANGE, worldPosition, GameEvent.Context.of(getBlockState()));
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OvenBlockEntity oven) {
        if (OvenBlock.isTopCovered(level, pos)) oven.dropFood();
        if (!state.getValue(OvenBlock.LIT)) {
            for (int i = 0; i < COOKING_SLOTS; i++) {
                if (oven.progress[i] > 0) {
                    oven.progress[i] = Math.max(0, oven.progress[i] - 2);
                    oven.setChanged();
                }
            }
            return;
        }
        if (oven.burnTime <= 0 && !oven.consumeNextFuel()) {
            oven.extinguish();
            return;
        }
        oven.burnTime--;
        for (int i = 0; i < COOKING_SLOTS; i++) {
            ItemStack ingredient = oven.items.getStackInSlot(i);
            if (ingredient.isEmpty()) continue;
            if (++oven.progress[i] < oven.totalTime[i]) continue;
            ItemStack output = oven.findRecipe(ingredient).map(CookingResult::result).orElseGet(ingredient::copy);
            if (!output.isItemEnabled(level.enabledFeatures())) continue;
            oven.items.setStackInSlot(i, ItemStack.EMPTY);
            oven.progress[i] = 0;
            oven.totalTime[i] = 0;
            oven.spawnOutput(output);
            oven.inventoryChanged();
        }
        // No relighting merely by adding fuel after the flame has gone out.
        if (oven.burnTime == 0 && fuelTicks(oven.getFuel()) == 0) oven.setLit(false);
        oven.setChanged();
    }

    public ItemStack takeFood() {
        for (int i = COOKING_SLOTS - 1; i >= 0; i--) {
            if (items.getStackInSlot(i).isEmpty()) continue;
            ItemStack result = items.extractItem(i, 1, false);
            progress[i] = 0;
            totalTime[i] = 0;
            inventoryChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    public ItemStack takeFuel() {
        ItemStack result = fuel.extractItem(0, 64, false);
        if (!result.isEmpty()) inventoryChanged();
        return result;
    }

    private void dropFood() {
        ItemStack stack;
        while (!(stack = takeFood()).isEmpty()) spawnOutput(stack);
    }

    public void dropContents() {
        dropFood();
        ItemStack remainingFuel = takeFuel();
        if (!remainingFuel.isEmpty()) spawnOutput(remainingFuel);
    }

    private void spawnOutput(ItemStack stack) {
        if (level == null || level.isClientSide || stack.isEmpty()) return;
        ItemEntity entity = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5, stack.copy());
        entity.setDeltaMovement(level.random.nextGaussian() * 0.01, 0.1, level.random.nextGaussian() * 0.01);
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);
    }

    private void inventoryChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Food", items.serializeNBT(registries));
        tag.put("Fuel", fuel.serializeNBT(registries));
        tag.putInt("BurnTime", burnTime);
        tag.putIntArray("Progress", progress);
        tag.putIntArray("TotalTime", totalTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Food"));
        fuel.deserializeNBT(registries, tag.getCompound("Fuel"));
        burnTime = Math.max(0, tag.getInt("BurnTime"));
        readTimes(tag.getIntArray("Progress"), progress);
        readTimes(tag.getIntArray("TotalTime"), totalTime);
    }

    private static void readTimes(int[] saved, int[] target) {
        for (int i = 0; i < target.length; i++) target[i] = i < saved.length ? Math.max(0, saved[i]) : 0;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }

    public static void particleTick(Level level, BlockPos pos, BlockState state, OvenBlockEntity oven) {
        if (!state.getValue(OvenBlock.LIT)) return;
        for (int i = 0; i < COOKING_SLOTS; i++) {
            if (oven.items.getStackInSlot(i).isEmpty() || level.random.nextFloat() >= 0.2F) continue;
            Vec2 offset = OFFSETS[i];
            Direction facing = state.getValue(OvenBlock.FACING);
            if (facing.get2DDataValue() % 2 != 0) offset = new Vec2(offset.y, offset.x);
            double x = pos.getX() + 0.5 - facing.getStepX() * offset.x + facing.getClockWise().getStepX() * offset.x;
            double z = pos.getZ() + 0.5 - facing.getStepZ() * offset.y + facing.getClockWise().getStepZ() * offset.y;
            for (int k = 0; k < 3; k++) level.addParticle(ParticleTypes.SMOKE, x, pos.getY() + 1.0, z, 0, 5.0E-4, 0);
        }
    }

    public record CookingResult(ItemStack result, int ticks) {}
}
