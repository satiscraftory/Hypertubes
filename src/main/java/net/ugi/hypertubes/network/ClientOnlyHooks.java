package net.ugi.hypertubes.network;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.ugi.hypertubes.hypertube.Curves.CurveTypes;
import net.ugi.hypertubes.hypertube.UI.HyperTubePlacerUI;

@OnlyIn(Dist.CLIENT)
public class ClientOnlyHooks {
    public static void hyperTubeOverlayClientOnly(HyperTubeOverlayPacket payload) {
        Minecraft.getInstance().execute(() -> {
            var mc = Minecraft.getInstance();
            if (mc.player == null) return;

            HyperTubePlacerUI UI = new HyperTubePlacerUI();
            UI.makeUI(
                    mc.player,
                    payload.availableResources(),
                    payload.tubeLength(),
                    payload.maxTubeLength(),
                    CurveTypes.Curves.get(payload.curveType()),
                    payload.isValid()
            );
        });
    }
}