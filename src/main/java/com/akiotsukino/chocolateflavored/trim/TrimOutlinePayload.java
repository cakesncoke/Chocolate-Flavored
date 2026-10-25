package com.akiotsukino.chocolateflavored.trim;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Server-authorized positions only: no replacement block states or fake entities. */
public record TrimOutlinePayload(List<BlockPos> positions, boolean archaeology) implements CustomPacketPayload {
    public static final int DURATION_TICKS = 30 * 20;
    public TrimOutlinePayload(List<BlockPos> positions) { this(positions, false); }
    public int durationTicks() { return archaeology ? 22 : DURATION_TICKS; }
    public TrimOutlinePayload { positions = List.copyOf(positions); }
    public static final Type<TrimOutlinePayload> TYPE = new Type<>(TrimAbilities.id("eye_outlines"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrimOutlinePayload> CODEC = new StreamCodec<>() {
        @Override public TrimOutlinePayload decode(RegistryFriendlyByteBuf buf) {
            int count = buf.readVarInt();
            if (count < 0 || count > 20000) throw new IllegalArgumentException("Invalid Eye outline count");
            var positions = new ArrayList<BlockPos>(count);
            for (int i = 0; i < count; i++) positions.add(buf.readBlockPos());
            return new TrimOutlinePayload(positions, buf.readBoolean());
        }
        @Override public void encode(RegistryFriendlyByteBuf buf, TrimOutlinePayload payload) {
            buf.writeVarInt(payload.positions.size());
            for (BlockPos pos : payload.positions) buf.writeBlockPos(pos);
            buf.writeBoolean(payload.archaeology);
        }
    };
    @Override public Type<TrimOutlinePayload> type() { return TYPE; }
    public static void register(RegisterPayloadHandlersEvent event) { event.registrar("2").playToClient(TYPE, CODEC); }
}
