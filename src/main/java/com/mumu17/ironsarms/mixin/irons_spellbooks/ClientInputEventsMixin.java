package com.mumu17.ironsarms.mixin.irons_spellbooks;

import com.llamalad7.mixinextras.sugar.Local;
import com.mumu17.arscurios.util.InteractionHandUtil;
import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.network.SpellSelectionPacket;
import com.mumu17.ironsarms.register.ModNetworking;
import com.tacz.guns.api.item.IGun;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.player.ClientInputEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientInputEvents.class)
public class ClientInputEventsMixin {

    @Inject(method = "clientMouseScrolled", at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/api/magic/SpellSelectionManager;makeSelection(I)V", shift = At.Shift.AFTER), remap = false)
    private static void clientMouseScrolled$beforeMakeSelection(InputEvent.MouseScrollingEvent event, CallbackInfo ci, @Local(name = "player")Player player, @Local(name = "spellSelectionManager")SpellSelectionManager spellSelectionManager) {
        SpellSelectionManager.SelectionOption selectionOption = spellSelectionManager.getSelection();
        if (selectionOption != null) {
            InteractionHand hand = InteractionHandUtil.getSlotByName(selectionOption.slot);
            // IronsArms.LOGGER.debug("Player: {}, Hand: {}, Index: {}", player.level().isClientSide ? "Local" : "Server", hand != null ? InteractionHandUtil.getSlotName(hand) : "null", spellSelectionManager.getGlobalSelectionIndex());
            if (hand != null && InteractionHandUtil.isAmmoBox(hand)) {
                ItemStack mainhand = player.getMainHandItem();
                if (mainhand.getItem() instanceof IGun) {
                    ModNetworking.INSTANCE.sendToServer(new SpellSelectionPacket(spellSelectionManager.getGlobalSelectionIndex(), InteractionHandUtil.getSlotName(hand)));
                }
            }
        }
    }
}
