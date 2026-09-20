package com.whatshouldipickup.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MoveItemPacket(
        int entityId,
        int slot
) implements CustomPacketPayload {

    public static final Type<MoveItemPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            "whatshouldipickup",
                            "move_item"
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            MoveItemPacket
            > STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> {
                        buf.writeInt(msg.entityId());
                        buf.writeInt(msg.slot());
                    },
                    buf -> new MoveItemPacket(
                            buf.readInt(),
                            buf.readInt()
                    )
            );

    public static void handle(
            MoveItemPacket msg,
            IPayloadContext ctx
    ) {

        ctx.enqueueWork(() -> {

            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }

            var level =
                    player.level();

            var entity =
                    level.getEntity(
                            msg.entityId()
                    );

            if (!(entity instanceof ItemEntity itemEntity)) {
                return;
            }

            if (player.distanceToSqr(itemEntity) > 25.0) {
                return;
            }

            int slot =
                    msg.slot();

            if (slot < 0 ||
                slot >= player.getInventory().items.size()) {

                return;
            }

            ItemStack worldStack =
                    itemEntity.getItem().copy();

            if (worldStack.isEmpty()) {
                return;
            }

            ItemStack existing =
                    player.getInventory().getItem(slot);

            player.getInventory().setItem(
                    slot,
                    worldStack
            );

            if (!existing.isEmpty()) {
                itemEntity.setItem(existing);
            } else {
                itemEntity.discard();
            }
        });
    }
}