package com.akiotsukino.chocolateflavored.echo;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.stats.Stats;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid="chocolateflavored")
public final class EchoShards {
    public static final int CHARGE_TICKS = 60;
    private static final String LINK = "chocolateflavored:echo_anchor";
    public static UUID target(ItemStack stack) {
        if (!stack.is(Items.ECHO_SHARD)) return null;
        String value=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr(LINK,"");
        try{return UUID.fromString(value);}catch(IllegalArgumentException ignored){return null;}
    }
    public static void clear(ItemStack stack) { CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->tag.remove(LINK)); }
    public static boolean linked(ItemStack stack,Level level) {
        UUID id=target(stack);
        if(id==null)return false;
        return level instanceof ServerLevel server ? EchoAnchors.get(server.getServer()).find(id)!=null : EchoLinkPayload.clientValid(id);
    }
    private static void setLink(ItemStack stack,UUID id){CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->tag.putString(LINK,id.toString()));}
    public static InteractionResult bind(UseOnContext context) {
        var level=context.getLevel();var player=context.getPlayer();var pos=context.getClickedPos();
        if(player==null||!level.dimension().equals(Level.OVERWORLD)||!level.getBlockState(pos).is(Blocks.LODESTONE))return InteractionResult.PASS;
        if(level instanceof ServerLevel server){
            var held=context.getItemInHand(); UUID id=EchoAnchors.get(server.getServer()).bind(server,pos);
            if(held.getCount()==1)setLink(held,id);
            else {
                var shard=held.copyWithCount(1);setLink(shard,id);held.shrink(1);
                if(!player.getInventory().add(shard))player.drop(shard,false);
            }
            level.playSound(null,pos,SoundEvents.LODESTONE_COMPASS_LOCK,SoundSource.PLAYERS,1,1);
        }
        return InteractionResult.SUCCESS;
    }
    public static InteractionResult use(Level level,Player player,InteractionHand hand) {
        var stack=player.getItemInHand(hand);
        if(!linked(stack,level))return InteractionResult.PASS;
        player.startUsingItem(hand);return InteractionResult.CONSUME;
    }
    /** Unlink stale inventory copies lazily; validity and glint already changed globally on block removal. */
    public static void inventoryTick(ItemStack stack,ServerLevel level,Entity owner){
        if(target(stack)!=null&&!linked(stack,level)){
            if(owner instanceof LivingEntity living && living.isUsingItem() && living.getUseItem()==stack)living.stopUsingItem();
            clear(stack);
        }
    }
    public static ItemStack finish(ItemStack stack,Level level,LivingEntity user){
        if(!(level instanceof ServerLevel server)||!(user instanceof ServerPlayer player))return stack;
        UUID id=target(stack);var anchor=id==null?null:EchoAnchors.get(server.getServer()).find(id);
        if(anchor==null){clear(stack);return stack;}
        Vec3 arrival=null;
        if(server.dimension().equals(Level.OVERWORLD)){
            server.getChunkAt(anchor);
            if(!server.getBlockState(anchor).is(Blocks.LODESTONE)){
                EchoAnchors.removed(server,anchor);clear(stack);return stack;
            }
            arrival=arrival(server,player,anchor);
        }
        // A completed use always spends exactly one linked shard, including creative/offworld use.
        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,ItemStackTemplate.fromNonEmptyStack(stack.copyWithCount(1))),
                player.getX(),player.getY()+1,player.getZ(),5,.15,.15,.15,.05);
        server.playSound(null,player.blockPosition(),SoundEvents.ITEM_BREAK.value(),SoundSource.PLAYERS,.8F,.8F+server.getRandom().nextFloat()*.4F);
        stack.shrink(1);player.awardStat(Stats.ITEM_USED.get(Items.ECHO_SHARD));
        if(arrival!=null){
            player.teleport(new TeleportTransition(server,arrival,Vec3.ZERO,player.getYRot(),player.getXRot(),TeleportTransition.PLACE_PORTAL_TICKET));
            player.resetFallDistance();player.resetCurrentImpulseContext();
            server.playSound(null,player.getX(),player.getY(),player.getZ(),SoundEvents.CHORUS_FRUIT_TELEPORT,SoundSource.PLAYERS,1,1);
            server.gameEvent(GameEvent.TELEPORT,arrival,GameEvent.Context.of(player));
        }
        return stack;
    }
    private static Vec3 arrival(ServerLevel level,ServerPlayer player,BlockPos anchor){
        // Prefer the top of the lodestone, then nearby supported spaces if that is obstructed.
        for(int radius=0;radius<=2;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
            if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;
            var feet=anchor.offset(dx,1,dz);var location=feet.getBottomCenter();
            if(!level.isInsideBuildHeight(feet.above())||!level.getWorldBorder().isWithinBounds(feet)
                    ||!level.getBlockState(feet.below()).isFaceSturdy(level,feet.below(),Direction.UP)
                    ||!level.getFluidState(feet).isEmpty()||!level.getFluidState(feet.above()).isEmpty())continue;
            if(level.noCollision(player,player.getBoundingBox().move(location.subtract(player.position()))))return location;
        }
        return null;
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer player)EchoLinkPayload.sync(player);}
    private EchoShards(){}
}
