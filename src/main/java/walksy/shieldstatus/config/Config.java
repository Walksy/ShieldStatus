package walksy.shieldstatus.config;

import main.walksy.lib.api.WalksyLibConfig;
import main.walksy.lib.core.config.impl.LocalConfig;
import main.walksy.lib.core.config.local.Category;
import main.walksy.lib.core.config.local.Option;
import main.walksy.lib.core.config.local.OptionDescription;
import main.walksy.lib.core.config.local.options.BooleanOption;
import main.walksy.lib.core.config.local.options.ColorOption;
import main.walksy.lib.core.config.local.options.SpriteOption;
import main.walksy.lib.core.config.local.options.groups.OptionGroup;
import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import main.walksy.lib.core.utils.IdentifierWrapper;
import main.walksy.lib.core.utils.PathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import walksy.shieldstatus.ShieldStatus;
import walksy.shieldstatus.manager.ShieldStateManager;

public class Config implements WalksyLibConfig {

    public static boolean modEnabled = true;
    public static boolean colorInterpolation = false;
    public static boolean grayscaleTexture = false;
    public static boolean selfStateOnly = false;
    public static boolean customEnabledShieldColor = true;
    public static boolean customUsingShieldColor = false;
    public static boolean customDisabledShieldColor = true;

    private static WalksyLibColor enabledColor = new WalksyLibColor(0, 255, 0, 255);
    private static WalksyLibColor usingColor = new WalksyLibColor(0, 255, 0, 255);
    private static WalksyLibColor disabledColor = new WalksyLibColor(255, 0, 0, 255);

    public static IdentifierWrapper enabledTexture = new IdentifierWrapper(Identifier.withDefaultNamespace("textures/entity/shield/shield_base_nopattern.png"));
    public static IdentifierWrapper disabledTexture = new IdentifierWrapper(Identifier.withDefaultNamespace("textures/entity/shield/shield_base_nopattern.png"));

    public static WalksyLibColor getColor(@Nullable Player player) {
        WalksyLibColor DEFAULT = new WalksyLibColor(255, 255, 255, 255);
        if (player == null) return DEFAULT;
        if (player != Minecraft.getInstance().player && selfStateOnly) {
            return DEFAULT;
        }

        ShieldStateManager ssm = ShieldStatus.getShieldStateManager();
        boolean cd = ssm.isCoolingDown(player);
        boolean active = ssm.isUsingShield(player);

        WalksyLibColor currentEnabledColor = customEnabledShieldColor ? enabledColor : DEFAULT;
        WalksyLibColor currentDisabledColor = customDisabledShieldColor ? disabledColor : DEFAULT;
        WalksyLibColor currentUseColor = customUsingShieldColor ? usingColor : DEFAULT;

        if (active && customUsingShieldColor) {
            return currentUseColor;
        }
        if (!colorInterpolation) {
            return cd ? currentDisabledColor : currentEnabledColor;
        }

        float progress = cd ? ssm.getCooldownProgress(player) : 0.0f;

        int red = (int) (currentEnabledColor.getRed()   + (currentDisabledColor.getRed()   - currentEnabledColor.getRed())   * progress);
        int green = (int) (currentEnabledColor.getGreen() + (currentDisabledColor.getGreen() - currentEnabledColor.getGreen()) * progress);
        int blue = (int) (currentEnabledColor.getBlue()  + (currentDisabledColor.getBlue()  - currentEnabledColor.getBlue())  * progress);
        int alpha = (int) (currentEnabledColor.getAlpha() + (currentDisabledColor.getAlpha() - currentEnabledColor.getAlpha()) * progress);

        return new WalksyLibColor(red, green, blue, alpha);
    }


    public static Identifier getTexture(Player player) {
        if (player != Minecraft.getInstance().player && selfStateOnly) {
            return Config.enabledTexture.getIdentifier();
        }

        return ShieldStatus.getShieldStateManager().isCoolingDown(player)
            ? Config.disabledTexture.getIdentifier()
            : Config.enabledTexture.getIdentifier();
    }

    public static void tick() {
        enabledColor.tick();
        disabledColor.tick();
        usingColor.tick();

        if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null && Minecraft.getInstance().screen == null) {
            if (ShieldStatus.toggleSelfState.consumeClick()) {
                selfStateOnly = !selfStateOnly;
            }
        }
    }

    //General Category
    private final Option<Boolean> modEnabledOption = BooleanOption.createBuilder("Mod Enabled", () -> modEnabled, modEnabled, newValue -> modEnabled = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Toggles ShieldStatuses on or off"))
        .build();

    //Color Category
    private final Option<Boolean> selfStateOnlyOption = BooleanOption.createBuilder("Self State Only", () -> selfStateOnly, selfStateOnly, newValue -> selfStateOnly = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Shows shield states for the client player only, ignores other players"))
        .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
        .build();
    private final Option<Boolean> interpolateShieldColorOption = BooleanOption.createBuilder("Interpolate Shield Color", () -> colorInterpolation, colorInterpolation, newValue -> colorInterpolation = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Transitions the shield color based on the player's disabled and enabled state. Ignores 'use' state"))
        .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
        .build();
    private final Option<Boolean> grayscaleShieldTextureOption = BooleanOption.createBuilder("Grayscale Shield Texture", () -> grayscaleTexture, grayscaleTexture, newValue -> grayscaleTexture = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Grayscales the texture of the shield before applying the color overlay. This can lead to more vibrant colors"))
        .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
        .build();

    private final Option<Boolean> customEnabledColorOption = BooleanOption.createBuilder("Custom Enabled Shield Color", () -> customEnabledShieldColor, customEnabledShieldColor, newValue -> customEnabledShieldColor = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Allows customization of the shield color when enabled"))
        .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
        .build();
    private final Option<WalksyLibColor> enabledColorOption = ColorOption.createBuilder("Enabled Color", () -> enabledColor, enabledColor, newValue -> enabledColor = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Color of the shield when enabled"))
        .availability(() -> customEnabledShieldColor && modEnabled, "Requires 'Custom Enabled Shield Color & Mod Enabled' to be enabled")
        .build();
    private final Option<Boolean> customUsingColorOption = BooleanOption.createBuilder("Custom Using Shield Color", () -> customUsingShieldColor, customUsingShieldColor, newValue -> customUsingShieldColor = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Allows customization of the shield color while in use"))
        .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
        .build();
    private final Option<WalksyLibColor> usingColorOption = ColorOption.createBuilder("Using Color", () -> usingColor, usingColor, newValue -> usingColor = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Color of the shield when in use"))
        .availability(() -> customUsingShieldColor && modEnabled, "Requires 'Custom Using Shield Color & Mod Enabled' to be enabled")
        .build();
    private final Option<Boolean> customDisabledColorOption = BooleanOption.createBuilder("Custom Disabled Shield Color", () -> customDisabledShieldColor, customDisabledShieldColor, newValue -> customDisabledShieldColor = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Allows customization of the shield color when disabled"))
        .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
        .build();
    private final Option<WalksyLibColor> disabledColorOption = ColorOption.createBuilder("Disabled Color", () -> disabledColor, disabledColor, newValue -> disabledColor = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Color of the shield when disabled"))
        .availability(() -> customDisabledShieldColor && modEnabled, "Requires 'Custom Disabled Shield Color & Mod Enabled' to be enabled")
        .build();

    private final Category generalCategory = Category.createBuilder("General")
        .group(OptionGroup.createBuilder("Global Options")
            .addOption(modEnabledOption)
            .build())
        .build();

    private final Category colorCategory = Category.createBuilder("Color")
        .group(OptionGroup.createBuilder("General Options")
            .addOption(selfStateOnlyOption)
            .addOption(interpolateShieldColorOption)
            .addOption(grayscaleShieldTextureOption)
            .build())
        .group(OptionGroup.createBuilder("Enabled Shield Options")
            .addOption(customEnabledColorOption)
            .addOption(enabledColorOption)
            .build())
        .group(OptionGroup.createBuilder("Using Shield Options")
            .addOption(customUsingColorOption)
            .addOption(usingColorOption)
            .build())
        .group(OptionGroup.createBuilder("Disabled Shield Options")
            .addOption(customDisabledColorOption)
            .addOption(disabledColorOption)
            .build())
        .build();

    //Texture category
    private final Option<IdentifierWrapper> enabledShieldTextureOption = SpriteOption.createBuilder("Enabled Shield Texture", () -> enabledTexture, enabledTexture, newValue -> enabledTexture = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Sets the texture used for the shield when enabled"))
        .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
        .build();
    private final Option<IdentifierWrapper> disabledShieldTextureOption = SpriteOption.createBuilder("Disabled Shield Texture", () -> disabledTexture, disabledTexture, newValue -> disabledTexture = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Sets the texture used for the shield when disabled"))
        .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
        .build();

    private final Category textureCategory = Category.createBuilder("Texture")
        .group(OptionGroup.createBuilder("Enabled Shield Options")
            .addOption(enabledShieldTextureOption)
            .build())
        .group(OptionGroup.createBuilder("Disabled Shield Options")
            .addOption(disabledShieldTextureOption)
            .build())
        .build();

    @Override
    public LocalConfig define() {
        return LocalConfig.createBuilder("Shield Status")
            .path(PathUtils.ofConfigDir("shieldstatus"))
            .category(generalCategory)
            .category(colorCategory)
            .category(textureCategory)
            .build();
    }
}
