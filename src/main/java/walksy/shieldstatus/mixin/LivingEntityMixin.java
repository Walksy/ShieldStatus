package walksy.shieldstatus.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.manager.ShieldDataManager;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "handleStatus", at = @At("HEAD"))
    private void handleStatus(byte status, CallbackInfo ci)
    {
        ShieldDataManager.INSTANCE.handlePlayerByteStatusEvent(status, this);
    }
}
