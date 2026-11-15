package dev.impactfx.render;

import dev.impactfx.config.ImpactConfig;
import dev.impactfx.impact.ImpactEngine;
import dev.impactfx.impact.ImpactMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * HUD extraction-only renderer: no FBO allocations, no GL state changes and no shader conflicts.
 * Phase 1 style renderer: directional overlays only. True per-pixel world monochrome
 * and scene-edge filters are intentionally NOT claimed to work here.
 */
public final class ImpactHudRenderer {
    public void render(GuiGraphicsExtractor g, Minecraft mc, ImpactConfig cfg, ImpactEngine.Frame frame) {
        if (frame == null) return; // Preview also works from the title screen (no world loaded).
        final long now = System.nanoTime();
        float t = frame.progress(now);
        if (t >= 1f) return;
        float strength = ImpactMath.envelope(t) * frame.strength();
        int w = g.guiWidth(), h = g.guiHeight();
        if (w <= 0 || h <= 0 || strength <= 0) return;
        long anchor = project(mc.getCameraEntity(), frame.x(), frame.y(), frame.z(), w, h);
        int cx = (int)(anchor >>> 32), cy = (int)anchor;
        float motion = cfg.reducedMotion ? 0.35f : 1f;
        switch (frame.preset()) {
            case MANGA -> manga(g,cfg,cx,cy,w,h,t,strength,motion);
            case ANIME -> anime(g,cfg,cx,cy,w,h,t,strength,motion);
            case INK -> ink(g,cfg,cx,cy,w,h,t,strength,motion);
            case RETRO -> retro(g,cfg,cx,cy,w,h,t,strength,motion);
            case CINEMATIC -> cinematic(g,cfg,cx,cy,w,h,t,strength,motion);
            case MINIMAL -> minimal(g,cfg,cx,cy,w,h,t,strength,motion);
        }
        if (frame.echo() && !cfg.reducedMotion && t > 0.38f) {
            float echo = strength * (1f - (t - 0.38f) / 0.62f) * 0.33f;
            int er = (int)((w + h) * (0.13f + t * 0.24f));
            ring(g,cx,cy,er,ImpactMath.argb(0xFFFFFF,echo),2);
        }
    }

    /** Perspective approximation uses the camera's world-space axes, no expensive ray casts. */
    private static long project(Entity camera, double x, double y, double z, int w, int h) {
        if (camera == null) return pack(w/2,h/2);
        Vec3 eye = camera.getEyePosition();
        Vec3 f = camera.getLookAngle().normalize();
        Vec3 right = new Vec3(0,1,0).cross(f);
        if (right.lengthSqr() < 0.001) return pack(w/2,h/2);
        right = right.normalize();
        Vec3 up = f.cross(right).normalize();
        Vec3 to = new Vec3(x-eye.x,y-eye.y,z-eye.z);
        double depth = to.dot(f);
        if (depth <= 0.1) return pack(w/2,h/2);
        double px = to.dot(right) / Math.max(depth,0.5);
        double py = to.dot(up) / Math.max(depth,0.5);
        int cx = (int)(w/2d + px * w * 0.48);
        int cy = (int)(h/2d - py * h * 0.80);
        return pack(Math.max(w/10,Math.min(w*9/10,cx)), Math.max(h/10,Math.min(h*9/10,cy)));
    }

    private static long pack(int x, int y) { return ((long)x << 32) | (y & 0xffffffffL); }

    private static void manga(GuiGraphicsExtractor g, ImpactConfig c, int x,int y,int w,int h,float t,float a,float motion) {
        if (c.tintWorld) g.fill(0,0,w,h,ImpactMath.argb(0xFFFFFF,a*0.38f));
        if (c.fullScreenCuts && !c.reducedFlashes && t < .10f) {
            int color = t < .05f ? 0xFFFFFF : 0x000000;
            g.fill(0,0,w,h,ImpactMath.argb(color,a * .64f));
        }
        radial(g,c,x,y,w,h,0x101010,a*1.2f,10,18,motion,0.2f);
        int r=(int)((w+h)*(.04f+t*.20f*motion));
        ring(g,x,y,r,ImpactMath.argb(0x151515,a*.80f),3);
        ring(g,x,y,(int)(r*.68f),ImpactMath.argb(0xFFFFFF,a*.9f),2);
        if(t<.14f) {
            g.fill(0,y-2,w,y+2,ImpactMath.argb(0x111111,a*.45f));
            g.fill(x-2,0,x+2,h,ImpactMath.argb(0xFFFFFF,a*.50f));
        }
    }

    private static void anime(GuiGraphicsExtractor g, ImpactConfig c, int x,int y,int w,int h,float t,float a,float motion) {
        int main = c.palette==1?0xFF4ABF:c.palette==2?0xEDC85C:0x5AE5FF;
        int alt = c.palette==1?0xFFDBEF:c.palette==2?0xFF9940:0xC870FF;
        if (c.tintWorld) g.fill(0,0,w,h,ImpactMath.argb(main,a*.18f));
        radial(g,c,x,y,w,h,alt,a*.85f,11,16,motion,0.35f);
        int r=(int)((w+h)*(.07f+t*.24f*motion));
        ring(g,x,y,r,ImpactMath.argb(main,a),4);
        ring(g,x,y,r+12,ImpactMath.argb(alt,a*.65f),2);
        sparkle(g,x,y,(int)(20+42*t*motion),ImpactMath.argb(0xFFFFFF,a*.85f));
    }

    private static void ink(GuiGraphicsExtractor g, ImpactConfig c, int x,int y,int w,int h,float t,float a,float motion) {
        if (c.tintWorld) g.fill(0,0,w,h,ImpactMath.argb(0xF3E9D5,a*.33f));
        int count = c.reducedMotion?8:18;
        for (int i=0;i<count;i++) {
            double theta=i*2.399963229728653;
            float distance = (24 + (i*29 % 140)) * (0.25f + t * motion);
            int bx=x+(int)(Math.cos(theta)*distance);
            int by=y+(int)(Math.sin(theta)*distance);
            int r=2+(i*7%11);
            int ink = i%5==0?0xFFF9EE:0x0C0D0E;
            g.fill(bx-r,by-r/2,bx+r,by+r/2,ImpactMath.argb(ink,a*(.35f+.4f*(i%3)/3)));
            if(i%3==0) g.fill(bx-r/3,by-r*2,bx+r/3,by+r*2,ImpactMath.argb(ink,a*.25f));
        }
        radial(g,c,x,y,w,h,0x06070A,a*.75f,6,13,motion,0.45f);
        ring(g,x,y,(int)((w+h)*(.08f+t*.18f*motion)),ImpactMath.argb(0x0C0D0E,a*.7f),4);
    }

    private static void retro(GuiGraphicsExtractor g, ImpactConfig c, int x,int y,int w,int h,float t,float a,float motion) {
        int base = c.palette==1?0xF67AB0:c.palette==2?0xFFA63E:0x70F9D4;
        int alt = c.palette==1?0x884FE5:c.palette==2?0xDFF071:0x605FEF;
        int stage=(int)(t*5);  // intentionally stepped
        if (c.tintWorld) g.fill(0,0,w,h,ImpactMath.argb(0x14204F,a*.25f));
        for (int i=0;i<24;i++) {
            int ax=(i*31+12)%11 - 5;
            int ay=(i*17+7)%9 - 4;
            int px=x+ax*(stage+2)*6;
            int py=y+ay*(stage+2)*4;
            int block=4+(i%3)*3;
            g.fill(px,py,px+block,py+block,ImpactMath.argb(i%2==0?base:alt,a*.85f));
        }
        int size=(stage+2)*19;
        ring(g,x,y,size,ImpactMath.argb(base,a),4);
        sparkle(g,x,y,19+stage*7,ImpactMath.argb(alt,a*.85f));
    }

    private static void cinematic(GuiGraphicsExtractor g, ImpactConfig c, int x,int y,int w,int h,float t,float a,float motion) {
        int tint = c.palette==1?0x3C4B64:c.palette==2?0x704322:0x342D2F;
        if (c.tintWorld) g.fill(0,0,w,h,ImpactMath.argb(tint,a*.38f));
        int bars=(int)(h*.07f*a);
        g.fill(0,0,w,bars,ImpactMath.argb(0x030306,a*.85f));
        g.fill(0,h-bars,w,h,ImpactMath.argb(0x030306,a*.85f));
        int r=(int)((w+h)*(.06f+t*.16f*motion));
        ring(g,x,y,r,ImpactMath.argb(0xEFE2D0,a*.56f),2);
        ring(g,x,y,r+6,ImpactMath.argb(0xFFF2DF,a*.23f),2);
        int v=Math.max(2,w/34);
        for(int i=0;i<4;i++) {
            float side=a*.04f*(4-i);
            g.fill(i*v,0,(i+1)*v,h,ImpactMath.argb(0x060305,side));
            g.fill(w-(i+1)*v,0,w-i*v,h,ImpactMath.argb(0x060305,side));
        }
    }

    private static void minimal(GuiGraphicsExtractor g, ImpactConfig c, int x,int y,int w,int h,float t,float a,float motion) {
        int col=c.palette==1?0xFFA5C4:c.palette==2?0xE9D18A:0xFFFFFF;
        int r=(int)(14+34*t*motion);
        ring(g,x,y,r,ImpactMath.argb(col,a*.55f),1);
        if (c.tintWorld) g.fill(0,0,w,h,ImpactMath.argb(col,a*.065f));
    }

    private static void radial(GuiGraphicsExtractor g, ImpactConfig cfg,int x,int y,int w,int h,
                               int rgb,float alpha,int maxRays,int segments,float motion,float offset) {
        int count=Math.min(maxRays,2+cfg.speedLines/8);
        for (int i=0;i<count;i++) {
            double angle=(i*2*Math.PI/count)+offset;
            int length=(int)(Math.max(w,h)*(.19f+ .12f*(i%4))*motion);
            int start=(int)(Math.min(w,h)*(.06f+(i%3)*.025f));
            double ca=Math.cos(angle),sa=Math.sin(angle);
            int c=ImpactMath.argb(rgb,alpha*(.45f+.05f*(i%4)));
            for(int k=0;k<segments;k++) {
                float z=k/(float)segments;
                int px=x+(int)(ca*(start+length*z));
                int py=y+(int)(sa*(start+length*z));
                int thick=1+((k%4)==0?2:0);
                g.fill(px-thick,py-thick,px+thick,py+thick,c);
            }
        }
    }
    private static void ring(GuiGraphicsExtractor g,int x,int y,int r,int argb,int thickness) {
        if(r<=0) return;
        for(int n=0;n<thickness;n++) g.outline(x-r-n,y-r-n,2*(r+n),2*(r+n),argb);
    }
    private static void sparkle(GuiGraphicsExtractor g,int x,int y,int r,int argb) {
        g.fill(x-r,y-2,x+r,y+2,argb);
        g.fill(x-2,y-r,x+2,y+r,argb);
        g.fill(x-r/2,y-r/2,x-r/2+3,y+r/2,argb);
        g.fill(x+r/2,y-r/2,x+r/2+3,y+r/2,argb);
    }
}
