package com.swordclient.mixin;

import com.swordclient.Config;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    private static final Identifier KARAMBIT = Identifier.fromNamespaceAndPath("swordclient", "karambit");

    @ModifyVariable(method = "updateForTopItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack swordclient$karambit(ItemStack stack) {
        if (Config.I.karambit && stack.is(ItemTags.SWORDS)) {
            ItemStack copy = stack.copy();
            copy.set(DataComponents.ITEM_MODEL, KARAMBIT);
            return copy;
        }
        return stack;
    }
}
