package com.akiotsukino.chocolateflavored.mixin;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import com.akiotsukino.chocolateflavored.trim.TrimAbilities;
import com.akiotsukino.chocolateflavored.trim.TrimTriggers;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ItemCooldowns.class)
abstract class TrimCooldownMixin {
    @Shadow @Final private Map<Identifier,Object> cooldowns;
    @Shadow private int tickCount;
    @Unique private float cf$percent(Identifier group,float partial){
        Object value=cooldowns.get(group);if(value==null)return 0;
        var access=(TrimCooldownInstanceAccessor)value;int duration=access.chocolateflavored$end()-access.chocolateflavored$start();
        return duration<=0?0:net.minecraft.util.Mth.clamp((access.chocolateflavored$end()-tickCount-partial)/duration,0,1);
    }
    @Inject(method="getCooldownPercent",at=@At("RETURN"),cancellable=true)
    private void cf$overlay(ItemStack stack,float partial,CallbackInfoReturnable<Float> cir){
        String pattern=TrimAbilities.pattern(stack);
        for(String ability:TrimTriggers.COOLDOWNS)if(TrimTriggers.cooldownApplies(stack,ability))cir.setReturnValue(Math.max(cir.getReturnValue(),cf$percent(TrimAbilities.id("trim_cooldown/"+ability),partial)));
    }
    @Redirect(method="isOnCooldown",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemCooldowns;getCooldownPercent(Lnet/minecraft/world/item/ItemStack;F)F"))
    private float cf$ordinaryUse(ItemCooldowns self,ItemStack stack,float partial){return cf$percent(self.getCooldownGroup(stack),partial);}
}
