package dev.elpu7.easyFreecam.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EasyFreecamConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("easy-freecam.json");

    private static EasyFreecamConfig config = new EasyFreecamConfig();

    private EasyFreecamConfigManager() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            EasyFreecamConfig loadedConfig = GSON.fromJson(reader, EasyFreecamConfig.class);
            if (loadedConfig == null) {
                config = new EasyFreecamConfig();
            } else {
                config = sanitize(loadedConfig);
            }
        } catch (IOException | JsonParseException exception) {
            config = new EasyFreecamConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to save easy-freecam config", exception);
        }
    }

    public static EasyFreecamConfig getConfig() {
        return config;
    }

    private static EasyFreecamConfig sanitize(EasyFreecamConfig loadedConfig) {
        loadedConfig.horizontalSpeed = clamp(loadedConfig.horizontalSpeed, 4.0D, 40.0D, EasyFreecamConfig.DEFAULT_HORIZONTAL_SPEED);
        loadedConfig.verticalSpeed = clamp(loadedConfig.verticalSpeed, 4.0D, 40.0D, EasyFreecamConfig.DEFAULT_VERTICAL_SPEED);
        loadedConfig.sprintMultiplier = clamp(loadedConfig.sprintMultiplier, 1.0D, 8.0D, EasyFreecamConfig.DEFAULT_SPRINT_MULTIPLIER);
        return loadedConfig;
    }

    private static double clamp(double value, double min, double max, double fallback) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return fallback;
        }

        return Math.clamp(value, min, max);
    }
}
