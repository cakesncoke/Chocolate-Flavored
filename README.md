# Chocolate Flavored

A standalone survival overhaul for **Minecraft 26.1.2**, built with **NeoForge 26.1.2.122** and **Java 25**. Mod ID: `chocolateflavored`. Base package: `com.akiotsukino.chocolateflavored`. Own code: MIT; see [LICENSE](LICENSE). Bundled third-party portions retain their licenses; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). The MDK attribution is preserved in [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt).

`main` is the active, fresh 26.1.2 project. The previous implementation and reusable assets remain on `archive/1.21.1` and the existing feature branches. Do not copy old Java code without checking the current APIs.

## Development

Install a JDK 25 and configure IntelliJ's project SDK and Gradle JVM to use it. Import the root Gradle project. Use the checked-in Gradle 9.2.1 wrapper rather than a system Gradle installation.

On Windows:

```powershell
.\gradlew.bat clean build
.\gradlew.bat runClient
```

On Linux/macOS:

```sh
bash ./gradlew clean build
bash ./gradlew runClient
```

The JAR is generated in `build/libs/`. Mod metadata is expanded from `src/main/templates/META-INF/neoforge.mods.toml`; do not edit generated copies under `build/`.

## Verification

CI runs a clean build with Java 25 and dedicated-server behavior checks, then launches an integrated development client under a virtual display with software rendering. It verifies the real cooking-pot menu and renders every new adult/baby animal in a CI-only gallery. Logs and a screenshot are uploaded as `client-startup-evidence`; verification classes are excluded from release JARs. These checks do not replace exploratory gameplay testing.

Equipment progression, rose gold tools and trims, knives, and early survival mining are implemented. See [equipment and mining](docs/equipment-and-mining.md) for exact behavior and development verification. Primitive lighting adds timed torches, a fire starter, unlit lanterns, and unlit normal-campfire placement; see [primitive lighting](docs/primitive-lighting.md). Anvils charge three levels only for adding or upgrading enchantments; repairs and naming are free, and iron ingots repair damaged anvils. See [anvil rules](docs/anvil-rules.md). Villages generate abandoned, with persistent residents, revised zombie-villager spawning and longer villager population timers; see [village rules](docs/village-rules.md). Standalone ovens and cooking pots add datapack/KubeJS processing recipes; see [cooking](docs/cooking.md). No crafting recipes are added for these blocks. Only NeoForge and Minecraft are required dependencies. Focused mixins handle current data-driven villager offers, campfire placement, and the fire-starter hand animation; no legacy mappings or access transformers are configured.

Custom recipes support datapacks. Oven and cooking-pot recipe compatibility is tested with an optional development-only KubeJS installation. Seasonal systems must account for Serene Seasons when present, without making it a required dependency; Serene Seasons integration remains untested.

Before changing vanilla behavior, inspect the exact project's current Minecraft sources through the IDE. Minecraft 26.1 is unobfuscated; do not introduce old mapping configuration.

## Earth animals

Standalone Earth-style animal variants, white/rare-pink natural sheep, Buttercup and Pink Daisy are described in [docs/earth-animals.md](docs/earth-animals.md), including exact spawn percentages, item-limit adaptations and upstream attribution.


### EarthMobs creatures

This standalone subset follows EarthMobs 26.1.x revision `d5acab9846bd3400ac5f093bfec7439351a1bd61`.

| Creature | Natural spawning / changes |
| --- | --- |
| Melon Golem | Build with two snow blocks and a carved melon. Shears always return one carved melon, including while aggressive. Retains seed bursts, snow trails, water/heat vulnerabilities and head removal. |
| Bouldering Zombie | Replaces every regular zombie below Y=0; 10% in mountainous biomes at Y=0 or above. Retains climbing, stronger melee and conversion to a vanilla drowned. |
| Skeleton Wolf | 1.5% of hostile spawn-table selections in forests and Soul Sand Valleys; reference ground, darkness and pack restrictions still apply. Retains bone taming, owner defense, sitting/following, bone/rotten-flesh healing and wolf armor. Cannot breed. |
| Bone Spider | Replaces 25% of spiders below Y=0, or 33% in Dark Forests/Pale Gardens at any height. The 33% rule takes precedence where both apply. Retains climbing, bone volleys, melee, rare potion payloads and freeze immunity. |
| Tropical Slime | Reference ocean/deep-ocean water and darkness restrictions, group size and weight. Retains swimming pursuit, splitting, fish release on death and tiny-slime bucket capture. Bucketed slimes remain persistent and save their fish. |
| Husk Rabbit | EarthMobs desert monster spawning: weight 15, groups of 3–4, rabbit ground and darkness checks. Hostile hopping, hunger attacks, no breeding, 10% natural-spawn chance for an adult to carry a baby Husk. The rider uses an allowed wooden sword. Rotten flesh and player-kill Husk Rabbit Foot loot are retained. |
| Swamped | Reference Lobber Zombie model, attributes and forest spawn weight/groups; uses regular zombie AI with the husk-style unarmed melee poison attack against players, and converts to vanilla drowned underwater. |

Carved Melon, Tropical Slime Bucket, Tropical Slime Block, Tropical Ball and Husk Rabbit Foot are available. Seeds and bone shards remain internal projectile entities; bone shards have their own visual model without an obtainable item. Bone Spider's rare Undead Body and Zombified payloads are internal mob effects, with no new potion items, brewing recipes or unrelated mob conversions. No crafting or brewing recipes are added. Every mob has a spawn egg in both the vanilla Spawn Eggs tab and the Chocolate Flavored tab; Muddy Pig has a state-bearing pig egg. EarthMobs and Bagus Lib are not dependencies. Imported portions retain their LGPL notices (see `THIRD_PARTY_NOTICES.md`).

Modern baby asset coverage and sources are recorded in [docs/baby-texture-audit.md](docs/baby-texture-audit.md). Bone shards use a dedicated projectile model; tropical slime particles use vanilla item-fragment physics and quarter-texture UVs and the Tropical Ball texture.

## Credits

Chocolate Flavored is noncommercial. Its own code is MIT; borrowed resources retain their original authorship and licenses. Thanks to these creators for the resources used here:

| Creator / project | Resources used in Chocolate Flavored |
| --- | --- |
| **bagu_chan (baguchi), MCVinnyq, Farcr and EarthMobs contributors** — [EarthMobs](https://github.com/baguchi/EarthMobs) | Behavior, models and textures for Moobloom, Moolip, Muddy Pig, Fancy Chicken, Horned Sheep, Jumbo Rabbit, Melon Golem, Bouldering Zombie, Skeleton Wolf, Bone Spider, Tropical Slime and Husk Rabbit; Lobber Zombie model/textures for **Swamped**; Skeleton Wolf sounds; Buttercup/Pink Daisy textures; carved melon textures; Tropical Slime Block model/texture/behavior; Tropical Slime Bucket, Tropical Ball, Husk Rabbit Foot and Bone Shard textures; Undead Body/Zombified effect icons; selected spawn-egg textures. LGPL-3.0-only. Exact egg sources and adaptation differences are listed in [third-party notices](THIRD_PARTY_NOTICES.md). |
| **Slexom and Earth2Java contributors** — [Earth2Java](https://github.com/Slexom/earth2java) | Adult Dairy Cow, Pale Pig, Bronzed Chicken, Fuzzy Sheep, Long Nosed Sheep and Patched Sheep textures, including the selected sheep wool textures. MIT. |
| **MintRefresh** — [Earth Animals](https://modrinth.com/datapack/earth-animals) | Modern Dairy Cow, Pale Pig and Bronzed Chicken baby textures; modern spawn-egg textures for Dairy Cow, Moobloom, Moolip, Pale Pig, Bronzed Chicken, Fancy Chicken, Skeleton Wolf and Muddy Pig. Resource pack v1.2.1 / MC 26.2, unchanged images; LGPL-3.0-or-later. |
| **TheFancyFedora** — [Ultimate Earth & Dungeons Variants](https://www.curseforge.com/minecraft/texture-packs/ultimate-earth-dungeons-variants) | Modern Fuzzy, Long Nose and Patched Sheep baby body textures, plus Fuzzy/Patched baby wool textures, from file 8210951. Unchanged images; CC BY-NC 3.0. Long Nose uses vanilla white baby wool to preserve the mod's natural sheep colors. The source pack credits **GillsBills / ReCrafted Mobs** as its base; unrelated pack assets were not imported. |
| **vectorwing and Farmer's Delight contributors** — [Farmer's Delight](https://github.com/vectorwing/FarmersDelight) | Stove models/textures adapted under the name **Oven**; cooking-pot block/item models and textures, menu GUI and empty-bowl slot sprite, cooking sounds, steam textures/particle behavior, Flint Knife and Iron Knife textures and reference stats, and cooking interaction/layout references. MIT. No Farmer's Delight runtime dependency. |
| **Wolfie_Waffle / WolfieWaffle** — [Hardcore Torches](https://github.com/WolfieWaffle/Hardcore-Torches-Forge) | Project-owner-supplied `textures/block/lit_torch.png`, `unlit_torch.png` and `unlit_lantern.png`; all three match the upstream files byte for byte. These resources are not relicensed as MIT; see the provenance notes below. |
| **Modern Better Than Wolves contributors; FlowerChild (original Better Than Wolves)** | The project owner supplied textures from the Modern Better Than Wolves project. This source credit is retained; the original archive/revision and exact per-file mapping were not supplied and could not be verified against the inspected ModernBTW branches. See [resource provenance](docs/resource-provenance.md) for the precise outstanding file list. Fire-starter swing/eating-sound/item-chip behavior was referenced from FlowerChild's original [Better Than Wolves](https://github.com/BetterThanWolves/BetterThanWolves) fire plough implementation. |
| **Mojang Studios / Minecraft Earth and Minecraft** | Original Minecraft Earth creature designs, and Minecraft's native models, animations, sounds and vanilla resources referenced by the standalone implementation. These are not claimed as original Chocolate Flavored artwork. |
| **NeoForged / NeoForge contributors** | NeoForge MDK project foundation; original template notice retained in [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt). |

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md), bundled `META-INF/licenses/` notices and [resource provenance](docs/resource-provenance.md) for source revisions, paths, license distinctions and unresolved ownership records. Attribution does not replace any permissions required by an upstream author.

## Additional creatures

Sunken Skeleton, Tuff Golem, Barnacle, Iceologer and Wildfire are standalone additions with spawn eggs in both creative tabs. They have no natural spawning, structures, biome/world-generation changes or designed loot drops. Empty entity loot tables are reserved for later datapacks. See [creature mechanics](docs/additional-creatures.md).

Additional resource credits: **Patrigan / Infamous Mods and Dungeons Mobs contributors** supply Sunken Skeleton, Iceologer and Wildfire textures and model geometry (MIT); **TheFancyFedora**, **GillsBills / ReCrafted Mobs** and **Mojang** are credited for the selected Barnacle pack resources (CC BY-NC 3.0). The Tuff Golem uses original native geometry and referenced Mojang textures; all five eggs use referenced modern vanilla egg textures with tint constants. Exact source files, revisions and changes are listed in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
