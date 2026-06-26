package walksy.shieldstatus.render;

import com.mojang.blaze3d.vertex.PoseStack;
import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
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
        final WalksyLibColor color = this.state.tintedColor;
        final Identifier sheet = this.state.shieldSheet;
        final BannerPatternLayers patterns = components != null ? components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY) : BannerPatternLayers.EMPTY;
        final DyeColor baseColor = components != null ? components.get(DataComponents.BASE_COLOR) : null;
        final boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;
        submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.entityTranslucent(sheet), lightCoords, overlayCoords, color.getRGB(), null, 0, null);
        if (hasPatterns) {
            BannerRenderer.submitPatterns(this.sprites, poseStack, submitNodeCollector, lightCoords, overlayCoords, this.model, Unit.INSTANCE, false, Objects.requireNonNullElse(baseColor, DyeColor.WHITE), patterns, null);
        }
        if (hasFoil) {
            submitNodeCollector.order(patterns.layers().size() + 1).submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.entityGlint(), lightCoords, overlayCoords, -1, null, 0, null);
        }
    }
}
