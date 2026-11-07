# Chocolate Flavored

A standalone survival overhaul for **Minecraft 26.3**, built with **NeoForge 26.3.0.99** and **Java 25**. Mod ID: `chocolateflavored`. Base package: `com.akiotsukino.chocolateflavored`.

Chocolate Flavored's own code is MIT-licensed; see [LICENSE](LICENSE). Bundled third-party resources keep their own licenses (several are noncommercial or no-derivatives); see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). The NeoForge MDK attribution is kept in [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt).

Only NeoForge and Minecraft are required. KubeJS and Serene Seasons are optional integration targets. Their compatibility is described per feature and is not claimed unless tested.

## Development

Install JDK 25 and set IntelliJ's project SDK and Gradle JVM to it. Import the root Gradle project and use the checked-in Gradle 9.2.1 wrapper.

```sh
./gradlew clean build
./gradlew runClient
```

On Windows, use `.\gradlew.bat` instead. The JAR is written to `build/libs/`. Mod metadata is expanded from `src/main/templates/META-INF/neoforge.mods.toml`; do not edit the generated copies under `build/`.

`main` is the active project. The pre-rewrite implementation and its assets remain on `archive/1.21.1` and older feature branches, as references only: check every API against the pinned versions before reusing old code. Minecraft is unobfuscated, so no mappings or access transformers are configured. Inspect the pinned vanilla sources in the IDE before changing vanilla behavior. Port notes for the latest version bump are in [docs/port-26.3.md](docs/port-26.3.md).

## Verification

Development-only checks live in the `verification` source set and are never packaged in the release JAR.

| Command | What it runs |
| --- | --- |
| `./gradlew runVerificationServer` | Dedicated-server gameplay checks in `run-verification/`. Success logs `CHOCOLATE_FLAVORED_VERIFICATION_PASSED`. |
| `bash scripts/client-smoke-test.sh` (inside `xvfb-run`) | Starts the development client with `-PverifyClient=true`, opens the real cooking-pot menu and renders every entity, block and item gallery. It needs a world from the server run above. |
| `./gradlew runKubejsVerificationServer -PverifyKubeJS=true` | Optional KubeJS recipe check. The pinned KubeJS builds predate 26.3, so this is currently unverified (see [cooking](docs/cooking.md#kubejs)). |

CI ([.github/workflows/build.yml](.github/workflows/build.yml)) runs all three and uploads logs and screenshots. These checks do not replace exploratory or multiplayer gameplay testing.

## Features

| Area | Summary | Details |
| --- | --- | --- |
| Equipment | Durability rebalance, removed early tools, rose gold tools (copper smithing upgrade), knives, hammers, Steel appearance for iron gear, new materials | [equipment](docs/equipment.md) |
| Mining | Hammer, flint and pickaxe conversions into pebbles and ore piles; axe-only logs; gravel flint and knife bonus loot | [mining](docs/mining.md) |
| Copper oxidation | Copper tools look exposed/weathered/oxidized at 25/50/75% wear; appearance only | [below](#copper-tool-oxidation) |
| Armor trims | All 18 trim patterns grant per-piece and full-set abilities; gold trim is Piglin-safe | [armor trims](docs/armor-trims.md), [design](docs/armor-trims-design.md) |
| Anvils | Flat 3-level enchanting cost, free repairs and naming, iron-ingot anvil repair | [anvil rules](docs/anvil-rules.md) |
| Primitive lighting | Timed torches, fire starter, unlit lanterns, campfires placed unlit | [primitive lighting](docs/primitive-lighting.md) |
| Cooking | Mud Kiln and cooking pot with datapack/KubeJS recipe types | [cooking](docs/cooking.md) |
| Crops | Soybean, chili, cotton, coffee, cabbage, tomato, onion, rice and blueberries, with wild patches | [crops](docs/crops.md) |
| Cakes | Pomegranate, Coffee, Cherry and Chocolate cakes with candle support | [cakes](docs/cakes.md) |
| Pastel wood | Sixteen dyed wood families and dyeing cauldrons | [dyed wood](docs/dyed-wood.md) |
| Earth animals | Earth-style animal variants, natural sheep colors, Buttercup and Pink Daisy | [earth animals](docs/earth-animals.md), [baby textures](docs/baby-texture-audit.md) |
| Monsters | Melon Golem, Bouldering Zombie, Skeleton Wolf, Bone Spider, Tropical Slime, Husk Rabbit, Swamped | [below](#earthmobs-creatures) |
| Sunken | Coral-covered underwater skeleton | [sunken](docs/sunken.md) |
| Tuff Golem | Item-displaying golem built from tuff, wool and a carved pumpkin | [tuff golem](docs/tuff-golem.md) |
| Villages | Abandoned villages, persistent residents, revised zombie-villager spawns | [village rules](docs/village-rules.md) |
| Wandering Traders | Guaranteed visits after five days; kept traders persist | [wandering traders](docs/wandering-traders.md) |
| Time and sleep | 20-minute days, 15-minute nights, accelerated sleep | [time and sleep](docs/time-and-sleep.md) |
| Horns | Copper Horns that link and recall mounts | [horns](docs/horns.md) |
| Echo Shards | Lodestone-linked, consumable teleport shards | [echo shards](docs/echo-shards.md) |
| Survival tweaks | Bundle limits for unstackable items, Nether ice by crying obsidian, Frost Walker horse armor, rose gold/steel beacons, trident repair | [survival tweaks](docs/survival-tweaks.md) |

### Copper tool oxidation

Copper swords, pickaxes, axes, shovels and hoes look exposed at 25% damage, weathered at 50% and oxidized at 75%. Repairs and Mending restore the appearance. They remain the same vanilla items with unchanged stats, recipes and enchantments, and the native normalized-damage model property respects the mod's copper durability. The supplied textures are used in the inventory, in hand, on the ground and in item frames.

### EarthMobs creatures

This standalone subset follows EarthMobs 26.1.x revision `d5acab9846bd3400ac5f093bfec7439351a1bd61`.

| Creature | Natural spawning / changes |
| --- | --- |
| Melon Golem | Build with two snow blocks and a carved melon. Shears always return one carved melon, including while aggressive. Keeps seed bursts, snow trails, water/heat vulnerabilities and head removal. |
| Bouldering Zombie | Replaces every regular zombie below Y=0; 10% in mountainous biomes at Y=0 or above. Keeps climbing, stronger melee and conversion to a vanilla drowned. |
| Skeleton Wolf | 1.5% of hostile spawn-table selections in forests and Soul Sand Valleys; reference ground, darkness and pack restrictions still apply. Keeps bone taming, owner defense, sitting/following, bone/rotten-flesh healing and wolf armor. Cannot breed. |
| Bone Spider | Replaces 25% of spiders below Y=0, or 33% in Dark Forests/Pale Gardens at any height. The 33% rule wins where both apply. Keeps climbing, bone volleys, melee, rare potion payloads and freeze immunity. |
| Tropical Slime | Reference ocean/deep-ocean water and darkness restrictions, group size and weight. Keeps swimming pursuit, splitting, fish release on death and tiny-slime bucket capture. Bucketed slimes stay persistent and keep their fish. |
| Husk Rabbit | Desert monster spawning: weight 15, groups of 3–4, rabbit ground and darkness checks. Hostile hopping, hunger attacks, no breeding, and a 10% natural-spawn chance for an adult to carry a baby Husk with an allowed wooden sword. Drops rotten flesh, plus Husk Rabbit Foot on player kills. |
| Swamped | Reference Lobber Zombie model, attributes and forest spawn weight/groups. Uses regular zombie AI with a husk-style unarmed poison melee attack against players, and converts to a vanilla drowned underwater. |

Carved Melon, Tropical Slime Bucket, Tropical Slime Block, Tropical Ball and Husk Rabbit Foot are obtainable. Melon seeds and bone shards are internal projectile entities; bone shards have their own model but no item. Bone Spider's rare Undead Body and Zombified payloads are internal mob effects, with no potion items or brewing recipes. Fifteen spawn eggs appear in both the vanilla Spawn Eggs tab and the Chocolate Flavored tab. Muddy Pig has a state-bearing pig egg. Dairy Cow, Pale Pig, Bronzed Chicken, Fuzzy Sheep, Long Nose Sheep and Patched Sheep have no spawn eggs.

## Credits

Chocolate Flavored is noncommercial. Thanks to these creators and projects. Exact revisions, file paths, hashes and licenses are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md), the bundled `META-INF/licenses/`, [docs/asset-manifests/](docs/asset-manifests/) and [resource provenance](docs/resource-provenance.md). Attribution does not replace any permission an upstream author requires.

| Creator / project | Used for | License |
| --- | --- | --- |
| bagu_chan (baguchi), MCVinnyq, Farcr and contributors — [EarthMobs](https://github.com/baguchi/EarthMobs) | Earth animal and monster behavior, models, textures and sounds; Buttercup/Pink Daisy; carved melon; tropical slime items; effect icons; selected spawn eggs | LGPL-3.0-only |
| Slexom and contributors — [Earth2Java](https://github.com/Slexom/earth2java) | Adult Dairy Cow, Pale Pig, Bronzed Chicken and Fuzzy/Long Nosed/Patched Sheep textures | MIT |
| MintRefresh — [Earth Animals](https://modrinth.com/datapack/earth-animals) | Modern baby textures and spawn eggs for several Earth animals | LGPL-3.0-or-later |
| TheFancyFedora — [Ultimate Earth & Dungeons Variants](https://www.curseforge.com/minecraft/texture-packs/ultimate-earth-dungeons-variants) (based on GillsBills / ReCrafted Mobs) | Modern baby sheep body and wool textures | CC BY-NC 3.0 |
| vectorwing and contributors — [Farmer's Delight](https://github.com/vectorwing/FarmersDelight) | Mud Kiln (adapted Stove) and cooking-pot assets, GUI, sounds and steam; knife textures and stats; cabbage, tomato, onion, rice and wild crop artwork | MIT |
| Lance5057 — Extra Delight | Soybean and chili artwork | CC BY-NC-SA 4.0 |
| PhantomWing — Rustic Delight | Cotton and coffee artwork | MIT |
| AlabasterLeking and contributors — Hearth and Harvest | Cultivated cotton and blueberry bush assets and behavior reference; blueberries texture | MIT |
| Faboslav, Furti Two and contributors — Friends & Foes | Tuff Golem model, animations, textures, sounds and spawn egg (unchanged) | CC BY-NC-ND 4.0 |
| GillsBills — [Recrafted Mobs v11.0](https://www.curseforge.com/minecraft/texture-packs/recrafted-mobs/files/6561402) | Sunken model and coral textures | CC BY-NC-SA 4.0 |
| Faboslav — [Variants & Ventures](https://github.com/Faboslav/variants-and-ventures) | Murk behavior reference, Sunken sounds and spawn egg (unchanged) | CC BY-NC-ND 4.0 |
| Wolfie_Waffle — [Hardcore Torches](https://github.com/WolfieWaffle/Hardcore-Torches-Forge) | Lit/unlit torch and unlit lantern block textures | Not relicensed; see provenance notes |
| Modern Better Than Wolves contributors; FlowerChild ([Better Than Wolves](https://github.com/BetterThanWolves/BetterThanWolves)) | Some owner-supplied textures (per-file mapping unverified); fire-plough interaction reference | See provenance notes |
| Wendall Cada — [Better Days](https://github.com/wendall911/BetterDays) | Accelerated sleep behavior reference | Reference only |
| [Vanilla Tweaks](https://vanillatweaks.net/picker/resource-packs/) | Copper tool oxidation idea; no code or assets included | Inspiration only |
| Mojang Studios | Minecraft, Minecraft Earth and Minecraft Dungeons designs; vanilla models, animations, sounds and resources referenced at runtime | Not claimed as original work |
| NeoForged contributors | NeoForge MDK foundation | MIT ([TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt)) |

Project-owner-supplied artwork without a recorded upstream source is listed in [resource provenance](docs/resource-provenance.md). This includes the cake, steel, oxidized-copper, Echo Shard, crafting-table and smoker textures.
