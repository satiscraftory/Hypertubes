package net.ugi.hypertubes.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = "hypertubes", bus = EventBusSubscriber.Bus.MOD ,value =  Dist.CLIENT)
public class ModNetwork {
    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        // "main" is the channel version.  Any non‐null string works.
        PayloadRegistrar registrar = event.registrar("main");
        // register for play phase, server → client
        registrar.playToClient(
                UncappedMotionPayload.TYPE,
                UncappedMotionPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    // this runs on the client when the packet arrives
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    mc.execute(() -> {
                        var level = mc.level;
                        if (level != null) {
                            var e = level.getEntity(payload.entityId());
                            if (e != null) {
                                // apply the motion on the client side
                                e.setDeltaMovement(payload.vx(), payload.vy(), payload.vz());
                                e.hasImpulse = true; // force client‑side physics
                            }
                        }
                    });
                }
        );
    }
}