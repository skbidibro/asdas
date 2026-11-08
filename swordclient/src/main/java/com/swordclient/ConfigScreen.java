package com.swordclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/** Modern sidebar + module-grid menu. All custom drawing, vanilla input handling. */
public class ConfigScreen extends Screen {
    private static final String[] TABS = {"Visuals", "HUD", "Sword", "Sky", "Cosmetics", "Effects"};
    private static int tab = 0;
    private final Screen parent;
    private int px, py, pw, ph;
    private int col, row, cx, cy, cw;
    private static final int CH = 22, GAP = 4, SIDE = 88;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Sword Client"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        pw = Math.min(400, width - 16);
        ph = Math.min(236, height - 16);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        cx = px + SIDE + 8;
        cy = py + 34;
        cw = (pw - SIDE - 8 - 12 - GAP) / 2;
        col = 0; row = 0;

        for (int i = 0; i < TABS.length; i++) {
            final int k = i;
            addRenderableWidget(new TabW(px + 8, py + 36 + i * 24, SIDE - 16, 22, TABS[i], i == tab,
                    () -> { tab = k; rebuildWidgets(); }));
        }

        Config c = Config.I;
        switch (tab) {
            case 0 -> {
                toggle("Fullbright", () -> c.fullbright, v -> c.fullbright = v);
                toggle("No rain", () -> c.noRain, v -> c.noRain = v);
                slider("Zoom", 0.05, 1.0, () -> c.zoomFactor, v -> c.zoomFactor = v, "%.2f");
                toggle("Smooth zoom", () -> c.smoothZoom, v -> c.smoothZoom = v);
                toggle("Vignette", () -> c.vignette, v -> c.vignette = v);
                toggle("Color filter", () -> c.colorFilter, v -> c.colorFilter = v);
                slider("Filter strength", 0.0, 0.5, () -> c.filterAlpha, v -> c.filterAlpha = v, "%.2f");
                cycle("Accent", () -> Config.ACCENT_NAMES[Math.floorMod(c.accent, Config.ACCENT_NAMES.length)],
                        () -> c.accent = (c.accent + 1) % Config.ACCENT_NAMES.length);
            }
            case 1 -> {
                toggle("FPS", () -> c.showFps, v -> c.showFps = v);
                toggle("Clock", () -> c.showClock, v -> c.showClock = v);
                toggle("Coordinates", () -> c.showCoords, v -> c.showCoords = v);
                toggle("Dimension", () -> c.showDimension, v -> c.showDimension = v);
                toggle("Direction", () -> c.showDirection, v -> c.showDirection = v);
                toggle("Linked coords", () -> c.linkedCoords, v -> c.linkedCoords = v);
                toggle("Keystrokes", () -> c.showKeystrokes, v -> c.showKeystrokes = v);
                toggle("CPS", () -> c.showCps, v -> c.showCps = v);
                toggle("Armor status", () -> c.showArmor, v -> c.showArmor = v);
            }
            case 2 -> {
                toggle("Custom pose", () -> c.swordCustom, v -> c.swordCustom = v);
                toggle("Karambit", () -> c.karambit, v -> c.karambit = v);
                slider("Offset X", -1, 1, () -> c.swordX, v -> c.swordX = v, "%.2f");
                slider("Rotate X", -180, 180, () -> c.swordRotX, v -> c.swordRotX = v, "%.0f");
                slider("Offset Y", -1, 1, () -> c.swordY, v -> c.swordY = v, "%.2f");
                slider("Rotate Y", -180, 180, () -> c.swordRotY, v -> c.swordRotY = v, "%.0f");
                slider("Offset Z", -1, 1, () -> c.swordZ, v -> c.swordZ = v, "%.2f");
                slider("Rotate Z", -180, 180, () -> c.swordRotZ, v -> c.swordRotZ = v, "%.0f");
                slider("Scale", 0.3, 2.0, () -> c.swordScale, v -> c.swordScale = v, "%.2f");
                cycle("Reset pose", () -> "click", () -> {
                    c.swordX = c.swordY = c.swordZ = 0;
                    c.swordRotX = c.swordRotY = c.swordRotZ = 0;
                    c.swordScale = 1.0;
                    rebuildWidgets();
                });
            }
            case 3 -> {
                cycle("Sky preset", () -> Sky.NAMES[Math.floorMod(c.skyPreset, Sky.NAMES.length)],
                        () -> c.skyPreset = (c.skyPreset + 1) % Sky.NAMES.length);
                cycle("Time lock", () -> Sky.TIME_NAMES[Math.floorMod(c.timeLock, Sky.TIME_NAMES.length)],
                        () -> c.timeLock = (c.timeLock + 1) % Sky.TIME_NAMES.length);
                slider("Custom R", 0, 1, () -> c.skyR, v -> c.skyR = v, "%.2f");
                slider("Custom G", 0, 1, () -> c.skyG, v -> c.skyG = v, "%.2f");
                slider("Custom B", 0, 1, () -> c.skyB, v -> c.skyB = v, "%.2f");
            }
            case 4 -> {
                cycle("Aura", () -> Cosmetics.AURA[Math.floorMod(c.aura, Cosmetics.AURA.length)],
                        () -> c.aura = (c.aura + 1) % Cosmetics.AURA.length);
                cycle("Trail", () -> Cosmetics.TRAIL[Math.floorMod(c.trail, Cosmetics.TRAIL.length)],
                        () -> c.trail = (c.trail + 1) % Cosmetics.TRAIL.length);
                cycle("Wings", () -> Cosmetics.WINGS[Math.floorMod(c.wings, Cosmetics.WINGS.length)],
                        () -> c.wings = (c.wings + 1) % Cosmetics.WINGS.length);
                cycle("Halo", () -> Cosmetics.HALO[Math.floorMod(c.halo, Cosmetics.HALO.length)],
                        () -> c.halo = (c.halo + 1) % Cosmetics.HALO.length);
                cycle("Ambient", () -> Cosmetics.AMBIENT[Math.floorMod(c.ambient, Cosmetics.AMBIENT.length)],
                        () -> c.ambient = (c.ambient + 1) % Cosmetics.AMBIENT.length);
                slider("Density", 0.2, 2.0, () -> c.density, v -> c.density = v, "%.2f");
            }
            default -> {
                cycle("Hit effect", () -> Cosmetics.HIT[Math.floorMod(c.hitFx, Cosmetics.HIT.length)],
                        () -> c.hitFx = (c.hitFx + 1) % Cosmetics.HIT.length);
                cycle("Kill effect", () -> Cosmetics.KILL[Math.floorMod(c.killFx, Cosmetics.KILL.length)],
                        () -> c.killFx = (c.killFx + 1) % Cosmetics.KILL.length);
                toggle("Landing ring", () -> c.landingRing, v -> c.landingRing = v);
                toggle("Hit marker", () -> c.hitMarker, v -> c.hitMarker = v);
                toggle("Hit flash", () -> c.hitFlash, v -> c.hitFlash = v);
                toggle("Speed lines", () -> c.speedLines, v -> c.speedLines = v);
                toggle("Hit FOV punch", () -> c.fovPunch, v -> c.fovPunch = v);
            }
        }
    }

    // ---- layout helpers (2-column grid) ----
    private int[] next() {
        int x = cx + col * (cw + GAP), y = cy + row * (CH + GAP);
        if (++col == 2) { col = 0; row++; }
        return new int[]{x, y};
    }

    private void toggle(String label, BooleanSupplier get, Consumer<Boolean> set) {
        int[] p = next();
        addRenderableWidget(new ToggleW(p[0], p[1], cw, CH, label, get, set));
    }

    private void slider(String label, double min, double max, DoubleSupplier get, DoubleConsumer set, String fmt) {
        int[] p = next();
        addRenderableWidget(new SliderW(p[0], p[1], cw, CH, label, min, max, get, set, fmt));
    }

    private void cycle(String label, Supplier<String> val, Runnable action) {
        int[] p = next();
        addRenderableWidget(new CycleW(p[0], p[1], cw, CH, label, val, action));
    }

    // ---- background / chrome ----
    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float dt) {
        int acc = Config.accent();
        g.fill(0, 0, width, height, 0x99000000);
        rrect(g, px - 1, py - 1, px + pw + 1, py + ph + 1, 0x33FFFFFF);
        rrect(g, px, py, px + pw, py + ph, 0xF00B0D12);
        rrect(g, px + 4, py + 4, px + SIDE, py + ph - 4, 0xFF11141B);
        Font f = font;
        g.drawString(f, "SWORD", px + 12, py + 12, 0xFFFFFFFF, false);
        g.drawString(f, "CLIENT", px + 12 + f.width("SWORD "), py + 12, acc, false);
        g.drawString(f, TABS[tab], cx, py + 14, 0xFFFFFFFF, false);
        g.fill(cx, py + 26, px + pw - 12, py + 27, 0x22FFFFFF);
        g.drawString(f, "Right Shift to close", px + 10, py + ph - 14, 0xFF6B7385, false);
    }

    @Override
    public void onClose() {
        Config.save();
        Minecraft.getInstance().setScreen(parent);
    }

    static void rrect(GuiGraphics g, int x1, int y1, int x2, int y2, int c) {
        g.fill(x1 + 1, y1, x2 - 1, y2, c);
        g.fill(x1, y1 + 1, x1 + 1, y2 - 1, c);
        g.fill(x2 - 1, y1 + 1, x2, y2 - 1, c);
    }

    // ---- widgets ----
    private static class TabW extends Button {
        private final String label; private final boolean selected;
        TabW(int x, int y, int w, int h, String label, boolean selected, Runnable r) {
            super(x, y, w, h, Component.empty(), b -> r.run(), DEFAULT_NARRATION);
            this.label = label; this.selected = selected;
        }
        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float dt) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            if (selected) {
                rrect(g, x, y, x + w, y + h, 0xFF1E2430);
                g.fill(x, y + 3, x + 2, y + h - 3, Config.accent());
            } else if (isHoveredOrFocused()) {
                rrect(g, x, y, x + w, y + h, 0xFF171B24);
            }
            g.drawString(Minecraft.getInstance().font, label, x + 10, y + (h - 8) / 2,
                    selected ? 0xFFFFFFFF : 0xFF9AA3B5, false);
        }
    }

    private static class ToggleW extends Button {
        private final String label; private final BooleanSupplier get;
        ToggleW(int x, int y, int w, int h, String label, BooleanSupplier get, Consumer<Boolean> set) {
            super(x, y, w, h, Component.empty(), b -> set.accept(!get.getAsBoolean()), DEFAULT_NARRATION);
            this.label = label; this.get = get;
        }
        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float dt) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean on = get.getAsBoolean();
            rrect(g, x, y, x + w, y + h, isHoveredOrFocused() ? 0xFF222733 : 0xFF191D26);
            g.drawString(Minecraft.getInstance().font, label, x + 7, y + (h - 8) / 2, 0xFFE6E9F0, false);
            int tx = x + w - 28, ty = y + (h - 10) / 2;
            rrect(g, tx, ty, tx + 22, ty + 10, on ? Config.accent() : 0xFF3A4050);
            int kx = on ? tx + 12 : tx + 2;
            rrect(g, kx, ty + 1, kx + 8, ty + 9, 0xFFFFFFFF);
        }
    }

    private static class CycleW extends Button {
        private final String label; private final Supplier<String> val;
        CycleW(int x, int y, int w, int h, String label, Supplier<String> val, Runnable action) {
            super(x, y, w, h, Component.empty(), b -> action.run(), DEFAULT_NARRATION);
            this.label = label; this.val = val;
        }
        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float dt) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            Font f = Minecraft.getInstance().font;
            rrect(g, x, y, x + w, y + h, isHoveredOrFocused() ? 0xFF222733 : 0xFF191D26);
            g.drawString(f, label, x + 7, y + (h - 8) / 2, 0xFFE6E9F0, false);
            String v = val.get();
            g.drawString(f, v, x + w - 7 - f.width(v), y + (h - 8) / 2, Config.accent(), false);
        }
    }

    private static class SliderW extends AbstractSliderButton {
        private final String label, fmt; private final double min, max; private final DoubleConsumer set;
        SliderW(int x, int y, int w, int h, String label, double min, double max,
                DoubleSupplier get, DoubleConsumer set, String fmt) {
            super(x, y, w, h, Component.empty(), (get.getAsDouble() - min) / (max - min));
            this.label = label; this.min = min; this.max = max; this.set = set; this.fmt = fmt;
        }
        @Override protected void updateMessage() { }
        @Override protected void applyValue() { set.accept(min + value * (max - min)); }
        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float dt) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            Font f = Minecraft.getInstance().font;
            rrect(g, x, y, x + w, y + h, isHoveredOrFocused() ? 0xFF222733 : 0xFF191D26);
            g.drawString(f, label, x + 7, y + 4, 0xFFE6E9F0, false);
            String v = String.format(fmt, min + value * (max - min));
            g.drawString(f, v, x + w - 7 - f.width(v), y + 4, 0xFF9AA3B5, false);
            g.fill(x + 7, y + h - 5, x + w - 7, y + h - 3, 0xFF2C3242);
            g.fill(x + 7, y + h - 5, x + 7 + (int) ((w - 14) * value), y + h - 3, Config.accent());
        }
    }
}
