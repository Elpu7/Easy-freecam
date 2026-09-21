package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;calculateFov(F)F",
            shift = At.Shift.BEFORE
        )
    )
    private void easyFreecam$positionCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!FreecamController.isEnabled() || client.player == null || client.level == null) {
            return;
        }

        float tickProgress = deltaTracker.getGameTimeDeltaPartialTick(false);
        setPosition(FreecamController.getInterpolatedPosition(tickProgress));
        setRotation(FreecamController.getYaw(), FreecamController.getPitch());
        detached = true;
        FreecamController.updateSmartCull(client, (Camera)(Object)this);
    }
}
