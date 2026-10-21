package com.akiotsukino.chocolateflavored.verification;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mob.tuff.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.common.util.FakePlayer;
public final class TuffGolemVerification {
    public static void run(ServerLevel level,FakePlayer player){
        verifyConstruction(level);
        player.setGameMode(GameType.SURVIVAL);player.getInventory().clearContent();
        TestGolem golem=new TestGolem(level);golem.snapTo(210.5,180,210.5,90,0);for(int i=0;i<201;i++)golem.step(level);
        check(golem.getMaxHealth()==20&&!golem.sleeping()&&!golem.immobilized(),"20 health and standing after initial ten seconds");
        golem.setHealth(10);held(player,Items.TUFF,2);golem.mobInteract(player,InteractionHand.MAIN_HAND);check(golem.getHealth()==15&&player.getMainHandItem().getCount()==1,"Tuff heals five and consumes one");
        held(player,Items.BLUE_DYE,2);golem.mobInteract(player,InteractionHand.MAIN_HAND);check(golem.color()==DyeColor.BLUE.getId()&&player.getMainHandItem().getCount()==1,"Dye colors cloth and consumes once");
        held(player,Items.HONEYCOMB,2);golem.mobInteract(player,InteractionHand.MAIN_HAND);check(golem.glued()&&golem.immobilized()&&player.getMainHandItem().getCount()==1,"Wax immobilizes without consuming extra");
        held(player,Items.IRON_AXE,1);golem.mobInteract(player,InteractionHand.MAIN_HAND);check(!golem.glued()&&player.getMainHandItem().getDamageValue()==1,"Axe removes wax with one durability");
        held(player,Items.DIAMOND,3);golem.mobInteract(player,InteractionHand.MAIN_HAND);check(golem.holding()&&golem.getMainHandItem().getCount()==1&&player.getMainHandItem().getCount()==2&&golem.animation().equals("showItem"),"Transfer exactly one displayed item with original animation");
        for(int i=0;i<11;i++)golem.step(level);
        golem.setSleeping(true);check(golem.sleeping()&&golem.animation().equals("sleepWithItem"),"Sleeping with item selects original animation");
        var reloaded=(TuffGolem)restore(level,golem);check(reloaded.sleeping()&&reloaded.holding()&&reloaded.color()==DyeColor.BLUE.getId()&&reloaded.home().distanceToSqr(golem.home())<.001,"Equipment, pose, color and exact home survive reload");
        for(int i=0;i<20;i++)golem.step(level);held(player,Items.AIR,0);golem.mobInteract(player,InteractionHand.MAIN_HAND);check(!golem.holding()&&!golem.sleeping()&&golem.animation().equals("wakeAndHideItem"),"Retrieving item wakes using combined animation");check(player.getInventory().countItem(Items.DIAMOND)==1,"Displayed item returned exactly once");
        for(int i=0;i<29;i++)golem.step(level);
        var item=new ItemEntity(level,210.5,180,210.5,new ItemStack(Items.EMERALD,4));item.setNoPickUpDelay();check(golem.collect(item)&&item.getItem().getCount()==3&&golem.getMainHandItem().is(Items.EMERALD),"Collect exactly one loose item");check(!golem.collect(item)&&item.getItem().getCount()==3,"Occupied display cannot duplicate loose items");
        for(int i=0;i<11;i++)golem.step(level);golem.setSleeping(true);for(int i=0;i<5999;i++)golem.step(level);check(golem.sleeping(),"Sleep lasts at least five minutes");for(int i=0;i<6021;i++)golem.step(level);check(!golem.sleeping(),"Sleep ends within ten minutes");
        level.registryAccess().lookupOrThrow(Registries.BIOME).listElements().forEach(h->{for(var category:MobCategory.values())check(h.value().getMobSettings().getMobs(category).unwrap().stream().noneMatch(e->e.value().type()==TuffGolemContent.TUFF_GOLEM.get()),"No natural Tuff Golem spawns");});
        golem.discard();reloaded.discard();item.discard();
        ChocolateFlavored.LOGGER.info("TUFF_GOLEM_VERIFICATION_PASSED: repair, dye, wax/scrape, exact item transfers, transitions, saved home/pose/item and sleep duration; no natural spawns");
    }
    private static void verifyConstruction(ServerLevel level){
        var head=new net.minecraft.core.BlockPos(48,182,0);
        for(var color:DyeColor.values())for(var pumpkin:java.util.List.of(net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN,net.minecraft.world.level.block.Blocks.JACK_O_LANTERN)){
            var base=head.below(2);
            level.setBlock(base,net.minecraft.world.level.block.Blocks.TUFF.defaultBlockState(),3);
            var wool=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(color.getName()+"_wool"));
            level.setBlock(head.below(),wool.defaultBlockState(),3);
            check(((net.minecraft.world.level.block.CarvedPumpkinBlock)net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN).canSpawnGolem(level,head),"Dispenser recognizes wool/tuff base");
            level.setBlock(head,pumpkin.defaultBlockState(),3);
            var golems=level.getEntitiesOfClass(TuffGolem.class,new net.minecraft.world.phys.AABB(base).inflate(2));
            check(golems.size()==1,"Exactly one constructed golem: "+color+" / "+pumpkin+"; count="+golems.size()+"; head="+level.getBlockState(head));
            var golem=golems.getFirst();check(golem.color()==color.getId(),"Wool determines cloth color");
            check(level.isEmptyBlock(head)&&level.isEmptyBlock(head.below())&&level.isEmptyBlock(base),"Consumes exactly the construction column");
            check(golem.home().distanceToSqr(golem.position())<.001,"Construction sets home");golem.discard();
        }
        level.setBlock(head.below(2),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
        level.setBlock(head.below(),net.minecraft.world.level.block.Blocks.WHITE_WOOL.defaultBlockState(),3);
        level.setBlock(head,net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN.defaultBlockState(),3);
        check(!level.isEmptyBlock(head)&&!TuffGolemConstruction.canBuild(level,head),"Invalid base remains intact");
        for(int i=0;i<3;i++)level.removeBlock(head.below(i),false);
    }
    private static void held(FakePlayer player,Item item,int count){player.setItemInHand(InteractionHand.MAIN_HAND,item==Items.AIR?ItemStack.EMPTY:new ItemStack(item,count));}
    private static Entity restore(ServerLevel level,Entity entity){var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());check(entity.saveAsPassenger(out),"Save entity");return EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()),level,EntitySpawnReason.LOAD).orElseThrow();}
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException("Tuff Golem verification: "+message);}
    private static final class TestGolem extends TuffGolem {TestGolem(ServerLevel level){super(TuffGolemContent.TUFF_GOLEM.get(),level);}void step(ServerLevel level){super.customServerAiStep(level);}}
    private TuffGolemVerification(){}
}
