# Anvil rules

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. No recipes or dependencies added.

Anvils charge **3 experience levels per operation** when the output gains an enchantment or an enchantment level. This includes enchanted books, transferring enchantments between tools, and combined repair/name/enchantment operations. The price remains three levels after any number of previous anvil uses. Survival players need three levels to take an enchanting result; creative mode retains vanilla's exemption.

All other valid operations cost **zero levels**, including repair materials, combining damaged equipment without enchantment gains, naming, and removing custom names. Free results can be taken at level zero. Existing enchantments do not themselves cause a fee. Repair amounts, material consumption, enchantment applicability, incompatibilities, maximum enchantment levels, and invalid/no-op inputs retain vanilla behavior. The 40-level rejection is removed, including for existing items whose prior-work penalty has reached the integer limit. Prior-work metadata cannot increase the final price.

Right-click a damaged anvil with an iron ingot in either hand to restore one stage: damaged to chipped, or chipped to intact. Each successful survival repair consumes one ingot and no experience, preserves facing, plays the anvil-use sound, and handles the click without opening the menu. Creative repairs do not consume the ingot. Intact anvils are not repaired further. Spectators and denied interactions cannot repair anvils. Normal use/falling damage remains vanilla.

Implementation uses NeoForge's post-calculation `AnvilUpdateEvent` for pricing and its right-click event for block repair. A focused menu mixin retains results that vanilla would discard at 40 levels and permits valid zero-cost pickup. Vanilla result calculation, payment and material handling remain in place. The menu changes run on both client and server; no custom screen or network protocol is needed.

The development-only dedicated server checks actual anvil menus and result pickup: material repairs across equipment types, zero-level rename/removal, tool combination, material leftovers, repeated enchantments with saturated prior-work penalties, book combination, tool-to-tool enchantment transfer, combined operations, insufficient XP, empty/invalid/incompatible inputs, and right-click anvil repair with orientation preservation and creative consumption rules. Client startup is checked separately; manual multiplayer screen/interaction testing is not automated.
