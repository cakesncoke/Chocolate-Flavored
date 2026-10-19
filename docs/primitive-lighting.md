# Primitive lighting

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. No external mod dependency or new recipes.

## Torches

`chocolateflavored:unlit_torch` and `chocolateflavored:lit_torch` support standing and wall placement, with separate matching wall block IDs. Unlit torches emit no light or flame/smoke particles. Lit torches use vanilla torch light level and particles.

A placed lit torch burns for 24,000 game ticks (20 minutes at 20 TPS). Its absolute game-time deadline is saved in a block entity. Server shutdown pauses game time; unloaded chunks do not reset deadlines, and an expired torch extinguishes when its chunk next ticks. Wall orientation is preserved. Burnout plays vanilla's fire-extinguish sound and changes the block to its unlit counterpart. Relighting starts a fresh 24,000-tick timer.

The timer applies to placed blocks, not inventory stacks. Breaking either torch drops an unlit torch, preventing pickup/replacement from refreshing a burning torch for free. Creative mode exposes both item variants. Vanilla torches are left unchanged.

## Fire starter

`chocolateflavored:fire_starter` has exactly two durability and a continuous 200-tick (10-second) use action. Hold use while aiming at an unlit mod torch or dry, unlit campfire. Both normal and soul campfires are supported.

The tool repeats use swings, high-pitched vanilla eating sounds, and item-chip particles. These are the primitive fire-plough cues found in [the original Better Than Wolves primitive fire starter](https://github.com/BetterThanWolves/BetterThanWolves/blob/master/Src/FCItemFireStarterPrimitive.java). The behavior is implemented using current APIs, without copying its Java implementation or requiring BTW. A client-only mixin applies vanilla's existing swing transform while the item is in its sustained use action; server swing updates also animate other players.

Only completing the entire hold lights the target and consumes one durability. Releasing use, changing the held stack, looking away, moving out of reach, changing dimensions, replacing the target, or losing permission cancels without ignition or wear. Interaction protection is checked again at completion. Successful ignition is deterministic; BTW's chance/exhaustion mechanics are not added.

## Lanterns and campfires

`chocolateflavored:unlit_lantern` supports standing, hanging and waterlogged placement using vanilla lantern geometry. Right-click a dry unlit lantern with flint and steel or a fire charge to convert it into a vanilla lit lantern, preserving its hanging state. Flint and steel loses one durability; fire charges consume one item, following vanilla creative-mode rules. Lit lanterns have no burnout timer. A waterlogged unlit lantern cannot be ignited.

Flint and steel and fire charges can also ignite the custom unlit torches. The fire starter is restricted to torches and campfires.

Both vanilla campfire variants are placed unlit. Their vanilla ignition, cooking, smoke, damage, waterlogging and extinguishing mechanics are retained. Once ignited, they burn indefinitely without added fuel or a burnout timer, and can still be deliberately extinguished through vanilla mechanics. World-generated or command-created campfire states are not globally rewritten.

## Verification

The development-only dedicated server suite tests campfire placement, pre-completion behavior, the full 200-tick hold, cancellation, two successful uses, saved deadlines, expiry, relighting, wall direction, lantern ignition, ignition-item consumption and waterlogging. Deadline tests use serialized block entities to exercise expiry without waiting 20 real minutes. The suite is excluded from the release JAR. Client startup is checked separately in CI; manual multiplayer animation/audio and long-duration live play remain separate checks.
