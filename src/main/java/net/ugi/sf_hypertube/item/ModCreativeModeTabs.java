package net.ugi.sf_hypertube.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ugi.sf_hypertube.SfHyperTube;
import net.ugi.sf_hypertube.block.ModBlocks;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SfHyperTube.MOD_ID);


    public static final Supplier<CreativeModeTab> SF_HYPERTUBE = CREATIVE_MODE_TAB.register("sf_hypertube_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.MAGIC_BLOCK))
                    .title(Component.translatable("creativetab.sf_hypertube.all"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.MAGIC_BLOCK);
                        output.accept(ModItems.BISMUTH);
                        output.accept(ModItems.RAW_BISMUTH);
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}