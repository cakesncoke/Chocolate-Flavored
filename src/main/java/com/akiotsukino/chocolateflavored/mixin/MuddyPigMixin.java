// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from EarthMobs' PigMixin; uses a serialized, synchronized NeoForge attachment instead of Bagus Lib packets.
package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.animal.MuddyPig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.IShearable;
import org.spongepowered.asm.mixin.*;

@Mixin(Pig.class)
public abstract class MuddyPigMixin extends Animal implements MuddyPig, IShearable {
    protected MuddyPigMixin(EntityType<? extends Animal> type, Level level) { super(type, level); }
    @Unique private int chocolateflavored$data() { return getData(com.akiotsukino.chocolateflavored.animal.AnimalContent.MUDDY_PIG_STATE); }
    @Unique private void chocolateflavored$bits(int mask, int value) {
        int old = chocolateflavored$data();
        int updated = old & ~mask | value & mask;
        if (old != updated) setData(com.akiotsukino.chocolateflavored.animal.AnimalContent.MUDDY_PIG_STATE, updated);
    }
    public boolean chocolateflavored$isMuddy() { return (chocolateflavored$data() & 1) != 0; }
    public void chocolateflavored$setMuddy(boolean value) { chocolateflavored$bits(1, value ? 1 : 0); }
    public boolean chocolateflavored$isSheared() { return (chocolateflavored$data() & 2) != 0; }
    public void chocolateflavored$setSheared(boolean value) { chocolateflavored$bits(2, value ? 2 : 0); }
    public DyeColor chocolateflavored$getFlowerColor() { return DyeColor.byId(chocolateflavored$data() >> 2 & 15); }
    public void chocolateflavored$setFlowerColor(DyeColor value) { chocolateflavored$bits(60, value.getId() << 2); }
    public int chocolateflavored$getShakeTicks() { int ticks = chocolateflavored$data() >> 6 & 127; return ticks > 64 ? 64 - ticks : ticks; }
    public void chocolateflavored$setShakeTicks(int value) { chocolateflavored$bits(8128, (value < 0 ? 64 - value : value) << 6); }
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
            Item dye = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(chocolateflavored$getFlowerColor().getName() + "_dye"));
            for (int i = 1 + random.nextInt(3); i > 0; --i) drops.add(new ItemStack(dye));
        }
        return drops;
    }
}
