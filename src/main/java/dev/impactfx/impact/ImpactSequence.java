package dev.impactfx.impact;

/** Four deliberate visual beats; intentionally no game-logic pause or tick manipulation. */
public final class ImpactSequence {
    private ImpactSequence() {}
    public enum Beat { SNAP, STRIKE, SHOCK, ECHO }
    public static Beat beat(float progress) {
        if (progress < 0.085f) return Beat.SNAP;
        if (progress < 0.30f) return Beat.STRIKE;
        if (progress < 0.77f) return Beat.SHOCK;
        return Beat.ECHO;
    }
    /** Progression is driven by elapsed wall time, not render FPS. */
    public static float ringProgress(float progress) {
        float normalized = (progress - 0.20f) / 0.70f;
        return ImpactMath.clamp(normalized, 0, 1);
    }
    public static float power( float progress, float frameStrength, int drama) {
        float scale = 1.0f + ImpactMath.clamp(drama / 100f, 0, 1) * .72f;
        float t = ImpactMath.clamp(progress,0,1);
        float rise = ImpactMath.clamp(t/.055f,0,1);
        float release = t < .66f ? 1.0f : (float)Math.pow(ImpactMath.clamp((1.0f-t)/.34f,0,1),1.25);
        // Sustained force for long cinematic frames; no abrupt constant screen flashing.
        float envelope = rise * release * (1.0f - t*.12f);
        return ImpactMath.clamp(envelope * frameStrength * scale, 0, 1);
    }
}
