package walksy.shieldstatus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.Config;


@Mixin(targets = "net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState")
public abstract class LayerRenderStateMixin {
    @Shadow @Final ItemStackRenderState this$0;
    @Shadow private @Nullable SpecialModelRenderer<Object> specialRenderer;
    @Shadow private @Nullable Object argumentForSpecialRendering;
    @Shadow private ItemStackRenderState.FoilType foilType;

    /**
     * Taken out of ShieldSpecialRenderer to acquire the displayContext
     */

    @Inject(method = "submit",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/special/SpecialModelRenderer;submit(Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V"),
            cancellable = true)
    private void redirectShieldSubmit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
        if (!Config.modEnabled) return;

        if ((Object)this.specialRenderer instanceof ShieldSpecialRenderer) {
            final ItemDisplayContext context = this$0.displayContext;
            final DataComponentMap components = (this.argumentForSpecialRendering instanceof DataComponentMap) ? (DataComponentMap) this.argumentForSpecialRendering : null;
            final boolean hasFoil = this.foilType != ItemStackRenderState.FoilType.NONE;
            ShieldStatus.getShieldSpecialSubmitter().submit(context, components, poseStack, submitNodeCollector, lightCoords, overlayCoords, hasFoil);
            poseStack.popPose();
            ci.cancel();
        }
    }
}