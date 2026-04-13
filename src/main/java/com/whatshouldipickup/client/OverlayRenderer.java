package com.whatshouldipickup.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class OverlayRenderer {

    private static final int SLOT_SIZE = 18;

    public static class Layout {
        public int startX, startY;
        public int listX, listY;
        public int listW, listH;
        public int scrollbarX, scrollbarY, scrollbarW, scrollbarH;
    }

    public static Layout getLayout(Minecraft mc, InventoryScreen screen) {

        int x = screen.getGuiLeft();
        int y = screen.getGuiTop();

        Layout l = new Layout();

        l.startX = x;
        l.startY = y + 166 + 4;

        l.listX = l.startX;
        l.listY = l.startY + 12;

        l.listW = 176;
        l.listH = 20;

        l.scrollbarX = l.startX;
        l.scrollbarY = l.startY + 32;

        l.scrollbarW = 176;
        l.scrollbarH = 6;

        return l;
    }

    // 🔥 JEDNO ŹRÓDŁO PRAWDY
    public static int getVisibleCount(Layout l) {
        return l.listW / SLOT_SIZE;
    }

    public static int getOffsetX(Layout l) {
        int visible = getVisibleCount(l);
        int totalWidth = visible * SLOT_SIZE;
        return (l.listW - totalWidth) / 2;
    }

    public static int getItemX(Layout l, int index) {
        return l.listX + getOffsetX(l) + index * SLOT_SIZE;
    }

    public static void render(GuiGraphics g, InventoryScreen screen, List<ItemEntity> items) {

        Minecraft mc = Minecraft.getInstance();
        Layout l = getLayout(mc, screen);

        g.fill(l.startX, l.startY,
                l.startX + l.listW,
                l.startY + 40,
                0xAA000000);

        g.drawString(mc.font, "Nearby Items", l.startX + 2, l.startY + 2, 0xFFFFFF);

        int scroll = OverlayState.getScrollIndex();
        int hoveredIndex = -1;

        int visible = getVisibleCount(l);

        for (int i = 0; i < visible; i++) {

            int index = i + scroll;
            if (index >= items.size()) break;

            ItemStack stack = items.get(index).getItem();

            int x = getItemX(l, i);
            int y = l.listY;

            g.renderItem(stack, x, y);

            if (stack.getCount() > 1) {

                String count = String.valueOf(stack.getCount());

                g.pose().pushPose();
                g.pose().translate(0, 0, 300);

                int textX = x + 16 - mc.font.width(count);
                int textY = y + 8;

                g.drawString(mc.font, count, textX, textY, 0xFFFFFF);

                g.pose().popPose();
            }

            if (isMouseOver(x, y, 16, 16, mc)) {
                hoveredIndex = index;
            }
        }

        renderScrollbar(g, l, items.size(), visible);
        renderDraggedItem(g);

        int mouseX = getMouseX(mc);
        int mouseY = getMouseY(mc);

        if (hoveredIndex != -1 && hoveredIndex < items.size()) {

            ItemStack hoveredStack = items.get(hoveredIndex).getItem();

            g.pose().pushPose();
            g.pose().translate(0, 0, 500);
            g.renderTooltip(mc.font, hoveredStack, mouseX, mouseY);
            g.pose().popPose();
        }
    }

    private static void renderScrollbar(GuiGraphics g, Layout l, int size, int visible) {

        g.fill(l.scrollbarX, l.scrollbarY,
                l.scrollbarX + l.scrollbarW,
                l.scrollbarY + l.scrollbarH,
                0x66000000);

        if (size <= visible) {
            g.fill(l.scrollbarX, l.scrollbarY,
                    l.scrollbarX + l.scrollbarW,
                    l.scrollbarY + l.scrollbarH,
                    0xFFFFFFFF);
            return;
        }

        int max = size - visible;

        int thumbW = (int)((float) visible / size * l.scrollbarW);
        thumbW = Math.max(10, thumbW);

        float progress = OverlayState.getScrollCurrent() / max;

        int thumbX = l.scrollbarX + (int)((l.scrollbarW - thumbW) * progress);

        g.fill(thumbX, l.scrollbarY,
                thumbX + thumbW,
                l.scrollbarY + l.scrollbarH,
                0xFFFFFFFF);
    }

    private static boolean isMouseOver(int x, int y, int w, int h, Minecraft mc) {

        int mx = getMouseX(mc);
        int my = getMouseY(mc);

        return mx >= x && mx <= x + w &&
               my >= y && my <= y + h;
    }

    private static int getMouseX(Minecraft mc) {
        return (int)(mc.mouseHandler.xpos()
                * mc.getWindow().getGuiScaledWidth()
                / mc.getWindow().getScreenWidth());
    }

    private static int getMouseY(Minecraft mc) {
        return (int)(mc.mouseHandler.ypos()
                * mc.getWindow().getGuiScaledHeight()
                / mc.getWindow().getScreenHeight());
    }

    private static void renderDraggedItem(GuiGraphics g) {

        ItemStack stack = OverlayState.getDraggedStack();
        if (stack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();

        int mouseX = getMouseX(mc);
        int mouseY = getMouseY(mc);

        g.pose().pushPose();
        g.pose().translate(0, 0, 600);
        g.renderItem(stack, mouseX - 8, mouseY - 8);
        g.pose().popPose();
    }
}