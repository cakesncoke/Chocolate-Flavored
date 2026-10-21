package com.akiotsukino.chocolateflavored.loot;

import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.content.ModTags;
import com.akiotsukino.chocolateflavored.gameplay.ItemRestrictions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class SurvivalLootModifier extends LootModifier {
    public static final MapCodec<SurvivalLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).and(instance.group(
            Codec.floatRange(0, 1).optionalFieldOf("fiber_chance", 0.33F).forGetter(m -> m.fiberChance),
            Codec.floatRange(0, 1).optionalFieldOf("extra_stick_chance", 0.20F).forGetter(m -> m.extraStickChance)
    )).apply(instance, SurvivalLootModifier::new));
    private final float fiberChance;
    private final float extraStickChance;

    public SurvivalLootModifier(LootItemCondition[] conditions, int priority, float fiberChance, float extraStickChance) {
        super(conditions, priority);
        this.fiberChance = fiberChance;
        this.extraStickChance = extraStickChance;
    }
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        loot.removeIf(ItemRestrictions::removed);
        BlockState state = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
        // Natural decay, explosions and structure loot retain their normal rules.
        if (state == null || !(context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player)) return loot;
        ItemInstance tool = context.getOptionalParameter(LootContextParams.TOOL);
        if (tool == null) tool = ItemStack.EMPTY;
        if (state.is(BlockTags.LOGS) && !tool.is(ItemTags.AXES)) {
            loot.clear();
            return loot;
        }
        if (tool.is(ModTags.KNIVES)) {
            if (state.is(ModTags.FIBER_PLANTS) && context.getRandom().nextFloat() < fiberChance) {
                loot.add(new ItemStack(ModContent.PLANT_FIBER.get()));
            }
            // Additional independent roll preserves vanilla saplings, apples, Fortune and stick drops.
            if (state.is(BlockTags.LEAVES) && context.getRandom().nextFloat() < extraStickChance) {
                loot.add(new ItemStack(Items.STICK, 1 + context.getRandom().nextInt(2)));
            }
        }
        return loot;
    }
    @Override
    public MapCodec<SurvivalLootModifier> codec() { return CODEC; }
}
