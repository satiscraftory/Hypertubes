package net.ugi.hypertubes.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, net.ugi.hypertubes.HyperTubes.MOD_ID);

    public static final Supplier<EntityType<HypertubeEntity>> HYPERTUBE_ENTITY =
            ENTITY_TYPES.register("hypertube_entity", () -> EntityType.Builder.of(HypertubeEntity::new, MobCategory.MISC)
                    .passengerAttachments(new Vec3(0,0,0)).sized(0.1f, 0.1f).build("hypertube_entity"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
