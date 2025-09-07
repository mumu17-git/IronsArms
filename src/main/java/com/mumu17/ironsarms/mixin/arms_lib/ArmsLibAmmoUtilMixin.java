package com.mumu17.ironsarms.mixin.arms_lib;

import com.llamalad7.mixinextras.sugar.Local;
import com.mumu17.armslib.util.ArmsLibAmmoUtil;
import com.mumu17.arscurios.util.ExtendedHand;
import com.mumu17.ironsarms.IronsArms;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmsLibAmmoUtil.class)
public class ArmsLibAmmoUtilMixin {

    @Inject(method = "isSelectedSpellSlot", at = @At(value = "RETURN"), remap = false, cancellable = true)
    private static void isSelectedSpellSlot(ExtendedHand ammoBoxSlot, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (ammoBoxSlot != null && ammoBoxSlot.isAmmoBox() && livingEntity instanceof Player player) {
            String hand = MagicData.getPlayerMagicData(player).getSyncedData().getSpellSelection().equipmentSlot;
            if (hand != null && !hand.isEmpty()) {
                cir.setReturnValue(hand.equals(ammoBoxSlot.getSlotName()));
                cir.cancel();
            }
        }
    }
}
