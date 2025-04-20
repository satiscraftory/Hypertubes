package net.ugi.sf_hypertube.hypertube.Calc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BezierCurve {

    //bezier curve calc
    //https://www.desmos.com/calculator/cahqdxeshd?lang=nl
    //3D version of this

    private HyperTubeCalcCore core;

    public BezierCurve(HyperTubeCalcCore core) {
        this.core = core;
    }

    public BlockPos[] getCurve(double bezierHelpPosMultiplier){
        BlockPos b1Pos = this.core.block1Pos;
        Direction.Axis b1Axis = this.core.block1Axis;
        int b1Direction = this.core.block1Direction;
        String b1ExtraData = this.core.block1ExtraData;
        BlockPos b2Pos = this.core.block2Pos;
        Direction.Axis b2Axis = this.core.block2Axis;
        int b2Direction = this.core.block2Direction;
        String b2ExtraData = this.core.block2ExtraData;

        BlockPos pos0 = b1Pos;
        BlockPos pos1 = null;
        BlockPos pos2 = null;
        BlockPos pos3 = b2Pos;
        double distanceBetweenBlocks = b2Pos.getCenter().distanceTo(b1Pos.getCenter());
        double helperPosOffSet = distanceBetweenBlocks * bezierHelpPosMultiplier;

        //fix connection problems ( with direction ) in Straight mode (posmultiplier = 0)
        if(bezierHelpPosMultiplier == 0){
            if (b1Direction == 0) {
                if (b1Axis == Direction.Axis.X) {
                    b1Direction = (pos3.getX() - pos0.getX());
                }
                if (b1Axis == Direction.Axis.Y) {
                    b1Direction = (pos3.getY() - pos0.getY());
                }
                if (b1Axis == Direction.Axis.Z) {
                    b1Direction = (pos3.getZ() - pos0.getZ());
                }
                if (b1Direction == 0) b1Direction = 1;
                b1Direction = b1Direction / Math.abs(b1Direction);
            }

            if (b2Direction == 0) {
                if (b2Axis == Direction.Axis.X) {
                    b2Direction = (pos3.getX() - pos0.getX());
                }
                if (b2Axis == Direction.Axis.Y) {
                    b2Direction = (pos3.getY() - pos0.getY());
                }
                if (b2Axis == Direction.Axis.Z) {
                    b2Direction = (pos3.getZ() - pos0.getZ());
                }
                if (b2Direction == 0) b2Direction = 1;
                b2Direction = -b2Direction / Math.abs(b2Direction);
            }

        }

        if (b1Direction == 0) { // 2 sides available
            pos1 = b2Pos.getCenter().distanceTo(b1Pos.relative(b1Axis,(int) helperPosOffSet).getCenter()) < b2Pos.getCenter().distanceTo(b1Pos.relative(b1Axis,-(int) helperPosOffSet).getCenter()) ? b1Pos.relative(b1Axis,(int) helperPosOffSet) : b1Pos.relative(b1Axis,-(int) helperPosOffSet);
            b1Direction = b2Pos.getCenter().distanceTo(b1Pos.relative(b1Axis,(int) helperPosOffSet).getCenter()) < b2Pos.getCenter().distanceTo(b1Pos.relative(b1Axis,-(int) helperPosOffSet).getCenter()) ? 1:-1;

        } else { // 1 side available
            pos1 = b1Pos.relative(b1Axis, (int) helperPosOffSet * b1Direction);
        }

        if (b2Direction == 0) { // 2 sides available
            pos2 = b1Pos.getCenter().distanceTo(b2Pos.relative(b2Axis, (int) helperPosOffSet).getCenter()) < b1Pos.getCenter().distanceTo(b2Pos.relative(b2Axis, -(int) helperPosOffSet).getCenter()) ? b2Pos.relative(b2Axis, (int) helperPosOffSet) : b2Pos.relative(b2Axis, -(int) helperPosOffSet);
            b2Direction = b1Pos.getCenter().distanceTo(b2Pos.relative(b2Axis, (int) helperPosOffSet).getCenter()) < b1Pos.getCenter().distanceTo(b2Pos.relative(b2Axis, -(int) helperPosOffSet).getCenter()) ? 1 : -1;

        } else { // 1 side available
            pos2 = b2Pos.relative(b2Axis, (int) helperPosOffSet * b2Direction);
        }

        this.core.setUsedDirections(b1Direction, b2Direction);

        int steps = (int)Math.round((1.5*Math.abs(b1Pos.getX() - b2Pos.getX()) + 1.5*Math.abs(b1Pos.getY() - b2Pos.getY()) + 1.5*Math.abs(b1Pos.getZ() - b2Pos.getZ()) + 10)*2*Math.clamp(bezierHelpPosMultiplier,0.4,100000));


        BlockPos[] blockPosArray = new BlockPos[steps];

        for(int i = 0 ; i < steps; i++) {
            double t = i/(double)steps;

            int x = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getX() + t*pos1.getX()) + t*((1-t)*pos1.getX() + t*pos2.getX())) + t*((1-t)*((1-t)*pos1.getX() + t*pos2.getX()) + t*((1-t)*pos2.getX() + t*pos3.getX())));
            int y = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getY() + t*pos1.getY()) + t*((1-t)*pos1.getY() + t*pos2.getY())) + t*((1-t)*((1-t)*pos1.getY() + t*pos2.getY()) + t*((1-t)*pos2.getY() + t*pos3.getY())));
            int z = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getZ() + t*pos1.getZ()) + t*((1-t)*pos1.getZ() + t*pos2.getZ())) + t*((1-t)*((1-t)*pos1.getZ() + t*pos2.getZ()) + t*((1-t)*pos2.getZ() + t*pos3.getZ())));

            blockPosArray[i] = new BlockPos(x,y,z);
        }

        return blockPosArray;
    }
}
