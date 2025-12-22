package com.mumu17.ironsarms.network;

import com.mumu17.armslib.util.GunItemNbt;
import com.mumu17.arscurios.util.InteractionHandUtil;
import com.mumu17.ironsarms.utils.GunTags;
import com.tacz.guns.api.item.IGun;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

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
                ItemStack mainhand = player.getMainHandItem();
                if (mainhand.getItem() instanceof IGun iGun && msg.slotIndex >= 0) {
                    GunTags.setSpellSlot(mainhand, msg.slotIndex);
                    GunItemNbt access = (GunItemNbt) iGun;
                    InteractionHand interactionHand = InteractionHandUtil.getSlotByName(msg.hand);
                    if (InteractionHandUtil.isAmmoBox(interactionHand)) {
                        access.setInteractionHand(mainhand, interactionHand);
                        //ArsCuriosLivingEntity.setPlayerExtendedHand(player, interactionHand);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
