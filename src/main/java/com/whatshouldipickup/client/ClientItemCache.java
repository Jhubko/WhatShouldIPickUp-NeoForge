package com.whatshouldipickup.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ClientItemCache {

    private static final List<ItemEntity> ITEMS = new ArrayList<>();

    private static int tickCounter = 0;

    private static final int UPDATE_INTERVAL = 10; // co 10 ticków (~0.5s)

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        tickCounter++;

        if (tickCounter < UPDATE_INTERVAL) return;
        tickCounter = 0;

        Level level = mc.player.level();

        ITEMS.clear();

        ITEMS.addAll(
                level.getEntitiesOfClass(
                        ItemEntity.class,
                        mc.player.getBoundingBox().inflate(2)
                )
        );
    }

    public static List<ItemEntity> getItems() {
        return ITEMS;
    }
}