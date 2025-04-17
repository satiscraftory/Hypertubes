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
    private float deltaRotation =2f;

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
        float f1 = Mth.clamp(f, -105.0F, 105.0F);
        entityToUpdate.yRotO += f1 - f;
        entityToUpdate.setYRot(entityToUpdate.getYRot() + f1 - f);
        entityToUpdate.setYHeadRot(entityToUpdate.getYRot());
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        super.positionRider(passenger, callback);
        if (!passenger.getType().is(EntityTypeTags.CAN_TURN_IN_BOATS)) {
            this.clampRotation(passenger);
            if (passenger instanceof Animal && this.getPassengers().size() == 1) {
                int i = passenger.getId() % 2 == 0 ? 90 : 270;
                passenger.setYBodyRot(((Animal)passenger).yBodyRot + (float)i);
                passenger.setYHeadRot(passenger.getYHeadRot() + (float)i);
            }
        }
    }


    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public boolean canRiderInteract() {
        return false;
    }

}
