# Chocolate Flavored agent guide

Instructions for AI coding agents (Claude Code and others) working on this repository. Claude Code loads this file through `CLAUDE.md`. Tool permissions live in `.claude/settings.json`, which the owner maintains; agents do not edit it.

## Project invariants

- Target: Minecraft 26.3, NeoForge 26.3.0.99, Java 25 (pinned in `gradle.properties`).
- Keep mod ID `chocolateflavored`, package `com.akiotsukino.chocolateflavored`, display name Chocolate Flavored, and MIT licensing for own code.
- Standalone: no required mod dependencies beyond NeoForge. Optional integrations such as Serene Seasons must never become required.
- Wherever seasonal systems interact, account for Serene Seasons. Never claim compatibility that has not been tested.
- Preserve asset licensing and attribution. Every bundled third-party file is recorded in `THIRD_PARTY_NOTICES.md`, `docs/resource-provenance.md` or `docs/asset-manifests/`. Never invent an author or license.

## Authority

You may, without asking first:

- Work directly on `main`: commit focused changes and push them to `origin main`. Do not create feature branches or pull requests unless asked.
- Read, create, edit, move and delete files in this repository.
- Run Gradle tasks, the verification servers, the client smoke test, `javap`, `git` (except the restricted operations below), and package installs for build tooling (JDK, Xvfb, Mesa, ImageMagick).
- Fix bugs you find, add or extend verification checks, refactor, and update documentation, as long as the change is verified (see below).
- Delete remote branches that are fully merged into `main`.
- Download build and development tools from Maven Central, the NeoForge Maven or `maven.latvian.dev`, and run them on the repository (for example a code formatter).

Ask the owner first before you:

- Change gameplay balance or design intent: numbers, drop rates, recipes, what an item does. Behavior that the owner's own verification checks explicitly assert is intentional, even if it looks odd.
- Remove or replace supplied assets, or import new third-party assets.
- Add a dependency to the release JAR, or change the mod ID, package, license or version targets.
- Rewrite published history (force push, `reset --hard` on pushed commits, rebases of `main`) or delete unmerged branches.
- Change this file's invariants, or publish anything outside this repository (issues, comments, releases).

## Development workflow

- Verify APIs against the pinned versions. The decompiled, NeoForge-patched sources are at `build/moddev/artifacts/minecraft-patched-26.3.0.99-sources.jar` after a build. Inspect vanilla code before modifying or recreating a mechanic, and prefer existing vanilla functionality, data and hooks over reimplementation.
- Treat `archive/1.21.1` and old feature branches as references and assets only. Check every API before reusing old Java code.
- Keep implementations modular, one feature package each, following current NeoForge conventions. Mixins go in `mixin/` and are registered in `chocolateflavored.mixins.json` (client-only mixins under `client`).
- Code style: four-space indentation, one statement per line, imports instead of inline fully qualified names. Single-line bodies are fine for trivial getters and overrides. Comment the reason for non-obvious logic, not the mechanics.

## Verification before committing

1. `./gradlew build` must pass.
2. For gameplay or server-side changes, run the dedicated-server suite in a fresh world. It must log `CHOCOLATE_FLAVORED_VERIFICATION_PASSED`, plus the marker of any suite you touched:
   ```sh
   rm -rf run-verification && mkdir -p run-verification
   echo 'eula=true' > run-verification/eula.txt
   printf 'online-mode=false\nlevel-type=minecraft:flat\ngenerate-structures=false\n' > run-verification/server.properties
   ./gradlew runVerificationServer --console=plain > verification.log 2>&1
   grep VERIFICATION_PASSED verification.log
   ```
   Gradle exits 0 even when a check throws, so always grep for the markers.
3. Client or rendering changes need the client smoke test: run `LIBGL_ALWAYS_SOFTWARE=true SDL_VIDEO_FORCE_EGL=1 xvfb-run -a -s "-screen 0 1280x720x24" bash scripts/client-smoke-test.sh` after the server run. `SDL_VIDEO_FORCE_EGL=1` is required on Xvfb, where 26.3's SDL window finds no GLX visual. If it still cannot open a window in your container, say so and rely on CI.
4. New mechanics get a check in `src/verification` (wired into `DevelopmentVerification`). Where practical, show that the check fails before a fix and passes after it.
5. For pure refactors or reformatting, compare `javap -c -p` output of the compiled classes before and after. Any difference must be explained in the commit message.
6. Update the relevant `docs/` page in the same commit as a behavior change.

Report results accurately: state which checks ran, which passed, and what remains untested. Never present unverified behavior as working.

## Commits

- One focused change per commit, with a descriptive subject and a body explaining why.
- Stage explicit paths. Check `git diff --cached --stat` before committing so unrelated staged files do not slip in.

## Documentation map

- `README.md`: overview, development and verification commands, feature index, credits summary.
- `docs/<feature>.md`: current behavior of each feature, written as a reference, not a changelog.
- `docs/cleanup-plan.md`: open maintenance items.
- `docs/port-26.3.md`: notes from the last version port.
