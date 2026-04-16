package com.mumu17.ironsarms.item;

import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.network.RequestSyncChargedManaMessage;
import com.mumu17.ironsarms.utils.GunTags;
import com.tacz.guns.api.item.IGun;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = IronsArms.MODID)
public class GunTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof IGun)) return;

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("InscribedSpell")) {
            CompoundTag spellTag = tag.getCompound("InscribedSpell");
            String spellId = spellTag.getString("SpellID");
            int level = spellTag.getInt("Level");
//            double percentage = Math.ceil((double) (GunTags.getMana(stack) / RequestSyncChargedManaMessage.MAX_MANA) * 1000d) / 10d;

            AbstractSpell spell = SpellRegistry.getSpell(spellId);
            if (spell != null && spell != SpellRegistry.none()) {
                List<Component> tooltip = event.getToolTip();

                tooltip.add(Component.literal(""));

                tooltip.add(Component.translatable("tooltip."+IronsArms.MODID+".inscribed_title")
                        .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));

                tooltip.add(Component.translatable("tooltip."+IronsArms.MODID+".spell_name")
                        .append(Component.literal(": "))
                        .append(spell.getDisplayName(event.getEntity()).withStyle(ChatFormatting.GREEN)));

                tooltip.add(Component.translatable("tooltip."+IronsArms.MODID+".spell_level")
                        .append(Component.literal(": "))
                        .append(Component.literal(String.valueOf(level)).withStyle(ChatFormatting.GOLD)));

                tooltip.add(Component.translatable("tooltip."+IronsArms.MODID+".mana")
                        .append(Component.literal(": "))
                        .append(Component.literal(String.valueOf(GunTags.getMana(stack))).withStyle(ChatFormatting.AQUA)));
            }
        }
    }
}
