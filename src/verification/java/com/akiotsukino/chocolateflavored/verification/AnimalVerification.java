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
        spawnRules(level); persistenceAndSpawning(level); interactions(level,player); loot(level,player);
        ChocolateFlavored.LOGGER.info("ANIMAL_VERIFICATION_PASSED: spawn boundaries, natural/chunk spawns, no reload rerolls, sheep colors, genetics, flower cows, muddy pigs, horns and jumbo rabbits");
    }
    private static void spawnRules(ServerLevel level) {
        var biomes=level.registryAccess().lookupOrThrow(Registries.BIOME);
        var plains=biomes.getOrThrow(Biomes.PLAINS);var flowers=biomes.getOrThrow(Biomes.FLOWER_FOREST);
        var jungle=biomes.getOrThrow(Biomes.JUNGLE);var snowy=biomes.getOrThrow(Biomes.SNOWY_PLAINS);
        var savanna=biomes.getOrThrow(Biomes.SAVANNA);var peaks=biomes.getOrThrow(Biomes.JAGGED_PEAKS);
        expect(EntityTypes.COW,plains,true,new Rolls(4999),AnimalContent.DAIRY_COW.get());
        expect(EntityTypes.COW,plains,true,new Rolls(5000),null);
        expect(EntityTypes.COW,plains,false,new Rolls(0),null);
        expect(EntityTypes.COW,flowers,true,new Rolls(499,32),AnimalContent.MOOLIP.get());
        expect(EntityTypes.COW,flowers,true,new Rolls(499,33),AnimalContent.MOOBLOOM.get());
        expect(EntityTypes.COW,flowers,true,new Rolls(500),AnimalContent.DAIRY_COW.get());
        expect(EntityTypes.COW,flowers,true,new Rolls(5499),AnimalContent.DAIRY_COW.get());
        expect(EntityTypes.COW,flowers,true,new Rolls(5500),null);
        expect(EntityTypes.PIG,plains,true,new Rolls(9999),AnimalContent.PALE_PIG.get());
        expect(EntityTypes.PIG,plains,true,new Rolls(10000),null);
        expect(EntityTypes.PIG,plains,false,new Rolls(0),null);
        expect(EntityTypes.CHICKEN,plains,false,new Rolls(249),AnimalContent.FANCY_CHICKEN.get());
        expect(EntityTypes.CHICKEN,plains,false,new Rolls(250),null);
        expect(EntityTypes.CHICKEN,jungle,false,new Rolls(250),AnimalContent.BRONZED_CHICKEN.get());
        expect(EntityTypes.CHICKEN,jungle,false,new Rolls(5249),AnimalContent.BRONZED_CHICKEN.get());
        expect(EntityTypes.CHICKEN,jungle,false,new Rolls(5250),null);
        expect(EntityTypes.SHEEP,snowy,false,new Rolls(99999,4),AnimalContent.PATCHED_SHEEP.get());
        expect(EntityTypes.SHEEP,snowy,false,new Rolls(99999,5),AnimalContent.FUZZY_SHEEP.get());
        expect(EntityTypes.SHEEP,savanna,false,new Rolls(99999),AnimalContent.LONG_NOSE_SHEEP.get());
        expect(EntityTypes.SHEEP,peaks,false,new Rolls(2999),AnimalContent.HORNED_SHEEP.get());
        expect(EntityTypes.SHEEP,peaks,false,new Rolls(3000,99),AnimalContent.FUZZY_SHEEP.get());
        expect(EntityTypes.RABBIT,flowers,false,new Rolls(249),AnimalContent.JUMBO_RABBIT.get());
        expect(EntityTypes.RABBIT,flowers,false,new Rolls(250),null);
        expect(EntityTypes.RABBIT,plains,false,new Rolls(0),null);
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
        // At ServerStarted, newly loaded chunks may not yet expose entities to AABB queries.
        // Observe actual joins and require addFreshEntity's success through cancellation of the original.
        java.util.List<EarthSheep> joined = new java.util.ArrayList<>();
        java.util.function.Consumer<EntityJoinLevelEvent> observer = (EntityJoinLevelEvent event) -> {
            if (event.getEntity() instanceof EarthSheep sheep && event.getLevel() == level) joined.add(sheep);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(observer);
        try {
        for(var reason:java.util.List.of(EntitySpawnReason.NATURAL,EntitySpawnReason.CHUNK_GENERATION)) {
            joined.clear();
            Sheep original=EntityTypes.SHEEP.create(level,reason); original.snapTo(72.5,164,72.5,0,0);
            original.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),reason,null);original.setAge(-1234);original.setColor(DyeColor.PINK);
            if(reason==EntitySpawnReason.NATURAL) check(!level.addFreshEntity(original),"Natural original replaced through event bus");
            else {
                // Simulate a newly generated protochunk's entities being deserialized before first joining the level.
                var restored=restore(level,original);var event=new EntityJoinLevelEvent(restored,level,true); AnimalSpawns.join(event);
                check(event.isCanceled(),"Chunk-generation spawn replacement survives protochunk serialization");
            }
            var spawned=joined;
            check(spawned.size()==1,"Exactly one cold sheep replacement: "+reason+" count="+spawned.size());
            var sheep=spawned.getFirst();check(sheep.getAge()==-1234&&sheep.getColor()==DyeColor.PINK,"Replacement preserves age and rare pink color");
            var loaded=restore(level,sheep);var loadEvent=new EntityJoinLevelEvent(loaded,level,true);AnimalSpawns.join(loadEvent);
            check(!loadEvent.isCanceled()&&loaded.getType()==sheep.getType(),"Saved animals do not reroll");sheep.discard();
        }
        } finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(observer); }
        var command=EntityTypes.SHEEP.create(level,EntitySpawnReason.COMMAND);command.snapTo(72.5,164,72.5,0,0);
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
            check(cow.isShearable(player,new ItemStack(Items.SHEARS),level,cow.blockPosition()),"Flower cow retains EarthMobs shearability");
            var drops=cow.onSheared(player,new ItemStack(Items.SHEARS),level,cow.blockPosition());
            check(drops.size()==4&&drops.stream().allMatch(s->s.is(cow.getFlower().asItem())),"Shearing yields four matching flowers");
        }
        Pig pig=EntityTypes.PIG.create(level,EntitySpawnReason.COMMAND);pig.snapTo(72.5,164,72.5,0,0);
        level.setBlockAndUpdate(pig.blockPosition().below(),Blocks.MUD.defaultBlockState());
        for(int i=0;i<40;i++) MuddyPigRules.tick(new EntityTickEvent.Post(pig));
        MuddyPig state=(MuddyPig)pig;check(state.chocolateflavored$isMuddy(),"Pig becomes muddy after rolling in vanilla mud");
        var restored=(MuddyPig)restore(level,pig);check(restored.chocolateflavored$isMuddy()&&restored.chocolateflavored$getFlowerColor()==state.chocolateflavored$getFlowerColor(),"Muddy flower state persists");
        var shear=(net.neoforged.neoforge.common.IShearable)pig;
        var dye=shear.onSheared(player,new ItemStack(Items.SHEARS),level,pig.blockPosition());
        check(dye.size()>=1&&dye.size()<=3&&state.chocolateflavored$isSheared(),"Muddy pig flowers shear into vanilla dye");
        pig.setNoAi(true);pig.setNoGravity(true);
        level.setBlockAndUpdate(pig.blockPosition(),Blocks.WATER.defaultBlockState());
        for(int i=0;i<85;i++) { pig.tick();MuddyPigRules.tick(new EntityTickEvent.Post(pig)); }
        check(pig.isInWater()&&!state.chocolateflavored$isMuddy(),"Water washes mud off and prevents immediate recoating over mud");
        HornedSheep horned=AnimalContent.HORNED_SHEEP.get().create(level,EntitySpawnReason.COMMAND);
        check(horned.hasHorn()&&horned.createHorn().is(Items.GOAT_HORN)&&horned.createHorn().has(DataComponents.INSTRUMENT),"Horned sheep uses functional vanilla goat horn");
        horned.snapTo(74.5,164,72.5,0,0);check(horned.dropHorn()&&!horned.hasHorn()&&!horned.dropHorn(),"Horns break only once");
        check(!((HornedSheep)restore(level,horned)).hasHorn(),"Broken horns remain broken after saving");
        var jumbo=AnimalContent.JUMBO_RABBIT.get().create(level,EntitySpawnReason.COMMAND);
        check(jumbo.getAttributeValue(Attributes.MAX_HEALTH)==10&&jumbo.getAttributeValue(Attributes.ATTACK_DAMAGE)==5,"Jumbo rabbit retains EarthMobs attributes");
        check(jumbo.getBbWidth()>.6&&jumbo.getBreedOffspring(level,jumbo).getType()==jumbo.getType(),"Jumbo rabbit dimensions and offspring");
        level.getEntitiesOfClass(Entity.class,new AABB(new BlockPos(72,164,72)).inflate(6)).forEach(Entity::discard);
    }
    private static void loot(ServerLevel level, FakePlayer player) {
        var jumbo=AnimalContent.JUMBO_RABBIT.get().create(level,EntitySpawnReason.COMMAND);
        jumbo.snapTo(72.5,164,72.5,0,0);
        var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY,jumbo)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,jumbo.position())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE,level.damageSources().playerAttack(player))
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.LAST_DAMAGE_PLAYER,player)
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
        var table=level.getServer().reloadableRegistries().getLootTable(jumbo.getType().getDefaultLootTable().orElseThrow());
        int feet=0,maxHide=0;
        var random=new LegacyRandomSource(29817);
        for(int i=0;i<1000;i++) {
            var drops=table.getRandomItems(params,random);
            feet+=drops.stream().filter(stack->stack.is(Items.RABBIT_FOOT)).mapToInt(ItemStack::getCount).sum();
            maxHide=Math.max(maxHide,drops.stream().filter(stack->stack.is(Items.RABBIT_HIDE)).mapToInt(ItemStack::getCount).sum());
        }
        check(feet>240&&feet<360&&maxHide==4,"Jumbo Rabbit retains 30% feet and 0–4 hide drops: "+feet+" feet, "+maxHide+" hide");
        for(var holder:java.util.List.of(AnimalContent.FUZZY_SHEEP,AnimalContent.LONG_NOSE_SHEEP,AnimalContent.PATCHED_SHEEP,AnimalContent.HORNED_SHEEP)) {
            Sheep sheep=(Sheep)holder.get().create(level,EntitySpawnReason.COMMAND);sheep.setColor(DyeColor.PINK);
            var sheepParams=new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY,sheep)
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,sheep.position())
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE,level.damageSources().playerAttack(player))
                    .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
            var sheepTable=level.getServer().reloadableRegistries().getLootTable(sheep.getType().getDefaultLootTable().orElseThrow());
            check(sheepTable.getRandomItems(sheepParams,1L).stream().anyMatch(stack->stack.is(Items.WOOL.pink())),"Custom sheep death loot honors wool color");
            sheep.setSheared(true);
            check(sheepTable.getRandomItems(sheepParams,1L).stream().noneMatch(stack->stack.is(Items.WOOL.pink())),"Sheared sheep cannot duplicate wool on death");
        }
    }
    private static Entity restore(ServerLevel level,Entity entity) {
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());entity.save(out);
        return EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()),level,new net.minecraft.world.entity.EntitySpawnRequest(EntitySpawnReason.LOAD, false)).orElseThrow();
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
