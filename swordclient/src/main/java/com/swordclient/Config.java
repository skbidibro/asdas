package com.swordclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("swordclient.json");
    public static Config I = new Config();

    public static final String[] ACCENT_NAMES = {"Cyan", "Violet", "Rose", "Mint", "Amber", "White"};
    public static final int[] ACCENTS = {0xFF58D6FF, 0xFFB48CFF, 0xFFFF7AB6, 0xFF6EF0A0, 0xFFFFB454, 0xFFFFFFFF};

    // Look
    public int accent = 0;

    // HUD
    public boolean showFps = true, showKeystrokes = true, showCps = true;
    public boolean showCoords = true, showArmor = true, showClock = false;
    public int hudX = 6, hudY = 6, keysX = 6, keysY = 30;

    // Visuals
    public double zoomFactor = 0.25;
    public boolean fullbright = false;
    public boolean noRain = false;
    public boolean colorFilter = false;
    public double filterAlpha = 0.12;

    // Sky
    public int skyPreset = 0;               // see Sky.NAMES
    public double skyR = 0.1, skyG = 0.1, skyB = 0.18; // used by "Custom"
    public int timeLock = 0;                // see Sky.TIME_NAMES

    // Cosmetics (particles, client-side)
    public int aura = 0, trail = 0, wings = 0, halo = 0, ambient = 0;
    public double density = 1.0;

    // Effects
    public int hitFx = 1, killFx = 1;
    public boolean landingRing = true, hitMarker = true, hitFlash = true;
    public boolean speedLines = true, fovPunch = true, vignette = true, smoothZoom = true;

    // HUD extras
    public boolean showDimension = true, showDirection = true, linkedCoords = false;

    // Sword
    public boolean swordCustom = true;
    public double swordX = 0, swordY = 0, swordZ = 0;
    public double swordRotX = 0, swordRotY = 0, swordRotZ = 0;
    public double swordScale = 1.0;
    public boolean karambit = false;

    public static int accent() { return ACCENTS[Math.floorMod(I.accent, ACCENTS.length)]; }

    public static void load() {
        try {
            if (Files.exists(FILE)) I = GSON.fromJson(Files.readString(FILE), Config.class);
        } catch (Exception e) { I = new Config(); }
        if (I == null) I = new Config();
    }

    public static void save() {
        try { Files.writeString(FILE, GSON.toJson(I)); } catch (Exception ignored) {}
    }
}
