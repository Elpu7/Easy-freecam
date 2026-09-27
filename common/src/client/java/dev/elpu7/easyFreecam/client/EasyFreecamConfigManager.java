package dev.elpu7.easyFreecam.client;

import dev.elpu7.elib.config.JsonConfigStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;

public final class EasyFreecamConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("easy-freecam");

    private static JsonConfigStore<EasyFreecamConfig> store;

    private EasyFreecamConfigManager() {
    }

    public static void initialize(Path configDirectory) {
        Path configPath = Objects.requireNonNull(configDirectory, "configDirectory")
            .resolve("easy-freecam.json");
        store = new JsonConfigStore<>(
            configPath,
            EasyFreecamConfig.class,
            EasyFreecamConfig::new,
            EasyFreecamConfigManager::sanitize,
            LOGGER
        );
        store.load();
    }

    public static boolean save() {
        return getStore().save();
    }

    public static EasyFreecamConfig getConfig() {
        return getStore().get();
    }

    private static JsonConfigStore<EasyFreecamConfig> getStore() {
        return Objects.requireNonNull(store, "Easy Freecam config manager has not been initialized");
    }

    private static void sanitize(EasyFreecamConfig config) {
        config.horizontalSpeed = clamp(config.horizontalSpeed, 4.0D, 40.0D, EasyFreecamConfig.DEFAULT_HORIZONTAL_SPEED);
        config.verticalSpeed = clamp(config.verticalSpeed, 4.0D, 40.0D, EasyFreecamConfig.DEFAULT_VERTICAL_SPEED);
        config.sprintMultiplier = clamp(config.sprintMultiplier, 1.0D, 8.0D, EasyFreecamConfig.DEFAULT_SPRINT_MULTIPLIER);
    }

    private static double clamp(double value, double min, double max, double fallback) {
        if (!Double.isFinite(value)) {
            return fallback;
        }

        return Math.clamp(value, min, max);
    }
}
