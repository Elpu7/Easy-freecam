package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
    protected abstract void setPos(Vec3d pos);

    @Inject(method = "update", at = @At("TAIL"))
    private void easyFreecam$applyFreecamPosition(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!FreecamController.isEnabled() || client.player == null || focusedEntity != client.player) {
            return;
        }

        this.setRotation(FreecamController.getYaw(), FreecamController.getPitch());
        this.setPos(FreecamController.getInterpolatedPosition(tickProgress));
    }
}
