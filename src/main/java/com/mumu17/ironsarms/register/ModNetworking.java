package com.mumu17.ironsarms.register;

import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.network.IronsModeTogglePacket;
import com.mumu17.ironsarms.network.RequestSyncChargedManaMessage;
import com.mumu17.ironsarms.network.SpellSelectionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    public static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(IronsArms.MODID, "main"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, IronsModeTogglePacket.class,
                IronsModeTogglePacket::encode,
                IronsModeTogglePacket::decode,
                IronsModeTogglePacket::handle
        );
        INSTANCE.registerMessage(id++, RequestSyncChargedManaMessage.class, RequestSyncChargedManaMessage::encode, RequestSyncChargedManaMessage::decode, RequestSyncChargedManaMessage::handle);
        INSTANCE.registerMessage(id++, SpellSelectionPacket.class, SpellSelectionPacket::encode, SpellSelectionPacket::decode, SpellSelectionPacket::handle);

    }
}