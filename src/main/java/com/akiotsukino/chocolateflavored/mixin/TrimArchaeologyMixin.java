package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.akiotsukino.chocolateflavored.trim.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(BrushableBlockEntity.class)
abstract class TrimArchaeologyMixin {
    @Shadow private ResourceKey<LootTable> lootTable;
    @Shadow private long lootTableSeed;
    @Shadow private int brushCount;
    @Shadow private long coolDownEndsAtTick;
    @Shadow private void dropContent(ServerLevel level,LivingEntity user,ItemStack brush) {}
    @Unique private ResourceKey<LootTable> cf$original;
    @Inject(method="unpackLootTable",at=@At("HEAD"))
    private void cf$context(ServerLevel level,LivingEntity user,ItemInstance brush,CallbackInfo ci){if(lootTable!=null)cf$original=lootTable;}
    @Inject(method="brushingCompleted",at=@At("HEAD"),cancellable=true)
    private void cf$preserve(ServerLevel level,LivingEntity user,ItemStack brush,CallbackInfo ci){
        BlockEntity self=(BlockEntity)(Object)this;
        boolean first=ArchaeologyMemory.claim(level,self.getBlockPos());
        if(!first || cf$original==null || !TrimAbilities.full(user,"dune") || level.random.nextFloat()>=.2f)return;
        dropContent(level,user,brush);
        lootTable=cf$original;lootTableSeed=0;brushCount=0;coolDownEndsAtTick=level.getGameTime()+10;
        level.setBlock(self.getBlockPos(),self.getBlockState().setValue(BlockStateProperties.DUSTED,0),3);
        self.setChanged();ci.cancel();
    }
    @Inject(method="saveAdditional",at=@At("TAIL"))
    private void cf$save(ValueOutput output,CallbackInfo ci){if(cf$original!=null)output.store("chocolateflavored_original_loot",LootTable.KEY_CODEC,cf$original);}
    @Inject(method="loadAdditional",at=@At("TAIL"))
    private void cf$load(ValueInput input,CallbackInfo ci){cf$original=input.read("chocolateflavored_original_loot",LootTable.KEY_CODEC).orElse(null);}
}
