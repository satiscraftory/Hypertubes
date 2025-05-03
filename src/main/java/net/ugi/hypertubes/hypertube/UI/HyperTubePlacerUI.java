package net.ugi.hypertubes.hypertube.UI;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.ugi.hypertubes.hypertube.Curves.CurveTypes;
import net.ugi.hypertubes.hypertube.HyperTubeUtil;
import net.ugi.hypertubes.item.custom.HyperTubePlacerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HyperTubePlacerUI {

    public void makeUI(Player player, int availableResourcesCount , int tubeLength, int maxTubeLength, CurveTypes.Curves curvetype, boolean isValidCurve) {

        int errorColor = 0xFF6363;
        int validColor = 0x28ACFF;
        HyperTubePlacerOverlayData.colorTubeType = 0xFFB630;

        HyperTubePlacerOverlayData.colorErrorMessage = errorColor;


        HyperTubePlacerOverlayData.colorTubeLength = player.isCreative() ?
                validColor :
                tubeLength > maxTubeLength ? errorColor : validColor;

        HyperTubePlacerOverlayData.colorResources = player.isCreative() ?
                validColor :
                availableResourcesCount < tubeLength ? errorColor : validColor;

        HyperTubePlacerOverlayData.hypertubeType = "hypertubes.curvetype." + curvetype.toString();


        if(tubeLength == -1 && maxTubeLength == -1 && availableResourcesCount == -1){
            HyperTubePlacerOverlayData.showOnlyType = true;
            HyperTubePlacerOverlayData.errorMessage = "";
            return;
        }

        HyperTubePlacerOverlayData.showOnlyType = false;

        HyperTubePlacerOverlayData.tubeLength = String.valueOf(tubeLength);
        HyperTubePlacerOverlayData.maxTubeLength = player.isCreative() ? "∞" : String.valueOf(maxTubeLength);
        HyperTubePlacerOverlayData.availableResources = player.isCreative() ? "∞" : String.valueOf(availableResourcesCount);

        List<String> errors = new ArrayList<>();

        if (!isValidCurve) errors.add("Invalid placement");
        if (tubeLength > maxTubeLength && !player.isCreative()) errors.add("Too long");
        if (availableResourcesCount < tubeLength && !player.isCreative()) errors.add("Not enough Resources");

        int numberOfErrors = errors.size();
        AtomicInteger i = new AtomicInteger();
        HyperTubePlacerOverlayData.errorMessage = "";
        errors.forEach(error -> {
            HyperTubePlacerOverlayData.errorMessage = HyperTubePlacerOverlayData.errorMessage + error;
            if( i.get() < numberOfErrors - 1) HyperTubePlacerOverlayData.errorMessage = HyperTubePlacerOverlayData.errorMessage + "  &  ";
            i.getAndIncrement();
        });



    }
}
