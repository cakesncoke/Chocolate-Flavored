package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.item.FlintItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replace flint at bootstrap, before any vanilla loot, trades or creative entries reference it. */
@Mixin(Items.class)
public abstract class ItemsMixin {
    @Inject(method = "registerItem(Ljava/lang/String;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/item/Item;",
            at = @At("HEAD"), cancellable = true)
    private static void chocolateflavored$replaceFlint(String name, Item item, CallbackInfoReturnable<Item> callback) {
        if (name.equals("flint")) {
            // Calling the ResourceLocation overload avoids re-entering this injection.
            callback.setReturnValue(Items.registerItem(ResourceLocation.fromNamespaceAndPath("minecraft", "flint"), new FlintItem()));
        }
    }
}
