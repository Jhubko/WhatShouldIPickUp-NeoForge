package com.whatshouldipickup.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(
        modid = "whatshouldipickup",
        bus = EventBusSubscriber.Bus.MOD
)
public class ModNetwork {

    @SubscribeEvent
    public static void register(
            RegisterPayloadHandlersEvent event
    ) {

        var registrar =
                event.registrar("1");

        registrar.playToServer(
                MoveItemPacket.TYPE,
                MoveItemPacket.STREAM_CODEC,
                MoveItemPacket::handle
        );

        registrar.playToServer(
                SetAutoPickupPacket.TYPE,
                SetAutoPickupPacket.STREAM_CODEC,
                SetAutoPickupPacket::handle
        );
    }
}