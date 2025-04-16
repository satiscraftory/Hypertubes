package net.ugi.sf_hypertube.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
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
    public void removePassenger(Entity passenger) {
        if(hasExactlyOnePlayerPassenger()) {
            Player playerEntity = (Player) passenger;
            if(playerEntity.isCreative()){
                super.removePassenger(passenger);
                this.remove(RemovalReason.DISCARDED);
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
