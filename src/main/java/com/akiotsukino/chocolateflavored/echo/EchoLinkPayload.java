package com.akiotsukino.chocolateflavored.echo;

import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Only incarnation IDs are sent; lodestone coordinates remain server-side. */
public record EchoLinkPayload(boolean reset, List<UUID> added, List<UUID> removed) implements CustomPacketPayload {
    public EchoLinkPayload { added = List.copyOf(added); removed = List.copyOf(removed); }
    public static final Type<EchoLinkPayload> TYPE = new Type<>(Identifier.parse("chocolateflavored:echo_links"));
    public static final StreamCodec<RegistryFriendlyByteBuf,EchoLinkPayload> CODEC = new StreamCodec<>() {
        private List<UUID> read(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            if (size < 0 || size > 4096) throw new IllegalArgumentException("Invalid echo link packet size");
            var ids = new ArrayList<UUID>(size); for (int i=0;i<size;i++) ids.add(buf.readUUID()); return ids;
        }
        private void write(RegistryFriendlyByteBuf buf,List<UUID> ids) { buf.writeVarInt(ids.size()); for(var id:ids)buf.writeUUID(id); }
        @Override public EchoLinkPayload decode(RegistryFriendlyByteBuf buf) { return new EchoLinkPayload(buf.readBoolean(),read(buf),read(buf)); }
        @Override public void encode(RegistryFriendlyByteBuf buf,EchoLinkPayload value) { buf.writeBoolean(value.reset()); write(buf,value.added());write(buf,value.removed()); }
    };
    // This cache has no Minecraft client classes, keeping common item hooks dedicated-server safe.
    private static final Set<UUID> CLIENT_VALID = java.util.concurrent.ConcurrentHashMap.newKeySet();
    public static boolean clientValid(UUID id) { return id != null && CLIENT_VALID.contains(id); }
    public static void apply(EchoLinkPayload payload) {
        if (payload.reset()) CLIENT_VALID.clear();
        CLIENT_VALID.removeAll(payload.removed()); CLIENT_VALID.addAll(payload.added());
    }
    public static void clearClient() { CLIENT_VALID.clear(); }
    public static void register(RegisterPayloadHandlersEvent event) { event.registrar("1").playToClient(TYPE,CODEC); }
    public static void broadcast(MinecraftServer server,List<UUID> added,List<UUID> removed) {
        for(var player:server.getPlayerList().getPlayers()) PacketDistributor.sendToPlayer(player,new EchoLinkPayload(false,added,removed));
    }
    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,new EchoLinkPayload(true,List.of(),List.of()));
        var ids=EchoAnchors.get(player.level().getServer()).ids();
        for(int i=0;i<ids.size();i+=4096)
            PacketDistributor.sendToPlayer(player,new EchoLinkPayload(false,ids.subList(i,Math.min(i+4096,ids.size())),List.of()));
    }
}
