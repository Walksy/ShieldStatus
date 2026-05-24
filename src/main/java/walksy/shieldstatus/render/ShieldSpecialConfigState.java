package walksy.shieldstatus.render;

import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import walksy.shieldstatus.GrayscaleTextureCache;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.config.Config;

public class ShieldSpecialConfigState {
    public WalksyLibColor tintedColor;
    public Identifier shieldTexture;
    public Identifier shieldSheet;

    public void extractConfigState() {
        final Player user = ShieldStatus.getFocusedPlayer();
        this.shieldTexture = Config.getTexture(user);
        this.shieldSheet = Config.grayscaleTexture ? GrayscaleTextureCache.get(this.shieldTexture) : this.shieldTexture;
        this.tintedColor = Config.getColor(user);
    }
}
