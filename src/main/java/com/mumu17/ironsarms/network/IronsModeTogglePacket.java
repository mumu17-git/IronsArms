package com.mumu17.ironsarms.network;

import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.utils.GunTags;
import com.mumu17.ironsarms.utils.IronsArmsAmmoBox;
import com.mumu17.ironsarms.utils.PlayerTags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class IronsModeTogglePacket {

    public static final String IS_IRONS_MODE = IronsArms.MODID +":irons_mode";

    public IronsModeTogglePacket() {}

    public static IronsModeTogglePacket decode(FriendlyByteBuf buf) {
        return new IronsModeTogglePacket();
    }

    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            PlayerTags.setIronsMode(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
