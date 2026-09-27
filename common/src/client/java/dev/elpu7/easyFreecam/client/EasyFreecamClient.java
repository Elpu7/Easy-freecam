package dev.elpu7.easyFreecam.client;

import dev.elpu7.elib.client.ElibConfigRegistry;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class EasyFreecamClient {
    private EasyFreecamClient() {
    }

    public static void initialize(Path configDirectory, String version) {
        EasyFreecamConfigManager.initialize(configDirectory);
        ElibConfigRegistry.register("easy-freecam", Component.translatable("screen.easy-freecam.config"), version,
            Identifier.fromNamespaceAndPath("easy-freecam", "icon.png"),
            parent -> new EasyFreecamConfigScreen(parent, Minecraft.getInstance().options));
    }
}
