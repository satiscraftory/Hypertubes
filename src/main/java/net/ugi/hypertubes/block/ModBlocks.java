package net.ugi.hypertubes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ugi.hypertubes.block.custom.HypertubeSupportBlock;
import net.ugi.hypertubes.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    private static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return false;
    }

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(net.ugi.hypertubes.HyperTubes.MOD_ID);

    public static final DeferredBlock<Block> HYPERTUBE_SUPPORT = registerBlock("hypertube_support",
            () -> new HypertubeSupportBlock(
                    BlockBehaviour.Properties.of().strength(2f).noLootTable().noOcclusion()));

    public static final DeferredBlock<Block> HYPERTUBE= registerBlock("hypertube",
            () ->   new TransparentBlock(
            BlockBehaviour.Properties.of()
                .strength(2f).sound(SoundType.GLASS).noOcclusion().isSuffocating(ModBlocks::never).isViewBlocking(ModBlocks::never).isValidSpawn(Blocks::never)
            ));

    public static final DeferredBlock<Block> HYPERTUBE_ENTRANCE = registerBlock("hypertube_entrance",
            () -> new TransparentBlock(
                    BlockBehaviour.Properties.of()
            ));

    public static final DeferredBlock<Block> HYPERTUBE_BOOSTER = registerBlock("hypertube_booster",
            () -> new TransparentBlock(
                    BlockBehaviour.Properties.of()
            ));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}