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
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPosition(Vec3 pos);

    @Inject(method = "update", at = @At("TAIL"))
    private void easyFreecam$applyFreecamPosition(DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!FreecamController.isEnabled() || client.player == null || client.getCameraEntity() != client.player) {
            return;
        }

        float tickProgress = deltaTracker.getGameTimeDeltaPartialTick(false);
        this.setRotation(FreecamController.getYaw(), FreecamController.getPitch());
        this.setPosition(FreecamController.getInterpolatedPosition(tickProgress));
    }
}
