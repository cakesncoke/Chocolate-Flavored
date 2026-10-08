# Materials and primitive tools

All supplied item textures have registered items, English names, item models and creative entries. Materials are ordinary items; food effects and additional crafting uses are intentionally left for future content decisions. Red Sand Pile uses the supplied red sand artwork. Coal Ore Pile uses the existing coal dust artwork until a separate texture is supplied.

Flint retains the `minecraft:flint` ID and vanilla artwork. A required bootstrap mixin replaces the vanilla item with a nonstackable tool with four durability. Vanilla recipes, loot tables, villager trades, creative entries and old saved item IDs therefore all resolve to this tool. Existing oversized stacks from older saves are not split automatically; newly acquired flint stacks to one.

In Survival, breaking stone, granite, diorite or andesite with flint produces one Stone Pebble and leaves cobblestone in place. Breaking cobblestone produces one pebble and removes the block. Flint also converts regular copper ore and coal ore into cobblestone, dropping one Copper Ore Pile or Coal Ore Pile respectively. Each successful operation consumes one durability, with no bonus drops on the final use. Flint mines at hand speed and does not wear when mining other blocks or attacking. Creative/adventure/spectator mining keeps its normal behavior. Previously canceled break events are respected.

The Flint Knife is crafted with flint above a stick. Its Farmer's Delight tier has 131 durability, mining speed 4, enchantability 5, total attack damage 2.5 and attack speed 2. It repairs with flint and loses one durability per hit. Sweeping Edge is excluded. This feature adds material harvesting rather than Farmer's Delight's cutting board/cake/pumpkin interactions.

Knife harvesting preserves original loot and independently adds:
- One Plant Fiber with a 33% chance from short grass, tall grass, fern or large fern.
- One additional stick with a 25% chance from leaves.

Mining with anything other than a shovel replaces normal drops with exactly three piles:
- Dirt Piles from dirt, grass blocks, coarse/rooted dirt, podzol, mycelium, dirt paths, farmland, mud and muddy mangrove roots; additionally 20% chance for one flint.
- Sand Piles from sand, Red Sand Piles from red sand.
- Gravel Piles from gravel; additionally 50% chance for one flint, replacing the original gravel/flint roll.

Shovels (identified by the vanilla shovel tag or ShovelItem class), creative mode, explosions and nonplayer automation preserve ordinary loot. Moss, short grass and plants are not dirt variants. The non-shovel dirt list can be changed in `data/chocolateflavored/tags/block/hand_dug_dirt.json`. Other chances are in `event/PrimitiveMining.java`. Item IDs, recipes and tags are available to KubeJS without requiring it.

## Validation

Dedicated-server GameTests cover the original features plus ore conversion, final-use drops, all eight blocked deepslate ores, real non-shovel/shovel harvesting, and the new durability limits. Check item artwork and damage bars in the client before merging.

## Copper ore progression

A copper pickaxe converts regular iron ore to cobblestone and yields exactly one Iron Ore Pile. It converts deepslate to cobbled deepslate and yields exactly one Deepslate Pebble. Each operation costs one durability and replaces normal block loot, independent of Fortune or Silk Touch.

Copper pickaxes have zero mining progress on every vanilla deepslate ore, cannot harvest them, and their server-side break attempts are rejected. The expandable `chocolateflavored:deepslate_ores` block tag supplies this list. Other pickaxes retain normal ore harvesting. Creative still removes blocks normally.

## Equipment durability

Vanilla item default components apply these multipliers to the five tool/weapon items (sword, pickaxe, axe, shovel, hoe) and four armor pieces in each material set. Repair materials, protection, attributes, enchantments and item IDs remain unchanged. Iron fractional values round up.

| Material | Tool/weapon durability | Helmet | Chestplate | Leggings | Boots |
| --- | ---: | ---: | ---: | ---: | ---: |
| Iron ×1.5 | 375 | 248 | 360 | 338 | 293 |
| Diamond ×2 | 3122 | 726 | 1056 | 990 | 858 |
| Netherite ×3 | 6093 | 1221 | 1776 | 1665 | 1443 |

Copper remains at the original iron-tier durability of 250. Untiered equipment such as bows, shields, shears and flint and steel retains its existing durability. Item stacks without a custom max-damage override—including ordinary items in existing saves—inherit the updated default automatically; accumulated damage is retained.
