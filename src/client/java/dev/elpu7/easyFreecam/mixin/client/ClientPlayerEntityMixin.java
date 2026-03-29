package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {
    @Inject(method = "aiStep", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$cancelPlayerMovement(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!FreecamController.isEnabled() || client.player != (Object)this) {
            return;
        }

        LocalPlayer player = (LocalPlayer)(Object)this;
        player.setDeltaMovement(Vec3.ZERO);
        player.setSprinting(false);
        ci.cancel();
    }

    @Inject(method = "isControlledCamera", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$renderRealPlayerBody(CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (FreecamController.isEnabled() && client.player == (Object)this) {
            cir.setReturnValue(false);
        }
    }

}
