package dev.elpu7.easyFreecam.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class EasyFreecamConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("easy-freecam");
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
        } catch (JsonParseException exception) {
            LOGGER.error("Failed to parse Easy Freecam config; using defaults", exception);
            boolean backupCreated = backupBrokenConfig();
            config = new EasyFreecamConfig();
            if (backupCreated) {
                save();
            }
        } catch (IOException exception) {
            LOGGER.error("Failed to read Easy Freecam config; using defaults without overwriting it", exception);
            config = new EasyFreecamConfig();
        }
    }

    public static boolean save() {
        Path temporaryPath = null;

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            temporaryPath = Files.createTempFile(CONFIG_PATH.getParent(), "easy-freecam-", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporaryPath)) {
                GSON.toJson(config, writer);
            }

            moveTemporaryConfig(temporaryPath);
            temporaryPath = null;
            return true;
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Failed to save Easy Freecam config", exception);
            return false;
        } finally {
            if (temporaryPath != null) {
                try {
                    Files.deleteIfExists(temporaryPath);
                } catch (IOException exception) {
                    LOGGER.warn("Failed to remove temporary Easy Freecam config {}", temporaryPath, exception);
                }
            }
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

    private static void moveTemporaryConfig(Path temporaryPath) throws IOException {
        try {
            Files.move(
                temporaryPath,
                CONFIG_PATH,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean backupBrokenConfig() {
        Path backupPath = CONFIG_PATH.resolveSibling(
            CONFIG_PATH.getFileName() + ".broken-" + System.currentTimeMillis()
        );

        try {
            Files.move(CONFIG_PATH, backupPath);
            LOGGER.warn("Moved broken Easy Freecam config to {}", backupPath);
            return true;
        } catch (IOException exception) {
            LOGGER.error("Failed to back up broken Easy Freecam config", exception);
            return false;
        }
    }
}
