package walksy.shieldstatus;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import walksy.shieldstatus.manager.ShieldStateManager;
import walksy.shieldstatus.render.ShieldItemModelRenderer;

public class ShieldStatus implements ModInitializer {

    private static ShieldItemModelRenderer shieldModelRenderer;
    private static ShieldStateManager shieldCooldownManager;
    public static PlayerEntity focusedEntity = null;

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
