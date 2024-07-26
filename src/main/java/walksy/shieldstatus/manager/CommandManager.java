package walksy.shieldstatus.manager;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class CommandManager {
    public static CommandManager INSTANCE = new CommandManager();
    public void initCommand()
    {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("interpolateshields")
                .executes(context -> {
                    ConfigManager.INSTANCE.interpolateShields = !ConfigManager.INSTANCE.interpolateShields;
                    String message = ConfigManager.INSTANCE.interpolateShields ? "enabled" : "disabled";
                    context.getSource().sendFeedback(Text.of("Shield color interpolation is now " + message));
                    ConfigManager.INSTANCE.saveConfig();
                    return 1;
                })
            )
        );
    }

}
