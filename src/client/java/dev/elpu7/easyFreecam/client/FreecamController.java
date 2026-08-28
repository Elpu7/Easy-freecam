package dev.elpu7.easyFreecam.client;

import dev.elpu7.easyFreecam.mixin.client.ClientInputAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;

public final class FreecamController {
    private static final float LOOK_MULTIPLIER = 0.15F;
    private static final double MIN_SPEED_MULTIPLIER = 0.25D;
    private static final double MAX_SPEED_MULTIPLIER = 8.0D;
    private static final double SPEED_MULTIPLIER_STEP = 1.25D;
    private static final double SMOOTH_ACCELERATION = 0.35D;
    private static final double SMOOTH_DECELERATION = 0.5D;
    private static final double MIN_CAMERA_VELOCITY_SQUARED = 1.0E-8D;
    private static final Input CROUCHING_INPUT = new Input(false, false, false, false, false, true, false);

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
    private static Vec3 cameraVelocity = Vec3.ZERO;
    private static float yaw;
    private static float pitch;
    private static boolean sprintToggled;
    private static boolean sprintKeyWasDown;
    private static boolean playerWasCrouching;
    private static double speedMultiplier = 1.0D;
    private static double scrollAccumulator;
    private static boolean previousSmartCull = true;
    private static boolean smartCullStateSaved;

    private FreecamController() {
    }

    public static void initialize() {
        ClientTickEvents.START_CLIENT_TICK.register(FreecamController::onStartTick);
        ClientTickEvents.END_CLIENT_TICK.register(FreecamController::onEndTick);
    }

    private static void onStartTick(Minecraft client) {
        while (TOGGLE_KEY.consumeClick()) {
            toggle(client);
        }
    }

    private static void onEndTick(Minecraft client) {
        if (!enabled) {
            return;
        }

        if (client.player == null || client.level == null) {
            clearState(client, false);
            return;
        }

        if (client.player.isDeadOrDying()) {
            disableImmediately(client);
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
        playerWasCrouching = player.isShiftKeyDown();
        previousSmartCull = client.smartCull;
        smartCullStateSaved = true;
        enabled = true;
        position = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        previousPosition = position;
        cameraVelocity = Vec3.ZERO;
        yaw = player.getYRot();
        pitch = player.getXRot();
        sprintToggled = false;
        sprintKeyWasDown = client.options.keySprint.isDown();
        speedMultiplier = 1.0D;
        scrollAccumulator = 0.0D;
        sanitizePlayerInput(player);
        stopDestroyBlock(client);
        sendStatus(player, true);
    }

    private static void disable(Minecraft client, boolean notifyPlayer) {
        clearState(client, notifyPlayer);
    }

    public static void handleLevelUnload(Minecraft client) {
        if (!enabled) {
            return;
        }

        clearState(client, false);
    }

    private static void clearState(Minecraft client, boolean notifyPlayer) {
        enabled = false;
        if (smartCullStateSaved) {
            client.smartCull = previousSmartCull;
            smartCullStateSaved = false;
        }
        sprintToggled = false;
        sprintKeyWasDown = false;
        playerWasCrouching = false;
        cameraVelocity = Vec3.ZERO;
        speedMultiplier = 1.0D;
        scrollAccumulator = 0.0D;
        stopDestroyBlock(client);

        if (notifyPlayer && client.player != null) {
            sendStatus(client.player, false);
        }
    }

    private static void tickMovement(Minecraft client) {
        LocalPlayer player = client.player;
        Options options = client.options;
        EasyFreecamConfig config = EasyFreecamConfigManager.getConfig();
        if (!config.adjustSpeedWithMouseWheel) {
            speedMultiplier = 1.0D;
            scrollAccumulator = 0.0D;
        }
        previousPosition = position;
        double horizontalSpeed = player.getAbilities().getFlyingSpeed() * config.horizontalSpeed * speedMultiplier;
        double verticalSpeed = player.getAbilities().getFlyingSpeed() * config.verticalSpeed * speedMultiplier;

        if (sprintToggled) {
            horizontalSpeed *= config.sprintMultiplier;
            verticalSpeed *= config.sprintMultiplier;
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
            movement = movement.add(0.0D, verticalSpeed, 0.0D);
        }
        if (options.keyShift.isDown()) {
            movement = movement.add(0.0D, -verticalSpeed, 0.0D);
        }

        Vec3 targetMovement = Vec3.ZERO;
        Vec3 horizontalMovement = new Vec3(movement.x, 0.0D, movement.z);
        if (horizontalMovement.lengthSqr() > 0.0D) {
            targetMovement = targetMovement.add(horizontalMovement.normalize().scale(horizontalSpeed));
        }
        if (movement.y != 0.0D) {
            targetMovement = targetMovement.add(0.0D, movement.y, 0.0D);
        }

        if (config.smoothCameraMovement) {
            double smoothing = targetMovement.lengthSqr() > 0.0D
                ? SMOOTH_ACCELERATION
                : SMOOTH_DECELERATION;
            cameraVelocity = cameraVelocity.lerp(targetMovement, smoothing);
            if (cameraVelocity.lengthSqr() < MIN_CAMERA_VELOCITY_SQUARED) {
                cameraVelocity = Vec3.ZERO;
            }
        } else {
            cameraVelocity = targetMovement;
        }

        position = position.add(cameraVelocity);
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

    public static void clearPlayerInput(LocalPlayer player) {
        setPlayerInput(player, Input.EMPTY);
    }

    public static boolean handleMouseScroll(Minecraft client, long window, double verticalOffset) {
        if (!enabled
            || !EasyFreecamConfigManager.getConfig().adjustSpeedWithMouseWheel
            || client.player == null
            || client.level == null
            || !client.mouseHandler.isMouseGrabbed()
            || client.getWindow().handle() != window) {
            return false;
        }

        scrollAccumulator += verticalOffset;
        int steps = 0;
        while (scrollAccumulator >= 1.0D) {
            scrollAccumulator -= 1.0D;
            steps++;
        }
        while (scrollAccumulator <= -1.0D) {
            scrollAccumulator += 1.0D;
            steps--;
        }

        if (steps != 0) {
            speedMultiplier = Mth.clamp(
                speedMultiplier * Math.pow(SPEED_MULTIPLIER_STEP, steps),
                MIN_SPEED_MULTIPLIER,
                MAX_SPEED_MULTIPLIER
            );
        }

        return true;
    }

    private static void setPlayerInput(LocalPlayer player, Input input) {
        ClientInputAccessor inputAccessor = (ClientInputAccessor)player.input;
        inputAccessor.easyFreecam$setKeyPresses(input);
        inputAccessor.easyFreecam$setMoveVector(Vec2.ZERO);
    }

    public static void suppressPassengerInput(LocalPlayer player) {
        clearPlayerInput(player);
        player.setShiftKeyDown(false);
        player.setJumping(false);
        player.setSprinting(false);
        player.xxa = 0.0F;
        player.yya = 0.0F;
        player.zza = 0.0F;
    }

    public static void sanitizePlayerInput(LocalPlayer player) {
        if (player.isFallFlying()) {
            suppressMovementInput(player);
            return;
        }

        if (shouldFreezeWalkingMovement(player)) {
            freezePlayerMovement(player);
            return;
        }

        if (player.isPassenger()) {
            suppressPassengerInput(player);
        }
    }

    public static boolean shouldFreezeWalkingMovement(LocalPlayer player) {
        return !player.isPassenger() && !player.isFallFlying();
    }

    private static void suppressMovementInput(LocalPlayer player) {
        clearPlayerInput(player);
        player.setJumping(false);
        player.xxa = 0.0F;
        player.yya = 0.0F;
        player.zza = 0.0F;
    }

    public static void freezePlayerMovement(LocalPlayer player) {
        setPlayerInput(player, playerWasCrouching ? CROUCHING_INPUT : Input.EMPTY);
        player.applyInput();
        player.xxa = 0.0F;
        player.yya = 0.0F;
        player.zza = 0.0F;
        player.setShiftKeyDown(playerWasCrouching);
        player.setJumping(false);
        // Preserve velocity from currents, knockback, gravity, and other world physics.
        player.setSprinting(false);
    }

    public static boolean shouldHideHand() {
        return enabled && !EasyFreecamConfigManager.getConfig().showHand;
    }

    public static boolean shouldShowPlayerModel() {
        return !enabled || EasyFreecamConfigManager.getConfig().showPlayer;
    }

    public static boolean shouldDisableOnDamage() {
        return EasyFreecamConfigManager.getConfig().disableOnDamage;
    }

    public static boolean shouldAllowInventoryActions() {
        return EasyFreecamConfigManager.getConfig().allowInventoryActions;
    }

    public static boolean isSafeItemUse(Player player, InteractionHand hand) {
        EasyFreecamConfig config = EasyFreecamConfigManager.getConfig();
        ItemStack stack = player.getItemInHand(hand);

        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable != null && consumable.animation() == ItemUseAnimation.DRINK) {
            return config.allowDrinks;
        }

        if (stack.has(DataComponents.FOOD)) {
            return config.allowFood;
        }

        return config.allowElytraRockets
            && player.isFallFlying()
            && stack.getItem() == Items.FIREWORK_ROCKET;
    }

    public static void disableImmediately(Minecraft client) {
        if (!enabled) {
            return;
        }

        disable(client, false);
    }

    public static void disableDueToDamage(Minecraft client) {
        if (!enabled) {
            return;
        }

        disable(client, false);
        if (client.player != null) {
            client.player.sendOverlayMessage(Component.translatable("message.easy-freecam.disabled_damage"));
        }
    }

    public static void updateSmartCull(Minecraft client, net.minecraft.client.Camera camera) {
        if (!smartCullStateSaved || client.level == null) {
            return;
        }

        boolean insideSolidBlock = client.level.getBlockState(camera.blockPosition()).isSolidRender();
        client.smartCull = previousSmartCull && !insideSolidBlock;
    }

    private static void stopDestroyBlock(Minecraft client) {
        if (client.gameMode != null && client.level != null) {
            client.gameMode.stopDestroyBlock();
        }
    }
}
