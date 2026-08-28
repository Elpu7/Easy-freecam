package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "update", at = @At("HEAD"))
    private void easyFreecam$bindFreecamEntityBeforeUpdate(DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!FreecamController.isEnabled() || client.player == null || client.level == null) {
            return;
        }

        Entity freecamEntity = FreecamController.getCameraEntity();
        if (freecamEntity == null) {
            return;
        }

        float tickProgress = deltaTracker.getGameTimeDeltaPartialTick(false);
        FreecamController.syncRenderCameraEntity(client, tickProgress);
        Camera camera = (Camera)(Object)this;
        camera.setLevel(client.level);
        camera.setEntity(freecamEntity);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void easyFreecam$disableSmartCullInsideBlocks(CameraRenderState renderState, float partialTick, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!FreecamController.isEnabled() || client.level == null) {
            return;
        }

        Camera camera = (Camera)(Object)this;
        if (client.level.getBlockState(camera.blockPosition()).isSolidRender()) {
            renderState.smartCull = false;
        }
    }
}
