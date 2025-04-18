package net.ugi.sf_hypertube.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.ugi.sf_hypertube.block.custom.HypertubeSupport;
import net.ugi.sf_hypertube.block.entity.HypertubeSupportBlockEntity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class HypertubeEntity extends Entity {
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private Vec3 targetDeltaMovement = Vec3.ZERO;
    private boolean inTube;
    private List<BlockPos> path = new ArrayList<>();
    private BlockPos previousPos;
    private BlockPos currentPos;
    private int speed = 4;
    private int currentPathIndex = 0;



    public HypertubeEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    ///-----------data saving------
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if(previousPos != null) {
            compound.putIntArray("previous_pos", new int[]{previousPos.getX(), previousPos.getY(), previousPos.getZ()});
        }
        if(currentPos != null) {
            compound.putIntArray("current_pos", new int[]{currentPos.getX(), currentPos.getY(), currentPos.getZ()});
        }
        if(currentPathIndex > 0) {
            compound.putInt("current_path_index", currentPathIndex);
        }
/*        if(!path.isEmpty()) {
            compound.put("path", );
        }*/
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        previousPos = null;
        currentPos = null;
        if(compound.contains("previous_pos")) {
            int[] arr = compound.getIntArray("previous_pos");
            if (arr.length == 3) {
                previousPos = new BlockPos(arr[0], arr[1], arr[2]);
            }
        }
        if(compound.contains("current_pos")) {
            int[] arr = compound.getIntArray("current_pos");
            if (arr.length == 3) {
                currentPos = new BlockPos(arr[0], arr[1], arr[2]);
            }
        }
        if(compound.contains("current_path_index")) {
            this.currentPathIndex = compound.getInt("current_path_index");
        }

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
    protected void positionRider(Entity passenger, MoveFunction callback) {
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

    /**
     * Updates the entity motion clientside, called by packets from the server
     */
    @Override
    public void lerpMotion(double x, double y, double z) {
        this.setDeltaMovement(x, y, z);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = Math.sqrt(x * x + z * z);
            this.setXRot((float)(Mth.atan2(y, d0) * 180.0F / (float)Math.PI));
            this.setYRot((float)(Mth.atan2(x, z) * 180.0F / (float)Math.PI));
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }
    }

    public void addPath(List<BlockPos> path, BlockPos previousPos, BlockPos currentPos) {
        this.path.addAll(path);
        this.previousPos = previousPos;
        this.currentPos = currentPos;
    }

    // ─── Copy of Boat#lerpTo ───────────────────────────────────────────────────
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX     = x;
        this.lerpY     = y;
        this.lerpZ     = z;
        this.lerpYRot  = yRot;
        this.lerpXRot  = xRot;
        this.lerpSteps = steps;
    }

    // ─── Bo​at’s diffused client interpolation ─────────────────────────────────
    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            // on the server (or host), zero out lerp and push immediate position
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }

        if (this.lerpSteps > 0) {
            // client‑side: interpolate into the new position
            this.lerpPositionAndRotationStep(
                    this.lerpSteps--,
                    this.lerpX, this.lerpY, this.lerpZ,
                    this.lerpYRot, this.lerpXRot
            );
        } else {
            // once done, ensure we're fully at our real position
            this.reapplyPosition();
            this.setRot(this.getYRot(), this.getXRot());
        }
    }

    // ─── DO NOT TOUCH AT ANY COST ──────────────────────
    @Override
    public void tick() {//DO NOT TOUCH AT ANY COST
        super.tick();
        tickLerp();  // always run first

        // Server‑only motion
        if (!this.level().isClientSide /*&& this.hasExactlyOnePlayerPassenger()*/) {
            // Path handling (ensure path & index are valid)
            if (path != null && currentPathIndex >= 0 && currentPathIndex < path.size()) {
                Vec3 target    = Vec3.atCenterOf(path.get(currentPathIndex));
                Vec3 current   = this.position();
                Vec3 diff      = target.subtract(current);
                double dist    = diff.length();

                if (dist < speed) {
                    // snap to block center and advance
                    this.setPos(target.x, target.y, target.z);
                    currentPathIndex+= speed;
                    this.setDeltaMovement(Vec3.ZERO);
                } else {
                    // move a small step toward the target
                    Vec3 step = diff.normalize().scale(speed);
                    this.setDeltaMovement(step);
                    this.move(MoverType.SELF, step);
                }

                // rotate smoothly
                if (dist > 1e-3) {
                    float yaw = (float)(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
                    this.setYRot(yaw);
                }
            } else {
                if ( path != null){
                    Block block = this.level().getBlockState(this.currentPos).getBlock();

                    if(block instanceof HypertubeSupport hypertubeSupport){
                        if(hypertubeSupport.isConnectedBothSides(this.level(), this.currentPos)){
                            hypertubeSupport.getNextPath(this.level(),this.previousPos,this.currentPos,this);
                        }else{
                            //yeet player
                        }
                    }
                }



                // no path or done – stop moving
                this.setDeltaMovement(Vec3.ZERO);
            }
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


    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    @Override
    public boolean canRiderInteract() {
        return false;
    }


}
