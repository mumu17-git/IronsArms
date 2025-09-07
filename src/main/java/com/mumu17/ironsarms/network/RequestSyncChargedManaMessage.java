package com.mumu17.ironsarms.network;

import com.mumu17.arscurios.util.ArsCuriosInventoryHelper;
import com.tacz.guns.item.AmmoBoxItem;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSyncChargedManaMessage {
    private final int manaCount;
    private final String curiosSlot;

    public RequestSyncChargedManaMessage(int ManaCount, String cs) {
        this.manaCount = ManaCount;
        this.curiosSlot = cs;
    }

    public static void encode(RequestSyncChargedManaMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.manaCount);
        buf.writeUtf(msg.curiosSlot);
    }

    public static RequestSyncChargedManaMessage decode(FriendlyByteBuf buf) {
        return new RequestSyncChargedManaMessage(buf.readInt(), buf.readUtf());
    }

    public static void handle(RequestSyncChargedManaMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                var stack = ArsCuriosInventoryHelper.getCuriosInventoryItem(player, msg.curiosSlot);
                if (!stack.isEmpty() && stack.getItem() instanceof AmmoBoxItem) {
                    int chargedManaCount = stack.getOrCreateTag().getInt("Mana");
                    float removeManaCount = ((float) msg.manaCount - (float) chargedManaCount);
                    if (removeManaCount > 0.0) {
                        MagicData.getPlayerMagicData(player).addMana(-removeManaCount);
                    }
                    stack.getOrCreateTag().putInt("Mana", msg.manaCount);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
