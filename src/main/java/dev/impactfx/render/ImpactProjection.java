package dev.impactfx.render;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Projects world points to screen coordinates before GUI scale is applied. */
public final class ImpactProjection {
    private ImpactProjection() {}

    /** Normalized GUI top-left origin, clamped to safe screen edges for offscreen targets. */
    public record Point(float x, float y, boolean offscreen) {
        public int screenX(int width) { return Math.round(x * width); }
        public int screenY(int height) { return Math.round(y * height); }
        /** The scene shader uses bottom-left image coordinates. */
        public float shaderY() { return 1f - y; }
        public int gridX() { return Math.min(4, Math.max(0, (int)(x * 5f))); }
        public int gridY() { return Math.min(2, Math.max(0, (int)(y * 3f))); }
    }
    public static Point project(Entity camera, double x, double y, double z, int width, int height) {
        if (camera == null) return new Point(.5f, .5f, false);
        Vec3 forward = camera.getLookAngle().normalize();
        Vec3 right = new Vec3(0,1,0).cross(forward);
        if (right.lengthSqr() < .001) right = new Vec3(1,0,0);
        else right = right.normalize();
        Vec3 up = forward.cross(right).normalize();
        Vec3 offset = new Vec3(x,y,z).subtract(camera.getEyePosition());
        double depth = offset.dot(forward);
        double side = offset.dot(right), rise = offset.dot(up);
        if (depth <= .05) {
            // Behind the player: point to the screen edge, not the false center.
            double angle = Math.atan2(-rise, side);
            double normX = .5 + Math.cos(angle) * .44;
            double normY = .5 + Math.sin(angle) * .44;
            return new Point(clamp(normX),clamp(normY),true);
        }
        double aspect = Math.max(.25, (double)Math.max(1,width)/Math.max(1,height));
        double halfTanY = Math.tan(Math.toRadians(70.0)*.5);
        double nx = .5 + side / (depth * halfTanY * 2 * aspect);
        double ny = .5 - rise / (depth * halfTanY * 2);
        boolean out = nx<.04 || nx>.96 || ny<.04 || ny>.96;
        return new Point(clamp(nx),clamp(ny),out);
    }
    private static float clamp(double a) { return (float)Math.max(.04,Math.min(.96,a)); }
}
