package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.multiplayer.MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Unique
    private static final int OFFHAND_SWAP_BUTTON = 40;

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
            cir.setReturnValue(FreecamController.isSafeItemUse(player, hand) ? InteractionResult.PASS : InteractionResult.FAIL);
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventUsingItemWhileFreecamIsActive(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (FreecamController.isEnabled() && !FreecamController.isSafeItemUse(player, hand)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "handlePickItemFromBlock", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventPickingBlockWhileFreecamIsActive(BlockPos pos, boolean includeData, CallbackInfo ci) {
        if (FreecamController.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePickItemFromEntity", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventPickingEntityWhileFreecamIsActive(Entity entity, boolean includeData, CallbackInfo ci) {
        if (FreecamController.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleContainerInput", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventDroppingFromContainer(
        int containerId,
        int slotId,
        int mouseButton,
        ContainerInput input,
        Player player,
        CallbackInfo ci
    ) {
        if (FreecamController.isEnabled()
            && !FreecamController.shouldAllowInventoryActions()
            && (input == ContainerInput.THROW
                || input == ContainerInput.SWAP && mouseButton == OFFHAND_SWAP_BUTTON)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleCreativeModeItemDrop", at = @At("HEAD"), cancellable = true)
    private void easyFreecam$preventCreativeItemDrop(ItemStack stack, CallbackInfo ci) {
        if (FreecamController.isEnabled() && !FreecamController.shouldAllowInventoryActions()) {
            ci.cancel();
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
