package com.mumu17.ironsarms.utils;

import com.mumu17.arscurios.util.ArsCuriosLivingEntity;
import com.mumu17.arscurios.util.ExtendedHand;
import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.network.IronsModeTogglePacket;
import com.mumu17.ironsarms.network.SpellSelectionPacket;
import com.mumu17.ironsarms.register.ModNetworking;
import com.tacz.guns.api.item.IGun;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.gui.overlays.network.ServerboundSelectSpell;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GunTags {
    public static final String SPELL_SLOT = "ironsarms:spell_slot";

    public static void setSpellSlot(ItemStack gunItem, int slotIndex) {
        if (gunItem != null && slotIndex >= 0) {
            gunItem.getOrCreateTag().putInt(SPELL_SLOT, slotIndex);
        }
    }

    public static int getSpellSlot(ItemStack gunItem) {
        if (gunItem != null && gunItem.getOrCreateTag().contains(SPELL_SLOT)) {
            return gunItem.getOrCreateTag().getInt(SPELL_SLOT);
        }
        return -1;
    }

    public static void updateSpellSelectionManager(Player player) {
        if (player.level().isClientSide) {
            ClientMagicData.updateSpellSelectionManager();
        } else if (player instanceof ServerPlayer serverPlayer) {
            SpellSelectionManager selectionManager = new SpellSelectionManager(serverPlayer);
            updateSpellSelection(serverPlayer, selectionManager);
        }
        // SpellSelectionManager selectionManager = player.level().isClientSide ? ClientMagicData.getSpellSelectionManager() : new SpellSelectionManager(player);
        // updateSpellSelection(player, selectionManager);
    }

    public static void updateSpellSelection(Player player, SpellSelectionManager selectionManager) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof IGun) {
            int spellCount = selectionManager.getAllSpells().size();
            if (spellCount > 0) {
                int index = GunTags.getSpellSlot(mainHand) >= 0 && GunTags.getSpellSlot(mainHand) < spellCount ? GunTags.getSpellSlot(mainHand) : 0;
                String hand = !selectionManager.getAllSpells().isEmpty() && selectionManager.getAllSpells().get(index) != null ? selectionManager.getAllSpells().get(index).slot : ExtendedHand.MAIN_HAND.getSlotName();
                IronsArms.LOGGER.debug("Update Spell Selection: {}, Hand: {}, Index: {}, Size: {}, SpellSlot: {}", player.level().isClientSide ? "Local" : "Server", hand, index, spellCount, GunTags.getSpellSlot(mainHand));
                selectionManager.makeSelection(index);
                if (player.level().isClientSide) {
                    Messages.sendToServer(new ServerboundSelectSpell(selectionManager.getCurrentSelection()));
                    ModNetworking.INSTANCE.sendToServer(new SpellSelectionPacket(index, hand));
                } else {
                    MagicData.getPlayerMagicData(player).getSyncedData().setSpellSelection(selectionManager.getCurrentSelection());
                    GunTags.setSpellSlot(mainHand, index);
                    ArsCuriosLivingEntity.setPlayerExtendedHand(player, ExtendedHand.getSlotByName(hand));
                }
            }
        }
    }

    public static void setIronsMode(Player player) {
        if (player != null) {
            if (player.getPersistentData().contains(IronsModeTogglePacket.IS_IRONS_MODE)) {
                player.getPersistentData().putBoolean(IronsModeTogglePacket.IS_IRONS_MODE, !player.getPersistentData().getBoolean(IronsModeTogglePacket.IS_IRONS_MODE));
            } else {
                player.getPersistentData().putBoolean(IronsModeTogglePacket.IS_IRONS_MODE, true);
            }
            boolean isIronsMode = player.getPersistentData().getBoolean(IronsModeTogglePacket.IS_IRONS_MODE);
            String msg = isIronsMode ? Component.translatable("tooltip.ironsarms.irons_mode.on").getString() : Component.translatable("tooltip.ironsarms.irons_mode.off").getString();
            player.displayClientMessage(Component.literal(msg), true);
            IronsArms.LOGGER.debug("IsIronsMode: {}", isIronsMode);
        }
    }
}
