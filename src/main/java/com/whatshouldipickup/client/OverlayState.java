package com.whatshouldipickup.client;

import com.whatshouldipickup.network.MoveItemPacket;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class OverlayState {

    private static ItemStack draggedStack = ItemStack.EMPTY;
    private static int draggedEntityId = -1;

    private static int scrollTarget = 0;
    private static float scrollCurrent = 0f;

    private static boolean draggingScroll = false;

    private static final int VISIBLE = 6;

    public static void updateScroll(int size) {

        int max = Math.max(0, size - VISIBLE);

        if (max == 0) {
            scrollTarget = 0;
            scrollCurrent = 0f;
            return;
        }

        scrollTarget = Math.max(0, Math.min(scrollTarget, max));

        scrollCurrent += (scrollTarget - scrollCurrent) * 0.25f;

        if (Math.abs(scrollCurrent - scrollTarget) < 0.01f) {
            scrollCurrent = scrollTarget;
        }

        scrollCurrent = Math.max(0f, Math.min(scrollCurrent, max));
    }

    public static void handleScroll(double delta, int size) {

        int max = Math.max(0, size - VISIBLE);

        if (max == 0) {
            scrollTarget = 0;
            return;
        }

        if (delta > 0) scrollTarget--;
        else scrollTarget++;

        scrollTarget = Math.max(0, Math.min(scrollTarget, max));
    }

    public static void handleClick(InventoryScreen screen, int mouseX, int mouseY, List<ItemEntity> items) {

        int startX = screen.getGuiLeft() + 185;
        int startY = screen.getGuiTop() + 38;
        int barX = startX + 130;
        int barHeight = 100;

        if (mouseX >= barX && mouseX <= barX + 6 &&
            mouseY >= startY && mouseY <= startY + barHeight) {

            draggingScroll = true;
            updateScrollFromMouse(mouseY, startY, barHeight, items.size());
            return;
        }

        for (int i = 0; i < VISIBLE; i++) {

            int index = i + scrollTarget;
            if (index >= items.size()) break;

            int rowY = startY + 4 + i * 18;

            if (inside(mouseX, mouseY, startX, rowY, 140, 16)) {

                ItemEntity entity = items.get(index);

                draggedEntityId = entity.getId();
                draggedStack = entity.getItem().copy();
                return;
            }
        }
    }

    public static void handleDrag(InventoryScreen screen, int mouseY, int size) {
        if (!draggingScroll) return;

        int startY = screen.getGuiTop() + 38;
        int barHeight = 100;

        updateScrollFromMouse(mouseY, startY, barHeight, size);
    }

    public static MoveItemPacket handleRelease(InventoryScreen screen, int mouseX, int mouseY) {

        draggingScroll = false;

        if (draggedStack.isEmpty() || draggedEntityId == -1) {
            clear();
            return null;
        }

        int x = screen.getGuiLeft();
        int y = screen.getGuiTop();

        for (int i = 0; i < 9; i++) {

            int slotX = x + 8 + i * 18;
            int slotY = y + 142;

            if (inside(mouseX, mouseY, slotX, slotY, 16, 16)) {
                return finish(i);
            }
        }

        int index = 9;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {

                int slotX = x + 8 + col * 18;
                int slotY = y + 84 + row * 18;

                if (inside(mouseX, mouseY, slotX, slotY, 16, 16)) {
                    return finish(index);
                }

                index++;
            }
        }

        clear();
        return null;
    }

    private static MoveItemPacket finish(int slot) {
        MoveItemPacket packet = new MoveItemPacket(draggedEntityId, slot);
        clear();
        return packet;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static void updateScrollFromMouse(int mouseY, int barY, int barHeight, int size) {

        int max = Math.max(0, size - VISIBLE);

        if (max == 0) {
            scrollTarget = 0;
            return;
        }

        float percent = (float)(mouseY - barY) / barHeight;
        percent = Math.max(0f, Math.min(1f, percent));

        scrollTarget = (int)(percent * max);
    }

    private static void clear() {
        draggedStack = ItemStack.EMPTY;
        draggedEntityId = -1;
    }

    public static ItemStack getDraggedStack() {
        return draggedStack;
    }

    public static float getScrollCurrent() {
        return scrollCurrent;
    }

    public static int getScrollIndex() {
        return scrollTarget;
    }

    public static boolean isDraggingScroll() {
    return draggingScroll;
    }

    public static boolean isInteracting() {
        return draggingScroll || !draggedStack.isEmpty();
    }
    
}