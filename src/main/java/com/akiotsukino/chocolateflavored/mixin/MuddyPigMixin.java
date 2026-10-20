// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from EarthMobs' PigMixin; uses vanilla synced data instead of Bagus Lib packets.
package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.animal.MuddyPig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.IShearable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pig.class)
public abstract class MuddyPigMixin extends Animal implements MuddyPig, IShearable {
    @Unique private static final EntityDataAccessor<Boolean> CF_MUDDY = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.BOOLEAN);
    @Unique private static final EntityDataAccessor<Byte> CF_FLOWER = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.BYTE);
    @Unique private static final EntityDataAccessor<Integer> CF_SHAKE = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.INT);
    protected MuddyPigMixin(EntityType<? extends Animal> type, Level level) { super(type, level); }
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void chocolateflavored$data(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(CF_MUDDY, false); builder.define(CF_FLOWER, (byte) 0); builder.define(CF_SHAKE, 0);
    }
    public boolean chocolateflavored$isMuddy() { return entityData.get(CF_MUDDY); }
    public void chocolateflavored$setMuddy(boolean value) { entityData.set(CF_MUDDY, value); }
    public boolean chocolateflavored$isSheared() { return (entityData.get(CF_FLOWER) & 16) != 0; }
    public void chocolateflavored$setSheared(boolean value) { byte b = entityData.get(CF_FLOWER); entityData.set(CF_FLOWER, (byte) (value ? b | 16 : b & ~16)); }
    public DyeColor chocolateflavored$getFlowerColor() { return DyeColor.byId(entityData.get(CF_FLOWER) & 15); }
    public void chocolateflavored$setFlowerColor(DyeColor value) { entityData.set(CF_FLOWER, (byte) (entityData.get(CF_FLOWER) & 16 | value.getId())); }
    public int chocolateflavored$getShakeTicks() { return entityData.get(CF_SHAKE); }
    public void chocolateflavored$setShakeTicks(int value) { entityData.set(CF_SHAKE, value); }
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void chocolateflavored$save(ValueOutput out, CallbackInfo ci) {
        out.putBoolean("ChocolateMuddy", chocolateflavored$isMuddy());
        out.putByte("ChocolateFlower", entityData.get(CF_FLOWER));
    }
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void chocolateflavored$load(ValueInput in, CallbackInfo ci) {
        chocolateflavored$setMuddy(in.getBooleanOr("ChocolateMuddy", false));
        entityData.set(CF_FLOWER, in.getByteOr("ChocolateFlower", (byte) 0));
    }
    @Override public boolean isShearable(Player player, ItemStack item, Level level, BlockPos pos) {
        return isAlive() && !isBaby() && chocolateflavored$isMuddy() && !chocolateflavored$isSheared();
    }
    @Override public List<ItemStack> onSheared(Player player, ItemStack item, Level level, BlockPos pos) {
        List<ItemStack> drops = new ArrayList<>();
        if (!isShearable(player, item, level, pos)) return drops;
        level.playSound(null, this, SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1, 1);
        gameEvent(GameEvent.SHEAR, player);
        if (!level.isClientSide()) {
            chocolateflavored$setSheared(true);
            DyeItem dye = DyeItem.byColor(chocolateflavored$getFlowerColor());
            for (int i = 1 + random.nextInt(3); i > 0; --i) drops.add(new ItemStack(dye));
        }
        return drops;
    }
}
