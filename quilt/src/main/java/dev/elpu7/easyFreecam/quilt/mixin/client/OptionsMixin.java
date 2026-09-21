package dev.elpu7.easyFreecam.quilt.mixin.client;

import dev.elpu7.easyFreecam.client.EasyFreecamClient;
import dev.elpu7.easyFreecam.client.FreecamController;
import java.io.File;
import java.util.Arrays;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.quiltmc.loader.api.QuiltLoader;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Shadow
    @Final
    @Mutable
    public KeyMapping[] keyMappings;

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;load()V",
            shift = At.Shift.BEFORE
        )
    )
    private void easyFreecam$registerKeyMapping(Minecraft minecraft, File gameDirectory, CallbackInfo ci) {
        EasyFreecamClient.initialize(QuiltLoader.getConfigDir());
        KeyMapping.Category category = KeyMapping.Category.register(FreecamController.getKeyCategoryId());
        KeyMapping toggleKey = FreecamController.createToggleKey(category);
        this.keyMappings = Arrays.copyOf(this.keyMappings, this.keyMappings.length + 1);
        this.keyMappings[this.keyMappings.length - 1] = toggleKey;
    }
}
