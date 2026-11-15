package dev.impactfx.render;

import dev.impactfx.ImpactFxClient;
import dev.impactfx.config.ImpactConfig;
import dev.impactfx.config.ImpactPreset;
import dev.impactfx.gui.ImpactConfigScreen;
import dev.impactfx.impact.ImpactEngine;
import dev.impactfx.impact.ImpactMath;
import dev.impactfx.impact.ImpactSequence;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Selects an optional 26.3 post-effect chain for the actual world framebuffer.
 * The original six HUD presets are intentionally kept as an always-available fallback.
 * No shaders are requested while idle. Effects on the player/server are never replaced.
 */
public final class ImpactPostEffects {
    private static final Identifier[] SOFT = new Identifier[ImpactPreset.values().length];
    private static final Identifier[] STRONG = new Identifier[ImpactPreset.values().length];
    private static final Identifier[][] OVERDRIVE = new Identifier[ImpactSequence.Beat.values().length][2];
    private static final Identifier[] OVERDRIVE_CLEAN = new Identifier[2];
    // 5x3 screen grid follows the location of each hit rather than fixed center.
    private static final Identifier[][][][] OVERDRIVE_GRID = new Identifier[7][2][5][3];
    private static final Identifier[][][] CLEAN_GRID = new Identifier[2][5][3];
    static {
        for (ImpactPreset preset : ImpactPreset.values()) {
            String name = "scene_" + preset.name().toLowerCase(java.util.Locale.ROOT);
            SOFT[preset.ordinal()] = Identifier.fromNamespaceAndPath("impactfx", name + "_soft");
            STRONG[preset.ordinal()] = Identifier.fromNamespaceAndPath("impactfx", name + "_strong");
        }
        for (ImpactSequence.Beat beat : ImpactSequence.Beat.values()) {
            String name = beat.name().toLowerCase(java.util.Locale.ROOT);
            OVERDRIVE[beat.ordinal()][0] = Identifier.fromNamespaceAndPath("impactfx", "scene_overdrive_" + name + "_soft");
            OVERDRIVE[beat.ordinal()][1] = Identifier.fromNamespaceAndPath("impactfx", "scene_overdrive_" + name + "_strong");
        }
        OVERDRIVE_CLEAN[0] = Identifier.fromNamespaceAndPath("impactfx", "scene_overdrive_clean_soft");
        OVERDRIVE_CLEAN[1] = Identifier.fromNamespaceAndPath("impactfx", "scene_overdrive_clean_strong");
        for(int gx=0;gx<5;gx++) for(int gy=0;gy<3;gy++) {
            String location = "_x" + gx + "_y" + gy;
            for(int tier=0;tier<2;tier++) {
                String weight = tier == 0 ? "_soft" : "_strong";
                CLEAN_GRID[tier][gx][gy] = Identifier.fromNamespaceAndPath("impactfx",
                    "scene_overdrive_clean" + weight + location);
                String[] names={"snap","strike","shock0","shock1","shock2","echo","clean"};
                for(int stage=0;stage<names.length;stage++) {
                    OVERDRIVE_GRID[stage][tier][gx][gy] = Identifier.fromNamespaceAndPath(
                        "impactfx","scene_overdrive_" + names[stage] + weight + location);
                }
            }
        }
    }
    private ImpactPostEffects() {}

    public enum Tier { NONE, SOFT, STRONG }

    /** This logic stays pure for easy unit testing. */
    public static Tier tier(float envelopeStrength, boolean reducedFlashes, boolean reducedMotion) {
        if (!Float.isFinite(envelopeStrength) || envelopeStrength < 0.095f) return Tier.NONE;
        if (reducedFlashes || reducedMotion || envelopeStrength < 0.38f) return Tier.SOFT;
        return Tier.STRONG;
    }

    /** Called during world-frame extraction, before Minecraft applies post effects. */
    public static Identifier current(Minecraft mc) {
        ImpactConfig cfg = ImpactFxClient.config;
        if (!cfg.enabled || !cfg.worldShaders || mc.level == null) return null;
        if (cfg.hideDuringGui && mc.gui.screen() != null
                && !(mc.gui.screen() instanceof ImpactConfigScreen)) return null;
        ImpactEngine.Frame frame = ImpactFxClient.ENGINE.frame(System.nanoTime());
        if (frame == null || frame.preset() == ImpactPreset.MINIMAL) return null;
        long now = System.nanoTime();
        float progress = frame.progress(now);
        float strength = ImpactSequence.power(progress, frame.strength(), cfg.drama);
        Tier tier = tier(strength, cfg.reducedFlashes, cfg.reducedMotion);
        if (tier == Tier.NONE) return null;
        if (frame.preset() == ImpactPreset.OVERDRIVE) {
            ImpactSequence.Beat beat = ImpactSequence.beat(progress);
            int tierIndex = tier == Tier.SOFT ? 0 : 1;
            ImpactProjection.Point point = ImpactProjection.project(mc.getCameraEntity(),
                frame.x(),frame.y(),frame.z(),mc.getWindow().getWidth(),mc.getWindow().getHeight());
            int gx=point.gridX(), gy=point.gridY();
            // User toggles must also disable the actual framebuffer disturbance.
            if ((!cfg.shockwave && (beat == ImpactSequence.Beat.SHOCK || beat == ImpactSequence.Beat.ECHO))
                    || (!cfg.screenPunch && (beat == ImpactSequence.Beat.SNAP || beat == ImpactSequence.Beat.STRIKE)))
                return CLEAN_GRID[tierIndex][gx][gy];
            int stage;
            switch (beat) {
                case SNAP -> stage=0;
                case STRIKE -> stage=1;
                case SHOCK -> stage=2+Math.min(2,Math.max(0,(int)((progress-.30f)/(.47f/3f))));
                default -> stage=5;
            }
            return OVERDRIVE_GRID[stage][tierIndex][gx][gy];
        }
        return tier == Tier.STRONG
                ? STRONG[frame.preset().ordinal()] : SOFT[frame.preset().ordinal()];
    }
}
