package com.akiotsukino.chocolateflavored.trim;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
import static net.minecraft.world.entity.EquipmentSlot.*;
@EventBusSubscriber(modid=ChocolateFlavored.MODID)
public final class TrimMining {
    private static final ThreadLocal<Boolean> MINING = ThreadLocal.withInitial(() -> false);
    @SubscribeEvent public static void speed(PlayerEvent.BreakSpeed e) {
        var p=e.getEntity(); var tool=p.getMainHandItem(); double bonus=0;
        if (trail(p,CHEST) && tool.getDestroySpeed(e.getState()) > 1 && (!e.getState().requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(e.getState()))) bonus+=.1;
        if (has(p,FEET,"bolt") && tool.is(BOLT_TOOLS)) bonus+=.2;
        if (full(p,"bolt") && storm(p) && tool.is(BOLT_TOOLS)) bonus+=.25;
        e.setNewSpeed((float)(e.getNewSpeed()*(1+bonus)));
    }
    public static void afterBreak(ServerPlayer p, BlockPos start, BlockState original) {
        if (MINING.get() || p.isCreative() || p.isSpectator() || !has(p,CHEST,"eye") || !original.is(ORES) || p.level().getBlockState(start).equals(original)) return;
        if (!p.getMainHandItem().isCorrectToolForDrops(original)) return;
        MINING.set(true);
        try {
            Set<BlockPos> seen=new HashSet<>(); ArrayDeque<BlockPos> queue=new ArrayDeque<>(); seen.add(start); queue.add(start); int broken=1;
            while (!queue.isEmpty() && broken<16 && !p.getMainHandItem().isEmpty()) {
                BlockPos from=queue.removeFirst();
                for (Direction d:Direction.values()) {
                    BlockPos pos=from.relative(d);
                    if (broken>=16 || !seen.add(pos) || !p.level().hasChunkAt(pos)) continue;
                    BlockState state=p.level().getBlockState(pos);
                    if (!state.is(original.getBlock()) || state.getDestroySpeed(p.level(),pos)<0 || !p.getMainHandItem().isCorrectToolForDrops(state) || !p.level().mayInteract(p,pos)) continue;
                    // Native game mode posts every protection/harvest event and performs all existing conversions.
                    p.gameMode.destroyBlock(pos);
                    if (!p.level().getBlockState(pos).equals(state)) { broken++; queue.addLast(pos); }
                }
            }
        } finally { MINING.remove(); }
    }
    private TrimMining() {}
}
