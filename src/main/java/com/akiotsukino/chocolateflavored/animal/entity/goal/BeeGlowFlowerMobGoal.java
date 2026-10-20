// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs, 26.1.x revision d5acab9846bd3400ac5f093bfec7439351a1bd61.
package com.akiotsukino.chocolateflavored.animal.entity.goal;

import com.akiotsukino.chocolateflavored.animal.api.IHasFlower;
import com.akiotsukino.chocolateflavored.animal.api.IPlantMob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.bee.Bee;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class BeeGlowFlowerMobGoal extends Goal {
    public static final Predicate<LivingEntity> FLOWER_MOB_SELECTOR = (p_30437_) -> {
        return p_30437_ instanceof IPlantMob && (!(p_30437_ instanceof IHasFlower) || p_30437_ instanceof IHasFlower && ((IHasFlower) p_30437_).hasFlower());
    };
    private final Bee bee;

    public BeeGlowFlowerMobGoal(Bee bee) {
        this.bee = bee;
    }


    public boolean canBeeUse() {
        if (this.bee.getCropsGrownSincePollination() >= 10) {
            return false;
        } else if (this.bee.getRandom().nextFloat() < 0.3F) {
            return false;
        } else {
            return this.bee.hasNectar() && this.bee.isHiveValid();
        }
    }


    @Override
    public boolean canUse() {
        return this.canBeeUse();
    }

    public void tick() {
        if (this.bee.getRandom().nextInt(this.adjustedTickDelay(30)) == 0) {

            Optional<Animal> optional = this.findNearbyFlowerMob();
            if (optional.isPresent()) {
                optional.get().ageUp(80, true);
                this.bee.incrementNumCropsGrownSincePollination();
                for (int i = 0; i < 16; i++) {
                    int x = optional.get().getBlockX() + this.bee.getRandom().nextInt(6) - this.bee.getRandom().nextInt(3);
                    int y = optional.get().getBlockY() + this.bee.getRandom().nextInt(6) - this.bee.getRandom().nextInt(3);
                    int z = optional.get().getBlockZ() + this.bee.getRandom().nextInt(6) - this.bee.getRandom().nextInt(3);
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (optional.get() instanceof IPlantMob flowerCow) {
                        if (flowerCow.getPlant().defaultBlockState().canSurvive(this.bee.level(), blockPos)) {
                            if (this.bee.level().getBlockState(blockPos).isAir()) {
                                this.bee.level().setBlock(blockPos, flowerCow.getPlant().defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }

        }
    }

    private Optional<Animal> findNearbyFlowerMob() {
        return this.findNearestFlowerMob(FLOWER_MOB_SELECTOR, 8.0D);
    }

    private Optional<Animal> findNearestFlowerMob(Predicate<LivingEntity> p_28076_, double p_28077_) {
        List<Animal> livingEntity = this.bee.level().getEntitiesOfClass(Animal.class, this.bee.getBoundingBox().expandTowards(0, -p_28077_, 0), p_28076_);

        if (!livingEntity.isEmpty()) {
            return livingEntity.stream().min(java.util.Comparator.comparingDouble(this.bee::distanceToSqr));
        }

        return Optional.empty();
    }
}
