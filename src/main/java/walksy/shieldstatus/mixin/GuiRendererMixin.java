package walksy.shieldstatus.mixin;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;


@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {

    @Unique
    private final String forEachItemElementConsumer = "method_71055";

    @Shadow
    @Final
    private Map<Object, GuiRenderer.RenderedItem> renderedItems;

    /**
     * Ensures shields are updated every frame in the GUI
     *
     * Injects into the lambda method produced by the consumer of the GuiRenderState (found using byte code)
     * iterating through each ItemGuiElementRenderState
     *
     * By declaring the shield's item state to be animated and unlinking the frame from GuiRenderer::frame
     * it's forced to render via GuiRenderer::prepareItemInitially instead of GuiRenderer::prepareItem
     * where it actually renders the shield model with the vertex consumers instead of adding it to the current state layer
     * as a TexturedQuadGuiElementRenderState where the current color and disabled status of the shield aren't properly recorded
     */
    @Inject(method = "method_71055", at = @At(value = "HEAD"))
    private void prepareItemElements(MutableBoolean mutableBoolean, int i, int j, MutableBoolean mutableBoolean2, MatrixStack matrixStack, ItemGuiElementRenderState itemGuiElementRenderState, CallbackInfo ci) {
        ItemRenderState itemRenderState = itemGuiElementRenderState.state();
        GuiRenderer.RenderedItem renderedItem = this.renderedItems.get(itemRenderState.getModelKey());
        if (itemRenderState.getModelKey().toString().contains("shield") && renderedItem != null) {
            itemRenderState.markAnimated();
            renderedItem.frame = -1;
        }
    }
}
