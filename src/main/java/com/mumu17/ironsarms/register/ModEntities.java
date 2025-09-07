package com.mumu17.ironsarms.register;

import com.mumu17.ironsarms.IronsArms;
import com.mumu17.ironsarms.utils.DummyMagicProjectile;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = IronsArms.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, IronsArms.MODID);

    public static final RegistryObject<EntityType<DummyMagicProjectile>> DUMMY_MAGIC_PROJECTILE =
            ENTITY_TYPES.register("dummy_magic_projectile", () ->
                    EntityType.Builder.<DummyMagicProjectile>of(DummyMagicProjectile::new, MobCategory.MISC)
                            .sized(0.0F, 0.0F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("dummy_magic_projectile")
            );

    @SubscribeEvent
    public static void onEntityAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DUMMY_MAGIC_PROJECTILE.get(), AbstractSpellCastingMob.createMobAttributes().build());
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
