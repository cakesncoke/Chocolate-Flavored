# Standalone crops

Minecraft 26.1.2 / NeoForge 26.1.2.122. No Farmer's Delight, Extra Delight, Rustic Delight, or other runtime dependency is added.

| Source | Cultivated crop | Planting material | Raw harvest |
| --- | --- | --- | --- |
| Extra Delight | Soybeans | Soybeans | Soybeans, soybean pods |
| Extra Delight | Chili | Chili seeds | Chili |
| Rustic Delight | Cotton | Cotton seeds | Cotton bolls |
| Rustic Delight | Coffee | Coffee beans | Coffee beans |
| Farmer's Delight | Cabbage | Cabbage seeds | Cabbage |
| Farmer's Delight | Tomato | Tomato seeds | Tomato |
| Farmer's Delight | Onion | Onion | Onion |
| Farmer's Delight | Rice | Rice, in source water over dirt | Rice with a knife; rice panicle otherwise |

All eight have wild forms. Wild carrots, potatoes, and beetroots yield their existing vanilla crop materials. New raw harvests are ordinary non-consumable items: no food, nutrition, drinks, meals, crates, sacks, compost blocks, ropes, recipes, or processing machinery is added. Existing vanilla crop behavior is preserved. All new items, including wild plants, appear in Chocolate Flavored's creative tab.

Field crops use native crop support/light/hydration, farmland growth, bonemeal and NeoForge growth events. Soybeans/chili retain slower random growth; each bonemeal use advances at least one stage rather than reproducing upstream's possible zero-stage division. Cotton has four stages, coffee six, chili seven, and soybeans/cabbage/onion eight. Tomatoes combine the source's four seedling and four fruiting stages in one block; right-clicking a ripe plant yields 1–2 tomatoes and resets it to the fruiting stage. No rotten-tomato projectile or rope block is introduced.

Rice grows four submerged stages, then four panicle stages above the water. Harvesting the top retains the root, which regrows. Knives in `chocolateflavored:knives` yield rice; other tools yield the original raw panicle. Wild rice supplies plantable rice without processing. Wild soybean/chili harvests include planting material so the requested recipe-free scope does not block cultivation. Shears preserve wild plants.

## Wild generation

The first placement filter has **four times the upstream rarity denominator**, reducing patch attempts to one quarter. This changes new-chunk generation only; existing plants remain. Cluster sizes and attempts inside a patch remain at the pinned source defaults. Actual visible density also depends on biome, soil, water, and terrain suitability.

| Wild plant | Source denominator | New denominator |
| --- | ---: | ---: |
| Soybean | 80 | 320 |
| Chili | 50 | 200 |
| Cotton | 32 | 128 |
| Coffee | 15 | 60 |
| Cabbage | 30 | 120 |
| Onion | 120 | 480 |
| Tomato | 100 | 400 |
| Carrot | 120 | 480 |
| Potato | 100 | 400 |
| Beetroot | 30 | 120 |
| Rice | 20 | 80 |

Source habitats are preserved: cabbage/beetroot beaches; carrot/onion temperate Overworld biomes (temperature 0.4–0.9, excluding lush caves/mushroom fields); potatoes cold Overworld biomes (0.1–0.3, excluding underground); tomatoes hot, non-wet Overworld biomes; rice wet, non-underground Overworld biomes; cotton forests; coffee jungles (including the common forest/jungle tags); soybean temperate biomes; chili hot biomes. Common `c:` biome tags are supplied by NeoForge. The source's decorative sandy shrub is replaced with vanilla dead bush; no unrelated block is imported.

Worldgen, biome filters, loot, and `c:crops` / `c:seeds` tags are datapack driven. KubeJS can reference ordinary `chocolateflavored:` item/block IDs; no crop-specific JavaScript integration dependency is necessary. Optional Serene Seasons block/item fertility tags classify cabbage/onion as spring/autumn crops, soybean as summer/autumn, and the other warm crops/rice as summer. Tags do not require Serene Seasons to be installed. The mod's global sleep acceleration also accelerates native random crop ticks; no extra crop time multiplier is applied. Seasonal gameplay with Serene Seasons remains untested.

## Sources and verification

Pinned sources, exact copied paths, source/bundled SHA-256 hashes and asset licenses are recorded in [crop-assets.json](crop-assets.json). Models/textures are reused; Java behavior is implemented against the exact native crop/vegetation/water-plant APIs. The MIT crop patch configuration/feature and biome-filter implementation are adapted from Farmer's Delight with attribution. Soybean/chili artwork remains CC BY-NC-SA 4.0 and is excluded from the repository's MIT grant.

The CI-only crop suite exercises actual seed placement/consumption, random and bonemeal growth, support loss, crop loot, tomato picking/regrowth, water-root/panicle rice and knife harvests, wild-plant shearing, configured land patch placement, encoded rarity values, and optional seasonal tags. The client resource gallery checks all crop-stage particle sprites and renders all new item/wild-plant icons. These checks do not replace exploratory farming, biome-distribution or multiplayer testing.

## Hearth and Harvest cotton and blueberries

Cultivated cotton now uses eight native crop ages and Hearth and Harvest artwork/geometry. Mature cotton right-click harvesting gives 1–2 existing cotton bolls and resets to age 5; breaking uses the source's mature-boll/seed/Fortune loot rules. Seeds and wild cotton are unchanged. Existing saved ages 0–3 retain their numeric age in the new 0–7 growth range.

Blueberries plant a four-age bush on native berry-bush soil, provide 1 nutrition / 0.3 saturation modifier and take 0.8 seconds to eat. Mature right-click harvest gives 2–3 berries and resets to age 1. The bush slows living entities except foxes/bees and does not deal thorn damage. Breaking age 2/3 bushes uses the source's berry/Fortune loot. Native bonemeal and random growth remain available. Summer season tags are supplied; Serene Seasons runtime compatibility has not been tested. No additional recipes, pips, crates or world generation are added.
