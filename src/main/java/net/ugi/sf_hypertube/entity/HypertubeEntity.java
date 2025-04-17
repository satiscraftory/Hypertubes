package net.ugi.sf_hypertube.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HypertubeEntity extends Entity {
    private ImmutableList<Entity> passengers = ImmutableList.of();
    protected int boardingCooldown;
    private Entity vehicle;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private Vec3 targetDeltaMovement = Vec3.ZERO;
    private boolean inTube;
    private List<Vec3> path = new ArrayList<>();

    private int currentPathIndex = 0;

    public HypertubeEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    public boolean startRiding(Entity vehicle, boolean force) {
        boolean bool = super.startRiding(vehicle, force);
        return bool;
    }

    @Override
    public void removePassenger(Entity passenger) {
        if(hasExactlyOnePlayerPassenger()) {
            Player playerEntity = (Player) passenger;
            if(playerEntity.isCreative()){
                super.removePassenger(passenger);
                this.discard();
            }

        }
    }

    protected void clampRotation(Entity entityToUpdate) {
        entityToUpdate.setYBodyRot(this.getYRot());
        float f = Mth.wrapDegrees(entityToUpdate.getYRot() - this.getYRot());
        float f1 = Mth.clamp(f, -180.0F, 180.0F);
        entityToUpdate.yRotO += f1 - f;
        entityToUpdate.setYRot(entityToUpdate.getYRot() + f1 - f);
        entityToUpdate.setYHeadRot(entityToUpdate.getYRot());
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        super.positionRider(passenger, callback);
        if (!passenger.getType().is(EntityTypeTags.CAN_TURN_IN_BOATS)) {
            //passenger.setYRot(passenger.getVehicle().getYRot());
            //passenger.setYHeadRot(passenger.getYHeadRot() + this.deltaRotation);
            //passenger.setPose(Pose.FALL_FLYING);
            this.clampRotation(passenger);
            if (passenger instanceof Animal && this.getPassengers().size() == 1) {
                passenger.setYBodyRot(passenger.getVehicle().getYRot());

                //passenger.setYHeadRot(passenger.getYHeadRot() + (float)i);

            }
        }
    }

    @Override
    public void tick() {

        super.tick();

        if (path.isEmpty() || currentPathIndex >= path.size()) {
            path.add(new Vec3(0,100,0));
            return;
        }



        Vec3 target = path.get(currentPathIndex);
        Vec3 currentPos = position();
        Vec3 direction = target.subtract(currentPos).normalize();
        //this.setRot((float) getMotionDirection().get, (float) direction.x);
        double speed = 10; // Adjust for smoother/faster movement
        Vec3 motion = direction.scale(speed);

        // Move
        setPos(currentPos.add(motion));

        // Check if close enough to target
        if (currentPos.distanceTo(target) < speed*2.5) {
            currentPathIndex++;
            path.add(new Vec3(this.getX() + 0.5f, this.getY(), this.getZ()));
        }
/*        this.checkBelowWorld();
        if (this.level().isClientSide) {
            if (this.lerpSteps > 0) {
                this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
                this.lerpSteps--;
            } else {
                this.reapplyPosition();
                this.setRot(this.getYRot(), this.getXRot());
            }
        } else {
            this.applyGravity();
            int i = Mth.floor(this.getX());
            int j = Mth.floor(this.getY());
            int k = Mth.floor(this.getZ());
            if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
                j--;
            }

            BlockPos blockpos = new BlockPos(i, j, k);
            BlockState blockstate = this.level().getBlockState(blockpos);
            this.inTube = BaseRailBlock.isRail(blockstate);
            if (this.inTube) {
                this.moveAlongTrack(blockpos, blockstate);

                this.setRot(this.getYRot(), this.getXRot());

                this.firstTick = false;
            }
        }*/
    }

    protected void moveAlongTrack(BlockPos pos, BlockState state) {

    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    @Override
    public boolean canRiderInteract() {
        return false;
    }

}
