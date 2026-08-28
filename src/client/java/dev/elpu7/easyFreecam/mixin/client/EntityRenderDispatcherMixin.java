package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$hideRealPlayer(
        Entity entity,
        Frustum frustum,
        double cameraX,
        double cameraY,
        double cameraZ,
        CallbackInfoReturnable<Boolean> cir
    ) {
        Minecraft client = Minecraft.getInstance();
        if (FreecamController.isEnabled()
            && !FreecamController.shouldShowPlayerModel()
            && entity == client.player) {
            cir.setReturnValue(false);
        }
    }
}
