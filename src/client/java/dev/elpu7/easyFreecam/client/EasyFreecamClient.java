package dev.elpu7.easyFreecam.client;

import net.fabricmc.api.ClientModInitializer;

public class EasyFreecamClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EasyFreecamConfigManager.load();
        FreecamController.initialize();
    }
}
