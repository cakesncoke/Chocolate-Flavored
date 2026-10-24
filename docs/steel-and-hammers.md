# Steel equipment, materials and hammers

Targets Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Standalone.

## Steel appearance

Vanilla iron swords, pickaxes, axes, shovels, hoes, spears, helmets, chestplates, leggings, boots, horse armor and nautilus armor display as Steel equipment in English. The existing mod iron knife displays as Steel Knife. The supplied native item, held-spear and humanoid/leggings/horse/nautilus equipment textures are used through their Minecraft resource paths. The knife retains its existing supplied sprite because no replacement knife texture was provided.

Registry IDs, recipes, repair ingredients, attributes, entity equipment, loot and existing saved stacks stay intact: this is a name/appearance change, not a second set of duplicate equipment. Iron ingots, nuggets, blocks and trim materials retain their original identities. Supplied `steel` trim palettes remain available as source assets; this request does not introduce a new armor-trim material. No custom baby humanoid armor texture was supplied; that native layer remains vanilla.

## Materials and block

New items: steel ingot, steel nugget, steel plate, diamond plate, tanned leather, cut tanned leather, strap, padding, wool, creeper oysters and diamond armor plate. These are stackable material items without food effects or assigned recipes/drops. Steel block copies native iron-block strength, blast resistance, sound and tool requirements and drops its own item when harvested correctly. Common `c:` ingot/nugget/plate/storage tags are supplied for datapacks and optional recipe scripting.

All fifteen additions (including the gold ore pile) (eleven materials, steel block and two hammers) appear in the mod creative tab. The Chocolate Cake tab icon remains unchanged. No new recipes are added by this change.

## Hammers

| Item | Durability | Total attack damage | Attacks per second | Repair material |
| --- | ---: | ---: | ---: | --- |
| Stone Hammer | 32 | 11 | 0.6 | Vanilla stone tool materials |
| Deepslate Hammer | 500 | 12 | 0.7 | Vanilla iron tool materials |

Damage and speed are native main-hand attribute modifiers (including the player's base 1 damage and 4 attack speed). Both exceed their corresponding vanilla axe's damage and have a slower cooldown. They use the native Weapon component: one durability per successful hit, no sword sweeping or mace smash behavior. Durability/sharp-weapon enchantment tags allow appropriate standard enchantments. Neither hammer strips wood or acts as an axe. Deepslate Hammer now uses stone-tier pickaxe rules, mining speed 4 and normal wear for ordinary mining; Stone Hammer retains hand-speed primitive mining.

Stone Hammer replaces Knapped Flint in the original conversion system; Deepslate Hammer now follows [the expanded mining rules](deepslate-mining.md):

- Stone, granite, diorite and andesite become cobblestone and drop one stone pebble.
- Cobblestone and mossy cobblestone disappear and drop one stone pebble.
- Coal and copper ore become cobblestone and drop one coal dust or copper ore pile.
- Each conversion uses one durability and native durability/enchantment handling. Stone Hammer ordinary mining uses hand speed and does not spend durability.
- Existing copper/steel pickaxe progression is unchanged. Deepslate Hammer ore conversion rules are documented separately.
- Creative breaking and canceled/protected breaks keep their prior behavior.

`chocolateflavored:hammers` selects tools for these mechanics. The existing `flint_to_cobblestone` and `flint_pebbles` block-tag IDs remain stable for datapacks. Iron Hammer is now a registry alias for Deepslate Hammer. Knapped Flint is no longer registered or shown; a NeoForge registry alias loads its old ID as Stone Hammer, preserving serialized component patches. Its supplied PNG is retained as archived artwork.

## Validation

The development-only server fixture exercises both hammers through real block destruction, conversion/drop counts, single wear and final-use breakage, attack components, free native anvil repairs, old-stack decoding, steel-block harvesting and material defaults. The client fixture checks translated equipment names, all new creative entries, models, native worn armor and held spear rendering. It renders the supplied item sprites unchanged. These fixtures are excluded from the release JAR.

No season mechanics change. Optional KubeJS is a regression test only; no required mod dependency is added.
