package com.whatshouldipickup.client;

import com.whatshouldipickup.config.ClientConfig;
import com.whatshouldipickup.network.SetAutoPickupPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
        modid = "whatshouldipickup",
        value = Dist.CLIENT
)
public class AutoPickupClientHandler {

    @SubscribeEvent
    public static void onLoggingIn(
            ClientPlayerNetworkEvent.LoggingIn event
    ) {

        PacketDistributor.sendToServer(
                new SetAutoPickupPacket(
                        ClientConfig.isAutoPickupDisabled()
                )
        );
    }

    public static void sync() {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        PacketDistributor.sendToServer(
                new SetAutoPickupPacket(
                        ClientConfig.isAutoPickupDisabled()
                )
        );
    }
}