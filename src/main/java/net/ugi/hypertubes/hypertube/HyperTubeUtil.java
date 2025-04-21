package net.ugi.hypertubes.hypertube;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.ugi.hypertubes.block.ModBlocks;

public class HyperTubeUtil {
    public HyperTubeUtil() {}

    public static int intValue(boolean val){
        return val ? 1 : 0;
    }

    public static boolean checkValidCurve(Level level, BlockPos[] blockPosArray, Player player, int maxTubeLength){
        boolean isValidCurve = true;
        for (int i = 0; i < blockPosArray.length; i++) {
            if(!(level.getBlockState(blockPosArray[i]).isAir() || level.getBlockState(blockPosArray[i]).is(ModBlocks.HYPERTUBE_SUPPORT))){
                isValidCurve = false;
            }
        }
        if(player.isCreative()) return isValidCurve;
        if (blockPosArray.length-2 > getResourcesCount(player)){
            isValidCurve = false;
        }
        if (blockPosArray.length-2 > maxTubeLength) isValidCurve = false;
        return isValidCurve;

    }

    public static int getResourcesCount(Player player){
        int resource1 = player.getInventory().countItem(Items.GLASS_PANE);
        int resource2 = player.getInventory().countItem(Items.GOLD_NUGGET);
        return Math.min(resource1, resource2);

    }

}
