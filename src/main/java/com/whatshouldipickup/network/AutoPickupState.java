package com.whatshouldipickup.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AutoPickupState {

    private static final Set<UUID> DISABLED_PLAYERS = new HashSet<>();

    public static void setDisabled(ServerPlayer player, boolean disabled) {
        UUID uuid = player.getUUID();

        if (disabled) {
            DISABLED_PLAYERS.add(uuid);
        } else {
            DISABLED_PLAYERS.remove(uuid);
        }
    }

    public static boolean isDisabled(ServerPlayer player) {
        return DISABLED_PLAYERS.contains(player.getUUID());
    }

    public static void remove(ServerPlayer player) {
        DISABLED_PLAYERS.remove(player.getUUID());
    }
}