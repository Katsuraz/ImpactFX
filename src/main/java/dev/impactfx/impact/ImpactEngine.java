package dev.impactfx.impact;

import dev.impactfx.config.ImpactConfig;
import dev.impactfx.config.ImpactPreset;

/** Holds at most one short-lived impact; keeps chain explosions bounded. */
public final class ImpactEngine {
    public enum Kind { EXPLOSION, MACE }
    public record Frame(Kind kind, ImpactPreset preset, double x, double y, double z,
                        float strength, long startedNanos, long durationNanos, boolean echo) {
        public float progress(long now) {
            return ImpactMath.clamp((float)((now - startedNanos) / (double)durationNanos), 0, 1);
        }
    }

    private Frame active;
    private long lastStarted;
    private long lastSeen;
    private double lastX, lastY, lastZ;
    private Kind lastKind;
    private boolean lastWasPreview;

    public Frame frame(long now) {
        if (active != null && now - active.startedNanos >= active.durationNanos) active = null;
        return active;
    }

    public void clear() { active = null; lastSeen = 0; lastStarted = 0; lastWasPreview = false; }

    public boolean trigger(ImpactConfig cfg, Kind kind, double x, double y, double z,
                           float strength, long now, boolean preview) {
        if (!preview && (!cfg.enabled || (kind == Kind.EXPLOSION && !cfg.explosions) ||
             (kind == Kind.MACE && !cfg.mace))) return false;
        strength = ImpactMath.clamp(strength * cfg.intensity / 100f, 0f, 1f);
        if (strength < 0.05f) return false;
        if (!preview && !lastWasPreview && lastKind == kind && now - lastSeen < 110_000_000L
            && squaredDistance(x,y,z,lastX,lastY,lastZ) < 9d) return false;
        if (!preview && lastStarted != 0 && now - lastStarted < cfg.cooldownMs * 1_000_000L) {
            if (active == null || strength < active.strength + 0.22f) return false;
        }
        ImpactPreset preset = kind == Kind.EXPLOSION ? cfg.explosionPreset : cfg.macePreset;
        long duration = cfg.durationMs * 1_000_000L;
        active = new Frame(kind,preset,x,y,z,strength,now,duration,cfg.echo && strength > 0.65f);
        lastSeen = now;
        lastStarted = now;
        lastKind = kind;
        lastX = x; lastY = y; lastZ = z;
        lastWasPreview = preview;
        return true;
    }

    private static double squaredDistance(double ax, double ay, double az,
                                          double bx, double by, double bz) {
        double dx=ax-bx,dy=ay-by,dz=az-bz;
        return dx*dx+dy*dy+dz*dz;
    }
}
