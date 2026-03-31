package walksy.shieldstatus.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.Config;

@Mixin(LocalPlayer.class)
public class PlayerEntityMixin {

    @Inject(method = "tick",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V",
            shift = At.Shift.AFTER))
    public void onTick(CallbackInfo ci) {
        if (!Config.modEnabled) return;
        ShieldStatus.getShieldStateManager().update();
    }
}
