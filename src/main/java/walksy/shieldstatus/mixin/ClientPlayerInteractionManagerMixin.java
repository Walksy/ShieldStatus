package walksy.shieldstatus.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.Config;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    public void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (!Config.modEnabled) return;
        if (target instanceof PlayerEntity targetPlayer) {
            ShieldStatus.getShieldStateManager().handlePlayerAttack(targetPlayer);
        }
    }
}
