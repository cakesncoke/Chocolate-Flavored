package com.akiotsukino.chocolateflavored.lighting;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class FireStarterItem extends Item {
    public static final int USE_TICKS = 10 * 20;
    private static final Map<LivingEntity, Attempt> ATTEMPTS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final class Attempt {
        final BlockPos pos;
        final Direction face;
        final Block block;
        final ResourceKey<Level> dimension;
        final ItemStack stack;
        final InteractionHand hand;
        int elapsed;
        Attempt(UseOnContext context) {
            pos = context.getClickedPos().immutable(); face = context.getClickedFace();
            block = context.getLevel().getBlockState(pos).getBlock(); dimension = context.getLevel().dimension();
            stack = context.getItemInHand(); hand = context.getHand();
        }
    }
    public FireStarterItem(Properties properties) { super(properties); }
    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !LightingIgnition.fireStarterTarget(context.getLevel().getBlockState(context.getClickedPos()))) return InteractionResult.PASS;
        if (!LightingIgnition.permitted(player, context.getLevel(), context.getClickedPos(), context.getClickedFace(), context.getItemInHand())) return InteractionResult.FAIL;
        ATTEMPTS.put(player, new Attempt(context));
        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }
    @Override public int getUseDuration(ItemStack stack, LivingEntity user) { return USE_TICKS; }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) { return ItemUseAnimation.NONE; }
    private static boolean valid(Player player, ItemStack stack, Attempt attempt) {
        if (attempt == null || attempt.stack != stack || player.getItemInHand(attempt.hand) != stack
                || player.level().dimension() != attempt.dimension || !player.level().getBlockState(attempt.pos).is(attempt.block)
                || !LightingIgnition.fireStarterTarget(player.level().getBlockState(attempt.pos))
                || !LightingIgnition.permitted(player, player.level(), attempt.pos, attempt.face, stack)) return false;
        HitResult sight = player.pick(player.blockInteractionRange(), 1.0F, false);
        return sight instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(attempt.pos);
    }
    @Override public void onUseTick(Level level, LivingEntity user, ItemStack stack, int ticksRemaining) {
        if (!(user instanceof Player player)) return;
        Attempt attempt = ATTEMPTS.get(user);
        if (ticksRemaining < 1 || ticksRemaining > USE_TICKS || !valid(player, stack, attempt)) {
            ATTEMPTS.remove(user); user.releaseUsingItem(); return;
        }
        attempt.elapsed = USE_TICKS - ticksRemaining + 1;
        if (attempt.elapsed % 4 != 0) return;
        // BTW's primitive starter uses ordinary use swings, high-pitched eating sounds,
        // and item-chip particles. Repeat those effects over the requested hold duration.
        player.swing(attempt.hand, true);
        if (level instanceof ServerLevel server) {
            server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EAT,
                    SoundSource.PLAYERS, 0.5F + 0.5F * server.getRandom().nextInt(2), 1.75F + server.getRandom().nextFloat() * 0.25F);
            Vec3 point = player.getEyePosition().add(player.getLookAngle().scale(0.6)).add(0, -0.3, 0);
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack.getItem()), point.x, point.y, point.z, 5, 0.15, 0.15, 0.1, 0.05);
        }
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        Attempt attempt = ATTEMPTS.remove(user);
        if (!(user instanceof Player player) || attempt == null || attempt.elapsed < USE_TICKS || !valid(player, stack, attempt)) return stack;
        if (level.isClientSide()) return stack;
        // Re-check interaction protections because access may have changed during the ten-second hold.
        var hit = new BlockHitResult(Vec3.atCenterOf(attempt.pos), attempt.face, attempt.pos, false);
        var permission = new PlayerInteractEvent.RightClickBlock(player, attempt.hand, attempt.pos, hit);
        NeoForge.EVENT_BUS.post(permission);
        if (permission.isCanceled() || permission.getUseItem() == TriState.FALSE || permission.getUseBlock() == TriState.FALSE) return stack;
        if (LightingIgnition.ignite(level, attempt.pos, player)) {
            level.playSound(null, attempt.pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            stack.hurtAndBreak(1, player, attempt.hand.asEquipmentSlot());
        }
        return stack;
    }
    @Override public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int ticksRemaining) {
        ATTEMPTS.remove(user);
        return false;
    }
}
