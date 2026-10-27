package com.akiotsukino.chocolateflavored.echo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.*;

/** A placed lodestone has one persistent incarnation ID, shared by every shard bound to it. */
public final class EchoAnchors extends SavedData {
    public record Anchor(UUID id, BlockPos pos) {
        public static final Codec<Anchor> CODEC = RecordCodecBuilder.create(b -> b.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(Anchor::id),
                BlockPos.CODEC.fieldOf("pos").forGetter(Anchor::pos)).apply(b, Anchor::new));
    }
    public static final Codec<EchoAnchors> CODEC = Anchor.CODEC.listOf().xmap(EchoAnchors::new, s -> List.copyOf(s.byId.values()));
    private static final SavedDataType<EchoAnchors> TYPE = new SavedDataType<>(
            Identifier.parse("chocolateflavored:echo_anchors"), () -> new EchoAnchors(List.of()), CODEC, DataFixTypes.LEVEL);
    private final Map<UUID, Anchor> byId = new HashMap<>();
    private final Map<BlockPos, UUID> byPos = new HashMap<>();
    private EchoAnchors(List<Anchor> anchors) { for (var anchor : anchors) { byId.put(anchor.id(), anchor); byPos.put(anchor.pos(), anchor.id()); } }
    public static EchoAnchors get(MinecraftServer server) { return server.getDataStorage().computeIfAbsent(TYPE); }
    public BlockPos find(UUID id) { var anchor = byId.get(id); return anchor == null ? null : anchor.pos(); }
    public List<UUID> ids() { return List.copyOf(byId.keySet()); }
    public UUID bind(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(Level.OVERWORLD) || !level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.LODESTONE))
            throw new IllegalArgumentException("Echo anchors require an Overworld lodestone");
        var existing = byPos.get(pos);
        if (existing != null) return existing;
        var id = UUID.randomUUID(); var anchor = new Anchor(id, pos.immutable());
        byId.put(id, anchor); byPos.put(anchor.pos(), id); setDirty();
        EchoLinkPayload.broadcast(level.getServer(), List.of(id), List.of());
        return id;
    }
    public static void removed(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(Level.OVERWORLD)) return;
        var state = get(level.getServer()); var id = state.byPos.remove(pos);
        if (id == null) return;
        state.byId.remove(id); state.setDirty();
        // No scan of inventories is needed: all shards refer to this now-dead incarnation.
        EchoLinkPayload.broadcast(level.getServer(), List.of(), List.of(id));
    }
}
