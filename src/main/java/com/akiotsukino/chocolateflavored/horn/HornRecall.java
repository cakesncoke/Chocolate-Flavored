package com.akiotsukino.chocolateflavored.horn;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = ChocolateFlavored.MODID)
public final class HornRecall {
    private record Pending(UUID player, UUID target, int expires) {}
    private static final Map<MinecraftServer, List<Pending>> PENDING = new WeakHashMap<>();

    public static boolean eligible(Player player, Entity target) {
        if (!(target instanceof LivingEntity living) || !target.isAlive()) return false;
        if (target instanceof OwnableEntity ownable && ownable.getOwnerReference() != null
                && !ownable.getOwnerReference().matches(player)) return false;
        return target instanceof TamableAnimal pet && pet.isTame()
                || target instanceof AbstractHorse horse && horse.isTamed()
                || !living.getItemBySlot(EquipmentSlot.SADDLE).isEmpty();
    }

    public static boolean bind(Player player, ItemStack horn, Entity target) {
        if (!(horn.getItem() instanceof CopperHornItem) || !eligible(player, target)) return false;
        if (player.level() instanceof ServerLevel level) {
            CopperHornItem.link(horn, target.getUUID());
            HornLinks.get(level.getServer()).remember(target);
            player.displayClientMessage(Component.translatable("message.chocolateflavored.horn_linked", target.getDisplayName()), true);
        }
        return true;
    }

    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (bind(event.getEntity(), event.getItemStack(), event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    public static Entity loaded(MinecraftServer server, UUID id) {
        for (var level : server.getAllLevels()) {
            var entity = level.getEntity(id);
            if (entity != null) return entity;
        }
        return null;
    }

    public static void request(ServerPlayer player, UUID target) {
        var server = player.level().getServer();
        var entity = loaded(server, target);
        if (entity != null) { recall(player, entity); return; }
        var location = HornLinks.get(server).find(target);
        var level = location == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, location.dimension()));
        if (level == null) { unavailable(player); return; }
        // Native portal tickets expire automatically; no permanent force-loaded chunks.
        level.getChunkSource().addTicketWithRadius(TicketType.PORTAL, new ChunkPos(location.pos()), 2);
        var pending = PENDING.computeIfAbsent(server, s -> new ArrayList<>());
        pending.removeIf(p -> p.player().equals(player.getUUID()) && p.target().equals(target));
        pending.add(new Pending(player.getUUID(), target, server.getTickCount() + 200));
    }

    public static boolean recall(ServerPlayer player, Entity entity) {
        if (!eligible(player, entity) || entity.isVehicle()) { unavailable(player); return false; }
        // Do not transport passengers or detach another player's mount. Sitting state is preserved.
        if (entity instanceof Mob mob) mob.getNavigation().stop();
        var moved = entity.teleport(new TeleportTransition(player.level(), player.position(), Vec3.ZERO,
                entity.getYRot(), entity.getXRot(), TeleportTransition.DO_NOTHING));
        if (moved == null) { unavailable(player); return false; }
        moved.fallDistance = 0;
        HornLinks.get(player.level().getServer()).remember(moved);
        return true;
    }

    private static void unavailable(Player player) {
        player.displayClientMessage(Component.translatable("message.chocolateflavored.horn_unavailable"), true);
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        var server = event.getServer();
        var pending = PENDING.get(server);
        if (pending == null) return;
        var iterator = pending.iterator();
        while (iterator.hasNext()) {
            var call = iterator.next();
            var player = server.getPlayerList().getPlayer(call.player());
            var target = loaded(server, call.target());
            if (player == null) iterator.remove();
            else if (target != null) { recall(player, target); iterator.remove(); }
            else if (server.getTickCount() >= call.expires()) { unavailable(player); iterator.remove(); }
        }
    }

    @SubscribeEvent public static void left(EntityLeaveLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var entity = event.getEntity();
        var links = HornLinks.get(level.getServer());
        if (links.find(entity.getUUID()) == null) return;
        var reason = entity.getRemovalReason();
        if (reason != null && reason.shouldDestroy()) links.forget(entity.getUUID());
        else links.remember(entity);
    }

    @SubscribeEvent public static void stopping(ServerStoppingEvent event) {
        var links = HornLinks.get(event.getServer());
        for (var level : event.getServer().getAllLevels())
            for (var entity : level.getAllEntities())
                if (links.find(entity.getUUID()) != null) links.remember(entity);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { PENDING.remove(event.getServer()); }
    private HornRecall() {}
}
