package walksy.shieldstatus.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.item.model.special.ShieldModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import walksy.shieldstatus.manager.ConfigManager;
import walksy.shieldstatus.manager.ShieldRenderManager;


@Mixin(ShieldModelRenderer.class)
public class BuiltinModelItemRendererMixin {


    @Shadow
    @Final
    private ShieldEntityModel model;

    @Unique ItemStack itemStack;

    @Inject(method = "getData(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/component/ComponentMap;", at = @At("HEAD"))
    public void getData(ItemStack itemStack, CallbackInfoReturnable<ComponentMap> cir)
    {
        this.itemStack = itemStack;
    }

    @Inject(at = {@At("HEAD")}, method = {"render(Lnet/minecraft/component/ComponentMap;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V"}, cancellable = true)
    public void render(ComponentMap componentMap, ModelTransformationMode mode, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, boolean bl, CallbackInfo ci) {
        if (!ConfigManager.modEnabled) return;
        ShieldRenderManager.INSTANCE.render(itemStack, componentMap, mode, matrixStack, vertexConsumerProvider, this.model, i, j, bl, ci);
    }
}
