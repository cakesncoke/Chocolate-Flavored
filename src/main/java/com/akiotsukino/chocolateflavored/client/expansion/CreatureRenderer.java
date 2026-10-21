package com.akiotsukino.chocolateflavored.client.expansion;
import com.akiotsukino.chocolateflavored.mob.expansion.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
public final class CreatureRenderer<T extends Mob> extends MobRenderer<T,CreatureState,NativeCreatureModel> {
    private final Identifier texture;
    public CreatureRenderer(EntityRendererProvider.Context context,String kind) {
        super(context,new NativeCreatureModel(context.bakeLayer(layer(kind)),kind),kind.equals("wildfire")?.7F:.5F);
        texture=Identifier.fromNamespaceAndPath("chocolateflavored","textures/entity/"+kind+"/"+kind+".png");
    }
    public static ModelLayerLocation layer(String kind){return new ModelLayerLocation(Identifier.fromNamespaceAndPath("chocolateflavored",kind),"main");}
    @Override protected int getBlockLightLevel(T entity,net.minecraft.core.BlockPos pos){return entity instanceof Wildfire?15:super.getBlockLightLevel(entity,pos);}
    @Override public CreatureState createRenderState(){return new CreatureState();}
    @Override public Identifier getTextureLocation(CreatureState state){return texture;}
    @Override public void extractRenderState(T entity,CreatureState state,float ticks){
        super.extractRenderState(entity,state,ticks);
        state.casting=entity instanceof Iceologer ice&&ice.castingTicks()>0 || entity instanceof Barnacle barnacle&&barnacle.preyId()!=0;
        state.shields=entity instanceof Wildfire wildfire?wildfire.shields():0;
    }
}
