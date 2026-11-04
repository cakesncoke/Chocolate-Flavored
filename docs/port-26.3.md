# Minecraft 26.3 compatibility

Target: Minecraft 26.3, NeoForge 26.3.0.99, Java 25, branch `port/26.3`.
Comparison baseline: working 26.2 commit `332671ca3fefee8fc6c8cd15296c7f6bd241963e`; starting branch head `cd6b9ac927038a1e4b245c80c5ec47357a1bb4e3`.

Reference: [official NeoForged 26.2 → 26.3 migration primer](https://docs.neoforged.net/primer/docs/26.3/), checked against the pinned vanilla and NeoForge patched sources. The primer is non-exhaustive; patched sources matter, notably for water conversion.

## Priority and exact compatibility changes

Java paths below are relative to `src/main/java/com/akiotsukino/chocolateflavored/`.

| Priority | Affected paths / symbols | Cause and scoped fix |
| --- | --- | --- |
| 1: registry loading | `crop/CropWorldgen.java`, `CropPatchFeature.CODEC`, `SmallCropPatchFeature.CODEC`, `WildRiceFeature.CODEC`, `CropPatchConfiguration`; `src/main/resources/data/chocolateflavored/worldgen/feature/patch_wild_*.json` | Configured features were merged into `Feature`; serializers register in `Registries.FEATURE_TYPE`. Implement the new feature interface, retain custom config fields and feature identifiers, expose nested subfeatures, migrate the resource directory, flatten vanilla feature configuration, and update block-state/provider encoding. |
| 1: data decoding | `src/main/resources/data/**/loot_table/**/*.json`; `loot/SurvivalLootModifier.java`; `data/chocolateflavored/loot_modifiers/survival.json` | Loot conditions/modifiers now use registered holders and `type` dispatch. Convert plural conditions to one condition (an `all_of` for multiple conditions), functions to modifier sequences, and block-state conditions to `match_block`. Preserve probabilities, counts, item identifiers and conditions. |
| 1: recipe references | `gameplay/ItemRestrictions.forbiddenRecipe`, `mixin/RecipeRestrictionsMixin.chocolateflavored$filterRecipes`, `ChocolateFlavored` | `ModifyRecipeJsonsEvent` is gone; recipes are registry entries. Removing restricted entries before reference binding causes unbound references from advancements. Keep registry entries bound and filter `RecipeMap.create` in the manager constructor. Preserve output-based filtering for arbitrary datapack filenames and preserve gold smelting/recycling. |
| 1: Mixin linking | `mixin/FireStarterHandMixin`, `PastelBoatMixin`, `PastelSignMixin`, `TrimLivingMixin`, `EntityPipelineAccessor` | First-person rendering moved to `FirstPersonHandsAndItemsRenderer`; RenderPipeline moved to Renderpearl; model submission lost its crumbling-overlay argument; sign slots and living-entity callback signatures changed. Update exact targets/descriptors and callback parameters; retain every existing Mixin. |
| 2: first custom render | `client/mob/SlimeRenderTypes.PIPELINE`; `client/trim/TrimOutlines.PIPELINE` | Shared entity/line snippets no longer supply color target state. The slime gallery reproduced `Render pass color attachment count must match pipeline color target state count`. Add an opaque target for the animated cutout slime and translucent blending for the Eye outline. Keep animation, shader, depth behavior and outline rendering. |
| 2: shader / palettes | `assets/chocolateflavored/shaders/core/eye_outline.fsh`; `assets/chocolateflavored/textures/palettes/**`; `data/chocolateflavored/trim_material/{rose_gold,steel}.json`; `assets/minecraft/equipment/iron.json` | Shaderc needs `#include`, explicit matching varying/output locations and the separate-shader-objects extension. Palette textures now live under `textures/palettes`; trim material uses `palette_id`, with darker iron overrides on equipment layers. Move identical PNG bytes, retain palette identifiers, and retain vanilla iron overrides and all equipment layers. The obsolete static armor-trim atlas is replaced by the 26.3 palette path. |
| 3: silent functionality loss | `crop/CropContent`, `cake/CakeContent`, `dye/DyedWood`; former `data/neoforge/data_maps/item/{compostables,furnace_fuels}.json` | NeoForge ignores the removed compostables/furnace_fuels data-map types. Supply `COMPOSTABLE` and `COOKING_FUEL` through item properties using the corresponding vanilla context providers. Preserve 30%/65%/100% composting and 100/150/200/300/800/1200-tick fuel values, including fast-furnace scaling. Add sign-text components to both sign forms. |
| 3: gameplay API changes | `mob/MonsterBiomeModifier`, `worldgen/ZombieVillagerSpawnModifier`, `crop/CropBiomeModifier`; custom crop blocks; `time/SleepAcceleration`; `mob/entity/{BoulderingZombie,Swamped}`; clock consumers | Biome modifiers now receive registry access, spawn categories can be absent, spawn counts use providers, bonemeal callbacks receive `BonemealSource`, clocks expose instances, beds use the new bed API, and zombies use `ConversionTracker`. Adapt calls while retaining spawn ratios, timings, crop growth and conversion types. |
| 3: item / animation APIs | `content/ModContent`, entity render layers/states, `lighting/FireStarterItem`, `echo/EchoShards`, `trim/TrimBasalt`, `client/trim/TrimTooltips` | Tool behavior moved to item properties; swings moved into native swing state; drop prediction is explicit; piston reaction names and input scancodes changed. Use native 26.3 behavior, remove duplicate legacy swing ticking, and use SDL-backed `InputConstants`. |

## Failure ordering and consequences

Registry decoding and Mixin application must succeed before world initialization can be assessed. A successful Gradle build alone does not exercise either path. In the fresh runtime tests, registry startup succeeded after the recipe-reference fix; the client then passed resource loading but failed when the mob gallery first rendered its custom slime pipeline.

The `Render pass had debug groups left open` suppressed exception follows the color-attachment mismatch; it is not an independent defect. System-information exceptions printed while producing crash reports are likewise unrelated to the triggering render failure. Missing/unbound registry references can be downstream effects of excluding recipe entries too early rather than missing mod item registrations.

Verification-only failures were corrected separately: use the server's reloadable recipe lookup, the empty-safe `getMobsToSpawn`, `damageCooldownTime` for combat cooldown resets, and actual water for NeoForge's fluid-interaction conversion test. A reused test world produced a lighting interaction failure; fresh generated verification worlds passed without relaxing gameplay permission checks.

## Warnings

Command-parser ambiguity messages are vanilla development diagnostics. Gradle deprecation notices do not prevent this pinned build. This sandbox also lacks normal OS process/mount metadata, audio hardware and access to Minecraft service discovery; those warnings do not establish a mod defect. Offline authentication and unavailable LAN broadcast are test-environment settings. The obsolete data-map warnings were **not** harmless and are fixed as described above.

## Validation and remaining checks

- `build`: passed with Java 25 and the pinned dependencies, including a fresh export of the exact staged tree.
- Full dedicated-server verification: passed, including the final compost/fuel regression checks and live sleep timing (approximately 20 seconds for 1,050 simulated ticks). A fresh staged-tree export also passed, without the obsolete data-map warnings.
- Client: all 18 smoke-test markers passed, including the previously failing animated slime, all entity galleries, 208 dyed-wood item models, all 18 trim patterns, Eye/Dune outlines, steel trim palettes, Echo Shards and copper wear models. No client ERROR/FATAL messages occurred in the successful run.
- Existing save/load, legacy cooking migration and saved hammer-alias checks are retained. No real user-provided 26.2 world was available, so a full existing-world upgrade is not yet certified.
- Optional KubeJS/Serene Seasons compatibility is not claimed. No external mod dependency was added; the optional KubeJS CI configuration still targets its previously pinned version.

Implementation stages: (1) registry/resource migration, (2) exact Mixin/API adaptation, (3) client pipeline/palette/shader validation, (4) restored fuel/compost components and regression checks, (5) clean build plus fresh-world server/client smoke tests, followed by a real 26.2 save upgrade check before release.
