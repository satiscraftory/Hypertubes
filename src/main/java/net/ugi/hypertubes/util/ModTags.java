package net.ugi.hypertubes.util;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.ugi.hypertubes.HyperTubes;

public class ModTags {
    public static class Blocks {

        public static final TagKey<Block> DONT_OBSTRUCT_HYPERTUBE = createTag("dont_obstruct_hypertube");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(HyperTubes.MOD_ID, name));
        }
    }

    public static class Items {
        //public static final TagKey<Item> TRANSFORMABLE_ITEMS = createTag("transformable_items");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(HyperTubes.MOD_ID, name));
        }
    }
}
