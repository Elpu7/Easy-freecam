package dev.elpu7.easyFreecam.neoforge;

import dev.elpu7.easyFreecam.client.EasyFreecamClient;
import dev.elpu7.easyFreecam.client.EasyFreecamConfigScreen;
import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = EasyFreecamNeoForgeClient.MOD_ID, dist = Dist.CLIENT)
public final class EasyFreecamNeoForgeClient {
    public static final String MOD_ID = "easy_freecam";

    public EasyFreecamNeoForgeClient(IEventBus modBus, ModContainer container) {
        EasyFreecamClient.initialize(FMLPaths.CONFIGDIR.get(), container.getModInfo().getVersion().toString());

        modBus.addListener(this::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(this::onStartTick);
        NeoForge.EVENT_BUS.addListener(this::onEndTick);
        container.registerExtensionPoint(
            IConfigScreenFactory.class,
            (ignored, parent) -> new EasyFreecamConfigScreen(parent, Minecraft.getInstance().options)
        );
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KeyMapping.Category category = new KeyMapping.Category(FreecamController.getKeyCategoryId());
        event.registerCategory(category);
        event.register(FreecamController.createToggleKey(category));
    }

    private void onStartTick(ClientTickEvent.Pre event) {
        FreecamController.onStartTick(Minecraft.getInstance());
    }

    private void onEndTick(ClientTickEvent.Post event) {
        FreecamController.onEndTick(Minecraft.getInstance());
    }
}
