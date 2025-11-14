package walksy.shieldstatus.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.item.model.special.ShieldModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ModelTransformationMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.Config;

@Mixin(ShieldModelRenderer.class)
public class ShieldModelRendererMixin {

    @Shadow
    @Final
    private ShieldEntityModel model;

    @Inject(method = "render(Lnet/minecraft/component/ComponentMap;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V", at = @At("HEAD"), cancellable = true)
    public void renderShieldModel(ComponentMap componentMap, ModelTransformationMode modelTransformationMode, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, boolean bl, CallbackInfo ci) {
        if (!Config.modEnabled) return;
        //if ((ShieldStatus.focusedEntity instanceof PlayerEntity || modelTransformationMode == ModelTransformationMode.GUI)) {
            ShieldStatus.getShieldModelRenderer().render(componentMap, this.model, modelTransformationMode, matrixStack, vertexConsumerProvider, i, j, bl);
            ci.cancel();
        //}
    }
}
