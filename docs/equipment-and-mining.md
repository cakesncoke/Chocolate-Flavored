# Equipment and early survival

Target: Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. No required external mods and no new recipes.

## Equipment

- Wooden sword and stone axe: 32 durability each.
- Fishing rod: 192 durability (vanilla ×3).
- Other damageable vanilla equipment: ×2 durability, except golden equipment, which retains vanilla durability. This includes armor, elytra, ranged weapons, spears, shields, shears, and other damageable utility equipment.
- Rose gold tools: iron harvesting tier and attack damage, diamond mining speed (8), gold enchantability (22), 576 durability, repairable with rose gold ingots. Iron pickaxe durability after the vanilla change is 500. Rose gold pickaxes use normal vanilla harvesting/removal.
- Stone hammer: 32 durability, 11 total attack damage and 0.6 attacks/second. Iron hammer: 500 durability (matching iron/steel tools), 12 total attack damage and 0.7 attacks/second. Both mine at hand speed, use one durability per special conversion or successful hit, and replace Knapped Flint.
- Flint knife: 131 durability, 2.5 total attack damage, 2 attacks/second, mining speed 4, enchantability 5. Iron knife: 250 durability, 3.5 total attack damage, 2 attacks/second, mining speed 6, enchantability 14. These are Farmer's Delight's base knife statistics, without its external dependency or unrelated gameplay features. Knives do not receive Sweeping Edge.

Registered content covers every custom item/block texture currently present, including the rose gold block, diamond ingot, ore piles, pebbles, plant fiber/string, and rose gold upgrade smithing template. There are no rose gold armor textures in this asset set, so no armor set is invented. The upgrade template is available in creative, with no upgrade recipe until one is supplied by a datapack.

Rose gold ingots provide the `chocolateflavored:rose_gold` trim material and extend vanilla's trim-material ingredient tag. Existing vanilla trimming recipes accept them. Both worn armor and inventory trim rendering are supported.

## Removed equipment

Wooden pickaxe/axe/shovel/hoe; stone sword/pickaxe/shovel/hoe; wooden/stone/copper spears; copper player armor, horse armor, and nautilus armor are unavailable.

Vanilla IDs are retained for registry compatibility. Their recipes are removed, creative/search entries hidden, loot filtered, and villager offers filtered before merchant menus access them. Dropped stacks and equipped items are removed on spawn/tick; player inventories, ender chests, carried stacks, and opened containers are sanitized. Golden equipment remains obtainable from loot/trades; only its crafting recipes are removed. Gold food, ingots, blocks and other non-equipment recipes remain intact.

## Mining and loot

| Block | Tool | Result |
|---|---|---|
| Dirt and dirt variants | Any tool / hands | Vanilla loot; no extra flint |
| Sand / red sand | Any tool / hands | Vanilla block loot |
| Gravel | Any tool / hands | 50% flint, otherwise one gravel; Silk Touch gives gravel and Fortune III guarantees flint. Fortune I/II retain the 50% floor. |
| Logs (including stripped logs/wood through the vanilla logs tag) | Anything except an axe | No drops; mining speed ×0.2 |
| Stone, granite, diorite, andesite | Either hammer | Cobblestone remains; one stone pebble |
| Cobblestone / mossy cobblestone | Either hammer | Block removed; one stone pebble |
| Coal / copper ore | Either hammer | Cobblestone remains; one coal dust / copper ore pile |
| Iron ore | Copper pickaxe | Cobblestone remains; one iron ore pile |
| Deepslate / any deepslate ore | Copper pickaxe | Hand mining speed; no drops |
| Deepslate | Steel pickaxe (vanilla iron ID) | Cobbled deepslate remains; one deepslate pebble |
| Any deepslate ore | Steel pickaxe (vanilla iron ID) | Cobbled deepslate remains; original ore loot table and XP rules |
| Short/tall grass, fern/large fern | Knife | Additional independent 33% chance of one plant fiber |
| Leaves | Knife | Additional independent 20% chance of 1–2 sticks; original loot retained |

Shovels retain vanilla dirt/sand/gravel loot. Rose gold and other unaffected tools retain vanilla mining behavior. Creative breaking does not generate survival conversion drops. Loot changes require a player mining context; natural leaf decay, explosions, and unrelated loot retain vanilla behavior. Potion/environment mining modifiers remain in effect.

Special conversions run at the lowest break-event priority, skip canceled/protected breaks, and replace the block immediately on the server. Tool wear respects vanilla durability handling. Normal ore loot goes through NeoForge's standard drop/XP hooks.

## Datapacks and compatibility

The block groups are editable under `data/chocolateflavored/tags/block/`; knives and repair materials have item tags. The global loot modifier at `data/chocolateflavored/loot_modifiers/survival.json` exposes fiber chance and the extra leaves-stick chance. Gravel uses the pinned vanilla loot table with the flint probabilities changed to `[0.5, 0.5, 0.5, 1.0]` under `data/minecraft/loot_table/blocks/gravel.json`. These probabilities can be changed without recompiling. Item recipe filtering runs on reload and examines recipe outputs rather than filenames.

No seasons, crop growth, climate or calendar systems are modified. Serene Seasons and KubeJS remain optional future integration/testing targets; compatibility with their 26.1.2 builds has not been tested.

## Verification

`./gradlew clean build` compiles production and development verification sources. `./gradlew runVerificationServer` boots a separate development-only server and checks live component values, recipe removal, trim data, loot distributions, actual mining replacements and tool wear, normal rose gold harvesting, and merchant filtering. It stops after logging `CHOCOLATE_FLAVORED_VERIFICATION_PASSED`. The verification source set is excluded from the distributed JAR.

CI also launches the development client and captures a screenshot and logs. This checks startup; manual multiplayer interaction, visual inspection of equipped armor trims, and optional-mod integration are separate checks.

## Implementation references

Vanilla behavior was inspected in the official unobfuscated 26.1.2 client: `Items`, `Item.Properties`, `ToolMaterial`, `Tool`, `Player`, `ServerPlayerGameMode`, `RecipeManager`, `AbstractVillager`, trim metadata, and loot/item-model/atlas resources. NeoForge APIs were checked against the published 26.1.2.122 sources and their matching block-break/drop patches.

Knife statistics reference [Farmer's Delight ModMaterials](https://github.com/vectorwing/FarmersDelight/blob/1.21/src/main/java/vectorwing/farmersdelight/common/registry/ModMaterials.java) and [ModItems](https://github.com/vectorwing/FarmersDelight/blob/1.21/src/main/java/vectorwing/farmersdelight/common/registry/ModItems.java). Its Java implementation was not ported.

Dirt, sand, red sand and gravel pile items and their resources have been removed. Existing stacks of those removed IDs are no longer registered. Copper/iron ore piles remain.
