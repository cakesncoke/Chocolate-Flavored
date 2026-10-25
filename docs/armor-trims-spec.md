# Implement Intrinsic Armor Trim Abilities

Implement the following armor trim ability system in my Minecraft NeoForge mod. First inspect the existing project structure, Minecraft version, armor trim implementation, and relevant custom mechanics. Integrate the system with existing code rather than duplicating functionality.

Every armor trim pattern grants an intrinsic ability to each corresponding armor piece, plus an additional bonus when all four equipped pieces share the same trim pattern.

## 1. General Rules

- Effects are determined by the **trim pattern**, not armor material or trim material.
- Different armor materials and trim materials can be mixed within a full set.
- Players can freely combine individual pieces from different trim patterns, retaining their individual abilities.
- Full-set bonuses activate only when all four equipped armor pieces have the same trim pattern.
- Individual bonuses remain active when wearing a full set. Where the full-set effect upgrades an individual bonus, use the specified total value rather than adding the full-set value again.
- All effects work on any valid armor material, including custom materials.
- Effects deactivate immediately when the corresponding armor is removed, replaced, or broken.
- Attribute modifiers must never duplicate or persist incorrectly through equipment changes, death, respawning, dimension travel, or world reloads.
- Enchantment-like intrinsic effects stack with actual enchantments, subject to explicitly defined effective-level limits and Minecraft's applicable protection caps.
- Avoid unintended additive or multiplicative stacking that bypasses damage-reduction limits.
- Trim abilities must not interfere with normal armor enchantments, attributes, durability, or existing modded mechanics.
- All effects must be server-authoritative and work correctly in multiplayer.

### Visual Feedback

- No persistent potion particles, status-effect icons, or special visual indicators for passive trim abilities.
- Preserve normal gameplay visuals for triggered abilities, including lightning, wind charges, resurrection, and temporary highlighting.
- Abilities with cooldowns should display appropriate cooldown overlays on equipped armor.
- Cooldowns must persist when armor is removed and re-equipped, preventing equipment-swapping exploits.
- Avoid unnecessary HUD elements.

### Gold Trim Material

- Any equipped armor piece using gold as its trim material counts as wearing gold armor for Piglin neutrality.
- Only one gold-trimmed piece is required.
- All armor materials qualify.
- Other trim materials have no special effects.
- Preserve normal Piglin provocation behavior.
- Piglin Brutes remain hostile regardless of gold trimming.

## 2. Individual Trim Abilities

### Wild — Wilderness Exploration and Mobility

- **Helmet:** Intrinsic Speed I.
- **Chestplate:** Reduces exhaustion from sprinting and jumping by 50%.
- **Leggings:** Allows stepping up one-block obstacles without jumping.
- **Boots:** Intrinsic Feather Falling II.
- **Full Set:** Players and their mounts can move horizontally through leaf blocks without collision. Landing on leaves preserves normal collision, allowing players to stand on them. Holding crouch allows descending through leaves. Ensure mounted traversal works without trapping riders or mounts.

### Trail Ruins — Construction and Building

Apply these abilities to all four Trail Ruins armor trim patterns: Wayfinder, Raiser, Shaper, and Host.

- **Helmet:** +1 block interaction reach.
- **Chestplate:** +10% block-breaking speed when using an appropriate tool.
- **Leggings:** Intrinsic Swift Sneak I.
- **Boots:** Completely negate fall damage from falls of five blocks or less.
- **Full Set:** Additional +2 block interaction reach, totaling +3 blocks with the helmet.

Reach bonuses affect block breaking and placement, never entity attack reach. Each Trail Ruins pattern counts as its own full set; different patterns cannot be mixed to activate the bonus.

### Ward — Maximum Health

- **Helmet:** +1 heart.
- **Chestplate:** +1 heart.
- **Leggings:** +1 heart.
- **Boots:** +1 heart.
- **Full Set:** +1 additional heart.

A complete set provides five additional hearts, increasing base maximum health from 20 to 30 HP.

Use attribute modifiers rather than continuously applying healing. Removing armor reduces maximum health and caps current health appropriately without inflicting additional damage. Never duplicate health modifiers.

### Vex — Illager Magic and Resurrection

- **Helmet:** Reduces Illager detection range by 50%.
- **Chestplate:** +25% damage against Illagers.
- **Leggings:** Intrinsic Looting II against Illagers.
- **Boots:** Grants Speed II for five seconds when health drops below five hearts, with a 30-second cooldown.
- **Full Set:** Prevents fatal damage and activates the equivalent of a Totem of Undying, consuming one-third of the maximum durability of each equipped armor piece. Villagers increase their trading prices by 25% while the complete set is worn.

Resurrection requirements:

- Every armor piece must have sufficient remaining durability to pay its individual cost.
- Durability consumption bypasses Unbreaking.
- Resurrection cannot activate if any piece cannot pay.
- Apply all costs atomically.
- Exclude void damage.
- Preserve normal Totem of Undying effects and prevent duplicate resurrection triggers.
- Define deterministic behavior when the player also possesses an actual Totem of Undying.

Villager price increases must be temporary, not permanently modify trades, and must not accumulate through repeated interactions.

### Tide — Underwater Exploration and Combat

- **Helmet:** Intrinsic Aqua Affinity.
- **Chestplate:** Adds 60 seconds of underwater breathing capacity.
- **Leggings:** Grants Dolphin's Grace while submerged.
- **Boots:** Allows normal land-like walking, sprinting, and jumping on underwater surfaces without preventing intentional swimming.
- **Full Set:** Adds another 60 seconds of breathing capacity, totaling 120 additional seconds, and increases underwater melee damage by 50%.

Additional breathing capacity must function correctly with normal air replenishment, Respiration, Water Breathing, and equipment changes. Avoid repeatedly resetting the player's air supply.

### Spire — Flight and Levitation

- **Helmet:** Reduces Elytra-style collision damage by 75%.
- **Chestplate:** Enables Elytra-style gliding while retaining normal chestplate protection. Fireworks cannot boost this flight.
- **Leggings:** Improves horizontal movement control by 50% while levitating.
- **Boots:** Reduces fall damage by 50%.
- **Full Set:** Holding crouch while airborne grants Levitation I, allowing indefinite upward movement until crouch is released.

**Important:** Do not introduce duration limits, cooldowns, or durability costs for the full-set levitation ability. Infinite but relatively slow upward movement is intentional because Spire is an endgame trim.

Ensure gliding and levitation interact correctly, do not accidentally enable firework boosting, and remain controllable without causing movement desynchronization.

### Snout — Piglins, Gold, and Bartering

- **Helmet:** Reduces Piglin and Piglin Brute detection range by 50%.
- **Chestplate:** Reduces damage from Piglins and Piglin Brutes by 30%.
- **Leggings:** Mobs killed by the player have a 10% chance to drop one additional gold nugget.
- **Boots:** +15% movement speed in the Nether.
- **Full Set:** Piglins remain neutral unless provoked. Bartering produces two independent rolls of the normal bartering loot table, awarding both results.

Piglin Brutes remain hostile. Gold nugget drops require player-attributed kills and should not activate from environmental deaths or automated farms without player involvement.

Preserve ordinary Piglin aggression triggers and bartering behavior.

### Silence — Stealth and Hostile Combat

- **Helmet:** Reduces hostile mob detection range by 15%.
- **Chestplate:** +10% melee damage against mobs.
- **Leggings:** 10% chance to grant Regeneration I for five seconds after killing a hostile mob.
- **Boots:** Prevents walking, sprinting, and jumping vibrations from activating sculk sensors and shriekers. Other vibrations remain detectable.
- **Full Set:** Upgrades detection reduction to 40% total, melee damage bonus to 20% total, and regeneration chance to 20%.

Regeneration requires a player-attributed hostile mob kill. Prevent farming exploits involving repeated death events.

The Warden is excluded from detection-range reduction. Its unique detection mechanics remain functional.

### Sentry — Ranged Combat

- **Helmet:** +15% damage from bows, crossbows, and thrown weapons.
- **Chestplate:** Intrinsic Projectile Protection II.
- **Leggings:** Reduces bow draw time and crossbow loading time by 20%.
- **Boots:** Removes movement-speed penalties while drawing bows or loading crossbows.
- **Full Set:** Upgrades projectile damage to 30% total. Crouching while holding a bow or crossbow highlights hostile mobs within 24 blocks for 10 seconds, with a 60-second cooldown.

Bow acceleration must reduce the time required to reach full power. Crossbow acceleration must affect loading time rather than projectile velocity.

Highlighting must not target players, passive mobs, or unrelated entities.

### Rib — Nether Terrain, Lava, and Wither

- **Helmet:** Reduces hostile Nether mob detection range by 25%.
- **Chestplate:** Prevents lava damage for five seconds after entering lava, with a 30-second cooldown.
- **Leggings:** +15% movement speed on Nether terrain, including netherrack, soul sand, soul soil, basalt, blackstone, and Nether bricks.
- **Boots:** Generates temporary basalt beneath the player when walking across lava and prevents magma block damage.
- **Full Set:** Melee attacks have a 30% chance to inflict Wither II for five seconds. Grants intrinsic Fire Protection IV.

Temporary basalt:

- Disappears after approximately three seconds.
- Never replaces existing solid blocks.
- Must not permanently alter terrain.
- Must not destroy supported blocks or structures when disappearing.
- Must avoid excessive block updates and server lag.
- Must not create permanent basalt-generation exploits.

Lava protection cannot be reset by briefly exiting and re-entering lava during its cooldown.

### Flow — Wind and Aerial Mobility

- **Helmet:** Negates wind-charge damage without preventing wind-charge knockback.
- **Chestplate:** Intrinsic Blast Protection II.
- **Leggings:** +10% movement speed while outdoors with sky exposure in the Overworld.
- **Boots:** Reduces fall damage by 25%.
- **Full Set:** Crouching while airborne releases a wind charge beneath the player, launching them upward.

The wind-charge ability:

- Activates once per jump.
- Resets upon landing on solid ground.
- Does not consume wind-charge items.
- Preserves normal wind-charge movement physics.
- Cannot repeatedly activate during uninterrupted flight or gliding.
- Must not damage the player through their own wind charge.

### Eye — Mining and Ore Detection

- **Helmet:** Grants Night Vision below Y=0.
- **Chestplate:** Enables vein mining exclusively for connected ore blocks, up to 16 blocks per activation.
- **Leggings:** Increases dropped-item pickup radius by two blocks.
- **Boots:** Reduces fall damage by 50% when landing on stone, deepslate, or their variants.
- **Full Set:** Crouching while holding a pickaxe highlights ore blocks within a 16-block radius, including ores hidden behind terrain, for 10 seconds. Cooldown: 10 minutes.

Vein mining requirements:

- Applies only to ores, never stone, logs, dirt, or other ordinary blocks.
- Mines only connected blocks of the same ore type.
- Maximum 16 blocks per activation, including the initial block.
- Requires a suitable tool and correct harvest tier for each block.
- Consumes normal tool durability per block.
- Respects Unbreaking and other relevant enchantments.
- Preserves all custom ore-pile drops, ore transformations, mining restrictions, and loot mechanics already implemented in the mod.
- Must not bypass progression restrictions or duplicate drops.
- Must not mine protected or unbreakable blocks.
- Must not load additional chunks unnecessarily.

Ore highlighting only reveals ores harvestable with the held pickaxe. Use temporary outlines or equivalent rendering that can reveal hidden ore locations without permanently changing blocks.

### Dune — Desert Exploration and Archaeology

- **Helmet:** Immunity to Blindness and Darkness.
- **Chestplate:** Intrinsic Projectile Protection II.
- **Leggings:** +20% movement speed on sand, red sand, and gravel.
- **Boots:** +25% movement speed for camels and camel husks while mounted.
- **Full Set:** Holding a brush highlights suspicious sand and suspicious gravel within 16 blocks. Additionally, successfully brushing a suspicious block has a 20% chance to preserve it for one additional archaeological loot extraction.

Archaeological preservation requirements:

- Each suspicious block can receive at most one additional loot extraction.
- The preservation roll occurs only after a legitimate completed brushing action.
- The second extraction uses the block's appropriate archaeology loot table.
- Never duplicate the same generated item directly.
- Preserve the block's archaeological context and loot-table identity.
- Track whether preservation has already occurred to prevent repeated exploitation.
- Breaking, replacing, or reloading the block must not reset its preservation eligibility.
- Respect existing archaeology mechanics and custom suspicious blocks where compatible.

Do not add sandwalking or quicksand immunity.

### Coast — Fishing, Boats, and Ocean Salvage

- **Helmet:** Reduces Drowned detection range by 50%.
- **Chestplate:** Intrinsic Luck of the Sea II while fishing.
- **Leggings:** +20% boat movement speed.
- **Boots:** Intrinsic Looting II against aquatic mobs, including Drowned and Guardians.
- **Full Set:** Every successful fishing catch has a 10% chance to award one additional item from a new, dedicated Ocean Salvage loot table.

Ocean Salvage requirements:

- Create a separate data-driven fishing loot table.
- Potential rewards include nautilus shells, iron nuggets, gold nuggets, emeralds, maps, and uncommon fishing treasures.
- Assign sensible weights, making valuable rewards appropriately rare.
- Roll the salvage table independently of ordinary fishing loot.
- Do not replace or modify the original fishing result.
- Do not trigger additional salvage rolls from the extra reward itself.
- Only legitimate player fishing catches qualify.
- Preserve normal fishing enchantments and compatibility with fishing-related mechanics.
- Allow the salvage loot table to be modified through datapacks.

Remove Coast's original suspicious-block highlighting ability entirely.

### Bolt — Copper, Rose Gold, and Lightning

- **Helmet:** Immunity to lightning damage.
- **Chestplate:** +20% damage with copper and rose gold weapons.
- **Leggings:** Reduces damage received from copper and rose gold weapons by 50%.
- **Boots:** +20% mining speed with copper and rose gold tools.
- **Full Set:** Grants both of the following effects:

**Static Charge**
- Every fifth successful melee attack releases an electrical discharge dealing two additional points of damage to the target.
- During thunderstorms, this discharge instead summons a lightning strike on the target.

**Storm Empowerment**
- During thunderstorms, copper and rose gold weapons gain an additional 25% attack-speed bonus.
- Copper and rose gold tools gain an additional 25% mining-speed bonus.

Static Charge requirements:

- Count successful player-attributed melee hits, not missed attacks.
- Prevent the discharge from recursively triggering itself.
- Prevent duplicate activations from multi-hit events.
- Reset the attack counter when the complete set is removed.
- Lightning must not damage the player wearing the complete Bolt set.
- Preserve normal lightning interactions where appropriate.
- Storm bonuses deactivate immediately when the thunderstorm ends.

## 3. Enchantment and Effect Stacking

Intrinsic enchantment-like abilities must stack with actual enchantments without allowing uncontrolled scaling.

- Respect vanilla protection and damage-reduction caps.
- Cap effective Swift Sneak movement speed at normal walking speed.
- Cap Feather Falling's effective protection at the normal fall-damage protection limit.
- Apply Looting bonuses only to their specified targets.
- Intrinsic Luck of the Sea contributes to the fishing calculation without duplicating catches.
- Aqua Affinity functions normally alongside actual Aqua Affinity.
- Attribute bonuses must have deterministic stacking behavior.
- Avoid duplicate damage calculations, especially when combining damage bonuses with enchantments.
- Percentage-based bonuses should use consistent, documented calculation rules.

## 4. Tooltips and Descriptions

Add informative tooltips without excessive clutter.

**Armor trim smithing templates:**
- Describe the trim's general theme.
- Describe its full-set bonus.
- For Trail Ruins patterns, explain their shared ability theme.

**Trimmed armor pieces:**
- Display the trim pattern and material normally.
- Add a concise description of the individual intrinsic ability.
- Holding Shift reveals detailed mechanics, numerical values, activation conditions, cooldowns, and the full-set bonus where relevant.

Descriptions should accurately reflect implemented behavior and update appropriately for custom armor.

## 5. Implementation and Compatibility Requirements

- Inspect and reuse existing registries, event handlers, item components, tags, loot tables, and utility classes.
- Use the appropriate NeoForge APIs for the project's actual Minecraft version.
- Prefer data-driven configuration, tags, and loot tables over hardcoded block or entity lists.
- Use persistent player data where necessary for cooldowns and attack counters.
- Avoid applying permanent potion effects when attribute modifiers or direct mechanics are more appropriate.
- Prevent effects from persisting after equipment removal.
- Ensure custom tools, weapons, armor, mounts, ores, and blocks participate appropriately.
- Preserve the mod's existing mining progression and custom harvesting mechanics.
- Avoid unnecessary per-tick scans, particularly for mob detection, ore highlighting, and equipment checks.
- Ensure rendering and collision mechanics remain stable in multiplayer.
- Handle player death, equipment breakage, dimension changes, disconnections, and world reloads.
- Ensure all new loot tables and other data-driven resources can be overridden through datapacks.
- Do not introduce mandatory dependencies on other mods.

## 6. Validation

After implementation:

1. Verify every individual trim ability independently.
2. Verify every full-set ability.
3. Verify mixed armor materials and mixed trim materials.
4. Verify that different trim patterns do not incorrectly activate full-set bonuses.
5. Test interactions with enchantments, durability, and equipment swapping.
6. Test cooldown persistence and prevention of repeated activation exploits.
7. Test resurrection, levitation, gliding, wind charges, and lightning interactions.
8. Test vein mining against existing custom ore and tool mechanics.
9. Test archaeological preservation and Ocean Salvage loot generation.
10. Test Piglin neutrality from gold trims.
11. Verify multiplayer synchronization and server performance.
12. Build the project and resolve compilation errors.

Preserve existing unrelated functionality.

Do not silently omit features or substitute simpler mechanics for the specified behavior. If a mechanic is technically problematic, explain the issue and implement the closest reliable equivalent, documenting any deviation.

When finished, provide a concise summary of implemented features, relevant files changed, validation performed, and any remaining limitations.