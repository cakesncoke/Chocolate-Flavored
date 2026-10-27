# MudKiln and cooking pot

Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. Both blocks are standalone; Farmer's Delight, KubeJS and recipe-viewer mods are not required. Assets, layout and steam behavior are attributed to Farmer's Delight in `THIRD_PARTY_NOTICES.md`, with its MIT notice included in the release JAR.

## MudKiln

ID: `chocolateflavored:mud_kiln`. It is named **MudKiln** throughout the mod and is placed **unlit**. Flint and steel and fire charges light it immediately with vanilla item consumption. The fire starter uses its five-second hold and consumes one durability on successful ignition. A shovel or water bucket extinguishes it. Once lit, it burns indefinitely, following Farmer's Delight's cooking-block behavior. Adding food to the mud_kiln plays the vanilla mud-brick placement sound.

Right-click the uncovered top/block with a matching ingredient to place a batch. Six batches cook independently, with visible items on top. A batch can require multiple items (for example, three wheat). Cooked items appear above the mud_kiln. Unlit batches cool toward zero progress. Covering the top, including placing a cooking pot on it, returns the raw ingredients; the mud_kiln can then heat the pot. Breaking the mud_kiln returns its uncooked inventory. Cooking progress and inventory survive saving/loading. Standing on the hot mud_kiln causes campfire damage unless sneaking.

The mud_kiln first searches `chocolateflavored:mud_kiln` recipes. If no custom recipe matches, it uses vanilla `minecraft:campfire_cooking` recipes. Recipe reloads use the current recipe manager and never retain a cached output after a recipe is removed. If a recipe's input count is reduced while a larger batch is already placed, unused ingredients are returned.

Default processing recipes:

| MudKiln input | Output | Time at 20 TPS |
| --- | --- | --- |
| 3 wheat | 1 bread | 10 seconds |
| 1 potato | 1 baked potato | 10 seconds |

Campfire fallback recipes retain their own cooking times. No crafting recipes for the mud_kiln or pot are added.

## Cooking pot

ID: `chocolateflavored:cooking_pot`. Right-click to open its normal container screen. It has **no recipe book, recipe button, ghost recipes or automatic recipe-placement interface**.

The pot has six input slots and six output slots. Recipes are shapeless: each counted ingredient occupies one slot, and each batch produces one to six output stacks. Unlisted nonempty input slots prevent a match. Every result must fit its normal stack limit (at most 64). Existing compatible outputs are merged first, then empty output slots are filled. The whole batch is blocked if any output cannot fit; no inputs or partial outputs are consumed/created.

Every batch takes **200 simulation ticks (10 seconds at 20 TPS)**, the vanilla furnace default. Accelerated sleep also accelerates cooking. There is no pot-specific `cookingtime` override. Bowls and other required containers are explicit ingredients. The old meal display, separate container slot, right-click serving and artificial 64-portion stack are removed. Input crafting/use remainders are returned into the world, one per consumed ingredient item.

The GUI retains the 176×166 panel and player inventory at (8,84), without a recipe book. Input item positions are (7,16), (25,16), (43,16), (7,34), (25,34), (43,34). Outputs use (115,16), (133,16), (151,16), (115,34), (133,34), (151,34). Progress is at (76,26), heat at (79,62); original icon sizes/scales remain. The pot/custom name is right-aligned at the top.

The pot requires heat below it. Default heat sources are a **lit mud_kiln**, magma blocks, lava cauldrons, lava, lit campfires and fire. A hopper conducts heat from one block further below. These sources and conductors are datapack tags: `chocolateflavored:heat_sources`, `chocolateflavored:tray_heat_sources` and `chocolateflavored:heat_conductors`. This system does not change biome or seasonal mechanics.

Pot support changes between the regular, tray and hanging-handle models. Placement against a block's underside selects the handle; shift-right-click with an empty hand toggles it. Waterlogging is supported. Heated pots produce the supplied boiling sounds and steam particles.

Breaking a pot drops all inputs, outputs and any pending migrated contents separately, as a furnace does. The pot item retains its name and lock, not a second copy of its inventory. Hoppers insert inputs from above/sides and extract outputs from below; outputs can also be pulled from the sides. Comparators read all twelve slots.

Existing placed pots and previously filled pot items migrate their old meal buffer, container slot and output into the new outputs. Buffered meals regain normal stack limits. If all migrated items cannot fit, a saved return queue releases them as output space becomes available and blocks new cooking until drained. Breaking a pot drops the remaining queue too. Already cooked legacy meals are returned as completed items without requiring new bowls. Existing input items remain in their input slots; old input layouts may need rearranging for the updated recipes.

Default mushroom stew now takes one red mushroom, one brown mushroom and one bowl. Beetroot soup takes a stack of six beetroot and one bowl. Both take 200 ticks. No crafting recipes are added.

## Datapack recipes

Place recipes under `data/<namespace>/recipe/<name>.json`. Current 26.1 ingredient syntax uses item strings, item arrays, or `#namespace:tag` strings. Results use `id`, `count` and optional `components`.

```json
{
  "type": "chocolateflavored:mud_kiln",
  "ingredient": "minecraft:wheat",
  "input_count": 3,
  "result": { "id": "minecraft:bread", "count": 1 },
  "cookingtime": 200,
  "experience": 0.1
}
```

`input_count` defaults to 1 and accepts 1–64. Mud kiln recipes accept `cookingtime` from 1–32767 game ticks (default 200) and `experience` from 0–1000 (default zero).

```json
{
  "type": "chocolateflavored:cooking_pot",
  "ingredients": [
    { "ingredient": "minecraft:beetroot", "count": 6 },
    { "ingredient": "minecraft:bowl" }
  ],
  "results": [{ "id": "minecraft:beetroot_soup" }],
  "experience": 0.1
}
```

Pot recipes accept one to six `ingredients` objects (`ingredient`: native item/array/tag syntax, `count`: 1–64, default 1) and one to six `results` item stacks (`id`, `count`, optional `components`). Overlapping item/tag requirements use counted backtracking rather than greedy assignment. `experience` remains optional. Native item components and stack limits are preserved. The previous `result`/`container` pot format must be updated to this schema; old `cookingtime` fields have no effect. No change is required for mud kiln recipes.

## KubeJS

Use `kubejs/server_scripts/cooking.js` and KubeJS's ordinary custom-JSON recipe API; no Chocolate Flavored-specific addon is needed:

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'chocolateflavored:mud_kiln/bread' })
  event.custom({
    type: 'chocolateflavored:mud_kiln',
    ingredient: 'minecraft:wheat',
    input_count: 3,
    result: { id: 'minecraft:bread' },
    cookingtime: 160,
    experience: 0.1
  }).id('kubejs:mud_kiln_bread')

  event.custom({
    type: 'chocolateflavored:cooking_pot',
    ingredients: [
      { ingredient: 'minecraft:red_mushroom', count: 2 },
      { ingredient: 'minecraft:brown_mushroom', count: 2 },
      { ingredient: 'minecraft:bowl', count: 2 }
    ],
    results: [{ id: 'minecraft:mushroom_stew' }, { id: 'minecraft:mushroom_stew' }]
  }).id('kubejs:mushroom_stew')
})
```

Recipes are loaded through vanilla's recipe manager, so normal datapack/KubeJS recipe reloads apply. The optional compatibility configuration pins KubeJS `26.1.2-8.0.7`, Rhino `2101.2.8-build.91` and Better Advanced Tooltips `2601.1.0-build.9` for development testing only. The standard build and mod metadata require neither.

## Verification

Dedicated-server checks exercise native kiln behavior, counted shapeless matching, 200-tick pot batches, unstackable output distribution, full-output blocking without consumption, exact menu coordinates, shift-click extraction, sided automation, legacy migration/queued-return persistence, and nonduplicating inventory drops. Client verification opens the real twelve-slot server menu, checks synchronized outputs/heat and exact slot coordinates, verifies the absence of a recipe book, and captures cooking-pot.png. A separate KubeJS server executes a six-input/six-output counted recipe and verifies atomic blocking when the sixth output has no room.
