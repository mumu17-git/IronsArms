package com.mumu17.ironsarms.mixin;

import com.mumu17.armslib.util.ArmsLibAmmoUtil;
import com.mumu17.armslib.util.GunItemNbt;
import com.mumu17.arscurios.util.ArsCuriosInventoryHelper;
import com.mumu17.arscurios.util.InteractionHandUtil;
import com.mumu17.ironsarms.utils.PlayerTags;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    public int lastSelected = -1;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity instanceof Player player) {
            if (player.level().isClientSide) {
                return;
            }
            List<ItemStack> inventory = player.getInventory().items;
            for (int i = 0; i < inventory.size();i++) {
                ItemStack stack = inventory.get(i);
                if (stack != null && stack.getItem() instanceof IGun iGun) {
                    GunItemNbt access = (GunItemNbt) iGun;
                    if (PlayerTags.containsIronsMode(player)) {
                        boolean isIronsMode = PlayerTags.getIronsMode(player);
                        if (access.getOwner(stack) == null || access.getOwner(stack).getUUID() != player.getUUID()) {
                            access.setOwner(stack, player);
                        }
                        CommonGunIndex index = TimelessAPI.getCommonGunIndex(iGun.getGunId(stack)).orElse(null);
                        if (index != null) {
                            GunData gunData = index.getGunData();
                            if (gunData != null) {
                                if (isIronsMode != access.getIsIronsMode(stack)) {
                                    int ammoCount = iGun.useInventoryAmmo(stack) ? ArmsLibAmmoUtil.handleInventoryAmmo(stack, player.getInventory()) + (iGun.hasBulletInBarrel(stack) && gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0) :
                                            iGun.getCurrentAmmoCount(stack) + (iGun.hasBulletInBarrel(stack) && gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0);
                                    ammoCount = Math.min(ammoCount, ArmsLibAmmoUtil.MAX_AMMO_COUNT);
                                    access.setIsIronsMode(stack, isIronsMode && (!access.getIsArsMode(stack) || ammoCount <= 0 || access.getLastAmmoCount(stack) <= -1 || iGun.useInventoryAmmo(stack)));
                                }
                                if (player.getInventory().selected == i && access.getIsIronsMode(stack)) {
                                    SpellSelection spellSelection = MagicData.getPlayerMagicData(player).getSyncedData().getSpellSelection();
                                    String spellSlot = spellSelection.equipmentSlot;
                                    InteractionHand spellHand = InteractionHandUtil.getSlotByName(spellSlot);
                                    int spellIndex = spellSelection.index >= 0 ? spellSelection.index : -1;
                                    ItemStack ammoBox = ArsCuriosInventoryHelper.getCuriosInventoryItem(player, spellSlot);
                                    if (spellHand != null && InteractionHandUtil.isAmmoBox(spellHand)) {
                                        // ArsCuriosLivingEntity.setPlayerExtendedHand(player, spellHand);
                                        access.setInteractionHand(stack, spellHand);
                                    }
                                    if (player.getInventory().selected != this.lastSelected) {
                                        this.lastSelected = player.getInventory().selected;
                                        if (spellHand != null && InteractionHandUtil.isAmmoBox(spellHand)) {
                                            // IronsArms.LOGGER.debug("SpellIndex: {}, SpellSlot: {}", spellIndex, spellSlot);
                                            // GunTags.setSpellSlot(stack, spellIndex);
                                        }
                                        //GunTags.updateSpellSelectionManager(player);
                                        /*int spellIndex = spellSelection.index;
                                        int lastSpellIndex = spellSelection.lastIndex;
                                        ItemStack ammoBox = ArsCuriosInventoryHelper.getCuriosInventoryItem(player, spellSlot);
                                        IronsArms.LOGGER.debug("{}, {}, {}", (ammoBox.getItem() instanceof IAmmoBox _iAmmoBox ? _iAmmoBox.isAmmoBoxOfGun(stack, ammoBox) : "null"), spellSlot, spellIndex);
                                        if (ExtendedHand.getSlotByName(spellSlot).isAmmoBox()) {
                                            if (ammoBox.getItem() instanceof IAmmoBox _iAmmoBox && !_iAmmoBox.isAmmoBoxOfGun(stack, ammoBox)) {
                                                int spellCount = spellSelectionManager.getSpellCount();
                                                for (int j = 0; j < spellCount; j++) {
                                                    spellIndex = nextSpellIndex(spellIndex, spellCount);
                                                    spellSlot = spellSelectionManager.getSpellSlot(spellIndex).slot;
                                                    spellSelection.makeSelection(spellSlot, spellIndex);
                                                    ammoBox = ArsCuriosInventoryHelper.getCuriosInventoryItem(player, spellSlot);
                                                    IronsArms.LOGGER.debug("{}, {}", spellSlot, spellIndex);
                                                    if (ammoBox.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(stack, ammoBox)) {
                                                        SyncedSpellData spellData = MagicData.getPlayerMagicData(player).getSyncedData();
                                                        spellData.setSpellSelection(spellSelection);
                                                        MagicData.getPlayerMagicData(player).setSyncedData(spellData);
                                                        break;
                                                    }
                                                }
                                            }
                                        }*/
                                    }
                                    int ammoCount = iGun.useInventoryAmmo(stack) ? ArmsLibAmmoUtil.handleInventoryAmmo(stack, player.getInventory()) + (iGun.hasBulletInBarrel(stack) && gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0) :
                                            iGun.getCurrentAmmoCount(stack) + (iGun.hasBulletInBarrel(stack) && gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0);
                                    ammoCount = Math.min(ammoCount, ArmsLibAmmoUtil.MAX_AMMO_COUNT);
                                    if (ammoCount <= 0 || access.getLastAmmoCount(stack) <= -1 || iGun.useInventoryAmmo(stack)) {
                                        if (spellSelection != null) {
                                            if (InteractionHandUtil.isAmmoBox(InteractionHandUtil.getSlotByName(spellSlot)) && ammoBox.getItem() instanceof IAmmoBox iAmmoBox && ArmsLibAmmoUtil.ArmsLib$isAmmoBoxOfGun(stack, ammoBox, iAmmoBox) && spellIndex >= 0) {
                                                // spellSelection.makeSelection(spellSlot, spellIndex);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        access.setIsIronsMode(stack, !access.getIsArsMode(stack));
                    }
                }
            }
        }
    }
}
