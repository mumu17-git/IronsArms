package com.mumu17.ironsarms.mixin.tacz;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mumu17.armslib.util.GunItemNbt;
import com.mumu17.ironsarms.utils.BulletTags;
import com.mumu17.ironsarms.utils.DummyMagicProjectile;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.TacHitResult;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = EntityKineticBullet.class)
public class EntityKineticBulletMixin {

    @Unique
    private HitResult hitResult;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/entity/EntityKineticBullet;onBulletTick()V", remap = false))
    public void tick(CallbackInfo ci) {
        this.initDummyMagicProjectile();
    }

    /*@Inject(method = "onHitEntity", at = @At(value = "HEAD"), remap = false)
    public void onHitEntity(TacHitResult result, Vec3 startVec, Vec3 endVec, CallbackInfo ci) {
        EntityKineticBullet projectileEntity = ((EntityKineticBullet)(Object)this);
        LivingEntity attacker = projectileEntity.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null;
        if (attacker instanceof Player player) {
            ItemStack mainhand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (mainhand.getItem() instanceof IGun iGun) {
                GunItemNbt access = (GunItemNbt) iGun;
                boolean isIronsMode = access.getIsIronsMode(mainhand);
                if (isIronsMode) {
                    IronsArmsProjectileData.setProjectileData(projectileEntity, result.getEntity(), null, ArsCuriosLivingEntity.getPlayerExtendedHand(player));
                }
            }
        }
    }*/

    @Inject(id = "onHitEntity", method = "onHitEntity", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/entity/EntityKineticBullet$MaybeMultipartEntity;core()Lnet/minecraft/world/entity/Entity;", ordinal = 2), remap = false)
    public void onHitEntity$core(TacHitResult result, Vec3 startVec, Vec3 endVec, CallbackInfo ci, @Local(name = "attacker") LivingEntity attacker) {
        EntityKineticBullet projectileEntity = ((EntityKineticBullet)(Object)this);
        if (projectileEntity != null && attacker instanceof Player player) {
            ItemStack mainhand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (mainhand.getItem() instanceof IGun iGun) {
                GunItemNbt access = (GunItemNbt) iGun;
                boolean isIronsMode = access.getIsIronsMode(mainhand);
                if (isIronsMode) {
                    if (result.getEntity() != null && result.getEntity() instanceof LivingEntity && result.getEntity().isAlive() && !(result.getEntity() instanceof DummyMagicProjectile)) {
                        BulletTags.setBulletIsHit(projectileEntity, true);
                        this.hitResult = result;
                        this.initDummyMagicProjectile();
                        /*if (!projectileEntity.getPersistentData().contains(BulletTags.DUMMY_MAGIC_PROJECTILE_UUID)) {
                            IronsArms.LOGGER.debug("Creating DummyMagicProjectile for entity: {}", result.getEntity().getUUID());
                            Level level = projectileEntity.level();
                            DummyMagicProjectile dummy = new DummyMagicProjectile(level, (LivingEntity) projectileEntity.getOwner(), result.getEntity(), null);
                            projectileEntity.getPersistentData().putUUID(BulletTags.DUMMY_MAGIC_PROJECTILE_UUID, dummy.getUUID());
                            level.addFreshEntity(dummy);
                        }*/
                    }
                }
            }
        }
    }


    /*@Inject(method = "onHitEntity", at = @At(value = "FIELD", target = "Lcom/tacz/guns/entity/EntityKineticBullet;explosion:Z", shift = At.Shift.AFTER), remap = false)
    public void explosionDamage(TacHitResult result, Vec3 startVec, Vec3 endVec, CallbackInfo ci) {
        if (explosion) {
            EntityKineticBullet projectileEntity = ((EntityKineticBullet)(Object)this);
            LivingEntity attacker = projectileEntity.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null;
            if (attacker instanceof Player player) {
                ItemStack gunItem = player.getItemInHand(InteractionHand.MAIN_HAND);
                IronsArmsProjectileData IronsArmsProjectileData = IronsArmsProjectileData.getProjectileData(projectileEntity);
                if (gunItem.getItem() instanceof IGun iGun && IronsArmsProjectileData != null) {
                    Entity entity = IronsArmsProjectileData.getTargetEntity();
                    ExtendedHand hand = IronsArmsProjectileData.getHand();
                    if (hand.isAmmoBox()) {
                        if (entity != null) {
                            GunItemNbt access = (GunItemNbt) iGun;
                            boolean isIronsMode = access.getIsIronsMode(gunItem);
                            if (isIronsMode) {
                                explosionDamage *= IronsArmsConfig.COMMON.damageMultiplier.get();
                            }
                        }
                    }
                }
            }
        }
    }*/

    @ModifyReturnValue(method = "getDamage", at = @At(value = "RETURN"), remap = false)
    public float getDamage(float original) {
        EntityKineticBullet projectileEntity = ((EntityKineticBullet)(Object)this);
        if (projectileEntity != null && projectileEntity.getOwner() instanceof LivingEntity livingEntity) {
            ItemStack mainhand = livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
            if (mainhand.getItem() instanceof IGun iGun) {
                GunItemNbt access = (GunItemNbt) iGun;
                boolean isIronsMode = access.getIsIronsMode(mainhand);
                if (isIronsMode) {
                    access.setGunDamage(livingEntity.getMainHandItem(), original);
                    return (float) (original * 0.0F);
                }
            }
        }

        return original;
    }

    /*@Inject(method = "onHitBlock", at = @At(value = "HEAD"), remap = false)
    public void onHitBlock_HEAD(BlockHitResult result, Vec3 startVec, Vec3 endVec, CallbackInfo ci) {
        EntityKineticBullet projectileEntity = ((EntityKineticBullet)(Object)this);
        LivingEntity attacker = projectileEntity.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null;
        if (attacker instanceof Player player) {
            ItemStack mainhand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (mainhand.getItem() instanceof IGun iGun) {
                GunItemNbt access = (GunItemNbt) iGun;
                boolean isIronsMode = access.getIsIronsMode(mainhand);
                if (isIronsMode) {
                    IronsArmsProjectileData.setProjectileData(projectileEntity, null, result, ArsCuriosLivingEntity.getPlayerExtendedHand(player));
                }
            }
        }
    }*/

    @Inject(method = "onHitBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V"))
    public void onHitBlock(BlockHitResult result, Vec3 startVec, Vec3 endVec, CallbackInfo ci) {
        EntityKineticBullet projectileEntity = ((EntityKineticBullet)(Object)this);
        LivingEntity attacker = projectileEntity.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null;
        if (attacker instanceof Player player) {
            ItemStack mainhand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (mainhand.getItem() instanceof IGun iGun) {
                GunItemNbt access = (GunItemNbt) iGun;
                boolean isIronsMode = access.getIsIronsMode(mainhand);
                if (isIronsMode) {
                    if (result != null) {
                        BulletTags.setBulletIsHit(projectileEntity, true);
                        this.hitResult = result;
                        this.initDummyMagicProjectile();
                        /*if (!projectileEntity.getPersistentData().contains(BulletTags.DUMMY_MAGIC_PROJECTILE_UUID)) {
                            IronsArms.LOGGER.debug("Creating DummyMagicProjectile for block: {}", result.getBlockPos().toShortString());
                            Level level = projectileEntity.level();
                            DummyMagicProjectile dummy = new DummyMagicProjectile(level, (LivingEntity) projectileEntity.getOwner(), null, result.getBlockPos().getCenter());
                            projectileEntity.getPersistentData().putUUID(BulletTags.DUMMY_MAGIC_PROJECTILE_UUID, dummy.getUUID());
                            level.addFreshEntity(dummy);
                        }*/
                    }
                }
            }
        }
    }

    @Unique
    private void initDummyMagicProjectile() {
        EntityKineticBullet projectileEntity = ((EntityKineticBullet)(Object)this);
        if (projectileEntity != null) {
            CompoundTag bulletData = projectileEntity.getPersistentData();
            if (bulletData != null) {
                boolean isHit = BulletTags.getBulletIsHit(projectileEntity);
                if (bulletData.contains(BulletTags.DUMMY_MAGIC_PROJECTILE_UUID)) {
                    MinecraftServer server = projectileEntity.getServer();
                    ResourceKey<Level> dimension = projectileEntity.level().dimension();
                    if (server != null && dimension != null) {
                        Entity entity = Objects.requireNonNull(server.getLevel(dimension)).getEntity(bulletData.getUUID(BulletTags.DUMMY_MAGIC_PROJECTILE_UUID));
                        if (entity instanceof DummyMagicProjectile dummy) {
                            if (hitResult != null && !hitResult.getType().equals(HitResult.Type.MISS)) {
                                if (hitResult instanceof EntityHitResult entityHitResult) {
                                    HitResult newHitResult = new EntityHitResult(entityHitResult.getEntity(), entityHitResult.getLocation());
                                    dummy.setHitResult(newHitResult);
                                    dummy.setTarget((LivingEntity) entityHitResult.getEntity());
                                } else if (hitResult instanceof BlockHitResult blockHitResult) {
                                    HitResult newHitResult = new BlockHitResult(blockHitResult.getLocation(), blockHitResult.getDirection(), blockHitResult.getBlockPos(), blockHitResult.isInside());
                                    dummy.setHitResult(newHitResult);
                                    dummy.setHitBlockPos(blockHitResult.getBlockPos().getCenter());
                                }
                                dummy.setIsHit(true);
                            }
                        }
                    }
                } else {
                    Level level = projectileEntity.level();
                    DummyMagicProjectile dummy = new DummyMagicProjectile(level, (LivingEntity) projectileEntity.getOwner(), projectileEntity, isHit);
                    dummy.setPos(projectileEntity.position());
                    BulletTags.setDummyUUIDToBullet(projectileEntity, dummy.getUUID());
                    if (hitResult != null && !hitResult.getType().equals(HitResult.Type.MISS) && (hitResult.getType().equals(HitResult.Type.ENTITY) || hitResult.getType().equals(HitResult.Type.BLOCK))) {
                        if (hitResult instanceof EntityHitResult entityHitResult) {
                            HitResult newHitResult = new EntityHitResult(entityHitResult.getEntity(), entityHitResult.getLocation());
                            dummy.setHitResult(newHitResult);
                            dummy.setTarget((LivingEntity) entityHitResult.getEntity());
                        } else if (hitResult instanceof BlockHitResult blockHitResult) {
                            HitResult newHitResult = new BlockHitResult(blockHitResult.getLocation(), blockHitResult.getDirection(), blockHitResult.getBlockPos(), blockHitResult.isInside());
                            dummy.setHitResult(newHitResult);
                            dummy.setHitBlockPos(blockHitResult.getLocation());
                        }
                    } else {
                        BulletTags.removeDummyUUIDFromBullet(projectileEntity);
                    }
                    level.addFreshEntity(dummy);
                }
            }
        }
    }
}
