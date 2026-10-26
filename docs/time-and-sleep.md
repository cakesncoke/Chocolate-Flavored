# Day length and sleeping

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. No additional mod required.

## Day and night

At the normal 20 simulation ticks per second, a complete Overworld solar cycle takes **35 minutes**:

| Solar-clock phase | Simulation ticks | Normal duration | Native clock rate |
| --- | ---: | ---: | ---: |
| Day, 0–12,000 | 24,000 | 20 minutes | 0.5 |
| Night, 12,000–24,000 | 18,000 | 15 minutes | 2/3 |

Dusk and dawn retain their vanilla positions within these solar halves. Bed eligibility, monster burning and visual darkness retain their own vanilla transition points; they do not all switch at exactly sunset. Clock coordinates still run from 0 to 24,000 per day. This keeps existing commands, datapack timelines, moon phases and saved worlds aligned.

The implementation adjusts Minecraft's native Overworld world-clock rate and leaves simulation time advancing normally. The native saved data preserves fractional progress, and native packets synchronize the clock with clients. Other clocks are not modified. `/time set`, `/time add`, `/time pause`, `/time resume` and `advance_time` continue to work; the mod manages the Overworld's `/time rate` automatically each tick.

Sun, moon, stars, fog/light transitions, item clocks, daylight detectors, daylight burning, bee returns, villager activities, early-game patrol eligibility, creakings, eyeblossoms and turtle-egg/cat-gift time windows follow the same slower clock. No vanilla timeline is replaced, so datapacks may still change those tracks.

Raw simulation timers that represent days are adjusted separately:

- Phantom insomnia: three extended days, 126,000 ticks.
- Ordinary wandering trader attempts: one extended day, 42,000 ticks; visits last two days, 84,000 ticks. A separate [five-calendar-day fallback and kept-trader spawn lock](wandering-traders.md) now also apply. Previously saved countdowns finish their remaining time before using the new interval.
- Villager gossip decay: 42,000 ticks. The restock fallback window is extended proportionally to 21,000 ticks; calendar-day detection remains native.

Ordinary elapsed-time mechanics keep their existing normal-speed durations: furnaces, crops/random ticks, redstone, weather, effects, animal growth, and mod timers such as 35-minute torches, 5-second fire starters, 35-minute villager breeding cooldowns and 60-minute villager maturation. The full simulation accelerates them together during sleep.

## Sleeping

When the normal multiplayer `players_sleeping_percentage` threshold is met, bed entry starts a session targeting **twenty real seconds to complete the remaining night**, including the entire dawn transition up to solar-clock tick 0. There is no extra five-second entry delay. The rate is sized once from the remaining simulation work; it does not multiply repeatedly or reset when additional players enter bed.

The server's native tick scheduler runs the complete simulation faster. Loaded ticking entities, block entities, scheduled block/fluid ticks, random ticks and vanilla/mod processes in every dimension advance together. `random_tick_speed` remains unchanged, and the clock progresses naturally through every simulation tick. Unloaded chunks retain vanilla behavior.

| Bed entry | Remaining simulation ticks | Target server rate |
| --- | ---: | ---: |
| Sunset, clock 12,000 | 18,000 | 900 TPS |
| Midnight, clock 18,000 | 9,000 | 450 TPS |
| Near dawn, clock 23,300 | 1,050 | 52.5 TPS |

These rates are targets. If the server cannot process all the work fast enough, sleep takes longer than twenty seconds; the mod keeps simulating the world instead of discarding process ticks. If dawn would arrive in less than twenty seconds at the server's ordinary rate, it finishes sooner rather than slowing the world. Storm-time bed entry includes the remaining daytime and night in its tick budget.

Players remain asleep through the native early dawn wake point so the complete night reaches morning. A missing/broken bed, death, manual Leave Bed or loss of the multiplayer threshold still interrupts acceleration. Morning explicitly wakes participating sleepers and restores the saved server tick rate. Sleep does not forcibly clear weather. Spectators do not count, zero percent still requires a sleeper, and percentages above 100 disable the feature.

Only dimensions sharing the Overworld clock participate; standard Nether/End bed restrictions remain native. The simulation rate is server-wide, so awake players and other dimensions also accelerate. Clock pauses, `advance_time=false`, freeze/step and tick sprint take precedence. Custom `/tick rate` baselines are restored after sleep. A deliberate rate change during sleep updates the saved baseline; a backwards `/time` command interrupts the current sleep.

### Better Days reference

Behavior reference: [Wendall Cada (wendall911), Better Days 26.1](https://github.com/wendall911/BetterDays/tree/995baf3e89f31a7ae3b4bd2005e5084d2b7fb76b), particularly `TimeService`, `SleepStatus` and `BlockEntityTimeEffect`; Better Days credits Nick Iacullo's Hourglass as its origin. Its clock acceleration and process-progression approach informed this change. Chocolate Flavored independently uses Minecraft 26.1.2's whole-server scheduler to advance all ticking systems, with a remaining-night budget targeting twenty seconds. No Better Days/Hourglass source code or assets are copied, and neither mod is required.

## Serene Seasons

Source-reviewed against Glitchfiend/SereneSeasons `26.1`, revision `6a1109c93f8a4df85fae3bd481d5c332d005dae0`: `SeasonHandler.onLevelTick` advances seasons by changes in `getOverworldClockTime()`, and `SeasonTime` reads `time_settings.day_duration`. Keep that setting at its **24,000 default** with this implementation: it describes solar-clock units, not the 42,000 simulation ticks in a cycle. Setting it to 42,000 would lengthen seasons a second time. Clock pauses pause calendar advancement, while sleeping advances the calendar at the session's accelerated simulation rate. No season weather/crop hooks are replaced. Runtime compatibility with Serene Seasons remains untested.

## Verification

The dedicated-server checks exercise a full 42,000-tick solar cycle, native clock serialization, paused clocks, real bed entry, multiplayer/spectator rules, a complete midnight-to-morning session with exactly twenty seconds of native scheduler periods, uninterrupted dawn, and rate restoration. A separate live server-loop test measures actual wall time over 1,050 world ticks and checks that a vanilla furnace smelts items while sleeping. Existing build, server regression and development-client startup checks remain required. Heavily loaded multiplayer servers and Serene Seasons runtime integration remain manual tests.
