package net.ugi.sf_hypertube.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class HypertubeEntity extends Entity {
    private ImmutableList<Entity> passengers = ImmutableList.of();
    protected int boardingCooldown;
    private Entity vehicle;

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

/*    public void tick() {
        super.tick();
        if(this.passengers.isEmpty())return;
        Entity passenger = this.passengers.getFirst();
        passenger.setPose(Pose.FALL_FLYING);
    }*/


    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    @Override
    public boolean canRiderInteract() {
        return false;
    }

}
