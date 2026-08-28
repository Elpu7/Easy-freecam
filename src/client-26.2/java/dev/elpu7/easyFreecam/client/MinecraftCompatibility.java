package dev.elpu7.easyFreecam.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Marker;

public final class MinecraftCompatibility {
    private MinecraftCompatibility() {
    }

    public static Marker createCameraEntity(Minecraft client) {
        return new Marker(EntityTypes.MARKER, client.level);
    }

    public static void setMainCameraEntity(Minecraft client, Entity cameraEntity) {
        client.gameRenderer.mainCamera().setLevel(client.level);
        client.gameRenderer.mainCamera().setEntity(cameraEntity);
    }

    public static boolean hasScreenOpen(Minecraft client) {
        return client.gui.screen() != null;
    }

    public static void openScreen(Minecraft client, Screen screen) {
        client.gui.setScreen(screen);
    }

    public static void onFreecamEnabled(Minecraft client) {
    }

    public static void onFreecamDisabled(Minecraft client) {
    }
}
