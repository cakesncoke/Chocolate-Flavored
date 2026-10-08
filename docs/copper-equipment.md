# Copper tools and nugget

The five tools and copper nugget use your supplied textures and match Java
1.21.9 Copper Age equipment behavior, with tool durability changed from 190 to
iron's 250. This is a backport to Minecraft 1.21.1; registry IDs use
`chocolateflavored:`.

| Item | Attack damage | Attacks/second | Durability |
| --- | ---: | ---: | ---: |
| Copper Sword | 5 | 1.6 | 250 |
| Copper Pickaxe | 3 | 1.2 | 250 |
| Copper Axe | 9 | 0.8 | 250 |
| Copper Shovel | 3.5 | 1.0 | 250 |
| Copper Hoe | 1 | 2.0 | 250 |

Values include the player's base attributes. Effective mining speed is 5 and
enchantability is 13. The pickaxe harvests the same ores as stone, including
iron and copper, but cannot harvest gold, diamond, redstone, emerald, obsidian,
or ancient debris. Higher durability does not change the mining tier.

Craft the usual tool patterns with copper ingots and sticks. Copper ingots
repair them in an anvil; repair combining and normal tool enchantments use
Minecraft's existing mechanics. The axe strips/scrapes/unwaxes, the shovel
makes paths, the hoe tills, and the sword has standard sword behavior. Equipment
does not oxidize.

One ingot crafts nine nuggets. Nine nuggets in a full crafting grid return one
ingot. Smelting any of the five tools, including damaged/enchanted ones, returns
one nugget in 200 ticks; blasting takes 100 ticks. Both award 0.1 XP. Nuggets
stack to 64 and appear in Ingredients. Tools appear in Tools & Utilities, with
the sword and axe also represented in Combat as appropriate.

Recipes and repair materials are data-pack editable. Defaults in
`chocolateflavored:copper_tool_materials` contain only `minecraft:copper_ingot`,
matching vanilla. Nuggets are also in `c:nuggets/copper` and `c:nuggets`.

Reference: [Mojang's Java 1.21.9 release notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-9).
Exact material statistics, attack modifiers, crafting patterns, recycling times
and XP were checked against Mojang's official Java 1.21.9 server distribution
and mappings. JSON formats were adapted to 1.21.1. Existing recipes and other
Minecraft equipment remain unchanged.

Four server GameTests check combat attributes and mining restrictions,
durability, copper repairs and enchantment eligibility, actual crafting and
nugget conversion, and damaged-tool smelting/blasting. Client model/texture
appearance and manual anvil/use interactions still require in-game playtesting.
