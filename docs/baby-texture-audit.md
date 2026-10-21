# Modern baby texture audit

Checked 2026-10-21. The mod remains Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Newer asset sources do not change those targets or introduce runtime dependencies.

| Installed variant | Verified modern texture source | Result |
| --- | --- | --- |
| Dairy Cow | MintRefresh / Earth Animals v1.2.1 (MC 26.2), `cow/cow_dairy_baby.png` (64×64) | Imported unchanged; native 26.1.2 BabyCowModel |
| Pale Pig | Same pack, `pig/pig_pale_baby.png` (32×32) | Imported unchanged; native 26.1.2 BabyPigModel |
| Bronzed Chicken | Same pack, `chicken/chicken_bronzed_baby.png` (16×16) | Imported unchanged; native 26.1.2 BabyChickenModel |
| Moobloom, Moolip | EarthMobs `textures/entity/moobloom/{moobloom,moolip}_baby.png` | Already installed; newer branch hashes match |
| Fancy Chicken | EarthMobs `textures/entity/fancy_chicken/fancy_chicken_baby.png` | Already installed; newer branch hash matches |
| Horned Sheep | EarthMobs `textures/entity/horned_sheep/horned_sheep_baby{,_hornless}.png` | Already installed; newer branch hashes match |
| Jumbo Rabbit | EarthMobs `textures/entity/jumbo_rabbit/jumbo_rabbit_baby.png` | Already installed; newer branch hash matches |
| Muddy Pig (pig state) | EarthMobs `textures/entity/muddypig/muddy_pig_baby.png` | Already installed; newer branch hash matches |
| Fuzzy Sheep | TheFancyFedora / Ultimate Earth & Dungeons Variants 26.1.x: `sheep_baby3.png`, `sheep_wool_baby3.png` | Imported unchanged; native 26.1.2 BabySheepModel, matching source base geometry |
| Long Nose Sheep | Same pack: `sheep_baby5.png`, `sheep_wool_baby5.png` | Imported unchanged; native BabySheepModel; Long Nose uses vanilla white baby wool |
| Patched Sheep | Same pack: `sheep_baby6.png`, `sheep_wool_baby6.png` | Imported unchanged; native BabySheepModel body/wool geometry |

## Sources and compatibility notes

- [Earth Animals](https://modrinth.com/datapack/earth-animals) / [v1.2.1 (MC 26.2)](https://modrinth.com/datapack/earth-animals/version/v1.2.1-mc-26.2): LGPL-3.0-or-later. Inspected the actual supplementary resource-pack ZIP (version `aPdhgyY7`), including baby PNG dimensions, rather than assuming an adult skin would fit a modern baby. The three named baby textures and the spawn-egg textures listed in THIRD_PARTY_NOTICES.md are imported, with attribution in THIRD_PARTY_NOTICES.md and license texts in META-INF/licenses.
- [EarthMobs](https://github.com/baguchi/EarthMobs): compared current 26.2.x (`7113ccf0fbfbec48921f5fb030f56ec3f3e948a7`) and 26.3.x (`5f87460378a856afa72c4e6afe8b2e7c9f8ade90`) trees against the pinned 26.1.x reference (`d5acab9846bd3400ac5f093bfec7439351a1bd61`). Requested existing baby assets have unchanged blob hashes; these branches do not supply modern Dairy Cow, Pale Pig, Bronzed Chicken, Fuzzy Sheep, Long Nose Sheep, or Patched Sheep assets.
- [Ultimate Earth & Dungeons Variants](https://www.curseforge.com/minecraft/texture-packs/ultimate-earth-dungeons-variants), [file 8210951](https://www.curseforge.com/minecraft/texture-packs/ultimate-earth-dungeons-variants/files/8210951): CC BY-NC 3.0. Inspected actual ZIP `Earth Dungeons Variants Pack 26.1.x.zip`. The sheep PNGs above live in `assets/minecraft/optifine/random/entity/sheep/`. `sheep_baby.properties` and `sheep_wool_baby.properties` map indices 3, 5 and 6 to Fuzzy, Long Nosed and Patched Sheep respectively. The archive also includes custom baby CEM geometry under `assets/minecraft/optifine/cem/sheep_baby/` and `sheep_baby_wool/`; the base models used by these three sheep have exactly the same six boxes, pivots and UVs as the pinned vanilla BabySheepModel after converting CEM coordinates. The five selected PNGs are bundled under their noncommercial license with the project owner's authorization. The Long Nose brown wool PNG is intentionally excluded in favor of vanilla tintable white baby wool.
- [Earth2Java](https://github.com/Slexom/earth2java): checked published branches; newest Minecraft-named branch is 1.21, predating the modern baby UV layouts. Existing adult textures are retained, but no modern baby assets were assumed from this source.
- [Earthly Mobs](https://modrinth.com/datapack/nydro-earthly-mobs): checked the updated 26.1 listing (MIT); it covers cow, pig, chicken, wolf and cat variants, not the three sheep variants above.

All three sheep variants now use modern dedicated baby textures and native baby geometry. White/rare-pink natural colors and player dyeing remain intact. No textures were generated, recolored, or remapped for this update.
