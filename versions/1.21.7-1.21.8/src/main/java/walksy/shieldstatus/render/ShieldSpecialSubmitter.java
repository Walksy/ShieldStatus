package walksy.shieldstatus.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import walksy.shieldstatus.ShieldStatus;

import java.util.Objects;


public class ShieldSpecialSubmitter {

    private final ShieldSpecialConfigState state;

    public ShieldSpecialSubmitter() {
        this.state = new ShieldSpecialConfigState();
    }

    public void render(final ShieldModel model, final ItemDisplayContext context, final DataComponentMap components, final PoseStack poseStack, final MultiBufferSource bufferSource, final int lightCoords, final int overlayCoords, final boolean hasFoil) {
        ShieldStatus.checkDisplayContext(context);
        this.state.extractConfigState();
        final int color = this.state.tintedColor.getRGB();
        final BannerPatternLayers patterns = components != null ? components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY) : BannerPatternLayers.EMPTY;
        final DyeColor baseColor = components != null ? components.get(DataComponents.BASE_COLOR) : null;
        final boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;
        final VertexConsumer consumer = ItemRenderer.getFoilBuffer(bufferSource, RenderType.entityTranslucent(this.state.shieldSheet), false, hasFoil);
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.0001F);
        model.handle().render(poseStack, consumer, lightCoords, overlayCoords, color);
        poseStack.popPose();
        model.plate().render(poseStack, consumer, lightCoords, overlayCoords, color);
        if (hasPatterns) {
            this.renderPatterns(model, poseStack, bufferSource, lightCoords, overlayCoords, Objects.requireNonNullElse(baseColor, DyeColor.WHITE), patterns, color);
        }
        if (this.state.banner != null) {
            BannerQuad.emit(poseStack.last(), bufferSource.getBuffer(RenderType.entityTranslucent(this.state.banner)), lightCoords, overlayCoords, color);
        }
        poseStack.popPose();
    }

    private void renderPatterns(final ShieldModel model, final PoseStack poseStack, final MultiBufferSource bufferSource, final int lightCoords, final int overlayCoords, final DyeColor baseColor, final BannerPatternLayers patterns, final int color) {
        this.renderPatternLayer(model, Sheets.SHIELD_BASE, bufferSource, poseStack, lightCoords, overlayCoords, baseColor, color);
        for (int maskIndex = 0; maskIndex < 16 && maskIndex < patterns.layers().size(); ++maskIndex) {
            final BannerPatternLayers.Layer layer = patterns.layers().get(maskIndex);
            this.renderPatternLayer(model, Sheets.getShieldMaterial(layer.pattern()), bufferSource, poseStack, lightCoords, overlayCoords, layer.color(), color);
        }
    }

    private void renderPatternLayer(final ShieldModel model, final Material material, final MultiBufferSource bufferSource, final PoseStack poseStack, final int lightCoords, final int overlayCoords, final DyeColor dyeColor, final int color) {
        model.plate().render(poseStack, material.buffer(bufferSource, RenderType::entityTranslucent), lightCoords, overlayCoords, ARGB.color(ARGB.alpha(color), dyeColor.getTextureDiffuseColor()));
    }
}
