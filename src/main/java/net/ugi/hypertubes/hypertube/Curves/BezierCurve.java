package net.ugi.hypertubes.hypertube.Curves;

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
        //get data
        BlockPos b1Pos = this.core.block1Pos;
        Direction.Axis b1Axis = this.core.block1Axis;
        int b1Direction = this.core.block1Direction;
        BlockPos b2Pos = this.core.block2Pos;
        Direction.Axis b2Axis = this.core.block2Axis;
        int b2Direction = this.core.block2Direction;


        double distanceBetweenBlocks = b2Pos.getCenter().distanceTo(b1Pos.getCenter());
        double helperPosOffSet = distanceBetweenBlocks * bezierHelpPosMultiplier;

        BlockPos pos0 = b1Pos;
        BlockPos pos1 = b1Pos.relative(b1Axis, (int) helperPosOffSet * b1Direction);
        BlockPos pos2 = b2Pos.relative(b2Axis, (int) helperPosOffSet * b2Direction);
        BlockPos pos3 = b2Pos;

        int steps = (int)Math.round((1.5*Math.abs(b1Pos.getX() - b2Pos.getX()) + 1.5*Math.abs(b1Pos.getY() - b2Pos.getY()) + 1.5*Math.abs(b1Pos.getZ() - b2Pos.getZ()) + 10)*2*Math.clamp(bezierHelpPosMultiplier,0.4,100000));

        BlockPos[] blockPosArray = new BlockPos[steps];

        for(int i = 0 ; i < steps; i++) {
            double t = i/(double)(steps-1);// -1 so tah the last one is t = 1

            int x = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getX() + t*pos1.getX()) + t*((1-t)*pos1.getX() + t*pos2.getX())) + t*((1-t)*((1-t)*pos1.getX() + t*pos2.getX()) + t*((1-t)*pos2.getX() + t*pos3.getX())));
            int y = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getY() + t*pos1.getY()) + t*((1-t)*pos1.getY() + t*pos2.getY())) + t*((1-t)*((1-t)*pos1.getY() + t*pos2.getY()) + t*((1-t)*pos2.getY() + t*pos3.getY())));
            int z = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getZ() + t*pos1.getZ()) + t*((1-t)*pos1.getZ() + t*pos2.getZ())) + t*((1-t)*((1-t)*pos1.getZ() + t*pos2.getZ()) + t*((1-t)*pos2.getZ() + t*pos3.getZ())));

            blockPosArray[i] = new BlockPos(x,y,z);
        }

        return blockPosArray;
    }
}
