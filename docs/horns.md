# Horns

Vanilla goat horns are displayed as **Horn**. Copper Horns use the supplied copper horn texture and vanilla instrument components: all eight vanilla sounds appear in the mod creative tab, with the same animation, duration, sound range and cooldown as their counterparts. No recipe is added.

Right-click your tamed pet or mount with a Copper Horn to link it. Saddled mounts without an owner (such as pigs and striders) and harnessed happy ghasts can also be linked. Other players' owned animals cannot be linked or recalled. Linking replaces the horn's previous target, preserves its instrument, and gives it an enchantment glint without adding an enchantment.

Blowing a linked horn recalls its living companion to your current position, including across dimensions. The item stores the entity UUID; global saved data records the linked entity's position before chunk unload and shutdown. Unloaded companions are retrieved using a temporary native portal chunk ticket with a ten-second timeout. Failed recalls show an action-bar message; they never create a replacement animal. Native horn cooldowns still apply. A companion carrying passengers cannot be recalled. Sitting state, equipment and other entity data are preserved. Dead companions cannot be resurrected; link the horn to another animal instead.
