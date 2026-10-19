# Chocolate Flavored development rules

- Develop against main: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25; this is a fresh foundation.
- Keep mod ID chocolateflavored, package com.akiotsukino.chocolateflavored, display name Chocolate Flavored, and MIT licensing.
- Verify APIs against the exact pinned versions. Inspect current vanilla sources before modifying/recreating vanilla mechanics, and prefer using existing vanilla functionality.
- Use archive/1.21.1 and old feature branches as references/assets only. Verify compatibility before reusing any Java code. Preserve asset licensing and attribution.
- Keep the mod standalone; no required mod dependencies beyond NeoForge. Keep implementations modular and follow current NeoForge conventions.
- Custom recipes must remain datapack-compatible, with KubeJS integration planned. Account for Serene Seasons wherever seasonal systems interact; do not claim compatibility without testing.
- Build after changes and report errors and untested behavior accurately. Make focused commits with descriptive messages.
- Do not begin gameplay implementation until the foundation build and development client startup have been verified.
