package com.swordclient;

public class Sky {
    public static final String[] NAMES = {
            "Default", "Storm", "Void", "Sunset", "Neon", "Aurora", "Rose",
            "Galaxy", "Blood", "Toxic", "Ice", "Mono", "Custom"};
    private static final int[] RGB = {
            0x000000, 0x5A606B, 0x040406, 0xFF7A45, 0x2B0A5E, 0x0B3D3A, 0xFF9EC4,
            0x1A0B3A, 0x4A0505, 0x2F7A1F, 0xA8E6FF, 0x202020, 0x000000};

    public static final String[] TIME_NAMES = {"Off", "Day", "Noon", "Sunset", "Night", "Midnight"};
    private static final long[] TIMES = {-1, 1000, 6000, 12500, 14000, 18000};

    public static int skyColor(int original) {
        Config c = Config.I;
        int p = Math.floorMod(c.skyPreset, NAMES.length);
        if (p == 0) return original;
        if (p == NAMES.length - 1) {
            int r = (int) (c.skyR * 255), g = (int) (c.skyG * 255), b = (int) (c.skyB * 255);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        return 0xFF000000 | RGB[p];
    }

    public static long time(long original) {
        int i = Math.floorMod(Config.I.timeLock, TIMES.length);
        return i == 0 ? original : TIMES[i];
    }
}
