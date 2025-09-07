package com.mumu17.ironsarms.mixin.tacz.client;

import com.mumu17.armslib.util.GunItemNbt;
import com.mumu17.ironsarms.utils.GunTags;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gameplay.LocalPlayerDataHolder;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayerDataHolder.class)
public class LocalPlayerDataHolderMixin {

    @Shadow(remap = false)
    public volatile boolean clientStateLock;

    @Final
    @Shadow(remap = false)
    private LocalPlayer player;

    @Inject(method = "tickStateLock", at = @At(value = "FIELD", target = "Lcom/tacz/guns/client/gameplay/LocalPlayerDataHolder;clientStateLock:Z"), remap = false)
    private void tickStateLock(CallbackInfo ci) {
        if (this.clientStateLock) {
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof IGun iGun) {
                    GunItemNbt access = (GunItemNbt) iGun;
                    if (access.getIsIronsMode(stack)) {
                        GunTags.updateSpellSelectionManager(player);
                    }
                } else {
                    GunTags.updateSpellSelectionManager(player);
                }
            }
        }
    }
}
