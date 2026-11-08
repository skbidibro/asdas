package com.swordclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.swordclient.Config;
import com.swordclient.Cosmetics;
import com.swordclient.SwordClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    private static float swordclient$zoomAnim = 1f;

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private float swordclient$fov(float fov) {
        float target = SwordClient.zooming() ? (float) Config.I.zoomFactor : 1f;
        swordclient$zoomAnim = Config.I.smoothZoom ? swordclient$zoomAnim + (target - swordclient$zoomAnim) * 0.2f : target;
        fov *= swordclient$zoomAnim;
        if (Config.I.fovPunch) fov *= 1f - 0.09f * Cosmetics.punch();
        return fov;
    }
}
