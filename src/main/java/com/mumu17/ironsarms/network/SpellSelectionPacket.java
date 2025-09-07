package com.mumu17.ironsarms.network;

import com.mumu17.arscurios.util.ArsCuriosLivingEntity;
import com.mumu17.arscurios.util.ExtendedHand;
import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.utils.GunTags;
import com.tacz.guns.api.item.IGun;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SpellSelectionPacket {

    private final int slotIndex;
    private final String hand;

    public SpellSelectionPacket(int index, String hand) {
        this.slotIndex = index;
        this.hand = hand;
    }

    public static SpellSelectionPacket decode(FriendlyByteBuf buf) {
        return new SpellSelectionPacket(buf.readInt(), buf.readUtf());
    }

    public static void encode(SpellSelectionPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slotIndex);
        buf.writeUtf(msg.hand);
    }

    public static void handle(SpellSelectionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                ItemStack mainHand = player.getMainHandItem();
                if (mainHand.getItem() instanceof IGun && msg.slotIndex >= 0) {
                    GunTags.setSpellSlot(mainHand, msg.slotIndex);
                }
                ExtendedHand extendedHand = ExtendedHand.getSlotByName(msg.hand);
                if (extendedHand.isAmmoBox()) {
                    ArsCuriosLivingEntity.setPlayerExtendedHand(player, extendedHand);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
