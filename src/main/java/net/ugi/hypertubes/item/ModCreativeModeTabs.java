package net.ugi.hypertubes.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ugi.hypertubes.HyperTubes;
import net.ugi.hypertubes.block.ModBlocks;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HyperTubes.MOD_ID);


    public static final Supplier<CreativeModeTab> hypertubes = CREATIVE_MODE_TAB.register("hypertubes_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.HYPERTUBE_SUPPORT))
                    .title(Component.translatable("creativetab.hypertubes.all"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.HYPERTUBE_SUPPORT);
                        output.accept(ModItems.HYPERTUBE_ENTRANCE);
                        output.accept(ModItems.HYPERTUBE_BOOSTER);
                        output.accept(ModBlocks.HYPERTUBE);
                        output.accept(ModItems.HYPERTUBE_PLACER);
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}