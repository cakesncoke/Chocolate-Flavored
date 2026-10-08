package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.item.FlintItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/** Replace the original constructor, avoiding an unregistered intrusive holder for an unused item. */
@Mixin(Items.class)
public abstract class ItemsMixin {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "net/minecraft/world/item/Item"),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Items;NETHERITE_BOOTS:Lnet/minecraft/world/item/Item;", opcode = Opcodes.PUTSTATIC),
                    to = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Items;FLINT:Lnet/minecraft/world/item/Item;", opcode = Opcodes.PUTSTATIC)))
    private static Item chocolateflavored$replaceFlint(Item.Properties properties) {
        return new FlintItem();
    }
}
