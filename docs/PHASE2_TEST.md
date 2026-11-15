# ImpactFX Alpha 2 — In-game testing checklist

**Important:** GitHub Actions can validate shaders *exist and are correctly referenced*, but cannot prove GPU rendering or mixin behavior on a local client.

Test on Fabric Minecraft 26.3 with Java 25 and matching Fabric API.

1. **Startup:** load the new JAR. There should be no Mixin injection failures mentioning `GameRendererPostMixin`.
2. **World load:** create/open a world with no shaders. Avoid old alpha JARs being installed alongside this one.
3. **Explosion:** summon TNT at different distances. Observe an appropriate effect; no duplicate continuous flashing.
4. **Mace:** land a genuine falling mace smash on a mob and check effect direction.
5. **Manga shader:** Mod Menu → ImpactFX → Visuals → World shaders ON. Choose Manga and press Preview **while in a world**. The scene itself should temporarily become mostly monochrome.
6. **Controls:** compare World shaders ON vs OFF. OFF should keep original HUD rings but never change scene colors.
7. **Presets:** cycle all six. Minimal should have no GPU scene pass; other five should have visually distinct styles.
8. **Accessibility:** enable Reduced flashes and Reduced motion, and verify soft variants are used.
9. **Compatibility:** test with Sodium and then Iris/shaderpacks, separately. If anything fails, turn World shaders OFF and save your latest.log.
10. **Perf:** compare F3 frame times while repeatedly previewing impacts. Try OpenGL and Vulkan if available.
11. **Other effects:** join a world/server using a vanilla post effect and verify ImpactFX doesn't clear it.
12. **Disabling:** switch ImpactFX OFF. On the very next frame the scene must return to its prior appearance.

Known limitations:
- No client runtime or GPU validation has been completed.
- Post-processing changes strength in two tiers rather than a continuous shader uniform.
- The config preview screen shows HUD visual overlays; for testing the world shader, the in-world background must be visible.
- Actual camera shake and advanced refraction have not been implemented.

Report `latest.log`, any crash report and if possible a short recording plus GPU/backend information.
