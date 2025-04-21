package net.ugi.hypertubes.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ugi.hypertubes.block.ModBlocks;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, net.ugi.hypertubes.HyperTubes.MOD_ID);

    public static final Supplier<BlockEntityType<HypertubeSupportBlockEntity>> HYPERTUBE_SUPPORT_BE =
            BLOCK_ENTITIES.register("hypertube_support_be", () -> BlockEntityType.Builder.of(
                    HypertubeSupportBlockEntity::new, ModBlocks.HYPERTUBE_SUPPORT.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
