package com.mumu17.ironsarms.event;

import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.utils.GunTags;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.entity.EntityKineticBullet;
import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.spells.ender.StarfallSpell;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = IronsArms.MODID)
public class IronsArmsBulletEvents {
    private static final Map<UUID, CompoundTag> pendingSpells = new ConcurrentHashMap<>();
    private static Boolean calamityRingExists = null;
    private static Item ringItemInstance = null;

    private static final Map<UUID, Integer> playersWithCastingSpells = new HashMap<>(Map.of());
    private static final List<UUID> castingPlayers = new ArrayList<>(List.of());

    @SubscribeEvent
    public static void onGunFire(GunFireEvent event) {
        if (event.getLogicalSide().isClient()) return;
        if (!(event.getShooter() instanceof Player player)) return;

        ItemStack gunStack = event.getGunItemStack();
        CompoundTag rootTag = gunStack.getTag();

        AbstractSpell spell = null;
        int level = 1;

        if (rootTag != null && rootTag.contains("InscribedSpell")) {
            CompoundTag inscribedSpell = rootTag.getCompound("InscribedSpell");
            spell = SpellRegistry.getSpell(inscribedSpell.getString("SpellID"));
            level = inscribedSpell.getInt("Level");
        } else if (ISpellContainer.isSpellContainer(gunStack)) {
            ISpellContainer container = ISpellContainer.get(gunStack);
            if (!container.isEmpty()) {
                SpellData spellData = container.getSpellAtIndex(0);
                spell = spellData.getSpell();
                level = spellData.getLevel();
            }
        }

        if (spell == null || spell == SpellRegistry.none()) return;

        ModifySpellLevelEvent levelEvent = new ModifySpellLevelEvent(spell, player, level, level);
        MinecraftForge.EVENT_BUS.post(levelEvent);
        level = levelEvent.getLevel();

        MagicData magicData = MagicData.getPlayerMagicData(player);
        String spellId = spell.getSpellResource().toString();
        boolean isRecasting = magicData.getPlayerRecasts().hasRecastForSpell(spellId);
        boolean hasCalamityRing = checkForCalamityRing(player);

        if (!player.isCreative() && !hasCalamityRing && !isRecasting) {
            float manaCost = spell.getManaCost(level);
            if (GunTags.getMana(gunStack) < manaCost) {
                return;
            }
            GunTags.addMana(gunStack, (int) -manaCost);
        }

        CompoundTag dataToPass = new CompoundTag();
        dataToPass.putString("SpellID", spell.getSpellId());
        dataToPass.putInt("Level", level);
        pendingSpells.put(player.getUUID(), dataToPass);
        castingPlayers.add(player.getUUID());
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (event.getEntity() instanceof EntityKineticBullet bullet) {
            if (bullet.getOwner() instanceof Player player) {
                if (!castingPlayers.contains(player.getUUID())) return;
                CompoundTag reservedData = pendingSpells.get(player.getUUID());
                if (reservedData != null) {
                    bullet.getPersistentData().put("MagicData", reservedData);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            pendingSpells.clear();
            playersWithCastingSpells.forEach((key, value) -> {
                if (value > 0) {
                    event.getServer().getAllLevels().forEach(level -> {
                        if (level.getEntity(key) instanceof ServerPlayer player) {
                            setManaFromGun(player, player.getMainHandItem());
                        }
                    });
                    playersWithCastingSpells.put(key, value - 1);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityHurtPre(EntityHurtByGunEvent.Pre event) {
        if (!(event.getAttacker() instanceof Player player)) return;
        if (!(event.getHurtEntity() instanceof LivingEntity target)) return;
        if (!castingPlayers.contains(player.getUUID())) return;
        castingPlayers.remove(player.getUUID());

        // getHitPos()を解決できないため、弾丸の現在位置を使用
        processImpact(player, target, target.position(), event.getBullet());
    }

    @SubscribeEvent
    public static void onAmmoHitBlock(AmmoHitBlockEvent event) {
        if (!(event.getAmmo().getOwner() instanceof Player player)) return;
        if (!castingPlayers.contains(player.getUUID())) return;
        castingPlayers.remove(player.getUUID());

        Vec3 hitPos = event.getHitResult().getLocation();

        ArmorStand dummy = new ArmorStand(EntityType.ARMOR_STAND, player.level());
        dummy.setPos(hitPos.x, hitPos.y, hitPos.z);
        dummy.setInvisible(true);
        dummy.setInvulnerable(true);
        dummy.setNoGravity(true);

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Marker", true);
        tag.putBoolean("Small", true);
        dummy.readAdditionalSaveData(tag);

        player.level().addFreshEntity(dummy);

        processImpact(player, dummy, hitPos, event.getAmmo());

        dummy.discard();
    }

    private static void processImpact(Player player, LivingEntity target, Vec3 hitPos, Entity bullet) {
        if (player.level().isClientSide) return;
        if (!bullet.getPersistentData().contains("MagicData")) return;
        CompoundTag magicTag = bullet.getPersistentData().getCompound("MagicData");

        AbstractSpell spell = SpellRegistry.getSpell(magicTag.getString("SpellID"));
        int level = magicTag.getInt("Level");
        if (spell == null || spell == SpellRegistry.none()) return;

        MagicData magicData = MagicData.getPlayerMagicData(player);
        String path = spell.getSpellResource().getPath();

        if (path.contains("starfall")) {
            magicData.setAdditionalCastData(new StarfallSpell.StarfallCastData(hitPos));
        } else if (path.contains("teleport")) {
            magicData.setAdditionalCastData(new TeleportSpell.TeleportData(hitPos));
        } else if (target != null) {
            magicData.setAdditionalCastData(new TargetEntityCastData(target));
        }

        Vec3 originalPos = player.position();
        float originalYRot = player.getYRot();
        float originalXRot = player.getXRot();

        try {
            Vec3 bulletMotion = bullet.getDeltaMovement().normalize();
            double d0 = bulletMotion.x;
            double d1 = bulletMotion.y;
            double d2 = bulletMotion.z;
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            float yaw = (float)(Math.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
            float pitch = (float)(-(Math.atan2(d1, d3) * (180D / Math.PI)));

            double offsetDistance = 2.2;
            Vec3 pseudoSpawnPos = hitPos.subtract(bulletMotion.scale(offsetDistance));

            player.setPos(pseudoSpawnPos.x, pseudoSpawnPos.y, pseudoSpawnPos.z);
            player.setYRot(yaw);
            player.setXRot(pitch);
            player.setYHeadRot(yaw);

            // setInitiatedCastSpellが解決できないため、initiateCast内で管理するか、
            // バージョンに応じて直接詠唱開始をシミュレート
            if (spell.getCastType() == CastType.CONTINUOUS) {
                int duration = spell.getCastTime(level);
                if (duration <= 0) duration = 20;
                playersWithCastingSpells.put(player.getUUID(), duration);
                setManaFromGun(player, player.getMainHandItem());
                magicData.initiateCast(spell, level, duration, CastSource.SWORD, spell.getSpellId());
            }

            executeSpell(spell, level, player, magicData);

        } finally {
            player.setPos(originalPos.x, originalPos.y, originalPos.z);
            player.setYRot(originalYRot);
            player.setXRot(originalXRot);
            player.setYHeadRot(originalYRot);

            updateRecastSystems(spell, magicData);

            if (spell.getCastType() != CastType.CONTINUOUS) {
                magicData.resetCastingState();
                magicData.setAdditionalCastData(null);
            }
        }
    }

    private static void updateRecastSystems(AbstractSpell spell, MagicData magicData) {
        String spellIdString = spell.getSpellResource().toString();
        if (magicData.getPlayerRecasts().hasRecastForSpell(spellIdString)) {
            RecastInstance oldInstance = magicData.getPlayerRecasts().getRecastInstance(spellIdString);
            int nextRemaining = oldInstance.getRemainingRecasts() - 1;

            if (nextRemaining >= 0) {
                RecastInstance newInstance = new RecastInstance(
                        oldInstance.getSpellId(),
                        oldInstance.getSpellLevel(),
                        nextRemaining + 1,
                        oldInstance.getTicksToLive(),
                        oldInstance.getCastSource(),
                        oldInstance.getCastData()
                );
                magicData.getPlayerRecasts().addRecast(newInstance, magicData);
            } else {
                magicData.getPlayerRecasts().removeRecast(oldInstance, io.redspace.ironsspellbooks.capabilities.magic.RecastResult.USED_ALL_RECASTS);
            }
        }
    }

    private static void executeSpell(AbstractSpell spell, int level, Player player, MagicData magicData) {
        try {
            // 第3引数(LivingEntity)にplayerを渡すことで、弾丸の主人が詠唱したことにする
            spell.onCast(player.level(), level, player, CastSource.SWORD, magicData);
        } catch (Exception e) {
            IronsArms.LOGGER.error("Failed to cast spell: " + spell.getSpellResource().toString(), e);
        }
    }

    private static boolean checkForCalamityRing(Player player) {
        try {
            if (calamityRingExists == null) initializeCalamityRingDetection();
            if (!calamityRingExists || ringItemInstance == null) return false;
            Class<?> curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Object curiosHelper = curiosApiClass.getMethod("getCuriosHelper").invoke(null);
            java.lang.reflect.Method findFirstCurio = curiosHelper.getClass().getMethod("findFirstCurio", LivingEntity.class, Item.class);
            Object result = findFirstCurio.invoke(curiosHelper, player, ringItemInstance);
            if (result instanceof java.util.Optional<?> optional) return optional.isPresent();
        } catch (Exception e) { return false; }
        return false;
    }

    private static void initializeCalamityRingDetection() {
        try {
            Class<?> itemRegistryClass = Class.forName("inovation_and_control.inovation_and_control.registry.ItemRegistry");
            java.lang.reflect.Field ringField = itemRegistryClass.getDeclaredField("RING_OF_CALAMITY");
            ringField.setAccessible(true);
            Object registryObject = ringField.get(null);
            java.lang.reflect.Method getMethod = registryObject.getClass().getMethod("get");
            Object item = getMethod.invoke(registryObject);
            if (item instanceof Item castedItem) {
                ringItemInstance = castedItem;
                calamityRingExists = true;
            }
        } catch (Exception e) { calamityRingExists = false; }
    }

    private static void setManaFromGun(Player player, ItemStack gunStack) {
        MagicData magicData = MagicData.getPlayerMagicData(player);
        float maxMana = (float) player.getAttributeValue((Attribute)AttributeRegistry.MAX_MANA.get());
        float addedMana = maxMana - magicData.getMana();
        magicData.addMana(addedMana);
        GunTags.addMana(gunStack, (int) -addedMana);
    }
}
