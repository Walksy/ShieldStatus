package walksy.shieldstatus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.config.ShieldStatusConfig;

import java.util.Map;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Shadow @Final private Map<Object, GuiRenderer.AtlasPosition> atlasPositions;

    @Inject(method = "method_71055", at = @At("HEAD"))
    private void prepareItemElements(MutableBoolean hasOversizedItems, int guiScale, int atlasSize, MutableBoolean atlasChanged, PoseStack poseStack, GuiItemRenderState itemState, CallbackInfo ci) {
        if (!ShieldStatusConfig.modEnabled) return;
        final TrackingItemStackRenderState itemStackRenderState = itemState.itemStackRenderState();
        final GuiRenderer.AtlasPosition atlasPosition = this.atlasPositions.get(itemStackRenderState.getModelIdentity());
        if (atlasPosition != null && itemStackRenderState.getModelIdentity().toString().contains("shield")) {
            itemStackRenderState.setAnimated();
            atlasPosition.lastAnimatedOnFrame = -1;
        }
    }
}
