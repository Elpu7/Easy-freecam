package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {
    @Shadow
    protected abstract boolean isControlledCamera();

    @Inject(method = "tick", at = @At("HEAD"))
    private void easyFreecam$clearPlayerInputBeforeTick(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!FreecamController.isEnabled() || client.player != (Object)this) {
            return;
        }

        LocalPlayer player = (LocalPlayer)(Object)this;
        FreecamController.sanitizePlayerInput(player);
    }

    @Inject(
        method = "aiStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/tutorial/Tutorial;onInput(Lnet/minecraft/client/player/ClientInput;)V",
            shift = At.Shift.AFTER
        )
    )
    private void easyFreecam$clearPlayerInputWhileFreecamIsActive(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!FreecamController.isEnabled() || client.player != (Object)this) {
            return;
        }

        LocalPlayer player = (LocalPlayer)(Object)this;
        FreecamController.sanitizePlayerInput(player);
    }

    @Inject(method = "hurtTo", at = @At("HEAD"))
    private void easyFreecam$disableFreecamWhenDamaged(float health, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!FreecamController.isEnabled() || client.player != (Object)this) {
            return;
        }

        LocalPlayer player = (LocalPlayer)(Object)this;
        if (health <= 0.0F) {
            FreecamController.disableImmediately(client);
            return;
        }

        if (!FreecamController.shouldDisableOnDamage()) {
            return;
        }

        if (health < player.getHealth()) {
            FreecamController.disableDueToDamage(client);
        }
    }

    @Inject(method = "respawn", at = @At("HEAD"))
    private void easyFreecam$disableFreecamBeforeRespawn(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (FreecamController.isEnabled() && client.player == (Object)this) {
            FreecamController.disableImmediately(client);
        }
    }

    @Redirect(
        method = "sendPosition",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;isControlledCamera()Z"
        )
    )
    private boolean easyFreecam$keepSendingPlayerMovementPackets(LocalPlayer player) {
        if (FreecamController.isEnabled() && Minecraft.getInstance().player == player) {
            return true;
        }

        return isControlledCamera();
    }
}
