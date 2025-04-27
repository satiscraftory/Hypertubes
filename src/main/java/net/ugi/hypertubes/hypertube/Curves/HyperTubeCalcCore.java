package net.ugi.hypertubes.hypertube.Curves;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.ugi.hypertubes.block.entity.HypertubeSupportBlockEntity;

import java.util.LinkedHashSet;
import java.util.Set;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;

public class HyperTubeCalcCore {
    public BlockPos block1Pos;
    public Direction.Axis block1Axis;
    public int block1Direction;
    public String block1ExtraData;
    public BlockPos block2Pos;
    public Direction.Axis block2Axis;
    public int block2Direction;
    public String block2ExtraData;

    public int block1UsedDirection = 0;
    public int block2UsedDirection = 0;


    public HyperTubeCalcCore() {
    }

    private int getDirectionIfUnspecified(BlockPos pos, Direction.Axis axis, BlockPos pos2) {
        int dir = 0;
        if (axis == Direction.Axis.X) {
            dir = (pos2.getX() - pos.getX());
        }
        if (axis == Direction.Axis.Y) {
            dir= (pos2.getY() - pos.getY());
        }
        if (axis == Direction.Axis.Z) {
            dir = (pos2.getZ() - pos.getZ());
        }
        if (dir == 0) dir = 1;

        dir = dir / Math.abs(dir);
        return dir;

    }

    public void setData(BlockPos b1Pos, Direction.Axis b1Axis, int b1Direction, String b1ExtraData, BlockPos b2Pos, Direction.Axis b2Axis, int b2Direction, String b2ExtraData){
        if (b1Direction == 0){ // 0 = unspecified direction, code takes shortest path
            b1Direction = getDirectionIfUnspecified(b1Pos, b1Axis, b2Pos);
        }
        if (b2Direction == 0){ // 0 = unspecified direction, code takes shortest path
            b2Direction = getDirectionIfUnspecified(b2Pos, b2Axis, b1Pos);
        }

        this.setUsedDirections(b1Direction, b2Direction);

        this.block1Pos = b1Pos.relative(b1Axis,b1Direction);
        this.block1Axis = b1Axis;
        this.block1Direction = b1Direction;
        this.block1ExtraData = b1ExtraData;
        this.block2Pos = b2Pos.relative(b2Axis,b2Direction);
        this.block2Axis = b2Axis;
        this.block2Direction = b2Direction;
        this.block2ExtraData = b2ExtraData;
    }

    public void setDataFromPosAndAxis(Level level, BlockPos supportPos1, Direction.Axis axis1, BlockPos supportPos2, Direction.Axis axis2) {
        HypertubeSupportBlockEntity hypertubeSupportBlockEntity1 = (HypertubeSupportBlockEntity) level.getBlockEntity(supportPos1);
        HypertubeSupportBlockEntity hypertubeSupportBlockEntity2 = (HypertubeSupportBlockEntity) level.getBlockEntity(supportPos2);

        if(hypertubeSupportBlockEntity1==null || hypertubeSupportBlockEntity2==null) return;

        int direction1 = hypertubeSupportBlockEntity1.getDirection(supportPos2);
        int direction2 = hypertubeSupportBlockEntity2.getDirection(supportPos1);
        String extraData1 =hypertubeSupportBlockEntity1.getExtraInfo(direction1);
        String extraData2 =hypertubeSupportBlockEntity2.getExtraInfo(direction2);
        setData(supportPos1, axis1, direction1, extraData1, supportPos2, axis2, direction2, extraData2);
    }

    public void setDataFromPos(Level level, BlockPos supportPos1, BlockPos supportPos2) {
        Direction.Axis axis1 = level.getBlockState(supportPos1).getValue(AXIS);
        Direction.Axis axis2 = level.getBlockState(supportPos2).getValue(AXIS);
        setDataFromPosAndAxis(level, supportPos1, axis1, supportPos2, axis2);
    }




    public void setUsedDirections(int dir1, int dir2){
        this.block1UsedDirection = dir1;
        this.block2UsedDirection = dir2;
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

    public BlockPos[] getHyperTubeArray(CurveTypes.Curves curvetype){

        BezierCurve bezierCurve = new BezierCurve(this);
        MinecraftCurve minecraftCurve = new MinecraftCurve(this);

        if(curvetype==null) return null;
        BlockPos[] blockArray = new BlockPos[0];
        switch(curvetype) {
            case CURVED -> {
                blockArray = bezierCurve.getCurve(0.5);
            }
            case OVERKILL -> {
                blockArray = bezierCurve.getCurve(3);
            }
            case STRAIGHT -> {
                blockArray = bezierCurve.getCurve(0);
            }
            case MINECRAFT -> {
                blockArray = minecraftCurve.getCurve();
            }
//            case HELIX -> {
//
//            }

        }
        return checkDuplicate(blockArray);
    }
}
