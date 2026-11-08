package com.swordclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.swordclient.Config;
import com.swordclient.Sky;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Client-side time lock and rain removal. Guarded so the integrated server is never touched. */
@Mixin(Level.class)
public class LevelMixin {
    @ModifyReturnValue(method = "getDayTime", at = @At("RETURN"), require = 0)
    private long swordclient$time(long original) {
        Level self = (Level) (Object) this;
        return self.isClientSide() ? Sky.time(original) : original;
    }

    @ModifyReturnValue(method = "getRainLevel", at = @At("RETURN"), require = 0)
    private float swordclient$rain(float original) {
        Level self = (Level) (Object) this;
        return self.isClientSide() && Config.I.noRain ? 0f : original;
    }
}
