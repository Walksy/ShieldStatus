package walksy.shieldstatus.main;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import walksy.shieldstatus.manager.ConfigManager;


public class ShieldStatus implements ModInitializer {
    @Override
    public void onInitialize()
    {
        ConfigManager.load();
    }
}
