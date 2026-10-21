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
