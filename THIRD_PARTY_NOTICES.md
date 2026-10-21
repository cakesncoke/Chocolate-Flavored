# Third-party notices

## Farmer's Delight

The supplied oven and cooking-pot models, textures, GUI, sounds and steam textures originate from [Farmer's Delight](https://github.com/vectorwing/FarmersDelight). Steam particle behavior and cooking interaction/layout concepts are adapted from that project. Reference revision: `22fe987639a9813f5b5474579518571ce91ed5d5` (26.1 branch).

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

MintRefresh's Earth Animals also supplies unchanged modern spawn-egg textures for Dairy Cow, Moobloom, Moolip, Pale Pig, Bronzed Chicken, Fancy Chicken, Skeleton Wolf and Muddy Pig, from `assets/earth_animals/textures/item/*_spawn_egg.png`; the LGPL-3.0-or-later notices above apply. Fuzzy, Long Nose and Patched Sheep use vanilla's modern sheep egg texture, since no corresponding dedicated egg texture was found in these sources.
