package com.whatshouldipickup.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class OverlayRenderer {

    private static final int VISIBLE = 6;
    private static float marqueeOffset = 0;

    public static void render(GuiGraphics g, InventoryScreen screen, List<ItemEntity> items) {

        Minecraft mc = Minecraft.getInstance();

        int x = screen.getGuiLeft();
        int y = screen.getGuiTop();

        int startX = x + 185;
        int startY = y + 38;
        int panelHeight = 120;

        int screenW = mc.getWindow().getGuiScaledWidth();

        int baseWidth = 150;
        int guiRight = x + 176;
        int availableWidth = screenW - guiRight - 15;

        int panelWidth = Math.min(baseWidth, availableWidth);
        panelWidth = Math.max(100, panelWidth);

        int scrollbarW = 10;

        int listX = startX;
        int listY = startY;
        int listW = panelWidth - scrollbarW;
        int listH = panelHeight;

        marqueeOffset += 0.6f;

        g.fill(startX - 5, startY - 15, startX + panelWidth, startY + panelHeight, 0xAA000000);
        g.drawString(mc.font, "Nearby Items", startX, startY - 10, 0xFFFFFF);

        int scroll = OverlayState.getScrollIndex();
        int hoveredIndex = -1;

        for (int i = 0; i < VISIBLE; i++) {

            int index = i + scroll;
            if (index >= items.size()) break;

            ItemEntity entity = items.get(index);
            ItemStack stack = entity.getItem();

            int rowY = listY + 4 + i * 18;

            g.renderItem(stack, listX, rowY);

            if (stack.getCount() > 1) {
                String countText = String.valueOf(stack.getCount());
                g.pose().pushPose();
                g.pose().translate(0, 0, 200);
                g.drawString(mc.font, countText, listX + 14, rowY + 9, 0xFFFFFF);
                g.pose().popPose();
            }

            String name = stack.getHoverName().getString();
            
            int textX = listX + 18 + (stack.getCount() > 1 ? 10 : 0);
            int textY = rowY + 4;

            int textWidth = mc.font.width(name);

            g.pose().pushPose();

            int leftClip = 18 + (stack.getCount() > 1 ? 10 : 0);
            g.enableScissor(listX + leftClip, listY, listX + listW, listY + listH);

            int availableMarqueeWidth = listW - (textX - listX);
            if (textWidth <= availableMarqueeWidth) {
                g.drawString(mc.font, name, textX, textY, 0xFFFFFF);
            } else {
                int gap = 40;

                int baseX = textX;

                float cycle = textWidth + gap;
                float offset = marqueeOffset % cycle;

                int drawX1 = (int)(baseX - offset);
                int drawX2 = (int)(baseX - offset + textWidth + gap);

                g.drawString(mc.font, name, drawX1, textY, 0xFFFFFF);
                g.drawString(mc.font, name, drawX2, textY, 0xFFFFFF);
            }
            g.disableScissor();
            g.pose().popPose();

            if (isMouseOverRow(listX, rowY, listW, 16, mc)) {
                hoveredIndex = index;
            }
        }

        renderScrollbar(g, listX, listY, panelHeight, items.size(), listW, scrollbarW);
        renderDraggedItem(g);

        int mouseX = (int) (mc.mouseHandler.xpos()
                * mc.getWindow().getGuiScaledWidth()
                / mc.getWindow().getScreenWidth());

        int mouseY = (int) (mc.mouseHandler.ypos()
                * mc.getWindow().getGuiScaledHeight()
                / mc.getWindow().getScreenHeight());

        if (!isMouseOverScrollbar(mouseX, mouseY, listX, listY, panelHeight, listW, scrollbarW)
                && hoveredIndex != -1
                && hoveredIndex < items.size()) {

            ItemStack hoveredStack = items.get(hoveredIndex).getItem();

            g.pose().pushPose();
            g.pose().translate(0, 0, 500);
            g.renderTooltip(mc.font, hoveredStack, mouseX, mouseY);
            g.pose().popPose();
        }
    }

    private static boolean isMouseOverRow(int x, int y, int w, int h, Minecraft mc) {
        int mx = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
        int my = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static boolean isMouseOverScrollbar(int mx, int my,
                                               int x, int y,
                                               int h,
                                               int listW,
                                               int scrollbarW) {

        int barX = x + listW;
        int barY = y;
        int barH = h - 20;

        return mx >= barX && mx <= barX + scrollbarW &&
               my >= barY && my <= barY + barH;
    }

    private static void renderScrollbar(GuiGraphics g,
                                        int x,
                                        int y,
                                        int panelHeight,
                                        int size,
                                        int listW,
                                        int scrollbarW) {

        int barX = x + listW;
        int barHeight = panelHeight - 20;

        g.fill(barX, y, barX + scrollbarW, y + barHeight, 0x66000000);

        int max = Math.max(1, size - VISIBLE);

        float progress = OverlayState.getScrollCurrent() / max;

        int thumbHeight = Math.max(12, barHeight / (max + VISIBLE));
        int thumbY = y + (int) ((barHeight - thumbHeight) * progress);

        g.fill(barX, thumbY, barX + scrollbarW, thumbY + thumbHeight, 0xFFFFFFFF);
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