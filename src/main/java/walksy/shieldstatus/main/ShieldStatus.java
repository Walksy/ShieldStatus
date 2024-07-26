package walksy.shieldstatus.main;

import net.fabricmc.api.ModInitializer;
import walksy.shieldstatus.manager.CommandManager;
import walksy.shieldstatus.manager.ConfigManager;
import walksy.shieldstatus.manager.ShieldDataManager;


public class ShieldStatus implements ModInitializer {
    @Override
    public void onInitialize()
    {
        ConfigManager.INSTANCE.loadConfig();
        CommandManager.INSTANCE.initCommand();
    }
}
