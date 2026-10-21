# Tuff Golem restart

Target: Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Standalone, no required external mods. PR #8 was reverted in full before this implementation.

## Appearance

The actual Friends & Foes Blockbench asset is bundled unchanged, including its nine model cubes, original UVs and eleven animation clips. The native loader reads it directly; there is no custom replacement geometry or hand-authored replacement animation. Original base skin, sixteen colored cloth skins, closed eyes, sounds and modern egg are also unchanged. The [asset manifest](tuff-golem-assets.json) gives source paths, revision and byte hashes; [notices](../THIRD_PARTY_NOTICES.md) preserve credits and licensing.

## Behavior

- 20 health, 0.225 base movement speed (0.175 carrying an item), knockback resistance and persistence.
- Starts standing with a ten-second initial pause. After 300–400 seconds awake, returns to its exact home position/yaw and rests for 300–600 seconds. If home stays unreachable for 30 seconds it rests at its current location instead of remaining permanently stuck.
- Displays one item, or collects exactly one nearby loose item while awake and empty-handed. Click a displayed item to retrieve it; item components and carried inventory survive reloads. Native equipment handling returns its carried item on death.
- Tuff repairs five health. Dye changes the cloth. Honeycomb immobilizes it; an axe removes the wax (one durability outside creative). These interactions use the Friends & Foes behavior as a guide.
- Original show/hide, sleep/wake, combined item transitions, and walk clips drive the original geometry. Displayed items float and rotate at the front like the source implementation.

## Scope and validation

The only new obtainable item is its spawn egg, included in Spawn Eggs and Chocolate Flavored. Use an egg or `/summon chocolateflavored:tuff_golem`. No natural spawns, structures, world generation, construction ritual, recipe, or designed loot are introduced. Its empty entity loot table is ready for future datapacks. No seasonal mechanics are changed.

CI checks repairs, dye, wax/scrape, item conservation, transition selection, persistence, sleep duration, creative tab membership and absence of biome spawns. The real client gallery submits every cloth texture and all original clips, sleeping eyes, displayed items and the egg. Gallery code is excluded from the release JAR. Audio listening and extended manual pathfinding/playtesting are separate from the automated checks.
