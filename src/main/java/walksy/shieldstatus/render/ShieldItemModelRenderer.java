package walksy.shieldstatus.render;

import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import walksy.shieldstatus.GrayscaleTextureCache;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.Config;

import java.util.Objects;

public class ShieldItemModelRenderer {

    public void render(ItemStack stack, ShieldEntityModel model, ModelTransformationMode modelTransformationMode, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        if (modelTransformationMode.isFirstPerson() || modelTransformationMode == ModelTransformationMode.GUI || ShieldStatus.focusedEntity == null) {
            ShieldStatus.focusedEntity = MinecraftClient.getInstance().player;
        }

        BannerPatternsComponent bannerPatternsComponent = (BannerPatternsComponent)stack.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
        DyeColor dyeColor2 = (DyeColor)stack.get(DataComponentTypes.BASE_COLOR);
        boolean bl = stack.hasGlint();
        boolean bl2 = !bannerPatternsComponent.layers().isEmpty() || dyeColor2 != null;

        WalksyLibColor color = Config.getColor(ShieldStatus.focusedEntity);
        int colorWithAlpha = ColorHelper.Argb.withAlpha(color.getAlpha(), color.getRGB());

        matrixStack.push();
        matrixStack.scale(1.0F, -1.0F, -1.0F);

        Identifier texture = ShieldStatus.getShieldStateManager().isCoolingDown(ShieldStatus.focusedEntity) ? Config.disabledTexture.getIdentifier() : Config.enabledTexture.getIdentifier();
        if (Config.grayscaleTexture) {
            texture = GrayscaleTextureCache.get(texture);
        }
        RenderLayer layer = RenderLayer.getEntityTranslucent(texture);
        VertexConsumer vertexConsumer = ItemRenderer.getItemGlintConsumer(vertexConsumerProvider, layer, false, bl);

        matrixStack.push();
        matrixStack.translate(0.0F, 0.0F, 0.0001F); //stops the handle from phasing through a lower opacity shield base
        model.getHandle().render(matrixStack, vertexConsumer, i, j, colorWithAlpha);
        matrixStack.pop();

        if (bl2) {
            this.renderCanvas(
                matrixStack,
                vertexConsumer,
                i,
                j,
                model.getPlate(),
                false,
                (DyeColor) Objects.requireNonNullElse(dyeColor2, DyeColor.WHITE),
                bannerPatternsComponent,
                bl,
                false,
                colorWithAlpha
            );
        } else {
            model.getPlate().render(matrixStack, vertexConsumer, i, j, colorWithAlpha);
        }

        matrixStack.pop();
    }

    public void renderCanvas(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, ModelPart canvas, boolean isBanner, DyeColor color, BannerPatternsComponent patterns, boolean glint, boolean solid, int statusColor) {
        canvas.render(matrices, vertexConsumer, light, overlay, statusColor);
        VertexConsumerProvider vertexConsumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        this.renderLayer(matrices, vertexConsumers, light, overlay, canvas, isBanner ? TexturedRenderLayers.BANNER_BASE : TexturedRenderLayers.SHIELD_BASE, color, statusColor);

        for(int i = 0; i < 16 && i < patterns.layers().size(); ++i) {
            BannerPatternsComponent.Layer layer = patterns.layers().get(i);
            SpriteIdentifier spriteIdentifier = isBanner ? TexturedRenderLayers.getBannerPatternTextureId(layer.pattern()) : TexturedRenderLayers.getShieldPatternTextureId(layer.pattern());
            this.renderLayer(matrices, vertexConsumers, light, overlay, canvas, spriteIdentifier, layer.color(), statusColor);
        }

    }

    private void renderLayer(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier textureId, DyeColor color, int statusColor) {
        canvas.render(matrices, textureId.getVertexConsumer(vertexConsumers, RenderLayer::getEntityTranslucent), light, overlay, ColorHelper.Argb.withAlpha(ColorHelper.Argb.getAlpha(statusColor), color.getEntityColor()));
    }

}
