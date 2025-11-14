package walksy.shieldstatus.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.item.model.special.ShieldModelRenderer;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemDisplayContext;
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

    @Shadow
    @Final
    private SpriteHolder spriteHolder;

    @Inject(method = "render(Lnet/minecraft/component/ComponentMap;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IIZI)V", at = @At("HEAD"), cancellable = true)
    public void renderShieldModel(ComponentMap componentMap, ItemDisplayContext itemDisplayContext, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, int i, int j, boolean bl, int k, CallbackInfo ci) {
        if (!Config.modEnabled) return;
        ShieldStatus.getShieldModelRenderer().render(componentMap, this.model, this.spriteHolder, itemDisplayContext, matrixStack, orderedRenderCommandQueue, i, j, k, bl);
        ci.cancel();
    }
}
