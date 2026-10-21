# Additional standalone creatures

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. No additional runtime mods are required. These are standalone implementations of the requested creatures, not complete ports of their source mods.

| Entity ID (`chocolateflavored:`) | Mechanics |
| --- | --- |
| `sunken_skeleton` | Aquatic undead skeleton with a vanilla crossbow; native crossbow charging/projectiles, bow/melee fallback when re-equipped, swimming navigation in water and ground navigation on land. Breathes underwater, does not burn in daylight or convert to a stray. The coral follows its head and leg animations. |
| `tuff_golem` | Persistent passive display golem. Starts as a statue, wakes after 60–120 seconds, wanders for 20 seconds, collects one loose item and returns home to rest. If home remains unreachable for 30 seconds it rests at its current location. Right-click with an item to display one, click again to retrieve it, use colored carpet to change the cloak, or empty-hand click an empty golem to toggle statue mode. The displayed item uses native equipment persistence and is returned on death as carried inventory, not generated creature loot. Home, phase, cloak and item survive reloads. Original native model uses vanilla tuff, wool and black-concrete materials. |
| `barnacle` | Aquatic hostile ambusher. Approaches swimmers or occupied boats; within six blocks it tethers its target, pulls it downward, drains extra air underwater and bites every two seconds. Leaving water, breaking line of sight, or moving out of range ends the tether. Native bubble particles show the tether. It cannot tether while stranded on land. The tentacles animate during swimming/attacking. |
| `iceologer` | Hostile illager with a two-second casting windup and six-second attack cooldown. Summons an owned ice chunk above a visible target. The chunk briefly tracks the target, then commits to its fall: impact deals 8 damage, five seconds of Slowness II and freezing, with native ice particles/glass sound. No blocks are placed or destroyed. The spell excludes its owner/allies, persists its lifetime/owner/target through reloads, shatters once and expires after eight seconds. No raid spawning/automatic raid participation. |
| `wildfire` | Fire-immune Blaze-derived miniboss with 50 health, four rotating shields (15 protection each), one shield's regeneration every 7.5 seconds (shield hits reset the timer), three-shot vanilla small-fireball volleys after a 1.5-second windup, and close-range damage/knockback. Shield health and cooldowns persist. Vanilla Blaze hovering, smoke, fireball collision and mob-griefing rules are reused. No automatic Blaze/minion summoning is added. |

Each creature has a modern vanilla-based tinted spawn egg in **Spawn Eggs** and **Chocolate Flavored**. `ice_chunk` is an internal entity without an egg or item; its renderer uses vanilla packed ice. No new equipment, materials, blocks or crafting/brewing recipes are introduced.

## Spawn and loot scope

No natural-spawn placement registration, spawn-replacement hooks, biome spawn additions, raid composition changes, structures or world-generation resources are added. Use eggs or `/summon` until spawn rules are supplied. Entity loot tables have empty pools so later datapacks can define drops at `data/chocolateflavored/loot_table/entities/<entity>.json`. No source-mod drops are imported. Existing or player-supplied equipment/inventory follows vanilla equipment handling; the Sunken Skeleton's generated crossbow has zero default drop chance.

## Resources and validation

Dungeons Mobs supplies selected Sunken Skeleton, Iceologer and Wildfire geometry/textures under MIT. Ultimate Earth & Dungeons Variants supplies Barnacle geometry/texture under CC BY-NC 3.0. Full authorship, exact paths/revision and adaptation details are in [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md). No Friends & Foes or nicky-bee/Tuff-Golem resources are used. Vanilla materials, projectiles, sounds, particles, item rendering and modern egg textures are referenced directly.

Dedicated-server checks exercise creature behavior and persistence; the CI-only client gallery submits every new model, item display, egg and ice spell. Manual exploratory combat/audio testing remains separate from CI. Serene Seasons integration is not added or claimed: these creatures do not select spawns or alter seasonal systems.
