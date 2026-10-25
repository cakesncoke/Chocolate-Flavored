package com.akiotsukino.chocolateflavored.trim;
import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.*;
/** One extraction bonus per world position, surviving destruction/replacement and reloads. */
public final class ArchaeologyMemory extends SavedData {
    private final Set<String> used;
    private ArchaeologyMemory(List<String> entries){used=new HashSet<>(entries);}
    private static final Codec<ArchaeologyMemory> CODEC=Codec.STRING.listOf().xmap(ArchaeologyMemory::new,m->List.copyOf(m.used));
    private static final SavedDataType<ArchaeologyMemory> TYPE=new SavedDataType<>(TrimAbilities.id("trim_archaeology"),()->new ArchaeologyMemory(List.of()),CODEC,DataFixTypes.LEVEL);
    public static boolean claim(ServerLevel level,BlockPos pos){var state=level.getServer().getDataStorage().computeIfAbsent(TYPE);boolean fresh=state.used.add(level.dimension().identifier()+"/"+pos.asLong());if(fresh)state.setDirty();return fresh;}
}
