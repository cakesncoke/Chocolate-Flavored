# Deepslate Hammer and consumable flint

Minecraft 26.1.2 / NeoForge 26.1.2.122. No added recipes or dependencies.

Iron Hammer is replaced by **Deepslate Hammer**, using the newly supplied texture. Its existing 500 durability, 12 total attack damage, 0.7 attacks/second, enchantability and repair materials are retained. A registry alias loads saved `chocolateflavored:iron_hammer` stacks as `chocolateflavored:deepslate_hammer`, preserving their component patches.

The new hammer uses native stone-pickaxe speed (4) on pickaxe blocks, hand speed elsewhere, and one durability per normally mined block, special conversion or successful hit. Converted blocks are explicitly valid for its tool so the native wrong-tier harvest-time penalty does not apply. Other blocks retain stone-tier harvesting restrictions. Silk Touch and Fortune apply to ordinary native loot, not the fixed conversion rewards.

| Mined block | Remaining block | Drop |
| --- | --- | --- |
| Stone, granite, diorite, andesite | Air | 4 stone pebbles |
| Cobblestone, mossy cobblestone | Air | 1 stone pebble (existing rule) |
| Deepslate | Cobbled deepslate | 1 deepslate pebble |
| Deepslate copper / iron / gold ore | Cobbled deepslate | 1 corresponding ore pile |
| Iron / gold ore | Cobblestone | 1 corresponding ore pile |
| Coal / copper ore | Air | Native pickaxe loot and XP, including enchantments |
| Other pickaxe blocks, including deepslate coal ore | Native breaking | Native stone-tier harvesting rules |

Gold Ore Pile is a new stackable material item with the supplied texture, available in the mod creative tab. It has no additional recipes, food effects or unrelated drops.

Holding **vanilla flint in the main hand** and mining stone, granite, diorite or andesite converts it into cobblestone, drops one stone pebble and consumes one flint. This is an item-count cost, not durability. Cobblestone and ore do not gain this flint behavior. Off-hand flint does not change the behavior of the main-hand tool.

Stone Hammer remains unchanged: hand-speed stone-to-cobblestone conversions, cobblestone removal, coal dust and copper piles. Existing copper/steel/rose-gold pickaxe rules remain intact. Creative/spectator and canceled/protected breaks do not generate conversion rewards or spend held flint. The server replaces blocks immediately and charges only after successful replacement.

Datapacks can edit the existing stone/cobblestone tags and `deepslate_hammer_conversions` tool-harvesting tag. If adding converted stone blocks through the existing stone tag, include them in the latter tag too so the new hammer gets the intended harvest speed. No crop or seasonal systems change.

Development-only server checks cover exact conversion output counts, single wear, full stone removal, ore paths and Silk Touch, speed/tier restrictions, both saved-ID aliases, final flint consumption and protected/creative behavior. The client gallery checks the updated model/translation, gold ore pile and creative entries. These checks are excluded from release JARs.
