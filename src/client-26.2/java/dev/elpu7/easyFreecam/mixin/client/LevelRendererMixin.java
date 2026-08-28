package dev.elpu7.easyFreecam.mixin.client;

import dev.elpu7.easyFreecam.client.FreecamController;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelExtractor.class)
public abstract class LevelRendererMixin {
    @Shadow
    public abstract boolean isEntityVisible(Entity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ);

    @Invoker("extractEntity")
    protected abstract EntityRenderState easyFreecam$extractEntity(Entity entity, float partialTick);

    @Inject(method = "extractVisibleEntities", at = @At("TAIL"))
    private void easyFreecam$extractRealPlayer(
        Camera camera,
        Frustum frustum,
        DeltaTracker deltaTracker,
        LevelRenderState renderState,
        CallbackInfo ci
    ) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (!FreecamController.isEnabled()
            || !FreecamController.shouldShowPlayerModel()
            || player == null
            || client.level == null) {
            return;
        }

        Vec3 cameraPosition = camera.position();
        if (!isEntityVisible(player, frustum, cameraPosition.x, cameraPosition.y, cameraPosition.z)) {
            return;
        }

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(
            !client.level.tickRateManager().isEntityFrozen(player)
        );
        EntityRenderState playerState = easyFreecam$extractEntity(player, partialTick);
        renderState.entityRenderStates.add(playerState);
        renderState.lastEntityRenderStateCount = renderState.entityRenderStates.size();
    }
}
