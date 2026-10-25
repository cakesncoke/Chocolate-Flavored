package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(EntityCollisionContext.class)
abstract class TrimLeafCollisionMixin implements com.akiotsukino.chocolateflavored.trim.TrimMovementContext {
    @Unique private boolean cf$movementValidation;
    @Override public void chocolateflavored$movementValidation() { cf$movementValidation = true; }
    @Inject(method="getCollisionShape",at=@At("HEAD"),cancellable=true)
    private void cf$leaves(BlockState state,CollisionGetter world,BlockPos pos,CallbackInfoReturnable<VoxelShape> cir) {
        var context=(EntityCollisionContext)(Object)this; var entity=context.getEntity();
        if ((context.isPlacement() && !cf$movementValidation) || entity==null || !state.is(BlockTags.LEAVES)) return;
        var player=rider(entity);
        if (player!=null && full(player,"wild") && (player.isShiftKeyDown() || !context.isAbove(Shapes.block(),pos,true))) cir.setReturnValue(Shapes.empty());
    }
}
