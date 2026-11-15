package dev.impactfx.impact;

/** Pure logic; intentionally independent of Minecraft so it can be unit-tested offline. */
public final class ImpactMath {
    private ImpactMath() {}
    public static float clamp(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }
    public static float eventStrength(float power, double distance, float maxDistance) {
        if (distance >= maxDistance) return 0f;
        float base = clamp(power / 6f, 0.24f, 1f);
        float closeness = clamp(1f - (float)(distance / maxDistance), 0f, 1f);
        return base * closeness * closeness;
    }
    public static float envelope(float progress) {
        if (progress <= 0 || progress >= 1) return 0;
        float attack = clamp(progress / 0.12f, 0, 1);
        float decay = (float)Math.pow(1f - progress, 1.55f);
        return attack * decay;
    }
    public static int alpha(float opacity) { return (int)(255 * clamp(opacity, 0f, 1f)); }
    public static int argb(int rgb, float opacity) { return (alpha(opacity) << 24) | (rgb & 0xFFFFFF); }
}
