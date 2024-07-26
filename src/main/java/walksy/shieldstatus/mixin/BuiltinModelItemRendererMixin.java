package walksy.shieldstatus.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.manager.ConfigManager;
import walksy.shieldstatus.manager.ShieldDataManager;

import java.awt.*;
import java.util.Objects;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {

    @Shadow
    private ShieldEntityModel modelShield;

    @Inject(at = {@At("HEAD")}, method = {"render"}, cancellable = true)
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        if (stack.isOf(Items.SHIELD)) {
            if (this.isModeNull(mode)) return;

            LivingEntity targetLivingEntity = this.findLivingEntityWithShield(stack);
            if (targetLivingEntity == null) {
                return;
            }

            ci.cancel();

            boolean isShieldDisabled = ShieldDataManager.INSTANCE.disabledShields.stream()
                .anyMatch(playerStats -> playerStats.player.getUuid().equals(targetLivingEntity.getUuid()));
            final Color shieldColor = this.getColor(targetLivingEntity, isShieldDisabled);

            BannerPatternsComponent bannerPatternsComponent = stack.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
            DyeColor dyeColor2 = stack.get(DataComponentTypes.BASE_COLOR);
            boolean bl = !bannerPatternsComponent.layers().isEmpty() || dyeColor2 != null;
            matrices.push();
            matrices.scale(1.0F, -1.0F, -1.0F);
            SpriteIdentifier spriteIdentifier = bl ? ModelLoader.SHIELD_BASE : ModelLoader.SHIELD_BASE_NO_PATTERN;
            VertexConsumer vertexConsumer = spriteIdentifier.getSprite().getTextureSpecificVertexConsumer(ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, this.modelShield.getLayer(spriteIdentifier.getAtlasId()), true, stack.hasGlint()));
            this.modelShield.getHandle().render(matrices, vertexConsumer, light, overlay, new Color(shieldColor.getRed() / 255.0F, shieldColor.getGreen() / 255.0F, shieldColor.getBlue() / 255.0F, 1.0F).getRGB());
            if (bl) {
                BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, this.modelShield.getPlate(), spriteIdentifier, false, (DyeColor) Objects.requireNonNullElse(dyeColor2, DyeColor.WHITE), bannerPatternsComponent, stack.hasGlint());
            } else {
                this.modelShield.getPlate().render(matrices, vertexConsumer, light, overlay, new Color(shieldColor.getRed() / 255.0F, shieldColor.getGreen() / 255.0F, shieldColor.getBlue() / 255.0F, 1.0F).getRGB());
            }
            matrices.pop();
        }
    }

    private Color getColor(LivingEntity livingEntity, boolean isShieldDisabled) {
        Color disabledColor = Color.RED;
        Color enabledColor = Color.GREEN;
        if (!ConfigManager.INSTANCE.interpolateShields) {
            return isShieldDisabled ? disabledColor : enabledColor;
        }

        for (ShieldDataManager.PlayerStats playerStats : ShieldDataManager.INSTANCE.disabledShields) {
            if (playerStats.player.equals(livingEntity)) {
                float progress = Math.min(1.0f, (float) playerStats.ticks / 150f);
                int red = (int) (disabledColor.getRed() + (enabledColor.getRed() - disabledColor.getRed()) * progress);
                int green = (int) (disabledColor.getGreen() + (enabledColor.getGreen() - disabledColor.getGreen()) * progress);
                int blue = (int) (disabledColor.getBlue() + (enabledColor.getBlue() - disabledColor.getBlue()) * progress);
                return new Color(red, green, blue);
            }
        }

        return enabledColor;
    }


    private boolean isModeNull(ModelTransformationMode mode)
    {
        return (mode != ModelTransformationMode.GUI
            && mode != ModelTransformationMode.FIRST_PERSON_LEFT_HAND
            && mode != ModelTransformationMode.FIRST_PERSON_RIGHT_HAND
            && mode != ModelTransformationMode.THIRD_PERSON_LEFT_HAND
            && mode != ModelTransformationMode.THIRD_PERSON_RIGHT_HAND);
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
}
