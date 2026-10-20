# Earth animals (Minecraft 26.1.2)

The mod adds eleven entity types and EarthMobs-style muddy state to vanilla and Pale Pigs. It adds exactly two items, Buttercup and Pink Daisy (placeable small flowers). No spawn eggs, extra equipment, mod dependencies, or recipes are added.

## Natural spawn replacements

Replacements apply only to natural and chunk-generation spawns, after vanilla determines climate variants. Commands, eggs, breeding, saved animals, and dimension travel do not reroll. Existing saved animals are unchanged. All original spawn restrictions and population weights remain in force.

| Animal | Rule |
| --- | --- |
| Dairy Cow | 5% of temperate cows |
| Moobloom / Moolip | 0.5% of Flower Forest cows; 33% of that group are Moolips (0.165% overall), 67% Mooblooms (0.335% overall) |
| Pale Pig | 10% of temperate pigs |
| Muddy Pig | Pigs naturally spawning in swamps start muddy; other pigs acquire mud by rolling on vanilla mud |
| Bronzed Chicken | 5% of chickens in biomes tagged `minecraft:is_jungle` |
| Fancy Chicken | 0.25% of all chickens |
| Fuzzy Sheep | Cold-climate sheep, except the replacements below |
| Long Nose Sheep | Warm-climate sheep, except Horned Sheep |
| Patched Sheep | 5% of the Fuzzy Sheep group |
| Horned Sheep | 3% of otherwise eligible sheep in biomes tagged `c:is_mountain`, taking priority over climate replacements |
| Jumbo Rabbit | 0.25% of Flower Forest rabbits |

Overlapping Dairy/flower-cow and Fancy/Bronzed rolls are disjoint, preserving each listed unconditional percentage. Sheep climate follows vanilla's `spawns_cold_variant_farm_animals` and `spawns_warm_variant_farm_animals` biome tags. Vanilla 26.1.2 has climate-specific sheep color rules rather than a sheep-variant registry.

Natural wool colors are white except vanilla's rare pink roll (0.164%). Wool dyeing, shearing, regrowth, and vanilla wool loot are retained. Simple sheep variants inherit parental dye genetics. Long Nose Sheep use tintable white wool instead of Earth2Java's fixed brown wool. Entity species checks recognize the custom animals so vanilla predator AI still works.

## Behaviors and the item limit

- Flower cows retain EarthMobs' adult bone-meal interaction, flower-specific suspicious stew, milk, four-flower shearing into a normal cow, same-species breeding, and nectar-carrying bees' growth/flower spreading. Buttercup stew gives 30 seconds of absorption; Pink Daisy gives 10 seconds of regeneration.
- Muddy pigs retain randomized flower colors, 1–3 matching vanilla dye drops from shearing, mud regrowth, water washing, shaking and persistent synchronized state. Vanilla mud supplies the coating surface; EarthMobs' separate mud fluid and bucket are not added.
- Fancy Chickens retain the 6000–12000 tick egg-laying interval, using vanilla eggs and feathers instead of the unrequested Fancy Egg and Fancy Feather.
- Horned Sheep retain retaliation, charging, horn-breaking on suitable blocks, hornless fleeing, breeding, and persistent horns. They drop functional vanilla goat horns instead of the unrequested Horn Flute. A native client camera-shake effect replaces the reference library's effect.
- Jumbo Rabbits retain their larger dimensions, 10 health, 5 attack damage (9 for killer variant), breeding, ground navigation, variable jump power, 0–4 hide drops, and the 30% player-kill rabbit-foot chance. Native step-height/pathfinding replaces Bagus Lib's navigation helper.

These are explicit adaptations required by the standalone/item constraints; EarthMobs, Earth2Java and Bagus Lib are not required or bundled as mods. Serene Seasons is not directly integrated: climate selection uses biome tags, without changing seasonal systems. No Serene Seasons compatibility claim is made.

## Provenance and verification

Reference: EarthMobs `26.1.x`, commit `d5acab9846bd3400ac5f093bfec7439351a1bd61`. The six simple-variant textures come from MIT-licensed Earth2Java because they are absent from EarthMobs' current branch. Adapted EarthMobs Java and assets retain LGPL-3.0 licensing; Chocolate Flavored's own code remains MIT. See `THIRD_PARTY_NOTICES.md` and the licenses shipped in the JAR.

Dedicated verification covers probability boundaries, biome tags, natural and protochunk spawn replacement, save/load stability, white/pink colors, breeding types, predator recognition, flower-cow interactions, muddy-pig coating/shearing/persistence, horns, and rabbit attributes. A CI-only client gallery renders adult and baby versions of every addition, after the existing cooking screen check. Neither verification source set is shipped in release JARs.
