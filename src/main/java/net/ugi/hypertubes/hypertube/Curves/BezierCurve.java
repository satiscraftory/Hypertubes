package net.ugi.hypertubes.hypertube.Curves;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.sql.SQLOutput;

public class BezierCurve {

    //bezier curve calc
    //https://www.desmos.com/calculator/cahqdxeshd?lang=nl
    //3D version of this

    private HyperTubeCalcCore core;

    private Vec3 pos0;
    private Vec3 pos1;
    private Vec3 pos2;
    private Vec3 pos3;
    private int steps;



    public BezierCurve() {
    }

    public void calcCurveData(double bezierHelpPosMultiplier, BlockPos b1Pos, Direction.Axis b1Axis, int b1Direction , BlockPos b2Pos, Direction.Axis b2Axis, int b2Direction) {

        double distanceBetweenBlocks = b2Pos.getCenter().distanceTo(b1Pos.getCenter());
        double helperPosOffSet = distanceBetweenBlocks * bezierHelpPosMultiplier;

        this.pos0 = b1Pos.getCenter();
        this.pos1 = b1Pos.relative(b1Axis, (int) helperPosOffSet * b1Direction).getCenter();
        this.pos2 = b2Pos.relative(b2Axis, (int) helperPosOffSet * b2Direction).getCenter();
        this.pos3 = b2Pos.getCenter();

        this.steps = (int)Math.round((1.5*Math.abs(b1Pos.getX() - b2Pos.getX()) + 1.5*Math.abs(b1Pos.getY() - b2Pos.getY()) + 1.5*Math.abs(b1Pos.getZ() - b2Pos.getZ()) + 10)*2*Math.clamp(bezierHelpPosMultiplier,0.4,100000));
    }

    public  BlockPos[] getCurveBlockposArray() {
        BlockPos[] blockPosArray = new BlockPos[this.steps];

        for(int i = 0 ; i < this.steps; i++) {
            double t = i/(double)(this.steps-1);// -1 so tah the last one is t = 1
            Vec3 vec = getCurvePos(t);
            int x = vec.x < 0 ? (int)(vec.x) - 1 : (int)(vec.x);
            int y = vec.y < 0 ? (int)(vec.y) - 1 : (int)(vec.y) ;
            int z = vec.z < 0 ? (int)(vec.z) - 1 : (int)(vec.z);
            blockPosArray[i] = new BlockPos(x,y,z);
        }

        return blockPosArray;
    }

    public Vec3 getCurverotation(double t){
        Vec3 facing = getCurvePos(t).subtract(getCurvePos(t-0.001));
//        Vec3 straightVec = new Vec3(0,0,1);
//        float rotation = new Vec3(straightVec.x,0,straightVec.z).toVector3f().angle(new Vec3(facing.x,0, facing.z).toVector3f()); //todo: probably wrong
//        float pitch = new Vec3(0, straightVec.y, straightVec.z).toVector3f().angle(new Vec3(0,facing.y, facing.z).toVector3f()); //todo: probably wrong

        return facing;
    }



    public Vec3 getCurvePos(double t){

        double x = (1-t)*((1-t)*((1-t)*this.pos0.x + t*this.pos1.x) + t*((1-t)*this.pos1.x + t*this.pos2.x)) + t*((1-t)*((1-t)*this.pos1.x + t*this.pos2.x) + t*((1-t)*this.pos2.x + t*this.pos3.x));
        double y = (1-t)*((1-t)*((1-t)*this.pos0.y + t*this.pos1.y) + t*((1-t)*this.pos1.y + t*this.pos2.y)) + t*((1-t)*((1-t)*this.pos1.y + t*this.pos2.y) + t*((1-t)*this.pos2.y + t*this.pos3.y));
        double z = (1-t)*((1-t)*((1-t)*this.pos0.z + t*this.pos1.z) + t*((1-t)*this.pos1.z + t*this.pos2.z)) + t*((1-t)*((1-t)*this.pos1.z + t*this.pos2.z) + t*((1-t)*this.pos2.z + t*this.pos3.z));

        return new Vec3(x,y,z);

    }

    public double getNextT(double current_t, float speed){
        int steps2 = 10000; //todo: config
        steps2 = (int)(steps2 / (speed * 2)); // todo: config : "2"
        double[] ts = new double[steps2 - (int)(current_t * steps2) + 1];
        int index = 0;
        for (int i = (int)Math.round(current_t * steps2); i <= steps2; i++) {
            ts[index++] = (double)i / steps2;
        }


        Vec3 prev_point = getCurvePos(current_t);
        double traveled = 0;
        for (double next_t : ts) {
            Vec3 point = getCurvePos(next_t);
            Vec3 delta = point.subtract(prev_point);
            double segment_length = delta.length();
            traveled += segment_length;

            if (traveled >= speed) {
                return next_t;
            }

            prev_point = point;
        }
        return 1.0;
    }


}
