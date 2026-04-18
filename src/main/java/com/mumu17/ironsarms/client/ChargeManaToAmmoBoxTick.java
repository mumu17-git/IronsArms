package com.mumu17.ironsarms.client;

import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.network.RequestSyncChargedManaMessage;
import com.mumu17.ironsarms.register.ModNetworking;
import com.mumu17.ironsarms.utils.GunTags;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsArms.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChargeManaToAmmoBoxTick {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!event.side.isClient() || event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player != null) {
                for (ItemStack stack : player.getInventory().items) {
                    if (!GunTags.isTargetItem(stack) || stack.getTag() == null || !stack.getTag().contains("InscribedSpell")) continue;
                    chargeManaOrCancel(stack);
                    return;
                }
            }
        }
    }

    private static void chargeManaOrCancel(ItemStack stack) {
        int chargeMinMana = 100;
        int chargedManaCount = GunTags.getMana(stack);
        if (chargedManaCount < 0) {
            chargedManaCount = 0;
        }

        double mana = ClientMagicData.getPlayerMana();
        if (mana < chargeMinMana) {
            return;
        }

        int maxManaCount = RequestSyncChargedManaMessage.MAX_MANA;

        int maxChargedManaCount = maxManaCount - chargedManaCount;

        int chargeManaCount = Math.min(maxChargedManaCount, chargeMinMana);

        if (chargeManaCount <= 0) return;

        sendManaCountToServer(Math.min((chargedManaCount + chargeManaCount), maxManaCount));
    }

    public static void sendManaCountToServer(int manaCount) {
        ClientMagicData.setMana(manaCount);
        ModNetworking.INSTANCE.sendToServer(new RequestSyncChargedManaMessage(manaCount));
    }
}