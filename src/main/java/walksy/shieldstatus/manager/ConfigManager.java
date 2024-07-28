package walksy.shieldstatus.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    public static boolean modEnabled = true;
    public static int enabledRed = 0, enabledGreen = 255, enabledBlue = 0;
    public static int disabledRed = 255, disabledGreen = 0, disabledBlue = 0;
    public static boolean interpolateShields = false;


    public static Color getEnabledShieldColor()
    {
        return new Color(enabledRed, enabledGreen, enabledBlue);
    }

    public static Color getDisabledShieldColor()
    {
        return new Color(disabledRed, disabledGreen, disabledBlue);
    }

    //Now THIS might be the worst config manager of all time

    private static final Path configDir = FabricLoader.getInstance().getConfigDir();
    private static final File configFile = configDir.resolve("shieldstatuses.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    public static void save() {
        ConfigData configData = new ConfigData(modEnabled, enabledRed, enabledGreen, enabledBlue,
            disabledRed, disabledGreen, disabledBlue, interpolateShields);
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(configData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                ConfigData configData = GSON.fromJson(reader, ConfigData.class);
                modEnabled = configData.modEnabled;
                enabledRed = configData.enabledRed;
                enabledGreen = configData.enabledGreen;
                enabledBlue = configData.enabledBlue;
                disabledRed = configData.disabledRed;
                disabledGreen = configData.disabledGreen;
                disabledBlue = configData.disabledBlue;
                interpolateShields = configData.interpolateShields;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ConfigData {
        boolean modEnabled;
        int enabledRed, enabledGreen, enabledBlue;
        int disabledRed, disabledGreen, disabledBlue;
        boolean interpolateShields;

        ConfigData(boolean modEnabled, int enabledRed, int enabledGreen, int enabledBlue,
                   int disabledRed, int disabledGreen, int disabledBlue, boolean interpolateShields) {
            this.modEnabled = modEnabled;
            this.enabledRed = enabledRed;
            this.enabledGreen = enabledGreen;
            this.enabledBlue = enabledBlue;
            this.disabledRed = disabledRed;
            this.disabledGreen = disabledGreen;
            this.disabledBlue = disabledBlue;
            this.interpolateShields = interpolateShields;
        }
    }
}
