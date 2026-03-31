package walksy.shieldstatus.mixin;

import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.config.Config;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {

    @Inject(method = "lambda$prepareItemElements$0",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/render/GuiItemAtlas;getOrUpdate(Lnet/minecraft/client/renderer/item/TrackingItemStackRenderState;)Lnet/minecraft/client/gui/render/GuiItemAtlas$SlotView;"))
    private void prepareItemElements(MutableBoolean hasOversizedItems, GuiItemAtlas itemAtlas, GuiItemRenderState itemState, CallbackInfo ci) {
        if (!Config.modEnabled) return;
        if (itemState.itemStackRenderState().getModelIdentity().toString().contains("shield")) {
            itemState.itemStackRenderState().setAnimated();
        }
    }
}
