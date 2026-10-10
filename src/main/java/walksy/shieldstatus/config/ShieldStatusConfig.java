package walksy.shieldstatus.config;

import main.walksy.lib.api.WalksyLibApi;
import main.walksy.lib.api.config.Config;
import main.walksy.lib.api.config.option.AvailabilityMode;
import main.walksy.lib.api.config.option.Category;
import main.walksy.lib.api.config.option.Option;
import main.walksy.lib.api.config.option.OptionGroup;
import main.walksy.lib.api.config.option.Options;
import main.walksy.lib.api.config.value.CroppedTexture;
import main.walksy.lib.api.config.value.WLColor;
import main.walksy.lib.api.data.ShieldState;
import main.walksy.lib.core.WalksyLib;
import main.walksy.lib.api.render.PreviewCanvas;
import main.walksy.lib.api.render.AnimatedTextures;
import main.walksy.lib.api.render.CachedTextures;
import main.walksy.lib.api.config.PathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import walksy.shieldstatus.GrayscaleTextureCache;
import walksy.shieldstatus.ShieldStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ShieldStatusConfig implements WalksyLibApi {
    public static final Identifier SHIELD_TEXTURE = Identifier.withDefaultNamespace("textures/entity/shield/shield_base_nopattern.png");
    private static final WLColor COLOR_WHITE = new WLColor(255, 255, 255, 255);
    private static final WLColor DEFAULT_ENABLED = new WLColor(0, 255, 0, 255);
    private static final WLColor DEFAULT_ACTIVE = new WLColor(0, 255, 0, 255);
    private static final WLColor DEFAULT_RISING = new WLColor(255, 255, 0, 255);
    private static final WLColor DEFAULT_DISABLED = new WLColor(255, 0, 0, 255);
    private static final float PHASE_SECONDS = 1.6f;
    private static final float SHIELD_HEIGHT_RATIO = 0.62f;
    private static final float SHIELD_ASPECT = 12f / 22f;
    private static final float TEXTURE_SIZE = 64f;
    private static final float FACE_U0 = 1f / TEXTURE_SIZE;
    private static final float FACE_V0 = 1f / TEXTURE_SIZE;
    private static final float FACE_U1 = 13f / TEXTURE_SIZE;
    private static final float FACE_V1 = 23f / TEXTURE_SIZE;
    private static final float BANNER_INSET_X = 1f / 12f;
    private static final float BANNER_INSET_Y = 1f / 22f;
    private static final float LABEL_SIZE = 10f;
    private static final float LABEL_GAP = 8f;
    private static final String FEATURE_OTHER_PLAYERS = "other-player-shields";
    public static boolean modEnabled = true;
    public static boolean colorInterpolation = false;
    public static boolean grayscaleTexture = false;
    public static ShownPlayers shownPlayers = ShownPlayers.ALL;
    public static boolean customEnabledShieldColor = true;
    public static boolean customUsingShieldColor = false;
    public static boolean customRisingShieldColor = false;
    public static boolean customDisabledShieldColor = true;
    private static WLColor enabledColor = DEFAULT_ENABLED.copy();
    private static WLColor usingColor = DEFAULT_ACTIVE.copy();
    private static WLColor risingColor = DEFAULT_RISING.copy();
    private static WLColor disabledColor = DEFAULT_DISABLED.copy();
    public static boolean customBanner = true;
    public static boolean disabledBanner = false;
    public static CroppedTexture bannerTexture = CroppedTexture.NONE;
    public static CroppedTexture disabledBannerTexture = CroppedTexture.NONE;
    private static boolean otherPlayersDisabledByServer;

    private record Phase(String label, int color, boolean disabled) {
    }

    private static boolean shows(final Player player) {
        final boolean self = player == Minecraft.getInstance().player;
        return switch (shownPlayers) {
            case ALL -> self || !otherPlayersDisabledByServer;
            case SELF_ONLY -> self;
            case OTHER_PLAYERS_ONLY -> !self && !otherPlayersDisabledByServer;
        };
    }

    public static WLColor getColor(@Nullable final Player player) {
        if (player == null) {
            return COLOR_WHITE;
        }
        if (!shows(player)) {
            return COLOR_WHITE;
        }
        final ShieldState stateData = WalksyLib.getInstance().getShieldStateData();
        final boolean cd = stateData.isCoolingDown(player);
        final boolean active = stateData.isUsingShield(player);
        final boolean rising = isShieldRising(player, stateData);
        if (rising && customRisingShieldColor) {
            return risingColor;
        }
        if (active && customUsingShieldColor) {
            return usingColor;
        }
        final WLColor currentEnabledColor = customEnabledShieldColor ? enabledColor : COLOR_WHITE;
        final WLColor currentDisabledColor = customDisabledShieldColor ? disabledColor : COLOR_WHITE;
        if (!colorInterpolation) {
            return cd ? currentDisabledColor : currentEnabledColor;
        }
        return WLColor.lerp(currentEnabledColor, currentDisabledColor, cd ? stateData.getCooldownProgress(player) : 0.0f);
    }

    private static boolean isShieldRising(final Player player, final ShieldState stateData) {
        final boolean active = stateData.isUsingShield(player);
        return stateData.isHoldingUsableShield(player) && player.isUsingItem() && !active && !stateData.isCoolingDown(player);
    }

    public static CroppedTexture shownBanner() {
        return customBanner ? bannerTexture : CroppedTexture.NONE;
    }

    public static CroppedTexture shownDisabledBanner() {
        return customBanner && disabledBanner ? disabledBannerTexture : CroppedTexture.NONE;
    }

    public static CroppedTexture bannerFor(@Nullable final Player player) {
        if (player == null || !customBanner || !disabledBanner || !shows(player)) {
            return shownBanner();
        }
        return WalksyLib.getInstance().getShieldStateData().isCoolingDown(player) && !disabledBannerTexture.isEmpty() ? disabledBannerTexture : shownBanner();
    }

    private static void drawPreview(final PreviewCanvas canvas) {
        final List<Phase> phases = new ArrayList<>();
        phases.add(new Phase("Enabled", tint(customEnabledShieldColor, enabledColor), false));
        if (customRisingShieldColor) {
            phases.add(new Phase("Rising", risingColor.getRGB(), false));
        }
        if (customUsingShieldColor) {
            phases.add(new Phase("Active", usingColor.getRGB(), false));
        }
        final float cycle = canvas.time() / PHASE_SECONDS;
        final int index = (int) cycle % (phases.size() + 1);
        final Phase phase;
        if (index < phases.size()) {
            phase = phases.get(index);
        } else {
            final WLColor from = customDisabledShieldColor ? disabledColor : COLOR_WHITE;
            final WLColor to = customEnabledShieldColor ? enabledColor : COLOR_WHITE;
            final int color = colorInterpolation ? WLColor.lerp(from, to, cycle - (float) Math.floor(cycle)).getRGB() : from.getRGB();
            phase = new Phase("Disabled", color, true);
        }
        final int color = modEnabled ? phase.color() : COLOR_WHITE.getRGB();
        final Identifier texture = grayscaleTexture && modEnabled ? GrayscaleTextureCache.get(SHIELD_TEXTURE) : SHIELD_TEXTURE;
        final float scale = canvas.uiScale();
        final float labelSize = LABEL_SIZE * scale;
        final float height = canvas.height() * SHIELD_HEIGHT_RATIO;
        final float width = height * SHIELD_ASPECT;
        final float x = canvas.x() + (canvas.width() - width) / 2f;
        final float y = canvas.y() + (canvas.height() - height - LABEL_GAP * scale - labelSize) / 2f;
        canvas.blit(texture, x, y, width, height, FACE_U0, FACE_V0, FACE_U1, FACE_V1, color);
        final CroppedTexture banner = phase.disabled() && !shownDisabledBanner().isEmpty() ? shownDisabledBanner() : shownBanner();
        if (modEnabled && !banner.isEmpty() && CachedTextures.isReady(banner.texture())) {
            final float bannerX = x + width * BANNER_INSET_X;
            final float bannerY = y + height * BANNER_INSET_Y;
            canvas.blit(AnimatedTextures.currentFrame(banner.texture()), bannerX, bannerY, width - (bannerX - x) * 2f, height - (bannerY - y) * 2f, banner.u0(), banner.v0(), banner.u1(), banner.v1(), color | 0x00FFFFFF);
        }
        final float baseline = y + height + LABEL_GAP * scale + labelSize * 0.8f;
        canvas.centeredText(phase.label(), canvas.x() + canvas.width() / 2f, baseline, labelSize, canvas.labelColor());
    }

    private static int tint(final boolean custom, final WLColor color) {
        return custom ? color.getRGB() : COLOR_WHITE.getRGB();
    }


    private static Option<Boolean> toggle(final String name, final String saveKey, final Supplier<Boolean> getter, final boolean defaultValue, final Consumer<Boolean> setter) {
        return toggle(name, saveKey, getter, defaultValue, setter, null);
    }

    private static Option<Boolean> toggle(final String name, final String saveKey, final Supplier<Boolean> getter, final boolean defaultValue, final Consumer<Boolean> setter, final String description) {
        return Options.ofBoolean(name, getter, defaultValue, setter).saveKey(saveKey).description(description).build();
    }

    private static Option<WLColor> color(final String name, final Supplier<WLColor> getter, final WLColor defaultValue, final Consumer<WLColor> setter, final Supplier<Boolean> shown) {
        return Options.ofColor(name, getter, defaultValue, setter).availability(shown, AvailabilityMode.HIDE).build();
    }

    @Override
    public Config define() {
        return Config.builder()
            .path(PathUtils.ofConfigDir("shieldstatus"))
            .allowOptOutFromServer(FEATURE_OTHER_PLAYERS, disabled -> otherPlayersDisabledByServer = disabled)
            .category(Category.builder("General")
                .group(OptionGroup.builder("Preview")
                    .option(Options.ofPreview("Preview", ShieldStatusConfig::drawPreview).build())
                    .build())
                .group(OptionGroup.builder("General")
                    .option(toggle("Mod Enabled", "Mod Enabled", () -> modEnabled, true, value -> modEnabled = value))
                    .option(Options.ofEnum("Shown Players", () -> shownPlayers, ShownPlayers.ALL, value -> shownPlayers = value, ShownPlayers.class).saveKey("Shown Players").description("Whose shields get colored").build())
                    .option(Options.ofKeybind("Cycle Shown Players", ShieldStatus.TOGGLE_SELF_STATE).build())
                    .build())
                .group(OptionGroup.builder("Appearance")
                    .option(toggle("Interpolate Colors", "Interpolate Shield Color", () -> colorInterpolation, false, value -> colorInterpolation = value, "Fade from the disabled color back to enabled as the cooldown ends"))
                    .option(toggle("Grayscale Texture", "Grayscale Shield Texture", () -> grayscaleTexture, false, value -> grayscaleTexture = value, "Gray out the shield so tints show their true color"))
                    .build())
                .group(OptionGroup.builder("Enabled & Disabled")
                    .option(toggle("Enabled Tint", "Custom Enabled Shield Color", () -> customEnabledShieldColor, true, value -> customEnabledShieldColor = value, "Color the shield while it can block"))
                    .option(color("Enabled Color", () -> enabledColor, DEFAULT_ENABLED, value -> enabledColor = value, () -> customEnabledShieldColor))
                    .option(toggle("Disabled Tint", "Custom Disabled Shield Color", () -> customDisabledShieldColor, true, value -> customDisabledShieldColor = value, "Color the shield while an axe has disabled it"))
                    .option(color("Disabled Color", () -> disabledColor, DEFAULT_DISABLED, value -> disabledColor = value, () -> customDisabledShieldColor))
                    .build())
                .group(OptionGroup.builder("Rising & Active")
                    .option(toggle("Rising Tint", "Custom Rising Shield Color", () -> customRisingShieldColor, false, value -> customRisingShieldColor = value, "Color the shield while it's being raised"))
                    .option(color("Rising Color", () -> risingColor, DEFAULT_RISING, value -> risingColor = value, () -> customRisingShieldColor))
                    .option(toggle("Active Tint", "Custom Active Shield Color", () -> customUsingShieldColor, false, value -> customUsingShieldColor = value, "Color the shield while it's blocking"))
                    .option(color("Active Color", () -> usingColor, DEFAULT_ACTIVE, value -> usingColor = value, () -> customUsingShieldColor))
                    .build())
                .group(OptionGroup.builder("Textures")
                    .option(toggle("Custom Banner", "Custom Banner", () -> customBanner, false, value -> customBanner = value))
                    .option(Options.ofCroppedTexture("Banner Texture", () -> bannerTexture, CroppedTexture.NONE, value -> bannerTexture = value).aspect(10f, 20f).availability(() -> customBanner, AvailabilityMode.HIDE).build())
                    .option(Options.ofBoolean("Disabled Banner", () -> disabledBanner, false, value -> disabledBanner = value).saveKey("Disabled Banner").availability(() -> customBanner, AvailabilityMode.HIDE).description("Use a different banner while the shield is disabled").build())
                    .option(Options.ofCroppedTexture("Disabled Banner Texture", () -> disabledBannerTexture, CroppedTexture.NONE, value -> disabledBannerTexture = value).aspect(10f, 20f).availability(() -> customBanner && disabledBanner, AvailabilityMode.HIDE).build())
                    .build())
                .build())
            .build();
    }
}
