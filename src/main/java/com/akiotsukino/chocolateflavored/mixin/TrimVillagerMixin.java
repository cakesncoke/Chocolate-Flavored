package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(Villager.class)
abstract class TrimVillagerMixin {
    @Inject(method="updateSpecialPrices",at=@At("TAIL"))
    private void cf$prices(Player p,CallbackInfo ci) { if(full(p,"vex")) for(var offer:((Villager)(Object)this).getOffers()) offer.addToSpecialPriceDiff((int)Math.ceil(offer.getCostA().getCount()*.25)); }
}
