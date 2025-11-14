package walksy.shieldstatus.render;

import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import walksy.shieldstatus.GrayscaleTextureCache;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.Config;

import java.awt.*;
import java.util.Objects;

public class ShieldItemModelRenderer {

    public void render(@Nullable ComponentMap componentMap, ShieldEntityModel model, SpriteHolder spriteHolder, ItemDisplayContext modelTransformationMode, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, int i, int j, int k, boolean bl) {
        if (modelTransformationMode.isFirstPerson() || modelTransformationMode == ItemDisplayContext.GUI || ShieldStatus.focusedEntity == null) {
            ShieldStatus.focusedEntity = MinecraftClient.getInstance().player;
        }
        WalksyLibColor tintColor = Config.getColor(ShieldStatus.focusedEntity);
        Identifier texture = ShieldStatus.getShieldStateManager().isCoolingDown(ShieldStatus.focusedEntity)
            ? Config.disabledTexture.getIdentifier()
            : Config.enabledTexture.getIdentifier();
        if (Config.grayscaleTexture) {
            texture = GrayscaleTextureCache.get(texture);
        }
        BannerPatternsComponent bannerPatternsComponent = componentMap != null ? (BannerPatternsComponent)componentMap.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT) : BannerPatternsComponent.DEFAULT;
        DyeColor dyeColor = componentMap != null ? (DyeColor)componentMap.get(DataComponentTypes.BASE_COLOR) : null;
        boolean bl2 = !bannerPatternsComponent.layers().isEmpty() || dyeColor != null;

        matrixStack.push();
        matrixStack.scale(1.0F, -1.0F, -1.0F);

        //OrderedRenderCommandQueue::submitModelPart doesn't support custom textures (sprites)
        //Bypassing the batching process leads to incorrect depth writing, so not recommended
        orderedRenderCommandQueue.submitCustom(matrixStack, RenderLayer.getEntityTranslucent(texture), (entry, vertexConsumer) -> {
            MatrixStack stack = new MatrixStack();
            stack.peek().copy(entry);
            stack.push();
            stack.translate(0, 0, 0.0001F);
            model.getHandle().render(stack, vertexConsumer, i, j, tintColor.getRGB());
            stack.pop();
        });

        if (bl2) {
            orderedRenderCommandQueue.submitCustom(matrixStack, RenderLayer.getEntityTranslucent(texture), (entry, vertexConsumer) -> {
                MatrixStack stack = new MatrixStack();
                stack.peek().copy(entry);
                this.renderCanvas(
                    stack,
                    spriteHolder,
                    vertexConsumer,
                    i,
                    j,
                    model.getPlate(),
                    false,
                    (DyeColor) Objects.requireNonNullElse(dyeColor, DyeColor.WHITE),
                    bannerPatternsComponent,
                    bl,
                    false,
                    tintColor.getRGB()
                );
            });
        } else {
            orderedRenderCommandQueue.submitCustom(matrixStack, RenderLayer.getEntityTranslucent(texture), (entry, vertexConsumer) -> {
                MatrixStack stack = new MatrixStack();
                stack.peek().copy(entry);
                model.getPlate().render(stack, vertexConsumer, i, j, tintColor.getRGB());
            });
        }

        matrixStack.pop();
    }

    public void renderCanvas(MatrixStack matrices, SpriteHolder spriteHolder, VertexConsumer vertexConsumer, int light, int overlay, ModelPart canvas, boolean isBanner, DyeColor color, BannerPatternsComponent patterns, boolean glint, boolean solid, int statusColor) {
        canvas.render(matrices, vertexConsumer, light, overlay, statusColor);
        VertexConsumerProvider vertexConsumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        this.renderLayer(matrices, spriteHolder, vertexConsumers, light, overlay, canvas, isBanner ? TexturedRenderLayers.BANNER_BASE : TexturedRenderLayers.SHIELD_BASE, color, statusColor);

        for(int i = 0; i < 16 && i < patterns.layers().size(); ++i) {
            BannerPatternsComponent.Layer layer = patterns.layers().get(i);
            SpriteIdentifier spriteIdentifier = isBanner ? TexturedRenderLayers.getBannerPatternTextureId(layer.pattern()) : TexturedRenderLayers.getShieldPatternTextureId(layer.pattern());
            this.renderLayer(matrices, spriteHolder, vertexConsumers, light, overlay, canvas, spriteIdentifier, layer.color(), statusColor);
        }

    }

    private void renderLayer(MatrixStack matrices, SpriteHolder spriteHolder, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier textureId, DyeColor color, int statusColor) {
        canvas.render(matrices, textureId.getVertexConsumer(spriteHolder, vertexConsumers, RenderLayer::getEntityTranslucent), light, overlay, ColorHelper.withAlpha(ColorHelper.getAlpha(statusColor), color.getEntityColor()));
    }
}
