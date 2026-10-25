# Intrinsic armor trim abilities (Minecraft 26.1.2)

The pattern, not the armor or trim material, determines an ability. Each of the four humanoid equipment slots has an independent ability. Full sets require four matching pattern registry IDs; Wayfinder, Raiser, Shaper and Host are four distinct sets. Mixed vanilla/custom armor and mixed trim materials work through the native `TRIM` and `EQUIPPABLE` components. Gold trim additionally satisfies Piglin safe-armor checks without changing provocation or Brute hostility.

The complete requested behavior is recorded in [armor-trims-spec.md](armor-trims-spec.md). This implementation adds no mandatory dependency, recipes, structures, or obtainable blocks/items. Its internal temporary basalt has no item or loot table.

## Implementation map

- `trim/TrimAbilities`: equipment identity, exact-set queries, configurable membership tags and stable attribute IDs.
- `TrimAttributes`: transient attributes, equipment/clone lifecycle, health/air clamping and cooldown persistence.
- `TrimCombat`, `TrimTriggers`: damage, target-specific bonuses, kills, resurrection, cooldowns, lightning, wind bursts and triggered highlights.
- `TrimMining`: bounded six-neighbor ore traversal through **the existing server game-mode break handler**. Initial and subsequent blocks count toward the 16-block limit; canceled blocks never bridge the traversal. The held tool must remain suitable. Stone and other non-ores never participate.
- `TrimLoot`: target-scoped native Looting calculations and independent Ocean Salvage generation.
- `TrimHighlights`: server-selected native block-display packets sent only to the activating player. No world block substitutions or server-side display entities. Eye scans occur only on activation; Dune scans the block-entity indexes of already-loaded nearby chunks once per second rather than scanning a block volume every tick.
- `ArchaeologyMemory` and `TrimArchaeologyMixin`: saved original loot identity, fresh second loot roll, and persistent spent world positions.
- `TrimBasalt`, `TrimTerrain`: 60-tick lava bridges, native lava-level restoration, and mounted camel attributes.
- `mixin/Trim*`: narrow hooks into the pinned native protection, movement, gliding, vibration, looting, fishing, archaeology, trading and item-use paths.
- `client/trim/TrimTooltips`: vanilla trim descriptions retained, concise per-slot descriptions, expanded Shift details, and template themes/full-set descriptions.

## Stacking and lifecycle

Damage and mining percentage bonuses are added within their respective calculation, then applied once to the incoming native value. Explicit full-set totals replace their per-piece value. Native armor, enchantments, Resistance, absorption and final protection caps still run. Intrinsic Feather Falling II contributes 6 protection points; Projectile/Blast Protection II contribute 4; Fire Protection IV contributes 8. Vanilla's combined protection cap remains 20 points. Blast knockback resistance and Fire Protection's burning-time reduction also follow the native enchantment attributes and their native limits.

Swift Sneak adds 0.15 to the native sneaking-speed attribute, whose maximum is 1. Aqua Affinity supplies the native fivefold underwater-mining multiplier only while equivalent native underwater mining performance is absent. It does not multiply actual Aqua Affinity again. Maximum-health modifiers are transient and use one stable ID; repeated updates do not duplicate them. Loading reconstructs Ward's maximum before restoring the saved health value, because native loading reads health before equipment.

Passive Night Vision, Dolphin's Grace and Spire levitation use equipment-dependent native effect queries, not permanent potion effects. They have no saved effect, particles, HUD icon or residual duration after removal. Vex's temporary speed uses an attribute; its activation/cooldown is player-bound. Native resurrection effects and temporary mob highlighting remain visible as appropriate.

Cooldown deadlines use the server's monotonic Overworld game time, survive logout/reload and cloning, and are independent of armor items. Overlay-only native cooldown groups display remaining time without making armor unusable. They do not reset when a piece is removed, replaced or re-equipped. Gameplay durations are simulation ticks: the existing accelerated-sleep system advances them with the rest of the world.

## Explicit behavior decisions

- Actual held death-protection items are tried first; Vex runs only if native protection did not activate. Each Vex payment is `ceil(max durability / 3)` and bypasses Unbreaking. All four payments are validated before any is charged. An exact final payment breaks that piece. Unbreakable/non-damageable armor cannot pay. Damage bypassing invulnerability (including the void) cannot activate Vex.
- Spire gliding uses native glider selection and normal gliding wear. Full-set levitation has **no durability cost, duration limit or cooldown**. Fireworks cannot accelerate Spire gliding. Crouching switches from gliding to controlled levitation through the native movement path.
- Speed and damage effects never confer entity attack reach. Trail reach modifies block interaction only.
- Native target visibility is reduced; existing aggression, line-of-sight rules and the Warden's special detection remain intact. Silence suppresses movement/landing vibration events, not unrelated interactions.
- Sentry's full-set highlighting uses native temporary Glowing on hostile mobs (visible to nearby clients, as ordinary Glowing is). Eye/Dune block reveals are private to their activating player, removed on expiry, equipment invalidation, death, dimension change or logout.
- Snout double bartering records the player who hands over or throws the ingot and checks that player's current full set when payment resolves. Unattributed dispenser/automation payments do not gain an arbitrary nearby player's bonus.
- Static Charge counts at most one successful melee attack per simulation tick, preventing sweep/multiple callbacks from incrementing it repeatedly. The non-storm discharge uses a separate magic damage type that bypasses the just-applied hit cooldown; it cannot recursively count as a melee attack. Native lightning retains its ordinary world interactions.
- Vein mining connects **the same block registry ID** through six faces. It does not merge different overworld/deepslate ore blocks into one vein. Existing copper-pickaxe restrictions, hammer/steel-pickaxe conversions, ore-pile loot, XP, enchantments and protection callbacks remain in control of each break.
- Archaeology's one-extra-extraction limit is bound to dimension + block position and never resets through replacement, breaking, or reload. The preservation roll occurs only at a real completed brushing action; the second extraction rerolls the original table rather than cloning the first item. Already-unpacked archaeological loot from before this feature was installed cannot reconstruct a lost table identity and therefore does not gain a speculative duplicate.
- Temporary basalt displaces only existing lava with empty space above; it never replaces a solid block. Its original lava level is saved in block state and its scheduled restoration survives chunk saving. It cannot be mined, pushed or harvested. Placement on/against it is rejected so new attachments cannot be destroyed on restoration. The three-second lifetime is measured in loaded simulation ticks, like vanilla scheduled block ticks.

## Datapack integration

- `chocolateflavored:gameplay/ocean_salvage`: independent fishing table, one roll on 10% of legitimate catches. Default weights: iron nugget 40, gold nugget 30, nautilus shell 8, map 8, emerald 5, name tag 5, saddle 3, heart of the sea 1. Ordinary fishing loot is untouched.
- `c:ores`: eligible ore membership, with the held tool's correct-harvest rules additionally enforced.
- `chocolateflavored:trim/nether_terrain`, `desert_terrain`, `stone_landings`: terrain membership.
- `chocolateflavored:trim/bolt_weapons`, `bolt_tools`: copper/rose-gold equipment membership.
- `chocolateflavored:trim/aquatic`, `nether_hostiles`, `camels`: entity membership, including the relevant existing custom mobs.

## Verification scope

Development-only `TrimVerification` exercises all 18 patterns' slot/set matching, mixed materials, Ward idempotence/removal/save-load, Trail reach isolation, native protection stacking, Tide air, Spire native glider selection and crouch levitation queries, Dune immunities, native fishing luck, gold-trim neutrality, exact/atomic Vex payments and void exclusion, cooldown swapping and overlay-only behavior, detection/Warden exception, crossbow loading, leaf collision, archaeology reload/second extraction, and 16-block/protected native ore conversion. `TrimClientVerification` renders 72 trimmed pieces across four armor materials and verifies native and intrinsic tooltip coexistence.

The complete existing dedicated-server and optional KubeJS suites also run, followed by a real development-client startup/rendering check. These checks are not a substitute for two-player latency testing or a server-load benchmark. Mounted leaf traversal, underwater walking/jumping, wind-charge propulsion, private through-wall outlines and all combinations of third-party movement/loot/protection mods still need hands-on multiplayer playtesting; no blanket third-party compatibility or performance claim is made.
