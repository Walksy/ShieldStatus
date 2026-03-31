package walksy.shieldstatus.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.Config;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleSoundEvent",
            at = @At("HEAD"))
    public void onSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        if (!Config.modEnabled) return;
        if (packet.getSound().getRegisteredName().toLowerCase().contains("shield.break")) {
            ShieldStatus.getShieldStateManager().handleBreakPacket(packet.getX(), packet.getY(), packet.getZ());
        }
    }
}
