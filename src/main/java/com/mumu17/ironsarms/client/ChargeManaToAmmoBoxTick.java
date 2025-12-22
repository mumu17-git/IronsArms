package com.mumu17.ironsarms.client;

import com.mumu17.arscurios.util.ArsCuriosInventoryHelper;
import com.mumu17.arscurios.util.InteractionHandUtil;
import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.network.RequestSyncChargedManaMessage;
import com.mumu17.ironsarms.register.ModNetworking;
import com.mumu17.ironsarms.utils.IronsArmsAmmoBox;
import com.tacz.guns.item.AmmoBoxItem;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = IronsArms.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChargeManaToAmmoBoxTick {

    public static int lastSelected = -1;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = ArsCuriosInventoryHelper.getCuriosInventoryItem(player, InteractionHandUtil.getSlotName(hand));
                if (isTargetItem(stack)) {
                    chargeManaOrCancel(stack, InteractionHandUtil.getSlotName(hand));
                }
            }
            List<ItemStack> inventory = player.getInventory().items;
            for (int i = 0; i < inventory.size();i++) {
                ItemStack stack = inventory.get(i);
                /*if (player.getInventory().selected == i) {
                    if (player.getInventory().selected != lastSelected) {
                        lastSelected = player.getInventory().selected;
                        if (stack.getItem() instanceof IGun iGun) {
                            GunItemNbt access = (GunItemNbt) iGun;
                            if (access.getIsIronsMode(stack)) {
                                GunTags.updateSpellSelectionManager(player);
                            }
                        } else {
                            GunTags.updateSpellSelectionManager(player);
                        }
                    }
                }*/
            }
        }
    }

    private static void chargeManaOrCancel(ItemStack stack, String curiosSlot) {
        if (isTargetItem(stack)) {
            int chargedManaCount = IronsArmsAmmoBox.getChargedManaCount(stack);
            if (chargedManaCount < 0) {
                chargedManaCount = 0;
            }

            double mana = ClientMagicData.getPlayerMana();
            if (mana < 100) {
                return;
            }

            int maxManaCount = IronsArmsAmmoBox.getMaxManaCount(stack);

            int maxChargedManaCount = IronsArmsAmmoBox.getMaxChargedManaCount(stack);

            if (chargedManaCount >= maxChargedManaCount) {
                return;
            }
            
            sendManaCountToServer(Math.min((int)(chargedManaCount + mana), maxManaCount), curiosSlot);
        }
    }

    public static void sendManaCountToServer(int manaCount, String curiosSlot) {
        ModNetworking.INSTANCE.sendToServer(new RequestSyncChargedManaMessage(manaCount, curiosSlot));
    }

    private static boolean isTargetItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof AmmoBoxItem;
    }
}