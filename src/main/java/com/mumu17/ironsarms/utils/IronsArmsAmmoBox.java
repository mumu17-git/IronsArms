package com.mumu17.ironsarms.utils;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.item.AmmoBoxItem;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.capabilities.magic.SpellContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;

public class IronsArmsAmmoBox {

    public static int getMaxManaCount(ItemStack stack) {
        if (stack.getItem() instanceof AmmoBoxItem ammoBoxItem) {
            ResourceLocation boxAmmoId = ammoBoxItem.getAmmoId(stack);

            AtomicInteger maxSize = new AtomicInteger();
            TimelessAPI.getCommonAmmoIndex(boxAmmoId).ifPresent((index) -> {
                int boxLevelMultiplier = ammoBoxItem.getAmmoLevel(stack) + 1;
                if (!boxAmmoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                    stack.getOrCreateTag().putInt("LastAmmoStackSize", index.getStackSize());
                    maxSize.set(index.getStackSize() * (Integer) SyncConfig.AMMO_BOX_STACK_SIZE.get() * boxLevelMultiplier);
                } else {
                    maxSize.set(stack.getOrCreateTag().getInt("LastAmmoStackSize") * (Integer) SyncConfig.AMMO_BOX_STACK_SIZE.get() * boxLevelMultiplier);
                }
            });

            if (stack.getOrCreateTag().contains("ISB_Spells")) {
                SpellContainer spellContainer = new SpellContainer(stack);
                if (!spellContainer.getActiveSpells().isEmpty()) {
                    SpellData spellData = spellContainer.getActiveSpells().get(0);
                    if (spellData != null && spellData.getSpell() != null) {
                        int cost = spellData.getSpell().getManaCost(spellData.getLevel());
                        return cost * maxSize.get();
                    }
                }
            }
        }

        return 0;
    }

    public static int getMaxChargedManaCount(ItemStack stack) {
        if (stack.getItem() instanceof AmmoBoxItem ammoBoxItem) {
            ResourceLocation boxAmmoId = ammoBoxItem.getAmmoId(stack);

            AtomicInteger size = new AtomicInteger();
            if (!boxAmmoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                size.set(ammoBoxItem.getAmmoCount(stack));
            } else {
                size.set(0);
            }

            if (stack.getOrCreateTag().contains("ISB_Spells")) {
                SpellContainer spellContainer = new SpellContainer(stack);
                if (!spellContainer.getActiveSpells().isEmpty()) {
                    SpellData spellData = spellContainer.getActiveSpells().get(0);
                    if (spellData != null && spellData.getSpell() != null) {
                        int cost = spellData.getSpell().getManaCost(spellData.getLevel());
                        return cost * size.get();
                    }
                }
            }
        }
        return 0;
    }

    public static int getChargedManaCount(ItemStack stack) {
        if (stack.getItem() instanceof AmmoBoxItem) {
            if (stack.hasTag() && stack.getOrCreateTag().contains("Mana")) {
                return stack.getOrCreateTag().getInt("Mana");
            }
        }
        return 0;
    }
}
