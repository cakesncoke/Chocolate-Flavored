package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.animal.*;
import com.akiotsukino.chocolateflavored.animal.entity.*;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Runs on the exact dedicated-server runtime, including real event dispatch and save/load. */
public final class AnimalVerification {
    public static void run(ServerLevel level, FakePlayer player) {
        spawnRules(level); persistenceAndSpawning(level); interactions(level,player);
        ChocolateFlavored.LOGGER.info("ANIMAL_VERIFICATION_PASSED: spawn boundaries, natural/chunk spawns, no reload rerolls, sheep colors, genetics, flower cows, muddy pigs, horns and jumbo rabbits");
    }
    private static void spawnRules(ServerLevel level) {
        var biomes=level.registryAccess().lookupOrThrow(Registries.BIOME);
        var plains=biomes.getOrThrow(Biomes.PLAINS);var flowers=biomes.getOrThrow(Biomes.FLOWER_FOREST);
        var jungle=biomes.getOrThrow(Biomes.JUNGLE);var snowy=biomes.getOrThrow(Biomes.SNOWY_PLAINS);
        var savanna=biomes.getOrThrow(Biomes.SAVANNA);var peaks=biomes.getOrThrow(Biomes.JAGGED_PEAKS);
        expect(EntityType.COW,plains,true,new Rolls(4999),AnimalContent.DAIRY_COW.get());
        expect(EntityType.COW,plains,true,new Rolls(5000),null);
        expect(EntityType.COW,plains,false,new Rolls(0),null);
        expect(EntityType.COW,flowers,true,new Rolls(499,32),AnimalContent.MOOLIP.get());
        expect(EntityType.COW,flowers,true,new Rolls(499,33),AnimalContent.MOOBLOOM.get());
        expect(EntityType.COW,flowers,true,new Rolls(500),AnimalContent.DAIRY_COW.get());
        expect(EntityType.COW,flowers,true,new Rolls(5499),AnimalContent.DAIRY_COW.get());
        expect(EntityType.COW,flowers,true,new Rolls(5500),null);
        expect(EntityType.PIG,plains,true,new Rolls(9999),AnimalContent.PALE_PIG.get());
        expect(EntityType.PIG,plains,true,new Rolls(10000),null);
        expect(EntityType.PIG,plains,false,new Rolls(0),null);
        expect(EntityType.CHICKEN,plains,false,new Rolls(249),AnimalContent.FANCY_CHICKEN.get());
        expect(EntityType.CHICKEN,plains,false,new Rolls(250),null);
        expect(EntityType.CHICKEN,jungle,false,new Rolls(250),AnimalContent.BRONZED_CHICKEN.get());
        expect(EntityType.CHICKEN,jungle,false,new Rolls(5249),AnimalContent.BRONZED_CHICKEN.get());
        expect(EntityType.CHICKEN,jungle,false,new Rolls(5250),null);
        expect(EntityType.SHEEP,snowy,false,new Rolls(99999,4),AnimalContent.PATCHED_SHEEP.get());
        expect(EntityType.SHEEP,snowy,false,new Rolls(99999,5),AnimalContent.FUZZY_SHEEP.get());
        expect(EntityType.SHEEP,savanna,false,new Rolls(99999),AnimalContent.LONG_NOSE_SHEEP.get());
        expect(EntityType.SHEEP,peaks,false,new Rolls(2999),AnimalContent.HORNED_SHEEP.get());
        expect(EntityType.SHEEP,peaks,false,new Rolls(3000,99),AnimalContent.FUZZY_SHEEP.get());
        expect(EntityType.RABBIT,flowers,false,new Rolls(249),AnimalContent.JUMBO_RABBIT.get());
        expect(EntityType.RABBIT,flowers,false,new Rolls(250),null);
        expect(EntityType.RABBIT,plains,false,new Rolls(0),null);
        for (var biome:java.util.List.of(plains,snowy,savanna)) {
            int pink=0;
            var random=new LegacyRandomSource(6142);
            for(int i=0;i<50_000;i++) {
                var color=net.minecraft.world.entity.animal.sheep.SheepColorSpawnRules.getSheepColor(biome,random);
                check(color==DyeColor.WHITE||color==DyeColor.PINK,"Only white or pink natural sheep");
                if(color==DyeColor.PINK) pink++;
            }
            check(pink>40&&pink<130,"Vanilla rare pink roll retained: "+pink);
        }
    }
    private static void persistenceAndSpawning(ServerLevel level) {
        BlockPos pos=new BlockPos(72,164,72); AABB area=new AABB(pos).inflate(5);
        level.getChunkAt(pos);
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),"fillbiome 64 160 64 79 167 79 minecraft:snowy_plains");
        check(level.getBiome(pos).is(Biomes.SNOWY_PLAINS),"Test biome changed");
        level.getEntitiesOfClass(Entity.class,area).forEach(Entity::discard);
        for(var reason:java.util.List.of(EntitySpawnReason.NATURAL,EntitySpawnReason.CHUNK_GENERATION)) {
            Sheep original=EntityType.SHEEP.create(level,reason); original.snapTo(72.5,164,72.5,0,0);
            original.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),reason,null);original.setAge(-1234);original.setColor(DyeColor.PINK);
            if(reason==EntitySpawnReason.NATURAL) check(!level.addFreshEntity(original),"Natural original replaced through event bus");
            else {
                // Simulate a newly generated protochunk's entities being deserialized before first joining the level.
                var restored=restore(level,original);var event=new EntityJoinLevelEvent(restored,level,true); AnimalSpawns.join(event);
                check(event.isCanceled(),"Chunk-generation spawn replacement survives protochunk serialization");
            }
            var spawned=level.getEntitiesOfClass(EarthSheep.class,area);
            check(spawned.size()==1,"Exactly one cold sheep replacement");
            var sheep=spawned.getFirst();check(sheep.getAge()==-1234&&sheep.getColor()==DyeColor.PINK,"Replacement preserves age and rare pink color");
            var loaded=restore(level,sheep);var loadEvent=new EntityJoinLevelEvent(loaded,level,true);AnimalSpawns.join(loadEvent);
            check(!loadEvent.isCanceled()&&loaded.getType()==sheep.getType(),"Saved animals do not reroll");sheep.discard();
        }
        var command=EntityType.SHEEP.create(level,EntitySpawnReason.COMMAND);command.snapTo(72.5,164,72.5,0,0);
        command.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),EntitySpawnReason.COMMAND,null);
        check(level.addFreshEntity(command),"Command sheep not randomly replaced");command.discard();
        for(var holder:AnimalContent.ENTITIES.getEntries()) {
            Mob mob=(Mob)holder.get().create(level,EntitySpawnReason.COMMAND);
            check(mob!=null&&mob.getMaxHealth()>0,"All attributes initialized: "+holder.getId());
            mob.snapTo(72.5,164,72.5,0,0);
            check(restore(level,mob).getType()==mob.getType(),"Every animal type round trips entity serialization");
            if(mob instanceof Sheep sheep) {
                sheep.setColor(DyeColor.BLUE);Sheep baby=sheep.getBreedOffspring(level,sheep);
                check(baby!=null&&baby.getType()==sheep.getType(),"Sheep variant breeds true");
                check(Wolf.PREY_SELECTOR.test(sheep,level),"Vanilla wolf prey selection recognizes custom sheep");
            }
        }
    }
    private static void interactions(ServerLevel level,FakePlayer player) {
        player.snapTo(72.5,164,72.5,0,0);
        for(var type:java.util.List.of(AnimalContent.MOOBLOOM.get(),AnimalContent.MOOLIP.get())) {
            Moobloom cow=(Moobloom)type.create(level,EntitySpawnReason.COMMAND);cow.snapTo(72.5,164,72.5,0,0);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BONE_MEAL));cow.mobInteract(player,InteractionHand.MAIN_HAND);
            check(player.getMainHandItem().is(cow.getFlower().asItem()),"Bone meal gives the matching flower");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BOWL));cow.mobInteract(player,InteractionHand.MAIN_HAND);
            check(player.getMainHandItem().is(Items.SUSPICIOUS_STEW)&&player.getMainHandItem().has(DataComponents.SUSPICIOUS_STEW_EFFECTS),"Flower cow stew has flower effects");
            cow.setBaby(true);check(!cow.isShearable(player,new ItemStack(Items.SHEARS),level,cow.blockPosition()),"Baby flower cows cannot be sheared");cow.setBaby(false);
            var drops=cow.onSheared(player,new ItemStack(Items.SHEARS),level,cow.blockPosition());
            check(drops.size()==4&&drops.stream().allMatch(s->s.is(cow.getFlower().asItem())),"Shearing yields four matching flowers");
        }
        Pig pig=EntityType.PIG.create(level,EntitySpawnReason.COMMAND);pig.snapTo(72.5,164,72.5,0,0);
        level.setBlockAndUpdate(pig.blockPosition().below(),Blocks.MUD.defaultBlockState());
        for(int i=0;i<40;i++) MuddyPigRules.tick(new EntityTickEvent.Post(pig));
        MuddyPig state=(MuddyPig)pig;check(state.chocolateflavored$isMuddy(),"Pig becomes muddy after rolling in vanilla mud");
        var restored=(MuddyPig)restore(level,pig);check(restored.chocolateflavored$isMuddy()&&restored.chocolateflavored$getFlowerColor()==state.chocolateflavored$getFlowerColor(),"Muddy flower state persists");
        var shear=(net.neoforged.neoforge.common.IShearable)pig;
        var dye=shear.onSheared(player,new ItemStack(Items.SHEARS),level,pig.blockPosition());
        check(dye.size()>=1&&dye.size()<=3&&state.chocolateflavored$isSheared(),"Muddy pig flowers shear into vanilla dye");
        HornedSheep horned=AnimalContent.HORNED_SHEEP.get().create(level,EntitySpawnReason.COMMAND);
        check(horned.hasHorn()&&horned.createHorn().is(Items.GOAT_HORN)&&horned.createHorn().has(DataComponents.INSTRUMENT),"Horned sheep uses functional vanilla goat horn");
        horned.snapTo(74.5,164,72.5,0,0);check(horned.dropHorn()&&!horned.hasHorn()&&!horned.dropHorn(),"Horns break only once");
        check(!((HornedSheep)restore(level,horned)).hasHorn(),"Broken horns remain broken after saving");
        var jumbo=AnimalContent.JUMBO_RABBIT.get().create(level,EntitySpawnReason.COMMAND);
        check(jumbo.getAttributeValue(Attributes.MAX_HEALTH)==10&&jumbo.getAttributeValue(Attributes.ATTACK_DAMAGE)==5,"Jumbo rabbit retains EarthMobs attributes");
        check(jumbo.getBbWidth()>.6&&jumbo.getBreedOffspring(level,jumbo).getType()==jumbo.getType(),"Jumbo rabbit dimensions and offspring");
        level.getEntitiesOfClass(Entity.class,new AABB(new BlockPos(72,164,72)).inflate(6)).forEach(Entity::discard);
    }
    private static Entity restore(ServerLevel level,Entity entity) {
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());entity.save(out);
        return EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()),level,EntitySpawnReason.LOAD).orElseThrow();
    }
    private static void expect(EntityType<?> type,Holder<Biome> biome,boolean temperate,Rolls rolls,EntityType<?> expected) {
        check(AnimalSpawns.select(type,biome,temperate,rolls)==expected,"Spawn boundary: "+type+" in "+biome);
    }
    private static void check(boolean ok,String description) { if(!ok) throw new IllegalStateException(description); }
    private static final class Rolls extends LegacyRandomSource {
        private final int[] values;private int index;
        Rolls(int... values) { super(0);this.values=values; }
        @Override public int nextInt(int bound) { int value=values[index++];check(value>=0&&value<bound,"Valid deterministic roll");return value; }
    }
    private AnimalVerification() {}
}
