package com.whatshouldipickup.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(
        modid = "whatshouldipickup",
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public class ClientKeyMappings {

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.whatshouldipickup.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.whatshouldipickup"
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
    }
}