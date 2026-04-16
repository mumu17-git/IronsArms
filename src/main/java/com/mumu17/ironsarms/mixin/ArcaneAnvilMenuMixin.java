package com.mumu17.ironsarms.mixin;

import com.tacz.guns.api.item.IGun;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArcaneAnvilMenu.class)
public abstract class ArcaneAnvilMenuMixin extends ItemCombinerMenu {

    public ArcaneAnvilMenuMixin(@Nullable MenuType<?> pType, int pContainerId, net.minecraft.world.entity.player.Inventory pPlayerInventory, net.minecraft.world.inventory.ContainerLevelAccess pAccess) {
        super(pType, pContainerId, pPlayerInventory, pAccess);
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void onCreateResult(CallbackInfo ci) {
        ItemStack baseStack = this.inputSlots.getItem(0);
        ItemStack modifierStack = this.inputSlots.getItem(1);

        // 既に結果がある場合は何もしない（Irons標準の処理を優先）
        if (!this.resultSlots.getItem(0).isEmpty()) return;

        // TACZの銃と、魔法入りのアイテム（スクロール等）の組み合わせかチェック
        if (baseStack.getItem() instanceof IGun && ISpellContainer.isSpellContainer(modifierStack)) {
            var spellContainer = ISpellContainer.get(modifierStack);
            if (!spellContainer.isEmpty()) {
                SpellData spellData = spellContainer.getSpellAtIndex(0);

                ItemStack result = baseStack.copy();
                CompoundTag tag = result.getOrCreateTag();

                CompoundTag spellTag = new CompoundTag();
                spellTag.putString("SpellID", spellData.getSpell().getSpellId());
                spellTag.putInt("Level", spellData.getLevel());

                tag.put("InscribedSpell", spellTag);

                // 結果スロット（index 2）にセット
                this.resultSlots.setItem(0, result);
            }
        }
    }
}
