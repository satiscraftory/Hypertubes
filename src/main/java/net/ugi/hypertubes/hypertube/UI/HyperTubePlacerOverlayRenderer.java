package net.ugi.hypertubes.hypertube.UI;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.ugi.hypertubes.item.custom.HyperTubePlacerItem;

public class HyperTubePlacerOverlayRenderer {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Pre event) {

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) return;

        if (!(player.getMainHandItem().getItem() instanceof HyperTubePlacerItem)) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = mc.font;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int y = mc.getWindow().getGuiScaledHeight() - 70;

        // Use the shared values
        Component typeComponent = Component.literal("Type: ").append(Component.translatable(HyperTubePlacerOverlayData.hypertubeType));
        Component lengthComponent = Component.literal("Length: " + HyperTubePlacerOverlayData.tubeLength + " / " + HyperTubePlacerOverlayData.maxTubeLength);
        Component resourceComponent = Component.literal("Resources: " + HyperTubePlacerOverlayData.availableResources + " / " + HyperTubePlacerOverlayData.tubeLength);
        int typeWidth = font.width(typeComponent);
        int lengthWidth = font.width(lengthComponent);
        int resourceWidth = font.width(resourceComponent);

        int spacing = 15;

        int x = (screenWidth - (typeWidth + spacing + lengthWidth + spacing + resourceWidth)) / 2;

        guiGraphics.drawString(font, typeComponent, x, y, HyperTubePlacerOverlayData.colorTubeType, false);

        guiGraphics.drawString(font, lengthComponent, x + typeWidth + spacing, y, HyperTubePlacerOverlayData.colorTubeLength, false);

        guiGraphics.drawString(font, resourceComponent, x + typeWidth + spacing + lengthWidth + spacing, y, HyperTubePlacerOverlayData.colorResources, false);

        if (HyperTubePlacerOverlayData.showError) {
            guiGraphics.drawString(font, HyperTubePlacerOverlayData.errorMessage, (screenWidth - font.width(HyperTubePlacerOverlayData.errorMessage)) / 2, y + 10, HyperTubePlacerOverlayData.colorErrorMessage, false);
        }
    }
}
