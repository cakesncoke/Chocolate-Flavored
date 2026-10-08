# Spears, rose gold and chocolate cake

## Spear backport

Reference: Mojang Java 1.21.11 release, official server mappings/game data and client assets. The original rules are implemented using NeoForge 1.21.1 networking, item use and rendering APIs; newer engine components are not available in this version.

Only iron, diamond and netherite spears are added. Player-controlled jab and charge attacks are authoritative on the server. Client messages request a jab without supplying a target, damage, velocity or reach. One ray can hit multiple enemies; walls block it, and the player cannot mine blocks with a spear. Targets closer than the two-block ray start are excluded. Survival range is 4.5 blocks; creative range is 6.5. Hitbox margin is 0.125.

| Spear | Jab damage | Jab cooldown | Charge warmup | Charge damage multiplier | Durability |
| --- | ---: | ---: | ---: | ---: | ---: |
| Iron | 3 | 19 ticks | 12 ticks | 0.95 | 375 |
| Diamond | 4 | 21 ticks | 10 ticks | 1.075 | 3122 |
| Netherite | 5 | 23 ticks | 8 ticks | 1.2 | 6093 |

Durability follows this mod's previous material multipliers rather than reverting to unmodified vanilla values. Repair uses the corresponding vanilla material, and netherite is fire resistant. Each successful impact costs one durability. Jab damage receives normal weapon enchantment effects and has no sword sweep.

Charge compares attacker and target motion projected along the view direction, in blocks/second. Damage is the player's base attack damage plus `floor(max(0, relative forward speed) * material multiplier)`. Modern engaged/tired/disengaged timing and own-speed/relative-speed thresholds are preserved. Contact cooldown is ten ticks per entity, including contacts before reaching speed thresholds. Server player motion is sampled from validated positions; teleports do not become charge velocity. Damage uses a dedicated damage type so vanilla's automatic knockback cannot leak into the disengaged phase. Full movement and sprinting are retained while charging through a client-only mixin.

Lunge I–III is a normal data-driven enchantment. Jab adds horizontal impulse of 0.458 × level, costs one durability and 4 × level exhaustion, and requires at least six food points. It does not activate while riding, in water or using elytra. Sword enchantments are supported except Sweeping Edge. Enchantments, recipes and tags are available to normal datapacks/KubeJS.

Iron/diamond spear crafting uses a material at the top right and two diagonal sticks. Netherite uses the vanilla netherite smithing template and ingot with a diamond spear. This backport adds player weapons; it does not add modern mounts, natural spear mob equipment/loot generation or modern mob spear AI. Hand sprites, charge poses and recoil are adapted to the 1.21.1 renderer rather than the newer animation engine. Visual playtesting remains useful after the automated startup check.

## Rose gold

Five tools: sword, pickaxe, axe, shovel and hoe. Each has 3122 durability, matching the mod's diamond tools. Compared with iron, mining speed rises from 6 to 7, tier attack bonus from 2 to 3, attack speed by 0.1, and enchantability from 14 to 15. Mining level remains iron, so obsidian still requires a diamond/netherite pickaxe. Gold ingots repair the tools.

Use the supplied Rose Gold Upgrade Smithing Template to upgrade an iron tool with a gold ingot. Vanilla smithing keeps accumulated damage, names and enchantments. Craft the template using `CGC / GPG / CGC`, where C is copper ingot, G is gold ingot and P is paper. Recipes are ordinary JSON and can be changed later.

## Chocolate cake

Uses the supplied cake item and all four block textures. The block inherits vanilla CakeBlock behavior, geometry, seven bites, two food points and 0.4 saturation per slice, comparator values 14 down to 2, support requirements and empty cake loot. The cake item stacks to one and is placed rather than eaten in the inventory.

All seventeen candles can be placed on an uneaten cake, lit and extinguished. Eating a candle cake returns the candle and leaves six chocolate bites. Separate chocolate candle blocks avoid modifying Minecraft's vanilla candle-to-cake lookup.

Craft with `MMM / SEC / WWW`: milk buckets, sugar, egg, cocoa beans and wheat. Empty milk buckets are returned just as in vanilla crafting.

## Checks

Dedicated-server tests exercise piercing jab/reach/cooldowns, walls, charge warmup/relative velocity/no-knockback phase, Lunge, smithing, all candle variants, seven cake bites/comparator/food values and milk bucket returns, alongside the existing regression suite. An Xvfb client startup check forces the movement mixin to load and verifies new item/cake models use the intended artwork. Run `./gradlew runClient` for in-world visual playtesting; `./gradlew runClient -PclientSmokeTest` performs the startup check and closes the client automatically.
