package dev.elpu7.easyFreecam.client;

import dev.elpu7.easyFreecam.mixin.client.ClientInputAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;

public final class FreecamController {
    private static final float LOOK_MULTIPLIER = 0.15F;

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
    private static Entity previousCameraEntity;
    private static Marker freecamCameraEntity;

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
            clearState(client, false, false, false);
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
        syncCameraEntity(client);
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
        sprintKeyWasDown = client.options.keySprint.isDown();
        previousChunkCullingState = client.smartCull;
        client.smartCull = false;
        previousCameraEntity = client.getCameraEntity();
        sanitizePlayerInput(player);
        stopDestroyBlock(client);
        freecamCameraEntity = createCameraEntity(client);
        syncCameraEntity(client);
        setMainCameraEntity(client, freecamCameraEntity);
        client.levelRenderer.allChanged();
        sendStatus(player, true);
    }

    private static void disable(Minecraft client, boolean notifyPlayer) {
        clearState(client, notifyPlayer, true, true);
    }

    public static void handleLevelUnload(Minecraft client) {
        if (!enabled) {
            return;
        }

        clearState(client, false, true, false);
    }

    private static void clearState(Minecraft client, boolean notifyPlayer, boolean restoreCamera, boolean refreshRenderer) {
        enabled = false;
        sprintToggled = false;
        sprintKeyWasDown = false;
        stopDestroyBlock(client);
        client.smartCull = previousChunkCullingState;

        if (restoreCamera) {
            restoreCameraEntity(client, refreshRenderer);
        } else {
            discardCameraState(client, refreshRenderer);
        }

        if (notifyPlayer && client.player != null) {
            sendStatus(client.player, false);
        }
    }

    private static void tickMovement(Minecraft client) {
        LocalPlayer player = client.player;
        Options options = client.options;
        EasyFreecamConfig config = EasyFreecamConfigManager.getConfig();
        previousPosition = position;
        double horizontalSpeed = player.getAbilities().getFlyingSpeed() * config.horizontalSpeed;
        double verticalSpeed = player.getAbilities().getFlyingSpeed() * config.verticalSpeed;

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

        if (movement.lengthSqr() > 0.0D) {
            Vec3 horizontalMovement = new Vec3(movement.x, 0.0D, movement.z);
            Vec3 verticalMovement = new Vec3(0.0D, movement.y, 0.0D);

            if (horizontalMovement.lengthSqr() > 0.0D) {
                position = position.add(horizontalMovement.normalize().scale(horizontalSpeed));
            }
            if (verticalMovement.lengthSqr() > 0.0D) {
                position = position.add(verticalMovement);
            }
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

    public static Entity getCameraEntity() {
        return freecamCameraEntity;
    }

    public static void handleMouseLook(double deltaX, double deltaY) {
        yaw += (float)deltaX * LOOK_MULTIPLIER;
        pitch = Mth.clamp(pitch + (float)deltaY * LOOK_MULTIPLIER, -90.0F, 90.0F);
    }

    public static void clearPlayerInput(LocalPlayer player) {
        ClientInputAccessor inputAccessor = (ClientInputAccessor)player.input;
        inputAccessor.easyFreecam$setKeyPresses(Input.EMPTY);
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
        if (shouldFreezeWalkingMovement(player)) {
            freezePlayerMovement(player);
            return;
        }

        if (player.isPassenger()) {
            suppressPassengerInput(player);
        }
    }

    public static boolean shouldFreezeWalkingMovement(LocalPlayer player) {
        return !player.isPassenger();
    }

    public static void freezePlayerMovement(LocalPlayer player) {
        clearPlayerInput(player);
        player.applyInput();
        player.xxa = 0.0F;
        player.yya = 0.0F;
        player.zza = 0.0F;
        player.setShiftKeyDown(false);
        player.setJumping(false);

        Vec3 velocity = player.getDeltaMovement();
        double verticalVelocity = shouldPreserveVerticalVelocity(player) ? velocity.y : 0.0D;
        player.setDeltaMovement(0.0D, verticalVelocity, 0.0D);
        player.setSprinting(false);
    }

    private static boolean shouldPreserveVerticalVelocity(LocalPlayer player) {
        return !player.getAbilities().flying
            && !player.isSwimming()
            && !player.onClimbable()
            && !player.isFallFlying();
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

    public static void syncRenderCameraEntity(Minecraft client, float tickProgress) {
        if (freecamCameraEntity == null) {
            return;
        }

        Vec3 interpolatedPosition = getInterpolatedPosition(tickProgress);
        freecamCameraEntity.setOldPosAndRot(interpolatedPosition, yaw, pitch);
        freecamCameraEntity.setPos(interpolatedPosition);
        freecamCameraEntity.setYRot(yaw);
        freecamCameraEntity.setXRot(pitch);
        freecamCameraEntity.setYHeadRot(yaw);

        if (client.getCameraEntity() != freecamCameraEntity) {
            client.setCameraEntity(freecamCameraEntity);
        }
    }

    private static void setMainCameraEntity(Minecraft client, Entity cameraEntity) {
        if (client.gameRenderer == null) {
            return;
        }

        client.gameRenderer.getMainCamera().setLevel(client.level);
        client.gameRenderer.getMainCamera().setEntity(cameraEntity);
    }

    private static Marker createCameraEntity(Minecraft client) {
        Marker cameraEntity = new Marker(EntityType.MARKER, client.level);
        cameraEntity.noPhysics = true;
        cameraEntity.setNoGravity(true);
        cameraEntity.setSilent(true);
        cameraEntity.setInvisible(true);
        return cameraEntity;
    }

    private static void syncCameraEntity(Minecraft client) {
        if (freecamCameraEntity == null) {
            return;
        }

        freecamCameraEntity.setOldPosAndRot(previousPosition, yaw, pitch);
        freecamCameraEntity.setPos(position);
        freecamCameraEntity.setYRot(yaw);
        freecamCameraEntity.setXRot(pitch);
        freecamCameraEntity.setYHeadRot(yaw);

        if (client.getCameraEntity() != freecamCameraEntity) {
            client.setCameraEntity(freecamCameraEntity);
        }
    }

    private static void restoreCameraEntity(Minecraft client, boolean refreshRenderer) {
        Entity restoredCamera = getRestoredCameraEntity(client);

        if (restoredCamera != null) {
            client.setCameraEntity(restoredCamera);
            setMainCameraEntity(client, restoredCamera);
        }

        discardCameraState(client, refreshRenderer);
    }

    private static Entity getRestoredCameraEntity(Minecraft client) {
        if (isRestorableCameraEntity(previousCameraEntity, client)) {
            return previousCameraEntity;
        }

        if (isRestorableCameraEntity(client.player, client)) {
            return client.player;
        }

        return null;
    }

    private static boolean isRestorableCameraEntity(Entity entity, Minecraft client) {
        return entity != null
            && client.level != null
            && entity.level() == client.level
            && !entity.isRemoved();
    }

    private static void discardCameraState(Minecraft client, boolean refreshRenderer) {
        previousCameraEntity = null;
        freecamCameraEntity = null;

        if (refreshRenderer && client.levelRenderer != null) {
            client.levelRenderer.allChanged();
        }
    }

    private static void stopDestroyBlock(Minecraft client) {
        if (client.gameMode != null && client.level != null) {
            client.gameMode.stopDestroyBlock();
        }
    }
}
