package net.ugi.hypertubes.hypertube.UI;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.ugi.hypertubes.hypertube.Curves.CurveTypes;
import net.ugi.hypertubes.hypertube.HyperTubeUtil;
import net.ugi.hypertubes.item.custom.HyperTubePlacerItem;

public class HyperTubePlacerUI {

    public void makeUI(Player player, ItemStack stack, int tubeLength, int maxTubeLength, CurveTypes.Curves curvetype) {
        HyperTubePlacerItem hyperTubePlacerItem = (HyperTubePlacerItem) stack.getItem();
        int availableResourcesCount = HyperTubeUtil.getResourcesCount(player);

        ChatFormatting errorColor = ChatFormatting.RED;
        ChatFormatting validColor = ChatFormatting.DARK_AQUA;
        ChatFormatting curveTypeColor = ChatFormatting.GOLD;


        ChatFormatting lengthColor = player.isCreative() ?
                validColor :
                tubeLength > maxTubeLength ? errorColor : validColor;

        ChatFormatting resourceColor = player.isCreative() ?
                validColor :
                availableResourcesCount < tubeLength ? errorColor : validColor;

        Component text = Component.literal("Type: ").withStyle(curveTypeColor)
                .append(Component.translatable("hypertubes.curvetype." + curvetype.toString()).withStyle(curveTypeColor))
                .append(Component.literal("    " + "Length: " + tubeLength + " / " + (player.isCreative() ? "∞" : maxTubeLength)).withStyle(lengthColor))
                .append(Component.literal("    "  + "Resource: " + (player.isCreative() ? "∞" : availableResourcesCount) + " / " + tubeLength).withStyle(resourceColor));

        player.displayClientMessage(text, true);
    }
}
