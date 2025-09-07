package com.mumu17.ironsarms.mixin.irons_spellbooks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mumu17.ironsarms.utils.DummyMagicProjectile;
import com.tacz.guns.api.item.IAmmoBox;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Utils.class)
public class UtilsMixin {

    @ModifyExpressionValue(method = "canImbue", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", ordinal = 1), remap = false)
    private static boolean canImbue$fixSetContains(boolean original, @Local(argsOnly = true) ItemStack stack) {
        if (stack.getItem() instanceof IAmmoBox) {
            return !isAmmoBoxArsMode(stack);
        }
        return original;
    }

    @Unique
    private static boolean isAmmoBoxArsMode(ItemStack ammoBox) {
        if (ammoBox.getItem() instanceof IAmmoBox) {
            if (ammoBox.hasTag() && ammoBox.getOrCreateTag().contains("ars_nouveau:reactive_caster")) {
                Tag ammoBoxTag = ammoBox.getOrCreateTag().get("ars_nouveau:reactive_caster");
                if (ammoBoxTag != null) {
                    if (ammoBox.getOrCreateTag().contains("Enchantments")) {
                        ListTag enchantments = ammoBox.getOrCreateTag().getList("Enchantments", Tag.TAG_COMPOUND);
                        if (!enchantments.isEmpty()) {
                            for (int i = 0; i < enchantments.size(); i++) {
                                CompoundTag enchantmentTag = enchantments.getCompound(i);
                                String enchantmentId = enchantmentTag.getString("id");
                                if ("ars_nouveau:reactive".equals(enchantmentId)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    @ModifyExpressionValue(method = "internalRaycastForEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"))
    private static BlockHitResult internalRaycastForEntity$fixBlockHitResult(BlockHitResult original, @Local(argsOnly = true) Entity originEntity) {
        if (originEntity instanceof DummyMagicProjectile dummyMagicProjectile) {
            HitResult hitResult = dummyMagicProjectile.getHitResult();
            if (hitResult != null) {
                if (hitResult.getType().equals(HitResult.Type.BLOCK)) {
                    return (BlockHitResult) hitResult;
                }
            }
        }
        return original;
    }

    @ModifyExpressionValue(method = "internalRaycastForEntity", at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/api/util/Utils;checkEntityIntersecting(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/HitResult;"), remap = false)
    private static HitResult internalRaycastForEntity$fixEntityHitResult(HitResult original, @Local(argsOnly = true) Entity originEntity) {
        if (originEntity instanceof DummyMagicProjectile dummyMagicProjectile) {
            HitResult hitResult = dummyMagicProjectile.getHitResult();
            if (hitResult != null) {
                if (hitResult.getType().equals(HitResult.Type.ENTITY)) {
                    return hitResult;
                }
            }
        }
        return original;
    }
}
