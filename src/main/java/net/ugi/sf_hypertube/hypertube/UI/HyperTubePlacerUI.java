package net.ugi.sf_hypertube.hypertube.UI;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.ugi.sf_hypertube.hypertube.Curves.CurveTypes;
import net.ugi.sf_hypertube.item.custom.HyperTubeItem;

public class HyperTubePlacerUI {

    public void makeUI(Player player, ItemStack stack, int tubeLength, int maxTubeLength, CurveTypes.Curves curvetype) {
        HyperTubeItem hyperTubeItem = (HyperTubeItem) stack.getItem();
        int availableResourcesCount = hyperTubeItem.getResourcesCount(player);

        ChatFormatting errorColor = ChatFormatting.RED;
        ChatFormatting validColor = ChatFormatting.DARK_AQUA;
        ChatFormatting curveTypeColor = ChatFormatting.GOLD;


        ChatFormatting lengthColor = tubeLength > maxTubeLength ? errorColor : validColor;
        ChatFormatting resourceColor = availableResourcesCount < tubeLength ? errorColor : validColor;

        Component text = Component.literal("Type: ").withStyle(curveTypeColor)
                .append(Component.translatable("sf_hypertube.curvetype." + curvetype.getLabel()).withStyle(curveTypeColor))
                .append(Component.literal("    " + "Length: " + tubeLength + " / " + (player.isCreative() ? "∞" : maxTubeLength)).withStyle(lengthColor))
                .append(Component.literal("    "  + "Resource: " + (player.isCreative() ? "∞" : availableResourcesCount) + " / " + tubeLength).withStyle(resourceColor));

        player.displayClientMessage(text, true);
    }
}
