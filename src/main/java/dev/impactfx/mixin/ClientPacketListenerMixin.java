package dev.impactfx.mixin;

import dev.impactfx.ImpactFxClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The vanilla handlers have already processed their packets when we observe them. */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleExplosion", at = @At("TAIL"))
    private void impactfx$onExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
        ImpactFxClient.onExplosion(packet.center(), packet.radius());
    }

    @Inject(method = "handleSoundEvent", at = @At("TAIL"))
    private void impactfx$onPositionalSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        ImpactFxClient.onMaceSound(packet.getSound().value().location().toString(),
            packet.getX(),packet.getY(),packet.getZ());
    }

    @Inject(method = "handleSoundEntityEvent", at = @At("TAIL"))
    private void impactfx$onEntitySound(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity=mc.level.getEntity(packet.getId());
        if (entity==null) return;
        Vec3 pos=entity.position();
        ImpactFxClient.onMaceSound(packet.getSound().value().location().toString(),pos.x,pos.y,pos.z);
    }
}
