package net.ugi.sf_hypertube.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.ugi.sf_hypertube.block.entity.HypertubeSupportBlockEntity;
import net.ugi.sf_hypertube.item.ModItems;
import org.jetbrains.annotations.Nullable;

public class HypertubeSupport extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static final MapCodec<HypertubeSupport> CODEC = simpleCodec(HypertubeSupport::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public HypertubeSupport(Properties properties) {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new HypertubeSupportBlockEntity(blockPos, blockState);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(state.getBlock() != newState.getBlock()) {
            if(level.getBlockEntity(pos) instanceof HypertubeSupportBlockEntity hypertubeSupportBlockEntity){
                hypertubeSupportBlockEntity.drops();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(level.getBlockEntity(pos) instanceof HypertubeSupportBlockEntity hypertubeSupportBlockEntity) {
            if(hypertubeSupportBlockEntity.inventory.getStackInSlot(0).isEmpty() && stack.is(Items.STICK)) {
                hypertubeSupportBlockEntity.inventory.insertItem(0, stack.copy(), false);
                hypertubeSupportBlockEntity.targetPositive = player.getOnPos();//example on how to edit data
                hypertubeSupportBlockEntity.targetNegative = player.getBlockPosBelowThatAffectsMyMovement();
                stack.shrink(1);
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
            } else if(stack.isEmpty()) {
                ItemStack stackOnPedestal = hypertubeSupportBlockEntity.inventory.extractItem(0, 1, false);
                player.setItemInHand(InteractionHand.MAIN_HAND, stackOnPedestal);
                hypertubeSupportBlockEntity.clearContents();
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
            }
        }

        return ItemInteractionResult.SUCCESS;
    }
}