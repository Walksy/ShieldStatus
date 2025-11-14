package walksy.shieldstatus.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.Config;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onPlaySound", at = @At("HEAD"))
    public void onSound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        if (!Config.modEnabled) return;
        if (packet.getSound().getIdAsString().toLowerCase().contains("shield.break")) {
            ShieldStatus.getShieldStateManager().handleBreakPacket(packet.getX(), packet.getY(), packet.getZ());
        }
    }
}
