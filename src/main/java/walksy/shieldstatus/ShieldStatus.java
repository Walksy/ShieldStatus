package walksy.shieldstatus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;
import walksy.shieldstatus.manager.ShieldStateManager;
import walksy.shieldstatus.render.ShieldItemModelRenderer;

public class ShieldStatus implements ModInitializer {

    private static ShieldItemModelRenderer shieldModelRenderer;
    private static ShieldStateManager shieldCooldownManager;
    public static PlayerEntity focusedEntity = null;

    public static KeyBinding toggleSelfState = KeyBindingHelper.registerKeyBinding(
        new KeyBinding("Toggle SelfState", GLFW.GLFW_KEY_UNKNOWN, "Shield Status"));


    @Override
    public void onInitialize() {
        shieldModelRenderer = new ShieldItemModelRenderer();
        shieldCooldownManager = new ShieldStateManager();
    }

    public static ShieldStateManager getShieldStateManager() {
        return shieldCooldownManager;
    }

    public static ShieldItemModelRenderer getShieldModelRenderer() {
        return shieldModelRenderer;
    }


}
