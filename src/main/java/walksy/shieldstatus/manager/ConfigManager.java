package walksy.shieldstatus.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    public static ConfigManager INSTANCE = new ConfigManager();
    private static final Path configDir = FabricLoader.getInstance().getConfigDir();
    private static final File configFile = configDir.resolve("shieldstatus.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean interpolateShields = false;

    //worst config manager ever :3 PT2

    public void loadConfig() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Config config = GSON.fromJson(reader, Config.class);
                INSTANCE.interpolateShields = config.interpolateShields;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            saveConfig();
        }
    }

    public void saveConfig() {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            Config config = new Config(INSTANCE.interpolateShields);
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Config {
        public boolean interpolateShields;
        public Config(boolean enabled) {
            this.interpolateShields = enabled;
        }
    }
}
