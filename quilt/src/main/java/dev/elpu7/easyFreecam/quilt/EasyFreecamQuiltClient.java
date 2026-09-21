package dev.elpu7.easyFreecam.quilt;

import dev.elpu7.easyFreecam.client.EasyFreecamClient;
import dev.elpu7.easyFreecam.client.FreecamController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.quiltmc.loader.api.QuiltLoader;

public final class EasyFreecamQuiltClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EasyFreecamClient.initialize(QuiltLoader.getConfigDir());

        KeyMapping.Category category = KeyMapping.Category.register(FreecamController.getKeyCategoryId());
        KeyMappingHelper.registerKeyMapping(FreecamController.createToggleKey(category));
        ClientTickEvents.START_CLIENT_TICK.register(FreecamController::onStartTick);
        ClientTickEvents.END_CLIENT_TICK.register(FreecamController::onEndTick);
    }
}
