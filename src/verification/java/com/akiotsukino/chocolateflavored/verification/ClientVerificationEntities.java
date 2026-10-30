package com.akiotsukino.chocolateflavored.verification;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

/** Gallery entities are never added to a world or assigned an ID by a spawn packet. */
final class ClientVerificationEntities {
    private static int nextPreviewId = -1;

    static <T extends Entity> T create(EntityType<T> type, ClientLevel level) {
        T entity = type.create(level, EntitySpawnReason.COMMAND);
        if (entity == null) throw new IllegalStateException("Cannot create gallery entity: " + type);
        // 26.2's render-state extraction requires a nonzero ID, including for UI-only previews.
        entity.setId(nextPreviewId--);
        return entity;
    }

    private ClientVerificationEntities() {}
}
