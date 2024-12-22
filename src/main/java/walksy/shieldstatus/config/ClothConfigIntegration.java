package walksy.shieldstatus.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import me.shedaniel.math.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import walksy.shieldstatus.manager.ConfigManager;

public class ClothConfigIntegration {

    protected static Screen getConfigScreen(Screen parent) {
        // Get the previous screen
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(MinecraftClient.getInstance().currentScreen)
            .setTitle(Text.literal("Shield Status Config"));

        ConfigCategory generalCategory = builder.getOrCreateCategory(Text.literal("General"));
        ConfigCategory colorCategory = builder.getOrCreateCategory(Text.literal("Colors"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        generalCategory.addEntry(entryBuilder.startBooleanToggle(Text.literal("Shield Status Enabled"), ConfigManager.modEnabled)
            .setDefaultValue(ConfigManager.modEnabled)
            .setTooltip(Text.literal("Should enable shield statuses"))
            .setSaveConsumer(newValue -> {
                ConfigManager.modEnabled = newValue;
            })
            .build());

        generalCategory.addEntry(entryBuilder.startBooleanToggle(Text.literal("Shield Interpolation"), ConfigManager.interpolateShields)
            .setDefaultValue(ConfigManager.interpolateShields)
            .setTooltip(Text.literal("Should shield colors interpolate"))
            .setSaveConsumer(newValue -> {
                ConfigManager.interpolateShields = newValue;
            })
            .build());

        SubCategoryBuilder enabledShieldSub = entryBuilder.startSubCategory(Text.literal("Enabled Shield Color"));

        enabledShieldSub.add(entryBuilder.startIntSlider(Text.literal("Enabled Red"), ConfigManager.enabledRed, 0, 255)
            .setDefaultValue(ConfigManager.enabledRed)
            .setTooltip(Text.literal(""))
            .setSaveConsumer(newValue -> {
                ConfigManager.enabledRed = newValue;
            })
            .build());
        enabledShieldSub.add(entryBuilder.startIntSlider(Text.literal("Enabled Green"), ConfigManager.enabledGreen, 0, 255)
            .setDefaultValue(ConfigManager.enabledGreen)
            .setTooltip(Text.literal(""))
            .setSaveConsumer(newValue -> {
                ConfigManager.enabledGreen = newValue;
            })
            .build());
        enabledShieldSub.add(entryBuilder.startIntSlider(Text.literal("Enabled Blue"), ConfigManager.enabledBlue, 0, 255)
            .setDefaultValue(ConfigManager.enabledBlue)
            .setTooltip(Text.literal(""))
            .setSaveConsumer(newValue -> {
                ConfigManager.enabledBlue = newValue;
            })
            .build());
        enabledShieldSub.add(entryBuilder.startIntSlider(Text.literal("Enabled Opacity"), ConfigManager.enabledOpacity, 0, 255)
            .setDefaultValue(ConfigManager.enabledOpacity)
            .setTooltip(Text.literal(""))
            .setSaveConsumer(newValue -> {
                ConfigManager.enabledOpacity = newValue;
            })
            .build());
        colorCategory.addEntry(enabledShieldSub.setExpanded(true).build());

        SubCategoryBuilder disabledShieldSub = entryBuilder.startSubCategory(Text.literal("Disabled Shield Color"));
        disabledShieldSub.add(entryBuilder.startIntSlider(Text.literal("Disabled Red"), ConfigManager.disabledRed, 0, 255)
            .setDefaultValue(ConfigManager.disabledRed)
            .setTooltip(Text.literal(""))
            .setSaveConsumer(newValue -> {
                ConfigManager.disabledRed = newValue;
            })
            .build());
        disabledShieldSub.add(entryBuilder.startIntSlider(Text.literal("Disabled Green"), ConfigManager.disabledGreen, 0, 255)
            .setDefaultValue(ConfigManager.disabledGreen)
            .setTooltip(Text.literal(""))
            .setSaveConsumer(newValue -> {
                ConfigManager.disabledGreen = newValue;
            })
            .build());
        disabledShieldSub.add(entryBuilder.startIntSlider(Text.literal("Disabled Blue"), ConfigManager.disabledBlue, 0, 255)
            .setDefaultValue(ConfigManager.disabledBlue)
            .setTooltip(Text.literal(""))
            .setSaveConsumer(newValue -> {
                ConfigManager.disabledBlue = newValue;
            })
            .build());
        disabledShieldSub.add(entryBuilder.startIntSlider(Text.literal("Disabled Opacity"), ConfigManager.disabledOpacity, 0, 255)
            .setDefaultValue(ConfigManager.disabledOpacity)
            .setTooltip(Text.literal(""))
            .setSaveConsumer(newValue -> {
                ConfigManager.disabledOpacity = newValue;
            })
            .build());
        colorCategory.addEntry(disabledShieldSub.setExpanded(true).build());
        builder.setSavingRunnable(ConfigManager::save);
        return builder.build();
    }
}
