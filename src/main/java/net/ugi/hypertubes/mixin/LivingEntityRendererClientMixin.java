package net.ugi.hypertubes.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.ugi.hypertubes.entity.HypertubeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.util.Mth.wrapDegrees;
import static org.joml.Math.cos;
import static org.joml.Math.sin;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererClientMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow protected abstract float getFlipDegrees(T livingEntity);

    @Inject(method = "setupRotations", at = @At("HEAD"), cancellable = true)
    public void onSetupRotations(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale, CallbackInfo ci) {
        // Apply pitch rotation to the entire model
        //if(entity.getControlledVehicle() s) return;
        Entity vehicle = entity.getVehicle();
        if(vehicle == null) return;
        if(!(vehicle instanceof HypertubeEntity)) return;
        // Interpolated pitch and yaw
        float prevPitch = vehicle.xRotO;
        float currentPitch = vehicle.getXRot();
        float interpolatedPitch = Mth.lerp(partialTick, prevPitch, currentPitch);

        float prevYaw = vehicle.yRotO;
        float currentYaw = vehicle.getYRot();
        float interpolatedYaw = Mth.lerp(partialTick, prevYaw, currentYaw);

        // Prevent large angle jumps (e.g. 359° to 1°)
        interpolatedPitch = wrapDegrees(interpolatedPitch);
        interpolatedYaw = wrapDegrees(interpolatedYaw);

        float rotationOffset = 0;
        if(entity.getBbHeight() > entity.getBbWidth()*1.6){
            rotationOffset = 90;
        }

        // Align rotation to entity's facing direction
        poseStack.mulPose(Axis.YP.rotationDegrees(-interpolatedYaw));//rotate yaw
        poseStack.mulPose(Axis.XP.rotationDegrees(interpolatedPitch + rotationOffset));//pitch
        poseStack.mulPose(Axis.YP.rotationDegrees(interpolatedYaw));//flatten player
        poseStack.translate(0, -entity.getBbHeight() / 2, 0);
        //poseStack.scale(100F, 1F, 0.1F);
    }


}