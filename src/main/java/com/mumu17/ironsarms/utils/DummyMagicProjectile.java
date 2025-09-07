package com.mumu17.ironsarms.utils;

import com.mumu17.arscurios.util.ArsCuriosInventoryHelper;
import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.register.ModEntities;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.item.AmmoBoxItem;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class DummyMagicProjectile extends AbstractSpellCastingMob {

    public int remainTicks = 60;
    private LivingEntity owner;
    private boolean isHit;
    private HitResult hitResult;
    private EntityKineticBullet bullet;
    private LivingEntity target = null;
    private Vec3 hitBlockPos;

    public DummyMagicProjectile(EntityType<? extends DummyMagicProjectile> type, Level level) {
        super(type, level);
    }

    public DummyMagicProjectile(Level level, LivingEntity owner, @NotNull EntityKineticBullet bullet, boolean isHit) {
        super(ModEntities.DUMMY_MAGIC_PROJECTILE.get(), level);
        this.owner = owner;
        this.bullet = bullet;
        this.isHit = isHit;
        BulletTags.setBulletUUIDToLivingEntity(this, bullet.getUUID());
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.updateDummyStatus();
        this.triggerSpell();
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.updateDummyStatus();

        remainTicks--;
        if (remainTicks <= 0) {
            if (this.isCasting())
                this.cancelCast();
            this.discard();
        }
    }

    private void updateDummyStatus() {
        if (this.getIsHit()) {
            LivingEntity _target = this.getTarget();
            if (getHitResult() != null && getHitResult().getType().equals(HitResult.Type.ENTITY) && _target != null && _target != this.getOwner()) {
                this.setPos(_target.getX(), _target.getY() + _target.getBbHeight() + 2.0F, _target.getZ());
            }

            float rx = this.getXRot();
            float ry = this.getYRot();
            if (getHitResult().getType().equals(HitResult.Type.BLOCK)) {
                if (getHitBlockPos() != null) {
                    Vec3 pos = getHitResult().getLocation();
                    BlockHitResult blockHitResult = (BlockHitResult) getHitResult();
                    Direction face = blockHitResult.getDirection();
                    Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
                    this.setPos(pos);
                    rx = (float) -(Math.toDegrees(Math.asin(normal.y)));
                    ry = (float) (Math.toDegrees(Math.atan2(-normal.x, normal.z)));
                    IronsArms.LOGGER.debug("SUCCESS HitBlockPos: {}, DummyLocation: {}", getHitResult().getLocation(), this.position());
                    setHitBlockPos(new Vec3(0.0D,0.0D,0.0D));
                } else {
                    IronsArms.LOGGER.warn("WARN HitBlockPos: {}, DummyLocation: {}", getHitResult().getLocation(), this.position());
                }

            } else {
                rx = 90.0F;
            }
            this.setXRot(rx);
            this.setYRot(ry);
            this.xRotO = rx;
            this.yRotO = ry;
        } else {
            EntityKineticBullet _bullet = this.getBullet();
            if (_bullet != null) {
                this.setPos(_bullet.position());
                this.setXRot(_bullet.getXRot());
                this.setYRot(_bullet.getYRot());
                this.xRotO = _bullet.getXRot();
                this.yRotO = _bullet.getYRot();
            }
        }
    }

    @Override
    public void setTarget(LivingEntity target) {
        if (target == null) {
            return;
        }
        this.target = target;
    }

    @Override
    public LivingEntity getTarget() {
        return this.target;
    }


    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    protected void triggerSpell() {
        if (!level().isClientSide) {
            if (this.getOwner() == null) {
                this.discard();
                return;
            }

            SpellSelectionManager spellSelectionManager = new SpellSelectionManager((Player) this.getOwner());
            AbstractSpell spell = spellSelectionManager.getSelectedSpellData().getSpell();
            int spellLevel = spellSelectionManager.getSelectedSpellData().getLevel();

            if (this.getTarget() != null || (this.getIsHit() && getHitResult().getType().equals(HitResult.Type.BLOCK)) && getHitBlockPos().equals(new Vec3(0.0D,0.0D,0.0D))) {
                this.remainTicks = spell.getEffectiveCastTime(spellLevel, this);
                if (this.getOwner() instanceof Player player) {
                    SpellSelection spellSelection = spellSelectionManager.getCurrentSelection();
                    if (spellSelection != null) {
                        String spellSlot = spellSelection.equipmentSlot;
                        var stack = ArsCuriosInventoryHelper.getCuriosInventoryItem(player, spellSlot);
                        if (!stack.isEmpty() && stack.getItem() instanceof AmmoBoxItem) {
                            float removeManaCount = spell.getManaCost(spellLevel);
                            int currentManaCount = stack.getOrCreateTag().getInt("Mana");
                            if (currentManaCount - removeManaCount >= 0.0F) {
                                stack.getOrCreateTag().putInt("Mana", (int) (currentManaCount - removeManaCount));
                                this.initiateCastSpell(spell, spellLevel);
                            }
                        }
                    }
                }
            }
        }
    }

    protected LivingEntity getOwner() {
        return this.owner;
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }

    @Override
    public @NotNull ItemStack getItemBySlot(@Nullable EquipmentSlot p_21127_) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@Nullable EquipmentSlot p_21036_, @Nullable ItemStack p_21037_) {

    }

    public boolean getIsHit() {
        return this.isHit;
    }

    public void setIsHit(boolean isHit) {
        this.isHit = isHit;
    }

    public void setHitResult(HitResult hitResult) {
        if (hitResult == null || hitResult.getType().equals(HitResult.Type.MISS)) {
            return;
        }
        this.hitResult = hitResult;
    }

    public HitResult getHitResult() {
        return this.hitResult;
    }

    public EntityKineticBullet getBullet() {
        return this.bullet;
    }

    public void setHitBlockPos(Vec3 pos) {
        if (pos == null) {
            return;
        }
        this.hitBlockPos = pos;
    }

    public Vec3 getHitBlockPos() {
        return this.hitBlockPos;
    }
}
