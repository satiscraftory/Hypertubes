package net.ugi.sf_hypertube.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.ugi.sf_hypertube.block.custom.HypertubeSupport;
import net.ugi.sf_hypertube.block.entity.HypertubeSupportBlockEntity;
import net.ugi.sf_hypertube.network.UncappedMotionPayload;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;

public class HypertubeEntity extends Entity {
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;

    private List<BlockPos> path = new ArrayList<>();
    private BlockPos previousPos;
    private BlockPos currentPos;
    private int ticks = 0;
    private int speed = 1;
    private int moveEveryXTicks = 1 ;
    private int currentPathIndex = 0;



    public HypertubeEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    public ListTag savePath(ListTag listTag) {
        for(int i = 0; i < this.path.size(); ++i) {
            if (this.path.get(i) != null) {
                CompoundTag compoundtag = new CompoundTag();
                BlockPos blockpos = this.path.get(i);
                compoundtag.putIntArray(String.valueOf(i), new int[] {blockpos.getX(), blockpos.getY(), blockpos.getZ()});
                listTag.add(compoundtag);
            }

        }
        return listTag;
    }

    public void loadPath(ListTag listTag) {
        for(int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundtag = listTag.getCompound(i);
            int[] arr  = compoundtag.getIntArray(String.valueOf(i));
            this.path.add(new BlockPos(arr[0], arr[1], arr[2]));
        }
    }


    ///-----------data saving------
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if(previousPos != null) {
            compound.putIntArray("previousPos", new int[]{previousPos.getX(), previousPos.getY(), previousPos.getZ()});
        }
        if(currentPos != null) {
            compound.putIntArray("currentPos", new int[]{currentPos.getX(), currentPos.getY(), currentPos.getZ()});
        }
        if(currentPathIndex > 0) {
            compound.putInt("currentPathIndex", currentPathIndex);
        }
        if(!path.isEmpty()) {
            compound.put("path", savePath(new ListTag()));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        previousPos = null;
        currentPos = null;
        if(compound.contains("previousPos")) {
            int[] arr = compound.getIntArray("previousPos");
            if (arr.length == 3) {
                previousPos = new BlockPos(arr[0], arr[1], arr[2]);
            }
        }
        if(compound.contains("currentPos")) {
            int[] arr = compound.getIntArray("currentPos");
            if (arr.length == 3) {
                currentPos = new BlockPos(arr[0], arr[1], arr[2]);
            }
        }
        if(compound.contains("currentPathIndex")) {
            this.currentPathIndex = compound.getInt("currentPathIndex");
        }
        if(compound.contains("path")) {
            ListTag listtag = compound.getList("path",10);
            loadPath(listtag);
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

            if(!(this.ticks >= this.moveEveryXTicks-1)) {
                this.ticks++;
                return;
            }
            this.ticks = 0;
            // Path handling (ensure path & index are valid)
            if (path != null && currentPathIndex >= 0 && currentPathIndex < path.size()) {
                Vec3 target    = Vec3.atCenterOf(path.get(currentPathIndex));
                Vec3 current   = this.position();
                Vec3 diff      = target.subtract(current);
                double dist    = diff.length();

                if (dist < (float)speed/moveEveryXTicks) {
                    // snap to block center and advance
                    this.setPos(target.x, target.y, target.z);
                    currentPathIndex+= speed;
                    //this.setDeltaMovement(Vec3.ZERO);
                } else {
                    // move a small step toward the target
                    Vec3 step = diff.normalize().scale((float)speed/moveEveryXTicks);
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
                            //cull path array
                            for (int i = currentPathIndex -1; i >=0 && !this.path.isEmpty(); i--) {//cull path array
                                this.path.removeFirst();
                                currentPathIndex--;
                            }
                            currentPathIndex = 0;
                            //--cull code
                        }else{

                            //TEST------
                            Direction.Axis axis = this.level().getBlockState(this.currentPos).getValue(AXIS);
                            BlockEntity blockEntity = this.level().getBlockEntity(this.currentPos);
                            if(blockEntity instanceof HypertubeSupportBlockEntity hypertubeSupportBlockEntity){
                            BlockPos exitpos = this.currentPos.relative(axis,-hypertubeSupportBlockEntity.getDirection(this.previousPos));
                            if(!this.position().equals(exitpos.getCenter())) {


                                Vec3 target = Vec3.atCenterOf(exitpos);
                                Vec3 current = this.position();
                                Vec3 diff = target.subtract(current);
                                double dist = diff.length();

                                if (dist < (float)speed/moveEveryXTicks) {
                                    // snap to block center and advance
                                    this.setPos(target.x, target.y, target.z);
                                    //this.setDeltaMovement(Vec3.ZERO);
                                } else {
                                    // move a small step toward the target
                                    Vec3 step = diff.normalize().scale(speed);
                                    this.setDeltaMovement(step);
                                    this.move(MoverType.SELF, step);
                                }

                                // rotate smoothly
                                if (dist > 1e-3) {
                                    float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
                                    this.setYRot(yaw);
                                }
                                if (this.getPassengers().size() != 0){
                                    hypertubeSupportBlockEntity.addEntityToDiscard(this.getPassengers().get(0));
                                }
                            }
                            else{
                                if (this.getPassengers().size() != 0){
                                    Entity passenger = this.getPassengers().get(0);
                                    passenger.stopRiding();
                                    this.discard();

                                    BlockPos blockPosVector = new BlockPos(0,0,0).relative(axis,-hypertubeSupportBlockEntity.getDirection(this.previousPos));
                                    Vec3 vector = new Vec3(blockPosVector.getX(),blockPosVector.getY(), blockPosVector.getZ()).scale((float)speed/moveEveryXTicks);

                                    passenger.teleportTo(exitpos.getCenter().x, exitpos.getCenter().y, exitpos.getCenter().z);

                                    hypertubeSupportBlockEntity.addEntityToDiscard(passenger);
                                    passenger.setDeltaMovement(vector);//maybe we need to somehow call this on the client too
                                    passenger.hasImpulse = true;
                                    if (passenger instanceof ServerPlayer serverPlayer) {
                                        //serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(passenger));
                                        serverPlayer.connection.send(new UncappedMotionPayload(passenger.getId(),vector.x, vector.y, vector.z));
                                    }
                                }

                            }
                            //TEST-------

                            //continue path to inside hypertubesupport (currentpos) and yeet player
                        }
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

    public void setSpeed(float s){
        float f = (s- (int)s);
        f = f == 0 ? 1 : f;
        int multiplier = (int)Math.round(1.0/f);
        this.moveEveryXTicks = multiplier;
        this.speed = (int)(s*multiplier);
    }

    public float getSpeed(){
        return (float)this.speed/(float)this.moveEveryXTicks;
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
