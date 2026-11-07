# Survival tweaks

These are small vanilla adjustments with no new items, recipes or dependencies.

## Bundles

- An unstackable item (maximum stack size 1, such as a tool, weapon or armor piece) uses **a quarter** of a bundle instead of the whole bundle, so a bundle holds up to four of them.
- A nonempty bundle accepts only items matching its contents' stackability: unstackable items cannot be added to stackable ones, and vice versa. An empty bundle accepts either.
- Nested bundles and beehives or nests with bees keep their vanilla weights, so a full bundle still cannot be put inside another bundle.

Implementation: `BundleContentsMixin` replaces the weight in vanilla's `BundleContents.getWeight`, which drives insertion, transfer, removal and total weight. `BundleMutableMixin` enforces the stackability rule in `BundleContents.Mutable.tryInsert`.

## Nether ice by crying obsidian

In the Nether, ice that melts from block light while touching crying obsidian on any face becomes water instead of evaporating. It uses vanilla's normal melting updates, so neighbors and sculk sensors react as usual. Elsewhere, and without adjacent crying obsidian, melting is unchanged. Breaking ice by hand in the Nether still evaporates it as in vanilla; only light-driven melting is affected.

The rule checks the Nether dimension key specifically. Other dimensions whose environment evaporates water are not affected.

## Frost Walker on horse armor

Frost Walker can be applied to all horse armor through `#chocolateflavored:frost_walker_enchantable` (foot armor plus leather, iron (Steel), golden, diamond and netherite horse armor). The overridden `data/minecraft/enchantment/frost_walker.json` also lists the `body` slot. A horse wearing enchanted armor freezes water while walking, using vanilla's location-changed enchantment hook and the vanilla requirements (on the ground, not itself riding something).

The enchantment file is a full copy of the vanilla 26.3 definition with only `slots` and `supported_items` changed. Re-copy it from the pinned vanilla data when updating Minecraft.

## Beacons

Rose gold and steel blocks count as beacon base blocks (`minecraft:beacon_base_blocks`). Rose gold and steel ingots are accepted as beacon payments (`minecraft:beacon_payment_items`).

## Equipment details

- Tridents can be repaired with prismarine shards.
- Wearing anything in the legs slot prevents sweet berry bush damage.

## Interactions documented elsewhere

Right-clicking a campfire (lit or unlit), a vanilla torch, a lit timed torch or a Kiln with an unlit torch converts one torch into a fully fueled lit torch. See [primitive lighting](primitive-lighting.md#torches).

## Resource overrides

The project owner supplied replacement vanilla textures for the crafting table (front, side, top) and smoker (all faces, with an animated lit front). Provenance is recorded in [resource provenance](resource-provenance.md#other-project-owner-supplied-textures).

## Verification

`SurvivalTweaksVerification` (dedicated server) checks the four-unstackable limit, quarter removal, both mixing rules and full-bundle nesting. It also checks Nether ice with crying obsidian on each of the six faces, ice without it, and unchanged Overworld melting. It checks that enchanted diamond horse armor freezes water through the vanilla hook while unenchanted armor does not, and that both ingots are beacon payments. The beacon-base tag is checked in `DevelopmentVerification`. Trident repair, berry bush protection, ridden-horse play and the bundle GUI are not automated.
