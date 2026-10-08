# Third-party notices

## Farmer's Delight

The Oven includes adapted stove behavior and item-rendering code, stove models,
blockstates, textures (including animation metadata), and adapted crafting/loot
data from [Farmer's Delight](https://github.com/vectorwing/FarmersDelight).

- Author: vectorwing and contributors
- Source branch: `1.21` (Minecraft 1.21.1)
- Source commit: `dd8b9b0bfec916a2da88a6d64e3a37f4a02c3259`
- License: MIT, Copyright (c) 2020 vectorwing
- Full license: [licenses/FarmersDelight-MIT.txt](licenses/FarmersDelight-MIT.txt)

Adapted Java sources: `AbstractStoveBlock`, `StoveBlock`,
`AbstractStoveBlockEntity`, `StoveBlockEntity`, and `DefaultStoveRenderer`.
Copied textures: `stove_bottom`, `stove_front`, `stove_front_on`, `stove_side`,
`stove_top`, and `stove_top_on`, renamed to `oven_*` in this mod's namespace.
Models and blockstates are similarly renamed. The crackle sound references the
same vanilla campfire sound event; particles use vanilla smoke and flame.

Changes include fuel storage/consumption, manual ignition, independent Oven
recipes, optional tag-based igniters, inventory retrieval, and independent
registrations. Farmer's Delight is not a build or runtime dependency. This is
not an official Farmer's Delight release or an endorsement by its authors.

The full upstream MIT notice is also bundled in the mod JAR at
`META-INF/licenses/FarmersDelight-MIT.txt`.

## Hardcore Torches

No Hardcore Torches source code or assets are copied. The optional
`hardcore_torches:fire_starter` item ID is included in a data-pack item tag with
`required: false`. When used on the Oven, this item follows the Oven's direct
right-click ignition rule (one item consumed on successful ignition). The
Hardcore Torches hold/release timing, random failure chance, and configuration
are not replicated or overridden for its own blocks.

## Cooking Pot adaptation

The Cooking Pot's block, block entity, heat checks, inventory/menu/slots, item
components, cooking and serving recipes, recipe book, tooltip, steam particle,
synchronization and utility methods are adapted from the same Farmer's Delight
commit and MIT license listed above. Its models, textures, GUI sprites, steam
frames and five boiling recordings are copied into this mod's namespace.
The three included meals (mushroom stew, beetroot soup, rabbit stew) and the
crafting/loot/recipe-unlock data are adapted from upstream data.

Registrations, resource IDs, translation keys and recipe book enum names use
Chocolate Flavored's namespace. Its Oven replaces the stove in the heat-source
tag. Vanilla foods represent the recipe book categories because Farmer's
Delight's food items are not part of this port. The client recipe-book option
retains its upstream default of enabled. No Farmer's Delight dependency is
required, and the two mods can be installed together.

## Minecraft 1.21.11 spear reference and assets

The spear rules are independently implemented for NeoForge 1.21.1 using the official Java 1.21.11 release as a reference. The three spear inventory/in-hand textures, spear hand-model display transforms and spear sound assets originate from Minecraft Java 1.21.11, copyright Mojang AB / Microsoft. They are not covered by the project's MIT license. No Minecraft classes or game jar are bundled.

Reference: https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-11
