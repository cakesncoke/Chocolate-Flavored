# Wandering traders

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Standalone.

## Five-day fallback

Ordinary vanilla trader spawning continues with the existing one-day attempt interval and two-day visit duration. If **five Minecraft calendar days pass without a successfully added wandering trader**, an additional guaranteed attempt bypasses the native random chance gates. This fallback uses vanilla's player/meeting-point selection, placement, obstruction and biome checks, and spawns **one trader without llamas**. Ordinary spawns retain their two llama companions.

The five days use the saved Overworld solar clock: 120,000 logical clock ticks, or **175 minutes** with the mod's 35-minute cycle at normal speed. Accelerated sleeping advances the same calendar. Newly created traders, including manual summons and eggs, reset the history; loading or moving an existing trader between dimensions does not. Only a confirmed entity addition counts, so canceled/failed additions and empty-player attempts cannot postpone the fallback.

The `spawn_wandering_traders` game rule remains authoritative. When overdue, attempts wait for an online player and a valid vanilla spawn position. Failed placement retries at most once per 1,200 simulation ticks (one minute at ordinary speed); the game does not force a trader into an unsafe or prohibited location. The fallback timer resets only after a real trader is added.

On the first use in a world with no saved history, a new five-day waiting period begins. A backwards time command that moves before the recorded spawn time restarts that period.

## Keeping a trader

The despawn countdown freezes while a trader is riding **any boat/chest boat or minecart**, has a custom name, or has vanilla persistence enabled. Actual name tags set that persistence flag. A trader configured with no positive despawn countdown is also inherently permanent and holds the same spawn lock.

Once a trader has been kept, **all automatic vanilla and fallback trader attempts stop until that trader dies or is permanently removed/despawns**. Multiple kept traders each hold the lock; all must leave before spawning resumes. Manual commands and spawn eggs remain usable.

An unnamed trader released from a vehicle resumes its existing countdown. Its spawn lock remains until it actually dies or despawns. Moving between dimensions, changing chunk visibility, unloading, or restarting the server do not release a saved lock. The tracker stores UUIDs globally rather than forcing trader chunks to stay loaded. Death releases the lock without waiting for the corpse animation to end.

Saved history and kept UUIDs are stored in `chocolateflavored:wandering_traders` saved data. Traders carry an internal recorded-spawn flag so reloading them does not look like a new spawn. Existing traders from before installation are recognized when their chunks first load; previously unloaded traders in an upgraded world need to be loaded once to enter the new index. External deletion of entity files is outside normal removal callbacks and can leave an orphaned lock.

## Verification

The dedicated-server checks exercise the exact five-day boundary, spawn receipts and reload history, retries, disabled spawning and no-player handling, actual native fallback placement without llamas, the ordinary path with two llamas, real name-tag interaction, boat/minecart countdown protection, multiple kept traders, vehicle detachment during unload, saved-state restoration, actual expiration, and death/removal release. Existing build, client startup, timing/sleep and optional KubeJS checks remain required. No seasonal mechanics are modified.
