package com.akiotsukino.chocolateflavored.horn;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Records only linked entities, including their final position before chunk unload or server shutdown. */
public final class HornLinks extends SavedData {
    public record Location(UUID id, Identifier dimension, BlockPos pos) {
        public static final Codec<Location> CODEC = RecordCodecBuilder.create(b -> b.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(Location::id),
                Identifier.CODEC.fieldOf("dimension").forGetter(Location::dimension),
                BlockPos.CODEC.fieldOf("pos").forGetter(Location::pos)).apply(b, Location::new));
    }
    public static final Codec<HornLinks> CODEC = Location.CODEC.listOf().xmap(HornLinks::new, s -> List.copyOf(s.locations.values()));
    private static final SavedDataType<HornLinks> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "horn_links"),
            () -> new HornLinks(List.of()), CODEC, DataFixTypes.LEVEL);
    private final Map<UUID, Location> locations = new HashMap<>();
    private HornLinks(List<Location> saved) { saved.forEach(l -> locations.put(l.id(), l)); }
    public static HornLinks get(MinecraftServer server) { return server.getDataStorage().computeIfAbsent(TYPE); }
    public Location find(UUID id) { return locations.get(id); }
    public void remember(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        var location = new Location(entity.getUUID(), level.dimension().identifier(), entity.blockPosition());
        if (!location.equals(locations.put(entity.getUUID(), location))) setDirty();
    }
    public void forget(UUID id) { if (locations.remove(id) != null) setDirty(); }
}
