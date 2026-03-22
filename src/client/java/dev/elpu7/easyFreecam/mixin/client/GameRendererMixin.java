package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.GameRenderer;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$hideHand(float tickProgress, boolean renderBlockOutline, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (FreecamController.shouldHideHand()) {
            ci.cancel();
        }
    }
}
