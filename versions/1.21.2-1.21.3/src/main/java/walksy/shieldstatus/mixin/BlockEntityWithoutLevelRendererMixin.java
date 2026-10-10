package walksy.shieldstatus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.ShieldStatusConfig;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BlockEntityWithoutLevelRendererMixin {
    @Shadow private ShieldModel shieldModel;

    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true)
    private void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int lightCoords, int overlayCoords, CallbackInfo ci) {
        if (!ShieldStatusConfig.modEnabled || !stack.is(Items.SHIELD)) return;
        ShieldStatus.getShieldSpecialSubmitter().render(this.shieldModel, context, stack.getComponents(), poseStack, bufferSource, lightCoords, overlayCoords, stack.hasFoil());
        ci.cancel();
    }
}
