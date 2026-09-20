package com.whatshouldipickup.client;

import com.whatshouldipickup.config.ClientConfig;
import com.whatshouldipickup.network.MoveItemPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientInventoryOverlay {

    private static final List<ItemEntity> cachedItems = new ArrayList<>();

    private static final int MAIN_INV_SIZE = 36;

    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post event) {

        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) return;

        Level level = player.level();

        if (!ClientConfig.isAlwaysShow() && !isInventoryFull(player)) {
            cachedItems.clear();
            return;
        }

        double radius = ClientConfig.getDetectionRadius();

        List<ItemEntity> freshItems = level.getEntitiesOfClass(
                ItemEntity.class,
                player.getBoundingBox().inflate(radius)
        );

        cachedItems.clear();
        cachedItems.addAll(freshItems);

        if (cachedItems.isEmpty()) return;

        OverlayState.updateScroll(cachedItems.size());

        OverlayRenderer.render(
                event.getGuiGraphics(),
                screen,
                cachedItems
        );
    }

    @SubscribeEvent
    public static void onScroll(ScreenEvent.MouseScrolled.Pre event) {

        if (!(event.getScreen() instanceof InventoryScreen)) return;

        if (cachedItems.isEmpty()) return;

        OverlayState.handleScroll(
                event.getScrollDeltaY(),
                cachedItems.size()
        );

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClick(ScreenEvent.MouseButtonPressed.Pre event) {

        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        if (cachedItems.isEmpty()) return;

        OverlayState.handleClick(
                screen,
                (int) event.getMouseX(),
                (int) event.getMouseY(),
                cachedItems
        );

        if (OverlayState.isInteracting()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDrag(ScreenEvent.MouseDragged.Pre event) {

        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        if (cachedItems.isEmpty()) return;

        OverlayState.handleDrag(
                screen,
                (int) event.getMouseX(),
                cachedItems.size()
        );

        if (OverlayState.isDraggingScroll()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRelease(ScreenEvent.MouseButtonReleased.Pre event) {

        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        MoveItemPacket packet = OverlayState.handleRelease(
                screen,
                (int) event.getMouseX(),
                (int) event.getMouseY()
        );

        if (packet != null) {
            PacketDistributor.sendToServer(packet);
            event.setCanceled(true);
        }
    }

    private static boolean isInventoryFull(Player player) {

        var items = player.getInventory().items;

        for (int i = 0; i < MAIN_INV_SIZE; i++) {
            if (items.get(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}