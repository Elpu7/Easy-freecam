package dev.elpu7.easyFreecam.quilt.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void easyFreecam$onStartTick(CallbackInfo ci) {
        FreecamController.onStartTick((Minecraft)(Object)this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void easyFreecam$onEndTick(CallbackInfo ci) {
        FreecamController.onEndTick((Minecraft)(Object)this);
    }
}
