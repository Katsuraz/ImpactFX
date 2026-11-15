package dev.impactfx.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** No hard Cloth Config dependency: save and load using the JDK only. */
public final class ImpactConfig {
    public boolean enabled = true;
    public boolean explosions = true;
    public boolean mace = true;
    public ImpactPreset explosionPreset = ImpactPreset.CINEMATIC;
    public ImpactPreset macePreset = ImpactPreset.MANGA;
    public int intensity = 85;          // 0..100
    public int durationMs = 170;        // 60..400
    public int cooldownMs = 180;        // 80..700
    public int speedLines = 55;         // 0..100
    public int cameraShake = 0;         // Reserved until camera integration is safe
    public int palette = 0;             // 0..2
    public int maxDistance = 42;        // 8..128
    public boolean echo = true;
    public boolean fullScreenCuts = true;
    public boolean reducedFlashes = true;
    public boolean reducedMotion = false;
    public boolean hideDuringGui = true;
    public boolean showWithF1 = true;
    public boolean tintWorld = true;    // Optional HUD tint
    public boolean worldShaders = true;   // Disable for pure HUD fallback (shader compatibility)

    private static final Path ROOT = Path.of("config", "impactfx");
    private static final Path SETTINGS = ROOT.resolve("impactfx.properties");

    public static ImpactConfig load() { return loadAt(SETTINGS); }
    public static ImpactConfig loadProfile(int slot) {
        return loadAt(profilePath(slot));
    }
    public void save() { saveAt(SETTINGS); }
    public void saveProfile(int slot) { saveAt(profilePath(slot)); }

    private static Path profilePath(int slot) {
        if (slot < 1 || slot > 3) throw new IllegalArgumentException("Slot 1-3 only");
        return ROOT.resolve("profile-" + slot + ".properties");
    }

    public static ImpactConfig loadAt(Path path) {
        ImpactConfig c = new ImpactConfig();
        if (!Files.isRegularFile(path)) return c;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        } catch (IOException ex) {
            System.err.println("[ImpactFX] Could not load " + path + ": " + ex.getMessage());
            return c;
        }
        c.enabled = bool(p,"enabled",c.enabled);
        c.explosions = bool(p,"explosions",c.explosions);
        c.mace = bool(p,"mace",c.mace);
        c.explosionPreset = ImpactPreset.parse(p.getProperty("explosionPreset", "CINEMATIC"),c.explosionPreset);
        c.macePreset = ImpactPreset.parse(p.getProperty("macePreset", "MANGA"),c.macePreset);
        c.intensity = num(p,"intensity",c.intensity,0,100);
        c.durationMs = num(p,"durationMs",c.durationMs,60,400);
        c.cooldownMs = num(p,"cooldownMs",c.cooldownMs,80,700);
        c.speedLines = num(p,"speedLines",c.speedLines,0,100);
        c.cameraShake = num(p,"cameraShake",c.cameraShake,0,100);
        c.maxDistance = num(p,"maxDistance",c.maxDistance,8,128);
        c.palette = num(p,"palette",c.palette,0,2);
        c.echo = bool(p,"echo",c.echo);
        c.fullScreenCuts = bool(p,"fullScreenCuts",c.fullScreenCuts);
        c.reducedFlashes = bool(p,"reducedFlashes",c.reducedFlashes);
        c.reducedMotion = bool(p,"reducedMotion",c.reducedMotion);
        c.hideDuringGui = bool(p,"hideDuringGui",c.hideDuringGui);
        c.showWithF1 = bool(p,"showWithF1",c.showWithF1);
        c.tintWorld = bool(p,"tintWorld",c.tintWorld);
        c.worldShaders = bool(p,"worldShaders",c.worldShaders);
        return c;
    }
    public void saveAt(Path path) {
        Properties p = new Properties();
        p.setProperty("enabled",Boolean.toString(enabled));
        p.setProperty("explosions",Boolean.toString(explosions));
        p.setProperty("mace",Boolean.toString(mace));
        p.setProperty("explosionPreset",explosionPreset.name());
        p.setProperty("macePreset",macePreset.name());
        p.setProperty("intensity",Integer.toString(intensity));
        p.setProperty("durationMs",Integer.toString(durationMs));
        p.setProperty("cooldownMs",Integer.toString(cooldownMs));
        p.setProperty("speedLines",Integer.toString(speedLines));
        p.setProperty("cameraShake",Integer.toString(cameraShake));
        p.setProperty("palette",Integer.toString(palette));
        p.setProperty("maxDistance",Integer.toString(maxDistance));
        p.setProperty("echo",Boolean.toString(echo));
        p.setProperty("fullScreenCuts",Boolean.toString(fullScreenCuts));
        p.setProperty("reducedFlashes",Boolean.toString(reducedFlashes));
        p.setProperty("reducedMotion",Boolean.toString(reducedMotion));
        p.setProperty("hideDuringGui",Boolean.toString(hideDuringGui));
        p.setProperty("showWithF1",Boolean.toString(showWithF1));
        p.setProperty("tintWorld",Boolean.toString(tintWorld));
        p.setProperty("worldShaders",Boolean.toString(worldShaders));
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) { p.store(out,"ImpactFX client settings"); }
        } catch (IOException ex) {
            System.err.println("[ImpactFX] Could not save " + path + ": " + ex.getMessage());
        }
    }
    private static boolean bool(Properties p,String k,boolean d) {
        return Boolean.parseBoolean(p.getProperty(k,Boolean.toString(d)));
    }
    private static int num(Properties p,String k,int d,int min,int max) {
        try { return Math.max(min,Math.min(max,Integer.parseInt(p.getProperty(k)))); }
        catch (Exception ignored) { return d; }
    }
}
