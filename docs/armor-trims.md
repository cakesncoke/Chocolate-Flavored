# Intrinsic armor trim abilities (Minecraft 26.1.2)

The pattern, not the armor or trim material, determines an ability. Each of the four humanoid equipment slots has an independent ability. Full sets require four matching pattern registry IDs; Wayfinder, Raiser, Shaper and Host are four distinct sets. Mixed vanilla/custom armor and mixed trim materials work through the native `TRIM` and `EQUIPPABLE` components. Gold trim additionally satisfies Piglin safe-armor checks without changing provocation or Brute hostility.

The original specification (superseded by the refinements below for Rib, Tide, Eye and Bolt) is recorded in [armor-trims-spec.md](armor-trims-spec.md). This implementation adds no mandatory dependency, recipes, structures, or obtainable blocks/items. Its internal temporary basalt has no item or loot table.

## Implementation map

- `trim/TrimAbilities`: equipment identity, exact-set queries, configurable membership tags and stable attribute IDs.
- `TrimAttributes`: transient attributes, equipment/clone lifecycle, health/air clamping and cooldown persistence.
- `TrimCombat`, `TrimTriggers`: damage, target-specific bonuses, kills, resurrection, cooldowns, lightning, wind bursts and triggered highlights.
- `TrimMining`: bounded six-neighbor ore traversal through **the existing server game-mode break handler**. Initial and subsequent blocks count toward the 16-block limit; canceled blocks never bridge the traversal. The held tool must remain suitable. Stone and other non-ores never participate.
- `TrimLoot`: target-scoped native Looting calculations and independent Ocean Salvage generation.
- `TrimHighlights`: server-selected, textureless Eye border geometry sent only to the activating player; Dune uses the same emissive border renderer for suspicious sand and gravel. No world block substitutions or display entities. Eye scans occur only on activation; Dune scans the block-entity indexes of already-loaded nearby chunks once per second rather than scanning a block volume every tick.
- `ArchaeologyMemory` and `TrimArchaeologyMixin`: saved original loot identity, fresh second loot roll, and persistent spent world positions.
- `TrimBasalt`, `TrimTerrain`: Frost Walker-style aging lava crust, native lava-level restoration, and mounted camel attributes.
- `mixin/Trim*`: narrow hooks into the pinned native protection, movement, gliding, vibration, looting, fishing, archaeology, trading and item-use paths.
- `client/trim/TrimTooltips`: vanilla trim descriptions retained, concise per-slot descriptions, expanded Shift details, and template themes/full-set descriptions.

## Stacking and lifecycle

Damage and mining percentage bonuses are added within their respective calculation, then applied once to the incoming native value. Explicit full-set totals replace their per-piece value. Native armor, enchantments, Resistance, absorption and final protection caps still run. Intrinsic Feather Falling II contributes 6 protection points; Projectile/Blast Protection II contribute 4; Fire Protection IV contributes 8. Vanilla's combined protection cap remains 20 points. Blast knockback resistance and Fire Protection's burning-time reduction also follow the native enchantment attributes and their native limits.

Swift Sneak adds 0.15 to the native sneaking-speed attribute, whose maximum is 1. Aqua Affinity supplies the native fivefold underwater-mining multiplier only while equivalent native underwater mining performance is absent. It does not multiply actual Aqua Affinity again. Maximum-health modifiers are transient and use one stable ID; repeated updates do not duplicate them. Loading reconstructs Ward's maximum before restoring the saved health value, because native loading reads health before equipment.

Passive Nether-only Fire Resistance, Night Vision, Dolphin's Grace and Spire levitation use equipment-dependent native effect queries, not permanent potion effects. They have no saved effect, particles, HUD icon or residual duration after removal. Vex's temporary speed uses an attribute; its activation/cooldown is player-bound. Native resurrection effects and temporary mob highlighting remain visible as appropriate.

Cooldown deadlines use the server's monotonic Overworld game time, survive logout/reload and cloning, and are independent of armor items. Overlay-only native cooldown groups display remaining time without making armor unusable. They do not reset when a piece is removed, replaced or re-equipped. Gameplay durations are simulation ticks: the existing accelerated-sleep system advances them with the rest of the world.

## Explicit behavior decisions

- Actual held death-protection items are tried first; Vex runs only if native protection did not activate. Each Vex payment is `ceil(max durability / 3)` and bypasses Unbreaking. All four payments are validated before any is charged. An exact final payment breaks that piece. Unbreakable/non-damageable armor cannot pay. Damage bypassing invulnerability (including the void) cannot activate Vex.
- Spire gliding uses native glider selection and normal gliding wear. Full-set levitation has **no durability cost, duration limit or cooldown**. Fireworks cannot accelerate Spire gliding. Crouching switches from gliding to controlled levitation through the native movement path.
- Tide boots keep surface walking/sprinting while grounded underwater. Jumping off the bottom permits the normal sprint-to-swim transition; ordinary non-sprinting jumps retain the land-style launch.
- Speed and damage effects never confer entity attack reach. Trail reach modifies block interaction only.
- Native target visibility is reduced; existing aggression, line-of-sight rules and the Warden's special detection remain intact. Silence suppresses movement/landing vibration events, not unrelated interactions.
- Sentry's full-set highlighting uses native temporary Glowing on hostile mobs (visible to nearby clients, as ordinary Glowing is). Eye/Dune block reveals are private to their activating player, removed on expiry, equipment invalidation, death, dimension change or logout.
- Snout double bartering records the player who hands over or throws the ingot and checks that player's current full set when payment resolves. Unattributed dispenser/automation payments do not gain an arbitrary nearby player's bonus.
- Static Charge counts at most one successful melee attack per simulation tick, preventing sweep/multiple callbacks from incrementing it repeatedly. The non-storm discharge uses a separate magic damage type that bypasses the just-applied hit cooldown; it cannot recursively count as a melee attack. Trim-summoned lightning retains damage and native transformations but its fire-placement routine is disabled on every flash. Natural lightning is unchanged.
- Vein mining connects **the same block registry ID** through six faces. It does not merge different overworld/deepslate ore blocks into one vein. Existing copper-pickaxe restrictions, hammer/steel-pickaxe conversions, ore-pile loot, XP, enchantments and protection callbacks remain in control of each break.
- Archaeology's one-extra-extraction limit is bound to dimension + block position and never resets through replacement, breaking, or reload. The preservation roll occurs only at a real completed brushing action; the second extraction rerolls the original table rather than cloning the first item. Already-unpacked archaeological loot from before this feature was installed cannot reconstruct a lost table identity and therefore does not gain a speculative duplicate.
- Rib chestplates supply infinite Fire Resistance only in the Nether, with no activation cooldown or lingering potion effect. Full-set Fire Protection IV still applies normally.
- Rib boots use Frost Walker I's circular radius-three disk, ground/no-vehicle/source-fluid/air-above checks and movement activation. Weak basalt ages from 0 to 3, starts ticking after 60–120 ticks, then every 20–40 ticks with native-style neighbor decay. It ages without a sunlight requirement so Nether bridges remain temporary. Its saved lava level is restored on decay or survival mining; it has no drops or item and cannot be pushed. Standing still does not refresh the crust. Existing placement-on-crust protection remains.
- Tide chestplates immediately grant a transient native `oxygen_bonus` of +8, including with the full set. Native maximum air stays 300; Respiration stacks through the native attribute, with no growing air tank or refill on armor swapping. Floor movement uses native land travel. NeoForge's active `aiStep -> jumpInFluid` path launches the player off the bottom and clears ground contact before travel, so the same tick resumes fluid movement; every off-floor state uses normal fluid movement, including when the player has not entered the swimming pose.
- Eye's full-set activation has a 2-minute (2400-tick) cooldown and a 30-second reveal. Only twelve border edges per ore block are drawn with an emissive bright core and translucent glow layers, with no texture, filled face or duplicate block model. Server selection retains the 16-block radius, loaded-chunk and correct-tool checks.
- Wild's leaf collision rule applies equally to ordinary movement and server movement validation, including the old-foot-position support test. Native validation uses a placement-marked collision context; a dedicated marker distinguishes it from genuine block placement. Leaf-top walking and crouching through leaves remain available.

## Datapack integration

- `chocolateflavored:gameplay/ocean_salvage`: independent fishing table, one roll on 10% of legitimate catches. Default weights: iron nugget 40, gold nugget 30, nautilus shell 8, map 8, emerald 5, name tag 5, saddle 3, heart of the sea 1. Ordinary fishing loot is untouched.
- `c:ores`: eligible ore membership, with the held tool's correct-harvest rules additionally enforced.
- `chocolateflavored:trim/nether_terrain`, `desert_terrain`, `stone_landings`: terrain membership.
- `chocolateflavored:trim/melee_attacks`: melee damage types, including 26.1 spears; custom weapon damage types can opt in.
- `chocolateflavored:trim/bolt_weapons`, `bolt_tools`: copper/rose-gold equipment membership.
- `chocolateflavored:trim/aquatic`, `nether_hostiles`, `camels`: entity membership, including the relevant existing custom mobs.

## Verification scope

Development-only `TrimVerification` exercises all 18 patterns' slot/set matching, mixed materials, Ward idempotence/removal/save-load, Trail reach isolation, native protection stacking, Tide oxygen, Spire native glider selection and crouch levitation queries, Dune immunities, native fishing luck, gold-trim neutrality, exact/atomic Vex payments and void exclusion, cooldown swapping and overlay-only behavior, detection/Warden exception, crossbow loading, leaf collision, archaeology reload/second extraction, and 16-block/protected native ore conversion. `TrimClientVerification` renders 72 trimmed pieces across four armor materials and verifies native and intrinsic tooltip coexistence.

The complete existing dedicated-server and optional KubeJS suites also run, followed by a real development-client startup/rendering check. These checks are not a substitute for two-player latency testing or a server-load benchmark. Two-player latency behavior and combinations of third-party movement/loot/protection mods still need hands-on multiplayer playtesting; no blanket third-party compatibility or performance claim is made.


`TrimRefinementVerification` additionally exercises the native movement-packet collision validator, actual Nether lava damage, immediate oxygen attributes, airborne vanilla fluid motion, floor jumping, weak-basalt source/radius/aging/mining restoration, natural-versus-trim lightning ignition, and the Eye activation cooldown. The client fixture sends a real Eye payload and renders exposed and occluded ore borders in-world.

Dune shares Eye’s bright core, layered glow and through-wall border geometry, preserving suspicious-block textures. Its existing loaded-chunk scan, 16-block range, brush requirement and once-per-second refresh remain unchanged. Dune packets expire after 22 ticks and are independent of Eye’s 30-second reveal.
