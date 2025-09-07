package com.mumu17.ironsarms.utils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class BulletTags {
    public static final String BULLET_UUID = "ironsarms:bullet_uuid",
            BULLET_TARGET_UUID = "ironsarms:bullet_target_uuid",
            BULLET_IS_HIT = "ironsarms:bullet_is_hit",
            DUMMY_MAGIC_PROJECTILE_UUID = "ironsarms:dummy_magic_projectile_uuid";

    public static void setBulletUUIDToLivingEntity(LivingEntity entity, UUID uuid) {
        if (entity != null && uuid != null) {
            entity.getPersistentData().putUUID(BULLET_UUID, uuid);
        }
    }

    public static UUID getBulletUUIDFromLivingEntity(LivingEntity entity) {
        if (entity != null) {
            if (entity.getPersistentData().contains(BULLET_UUID)) {
                return entity.getPersistentData().getUUID(BULLET_UUID);
            }
        }
        return null;
    }

    public static Entity getEntityFromUUID(Entity entity, UUID uuid) {
        Level level = entity.getCommandSenderWorld();
        ResourceKey<Level> dimension = level.dimension();
        MinecraftServer server = level.getServer();
        if (server != null) {
            ServerLevel serverLevel = server.getLevel(dimension);
            if (serverLevel != null && uuid != null) {
                return serverLevel.getEntity(uuid);
            }
        }
        return null;
    }

    public static Entity getEntityBulletFromLivingEntity(LivingEntity livingEntity) {
        return getEntityFromUUID(livingEntity, getBulletUUIDFromLivingEntity(livingEntity));
    }

    public static void setTargetEntityToBullet(Entity bullet, Entity target) {
        if (bullet != null && target != null) {
            bullet.getPersistentData().putUUID(BULLET_TARGET_UUID, target.getUUID());
        }
    }

    public static Entity getTargetEntityFromBullet(Entity bullet) {
        if (bullet != null && bullet.getPersistentData().contains(BULLET_TARGET_UUID)) {
            UUID uuid = bullet.getPersistentData().getUUID(BULLET_TARGET_UUID);
            return getEntityFromUUID(bullet, uuid);
        }
        return null;
    }

    public static void setBulletIsHit(Entity bullet, boolean isHit) {
        if (bullet != null) {
            bullet.getPersistentData().putBoolean(BULLET_IS_HIT, isHit);
        }
    }

    public static boolean getBulletIsHit(Entity bullet) {
        if (bullet != null && bullet.getPersistentData().contains(BULLET_IS_HIT)) {
            return bullet.getPersistentData().getBoolean(BULLET_IS_HIT);
        }
        return false;
    }

    public static void setDummyUUIDToBullet(Entity bullet, UUID dummyUUID) {
        if (bullet != null && dummyUUID != null) {
            bullet.getPersistentData().putUUID(DUMMY_MAGIC_PROJECTILE_UUID, dummyUUID);
        }
    }

    public static void removeDummyUUIDFromBullet(Entity bullet) {
        if (bullet != null && bullet.getPersistentData().contains(DUMMY_MAGIC_PROJECTILE_UUID)) {
            bullet.getPersistentData().remove(DUMMY_MAGIC_PROJECTILE_UUID);
        }
    }

    public static UUID getDummyUUIDFromBullet(Entity bullet) {
        if (bullet != null && bullet.getPersistentData().contains(DUMMY_MAGIC_PROJECTILE_UUID)) {
            return bullet.getPersistentData().getUUID(DUMMY_MAGIC_PROJECTILE_UUID);
        }
        return null;
    }
}
