package dev.impactfx.mixin;

import dev.impactfx.render.ImpactPostEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3 uses GameRenderState.requestedPostEffects. Add only our transient ID
 * after vanilla has collected the server's and spectator's effects. Never clear
 * or mutate the persistent requested-post-effects list.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererPostMixin {
    @Inject(method = "extract", at = @At("TAIL"))
    private void impactfx$appendWorldImpact(DeltaTracker tracker, boolean advanceGameTime, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        Identifier effect = ImpactPostEffects.current(mc);
        if (effect == null) return;
        var requested = ((GameRenderer)(Object)this).gameRenderState().requestedPostEffects;
        if (!requested.contains(effect)) requested.add(effect);
    }
}
