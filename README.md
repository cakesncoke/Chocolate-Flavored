# Chocolate Flavored

A standalone survival overhaul for **Minecraft 26.1.2**, built with **NeoForge 26.1.2.122** and **Java 25**. Mod ID: `chocolateflavored`. Base package: `com.akiotsukino.chocolateflavored`. Own code: MIT; see [LICENSE](LICENSE). Bundled third-party portions retain their licenses; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). The MDK attribution is preserved in [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt).

`main` is the active, fresh 26.1.2 project. The previous implementation and reusable assets remain on `archive/1.21.1` and the existing feature branches. Do not copy old Java code without checking the current APIs.

## Development

Install a JDK 25 and configure IntelliJ's project SDK and Gradle JVM to use it. Import the root Gradle project. Use the checked-in Gradle 9.2.1 wrapper rather than a system Gradle installation.

On Windows:

```powershell
.\gradlew.bat clean build
.\gradlew.bat runClient
```

On Linux/macOS:

```sh
bash ./gradlew clean build
bash ./gradlew runClient
```

The JAR is generated in `build/libs/`. Mod metadata is expanded from `src/main/templates/META-INF/neoforge.mods.toml`; do not edit generated copies under `build/`.

## Verification

CI runs a clean build with Java 25 and dedicated-server behavior checks, then launches an integrated development client under a virtual display with software rendering. It verifies the real cooking-pot menu and renders every new adult/baby animal in a CI-only gallery. Logs and a screenshot are uploaded as `client-startup-evidence`; verification classes are excluded from release JARs. These checks do not replace exploratory gameplay testing.

Equipment progression, rose gold tools and trims, knives, and early survival mining are implemented. See [equipment and mining](docs/equipment-and-mining.md) for exact behavior and development verification. Primitive lighting adds timed torches, a fire starter, unlit lanterns, and unlit normal-campfire placement; see [primitive lighting](docs/primitive-lighting.md). Anvils charge three levels only for adding or upgrading enchantments; repairs and naming are free, and iron ingots repair damaged anvils. See [anvil rules](docs/anvil-rules.md). Villages generate abandoned, with persistent residents, revised zombie-villager spawning and longer villager population timers; see [village rules](docs/village-rules.md). Standalone ovens and cooking pots add datapack/KubeJS processing recipes; see [cooking](docs/cooking.md). No crafting recipes are added for these blocks. Only NeoForge and Minecraft are required dependencies. Focused mixins handle current data-driven villager offers, campfire placement, and the fire-starter hand animation; no legacy mappings or access transformers are configured.

Custom recipes support datapacks. Oven and cooking-pot recipe compatibility is tested with an optional development-only KubeJS installation. Seasonal systems must account for Serene Seasons when present, without making it a required dependency; Serene Seasons integration remains untested.

Before changing vanilla behavior, inspect the exact project's current Minecraft sources through the IDE. Minecraft 26.1 is unobfuscated; do not introduce old mapping configuration.

## Earth animals

Standalone Earth-style animal variants, white/rare-pink natural sheep, Buttercup and Pink Daisy are described in [docs/earth-animals.md](docs/earth-animals.md), including exact spawn percentages, item-limit adaptations and upstream attribution.

