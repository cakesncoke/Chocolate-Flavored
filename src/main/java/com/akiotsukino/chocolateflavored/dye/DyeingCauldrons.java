package com.akiotsukino.chocolateflavored.dye;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.stats.Stats;
import net.minecraft.util.TriState;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Explicit allow-list: never turns unrelated modded items or containers into wood. */
public final class DyeingCauldrons {
    private static final Map<Item, String> FAMILIES = new IdentityHashMap<>();
    private static final Map<Item, DyeColor> DYES = new IdentityHashMap<>();
    private static final List<String> VANILLA_WOODS = List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped");
    private static final List<String> COLORED = List.of("wool", "concrete", "glass", "glass_pane", "terracotta", "candle", "concrete_powder");
    public static void initialize() {
        for (DyeColor c : DyeColor.values()) {
            DYES.put(vanilla(c.getName() + "_dye"), c);
            for (String kind : COLORED) {
                String name = c.getName() + "_" + (kind.equals("glass") || kind.equals("glass_pane") ? "stained_" : "") + kind;
                FAMILIES.put(vanilla(name), kind);
            }
            var f = DyedWood.FAMILIES.get(c);
            for (String kind : DyedWood.KINDS) FAMILIES.put(f.item(kind), kind);
        }
        for (String kind : List.of("glass", "glass_pane", "terracotta", "candle")) FAMILIES.put(vanilla(kind), kind);
        for (String wood : VANILLA_WOODS) for (String kind : DyedWood.KINDS) {
            if (kind.equals("boat") && (wood.equals("crimson") || wood.equals("warped"))) continue;
            String name = wood + "_" + (wood.equals("bamboo") && kind.equals("boat") ? "raft" : kind);
            FAMILIES.put(vanilla(name), kind);
        }
        // Bamboo mosaic is also wooden building material, but never a chest boat/container.
        FAMILIES.put(Items.BAMBOO_MOSAIC, "planks");
        FAMILIES.put(Items.BAMBOO_MOSAIC_STAIRS, "stairs");
        FAMILIES.put(Items.BAMBOO_MOSAIC_SLAB, "slab");
        FAMILIES.remove(Items.AIR);
    }
    private static Item vanilla(String path) { return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path)); }
    public static Item target(Item input, DyeColor color) {
        String kind = FAMILIES.get(input);
        if (kind == null) return null;
        if (DyedWood.KINDS.contains(kind)) return DyedWood.FAMILIES.get(color).item(kind);
        if (kind.equals("concrete_powder")) kind = "concrete";
        return vanilla(color.getName() + "_" + (kind.equals("glass") || kind.equals("glass_pane") ? "stained_" : "") + kind);
    }
    /** Match native dye crafting; when no two-dye recipe exists, the newly added color wins. */
    public static DyeColor mix(ServerLevel level, DyeColor old, DyeColor added) {
        if (old == added) return added;
        var input = CraftingInput.of(2, 1, List.of(new ItemStack(vanilla(old.getName() + "_dye")), new ItemStack(vanilla(added.getName() + "_dye"))));
        var recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (recipe.isPresent()) {
            ItemStack output = recipe.get().value().assemble(input);
            DyeColor result = DYES.get(output.getItem());
            if (result != null) return result;
        }
        return added;
    }
    public static void interact(PlayerInteractEvent.RightClickBlock e) {
        if (e.getUseBlock() == TriState.FALSE || e.getUseItem() == TriState.FALSE) return;
        var player = e.getEntity(); var pos = e.getPos(); var stack = e.getItemStack();
        if (player.isSpectator() || !player.mayUseItemAt(pos, e.getFace(), stack) || !e.getLevel().mayInteract(player, pos)
                || !player.isWithinBlockInteractionRange(pos, 0)) return;
        if (use(e.getLevel(), pos, player, e.getHand()).consumesAction()) {
            e.setCanceled(true); e.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    public static InteractionResult use(Level level, BlockPos pos, Player player, InteractionHand hand) {
        BlockState state = level.getBlockState(pos);
        DyeColor existing = DyedWood.BLOCK_COLORS.get(state.getBlock());
        boolean dyed = existing != null && state.is(DyedWood.FAMILIES.get(existing).block("water_cauldron"));
        if (!dyed && !state.is(Blocks.WATER_CAULDRON)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        DyeColor added = DYES.get(held.getItem());
        if (added != null) {
            if (level instanceof ServerLevel server) {
                DyeColor result = dyed ? mix(server, existing, added) : added;
                if (dyed && result == existing) return InteractionResult.SUCCESS;
                level.setBlockAndUpdate(pos, DyedWood.FAMILIES.get(result).block("water_cauldron").defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, state.getValue(LayeredCauldronBlock.LEVEL)));
                held.consume(1, player);
                sound(level, pos, player);
            }
            return InteractionResult.SUCCESS;
        }
        Item replacement = dyed ? target(held.getItem(), existing) : null;
        if (replacement == null) return InteractionResult.PASS;
        if (level instanceof ServerLevel) {
            // Copy the component patch as well as count: names, sign text and shelf contents survive recoloring.
            ItemStack result = new ItemStack(replacement.builtInRegistryHolder(), held.getCount(), held.getComponentsPatch());
            player.setItemInHand(hand, result);
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            player.awardStat(Stats.USE_CAULDRON);
            sound(level, pos, player);
        }
        return InteractionResult.SUCCESS;
    }
    private static void sound(Level level, BlockPos pos, Player player) {
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, .6F, 1.1F);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player));
    }
    private DyeingCauldrons() {}
}
