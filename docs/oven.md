# Oven

Registry ID: `chocolateflavored:oven`. Minecraft 1.21.1, NeoForge 21.1.252,
Java 21. No Farmer's Delight or Hardcore Torches dependency.

## Using the Oven

1. Craft it using three iron ingots across the top, bricks in the two middle
   corners, and bricks/campfire/bricks across the bottom. It also appears in
   the Functional Blocks creative tab.
2. Place it: it faces you and starts unlit.
3. Right-click a side with furnace fuel to queue one item. There is one fuel
   stack, separate from the six food slots. A different fuel can be inserted
   when the queued stack is empty. Coal, charcoal, sticks, planks, lava buckets,
   and modded fuels use NeoForge's furnace burn-time lookup.
4. Right-click with flint and steel or a fire charge. No fuel means no ignition
   and no tool/item consumption. If Hardcore Torches is installed, its
   firestarter works by direct right-click and consumes one on success.
5. Right-click with a valid ingredient to place one item in each free cooking
   slot. For ingredients that are also fuels, click the top for cooking or a
   side for fuel. Six items cook independently and appear on top.
6. Finished items pop out above the Oven. Right-click empty-handed to retrieve
   the last uncooked item (both hands must be empty). Sneak-right-click to recover
   queued fuel. If nothing is retrieved, an action-bar message shows fuel status.

Fuel burns continuously while lit, even with no food, and automatically moves
to the next queued fuel unit. After fuel runs out, adding more fuel does not
relight it: use an igniter again. A shovel or water bucket extinguishes it and
discards the remainder of the currently burning fuel unit; queued fuel stays.
Lava fuel returns an empty bucket when consumed. Creative players do not lose
held fuel, food, igniters or tool durability.

Unlit food gradually loses cooking progress. Covering the grilling area ejects
food (as in Farmer's Delight), while the fire can keep burning. Breaking the
block drops its food and queued fuel. The active fuel unit has already been
consumed. Inventory, progress and remaining burn time persist across saves.
An unloaded chunk does not advance the cooking clock.

## Recipes

The Oven first looks for `chocolateflavored:oven_cooking`, then falls back to
`minecraft:campfire_cooking`. It does not automatically import furnace/smoker
recipes. Campfire changes from other data packs are picked up automatically.
If multiple Oven recipes match an input, avoid relying on a particular winner.

Two editable starting recipes are included: one wheat -> one bread and one
kelp -> one dried kelp, each taking 200 ticks (10 seconds). These are initial
balance choices, not recipes copied from Farmer's Delight. Campfire cooking
times remain unchanged. A single-input recipe may produce multiple outputs.

Put a new recipe in `src/main/resources/data/chocolateflavored/recipe/oven_cooking/`:

```json
{
  "type": "chocolateflavored:oven_cooking",
  "ingredient": {"item": "minecraft:wheat"},
  "result": {"id": "minecraft:bread", "count": 1},
  "cookingtime": 200
}
```

`cookingtime` is 1-72000 ticks, defaults to 200. No XP is awarded, matching
surface cooking. These JSON files are hand-maintained; do not generate duplicate
files at the same resource paths. Dedicated recipe-viewer integration is not
included in this first feature.

## Optional ignition compatibility

Extend these item tags in a data pack:

- `chocolateflavored:oven_igniters_damage`: damage one durability on success.
- `chocolateflavored:oven_igniters_consume`: consume one item on success.

Use `{"id": "othermod:item", "required": false}` for optional mod items.
These tags describe direct ignition, not another mod's charging or success-chance
mechanics. Mods that intercept and cancel right-click events may need an adapter.

## Files to know

- `block/OvenBlock.java`: placement, interaction, light, damage and ambient particles.
- `block/entity/OvenBlockEntity.java`: fuel, six cooking slots, recipes, saving and synchronization.
- `recipe/OvenRecipe.java`: recipe format and network serialization.
- `client/renderer/OvenRenderer.java`: the six visible food items.
- `registry/ModRecipes.java`, `ModSounds.java`, `ModTags.java`: registrations and tags.
- `assets/chocolateflavored/`: copied/renamed visuals and translations.
- `THIRD_PARTY_NOTICES.md`: upstream origin and license.

## Manual test checklist

`gradlew runGameTestServer` runs six automated server-side regression tests for
manual ignition/relighting, six-slot cooking and recipe loading, queued fuels
and bucket remainders, save/reload, cooling/covered tops, and drops on removal.
These tests are also run by the GitHub build workflow. They do not replace the
client rendering or optional-mod compatibility checks below.

- Run `gradlew build`, then `gradlew runClient` and `gradlew runServer` separately.
- Test first with only Chocolate Flavored: place unlit, refuse empty ignition,
  fuel then light, cook six items, run out, refill without auto-ignition, relight.
- Test all four facing directions, item placement, animated textures, smoke,
  flames, subtitles, crackle, light emission and hot-surface damage.
- Test food/fuel recovery, mixed fuel rejection, lava-bucket return, extinguish,
  covered top, breaking, save/reload and two-player synchronization.
- Install Hardcore Torches optionally and test its firestarter separately.
  Verify that failed ignition uses no item and success uses exactly one.
- Confirm the same JAR starts on a dedicated server with no client class errors.
