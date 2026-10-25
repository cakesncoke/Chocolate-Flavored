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
        if(cir.getReturnValue() instanceof InteractionResult.Success) piglin.getPersistentData().putBoolean("chocolateflavored:trim/double_barter",full(p,"snout"));
    }
    @Inject(method="getBarterResponseItems",at=@At("RETURN"),cancellable=true)
    private static void cf$extra(Piglin piglin,CallbackInfoReturnable<List<ItemStack>> cir) {
        if(!piglin.getPersistentData().getBooleanOr("chocolateflavored:trim/double_barter",false))return;
        piglin.getPersistentData().remove("chocolateflavored:trim/double_barter");
        var level=(ServerLevel)piglin.level(); var result=new ArrayList<>(cir.getReturnValue());
        result.addAll(level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.PIGLIN_BARTERING).getRandomItems(new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY,piglin).create(LootContextParamSets.PIGLIN_BARTER)));
        cir.setReturnValue(result);
    }
    @Inject(method="stopHoldingOffHandItem",at=@At("RETURN"))
    private static void cf$clear(ServerLevel l,Piglin piglin,boolean bartering,CallbackInfo ci) { piglin.getPersistentData().remove("chocolateflavored:trim/double_barter"); }
}
