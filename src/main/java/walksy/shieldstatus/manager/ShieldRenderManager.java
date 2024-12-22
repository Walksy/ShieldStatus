package walksy.shieldstatus.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Objects;

public class ShieldRenderManager {

    public static ShieldRenderManager INSTANCE = new ShieldRenderManager();

    public void render(ItemStack itemStack, ComponentMap componentMap, ModelTransformationMode mode, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, ShieldEntityModel shieldModel, int i, int j, boolean bl, CallbackInfo ci) {
        LivingEntity targetLivingEntity = this.findLivingEntityWithShield(itemStack);
        if (targetLivingEntity == null) {
            return;
        }

        ci.cancel();

        boolean isShieldDisabled = ShieldDataManager.INSTANCE.disabledShields.stream()
            .anyMatch(playerStats -> playerStats.player.getUuid().equals(targetLivingEntity.getUuid()));
        final Color shieldColor = this.getColor(targetLivingEntity, isShieldDisabled);
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("" + shieldColor));
        BannerPatternsComponent bannerPatternsComponent = componentMap != null
            ? componentMap.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT)
            : BannerPatternsComponent.DEFAULT;

        DyeColor dyeColor = componentMap != null ? componentMap.get(DataComponentTypes.BASE_COLOR) : null;

        boolean bl2 = !bannerPatternsComponent.layers().isEmpty() || dyeColor != null;

        matrixStack.push();
        matrixStack.scale(1.0F, -1.0F, -1.0F);

        SpriteIdentifier spriteIdentifier = bl2 ? ModelBaker.SHIELD_BASE : ModelBaker.SHIELD_BASE_NO_PATTERN;
        VertexConsumer vertexConsumer = spriteIdentifier.getSprite()
            .getTextureSpecificVertexConsumer(
                ItemRenderer.getItemGlintConsumer(
                    vertexConsumerProvider, shieldModel.getLayer(spriteIdentifier.getAtlasId()), mode == ModelTransformationMode.GUI, bl
                )
            );

        shieldModel.getHandle().render(matrixStack, spriteIdentifier.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityTranslucent, false, itemStack.hasGlint()), i, j, ColorHelper.withAlpha(ColorHelper.getAlpha(shieldColor.getRGB()), shieldColor.getRGB()));
        if (bl2) {
            /**
             * Custom override of the banner being rendered
             */
            this.renderBannerBlockEntityCanvas(matrixStack, vertexConsumerProvider, i, j, shieldModel.getPlate(), spriteIdentifier, false, (DyeColor)Objects.requireNonNullElse(dyeColor, DyeColor.WHITE), bannerPatternsComponent, bl, false, ColorHelper.withAlpha(ColorHelper.getAlpha(shieldColor.getRGB()), shieldColor.getRGB()));
        } else {
            shieldModel.getPlate().render(matrixStack, spriteIdentifier.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityTranslucent, false, itemStack.hasGlint()), i, j, ColorHelper.withAlpha(ColorHelper.getAlpha(shieldColor.getRGB()), shieldColor.getRGB()));
        }

        matrixStack.pop();
    }


    private void renderBannerBlockEntityCanvas(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner, DyeColor color, BannerPatternsComponent patterns, boolean glint, boolean solid, int colorOverlay) {
        canvas.render(matrices, baseSprite.getVertexConsumer(vertexConsumers, RenderLayer::getEntityTranslucent, solid, glint), light, overlay, colorOverlay);
        this.renderLayer(matrices, vertexConsumers, light, overlay, canvas, isBanner ? TexturedRenderLayers.BANNER_BASE : TexturedRenderLayers.SHIELD_BASE, color, colorOverlay);
        for (int i = 0; i < 16 && i < patterns.layers().size(); ++i) {
            BannerPatternsComponent.Layer layer = patterns.layers().get(i);
            SpriteIdentifier spriteIdentifier = isBanner ? TexturedRenderLayers.getBannerPatternTextureId(layer.pattern()) : TexturedRenderLayers.getShieldPatternTextureId(layer.pattern());
            this.renderLayer(matrices, vertexConsumers, light, overlay, canvas, spriteIdentifier, layer.color(), colorOverlay);
        }
    }

    private void renderLayer(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier textureId, DyeColor color, int colorOverlay) {
        canvas.render(matrices, textureId.getVertexConsumer(vertexConsumers, RenderLayer::getEntityTranslucent), light, overlay, ColorHelper.withAlpha(ColorHelper.getAlpha(colorOverlay), color.getEntityColor()));
    }

    private LivingEntity findLivingEntityWithShield(ItemStack stack) {
        for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
            if (entity instanceof LivingEntity livingEntity) {
                if (hasShieldInHand(livingEntity, stack)) {
                    return livingEntity;
                }
            }
        }
        return null;
    }

    private boolean hasShieldInHand(LivingEntity livingEntity, ItemStack stack) {
        return livingEntity.getOffHandStack().equals(stack) || livingEntity.getMainHandStack().equals(stack);
    }

    private Color getColor(LivingEntity livingEntity, boolean isShieldDisabled) {
        Color disabledColor = ConfigManager.getDisabledShieldColor();
        Color enabledColor = ConfigManager.getEnabledShieldColor();
        if (!ConfigManager.interpolateShields) {
            return isShieldDisabled ? disabledColor : enabledColor;
        }

        for (ShieldDataManager.PlayerStats playerStats : ShieldDataManager.INSTANCE.disabledShields) {
            if (playerStats.player.equals(livingEntity)) {
                float progress = Math.min(1.0f, (float) playerStats.ticks / 150f);
                int red = (int) (disabledColor.getRed() + (enabledColor.getRed() - disabledColor.getRed()) * progress);
                int green = (int) (disabledColor.getGreen() + (enabledColor.getGreen() - disabledColor.getGreen()) * progress);
                int blue = (int) (disabledColor.getBlue() + (enabledColor.getBlue() - disabledColor.getBlue()) * progress);
                int alpha = (int) (disabledColor.getAlpha() + (enabledColor.getAlpha() - disabledColor.getAlpha()) * progress);
                return new Color(red, green, blue, alpha);
            }
        }

        return enabledColor;
    }
}
