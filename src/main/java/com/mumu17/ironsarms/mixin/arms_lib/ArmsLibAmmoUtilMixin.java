package com.mumu17.ironsarms.mixin.arms_lib;

import com.mumu17.armslib.util.ArmsLibAmmoUtil;
import com.mumu17.arscurios.util.InteractionHandUtil;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmsLibAmmoUtil.class)
public class ArmsLibAmmoUtilMixin {

    @Inject(method = "isSelectedSpellSlot", at = @At(value = "RETURN"), remap = false, cancellable = true)
    private static void isSelectedSpellSlot(InteractionHand ammoBoxSlot, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (ammoBoxSlot != null && InteractionHandUtil.isAmmoBox(ammoBoxSlot) && livingEntity instanceof Player player) {
            String hand = MagicData.getPlayerMagicData(player).getSyncedData().getSpellSelection().equipmentSlot;
            if (hand != null && !hand.isEmpty()) {
                cir.setReturnValue(hand.equals(InteractionHandUtil.getSlotName(ammoBoxSlot)));
            }
        }
    }
}
