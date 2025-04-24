package net.ugi.hypertubes.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.ugi.hypertubes.hypertube.Curves.CurveTypes;
import net.ugi.hypertubes.hypertube.UI.HyperTubePlacerUI;

import java.lang.reflect.Type;


/**
 * Registers all payloads on the client side only.
 */
@EventBusSubscriber(modid = "hypertubes", bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {
    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("main");
        registrar.playToClient(
                UncappedMotionPayload.TYPE,
                UncappedMotionPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    // runs on the client when the packet arrives
                    net.minecraft.client.Minecraft.getInstance().execute(() -> {
                        var level = net.minecraft.client.Minecraft.getInstance().level;
                        if (level != null) {
                            var e = level.getEntity(payload.entityId());
                            if (e != null) {
                                e.setDeltaMovement(payload.vx(), payload.vy(), payload.vz());
                                e.hasImpulse = true; // force client‑side physics
                            }
                        }
                    });
                }
        );

        registrar.playToClient(
                HyperTubeOverlayPacket.TYPE,
                HyperTubeOverlayPacket.STREAM_CODEC,
                (payload, ctx) -> {
                    if (FMLEnvironment.dist.isClient()) {
                        ClientOnlyHooks.hyperTubeOverlayClientOnly(payload);
                    }
                }
        );
    }
}
