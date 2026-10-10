package walksy.shieldstatus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.ShieldStatusConfig;

@Mixin(ShieldSpecialRenderer.class)
public class ShieldSpecialRendererMixin {

    @Inject(method = "<init>", at = @At("HEAD"))
    private static void init(MaterialSet sprites, ShieldModel model, CallbackInfo ci) {
        ShieldStatus.setupShieldSpecialSubmitter(model, sprites);
    }

    @Inject(method = "submit(Lnet/minecraft/core/component/DataComponentMap;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V", at = @At("HEAD"), cancellable = true)
    private void submit(DataComponentMap components, ItemDisplayContext context, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor, CallbackInfo ci) {
        if (!ShieldStatusConfig.modEnabled) return;
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        ShieldStatus.getShieldSpecialSubmitter().submit(context, components, poseStack, submitNodeCollector, lightCoords, overlayCoords, hasFoil);
        poseStack.popPose();
        ci.cancel();
    }
}
