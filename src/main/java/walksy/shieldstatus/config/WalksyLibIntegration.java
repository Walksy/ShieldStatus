package walksy.shieldstatus.config;

import main.walksy.lib.api.WalksyLibApi;
import main.walksy.lib.api.WalksyLibConfig;
import main.walksy.lib.core.config.impl.LocalConfig;
import main.walksy.lib.core.gui.impl.WalksyLibConfigScreen;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.UnaryOperator;

public class WalksyLibIntegration implements WalksyLibApi {

    @Override
    public LocalConfig getConfig() {
        WalksyLibConfig config = new Config();
        return config.getOrCreateConfig();
    }
}
