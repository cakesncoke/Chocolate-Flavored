# Equipment and materials

No required external mods. The only recipes added here are the rose gold smithing upgrades. Mining behavior, including both hammers, is described in [mining](mining.md).

## Durability and tool statistics

- Wooden sword and stone axe: 32 durability each.
- Fishing rod: 192 durability (vanilla ×3).
- Other damageable vanilla equipment: ×2 durability, except golden equipment, which keeps vanilla durability. This includes armor, elytra, ranged weapons, spears, shields, shears and other damageable utility equipment. The iron (Steel) pickaxe therefore has 500 durability.
- Rose gold tools: iron harvesting tier and attack damage, diamond mining speed (8), gold enchantability (22), 576 durability, repairable with rose gold ingots. Rose gold pickaxes use normal vanilla harvesting.
- Flint knife: 131 durability, 2.5 total attack damage, 2 attacks/second, mining speed 4, enchantability 5. Iron knife: 250 durability, 3.5 total attack damage, 2 attacks/second, mining speed 6, enchantability 14. These are Farmer's Delight's base knife statistics, without its dependency or unrelated features. Knives do not receive Sweeping Edge.

| Hammer | Durability | Total attack damage | Attacks per second | Repair material |
| --- | ---: | ---: | ---: | --- |
| Stone Hammer | 32 | 11 | 0.6 | Vanilla stone tool materials |
| Deepslate Hammer | 500 | 12 | 0.7 | Vanilla iron tool materials |

Hammer damage and speed are native main-hand attribute modifiers (including the player's base 1 damage and 4 attack speed). Both exceed the matching vanilla axe's damage with a slower cooldown. They use the native Weapon component: one durability per successful hit, with no sweeping or mace smash. Durability and sharp-weapon enchantment tags allow the appropriate standard enchantments. Neither hammer strips wood or acts as an axe.

## Removed equipment

Wooden pickaxe/axe/shovel/hoe; stone sword/pickaxe/shovel/hoe; wooden/stone/copper spears; and copper player, horse and nautilus armor are unavailable.

Vanilla IDs are kept for registry compatibility. Their recipes are removed, creative and search entries are hidden, loot is filtered, and villager offers are filtered before merchant menus access them. Dropped stacks and equipped items are removed on spawn/tick. Player inventories, ender chests, carried stacks and opened containers are sanitized. Golden equipment stays obtainable from loot and trades; only its crafting recipes are removed. Gold food, ingots, blocks and other non-equipment recipes remain.

## Steel appearance

Vanilla iron swords, pickaxes, axes, shovels, hoes, spears, helmets, chestplates, leggings, boots, horse armor and nautilus armor display as **Steel** equipment in English, using the supplied item, held-spear and humanoid/leggings/horse/nautilus equipment textures at their Minecraft resource paths. This is a name and appearance change only. Registry IDs, recipes, repair ingredients, attributes, entity equipment, loot and saved stacks are unchanged, and iron ingots, nuggets, blocks and the iron trim material keep their identities. The mod's iron knife keeps its name and sprite. Baby humanoid armor keeps the vanilla layer because no custom texture was supplied.

## Rose gold and steel trims

Rose gold ingots provide the `chocolateflavored:rose_gold` trim material, and steel ingots provide `chocolateflavored:steel`. Both extend vanilla's trim-material ingredient tag, so vanilla trimming recipes accept them. Trims render on worn armor and in the inventory using the supplied `rose_gold.png` and `steel.png` palettes. Native iron (Steel) armor uses `steel_darker.png` for contrast. No rose gold armor textures exist, so no rose gold armor set is added. Rose gold tools are made at a smithing table from the matching copper sword, pickaxe, axe, shovel or hoe, the rose gold upgrade smithing template and one rose gold ingot (`data/chocolateflavored/recipe/rose_gold_*_smithing.json`). Damage, custom names and other components carry over. The template itself has no crafting recipe and is available in creative. Trim abilities are described in [armor trims](armor-trims.md).

## Materials and blocks

Registered materials cover every supplied texture: rose gold ingot, nugget and block; diamond ingot; coal dust; copper, iron and gold ore piles; stone and deepslate pebbles; plant fiber and plant string; steel ingot, nugget, plate and block; diamond plate; tanned leather and cut tanned leather; strap; padding; wool; creeper oysters; and diamond armor plate. They are stackable, have no food effects, and have no assigned recipes. The steel block copies native iron-block strength, blast resistance, sound and tool requirements, and drops itself when harvested correctly. Common `c:` ingot/nugget/plate/storage tags are supplied for datapacks and recipe scripts. All of these appear in the mod creative tab. Rose gold and steel ingots are also valid beacon payments (see [survival tweaks](survival-tweaks.md)).

## Saved-data aliases

- `chocolateflavored:knapped_flint` loads as the Stone Hammer, preserving component patches. Knapped Flint is no longer registered; its PNG is kept as archived artwork.
- `chocolateflavored:iron_hammer` loads as the Deepslate Hammer.
- Dirt, sand, red-sand and gravel pile items were removed, and their resources are no longer bundled. Existing stacks of those IDs are not migrated.

## Verification

The development-only dedicated server checks the five rose gold smithing upgrades (including component preservation), live component values (durability, speed, enchantability), recipe removal, trim data, merchant filtering, both hammers' attack components and free anvil repairs, saved-ID aliases, steel-block harvesting and material defaults. The client fixture checks translated Steel names, creative entries, item models, worn armor, held-spear rendering and trim palettes. Manual multiplayer interaction and visual inspection of worn trims are not automated.

## References

Vanilla behavior was originally inspected in the unobfuscated 26.1.2 client (`Items`, `Item.Properties`, `ToolMaterial`, `Tool`, `Player`, `ServerPlayerGameMode`, `RecipeManager`, `AbstractVillager`, trim metadata, loot/item-model/atlas resources). APIs that changed were re-checked during the 26.2 and 26.3 ports (see [port-26.3.md](port-26.3.md)). Knife statistics reference Farmer's Delight [ModMaterials](https://github.com/vectorwing/FarmersDelight/blob/1.21/src/main/java/vectorwing/farmersdelight/common/registry/ModMaterials.java) and [ModItems](https://github.com/vectorwing/FarmersDelight/blob/1.21/src/main/java/vectorwing/farmersdelight/common/registry/ModItems.java); its Java implementation was not ported.
