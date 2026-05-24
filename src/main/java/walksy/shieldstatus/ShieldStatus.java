package walksy.shieldstatus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import org.lwjgl.glfw.GLFW;
import walksy.shieldstatus.render.ShieldSpecialSubmitter;

public class ShieldStatus implements ModInitializer {

    private static ShieldSpecialSubmitter shieldSpecialSubmitter;
    private static Player focusedPlayer;
    public static final KeyMapping toggleSelfState = KeyMappingHelper.registerKeyMapping(new KeyMapping("Toggle SelfState", GLFW.GLFW_KEY_UNKNOWN, new KeyMapping.Category(Identifier.fromNamespaceAndPath("walksy", "key"))));

    @Override
    public void onInitialize() {

    }

    public static void setCurrentFocusedPlayer(final Player player) {
        ShieldStatus.focusedPlayer = player;
    }

    public static void checkDisplayContext(final ItemDisplayContext context) {
        if (context.firstPerson() || context == ItemDisplayContext.GUI || ShieldStatus.focusedPlayer == null) {
            ShieldStatus.focusedPlayer = Minecraft.getInstance().player;
        }
    }

    public static void setupShieldSpecialSubmitter(final ShieldModel model, final SpriteGetter sprites) {
        ShieldStatus.shieldSpecialSubmitter = new ShieldSpecialSubmitter(model, sprites);
    }

    public static ShieldSpecialSubmitter getShieldSpecialSubmitter() {
        return ShieldStatus.shieldSpecialSubmitter;
    }

    public static Player getFocusedPlayer() {
        return ShieldStatus.focusedPlayer;
    }
}
