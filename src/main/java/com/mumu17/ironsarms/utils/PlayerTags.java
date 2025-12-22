package com.mumu17.ironsarms.utils;

import com.mumu17.ironsarms.network.IronsModeTogglePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class PlayerTags {

    public static void setIronsMode(Player player) {
        if (player != null) {
            player.getPersistentData().putBoolean(IronsModeTogglePacket.IS_IRONS_MODE, !getIronsMode(player));
            boolean isIronsMode = getIronsMode(player);
            String msg = isIronsMode ? Component.translatable("tooltip.ironsarms.irons_mode.on").getString() : Component.translatable("tooltip.ironsarms.irons_mode.off").getString();
            player.displayClientMessage(Component.literal(msg), true);
        }
    }

    public static boolean getIronsMode(Player player) {
        return containsIronsMode(player) && player.getPersistentData().getBoolean(IronsModeTogglePacket.IS_IRONS_MODE);
    }

    public static boolean containsIronsMode(Player player) {
        return player != null && player.getPersistentData().contains(IronsModeTogglePacket.IS_IRONS_MODE);
    }
}
