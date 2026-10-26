# Villages and population

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. Standalone; no new recipes.

## Abandoned villages

All five vanilla village starting pools (plains, desert, savanna, snowy and taiga) select only their existing abandoned/zombie town-center templates. Their vanilla abandoned streets, buildings, processors, resident pools and relative layout weights remain in use. This affects newly generated villages; existing generated villages are not rewritten. Higher-priority datapacks can override the starting pools.

The ten vanilla abandoned resident templates already contain `PersistenceRequired: 1b`. This is retained: residents do not naturally despawn and their persistence survives saving and loading. This does not prevent killing them or vanilla removal in Peaceful difficulty. Naturally spawned zombie villagers retain normal despawning rules.

## Zombie villagers

Zombie villagers are removed from the current `minecraft:burn_in_daylight` entity-type tag using NeoForge's additive tag-removal support; they do not ignite from daylight. Other fire and lava mechanics remain vanilla.

Outside old growth pine taigas, the biome spawn modifier assigns zombie villagers exactly **1% of the combined normal-zombie and zombie-villager selection weight**, with normal zombies receiving the other 99%. It preserves their combined probability and existing group sizes, plus every other monster's relative probability, including husks and zombie horses. These are spawn-table selection weights, not a guarantee about the observed proportion after group spawning and despawning. The modifier runs once when biomes load and does not create zombie entries in biomes that lack the pair.

Old growth pine taigas keep their vanilla zombie weight of 100 and zombie-villager weight of 25. This modifies natural biome spawning, not infected villagers, spawn eggs, spawners or structure residents. The modifier can be disabled by overriding `chocolateflavored:neoforge/biome_modifier/zombie_villager_spawns.json` with a `neoforge:none` modifier.

## Villagers and golems

Villagers cannot summon iron golems through panic or gossip. Player-built iron golems and golems placed by pillager-outpost templates continue to work.

Successful villager breeding gives both parents a **42,000-tick (35-minute)** cooldown. New baby villagers start at **-42,000 ticks (35 minutes)** and mature at zero. The longer initial age applies to bred babies and other newly initialized villager babies; animal growth remains vanilla. Age progression, saving/loading, and vanilla age-lock mechanics are retained. Previously saved villagers retain their remaining age/cooldown. These durations assume 20 TPS and normally ticking entities.

## Verification

The development server verifies all five loaded starting pools contain only abandoned starts, places all ten abandoned resident templates, checks persistent entities and entity save/load, and tests actual daytime exposure alongside a normal zombie that must ignite. It verifies the loaded biome weights and pine exception, other-monster probability preservation, actual villager breeding results and ticking age boundaries, failed villager golem summons under otherwise eligible conditions, actual pumpkin/iron-block construction and the vanilla outpost cage template's golem. Client startup runs separately. Full-world exploration, an hour of live play, and other mods' world-generation/seasonal integrations are not automated compatibility tests.
