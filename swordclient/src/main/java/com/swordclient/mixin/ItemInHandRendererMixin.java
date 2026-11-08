package com.swordclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.swordclient.Config;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "renderItem", at = @At("HEAD"))
    private void swordclient$pose(LivingEntity entity, ItemStack stack, ItemDisplayContext ctx,
                                  PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        Config c = Config.I;
        if (!c.swordCustom || !ctx.firstPerson() || !stack.is(ItemTags.SWORDS)) return;

        boolean left = ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        float m = left ? -1f : 1f;

        pose.translate(c.swordX * m, c.swordY, c.swordZ);
        pose.mulPose(Axis.XP.rotationDegrees((float) c.swordRotX));
        pose.mulPose(Axis.YP.rotationDegrees((float) c.swordRotY * m));
        pose.mulPose(Axis.ZP.rotationDegrees((float) c.swordRotZ * m));
        float s = (float) c.swordScale;
        pose.scale(s, s, s);
    }
}
