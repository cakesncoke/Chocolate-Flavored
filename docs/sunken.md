# Sunken

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Standalone.

- Purple, red and yellow coral variants have equal chances when created; saved `CoralColor` values are 0, 1 and 2. Spawn egg is in both creative tabs.
- Same base attributes as a vanilla skeleton. Daylight burning, underwater breathing, Smite and undead interactions use vanilla entity tags.
- New spawns carry a crossbow (90%) or trident (10%). Native crossbow charging/shooting and thrown tridents are used. Converted mobs keep their existing equipment, just like vanilla zombie drowning; retained bows fire ordinary arrows.
- Shearing once yields one living bubble (purple), fire (red) or horn (yellow) coral fan and hides the source model's coral planes. Color and sheared state persist. There is no coral regrowth.
- Skeleton, Stray, Bogged, Wither Skeleton and Parched convert after 600 submerged-eye ticks plus a 300-tick conversion countdown. Leaving water before the countdown resets exposure; once conversion starts it completes on land too, matching vanilla zombies. Timers survive saving. No-AI/dead entities are skipped. Other mods' AbstractSkeleton subclasses participate; that interoperability has not been playtested. Sunken are excluded from reconversion.
- Native conversion preserves equipment, names, persistence, passengers and team behavior. NeoForge conversion hooks allow cancellation.
- Amphibious navigation and Drowned-style water steering support chasing submerged targets, seeking water during daylight, and surfacing/approaching beaches at night. Land combat retains skeleton targets.
- No biome spawn entries, structures, recipes or designed death loot are added. Normal equipment-drop rules still apply.

Resources and credit: [asset manifest](sunken-assets.json), [third-party notices](../THIRD_PARTY_NOTICES.md). Original CEM model and all three textures are preserved unchanged; native humanoid animation supplies weapon and swimming poses. No OptiFine/EMF/ETF dependency.

Verification covers skeleton attributes, actual daylight ignition, spawn weapons/colors, one-time shearing and save/load, conversion of all five vanilla skeleton types and equipment preservation, underwater steering, and actual arrow/trident creation. The development client checks coral visibility and weapon poses and renders all three colors intact, sheared, and swimming. Extended multiplayer and ocean/pathfinding playtests remain manual.
