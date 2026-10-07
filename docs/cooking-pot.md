# Cooking Pot

Registry ID: `chocolateflavored:cooking_pot`. This workstation is adapted from
Farmer's Delight for Minecraft 1.21.1 without requiring that mod. See
[third-party notices](../THIRD_PARTY_NOTICES.md) for its source and license.

## Using the pot

- Craft it with bricks / wooden shovel / bricks in the top row, iron / water
  bucket / iron in the middle, and three iron ingots below. It also appears in
  the Functional Blocks creative tab.
- Place it over a lit Oven, lit campfire, fire, lava, magma block, or lava
  cauldron. A hopper between the heat source and pot conducts heat. An unlit
  Oven or campfire does not supply heat. The pot needs no fuel of its own.
- Right-click to open its menu. Add ingredients to the six input slots.
  Finished meals accumulate in the display slot; that slot is not an output
  you can take directly.
- Add the appropriate serving container (normally bowls) to the container
  slot, then take servings from the output slot. Right-click the placed pot
  with a suitable container to take a serving directly.
- Breaking the pot preserves its stored, unserved meals in the dropped pot
  item. Other inventory items drop separately. The filled pot can be placed
  again or combined with a container in a crafting grid to take one serving.
- Sneak-click empty-handed to switch the handle/support appearance. Supports,
  waterlogging, comparator output, ingredient remainders, XP, and serving
  behavior follow the upstream workstation.

Three upstream recipes using vanilla foods are included: mushroom stew,
beetroot soup, and rabbit stew. Farmer's Delight's other foods are not included,
so their recipes cannot be copied unchanged. Recipe book category icons use
vanilla mushroom stew, honey bottle, bread, and bowl. The pot's textures, GUI,
models, steam and boiling sounds are copied from upstream.

The recipe book is enabled by default. Its option is in the mod's generated
client config: `enableCookingPotRecipeBook`.

## Automation and heat tags

Top item handlers insert/extract ingredients. Side/bottom handlers insert
serving containers and extract served output. They do not expose the stored
meal display for extraction. Configure heat behavior using these block tags:

- `chocolateflavored:heat_sources`
- `chocolateflavored:heat_conductors`
- `chocolateflavored:tray_heat_sources`

The optional Create chute in the conductor tag uses `required: false` and
does not add a Create dependency. If a heat-source block has a `lit` property,
that property must be true. One conductor block is supported.

## Recipes and KubeJS

Pot recipes use `chocolateflavored:cooking`; Oven recipes use
`chocolateflavored:oven_cooking`. Both are normal data-pack recipe types, so
KubeJS can remove them and add replacements using its standard recipe event.
No mandatory KubeJS dependency or named KubeJS recipe builder is provided.

Put this in your Minecraft instance's
`kubejs/server_scripts/chocolate_flavored_recipes.js`:

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'chocolateflavored:cooking/mushroom_stew' })
  event.custom({
    type: 'chocolateflavored:cooking',
    ingredients: [
      { item: 'minecraft:brown_mushroom' },
      { item: 'minecraft:red_mushroom' }
    ],
    result: { id: 'minecraft:mushroom_stew', count: 1 },
    container: { id: 'minecraft:bowl', count: 1 },
    experience: 1.0,
    cookingtime: 200,
    recipe_book_tab: 'meals'
  }).id('chocolateflavored:cooking/mushroom_stew')

  event.remove({ id: 'chocolateflavored:oven_cooking/bread_from_wheat' })
  event.custom({
    type: 'chocolateflavored:oven_cooking',
    ingredient: { item: 'minecraft:wheat' },
    result: { id: 'minecraft:bread', count: 1 },
    cookingtime: 400
  }).id('chocolateflavored:oven_cooking/bread_from_wheat')
})
```

Save and use `/reload`. Pot recipes accept one to six ingredient entries,
consuming one item per occupied input slot. Repeat an ingredient entry to
require more than one of it, in separate slots. Ingredient entries can also
use item tags. `cookingtime` is in ticks and defaults to 200; `experience`
defaults to zero. Tabs are `meals`, `drinks`, or `misc` (default). Use positive
cooking times and nonnegative experience. The optional `container` field can
be inferred from an output's crafting remainder; explicitly supplying it is
useful for custom meals.

The crafting-grid serving serializer is `chocolateflavored:food_serving`.
It does not represent a Cooking Pot cooking recipe. Removing Oven recipes
leaves campfire fallback; removing campfire recipes also affects campfires.
Avoid relying on a winner when multiple recipes match the same ingredients.

## Validation

`gradlew runGameTestServer` runs the six Oven tests and four pot tests. Pot
checks cover unlit/lit Oven heating, recipe cooking, heat conduction, sided
capabilities, bowl serving, saved meals, component-preserving block loot, and
crafting-grid serving. The workflow compiles client code and starts a dedicated
GameTest server without Farmer's Delight installed.

In-game checks still needed: recipe-book clicks and recipe placement, menu
shift-clicks, waterlogging and support appearances, GUI/tooltips, steam/sounds,
multiplayer synchronization, and actual KubeJS script reloads. Automated server
tests do not verify rendered client behavior or third-party runtime integration.
