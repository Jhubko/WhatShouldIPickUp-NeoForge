package com.whatshouldipickup.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.GAME
)
public class ClientKeyHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();

        while (ClientKeyMappings.OPEN_CONFIG.consumeClick()) {

            if (mc.screen == null) {
                mc.setScreen(new WhatShouldIPickUpConfigScreen(null));
            }
        }
    }
}