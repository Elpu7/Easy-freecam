package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "handleDamageEvent", at = @At("HEAD"))
    private void easyFreecam$disableFreecamOnDamage(DamageSource source, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (FreecamController.isEnabled()
            && FreecamController.shouldDisableOnDamage()
            && client.player == (Object)this) {
            FreecamController.disableDueToDamage(client);
        }
    }
}
