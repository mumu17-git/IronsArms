package com.mumu17.ironsarms.network;

import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.utils.GunTags;
import com.tacz.guns.api.item.IGun;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSyncChargedManaMessage {
    private final int manaCount;
    public static final String MANA = IronsArms.MODID+":Mana";
    public static final int MAX_MANA = 10000;

    public RequestSyncChargedManaMessage(int ManaCount) {
        this.manaCount = ManaCount;
    }

    public static void encode(RequestSyncChargedManaMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.manaCount);
    }

    public static RequestSyncChargedManaMessage decode(FriendlyByteBuf buf) {
        return new RequestSyncChargedManaMessage(buf.readInt());
    }

    public static void handle(RequestSyncChargedManaMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                for (ItemStack stack : player.getInventory().items) {
                    if (!stack.isEmpty() && stack.getItem() instanceof IGun) {
                        int chargedManaCount = GunTags.getMana(stack);
                        int removeManaCount = (int) ((float) msg.manaCount - (float) chargedManaCount);
                        if (removeManaCount > 0.0) {
                            MagicData.getPlayerMagicData(player).addMana(-removeManaCount);
                        }
                        GunTags.addMana(stack, removeManaCount);

                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
