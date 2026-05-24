package walksy.shieldstatus.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import walksy.shieldstatus.ShieldStatus;

import java.util.Objects;


public class ShieldSpecialSubmitter {

    private final ShieldModel model;
    private final SpriteGetter sprites;
    private final ShieldSpecialConfigState state;

    public ShieldSpecialSubmitter(final ShieldModel model, final SpriteGetter sprites) {
        this.model = model;
        this.sprites = sprites;
        this.state = new ShieldSpecialConfigState();
    }

    public void submit(final ItemDisplayContext context, final DataComponentMap components, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final int overlayCoords, final boolean hasFoil) {
        ShieldStatus.checkDisplayContext(context);
        this.state.extractConfigState();
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(this.state.shieldSheet), (pose, vertexConsumer) -> {
            this.submitShieldModel(pose, components, lightCoords, overlayCoords, hasFoil);
        });
    }

    private void submitShieldModel(final PoseStack.Pose pose, final DataComponentMap components, final int lightCoords, final int overlayCoords, final boolean hasFoil) {
        final PoseStack poseStack = new PoseStack();
        poseStack.last().set(pose);
        poseStack.pushPose();
        final BannerPatternLayers patterns = components != null ? (BannerPatternLayers) components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY) : BannerPatternLayers.EMPTY;
        final DyeColor baseColor = components != null ? (DyeColor) components.get(DataComponents.BASE_COLOR) : null;
        final boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;
        final RenderType renderType = RenderTypes.entityTranslucent(this.state.shieldSheet);
        final MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        final VertexConsumer glintConsumer = ItemFeatureRenderer.getFoilBuffer(bufferSource, renderType, false, hasFoil);
        this.model.handle().render(poseStack, glintConsumer, lightCoords, overlayCoords, this.state.tintedColor.getRGB());
        this.model.plate().render(poseStack, glintConsumer, lightCoords, overlayCoords, this.state.tintedColor.getRGB());
        if (hasPatterns) {
            this.submitPatterns(poseStack, bufferSource, lightCoords, overlayCoords, Objects.requireNonNullElse(baseColor, DyeColor.WHITE), patterns);
        }
        poseStack.popPose();
        bufferSource.endBatch();
    }

    private void submitPatterns(final PoseStack poseStack, final MultiBufferSource.BufferSource bufferSource, final int lightCoords, final int overlayCoords, final DyeColor baseColor, final BannerPatternLayers patterns) {
        this.submitPatternLayer(Sheets.SHIELD_PATTERN_BASE, bufferSource, poseStack, lightCoords, overlayCoords, baseColor);
        for (int maskIndex = 0; maskIndex < 16 && maskIndex < patterns.layers().size(); ++maskIndex) {
            final BannerPatternLayers.Layer layer = patterns.layers().get(maskIndex);
            this.submitPatternLayer(Sheets.getShieldSprite(layer.pattern()), bufferSource, poseStack, lightCoords, overlayCoords, layer.color());
        }
    }

    private void submitPatternLayer(final SpriteId sprite, final MultiBufferSource.BufferSource bufferSource, final PoseStack poseStack, final int lightCoords, final int overlayCoords, final DyeColor color) {
        this.model.plate().render(poseStack, sprite.buffer(this.sprites, bufferSource, RenderTypes::entityTranslucent), lightCoords, overlayCoords, color.getTextureDiffuseColor());
    }

}