package dev.elpu7.easyFreecam.client;

import java.nio.file.Path;

public final class EasyFreecamClient {
    private EasyFreecamClient() {
    }

    public static void initialize(Path configDirectory) {
        EasyFreecamConfigManager.initialize(configDirectory);
    }
}
