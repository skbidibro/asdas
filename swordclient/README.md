# Sword Client (Fabric, Minecraft 1.21.11)

Build: push this folder to GitHub (the included workflow builds the jar under Actions -> swordclient-jar),
or install JDK 21 + Gradle and run `gradle build`.

Keys: C zoom (hold) | G fullbright | Right Shift = client menu

Menu tabs
- Visuals: fullbright, no rain, zoom (smooth), vignette, color filter, accent colour
- HUD: FPS, clock, coordinates WITH dimension + direction (+ linked Nether/Overworld coords), keystrokes, CPS, armor
- Sword: custom first-person pose (offset / rotation / scale) + karambit model
- Sky: 12 presets + custom RGB + time lock
- Cosmetics: aura, trail, wings, halo, ambient particles (ash / petals / snow / embers), density
- Effects: hit ring/burst, kill effect, landing ring, hit marker, hit flash, speed lines, hit FOV punch
(Cosmetics/Effects are client-side particles, only you see them.)

Sky and time use soft mixins (require = 0). If 1.21.11 changed those methods the game still launches,
the sky just won't change; send me the log and I'll retarget it.
