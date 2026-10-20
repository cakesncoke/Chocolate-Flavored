package com.akiotsukino.chocolateflavored.mob.entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
/** Uses the current vanilla zombie goals; poison follows the husk's unarmed-hit timing. */
public class Swamped extends Zombie {
 public Swamped(EntityType<? extends Zombie> type,Level level){super(type,level);}
 public static AttributeSupplier.Builder createAttributes(){return Zombie.createAttributes().add(Attributes.MAX_HEALTH,24).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.ATTACK_DAMAGE,3).add(Attributes.ARMOR,2);}
 @Override public boolean doHurtTarget(ServerLevel level,Entity target){
  boolean hit=super.doHurtTarget(level,target);
  if(hit&&getMainHandItem().isEmpty()&&target instanceof Player player){
   float difficulty=level.getCurrentDifficultyAt(blockPosition()).getEffectiveDifficulty();
   player.addEffect(new MobEffectInstance(MobEffects.POISON,140*(int)difficulty),this);
  }
  return hit;
 }
}
