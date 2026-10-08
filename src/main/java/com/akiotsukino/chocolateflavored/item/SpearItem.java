package com.akiotsukino.chocolateflavored.item;

import com.akiotsukino.chocolateflavored.event.SpearCombat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Player weapon behavior backported from the 1.21.11 spear component rules. */
public final class SpearItem extends Item {
    public enum Material {
        IRON(Tiers.IRON, 375, .95F, .95F, 12, 50, 8F, 135, 225),
        DIAMOND(Tiers.DIAMOND, 3122, 1.05F, 1.075F, 10, 60, 7.5F, 130, 200),
        NETHERITE(Tiers.NETHERITE, 6093, 1.15F, 1.2F, 8, 50, 7F, 110, 175);

        public final Tier tier;
        public final int durability, delay, dismountDuration, knockbackDuration, damageDuration;
        public final float jabSeconds, damageMultiplier, dismountSpeed;
        Material(Tier tier, int durability, float jabSeconds, float damageMultiplier, int delay,
                 int dismountDuration, float dismountSpeed, int knockbackDuration, int damageDuration) {
            this.tier = tier; this.durability = durability; this.jabSeconds = jabSeconds;
            this.damageMultiplier = damageMultiplier; this.delay = delay;
            this.dismountDuration = dismountDuration; this.dismountSpeed = dismountSpeed;
            this.knockbackDuration = knockbackDuration; this.damageDuration = damageDuration;
        }
        public int jabTicks() { return (int) (jabSeconds * 20); }
    }
    private final Material material;
    public SpearItem(Material material) {
        super(properties(material));
        this.material = material;
    }
    private static Properties properties(Material material) {
        Properties props = new Properties().durability(material.durability).attributes(
                SwordItem.createAttributes(material.tier, 0, 1F / material.jabSeconds - 4F));
        if (material == Material.NETHERITE) props.fireResistant();
        return props;
    }
    @Override public int getEnchantmentValue() { return material.tier.getEnchantmentValue(); }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return material.tier.getRepairIngredient().test(ingredient) || super.isValidRepairItem(stack, ingredient);
    }
    public Material material() { return material; }
    @Override public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) { return false; }
    @Override public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.SWEEPING_EDGE) && super.supportsEnchantment(stack, enchantment);
    }
    @Override public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.SWEEPING_EDGE) && super.isPrimaryItemFor(stack, enchantment);
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.SPEAR; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity user) { return material.delay + material.damageDuration; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isSpectator()) return InteractionResultHolder.pass(player.getItemInHand(hand));
        player.startUsingItem(hand);
        if (!level.isClientSide) SpearCombat.beginCharge(player);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
    @Override public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (!level.isClientSide && user instanceof Player player) {
            SpearCombat.charge(player, stack, getUseDuration(stack, user) - remaining);
        }
    }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        if (user instanceof Player player) SpearCombat.endCharge(player);
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (user instanceof Player player) SpearCombat.endCharge(player);
        return stack;
    }
}
