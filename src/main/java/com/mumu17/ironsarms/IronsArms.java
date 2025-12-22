package com.mumu17.ironsarms;

import com.mojang.logging.LogUtils;
import com.mumu17.ironsarms.register.ModEntities;
import com.mumu17.ironsarms.register.ModNetworking;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(IronsArms.MODID)
public class IronsArms {

    public static final String MODID = "ironsarms";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronsArms() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.register(modEventBus);
        ModNetworking.register();
    }
}
