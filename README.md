
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

See [the Oven guide](docs/oven.md) for controls, recipes, compatibility and tests,
and [third-party notices](THIRD_PARTY_NOTICES.md) for upstream credits.

Early Development:
============
The Oven is the first feature and is ready for in-game playtesting.

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
