package com.whatshouldipickup.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class OverlayRenderer {

    private static final int VISIBLE = OverlayState.getVisibleCount();
    private static float marqueeOffset = 0;

    public static class Layout {
        public int startX, startY;
        public int listX, listY;
        public int listW, listH;
        public int scrollbarX, scrollbarY, scrollbarH;
        public int scrollbarW = 10;
    }

    public static Layout getLayout(Minecraft mc, InventoryScreen screen) {

        int x = screen.getGuiLeft();
        int y = screen.getGuiTop();

        int startX = x + 185;
        int startY = y + 15;

        int panelHeight = 150;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int guiRight = x + 176;

        int availableWidth = screenW - guiRight - 15;

        int baseWidth = 150;
        int panelWidth = Math.min(baseWidth, availableWidth);
        panelWidth = Math.max(100, panelWidth);

        int listW = panelWidth - 10;
        int listH = panelHeight;

        Layout l = new Layout();

        l.startX = startX;
        l.startY = startY;

        l.listX = startX;
        l.listY = startY;

        l.listW = listW;
        l.listH = listH;

        l.scrollbarX = startX + listW;
        l.scrollbarY = startY;
        l.scrollbarH = panelHeight - 20;

        return l;
    }

    public static void render(GuiGraphics g, InventoryScreen screen, List<ItemEntity> items) {

        Minecraft mc = Minecraft.getInstance();
        Layout l = getLayout(mc, screen);

        marqueeOffset += 0.6f;

        g.fill(l.startX - 5, l.startY - 15,
                l.startX + l.listW + l.scrollbarW,
                l.startY + l.listH,
                0xAA000000);

        g.drawString(mc.font, "Nearby Items", l.startX, l.startY - 10, 0xFFFFFF);

        int scroll = OverlayState.getScrollIndex();
        int hoveredIndex = -1;

        for (int i = 0; i < VISIBLE; i++) {

            int index = i + scroll;
            if (index >= items.size()) break;

            ItemEntity entity = items.get(index);
            ItemStack stack = entity.getItem();

            int rowY = l.listY + 4 + i * 18;

            g.renderItem(stack, l.listX, rowY);

            if (stack.getCount() > 1) {
                g.pose().pushPose();
                g.pose().translate(0, 0, 200);
                g.drawString(mc.font, String.valueOf(stack.getCount()),
                        l.listX + 14, rowY + 9, 0xFFFFFF);
                g.pose().popPose();
            }

            String name = stack.getHoverName().getString();

            int textX = l.listX + 18 + (stack.getCount() > 1 ? 10 : 0);
            int textY = rowY + 4;

            int textWidth = mc.font.width(name);

            g.pose().pushPose();

            int leftClip = 18 + (stack.getCount() > 1 ? 10 : 0);

            g.enableScissor(
                    l.listX + leftClip,
                    l.listY,
                    l.listX + l.listW,
                    l.listY + l.listH
            );

            int available = l.listW - (textX - l.listX);

            if (textWidth <= available) {
                g.drawString(mc.font, name, textX, textY, 0xFFFFFF);
            } else {
                int gap = 40;
                float cycle = textWidth + gap;

                float offset = marqueeOffset % cycle;

                g.drawString(mc.font, name, (int)(textX - offset), textY, 0xFFFFFF);
                g.drawString(mc.font, name, (int)(textX - offset + cycle), textY, 0xFFFFFF);
            }

            g.disableScissor();
            g.pose().popPose();

            if (isMouseOverRow(l, rowY, 16, mc)) {
                hoveredIndex = index;
            }
        }

        renderScrollbar(g, l, items.size());
        renderDraggedItem(g);

        int mouseX = (int) (mc.mouseHandler.xpos()
                * mc.getWindow().getGuiScaledWidth()
                / mc.getWindow().getScreenWidth());

        int mouseY = (int) (mc.mouseHandler.ypos()
                * mc.getWindow().getGuiScaledHeight()
                / mc.getWindow().getScreenHeight());

        if (!isMouseOverScrollbar(mouseX, mouseY, l)
                && hoveredIndex != -1
                && hoveredIndex < items.size()) {

            ItemStack hoveredStack = items.get(hoveredIndex).getItem();

            g.pose().pushPose();
            g.pose().translate(0, 0, 500);
            g.renderTooltip(mc.font, hoveredStack, mouseX, mouseY);
            g.pose().popPose();
        }
    }

    private static boolean isMouseOverRow(Layout l, int y, int h, Minecraft mc) {

        int mx = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
        int my = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());

        return mx >= l.listX && mx <= l.listX + l.listW &&
               my >= y && my <= y + h;
    }

    private static boolean isMouseOverScrollbar(int mx, int my, Layout l) {

        return mx >= l.scrollbarX && mx <= l.scrollbarX + l.scrollbarW &&
               my >= l.scrollbarY && my <= l.scrollbarY + l.scrollbarH;
    }

    private static void renderScrollbar(GuiGraphics g, Layout l, int size) {

        g.fill(l.scrollbarX, l.scrollbarY,
                l.scrollbarX + l.scrollbarW,
                l.scrollbarY + l.scrollbarH,
                0x66000000);

        int total = size;
        int visible = VISIBLE;

        if (total <= visible) {
            g.fill(l.scrollbarX, l.scrollbarY,
                    l.scrollbarX + l.scrollbarW,
                    l.scrollbarY + l.scrollbarH,
                    0xFFFFFFFF);
            return;
        }

        int max = total - visible;

        int thumbH = (int)((float) visible / total * l.scrollbarH);
        thumbH = Math.max(12, thumbH);

        float progress = OverlayState.getScrollCurrent() / max;

        int thumbY = l.scrollbarY + (int)((l.scrollbarH - thumbH) * progress);

        g.fill(l.scrollbarX, thumbY,
                l.scrollbarX + l.scrollbarW,
                thumbY + thumbH,
                0xFFFFFFFF);
    }

    private static void renderDraggedItem(GuiGraphics g) {

        ItemStack stack = OverlayState.getDraggedStack();
        if (stack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();

        int mouseX = (int) (mc.mouseHandler.xpos()
                * mc.getWindow().getGuiScaledWidth()
                / mc.getWindow().getScreenWidth());

        int mouseY = (int) (mc.mouseHandler.ypos()
                * mc.getWindow().getGuiScaledHeight()
                / mc.getWindow().getScreenHeight());

        g.pose().pushPose();
        g.pose().translate(0, 0, 600);
        g.renderItem(stack, mouseX - 8, mouseY - 8);
        g.pose().popPose();
    }
}