package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.echo.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Item.class)
public abstract class EchoShardItemMixin {
    @Inject(method="useOn",at=@At("HEAD"),cancellable=true)
    private void chocolateflavored$link(UseOnContext context,CallbackInfoReturnable<InteractionResult> cir){
        if((Object)this==Items.ECHO_SHARD){var result=EchoShards.bind(context);if(result.consumesAction())cir.setReturnValue(result);}
    }
    @Inject(method="use",at=@At("HEAD"),cancellable=true)
    private void chocolateflavored$use(Level level,Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir){
        if((Object)this==Items.ECHO_SHARD){var result=EchoShards.use(level,player,hand);if(result.consumesAction())cir.setReturnValue(result);}
    }
    @Inject(method="getUseDuration",at=@At("HEAD"),cancellable=true)
    private void chocolateflavored$duration(ItemStack stack,LivingEntity user,CallbackInfoReturnable<Integer> cir){
        if(stack.is(Items.ECHO_SHARD)&&EchoShards.target(stack)!=null)cir.setReturnValue(EchoShards.CHARGE_TICKS);
    }
    @Inject(method="getUseAnimation",at=@At("HEAD"),cancellable=true)
    private void chocolateflavored$animation(ItemStack stack,CallbackInfoReturnable<ItemUseAnimation> cir){
        if(stack.is(Items.ECHO_SHARD)&&EchoShards.target(stack)!=null)cir.setReturnValue(ItemUseAnimation.BOW);
    }
    @Inject(method="finishUsingItem",at=@At("HEAD"),cancellable=true)
    private void chocolateflavored$finish(ItemStack stack,Level level,LivingEntity user,CallbackInfoReturnable<ItemStack> cir){
        if(stack.is(Items.ECHO_SHARD)&&EchoShards.target(stack)!=null)cir.setReturnValue(EchoShards.finish(stack,level,user));
    }
    @Inject(method="inventoryTick",at=@At("HEAD"))
    private void chocolateflavored$invalid(ItemStack stack,ServerLevel level,Entity owner,EquipmentSlot slot,CallbackInfo ci){
        if(stack.is(Items.ECHO_SHARD))EchoShards.inventoryTick(stack,level,owner);
    }
    @Inject(method="isFoil",at=@At("HEAD"),cancellable=true)
    private void chocolateflavored$glint(ItemStack stack,CallbackInfoReturnable<Boolean> cir){
        if(stack.is(Items.ECHO_SHARD)&&EchoShards.target(stack)!=null)cir.setReturnValue(EchoLinkPayload.clientValid(EchoShards.target(stack)));
    }
}
