package dev.elpu7.easyFreecam.fabric;

import dev.elpu7.easyFreecam.client.EasyFreecamClient;
import dev.elpu7.easyFreecam.client.FreecamController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

public final class EasyFreecamFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EasyFreecamClient.initialize(FabricLoader.getInstance().getConfigDir(),
            FabricLoader.getInstance().getModContainer("easy-freecam").orElseThrow()
                .getMetadata().getVersion().getFriendlyString());

        KeyMapping.Category category = KeyMapping.Category.register(FreecamController.getKeyCategoryId());
        KeyMappingHelper.registerKeyMapping(FreecamController.createToggleKey(category));
        ClientTickEvents.START_CLIENT_TICK.register(FreecamController::onStartTick);
        ClientTickEvents.END_CLIENT_TICK.register(FreecamController::onEndTick);
    }
}
