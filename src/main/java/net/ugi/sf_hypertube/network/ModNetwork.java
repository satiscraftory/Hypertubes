package net.ugi.sf_hypertube.network;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.ugi.sf_hypertube.SfHyperTube;

@EventBusSubscriber(modid = "sf_hypertube", bus = EventBusSubscriber.Bus.MOD)
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