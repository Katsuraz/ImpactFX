# ImpactFX — Cinematic Impact Frames for Fabric 26.3

**Version:** 0.4.0-alpha.1 — Phase 4 extended, spatially anchored impacts (compilation is not runtime validation)

A lightweight, client-side Minecraft Fabric mod that adds stylized impact frames to explosions and successful mace smash attacks. The mod doesn't modify damage, knockback, physics, or time.

## Phase 4 — Spatial impacts, longer duration and optional anime audio

* **Cinematic length:** a new **Length Scale** slider (100–500%) multiplies your saved base duration. Default is 300%, including for existing Alpha 3 settings. The base duration slider also extends to 1,400 ms. Animations are capped at 2,600 ms to avoid indefinitely obscuring gameplay.
* **Hit-space positions:** the HUD pulse is projected toward the real packet explosion coordinates or the approximate mace impact. For mace smashes performed by the local player, a directional trail appears from the player's attacking position toward the target/ground indicated by the crosshair. When accurate target information is unavailable, the trajectory is estimated rather than treated as authoritative.
* **World shader anchors:** the GPU zoom/distortion no longer has a hard-coded center. It chooses the nearest cell in a **5-column × 3-row** grid around the projected hit. The HUD ring remains closer to the precise screen position.
* **Moving distortion:** the strong Overdrive shockwave now moves through three distinct expansion stages.
* **Long-session comfort:** in Mod Menu → ImpactFX → Impact Power you can reduce Duration Scale, disable Screen Punch or Shockwaves, or use Reduced Flashes.

**Anime audio is intentionally not included in this public repository or JAR.** A separate private/local resource pack is generated from the video uploaded in the conversation; users must provide their own licensed audio for redistribution. The pack overrides the vanilla `entity.generic.explode` sound event, leaving fireworks and mace sounds unchanged. Audio clipped from television episodes can contain dialog or background music and should not be treated as clean isolated sound effects.

## Phase 3 — Impact Power / Overdrive

**New:** Overdrive Finisher is a seventh preset designed mainly for mace smashes.
It uses an intentional four-beat sequence:

1. **SNAP** — brief monochrome impact cut.
2. **STRIKE** — intense black-and-white ink silhouette and visual screen punch.
3. **SHOCK** — expanding concentric shockwave, refracted world frame, radial speed lines.
4. **ECHO** — soft violet afterimage; no additional hard flash.

The world shader uses a handful of fixed short-duration shader passes (no per-frame framebuffer allocations), and the HUD impact remains anchored to the estimated hit direction. The **Impact Power** page provides an extra Drama slider, screen punch, shockwaves, speed lines and reduced-flash options.

**This is an illusion of hit-stop and camera punch; the game's actual camera rotation, physics, ticks, multiplayer synchronization and attack rules are unchanged.** The GPU shockwave is centered on-screen for now; the HUD effects are projected around the impact position.

Existing installs may have saved the old Manga mace preset; select **Overdrive Finisher** manually in Mod Menu → ImpactFX → Basics or Impact Power. World shaders remain independently togglable.

## Phase 2: scene-aware impact frames

**New in Alpha 2:** Minecraft 26.3-native post-effect chains temporarily process the *actual world framebuffer* while an impact is active. The player's HUD remains legible. No special server mod or resource pack installation is required: post effects are included in this Fabric mod's resource assets.

Available looks:

| Preset | World treatment | HUD fallback |
|---|---|---|
| Manga Impact | High-contrast grayscale, ink-dark contours | Black/white speed lines, bursts |
| Anime Impact | Vibrant cel shading and scene outlines | Colored energy rings |
| Ink Burst | Sepia/ink threshold, textured halftone | Ink-like splashes |
| Retro Arcade | Pixelation, limited-color quantization | Stepped arcade bursts |
| Cinematic Impact | Desaturation, shadow contrast, vignette | Cinematic bars and rings |
| Minimal Impact | **No extra GPU post pass** | Quiet shock ring |
| Overdrive Finisher | Staged monochrome, zoom-punch and ripple | Heavy manga ink, cracks, expanding rings |

Low and strong GPU variants (plus four staged Overdrive effects) are selected by impact strength, and automatically use the softer one with reduced-flashing or reduced-motion accessibility settings. If a custom shader fails to load or you experience conflicts, disable **World shaders** on the Visuals page to keep the HUD effects.

The shader is active only during short impacts and uses Minecraft's own render graph, not raw OpenGL. Original shader/resource packs and multiplayer-provided effects are not intentionally cleared or replaced, but compatibility with Iris, Sodium, Vulkan and other effects still requires **in-game testing**.

## Usage

1. Install the built JAR in the Minecraft 26.3 Fabric `mods` folder with a compatible Fabric API and loader.
2. Open Mod Menu → ImpactFX → Configure.
3. Choose separate **Explosion** and **Mace** presets.
4. Enable **World shaders** under Visuals (enabled by default in Alpha 2); disable it for safe fallback.
5. Use the Preview buttons from an **open world** to test world-processing. The main-menu preview only displays the HUD effect.

The default reduced-flashing mode picks softer shader settings. All settings persist at `config/impactfx/impactfx.properties`; three custom profiles can be saved from the interface.

## Build / verification

- Java 25 + Gradle 9.7.1
- Run: `gradle build verifyLogic`
- The GitHub Actions `Build ImpactFX Fabric 26.3` workflow validates JSON post effects, compiles with Fabric Loom and tests pure Java logic.
- GitHub Actions artifacts contain both the mod JAR and the sources JAR; only the non-`sources` JAR belongs in `mods`.
- A **successful build is not a runtime or shader-GPU validation**. Please check startup, opening a world, explosion triggering, mace smashing, and the game log on the graphics backend you're using.

See [Phase 2 test checklist](docs/PHASE2_TEST.md).

## Work still planned

More accurately world-positioned GPU shockwaves, true continuous shader parameters, camera feedback (without physics changes), richer brush masks, wider modded explosion detection, further compatibility and benchmark testing.

Licensed MIT.
