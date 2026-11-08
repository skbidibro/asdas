package com.swordclient;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

/** Client-side cosmetics: everything here is particles, so only YOU see it. */
public class Cosmetics {
    public static final String[] AURA = {"Off", "Sparkle", "Verdant", "Soul", "Electric"};
    public static final String[] TRAIL = {"Off", "Smoke", "Glow", "Soul", "Verdant"};
    public static final String[] WINGS = {"Off", "Angel", "Demon", "Void"};
    public static final String[] HALO = {"Off", "Light", "Soul", "Verdant"};
    public static final String[] AMBIENT = {"Off", "Ash", "Petals", "Snow", "Embers"};
    public static final String[] HIT = {"Off", "Ring", "Burst", "Ring + Burst"};
    public static final String[] KILL = {"Off", "Soul", "Firework", "Totem"};

    private static final ParticleOptions[] AURA_P = {null, ParticleTypes.END_ROD, ParticleTypes.HAPPY_VILLAGER,
            ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.ELECTRIC_SPARK};
    private static final ParticleOptions[] TRAIL_P = {null, ParticleTypes.CLOUD, ParticleTypes.END_ROD,
            ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.HAPPY_VILLAGER};
    private static final ParticleOptions[] WING_P = {null, ParticleTypes.END_ROD, ParticleTypes.FLAME,
            ParticleTypes.SOUL_FIRE_FLAME};
    private static final ParticleOptions[] HALO_P = {null, ParticleTypes.END_ROD, ParticleTypes.SOUL_FIRE_FLAME,
            ParticleTypes.HAPPY_VILLAGER};
    private static final ParticleOptions[] AMBIENT_P = {null, ParticleTypes.WHITE_ASH, ParticleTypes.CHERRY_LEAVES,
            ParticleTypes.SNOWFLAKE, ParticleTypes.FLAME};

    // wing outline: {lateral, height} per side
    private static final double[][] WING = {
            {0.30, 1.25}, {0.60, 1.50}, {0.95, 1.62}, {1.30, 1.52}, {1.58, 1.28},
            {1.46, 1.02}, {1.16, 0.86}, {0.86, 0.72}, {0.55, 0.95}};

    private static final Random RND = new Random();
    public static long lastHit = 0;
    private static int t = 0;
    private static Entity lastTarget;
    private static double tx, ty, tz;
    private static boolean prevOnGround = true;
    private static double prevVy = 0;

    private static int count(double base) {
        return (int) Math.floor(base * Config.I.density + RND.nextDouble());
    }

    private static double rnd() { return RND.nextDouble() - 0.5; }

    public static void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        t++;
        Config c = Config.I;
        Player p = mc.player;

        // --- aura ---
        int ai = Math.floorMod(c.aura, AURA.length);
        if (ai > 0) {
            int n = count(2);
            for (int i = 0; i < n; i++) {
                double a = t * 0.35 + i * Math.PI;
                double y = p.getY() + ((t * 0.05 + i * 0.9) % 1.9);
                mc.level.addParticle(AURA_P[ai], p.getX() + Math.cos(a) * 0.65, y,
                        p.getZ() + Math.sin(a) * 0.65, 0, 0.01, 0);
            }
        }

        // --- trail ---
        int ti = Math.floorMod(c.trail, TRAIL.length);
        double dx = p.getX() - p.xo, dz = p.getZ() - p.zo;
        if (ti > 0 && dx * dx + dz * dz > 0.004) {
            int n = count(3);
            for (int i = 0; i < n; i++)
                mc.level.addParticle(TRAIL_P[ti], p.getX() + rnd() * 0.4, p.getY() + 0.1 + RND.nextDouble() * 0.3,
                        p.getZ() + rnd() * 0.4, 0, 0.01, 0);
        }

        // --- wings (particle outline, follows body yaw, flaps) ---
        int wi = Math.floorMod(c.wings, WINGS.length);
        if (wi > 0) {
            double yaw = Math.toRadians(p.yBodyRot);
            double bx = Math.sin(yaw) * 0.28, bz = -Math.cos(yaw) * 0.28;   // behind the back
            double px = Math.cos(yaw), pz = Math.sin(yaw);                  // sideways
            double flap = 1 + 0.10 * Math.sin(t * 0.25);
            for (int s = -1; s <= 1; s += 2) {
                for (double[] pt : WING) {
                    if (RND.nextDouble() > 0.42 * c.density) continue;
                    double lat = pt[0] * flap * s;
                    mc.level.addParticle(WING_P[wi],
                            p.getX() + bx + px * lat, p.getY() + pt[1] * (p.isCrouching() ? 0.85 : 1.0),
                            p.getZ() + bz + pz * lat, 0, 0, 0);
                }
            }
        }

        // --- halo ---
        int hi = Math.floorMod(c.halo, HALO.length);
        if (hi > 0) {
            double hy = p.getY() + p.getBbHeight() + 0.22;
            for (int i = 0; i < 2; i++) {
                double a = t * 0.22 + i * Math.PI;
                mc.level.addParticle(HALO_P[hi], p.getX() + Math.cos(a) * 0.38, hy, p.getZ() + Math.sin(a) * 0.38, 0, 0, 0);
            }
        }

        // --- ambient weather-like particles around you ---
        int mi = Math.floorMod(c.ambient, AMBIENT.length);
        if (mi > 0) {
            int n = count(3);
            for (int i = 0; i < n; i++)
                mc.level.addParticle(AMBIENT_P[mi], p.getX() + rnd() * 16, p.getY() + RND.nextDouble() * 6 - 1,
                        p.getZ() + rnd() * 16, rnd() * 0.03, -0.02, rnd() * 0.03);
        }

        // --- landing ring ---
        boolean og = p.onGround();
        double vy = p.getY() - p.yo;
        if (c.landingRing && og && !prevOnGround && prevVy < -0.5) {
            for (int i = 0; i < 18; i++) {
                double a = i * Math.PI * 2 / 18;
                mc.level.addParticle(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.05, p.getZ(),
                        Math.cos(a) * 0.16, 0.01, Math.sin(a) * 0.16);
            }
        }
        prevOnGround = og;
        prevVy = vy;

        // --- kill effect ---
        if (lastTarget != null) {
            boolean dead = lastTarget.isRemoved()
                    || (lastTarget instanceof LivingEntity le && le.isDeadOrDying());
            if (dead) {
                if (System.currentTimeMillis() - lastHit < 4000) killFx(mc);
                lastTarget = null;
            } else {
                tx = lastTarget.getX();
                ty = lastTarget.getY() + lastTarget.getBbHeight() * 0.5;
                tz = lastTarget.getZ();
            }
        }
    }

    private static void killFx(Minecraft mc) {
        int k = Math.floorMod(Config.I.killFx, KILL.length);
        if (k == 1) {
            for (int i = 0; i < 26; i++) {
                mc.level.addParticle(ParticleTypes.SOUL, tx, ty, tz, rnd() * 0.15, 0.1 + RND.nextDouble() * 0.15, rnd() * 0.15);
                if (i % 2 == 0)
                    mc.level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, tx, ty, tz, rnd() * 0.2, 0.05 + RND.nextDouble() * 0.2, rnd() * 0.2);
            }
        } else if (k == 2) {
            for (int i = 0; i < 40; i++)
                mc.level.addParticle(ParticleTypes.FIREWORK, tx, ty, tz, rnd() * 0.5, rnd() * 0.5, rnd() * 0.5);
        } else if (k == 3) {
            for (int i = 0; i < 44; i++)
                mc.level.addParticle(ParticleTypes.TOTEM_OF_UNDYING, tx, ty, tz, rnd() * 0.6, RND.nextDouble() * 0.6, rnd() * 0.6);
        }
    }

    public static void onHit(Entity e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        lastHit = System.currentTimeMillis();
        lastTarget = e;
        tx = e.getX(); ty = e.getY() + e.getBbHeight() * 0.5; tz = e.getZ();
        int h = Math.floorMod(Config.I.hitFx, HIT.length);

        if (h == 1 || h == 3) {
            for (int i = 0; i < 28; i++) {
                double a = i * Math.PI * 2 / 28;
                mc.level.addParticle(ParticleTypes.END_ROD, tx, ty, tz, Math.cos(a) * 0.28, 0, Math.sin(a) * 0.28);
            }
            for (int i = 0; i < 20; i++) {
                double a = i * Math.PI * 2 / 20;
                mc.level.addParticle(ParticleTypes.END_ROD, tx, ty, tz, Math.cos(a) * 0.18, Math.sin(a) * 0.18, 0);
            }
        }
        if (h == 2 || h == 3) {
            for (int i = 0; i < 18; i++)
                mc.level.addParticle(ParticleTypes.CRIT, tx, ty, tz, rnd() * 0.7, RND.nextDouble() * 0.5, rnd() * 0.7);
            for (int i = 0; i < 8; i++)
                mc.level.addParticle(ParticleTypes.ELECTRIC_SPARK, tx, ty, tz, rnd() * 0.5, rnd() * 0.5, rnd() * 0.5);
        }
    }

    /** 0..1, strength of the hit FOV punch right now. */
    public static float punch() {
        long dt = System.currentTimeMillis() - lastHit;
        return dt >= 250 ? 0f : 1f - dt / 250f;
    }
}
