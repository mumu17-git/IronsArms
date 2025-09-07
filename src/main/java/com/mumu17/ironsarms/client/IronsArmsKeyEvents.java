package com.mumu17.ironsarms.client;

import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.network.IronsModeTogglePacket;
import com.mumu17.ironsarms.register.ModNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsArms.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class IronsArmsKeyEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (IronsArmsKeyHandler.IRONS_MODE_KEY.consumeClick()) {
            if (Minecraft.getInstance().player != null) {
                ModNetworking.INSTANCE.sendToServer(new IronsModeTogglePacket());
            }
        }
    }
}
