package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mob.expansion.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import java.util.*;
import java.util.function.Consumer;

/** Runtime checks for actual 26.1.2 combat, state persistence and spawn isolation. */
public final class ExpansionVerification {
    public static void run(ServerLevel level) {
        CombatPlayer player = new CombatPlayer(level);
        BlockPos pos = new BlockPos(180,180,180);level.getChunkAt(pos);
        level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);player.snapTo(180.5,180,182.5,0,0);
        var sunken = ExpansionContent.SUNKEN_SKELETON.get().create(level,EntitySpawnReason.COMMAND);
        sunken.snapTo(180.5,180,180.5,0,0);
        check(sunken.getMainHandItem().is(Items.CROSSBOW) && sunken.canBreatheUnderwater(),"Aquatic crossbow skeleton");
        check(sunken.canUseNonMeleeWeapon(new ItemStack(Items.CROSSBOW)),"Native crossbow weapon support");
        sunken.startFreezeConversion(1);check(!sunken.isFreezeConverting(),"Sunken skeleton cannot turn into stray");
        var waterArrow=new TestArrow(level);waterArrow.setOwner(sunken);check(waterArrow.waterDrag()==.99F,"Sunken arrows keep useful underwater range");waterArrow.setOwner(player);check(waterArrow.waterDrag()==.6F,"Other archers keep vanilla arrow drag");
        sunken.setChargingCrossbow(true);check(sunken.isChargingCrossbow(),"Crossbow charge synchronization");sunken.setChargingCrossbow(false);
        List<Entity> shots = new ArrayList<>();Consumer<EntityJoinLevelEvent> projectileObserver = e -> {if(e.getEntity() instanceof AbstractArrow || e.getEntity() instanceof SmallFireball)shots.add(e.getEntity());};
        NeoForge.EVENT_BUS.addListener(projectileObserver);
        try {
            sunken.setTarget(player);sunken.getMainHandItem().set(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.ofNonEmpty(List.of(new ItemStack(Items.ARROW))));
            sunken.performRangedAttack(player,1);
            check(shots.size()==1 && shots.getFirst() instanceof AbstractArrow,"Sunken fires native crossbow arrow");shots.forEach(Entity::discard);shots.clear();
            var wildfire = new TestWildfire(level);wildfire.snapTo(181,180,180,0,0);
            check(wildfire.shields()==4 && wildfire.getMaxHealth()==50,"Wildfire starts with four shields");
            float health=wildfire.getHealth();wildfire.hurtServer(level,level.damageSources().mobAttack(player),15);
            check(wildfire.shields()==3 && wildfire.getHealth()==health,"Shield absorbs damage and breaks");
            var restored=(Wildfire)restore(level,wildfire);check(restored.shields()==3,"Wildfire shields persist");
            wildfire.invulnerableTime=0;wildfire.hurtServer(level,level.damageSources().mobAttack(player),45);
            check(wildfire.shields()==0 && wildfire.getHealth()==health,"Remaining shields can be broken");
            wildfire.invulnerableTime=0;wildfire.hurtServer(level,level.damageSources().mobAttack(player),4);
            check(wildfire.getHealth()==health-4,"Unshielded Wildfire takes body damage");
            wildfire.setTarget(null);for(int i=0;i<149;i++)wildfire.step(level);check(wildfire.shields()==0,"No early shield regeneration");
            wildfire.step(level);check(wildfire.shields()==1,"One shield regenerates after 150 ticks");
            wildfire.shootVolley(level,player);check(shots.size()==3 && shots.stream().allMatch(e->e instanceof SmallFireball),"Wildfire uses three vanilla fireballs");shots.forEach(Entity::discard);shots.clear();
            player.snapTo(181,180,190,0,0);wildfire.setTarget(player);wildfire.step(level);
            check(wildfire.chargeTicks()==30&&wildfire.isOnFire(),"Wildfire telegraphs its attack with native charged flames");
            for(int i=0;i<30;i++)wildfire.step(level);
            check(shots.size()==3&&wildfire.chargeTicks()==0&&!wildfire.isOnFire(),"AI completes its windup and fires the volley");shots.forEach(Entity::discard);shots.clear();
            player.snapTo(180.5,180,182.5,0,0);
        } finally {NeoForge.EVENT_BUS.unregister(projectileObserver);}
        var tuff=new TestTuff(level);tuff.snapTo(180.5,180,180.5,0,0);tuff.step(level);
        check(tuff.isStatue()&&!tuff.isPushable()&&tuff.isPersistenceRequired(),"Persistent statue golem");
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND,2));tuff.mobInteract(player,InteractionHand.MAIN_HAND);
        check(tuff.getMainHandItem().is(Items.DIAMOND)&&tuff.getMainHandItem().getCount()==1&&player.getMainHandItem().getCount()==1,"One display item transferred");
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BLUE_CARPET,2));tuff.mobInteract(player,InteractionHand.MAIN_HAND);
        var saved=(TuffGolem)restore(level,tuff);
        check(saved.isStatue()&&saved.cloakColor()==DyeColor.BLUE.getId()&&saved.getMainHandItem().is(Items.DIAMOND)&&saved.home().equals(pos),"Home, statue, cloak and inventory persist");
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);tuff.mobInteract(player,InteractionHand.MAIN_HAND);check(tuff.getMainHandItem().isEmpty(),"Displayed item returned");
        var loose=new ItemEntity(level,180.5,180,180.5,new ItemStack(Items.STICK,3));
        check(!tuff.collectItem(loose),"Statue does not collect");
        tuff.setStatue(false);check(tuff.collectItem(loose)&&tuff.getMainHandItem().getCount()==1&&loose.getItem().getCount()==2,"Awake golem collects exactly one loose item");
        check(!tuff.collectItem(loose)&&loose.getItem().getCount()==2,"Carried item prevents duplication");
        for(int i=0;i<400;i++)tuff.step(level);check(tuff.isStatue(),"Golem returns home and rests after walking interval");
        var barnacle=new TestBarnacle(level);barnacle.snapTo(180.5,180,180.5,0,0);player.snapTo(180.5,180,183.5,0,0);player.wet=true;player.setAirSupply(300);player.setDeltaMovement(0,0,0);player.setHealth(20);player.invulnerableTime=0;
        check(barnacle.canTether(player),"Swimmer in tether range");barnacle.setTarget(player);
        for(int i=0;i<40;i++)barnacle.step(level);
        check(barnacle.preyId()==player.getId()&&player.getDeltaMovement().y<0&&player.getAirSupply()==260&&player.getHealth()<20,"Tether pulls, drains air and bites");
        player.wet=false;barnacle.step(level);check(barnacle.preyId()==0,"Leaving water cancels tether");
        // NeoForge FakePlayer deliberately refuses all riding; use a native living passenger for the boat branch.
        var boat=EntityType.OAK_BOAT.create(level,EntitySpawnReason.COMMAND);boat.snapTo(player.getX(),player.getY(),player.getZ(),0,0);
        var rider=EntityType.PIG.create(level,EntitySpawnReason.COMMAND);rider.snapTo(player.getX(),player.getY(),player.getZ(),0,0);
        check(rider.startRiding(boat,true,false),"Native test passenger enters boat");barnacle.setTarget(rider);
        check(barnacle.canTether(rider),"Boat riders are eligible prey");barnacle.step(level);check(boat.getDeltaMovement().y<0,"Tether pulls occupied boat");rider.stopRiding();rider.discard();boat.discard();barnacle.setTarget(player);
        player.wet=true;barnacle.wet=false;check(!barnacle.canTether(player),"Stranded Barnacle cannot tether");barnacle.wet=true;
        player.snapTo(220,180,180,0,0);check(!barnacle.canTether(player),"Out of range cancels tether");
        player.snapTo(180.5,180,183.5,0,0);player.wet=false;player.removeAllEffects();player.setHealth(20);player.invulnerableTime=0;
        var caster=ExpansionContent.ICEOLOGER.get().create(level,EntitySpawnReason.COMMAND);caster.snapTo(180.5,180,180.5,0,0);
        check(!caster.canJoinRaid()&&!caster.canBeLeader(),"No automatic raid integration");
        List<IceChunk> spells=new ArrayList<>();Consumer<EntityJoinLevelEvent> spellObserver=e->{if(e.getEntity() instanceof IceChunk ice)spells.add(ice);};NeoForge.EVENT_BUS.addListener(spellObserver);
        try {
            IceChunk ice=caster.summonIce(level,player);check(ice!=null&&spells.size()==1,"Iceologer creates registered ice spell");
            for(int i=0;i<39;i++)ice.tick();check(!ice.isFalling(),"Ice warns before falling");
            IceChunk reloaded=(IceChunk)restore(level,ice);check(reloaded.spellAge()==39,"Ice spell age persists");reloaded.tick();check(reloaded.isFalling(),"Restored spell falls at correct time");
            var impact=new TestIce(level);impact.snapTo(player.getX(),player.getY(),player.getZ(),0,0);
            check(!impact.hit(level,caster,caster),"Ice cannot damage owner");
            check(impact.hit(level,caster,player)&&player.hasEffect(MobEffects.SLOWNESS)&&player.getEffect(MobEffects.SLOWNESS).getAmplifier()==1&&player.getTicksFrozen()>=140,"Ice damages, slows and freezes prey");
            var terrain=level.getBlockState(pos.below());ice.shatter(level);ice.shatter(level);check(ice.isRemoved()&&level.getBlockState(pos.below()).equals(terrain),"Ice shatters once without placing/destroying blocks");
            reloaded.discard();impact.discard();
        } finally {spells.forEach(Entity::discard);NeoForge.EVENT_BUS.unregister(spellObserver);}
        Set<EntityType<?>> types=Set.of(ExpansionContent.SUNKEN_SKELETON.get(),ExpansionContent.TUFF_GOLEM.get(),ExpansionContent.BARNACLE.get(),ExpansionContent.ICEOLOGER.get(),ExpansionContent.WILDFIRE.get());
        level.registryAccess().lookupOrThrow(Registries.BIOME).listElements().forEach(holder -> {
            for(var category:MobCategory.values()) check(holder.value().getMobSettings().getMobs(category).unwrap().stream().noneMatch(e->types.contains(e.value().type())),"No biome spawn entries for new creatures");
        });
        List<ItemEntity> loot=new ArrayList<>();Consumer<EntityJoinLevelEvent> lootObserver=e->{if(e.getEntity() instanceof ItemEntity item)loot.add(item);};NeoForge.EVENT_BUS.addListener(lootObserver);
        try {for(var type:types){Mob mob=(Mob)type.create(level,EntitySpawnReason.COMMAND);mob.snapTo(180.5,180,180.5,0,0);loot.clear();mob.hurtServer(level,level.damageSources().genericKill(),Float.MAX_VALUE);check(loot.isEmpty(),"No generated loot for "+type);mob.discard();}}finally{NeoForge.EVENT_BUS.unregister(lootObserver);}
        ChocolateFlavored.LOGGER.info("EXPANSION_VERIFICATION_PASSED: native crossbow/fireballs, shields/regen/save-load, tuff item conservation/home/cloak, swimmer/boat tether cancellation, ice warning/impact/persistence, empty loot and no natural spawns");
    }
    private static Entity restore(ServerLevel level,Entity entity){var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());check(entity.saveAsPassenger(out),"Entity can be saved");return EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()),level,EntitySpawnReason.LOAD).orElseThrow();}
    private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException("Expansion verification: "+message);}
    private static final class TestArrow extends net.minecraft.world.entity.projectile.arrow.Arrow {TestArrow(ServerLevel l){super(EntityType.ARROW,l);}float waterDrag(){return getWaterInertia();}}
    private static final class TestWildfire extends Wildfire {TestWildfire(ServerLevel l){super(ExpansionContent.WILDFIRE.get(),l);}void step(ServerLevel l){super.customServerAiStep(l);}}
    private static final class TestTuff extends TuffGolem {TestTuff(ServerLevel l){super(ExpansionContent.TUFF_GOLEM.get(),l);}void step(ServerLevel l){super.customServerAiStep(l);}}
    private static final class TestBarnacle extends Barnacle {boolean wet=true;TestBarnacle(ServerLevel l){super(ExpansionContent.BARNACLE.get(),l);}@Override public boolean isInWater(){return wet;}void step(ServerLevel l){super.customServerAiStep(l);}}
    private static final class TestIce extends IceChunk {TestIce(ServerLevel l){super(ExpansionContent.ICE_CHUNK.get(),l);}boolean hit(ServerLevel l,Entity owner,LivingEntity target){return super.affect(l,owner,target);}}
    private static final class CombatPlayer extends FakePlayer {
        boolean wet;
        CombatPlayer(ServerLevel level){super(level,new com.mojang.authlib.GameProfile(UUID.fromString("1434d06a-d27d-454a-ae78-d84c8ec31c86"),"CreatureChecks"));setInvulnerable(false);setGameMode(GameType.SURVIVAL);}
        @Override public boolean isInvulnerableTo(ServerLevel l,DamageSource s){return false;}
        @Override public boolean isInWater(){return wet;}
    }
    private ExpansionVerification(){}
}
