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
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import walksy.shieldstatus.GrayscaleTextureCache;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.Config;


public class ShieldModelSubmitter {

    private final ShieldModel model;
    private final SpriteGetter sprites;

    public ShieldModelSubmitter(ShieldModel model, SpriteGetter sprites) {
        this.model = model;
        this.sprites = sprites;
    }

    public void submit(ItemDisplayContext context, DataComponentMap components, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (context.firstPerson() || context == ItemDisplayContext.GUI || ShieldStatus.focusedEntity == null) {
            ShieldStatus.focusedEntity = Minecraft.getInstance().player;
        }
        final Player user = ShieldStatus.focusedEntity;
        final Identifier shieldTexture = Config.getTexture(user);
        final Identifier shieldSheet = Config.grayscaleTexture ? GrayscaleTextureCache.get(shieldTexture) : shieldTexture;

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(shieldSheet), (pose, vertexConsumer) -> {
            this.submitShieldModel(pose, user, components, shieldSheet, lightCoords, overlayCoords, hasFoil);
        });
    }

    private void submitShieldModel(PoseStack.Pose pose, Player user, DataComponentMap components, Identifier shieldSheet, int lightCoords, int overlayCoords, boolean hasFoil) {
        PoseStack poseStack = new PoseStack();
        poseStack.last().set(pose);
        poseStack.pushPose();
        final WalksyLibColor tintedColor = Config.getColor(user);
        final BannerPatternLayers patterns = components != null ? (BannerPatternLayers) components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY) : BannerPatternLayers.EMPTY;
        final DyeColor baseColor = components != null ? (DyeColor) components.get(DataComponents.BASE_COLOR) : null;
        final boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;
        RenderType renderType = RenderTypes.entityTranslucent(shieldSheet);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer glintConsumer = ItemFeatureRenderer.getFoilBuffer(bufferSource, renderType, false, hasFoil);

        this.model.handle().render(poseStack, glintConsumer, lightCoords, overlayCoords, tintedColor.getRGB());
        this.model.plate().render(poseStack, glintConsumer, lightCoords, overlayCoords, tintedColor.getRGB());
        if (hasPatterns) {
            submitPatterns(poseStack, bufferSource, lightCoords, overlayCoords, baseColor, patterns);
        }
        poseStack.popPose();
        bufferSource.endBatch();
    }

    public void submitPatterns(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int lightCoords, int overlayCoords, DyeColor baseColor, BannerPatternLayers patterns) {
        this.submitPatternLayer(Sheets.SHIELD_PATTERN_BASE, bufferSource, poseStack, lightCoords, overlayCoords, baseColor);

        for (int maskIndex = 0; maskIndex < 16 && maskIndex < patterns.layers().size(); ++maskIndex) {
            BannerPatternLayers.Layer layer = patterns.layers().get(maskIndex);
            this.submitPatternLayer(Sheets.getShieldSprite(layer.pattern()), bufferSource, poseStack, lightCoords, overlayCoords, layer.color());
        }
    }

    private void submitPatternLayer(SpriteId sprite, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, int lightCoords, int overlayCoords, DyeColor color) {
        this.model.plate().render(poseStack, sprite.buffer(this.sprites, bufferSource, RenderTypes::entityTranslucent), lightCoords, overlayCoords, color.getTextureDiffuseColor());
    }

}