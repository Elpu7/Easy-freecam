package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.multiplayer.MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow
    public abstract void stopDestroyBlock();

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventAttackingWhileFreecamIsActive(Player player, Entity target, CallbackInfo ci) {
        if (FreecamController.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventDestroyingBlockWhileFreecamIsActive(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (FreecamController.isEnabled()) {
            stopDestroyBlock();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventStartingBlockBreakWhileFreecamIsActive(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (FreecamController.isEnabled()) {
            stopDestroyBlock();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventContinuingBlockBreakWhileFreecamIsActive(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (FreecamController.isEnabled()) {
            stopDestroyBlock();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventUsingBlockWhileFreecamIsActive(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (FreecamController.isEnabled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventUsingItemWhileFreecamIsActive(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (FreecamController.isEnabled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventEntityInteractionWhileFreecamIsActive(Player player, Entity target, EntityHitResult hitResult, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (FreecamController.isEnabled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "piercingAttack", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventPiercingAttacksWhileFreecamIsActive(PiercingWeapon weapon, CallbackInfo ci) {
        if (FreecamController.isEnabled()) {
            ci.cancel();
        }
    }
}
