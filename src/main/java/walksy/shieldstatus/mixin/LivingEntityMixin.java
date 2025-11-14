package walksy.shieldstatus.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.Config;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "handleStatus", at = @At("HEAD"))
    public void onHandleStatusUpdate(byte status, CallbackInfo ci) {
        if (!Config.modEnabled) return;
        LivingEntity entity = LivingEntity.class.cast(this);
        if (entity instanceof PlayerEntity player) {
            ShieldStatus.getShieldStateManager().handleEntityStatus(player, status);
        }
    }
}
