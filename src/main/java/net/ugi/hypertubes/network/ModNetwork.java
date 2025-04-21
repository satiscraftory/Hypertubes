package net.ugi.hypertubes.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;


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
    }
}
