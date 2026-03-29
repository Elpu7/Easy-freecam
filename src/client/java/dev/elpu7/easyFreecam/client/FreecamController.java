package dev.elpu7.easyFreecam.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;

public final class FreecamController {
    private static final float LOOK_MULTIPLIER = 0.15F;
    private static final double BASE_SPEED_MULTIPLIER = 18.0D;
    private static final double SPRINT_MULTIPLIER = 4.0D;

    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("easy-freecam", "controls")
    );

    private static final KeyMapping TOGGLE_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        "key.easy-freecam.toggle_freecam",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F8,
        KEY_CATEGORY
    ));

    private static boolean enabled;
    private static Vec3 position = Vec3.ZERO;
    private static Vec3 previousPosition = Vec3.ZERO;
    private static float yaw;
    private static float pitch;
    private static boolean sprintToggled;
    private static boolean sprintKeyWasDown;
    private static boolean previousChunkCullingState;

    private FreecamController() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(FreecamController::onEndTick);
    }

    private static void onEndTick(Minecraft client) {
        while (TOGGLE_KEY.consumeClick()) {
            toggle(client);
        }

        if (!enabled) {
            return;
        }

        if (client.player == null || client.level == null) {
            disable(client, false);
            return;
        }

        boolean sprintKeyDown = client.options.keySprint.isDown();
        if (sprintKeyDown && !sprintKeyWasDown) {
            sprintToggled = !sprintToggled;
        }
        sprintKeyWasDown = sprintKeyDown;

        tickMovement(client);
    }

    private static void toggle(Minecraft client) {
        if (enabled) {
            disable(client, true);
        } else {
            enable(client);
        }
    }

    private static void enable(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        LocalPlayer player = client.player;
        enabled = true;
        position = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        previousPosition = position;
        yaw = player.getYRot();
        pitch = player.getXRot();
        sprintToggled = false;
        sprintKeyWasDown = false;
        previousChunkCullingState = client.smartCull;
        client.smartCull = false;
        sendStatus(player, true);
    }

    private static void disable(Minecraft client, boolean notifyPlayer) {
        enabled = false;
        sprintToggled = false;
        sprintKeyWasDown = false;
        client.smartCull = previousChunkCullingState;
        if (notifyPlayer && client.player != null) {
            sendStatus(client.player, false);
        }
    }

    private static void tickMovement(Minecraft client) {
        LocalPlayer player = client.player;
        Options options = client.options;
        previousPosition = position;
        double speed = player.getAbilities().getFlyingSpeed() * BASE_SPEED_MULTIPLIER;

        if (sprintToggled) {
            speed *= SPRINT_MULTIPLIER;
        }

        double yawRadians = Math.toRadians(yaw);
        Vec3 forward = new Vec3(
            -Math.sin(yawRadians),
            0.0D,
            Math.cos(yawRadians)
        );
        Vec3 sideways = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 movement = Vec3.ZERO;

        if (options.keyUp.isDown()) {
            movement = movement.add(forward);
        }
        if (options.keyDown.isDown()) {
            movement = movement.subtract(forward);
        }
        if (options.keyLeft.isDown()) {
            movement = movement.subtract(sideways);
        }
        if (options.keyRight.isDown()) {
            movement = movement.add(sideways);
        }
        if (options.keyJump.isDown()) {
            movement = movement.add(0.0D, 1.0D, 0.0D);
        }
        if (options.keyShift.isDown()) {
            movement = movement.add(0.0D, -1.0D, 0.0D);
        }

        if (movement.lengthSqr() > 0.0D) {
            position = position.add(movement.normalize().scale(speed));
        }
    }

    private static void sendStatus(LocalPlayer player, boolean enabled) {
        player.sendOverlayMessage(Component.translatable(
            enabled ? "message.easy-freecam.enabled" : "message.easy-freecam.disabled"
        ));
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static Vec3 getPosition() {
        return position;
    }

    public static Vec3 getInterpolatedPosition(float tickProgress) {
        return previousPosition.lerp(position, tickProgress);
    }

    public static float getYaw() {
        return yaw;
    }

    public static float getPitch() {
        return pitch;
    }

    public static void handleMouseLook(double deltaX, double deltaY) {
        yaw += (float)deltaX * LOOK_MULTIPLIER;
        pitch = Mth.clamp(pitch + (float)deltaY * LOOK_MULTIPLIER, -90.0F, 90.0F);
    }

    public static boolean shouldHideHand() {
        return enabled;
    }
}
