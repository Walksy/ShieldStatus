package walksy.shieldstatus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import walksy.shieldstatus.manager.ShieldStateManager;
import walksy.shieldstatus.render.ShieldModelSubmitter;

public class ShieldStatus implements ModInitializer {

    private static ShieldStateManager shieldCooldownManager;
    private static ShieldModelSubmitter shieldModelSubmitter;
    public static Player focusedEntity = null;

    public static KeyMapping toggleSelfState = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("Toggle SelfState", GLFW.GLFW_KEY_UNKNOWN, new KeyMapping.Category(Identifier.fromNamespaceAndPath("walksy", "key"))));

    @Override
    public void onInitialize() {
        shieldCooldownManager = new ShieldStateManager();
    }

    public static void setupShieldModelSubmitter(ShieldModel model, SpriteGetter sprites) {
        shieldModelSubmitter = new ShieldModelSubmitter(model, sprites);
    }

    public static ShieldModelSubmitter getShieldModelSubmitter() {
        return shieldModelSubmitter;
    }

    public static ShieldStateManager getShieldStateManager() {
        return shieldCooldownManager;
    }

}
