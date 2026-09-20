package com.whatshouldipickup.event;

import com.whatshouldipickup.network.AutoPickupState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.common.util.TriState;

@EventBusSubscriber(
        modid = "whatshouldipickup"
)
public class ItemPickupHandler {

    @SubscribeEvent
    public static void onItemPickup(
            ItemEntityPickupEvent.Pre event
    ) {

        if (!(event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }

        if (AutoPickupState.isDisabled(player)) {
            event.setCanPickup(TriState.FALSE);
        }
    }
}