package com.akiotsukino.chocolateflavored.item;

// Tier and combat/enchantment behavior adapted from Farmer's Delight (MIT; see bundled license).
import com.akiotsukino.chocolateflavored.registry.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class FlintKnifeItem extends DiggerItem {
    private static final Tier FLINT = new Tier() {
        public int getUses() { return 131; }
        public float getSpeed() { return 4.0F; }
        public float getAttackDamageBonus() { return 1.0F; }
        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
        }
        public int getEnchantmentValue() { return 5; }
        public Ingredient getRepairIngredient() { return Ingredient.of(Items.FLINT); }
    };

    public FlintKnifeItem() {
        super(FLINT, ModTags.Blocks.MINEABLE_WITH_KNIFE,
                new Properties().attributes(DiggerItem.createAttributes(FLINT, 0.5F, -2.0F)));
    }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) { return true; }
    @Override public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }
    @Override public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.SWEEPING_EDGE) && super.isPrimaryItemFor(stack, enchantment);
    }
    @Override public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.SWEEPING_EDGE) && super.supportsEnchantment(stack, enchantment);
    }
}
