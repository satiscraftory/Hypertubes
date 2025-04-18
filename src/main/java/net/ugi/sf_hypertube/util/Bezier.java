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

    private double bezierHelpPosMultiplier = 0.5;//default 0.5
    private String curveType = "bezier";

    private int block1UsedDirection = 0;
    private int block2UsedDirection = 0;

    public Bezier() {

    }

    public int getBlock1Direction(){
        return block1UsedDirection;
    }
    public int getBlock2Direction(){
        return block2UsedDirection;
    }

    public void setCurve(String name){
        if (name.equals("Curved")) {
            this.bezierHelpPosMultiplier = 0.5;
            this.curveType = "bezier";
        }
        if (name.equals("Overkill")) {
            this.bezierHelpPosMultiplier = 3;
            this.curveType = "bezier";
        }
        if (name.equals("Straight")) {
            this.bezierHelpPosMultiplier = 0;
            this.curveType = "bezier";
        }
        if (name.equals("Minecraft")) {
            this.curveType = "Minecraft";
        }

    }

    private BlockPos[] checkDuplicate(BlockPos[] blockPosArray){
        Set<BlockPos> blockSet = new LinkedHashSet<>();
        for (BlockPos pos : blockPosArray) {
            if (pos != null) {
                blockSet.add(pos);
            }
        }

        return blockSet.toArray(new BlockPos[0]);
    }



    private Direction.Axis getNextAxis(Direction.Axis Axis){
        if (Axis.equals(Direction.Axis.X)) {
            return Direction.Axis.Y;
        }
        if (Axis.equals(Direction.Axis.Y)) {
            return Direction.Axis.Z;
        }
        return Direction.Axis.X;

    }

    private BlockPos[] calcMinecraftCurve(BlockPos b1Pos, Direction.Axis b1Axis, int b1Direction, BlockPos b2Pos, Direction.Axis b2Axis, int b2Direction){
        if (b1Direction == 0) {
            if (b1Axis == Direction.Axis.X) {
                b1Direction = (b2Pos.getX() - b1Pos.getX());
            }
            if (b1Axis == Direction.Axis.Y) {
                b1Direction = (b2Pos.getY() - b1Pos.getY());
            }
            if (b1Axis == Direction.Axis.Z) {
                b1Direction = (b2Pos.getZ() - b1Pos.getZ());
            }
            if (b1Direction == 0) b1Direction = 1;
            b1Direction = b1Direction / Math.abs(b1Direction);
        }
        else

        if (b2Direction == 0) {
            if (b2Axis == Direction.Axis.X) {
                b2Direction = (b2Pos.getX() - b1Pos.getX());
            }
            if (b2Axis == Direction.Axis.Y) {
                b2Direction = (b2Pos.getY() - b1Pos.getY());
            }
            if (b2Axis == Direction.Axis.Z) {
                b2Direction = (b2Pos.getZ() - b1Pos.getZ());
            }
            if (b2Direction == 0) b2Direction = 1;
            b2Direction = -b2Direction / Math.abs(b2Direction);
        }


        BlockPos pos0 = b1Pos;
        BlockPos pos1 = b1Pos.relative(b1Axis,b1Direction);
        BlockPos pos2 = b2Pos.relative(b2Axis,b2Direction);
        BlockPos pos3 = b2Pos;

        int deltaX = (pos2.getX() - pos1.getX());
        int deltaY = (pos2.getY() - pos1.getY());
        int deltaZ = (pos2.getZ() - pos1.getZ());
        int steps = Math.abs(deltaX) + Math.abs(deltaY) + Math.abs(deltaZ) + 10;

        BlockPos[] blockPosArray = new BlockPos[steps];
        BlockPos pos = pos1;
        int i = 0;

        //axis1 ( first half)
        Direction.Axis nextAxis = b1Axis;
        int nextDelta = (b2Pos.get(nextAxis) - b1Pos.get(nextAxis));
        int nextDirection = nextDelta != 0 ? nextDelta / Math.abs(nextDelta) : 0;

        for(int j = 0; j < Math.abs(nextDelta); j++){
            blockPosArray [i] = pos;
            pos = pos.relative(nextAxis,nextDirection);
            i++;

        }

        //axis2
        nextAxis = getNextAxis(nextAxis);
        nextDelta = (b2Pos.get(nextAxis) - b1Pos.get(nextAxis));
        nextDirection = nextDelta != 0 ? nextDelta / Math.abs(nextDelta) : 0;

        for(int j = 0; j < Math.abs(nextDelta); j++){
            blockPosArray [i] = pos;
            pos = pos.relative(nextAxis,nextDirection);
            i++;

        }

        //axis3
        nextAxis = getNextAxis(nextAxis);
        nextDelta = (b2Pos.get(nextAxis) - b1Pos.get(nextAxis));
        nextDirection = nextDelta != 0 ? nextDelta / Math.abs(nextDelta) : 0;

        for(int j = 0; j < Math.abs(nextDelta); j++){
            blockPosArray [i] = pos;
            pos = pos.relative(nextAxis,nextDirection);
            i++;

        }


        return checkDuplicate(blockPosArray);

    }

    public BlockPos[] calcBezierArray(BlockPos b1Pos, Direction.Axis b1Axis, int b1Direction, BlockPos b2Pos, Direction.Axis b2Axis, int b2Direction) {
        if (this.curveType.equals("Minecraft")) {
            return calcMinecraftCurve(b1Pos, b1Axis, b1Direction, b2Pos, b2Axis, b2Direction);
        }
        BlockPos pos0 = b1Pos;
        BlockPos pos1 = null;
        BlockPos pos2 = null;
        BlockPos pos3 = b2Pos;
        double distanceBetweenBlocks = b2Pos.getCenter().distanceTo(b1Pos.getCenter());
        double helperPosOffSet = distanceBetweenBlocks * bezierHelpPosMultiplier;

        if (b1Direction == 0) { // 2 sides available
            pos1 = b2Pos.getCenter().distanceTo(b1Pos.relative(b1Axis,(int) helperPosOffSet).getCenter()) < b2Pos.getCenter().distanceTo(b1Pos.relative(b1Axis,-(int) helperPosOffSet).getCenter()) ? b1Pos.relative(b1Axis,(int) helperPosOffSet) : b1Pos.relative(b1Axis,-(int) helperPosOffSet);
            this.block1UsedDirection = b2Pos.getCenter().distanceTo(b1Pos.relative(b1Axis,(int) helperPosOffSet).getCenter()) < b2Pos.getCenter().distanceTo(b1Pos.relative(b1Axis,-(int) helperPosOffSet).getCenter()) ? 1:-1;

        } else { // 1 side available
            pos1 = b1Pos.relative(b1Axis, (int) helperPosOffSet * b1Direction);
            this.block1UsedDirection = b1Direction;
        }

        if (b2Direction == 0) { // 2 sides available
            pos2 = b1Pos.getCenter().distanceTo(b2Pos.relative(b2Axis, (int) helperPosOffSet).getCenter()) < b1Pos.getCenter().distanceTo(b2Pos.relative(b2Axis, -(int) helperPosOffSet).getCenter()) ? b2Pos.relative(b2Axis, (int) helperPosOffSet) : b2Pos.relative(b2Axis, -(int) helperPosOffSet);
            this.block2UsedDirection = b1Pos.getCenter().distanceTo(b2Pos.relative(b2Axis, (int) helperPosOffSet).getCenter()) < b1Pos.getCenter().distanceTo(b2Pos.relative(b2Axis, -(int) helperPosOffSet).getCenter()) ? 1 : -1;

        } else { // 1 side available
            pos2 = b2Pos.relative(b2Axis, (int) helperPosOffSet * b2Direction);
            this.block2UsedDirection = b2Direction;
        }

        //fix connection problems ( with direction ) in Straight mode (posmultiplier = 0)
        if(bezierHelpPosMultiplier == 0){
            if (b1Direction == 0) {
                if (b1Axis == Direction.Axis.X) {
                    this.block1UsedDirection = (pos3.getX() - pos0.getX());
                }
                if (b1Axis == Direction.Axis.Y) {
                    this.block1UsedDirection = (pos3.getY() - pos0.getY());
                }
                if (b1Axis == Direction.Axis.Z) {
                    this.block1UsedDirection = (pos3.getZ() - pos0.getZ());
                }
                if (this.block1UsedDirection == 0) this.block1UsedDirection = 1;
                this.block1UsedDirection = this.block1UsedDirection / Math.abs(this.block1UsedDirection);
            }

            if (b2Direction == 0) {
                if (b2Axis == Direction.Axis.X) {
                    this.block2UsedDirection = (pos3.getX() - pos0.getX());
                }
                if (b2Axis == Direction.Axis.Y) {
                    this.block2UsedDirection = (pos3.getY() - pos0.getY());
                }
                if (b2Axis == Direction.Axis.Z) {
                    this.block2UsedDirection = (pos3.getZ() - pos0.getZ());
                }
                if (this.block2UsedDirection == 0) this.block2UsedDirection = 1;
                this.block2UsedDirection = -this.block2UsedDirection / Math.abs(this.block2UsedDirection);
            }

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

        return checkDuplicate(blockPosArray);
    }
}
