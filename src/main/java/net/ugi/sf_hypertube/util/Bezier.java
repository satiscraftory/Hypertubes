package net.ugi.sf_hypertube.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.ugi.sf_hypertube.block.ModBlocks;

import java.util.LinkedHashSet;
import java.util.Set;

public class Bezier {

    double bezierHelpPosMultiplier =0.5;//default 0.5

    public Bezier(double helpPosMultiplier) {
        this.bezierHelpPosMultiplier = helpPosMultiplier;
    }

    private BlockPos getVectorFromAxis(Direction.Axis axis, int offset){
        BlockPos vector = new BlockPos(offset,0,0);
        if (axis == Direction.Axis.X)vector = new BlockPos(offset,0,0);
        if (axis == Direction.Axis.Y)vector = new BlockPos(0,offset,0);
        if (axis == Direction.Axis.Z)vector = new BlockPos(0,0,offset);
        return vector;
    }

    public BlockPos[] calcBezierArray(BlockPos b1Pos, Direction.Axis b1Axis, int b1Direction, BlockPos b2Pos, Direction.Axis b2Axis, int b2Direction){
        BlockPos pos0 = b1Pos;
        BlockPos pos1 = null;
        BlockPos pos2 = null;
        BlockPos pos3 = b2Pos;
        double distanceBetweenBlocks =  b2Pos.getCenter().distanceTo(b1Pos.getCenter());
        double helperPosOffSet = distanceBetweenBlocks * bezierHelpPosMultiplier;

        if (b1Direction == 0) { // 2 sides available
            pos1 = b2Pos.getCenter().distanceTo(b1Pos.offset(getVectorFromAxis(b1Axis, (int) helperPosOffSet)).getCenter()) < b2Pos.getCenter().distanceTo(b1Pos.offset(getVectorFromAxis(b1Axis, -(int) helperPosOffSet)).getCenter()) ? b1Pos.offset(getVectorFromAxis(b1Axis, (int) helperPosOffSet)) : b1Pos.offset(getVectorFromAxis(b1Axis, -(int) helperPosOffSet));
        }
        else { // 1 side available
            pos1 = b1Pos.offset(getVectorFromAxis(b1Axis, (int) helperPosOffSet * b1Direction));
        }

        if (b2Direction == 0) { // 2 sides available
            pos2 = b1Pos.getCenter().distanceTo(b2Pos.offset(getVectorFromAxis(b2Axis,(int)helperPosOffSet)).getCenter()) < b1Pos.getCenter().distanceTo(b2Pos.offset(getVectorFromAxis(b2Axis,-(int)helperPosOffSet)).getCenter())  ? b2Pos.offset(getVectorFromAxis(b2Axis,(int)helperPosOffSet)) : b2Pos.offset(getVectorFromAxis(b2Axis,-(int)helperPosOffSet));
        }
        else { // 1 side available
            pos2 = b2Pos.offset(getVectorFromAxis(b2Axis,(int)helperPosOffSet * b2Direction));
        }

        int steps = (int)(1.5*Math.abs(b1Pos.getX() - b2Pos.getX()) + 1.5*Math.abs(b1Pos.getY() - b2Pos.getY()) + 1.5*Math.abs(b1Pos.getZ() - b2Pos.getZ()) + 10);


        BlockPos[] blockPosArray = new BlockPos[steps];

        //bezier curve calc
        //https://www.desmos.com/calculator/cahqdxeshd?lang=nl
        //3D version of this

        for(int i = 0 ; i < steps; i++) {
            double t = i/(double)steps;

            int x = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getX() + t*pos1.getX()) + t*((1-t)*pos1.getX() + t*pos2.getX())) + t*((1-t)*((1-t)*pos1.getX() + t*pos2.getX()) + t*((1-t)*pos2.getX() + t*pos3.getX())));
            int y = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getY() + t*pos1.getY()) + t*((1-t)*pos1.getY() + t*pos2.getY())) + t*((1-t)*((1-t)*pos1.getY() + t*pos2.getY()) + t*((1-t)*pos2.getY() + t*pos3.getY())));
            int z = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getZ() + t*pos1.getZ()) + t*((1-t)*pos1.getZ() + t*pos2.getZ())) + t*((1-t)*((1-t)*pos1.getZ() + t*pos2.getZ()) + t*((1-t)*pos2.getZ() + t*pos3.getZ())));

            blockPosArray[i] = new BlockPos(x,y,z);
        }
        Set<BlockPos> blockSet = new LinkedHashSet<>();
        for (BlockPos pos : blockPosArray) {
            if (pos != null) {
                blockSet.add(pos);
            }
        }

        return blockSet.toArray(new BlockPos[0]);
    }
}
