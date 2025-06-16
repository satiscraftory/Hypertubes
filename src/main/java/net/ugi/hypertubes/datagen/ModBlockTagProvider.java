package net.ugi.hypertubes.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.ugi.hypertubes.block.ModBlocks;
import net.ugi.hypertubes.util.ModTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, net.ugi.hypertubes.HyperTubes.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.HYPERTUBE_SUPPORT.get());
/*
                .add(ModBlocks.BISMUTH_ORE.get())
                .add(ModBlocks.BISMUTH_DEEPSLATE_ORE.get());
*/

/*        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.BISMUTH_DEEPSLATE_ORE.get());*/

        tag(ModTags.Blocks.DONT_OBSTRUCT_HYPERTUBE)
                .addTag(BlockTags.REPLACEABLE)
                .addTag(BlockTags.FLOWERS)
                .addTag(BlockTags.CORALS)
                .add(Blocks.KELP)
                .add(Blocks.MOSS_CARPET)
                .add(Blocks.TWISTING_VINES)
                .add(Blocks.WEEPING_VINES);


    }
}