package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("HEAD"))
    private void easyFreecam$disableFreecamBeforeDisconnect(Screen screen, boolean transferring, boolean skippingPackCleanup, CallbackInfo ci) {
        FreecamController.handleLevelUnload((Minecraft)(Object)this);
    }

    @Inject(method = "clearClientLevel", at = @At("HEAD"))
    private void easyFreecam$disableFreecamBeforeClearingLevel(Screen screen, CallbackInfo ci) {
        FreecamController.handleLevelUnload((Minecraft)(Object)this);
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void easyFreecam$disableFreecamBeforeLevelChange(ClientLevel level, CallbackInfo ci) {
        Minecraft client = (Minecraft)(Object)this;
        if (FreecamController.isEnabled() && client.level != level) {
            FreecamController.handleLevelUnload(client);
        }
    }
}
