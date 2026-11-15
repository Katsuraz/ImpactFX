# ImpactFX — Cinematic Impact Frames for Fabric 26.3

**Version:** 0.2.0-alpha.1 — Phase 2 scene post-processing (compiled CI builds are not equivalent to runtime testing)

A lightweight, client-side Minecraft Fabric mod that adds stylized impact frames to explosions and successful mace smash attacks. The mod doesn't modify damage, knockback, physics, or time.

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

Low and strong GPU variants are selected by impact strength, and automatically use the softer one with reduced-flashing or reduced-motion accessibility settings. If a custom shader fails to load or you experience conflicts, disable **World shaders** on the Visuals page to keep the HUD effects.

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

True per-impact GPU uniforms (smooth continuous shader intensity), optional shockwave refraction, richer animated brush masks, camera shake/hit-stop illusion, wider modded explosion detection, further compatibility and benchmark testing.

Licensed MIT.
