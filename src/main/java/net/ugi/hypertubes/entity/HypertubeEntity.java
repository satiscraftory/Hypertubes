package net.ugi.hypertubes.entity;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.ugi.hypertubes.Config;
import net.ugi.hypertubes.block.ModBlocks;
import net.ugi.hypertubes.block.custom.HypertubeSupportBlock;
import net.ugi.hypertubes.block.entity.HypertubeSupportBlockEntity;
import net.ugi.hypertubes.network.UncappedMotionPayload;

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
    private float speed = 1;
    private int currentPathIndex = 0;
    private static double viewScale = 1.0;
    private double t = 0;
    private boolean exit = false ;


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
            //passenger.setPose(Pose.FALL_FLYING);
            /*AABB smallBox = new AABB(
                    passenger.getX() - 0.25, passenger.getY() -0.25, passenger.getZ() - 0.25,
                    passenger.getX() + 0.25, passenger.getY() + 0.25, passenger.getZ() + 0.25
            );
            passenger.setBoundingBox(smallBox);*/ // todo:fix? , smaller box = less render distance
            this.clampRotation(passenger);
            if (passenger instanceof Animal && this.getPassengers().size() == 1) {
                Entity vehicle =  passenger.getVehicle();
                if(vehicle == null) return;//fixes crash idk how passanger.getvehicle ever got null though
                passenger.setYBodyRot(vehicle.getYRot());
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

    public void newCurve(BlockPos currentPos, BlockPos nextPos, double t, double lastT) {
        this.t = t;
        this.previousPos = currentPos;
        this.currentPos = nextPos;
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

    // ─── Boat’s diffused client interpolation ─────────────────────────────────
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

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        if(!(entity instanceof Animal)) { //todo fix checks for entity heights
            return new Vec3(0,0.6, 0);
        }
        return new Vec3(0,0,0);
    }

    private void atSupportBlock() {
        Block block = this.level().getBlockState(this.currentPos).getBlock();

        if(block instanceof HypertubeSupportBlock hypertubeSupportBlock){
            if(hypertubeSupportBlock.isConnectedBothSides(this.level(), this.currentPos)){
                HypertubeSupportBlockEntity hypertubeSupportBlockEntity = (HypertubeSupportBlockEntity)this.level().getBlockEntity(this.currentPos);

                if(hypertubeSupportBlockEntity.isBooster(this.level(),currentPos)){
                    this.setSpeed(this.getSpeed()*1.2f);//todo: config
                }

                if(hypertubeSupportBlockEntity.isDetector(this.level(),currentPos)){
                    hypertubeSupportBlockEntity.redstonePowerOutput = (int)(this.getSpeed() / 2);
                    this.level().blockUpdated(currentPos, ModBlocks.HYPERTUBE_SUPPORT.get());

                }

                this.newCurve(this.currentPos, hypertubeSupportBlock.getNextTargetPos(this.level(), previousPos, currentPos), 0, 0);
                this.tick();
            }
            else {
                //start exit process
                if(!this.level().getBlockState(this.currentPos).hasProperty(AXIS))return;//anti crash
                Direction.Axis axis = this.level().getBlockState(this.currentPos).getValue(AXIS);
                if(this.level().getBlockEntity(this.currentPos)== null)return;//anti crash
                BlockEntity blockEntity = this.level().getBlockEntity(this.currentPos);

                if(blockEntity instanceof HypertubeSupportBlockEntity hypertubeSupportBlockEntity) {
                    BlockPos exitPos = this.currentPos.relative(axis, -hypertubeSupportBlockEntity.getDirection(this.previousPos));
                    if (!this.position().equals(exitPos.getCenter())) {
                        //move player to the end of the tube

                        Vec3 target = Vec3.atCenterOf(exitPos);
                        Vec3 current = this.position();
                        Vec3 diff = target.subtract(current);
                        double dist = diff.length();


                        if (dist < speed) {
                            // snap to block center and advance
                            this.setPos(target.x, target.y, target.z);
                            //this.setDeltaMovement(Vec3.ZERO);
                        } else {
                            // move a small step toward the target
                            Vec3 step = diff; //.normalize().scale(speed);
                            this.setDeltaMovement(step);
                            this.move(MoverType.SELF, step);
                        }

                        this.exit = true;

                        if (!this.getPassengers().isEmpty()) {
                            hypertubeSupportBlockEntity.addEntityToIgnore(this.getPassengers().get(0));
                        }
                    } else {
                        //launch player or entity
                        this.tryLaunchEntity(exitPos, hypertubeSupportBlockEntity, axis);
                    }
                    //TEST-------

                    //continue path to inside hypertubesupport (currentpos) and yeet player
                }
            }
        }

    }
    //-------debug-----
    Vec3 lastpos = this.position();
    //-------debug-----

    // ─── DO NOT TOUCH AT ANY COST ──────────────────────
    @Override
    public void tick() {//DO NOT TOUCH AT ANY COST

        super.tick();
        tickLerp();  // always run first

        if(this.getPassengers().isEmpty() /*&& !this.isChunkLoadint()*/){this.kill();}
        // Server‑only motion
        if (!this.level().isClientSide /*&& this.hasExactlyOnePlayerPassenger()*/) {

            //-------debug-----
            System.out.println("tick: " + this.level().getDayTime());
            System.out.println("this.speed: " + this.speed);
            System.out.println("this.speed * 20: " + this.speed*20);
            Vec3 thispos = this.position();
            System.out.println("calculated speed: "  + thispos.subtract(this.lastpos).length());
            System.out.println("calculated speed * 20: "  + thispos.subtract(this.lastpos).length() *20);
            this.lastpos = thispos;
            //-------debug-----

            // Path handling (ensure path & index are valid)
            this.t = ((HypertubeSupportBlock)this.level().getBlockState(this.previousPos).getBlock()).getNextT(this.level(),this.previousPos,this.currentPos,this.t,this.speed);
            Vec3 target    = ((HypertubeSupportBlock)this.level().getBlockState(this.previousPos).getBlock()).getPos(this.level(),this.previousPos,this.currentPos,this.t);
            Vec3 rot = ((HypertubeSupportBlock)this.level().getBlockState(this.previousPos).getBlock()).getRot(this.level(),this.previousPos,this.currentPos,this.t);
            Vec3 current   = this.position();
            Vec3 diff      = target.subtract(current);
            double dist    = diff.length();
            if(t == 1.0 && dist < this.speed  || this.exit == true){
                this.atSupportBlock();
                return;
            }

            this.setSpeed(this.updateSpeed(this.getSpeed()));

            if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof Player player){
                this.getPlayerInputs(player,rot);
            }
            if (this.speed < 0.02){
                swapDirection();
            }

            if (dist < speed) {
                // snap to block center and advance
                this.setPos(target.x, target.y, target.z);
                this.teleportTo(target.x, target.y, target.z);

            } else {
                // move a small step toward the target
                Vec3 step = diff;//.normalize().scale(speed);
                this.setDeltaMovement(step);
                this.move(MoverType.SELF, step);
            }

            // rotate smoothly
            if (dist > 1e-3) {

                // Calculate yaw: rotation around Y axis (horizontal turn)
                float targetYaw = (float)(Math.toDegrees(Math.atan2(rot.z, rot.x)) - 90.0f);

                // Calculate pitch: rotation around X axis (looking up/down)
                float horizontalMag = (float) Math.sqrt(rot.x *rot.x + rot.z * rot.z);
                float targetPitch = (float)(-Math.toDegrees(Math.atan2(rot.y, horizontalMag)));

                // Get current rotation
                float currentYaw = this.getYRot();
                float currentPitch = this.getXRot();

                // Interpolate smoothly (adjust 0.25f to control smoothness)
                float smoothYaw = Mth.rotLerp(1f, currentYaw, targetYaw);//might be able to cut most of this code since interpolation delta is 1
                float smoothPitch = Mth.lerp(1f, currentPitch, targetPitch);

                // Apply new rotation
                this.setYRot(smoothYaw);
                this.setXRot(smoothPitch);
            }
        }
    }

    float updateSpeed(double currentSpeed) {
        double targetSpeed = Config.unBoostedSpeed;
        double dampingFactor = 0.007; // Change this between 0 and 1 //todo: config
        if (currentSpeed > targetSpeed) {
            dampingFactor = dampingFactor/2;
        }
        return (float)(currentSpeed + dampingFactor * (targetSpeed - currentSpeed));
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }

    public float getSpeed(){
        return this.speed;
    }

    public void tryLaunchEntity(BlockPos exitPos, HypertubeSupportBlockEntity hypertubeSupportBlockEntity,  Direction.Axis axis){
        if (!this.getPassengers().isEmpty()){
            Entity passenger = this.getPassengers().get(0);
            passenger.stopRiding();
            this.discard();

            BlockPos blockPosVector = new BlockPos(0,0,0).relative(axis,-hypertubeSupportBlockEntity.getDirection(this.previousPos));
            Vec3 vector = new Vec3(blockPosVector.getX(),blockPosVector.getY(), blockPosVector.getZ()).scale((float)speed);

            passenger.setPos(exitPos.getCenter().x, exitPos.getCenter().y, exitPos.getCenter().z);
            passenger.teleportTo(exitPos.getCenter().x, exitPos.getCenter().y, exitPos.getCenter().z);


            hypertubeSupportBlockEntity.addEntityToIgnore(passenger);
            passenger.setDeltaMovement(vector);//maybe we need to somehow call this on the client too
            passenger.hasImpulse = true;
            if (passenger instanceof ServerPlayer serverPlayer) {
                //serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(passenger));
                serverPlayer.connection.send(new UncappedMotionPayload(passenger.getId(),vector.x, vector.y, vector.z));
                serverPlayer.startFallFlying();
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

    Minecraft mc = Minecraft.getInstance();
    KeyMapping forwardKey = mc.options.keyUp;
    KeyMapping downKey = mc.options.keyDown;
    KeyMapping leftKey = mc.options.keyLeft;
    KeyMapping rightKey = mc.options.keyRight;

    private void getPlayerInputs(Player player, Vec3 moveDirection) {

        Vec3 lookDir = player.getLookAngle();
        Vec3 normalizedVec = lookDir.normalize();
        Vec3 finalVec = new Vec3(0,0,0);

        if (forwardKey.isDown()) {
            finalVec = finalVec.add(new Vec3(normalizedVec.x,normalizedVec.y,normalizedVec.z));
        }

        if (downKey.isDown()) {
            finalVec = finalVec.add(new Vec3(-normalizedVec.x,-normalizedVec.y,-normalizedVec.z));
        }

        if (leftKey.isDown()) {
            finalVec = finalVec.add(new Vec3(normalizedVec.z,0,-normalizedVec.x));
        }
        if (rightKey.isDown()) {
            finalVec = finalVec.add(new Vec3(-normalizedVec.z,0,normalizedVec.x));
        }
        float finalAcceleration = (float) (((moveDirection.dot(finalVec))/moveDirection.length()) * Config.manualAccelerationStrength);
        if(this.speed > Config.unBoostedSpeed && finalAcceleration > 0){
            return;
        }

        this.setSpeed(this.getSpeed() + finalAcceleration);
    }

    private void swapDirection(){
        BlockPos tempPos = this.currentPos;
        this.currentPos = this.previousPos;
        this.previousPos = tempPos;
        this.t = 1 - this.t;
        this.speed = 0.025f;
    }



    /**
     * Checks if the entity is in range to render.
     */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getPassengers().isEmpty() ? 2D: 100d;


        d0 *= 64.0 * viewScale;
        return distance < d0 * d0;
    }


}
