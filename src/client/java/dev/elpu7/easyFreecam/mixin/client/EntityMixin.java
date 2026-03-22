package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$redirectLook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!FreecamController.isEnabled() || client.player != (Object)this) {
            return;
        }

        FreecamController.handleMouseLook(cursorDeltaX, cursorDeltaY);
        ci.cancel();
    }
}
