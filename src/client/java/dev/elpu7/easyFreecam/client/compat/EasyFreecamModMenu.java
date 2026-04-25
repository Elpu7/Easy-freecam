package dev.elpu7.easyFreecam.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.elpu7.easyFreecam.client.EasyFreecamConfigScreen;
import net.minecraft.client.Minecraft;

public final class EasyFreecamModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> new EasyFreecamConfigScreen(screen, Minecraft.getInstance().options);
    }
}
