package net.ugi.sf_hypertube.item.custom;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.ugi.sf_hypertube.block.ModBlocks;

public class HyperTubeItem extends Item {
    public HyperTubeItem(Item.Properties properties) {
        super(properties);
    }


    private BlockPos block1Pos;
    private  BlockState block1State;
    private boolean selectedBlock1 = false;
    private BlockPos block2Pos;
    private  BlockState block2State;

    private BlockPos getVectorFromAxis(Direction.Axis axis, int offset){
        BlockPos vector = new BlockPos(offset,0,0);
        if (axis == Direction.Axis.X)vector = new BlockPos(offset,0,0);
        if (axis == Direction.Axis.Y)vector = new BlockPos(0,offset,0);
        if (axis == Direction.Axis.Z)vector = new BlockPos(0,0,offset);
        return vector;
    }


    /**
     * Called when this item is used when targeting a Block
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        if (level.isClientSide()) return InteractionResult.FAIL;
        if (!level.getBlockState(blockpos).is(ModBlocks.HYPERTUBE_SUPPORT)) return InteractionResult.FAIL;


        if(!selectedBlock1) {
            block1Pos = blockpos;
            block1State = level.getBlockState(block1Pos);
            selectedBlock1 = true;
        }
        else {
            block2Pos = blockpos;
            block2State = level.getBlockState(block2Pos);

            //bezier curve calc
            //https://www.desmos.com/calculator/cahqdxeshd?lang=nl
            //3D version of this
            BlockPos pos0 = block1Pos;
            BlockPos pos1;
            BlockPos pos2;
            BlockPos pos3 = block2Pos;
            double distanceBetweenBlocks =  block2Pos.getCenter().distanceTo(block1Pos.getCenter());
            double bezierHelpPosMultiplier = 0.5;//default 0.5
            double helperPosOffSet = distanceBetweenBlocks * bezierHelpPosMultiplier;
            pos1 = block2Pos.getCenter().distanceTo(block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)).getCenter()) < distanceBetweenBlocks  ? block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)) : block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),-(int)helperPosOffSet));
            pos2 = block1Pos.getCenter().distanceTo(block2Pos.offset(getVectorFromAxis(block2State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)).getCenter()) < distanceBetweenBlocks  ? block2Pos.offset(getVectorFromAxis(block2State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)) : block2Pos.offset(getVectorFromAxis(block2State.getValue(BlockStateProperties.AXIS),-(int)helperPosOffSet));

            int steps = (int)(1.5*Math.abs(block1Pos.getX() - block2Pos.getX()) + 1.5*Math.abs(block1Pos.getY() - block2Pos.getY()) + 1.5*Math.abs(block1Pos.getZ() - block2Pos.getZ()) + 10);
            for(int i = 0 ; i < steps; i++) {
                double t = i/(double)steps;

                 int x = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getX() + t*pos1.getX()) + t*((1-t)*pos1.getX() + t*pos2.getX())) + t*((1-t)*((1-t)*pos1.getX() + t*pos2.getX()) + t*((1-t)*pos2.getX() + t*pos3.getX())));
                 int y = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getY() + t*pos1.getY()) + t*((1-t)*pos1.getY() + t*pos2.getY())) + t*((1-t)*((1-t)*pos1.getY() + t*pos2.getY()) + t*((1-t)*pos2.getY() + t*pos3.getY())));
                 int z = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getZ() + t*pos1.getZ()) + t*((1-t)*pos1.getZ() + t*pos2.getZ())) + t*((1-t)*((1-t)*pos1.getZ() + t*pos2.getZ()) + t*((1-t)*pos2.getZ() + t*pos3.getZ())));
                 level.setBlock(new BlockPos(x,y,z), Blocks.GLASS.defaultBlockState(),2 );
                 level.blockUpdated(new BlockPos(x,y,z),Blocks.GLASS);
            }
            selectedBlock1 = false;
        }
        return InteractionResult.SUCCESS;

    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility itemAbility) {
        return net.neoforged.neoforge.common.ItemAbilities.DEFAULT_FLINT_ACTIONS.contains(itemAbility);
    }

}
