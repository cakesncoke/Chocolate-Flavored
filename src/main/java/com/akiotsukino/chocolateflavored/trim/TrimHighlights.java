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
/** Server-selected Eye borders and client-only Dune displays; no server world entities. */
public final class TrimHighlights {
    private record View(int[] ids,long until,boolean archaeology,net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {}
    private static final Map<ServerPlayer,List<View>> VIEWS=new WeakHashMap<>();
    public static void scan(ServerPlayer p,boolean archaeology) {
        clear(p, archaeology);
        var outlines = new ArrayList<BlockPos>();
        var ids=new ArrayList<Integer>(); BlockPos center=p.blockPosition();
        Iterable<BlockPos> candidates;
        if(archaeology){
            var positions=new ArrayList<BlockPos>();
            for(int cx=(center.getX()-16)>>4;cx<=((center.getX()+16)>>4);cx++)for(int cz=(center.getZ()-16)>>4;cz<=((center.getZ()+16)>>4);cz++){
                var origin=new BlockPos(cx<<4,center.getY(),cz<<4);
                if(p.level().hasChunkAt(origin))positions.addAll(p.level().getChunkAt(origin).getBlockEntities().keySet());
            }
            candidates=positions;
        }else candidates=BlockPos.betweenClosed(center.offset(-16,-16,-16),center.offset(16,16,16));
        for(BlockPos pos:candidates) {
            if (pos.distSqr(center)>256 || !p.level().hasChunkAt(pos)) continue;
            var state=p.level().getBlockState(pos);
            if (archaeology ? !(p.level().getBlockEntity(pos) instanceof BrushableBlockEntity) : !state.is(ORES) || !p.getMainHandItem().isCorrectToolForDrops(state)) continue;
            if (!archaeology) { outlines.add(pos.immutable()); continue; }
            Display.BlockDisplay display=new Display.BlockDisplay(EntityType.BLOCK_DISPLAY,p.level());
            display.setPos(Vec3.atLowerCornerOf(pos)); display.setGlowingTag(true);
            ((TrimDisplayAccessor)display).chocolateflavored$state(state);
            p.connection.send(new ClientboundAddEntityPacket(display.getId(),display.getUUID(),pos.getX(),pos.getY(),pos.getZ(),0,0,EntityType.BLOCK_DISPLAY,0,Vec3.ZERO,0));
            var values=display.getEntityData().getNonDefaultValues();
            if(values!=null) p.connection.send(new ClientboundSetEntityDataPacket(display.getId(),values));
            ids.add(display.getId());
        }
        if (!archaeology) net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, new TrimOutlinePayload(outlines));
        VIEWS.computeIfAbsent(p,k->new ArrayList<>()).add(new View(ids.stream().mapToInt(Integer::intValue).toArray(),TrimTriggers.clock(p)+(archaeology?22:200),archaeology,p.level().dimension()));
    }
    private static void remove(ServerPlayer p, View view) {
        if (view.archaeology) p.connection.send(new ClientboundRemoveEntitiesPacket(view.ids));
        else net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, new TrimOutlinePayload(List.of()));
    }
    private static void clear(ServerPlayer p,boolean archaeology) {
        var list=VIEWS.get(p);if(list==null)return;
        list.removeIf(v->{ if(v.archaeology==archaeology){remove(p, v);return true;}return false;});
    }
    public static void clearAll(ServerPlayer p){var list=VIEWS.remove(p);if(list!=null)for(var view:list)remove(p, view);}
    public static void expire(ServerPlayer p) {
        var list=VIEWS.get(p);if(list==null)return;
        list.removeIf(v->{boolean valid=v.archaeology?full(p,"dune") && (p.getMainHandItem().is(net.minecraft.world.item.Items.BRUSH)||p.getOffhandItem().is(net.minecraft.world.item.Items.BRUSH)):full(p,"eye");
            if(!p.isAlive() || !valid || v.until<=TrimTriggers.clock(p) || v.dimension!=p.level().dimension()){remove(p, v);return true;}return false;});
    }
    private TrimHighlights() {}
}
