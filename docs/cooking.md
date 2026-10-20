# Oven and cooking pot

Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. Both blocks are standalone; Farmer's Delight, KubeJS and recipe-viewer mods are not required. Assets, layout and steam behavior are attributed to Farmer's Delight in `THIRD_PARTY_NOTICES.md`, with its MIT notice included in the release JAR.

## Oven

ID: `chocolateflavored:oven`. It is named **Oven** throughout the mod and is placed **unlit**. Flint and steel and fire charges light it immediately with vanilla item consumption. The fire starter uses its existing ten-second hold and consumes one durability on successful ignition. A shovel or water bucket extinguishes it. Once lit, it burns indefinitely, following Farmer's Delight's cooking-block behavior.

Right-click the uncovered top/block with a matching ingredient to place a batch. Six batches cook independently, with visible items on top. A batch can require multiple items (for example, three wheat). Cooked items appear above the oven. Unlit batches cool toward zero progress. Covering the top, including placing a cooking pot on it, returns the raw ingredients; the oven can then heat the pot. Breaking the oven returns its uncooked inventory. Cooking progress and inventory survive saving/loading. Standing on the hot oven causes campfire damage unless sneaking.

The oven first searches `chocolateflavored:oven` recipes. If no custom recipe matches, it uses vanilla `minecraft:campfire_cooking` recipes. Recipe reloads use the current recipe manager and never retain a cached output after a recipe is removed. If a recipe's input count is reduced while a larger batch is already placed, unused ingredients are returned.

Default processing recipes:

| Oven input | Output | Time at 20 TPS |
| --- | --- | --- |
| 3 wheat | 1 bread | 10 seconds |
| 1 potato | 1 baked potato | 10 seconds |

Campfire fallback recipes retain their own cooking times. No crafting recipes for the oven or pot are added.

## Cooking pot

ID: `chocolateflavored:cooking_pot`. Right-click to open its normal container screen. It has **no recipe book, recipe button, ghost recipes or automatic recipe-placement interface**.

The pot has six ingredient slots, a read-only meal display, a serving-container input, and an output slot. Recipes are shapeless: put each listed ingredient in its own slot. Stacks allow repeated batches. The meal buffer holds up to 64 portions, including normally unstackable soups. Supply bowls or the recipe's serving container to produce normal food items, or right-click the pot with the correct container for one serving. Served food keeps its original components and stack limits. Different meal types/components and serving containers cannot mix in one occupied buffer.

The pot requires heat below it. Default heat sources are a **lit oven**, magma blocks, lava cauldrons, lava, lit campfires and fire. A hopper conducts heat from one block further below. These sources and conductors are datapack tags: `chocolateflavored:heat_sources`, `chocolateflavored:tray_heat_sources` and `chocolateflavored:heat_conductors`. This system does not change biome or seasonal mechanics.

Pot support changes between the regular, tray and hanging-handle models. Placement against a block's underside selects the handle; shift-right-click with an empty hand toggles it. Waterlogging is supported. Heated pots produce the supplied boiling sounds and steam particles.

Breaking a pot drops its ingredients, empty containers and output separately. The dropped pot retains its unserved meal buffer and serving-container definition; replacing it restores those meals. Its custom name and vanilla lock component are retained. Hoppers insert ingredients from above, insert containers from the sides and extract served output from below/sides. The meal display is never extractable. Comparators read the inventory.

Default pot recipes (10 seconds per batch):

| Ingredients | Result | Serving container |
| --- | --- | --- |
| Red mushroom + brown mushroom | Mushroom stew | Bowl |
| Six beetroot in six slots | Beetroot soup | Bowl |

## Datapack recipes

Place recipes under `data/<namespace>/recipe/<name>.json`. Current 26.1 ingredient syntax uses item strings, item arrays, or `#namespace:tag` strings. Results use `id`, `count` and optional `components`.

```json
{
  "type": "chocolateflavored:oven",
  "ingredient": "minecraft:wheat",
  "input_count": 3,
  "result": { "id": "minecraft:bread", "count": 1 },
  "cookingtime": 200,
  "experience": 0.1
}
```

`input_count` defaults to 1 and accepts 1–64. Both recipe types accept `cookingtime` from 1–32767 game ticks (default 200) and nonnegative `experience` (default zero).

```json
{
  "type": "chocolateflavored:cooking_pot",
  "ingredients": ["minecraft:red_mushroom", "minecraft:brown_mushroom"],
  "result": { "id": "minecraft:mushroom_stew" },
  "container": { "id": "minecraft:bowl" },
  "cookingtime": 200,
  "experience": 0.1
}
```

Pot recipes accept one to six ingredients, consume one item per occupied ingredient slot and reject extra inputs. Overlapping tag/item ingredients use full shapeless matching. `container` is optional; when omitted, the result's crafting/use remainder supplies it when available. Results without a container move straight to output. Meal results must be non-damageable and fit the 64-portion buffer; each output stack must also satisfy vanilla item validation. Output and container component identity are preserved.

## KubeJS

Use `kubejs/server_scripts/cooking.js` and KubeJS's ordinary custom-JSON recipe API; no Chocolate Flavored-specific addon is needed:

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'chocolateflavored:oven/bread' })
  event.custom({
    type: 'chocolateflavored:oven',
    ingredient: 'minecraft:wheat',
    input_count: 3,
    result: { id: 'minecraft:bread' },
    cookingtime: 160,
    experience: 0.1
  }).id('kubejs:oven_bread')

  event.custom({
    type: 'chocolateflavored:cooking_pot',
    ingredients: ['minecraft:red_mushroom', 'minecraft:brown_mushroom'],
    result: { id: 'minecraft:mushroom_stew' },
    container: { id: 'minecraft:bowl' },
    cookingtime: 160
  }).id('kubejs:mushroom_stew')
})
```

Recipes are loaded through vanilla's recipe manager, so normal datapack/KubeJS recipe reloads apply. The optional compatibility configuration pins KubeJS `26.1.2-8.0.7` and Rhino `2101.2.8-build.91` for development testing only. The standard build and mod metadata require neither.

## Verification

Dedicated-server checks exercise custom and fallback oven recipes, six batches, ingredient counts, ignition/dousing, the fire starter's full use lifecycle on the oven, progress persistence, covered-top returns, heat requirements, shapeless matching, serving, full-buffer behavior, 64-portion save/load/drop/replacement, container consumption and sided automation policies. Client verification quick-plays the saved world, opens the pot through the integrated server, checks synchronized meal counts and heat data, and captures the recipe-book-free screen. A separate KubeJS server loads a script that removes a built-in oven recipe and executes new oven and pot recipes without Farmer's Delight installed.

The verification source set is excluded from the release JAR. Other mods' automation, multiplayer latency/permissions integrations and seasonal-mod compatibility are not claimed as tested.
