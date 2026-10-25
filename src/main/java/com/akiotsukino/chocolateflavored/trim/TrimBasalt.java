package com.akiotsukino.chocolateflavored.trim;
import com.akiotsukino.chocolateflavored.content.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import com.mojang.serialization.MapCodec;
/** Internal, unobtainable basalt, retaining the displaced lava level in its saved block state. */
public final class TrimBasalt extends Block {
    public static final MapCodec<TrimBasalt> CODEC=simpleCodec(TrimBasalt::new);
    public static final DeferredBlock<TrimBasalt> BLOCK=ModContent.BLOCKS.registerBlock("temporary_trim_basalt",TrimBasalt::new,()->BlockBehaviour.Properties.ofFullCopy(Blocks.BASALT).strength(-1,3600000).noLootTable().pushReaction(PushReaction.BLOCK));
    public TrimBasalt(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.LEVEL,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(BlockStateProperties.LEVEL);}
    @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){level.setBlock(pos,Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL,state.getValue(BlockStateProperties.LEVEL)),3);}
    public static void register(){}
    public static void walk(ServerPlayer p){
        if(p.isShiftKeyDown() || p.isInLava() || p.getDeltaMovement().y>.1)return;
        var level=p.level();BlockPos center=p.blockPosition().below();
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-1,0,-1),center.offset(1,0,1))){
            if(!level.hasChunkAt(pos) || !level.mayInteract(p,pos) || !level.getBlockState(pos.above()).isAir())continue;
            var state=level.getBlockState(pos);if(!state.is(Blocks.LAVA))continue;
            if(level.setBlock(pos,BLOCK.get().defaultBlockState().setValue(BlockStateProperties.LEVEL,state.getValue(LiquidBlock.LEVEL)),3))level.scheduleTick(pos,BLOCK.get(),60);
        }
    }
}
