package net.ugi.hypertubes.mixin;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.ugi.hypertubes.entity.HypertubeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerPoseMixin {

    @Inject(method = "setForcedPose", at = @At("HEAD"), cancellable = true)
    public void onSetForcedPose(Pose pose, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // Prevent the pose from being cleared if riding your entity
        if (player.getVehicle() instanceof HypertubeEntity) {
            // Cancel resetting the forced pose
            ci.cancel();
        }

        // Optional: Automatically apply FALL_FLYING if riding your entity
        if (player.getVehicle() instanceof HypertubeEntity && pose == Pose.SWIMMING) {
            // Log or do something here if needed
        }
    }
}