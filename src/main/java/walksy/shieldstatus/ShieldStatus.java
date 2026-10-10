package walksy.shieldstatus;

import com.mojang.blaze3d.platform.InputConstants;
import main.walksy.lib.api.event.WalksyLibEvents;
import main.walksy.lib.core.platform.WLClient;
import net.fabricmc.api.ModInitializer;
import main.walksy.lib.api.config.value.WLKeyMapping;
import main.walksy.lib.core.WalksyLib;
import main.walksy.lib.core.toast.WLToast;
import main.walksy.lib.core.toast.WLToastManager;
import main.walksy.lib.core.utils.EnumNames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import walksy.shieldstatus.config.ShieldStatusConfig;
import walksy.shieldstatus.render.ShieldSpecialSubmitter;

public class ShieldStatus implements ModInitializer {

    private static ShieldSpecialSubmitter shieldSpecialSubmitter;
    private static Player focusedPlayer;
    private static final Object SHOWN_PLAYERS_TOAST = new Object();
    public static final WLKeyMapping TOGGLE_SELF_STATE = WLKeyMapping.create("Toggle SelfState", InputConstants.UNKNOWN.getValue(), "Shield Status");

    @Override
    public void onInitialize() {
        WalksyLibEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null && client.player != null && WLClient.screen() == null) {
                if (TOGGLE_SELF_STATE.consumeClick()) {
                    ShieldStatusConfig.shownPlayers = ShieldStatusConfig.shownPlayers.next();
                    final WLToastManager toasts = WalksyLib.getInstance().getToastManager();
                    toasts.dismiss(SHOWN_PLAYERS_TOAST);
                    toasts.submit(WLToast.of("Shield Status", "Shown Players: " + EnumNames.pretty(ShieldStatusConfig.shownPlayers)).info().id(SHOWN_PLAYERS_TOAST));
                }
            }
        });
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
