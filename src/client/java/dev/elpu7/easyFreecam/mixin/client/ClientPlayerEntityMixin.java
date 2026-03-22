package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$cancelPlayerMovement(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!FreecamController.isEnabled() || client.player != (Object)this) {
            return;
        }

        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
        player.setVelocity(Vec3d.ZERO);
        player.setSprinting(false);
        ci.cancel();
    }

    @Inject(method = "isCamera", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$renderRealPlayerBody(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (FreecamController.isEnabled() && client.player == (Object)this) {
            cir.setReturnValue(false);
        }
    }

}
