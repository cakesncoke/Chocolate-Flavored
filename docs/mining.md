# Mining and early survival

No added recipes or dependencies. Tool statistics are in [equipment](equipment.md).

## Conversions by tool

Each conversion runs only in survival or adventure mode, on the server, at the lowest break-event priority. It skips canceled or protected breaks. The block is replaced immediately, then one durability is charged through native durability and enchantment handling. Creative and spectator breaks keep vanilla behavior.

| Tool | Mined block | Remaining block | Drop |
| --- | --- | --- | --- |
| Stone Hammer | Stone, granite, diorite, andesite | Cobblestone | 1 stone pebble |
| Stone Hammer | Cobblestone, mossy cobblestone | Air | 1 stone pebble |
| Stone Hammer | Coal / copper ore | Cobblestone | 1 coal dust / copper ore pile |
| Deepslate Hammer | Stone, granite, diorite, andesite | Air | 4 stone pebbles |
| Deepslate Hammer | Cobblestone, mossy cobblestone | Air | 1 stone pebble |
| Deepslate Hammer | Deepslate | Cobbled deepslate | 1 deepslate pebble |
| Deepslate Hammer | Iron / gold ore | Cobblestone | 1 iron / gold ore pile |
| Deepslate Hammer | Deepslate copper / iron / gold ore | Cobbled deepslate | 1 matching ore pile |
| Deepslate Hammer | Coal / copper ore, deepslate coal ore, other pickaxe blocks | Native breaking | Native stone-tier loot and XP |
| Copper pickaxe | Iron ore | Cobblestone | 1 iron ore pile |
| Copper pickaxe | Deepslate and any deepslate ore | — | Hand mining speed, no drops |
| Steel (vanilla iron) pickaxe | Deepslate | Cobbled deepslate | 1 deepslate pebble |
| Steel (vanilla iron) pickaxe | Any deepslate ore | Cobbled deepslate | Original ore loot table and XP |
| Vanilla flint (main hand) | Stone, granite, diorite, andesite | Cobblestone | 1 stone pebble; consumes 1 flint |

- **Stone Hammer** mines at hand speed and spends durability only on conversions and hits.
- **Deepslate Hammer** uses stone-pickaxe speed (4) on pickaxe blocks and hand speed elsewhere. It spends one durability per normally mined block, conversion or hit. Converted blocks are explicitly valid for it, so the wrong-tier harvest-time penalty does not apply. Silk Touch and Fortune affect only native loot, not the fixed conversion rewards.
- **Flint** costs one item, not durability, and plays the item-break sound. Off-hand flint has no effect, and cobblestone and ores have no flint conversion.
- Rose gold and other unlisted tools keep vanilla mining behavior.

## Other mining and loot rules

| Block | Tool | Result |
| --- | --- | --- |
| Dirt and dirt variants | Any tool / hands | Vanilla loot; no extra flint |
| Sand / red sand | Any tool / hands | Vanilla block loot |
| Gravel | Any tool / hands | 50% flint, otherwise one gravel; Silk Touch gives gravel and Fortune III guarantees flint. Fortune I/II keep the 50% floor. |
| Logs (including stripped logs/wood in the vanilla logs tag) | Anything except an axe | No drops; mining speed ×0.2 |
| Short/tall grass, fern/large fern | Knife | Additional independent 33% chance of one plant fiber |
| Leaves | Knife | Additional independent 20% chance of 1–2 sticks; original loot kept |

Loot changes require a player mining context. Natural leaf decay, explosions and unrelated loot keep vanilla behavior. Potion and environment mining modifiers still apply.

## Datapacks

- Block groups live under `data/chocolateflavored/tags/block/`. The `flint_to_cobblestone` and `flint_pebbles` tag IDs are stable. If you add stone blocks to the stone tag, also add them to `deepslate_hammer_conversions` so the Deepslate Hammer gets the intended harvest speed.
- `chocolateflavored:hammers` (item tag) selects tools for the hammer mechanics. Knives and repair materials have item tags.
- The global loot modifier `data/chocolateflavored/loot_modifiers/survival.json` exposes the fiber chance and the extra leaves-stick chance.
- Gravel uses the vanilla loot table with flint probabilities `[0.5, 0.5, 0.5, 1.0]` in `data/minecraft/loot_table/blocks/gravel.json`.
- Recipe filtering for removed equipment runs on reload and examines recipe outputs, not filenames.

No seasons, crop growth, climate or calendar systems are changed here.

## Verification

The development-only dedicated server performs real block destruction for every conversion above. It checks exact drop counts, single wear and final-use breakage, full stone removal, ore paths with Silk Touch, speed and tier restrictions, loot distributions, final flint consumption, and protected/creative behavior. The client gallery checks hammer and ore-pile models, translations and creative entries. These checks are excluded from release JARs.
