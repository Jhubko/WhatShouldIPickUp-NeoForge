package com.whatshouldipickup.client;

import com.whatshouldipickup.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WhatShouldIPickUpConfigScreen extends Screen {

    private final Screen parent;

    private Checkbox alwaysShowCheckbox;
    private Checkbox disableAutoPickupCheckbox;
    private RadiusSlider radiusSlider;

    public WhatShouldIPickUpConfigScreen(Screen parent) {
        super(Component.literal("What Should I Pick Up"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        alwaysShowCheckbox = Checkbox.builder(
                        Component.translatable(
                                "config.whatshouldipickup.always_show"
                        ),
                        this.font
                )
                .pos(centerX - 100, 80)
                .selected(ClientConfig.isAlwaysShow())
                .onValueChange((checkbox, selected) ->
                        ClientConfig.setAlwaysShow(selected)
                )
                .build();

        this.addRenderableWidget(alwaysShowCheckbox);

        disableAutoPickupCheckbox = Checkbox.builder(
                        Component.translatable(
                                "config.whatshouldipickup.disable_auto_pickup"
                        ),
                        this.font
                )
                .pos(centerX - 100, 110)
                .selected(ClientConfig.isAutoPickupDisabled())
                .onValueChange((checkbox, selected) -> {

                    ClientConfig.setAutoPickupDisabled(selected);
                    AutoPickupClientHandler.sync();
                })
                .build();

        this.addRenderableWidget(disableAutoPickupCheckbox);

        radiusSlider = new RadiusSlider(
                centerX - 100,
                160,
                200,
                20,
                ClientConfig.getDetectionRadius()
        );

        this.addRenderableWidget(radiusSlider);

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("Done"),
                                button -> this.onClose()
                        )
                        .pos(centerX - 100, 205)
                        .width(200)
                        .build()
        );
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        this.renderBackground(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                35,
                0xFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable(
                        "config.whatshouldipickup.detection_radius"
                ),
                this.width / 2,
                140,
                0xFFFFFF
        );

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private static class RadiusSlider extends AbstractSliderButton {

        private static final double MIN = 1.0;
        private static final double MAX = 5.0;

        public RadiusSlider(
                int x,
                int y,
                int width,
                int height,
                double radius
        ) {
            super(
                    x,
                    y,
                    width,
                    height,
                    Component.empty(),
                    toSliderValue(radius)
            );

            updateMessage();
        }

        private static double toSliderValue(double radius) {
            return (radius - MIN) / (MAX - MIN);
        }

        private static double toRadius(double value) {
            double radius =
                    MIN + value * (MAX - MIN);

            return Math.round(radius * 2.0) / 2.0;
        }

        @Override
        protected void updateMessage() {

            double radius =
                    toRadius(this.value);

            this.setMessage(
                    Component.literal(
                            String.format(
                                    "Detection radius: %.1f blocks",
                                    radius
                            )
                    )
            );
        }

        @Override
        protected void applyValue() {

            double radius =
                    toRadius(this.value);

            this.value =
                    toSliderValue(radius);

            ClientConfig.setDetectionRadius(radius);

            updateMessage();
        }
    }
}