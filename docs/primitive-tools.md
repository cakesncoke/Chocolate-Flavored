# Materials and primitive tools

All 26 newly supplied item textures have registered items, English names, item models and creative entries. Materials are ordinary items; food effects and additional crafting uses are intentionally left for future content decisions. Red Sand Pile additionally uses a tinted Sand Pile texture.

Flint retains the `minecraft:flint` ID and vanilla artwork. A required bootstrap mixin replaces the vanilla item with a nonstackable tool with four durability. Vanilla recipes, loot tables, villager trades, creative entries and old saved item IDs therefore all resolve to this tool. Existing oversized stacks from older saves are not split automatically; newly acquired flint stacks to one. Do not disable the mixin or install a second mod that replaces the vanilla flint registration.

In Survival, breaking stone, granite, diorite or andesite with flint produces one Stone Pebble and leaves cobblestone in place. Breaking cobblestone produces one pebble and removes the block. Each successful operation consumes one durability, with no bonus drops on the final use. Flint mines at hand speed and does not wear when mining other blocks or attacking. Creative/adventure/spectator mining keeps its normal behavior. Previously canceled break events are respected.

The Flint Knife is crafted with flint above a stick. Its Farmer's Delight tier has 131 durability, mining speed 4, enchantability 5, total attack damage 2.5 and attack speed 2. It repairs with flint and loses one durability per hit. Sweeping Edge is excluded. This feature adds material harvesting rather than Farmer's Delight's cutting board/cake/pumpkin interactions.

Knife harvesting preserves original loot and independently adds:
- One Plant Fiber with a 33% chance from short grass, tall grass, fern or large fern.
- One additional stick with a 25% chance from leaves.

An empty main hand replaces normal drops with exactly three piles:
- Dirt Piles from dirt, grass blocks, coarse/rooted dirt, podzol, mycelium, dirt paths, farmland, mud and muddy mangrove roots; additionally 20% chance for one flint.
- Sand Piles from sand, Red Sand Piles from red sand.
- Gravel Piles from gravel; additionally 50% chance for one flint, replacing the original gravel/flint roll.

Held items, creative mode, explosions and automation do not use the hand-drop rules. Moss, short grass and plants are not dirt variants. The dirt list can be changed in `data/chocolateflavored/tags/block/hand_dug_dirt.json`. Other chances are in `event/PrimitiveMining.java`. Item IDs, recipes and tags are available to KubeJS without requiring it.

## Validation

Dedicated-server GameTests cover bootstrap identity, four-use stone conversions, cobblestone removal, normal tool/held-flint behavior, hand-loot replacement and deterministic probability sampling. Check textures, creative search, damage bars and upper/lower halves of double plants in the client before merging.
