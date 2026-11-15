package dev.impactfx.config;

/** The styles use different geometry, contrast and motion—not simply color tints. */
public enum ImpactPreset {
    MANGA("Manga Impact"),
    ANIME("Anime Impact"),
    INK("Ink Burst"),
    RETRO("Retro Arcade"),
    CINEMATIC("Cinematic Impact"),
    MINIMAL("Minimal Impact"),
    OVERDRIVE("Overdrive Finisher");

    public final String label;
    ImpactPreset(String label) { this.label = label; }
    public ImpactPreset next() { return values()[(ordinal() + 1) % values().length]; }
    public static ImpactPreset parse(String raw, ImpactPreset fallback) {
        try { return valueOf(raw.toUpperCase(java.util.Locale.ROOT)); }
        catch (Exception ex) { return fallback; }
    }
}
