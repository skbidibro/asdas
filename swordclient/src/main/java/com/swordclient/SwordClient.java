package com.swordclient;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.lwjgl.glfw.GLFW;

public class SwordClient implements ClientModInitializer {
    public static final String MOD_ID = "swordclient";
    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    public static KeyMapping ZOOM, FULLBRIGHT, MENU;
    private static boolean hadFullbright = false;

    @Override
    public void onInitializeClient() {
        Config.load();

        ZOOM = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.swordclient.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY));
        FULLBRIGHT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.swordclient.fullbright", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));
        MENU = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.swordclient.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(SwordClient::tick);

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (level.isClientSide() && player == Minecraft.getInstance().player) Cosmetics.onHit(entity);
            return InteractionResult.PASS;
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
                Hud::render);
    }

    public static boolean zooming() {
        return ZOOM != null && ZOOM.isDown();
    }

    private static void tick(Minecraft mc) {
        while (FULLBRIGHT.consumeClick()) {
            Config.I.fullbright = !Config.I.fullbright;
            Config.save();
        }
        while (MENU.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new ConfigScreen(null));
        }
        Hud.tick(mc);
        Cosmetics.tick(mc);

        // Fullbright = client-side night vision (cosmetic, never sent to the server)
        if (mc.player != null) {
            if (Config.I.fullbright) {
                mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1000, 0, false, false, false));
                hadFullbright = true;
            } else if (hadFullbright) {
                mc.player.removeEffect(MobEffects.NIGHT_VISION);
                hadFullbright = false;
            }
        }
    }
}
