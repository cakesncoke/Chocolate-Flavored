# Sunken

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Standalone.

Behavior follows [Variants & Ventures' Murk](https://github.com/Faboslav/variants-and-ventures/tree/0ac431558806d10ae60e58ac60d52f431d6eca7b), with the existing Sunken identity, Recrafted Mobs appearance and drowning conversion retained.

- Purple, red and yellow coral variants have equal chances; saved `CoralColor` values remain 0, 1 and 2. Spawn egg remains in both creative tabs.
- 16 health, with other skeleton base attributes. Daylight burning, underwater breathing, Smite and undead interactions use vanilla tags. Immune to freezing; never converts into a Stray.
- New spawns carry bows and shoot ordinary arrows using skeleton attack intervals (20 ticks on Hard, 40 otherwise). Unarmed mobs use skeleton melee combat. Converted or existing mobs retain equipment; crossbows and tridents are no longer ranged weapons for Sunken.
- Shearing once rolls two matching coral items, each independently either living or dead: bubble (purple), fire (red), horn (yellow). The three `chocolateflavored:shearing/sunken_<color>` loot tables are datapack-overridable. Shearing consumes one tool durability, hides the coral planes, and persists without regrowth.
- Water seeking by day, surfacing and beach seeking by night, and underwater pursuit use Minecraft 26.1.2 amphibious navigation and Drowned-style steering. Water currents/friction are suppressed while swimming. The current vanilla amphibious navigator replaces the older dual land/water navigator implementation in Murk.
- Players on land are acquired at night; swimming players can be acquired during daylight. Retaliation, iron golems, axolotls and baby turtles on land remain independent targets. Sun avoidance and wolf avoidance remain.
- Skeleton bow/melee/walking/swimming limb animations match Murk's use of the vanilla skeleton model. No extra Drowned body pitch is applied. Recrafted geometry, UVs and three textures remain unchanged.
- Murk's original ambient, hurt and death sounds switch between land and water; underwater ambient volume is reduced. Original attack, shearing and step sounds are bundled unchanged. Like the reference, land footsteps use vanilla skeleton sounds and water footsteps use Murk's step sound.
- Skeleton, Stray, Bogged, Wither Skeleton and Parched convert after 600 submerged-eye ticks plus a 300-tick conversion countdown. Leaving water before the countdown resets exposure; committed conversion continues on land. Timers survive saving; no-AI/dead mobs are skipped. Native conversion preserves equipment, names, persistence, passengers and teams; NeoForge hooks support cancellation. Sunken never reconverts.
- No natural spawn entries, structures, recipes or designed death loot are added. Normal equipment-drop rules still apply.

Resources and credits: [Recrafted manifest](sunken-assets.json), [Murk sound manifest](sunken-murk-assets.json), [third-party notices](../THIRD_PARTY_NOTICES.md). No OptiFine/EMF/ETF or other mod dependency.

Verification exercises attributes, daylight burning, freeze immunity, bow-equipped spawns, coral drops and persistence, five vanilla skeleton conversions, water steering, day/night player targeting, and actual bow AI firing. Client verification checks skeleton bow/melee poses, coral visibility and rendering of all three variants. Extended multiplayer and ocean navigation remain manual playtests.
