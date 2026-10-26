# Supplied resource prmud_kilnance

The README credits all identified resource projects and creators. THIRD_PARTY_NOTICES.md records confirmed source paths and retained licenses. Newly imported resources in this cleanup are copied unchanged from the documented source archives. A source project's contributor list is credited collectively where individual PNG authorship is not recorded; no file-specific artist is invented.

## Confirmed supplied texture matches

- Farmer's Delight: `textures/item/flint_knife.png`, `iron_knife.png`, `cooking_pot.png`, `empty_container_slot_bowl.png`, plus the mud_kiln/pot resources documented in THIRD_PARTY_NOTICES.md.
- Hardcore Torches: `textures/block/lit_torch.png`, `unlit_torch.png`, `unlit_lantern.png`. All three have identical Git blob hashes to the inspected Forge upstream. Upstream metadata declares All Rights Reserved; the Forge boilerplate license is not taken as permission to relicense those images.
- EarthMobs, Earth2Java, Earth Animals and Ultimate Earth & Dungeons Variants: selected resources and exact revisions/paths are recorded in THIRD_PARTY_NOTICES.md and the baby texture audit.

## Modern Better Than Wolves: supplied collection, unresolved file mapping

The project owner explicitly credits Modern Better Than Wolves for some supplied textures. The original archive/revision and a per-file author mapping were not included. Comparing byte hashes and RGBA pixels with the public eragaxshim/ModernBTW branches did not establish an exact match. This is recorded as an unresolved attribution detail rather than asserting that unrelated authors made these files.

Remaining originally supplied item texture files whose individual prmud_kilnance is not established here:

- `coal_dust.png`, `copper_ore_pile.png`, `iron_ore_pile.png`
- `stone_pebble.png`, `deepslate_pebble.png`
- `plant_fiber.png`, `plant_string.png`
- `knapped_flint.png`, `fire_starter.png`
- `diamond_ingot.png`
- `rose_gold_ingot.png`, `rose_gold_nugget.png`, `rose_gold_sword.png`, `rose_gold_pickaxe.png`, `rose_gold_axe.png`, `rose_gold_shovel.png`, `rose_gold_hoe.png`, `rose_gold_upgrade_smithing_template.png`
- `unlit_lantern.png` **item** sprite (distinct from the confirmed Hardcore Torches **block** texture)

The supplied `textures/block/rose_gold_block.png` likewise has no source record. These files are not all asserted to come from Modern Better Than Wolves: some may be original project artwork or adaptations. Confirming that distinction requires the project owner's original source files or links. Attribution in this repository does not change an unknown or reserved upstream license.

The removed dirt/sand/red-sand/gravel pile PNGs are no longer bundled. Modern baby sheep textures retain CC BY-NC 3.0 and are distributed for this noncommercial mod; other third-party portions keep their separate licenses.
