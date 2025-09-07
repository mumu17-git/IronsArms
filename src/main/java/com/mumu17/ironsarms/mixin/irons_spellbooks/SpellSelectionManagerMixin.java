package com.mumu17.ironsarms.mixin.irons_spellbooks;

import com.llamalad7.mixinextras.sugar.Local;
import com.mumu17.arscurios.util.ArsCuriosInventoryHelper;
import com.mumu17.arscurios.util.ArsCuriosLivingEntity;
import com.mumu17.arscurios.util.ExtendedHand;
import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.network.SpellSelectionPacket;
import com.mumu17.ironsarms.register.ModNetworking;
import com.mumu17.ironsarms.utils.GunTags;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = SpellSelectionManager.class)
public abstract class SpellSelectionManagerMixin {

    @Shadow(remap = false)
    private void initItem(@Nullable ItemStack itemStack, String equipmentSlot) {}

    @Shadow(remap = false)
    @Nullable
    public abstract SpellSelectionManager.@Nullable SelectionOption getSelection();

    @Shadow(remap = false)
    public abstract int getGlobalSelectionIndex();

    @Shadow(remap = false)
    public abstract List<SpellSelectionManager.SelectionOption> getAllSpells();

    @Shadow(remap = false)
    public abstract void makeSelection(int index);

    @Shadow(remap = false)
    public abstract SpellSelection getCurrentSelection();

    @Shadow(remap = false)
    private SpellSelection spellSelection;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/api/magic/SpellSelectionManager;initItem(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)V", ordinal = 0), remap = false)
    private void init$afterInitItem(CallbackInfo ci, @Local(argsOnly = true)Player player) {
        ExtendedHand[] hands = ExtendedHand.values();
        ItemStack mainhand = player.getMainHandItem();
        if (mainhand.getItem() instanceof IGun) {
            for (ExtendedHand hand : hands) {
                if (hand.isAmmoBox()) {
                    ItemStack stack = ArsCuriosInventoryHelper.getCuriosInventoryItem(player, hand.getSlotName());
                    if (stack != null && stack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(mainhand, stack)) {
                        IronsArms.LOGGER.debug("ADD : {}",hand.getSlotName());
                        this.initItem(stack, hand.getSlotName());
                    }
                }
            }
        }
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z", shift = At.Shift.AFTER), remap = false)
    private void init$tail(CallbackInfo ci, @Local(argsOnly = true)Player player) {
        ItemStack mainhand = player.getMainHandItem();
        if (mainhand.getItem() instanceof IGun) {
            int spellCount = getAllSpells().size();
            int spellSlot = GunTags.getSpellSlot(mainhand) >= 0 && GunTags.getSpellSlot(mainhand) < spellCount ? GunTags.getSpellSlot(mainhand) : 0;
            makeSelection(spellSlot);
            SpellSelection selection = getCurrentSelection();
            if (selection != null) {
                String slot = selection.equipmentSlot;
                ExtendedHand hand = ExtendedHand.getSlotByName(slot);
                if (hand != null && hand.isAmmoBox()) {
                    int index = selection.index >= 0 && selection.index < spellCount ? selection.index : -1;
                    IronsArms.LOGGER.debug("Get Hand: {}", slot);
                    selection.makeSelection(slot, index);
                    this.spellSelection = selection;
                    GunTags.updateSpellSelection(player, (SpellSelectionManager) (Object) this);
                }
            }
        }
    }
}
