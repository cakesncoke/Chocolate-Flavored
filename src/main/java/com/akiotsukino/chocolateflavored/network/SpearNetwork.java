package com.akiotsukino.chocolateflavored.network;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.event.SpearCombat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class SpearNetwork {
    private SpearNetwork() {}
    public record Jab() implements CustomPacketPayload {
        public static final Jab INSTANCE = new Jab();
        public static final Type<Jab> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "spear_jab"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Jab> CODEC = StreamCodec.unit(INSTANCE);
        @Override public Type<Jab> type() { return TYPE; }
    }
    public record Hit(int entityId) implements CustomPacketPayload {
        public static final Type<Hit> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "spear_hit"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Hit> CODEC = StreamCodec.composite(
                net.minecraft.network.codec.ByteBufCodecs.VAR_INT, Hit::entityId, Hit::new);
        @Override public Type<Hit> type() { return TYPE; }
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Hit.TYPE, Hit.CODEC, (payload, context) -> context.enqueueWork(() -> {
            var entity = context.player().level().getEntity(payload.entityId());
            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                player.getPersistentData().putLong("chocolateflavored:spear_hit_time", player.level().getGameTime());
            }
        }));
        event.registrar("1").playToServer(Jab.TYPE, Jab.CODEC,
                (payload, context) -> context.enqueueWork(() -> SpearCombat.jab(context.player())));
    }
}
