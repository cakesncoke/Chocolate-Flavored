# Pastel wood and dyeing cauldrons

Minecraft 26.1.2 / NeoForge 26.1.2.122. Standalone, with no new dependencies.

Every vanilla dye has planks, oak-pattern doors and trapdoors, stairs, slabs, pressure plates, fences, gates, signs, hanging signs, buttons, boats and shelves. These use native block/item/entity classes. Signs support normal editing, two-sided text, ink and wax; shelves retain their native inventory/redstone behavior. Boats use native models, rowing animations, leashes and drops. Items appear in the mod creative tab; the Chocolate Cake icon remains unchanged.

The palette mixes 45% white into each native dye RGB, keeping black/gray muted and all sixteen colors distinct. Block and item model tints apply to vanilla textures at render time: oak doors/trapdoors, pale oak for the other wood. Signs and boats tint only their native model submission. Minecraft textures are referenced, not redistributed or edited.

One vanilla plank plus one vanilla dye crafts one corresponding dyed plank (shapeless). The explicit `chocolateflavored:vanilla_planks` ingredient tag includes all twelve vanilla wood families, including bamboo, crimson and warped. Dyed planks join vanilla `planks` tags and work in generic plank recipes. Standard plank-based recipes produce matching doors, trapdoors, stairs, slabs, plates, fences, gates, signs, buttons and boats with vanilla counts. Hanging signs and shelves can be obtained by dyeing their vanilla counterparts; no new log types are introduced.

## Cauldron rules

- Right-click water with a vanilla dye to color it without changing its water level. One dye is consumed in survival; adding the existing color does nothing.
- For two different dyes, use the current datapack's vanilla-dye crafting result if one exists; otherwise the new dye replaces the old color. Outputs are limited to the sixteen vanilla dye items. No arbitrary RGB mixing.
- Right-click dyed water with any held amount of supported items: the entire held stack changes color and exactly one water level is consumed. This also applies in creative. Vanilla stack limits remain unchanged (boats stack to one, signs to sixteen).
- Supports vanilla and this mod's dyed wooden families; wool, concrete, plain/stained glass and panes, terracotta, candles and concrete powder. Powder also hardens into concrete. Bamboo rafts and mosaic building blocks are accepted. Chest boats, unrelated containers, iron doors and nonwood stairs are excluded.
- Preserve item component patches, including custom names and stored sign/shelf data. Only the item identity changes.
- Native buckets, bottles, rain, dripstone, extinguishing, comparators and breaking the cauldron continue to use vanilla behavior. Buckets/bottles extract ordinary water; dye belongs to the cauldron, not a new fluid or portable water item.
- The final use becomes an empty vanilla cauldron. Color and level are block states, so they synchronize and persist without a custom block entity.

Recipe data and tags are datapack-editable. No crop, season or timer mechanics change. No Serene Seasons runtime compatibility claim is made.

## Verification

The development-only dedicated-server suite exercises all sixteen colors, actual player interactions, full stacks, metadata preservation, dye combinations and replacement, powder hardening, emptying/buckets, recipes/tags, native block entities and colored boat items/loot. The client suite checks all block-state models and submits all 208 items and sixteen boat renderers. These fixtures are excluded from the release JAR.
