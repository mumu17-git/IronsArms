package com.mumu17.ironsarms.client;

import com.mumu17.armslib.util.GunItemNbt;
import com.mumu17.arscurios.util.ArsCuriosInventoryHelper;
import com.mumu17.arscurios.util.ExtendedHand;
import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.network.RequestSyncChargedManaMessage;
import com.mumu17.ironsarms.register.ModNetworking;
import com.mumu17.ironsarms.utils.GunTags;
import com.mumu17.ironsarms.utils.IronsArmsAmmoBox;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.item.AmmoBoxItem;
import com.tacz.guns.network.NetworkHandler;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicProvider;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import io.redspace.ironsspellbooks.gui.overlays.network.ServerboundSelectSpell;
import io.redspace.ironsspellbooks.player.ClientInputEvents;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mod.EventBusSubscriber(modid = IronsArms.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChargeManaToAmmoBoxTick {

    public static int lastSelected = -1;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            for (ExtendedHand hand : ExtendedHand.values()) {
                ItemStack stack = ArsCuriosInventoryHelper.getCuriosInventoryItem(player, hand.getSlotName());
                if (isTargetItem(stack)) {
                    chargeManaOrCancel(stack, hand.getSlotName());
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