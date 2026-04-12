package com.whatshouldipickup.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MoveItemPacket(int entityId, int slot) implements CustomPacketPayload {

    public static final Type<MoveItemPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("whatshouldipickup", "move_item"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, MoveItemPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> {
                        buf.writeInt(msg.entityId());
                        buf.writeInt(msg.slot());
                    },
                    buf -> new MoveItemPacket(buf.readInt(), buf.readInt())
            );

    public static void handle(MoveItemPacket msg, IPayloadContext ctx) {

        ctx.enqueueWork(() -> {

            ServerPlayer player = (ServerPlayer) ctx.player();

            var level = player.level();

            var entity = level.getEntity(msg.entityId());

            if (!(entity instanceof ItemEntity itemEntity)) return;

            ItemStack stackFromWorld = itemEntity.getItem();

            int slot = msg.slot();

            // 🔥 HOTBAR (0–8) + INVENTORY (9–35)
            if (slot < 0 || slot >= player.getInventory().items.size()) return;

            // 🔥 SWAP (KLUCZ FIX)
            ItemStack existing = player.getInventory().getItem(slot);

            // wstaw item z ziemi
            player.getInventory().setItem(slot, stackFromWorld.copy());

            // zwróć poprzedni item na ziemię (swap)
            if (!existing.isEmpty()) {
                itemEntity.setItem(existing);
            } else {
                itemEntity.discard();
            }
        });
    }
}