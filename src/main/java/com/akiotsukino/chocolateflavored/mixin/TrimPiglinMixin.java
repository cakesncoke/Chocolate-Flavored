package com.akiotsukino.chocolateflavored.mixin;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(PiglinAi.class)
abstract class TrimPiglinMixin {
    @Inject(method="isWearingSafeArmor",at=@At("RETURN"),cancellable=true)
    private static void cf$safe(LivingEntity e,CallbackInfoReturnable<Boolean> cir) { if(gold(e)||full(e,"snout"))cir.setReturnValue(true); }
    @Inject(method="mobInteract",at=@At("RETURN"))
    private static void cf$barterOwner(ServerLevel l,Piglin piglin,Player p,InteractionHand h,CallbackInfoReturnable<InteractionResult> cir) {
        if(cir.getReturnValue() instanceof InteractionResult.Success) piglin.getPersistentData().putString("chocolateflavored:trim/barter_player",p.getUUID().toString());
    }
    @Inject(method="pickUpItem",at=@At("HEAD"))
    private static void cf$thrownOwner(ServerLevel l,Piglin piglin,net.minecraft.world.entity.item.ItemEntity item,CallbackInfo ci){
        if(item.getItem().is(net.minecraft.world.item.Items.GOLD_INGOT)){piglin.getPersistentData().remove("chocolateflavored:trim/barter_player");if(item.getOwner() instanceof Player p)piglin.getPersistentData().putString("chocolateflavored:trim/barter_player",p.getUUID().toString());}
    }
    @Inject(method="getBarterResponseItems",at=@At("RETURN"),cancellable=true)
    private static void cf$extra(Piglin piglin,CallbackInfoReturnable<List<ItemStack>> cir) {
        String owner=piglin.getPersistentData().getStringOr("chocolateflavored:trim/barter_player","");
        if(owner.isEmpty())return;
        Player player=piglin.level().getServer().getPlayerList().getPlayer(UUID.fromString(owner));
        piglin.getPersistentData().remove("chocolateflavored:trim/barter_player");
        if(player==null || !full(player,"snout"))return;
        var level=(ServerLevel)piglin.level(); var result=new ArrayList<>(cir.getReturnValue());
        result.addAll(level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.PIGLIN_BARTERING).getRandomItems(new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY,piglin).create(LootContextParamSets.PIGLIN_BARTER)));
        cir.setReturnValue(result);
    }
    @Inject(method="stopHoldingOffHandItem",at=@At("RETURN"))
    private static void cf$clear(ServerLevel l,Piglin piglin,boolean bartering,CallbackInfo ci) { piglin.getPersistentData().remove("chocolateflavored:trim/barter_player"); }
}
