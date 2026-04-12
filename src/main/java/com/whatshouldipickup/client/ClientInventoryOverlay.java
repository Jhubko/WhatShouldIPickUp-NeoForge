package com.whatshouldipickup.client;

import com.whatshouldipickup.network.MoveItemPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

    private static ItemStack draggedStack = ItemStack.EMPTY;
    private static ItemEntity draggedEntity = null;

    private static int scrollTarget = 0;
    private static float scrollCurrent = 0;
    private static boolean draggingScroll = false;

    private static final int VISIBLE = 6;

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {

        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        cachedItems.clear();
        cachedItems.addAll(
                player.level().getEntitiesOfClass(
                        ItemEntity.class,
                        player.getBoundingBox().inflate(2)
                )
        );

        if (cachedItems.isEmpty()) return;

        scrollCurrent += (scrollTarget - scrollCurrent) * 0.25f;

        GuiGraphics g = event.getGuiGraphics();

        int x = screen.getGuiLeft();
        int y = screen.getGuiTop();

        int startX = x + 185;
        int startY = y + 38;

        int panelHeight = 120;

        g.fill(startX - 5, startY - 15, startX + 150, startY + panelHeight, 0xAA000000);
        g.drawString(mc.font, "Nearby Items", startX, startY - 10, 0xFFFFFF);

        for (int i = 0; i < VISIBLE; i++) {

            int index = i + (int) scrollCurrent;

            if (index >= cachedItems.size()) break;

            ItemStack stack = cachedItems.get(index).getItem();

            int rowY = startY + 4 + i * 18;

            g.renderItem(stack, startX, rowY);
            g.drawString(mc.font,
                    stack.getHoverName().getString(),
                    startX + 20,
                    rowY + 4,
                    0xFFFFFF
            );
        }

        int barX = startX + 130;
        int barY = startY;
        int barHeight = panelHeight - 20;

        g.fill(barX, barY, barX + 6, barY + barHeight, 0x66000000);

        int max = Math.max(1, cachedItems.size() - VISIBLE);

        float progress = scrollCurrent / max;

        int thumbHeight = Math.max(12, barHeight / (max + VISIBLE));
        int thumbY = barY + (int) ((barHeight - thumbHeight) * progress);

        g.fill(barX, thumbY, barX + 6, thumbY + thumbHeight, 0xFFFFFFFF);

        if (!draggedStack.isEmpty()) {

            Minecraft mcClient = Minecraft.getInstance();

            int mouseX = (int) (mcClient.mouseHandler.xpos() * mcClient.getWindow().getGuiScaledWidth() / mcClient.getWindow().getScreenWidth());
            int mouseY = (int) (mcClient.mouseHandler.ypos() * mcClient.getWindow().getGuiScaledHeight() / mcClient.getWindow().getScreenHeight());

            g.pose().pushPose();
            g.pose().translate(0, 0, 300);

            g.renderItem(draggedStack, mouseX - 8, mouseY - 8);

            g.pose().popPose();
        }
    }

    @SubscribeEvent
    public static void onScroll(ScreenEvent.MouseScrolled.Pre event) {

        if (!(event.getScreen() instanceof InventoryScreen)) return;

        int max = Math.max(0, cachedItems.size() - VISIBLE);

        if (event.getScrollDeltaY() > 0) {
            scrollTarget = Math.max(0, scrollTarget - 1);
        } else {
            scrollTarget = Math.min(max, scrollTarget + 1);
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {

        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (!draggedStack.isEmpty()) return;

        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();

        int x = screen.getGuiLeft();
        int y = screen.getGuiTop();

        int startX = x + 185;
        int startY = y + 38;

        int panelHeight = 120;

        int barX = startX + 130;
        int barY = startY;
        int barHeight = panelHeight - 20;

        // 🔥 SCROLLBAR CLICK FIX
        if (mouseX >= barX && mouseX <= barX + 6 &&
            mouseY >= barY && mouseY <= barY + barHeight) {

            draggingScroll = true;

            updateScrollFromMouse(
                    mouseY,
                    barY,
                    barHeight,
                    cachedItems.size()
            );

            event.setCanceled(true);
            return;
        }

        // ITEM CLICK
        for (int i = 0; i < VISIBLE; i++) {

            int index = i + (int) scrollCurrent;

            if (index >= cachedItems.size()) break;

            int rowY = startY + 4 + i * 18;

            if (mouseX >= startX && mouseX <= startX + 140 &&
                mouseY >= rowY && mouseY <= rowY + 16) {

                draggedEntity = cachedItems.get(index);
                draggedStack = draggedEntity.getItem().copy();

                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseDrag(ScreenEvent.MouseDragged.Pre event) {

        if (!draggingScroll) return;
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        int mouseY = (int) event.getMouseY();

        int startY = screen.getGuiTop() + 38;
        int panelHeight = 120;
        int barHeight = panelHeight - 20;

        updateScrollFromMouse(mouseY, startY, barHeight, cachedItems.size());

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {

        draggingScroll = false;

        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        if (draggedStack.isEmpty() || draggedEntity == null) {
            clearDrag();
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();

        int x = screen.getGuiLeft();
        int y = screen.getGuiTop();

        // HOTBAR
        for (int i = 0; i < 9; i++) {

            int slotX = x + 8 + i * 18;
            int slotY = y + 142;

            if (mouseX >= slotX && mouseX <= slotX + 16 &&
                mouseY >= slotY && mouseY <= slotY + 16) {

                PacketDistributor.sendToServer(
                        new MoveItemPacket(draggedEntity.getId(), i)
                );

                clearDrag();
                event.setCanceled(true);
                return;
            }
        }

        // INVENTORY
        int index = 9;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {

                int slotX = x + 8 + col * 18;
                int slotY = y + 84 + row * 18;

                if (mouseX >= slotX && mouseX <= slotX + 16 &&
                    mouseY >= slotY && mouseY <= slotY + 16) {

                    PacketDistributor.sendToServer(
                            new MoveItemPacket(draggedEntity.getId(), index)
                    );

                    clearDrag();
                    event.setCanceled(true);
                    return;
                }

                index++;
            }
        }

        clearDrag();
    }

    private static void updateScrollFromMouse(int mouseY, int barY, int barHeight, int itemCount) {

        int max = Math.max(0, itemCount - VISIBLE);

        if (max <= 0) {
            scrollTarget = 0;
            return;
        }

        float percent = (float)(mouseY - barY) / (float)barHeight;
        percent = Math.max(0f, Math.min(1f, percent));

        scrollTarget = (int)(percent * max);
    }

    private static void clearDrag() {
        draggedStack = ItemStack.EMPTY;
        draggedEntity = null;
    }
}