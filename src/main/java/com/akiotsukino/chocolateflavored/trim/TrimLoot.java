package com.akiotsukino.chocolateflavored.trim;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
import static net.minecraft.world.entity.EquipmentSlot.*;
@EventBusSubscriber(modid=ChocolateFlavored.MODID)
public final class TrimLoot {
    public static int looting(Holder<Enchantment> enchantment,LivingEntity killer,LootContext context) {
        int result=EnchantmentHelper.getEnchantmentLevel(enchantment,killer);
        if(!enchantment.is(Enchantments.LOOTING))return result;
        var target=context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if(target!=null && (has(killer,LEGS,"vex") && target.getType().is(net.minecraft.tags.EntityTypeTags.ILLAGER) || has(killer,FEET,"coast") && target.getType().is(AQUATIC))) result+=2;
        return result;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void fish(ItemFishedEvent event) {
        Player p=event.getEntity();
        if(event.isCanceled() || event.getDrops().isEmpty() || !full(p,"coast") || !(p.level() instanceof ServerLevel level))return;
        var hook=event.getHookEntity();
        if(hook.getPersistentData().getBooleanOr("chocolateflavored:trim/salvaged",false))return;
        hook.getPersistentData().putBoolean("chocolateflavored:trim/salvaged",true);
        if(p.getRandom().nextFloat()>=.1f)return;
        var params=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,hook.position()).withParameter(LootContextParams.TOOL,p.getMainHandItem()).withParameter(LootContextParams.THIS_ENTITY,p).withLuck(p.getLuck()).create(LootContextParamSets.FISHING);
        var loot=level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,id("gameplay/ocean_salvage"))).getRandomItems(params);
        for(var stack:loot) {var item=new ItemEntity(level,hook.getX(),hook.getY(),hook.getZ(),stack);var delta=p.position().subtract(hook.position());item.setDeltaMovement(delta.x*.1,delta.y*.1+Math.sqrt(Math.sqrt(delta.lengthSqr()))*.08,delta.z*.1);level.addFreshEntity(item);}
    }
    private TrimLoot() {}
}
