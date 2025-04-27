package net.ugi.hypertubes.hypertube.Functionalities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.ugi.hypertubes.block.ModBlocks;
import net.ugi.hypertubes.block.entity.HypertubeSupportBlockEntity;

public class HyperTubeDetector {

    public static void resetDetector(HypertubeSupportBlockEntity hypertubeSupportBlockEntity, Level level, BlockPos pos, int ticks){
        if (hypertubeSupportBlockEntity.redstonePowerOutput == 0) return;

        if (hypertubeSupportBlockEntity.redstonePowerTimer == ticks){
            hypertubeSupportBlockEntity.redstonePowerTimer = 0;
            hypertubeSupportBlockEntity.redstonePowerOutput = 0;
            level.blockUpdated(pos, ModBlocks.HYPERTUBE_SUPPORT.get());
        }
        else {
            hypertubeSupportBlockEntity.redstonePowerTimer = hypertubeSupportBlockEntity.redstonePowerTimer + 1;
        }
    }
}
