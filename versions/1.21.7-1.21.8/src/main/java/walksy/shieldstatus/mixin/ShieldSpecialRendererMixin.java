package walksy.shieldstatus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.ShieldStatusConfig;

@Mixin(ShieldSpecialRenderer.class)
public class ShieldSpecialRendererMixin {
    @Shadow @Final private ShieldModel model;

    @Inject(method = "render(Lnet/minecraft/core/component/DataComponentMap;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IIZ)V", at = @At("HEAD"), cancellable = true)
    private void render(DataComponentMap components, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int lightCoords, int overlayCoords, boolean hasFoil, CallbackInfo ci) {
        if (!ShieldStatusConfig.modEnabled) return;
        ShieldStatus.getShieldSpecialSubmitter().render(this.model, context, components, poseStack, bufferSource, lightCoords, overlayCoords, hasFoil);
        ci.cancel();
    }
}
