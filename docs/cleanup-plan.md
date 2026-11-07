# Project cleanup plan (Minecraft 26.3 / NeoForge 26.3.0.99)

Audit of `main` at `c8f7906` (2026-11-07). This file records what was found, what was cleaned up, and what is deliberately deferred. Remove it once the deferred items are resolved.

## Findings

### Documentation: wrong or out of date

| # | Problem | Status |
| --- | --- | --- |
| D1 | README and 17 feature docs stated Minecraft 26.1.2 / NeoForge 26.1.2.122 as the target, though the build pins 26.3 / 26.3.0.99. | Fixed. The target is stated once in the README; per-doc headers are removed. Upstream source revisions such as EarthMobs 26.1.x are kept as provenance facts. |
| D2 | A blanket "oven → mud_kiln" rename had corrupted the word *provenance* (into `prmud_kilnance`) in a file name, links and five documents. | Fixed. The file is now `docs/resource-provenance.md`. |
| D3 | Prose used "MudKiln" / "mud_kiln"; the in-game name was **Mud Kiln** (now **Kiln**). | Fixed. Identifiers stay `mud_kiln`. |
| D4 | Undocumented gameplay: bundle limits, Nether ice by crying obsidian, horse-armor Frost Walker, rose gold/steel beacon blocks and payments, trident repair, berry-bush protection, unlit-torch conversion, and the crafting-table/smoker texture overrides. | Fixed in `survival-tweaks.md` and `primitive-lighting.md`. |
| D5 | Docs claimed rose gold tools had no upgrade recipe, but five copper → rose gold smithing recipes ship. | Fixed in `equipment.md`. |
| D6 | The old README said the Sunken egg was custom; the bundled file is the unchanged Variants & Ventures Murk egg (hash-verified). | Fixed in the README and notices. |
| D7 | `port-26.3.md` referenced the deleted `port/26.3` branch and read as a live status. | Reframed as a port record. |
| D8 | KubeJS pins (`26.1.2-8.0.7` etc.) predate 26.3 but were presented as the current test setup. | Documented as unverified on 26.3 in Gradle, CI, the README and `cooking.md`. |
| D9 | `armor-trims-spec.md` was the original implementation prompt. | Now `armor-trims-design.md`, a design reference that defers to `armor-trims.md`. |
| D10 | Vanilla Tweaks (copper oxidation idea) was credited only in the README. | Added to THIRD_PARTY_NOTICES. |

### Documentation: redundant or messy

| # | Problem | Status |
| --- | --- | --- |
| R1 | The README mixed setup, a changelog narrative ("is now", "rebuilt after reverting PR #8") and a second full copy of the credits. | Restructured: overview, development, a verification command table, a feature index and a condensed credits table. |
| R2 | Tool and mining rules were split across three overlapping docs written as change requests. | Merged into `equipment.md` and `mining.md`, with one conversion table checked against `MiningRules`. |
| R3 | Eight hash manifests were mixed in with the prose docs; `crop-wild-rarity.json` was never linked. | Moved to `docs/asset-manifests/` and linked. |

### Implementation and build

| # | Problem | Status |
| --- | --- | --- |
| I1 | **Bug:** `BundleContentsMixin` set the quarter weight on `Mutable.getStackedWeight(ItemStack)`, which 26.3 only calls on removal. One unstackable item still filled a bundle. | Fixed by hooking `BundleContents.getWeight`, keeping nested-bundle and beehive weights. The new check failed before the fix and passes after. |
| I2 | `AbstractHorseFrostWalkerMixin` re-ran location effects every `aiStep`, although `LivingEntity.onChangedBlock` already covers the BODY slot. | Removed. Verified through the vanilla hook. |
| I3 | `IceBlockMeltMixin` skipped vanilla's `neighborChanged` and `BLOCK_DESTROY` game event. | Fixed to match vanilla's water branch. |
| I4 | No verification for the newest mechanics. | Added `SurvivalTweaksVerification` (server). |
| I5 | All client checks were gated by `chocolateflavored.verify.cooking.client` / `-PverifyCookingClient` and a `CookingVerification` world. | Renamed to `chocolateflavored.verify.client`, `-PverifyClient` and `ClientVerification`. |
| I6 | `ChocolateFlavored.java` used inline fully-qualified names and a one-expression creative tab. The empty `CakeContent.register` / `TrimBasalt.register()` calls looked dead but force static registration. | Imports added, tab builder extracted, and load-order comment added. Behavior is unchanged. |
| I7 | MDK leftovers: `maven-publish` to a local `repo/`, unused `data` run and `src/generated` source dir, an unused game-test run, empty blocks, a trailing space, and `.gitattributes` covering only `src/generated`. | Removed. |
| I8 | `.gitignore` lacked the KubeJS run directory and log; CI ran `chmod +x gradlew`. | Ignores added. The wrapper's executable bit is set in git and the CI steps are dropped. |

## Deferred (needs owner input or wider testing)

1. **Code style:** done for the densest shipped classes: TuffGolem, Sunken, SunkenSwimming, MonsterSpawns, MobContent, the Kiln and pot block entities, CookingContent/CookingRecipes, the trim combat/trigger/basalt classes, TrimLivingMixin, the Blockbench/CEM loaders, TuffGolemRenderer and EchoShards. Every step was checked by comparing `javap -c` output: identical except two documented local-variable hoists. Remaining: the long model and registry builder lines in `client/**/model` and `*Content` (readable as is), and the `verification` source set, especially `MobVerification`. Do these the same way, one package per commit.
2. **KubeJS on 26.3:** blocked upstream. As of 2026-11-07 the newest KubeJS is `26.1.2-8.0.8`, and the pinned build fails to start on NeoForge 26.3 (`RecipeManagerMixin` targets the removed `ContextAwareReloadListener`). The CI job is now `continue-on-error`. Update the pins and remove that flag once KubeJS ships for 26.3.
3. **Nether ice scope:** the rule checks `Level.NETHER`. Vanilla uses the `WATER_EVAPORATES` environment attribute. Decide whether other evaporating dimensions should also keep water by crying obsidian.
4. **Unused assets:** six Earth2Java `*_blink.png` textures are bundled but unused. Wire up blinking or remove them.
5. **Provenance:** confirm sources for owner-supplied textures listed in `resource-provenance.md` (Better Than Wolves set, crafting table, smoker, Echo Shard, oxidation).
6. **Manual testing:** bundle GUI and tooltip fullness, ridden-horse Frost Walker, trident repair, berry-bush protection, multiplayer.
7. **Upgrade check:** load a real 26.2 world before release (carried over from the port notes).
8. **AGENTS.md:** its last rule (no gameplay before foundation verification) is satisfied and could be retired. It was left unchanged because it is the owner's instruction file.
9. **Registration style:** some features self-register with `@EventBusSubscriber`; others are wired manually in the main class. Pick one convention per feature package during item 1.
