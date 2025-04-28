package net.ugi.hypertubes.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

import static net.minecraft.world.level.block.RotatedPillarBlock.AXIS;

public class HyperTubeSupportPoleBlock extends RotatedPillarBlock {
    public static final MapCodec<HyperTubeSupportPoleBlock> CODEC = simpleCodec(HyperTubeSupportPoleBlock::new);
    protected static final float AABB_MIN = 6.5F;
    protected static final float AABB_MAX = 9.5F;
    protected static final VoxelShape Y_AXIS_AABB = Block.box((double)3F, (double)0.0F, (double)3F, (double)13F, (double)16.0F, (double)13F);
    protected static final VoxelShape Z_AXIS_AABB = Block.box((double)3F, (double)3F, (double)0.0F, (double)13F, (double)13F, (double)16.0F);
    protected static final VoxelShape X_AXIS_AABB = Block.box((double)0.0F, (double)3F, (double)3F, (double)16.0F, (double)13F, (double)13F);

    public MapCodec<HyperTubeSupportPoleBlock> codec() {
        return CODEC;
    }

    public HyperTubeSupportPoleBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch ((Direction.Axis)state.getValue(AXIS)) {
            case X:
            default:
                return X_AXIS_AABB;
            case Z:
                return Z_AXIS_AABB;
            case Y:
                return Y_AXIS_AABB;
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        return (BlockState)super.getStateForPlacement(context);
    }

    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{AXIS});
    }

    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

}
