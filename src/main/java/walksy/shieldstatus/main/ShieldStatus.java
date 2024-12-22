package walksy.shieldstatus.main;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import walksy.shieldstatus.manager.ConfigManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;


public class ShieldStatus implements ModInitializer {
    @Override
    public void onInitialize()
    {
        ConfigManager.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("walksyshieldstatusopacity")
                .then(ClientCommandManager.argument("num", IntegerArgumentType.integer())
                    .executes(context -> {
                        ConfigManager.enabledOpacity = IntegerArgumentType.getInteger(context, "num");
                        ConfigManager.disabledOpacity = IntegerArgumentType.getInteger(context, "num");
                        context.getSource().sendFeedback(Text.of("Opacity set to: [Enabled] " + ConfigManager.enabledOpacity + " [Disabled] " + ConfigManager.disabledOpacity));
                        ConfigManager.save();
                        return 1;
                    })
                )
            )
        );
    }
}
