package dev.impactfx;

import dev.impactfx.config.ImpactConfig;
import dev.impactfx.impact.ImpactEngine;
import dev.impactfx.impact.ImpactMath;
import dev.impactfx.render.ImpactHudRenderer;
import dev.impactfx.gui.ImpactConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class ImpactFxClient implements ClientModInitializer {
    public static ImpactConfig config = ImpactConfig.load();
    public static final ImpactEngine ENGINE = new ImpactEngine();
    public static final ImpactHudRenderer RENDERER = new ImpactHudRenderer();

    @Override
    public void onInitializeClient() {
        HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath("impactfx", "impact_frames"), ImpactFxClient::renderHud);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null && !(client.gui.screen() instanceof ImpactConfigScreen)) ENGINE.clear();
        });
        System.out.println("[ImpactFX] 0.3.0-alpha.1 loaded; power sequences and world effects are optional");
    }

    private static void renderHud(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !config.enabled) return;
        if (config.hideDuringGui && mc.gui.screen() != null) return;
        if (!config.showWithF1 && mc.gui.hud.isHidden()) return;
        RENDERER.render(graphics,mc,config,ENGINE.frame(System.nanoTime()));
    }

    /** Invoked on the client thread by the vanilla explosion packet handler mixin. */
    public static void onExplosion(Vec3 center, float radius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !config.explosions) return;
        double distance = mc.player.getEyePosition().distanceTo(center);
        float strength = ImpactMath.eventStrength(Math.max(1,radius),distance,config.maxDistance);
        ENGINE.trigger(config,ImpactEngine.Kind.EXPLOSION,center.x,center.y,center.z,
            strength,System.nanoTime(),false);
    }

    /** Mace smash sounds are emitted by vanilla only when a smash attack succeeds. */
    public static void onMaceSound(String identifier, double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !config.mace) return;
        float power;
        if (identifier.endsWith("item.mace.smash_ground_heavy")) power=1f;
        else if (identifier.endsWith("item.mace.smash_ground")) power=.60f;
        else if (identifier.endsWith("item.mace.smash_air")) power=.42f;
        else return;
        double distance = mc.player.getEyePosition().distanceTo(new Vec3(x,y,z));
        if(distance >= config.maxDistance) return;
        float strength = power * (float)Math.pow(1-distance/config.maxDistance,1.25);
        ENGINE.trigger(config,ImpactEngine.Kind.MACE,x,y,z,strength,System.nanoTime(),false);
    }

    public static void preview(boolean mace) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 spot = mc.player==null?new Vec3(0,64,3):mc.player.getEyePosition().add(mc.player.getLookAngle().scale(4));
        ENGINE.trigger(config,mace?ImpactEngine.Kind.MACE:ImpactEngine.Kind.EXPLOSION,
            spot.x,spot.y,spot.z,.95f,System.nanoTime(),true);
    }
    public static void openConfig() {
        Minecraft mc=Minecraft.getInstance();
        mc.gui.setScreen(new ImpactConfigScreen(mc.gui.screen()));
    }
}
