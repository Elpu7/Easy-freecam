package dev.elpu7.easyFreecam.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public final class FreecamController {
    private static final float LOOK_MULTIPLIER = 0.15F;
    private static final double BASE_SPEED_MULTIPLIER = 18.0D;
    private static final double SPRINT_MULTIPLIER = 4.0D;

    private static final KeyBinding TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.easy-freecam.toggle_freecam",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_F8,
        KeyBinding.Category.create(Identifier.of("easy-freecam", "controls"))
    ));

    private static boolean enabled;
    private static Vec3d position = Vec3d.ZERO;
    private static Vec3d previousPosition = Vec3d.ZERO;
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

    private static void onEndTick(MinecraftClient client) {
        while (TOGGLE_KEY.wasPressed()) {
            toggle(client);
        }

        if (!enabled) {
            return;
        }

        if (client.player == null || client.world == null) {
            disable(client, false);
            return;
        }

        boolean sprintKeyDown = client.options.sprintKey.isPressed();
        if (sprintKeyDown && !sprintKeyWasDown) {
            sprintToggled = !sprintToggled;
        }
        sprintKeyWasDown = sprintKeyDown;

        tickMovement(client);
    }

    private static void toggle(MinecraftClient client) {
        if (enabled) {
            disable(client, true);
        } else {
            enable(client);
        }
    }

    private static void enable(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        ClientPlayerEntity player = client.player;
        enabled = true;
        position = new Vec3d(player.getX(), player.getEyeY(), player.getZ());
        previousPosition = position;
        yaw = player.getYaw();
        pitch = player.getPitch();
        sprintToggled = false;
        sprintKeyWasDown = false;
        previousChunkCullingState = client.chunkCullingEnabled;
        client.chunkCullingEnabled = false;
        sendStatus(player, true);
    }

    private static void disable(MinecraftClient client, boolean notifyPlayer) {
        enabled = false;
        sprintToggled = false;
        sprintKeyWasDown = false;
        client.chunkCullingEnabled = previousChunkCullingState;
        if (notifyPlayer && client.player != null) {
            sendStatus(client.player, false);
        }
    }

    private static void tickMovement(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        GameOptions options = client.options;
        previousPosition = position;
        double speed = player.getAbilities().getFlySpeed() * BASE_SPEED_MULTIPLIER;

        if (sprintToggled) {
            speed *= SPRINT_MULTIPLIER;
        }

        double yawRadians = Math.toRadians(yaw);
        Vec3d forward = new Vec3d(
            -Math.sin(yawRadians),
            0.0D,
            Math.cos(yawRadians)
        );
        Vec3d sideways = new Vec3d(-forward.z, 0.0D, forward.x);
        Vec3d movement = Vec3d.ZERO;

        if (options.forwardKey.isPressed()) {
            movement = movement.add(forward);
        }
        if (options.backKey.isPressed()) {
            movement = movement.subtract(forward);
        }
        if (options.leftKey.isPressed()) {
            movement = movement.subtract(sideways);
        }
        if (options.rightKey.isPressed()) {
            movement = movement.add(sideways);
        }
        if (options.jumpKey.isPressed()) {
            movement = movement.add(0.0D, 1.0D, 0.0D);
        }
        if (options.sneakKey.isPressed()) {
            movement = movement.add(0.0D, -1.0D, 0.0D);
        }

        if (movement.lengthSquared() > 0.0D) {
            position = position.add(movement.normalize().multiply(speed));
        }
    }

    private static void sendStatus(ClientPlayerEntity player, boolean enabled) {
        player.sendMessage(Text.translatable(enabled ? "message.easy-freecam.enabled" : "message.easy-freecam.disabled"), true);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static Vec3d getPosition() {
        return position;
    }

    public static Vec3d getInterpolatedPosition(float tickProgress) {
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
        pitch = MathHelper.clamp(pitch + (float)deltaY * LOOK_MULTIPLIER, -90.0F, 90.0F);
    }

    public static boolean shouldHideHand() {
        return enabled;
    }
}
