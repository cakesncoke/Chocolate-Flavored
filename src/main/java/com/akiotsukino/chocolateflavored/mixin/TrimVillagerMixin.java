package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.server.level.ServerLevel;
import java.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(Villager.class)
abstract class TrimVillagerMixin {
    @Unique private final Map<MerchantOffer,Integer> cf$prices=new IdentityHashMap<>();
    @Unique private void cf$apply(Player p){
        var villager=(Villager)(Object)this;
        for(var offer:villager.getOffers()){
            offer.addToSpecialPriceDiff(-cf$prices.getOrDefault(offer,0));
            int extra=full(p,"vex")?(int)Math.ceil(offer.getCostA().getCount()*.25):0;
            offer.addToSpecialPriceDiff(extra);cf$prices.put(offer,extra);
        }
    }
    @Inject(method="updateSpecialPrices",at=@At("TAIL")) private void cf$open(Player p,CallbackInfo ci){cf$apply(p);}
    @Inject(method="resetSpecialPrices",at=@At("TAIL")) private void cf$reset(CallbackInfo ci){cf$prices.clear();}
    @Inject(method="customServerAiStep",at=@At("TAIL")) private void cf$tick(ServerLevel l,CallbackInfo ci){var p=((Villager)(Object)this).getTradingPlayer();if(p!=null)cf$apply(p);}
}
