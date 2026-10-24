# Third-party notices

## Farmer's Delight

The supplied oven and cooking-pot models, textures, GUI, empty-bowl slot sprite, Flint Knife/Iron Knife textures, sounds and steam textures originate from [Farmer's Delight](https://github.com/vectorwing/FarmersDelight). Steam particle behavior and cooking interaction/layout concepts are adapted from that project. Reference revision: `22fe987639a9813f5b5474579518571ce91ed5d5` (26.1 branch).

Copyright (c) 2020 vectorwing. MIT License. The complete notice is distributed in the mod JAR at `META-INF/licenses/FarmersDelight-MIT.txt`.

The standalone implementation uses Chocolate Flavored registrations and Minecraft 26.1.2/NeoForge APIs; Farmer's Delight is not a runtime dependency.

## Earth Mobs

Selected animal and monster behavior, native models, textures, sounds, and loot data are adapted from [baguchi/EarthMobs](https://github.com/baguchi/EarthMobs), revision `d5acab9846bd3400ac5f093bfec7439351a1bd61` (26.1.x). Copyright Earth Mobs contributors, including baguchi, MCVinnyq, Farcr, Evo_934 and the credited artists. These portions remain **LGPL-3.0-only**, not MIT; their license is included at `META-INF/licenses/EarthMobs-LGPL-3.0.txt`. Adapted Java files carry SPDX notices; imported textures are under `textures/entity/{moobloom,fancy_chicken,horned_sheep,jumbo_rabbit,muddypig}` and the Buttercup/Pink Daisy block textures. Additional LGPL adaptations cover Melon Golem, Bouldering Zombie, Skeleton Wolf, Bone Spider and its projectiles/effects, Tropical Slime, and the Lobber Zombie model used for Swamped. Imported monster assets are under `textures/entity/{melon_golem,bouldering_zombie,skeleton_wolf,bone_spider,tropical_slime,lobber_zombie}`, `sounds/mob/skeleton_wolf`, `textures/mob_effect/{undead_body,zombified}.png`, the carved-melon block textures, Bone Shard projectile texture, Tropical Slime Bucket texture, and Tropical Splash particle texture. Swamped uses vanilla zombie AI with the requested poison attack; reference drops that require unrequested items use vanilla equivalents. Source and modifications are available in this repository. No EarthMobs or Bagus Lib runtime dependency is used.

## Earth2Java

Dairy Cow, Pale Pig, Bronzed Chicken, Fuzzy Sheep, Long Nosed Sheep and Patched Sheep textures originate from [Slexom/earth2java](https://github.com/Slexom/earth2java). Copyright (c) 2021 Slexom, MIT License, included at `META-INF/licenses/Earth2Java-MIT.txt`. These variants are absent from EarthMobs' current branch. No Earth2Java runtime dependency is used.

## Earth Animals

The modern Dairy Cow, Pale Pig and Bronzed Chicken baby textures are copied unchanged from MintRefresh's [Earth Animals](https://modrinth.com/datapack/earth-animals), resource pack `v1.2.1 (MC 26.2)`, Modrinth version `aPdhgyY7`. These assets remain **LGPL-3.0-or-later**; see `META-INF/licenses/EarthAnimals-LGPL-3.0.txt` and the companion GPL text. Original Minecraft Earth designs are credited to Mojang. Source files are `assets/earth_animals/textures/entity/{cow/cow_dairy_baby,pig/pig_pale_baby,chicken/chicken_bronzed_baby}.png`; imported paths retain the corresponding mob names under `assets/chocolateflavored/textures/entity/`. The implementation uses Minecraft 26.1.2 native baby models and does not load the datapack or require another mod.

Reference resource-pack ZIP SHA-256: `556f0cf0778b448605ca24a36abee18d0787b1f0a1048971a9f90fd178d72a14`.

## Ultimate Earth & Dungeons Variants

TheFancyFedora's [Ultimate Earth & Dungeons Variants](https://www.curseforge.com/minecraft/texture-packs/ultimate-earth-dungeons-variants), [26.1.x file 8210951](https://www.curseforge.com/minecraft/texture-packs/ultimate-earth-dungeons-variants/files/8210951), supplies unchanged modern Fuzzy Sheep, Long Nose Sheep and Patched Sheep baby body textures (`sheep_baby3.png`, `sheep_baby5.png`, `sheep_baby6.png`) and Fuzzy/Patched baby wool (`sheep_wool_baby3.png`, `sheep_wool_baby6.png`). Source directory: `assets/minecraft/optifine/random/entity/sheep/`; destination: corresponding `textures/entity/{fuzzy_sheep,long_nosed_sheep,patched_sheep}/*_baby.png`. These assets retain **CC BY-NC 3.0 Unported**, included at `META-INF/licenses/UltimateEarth-CC-BY-NC-3.0.txt`, and are not covered by this repository's MIT license. The project's distribution is noncommercial. Attribution: TheFancyFedora (custom Minecraft Earth/Dungeons baby textures), Mojang (original Minecraft Earth designs); the source pack credits GillsBills' ReCrafted Mobs as its base. No unrelated assets from that base pack are imported. No textures were recolored. The source CEM base sheep boxes and UVs were compared with the exact 26.1.2 vanilla BabySheepModel and match; native vanilla geometry is used without OptiFine/EMF. Long Nose uses vanilla tintable white baby wool instead of the source's brown wool.

## Cleanup additions to the sources above

EarthMobs additionally supplies Husk Rabbit behavior (including its ZombifiedRabbit base), adult/baby skins, Tropical Slime Block behavior/model/texture, Tropical Ball behavior/texture, Husk Rabbit Foot texture, and modern eggs for Horned Sheep, Jumbo Rabbit, Melon Golem, Bouldering Zombie, Bone Spider, Tropical Slime, Swamped (source Lobber Zombie egg), and Husk Rabbit (source Zombified Rabbit egg). LGPL-3.0-only notices above apply. The rider's removed wooden spear is replaced by a wooden sword; private vanilla hop-delay access uses a narrow mixin; native consumable handling prevents double consumption. No new crafting or brewing recipes are imported.

MintRefresh's Earth Animals also supplies unchanged modern spawn-egg textures for Dairy Cow, Moobloom, Moolip, Pale Pig, Bronzed Chicken, Fancy Chicken, Skeleton Wolf and Muddy Pig, from `assets/earth_animals/textures/item/*_spawn_egg.png`; the LGPL-3.0-or-later notices above apply. Dairy Cow, Pale Pig and Bronzed Chicken egg images are retained as unused reusable assets; their eggs, along with Fuzzy, Long Nose and Patched Sheep eggs, are no longer registered.

## Hardcore Torches

Wolfie_Waffle (GitHub WolfieWaffle), [Hardcore Torches Forge](https://github.com/WolfieWaffle/Hardcore-Torches-Forge). Three project-owner-supplied block textures match upstream byte for byte: `textures/block/lit_torch.png`, `unlit_torch.png`, `unlit_lantern.png`. Source root: `src/main/resources/assets/hardcore_torches/`; destination root: `assets/chocolateflavored/`. This is texture reuse only; torch timer and ignition code are independent implementations. Upstream `gradle.properties` declares **All Rights Reserved**; its Forge boilerplate LICENSE.txt is not treated as a blanket license grant for the artist's textures. Attribution is not a substitute for the applicable redistribution permission. These assets are excluded from Chocolate Flavored's MIT grant.

## Better Than Wolves / Modern Better Than Wolves

FlowerChild is credited for original Better Than Wolves and the primitive fire-plough interaction reference (ordinary swings, high-pitched eating sounds and item-chip particles), inspected in the [original source repository](https://github.com/BetterThanWolves/BetterThanWolves). The project owner also identifies Modern Better Than Wolves as a source of supplied textures. No upstream revision, per-file artist list or original download accompanied that supplied collection; do not interpret the repository's MIT license as relicensing it. The inspected [eragaxshim/ModernBTW](https://github.com/eragaxshim/mbtw) 1.16/1.18/1.19/1.20/1.21 sources did not establish exact file matches. [Resource provenance](docs/resource-provenance.md) lists the remaining supplied files, without inventing a definite per-file attribution. Source-level credit is retained while those records remain unverified.

## Friends & Foes — Tuff Golem restart

[Faboslav/friends-and-foes](https://github.com/Faboslav/friends-and-foes), revision `e76a58eb6ffb47f694b60dbe4ca27613aa0c2b3a`. Faboslav (mod author), Furti Two (models/textures contributor credited upstream), and the Friends & Foes contributors. Original mob concept: Mojang.

The original `blockbench/tuffgolem/TuffGolem.bbmodel` (including its eleven animation clips), all eighteen entity PNGs (base, closed eyes and sixteen cloth colors), the modern Tuff Golem egg PNG, and fourteen Tuff Golem OGG sounds are copied **byte for byte without alterations**. Original file paths and SHA-256 hashes are recorded in [tuff-golem-assets.json](docs/tuff-golem-assets.json). They remain **CC BY-NC-ND 4.0**, excluded from this repository's MIT grant. Full license: `META-INF/licenses/FriendsAndFoes-CC-BY-NC-ND-4.0.txt`. Distribution remains noncommercial; no endorsement is implied.

The Blockbench file is loaded directly at runtime by an independently written native box-UV/keyframe reader. No replacement model, edited model export, changed artwork or edited keyframe resource is distributed. Native coordinate/format interpretation is performed by the reader. The state machine and renderer integration are standalone code guided by upstream interactions and timing, rather than a copy of its version-preprocessed entity/model classes. The upstream README explicitly permits incorporating code snippets; attribution is retained. No Friends & Foes, GeckoLib, Architectury, OptiFine or other runtime dependency is added.

## Recrafted Mobs — Sunken

GillsBills' [Recrafted Mobs v11.0, file 6561402](https://www.curseforge.com/minecraft/texture-packs/recrafted-mobs/files/6561402) supplies the unchanged `skeleton4.jem` CEM model and three `sunken_skeleton*.png` textures. Exact source and destination paths and SHA-256 hashes: [manifest](docs/sunken-assets.json). The original pack notice is bundled at `META-INF/licenses/Recrafted-Mobs.txt`. These assets remain [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International](https://creativecommons.org/licenses/by-nc-sa/4.0/), not MIT; any adaptations of these assets must retain that license. This project's distribution is noncommercial.

The pack's credits list GillsBills as creator, with help from Cave Fox, Kekkman, PinkuRinku, RavenVLemon, Ewanhowell5195, creepermax123 and MickeyJoeAlpha. No individual Sunken artist is identified. Mojang is credited for the original Minecraft Dungeons designs. The native loader preserves the source geometry and UVs, attaches it to vanilla humanoid animation, and makes its coral planes hide when sheared; the bundled source files are unmodified. The Java loader and behavior are independently implemented. Native arrows are referenced rather than redistributed; the Sunken egg uses the custom image supplied in the repository. No resource-pack loader or external mod is required.

## Variants & Ventures — Murk reference and Sunken sounds

Faboslav's [Variants & Ventures](https://github.com/Faboslav/variants-and-ventures), commit `0ac431558806d10ae60e58ac60d52f431d6eca7b`, provides the Murk behavior reference and unmodified sound recordings listed in [the exact asset manifest](docs/sunken-murk-assets.json). Included categories: ambient, ambient_water, hurt, hurt_water, death, death_water, attack, step and shear. Upstream identifies Faboslav as mod author and does not identify a separate recording author in its README. All recordings are byte-for-byte copies under **CC BY-NC-ND 4.0**, with the original license bundled at `META-INF/licenses/Variants-and-Ventures.txt`; they are not MIT and this distribution is noncommercial. The unchanged Murk spawn egg image is included under the same **CC BY-NC-ND 4.0** license; no upstream entity model, entity texture or Java source is bundled. Behavior is independently implemented using the exact Minecraft 26.1.2 APIs, and animation is supplied by Minecraft's native skeleton model. No endorsement by the upstream author is implied.


## Better Days — sleep behavior reference

[Wendall Cada (wendall911) — Better Days](https://github.com/wendall911/BetterDays), 26.1 revision `995baf3e89f31a7ae3b4bd2005e5084d2b7fb76b`, was consulted for its accelerated sleeping and process-progression design (`TimeService`, `SleepStatus`, `BlockEntityTimeEffect`). Better Days identifies [Nick Iacullo (DuckyCrayfish) — Hourglass](https://github.com/DuckyCrayfish/hourglass) as the original source of that design. This is a reference credit: no Java code, textures, sounds or other assets were copied from either project. The new ten-second sleep controller is independently implemented against Minecraft 26.1.2's native scheduler; neither reference mod is a dependency.

## Crop resources and generation references

- **Lance5057 and Extra Delight contributors**, [Extra Delight](https://github.com/Lance5057/ExtraDelight), `effd768cb8a4b8a2c56deb7d9cf071e795fffe5b`: unchanged soybean/chili crop-stage, seed, raw-harvest and wild-plant textures; crop and item models/blockstates with namespace changes. Upstream `gradle.properties` declares **CC BY-NC-SA 4.0**. Artwork and resource adaptations retain that license, excluded from this repository's MIT grant. Notice/URI: `META-INF/licenses/ExtraDelight-CC-BY-NC-SA-4.0.txt`. Native Java behavior is independently implemented; no Extra Delight Java is copied.
- **PhantomWing and Rustic Delight contributors**, [Rustic Delight](https://github.com/PhantomWing/RusticDelight), `e4e0ca19f3d8baa61e43001c57b5ecbe652d6d58`: unchanged cotton/coffee crop-stage, seed, raw-harvest and wild-plant textures; model/blockstate JSON adapted to the local namespace; harvest and cluster-layout references. **MIT**, preserved at `META-INF/licenses/RusticDelight-MIT.txt`.
- **vectorwing and Farmer's Delight contributors**, [Farmer's Delight](https://github.com/vectorwing/FarmersDelight), `dd8b9b0bfec916a2da88a6d64e3a37f4a02c3259`: unchanged cabbage, tomato, onion, rice and wild-crop artwork (including wild vanilla carrots/potatoes/beetroots); crop templates/models and item/blockstate JSON adapted to local IDs; loot/cluster layouts; the MIT crop-patch configuration/feature and filtered-biome modifier are adapted to native 26.1.2 APIs. **MIT**, preserved at `META-INF/licenses/FarmersDelight-MIT.txt`.

The precise asset list and original/bundled SHA-256 hashes are in [docs/crop-assets.json](docs/crop-assets.json). None of these mods is a dependency. No meals, crates, machinery, or unrelated resources are imported; no endorsement is implied.

## Project-owner-supplied cake textures

The Pomegranate, Coffee, Cherry, and Chocolate Cake block/item PNGs were added by the project owner in main commit `c66392874998898cc315ba973a02137b8ea418cf`. They are reused unchanged; [docs/cake-assets.json](docs/cake-assets.json) records the twenty exact paths and hashes. No upstream artist, source project or per-file license accompanied this addition; existing supplied-resource provenance notices remain applicable, and no new attribution or MIT relicensing of third-party artwork is inferred. Models reference Minecraft 26.1.2's existing cake/slice/candle geometry and native candle textures rather than importing external models or artwork.

## Pastel wood rendering

The dyed wood set references Mojang/Minecraft 26.1.2 oak door and trapdoor textures, pale oak wood/item/sign/boat textures, vanilla model geometry and water-cauldron models. No Minecraft PNGs are copied into this mod. Small model JSON derivatives add tint indices; the tint palette, registration and cauldron code are Chocolate Flavored implementations. Minecraft resources remain the property of Mojang/Microsoft and are not relicensed under this project's MIT license.
