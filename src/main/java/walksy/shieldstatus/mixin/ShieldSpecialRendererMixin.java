package walksy.shieldstatus.mixin;

import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.ShieldStatus;

@Mixin(ShieldSpecialRenderer.class)
public class ShieldSpecialRendererMixin {

    @Inject(method = "<init>",
            at = @At("HEAD"))
    private static void init(SpriteGetter sprites, ShieldModel model, CallbackInfo ci) {
        ShieldStatus.setupShieldModelSubmitter(model, sprites);
    }
}
