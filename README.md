
Chocolate Flavored
=======

Chocolate Flavored is a Minecraft mod that aims to revamp survival Minecraft into a more challenging 
experience while maintaining the core essence of an open world game. The mod does NOT claim to be the one true
way to play Minecraft, and instead focuses its energy on delivering a fulfilling experience to as many types of
players as possible.

Features:
============
- **Oven:** six visible cooking slots, furnace fuel, manual ignition, and
  Oven-specific recipes with campfire-recipe fallback. Uses adapted Farmer's
  Delight stove visuals and behavior without requiring Farmer's Delight.

- **Cooking Pot:** Farmer's Delight's six-ingredient cooking workstation,
  serving containers, stored meals, recipe book, and hopper automation.
  Requires heat from below, including a lit Oven.

- **Copper equipment:** the five Copper Age tools and copper nugget, with
  vanilla copper statistics and iron-level tool durability (250).

See [the copper equipment guide](docs/copper-equipment.md), [the Cooking Pot guide](docs/cooking-pot.md) and [the Oven guide](docs/oven.md) for controls, recipes, compatibility and tests,
and [third-party notices](THIRD_PARTY_NOTICES.md) for upstream credits.

Early Development:
============
The Oven and Cooking Pot are ready for in-game playtesting.

Development:
============
Requires Java 21 and NeoForge 21.1.252 for Minecraft 1.21.1.
Open this Gradle project in IntelliJ IDEA and use the included Gradle wrapper.

On Windows PowerShell:

```powershell
.\gradlew.bat build
.\gradlew.bat runGameTestServer
.\gradlew.bat runClient
```

On Linux/macOS, use `./gradlew` in place of `.\gradlew.bat`.
Run `runServer` separately for dedicated-server playtesting.
Report problems through [GitHub Issues](https://github.com/cakesncoke/Chocolate-Flavored/issues).
