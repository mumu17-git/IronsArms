package com.mumu17.ironsarms;

import com.mojang.logging.LogUtils;
import com.mumu17.ironsarms.client.ChargeManaToAmmoBoxTick;
import com.mumu17.ironsarms.event.IronsArmsBulletEvents;
import com.mumu17.ironsarms.register.ModNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(IronsArms.MODID)
public class IronsArms {

    public static final String MODID = "ironsarms";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronsArms() {
        MinecraftForge.EVENT_BUS.register(IronsArmsBulletEvents.class);
        ModNetworking.register();
    }
}
