package net.ugi.sf_hypertube.network;

import com.mojang.datafixers.util.Function4;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.ugi.sf_hypertube.SfHyperTube;

public record UncappedMotionPayload(int entityId, double vx, double vy, double vz)
        implements CustomPacketPayload
{
    public static final Type<UncappedMotionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SfHyperTube.MOD_ID, "uncapped_motion"));

    public static final StreamCodec<FriendlyByteBuf, UncappedMotionPayload> STREAM_CODEC =
                        StreamCodec.composite(
                                        // codec for entityId
                                               ByteBufCodecs.VAR_INT,   UncappedMotionPayload::entityId,
                                        // codec for vx
                                                ByteBufCodecs.DOUBLE,    UncappedMotionPayload::vx,
                                        // codec for vy
                                                ByteBufCodecs.DOUBLE,    UncappedMotionPayload::vy,
                                        // codec for vz
                                               ByteBufCodecs.DOUBLE,    UncappedMotionPayload::vz,
                                        // constructor
                                                UncappedMotionPayload::new
                                       );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
