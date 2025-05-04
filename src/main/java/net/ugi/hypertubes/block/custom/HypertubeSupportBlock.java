package net.ugi.hypertubes.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.ugi.hypertubes.block.ModBlocks;
import net.ugi.hypertubes.block.entity.HypertubeSupportBlockEntity;
import net.ugi.hypertubes.entity.HypertubeEntity;
import net.ugi.hypertubes.entity.ModEntities;
import net.ugi.hypertubes.hypertube.Curves.HyperTubeCalcCore;
import net.ugi.hypertubes.hypertube.Functionalities.HyperTubeDetector;
import net.ugi.hypertubes.hypertube.Functionalities.HyperTubeEntrance;
import net.ugi.hypertubes.item.ModItems;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MathUtil;

import java.util.Arrays;
import java.util.List;

public class HypertubeSupportBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static final MapCodec<HypertubeSupportBlock> CODEC = simpleCodec(HypertubeSupportBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public HypertubeSupportBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    //----------------pillar rotations ----------
    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return rotatePillar(state, rot);
    }

    public static BlockState rotatePillar(BlockState state, Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch ((Direction.Axis)state.getValue(AXIS)) {
                    case X:
                        return state.setValue(AXIS, Direction.Axis.Z);
                    case Z:
                        return state.setValue(AXIS, Direction.Axis.X);
                    default:
                        return state;
                }
            default:
                return state;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    //----------------block entity-----------------


    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(new BlockPos(pos), this, 1);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        if (blockEntity instanceof HypertubeSupportBlockEntity hypertubeSupportBlockEntity ) {

            if(hypertubeSupportBlockEntity.isDetector(level,pos)){
                HyperTubeDetector.resetDetector(hypertubeSupportBlockEntity,level,pos,2);
            }

            if(!hypertubeSupportBlockEntity.isEntrance(level,pos)) return;

            List<Entity> entities = level.getEntities(null, HyperTubeEntrance.getEntranceZone(axis, HyperTubeEntrance.getEntranceDirection(hypertubeSupportBlockEntity), pos));
            hypertubeSupportBlockEntity.removeEntitiesFromIgnore(entities);
            if(entities.isEmpty()) return;

            initiateHypertubeTravelForEntities(level, entities, hypertubeSupportBlockEntity, pos);

        }
        super.tick(state, level, pos, random);
    }


    private void initiateHypertubeTravelForEntities(Level level, List<Entity> entities, HypertubeSupportBlockEntity hypertubeSupportBlockEntity, BlockPos pos) {
        entities.forEach(entity -> {
        if(!hypertubeSupportBlockEntity.ignoredEntities.containsKey(entity) && !(entity instanceof HypertubeEntity)) {
            HypertubeEntity hyperTubeEntity = new HypertubeEntity(ModEntities.HYPERTUBE_ENTITY.get(), level);
            hyperTubeEntity.setPos(pos.getX() + 0.5, pos.getY()+0.5, pos.getZ() + 0.5);
            //hyperTubeEntity.path
            //level.addFreshEntity(hyperTubeEntity);
            //entity.startRiding(hyperTubeEntity);
            BlockPos currentPos = pos;
                    /*
                    crash fix for: Cannot get property EnumProperty{name=axis, clazz=class net.minecraft.core.Direction$Axis, values=[x, y, z]} as it does not exist in Block{minecraft:air}
                    when entering 2 hypertubes
                    */
            if(!level.getBlockState(currentPos).getProperties().contains(AXIS)) return;

            Direction.Axis currentAxis = level.getBlockState(currentPos).getValue(AXIS);
            int currentDirection = - HyperTubeEntrance.getEntranceDirection(hypertubeSupportBlockEntity);

            BlockPos nextPos = hypertubeSupportBlockEntity.getTargetPos(currentDirection);
            if (nextPos == null) return;
            if(!level.getBlockState(nextPos).getProperties().contains(AXIS)) return; //crashfix part 2
            Direction.Axis nextAxis = level.getBlockState(nextPos).getValue(AXIS);
            BlockEntity nextEntity = level.getBlockEntity(nextPos);
            int nextDirection = 0;
            if (nextEntity instanceof HypertubeSupportBlockEntity nextHypertubeSupportBlockEntity) {
                nextDirection = nextHypertubeSupportBlockEntity.getDirection(currentPos);

                String extraData1 =hypertubeSupportBlockEntity.getExtraInfo(currentDirection);
                String extraData2 =nextHypertubeSupportBlockEntity.getExtraInfo(nextDirection);

                HyperTubeCalcCore curveCore = new HyperTubeCalcCore();
                curveCore.setData(currentPos, currentAxis, currentDirection, extraData1, nextPos, nextAxis, nextDirection, extraData2);

                BlockPos[] pathArray = curveCore.getHyperTubeArray(hypertubeSupportBlockEntity.getCurveType(currentDirection));
                if(pathArray == null){//maybe fix extra crashes
                    hyperTubeEntity.discard();
                    return;
                };
                hyperTubeEntity.addPath(
                        Arrays.stream(pathArray).toList(),
                        currentPos, nextPos);
                level.addFreshEntity(hyperTubeEntity);
                float speed = (float) Math.clamp(Math.floor( entity.getDeltaMovement().length()), 1, 20);//todo config maxSpeed
                hyperTubeEntity.setSpeed(speed);
                hypertubeSupportBlockEntity.addEntityToIgnore(entity);
                entity.startRiding(hyperTubeEntity);
            }
        }
    });
    }
    
    public List<BlockPos> getNextPath(Level level, BlockPos previousSupportPos, BlockPos currentSupportPos) {
        Direction.Axis currentAxis = level.getBlockState(currentSupportPos).getValue(AXIS);

        BlockEntity currentEntity = level.getBlockEntity(currentSupportPos);
        if(currentEntity instanceof HypertubeSupportBlockEntity currentHypertubeSupportBlockEntity) {
            int currentDirection = -currentHypertubeSupportBlockEntity.getDirection(previousSupportPos);
            BlockPos nextPos = currentHypertubeSupportBlockEntity.getTargetPos(currentDirection);
            BlockEntity nextEntity = level.getBlockEntity(nextPos);
            if(!(level.getBlockState(nextPos).getBlock() instanceof HypertubeSupportBlock)){//extra anti crash
                if(currentHypertubeSupportBlockEntity.getDirection(nextPos)==1){//todo maybe make this a function and call more often
                    currentHypertubeSupportBlockEntity.targetPositive = null;
                    currentHypertubeSupportBlockEntity.targetPositiveType = null;
                }else if(currentHypertubeSupportBlockEntity.getDirection(nextPos)==-1){
                    currentHypertubeSupportBlockEntity.targetNegative = null;
                    currentHypertubeSupportBlockEntity.targetNegativeType = null;
                }
                return null;
            }
            Direction.Axis nextAxis = level.getBlockState(nextPos).getValue(AXIS);
            if(nextEntity instanceof HypertubeSupportBlockEntity nextHypertubeSupportBlockEntity) {
                int nextDirection = nextHypertubeSupportBlockEntity.getDirection(currentSupportPos);

                String extraData1 =currentHypertubeSupportBlockEntity.getExtraInfo(currentDirection);
                String extraData2 =nextHypertubeSupportBlockEntity.getExtraInfo(nextDirection);

                HyperTubeCalcCore curveCore = new HyperTubeCalcCore();
                curveCore.setData(currentSupportPos, currentAxis, currentDirection, extraData1, nextPos, nextAxis, nextDirection, extraData2);


                return List.of(curveCore.getHyperTubeArray(currentHypertubeSupportBlockEntity.getCurveType(currentDirection)));
            }
        }
        return null;
    }


    public BlockPos getNextTargetPos(Level level, BlockPos previousPos, BlockPos currentPos) {
        BlockEntity currentEntity = level.getBlockEntity(currentPos);
        if(currentEntity instanceof HypertubeSupportBlockEntity currentHypertubeSupportBlockEntity) {
            int currentDirection = -currentHypertubeSupportBlockEntity.getDirection(previousPos);
            return currentHypertubeSupportBlockEntity.getTargetPos(currentDirection);
            }
        return null;
    }

    public boolean isConnectedBothSides(Level level, BlockPos pos){
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof HypertubeSupportBlockEntity hypertubeSupportBlockEntity) {
            return hypertubeSupportBlockEntity.targetPositive != null && hypertubeSupportBlockEntity.targetNegative != null;
        }
        return false;
    }
    

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.scheduleTick(new BlockPos(pos), this, 1);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new HypertubeSupportBlockEntity(blockPos, blockState);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {//todo remove connected tube blocks
        if(state.getBlock() != newState.getBlock()) {
            if(level.getBlockEntity(pos) instanceof HypertubeSupportBlockEntity hypertubeSupportBlockEntity1){
                hypertubeSupportBlockEntity1.drops();
                if(hypertubeSupportBlockEntity1.targetPositive != null){//experintal remove connected tube blocks
                    HypertubeSupportBlockEntity hypertubeSupportBlockEntity2 = (HypertubeSupportBlockEntity)level.getBlockEntity(hypertubeSupportBlockEntity1.targetPositive);
                    HyperTubeCalcCore hyperTubeCalc = new HyperTubeCalcCore();
                    hyperTubeCalc.setDataFromPosAndAxis(level, pos, state.getValue(AXIS), hypertubeSupportBlockEntity1.targetPositive, level.getBlockState(hypertubeSupportBlockEntity1.targetPositive).getValue(AXIS));
                    List<BlockPos> path = new java.util.ArrayList<>(List.of(hyperTubeCalc.getHyperTubeArray(hypertubeSupportBlockEntity1.targetPositiveType)));
                    path.remove(hypertubeSupportBlockEntity1.targetPositive);
                    RemovePath(level, path, state);
                    hypertubeSupportBlockEntity2.removeTarget(pos);
                }
                if(hypertubeSupportBlockEntity1.targetNegative != null){
                    HypertubeSupportBlockEntity hypertubeSupportBlockEntity2 = (HypertubeSupportBlockEntity)level.getBlockEntity(hypertubeSupportBlockEntity1.targetNegative);
                    HyperTubeCalcCore hyperTubeCalc = new HyperTubeCalcCore();
                    hyperTubeCalc.setDataFromPosAndAxis(level, pos, state.getValue(AXIS), hypertubeSupportBlockEntity1.targetNegative, level.getBlockState(hypertubeSupportBlockEntity1.targetNegative).getValue(AXIS));
                    List<BlockPos> path = new java.util.ArrayList<>(List.of((hyperTubeCalc.getHyperTubeArray(hypertubeSupportBlockEntity1.targetNegativeType))));
                    path.remove(hypertubeSupportBlockEntity1.targetNegative);
                    RemovePath(level, path, state);
                    hypertubeSupportBlockEntity2.removeTarget(pos);
                }
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected void RemovePath(Level level, List<BlockPos> path, BlockState oldState) {
        path.forEach( blockPos ->{
                    level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                }
                );
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(level.getBlockEntity(pos) instanceof HypertubeSupportBlockEntity hypertubeSupportBlockEntity) {
            if(hypertubeSupportBlockEntity.inventory.getStackInSlot(0).isEmpty() && stack.is(ModItems.HYPERTUBE_BOOSTER) || stack.is(ModItems.HYPERTUBE_ENTRANCE) || stack.is(ModItems.HYPERTUBE_DETECTOR)) {
                hypertubeSupportBlockEntity.inventory.insertItem(0, stack.copy(), false);
                if(!player.isCreative()) {
                    stack.shrink(1);
                }
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                return ItemInteractionResult.SUCCESS;
            } else if(stack.isEmpty()) {
                ItemStack stackOnSupport = hypertubeSupportBlockEntity.inventory.extractItem(0, 1, false);
                if (stackOnSupport.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                if(!player.isCreative() || !player.getInventory().contains(stackOnSupport)){
                    player.setItemInHand(InteractionHand.MAIN_HAND, stackOnSupport);
                }
                hypertubeSupportBlockEntity.clearContents();
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        int redstonePower = 0;
        if (blockEntity instanceof HypertubeSupportBlockEntity hypertubeBlockEntity) {
            redstonePower = hypertubeBlockEntity.redstonePowerOutput;
        }

        return redstonePower;
    }
}