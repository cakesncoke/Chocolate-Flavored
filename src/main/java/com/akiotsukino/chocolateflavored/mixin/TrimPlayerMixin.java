package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
import static net.minecraft.world.entity.EquipmentSlot.*;
@Mixin(ServerPlayer.class)
abstract class TrimPlayerMixin {
    // Only the native movement/jump call sites are modified, never attacks, damage or mining exhaustion.
    @ModifyArg(method={"jumpFromGround","checkMovementStatistics"},at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"),index=0)
    private float cf$exhaustion(float value) { Player p=(Player)(Object)this; return has(p,CHEST,"wild") && (p.isSprinting() || value==.05f) ? value*.5f : value; }
}
