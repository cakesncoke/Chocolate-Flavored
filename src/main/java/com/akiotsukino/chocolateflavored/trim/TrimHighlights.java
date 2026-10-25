package com.akiotsukino.chocolateflavored.trim;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.phys.Vec3;
import com.akiotsukino.chocolateflavored.mixin.TrimDisplayAccessor;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
/** Client-only native displays: no blocks/entities are added to the server world. */
public final class TrimHighlights {
    private record View(int[] ids,long until,boolean archaeology,net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {}
    private static final Map<ServerPlayer,List<View>> VIEWS=new WeakHashMap<>();
    public static void scan(ServerPlayer p,boolean archaeology) {
        if (archaeology) clear(p,true);
        var ids=new ArrayList<Integer>(); BlockPos center=p.blockPosition();
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-16,-16,-16),center.offset(16,16,16))) {
            if (pos.distSqr(center)>256 || !p.level().hasChunkAt(pos)) continue;
            var state=p.level().getBlockState(pos);
            if (archaeology ? !(p.level().getBlockEntity(pos) instanceof BrushableBlockEntity) : !state.is(ORES) || !p.getMainHandItem().isCorrectToolForDrops(state)) continue;
            Display.BlockDisplay display=new Display.BlockDisplay(EntityType.BLOCK_DISPLAY,p.level());
            display.setPos(Vec3.atLowerCornerOf(pos)); display.setGlowingTag(true);
            ((TrimDisplayAccessor)display).chocolateflavored$state(state);
            p.connection.send(new ClientboundAddEntityPacket(display.getId(),display.getUUID(),pos.getX(),pos.getY(),pos.getZ(),0,0,EntityType.BLOCK_DISPLAY,0,Vec3.ZERO,0));
            var values=display.getEntityData().getNonDefaultValues();
            if(values!=null) p.connection.send(new ClientboundSetEntityDataPacket(display.getId(),values));
            ids.add(display.getId());
        }
        VIEWS.computeIfAbsent(p,k->new ArrayList<>()).add(new View(ids.stream().mapToInt(Integer::intValue).toArray(),TrimTriggers.clock(p)+(archaeology?22:200),archaeology,p.level().dimension()));
    }
    private static void clear(ServerPlayer p,boolean archaeology) {
        var list=VIEWS.get(p);if(list==null)return;
        list.removeIf(v->{ if(v.archaeology==archaeology){p.connection.send(new ClientboundRemoveEntitiesPacket(v.ids));return true;}return false;});
    }
    public static void expire(ServerPlayer p) {
        var list=VIEWS.get(p);if(list==null)return;
        list.removeIf(v->{boolean valid=v.archaeology?full(p,"dune") && (p.getMainHandItem().is(net.minecraft.world.item.Items.BRUSH)||p.getOffhandItem().is(net.minecraft.world.item.Items.BRUSH)):full(p,"eye");
            if(!valid || v.until<=TrimTriggers.clock(p) || v.dimension!=p.level().dimension()){p.connection.send(new ClientboundRemoveEntitiesPacket(v.ids));return true;}return false;});
    }
    private TrimHighlights() {}
}
