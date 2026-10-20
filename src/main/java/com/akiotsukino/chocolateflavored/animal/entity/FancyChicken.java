package com.akiotsukino.chocolateflavored.animal.entity;
import com.akiotsukino.chocolateflavored.animal.EarthChicken;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.level.Level;
/** EarthMobs' 6000–12000 tick laying interval matches vanilla; uses vanilla eggs under the item limit. */
public class FancyChicken extends EarthChicken {
    public FancyChicken(EntityType<? extends Chicken> type, Level level) { super(type, level); }
}
