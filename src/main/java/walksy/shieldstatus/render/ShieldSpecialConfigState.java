package walksy.shieldstatus.render;

import main.walksy.lib.api.config.value.WLColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import walksy.shieldstatus.GrayscaleTextureCache;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.ShieldStatusConfig;

public class ShieldSpecialConfigState {
    public WLColor tintedColor;
    public Identifier shieldSheet;
    public Identifier banner;

    public void extractConfigState() {
        final Player user = ShieldStatus.getFocusedPlayer();
        this.shieldSheet = ShieldStatusConfig.grayscaleTexture ? GrayscaleTextureCache.get(ShieldStatusConfig.SHIELD_TEXTURE) : ShieldStatusConfig.SHIELD_TEXTURE;
        this.tintedColor = ShieldStatusConfig.getColor(user);
        BannerTextureCache.retain(ShieldStatusConfig.shownBanner(), ShieldStatusConfig.shownDisabledBanner());
        this.banner = BannerTextureCache.get(ShieldStatusConfig.bannerFor(user));
    }
}
