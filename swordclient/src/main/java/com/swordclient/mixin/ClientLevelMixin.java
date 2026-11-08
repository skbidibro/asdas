package com.swordclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.swordclient.Sky;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Sky colour presets. require = 0: if 1.21.11 moved this, the game still starts (sky just won't change). */
@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @ModifyReturnValue(method = "getSkyColor", at = @At("RETURN"), require = 0)
    private int swordclient$sky(int original) {
        return Sky.skyColor(original);
    }
}
