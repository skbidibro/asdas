package com.swordclient;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;

public class Hud {
    private static final ArrayDeque<Long> lmb = new ArrayDeque<>(), rmb = new ArrayDeque<>();
    private static boolean lastL, lastR;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    public static void tick(Minecraft mc) {
        boolean l = mc.options.keyAttack.isDown(), r = mc.options.keyUse.isDown();
        long now = System.currentTimeMillis();
        if (l && !lastL) lmb.add(now);
        if (r && !lastR) rmb.add(now);
        lastL = l; lastR = r;
        while (!lmb.isEmpty() && now - lmb.peek() > 1000) lmb.poll();
        while (!rmb.isEmpty() && now - rmb.peek() > 1000) rmb.poll();
    }

    public static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        Config c = Config.I;
        if (mc.options.hideGui || mc.player == null || mc.level == null) return;
        int acc = Config.accent();
        int w = g.guiWidth(), h = g.guiHeight();
        long now = System.currentTimeMillis();

        if (c.vignette) vignette(g, w, h);
        if (c.colorFilter) {
            int a = (int) (Math.max(0, Math.min(1, c.filterAlpha)) * 255);
            g.fill(0, 0, w, h, (a << 24) | (acc & 0xFFFFFF));
        }
        if (c.hitFlash) {
            long d = now - Cosmetics.lastHit;
            if (d < 140) g.fill(0, 0, w, h, ((int) (55 * (1 - d / 140f)) << 24) | 0xFFFFFF);
        }
        if (c.speedLines && (mc.player.isSprinting() || mc.player.getDeltaMovement().horizontalDistanceSqr() > 0.06))
            speedLines(g, w, h);

        // ---- info pill: FPS | clock ----
        int y = c.hudY;
        StringBuilder sb = new StringBuilder();
        if (c.showFps) sb.append(mc.getFps()).append(" FPS");
        if (c.showClock) { if (sb.length() > 0) sb.append("  |  "); sb.append(LocalTime.now().format(CLOCK)); }
        if (sb.length() > 0) {
            String s = sb.toString();
            pill(g, c.hudX, y, mc.font.width(s) + 14, 16, 0x99000000);
            g.fill(c.hudX + 1, y + 3, c.hudX + 3, y + 13, acc);
            g.drawString(mc.font, s, c.hudX + 8, y + 4, 0xFFFFFFFF, false);
            y += 19;
        }

        // ---- coordinates pill with DIMENSION + direction ----
        if (c.showCoords) {
            int bx = mc.player.getBlockX(), by = mc.player.getBlockY(), bz = mc.player.getBlockZ();
            String dim = "Dimension"; int dimCol = acc;
            boolean ow = false, nether = false;
            if (mc.level.dimension() == Level.OVERWORLD) { dim = "Overworld"; dimCol = 0xFF6EF0A0; ow = true; }
            else if (mc.level.dimension() == Level.NETHER) { dim = "Nether"; dimCol = 0xFFFF6B5A; nether = true; }
            else if (mc.level.dimension() == Level.END) { dim = "The End"; dimCol = 0xFFC9A0FF; }

            StringBuilder cs = new StringBuilder();
            if (c.showDimension) cs.append(dim).append("  ");
            cs.append(bx).append(' ').append(by).append(' ').append(bz);
            if (c.showDirection) {
                String d = mc.player.getDirection().getSerializedName();
                cs.append("  ").append(Character.toUpperCase(d.charAt(0)));
            }
            if (c.linkedCoords && (ow || nether)) {
                int lx = ow ? Math.floorDiv(bx, 8) : bx * 8, lz = ow ? Math.floorDiv(bz, 8) : bz * 8;
                cs.append("  >  ").append(ow ? "Nether " : "Overworld ").append(lx).append(' ').append(lz);
            }
            String s = cs.toString();
            pill(g, c.hudX, y, mc.font.width(s) + 14, 16, 0x99000000);
            g.fill(c.hudX + 1, y + 3, c.hudX + 3, y + 13, c.showDimension ? dimCol : acc);
            g.drawString(mc.font, s, c.hudX + 8, y + 4, 0xFFFFFFFF, false);
            y += 19;
        }

        if (c.showKeystrokes) {
            int x = c.keysX, ky = y + 3, s = 20, gap = 2;
            key(g, mc, x + s + gap, ky, s, s, "W", mc.options.keyUp);
            key(g, mc, x, ky + s + gap, s, s, "A", mc.options.keyLeft);
            key(g, mc, x + s + gap, ky + s + gap, s, s, "S", mc.options.keyDown);
            key(g, mc, x + 2 * (s + gap), ky + s + gap, s, s, "D", mc.options.keyRight);
            int w3 = 3 * s + 2 * gap;
            key(g, mc, x, ky + 2 * (s + gap), w3, 14, "SPACE", mc.options.keyJump);
            int hw = (w3 - gap) / 2;
            key(g, mc, x, ky + 2 * (s + gap) + 16, hw, 20, c.showCps ? "LMB " + lmb.size() : "LMB", mc.options.keyAttack);
            key(g, mc, x + hw + gap, ky + 2 * (s + gap) + 16, hw, 20, c.showCps ? "RMB " + rmb.size() : "RMB", mc.options.keyUse);
        }

        if (c.showArmor) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            int ax = w - 22, ay = h / 2 - 40;
            for (int i = 0; i < slots.length; i++) {
                ItemStack st = mc.player.getItemBySlot(slots[i]);
                if (st.isEmpty()) continue;
                g.fill(ax - 2, ay + i * 20 - 2, ax + 18, ay + i * 20 + 18, 0x80000000);
                g.renderItem(st, ax, ay + i * 20);
                g.renderItemDecorations(mc.font, st, ax, ay + i * 20);
            }
        }

        // ---- hit marker (little X around the crosshair) ----
        long d = now - Cosmetics.lastHit;
        if (c.hitMarker && d < 220) {
            int col = ((int) (255 * (1 - d / 220f)) << 24) | 0xFF4D5E;
            int cx = w / 2, cy = h / 2;
            for (int k = 4; k <= 9; k++) {
                g.fill(cx + k, cy + k, cx + k + 2, cy + k + 2, col);
                g.fill(cx - k - 2, cy + k, cx - k, cy + k + 2, col);
                g.fill(cx + k, cy - k - 2, cx + k + 2, cy - k, col);
                g.fill(cx - k - 2, cy - k - 2, cx - k, cy - k, col);
            }
        }
    }

    private static void vignette(GuiGraphics g, int w, int h) {
        for (int i = 0; i < 14; i++) {
            float f = 1f - i / 14f;
            int col = ((int) (90 * f * f) << 24);
            int in = i * 4;
            g.fill(0, in, w, in + 4, col);
            g.fill(0, h - in - 4, w, h - in, col);
            g.fill(in, 0, in + 4, h, col);
            g.fill(w - in - 4, 0, w - in, h, col);
        }
    }

    private static void speedLines(GuiGraphics g, int w, int h) {
        java.util.Random r = new java.util.Random(System.currentTimeMillis() / 60);
        for (int i = 0; i < 26; i++) {
            int col = ((40 + r.nextInt(70)) << 24) | 0xFFFFFF;
            int len = 18 + r.nextInt(46);
            if (i % 2 == 0) {
                int y = r.nextInt(h);
                int x = r.nextBoolean() ? r.nextInt(w / 5) : w - len - r.nextInt(w / 5);
                g.fill(x, y, x + len, y + 1, col);
            } else {
                int x = r.nextInt(w);
                int y = r.nextBoolean() ? r.nextInt(h / 5) : h - len - r.nextInt(h / 5);
                g.fill(x, y, x + 1, y + len, col);
            }
        }
    }

    private static void pill(GuiGraphics g, int x, int y, int w, int h, int col) {
        g.fill(x + 1, y, x + w - 1, y + h, col);
        g.fill(x, y + 1, x + 1, y + h - 1, col);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, col);
    }

    private static void key(GuiGraphics g, Minecraft mc, int x, int y, int w, int h, String label, KeyMapping k) {
        boolean down = k.isDown();
        pill(g, x, y, w, h, down ? Config.accent() : 0x99000000);
        int tw = mc.font.width(label);
        g.drawString(mc.font, label, x + (w - tw) / 2, y + (h - 8) / 2, down ? 0xFF0B0D12 : 0xFFFFFFFF, false);
    }
}
