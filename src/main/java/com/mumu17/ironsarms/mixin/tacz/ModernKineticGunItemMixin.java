package com.mumu17.ironsarms.mixin.tacz;

import com.mumu17.armslib.util.GunItemNbt;
import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.network.IronsModeTogglePacket;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.item.ModernKineticGunItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ModernKineticGunItem.class, remap = false)
public class ModernKineticGunItemMixin implements GunItemNbt {

    @Unique
    private static final String IS_IRONS_MODE = IronsArms.MODID+":IsIronsMode";

    @Unique
    @Override
    public boolean getIsIronsMode(ItemStack gunItem) {
        if (gunItem != null && gunItem.getItem() instanceof IGun) {
            if (gunItem.getOrCreateTag().contains(IS_IRONS_MODE)) {
                return gunItem.getOrCreateTag().getBoolean(IS_IRONS_MODE);
            }
        }
        return false;
    }

    @Unique
    @Override
    public void setIsIronsMode(ItemStack gunItem, boolean isIronsMode) {
        if (gunItem != null && gunItem.getItem() instanceof IGun) {
            gunItem.getOrCreateTag().putBoolean(IS_IRONS_MODE, isIronsMode);
        }
    }
}
