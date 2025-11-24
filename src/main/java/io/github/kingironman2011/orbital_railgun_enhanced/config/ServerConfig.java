package io.github.kingironman2011.orbital_railgun_enhanced.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ServerConfig {
    private static final File CONFIG_FILE = new File("config/orbital-railgun-sounds-server-config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final ServerConfig INSTANCE = new ServerConfig();
    private boolean debugMode = false;
    private double soundRange = 500.0;

    public boolean isDebugMode() {
        return debugMode;
    }

    public double getSoundRange() {
        return soundRange;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        saveConfig();
    }

    public void setSoundRange(double soundRange) {
        this.soundRange = soundRange;
        saveConfig();
    }

    public void loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ServerConfig config = GSON.fromJson(reader, ServerConfig.class);
                if (config != null) {
                    this.debugMode = config.debugMode;
                    this.soundRange = config.soundRange;
                } else {
                    OrbitalRailgun.LOGGER.warn("Config file parsed to null, using defaults: {}", CONFIG_FILE.getAbsolutePath());
                }
            } catch (IOException e) {
                OrbitalRailgun.LOGGER.error("Failed to load config: {}", e.getMessage());
            }
        } else {
            saveConfig();
        }
    }

    private void saveConfig() {
        try {
            File parentDir = CONFIG_FILE.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created && !parentDir.exists()) {
                    OrbitalRailgun.LOGGER.warn("Could not create config directory: {}", parentDir.getAbsolutePath());
                }
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            OrbitalRailgun.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }
}
