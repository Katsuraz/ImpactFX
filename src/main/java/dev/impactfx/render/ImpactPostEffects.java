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
            // User toggles must also disable the actual framebuffer disturbance.
            if ((!cfg.shockwave && (beat == ImpactSequence.Beat.SHOCK || beat == ImpactSequence.Beat.ECHO))
                    || (!cfg.screenPunch && (beat == ImpactSequence.Beat.SNAP || beat == ImpactSequence.Beat.STRIKE)))
                return OVERDRIVE_CLEAN[tierIndex];
            return OVERDRIVE[beat.ordinal()][tierIndex];
        }
        return tier == Tier.STRONG
                ? STRONG[frame.preset().ordinal()] : SOFT[frame.preset().ordinal()];
    }
}
