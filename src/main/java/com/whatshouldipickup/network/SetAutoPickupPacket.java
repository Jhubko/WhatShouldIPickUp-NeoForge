package com.whatshouldipickup.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetAutoPickupPacket(
        boolean disabled
) implements CustomPacketPayload {

    public static final Type<SetAutoPickupPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            "whatshouldipickup",
                            "set_auto_pickup"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            SetAutoPickupPacket
            > STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> buf.writeBoolean(msg.disabled()),
                    buf -> new SetAutoPickupPacket(buf.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            SetAutoPickupPacket msg,
            IPayloadContext ctx
    ) {
        ctx.enqueueWork(() -> {

            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }

            AutoPickupState.setDisabled(
                    player,
                    msg.disabled()
            );
        });
    }
}