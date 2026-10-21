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
- Wandering trader attempts: one extended day, 42,000 ticks; visits last two days, 84,000 ticks. Previously saved countdowns finish their remaining time before using the new interval.
- Villager gossip decay: 42,000 ticks. The restock fallback window is extended proportionally to 21,000 ticks; calendar-day detection remains native.

Ordinary elapsed-time mechanics keep their existing normal-speed durations: furnaces, crops/random ticks, redstone, weather, effects, animal growth, and mod timers such as 20-minute torches, 5-second fire starters, 30-minute villager breeding cooldowns and 60-minute villager maturation. The full simulation accelerates them together during sleep.

## Sleeping

Sleeping no longer jumps the clock, forcibly clears weather, or immediately wakes everyone. Once the normal five-second deep-sleep entry has completed and the dimension's `players_sleeping_percentage` is met, the server's native tick scheduler runs at **twice its baseline rate** (normally **20 → 40 TPS**). This speeds up loaded ticking entities, block entities, scheduled block/fluid ticks, random ticks and vanilla/mod processes in every dimension. `random_tick_speed` is unchanged; the server performs twice as many full ticks per real second. Unloaded chunks retain normal vanilla behavior.

The multiplier applies once, regardless of the number of sleepers. Spectators do not count, zero percent still requires a sleeper, and percentages over 100 disable acceleration. The previous rate returns when the threshold is no longer met, including leaving bed, waking, disconnecting or changing dimensions. Native bed rules still control safety and waking at dawn, including their thunderstorm behavior. Sleeping can be ended normally using Leave Bed. Freeze, stepping and sprinting commands take precedence. Custom `/tick rate` baselines are doubled and restored, including deliberate rate changes made during sleep.

With sleep active, the nominal 15-minute night advances in about 7.5 real minutes; actual time spent in bed depends on entry time and the vanilla dawn wake point. All durations assume the server can sustain its target TPS. The multiplier is server-wide, so awake players are also affected.

## Serene Seasons

Source-reviewed against Glitchfiend/SereneSeasons `26.1`, revision `6a1109c93f8a4df85fae3bd481d5c332d005dae0`: `SeasonHandler.onLevelTick` advances seasons by changes in `getOverworldClockTime()`, and `SeasonTime` reads `time_settings.day_duration`. Keep that setting at its **24,000 default** with this implementation: it describes solar-clock units, not the 42,000 simulation ticks in a cycle. Setting it to 42,000 would lengthen seasons a second time. Clock pauses pause calendar advancement, while sleeping advances the calendar twice as fast in real time. No season weather/crop hooks are replaced. Runtime compatibility with Serene Seasons remains untested.

## Verification

The dedicated-server checks exercise a full 42,000-tick solar cycle, native clock serialization with fractional progress, pause/game-rule handling, real bed entry and deep sleep, multiplayer thresholds, spectator handling, the actual level tick without skipping, native 40-TPS scheduling, administrator rate/freeze handling, dawn wake-up, baseline restoration and trader initialization/reset intervals. The existing build, server regression and client startup checks remain required.
