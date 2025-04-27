package net.ugi.hypertubes.hypertube.Functionalities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.ugi.hypertubes.block.entity.HypertubeSupportBlockEntity;

public class HyperTubeEntrance {

    public static int getEntranceDirection(HypertubeSupportBlockEntity hypertubeSupportBlockEntity) {
        if (hypertubeSupportBlockEntity.targetNegative != null && hypertubeSupportBlockEntity.targetPositive == null) {
            return 1;
        }
        if (hypertubeSupportBlockEntity.targetNegative == null && hypertubeSupportBlockEntity.targetPositive != null) {
            return -1;
        }
        return 0;
    }


    public static AABB getEntranceZone(Direction.Axis axis, int direction, BlockPos entrancePos) {
        BlockPos posmax;
        BlockPos posmin;
        if(axis == Direction.Axis.Y){
            posmax = entrancePos.relative(axis,direction);
            posmin = posmax;
        }else{
            posmax = entrancePos.relative(axis,direction);
            posmin = entrancePos.relative(axis,direction).below();
        }
        return new AABB(posmax.getCenter().add(0.5,0.5,0.5), posmin.getCenter().add(-0.5,-0.5,-0.5)); // .Add is to take the full blocks, not the center only
    }


}
