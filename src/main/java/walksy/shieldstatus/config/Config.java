package walksy.shieldstatus.config;

import main.walksy.lib.api.WalksyLibConfig;
import main.walksy.lib.core.WalksyLib;
import main.walksy.lib.core.config.impl.ModConfig;
import main.walksy.lib.core.config.local.Category;
import main.walksy.lib.core.config.local.Option;
import main.walksy.lib.core.config.local.OptionDescription;
import main.walksy.lib.core.config.local.options.BooleanOption;
import main.walksy.lib.core.config.local.options.ColorOption;
import main.walksy.lib.core.config.local.options.SpriteOption;
import main.walksy.lib.core.config.local.options.groups.OptionGroup;
import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import main.walksy.lib.core.manager.WalksyLibShieldStateManager;
import main.walksy.lib.core.utils.IdentifierWrapper;
import main.walksy.lib.core.utils.PathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import walksy.shieldstatus.ShieldStatus;

public class Config implements WalksyLibConfig {
    private static final Identifier SHIELD_TEXTURE = Identifier.withDefaultNamespace("textures/entity/shield/shield_base_nopattern.png");
    private static final String SHIELD_TEXTURE_PRE_26_1_PATH = "textures/entity/shield_base_nopattern.png";
    private static final WalksyLibColor COLOR_WHITE = new WalksyLibColor(255, 255, 255, 255);
    public static boolean modEnabled = true;
    public static boolean colorInterpolation = false;
    public static boolean grayscaleTexture = false;
    public static boolean selfStateOnly = false;
    public static boolean customEnabledShieldColor = true;
    public static boolean customUsingShieldColor = false;
    public static boolean customRisingShieldColor = false;
    public static boolean customDisabledShieldColor = true;
    private static WalksyLibColor enabledColor = new WalksyLibColor(0, 255, 0, 255);
    private static WalksyLibColor usingColor = new WalksyLibColor(0, 255, 0, 255);
    private static WalksyLibColor risingColor = new WalksyLibColor(255, 255, 0, 255);
    private static WalksyLibColor disabledColor = new WalksyLibColor(255, 0, 0, 255);
    public static IdentifierWrapper enabledTexture = new IdentifierWrapper(SHIELD_TEXTURE);
    public static IdentifierWrapper disabledTexture = new IdentifierWrapper(SHIELD_TEXTURE);

    public static WalksyLibColor getColor(final @Nullable Player player) {
        if (player == null) {
            return COLOR_WHITE;
        }
        if (player != Minecraft.getInstance().player && selfStateOnly) {
            return COLOR_WHITE;
        }
        final WalksyLibShieldStateManager stateManager = WalksyLib.getInstance().getShieldStateManager();
        final boolean cd = stateManager.isCoolingDown(player);
        final boolean active = stateManager.isUsingShield(player);
        final boolean rising = isShieldRising(player, stateManager);
        final WalksyLibColor currentEnabledColor = customEnabledShieldColor ? enabledColor : COLOR_WHITE;
        final WalksyLibColor currentDisabledColor = customDisabledShieldColor ? disabledColor : COLOR_WHITE;
        final WalksyLibColor currentUseColor = customUsingShieldColor ? usingColor : COLOR_WHITE;
        final WalksyLibColor currentRisingColor = customRisingShieldColor ? risingColor : COLOR_WHITE;
        if (rising && customRisingShieldColor) {
            return currentRisingColor;
        }
        if (active && customUsingShieldColor) {
            return currentUseColor;
        }
        if (!colorInterpolation) {
            return cd ? currentDisabledColor : currentEnabledColor;
        }
        final float progress = cd ? stateManager.getCooldownProgress(player) : 0.0f;
        final int red = (int) (currentEnabledColor.getRed() + (currentDisabledColor.getRed() - currentEnabledColor.getRed()) * progress);
        final int green = (int) (currentEnabledColor.getGreen() + (currentDisabledColor.getGreen() - currentEnabledColor.getGreen()) * progress);
        final int blue = (int) (currentEnabledColor.getBlue()  + (currentDisabledColor.getBlue() - currentEnabledColor.getBlue()) * progress);
        final int alpha = (int) (currentEnabledColor.getAlpha() + (currentDisabledColor.getAlpha() - currentEnabledColor.getAlpha()) * progress);
        return new WalksyLibColor(red, green, blue, alpha);
    }

    private static boolean isShieldRising(final Player player, final WalksyLibShieldStateManager stateManager) {
        final boolean active = stateManager.isUsingShield(player);
        return stateManager.isHoldingUsableShield(player) && player.isUsingItem() && !active && !stateManager.isCoolingDown(player);
    }

    public static Identifier getTexture(final Player player) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (pre26_1TexturePath()) {
            enabledTexture = new IdentifierWrapper(SHIELD_TEXTURE);
            disabledTexture = new IdentifierWrapper(SHIELD_TEXTURE);
        }
        if (player != minecraft.player && selfStateOnly) {
            return Config.enabledTexture.getIdentifier();
        }
        return WalksyLib.getInstance().getShieldStateManager().isCoolingDown(player)
            ? Config.disabledTexture.getIdentifier()
            : Config.enabledTexture.getIdentifier();
    }

    public static void tick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null && minecraft.screen == null) {
            if (ShieldStatus.toggleSelfState.consumeClick()) {
                selfStateOnly = !selfStateOnly;
            }
        }
    }

    private static boolean pre26_1TexturePath() {
        return (enabledTexture != null && SHIELD_TEXTURE_PRE_26_1_PATH.equals(enabledTexture.getIdentifier().getPath())) ||
                (disabledTexture != null && SHIELD_TEXTURE_PRE_26_1_PATH.equals(disabledTexture.getIdentifier().getPath()));
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
    private final Option<Boolean> customUsingColorOption = BooleanOption.createBuilder("Custom Active Shield Color", () -> customUsingShieldColor, customUsingShieldColor, newValue -> customUsingShieldColor = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Allows customization of the shield color while in use"))
        .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
        .build();
    private final Option<WalksyLibColor> usingColorOption = ColorOption.createBuilder("Active Color", () -> usingColor, usingColor, newValue -> usingColor = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Color of the shield when in use"))
        .availability(() -> customUsingShieldColor && modEnabled, "Requires 'Custom Using Shield Color & Mod Enabled' to be enabled")
        .build();
    private final Option<Boolean> customRisingColorOption = BooleanOption.createBuilder("Custom Rising Shield Color", () -> customRisingShieldColor, customRisingShieldColor, newValue -> customRisingShieldColor = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Allows customization of the shield color while rising (held but not yet active)"))
        .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
        .build();
    private final Option<WalksyLibColor> risingColorOption = ColorOption.createBuilder("Rising Color", () -> risingColor, risingColor, newValue -> risingColor = newValue)
        .description(OptionDescription.ofOrderedString(() -> "Color of the shield while rising (held but not yet active)"))
        .availability(() -> customRisingShieldColor && modEnabled, "Requires 'Custom Rising Shield Color & Mod Enabled' to be enabled")
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
        .group(OptionGroup.createBuilder("Rising Shield Options")
            .addOption(customRisingColorOption)
            .addOption(risingColorOption)
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
    public ModConfig define() {
        return ModConfig.createBuilder()
            .path(PathUtils.ofConfigDir("shieldstatus"))
            .category(generalCategory)
            .category(colorCategory)
            .category(textureCategory)
            .build();
    }
}
