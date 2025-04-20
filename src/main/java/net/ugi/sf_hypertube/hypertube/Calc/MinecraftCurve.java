package net.ugi.sf_hypertube.hypertube.Calc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class MinecraftCurve {
    private HyperTubeCalcCore core;

    public MinecraftCurve(HyperTubeCalcCore core) {
        this.core = core;
    }

    private static Direction.Axis getNextAxis(Direction.Axis Axis) {
        if (Axis.equals(Direction.Axis.X)) {
            return Direction.Axis.Y;
        }
        if (Axis.equals(Direction.Axis.Y)) {
            return Direction.Axis.Z;
        }
        return Direction.Axis.X;

    }

    private BlockPos[] calcArray(BlockPos b1Pos, Direction.Axis b1Axis, int b1Direction, BlockPos b2Pos, Direction.Axis b2Axis, int b2Direction) {
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

        this.core.setUsedDirections(b1Direction, b2Direction);


        BlockPos pos0 = b1Pos;
        BlockPos pos1 = b1Pos.relative(b1Axis, b1Direction);
        BlockPos pos2 = b2Pos.relative(b2Axis, b2Direction);
        BlockPos pos3 = b2Pos;

        int deltaX = (pos2.getX() - pos1.getX());
        int deltaY = (pos2.getY() - pos1.getY());
        int deltaZ = (pos2.getZ() - pos1.getZ());
        int steps = Math.abs(deltaX) + Math.abs(deltaY) + Math.abs(deltaZ) + 10;

        BlockPos[] blockPosArray = new BlockPos[steps];
        BlockPos pos = pos1;
        int i = 0;

        //axis1
        Direction.Axis nextAxis = b1Axis;
        int nextDelta = (pos2.get(nextAxis) - pos1.get(nextAxis));
        int nextDirection = nextDelta != 0 ? nextDelta / Math.abs(nextDelta) : 0;

        for (int j = 0; j < Math.abs(nextDelta); j++) {
            blockPosArray[i] = pos;
            pos = pos.relative(nextAxis, nextDirection);
            i++;

        }

        //axis2
        nextAxis = getNextAxis(nextAxis);
        nextDelta = (pos2.get(nextAxis) - pos1.get(nextAxis));
        nextDirection = nextDelta != 0 ? nextDelta / Math.abs(nextDelta) : 0;

        for (int j = 0; j < Math.abs(nextDelta); j++) {
            blockPosArray[i] = pos;
            pos = pos.relative(nextAxis, nextDirection);
            i++;

        }

        //axis3
        nextAxis = getNextAxis(nextAxis);
        nextDelta = (pos2.get(nextAxis) - pos1.get(nextAxis));
        nextDirection = nextDelta != 0 ? nextDelta / Math.abs(nextDelta) : 0;

        for (int j = 0; j < Math.abs(nextDelta) + 1; j++) {
            blockPosArray[i] = pos;
            pos = pos.relative(nextAxis, nextDirection);
            i++;

        }
        return blockPosArray;
    }

    public BlockPos[] getCurve() {
        BlockPos b1Pos = this.core.block1Pos;
        Direction.Axis b1Axis = this.core.block1Axis;
        int b1Direction = this.core.block1Direction;
        String b1ExtraData = this.core.block1ExtraData;
        BlockPos b2Pos = this.core.block2Pos;
        Direction.Axis b2Axis = this.core.block2Axis;
        int b2Direction = this.core.block2Direction;
        String b2ExtraData = this.core.block2ExtraData;
        if (b2ExtraData == "isFirst" && b1ExtraData != "isFirst") { //if player goes in from other side (this curveType is not symmetrical)
            BlockPos[] array = calcArray(b2Pos, b2Axis, b2Direction, b1Pos, b1Axis, b1Direction);// swap variables

            for (int j = 0; j < array.length / 2; j++) { // reverse array
                BlockPos t = array[j];
                array[j] = array[array.length - 1 - j];
                array[array.length - 1 - j] = t;
            }

            return array;

        }
        return calcArray(b1Pos, b1Axis, b1Direction, b2Pos, b2Axis, b2Direction);
    }
}
