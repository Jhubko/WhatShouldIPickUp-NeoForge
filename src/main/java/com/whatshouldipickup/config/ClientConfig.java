package com.whatshouldipickup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ALWAYS_SHOW;
    public static final ModConfigSpec.BooleanValue DISABLE_AUTO_PICKUP;
    public static final ModConfigSpec.DoubleValue DETECTION_RADIUS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");

        ALWAYS_SHOW = builder
                .comment("Always show the nearby items bar when there are nearby items.")
                .translation("config.whatshouldipickup.always_show")
                .define("alwaysShow", false);

        DISABLE_AUTO_PICKUP = builder
                .comment("Prevent normal automatic item pickup.")
                .translation("config.whatshouldipickup.disable_auto_pickup")
                .define("disableAutoPickup", false);

        DETECTION_RADIUS = builder
                .comment("Maximum distance in blocks at which nearby dropped items are detected.")
                .translation("config.whatshouldipickup.detection_radius")
                .defineInRange("detectionRadius", 1.5, 1.0, 5.0);

        builder.pop();

        SPEC = builder.build();
    }

    public static boolean isAlwaysShow() {
        return ALWAYS_SHOW.get();
    }

    public static boolean isAutoPickupDisabled() {
        return DISABLE_AUTO_PICKUP.get();
    }

    public static double getDetectionRadius() {
        return DETECTION_RADIUS.get();
    }

    public static void setAlwaysShow(boolean value) {
        ALWAYS_SHOW.set(value);
        ALWAYS_SHOW.save();
    }

    public static void setAutoPickupDisabled(boolean value) {
        DISABLE_AUTO_PICKUP.set(value);
        DISABLE_AUTO_PICKUP.save();
    }

    public static void setDetectionRadius(double value) {
        value = Math.round(value * 2.0) / 2.0;
        value = Math.max(1.0, Math.min(5.0, value));

        DETECTION_RADIUS.set(value);
        DETECTION_RADIUS.save();
    }
}