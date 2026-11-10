# Chocolate Flavored — Project Development Instructions

## 1. Project Identity and Technical Requirements

You are developing **Chocolate Flavored**, a standalone Minecraft modification.

The following specifications are mandatory and must not be changed without explicit authorization:

- **Minecraft:** 26.3
- **Mod Loader:** NeoForge 26.3.0.102
- **Java:** 25
- **Mod Name:** Chocolate Flavored
- **Mod ID:** `chocolateflavored`
- **Package:** `com.akiotsukino.chocolateflavored`
- **License:** MIT

All implementations, registries, APIs, data files, resources, recipes, rendering systems, and other mod components must be specifically designed for these versions.

Never introduce code from older Minecraft versions without thoroughly adapting it to the current APIs.

Treat the official Minecraft and NeoForge implementations as the primary technical reference. Verify the actual available APIs before implementing anything. Never assume that an API, method, registry, event, or implementation from an older version still exists.

### Project philosophy

Chocolate Flavored should feel like a cohesive extension of vanilla Minecraft rather than a collection of unrelated features taken from different modifications.

Prioritize:
- Vanilla-like gameplay and presentation.
- Consistent mechanics and visual design.
- Maintainable and understandable code.
- Minimal unnecessary complexity.
- Compatibility with vanilla systems and datapacks.
- A coherent player experience.

Do not introduce unnecessary systems simply because another mod uses them.

---

## 2. Architecture and Code Organization

### Establish consistent conventions

Maintain a consistent organizational structure throughout the entire project.

When implementing features inspired by the old Chocolate Flavored mod, vanilla Minecraft, or other modifications:

1. Study how the original feature works.
2. Identify its underlying gameplay mechanics and requirements.
3. Determine the most appropriate way to implement it within Chocolate Flavored.
4. Adapt the implementation to the existing architecture.
5. Reuse established project conventions wherever possible.

**Never directly reproduce another project's file structure, architecture, naming conventions, or implementation patterns simply because you are referencing it.**

For example, if another modification organizes its equipment, items, or world mechanics using a particular system, do not automatically reproduce that system. Use Chocolate Flavored's existing organizational conventions instead.

Features that serve similar purposes should be organized together.

### Minimize unnecessary files

Keep the project organized, but avoid excessive fragmentation.

- Do not create a separate class for every minor operation.
- Do not introduce unnecessary managers, registries, wrappers, interfaces, or abstractions.
- Avoid creating multiple systems that perform essentially the same function.
- Keep related functionality together where appropriate.
- Separate genuinely independent systems when doing so improves maintainability.
- Avoid excessive nesting of packages and folders.

The objective is not to achieve the fewest possible lines of code. It is to maintain a clean, understandable architecture without unnecessary overhead.

Before introducing a new architectural pattern, check whether an existing project convention already solves the problem.

### Consistency is mandatory

Once a convention has been established, follow it for subsequent implementations unless there is a concrete technical reason to change it.

Do not introduce a different implementation approach for every new feature.

---

## 3. Standalone Design and Mod Compatibility

Chocolate Flavored must remain a standalone modification.

### No mandatory dependencies

Never introduce dependencies on other Minecraft modifications.

All gameplay functionality must work without any additional mods installed.

Do not require:
- JEI
- Serene Seasons
- Any other content or library modification.

### Optional compatibility

Although external modifications must never be required, Chocolate Flavored should coexist properly with relevant modifications.

The following are particularly important:

**JEI**
- Ensure relevant recipes are discoverable when JEI is installed.
- Ensure custom recipe types and ingredients integrate appropriately.
- Avoid breaking recipe visibility or recipe-book-related functionality.

**Serene Seasons**
- Ensure relevant crops, plants, blocks, and seasonal mechanics interact correctly with Serene Seasons.
- Avoid unnecessarily overriding or bypassing its mechanics.

Do not spend time testing optional compatibility for unrelated features.

Only investigate and test JEI when implementing or modifying features involving recipes, cooking, or recipe displays.

Only investigate and test Serene Seasons when implementing or modifying features involving crops, plants, farming, or seasonal interactions.

Do not introduce compatibility code without a relevant gameplay requirement.

Keep optional integrations isolated so that the absence of another mod never prevents Chocolate Flavored from loading.

---

## 4. Feature Implementation Standards

### Understand before implementing

Before implementing a feature:

1. Read its existing specification, if available.
2. Inspect relevant existing Chocolate Flavored implementations.
3. Examine vanilla Minecraft behavior where relevant.
4. Consult official NeoForge documentation and source code.
5. Identify interactions with existing Chocolate Flavored features.
6. Determine the simplest appropriate implementation.

Do not start implementing based on assumptions.

### Vanilla-first implementation

Prefer using existing vanilla mechanics and NeoForge APIs whenever possible.

Avoid unnecessarily recreating functionality that Minecraft already provides.

However, do not force a feature into an unsuitable vanilla system if doing so creates limitations or undesirable behavior.

### Feature completeness

An implementation is not complete merely because the code compiles.

Consider all relevant aspects of a feature:

- Gameplay behavior.
- Item and block interactions.
- Crafting and processing recipes.
- Inventory behavior.
- Creative inventory organization.
- Visual appearance and textures.
- Sounds and particles.
- Tooltips and descriptions.
- Multiplayer synchronization.
- Saving and loading.
- Interaction with vanilla mechanics.
- Relevant optional mod compatibility.

Only implement aspects that are relevant to the feature.

Do not introduce unrelated improvements or modify existing gameplay specifications without authorization.

### Preserve existing functionality

Never remove, replace, or silently alter an existing feature just to make a new implementation easier.

Before changing shared systems, inspect their existing uses and potential side effects.

When a feature needs to be redesigned, preserve its intended gameplay behavior unless the specification explicitly requests a change.

---

## 5. Creative Inventory Organization

Creative inventory organization is an important part of the mod's presentation.

Always organize Chocolate Flavored's creative inventory deliberately.

### General rules

- Group similar items together within the appropriate tabs.
- Keep related blocks, materials, equipment, tools, and decorative items organized logically.
- Follow vanilla Minecraft's general organizational conventions.
- Avoid scattering related items across unrelated tabs.
- Maintain a consistent ordering within each tab.

### Multiple tabs

Do not force excessively large collections of items into a single creative tab.

Create multiple tabs when a category becomes sufficiently large to justify subdivision.

For example, a large collection of content might be divided into:
- Equipment and Combat.
- Building Blocks and Materials.
- Natural Blocks and Resources.
- Food and Agriculture.
- Tools and Utilities.

These are examples, not mandatory tab names.

Use good judgment when deciding whether additional tabs are necessary.

Avoid creating a separate tab for a category containing only a handful of items unless there is a compelling reason.

Whenever new items are introduced, immediately determine their appropriate creative tab and position.

---

## 6. Testing and Development Efficiency

Testing must be reliable without unnecessarily consuming development time or context.

### Persistent test environments

Create and maintain reusable testing environments wherever practical.

Do not repeatedly generate new test environments, worlds, configurations, or test resources for every implementation.

Maintain suitable reusable environments for:
- Client testing.
- Server testing.
- World and gameplay testing.
- Relevant feature-specific testing.

Reuse existing worlds and test configurations whenever possible.

Only create additional environments when the existing ones cannot adequately test the feature.

Preserve important test worlds and their data. Avoid destructive testing against the primary testing environment.

### Efficient testing

Use the most appropriate level of testing for each change.

Prioritize:
1. Compilation and build verification.
2. Targeted automated tests.
3. Relevant registry and data validation.
4. Client startup and loading checks.
5. Relevant gameplay testing.
6. Server-side and multiplayer checks when applicable.

Do not repeatedly perform expensive full testing procedures when a smaller targeted test is sufficient.

However, do not skip necessary verification simply to save time.

### Testing accuracy

Never claim something has been tested if it has not actually been tested.

Distinguish clearly between:
- Implemented.
- Compiled successfully.
- Automated tests passed.
- Client startup verified.
- Manually tested in-game.
- Server or multiplayer tested.
- Optional compatibility verified.

If a test cannot be performed, explicitly state that it remains unverified.

### Avoid wasting context

Be economical with Claude Code's context window.

- Do not repeatedly read the entire project.
- Do not repeatedly inspect unrelated files.
- Do not regenerate information that is already available.
- Reuse existing test configurations and established implementation knowledge.
- Read only the documentation and source files relevant to the current task.
- Avoid repeatedly explaining or documenting information already established in the project.

Maintain a concise understanding of the project's architecture and conventions.

---

## 7. Documentation and Wiki

The project's documentation should primarily serve players, not developers.

We are aiming to build a Minecraft-style community wiki, similar in presentation and usefulness to the official Minecraft Wiki, rather than a technical developer handbook.

### Documentation philosophy

Prioritize:
- Gameplay information.
- Item and block descriptions.
- Crafting recipes.
- Mechanics and interactions.
- Mob behavior.
- Farming and agriculture.
- World generation.
- Equipment statistics.
- Practical player information.

Write in clear, natural language that players can understand.

Documentation should answer questions such as:
- What does this item do?
- How is it obtained?
- How is it crafted?
- What can it be used for?
- How does this mechanic work?
- What makes this feature different from vanilla Minecraft?

Avoid unnecessary technical information such as:
- Java implementation details.
- Internal class names.
- Registry implementation explanations.
- Detailed code architecture.
- Development logs.
- Excessive implementation notes.

Technical documentation should only exist when it is genuinely necessary for development or maintenance.

### Keep documentation minimal

Do not create a separate document for every small feature.

Organize related information into sensible wiki pages.

Avoid:
- Multiple documents explaining the same thing.
- Excessively detailed feature specifications.
- Repeated project instructions.
- Unnecessary changelogs and implementation reports.
- Documents that become outdated immediately after code changes.

Maintain a small number of useful, well-organized documents.

### Documentation accuracy

The wiki must reflect the actual implemented gameplay.

Whenever a feature's gameplay behavior changes, update the relevant wiki entry.

Never document planned or incomplete functionality as if it were already implemented.

Use consistent terminology, formatting, and naming throughout the wiki.

---

## 8. Assets, References, and Credits

Chocolate Flavored may use existing open-source resources and references to help develop its content.

However, resources and implementations must be evaluated individually.

### Asset handling

When using resources from other modifications or open-source projects:

- Check the original license.
- Verify whether redistribution and modification are permitted.
- Preserve any legally required license notices.
- Keep original asset attribution information where necessary.
- Adapt resources appropriately to Chocolate Flavored's visual identity.
- Do not unnecessarily reproduce the source modification's organizational structure.

Do not automatically add credits simply because a resource was obtained from another modification.

Most importantly, do not assume that attribution is unnecessary either.

Only include credits when legally required, explicitly requested by the original creator, or genuinely relevant to acknowledging a meaningful contribution.

Keep the credits section concise and factual.

Do not clutter the wiki, README, or individual feature pages with repetitive attribution.

### Resource organization

Keep resources organized using consistent naming conventions and directory structures.

Avoid creating duplicate assets when an existing resource can be reused.

Do not introduce unnecessary asset-management systems.

---

## 9. Git and Project Management

Keep development organized and changes easy to review.

- Work on the designated development branch.
- Use focused commits with descriptive messages.
- Avoid combining unrelated features into one commit.
- Do not commit temporary testing files, generated build outputs, or unnecessary development artifacts.
- Preserve useful project history and reusable resources.
- Do not delete existing resources without checking whether they are needed.
- Never overwrite or discard user work without authorization.

Do not directly push unreviewed changes to the main branch.

When completing a feature, provide a concise summary of:
1. What was implemented.
2. Which gameplay behaviors were introduced or changed.
3. Which files or systems were affected.
4. What testing was performed.
5. Anything that remains incomplete or unverified.

Do not produce lengthy technical reports unless specifically requested.

---

## 10. Scope Control and Decision-Making

Stay focused on the task currently being implemented.

Do not:
- Introduce unrelated improvements.
- Redesign established systems without a reason.
- Add features that were not requested.
- Create unnecessary abstractions for hypothetical future functionality.
- Change gameplay balance without authorization.
- Introduce new dependencies.
- Overcomplicate straightforward mechanics.

When a specification is ambiguous, inspect the existing project and relevant vanilla behavior before deciding how to proceed.

If a decision would significantly affect gameplay, architecture, compatibility, or existing functionality, ask for clarification rather than making an irreversible assumption.

For minor implementation decisions, use sound engineering judgment and follow established project conventions.

### Final guiding principle

**Chocolate Flavored should be a cohesive, polished, vanilla-inspired Minecraft modification, not an unnecessarily complicated collection of independently developed systems.**

Every implementation should contribute to that goal.

Prioritize gameplay quality, consistency, maintainability, and simplicity over architectural novelty or excessive documentation.