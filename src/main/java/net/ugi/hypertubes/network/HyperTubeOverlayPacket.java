package net.ugi.hypertubes.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

public record HyperTubeOverlayPacket(String curveType, int tubeLength, int maxTubeLength, int availableResources,boolean isValid)
        implements CustomPacketPayload {

    public static final Type<HyperTubeOverlayPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(net.ugi.hypertubes.HyperTubes.MOD_ID, "hypertube_overlay"));



    public static final StreamCodec<FriendlyByteBuf, HyperTubeOverlayPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,   HyperTubeOverlayPacket::curveType,
                    ByteBufCodecs.VAR_INT,    HyperTubeOverlayPacket::tubeLength,
                    ByteBufCodecs.VAR_INT,    HyperTubeOverlayPacket::maxTubeLength,
                    ByteBufCodecs.VAR_INT,    HyperTubeOverlayPacket::availableResources,
                    ByteBufCodecs.BOOL, HyperTubeOverlayPacket::isValid,
                    // constructor
                    HyperTubeOverlayPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}