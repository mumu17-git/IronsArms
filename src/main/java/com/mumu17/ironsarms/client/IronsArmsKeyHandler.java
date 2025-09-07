package com.mumu17.ironsarms.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mumu17.ironsarms.IronsArms;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsArms.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class IronsArmsKeyHandler {

    public static final KeyMapping IRONS_MODE_KEY = new KeyMapping(
            "key.ironsarms.irons_mode", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_I, "key.categories.ironsarms"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(IRONS_MODE_KEY);
    }
}

