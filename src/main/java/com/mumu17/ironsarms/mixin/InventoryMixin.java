package com.mumu17.ironsarms.mixin;

import com.mumu17.ironsarms.utils.GunTags;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin {
    @Shadow
    @Final
    public Player player;

    @Inject(method = "setItem", at = @At("TAIL"))
    private void onSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        GunTags.updateSpellSelectionManager(this.player);
    }

    @Inject(method = "removeItem(Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void onRemoveItem(ItemStack p_36058_, CallbackInfo ci) {
        GunTags.updateSpellSelectionManager(this.player);
    }

    @Inject(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;", at = @At("TAIL"))
    private void onRemoveItem(int p_35993_, int p_35994_, CallbackInfoReturnable<ItemStack> cir) {
        GunTags.updateSpellSelectionManager(this.player);
    }

}
