package dev.elpu7.easyFreecam.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;

public final class MinecraftCompatibility {
    private static boolean previousSmartCull = true;
    private static boolean smartCullStateSaved;

    private MinecraftCompatibility() {
    }

    public static Marker createCameraEntity(Minecraft client) {
        return new Marker(EntityType.MARKER, client.level);
    }

    public static void setMainCameraEntity(Minecraft client, Entity cameraEntity) {
        client.gameRenderer.getMainCamera().setLevel(client.level);
        client.gameRenderer.getMainCamera().setEntity(cameraEntity);
    }

    public static boolean hasScreenOpen(Minecraft client) {
        return client.screen != null;
    }

    public static void openScreen(Minecraft client, Screen screen) {
        client.setScreen(screen);
    }

    public static void onFreecamEnabled(Minecraft client) {
        previousSmartCull = client.smartCull;
        smartCullStateSaved = true;
    }

    public static void onFreecamDisabled(Minecraft client) {
        if (smartCullStateSaved) {
            client.smartCull = previousSmartCull;
            smartCullStateSaved = false;
        }
    }

    public static void updateSmartCull(Minecraft client, Camera camera) {
        if (!smartCullStateSaved || client.level == null) {
            return;
        }

        boolean insideSolidBlock = client.level.getBlockState(camera.blockPosition()).isSolidRender();
        client.smartCull = previousSmartCull && !insideSolidBlock;
    }
}
