package com.akiotsukino.chocolateflavored.verification;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.equipment.SteelContent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
final class SurvivalRefinementVerification {
 static void run(ServerLevel level){
  var ops=level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
  for(String tool:java.util.List.of("sword","pickaxe","axe","shovel","hoe")){
   String json="{\"type\":\"minecraft:smithing_transform\",\"template\":\"chocolateflavored:rose_gold_upgrade_smithing_template\",\"base\":\"minecraft:copper_"+tool+"\",\"addition\":\"chocolateflavored:rose_gold_ingot\",\"result\":{\"id\":\"chocolateflavored:rose_gold_"+tool+"\"}}";
   var recipe=(SmithingTransformRecipe)Recipe.CODEC.parse(ops,JsonParser.parseString(json)).getOrThrow();
   var base=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("minecraft:copper_"+tool)));base.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Kept name"));base.setDamageValue(7);
   var input=new SmithingRecipeInput(new ItemStack(ModContent.ROSE_GOLD_UPGRADE.get()),base,new ItemStack(ModContent.ROSE_GOLD_INGOT.get()));
   var result=recipe.assemble(input);
   if(!recipe.matches(input,level)||result.getDamageValue()!=7||!result.get(DataComponents.CUSTOM_NAME).getString().equals("Kept name"))throw new IllegalStateException("Smithing components lost: "+tool);
  }
  if(!new ItemStack(SteelContent.STEEL_INGOT.get()).has(DataComponents.PROVIDES_TRIM_MATERIAL))throw new IllegalStateException("Steel trim component missing");
  level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(ResourceKey.create(Registries.TRIM_MATERIAL,Identifier.parse("chocolateflavored:steel")));
  var old=ItemStack.CODEC.parse(ops,JsonParser.parseString("{\"id\":\"chocolateflavored:oven\",\"count\":1}")).getOrThrow();
  if(!old.is(com.akiotsukino.chocolateflavored.cooking.CookingContent.MUD_KILN_ITEM.get()))throw new IllegalStateException("Legacy kiln alias failed");
  ChocolateFlavored.LOGGER.info("SURVIVAL_REFINEMENT_VERIFICATION_PASSED: five native copper upgrades preserve components, steel trim material, legacy kiln item alias");
 }
}
